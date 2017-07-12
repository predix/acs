/*******************************************************************************
 * Copyright 2016 General Electric Company.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.ge.predix.acs.zone.resolver;

import com.ge.predix.acs.request.context.AcsRequestContext;
import com.ge.predix.acs.request.context.AcsRequestContext.ACSRequestContextAttribute;
import com.ge.predix.acs.request.context.AcsRequestContextHolder;
import com.ge.predix.acs.service.InvalidACSRequestException;
import com.ge.predix.acs.zone.management.dao.ZoneEntity;
import com.ge.predix.uaa.token.lib.ZoneOAuth2Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SpringSecurityZoneResolver implements ZoneResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringSecurityZoneResolver.class);

    public static ZoneEntity getZoneEntity() {
        AcsRequestContext acsRequestContext = AcsRequestContextHolder.getAcsRequestContext();
        ZoneEntity result = (ZoneEntity) acsRequestContext.get(ACSRequestContextAttribute.ZONE_ENTITY);

        // This could happen if a zone was removed from ACS but a registration still exists in ZAC
        if (null == result) {
            ZoneOAuth2Authentication zoneAuth = (ZoneOAuth2Authentication) SecurityContextHolder.getContext()
                    .getAuthentication();
            throw new InvalidACSRequestException("The zone '" + zoneAuth.getZoneId() + "' does not exist.");
        }
        return result;
    }
    
    @Override
    public ZoneEntity getZoneEntityOrFail() {
        ZoneEntity result = null;
        try {
        result = getZoneEntity();
        } catch (InvalidACSRequestException e) {
            LOGGER.debug(e.getMessage());
            return null;
        }
        return result;
    }
    
    public static String getZoneName() {
        String result = null;
        try {
            result = getZoneEntity().getName();
        } catch (InvalidACSRequestException e) {
            LOGGER.debug(e.getMessage());
            return null;
        }
        return result;
    }
}
