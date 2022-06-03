/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.messages.web.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openmrs.module.messages.api.dto.PatientTemplateDTO;

import java.util.List;

public class PatientTemplatesWrapper {

    private List<PatientTemplateDTO> patientTemplates;

    public PatientTemplatesWrapper() {
    }

    public PatientTemplatesWrapper(List<PatientTemplateDTO> patientTemplates) {
        this.patientTemplates = patientTemplates;
    }

    public List<PatientTemplateDTO> getPatientTemplates() {
        return patientTemplates;
    }

    public PatientTemplatesWrapper setPatientTemplates(List<PatientTemplateDTO> patientTemplates) {
        this.patientTemplates = patientTemplates;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
