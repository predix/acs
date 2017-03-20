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

package com.ge.predix.acs.policy.evaluation.cache;

import com.ge.predix.acs.model.Effect;
import com.ge.predix.acs.privilege.management.dao.ResourceEntity;
import com.ge.predix.acs.privilege.management.dao.SubjectEntity;
import com.ge.predix.acs.rest.PolicyEvaluationRequestV1;
import com.ge.predix.acs.rest.PolicyEvaluationResult;
import com.ge.predix.acs.zone.management.dao.ZoneEntity;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ge.predix.acs.testutils.XFiles.AGENT_MULDER;
import static com.ge.predix.acs.testutils.XFiles.XFILES_ID;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

public class AbstractPolicyEvaluationCacheTest {

    public static final String ZONE_NAME = "testzone1";
    public static final ZoneEntity ZONE_ENTITY = new ZoneEntity(1L, ZONE_NAME);
    public static final String ACTION_GET = "GET";
    public static final Set<String> EVALUATION_ORDER_POLICYONE_POLICYTWO =
        Stream.of("policyOne", "policyTwo").collect(Collectors.toCollection(LinkedHashSet::new));
    public static final Set<String> EVALUATION_ORDER_POLICYTWO_POLICYONE =
        Stream.of("policyTwo", "policyOne").collect(Collectors.toCollection(LinkedHashSet::new));
    public static final Set<String> EVALUATION_ORDER_POLICYONE =
        Stream.of("policyOne").collect(Collectors.toCollection(LinkedHashSet::new));

    private final InMemoryPolicyEvaluationCache cache = new InMemoryPolicyEvaluationCache();

    @AfterMethod
    public void cleanupTest() {
        this.cache.reset();
    }

    @Test
    public void testGetWithCacheMiss() {

        PolicyEvaluationRequestV1 request = new PolicyEvaluationRequestV1();
        request.setAction(ACTION_GET);
        request.setSubjectIdentifier(AGENT_MULDER);
        request.setResourceIdentifier(XFILES_ID);
        PolicyEvaluationRequestCacheKey key = new PolicyEvaluationRequestCacheKey.Builder().zoneId(ZONE_NAME)
                .request(request).build();
        assertNull(this.cache.get(key));
    }

    @Test
    public void testGetWithCacheHit() {

        PolicyEvaluationRequestV1 request = new PolicyEvaluationRequestV1();
        request.setAction(ACTION_GET);
        request.setSubjectIdentifier(AGENT_MULDER);
        request.setResourceIdentifier(XFILES_ID);
        PolicyEvaluationRequestCacheKey key = new PolicyEvaluationRequestCacheKey.Builder().zoneId(ZONE_NAME)
                .request(request).build();

        PolicyEvaluationResult result = mockPermitResult();
        this.cache.set(key, result);

        PolicyEvaluationResult cachedResult = this.cache.get(key);
        assertEquals(cachedResult.getEffect(), result.getEffect());
    }

    @Test
    public void testGetWithPolicyInvalidation() throws Exception {

        PolicyEvaluationRequestV1 request = new PolicyEvaluationRequestV1();
        request.setAction(ACTION_GET);
        request.setSubjectIdentifier(AGENT_MULDER);
        request.setResourceIdentifier(XFILES_ID);
        request.setPolicySetsEvaluationOrder(EVALUATION_ORDER_POLICYONE);
        PolicyEvaluationRequestCacheKey key = new PolicyEvaluationRequestCacheKey.Builder().zoneId(ZONE_NAME)
                .request(request).build();

        PolicyEvaluationResult result = mockPermitResult();
        this.cache.set(key, result);

        PolicyEvaluationResult cachedResult = this.cache.get(key);
        assertEquals(cachedResult.getEffect(), result.getEffect());

        Thread.sleep(1);
        this.cache.resetForPolicySet(ZONE_NAME, "policyOne");
        assertNull(this.cache.get(key));
    }

    @Test
    public void testGetWithResetForMultiplePolicySets() throws Exception {

        PolicyEvaluationRequestV1 request = new PolicyEvaluationRequestV1();
        request.setAction(ACTION_GET);
        request.setSubjectIdentifier(AGENT_MULDER);
        request.setResourceIdentifier(XFILES_ID);
        request.setPolicySetsEvaluationOrder(EVALUATION_ORDER_POLICYONE_POLICYTWO);
        PolicyEvaluationRequestCacheKey key = new PolicyEvaluationRequestCacheKey.Builder().zoneId(ZONE_NAME)
                .request(request).build();

        PolicyEvaluationResult result = mockPermitResult();
        this.cache.set(key, result);

        PolicyEvaluationResult cachedResult = this.cache.get(key);
        assertNotNull(cachedResult);
        assertEquals(cachedResult.getEffect(), result.getEffect());

        Thread.sleep(1);
        this.cache.resetForPolicySet(ZONE_NAME, "policyTwo");
        assertNull(this.cache.get(key));
    }
    
