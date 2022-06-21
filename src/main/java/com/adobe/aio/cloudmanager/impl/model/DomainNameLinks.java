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
 * DomainNameLinks
 */


public class DomainNameLinks implements Serializable{
  private static final long serialVersionUID = 1L;
  @JsonProperty("http://ns.adobe.com/adobecloud/rel/domainName/certificates")
  private HalLink httpnsAdobeComadobecloudreldomainNamecertificates = null;

  @JsonProperty("http://ns.adobe.com/adobecloud/rel/domainName/deploy")
  private HalLink httpnsAdobeComadobecloudreldomainNamedeploy = null;

  @JsonProperty("http://ns.adobe.com/adobecloud/rel/domainName/verify")
  private HalLink httpnsAdobeComadobecloudreldomainNameverify = null;

  @JsonProperty("http://ns.adobe.com/adobecloud/rel/environment")
  private HalLink httpnsAdobeComadobecloudrelenvironment = null;

  @JsonProperty("http://ns.adobe.com/adobecloud/rel/program")
  private HalLink httpnsAdobeComadobecloudrelprogram = null;

  @JsonProperty("self")
  private HalLink self = null;

  public DomainNameLinks httpnsAdobeComadobecloudreldomainNamecertificates(HalLink httpnsAdobeComadobecloudreldomainNamecertificates) {
    this.httpnsAdobeComadobecloudreldomainNamecertificates = httpnsAdobeComadobecloudreldomainNamecertificates;
    return this;
  }

   /**
   * Get httpnsAdobeComadobecloudreldomainNamecertificates
   * @return httpnsAdobeComadobecloudreldomainNamecertificates
  **/
  @Schema(description = "")
  public HalLink getHttpnsAdobeComadobecloudreldomainNamecertificates() {
    return httpnsAdobeComadobecloudreldomainNamecertificates;
  }

  public void setHttpnsAdobeComadobecloudreldomainNamecertificates(HalLink httpnsAdobeComadobecloudreldomainNamecertificates) {
    this.httpnsAdobeComadobecloudreldomainNamecertificates = httpnsAdobeComadobecloudreldomainNamecertificates;
  }

  public DomainNameLinks httpnsAdobeComadobecloudreldomainNamedeploy(HalLink httpnsAdobeComadobecloudreldomainNamedeploy) {
    this.httpnsAdobeComadobecloudreldomainNamedeploy = httpnsAdobeComadobecloudreldomainNamedeploy;
    return this;
  }

   /**
   * Get httpnsAdobeComadobecloudreldomainNamedeploy
   * @return httpnsAdobeComadobecloudreldomainNamedeploy
  **/
  @Schema(description = "")
  public HalLink getHttpnsAdobeComadobecloudreldomainNamedeploy() {
    return httpnsAdobeComadobecloudreldomainNamedeploy;
  }

  public void setHttpnsAdobeComadobecloudreldomainNamedeploy(HalLink httpnsAdobeComadobecloudreldomainNamedeploy) {
    this.httpnsAdobeComadobecloudreldomainNamedeploy = httpnsAdobeComadobecloudreldomainNamedeploy;
  }

  public DomainNameLinks httpnsAdobeComadobecloudreldomainNameverify(HalLink httpnsAdobeComadobecloudreldomainNameverify) {
    this.httpnsAdobeComadobecloudreldomainNameverify = httpnsAdobeComadobecloudreldomainNameverify;
    return this;
  }

   /**
   * Get httpnsAdobeComadobecloudreldomainNameverify
   * @return httpnsAdobeComadobecloudreldomainNameverify
  **/
  @Schema(description = "")
  public HalLink getHttpnsAdobeComadobecloudreldomainNameverify() {
    return httpnsAdobeComadobecloudreldomainNameverify;
  }

  public void setHttpnsAdobeComadobecloudreldomainNameverify(HalLink httpnsAdobeComadobecloudreldomainNameverify) {
    this.httpnsAdobeComadobecloudreldomainNameverify = httpnsAdobeComadobecloudreldomainNameverify;
  }

