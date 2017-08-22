package com.ge.predix.acs.rest;


import com.ge.predix.acs.model.Attribute;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

@SuppressWarnings({ "javadoc", "nls" })
@ApiModel(description = "Policy evaluation request for V1.")
public class PolicyEvaluationBatchRequestV2 {
    @Deprecated
    public static final LinkedHashSet<String> EMPTY_POLICY_EVALUATION_ORDER = new LinkedHashSet<>();

    private String subjectIdentifier;

    private Set<Attribute> subjectAttributes;

    private Set<ResourceRequest> resources;

    private String action;

    private LinkedHashSet<String> policySetsEvaluationOrder = new LinkedHashSet<>();

    @ApiModelProperty(value = "The resource URI to be consumed",
            required = true)
    public String getResourceIdentifier(ResourceRequest resource) {
        return resource.getResourceIdentifier();
    }

    public void setResourceIdentifier(final String resourceUri, ResourceRequest resource) {
        resource.setResourceIdentifier(resourceUri);
    }

    public Set<ResourceRequest> getAllResources() {
        return this.resources;
    }

    public Set<String> getAllResourceIdentifiers(Set<ResourceRequest> resources) {

        Set<String> resourceIdentfiers = null;

        Iterator<ResourceRequest> resourceItr = resources.iterator();
        while(resourceItr.hasNext())
        {
            resourceIdentfiers.add(resourceItr.next().getResourceIdentifier());
        }
        return resourceIdentfiers;
    }


    @ApiModelProperty(value = "The subject identifier",
            required = true)
    public String getSubjectIdentifier() {
        return this.subjectIdentifier;
    }

    public void setSubjectIdentifier(final String subjectIdentifier) {
        this.subjectIdentifier = subjectIdentifier;
    }

    @ApiModelProperty(value = "Supplemental resource attributes provided by the requestor")
    public Set<Attribute> getResourceAttributes(ResourceRequest resource) {
        return resource.getResourceAttributes();
    }

    public void setResourceAttributes(final Set<Attribute> resourceAttributes, ResourceRequest resource) {
        resource.setResourceAttributes(resourceAttributes);
    }

    /**
     * @return the subjectAttributes
     */
    @ApiModelProperty(value = "Supplemental subject attributes provided by the requestor")
    public Set<Attribute> getSubjectAttributes() {
        return this.subjectAttributes;
    }

    /**
     * @param subjectAttributes the subjectAttributes to set
     */
    public void setSubjectAttributes(final Set<Attribute> subjectAttributes) {
        this.subjectAttributes = subjectAttributes;
    }

    @ApiModelProperty(value = "The action on the given resource URI",
            required = true)
    public String getAction() {
        return this.action;
    }

    public void setAction(final String action) {
        this.action = action;
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

    @Override
    public int hashCode() {
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        hashCodeBuilder.append(this.action).append(this.resourceIdentifier).append(this.subjectIdentifier);
        if (null != this.subjectAttributes) {
            for (Attribute attribute : this.subjectAttributes) {
                hashCodeBuilder.append(attribute);
            }
        }
        for (String policyID : this.policySetsEvaluationOrder) {
            hashCodeBuilder.append(policyID);
        }
        return hashCodeBuilder.toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {

        if (obj instanceof PolicyEvaluationBatchRequestV2) {
            final PolicyEvaluationBatchRequestV2 other = (PolicyEvaluationBatchRequestV2) obj;
            EqualsBuilder equalsBuilder = new EqualsBuilder();

            // Element by element comparison may produce true negative in Sets so use built in equals
            // From AbstractSet's (HashSet's ancestor) documentation
            // This implementation first checks if the specified object is this set; if so it returns true.
            // Then, it checks if the specified object is a set whose size is identical to the size of this set;
            // if not, it returns false. If so, it returns containsAll((Collection) o).
            equalsBuilder.append(this.subjectAttributes, other.subjectAttributes);
            equalsBuilder.append(this.policySetsEvaluationOrder, other.policySetsEvaluationOrder);

            equalsBuilder.append(this.action, other.action).append(this.resourceIdentifier, other.resourceIdentifier)
                    .append(this.subjectIdentifier, other.subjectIdentifier);
            return equalsBuilder.isEquals();
        }
        return false;
    }

    //To-Do add all the multiple resource related data to batch request
    @Override
    public String toString() {
        return "PolicyEvaluationRequest [resourceIdentifier=" + this.resourceIdentifier + ", subjectIdentifier="
                + this.subjectIdentifier + ", action=" + this.action + "]";
    }}
