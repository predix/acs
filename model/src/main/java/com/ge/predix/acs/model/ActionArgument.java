package com.ge.predix.acs.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author Sebastian Torres Brown
 * 
 *         An array of arguments that resolves an obligation expression into the actual obligation.
 *
 */

@ApiModel(description = "An array of arguments that resolves an obligation expression into the actual obligation.")
public class ActionArgument {

    private String name;
    private String value;

    /**
     * @return the name
     */
    @ApiModelProperty(required = true)
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
     * @return the value
     */
    @ApiModelProperty(required = true)
    public String getValue() {
        return this.value;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(final String value) {
        this.value = value;
    }

}