  public DomainNameLinks httpnsAdobeComadobecloudrelenvironment(HalLink httpnsAdobeComadobecloudrelenvironment) {
    this.httpnsAdobeComadobecloudrelenvironment = httpnsAdobeComadobecloudrelenvironment;
    return this;
  }

   /**
   * Get httpnsAdobeComadobecloudrelenvironment
   * @return httpnsAdobeComadobecloudrelenvironment
  **/
  @Schema(description = "")
  public HalLink getHttpnsAdobeComadobecloudrelenvironment() {
    return httpnsAdobeComadobecloudrelenvironment;
  }

  public void setHttpnsAdobeComadobecloudrelenvironment(HalLink httpnsAdobeComadobecloudrelenvironment) {
    this.httpnsAdobeComadobecloudrelenvironment = httpnsAdobeComadobecloudrelenvironment;
  }

  public DomainNameLinks httpnsAdobeComadobecloudrelprogram(HalLink httpnsAdobeComadobecloudrelprogram) {
    this.httpnsAdobeComadobecloudrelprogram = httpnsAdobeComadobecloudrelprogram;
    return this;
  }

   /**
   * Get httpnsAdobeComadobecloudrelprogram
   * @return httpnsAdobeComadobecloudrelprogram
  **/
  @Schema(description = "")
  public HalLink getHttpnsAdobeComadobecloudrelprogram() {
    return httpnsAdobeComadobecloudrelprogram;
  }

  public void setHttpnsAdobeComadobecloudrelprogram(HalLink httpnsAdobeComadobecloudrelprogram) {
    this.httpnsAdobeComadobecloudrelprogram = httpnsAdobeComadobecloudrelprogram;
  }

  public DomainNameLinks self(HalLink self) {
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
    DomainNameLinks domainNameLinks = (DomainNameLinks) o;
    return Objects.equals(this.httpnsAdobeComadobecloudreldomainNamecertificates, domainNameLinks.httpnsAdobeComadobecloudreldomainNamecertificates) &&
        Objects.equals(this.httpnsAdobeComadobecloudreldomainNamedeploy, domainNameLinks.httpnsAdobeComadobecloudreldomainNamedeploy) &&
        Objects.equals(this.httpnsAdobeComadobecloudreldomainNameverify, domainNameLinks.httpnsAdobeComadobecloudreldomainNameverify) &&
        Objects.equals(this.httpnsAdobeComadobecloudrelenvironment, domainNameLinks.httpnsAdobeComadobecloudrelenvironment) &&
        Objects.equals(this.httpnsAdobeComadobecloudrelprogram, domainNameLinks.httpnsAdobeComadobecloudrelprogram) &&
        Objects.equals(this.self, domainNameLinks.self);
  }

  @Override
  public int hashCode() {
    return Objects.hash(httpnsAdobeComadobecloudreldomainNamecertificates, httpnsAdobeComadobecloudreldomainNamedeploy, httpnsAdobeComadobecloudreldomainNameverify, httpnsAdobeComadobecloudrelenvironment, httpnsAdobeComadobecloudrelprogram, self);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DomainNameLinks {\n");
    
    sb.append("    httpnsAdobeComadobecloudreldomainNamecertificates: ").append(toIndentedString(httpnsAdobeComadobecloudreldomainNamecertificates)).append("\n");
    sb.append("    httpnsAdobeComadobecloudreldomainNamedeploy: ").append(toIndentedString(httpnsAdobeComadobecloudreldomainNamedeploy)).append("\n");
    sb.append("    httpnsAdobeComadobecloudreldomainNameverify: ").append(toIndentedString(httpnsAdobeComadobecloudreldomainNameverify)).append("\n");
    sb.append("    httpnsAdobeComadobecloudrelenvironment: ").append(toIndentedString(httpnsAdobeComadobecloudrelenvironment)).append("\n");
    sb.append("    httpnsAdobeComadobecloudrelprogram: ").append(toIndentedString(httpnsAdobeComadobecloudrelprogram)).append("\n");
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