package org.openmrs.module.messages.web.it;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Relationship;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.module.messages.api.dao.PatientTemplateDao;
import org.openmrs.module.messages.api.dao.TemplateDao;
import org.openmrs.module.messages.api.dto.MessageDTO;
import org.openmrs.module.messages.api.dto.MessageDetailsDTO;
import org.openmrs.module.messages.api.model.PatientTemplate;
import org.openmrs.module.messages.api.model.Template;
import org.openmrs.module.messages.builder.PatientTemplateBuilder;
import org.openmrs.module.messages.builder.TemplateBuilder;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
public class MessagingControllerITTest extends BaseModuleWebContextSensitiveTest {

    private static final String QUERY_1 = "query1";
    private static final String QUERY_TYPE_1 = "query_type1";
    private static final String QUERY_2 = "query2";
    private static final String QUERY_TYPE_2 = "query_type2";
    private static final int THREE_ROWS = 3;
    private static final int FIRST_PAGE = 1;
    private static final int SECOND_PAGE = 2;
    private static final String ROWS_PARAM = "rows";
    private static final String PAGE_PARAM = "page";
    private static final String PATIENT_ID_PARAM = "patientId";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private PatientTemplateDao patientTemplateDao;

    @Autowired
    private PatientService patientService;

    @Autowired
    private PersonService personService;

    @Autowired
    private TemplateDao templateDao;

    private Patient patient1;
    private Patient patient2;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        patient1 = patientService.getAllPatients().get(0);
        patient2 = patientService.getAllPatients().get(1);
    }

    @Test
    public void testFetchingMessagesForPatient() throws Exception {
        Relationship relationship = personService.getAllRelationships().get(0);
        Relationship otherRelationship = personService.getAllRelationships().get(1);
        PatientTemplate template1 = createPatientTemplate(patient1, relationship.getPersonA(),
                relationship, QUERY_1, QUERY_TYPE_1);

        createPatientTemplate(patient2, otherRelationship.getPersonA(),
                otherRelationship, QUERY_1, QUERY_TYPE_2);

        PatientTemplate template2 = createPatientTemplate(patient1, relationship.getPersonA(),
                relationship, QUERY_2, QUERY_TYPE_2);

        Patient patientWithId = new Patient();
        patientWithId.setId(patient1.getId());

        MvcResult result = mockMvc.perform(get("/messages/details")
                .param(ROWS_PARAM, String.valueOf(THREE_ROWS))
                .param(PAGE_PARAM, String.valueOf(FIRST_PAGE))
                .param(PATIENT_ID_PARAM, patient1.getId().toString()))
                .andExpect(status().is(HttpStatus.OK.value())).andReturn();

        assertThat(result, is(notNullValue()));
        MessageDetailsDTO dto = getDtoFromResult(result);

        assertThat(dto.getPatientId(), is(equalTo(patientWithId.getId())));
        assertThat(dto.getMessages().size(), is(equalTo(2)));

        for (MessageDTO messageDTO : dto.getMessages()) {
            assertThat(messageDTO.getType(), anyOf(is(QUERY_TYPE_1), is(QUERY_TYPE_2)));
            assertThat(messageDTO.getAuthor().getUsername(), is(equalTo("admin")));
            assertThat(messageDTO.getActorSchedule().getActorType(),
                    is(equalTo(relationship.getRelationshipType().getaIsToB())));
            assertThat(messageDTO.getCreatedAt(), anyOf(
                    is(equalTo(template1.getDateCreated())),
                    is(equalTo(template2.getDateCreated()))));
        }
    }

    @Test
    public void testPagination() throws Exception {
        Relationship relationship = personService.getAllRelationships().get(0);

        // Page 1, with QUERY_TYPE_1
        createPatientTemplate(patient1, relationship.getPersonA(),
                relationship, QUERY_1, QUERY_TYPE_1);

        createPatientTemplate(patient1, relationship.getPersonA(),
                relationship, QUERY_1, QUERY_TYPE_1);

        createPatientTemplate(patient1, relationship.getPersonA(),
                relationship, QUERY_2, QUERY_TYPE_1);

        // Page 1, with QUERY_TYPE_2
        createPatientTemplate(patient1, relationship.getPersonA(),
                relationship, QUERY_1, QUERY_TYPE_2);

        createPatientTemplate(patient1, relationship.getPersonA(),
                relationship, QUERY_1, QUERY_TYPE_2);

        createPatientTemplate(patient1, relationship.getPersonA(),
                relationship, QUERY_2, QUERY_TYPE_2);

        Patient patientWithId = new Patient();
        patientWithId.setId(patient1.getId());

        // Fetch page 1
        MvcResult result = mockMvc.perform(get("/messages/details")
                .param(ROWS_PARAM, String.valueOf(THREE_ROWS))
                .param(PAGE_PARAM, String.valueOf(FIRST_PAGE))
                .param(PATIENT_ID_PARAM, patient1.getId().toString()))
                .andExpect(status().is(HttpStatus.OK.value())).andReturn();

        assertThat(result, is(notNullValue()));
        assertMessageDetailsDTO(getDtoFromResult(result), QUERY_TYPE_1,
                patientWithId.getPatientId());

        // Fetch page 2
        result = mockMvc.perform(get("/messages/details")
                .param(ROWS_PARAM, String.valueOf(THREE_ROWS))
                .param(PAGE_PARAM, String.valueOf(SECOND_PAGE))
                .param(PATIENT_ID_PARAM, patient1.getId().toString()))
                .andExpect(status().is(HttpStatus.OK.value())).andReturn();

        assertThat(result, is(notNullValue()));
        assertMessageDetailsDTO(getDtoFromResult(result), QUERY_TYPE_2,
                patientWithId.getPatientId());
    }

    private void assertMessageDetailsDTO(MessageDetailsDTO dto, String queryType, Integer patientId) {
        assertThat(dto.getPatientId(), is(equalTo(patientId)));
        assertThat(dto.getMessages().size(), is(equalTo(THREE_ROWS)));

        for (MessageDTO messageDTO : dto.getMessages()) {
            assertThat(messageDTO.getType(), is(queryType));
        }
    }

    private MessageDetailsDTO getDtoFromResult(MvcResult result) throws IOException {
        return new ObjectMapper().readValue(
                result.getResponse().getContentAsString(),
                MessageDetailsDTO.class);
    }

    private PatientTemplate createPatientTemplate(Patient patient, Person person,
                                                  Relationship relationship,
                                                  String query, String queryType) {
        return patientTemplateDao.saveOrUpdate(new PatientTemplateBuilder()
                .withActor(person)
                .withActorType(relationship)
                .withPatient(patient)
                .withServiceQuery(query)
                .withServiceQueryType(queryType)
                .withTemplate(createTemplate())
                .buildAsNew());
    }

    private Template createTemplate() {
        Template template = new TemplateBuilder()
                .withServiceQuery("Query")
                .withServiceQueryType("Query type")
                .setName("Service Name")
                .buildAsNew();
        return templateDao.saveOrUpdate(template);
    }
}