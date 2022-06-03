/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.messages.builder;

import org.openmrs.module.messages.api.dto.TemplateDTO;
import org.openmrs.module.messages.api.dto.TemplateFieldDTO;
import org.openmrs.module.messages.api.model.Template;

import java.util.Collections;
import java.util.List;

public final class TemplateDTOBuilder extends AbstractBuilder<TemplateDTO> {

    private Integer id;

    private String name;

    private String serviceQuery;

    private String serviceQueryType;

    private List<TemplateFieldDTO> templateFields;

    private String uuid;

    private String calendarServiceQuery;

    private boolean shouldUseOptimizedQuery;

    public TemplateDTOBuilder() {
        Template template = new TemplateBuilder().build();
        id = template.getId();
        name = template.getName();
        serviceQuery = template.getServiceQuery();
        serviceQueryType = template.getServiceQueryType();
        templateFields = Collections.singletonList(new TemplateFieldDTOBuilder().build());
        uuid = template.getUuid();
        calendarServiceQuery = template.getCalendarServiceQuery();
        shouldUseOptimizedQuery = template.isShouldUseOptimizedQuery();
    }

    @Override
    public TemplateDTO build() {
        return new TemplateDTO()
                .setId(id)
                .setName(name)
                .setServiceQuery(serviceQuery)
                .setServiceQueryType(serviceQueryType)
                .setTemplateFields(templateFields)
                .setUuid(uuid)
                .setCalendarServiceQuery(calendarServiceQuery)
                .setShouldUseOptimizedQuery(shouldUseOptimizedQuery);
    }

    @Override
    public TemplateDTO buildAsNew() {
        return withId(null).withUuid(null).build();
    }

    public TemplateDTOBuilder withId(Integer id) {
        this.id = id;
        return this;
    }

    public TemplateDTOBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public TemplateDTOBuilder withServiceQuery(String serviceQuery) {
        this.serviceQuery = serviceQuery;
        return this;
    }

    public TemplateDTOBuilder withServiceQueryType(String serviceQueryType) {
        this.serviceQueryType = serviceQueryType;
        return this;
    }

    public TemplateDTOBuilder withTemplateFields(List<TemplateFieldDTO> templateFields) {
        this.templateFields = templateFields;
        return this;
    }

    public TemplateDTOBuilder withUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public TemplateDTOBuilder withCalendarServiceQuery(String calendarServiceQuery) {
        this.calendarServiceQuery = calendarServiceQuery;
        return this;
    }

    public TemplateDTOBuilder withShouldUseOptimizedQuery(boolean shouldUseOptimizedQuery) {
        this.shouldUseOptimizedQuery = shouldUseOptimizedQuery;
        return this;
    }
}
