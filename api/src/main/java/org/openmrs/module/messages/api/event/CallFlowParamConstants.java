/* * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.messages.api.event;

public final class CallFlowParamConstants {
    
    /**
     * Recipient's phone number
     */
    public static final String PHONE = "phone";
    
    /**
     * Config that was used for this call
     */
    public static final String CONFIG = "config";

    /**
     * Flow that was used for this call
     */
    public static final String FLOW_NAME = "flowName";

    /**
     * Map of additional parameters
     */
    public static final String ADDITIONAL_PARAMS = "params";

    /**
     * Message id of the call
     */
    public static final String MESSAGE_ID = "message_id";

    /**
     * Used service name
     */
    public static final String SERVICE_NAME = "service";

    /**
     * Services used in multiple call flow
     */
    public static final String MESSAGES = "messages";

    /**
     * Service objects used in multiple call flow. Alternative for MESSAGES.
     * Might be required, for now we will send both
     */
    public static final String MESSAGES_OBJECTS = "messages_objects";

    private CallFlowParamConstants() {
    }
}
