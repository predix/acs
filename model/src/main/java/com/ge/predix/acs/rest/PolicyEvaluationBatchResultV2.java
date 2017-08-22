package com.ge.predix.acs.rest;

import java.util.Collections;
import java.util.Set;

import com.ge.predix.acs.model.Attribute;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@SuppressWarnings({ "javadoc", "nls" })
@ApiModel(description = "Policy evaluation Batch result")
public class PolicyEvaluationBatchResultV2 {


    @ApiModelProperty(value = "The collection of the subject's attributes", required = false)
    private Set<Attribute> subjectAttributes = Collections.emptySet();

    @ApiModelProperty(value = "The collection of resource policy evaluations", required = false)
    private Set <ResourceEvaluationResult> resources = Collections.emptySet();

    private long timestamp;

    public Set<Attribute> getSubjectAttributes() {
        return subjectAttributes;
    }

    public void setSubjectAttributes(final Set<Attribute> subjectAttributes) {
        this.subjectAttributes = subjectAttributes;
    }

    public Set<ResourceEvaluationResult> getResources() {
        return resources;
    }

    public void setResources(final Set<ResourceEvaluationResult> resources) {
        this.resources = resources;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }
}
