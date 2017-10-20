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

package com.ge.predix.acs.obligation.management.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ge.predix.acs.config.InMemoryDataSourceConfig;
import com.ge.predix.acs.testutils.TestActiveProfilesResolver;
import com.ge.predix.acs.zone.management.dao.ZoneEntity;
import com.ge.predix.acs.zone.management.dao.ZoneRepository;

@ContextConfiguration(classes = InMemoryDataSourceConfig.class)
@EnableAutoConfiguration
@ActiveProfiles(resolver = TestActiveProfilesResolver.class)
public class ObligationRepositoryTest extends AbstractTransactionalTestNGSpringContextTests {
    private static final String SUBDOMAIN = "ObligationRepositoryTest-acs";

    @Autowired
    private ObligationRepository obligationRepository;

    @Autowired
    private ZoneRepository zoneRepository;

    @Test
    public void testJPAObligation() {

        ZoneEntity zone = createZone();
        this.zoneRepository.save(zone);
        String obligationId = "obligation-2";
        ObligationEntity obligationEntity = new ObligationEntity(zone, obligationId, "{}");
        ObligationEntity savedObligation = this.obligationRepository.save(obligationEntity);
        Assert.assertNotNull(this.obligationRepository.getByZoneAndObligationId(zone, obligationId));
        Assert.assertEquals(this.obligationRepository.count(), 1);
        Assert.assertTrue(savedObligation.getId() > 0);
        this.obligationRepository.delete(savedObligation.getId());
        Assert.assertEquals(this.obligationRepository.findByZone(zone).size(), 0);
    }

    private ZoneEntity createZone() {
        ZoneEntity zone = new ZoneEntity();
        zone.setName("ObligationRepositoryTest-ACS");
        zone.setSubdomain(SUBDOMAIN);
        zone.setDescription("ObligationRepositoryTest zone description");
        return zone;
    }
}
