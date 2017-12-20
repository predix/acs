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
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/

package com.ge.predix.acs.privilege.management.dao;

import com.ge.predix.acs.model.Attribute;
import com.ge.predix.acs.zone.management.dao.ZoneEntity;

import java.util.Set;

public interface SubjectHierarchicalRepository {
    SubjectEntity getSubjectWithInheritedAttributesForScopes(ZoneEntity zone, String subjectIdentifier,
                                                             Set<Attribute> scopes);

    SubjectEntity getSubjectWithInheritedAttributes(ZoneEntity zone, String subjectIdentifier);

    Set<String> getSubjectEntityAndDescendantsIds(SubjectEntity entity); 
}