    @Test
    public void testGetWithPolicyEvaluationOrderChange() throws Exception {

        PolicyEvaluationRequestV1 request = new PolicyEvaluationRequestV1();
        request.setAction(ACTION_GET);
        request.setSubjectIdentifier(AGENT_MULDER);
        request.setResourceIdentifier(XFILES_ID);
        request.setPolicySetsEvaluationOrder(EVALUATION_ORDER_POLICYONE_POLICYTWO);
        PolicyEvaluationRequestCacheKey key = new PolicyEvaluationRequestCacheKey.Builder().zoneId(ZONE_NAME)
                .request(request).build();

        PolicyEvaluationResult result = mockPermitResult();
        this.cache.set(key, result);

        PolicyEvaluationResult cachedResult = this.cache.get(key);
        assertNotNull(cachedResult);
        assertEquals(cachedResult.getEffect(), result.getEffect());

        Thread.sleep(1);

        request.setPolicySetsEvaluationOrder(EVALUATION_ORDER_POLICYTWO_POLICYONE);
        key = new PolicyEvaluationRequestCacheKey.Builder().zoneId(ZONE_NAME).request(request).build();

        assertNull(this.cache.get(key));
    }

    @Test
    public void testGetWithResetForResource() throws Exception {

        PolicyEvaluationRequestV1 request = new PolicyEvaluationRequestV1();
        request.setAction(ACTION_GET);
        request.setSubjectIdentifier(AGENT_MULDER);
        request.setResourceIdentifier(XFILES_ID);
        PolicyEvaluationRequestCacheKey key = new PolicyEvaluationRequestCacheKey.Builder().zoneId(ZONE_NAME)
                .request(request).build();

        PolicyEvaluationResult result = mockPermitResult();
        this.cache.set(key, result);

        PolicyEvaluationResult cachedResult = this.cache.get(key);
        assertEquals(cachedResult.getEffect(), result.getEffect());

        Thread.sleep(1);
        this.cache.resetForResource(ZONE_NAME, XFILES_ID);
        assertNull(this.cache.get(key));
    }

    @Test
    public void testGetWithResetForLongResource() throws Exception {

        PolicyEvaluationRequestV1 request = new PolicyEvaluationRequestV1();
        request.setAction(ACTION_GET);
        request.setSubjectIdentifier(AGENT_MULDER);
        request.setResourceIdentifier("/v1/x-files");
        PolicyEvaluationRequestCacheKey key = new PolicyEvaluationRequestCacheKey.Builder().zoneId(ZONE_NAME)
                .request(request).build();

        PolicyEvaluationResult result = mockPermitResult();
        this.cache.set(key, result);

        PolicyEvaluationResult cachedResult = this.cache.get(key);
        assertEquals(cachedResult.getEffect(), result.getEffect());

        Thread.sleep(1);
        this.cache.resetForResource(ZONE_NAME, XFILES_ID);
        assertNull(this.cache.get(key));
    }

    @Test
    public void testGetWithResetForResources() throws Exception {

        PolicyEvaluationRequestV1 request = new PolicyEvaluationRequestV1();
        request.setAction(ACTION_GET);
        request.setSubjectIdentifier(AGENT_MULDER);
        request.setResourceIdentifier(XFILES_ID);
        PolicyEvaluationRequestCacheKey key = new PolicyEvaluationRequestCacheKey.Builder().zoneId(ZONE_NAME)
                .request(request).build();

        PolicyEvaluationResult result = mockPermitResult();
        this.cache.set(key, result);

        PolicyEvaluationResult cachedResult = this.cache.get(key);
        assertEquals(cachedResult.getEffect(), result.getEffect());

        Thread.sleep(1);
        this.cache.resetForResources(ZONE_NAME,
                Arrays.asList(new ResourceEntity[] { new ResourceEntity(ZONE_ENTITY, XFILES_ID) }));
        assertNull(this.cache.get(key));
    }

