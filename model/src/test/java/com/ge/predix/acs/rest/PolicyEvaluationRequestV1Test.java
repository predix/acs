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

package com.ge.predix.acs.rest;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.ge.predix.acs.model.Attribute;

public class PolicyEvaluationRequestV1Test {

    @Test
    public void testEqualsNoAttributes() {
        PolicyEvaluationRequestV1 a = new PolicyEvaluationRequestV1();
        a.setSubjectIdentifier("subject");
        a.setAction("GET");
        a.setResourceIdentifier("/resource");

        PolicyEvaluationRequestV1 b = new PolicyEvaluationRequestV1();
        b.setSubjectIdentifier("subject");
        b.setAction("GET");
        b.setResourceIdentifier("/resource");
        Assert.assertEquals(a, b);
    }

    @Test
    public void testEqualsSameAttributesAndPolicySetsPriority() {
        PolicyEvaluationRequestV1 a = new PolicyEvaluationRequestV1();
        a.setSubjectIdentifier("subject");
        a.setAction("GET");
        a.setResourceIdentifier("/resource");
        a.setSubjectAttributes(
                Arrays.asList(new Attribute[] { new Attribute("issuer", "role"), new Attribute("issuer", "group") }));
        a.setPolicySetsEvaluationOrder(Arrays.asList(new String[] { "P1", "P2" }));

        PolicyEvaluationRequestV1 b = new PolicyEvaluationRequestV1();
        b.setSubjectIdentifier("subject");
        b.setAction("GET");
        b.setResourceIdentifier("/resource");
        b.setSubjectAttributes(
                Arrays.asList(new Attribute[] { new Attribute("issuer", "role"), new Attribute("issuer", "group") }));
        b.setPolicySetsEvaluationOrder(Arrays.asList(new String[] { "P1", "P2" }));
        Assert.assertEquals(a, b);
    }

    @Test
    public void testEqualsThisHasNoAttributes() {
        PolicyEvaluationRequestV1 a = new PolicyEvaluationRequestV1();
        a.setSubjectIdentifier("subject");
        a.setAction("GET");
        a.setResourceIdentifier("/resource");

        PolicyEvaluationRequestV1 b = new PolicyEvaluationRequestV1();
        b.setSubjectIdentifier("subject");
        b.setAction("GET");
        b.setResourceIdentifier("/resource");
        b.setSubjectAttributes(
                Arrays.asList(new Attribute[] { new Attribute("issuer", "role"), new Attribute("issuer", "group") }));
        Assert.assertNotEquals(a, b);
    }

    @Test
    public void testEqualsThatHasNoAttributes() {
        PolicyEvaluationRequestV1 a = new PolicyEvaluationRequestV1();
        a.setSubjectIdentifier("subject");
        a.setAction("GET");
        a.setResourceIdentifier("/resource");
        a.setSubjectAttributes(
                Arrays.asList(new Attribute[] { new Attribute("issuer", "role"), new Attribute("issuer", "group") }));

        PolicyEvaluationRequestV1 b = new PolicyEvaluationRequestV1();
        b.setSubjectIdentifier("subject");
        b.setAction("GET");
        b.setResourceIdentifier("/resource");
        Assert.assertNotEquals(a, b);
    }

    @Test
    public void testEqualsSizeAttributes() {
        PolicyEvaluationRequestV1 a = new PolicyEvaluationRequestV1();
        a.setSubjectIdentifier("subject");
        a.setAction("GET");
        a.setResourceIdentifier("/resource");
        a.setSubjectAttributes(Arrays.asList(new Attribute[] { new Attribute("issuer", "role") }));

        PolicyEvaluationRequestV1 b = new PolicyEvaluationRequestV1();
        b.setSubjectIdentifier("subject");
        b.setAction("GET");
        b.setResourceIdentifier("/resource");
        b.setSubjectAttributes(
                Arrays.asList(new Attribute[] { new Attribute("issuer", "role"), new Attribute("issuer", "group") }));
        Assert.assertNotEquals(a, b);
    }

