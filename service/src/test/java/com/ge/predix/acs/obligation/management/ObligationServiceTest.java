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

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ge.predix.acs.SpringSecurityPolicyContextResolver;
import com.ge.predix.acs.config.InMemoryDataSourceConfig;
import com.ge.predix.acs.model.Obligation;
import com.ge.predix.acs.model.ObligationType;
import com.ge.predix.acs.testutils.TestActiveProfilesResolver;
import com.ge.predix.acs.utils.JsonUtils;
import com.ge.predix.acs.zone.management.dao.ZoneEntity;
import com.ge.predix.acs.zone.management.dao.ZoneRepository;
import com.ge.predix.acs.zone.resolver.SpringSecurityZoneResolver;
import com.ge.predix.acs.zone.resolver.ZoneResolver;

@Test
@TestPropertySource("classpath:application.properties")
@ActiveProfiles(resolver = TestActiveProfilesResolver.class)
@ContextConfiguration(
        classes = { InMemoryDataSourceConfig.class, ObligationServiceImpl.class,
                SpringSecurityPolicyContextResolver.class, SpringSecurityZoneResolver.class })
public class ObligationServiceTest extends AbstractTransactionalTestNGSpringContextTests {

    private static final String SUBDOMAIN1 = "obligationTenant1";
    private static final String SUBDOMAIN2 = "obligationTenant2";
    private static final String DEFAULT_SUBDOMAIN = "obligationDefaultTenant";

    @Autowired
    @InjectMocks
    private ObligationServiceImpl obligationService;

    @Autowired
    private ZoneRepository zoneRepository;

    @Mock
    private final ZoneResolver mockZoneResolver = mock(ZoneResolver.class);

    private final JsonUtils jsonUtils = new JsonUtils();

    private final ZoneEntity zone1 = this.createZone("obligationZone1", SUBDOMAIN1, "description for Zone1");
    private final ZoneEntity zone2 = this.createZone("obligationZone2", SUBDOMAIN2, "description for Zone2");
    private final ZoneEntity defaultZone = this.createZone("obligationDefaultZone", DEFAULT_SUBDOMAIN,
            "description for defaultZone");

    @BeforeClass
    public void beforeClass() {
        this.zoneRepository.save(this.zone1);
        this.zoneRepository.save(this.zone2);
        this.zoneRepository.save(this.defaultZone);
    }

