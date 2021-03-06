/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.messages.api.dao.impl;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.messages.ContextSensitiveTest;
import org.openmrs.module.messages.api.dao.ActorResponseDao;
import org.openmrs.module.messages.api.dao.MessagingDao;
import org.openmrs.module.messages.api.model.ActorResponse;
import org.openmrs.module.messages.api.model.ScheduledService;
import org.openmrs.module.messages.api.service.MessagingService;
import org.openmrs.module.messages.builder.ActorResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class ActorResponseITTest extends ContextSensitiveTest {
    
    private static final String XML_DATA_SET_PATH = "datasets/";
    
    private static final String QUESTION_UUID = "16f1ce79-ef8a-47ca-bc40-fee648a835b4";
    
    private static final String RESPONSE_UUID = "9b251fd0-b900-4b11-9b77-b5174a0368b8";
    
    private static final String SCHEDULE_UUID = "b3de6d76-3e31-41cf-955d-ad14b9db07ff";
    
    private static final String EXPECTED_PATIENT_UUID = "7321f17d-bdef-4571-8f9b-c2ec868dc251";
    
    private static final Date TIMESTAMP = new Date(2019, Calendar.NOVEMBER, 21);
    
    private static final int GET_ALL_EXPECTED_LIST_SIZE = 10;
    
    private static final int UPDATED_YEAR = 2019;
    
    private static final int UPDATED_DAY = 25;
    
    private static final int PATIENT_ID = 1;
    
    private static final int ACTOR_ID = 1;
    
    private static final int QUESTION_CONCEPT_ID = 1;
    
    private static final int RESPONSES_LIMIT = 3;
    
    private static final int ACTOR_RESPONSE_ID = 6;
    
    private static final int ACTOR_RESPONSE_2_ID = 9;
    
    private static final int ACTOR_RESPONSE_3_ID = 10;
    
    private static final String TEXT_QUESTION = "Have you take a medicine?";
    
    private static final String HEALTH_TIP_MESSAGE_TYPE = "Health tip";
    
    private Concept question;
    
    private Concept response;
    
    private Patient patient;
    
    private ScheduledService scheduledService;
    
    @Autowired
    @Qualifier("messages.MessagingDao")
    private MessagingDao messagingDao;
    
    @Autowired
    @Qualifier("messages.ActorResponseDao")
    private ActorResponseDao actorResponseDao;
    
    @Autowired
    @Qualifier("patientService")
    private PatientService patientService;
    
    @Autowired
    private MessagingService messagingService;
    
    @Before
    public void setUp() throws Exception {
        executeDataSet(XML_DATA_SET_PATH + "ConceptDataSet.xml");
        executeDataSet(XML_DATA_SET_PATH + "MessageDataSet.xml");
        executeDataSet(XML_DATA_SET_PATH + "ActorResponsesDataSet.xml");
        question = Context.getConceptService().getConceptByUuid(QUESTION_UUID);
        response = Context.getConceptService().getConceptByUuid(RESPONSE_UUID);
        patient = patientService.getPatientByUuid(EXPECTED_PATIENT_UUID);
        scheduledService = messagingDao.getByUuid(SCHEDULE_UUID);
    }
    
    @Test
    public void shouldSaveAllPropertiesInDb() {
        ActorResponse expected = createDefault();
        
        ActorResponse actual = actorResponseDao.saveOrUpdate(createDefault());
        
        assertThat(actual, not(nullValue()));
        assertThat(actual.getId(), not(nullValue()));
        
        assertThat(actual, hasProperty("actor", is(expected.getActor())));
        assertThat(actual, hasProperty("patient", is(expected.getPatient())));
        assertThat(actual, hasProperty("sourceId", is(expected.getSourceId())));
        assertThat(actual, hasProperty("sourceType", is(expected.getSourceType())));
        assertThat(actual, hasProperty("textQuestion", is(expected.getTextQuestion())));
        assertThat(actual, hasProperty("question", is(expected.getQuestion())));
        assertThat(actual, hasProperty("response", is(expected.getResponse())));
        assertThat(actual, hasProperty("textResponse", is(expected.getTextResponse())));
        assertThat(actual, hasProperty("answeredTime", is(expected.getAnsweredTime())));
        
        assertThat(actual, hasProperty("uuid", not(nullValue())));
        assertThat(actual, hasProperty("dateCreated", not(nullValue())));
        assertThat(actual, hasProperty("creator", not(nullValue())));
        assertThat(actual, hasProperty("voided", is(false)));
    }
    
    @Test
    public void shouldDeleteActorResponse() {
        ActorResponse actorResponse = actorResponseDao.saveOrUpdate(createDefault());
        int beforeRemove = actorResponseDao.getAll(true).size();
        
        actorResponseDao.delete(actorResponse);
        
        assertNull(actorResponseDao.getByUuid(actorResponse.getUuid()));
        assertEquals(GET_ALL_EXPECTED_LIST_SIZE + 1, beforeRemove);
        assertEquals(GET_ALL_EXPECTED_LIST_SIZE, actorResponseDao.getAll(true).size());
    }
    
    @Test
    public void shouldReturnAllSavedActorResponses() {
        actorResponseDao.saveOrUpdate(createDefault());
        List<ActorResponse> actorResponses = actorResponseDao.getAll(true);
        
        assertEquals(GET_ALL_EXPECTED_LIST_SIZE + 1, actorResponses.size());
    }
    
    @Test
    public void shouldUpdateExistingActorResponse() {
        ActorResponse actor = actorResponseDao.saveOrUpdate(createDefault());
        
        ScheduledService updatedService = messagingDao.getByUuid("532f0b56-3ff9-427b-b4f3-b92796c7eea2");
        Concept concept12 = Context.getConceptService().getConceptByUuid(
                "a8f03299-ab40-49ea-91df-d034d0de009c");
        Concept concept13 = Context.getConceptService().getConceptByUuid(
                "e3d2a6d1-9518-44f4-875c-40b8a83fd7e8");
        String updatedTextResponse = "updated text";
        Date updatedTimestamp = new Date(UPDATED_YEAR, Calendar.NOVEMBER, UPDATED_DAY);
        
        actor.setQuestion(concept12);
        actor.setResponse(concept13);
        actor.setTextResponse(updatedTextResponse);
        actor.setAnsweredTime(updatedTimestamp);
        
        ActorResponse updated = actorResponseDao.saveOrUpdate(actor);
        
        assertThat(updated, hasProperty("question", is(concept12)));
        assertThat(updated, hasProperty("response", is(concept13)));
        assertThat(updated, hasProperty("textResponse", is(updatedTextResponse)));
        assertThat(updated, hasProperty("answeredTime", is(updatedTimestamp)));
    }
    
    @Test
    public void shouldReturnThreeActorResponsesForConceptQuestion() {
        
        List<ActorResponse> actorResponses = messagingService.getLastActorResponsesForConceptQuestion(
                PATIENT_ID, ACTOR_ID, QUESTION_CONCEPT_ID, RESPONSES_LIMIT);
        assertEquals(RESPONSES_LIMIT, actorResponses.size());
        assertEquals(Integer.valueOf(ACTOR_RESPONSE_ID), actorResponses.get(0).getId());
    }
    
    @Test
    public void shouldReturnThreeActorResponsesForTextQuestion() {
        List<ActorResponse> actorResponses = messagingService.getLastActorResponsesForTextQuestion(
                PATIENT_ID, ACTOR_ID, TEXT_QUESTION, RESPONSES_LIMIT);
        
        assertEquals(RESPONSES_LIMIT, actorResponses.size());
        assertEquals(Integer.valueOf(ACTOR_RESPONSE_2_ID), actorResponses.get(0).getId());
    }
    
    @Test
    public void shouldReturnThreeActorResponsesForMessageType() {
        List<ActorResponse> actorResponses = messagingService.getLastActorResponsesForServiceType(
                PATIENT_ID, ACTOR_ID, HEALTH_TIP_MESSAGE_TYPE, RESPONSES_LIMIT);
        
        assertEquals(RESPONSES_LIMIT, actorResponses.size());
        assertEquals(Integer.valueOf(ACTOR_RESPONSE_3_ID), actorResponses.get(0).getId());
    }
    
    private ActorResponse createDefault() {
        return new ActorResponseBuilder()
                .withActor(patient)
                .withPatient(patient)
                .withSourceId(scheduledService.getId().toString())
                .withQuestion(question)
                .withResponse(response)
                .withAnsweredTime(TIMESTAMP)
                .buildAsNew();
    }
}
