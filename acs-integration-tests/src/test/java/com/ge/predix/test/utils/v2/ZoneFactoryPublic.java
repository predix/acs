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

import static com.ge.predix.test.utils.ACSTestUtil.ACS_VERSION;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.ge.predix.acs.rest.Zone;

@Component
@Profile({ "public", "public-titan" })
public class ZoneFactoryPublic implements ZoneFactory {

    @Value("${ACS_URL}")
    private String acsBaseUrl;

    @Value("${CF_BASE_DOMAIN:localhost}")
    private String cfBaseDomain;

    public static final String ACS_ZONE_API_PATH = ACS_VERSION + "/zone/";

    @Override
    public Zone createTestZone(final RestTemplate restTemplate, final String zoneId,
            final List<String> trustedIssuerIds) throws IOException {
        Zone zone = new Zone(zoneId, zoneId, "Zone for integration testing.");
        restTemplate.put(this.acsBaseUrl + ACS_ZONE_API_PATH + zoneId, zone);
        return zone;
    }

    @Override
    public HttpStatus deleteZone(final RestTemplate restTemplate, final String zoneName) {
        try {
            restTemplate.delete(this.acsBaseUrl + ACS_ZONE_API_PATH + zoneName);
            return HttpStatus.NO_CONTENT;
        } catch (HttpClientErrorException httpException) {
            return httpException.getStatusCode();
        } catch (RestClientException e) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }



    @Override
    public String getAcsBaseURL() {
        return this.acsBaseUrl;
    }

    @Override
    public String getZoneSpecificUrl(final String zoneId) {
        URI uri = null;
        String zoneurl = null;
        try {
            uri = new URI(this.acsBaseUrl);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        zoneurl = uri.getScheme() + "://" + zoneId + "." + this.cfBaseDomain;
        if (uri.getPort() != -1) {
            zoneurl += ":" + uri.getPort();
        }
        return zoneurl;
    }

}
