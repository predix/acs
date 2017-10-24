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

package com.ge.predix.acs.obligation.management.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.ge.predix.acs.zone.management.dao.ZoneEntity;

/**
 *
 * @author Sebastian Torres Brown Obligation Entity
 */
@SuppressWarnings({ "nls", "javadoc" })
@Entity
@Table(
        name = "obligation",
        uniqueConstraints = { @UniqueConstraint(columnNames = { "authorization_zone_id", "obligation_id" }) })
public class ObligationEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "authorization_zone_id", referencedColumnName = "id", nullable = false, updatable = false)
    private ZoneEntity zone;

    /** ID unique per client-id and issuer combination. */
    @Column(name = "obligation_id", nullable = false)
    private String obligationId;

    /** Clob representing the JSON onligation. */
    @Column(name = "obligation_json", columnDefinition = "CLOB NOT NULL")
    private String obligationJson;

    public ObligationEntity(final ZoneEntity zone, final String oligationId, final String obligationJson) {
        super();
        this.zone = zone;
        this.obligationId = oligationId;
        this.obligationJson = obligationJson;
    }

    public ObligationEntity() {
        super();
    }

    public long getId() {
        return this.id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getObligationId() {
        return this.obligationId;
    }

    public String getObligationJson() {
        return this.obligationJson;
    }

    @Override
    public String toString() {
        return "ObligationEntity [id=" + this.id + ", zone=" + this.zone + ", obligationId=" + this.obligationId
                + ", obligationJson=" + this.obligationJson + "]";
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.zone).append(this.obligationId).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {

        if (obj instanceof ObligationEntity) {
            final ObligationEntity other = (ObligationEntity) obj;
            return new EqualsBuilder().append(this.zone, other.zone).append(this.obligationId, other.obligationId)
                    .isEquals();
        }
        return false;
    }

}
