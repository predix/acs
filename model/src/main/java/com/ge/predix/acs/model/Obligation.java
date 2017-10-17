package com.ge.predix.acs.model;

import java.util.Collections;
import java.util.List;

import io.swagger.annotations.ApiModel;

/**
 * @author Sebastian Torres Brown
 * 
 *         Keti obligations model defined by XACML 3.O
 *
 */
@ApiModel(
        description = "Obligation augments the interface contract between the policy engine (PDP) and "
                + "enforcement point (PEP).  In addition to the actual access decision of either 'Permit' or 'Deny', "
                + "the policy engine may return some supplementary information to the enforcement point in the form"
                + " of an Obligation. Obligations are a set of operations that must be performed by the PEP "
                + "in conjunction with an authorization decision, e.g., 'Permit, and in addition fulfill the "
                + "indicated Obligation(s)' or, alternatively: 'Deny, and in addition fulfill the indicated"
                + " Obligation(s)'.  If the PEP does not understand, or cannot fulfill, any of the obligations, "
                + "then it MUST act as if the PDP had returned a 'Deny' authorization decision value.")
public class Obligation {

    private String name;

    private ObligationType type;

    private boolean optional;

    private Object actionTemplate;

    private List<ActionArgument> attributes = Collections.emptyList();

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return the type
     */
    public ObligationType getType() {
        return this.type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(final ObligationType type) {
        this.type = type;
    }

    /**
     * @return the optional
     */
    public boolean isOptional() {
        return this.optional;
    }

    /**
     * @param optional
     *            the optional to set
     */
    public void setOptional(final boolean optional) {
        this.optional = optional;
    }

    /**
     * @return the actionTemplate
     */
    public Object getActionTemplate() {
        return this.actionTemplate;
    }

    /**
     * @param actionTemplate
     *            the actionTemplate to set
     */
    public void setActionTemplate(final Object actionTemplate) {
        this.actionTemplate = actionTemplate;
    }

    /**
     * @return the attributes
     */
    public List<ActionArgument> getAttributes() {
        return this.attributes;
    }

    /**
     * @param attributes
     *            the attributes to set
     */
    public void setAttributes(final List<ActionArgument> attributes) {
        this.attributes = attributes;
    }

}
