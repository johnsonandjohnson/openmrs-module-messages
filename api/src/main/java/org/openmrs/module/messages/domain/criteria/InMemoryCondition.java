/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.messages.domain.criteria;

import org.openmrs.OpenmrsObject;

/**
 * The InMemoryCondition Class.
 * <p>
 * The In Memory Conditions are executed after data has been read from the database.
 * </p>
 * <p>
 * The {@code applyCondition} shall accept any {@code OpenmrsObject} entity and return boolean true or false whether the
 * entity fulfills the condition or not.
 * </p>
 */
public interface InMemoryCondition extends Condition<Boolean, OpenmrsObject> {

}
