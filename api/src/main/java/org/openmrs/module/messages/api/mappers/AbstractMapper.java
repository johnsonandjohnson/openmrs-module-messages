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

import org.openmrs.BaseOpenmrsObject;
import org.openmrs.module.messages.api.dto.DTO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class AbstractMapper<T extends DTO, R extends BaseOpenmrsObject>
        implements Mapper<T, R>, UpdateMapper<T, R> {

    @Override
    public List<T> toDtos(Iterable<R> daos) {
        final List<T> dtos = new ArrayList<T>();
        for (R dao : daos) {
            dtos.add(toDto(dao));
        }
        return dtos;
    }

    @Override
    public List<R> fromDtos(List<T> dtos) {
        final List<R> daos = new ArrayList<R>(dtos.size());
        for (T dto : dtos) {
            daos.add(fromDto(dto));
        }
        return daos;
    }

    public void updateFromDtos(List<T> sources, Collection<R> targets) {
        updateExisting(sources, targets);
        addNew(sources, targets);
    }

    /**
     * Marks the {@code target} as deleted.
     * <p>
     * The regular data and metadata in OpenMRS have different property to for safe deletion.
     * </p>
     *
     * @param target the entity to set status for, not null
     */
    protected abstract void doSafeDelete(final R target);

    private void updateExisting(List<T> sources, Iterable<R> targets) {
        Iterator<R> targetIterator = targets.iterator();
        Map<Integer, T> sourcesByIds = mapDtoById(sources);
        while (targetIterator.hasNext()) {
            R target = targetIterator.next();
            T source = sourcesByIds.get(target.getId());
            if (source == null) {
                doSafeDelete(target);
            } else {
                updateFromDto(source, target);
            }
        }
    }

    private void addNew(List<T> sources, Collection<R> targets) {
        for (T source : sources) {
            if (source.getId() == null) {
                targets.add(fromDto(source));
            }
        }
    }

    private Map<Integer, T> mapDtoById(List<T> dtos) {
        HashMap<Integer, T> map = new HashMap<Integer, T>();
        for (T dto : dtos) {
            map.put(dto.getId(), dto);
        }
        return map;
    }
}
