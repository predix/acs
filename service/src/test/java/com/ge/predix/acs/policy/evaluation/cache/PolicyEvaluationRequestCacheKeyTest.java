package com.ge.predix.acs.policy.evaluation.cache;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import com.ge.predix.acs.rest.PolicyEvaluationRequestV1;

public class PolicyEvaluationRequestCacheKeyTest {
    public static final String ZONE_NAME = "testzone1";

    @Test
    public void testBuild() {
        String subjectId = "mulder";
        String resourceId = "/x-files";
        List<String> policyEvaluationOrder = Arrays.asList(new String[] { "policyOne" });
        PolicyEvaluationRequestCacheKey key = new PolicyEvaluationRequestCacheKey.Builder().zoneId(ZONE_NAME).resourceId(resourceId).subjectId(subjectId)
                .policySetIds(policyEvaluationOrder).build();

        assertEquals(key.getZoneId(), ZONE_NAME);
        assertEquals(key.getSubjectId(), subjectId);
        assertEquals(key.getResourceId(), resourceId);
        assertEquals(key.getPolicySetIds(), policyEvaluationOrder);
        assertNull(key.getRequest());
    }

    @Test
    public void testBuildByRequest() {
        PolicyEvaluationRequestV1 request = new PolicyEvaluationRequestV1();
        request.setAction("GET");
        request.setSubjectIdentifier("mulder");
        request.setResourceIdentifier("/x-files");
        request.setPolicySetsEvaluationOrder(Arrays.asList(new String[] { "policyOne" }));
        PolicyEvaluationRequestCacheKey key = new PolicyEvaluationRequestCacheKey.Builder().zoneId(ZONE_NAME)
                .request(request).build();

        assertEquals(key.getZoneId(), ZONE_NAME);
        assertEquals(key.getSubjectId(), request.getSubjectIdentifier());
        assertEquals(key.getResourceId(), request.getResourceIdentifier());
        assertEquals(key.getPolicySetIds(), request.getPolicySetsEvaluationOrder());
        assertEquals(key.getRequest(), request);
    }

    @Test
    public void testBuildByRequestAndPolicySetEvaluationOrder() {
        PolicyEvaluationRequestV1 request = new PolicyEvaluationRequestV1();
        request.setAction("GET");
        request.setSubjectIdentifier("mulder");
        request.setResourceIdentifier("/x-files");
        List<String> policyEvaluationOrder = Arrays.asList(new String[] { "policyOne" });
        PolicyEvaluationRequestCacheKey key = new PolicyEvaluationRequestCacheKey.Builder().zoneId(ZONE_NAME)
                .policySetIds(policyEvaluationOrder).request(request).build();

        assertEquals(key.getZoneId(), ZONE_NAME);
        assertEquals(key.getSubjectId(), request.getSubjectIdentifier());
        assertEquals(key.getResourceId(), request.getResourceIdentifier());
        assertEquals(key.getPolicySetIds(), policyEvaluationOrder);
        assertEquals(key.getRequest(), request);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testIllegalStateExceptionForSettingPolicySetIds() {
        PolicyEvaluationRequestV1 request = new PolicyEvaluationRequestV1();
        request.setPolicySetsEvaluationOrder(Arrays.asList(new String[] { "policyOne" }));
        new PolicyEvaluationRequestCacheKey.Builder().zoneId(ZONE_NAME)
                .request(request).policySetIds(request.getPolicySetsEvaluationOrder()).build();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testIllegalStateExceptionForSettingSubjectId() {
        PolicyEvaluationRequestV1 request = new PolicyEvaluationRequestV1();
        new PolicyEvaluationRequestCacheKey.Builder().zoneId(ZONE_NAME)
                .request(request).subjectId("subject").build();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testIllegalStateExceptionForSettingResourceId() {
        PolicyEvaluationRequestV1 request = new PolicyEvaluationRequestV1();
        new PolicyEvaluationRequestCacheKey.Builder().zoneId(ZONE_NAME)
                .request(request).resourceId("resource").build();
    }

    @Test
    public void testKeyEqualsForSameRequests() {
        PolicyEvaluationRequestV1 request = new PolicyEvaluationRequestV1();
        request.setAction("GET");
        request.setSubjectIdentifier("mulder");
        request.setResourceIdentifier("/x-files");
        request.setPolicySetsEvaluationOrder(Arrays.asList(new String[] { "policyOne" }));
        PolicyEvaluationRequestCacheKey key = new PolicyEvaluationRequestCacheKey.Builder().zoneId(ZONE_NAME)
                .request(request).build();

        PolicyEvaluationRequestV1 otherRequest = new PolicyEvaluationRequestV1();
        otherRequest.setAction("GET");
        otherRequest.setSubjectIdentifier("mulder");
        otherRequest.setResourceIdentifier("/x-files");
        otherRequest.setPolicySetsEvaluationOrder(Arrays.asList(new String[] { "policyOne" }));
        PolicyEvaluationRequestCacheKey otherKey = new PolicyEvaluationRequestCacheKey.Builder().zoneId(ZONE_NAME)
                .request(otherRequest).build();
        assertTrue(key.equals(otherKey));
    }

    @Test
    public void testKeyEqualsForDifferentRequests() {
        PolicyEvaluationRequestV1 request = new PolicyEvaluationRequestV1();
        request.setAction("GET");
        request.setSubjectIdentifier("mulder");
        request.setResourceIdentifier("/x-files");
        request.setPolicySetsEvaluationOrder(Arrays.asList(new String[] { "policyOne" }));
        PolicyEvaluationRequestCacheKey key = new PolicyEvaluationRequestCacheKey.Builder().zoneId(ZONE_NAME)
                .request(request).build();

        PolicyEvaluationRequestV1 otherRequest = new PolicyEvaluationRequestV1();
        otherRequest.setAction("GET");
        otherRequest.setSubjectIdentifier("mulder");
        otherRequest.setResourceIdentifier("/x-files");
        PolicyEvaluationRequestCacheKey otherKey = new PolicyEvaluationRequestCacheKey.Builder().zoneId(ZONE_NAME)
                .request(otherRequest).build();
        assertFalse(key.equals(otherKey));
    }

    @Test
    public void testToRedisKey() {
        PolicyEvaluationRequestV1 request = new PolicyEvaluationRequestV1();
        PolicyEvaluationRequestCacheKey key = new PolicyEvaluationRequestCacheKey.Builder().zoneId(ZONE_NAME)
                .request(request).build();
        assertEquals(key.toRedisKey(), ZONE_NAME + ":*:*:" + Integer.toHexString(request.hashCode()));
    }
}
