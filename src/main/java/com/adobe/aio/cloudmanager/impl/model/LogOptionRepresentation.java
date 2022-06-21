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
 * LogOptionRepresentation
 */


public class LogOptionRepresentation implements Serializable{
  private static final long serialVersionUID = 1L;
  @JsonProperty("service")
  private String service = null;

  @JsonProperty("name")
  private String name = null;

  public LogOptionRepresentation service(String service) {
    this.service = service;
    return this;
  }

   /**
   * Name of the service in environment. Example: author
   * @return service
  **/
  @Schema(required = true, description = "Name of the service in environment. Example: author")
  public String getService() {
    return service;
  }

  public void setService(String service) {
    this.service = service;
  }

  public LogOptionRepresentation name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Name of the log for service in environment. Example: aemerror
   * @return name
  **/
  @Schema(required = true, description = "Name of the log for service in environment. Example: aemerror")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LogOptionRepresentation logOptionRepresentation = (LogOptionRepresentation) o;
    return Objects.equals(this.service, logOptionRepresentation.service) &&
        Objects.equals(this.name, logOptionRepresentation.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(service, name);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LogOptionRepresentation {\n");
    
    sb.append("    service: ").append(toIndentedString(service)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
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