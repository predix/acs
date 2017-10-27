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

import java.util.List;

import com.ge.predix.acs.model.Obligation;

/**
 *
 * @author Sebastian Torres Brown
 * 
 *         Obligation Management Service
 */
public interface ObligationService {

    void upsertObligation(Obligation obligation);

    void upsertObligations(List<Obligation> obligations);

    List<Obligation> getObligationsForZone();

    Obligation getObligationByZoneAndName(String obligationName);

    Boolean deleteObligation(String obligationName);

}
