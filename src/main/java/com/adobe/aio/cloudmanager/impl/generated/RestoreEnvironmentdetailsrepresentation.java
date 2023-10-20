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
 * RestoreEnvironmentdetailsrepresentation
 */



public class RestoreEnvironmentdetailsrepresentation implements Serializable{
  private static final long serialVersionUID = 1L;
  @JsonProperty("releaseId")
  private String releaseId = null;

  @JsonProperty("repositoryId")
  private String repositoryId = null;

  @JsonProperty("repositoryName")
  private String repositoryName = null;

  @JsonProperty("branch")
  private String branch = null;

  @JsonProperty("commitSha")
  private String commitSha = null;

  @JsonProperty("repositoryUrl")
  private String repositoryUrl = null;

  @JsonProperty("timestamp")
  private Long timestamp = null;

  public RestoreEnvironmentdetailsrepresentation releaseId(String releaseId) {
    this.releaseId = releaseId;
    return this;
  }

   /**
   * Get releaseId
   * @return releaseId
  **/
  @Schema(description = "")
  public String getReleaseId() {
    return releaseId;
  }

  public void setReleaseId(String releaseId) {
    this.releaseId = releaseId;
  }

  public RestoreEnvironmentdetailsrepresentation repositoryId(String repositoryId) {
    this.repositoryId = repositoryId;
    return this;
  }

   /**
   * Get repositoryId
   * @return repositoryId
  **/
  @Schema(description = "")
  public String getRepositoryId() {
    return repositoryId;
  }

  public void setRepositoryId(String repositoryId) {
    this.repositoryId = repositoryId;
  }

  public RestoreEnvironmentdetailsrepresentation repositoryName(String repositoryName) {
    this.repositoryName = repositoryName;
    return this;
  }

   /**
   * Get repositoryName
   * @return repositoryName
  **/
  @Schema(description = "")
  public String getRepositoryName() {
    return repositoryName;
  }

  public void setRepositoryName(String repositoryName) {
    this.repositoryName = repositoryName;
  }

  public RestoreEnvironmentdetailsrepresentation branch(String branch) {
    this.branch = branch;
    return this;
  }

   /**
   * Get branch
   * @return branch
  **/
  @Schema(description = "")
  public String getBranch() {
    return branch;
  }

  public void setBranch(String branch) {
    this.branch = branch;
  }

  public RestoreEnvironmentdetailsrepresentation commitSha(String commitSha) {
    this.commitSha = commitSha;
    return this;
  }

   /**
   * Get commitSha
   * @return commitSha
  **/
  @Schema(description = "")
  public String getCommitSha() {
    return commitSha;
  }

  public void setCommitSha(String commitSha) {
    this.commitSha = commitSha;
  }

  public RestoreEnvironmentdetailsrepresentation repositoryUrl(String repositoryUrl) {
    this.repositoryUrl = repositoryUrl;
    return this;
  }

   /**
   * Get repositoryUrl
   * @return repositoryUrl
  **/
  @Schema(description = "")
  public String getRepositoryUrl() {
    return repositoryUrl;
  }

  public void setRepositoryUrl(String repositoryUrl) {
    this.repositoryUrl = repositoryUrl;
  }

  public RestoreEnvironmentdetailsrepresentation timestamp(Long timestamp) {
    this.timestamp = timestamp;
    return this;
  }

   /**
   * Get timestamp
   * @return timestamp
  **/
  @Schema(description = "")
  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RestoreEnvironmentdetailsrepresentation restoreEnvironmentdetailsrepresentation = (RestoreEnvironmentdetailsrepresentation) o;
    return Objects.equals(this.releaseId, restoreEnvironmentdetailsrepresentation.releaseId) &&
        Objects.equals(this.repositoryId, restoreEnvironmentdetailsrepresentation.repositoryId) &&
        Objects.equals(this.repositoryName, restoreEnvironmentdetailsrepresentation.repositoryName) &&
        Objects.equals(this.branch, restoreEnvironmentdetailsrepresentation.branch) &&
        Objects.equals(this.commitSha, restoreEnvironmentdetailsrepresentation.commitSha) &&
        Objects.equals(this.repositoryUrl, restoreEnvironmentdetailsrepresentation.repositoryUrl) &&
        Objects.equals(this.timestamp, restoreEnvironmentdetailsrepresentation.timestamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(releaseId, repositoryId, repositoryName, branch, commitSha, repositoryUrl, timestamp);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RestoreEnvironmentdetailsrepresentation {\n");
    
    sb.append("    releaseId: ").append(toIndentedString(releaseId)).append("\n");
    sb.append("    repositoryId: ").append(toIndentedString(repositoryId)).append("\n");
    sb.append("    repositoryName: ").append(toIndentedString(repositoryName)).append("\n");
    sb.append("    branch: ").append(toIndentedString(branch)).append("\n");
    sb.append("    commitSha: ").append(toIndentedString(commitSha)).append("\n");
    sb.append("    repositoryUrl: ").append(toIndentedString(repositoryUrl)).append("\n");
    sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
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