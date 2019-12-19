package org.openmrs.module.messages.api.scheduler.job;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Person;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.messages.api.constants.MessagesConstants;
import org.openmrs.module.messages.api.exception.MessagesRuntimeException;
import org.openmrs.module.messages.api.execution.ExecutionException;
import org.openmrs.module.messages.api.execution.GroupedServiceResultList;
import org.openmrs.module.messages.api.execution.ServiceResult;
import org.openmrs.module.messages.api.execution.ServiceResultGroupHelper;
import org.openmrs.module.messages.api.execution.ServiceResultList;
import org.openmrs.module.messages.api.model.PersonStatus;
import org.openmrs.module.messages.api.service.MessagesSchedulerService;
import org.openmrs.module.messages.api.service.MessagingService;
import org.openmrs.module.messages.api.util.DateUtil;

import java.util.Date;
import java.util.List;

import static org.openmrs.module.messages.api.util.PersonAttributeUtil.getPersonStatus;

public class MessageDeliveriesJobDefinition extends JobDefinition {

    private static final Log LOGGER = LogFactory.getLog(MessageDeliveriesJobDefinition.class);
    private static final String TASK_NAME = "Message Deliveries Job Task";

    @Override
    public void execute() {
        LOGGER.info(getTaskName() + " started");
        try {
            List<ServiceResultList> results =
                getMessagingService().retrieveAllServiceExecutions(DateUtil.now(),
                DateUtil.getDatePlusSeconds(getTaskDefinition().getRepeatInterval()));

            List<GroupedServiceResultList> groupedResults = ServiceResultGroupHelper
                .groupByActorAndExecutionDate(results);

            for (GroupedServiceResultList groupedResult : groupedResults) {
                ServiceResultList group = groupedResult.getGroup();
                JobDefinition definition = new ServiceGroupDeliveryJobDefinition(groupedResult);
                Date startDate = getGroupResultsStartDate(group.getResults());
                Person person = getPersonService().getPerson(group.getActorId());
                if (PersonStatus.ACTIVE.equals(getPersonStatus(person))) {
                    getSchedulerService().createNewTask(definition, startDate, JobRepeatInterval.NEVER);
                } else {
                    LOGGER.warn("Status of a person with id=" + person.getId() + " is not active, " +
                            "so no service execution events will be sent");
                }
            }
        } catch (ExecutionException e) {
            LOGGER.error("Failed to execute task: " + getTaskName());
            throw new MessagesRuntimeException("Failed to execute task: " + getTaskName(), e);
        }
    }

    @Override
    public String getTaskName() {
        return TASK_NAME;
    }

    @Override
    public boolean shouldStartAtFirstCreation() {
        return true;
    }

    @Override
    public Class getTaskClass() {
        return MessageDeliveriesJobDefinition.class;
    }

    private MessagingService getMessagingService() {
        return Context.getRegisteredComponent(
            MessagesConstants.MESSAGING_SERVICE, MessagingService.class);
    }

    private MessagesSchedulerService getSchedulerService() {
        return Context.getRegisteredComponent(
            MessagesConstants.SCHEDULER_SERVICE, MessagesSchedulerService.class);
    }

    private PersonService getPersonService() {
        return Context.getRegisteredComponent(
                MessagesConstants.PERSON_SERVICE, PersonService.class);
    }

    private Date getGroupResultsStartDate(List<ServiceResult> results) {
        //TODO in CFLM-470: Refactor so we're not using get(0) here (which works but is ugly)
        return results.get(0).getExecutionDate();
    }
}
