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
import io.adobe.cloudmanager.event.PipelineExecutionStepStartEventEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
/**
 * PipelineExecutionStepEndEvent
 */



public class PipelineExecutionStepEndEvent implements Serializable{
  private static final long serialVersionUID = 1L;
  @JsonProperty("event_id")
  private String eventId = null;

  @JsonProperty("event")
  private PipelineExecutionStepStartEventEvent event = null;

  public PipelineExecutionStepEndEvent eventId(String eventId) {
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

  public PipelineExecutionStepEndEvent event(PipelineExecutionStepStartEventEvent event) {
    this.event = event;
    return this;
  }

   /**
   * Get event
   * @return event
  **/
  @Schema(description = "")
  public PipelineExecutionStepStartEventEvent getEvent() {
    return event;
  }

  public void setEvent(PipelineExecutionStepStartEventEvent event) {
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
    PipelineExecutionStepEndEvent pipelineExecutionStepEndEvent = (PipelineExecutionStepEndEvent) o;
    return Objects.equals(this.eventId, pipelineExecutionStepEndEvent.eventId) &&
        Objects.equals(this.event, pipelineExecutionStepEndEvent.event);
  }

  @Override
  public int hashCode() {
    return Objects.hash(eventId, event);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PipelineExecutionStepEndEvent {\n");
    
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
