/*
 * Cloud Manager API
 * This API allows access to Cloud Manager programs, pipelines, and environments by an authorized technical account created through the Adobe I/O Console. The base url for this API is https://cloudmanager.adobe.io, e.g. to get the list of programs for an organization, you would make a GET request to https://cloudmanager.adobe.io/api/programs (with the correct set of headers as described below). This swagger file can be downloaded from https://raw.githubusercontent.com/AdobeDocs/cloudmanager-api-docs/master/swagger-specs/api.yaml.
 *
 * OpenAPI spec version: 1.0.0
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package com.adobe.aio.cloudmanager.impl.model;

/*-
 * #%L
 * Adobe Cloud Manager Client Library
 * %%
 * Copyright (C) 2020 - 2022 Adobe Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
/**
 * Describes an __IP Allowed List Binding__
 */
@Schema(description = "Describes an __IP Allowed List Binding__")

public class IPAllowedListBinding implements Serializable{
  private static final long serialVersionUID = 1L;
  @JsonProperty("id")
  private String id = null;

  /**
   * Tier of the environment.
   */
  public enum TierEnum {
    AUTHOR("author"),
    PUBLISH("publish"),
    PREVIEW("preview");

    private final String value;

    TierEnum(String value) {
      this.value = value;
    }
    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
    @JsonCreator
    public static TierEnum fromValue(String text) {
      for (TierEnum b : TierEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }

  }  @JsonProperty("tier")
  private TierEnum tier = null;

  /**
   * Status of the binding.
   */
  public enum StatusEnum {
    FAILED("failed"),
    DELETE_FAILED("delete_failed"),
    RUNNING("running"),
    DELETING("deleting"),
    COMPLETED("completed"),
    DELETED("deleted"),
    NOT_STARTED("not_started");

    private final String value;

    StatusEnum(String value) {
      this.value = value;
    }
    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
    @JsonCreator
    public static StatusEnum fromValue(String text) {
      for (StatusEnum b : StatusEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }

  }  @JsonProperty("status")
  private StatusEnum status = null;

  @JsonProperty("programId")
  private String programId = null;

  @JsonProperty("ipAllowListId")
  private String ipAllowListId = null;

  @JsonProperty("environmentId")
  private String environmentId = null;

  @JsonProperty("_links")
  private IPAllowedListBindingLinks _links = null;

  public IPAllowedListBinding id(String id) {
    this.id = id;
    return this;
  }

   /**
   * Identifier of the IP Allowed List Binding to an Environment
   * @return id
  **/
  @Schema(example = "14", description = "Identifier of the IP Allowed List Binding to an Environment")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public IPAllowedListBinding tier(TierEnum tier) {
    this.tier = tier;
    return this;
  }

   /**
   * Tier of the environment.
   * @return tier
  **/
  @Schema(example = "publish", description = "Tier of the environment.")
  public TierEnum getTier() {
    return tier;
  }

  public void setTier(TierEnum tier) {
    this.tier = tier;
  }

  public IPAllowedListBinding status(StatusEnum status) {
    this.status = status;
    return this;
  }

   /**
   * Status of the binding.
   * @return status
  **/
  @Schema(example = "NOT_STARTED", description = "Status of the binding.")
  public StatusEnum getStatus() {
    return status;
  }

  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  public IPAllowedListBinding programId(String programId) {
    this.programId = programId;
    return this;
  }

   /**
   * Identifier of the program.
   * @return programId
  **/
  @Schema(example = "22", description = "Identifier of the program.")
  public String getProgramId() {
    return programId;
  }

  public void setProgramId(String programId) {
    this.programId = programId;
  }

  public IPAllowedListBinding ipAllowListId(String ipAllowListId) {
    this.ipAllowListId = ipAllowListId;
    return this;
  }

   /**
   * Identifier of the IP allow list.
   * @return ipAllowListId
  **/
  @Schema(example = "17", description = "Identifier of the IP allow list.")
  public String getIpAllowListId() {
    return ipAllowListId;
  }

  public void setIpAllowListId(String ipAllowListId) {
    this.ipAllowListId = ipAllowListId;
  }

  public IPAllowedListBinding environmentId(String environmentId) {
    this.environmentId = environmentId;
    return this;
  }

   /**
   * Identifier of the environment.
   * @return environmentId
  **/
  @Schema(example = "5", required = true, description = "Identifier of the environment.")
  public String getEnvironmentId() {
    return environmentId;
  }

  public void setEnvironmentId(String environmentId) {
    this.environmentId = environmentId;
  }

  public IPAllowedListBinding _links(IPAllowedListBindingLinks _links) {
    this._links = _links;
    return this;
  }

   /**
   * Get _links
   * @return _links
  **/
  @Schema(description = "")
  public IPAllowedListBindingLinks getLinks() {
    return _links;
  }

  public void setLinks(IPAllowedListBindingLinks _links) {
    this._links = _links;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IPAllowedListBinding ipAllowedListBinding = (IPAllowedListBinding) o;
    return Objects.equals(this.id, ipAllowedListBinding.id) &&
        Objects.equals(this.tier, ipAllowedListBinding.tier) &&
        Objects.equals(this.status, ipAllowedListBinding.status) &&
        Objects.equals(this.programId, ipAllowedListBinding.programId) &&
        Objects.equals(this.ipAllowListId, ipAllowedListBinding.ipAllowListId) &&
        Objects.equals(this.environmentId, ipAllowedListBinding.environmentId) &&
        Objects.equals(this._links, ipAllowedListBinding._links);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, tier, status, programId, ipAllowListId, environmentId, _links);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class IPAllowedListBinding {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    tier: ").append(toIndentedString(tier)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    programId: ").append(toIndentedString(programId)).append("\n");
    sb.append("    ipAllowListId: ").append(toIndentedString(ipAllowListId)).append("\n");
    sb.append("    environmentId: ").append(toIndentedString(environmentId)).append("\n");
    sb.append("    _links: ").append(toIndentedString(_links)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}
