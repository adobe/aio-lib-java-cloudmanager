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

package io.adobe.cloudmanager.impl.generated;

import java.util.Objects;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.adobe.cloudmanager.impl.generated.HalLink;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
/**
 * RepositoryBranchLinks
 */



public class RepositoryBranchLinks implements Serializable{
  private static final long serialVersionUID = 1L;
  @JsonProperty("http://ns.adobe.com/adobecloud/rel/program")
  private HalLink httpnsAdobeComadobecloudrelprogram = null;

  @JsonProperty("http://ns.adobe.com/adobecloud/rel/repository")
  private HalLink httpnsAdobeComadobecloudrelrepository = null;

  public RepositoryBranchLinks httpnsAdobeComadobecloudrelprogram(HalLink httpnsAdobeComadobecloudrelprogram) {
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

  public RepositoryBranchLinks httpnsAdobeComadobecloudrelrepository(HalLink httpnsAdobeComadobecloudrelrepository) {
    this.httpnsAdobeComadobecloudrelrepository = httpnsAdobeComadobecloudrelrepository;
    return this;
  }

   /**
   * Get httpnsAdobeComadobecloudrelrepository
   * @return httpnsAdobeComadobecloudrelrepository
  **/
  @Schema(description = "")
  public HalLink getHttpnsAdobeComadobecloudrelrepository() {
    return httpnsAdobeComadobecloudrelrepository;
  }

  public void setHttpnsAdobeComadobecloudrelrepository(HalLink httpnsAdobeComadobecloudrelrepository) {
    this.httpnsAdobeComadobecloudrelrepository = httpnsAdobeComadobecloudrelrepository;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RepositoryBranchLinks repositoryBranchLinks = (RepositoryBranchLinks) o;
    return Objects.equals(this.httpnsAdobeComadobecloudrelprogram, repositoryBranchLinks.httpnsAdobeComadobecloudrelprogram) &&
        Objects.equals(this.httpnsAdobeComadobecloudrelrepository, repositoryBranchLinks.httpnsAdobeComadobecloudrelrepository);
  }

  @Override
  public int hashCode() {
    return Objects.hash(httpnsAdobeComadobecloudrelprogram, httpnsAdobeComadobecloudrelrepository);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RepositoryBranchLinks {\n");
    
    sb.append("    httpnsAdobeComadobecloudrelprogram: ").append(toIndentedString(httpnsAdobeComadobecloudrelprogram)).append("\n");
    sb.append("    httpnsAdobeComadobecloudrelrepository: ").append(toIndentedString(httpnsAdobeComadobecloudrelrepository)).append("\n");
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
