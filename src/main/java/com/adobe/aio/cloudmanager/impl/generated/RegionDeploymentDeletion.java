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
 * Description of region deployment to be deleted in the environment
 */
@Schema(description = "Description of region deployment to be deleted in the environment")


public class RegionDeploymentDeletion implements Serializable{
  private static final long serialVersionUID = 1L;
  @JsonProperty("id")
  private String id = null;

  @JsonProperty("status")
  private String status = null;

  public RegionDeploymentDeletion id(String id) {
    this.id = id;
    return this;
  }

   /**
   * Identifier of the  region deployment
   * @return id
  **/
  @Schema(required = true, description = "Identifier of the  region deployment")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public RegionDeploymentDeletion status(String status) {
    this.status = status;
    return this;
  }

   /**
   * Status of the region deployment to be set
   * @return status
  **/
  @Schema(example = "TO_DELETE", required = true, description = "Status of the region deployment to be set")
  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RegionDeploymentDeletion regionDeploymentDeletion = (RegionDeploymentDeletion) o;
    return Objects.equals(this.id, regionDeploymentDeletion.id) &&
        Objects.equals(this.status, regionDeploymentDeletion.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, status);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RegionDeploymentDeletion {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
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
