package com.ge.predix.controller.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ge.predix.acs.model.Effect;
import com.ge.predix.acs.model.PolicySet;
import com.ge.predix.acs.privilege.management.PrivilegeManagementService;
import com.ge.predix.acs.rest.BaseResource;
import com.ge.predix.acs.rest.BaseSubject;
import com.ge.predix.acs.rest.PolicyEvaluationRequestV1;
import com.ge.predix.acs.rest.PolicyEvaluationResult;
import com.ge.predix.acs.rest.Zone;
import com.ge.predix.acs.service.policy.admin.PolicyManagementService;
import com.ge.predix.acs.testutils.MockAcsRequestContext;
import com.ge.predix.acs.testutils.MockMvcContext;
import com.ge.predix.acs.testutils.MockSecurityContext;
import com.ge.predix.acs.testutils.TestActiveProfilesResolver;
import com.ge.predix.acs.testutils.TestUtils;
import com.ge.predix.acs.utils.JsonUtils;
import com.ge.predix.acs.zone.management.ZoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
@ContextConfiguration("classpath:controller-tests-context.xml")
@ActiveProfiles(resolver = TestActiveProfilesResolver.class)
@Test
public class PolicyEvaluationControllerIT extends AbstractTestNGSpringContextTests {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String POLICY_EVAL_URL = "v1/policy-evaluation";

    private final JsonUtils jsonUtils = new JsonUtils();
    private final TestUtils testUtils = new TestUtils();
    private Zone testZone;
    private BaseSubject testSubject;
    private BaseResource testResource;
    private List<PolicySet> denyPolicySet;
    private List<PolicySet> notApplicableAndDenyPolicySets;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ZoneService zoneService;

    @Autowired
    private PrivilegeManagementService privilegeManagementService;

    @Autowired
    private PolicyManagementService policyManagementService;

    @BeforeClass
    public void setup() {
        this.testZone = new TestUtils().createTestZone("PolicyEvaluationControllerITZone");
        this.zoneService.upsertZone(this.testZone);
        MockSecurityContext.mockSecurityContext(this.testZone);
        MockAcsRequestContext.mockAcsRequestContext(this.testZone);

        this.testSubject = new BaseSubject("testSubject");
        this.testResource = new BaseResource("testResource");
        Assert.assertTrue(this.privilegeManagementService.upsertResource(this.testResource));
        Assert.assertTrue(this.privilegeManagementService.upsertSubject(this.testSubject));

        this.denyPolicySet = createDenyPolicySet();
        this.notApplicableAndDenyPolicySets = createNotApplicableAndDenyPolicySets();
    }

    @AfterMethod
    public void testCleanup() {
        List<PolicySet> policySets = this.policyManagementService.getAllPolicySets();
        policySets.forEach(policySet -> this.policyManagementService.deletePolicySet(policySet.getName()));
    }

    @Test(dataProvider = "policyEvalDataProvider")
    public void testPolicyEvaluation(final PolicyEvaluationRequestV1 policyEvalRequest,
            final List<PolicySet> policySets, final Effect expectedEffect) throws Exception {

        if (policySets != null) {
            upsertMultiplePolicySets(policySets);
        }

        MockMvcContext postPolicyEvalContext = this.testUtils.createWACWithCustomPOSTRequestBuilder(this.wac,
                this.testZone.getSubdomain(), POLICY_EVAL_URL);
        MvcResult mvcResult = postPolicyEvalContext.getMockMvc()
                .perform(postPolicyEvalContext.getBuilder().contentType(MediaType.APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(policyEvalRequest)))
                .andExpect(status().isOk()).andReturn();
        PolicyEvaluationResult policyEvalResult = OBJECT_MAPPER
                .readValue(mvcResult.getResponse().getContentAsByteArray(), PolicyEvaluationResult.class);

        assertThat(policyEvalResult.getEffect(), equalTo(expectedEffect));
    }

    @Test(dataProvider = "policyEvalBadRequestDataProvider")
    public void testPolicyEvaluationBadRequest(final PolicyEvaluationRequestV1 policyEvalRequest,
            final List<PolicySet> policySets) throws Exception {

        upsertMultiplePolicySets(policySets);

        MockMvcContext postPolicyEvalContext = this.testUtils.createWACWithCustomPOSTRequestBuilder(this.wac,
                this.testZone.getSubdomain(), POLICY_EVAL_URL);
        postPolicyEvalContext.getMockMvc()
                .perform(postPolicyEvalContext.getBuilder().contentType(MediaType.APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(policyEvalRequest)))
                .andExpect(status().isBadRequest());
    }

    @DataProvider(name = "policyEvalDataProvider")
    private Object[][] policyEvalDataProvider() {
        return new Object[][] { requestEvaluationWithEmptyPolicySet(),
                requestEvaluationWithOnePolicySetAndEmptyPriorityList(),
                requestEvaluationWithOnePolicySetAndPriorityList(), requestEvaluationWithAllOfTwoPolicySets(),
                requestEvaluationWithFirstOfTwoPolicySets(), requestEvaluationWithSecondOfTwoPolicySets() };
    }

    private Object[] requestEvaluationWithEmptyPolicySet() {
        return new Object[] {
                createPolicyEvalRequest("GET", this.testResource.getResourceIdentifier(),
                                        this.testSubject.getSubjectIdentifier(),
                                        PolicyEvaluationRequestV1.getEmptyPolicyEvaluationOrder()),
                Collections.emptyList(), Effect.NOT_APPLICABLE };
    }

    private Object[] requestEvaluationWithSecondOfTwoPolicySets() {
        return new Object[] {
                createPolicyEvalRequest("GET", this.testResource.getResourceIdentifier(),
                        this.testSubject.getSubjectIdentifier(),
                        Stream.of(this.notApplicableAndDenyPolicySets.get(1).getName())
                                .collect(Collectors.toCollection(LinkedHashSet::new))),
                this.notApplicableAndDenyPolicySets, Effect.DENY };
    }

