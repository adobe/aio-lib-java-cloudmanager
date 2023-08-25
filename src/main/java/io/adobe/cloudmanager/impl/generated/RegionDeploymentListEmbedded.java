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
import io.adobe.cloudmanager.impl.generated.RegionDeployment;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;
/**
 * RegionDeploymentListEmbedded
 */



public class RegionDeploymentListEmbedded implements Serializable{
  private static final long serialVersionUID = 1L;
  @JsonProperty("regionDeployments")
  private List<RegionDeployment> regionDeployments = null;

  public RegionDeploymentListEmbedded regionDeployments(List<RegionDeployment> regionDeployments) {
    this.regionDeployments = regionDeployments;
    return this;
  }

  public RegionDeploymentListEmbedded addRegionDeploymentsItem(RegionDeployment regionDeploymentsItem) {
    if (this.regionDeployments == null) {
      this.regionDeployments = new ArrayList<>();
    }
    this.regionDeployments.add(regionDeploymentsItem);
    return this;
  }

   /**
   * Get regionDeployments
   * @return regionDeployments
  **/
  @Schema(description = "")
  public List<RegionDeployment> getRegionDeployments() {
    return regionDeployments;
  }

  public void setRegionDeployments(List<RegionDeployment> regionDeployments) {
    this.regionDeployments = regionDeployments;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RegionDeploymentListEmbedded regionDeploymentListEmbedded = (RegionDeploymentListEmbedded) o;
    return Objects.equals(this.regionDeployments, regionDeploymentListEmbedded.regionDeployments);
  }

  @Override
  public int hashCode() {
    return Objects.hash(regionDeployments);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RegionDeploymentListEmbedded {\n");
    
    sb.append("    regionDeployments: ").append(toIndentedString(regionDeployments)).append("\n");
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
