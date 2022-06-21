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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
/**
 * InvalidParameter
 */


public class InvalidParameter implements Serializable{
  private static final long serialVersionUID = 1L;
  @JsonProperty("name")
  private String name = null;

  @JsonProperty("reason")
  private String reason = null;

  public InvalidParameter name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Name of the invalid parameter.
   * @return name
  **/
  @Schema(example = "paramName", description = "Name of the invalid parameter.")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public InvalidParameter reason(String reason) {
    this.reason = reason;
    return this;
  }

   /**
   * Reason of why the parameter&#x27;s value is not accepted.
   * @return reason
  **/
  @Schema(example = "value must be a positive number", description = "Reason of why the parameter's value is not accepted.")
  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InvalidParameter invalidParameter = (InvalidParameter) o;
    return Objects.equals(this.name, invalidParameter.name) &&
        Objects.equals(this.reason, invalidParameter.reason);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, reason);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class InvalidParameter {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    reason: ").append(toIndentedString(reason)).append("\n");
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