    private Object[] requestEvaluationWithFirstOfTwoPolicySets() {
        return new Object[] {
                createPolicyEvalRequest("GET", this.testResource.getResourceIdentifier(),
                        this.testSubject.getSubjectIdentifier(),
                        Stream.of(this.notApplicableAndDenyPolicySets.get(0).getName())
                                .collect(Collectors.toCollection(LinkedHashSet::new))),
                this.notApplicableAndDenyPolicySets, Effect.NOT_APPLICABLE };
    }

    private Object[] requestEvaluationWithOnePolicySetAndPriorityList() {
        return new Object[] {
                createPolicyEvalRequest("GET", this.testResource.getResourceIdentifier(),
                        this.testSubject.getSubjectIdentifier(),
                        Stream.of(this.denyPolicySet.get(0).getName())
                                .collect(Collectors.toCollection(LinkedHashSet::new))),
                this.denyPolicySet, Effect.DENY };
    }

    private Object[] requestEvaluationWithOnePolicySetAndEmptyPriorityList() {
        return new Object[] {
                createPolicyEvalRequest("GET", this.testResource.getResourceIdentifier(),
                                        this.testSubject.getSubjectIdentifier(),
                                        PolicyEvaluationRequestV1.getEmptyPolicyEvaluationOrder()),
                this.denyPolicySet, Effect.DENY };
    }

    private Object[] requestEvaluationWithAllOfTwoPolicySets() {
        return new Object[] {
                createPolicyEvalRequest("GET", this.testResource.getResourceIdentifier(),
                        this.testSubject.getSubjectIdentifier(),
                        Stream.of(this.notApplicableAndDenyPolicySets.get(0).getName(),
                                this.notApplicableAndDenyPolicySets.get(1).getName())
                                .collect(Collectors.toCollection(LinkedHashSet::new))),
                this.notApplicableAndDenyPolicySets, Effect.DENY };

    }

    @DataProvider(name = "policyEvalBadRequestDataProvider")
    private Object[][] policyEvalBadRequestDataProvider() {
        return new Object[][] { requestEvaluationWithNonExistentPolicySet(),
                requestEvaluationWithTwoPolicySetsAndNoPriorityList(),
                requestEvaluationWithExistentAndNonExistentPolicySets() };
    }

    private Object[] requestEvaluationWithExistentAndNonExistentPolicySets() {
        return new Object[] {
                createPolicyEvalRequest("GET", this.testResource.getResourceIdentifier(),
                        this.testSubject.getSubjectIdentifier(),
                        Stream.of(this.notApplicableAndDenyPolicySets.get(0).getName(), "noexistent-policy-set")
                                .collect(Collectors.toCollection(LinkedHashSet::new))),
                this.notApplicableAndDenyPolicySets };
    }

    private Object[] requestEvaluationWithTwoPolicySetsAndNoPriorityList() {
        return new Object[] {
                createPolicyEvalRequest("GET", this.testResource.getResourceIdentifier(),
                                        this.testSubject.getSubjectIdentifier(),
                                        PolicyEvaluationRequestV1.getEmptyPolicyEvaluationOrder()),
                this.notApplicableAndDenyPolicySets };
    }

    private Object[] requestEvaluationWithNonExistentPolicySet() {
        return new Object[] { createPolicyEvalRequest("GET", this.testResource.getResourceIdentifier(),
                this.testSubject.getSubjectIdentifier(),
                Stream.of("nonexistent-policy-set").collect(Collectors.toCollection(LinkedHashSet::new))),
                this.denyPolicySet };
    }

    private PolicyEvaluationRequestV1 createPolicyEvalRequest(final String action, final String resourceIdentifier,
            final String subjectIdentifier, final LinkedHashSet<String> policySetsPriority) {
        PolicyEvaluationRequestV1 policyEvalRequest = new PolicyEvaluationRequestV1();
        policyEvalRequest.setAction("GET");
        policyEvalRequest.setResourceIdentifier(resourceIdentifier);
        policyEvalRequest.setSubjectIdentifier(subjectIdentifier);
        policyEvalRequest.setPolicySetsEvaluationOrder(policySetsPriority);
        return policyEvalRequest;
    }

    private List<PolicySet> createDenyPolicySet() {
        List<PolicySet> policySets = new ArrayList<PolicySet>();
        policySets.add(this.jsonUtils.deserializeFromFile("policies/testPolicyEvalDeny.json", PolicySet.class));
        Assert.assertNotNull(policySets, "Policy set file is not found or invalid");
        return policySets;
    }

    private List<PolicySet> createNotApplicableAndDenyPolicySets() {
        List<PolicySet> policySets = new ArrayList<PolicySet>();
        policySets
                .add(this.jsonUtils.deserializeFromFile("policies/testPolicyEvalNotApplicable.json", PolicySet.class));
        policySets.add(this.jsonUtils.deserializeFromFile("policies/testPolicyEvalDeny.json", PolicySet.class));
        Assert.assertNotNull(policySets, "Policy set files are not found or invalid");
        Assert.assertTrue(policySets.size() == 2, "One or more policy set files are not found or invalid");
        return policySets;
    }

    private void upsertPolicySet(final PolicySet policySet) {
        this.policyManagementService.upsertPolicySet(policySet);
        Assert.assertNotNull(this.policyManagementService.getPolicySet(policySet.getName()));
    }

    private void upsertMultiplePolicySets(final List<PolicySet> policySets) {
        policySets.forEach(policySet -> upsertPolicySet(policySet));
        return;
    }
}
