/* * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.messages.api.util.end.date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.messages.api.util.TimeType;

import java.util.Calendar;
import java.util.Date;

import static org.openmrs.module.messages.api.util.FieldDateUtil.SEPARATOR;

public class OtherEndDate implements EndDate {

    private static final Log LOGGER = LogFactory.getLog(OtherEndDate.class);

    private EndDateParams params;

    public OtherEndDate(EndDateParams params) {
        this.params = params;
    }

    @Override
    public Date getDate() {
        Date endDate = null;
        if (null != params && null != params.getValue() && null != params.getStartDate()) {
            String[] chain = StringUtils.split(params.getValue(), SEPARATOR);
            if (chain.length > 1) {
                try {
                    TimeType timeType = TimeType.valueOf(chain[0]);
                    Integer units = Integer.valueOf(chain[1]);
                    endDate = getOtherEndDate(params.getStartDate(), timeType, units);
                } catch (IllegalArgumentException e) {
                    LOGGER.warn("Exception occurred during processing end date: " + e.getMessage());
                }
            }
        } else {
            LOGGER.warn("The end date could not be calculated for provided params:" + params);
        }
        return endDate;
    }

    private Date getOtherEndDate(Date startDate, TimeType timeType, Integer units) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        try {
            calculateOtherDate(calendar, timeType, units);
        } catch (IllegalArgumentException e) {
            return null;
        }
        return calendar.getTime();
    }

    private void calculateOtherDate(Calendar calendar, TimeType timeType, Integer units)
            throws IllegalArgumentException {
        switch (timeType) {
            case DAY:
                calendar.add(Calendar.DAY_OF_MONTH, units);
                break;
            case MONTH:
                calendar.add(Calendar.MONTH, units);
                break;
            case YEAR:
                calendar.add(Calendar.YEAR, units);
                break;
            default:
                throw new IllegalArgumentException(String.format("Invalid Time Type provided '%s'.",
                        timeType));
        }
    }
}