    @Test
    public void testGetWithResetForResourcesByIds() throws Exception {

        PolicyEvaluationRequestV1 request = new PolicyEvaluationRequestV1();
        request.setAction(ACTION_GET);
        request.setSubjectIdentifier(AGENT_MULDER);
        request.setResourceIdentifier(XFILES_ID);
        PolicyEvaluationRequestCacheKey key = new PolicyEvaluationRequestCacheKey.Builder().zoneId(ZONE_NAME)
                .request(request).build();

        PolicyEvaluationResult result = mockPermitResult();
        this.cache.set(key, result);

        PolicyEvaluationResult cachedResult = this.cache.get(key);
        assertEquals(cachedResult.getEffect(), result.getEffect());

        Thread.sleep(1);
        this.cache.resetForResourcesByIds(ZONE_NAME, new HashSet<>(Arrays.asList(XFILES_ID)));
        assertNull(this.cache.get(key));
    }

    @Test
    public void testGetWithResetForSubject() throws Exception {

        PolicyEvaluationRequestV1 request = new PolicyEvaluationRequestV1();
        request.setAction(ACTION_GET);
        request.setSubjectIdentifier(AGENT_MULDER);
        request.setResourceIdentifier(XFILES_ID);
        PolicyEvaluationRequestCacheKey key = new PolicyEvaluationRequestCacheKey.Builder().zoneId(ZONE_NAME)
                .request(request).build();

        PolicyEvaluationResult result = mockPermitResult();
        this.cache.set(key, result);

        PolicyEvaluationResult cachedResult = this.cache.get(key);
        assertEquals(cachedResult.getEffect(), result.getEffect());

        Thread.sleep(1);
        this.cache.resetForSubject(ZONE_NAME, AGENT_MULDER);
        assertNull(this.cache.get(key));
    }

    @Test
    public void testGetWithResetForSubjectsByIds() throws Exception {

        PolicyEvaluationRequestV1 request = new PolicyEvaluationRequestV1();
        request.setAction(ACTION_GET);
        request.setSubjectIdentifier(AGENT_MULDER);
        request.setResourceIdentifier(XFILES_ID);
        PolicyEvaluationRequestCacheKey key = new PolicyEvaluationRequestCacheKey.Builder().zoneId(ZONE_NAME)
                .request(request).build();

        PolicyEvaluationResult result = mockPermitResult();
        this.cache.set(key, result);

        PolicyEvaluationResult cachedResult = this.cache.get(key);
        assertEquals(cachedResult.getEffect(), result.getEffect());

        Thread.sleep(1);
        this.cache.resetForSubjectsByIds(ZONE_NAME, new HashSet<>(Arrays.asList(AGENT_MULDER)));
        assertNull(this.cache.get(key));
    }

    @Test
    public void testGetWithResetForSubjects() throws Exception {

        PolicyEvaluationRequestV1 request = new PolicyEvaluationRequestV1();
        request.setAction(ACTION_GET);
        request.setSubjectIdentifier(AGENT_MULDER);
        request.setResourceIdentifier(XFILES_ID);
        PolicyEvaluationRequestCacheKey key = new PolicyEvaluationRequestCacheKey.Builder().zoneId(ZONE_NAME)
                .request(request).build();

        PolicyEvaluationResult result = mockPermitResult();
        this.cache.set(key, result);

        PolicyEvaluationResult cachedResult = this.cache.get(key);
        assertEquals(cachedResult.getEffect(), result.getEffect());

        Thread.sleep(1);
        this.cache.resetForSubjects(ZONE_NAME,
                Arrays.asList(new SubjectEntity[] { new SubjectEntity(ZONE_ENTITY, AGENT_MULDER) }));
        assertNull(this.cache.get(key));
    }

    @Test
    public void testGetWithReset() {

        PolicyEvaluationRequestV1 request = new PolicyEvaluationRequestV1();
        request.setAction(ACTION_GET);
        request.setSubjectIdentifier(AGENT_MULDER);
        request.setResourceIdentifier(XFILES_ID);
        PolicyEvaluationRequestCacheKey key = new PolicyEvaluationRequestCacheKey.Builder().zoneId(ZONE_NAME)
                .request(request).build();

        PolicyEvaluationResult result = mockPermitResult();
        this.cache.set(key, result);

        PolicyEvaluationResult cachedResult = this.cache.get(key);
        assertEquals(cachedResult.getEffect(), result.getEffect());

        this.cache.reset();
        assertNull(this.cache.get(key));
    }

    public static PolicyEvaluationResult mockPermitResult() {
        PolicyEvaluationResult result = new PolicyEvaluationResult(Effect.PERMIT);
        result.setResolvedResourceUris(new HashSet<>(Arrays.asList(new String[] { XFILES_ID })));
        return result;
    }
}
