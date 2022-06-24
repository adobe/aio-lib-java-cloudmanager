/*
 * Cloud Manager API
 * This API allows access to Cloud Manager programs, pipelines, and environments by an authorized technical account created through the Adobe I/O Console. The base url for this API is https://cloudmanager.adobe.io, e.g. to get the list of programs for an organization, you would make a GET request to https://cloudmanager.adobe.io/api/programs (with the correct set of headers as described below). This swagger file can be downloaded from https://raw.githubusercontent.com/AdobeDocs/cloudmanager-api-docs/main/swagger-specs/api.yaml.
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

import java.util.Objects;
import java.util.Arrays;
import com.adobe.aio.cloudmanager.impl.model.HalLink;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
/**
 * TenantLinks
 */


public class TenantLinks implements Serializable{
  private static final long serialVersionUID = 1L;
  @JsonProperty("http://ns.adobe.com/adobecloud/rel/programs")
  private HalLink httpnsAdobeComadobecloudrelprograms = null;

  @JsonProperty("self")
  private HalLink self = null;

  public TenantLinks httpnsAdobeComadobecloudrelprograms(HalLink httpnsAdobeComadobecloudrelprograms) {
    this.httpnsAdobeComadobecloudrelprograms = httpnsAdobeComadobecloudrelprograms;
    return this;
  }

   /**
   * Get httpnsAdobeComadobecloudrelprograms
   * @return httpnsAdobeComadobecloudrelprograms
  **/
  @Schema(description = "")
  public HalLink getHttpnsAdobeComadobecloudrelprograms() {
    return httpnsAdobeComadobecloudrelprograms;
  }

  public void setHttpnsAdobeComadobecloudrelprograms(HalLink httpnsAdobeComadobecloudrelprograms) {
    this.httpnsAdobeComadobecloudrelprograms = httpnsAdobeComadobecloudrelprograms;
  }

  public TenantLinks self(HalLink self) {
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
    TenantLinks tenantLinks = (TenantLinks) o;
    return Objects.equals(this.httpnsAdobeComadobecloudrelprograms, tenantLinks.httpnsAdobeComadobecloudrelprograms) &&
        Objects.equals(this.self, tenantLinks.self);
  }

  @Override
  public int hashCode() {
    return Objects.hash(httpnsAdobeComadobecloudrelprograms, self);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TenantLinks {\n");
    
    sb.append("    httpnsAdobeComadobecloudrelprograms: ").append(toIndentedString(httpnsAdobeComadobecloudrelprograms)).append("\n");
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
