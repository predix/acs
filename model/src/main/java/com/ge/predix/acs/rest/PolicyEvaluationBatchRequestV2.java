package com.ge.predix.acs.rest;

import java.util.LinkedHashSet;
import java.util.Set;

import com.ge.predix.acs.model.Attribute;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@SuppressWarnings({ "javadoc", "nls" })
@ApiModel(description = "Policy evaluation request for V2.")
public class PolicyEvaluationBatchRequestV2 {

    private String subjectIdentifier;

    private Set<Attribute> subjectAttributes;

    private Set<ResourceRequest> resources;

    private LinkedHashSet<String> policySetsEvaluationOrder = new LinkedHashSet<>();

    @ApiModelProperty(value = "The subject identifier",
            required = true)
    public String getSubjectIdentifier() {
        return this.subjectIdentifier;
    }

    public void setSubjectIdentifier(final String subjectIdentifier) {
        this.subjectIdentifier = subjectIdentifier;
    }

    @ApiModelProperty(value = "Supplemental subject attributes provided by the requestor")
    public Set<Attribute> getSubjectAttributes() {
        return this.subjectAttributes;
    }

    public void setSubjectAttributes(final Set<Attribute> subjectAttributes) {
        this.subjectAttributes = subjectAttributes;
    }

    @ApiModelProperty(value = "resources to be evaluated provided by the requestor")
    public Set<ResourceRequest> getResources() {
        return resources;
    }

    public void setResources(final Set<ResourceRequest> resources) {
        this.resources = resources;
    }

    @ApiModelProperty(value =
            "This list of policy set IDs specifies the order in which the service will evaluate policies. "
                    + "Evaluation stops when a policy with matching target is found and the condition returns true, "
                    + "Or all policies are exhausted.")
    public LinkedHashSet<String> getPolicySetsEvaluationOrder() {
        return this.policySetsEvaluationOrder;
    }

    public void setPolicySetsEvaluationOrder(final LinkedHashSet<String> policySetIds) {
        if (policySetIds != null) {
            this.policySetsEvaluationOrder = policySetIds;
        }
    }

}