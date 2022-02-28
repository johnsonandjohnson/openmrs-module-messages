package org.openmrs.module.messages.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang.StringUtils;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.VisitAttributeType;
import org.openmrs.VisitType;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.messages.api.constants.MessagesConstants;
import org.openmrs.module.messages.api.model.PatientTemplate;
import org.openmrs.module.messages.api.model.ScheduledExecutionContext;
import org.openmrs.module.messages.api.model.ScheduledService;
import org.openmrs.module.messages.api.model.ScheduledServiceGroup;
import org.openmrs.module.messages.api.model.types.ServiceStatus;
import org.openmrs.module.messages.api.service.MessagesDeliveryService;
import org.openmrs.module.messages.api.service.MessagingGroupService;
import org.openmrs.module.messages.api.service.PatientTemplateService;
import org.openmrs.module.messages.api.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static org.openmrs.module.messages.api.constants.ConfigConstants.PERSON_PHONE_ATTR;

@Api(value = "Message trigger actions", tags = "REST API for message trigger actions")
@Controller
@RequestMapping(value = "/messages/triggerMessage")
public class TriggerMessageController extends BaseRestController {

  private static final Logger LOGGER = LoggerFactory.getLogger(TriggerMessageController.class);

  private static final String VISIT_REMINDER_TEMPLATE_NAME = "Visit reminder";

  @Autowired private MessagingGroupService messagingGroupService;

  @Autowired private PatientService patientService;

  @Autowired private PatientTemplateService patientTemplateService;

  @Autowired private MessagesDeliveryService messagesDeliveryService;

  @ApiOperation(
      value = "None",
      notes = "Triggers sending of an instant message. Creates required message entities.")
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "Message has been triggered"),
        @ApiResponse(code = 500, message = "Failed to trigger message")
      })
  @RequestMapping(
      value = "/{personUuid}/{templateNames}/{channelType}",
      method = RequestMethod.POST)
  @ResponseStatus(value = HttpStatus.OK)
  public void triggerSendMessage(
      @PathVariable(value = "personUuid") String personUuid,
      @PathVariable(value = "templateNames") String templateNames,
      @PathVariable(value = "channelType") String channelType) {

    final Patient patient = patientService.getPatientByUuid(personUuid);
    LOGGER.info(
        String.format(
            "Message triggering started with parameters: personUuid - %s, templateNames - %s, channelType - %s",
            personUuid, templateNames, channelType));
    final ScheduledServiceGroup scheduledServiceGroup =
        createScheduledServiceGroupWithScheduledServices(patient, templateNames, channelType);

    final ScheduledExecutionContext executionContext =
        new ScheduledExecutionContext(
            scheduledServiceGroup.getScheduledServices(),
            channelType,
            new Date(),
            patient,
            patient.getId(),
            MessagesConstants.PATIENT_DEFAULT_ACTOR_TYPE,
            scheduledServiceGroup.getId());

    if (StringUtils.isNotBlank(getPersonPhone(patient))) {
      messagesDeliveryService.scheduleDelivery(executionContext);
      LOGGER.info("Message triggering finished");
    } else {
      LOGGER.error("Patient %s with id %d does not have an assigned phone number");
    }
  }

  private ScheduledServiceGroup createScheduledServiceGroupWithScheduledServices(
      Patient patient, String templateNames, String channelType) {
    final ScheduledServiceGroup scheduledServiceGroup = new ScheduledServiceGroup();

    for (String templateName : templateNames.split(",")) {
      if (templateName.startsWith(VISIT_REMINDER_TEMPLATE_NAME)) {
        handleVisitReminder(patient, templateName);
        templateName = VISIT_REMINDER_TEMPLATE_NAME;
      }

      final PatientTemplate patientTemplate =
          patientTemplateService.getOrCreatePatientTemplate(patient, templateName);

      final ScheduledService scheduledService = new ScheduledService();
      scheduledService.setStatus(ServiceStatus.PENDING);
      scheduledService.setPatientTemplate(patientTemplate);
      scheduledService.setScheduledServiceParameters(Collections.emptyList());
      scheduledService.setService(patientTemplate.getTemplate().getName());

      scheduledServiceGroup.getScheduledServices().add(scheduledService);
      scheduledService.setGroup(scheduledServiceGroup);
    }

    scheduledServiceGroup.setPatient(patient);
    scheduledServiceGroup.setActor(patient);
    scheduledServiceGroup.setStatus(ServiceStatus.PENDING);
    scheduledServiceGroup.setChannelType(channelType);
    scheduledServiceGroup.setMsgSendTime(new Date());

    return messagingGroupService.saveGroup(scheduledServiceGroup);
  }

  private String getPersonPhone(Person person) {
    PersonAttribute phoneAttribute = person.getAttribute(PERSON_PHONE_ATTR);
    if (phoneAttribute == null) {
      return null;
    } else {
      return phoneAttribute.getValue();
    }
  }

  private void handleVisitReminder(Patient patient, String templateName) {
    Visit visit = createVisit(patient, templateName);
    Context.getVisitService().saveVisit(visit);
  }

  private Visit createVisit(Patient patient, String templateName) {
    Visit visit = new Visit();
    visit.setPatient(patient);
    visit.setStartDatetime(DateUtil.addDaysToDate(now(), 1));
    visit.setLocation(Context.getLocationService().getDefaultLocation());
    visit.setVisitType(findVisitTypeByName(StringUtils.substringBetween(templateName, "(", ")")));
    setVisitAttributes(visit);

    return visit;
  }

  private VisitType findVisitTypeByName(String name) {
    VisitType visitType = null;
    Optional<VisitType> type =
        Context.getVisitService().getAllVisitTypes().stream()
            .filter(vt -> StringUtils.equalsIgnoreCase(vt.getName(), name))
            .findFirst();
    if (type.isPresent()) {
      visitType = type.get();
    } else {
      LOGGER.warn(String.format("Visit type with name %s not found", name));
    }

    return visitType;
  }

  private void setVisitAttributes(Visit visit) {
    visit.setAttribute(createAttribute("Visit Status", "SCHEDULED"));
    visit.setAttribute(createAttribute("Visit Time", "Morning"));
  }

  private VisitAttribute createAttribute(String attributeTypeName, String value) {
    VisitAttribute visitAttribute = new VisitAttribute();
    visitAttribute.setAttributeType(findVisitAttributeTypeByName(attributeTypeName));
    visitAttribute.setValueReferenceInternal(value);

    return visitAttribute;
  }

  private VisitAttributeType findVisitAttributeTypeByName(String name) {
    VisitAttributeType visitAttributeType = null;
    Optional<VisitAttributeType> attributeType =
        Context.getVisitService().getAllVisitAttributeTypes().stream()
            .filter(attr -> StringUtils.equalsIgnoreCase(attr.getName(), name))
            .findFirst();
    if (attributeType.isPresent()) {
      visitAttributeType = attributeType.get();
    } else {
      LOGGER.warn(String.format("Visit attribute type with name %s not found", name));
    }

    return visitAttributeType;
  }

  private Date now() {
    return Date.from(DateUtil.now().toInstant());
  }
}
