package com.ge.predix.acs.rest;

import com.ge.predix.acs.model.Attribute;

import java.util.Set;

public class ResourceRequest {


    private String resourceIdentifier;

    private Set<Attribute> resourceAttributes;

    private Set<String> actions;

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

    public Set<String> getActions() {
        return actions;
    }

    public void setActions(Set<String> actions) {
        this.actions = actions;
    }

}
