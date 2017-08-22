package com.ge.predix.acs.rest;

import java.util.Set;

import com.ge.predix.acs.model.Attribute;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents a Requested Resource in the system identified by a subjectIdentifier.
 */
@ApiModel(description = "Represents a managed protected resource for V2.")
public class ResourceRequest {

    @ApiModelProperty(value = "Resource identifier provided by the requestor")
    private String resourceIdentifier;

    @ApiModelProperty(value = "Supplemental resource attributes provided by the requestor")
    private Set<Attribute> resourceAttributes;

    @ApiModelProperty(value = "Action provided by the requestor")
    private String action;

    public String getResourceIdentifier() {
        return resourceIdentifier;
    }

    public void setResourceIdentifier(String resourceIdentifier) {
        this.resourceIdentifier = resourceIdentifier;
    }

    public Set<Attribute> getResourceAttributes() {
        return resourceAttributes;
    }

    public void setResourceAttributes(Set<Attribute> resourceAttributes) {
        this.resourceAttributes = resourceAttributes;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

}
