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
 * ProgramListLinks
 */


public class ProgramListLinks implements Serializable{
  private static final long serialVersionUID = 1L;
  @JsonProperty("next")
  private HalLink next = null;

  @JsonProperty("page")
  private HalLink page = null;

  @JsonProperty("prev")
  private HalLink prev = null;

  @JsonProperty("self")
  private HalLink self = null;

  public ProgramListLinks next(HalLink next) {
    this.next = next;
    return this;
  }

   /**
   * Get next
   * @return next
  **/
  @Schema(description = "")
  public HalLink getNext() {
    return next;
  }

  public void setNext(HalLink next) {
    this.next = next;
  }

  public ProgramListLinks page(HalLink page) {
    this.page = page;
    return this;
  }

   /**
   * Get page
   * @return page
  **/
  @Schema(description = "")
  public HalLink getPage() {
    return page;
  }

  public void setPage(HalLink page) {
    this.page = page;
  }

  public ProgramListLinks prev(HalLink prev) {
    this.prev = prev;
    return this;
  }

   /**
   * Get prev
   * @return prev
  **/
  @Schema(description = "")
  public HalLink getPrev() {
    return prev;
  }

  public void setPrev(HalLink prev) {
    this.prev = prev;
  }

  public ProgramListLinks self(HalLink self) {
    this.self = self;
    return this;
  }

   /**
   * Get self
   * @return self
  **/
  @Schema(description = "")
  public HalLink getSelf() {
    return self;
  }

  public void setSelf(HalLink self) {
    this.self = self;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ProgramListLinks programListLinks = (ProgramListLinks) o;
    return Objects.equals(this.next, programListLinks.next) &&
        Objects.equals(this.page, programListLinks.page) &&
        Objects.equals(this.prev, programListLinks.prev) &&
        Objects.equals(this.self, programListLinks.self);
  }

  @Override
  public int hashCode() {
    return Objects.hash(next, page, prev, self);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ProgramListLinks {\n");
    
    sb.append("    next: ").append(toIndentedString(next)).append("\n");
    sb.append("    page: ").append(toIndentedString(page)).append("\n");
    sb.append("    prev: ").append(toIndentedString(prev)).append("\n");
    sb.append("    self: ").append(toIndentedString(self)).append("\n");
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
