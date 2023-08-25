/*
 * Cloud Manager Event Definitions
 * No description provided (generated by Swagger Codegen https://github.com/swagger-api/swagger-codegen)
 *
 * OpenAPI spec version: 1.0.0
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package io.adobe.cloudmanager.event;

/*-
 * #%L
 * Adobe Cloud Manager Client Library
 * %%
 * Copyright (C) 2020 - 2023 Adobe Inc.
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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.adobe.cloudmanager.event.Organization;
import io.adobe.cloudmanager.event.PipelineExecution;
import io.adobe.cloudmanager.event.User;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.io.Serializable;
/**
 * PipelineExecutionStartEventEvent
 */



public class PipelineExecutionStartEventEvent implements Serializable{
  private static final long serialVersionUID = 1L;
  @JsonProperty("@id")
  private String _atId = null;

  @JsonProperty("@type")
  private String _atType = null;

  @JsonProperty("xdmEventEnvelope:objectType")
  private String xdmEventEnvelopeobjectType = null;

  @JsonProperty("activitystreams:published")
  private OffsetDateTime activitystreamspublished = null;

  @JsonProperty("activitystreams:to")
  private Organization activitystreamsto = null;

  @JsonProperty("activitystreams:actor")
  private User activitystreamsactor = null;

  @JsonProperty("activitystreams:object")
  private PipelineExecution activitystreamsobject = null;

  public PipelineExecutionStartEventEvent _atId(String _atId) {
    this._atId = _atId;
    return this;
  }

   /**
   * A unique identifier for the event
   * @return _atId
  **/
  @Schema(description = "A unique identifier for the event")
  public String getAtId() {
    return _atId;
  }

  public void setAtId(String _atId) {
    this._atId = _atId;
  }

  public PipelineExecutionStartEventEvent _atType(String _atType) {
    this._atType = _atType;
    return this;
  }

   /**
   * The XDM event type.
   * @return _atType
  **/
  @Schema(description = "The XDM event type.")
  public String getAtType() {
    return _atType;
  }

  public void setAtType(String _atType) {
    this._atType = _atType;
  }

  public PipelineExecutionStartEventEvent xdmEventEnvelopeobjectType(String xdmEventEnvelopeobjectType) {
    this.xdmEventEnvelopeobjectType = xdmEventEnvelopeobjectType;
    return this;
  }

   /**
   * The object type.
   * @return xdmEventEnvelopeobjectType
  **/
  @Schema(description = "The object type.")
  public String getXdmEventEnvelopeobjectType() {
    return xdmEventEnvelopeobjectType;
  }

  public void setXdmEventEnvelopeobjectType(String xdmEventEnvelopeobjectType) {
    this.xdmEventEnvelopeobjectType = xdmEventEnvelopeobjectType;
  }

  public PipelineExecutionStartEventEvent activitystreamspublished(OffsetDateTime activitystreamspublished) {
    this.activitystreamspublished = activitystreamspublished;
    return this;
  }

   /**
   * The timestamp of the event.
   * @return activitystreamspublished
  **/
  @Schema(description = "The timestamp of the event.")
  public OffsetDateTime getActivitystreamspublished() {
    return activitystreamspublished;
  }

  public void setActivitystreamspublished(OffsetDateTime activitystreamspublished) {
    this.activitystreamspublished = activitystreamspublished;
  }

  public PipelineExecutionStartEventEvent activitystreamsto(Organization activitystreamsto) {
    this.activitystreamsto = activitystreamsto;
    return this;
  }

   /**
   * Get activitystreamsto
   * @return activitystreamsto
  **/
  @Schema(description = "")
  public Organization getActivitystreamsto() {
    return activitystreamsto;
  }

  public void setActivitystreamsto(Organization activitystreamsto) {
    this.activitystreamsto = activitystreamsto;
  }

  public PipelineExecutionStartEventEvent activitystreamsactor(User activitystreamsactor) {
    this.activitystreamsactor = activitystreamsactor;
    return this;
  }

   /**
   * Get activitystreamsactor
   * @return activitystreamsactor
  **/
  @Schema(description = "")
  public User getActivitystreamsactor() {
    return activitystreamsactor;
  }

  public void setActivitystreamsactor(User activitystreamsactor) {
    this.activitystreamsactor = activitystreamsactor;
  }

  public PipelineExecutionStartEventEvent activitystreamsobject(PipelineExecution activitystreamsobject) {
    this.activitystreamsobject = activitystreamsobject;
    return this;
  }

   /**
   * Get activitystreamsobject
   * @return activitystreamsobject
  **/
  @Schema(description = "")
  public PipelineExecution getActivitystreamsobject() {
    return activitystreamsobject;
  }

  public void setActivitystreamsobject(PipelineExecution activitystreamsobject) {
    this.activitystreamsobject = activitystreamsobject;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PipelineExecutionStartEventEvent pipelineExecutionStartEventEvent = (PipelineExecutionStartEventEvent) o;
    return Objects.equals(this._atId, pipelineExecutionStartEventEvent._atId) &&
        Objects.equals(this._atType, pipelineExecutionStartEventEvent._atType) &&
        Objects.equals(this.xdmEventEnvelopeobjectType, pipelineExecutionStartEventEvent.xdmEventEnvelopeobjectType) &&
        Objects.equals(this.activitystreamspublished, pipelineExecutionStartEventEvent.activitystreamspublished) &&
        Objects.equals(this.activitystreamsto, pipelineExecutionStartEventEvent.activitystreamsto) &&
        Objects.equals(this.activitystreamsactor, pipelineExecutionStartEventEvent.activitystreamsactor) &&
        Objects.equals(this.activitystreamsobject, pipelineExecutionStartEventEvent.activitystreamsobject);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_atId, _atType, xdmEventEnvelopeobjectType, activitystreamspublished, activitystreamsto, activitystreamsactor, activitystreamsobject);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PipelineExecutionStartEventEvent {\n");
    
    sb.append("    _atId: ").append(toIndentedString(_atId)).append("\n");
    sb.append("    _atType: ").append(toIndentedString(_atType)).append("\n");
    sb.append("    xdmEventEnvelopeobjectType: ").append(toIndentedString(xdmEventEnvelopeobjectType)).append("\n");
    sb.append("    activitystreamspublished: ").append(toIndentedString(activitystreamspublished)).append("\n");
    sb.append("    activitystreamsto: ").append(toIndentedString(activitystreamsto)).append("\n");
    sb.append("    activitystreamsactor: ").append(toIndentedString(activitystreamsactor)).append("\n");
    sb.append("    activitystreamsobject: ").append(toIndentedString(activitystreamsobject)).append("\n");
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
