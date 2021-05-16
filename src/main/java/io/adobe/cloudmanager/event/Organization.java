/*
 * Cloud Manager Event Definitions
 * No description provided (generated by Swagger Codegen https://github.com/swagger-api/swagger-codegen)
 *
 * OpenAPI spec version: 1.0.0
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package io.adobe.cloudmanager.event;

/*-
 * #%L
 * Adobe Cloud Manager Client Library
 * %%
 * Copyright (C) 2020 - 2021 Adobe Inc.
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

import java.util.Objects;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
/**
 * Organization
 */


public class Organization implements Serializable{
  private static final long serialVersionUID = 1L;
  @JsonProperty("xdmImsOrg:id")
  private String xdmImsOrgid = null;

  @JsonProperty("@type")
  private String _atType = null;

  public Organization xdmImsOrgid(String xdmImsOrgid) {
    this.xdmImsOrgid = xdmImsOrgid;
    return this;
  }

   /**
   * An Adobe Organization Id
   * @return xdmImsOrgid
  **/
  @Schema(description = "An Adobe Organization Id")
  public String getXdmImsOrgid() {
    return xdmImsOrgid;
  }

  public void setXdmImsOrgid(String xdmImsOrgid) {
    this.xdmImsOrgid = xdmImsOrgid;
  }

  public Organization _atType(String _atType) {
    this._atType = _atType;
    return this;
  }

   /**
   * Get _atType
   * @return _atType
  **/
  @Schema(description = "")
  public String getAtType() {
    return _atType;
  }

  public void setAtType(String _atType) {
    this._atType = _atType;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Organization organization = (Organization) o;
    return Objects.equals(this.xdmImsOrgid, organization.xdmImsOrgid) &&
        Objects.equals(this._atType, organization._atType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(xdmImsOrgid, _atType);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Organization {\n");
    
    sb.append("    xdmImsOrgid: ").append(toIndentedString(xdmImsOrgid)).append("\n");
    sb.append("    _atType: ").append(toIndentedString(_atType)).append("\n");
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
