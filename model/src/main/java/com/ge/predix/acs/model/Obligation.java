package com.ge.predix.acs.model;

import java.util.Collections;
import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

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

    @NotNull(message = "Obligation Name cannot be null")
    @Min(value = 1, message = "Obligation Name should have one character minimum")
    @Max(value = 128, message = "Obligation Name should have a maximun of 128 characters")
    private String name;

    @NotNull(message = "Obligation Type should not be null")
    private ObligationType type;

    private boolean optional;

    @NotNull(message = "Obligation Action Template cannot be null")
    private Object actionTemplate;

    private List<ActionArgument> actionArguments = Collections.emptyList();

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
    public List<ActionArgument> getActionArguments() {
        return this.actionArguments;
    }

    /**
     * @param attributes
     *            the attributes to set
     */
    public void setActionArguments(final List<ActionArgument> actionArguments) {
        this.actionArguments = actionArguments;
    }

}