    @Test
    public void testEqualsDifferentOrderAttributes() {
        PolicyEvaluationRequestV1 a = new PolicyEvaluationRequestV1();
        a.setSubjectIdentifier("subject");
        a.setAction("GET");
        a.setResourceIdentifier("/resource");
        a.setSubjectAttributes(
                Arrays.asList(new Attribute[] { new Attribute("issuer", "group"), new Attribute("issuer", "role") }));

        PolicyEvaluationRequestV1 b = new PolicyEvaluationRequestV1();
        b.setSubjectIdentifier("subject");
        b.setAction("GET");
        b.setResourceIdentifier("/resource");
        b.setSubjectAttributes(
                Arrays.asList(new Attribute[] { new Attribute("issuer", "role"), new Attribute("issuer", "group") }));
        Assert.assertNotEquals(a, b);
    }

    @Test
    public void testEqualsDifferentAttributes() {
        PolicyEvaluationRequestV1 a = new PolicyEvaluationRequestV1();
        a.setSubjectIdentifier("subject");
        a.setAction("GET");
        a.setResourceIdentifier("/resource");
        a.setSubjectAttributes(
                Arrays.asList(new Attribute[] { new Attribute("issuer", "role"), new Attribute("issuer", "site") }));

        PolicyEvaluationRequestV1 b = new PolicyEvaluationRequestV1();
        b.setSubjectIdentifier("subject");
        b.setAction("GET");
        b.setResourceIdentifier("/resource");
        b.setSubjectAttributes(
                Arrays.asList(new Attribute[] { new Attribute("issuer", "role"), new Attribute("issuer", "group") }));
        Assert.assertNotEquals(a, b);
    }

    @Test
    public void testEqualsDifferentOrderPolicySetPriorities() {
        PolicyEvaluationRequestV1 a = new PolicyEvaluationRequestV1();
        a.setSubjectIdentifier("subject");
        a.setAction("GET");
        a.setResourceIdentifier("/resource");
        a.setSubjectAttributes(
                Arrays.asList(new Attribute[] { new Attribute("issuer", "role"), new Attribute("issuer", "site") }));
        a.setPolicySetsEvaluationOrder(Arrays.asList(new String[] { "P1", "P2" }));

        PolicyEvaluationRequestV1 b = new PolicyEvaluationRequestV1();
        b.setSubjectIdentifier("subject");
        b.setAction("GET");
        b.setResourceIdentifier("/resource");
        b.setSubjectAttributes(
                Arrays.asList(new Attribute[] { new Attribute("issuer", "role"), new Attribute("issuer", "site") }));
        a.setPolicySetsEvaluationOrder(Arrays.asList(new String[] { "P2", "P1" }));
        Assert.assertNotEquals(a, b);
    }

