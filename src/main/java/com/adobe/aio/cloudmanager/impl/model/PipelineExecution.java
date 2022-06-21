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
import java.time.OffsetDateTime;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
/**
 * A representation of an execution of a CI/CD Pipeline.
 */
@Schema(description = "A representation of an execution of a CI/CD Pipeline.")

public class PipelineExecution implements Serializable{
  private static final long serialVersionUID = 1L;
  @JsonProperty("id")
  private String id = null;

  @JsonProperty("programId")
  private String programId = null;

  @JsonProperty("pipelineId")
  private String pipelineId = null;

  @JsonProperty("artifactsVersion")
  private String artifactsVersion = null;

  @JsonProperty("user")
  private String user = null;

  /**
   * Status of the execution
   */
  public enum StatusEnum {
    NOT_STARTED("NOT_STARTED"),
    RUNNING("RUNNING"),
    CANCELLING("CANCELLING"),
    CANCELLED("CANCELLED"),
    FINISHED("FINISHED"),
    ERROR("ERROR"),
    FAILED("FAILED");

    private final String value;

    StatusEnum(String value) {
      this.value = value;
    }
    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
    @JsonCreator
    public static StatusEnum fromValue(String text) {
      for (StatusEnum b : StatusEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }

  }  @JsonProperty("status")
  private StatusEnum status = null;

  /**
   * How the execution was triggered.
   */
  public enum TriggerEnum {
    ON_COMMIT("ON_COMMIT"),
    MANUAL("MANUAL"),
    PUSH_UPGRADES("PUSH_UPGRADES");

    private final String value;

    TriggerEnum(String value) {
      this.value = value;
    }
    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
    @JsonCreator
    public static TriggerEnum fromValue(String text) {
      for (TriggerEnum b : TriggerEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }

  }  @JsonProperty("trigger")
  private TriggerEnum trigger = null;

  @JsonProperty("createdAt")
  private OffsetDateTime createdAt = null;

  @JsonProperty("updatedAt")
  private OffsetDateTime updatedAt = null;

  @JsonProperty("finishedAt")
  private OffsetDateTime finishedAt = null;

  @JsonProperty("_embedded")
  private PipelineExecutionEmbedded _embedded = null;

  @JsonProperty("_links")
  private PipelineExecutionLinks _links = null;

  public PipelineExecution id(String id) {
    this.id = id;
    return this;
  }

   /**
   * Pipeline execution identifier
   * @return id
  **/
  @Schema(description = "Pipeline execution identifier")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

   /**
   * Identifier of the program. Unique within the space.
   * @return programId
  **/
  @Schema(example = "14", description = "Identifier of the program. Unique within the space.")
  public String getProgramId() {
    return programId;
  }

   /**
   * Identifier of the pipeline. Unique within the space.
   * @return pipelineId
  **/
  @Schema(example = "10", description = "Identifier of the pipeline. Unique within the space.")
  public String getPipelineId() {
    return pipelineId;
  }

  public PipelineExecution artifactsVersion(String artifactsVersion) {
    this.artifactsVersion = artifactsVersion;
    return this;
  }

   /**
   * Version of the artifacts generated during this execution
   * @return artifactsVersion
  **/
  @Schema(description = "Version of the artifacts generated during this execution")
  public String getArtifactsVersion() {
    return artifactsVersion;
  }

  public void setArtifactsVersion(String artifactsVersion) {
    this.artifactsVersion = artifactsVersion;
  }

  public PipelineExecution user(String user) {
    this.user = user;
    return this;
  }

   /**
   * AdobeID who started the pipeline. Empty for auto triggered builds
   * @return user
  **/
  @Schema(example = "0123456789ABCDE@AdobeID", description = "AdobeID who started the pipeline. Empty for auto triggered builds")
  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public PipelineExecution status(StatusEnum status) {
    this.status = status;
    return this;
  }

   /**
   * Status of the execution
   * @return status
  **/
  @Schema(description = "Status of the execution")
  public StatusEnum getStatus() {
    return status;
  }

  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  public PipelineExecution trigger(TriggerEnum trigger) {
    this.trigger = trigger;
    return this;
  }

   /**
   * How the execution was triggered.
   * @return trigger
  **/
  @Schema(description = "How the execution was triggered.")
  public TriggerEnum getTrigger() {
    return trigger;
  }

  public void setTrigger(TriggerEnum trigger) {
    this.trigger = trigger;
  }

  public PipelineExecution createdAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

   /**
   * Start time
   * @return createdAt
  **/
  @Schema(description = "Start time")
  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public PipelineExecution updatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

   /**
   * Date of last status change
   * @return updatedAt
  **/
  @Schema(description = "Date of last status change")
  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public PipelineExecution finishedAt(OffsetDateTime finishedAt) {
    this.finishedAt = finishedAt;
    return this;
  }

   /**
   * Date the execution reached a final state
   * @return finishedAt
  **/
  @Schema(description = "Date the execution reached a final state")
  public OffsetDateTime getFinishedAt() {
    return finishedAt;
  }

  public void setFinishedAt(OffsetDateTime finishedAt) {
    this.finishedAt = finishedAt;
  }

  public PipelineExecution _embedded(PipelineExecutionEmbedded _embedded) {
    this._embedded = _embedded;
    return this;
  }

   /**
   * Get _embedded
   * @return _embedded
  **/
  @Schema(description = "")
  public PipelineExecutionEmbedded getEmbedded() {
    return _embedded;
  }

  public void setEmbedded(PipelineExecutionEmbedded _embedded) {
    this._embedded = _embedded;
  }

  public PipelineExecution _links(PipelineExecutionLinks _links) {
    this._links = _links;
    return this;
  }

   /**
   * Get _links
   * @return _links
  **/
  @Schema(description = "")
  public PipelineExecutionLinks getLinks() {
    return _links;
  }

  public void setLinks(PipelineExecutionLinks _links) {
    this._links = _links;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PipelineExecution pipelineExecution = (PipelineExecution) o;
    return Objects.equals(this.id, pipelineExecution.id) &&
        Objects.equals(this.programId, pipelineExecution.programId) &&
        Objects.equals(this.pipelineId, pipelineExecution.pipelineId) &&
        Objects.equals(this.artifactsVersion, pipelineExecution.artifactsVersion) &&
        Objects.equals(this.user, pipelineExecution.user) &&
        Objects.equals(this.status, pipelineExecution.status) &&
        Objects.equals(this.trigger, pipelineExecution.trigger) &&
        Objects.equals(this.createdAt, pipelineExecution.createdAt) &&
        Objects.equals(this.updatedAt, pipelineExecution.updatedAt) &&
        Objects.equals(this.finishedAt, pipelineExecution.finishedAt) &&
        Objects.equals(this._embedded, pipelineExecution._embedded) &&
        Objects.equals(this._links, pipelineExecution._links);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, programId, pipelineId, artifactsVersion, user, status, trigger, createdAt, updatedAt, finishedAt, _embedded, _links);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PipelineExecution {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    programId: ").append(toIndentedString(programId)).append("\n");
    sb.append("    pipelineId: ").append(toIndentedString(pipelineId)).append("\n");
    sb.append("    artifactsVersion: ").append(toIndentedString(artifactsVersion)).append("\n");
    sb.append("    user: ").append(toIndentedString(user)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    trigger: ").append(toIndentedString(trigger)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
    sb.append("    finishedAt: ").append(toIndentedString(finishedAt)).append("\n");
    sb.append("    _embedded: ").append(toIndentedString(_embedded)).append("\n");
    sb.append("    _links: ").append(toIndentedString(_links)).append("\n");
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