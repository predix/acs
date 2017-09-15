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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.ge.predix.acs.utils.JsonUtils;
import com.ge.predix.test.utils.ACSRestTemplateFactory;

public class UaaTestUtil {

    private static final JsonUtils JSON_UTILS = new JsonUtils();

    private final OAuth2RestTemplate adminRestTemplate;
    private final String uaaUrl;
    private final String tokenUrl;
    private final ACSRestTemplateFactory acsRestTemplateFactory;

    public UaaTestUtil(final ACSRestTemplateFactory acsRestTemplateFactory, final String uaaUrl,
            final String adminSecret) {
        this.uaaUrl = uaaUrl;
        this.tokenUrl = this.uaaUrl + "/oauth/token";
        this.acsRestTemplateFactory = acsRestTemplateFactory;
        this.adminRestTemplate = this.acsRestTemplateFactory
                .getOAuth2RestTemplateForUaaAdmin(this.tokenUrl, "admin", adminSecret);
    }

    public void createAcsAdminClient(final String clientId, final String clientSecret) {
        createClientWithAuthorities(clientId, clientSecret,
                Collections.singletonList(new SimpleGrantedAuthority("acs.zones.admin")));
    }

    public OAuth2RestTemplate createAcsAdminClient(final List<String> acsZones) {

        if (this.acsRestTemplateFactory == null) {
            throw new IllegalStateException("ACSRestTemplateFactory is null");
        }
        String clientId = "super-admin-" + acsZones.get(0);
        String clientSecret = "super-adminSecret-" + acsZones.get(0);
        ArrayList<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("acs.zones.admin"));
        authorities.add(new SimpleGrantedAuthority("acs.attributes.read"));
        authorities.add(new SimpleGrantedAuthority("acs.attributes.write"));
        authorities.add(new SimpleGrantedAuthority("acs.policies.read"));
        authorities.add(new SimpleGrantedAuthority("acs.policies.write"));
        authorities.add(new SimpleGrantedAuthority("acs.connectors.read"));
        authorities.add(new SimpleGrantedAuthority("acs.connectors.write"));
        for (int i = 0; i < acsZones.size(); i++) {
            authorities.add(new SimpleGrantedAuthority("predix-acs.zones." + acsZones.get(i) + ".admin"));
            authorities.add(new SimpleGrantedAuthority("predix-acs.zones." + acsZones.get(i) + ".user"));
        }
        createClientWithAuthorities(clientId, clientSecret, authorities);
        return this.acsRestTemplateFactory.getOAuth2RestTemplateForClient(this.tokenUrl, clientId, clientSecret);
    }

    public OAuth2RestTemplate createAcsAdminClientAndGetTemplate(final String zoneName) {
        if (this.acsRestTemplateFactory == null) {
            throw new IllegalStateException("ACSRestTemplateFactory is null");
        }
        String clientId = "admin-" + zoneName;
        String clientSecret = "adminSecret-" + zoneName;
        createAcsAdminClient(clientId, clientSecret);
        return this.acsRestTemplateFactory.getOAuth2RestTemplateForClient(this.tokenUrl, clientId, clientSecret);
    }

    public OAuth2RestTemplate createZoneClientAndGetTemplate(final String zoneName, final String serviceId) {
        if (this.acsRestTemplateFactory == null) {
            throw new IllegalStateException("ACSRestTemplateFactory is null");
        }
        String clientId = "zoneAdmin-" + zoneName;
        String clientSecret = "zoneAdmin-" + zoneName;
        createAcsZoneClient(zoneName, clientId, clientSecret, serviceId);
        return this.acsRestTemplateFactory.getOAuth2RestTemplateForClient(this.tokenUrl, clientId, clientSecret);
    }

    public OAuth2RestTemplate createReadOnlyPolicyScopeClient(final String zoneName) {

        String clientId = "admin-" + zoneName + "-readonly";
        String clientSecret = "adminSecret-" + zoneName + "-readonly";
        createReadOnlyPolicyScopeClient(clientId, clientSecret, zoneName);
        return this.acsRestTemplateFactory.getOAuth2RestTemplateForClient(this.tokenUrl, clientId, clientSecret);
    }

    public void createReadOnlyPolicyScopeClient(final String clientId, final String clientSecret, final String zone) {
        this.createClientWithAuthorities(clientId, clientSecret,
                Arrays.asList(new SimpleGrantedAuthority("predix-acs.zones." + zone + ".user"),
                        new SimpleGrantedAuthority("acs.policies.read")));
    }

    public OAuth2RestTemplate createNoPolicyScopeClient(final String zoneName) {
        String clientId = "admin-" + zoneName + "-nopolicy";
        String clientSecret = "adminSecret-" + zoneName + "-nopolicy";
        this.createNoPolicyScopeClient(clientId, clientSecret, zoneName);
        return this.acsRestTemplateFactory.getOAuth2RestTemplateForClient(this.tokenUrl, clientId, clientSecret);
    }

    public void createNoPolicyScopeClient(final String clientId, final String clientSecret, final String zone) {
        this.createClientWithAuthorities(clientId, clientSecret,
                Collections.singletonList(new SimpleGrantedAuthority("predix-acs.zones." + zone + ".user")));
    }

    public OAuth2RestTemplate createReadOnlyConnectorScopeClient(final String zoneName) {
        String clientId = "admin-connector-" + zoneName + "-readonly";
        String clientSecret = "adminSecret-connector-" + zoneName + "-readonly";
        this.createReadOnlyConnectorScopeClient(clientId, clientSecret, zoneName);
        return this.acsRestTemplateFactory.getOAuth2RestTemplateForClient(this.tokenUrl, clientId, clientSecret);
    }

    public OAuth2RestTemplate createAdminConnectorScopeClient(final String zoneName) {
        String clientId = "admin-connector-" + zoneName;
        String clientSecret = "adminSecret-connector-" + zoneName;
        this.createAdminConnectorScopeClient(clientId, clientSecret, zoneName);
        return this.acsRestTemplateFactory.getOAuth2RestTemplateForClient(this.tokenUrl, clientId, clientSecret);
    }


    public void createReadOnlyConnectorScopeClient(final String zone, final String clientId,
            final String clientSecret) {
        this.createClientWithAuthorities(clientId, clientSecret,
                Arrays.asList(new SimpleGrantedAuthority("predix-acs.zones." + zone + ".user"),
                        new SimpleGrantedAuthority("acs.connectors.read")));
    }

    public void createAdminConnectorScopeClient(final String zone, final String clientId, final String clientSecret) {
        this.createClientWithAuthorities(clientId, clientSecret,
                Arrays.asList(new SimpleGrantedAuthority("predix-acs.zones." + zone + ".user"),
                        new SimpleGrantedAuthority("acs.connectors.read"),
                        new SimpleGrantedAuthority("acs.connectors.write")));
    }

    public void createAcsZoneClient(final String acsZone, final String clientId, final String clientSecret,
            final String serviceId) {
        ArrayList<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>() {{
            add(new SimpleGrantedAuthority("acs.attributes.read"));
            add(new SimpleGrantedAuthority("acs.attributes.write"));
            add(new SimpleGrantedAuthority("acs.policies.read"));
            add(new SimpleGrantedAuthority("acs.policies.write"));
            add(new SimpleGrantedAuthority(serviceId + ".zones." + acsZone + ".user"));
            add(new SimpleGrantedAuthority(serviceId + ".zones." + acsZone + ".admin"));
        }};
        createClientWithAuthorities(clientId, clientSecret, authorities);
    }

    private void createClientWithAuthorities(final String clientId, final String clientSecret,
            final Collection<? extends GrantedAuthority> authorities) {
        BaseClientDetails client = new BaseClientDetails();
        client.setAuthorities(authorities);
        client.setAuthorizedGrantTypes(Collections.singletonList("client_credentials"));
        client.setClientId(clientId);
        client.setClientSecret(clientSecret);
        client.setResourceIds(Collections.singletonList("uaa.none"));
        createOrUpdateClient(client);
    }

    private BaseClientDetails createOrUpdateClient(final BaseClientDetails client) {

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<String> postEntity = new HttpEntity<>(JSON_UTILS.serialize(client), headers);

        ResponseEntity<String> clientCreate = null;
        try {
            clientCreate = this.adminRestTemplate
                    .exchange(this.uaaUrl + "/oauth/clients", HttpMethod.POST, postEntity, String.class);
            if (clientCreate.getStatusCode() == HttpStatus.CREATED) {
                return JSON_UTILS.deserialize(clientCreate.getBody(), BaseClientDetails.class);
            } else {
                throw new RuntimeException("Unexpected return code for client create: " + clientCreate.getStatusCode());
            }
        } catch (InvalidClientException ex) {
            if (ex.getMessage().equals("Client already exists: " + client.getClientId())) {
                HttpEntity<String> putEntity = new HttpEntity<String>(JSON_UTILS.serialize(client), headers);
                ResponseEntity<String> clientUpdate = this.adminRestTemplate
                        .exchange(this.uaaUrl + "/oauth/clients/" + client.getClientId(), HttpMethod.PUT, putEntity,
                                String.class);
                if (clientUpdate.getStatusCode() == HttpStatus.OK) {
                    return JSON_UTILS.deserialize(clientUpdate.getBody(), BaseClientDetails.class);
                } else {
                    throw new RuntimeException(
                            "Unexpected return code for client update: " + clientUpdate.getStatusCode());
                }
            }
        }
        throw new RuntimeException("Unexpected return code for client creation: " + clientCreate.getStatusCode());
    }

    public void deleteClient(final String clientId) {
        this.adminRestTemplate.delete(this.uaaUrl + "/oauth/clients/" + clientId);
    }

}
