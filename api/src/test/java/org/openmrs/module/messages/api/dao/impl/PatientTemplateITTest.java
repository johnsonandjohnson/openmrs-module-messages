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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.messages.ContextSensitiveTest;
import org.openmrs.module.messages.api.dao.PatientTemplateDao;
import org.openmrs.module.messages.api.dao.TemplateDao;
import org.openmrs.module.messages.api.helper.PatientTemplateHelper;
import org.openmrs.module.messages.api.helper.TemplateFieldHelper;
import org.openmrs.module.messages.api.helper.TemplateFieldValueHelper;
import org.openmrs.module.messages.api.helper.TemplateHelper;
import org.openmrs.module.messages.api.model.PatientTemplate;
import org.openmrs.module.messages.api.model.Template;
import org.openmrs.module.messages.api.model.TemplateField;
import org.openmrs.module.messages.api.model.TemplateFieldValue;
import org.openmrs.module.messages.api.util.TestConstants;
import org.openmrs.module.messages.domain.PagingInfo;
import org.openmrs.module.messages.domain.criteria.PatientTemplateCriteria;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PatientTemplateITTest extends ContextSensitiveTest {

    @Autowired
    private PatientTemplateDao patientTemplateDao;
    
    @Autowired
    private TemplateDao templateDao;
    
    private PatientTemplate patientTemplate;

    @Before
    public void setUp() {
        createTestInstance();
        createTestInstance();
        createTestInstance();
    }
    
    @Test
    public void shouldSaveAllPropertiesInDb() {
        PatientTemplate savedPatientTemplate = patientTemplateDao.getByUuid(patientTemplate.getUuid());
        
        Assert.assertThat(savedPatientTemplate, hasProperty("uuid", is(patientTemplate.getUuid())));
        Assert.assertThat(savedPatientTemplate, hasProperty("actor", is(patientTemplate.getActor())));
        Assert.assertThat(savedPatientTemplate, hasProperty("actorType",
                is(patientTemplate.getActorType())));
        Assert.assertThat(savedPatientTemplate, hasProperty("patient",
                is(patientTemplate.getPatient())));
        Assert.assertThat(savedPatientTemplate, hasProperty("template",
                is(patientTemplate.getTemplate())));
    }
    
    @Test
    public void shouldReturnAllSavedPatientTemplates() {
        List<PatientTemplate> patientTemplates = patientTemplateDao.getAll(true);

        Assert.assertEquals(TestConstants.GET_ALL_EXPECTED_LIST_SIZE, patientTemplates.size());
    }
    
    @Test
    public void shouldDeletePatientTemplate() {
        patientTemplateDao.delete(patientTemplate);
        
        Assert.assertNull(patientTemplateDao.getByUuid(patientTemplate.getUuid()));
        Assert.assertEquals(TestConstants.DELETE_EXPECTED_LIST_SIZE, patientTemplateDao.getAll(true).size());
    }

    @Test
    public void shouldFindPatientTemplatesByCriteriaAndPagination() {
        Template template = templateDao.getAll(false).get(0);
        patientTemplateDao.getAll(false).forEach(pt -> pt.setTemplate(template));
        PatientTemplateCriteria patientTemplateCriteria = PatientTemplateCriteria.forTemplate(template.getId());

        List<PatientTemplate> actual = patientTemplateDao.findAllByCriteria(patientTemplateCriteria, new PagingInfo(1, 2));

        assertNotNull(actual);
        assertEquals(2, actual.size());
    }

    private void createTestInstance() {
        Template template = TemplateHelper.createTestInstance();
        templateDao.saveOrUpdate(template);
        Template savedTemplate = templateDao.getByUuid(template.getUuid());
        
        TemplateField templateField = TemplateFieldHelper.createTestInstace();
        templateField.setTemplate(savedTemplate);
        
        TemplateFieldValue templateFieldValue = TemplateFieldValueHelper.createTestInstance();
        templateFieldValue.setTemplateField(templateField);
        
        patientTemplate = PatientTemplateHelper.createTestInstance();
        patientTemplate.setTemplate(savedTemplate);
        
        patientTemplateDao.saveOrUpdate(patientTemplate);
    }
}
