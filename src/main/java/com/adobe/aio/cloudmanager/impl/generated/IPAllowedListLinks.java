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

package com.adobe.aio.cloudmanager.impl.generated;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
/**
 * IPAllowedListLinks
 */



public class IPAllowedListLinks implements Serializable{
  private static final long serialVersionUID = 1L;
  @JsonProperty("http://ns.adobe.com/adobecloud/rel/ipAllowlistBindings")
  private HalLink httpnsAdobeComadobecloudrelipAllowlistBindings = null;

  @JsonProperty("http://ns.adobe.com/adobecloud/rel/program")
  private HalLink httpnsAdobeComadobecloudrelprogram = null;

  @JsonProperty("self")
  private HalLink self = null;

  public IPAllowedListLinks httpnsAdobeComadobecloudrelipAllowlistBindings(HalLink httpnsAdobeComadobecloudrelipAllowlistBindings) {
    this.httpnsAdobeComadobecloudrelipAllowlistBindings = httpnsAdobeComadobecloudrelipAllowlistBindings;
    return this;
  }

   /**
   * Get httpnsAdobeComadobecloudrelipAllowlistBindings
   * @return httpnsAdobeComadobecloudrelipAllowlistBindings
  **/
  @Schema(description = "")
  public HalLink getHttpnsAdobeComadobecloudrelipAllowlistBindings() {
    return httpnsAdobeComadobecloudrelipAllowlistBindings;
  }

  public void setHttpnsAdobeComadobecloudrelipAllowlistBindings(HalLink httpnsAdobeComadobecloudrelipAllowlistBindings) {
    this.httpnsAdobeComadobecloudrelipAllowlistBindings = httpnsAdobeComadobecloudrelipAllowlistBindings;
  }

  public IPAllowedListLinks httpnsAdobeComadobecloudrelprogram(HalLink httpnsAdobeComadobecloudrelprogram) {
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

  public IPAllowedListLinks self(HalLink self) {
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
    IPAllowedListLinks ipAllowedListLinks = (IPAllowedListLinks) o;
    return Objects.equals(this.httpnsAdobeComadobecloudrelipAllowlistBindings, ipAllowedListLinks.httpnsAdobeComadobecloudrelipAllowlistBindings) &&
        Objects.equals(this.httpnsAdobeComadobecloudrelprogram, ipAllowedListLinks.httpnsAdobeComadobecloudrelprogram) &&
        Objects.equals(this.self, ipAllowedListLinks.self);
  }

  @Override
  public int hashCode() {
    return Objects.hash(httpnsAdobeComadobecloudrelipAllowlistBindings, httpnsAdobeComadobecloudrelprogram, self);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class IPAllowedListLinks {\n");
    
    sb.append("    httpnsAdobeComadobecloudrelipAllowlistBindings: ").append(toIndentedString(httpnsAdobeComadobecloudrelipAllowlistBindings)).append("\n");
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
