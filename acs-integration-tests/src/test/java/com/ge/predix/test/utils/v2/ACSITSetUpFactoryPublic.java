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
import java.util.Arrays;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Component;

import com.ge.predix.acs.rest.Zone;
import com.ge.predix.test.utils.ACSRestTemplateFactory;
import com.ge.predix.test.utils.ACSTestUtil;
import com.ge.predix.test.utils.PolicyHelper;

@Component
@Scope("prototype")
public class ACSITSetUpFactoryPublic implements ACSITSetUpFactory {

    @Autowired
    private ACSRestTemplateFactory acsRestTemplateFactory;

    private String acsZone1Name;
    private String acsZone2Name;
    private String acsZone3Name;

    @Value("${ACS_TESTING_UAA}")
    private String uaaUrl;

    @Value("${UAA_ADMIN_SECRET:adminsecret}")
    private String uaaAdminSecret;

    @Value("${ACS_SERVICE_ID:predix-acs}")
    private String serviceId;

    private String acsUrl;
    private HttpHeaders zone1Headers;
    private HttpHeaders zone3Headers;
    private OAuth2RestTemplate acsAdminRestTemplate;
    private OAuth2RestTemplate acsAdminRestTemplate2;
    private OAuth2RestTemplate acsZoneRestTemplate;
    private OAuth2RestTemplate acsZone2RestTemplate;
    private OAuth2RestTemplate acsReadOnlyRestTemplate;
    private OAuth2RestTemplate acsNoPolicyScopeRestTemplate;
    private Zone zone1;
    private Zone zone2;

    private UaaTestUtil uaaTestUtil;

    @Autowired
    private ZoneFactory zoneFactory;

    @Override
    public void setUp() throws IOException {
        // TestConfig.setupForEclipse(); // Starts ACS when running the test in eclipse.
        this.acsUrl = this.zoneFactory.getAcsBaseURL();

        this.acsZone1Name = ZoneFactory.getRandomName(this.getClass().getSimpleName());
        this.acsZone2Name = ZoneFactory.getRandomName(this.getClass().getSimpleName());
        this.acsZone3Name = ZoneFactory.getRandomName(this.getClass().getSimpleName());

        this.zone1Headers = ACSTestUtil.httpHeaders();
        this.zone1Headers.set(PolicyHelper.PREDIX_ZONE_ID, this.acsZone1Name);

        this.zone3Headers = ACSTestUtil.httpHeaders();
        this.zone3Headers.set(PolicyHelper.PREDIX_ZONE_ID, this.acsZone3Name);

        this.uaaTestUtil = new UaaTestUtil(this.acsRestTemplateFactory, this.uaaUrl, this.uaaAdminSecret);

        this.acsAdminRestTemplate = this.uaaTestUtil.createAcsAdminClientAndGetTemplate(this.acsZone1Name);
        this.acsAdminRestTemplate2 = this.uaaTestUtil.createAcsAdminClient(Arrays.asList(new String[] { this
                .acsZone1Name, this.acsZone2Name, this.acsZone3Name }));
        this.acsReadOnlyRestTemplate = this.uaaTestUtil.createReadOnlyPolicyScopeClient(this.acsZone1Name);
        this.acsNoPolicyScopeRestTemplate = this.uaaTestUtil.createNoPolicyScopeClient(this.acsZone1Name);
        this.zone1 = this.zoneFactory.createTestZone(this.acsAdminRestTemplate, this.acsZone1Name,
                Collections.singletonList(this.uaaUrl + "/oauth/token"));
        this.acsZoneRestTemplate = this.uaaTestUtil.createZoneClientAndGetTemplate(this.acsZone1Name, this.serviceId);

        this.zone2 = this.zoneFactory.createTestZone(this.acsAdminRestTemplate, this.acsZone2Name,
                Collections.singletonList(this.uaaUrl + "/oauth/token"));
        this.acsZone2RestTemplate = this.uaaTestUtil.createZoneClientAndGetTemplate(this.acsZone2Name, this.serviceId);
    }

    @Override
    public void destroy() {
        UaaTestUtil uaaTestUtil2 = new UaaTestUtil(this.acsRestTemplateFactory, this.uaaUrl, this.uaaAdminSecret);
        this.zoneFactory.deleteZone(this.acsAdminRestTemplate, this.acsZone1Name);
        this.zoneFactory.deleteZone(this.acsAdminRestTemplate, this.acsZone2Name);
        uaaTestUtil2.deleteClient(this.acsAdminRestTemplate.getResource().getClientId());
        uaaTestUtil2.deleteClient(this.acsZoneRestTemplate.getResource().getClientId());
        uaaTestUtil2.deleteClient(this.acsZone2RestTemplate.getResource().getClientId());
        uaaTestUtil2.deleteClient(this.acsReadOnlyRestTemplate.getResource().getClientId());
        uaaTestUtil2.deleteClient(this.acsNoPolicyScopeRestTemplate.getResource().getClientId());
    }


    @Override
    public String getAcsUrl() {

        return this.acsUrl;
    }

    @Override
    public HttpHeaders getZone1Headers() {

        return this.zone1Headers;
    }


    @Override
    public OAuth2RestTemplate getAcsZoneAdminRestTemplate() {

        return this.acsZoneRestTemplate;
    }

    @Override
    public OAuth2RestTemplate getAcsZone2AdminRestTemplate() {

        return this.acsZone2RestTemplate;
    }

    @Override
    public OAuth2RestTemplate getAcsReadOnlyRestTemplate() {

        return this.acsReadOnlyRestTemplate;
    }

    @Override
    public OAuth2RestTemplate getAcsNoPolicyScopeRestTemplate() {

        return this.acsNoPolicyScopeRestTemplate;
    }

    @Override
    public Zone getZone1() {
        return this.zone1;
    }

    @Override
    public Zone getZone2() {
        return this.zone2;
    }

    @Override
    public String getAcsZone3Name() {
        return this.acsZone3Name;
    }

    @Override
    public HttpHeaders getZone3Headers() {
        return this.zone3Headers;
    }

    @Override
    public OAuth2RestTemplate getAcsAdminRestTemplate2() {
        return this.acsAdminRestTemplate2;
    }


}