    @AfterClass
    public void afterClass() {

        this.zoneRepository.delete(this.defaultZone);
        this.zoneRepository.delete(this.zone1);
        this.zoneRepository.delete(this.zone2);
    }

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initializeDefaultResolverBehavior();
    }

    public void testDeleteWhenObligationExists() {
        Obligation obligation = this.jsonUtils.deserializeFromFile("obligations/obligation.json", Obligation.class);
        this.obligationService.upsertObligation(obligation);
        this.obligationService.deleteObligation(obligation.getName());
        Obligation retrievedObligation = this.obligationService.getObligation(obligation.getName());
        Assert.assertNull(retrievedObligation);
    }

    @Test
    public void testDeleteWhenObligationDoesNotExists() {
        Assert.assertFalse(this.obligationService.deleteObligation("obligationId")); // nothing found
    }

    @Test
    public void testDeleteWhenObligationIdIsNull() {
        Assert.assertFalse(this.obligationService.deleteObligation(null)); // nothing found
    }

    @Test
    public void testCreateObligationPositive() {
        Obligation obligation = this.jsonUtils.deserializeFromFile("obligations/obligation2.json", Obligation.class);
        String obligationName = obligation.getName();
        this.obligationService.upsertObligation(obligation);
        Obligation savedObligation = this.obligationService.getObligation(obligationName);
        Assert.assertNotNull(savedObligation);
        Assert.assertEquals(savedObligation.getName(), "obligation2");
        Assert.assertEquals(savedObligation.getType(), ObligationType.CUSTOM);
        Assert.assertEquals(savedObligation.isOptional(), true);
        Assert.assertEquals(savedObligation.getActionArguments().size(), 2);
        Assert.assertNotNull(savedObligation.getActionTemplate());
        this.obligationService.deleteObligation(obligationName);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateObligation() {
        Obligation obligation = this.jsonUtils.deserializeFromFile("obligations/obligation-update.json",
                Obligation.class);

        this.obligationService.upsertObligation(obligation);

        Obligation retObligation = this.obligationService.getObligation(obligation.getName());
        Assert.assertEquals(retObligation.getName(), obligation.getName());

        HashMap<String, Object> retActionTemplate = (HashMap<String, Object>) retObligation.getActionTemplate();
        HashMap<String, Object> actionTemplate = (HashMap<String, Object>) obligation.getActionTemplate();
        Assert.assertEquals(actionTemplate.keySet().contains("sqlStatement"),
                retActionTemplate.keySet().contains("sqlStatement"));

        Obligation obligationUpdate = this.jsonUtils
                .deserializeFromFile("obligations/obligation-update-replacement.json", Obligation.class);
        this.obligationService.upsertObligation(obligationUpdate);

        Obligation retObligationUpdate = this.obligationService.getObligation(obligationUpdate.getName());

        HashMap<String, Object> retUpdateActionTemplate = (HashMap<String, Object>) retObligationUpdate
                .getActionTemplate();
        HashMap<String, Object> updateActionTemplate = (HashMap<String, Object>) obligationUpdate.getActionTemplate();
        Assert.assertEquals(updateActionTemplate.keySet().contains("resource site"),
                retUpdateActionTemplate.keySet().contains("resource site"));

        this.obligationService.deleteObligation(obligationUpdate.getName());
        Assert.assertEquals(this.obligationService.getObligations().size(), 0);

    }

    @Test
    public void testCreateMultipleObligations() {
        Obligation obligation = this.jsonUtils.deserializeFromFile(" V", Obligation.class);
        Obligation obligation2 = this.jsonUtils.deserializeFromFile("obligations/obligation2.json", Obligation.class);

        List<Obligation> obligations = new ArrayList<Obligation>();
        obligations.add(obligation);
        obligations.add(obligation2);
        this.obligationService.upsertObligations(obligations);
        List<Obligation> expectedObligations = this.obligationService.getObligations();
        Assert.assertEquals(expectedObligations.size(), 2);

        this.obligationService.deleteObligation(obligation.getName());
        this.obligationService.deleteObligation(obligation2.getName());
        Assert.assertEquals(this.obligationService.getObligations().size(), 0);

    }

    @Test(expectedExceptions = { ObligationException.class })
    public void testCreateObligationWithInvalidJson() {
        Obligation obligation = this.jsonUtils.deserializeFromFile("obligations/obligation-invalid.json",
                Obligation.class);
        this.obligationService.upsertObligation(obligation);
    }

    @Test()
    public void testGetAllObligationAndReturnEmptyList() {
        Mockito.when(this.mockZoneResolver.getZoneEntityOrFail()).thenReturn(this.zone1);

        List<Obligation> allObligations = this.obligationService.getObligations();
        Assert.assertEquals(allObligations.size(), 0);
    }

    @Test
    public void testCreateDeleteObligationsForMultipleZones() {
        Obligation zone1Obligation = this.jsonUtils.deserializeFromFile("obligations/obligation.json",
                Obligation.class);
        Obligation zone2Obligation = this.jsonUtils.deserializeFromFile("obligations/obligation2.json",
                Obligation.class);

        Mockito.when(this.mockZoneResolver.getZoneEntityOrFail()).thenReturn(this.zone1);
        this.obligationService.upsertObligation(zone1Obligation);
        Assert.assertEquals(this.obligationService.getObligations().size(), 1);
        this.obligationService.deleteObligation(zone1Obligation.getName());

        Mockito.when(this.mockZoneResolver.getZoneEntityOrFail()).thenReturn(this.zone2);
        this.obligationService.upsertObligation(zone2Obligation);
        Assert.assertEquals(this.obligationService.getObligations().size(), 1);
        this.obligationService.deleteObligation(zone2Obligation.getName());
        Assert.assertEquals(this.obligationService.getObligations().size(), 0);

    }

    private void initializeDefaultResolverBehavior() {
        Mockito.when(this.mockZoneResolver.getZoneEntityOrFail()).thenReturn(this.defaultZone);
    }

    private ZoneEntity createZone(final String name, final String subdomain, final String description) {
        ZoneEntity zone = new ZoneEntity();
        zone.setName(name);
        zone.setSubdomain(subdomain);
        zone.setDescription(description);
        return zone;
    }

}
