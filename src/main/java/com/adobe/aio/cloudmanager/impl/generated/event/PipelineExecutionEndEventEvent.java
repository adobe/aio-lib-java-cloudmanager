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

package com.adobe.aio.cloudmanager.impl.generated.event;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.io.Serializable;
/**
 * PipelineExecutionEndEventEvent
 */



public class PipelineExecutionEndEventEvent implements Serializable{
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

  @JsonProperty("activitystreams:object")
  private PipelineExecution activitystreamsobject = null;

  public PipelineExecutionEndEventEvent _atId(String _atId) {
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

  public PipelineExecutionEndEventEvent _atType(String _atType) {
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

  public PipelineExecutionEndEventEvent xdmEventEnvelopeobjectType(String xdmEventEnvelopeobjectType) {
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

  public PipelineExecutionEndEventEvent activitystreamspublished(OffsetDateTime activitystreamspublished) {
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

  public PipelineExecutionEndEventEvent activitystreamsto(Organization activitystreamsto) {
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

  public PipelineExecutionEndEventEvent activitystreamsobject(PipelineExecution activitystreamsobject) {
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
    PipelineExecutionEndEventEvent pipelineExecutionEndEventEvent = (PipelineExecutionEndEventEvent) o;
    return Objects.equals(this._atId, pipelineExecutionEndEventEvent._atId) &&
        Objects.equals(this._atType, pipelineExecutionEndEventEvent._atType) &&
        Objects.equals(this.xdmEventEnvelopeobjectType, pipelineExecutionEndEventEvent.xdmEventEnvelopeobjectType) &&
        Objects.equals(this.activitystreamspublished, pipelineExecutionEndEventEvent.activitystreamspublished) &&
        Objects.equals(this.activitystreamsto, pipelineExecutionEndEventEvent.activitystreamsto) &&
        Objects.equals(this.activitystreamsobject, pipelineExecutionEndEventEvent.activitystreamsobject);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_atId, _atType, xdmEventEnvelopeobjectType, activitystreamspublished, activitystreamsto, activitystreamsobject);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PipelineExecutionEndEventEvent {\n");
    
    sb.append("    _atId: ").append(toIndentedString(_atId)).append("\n");
    sb.append("    _atType: ").append(toIndentedString(_atType)).append("\n");
    sb.append("    xdmEventEnvelopeobjectType: ").append(toIndentedString(xdmEventEnvelopeobjectType)).append("\n");
    sb.append("    activitystreamspublished: ").append(toIndentedString(activitystreamspublished)).append("\n");
    sb.append("    activitystreamsto: ").append(toIndentedString(activitystreamsto)).append("\n");
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