    @Test
    public void testHashCodeNoAttributes() {
        PolicyEvaluationRequestV1 a = new PolicyEvaluationRequestV1();
        a.setSubjectIdentifier("subject");
        a.setAction("GET");
        a.setResourceIdentifier("/resource");

        PolicyEvaluationRequestV1 b = new PolicyEvaluationRequestV1();
        b.setSubjectIdentifier("subject");
        b.setAction("GET");
        b.setResourceIdentifier("/resource");
        Assert.assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testHashCodeSameAttributesAndPolicySetsPriority() {
        PolicyEvaluationRequestV1 a = new PolicyEvaluationRequestV1();
        a.setSubjectIdentifier("subject");
        a.setAction("GET");
        a.setResourceIdentifier("/resource");
        a.setSubjectAttributes(
                Arrays.asList(new Attribute[] { new Attribute("issuer", "role"), new Attribute("issuer", "group") }));
        a.setPolicySetsEvaluationOrder(Arrays.asList(new String[] { "P1", "P2" }));

        PolicyEvaluationRequestV1 b = new PolicyEvaluationRequestV1();
        b.setSubjectIdentifier("subject");
        b.setAction("GET");
        b.setResourceIdentifier("/resource");
        b.setSubjectAttributes(
                Arrays.asList(new Attribute[] { new Attribute("issuer", "role"), new Attribute("issuer", "group") }));
        b.setPolicySetsEvaluationOrder(Arrays.asList(new String[] { "P1", "P2" }));
        Assert.assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testHashCodeThisHasNoAttributes() {
        PolicyEvaluationRequestV1 a = new PolicyEvaluationRequestV1();
        a.setSubjectIdentifier("subject");
        a.setAction("GET");
        a.setResourceIdentifier("/resource");

        PolicyEvaluationRequestV1 b = new PolicyEvaluationRequestV1();
        b.setSubjectIdentifier("subject");
        b.setAction("GET");
        b.setResourceIdentifier("/resource");
        b.setSubjectAttributes(
                Arrays.asList(new Attribute[] { new Attribute("issuer", "role"), new Attribute("issuer", "group") }));
        Assert.assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testHashCodeThatHasNoAttributes() {
        PolicyEvaluationRequestV1 a = new PolicyEvaluationRequestV1();
        a.setSubjectIdentifier("subject");
        a.setAction("GET");
        a.setResourceIdentifier("/resource");
        a.setSubjectAttributes(
                Arrays.asList(new Attribute[] { new Attribute("issuer", "role"), new Attribute("issuer", "group") }));

        PolicyEvaluationRequestV1 b = new PolicyEvaluationRequestV1();
        b.setSubjectIdentifier("subject");
        b.setAction("GET");
        b.setResourceIdentifier("/resource");
        Assert.assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testHashCodeSizeAttributes() {
        PolicyEvaluationRequestV1 a = new PolicyEvaluationRequestV1();
        a.setSubjectIdentifier("subject");
        a.setAction("GET");
        a.setResourceIdentifier("/resource");
        a.setSubjectAttributes(Arrays.asList(new Attribute[] { new Attribute("issuer", "role") }));

        PolicyEvaluationRequestV1 b = new PolicyEvaluationRequestV1();
        b.setSubjectIdentifier("subject");
        b.setAction("GET");
        b.setResourceIdentifier("/resource");
        b.setSubjectAttributes(
                Arrays.asList(new Attribute[] { new Attribute("issuer", "role"), new Attribute("issuer", "group") }));
        Assert.assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testHashCodeDifferentOrderAttributes() {
        PolicyEvaluationRequestV1 a = new PolicyEvaluationRequestV1();
        a.setSubjectIdentifier("subject");
        a.setAction("GET");
        a.setResourceIdentifier("/resource");
        a.setSubjectAttributes(
                Arrays.asList(new Attribute[] { new Attribute("issuer", "group"), new Attribute("issuer", "role") }));

        PolicyEvaluationRequestV1 b = new PolicyEvaluationRequestV1();
        b.setSubjectIdentifier("subject");
        b.setAction("GET");
        b.setResourceIdentifier("/resource");
        b.setSubjectAttributes(
                Arrays.asList(new Attribute[] { new Attribute("issuer", "role"), new Attribute("issuer", "group") }));
        Assert.assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testHashCodeDifferentOrderPolicySetsPriority() {
        PolicyEvaluationRequestV1 a = new PolicyEvaluationRequestV1();
        a.setSubjectIdentifier("subject");
        a.setAction("GET");
        a.setResourceIdentifier("/resource");
        a.setPolicySetsEvaluationOrder(Arrays.asList(new String[] { "P1", "P2" }));

        PolicyEvaluationRequestV1 b = new PolicyEvaluationRequestV1();
        b.setSubjectIdentifier("subject");
        b.setAction("GET");
        b.setResourceIdentifier("/resource");
        b.setPolicySetsEvaluationOrder(Arrays.asList(new String[] { "P2", "P1" }));
        Assert.assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testHashCodeDifferentAttributes() {
        PolicyEvaluationRequestV1 a = new PolicyEvaluationRequestV1();
        a.setSubjectIdentifier("subject");
        a.setAction("GET");
        a.setResourceIdentifier("/resource");
        a.setSubjectAttributes(
                Arrays.asList(new Attribute[] { new Attribute("issuer", "role"), new Attribute("issuer", "site") }));

        PolicyEvaluationRequestV1 b = new PolicyEvaluationRequestV1();
        b.setSubjectIdentifier("subject");
        b.setAction("GET");
        b.setResourceIdentifier("/resource");
        b.setSubjectAttributes(
                Arrays.asList(new Attribute[] { new Attribute("issuer", "role"), new Attribute("issuer", "group") }));
        Assert.assertNotEquals(a.hashCode(), b.hashCode());
    }
}
