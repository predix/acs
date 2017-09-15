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

package com.ge.predix.test.utils.v2;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

import com.ge.predix.acs.rest.Zone;

/**
 * Provides functionality that helps the create and setups of zones for acs.
 * 
 * @author Sebastian Torres Brown
 * 
 */
public interface ZoneFactory {

    /**
     * Creates a random zone name.
     * 
     * @param clazz
     *            a classname to pass
     * @return String randomize name of the zone
     */
    static String getRandomName(final String clazz) {
        return clazz + UUID.randomUUID().toString();
    }

    /**
     * Creates desired Zone.
     * 
     * @param restTemplate
     * @param zoneId
     * @param trustedIssuerIds
     * @return Zone
     * @throws IOException
     */
    Zone createTestZone(RestTemplate restTemplate, String zoneId, List<String> trustedIssuerIds) throws IOException;

    /**
     * makes a client call that deletes the desired zone.
     * 
     * @param restTemplate
     * @param zoneName
     * @return HttpStatus
     */
    HttpStatus deleteZone(RestTemplate restTemplate, String zoneName);

    /**
     * Returns ACS service base URL.
     * 
     * @return string URL
     */
    String getAcsBaseURL();

    /**
     * Returns a zone specific url.
     *
     * @param zoneId
     * @return
     */
    String getZoneSpecificUrl(String zoneId);

}
