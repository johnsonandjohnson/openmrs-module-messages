/* * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.messages.api.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openmrs.Person;
import org.openmrs.module.messages.api.util.OpenmrsObjectUtil;

public class ScheduledExecutionContext implements Serializable {

    private static final long serialVersionUID = 7043667008864304408L;

    private List<Integer> serviceIdsToExecute;

    private Date executionDate;

    private int actorId;

    public ScheduledExecutionContext(List<ScheduledService> scheduledServices,
                                     Date executionDate, Person actor) {
        this.serviceIdsToExecute = OpenmrsObjectUtil.getIds(scheduledServices);
        this.executionDate = executionDate;
        this.actorId = actor.getId();
    }

    public List<Integer> getServiceIdsToExecute() {
        return serviceIdsToExecute;
    }

    public Date getExecutionDate() {
        return executionDate;
    }

    public int getActorId() {
        return actorId;
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