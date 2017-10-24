/*******************************************************************************
 * Copyright 2017 General Electric Company
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.ge.predix.acs.obligation.management;

import static com.ge.predix.acs.commons.web.AcsApiUriTemplates.OBLIGATIONS_URL;
import static com.ge.predix.acs.commons.web.AcsApiUriTemplates.OBLIGATION_URL;
import static com.ge.predix.acs.commons.web.AcsApiUriTemplates.V1;
import static com.ge.predix.acs.commons.web.ResponseEntityBuilder.created;
import static com.ge.predix.acs.commons.web.ResponseEntityBuilder.noContent;
import static com.ge.predix.acs.commons.web.ResponseEntityBuilder.notFound;
import static com.ge.predix.acs.commons.web.ResponseEntityBuilder.ok;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.net.URI;
import java.util.List;

import javax.validation.Valid;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ge.predix.acs.commons.web.BaseRestApi;
import com.ge.predix.acs.commons.web.RestApiException;
import com.ge.predix.acs.commons.web.UriTemplateUtils;
import com.ge.predix.acs.model.Obligation;
import com.ge.predix.acs.util.collection.ValidList;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author Sebastian Torres Brown
 * 
 *         Obligation controller, It exposes the Obligation management service API as a RestFul service
 */

@RestController
@RequestMapping(value = { V1 })
public class ObligationController extends BaseRestApi {

    @Autowired
    private ObligationService service;

    @ApiOperation(value = "Creates/Updates an obligation set for the given zone.", tags = { "Obligation Management" })
    @ApiResponses(
            value = { @ApiResponse(
                    code = 201,
                    message = "Obligation creation successful. Obligation set URI is returned "
                            + "in 'Location' header."), })
    @RequestMapping(method = PUT, value = OBLIGATION_URL, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createObligation(@RequestBody @Valid final Obligation obligation,
            @PathVariable("url_encoded_obligation_name") final String obligationName) {
        validateObligationNameOrFail(obligation, obligationName);

        try {
            this.service.upsertObligation(obligation);
            URI obligationUri = UriTemplateUtils.expand(OBLIGATION_URL, "obligationName:" + obligation.getName());
            return created(obligationUri.getPath());
        } catch (ObligationException e) {
            throw new RestApiException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), e);
        }
    }

    @ApiOperation(value = "Creates/Updates an obligation set for the given zone.", tags = { "Obligation Management" })
    @ApiResponses(
            value = { @ApiResponse(
                    code = 201,
                    message = "Obligation creation successful. Obligation set URI is returned in "
                            + "'Location' header."), })
    @RequestMapping(method = POST, value = OBLIGATION_URL, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createObligations(@RequestBody @Valid final ValidList<Obligation> obligations) {
        try {
            this.service.upsertObligations(obligations);
            URI obligationUri = UriTemplateUtils.expand(OBLIGATIONS_URL);
            return created(obligationUri.getPath());
        } catch (ObligationException e) {
            throw new RestApiException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), e);
        }
    }

    @ApiOperation(value = "Retrieves Obligation for the given zone.", tags = { "Obligation Management" })
    @RequestMapping(method = GET, value = OBLIGATIONS_URL, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Obligation> getObligation(
            @PathVariable(value = "url_encoded_obligation_name") final String obligationName) {
        Obligation result = this.service.getObligation(obligationName);
        if (result != null) {
            return ok(result);
        }

        return notFound();
    }

    @ApiOperation(
            value = "Retrieves Obligations for the given zone. Can filter given an obligation name",
            tags = { "Obligation Management" })
    @RequestMapping(method = GET, value = OBLIGATIONS_URL, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<Obligation>> getObligations() {
        List<Obligation> results = this.service.getObligations();
        if (!CollectionUtils.isEmpty(results)) {
            return ok(results);
        }
        return notFound();
    }

    @ApiOperation(value = "Deletes a policy set for the given zone.", tags = { "Obligation Management" })
    @RequestMapping(method = DELETE, value = OBLIGATION_URL)
    public ResponseEntity<Void> deletePolicySet(
            @PathVariable("url_encoded_obligation_name") final String obligationName) {
        if (this.service.deleteObligation(obligationName)) {
            return noContent();
        }
        return notFound();
    }

    private void validateObligationNameOrFail(final Obligation obligation, final String obligationName) {
        String name = obligation.getName();
        if (!StringUtils.isEmpty(name) && !obligationName.equals(name)) {
            throw new RestApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                    String.format("Obligation name in the payload = %s, does not match the one provided in URI = %s",
                            name, obligationName));
        }
    }
}
