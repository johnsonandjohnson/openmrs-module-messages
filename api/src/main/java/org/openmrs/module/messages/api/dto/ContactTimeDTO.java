/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.messages.api.dto;

/**
 * Represents a contact time DTO
 */
public class ContactTimeDTO extends BaseDTO {

    private static final long serialVersionUID = 7391388329457186931L;

    private Integer personId;

    private String time;

    public Integer getPersonId() {
        return personId;
    }

    public ContactTimeDTO setPersonId(Integer personId) {
        this.personId = personId;
        return this;
    }

    public String getTime() {
        return time;
    }

    public ContactTimeDTO setTime(String time) {
        this.time = time;
        return this;
    }
}
