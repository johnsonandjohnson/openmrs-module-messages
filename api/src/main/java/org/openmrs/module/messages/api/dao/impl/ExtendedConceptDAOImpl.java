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

import org.hibernate.criterion.Restrictions;
import org.openmrs.Concept;
import org.openmrs.ConceptAttribute;
import org.openmrs.api.db.hibernate.HibernateOpenmrsObjectDAO;
import org.openmrs.module.messages.api.dao.ExtendedConceptDAO;

import java.util.List;

public class ExtendedConceptDAOImpl extends HibernateOpenmrsObjectDAO<Concept> implements ExtendedConceptDAO {
  @Override
  public List<ConceptAttribute> getConceptAttributesByTypeUuid(String uuid) {
    return this.sessionFactory
        .getCurrentSession()
        .createCriteria(ConceptAttribute.class)
        .add(Restrictions.eq("voided", Boolean.FALSE))
        .createCriteria("attributeType", "at")
        .add(Restrictions.eq("uuid", uuid))
        .list();
  }
}
