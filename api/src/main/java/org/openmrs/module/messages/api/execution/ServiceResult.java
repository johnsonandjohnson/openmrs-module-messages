/* * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.messages.api.execution;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.openmrs.module.messages.api.dto.DTO;
import org.openmrs.module.messages.api.model.PatientTemplate;
import org.openmrs.module.messages.api.model.types.ServiceStatus;
import org.openmrs.module.messages.api.util.DateUtil;
import org.openmrs.module.messages.api.util.ZoneConverterUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a single execution for a service/message.
 */
public class ServiceResult implements Serializable, DTO {

    private static final long serialVersionUID = 2598236499107927781L;

    public static final String EXEC_DATE_ALIAS = "EXECUTION_DATE";
    public static final String MSG_ID_ALIAS = "MESSAGE_ID";
    public static final String CHANNEL_NAME_ALIAS = "CHANNEL_ID";
    public static final String STATUS_COL_ALIAS = "STATUS_ID";
    public static final int MIN_COL_NUM = 3;

    private Date executionDate;
    private Object messageId;
    private String channelType;
    private ServiceStatus serviceStatus = ServiceStatus.FUTURE;
    private Map<String, Object> additionalParams = new HashMap<>();
    private Integer patientTemplateId;

    public static ServiceResult parse(Map<String, Object> row) {
        if (row.size() < MIN_COL_NUM) {
            throw new IllegalStateException("Invalid number of columns in result row: " + row.size());
        }

        Date date = null;
        Object msgId = null;
        String channelType = null;
        ServiceStatus status = ServiceStatus.FUTURE;
        Map<String, Object> params = new HashMap<>();

        for (Map.Entry<String, Object> entry : row.entrySet()) {
            switch (entry.getKey()) {
                case EXEC_DATE_ALIAS:
                    date = DateUtil.toSimpleDate((Date) entry.getValue());
                    break;
                case MSG_ID_ALIAS:
                    msgId = entry.getValue();
                    break;
                case CHANNEL_NAME_ALIAS:
                    channelType = String.valueOf(entry.getValue());
                    break;
                case STATUS_COL_ALIAS:
                    status = parseStatus((String) entry.getValue());
                    break;
                default:
                    params.put(entry.getKey(), entry.getValue());
                    break;
            }
        }

        date = adjustTimezoneIfFuturePlannedEvent(date, status);
        return new ServiceResult(date, msgId, channelType, status, params);
    }

    public static List<ServiceResult> parseList(List<Map<String, Object>> list, PatientTemplate patientTemplate) {
        Map<String, ServiceResult> resultServices = new LinkedHashMap<>();
        for (Map<String, Object> row : list) {
            ServiceResult result = ServiceResult.parse(row);
            result.patientTemplateId = patientTemplate.getId();
            String key = ZoneConverterUtil.formatToUserZone(result.getExecutionDate())
                    + result.getChannelType();
            if (resultServices.containsKey(key)) {
                if (result.getServiceStatus() != null
                        && !ServiceStatus.FUTURE.equals(result.getServiceStatus())) {
                    resultServices.put(key, result);
                }
            } else {
                resultServices.put(key, result);
            }
        }
        return new ArrayList(resultServices.values());
    }

    public ServiceResult() {
    }

    public ServiceResult(
            Date executionDate,
            Object messageId,
            String channelType,
            ServiceStatus serviceStatus,
            Map<String, Object> additionalParams
    ) {
        if (executionDate == null) {
            throw new IllegalArgumentException("Execution date is mandatory");
        }
        if (messageId == null) {
            throw new IllegalArgumentException("Message ID (external execution id) is required");
        }
        if (channelType == null) {
            throw new IllegalArgumentException("Channel type (name) is required");
        }

        this.executionDate = executionDate;
        this.messageId = messageId;
        this.channelType = channelType;
        this.serviceStatus = serviceStatus;
        this.additionalParams = additionalParams == null ? new HashMap<>() : additionalParams;
    }

    @Override
    @JsonIgnore
    public Integer getId() {
        throw new NotImplementedException("not implemented yet");
    }

    public Date getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(Date executionDate) {
        this.executionDate = executionDate;
    }

    public Object getMessageId() {
        return messageId;
    }

    public void setMessageId(Object messageId) {
        this.messageId = messageId;
    }

    public String getChannelType() {
        return channelType;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    public ServiceStatus getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(ServiceStatus serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    public Map<String, Object> getAdditionalParams() {
        return additionalParams;
    }

    public void setAdditionalParams(Map<String, Object> additionalParams) {
        this.additionalParams = additionalParams;
    }

    public Integer getPatientTemplateId() {
        return patientTemplateId;
    }

    public void setPatientTemplateId(Integer patientTemplateId) {
        this.patientTemplateId = patientTemplateId;
    }

    private static ServiceStatus parseStatus(String statusString) {
        if (StringUtils.isNotBlank(statusString)) {
            return ServiceStatus.valueOf(statusString);
        } else {
            return ServiceStatus.FUTURE;
        }
    }

    private static Date adjustTimezoneIfFuturePlannedEvent(Date date, ServiceStatus status) {
        return status == null || status == ServiceStatus.FUTURE ? ZoneConverterUtil.convertToUserZone(date) : date;
    }
}
