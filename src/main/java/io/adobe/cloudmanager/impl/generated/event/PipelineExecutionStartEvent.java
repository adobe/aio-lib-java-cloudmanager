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

package io.adobe.cloudmanager.impl.generated.event;

import java.util.Objects;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.adobe.cloudmanager.impl.generated.event.PipelineExecutionStartEventEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
/**
 * PipelineExecutionStartEvent
 */



public class PipelineExecutionStartEvent implements Serializable{
  private static final long serialVersionUID = 1L;
  @JsonProperty("event_id")
  private String eventId = null;

  @JsonProperty("event")
  private PipelineExecutionStartEventEvent event = null;

  public PipelineExecutionStartEvent eventId(String eventId) {
    this.eventId = eventId;
    return this;
  }

   /**
   * Unique identifier for the event.
   * @return eventId
  **/
  @Schema(description = "Unique identifier for the event.")
  public String getEventId() {
    return eventId;
  }

  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  public PipelineExecutionStartEvent event(PipelineExecutionStartEventEvent event) {
    this.event = event;
    return this;
  }

   /**
   * Get event
   * @return event
  **/
  @Schema(description = "")
  public PipelineExecutionStartEventEvent getEvent() {
    return event;
  }

  public void setEvent(PipelineExecutionStartEventEvent event) {
    this.event = event;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PipelineExecutionStartEvent pipelineExecutionStartEvent = (PipelineExecutionStartEvent) o;
    return Objects.equals(this.eventId, pipelineExecutionStartEvent.eventId) &&
        Objects.equals(this.event, pipelineExecutionStartEvent.event);
  }

  @Override
  public int hashCode() {
    return Objects.hash(eventId, event);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PipelineExecutionStartEvent {\n");
    
    sb.append("    eventId: ").append(toIndentedString(eventId)).append("\n");
    sb.append("    event: ").append(toIndentedString(event)).append("\n");
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
