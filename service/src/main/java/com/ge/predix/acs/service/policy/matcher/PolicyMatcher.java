/*******************************************************************************
 * Copyright 2016 General Electric Company.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.ge.predix.acs.service.policy.matcher;

import com.ge.predix.acs.model.Policy;
import com.ge.predix.acs.service.policy.evaluation.MatchedPolicy;

import java.util.List;

/**
 * Matches an access control request to a policy.
 *
 * @author 212314537
 */
public interface PolicyMatcher {
    /**
     * @param candidate
     *            the criteria for a match.
     * @param policies
     *            the list of potential policy matches
     * @return the policies that match the access control request.
     */
    List<MatchedPolicy> match(PolicyMatchCandidate candidate, List<Policy> policies);

    /**
     * @param candidate
     *            the criteria for a match.
     * @param policies
     *            the list of potential policy matches
     * @return the policies that match the access control request and the set of resolved URIs from applying all
     *         attribute URI templates.
     */
    MatchResult matchForResult(PolicyMatchCandidate candidate, List<Policy> policies);
}
