package com.ge.predix.acs.rest;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.ge.predix.acs.model.Attribute;
import com.ge.predix.acs.model.Effect;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@SuppressWarnings({ "javadoc", "nls" })
@ApiModel(description = "Resource evaluation result")
public class ResourceEvaluationResult {

    @ApiModelProperty(value = "The resources that matched the policy evaluation request's resource identifier based on "
            + "the attribute uri templates defined in the policy set. For example, a policy request of"
            + "    /v1/site/1/plant/asset/1\n" + "against a policy set with attribute uri templates:\n"
            + "    /v1{attribute_uri}/plant/asset/{asset_id}\n" + "    /v1/site/{site_id}/plant/asset/{asset_id}\n"
            + "would include:\n" + "    /site/1\n" + "    /asset/2\n" + "in this set.",
            required = false)
    private Set<String> resolvedResourceUris = Collections.emptySet();

    @ApiModelProperty(value = "The collection of the resource's attributes",
            required = false)
    private List<Attribute> resourceAttributes = Collections.emptyList();

    @ApiModelProperty(value = "The action on the given resource URI",
            required = true)
    private String action;

    @ApiModelProperty(value = "The effect of the policy evaluation",
            required = true)
    private Effect effect;

    private String message;

    public Set<String> getResolvedResourceUris() {
        return resolvedResourceUris;
    }

    public void setResolvedResourceUris(final Set<String> resolvedResourceUris) {
        this.resolvedResourceUris = resolvedResourceUris;
    }

    public List<Attribute> getResourceAttributes() {
        return resourceAttributes;
    }

    public void setResourceAttributes(final List<Attribute> resourceAttributes) {
        this.resourceAttributes = resourceAttributes;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Effect getEffect() {
        return effect;
    }

    public void setEffect(final Effect effect) {
        this.effect = effect;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }
}
