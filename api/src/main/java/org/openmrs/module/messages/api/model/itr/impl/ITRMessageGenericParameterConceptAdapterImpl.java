/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.messages.api.model.itr.impl;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.openmrs.Concept;
import org.openmrs.api.APIException;
import org.openmrs.module.messages.api.constants.ConfigConstants;
import org.openmrs.module.messages.api.model.itr.ITRMessageGenericParameter;
import org.openmrs.module.messages.api.util.VelocityContextFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * The immutable Concept adapter for ITRMessageGenericParameter.
 */
public final class ITRMessageGenericParameterConceptAdapterImpl extends ITRMessageBase
    implements ITRMessageGenericParameter {
  private final String name;
  private final String valueScript;

  ITRMessageGenericParameterConceptAdapterImpl(Concept parameterConcept) {
    this.name = parameterConcept.getName().getName();
    this.valueScript = getStringAttribute(ConfigConstants.ITR_MESSAGE_TEXT_ATTR_TYPE_UUID, parameterConcept);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getValue(Map<String, Object> contextParameters) {
    final VelocityContext velocityContext = VelocityContextFactory.createDefaultContext();
    velocityContext.put("contextParameters", contextParameters);

    try {
      final StringWriter scriptResultWriter = new StringWriter();
      Velocity.evaluate(velocityContext, scriptResultWriter, name, valueScript);
      return removeNewLines(scriptResultWriter.toString());
    } catch (IOException ioe) {
      throw new APIException(ioe);
    }
  }

  // Remove new line characters, otherwise they will break the SMS Request template creation
  private static String removeNewLines(String text) {
    return text.replaceAll("\r?\n", "");
  }
}
