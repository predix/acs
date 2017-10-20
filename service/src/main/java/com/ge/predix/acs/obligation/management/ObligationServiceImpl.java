/*******************************************************************************
 * Copyright 2017 General Electric Company
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.ge.predix.acs.obligation.management;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ge.predix.acs.commons.exception.UntrustedIssuerException;
import com.ge.predix.acs.model.Obligation;
import com.ge.predix.acs.obligation.management.dao.ObligationEntity;
import com.ge.predix.acs.obligation.management.dao.ObligationRepository;
import com.ge.predix.acs.utils.JsonUtils;
import com.ge.predix.acs.zone.management.dao.ZoneEntity;
import com.ge.predix.acs.zone.resolver.ZoneResolver;

/**
 * @author Sebastian Torres Brown
 *
 */
@Service
public class ObligationServiceImpl implements ObligationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObligationServiceImpl.class);

    @Autowired
    private ObligationRepository obligationRepository;

    @Autowired
    private ZoneResolver zoneResolver;

    private final JsonUtils jsonUtils = new JsonUtils();

    @Transactional
    @Override
    public void upsertObligation(final Obligation obligation) {
        try {
            ZoneEntity zone = this.zoneResolver.getZoneEntityOrFail();
            upsertObligation(zone, obligation);
        } catch (Exception e) {
            handleException(e, obligation.getName());
        }
    }

    @Transactional
    @Override
    public void upsertObligations(final List<Obligation> obligations) {
        String obligationName = "";
        try {
            ZoneEntity zone = this.zoneResolver.getZoneEntityOrFail();
            for (Obligation obligation : obligations) {
                obligationName = obligation.getName();
                upsertObligation(zone, obligation);
            }
        } catch (Exception e) {
            handleException(e, obligationName);
        }
    }

    @Override
    public List<Obligation> retrieveObligations() {
        ZoneEntity zone = this.zoneResolver.getZoneEntityOrFail();
        ArrayList<Obligation> result = new ArrayList<>();
        List<ObligationEntity> obligationEnityList = this.obligationRepository.findByZone(zone);
        for (ObligationEntity obligationEntity : obligationEnityList) {
            result.add(this.jsonUtils.deserialize(obligationEntity.getObligationJson(), Obligation.class));
        }
        return result;
    }

    @Override
    public Obligation retrieveObligation(final String obligationName) {
        ZoneEntity zone = this.zoneResolver.getZoneEntityOrFail();
        ObligationEntity obligationEntity = this.obligationRepository.getByZoneAndObligationId(zone, obligationName);
        if (obligationEntity != null) {
            return this.jsonUtils.deserialize(obligationEntity.getObligationJson(), Obligation.class);
        }
        LOGGER.debug("No policy set found for obligationName = {},  zone = {}.", obligationName, zone);
        return null;
    }

    @Override
    public Boolean deleteObligation(final String obligationName) {
        ZoneEntity zone = this.zoneResolver.getZoneEntityOrFail();
        ObligationEntity obligationEntity = this.obligationRepository.getByZoneAndObligationId(zone, obligationName);
        if (obligationEntity != null) {
            LOGGER.info("Found an existing obligation obligationName={}, zone={}, deleting now.", obligationName,
                    zone.getName());
            this.obligationRepository.delete(obligationEntity);
        } else {
            LOGGER.debug("Cound not find an existing policy set " + "obligationName={}, zone={}, Could not delete it.",
                    obligationName, zone.getName());
            return false;
        }
        return true;
    }

    private void upsertObligation(final ZoneEntity zone, final Obligation obligation) {
        String obligationName = obligation.getName();
        String obligationPayload = this.jsonUtils.serialize(obligation);
        upsertObligation(zone, obligationName, obligationPayload);

    }

    private void upsertObligation(final ZoneEntity zone, final String obligationName, final String obligationPayload) {
        ObligationEntity existingObligationEntity = this.obligationRepository.getByZoneAndObligationId(zone,
                obligationName);
        ObligationEntity obligationEntity = new ObligationEntity(zone, obligationName, obligationPayload);

        // If policy Set already exists, set PK of entity for update
        if (null != existingObligationEntity) {
            LOGGER.debug("Found an obligation set obligationName = {}, zone = {}, upserting now .", obligationName,
                    zone);
            obligationEntity.setId(existingObligationEntity.getId());
        } else {
            LOGGER.debug("No existing obligation found for obligationName = {},  zone = {}, inserting now .",
                    obligationName, zone);
        }
        this.obligationRepository.save(obligationEntity);

    }

    private void handleException(final Exception e, final String obligationName) {

        String message = String.format("Creation of Obligation %s failed with the following error %s", obligationName,
                e.getMessage());
        LOGGER.error(message, e);

        if (e instanceof UntrustedIssuerException || e instanceof ObligationException) {
            throw (RuntimeException) e;
        }
        throw new ObligationException(message, e);
    }

}
