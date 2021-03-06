/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.messages.api.mappers;

import org.apache.commons.lang.NotImplementedException;
import org.openmrs.module.messages.api.dto.ActorScheduleDTO;
import org.openmrs.module.messages.api.dto.MessageDTO;
import org.openmrs.module.messages.api.model.PatientTemplate;
import org.openmrs.module.messages.api.model.Template;

import java.util.Collections;
import java.util.List;

public class MessageMapper implements ListMapper<MessageDTO, PatientTemplate> {

    private UserMapper userMapper;

    private ActorScheduleMapper actorScheduleMapper;

    public void setUserMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public void setActorScheduleMapper(ActorScheduleMapper actorScheduleMapper) {
        this.actorScheduleMapper = actorScheduleMapper;
    }

    @Override
    public MessageDTO toDto(List<PatientTemplate> daos) {
        Template sharedTemplate = getSharedTemplate(daos);
        List<ActorScheduleDTO> actorScheduleDTOs = actorScheduleMapper.toDtos(daos);
        Collections.sort(actorScheduleDTOs);
        return new MessageDTO(
                sharedTemplate.getName(),
                sharedTemplate.getDateCreated(),
                userMapper.toDto(sharedTemplate.getCreator()),
                actorScheduleDTOs
        );
    }

    @Override
    public List<PatientTemplate> fromDto(MessageDTO dto) {
        throw new NotImplementedException("mapping from MessageDTO to List<PatientTemplate> is not implemented yet");
    }

    private Template getSharedTemplate(List<PatientTemplate> daos) {
        validateNotEmpty(daos);
        validateTemplateIsShared(daos);
        return daos.get(0).getTemplate();
    }

    private void validateNotEmpty(List<PatientTemplate> daos) {
        if (daos.isEmpty()) {
            throw new IllegalArgumentException("To create one MessageDTO from list of Patient Templates," +
                    " the list can't be empty!");
        }
    }

    private void validateTemplateIsShared(List<PatientTemplate> daos) {
        for (PatientTemplate patientTemplate : daos) {
            if (!patientTemplate.getTemplate().equals(daos.get(0).getTemplate())) {
                throw new IllegalArgumentException("To create one MessageDTO from list of Patient Templates," +
                        " all of those should be relates dto the same template!");
            }
        }
    }
}
