package org.openmrs.module.messages.api.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class DefaultPatientTemplateStateDTO implements Serializable {

    private static final long serialVersionUID = -2593240814951309883L;

    private boolean defaultValuesUsed = true;

    private List<PatientTemplateDTO> lackingPatientTemplates = new ArrayList<>();

    private MessageDetailsDTO details;

    public DefaultPatientTemplateStateDTO() {
    }

    public DefaultPatientTemplateStateDTO(List<PatientTemplateDTO> lacking, MessageDetailsDTO details) {
        this.lackingPatientTemplates = lacking;
        this.defaultValuesUsed = lacking.size() > 0;
        this.details = details;
    }

    public boolean isDefaultValuesUsed() {
        return defaultValuesUsed;
    }

    public DefaultPatientTemplateStateDTO setDefaultValuesUsed(boolean defaultValuesUsed) {
        this.defaultValuesUsed = defaultValuesUsed;
        return this;
    }

    public List<PatientTemplateDTO> getLackingPatientTemplates() {
        return lackingPatientTemplates;
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

    public MessageDetailsDTO getDetails() {
        return details;
    }

    public DefaultPatientTemplateStateDTO setDetails(MessageDetailsDTO details) {
        this.details = details;
        return this;
    }
}