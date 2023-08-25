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
import io.adobe.cloudmanager.impl.generated.PipelineExecutionStepState;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;
/**
 * PipelineExecutionEmbedded
 */



public class PipelineExecutionEmbedded implements Serializable{
  private static final long serialVersionUID = 1L;
  @JsonProperty("stepStates")
  private List<PipelineExecutionStepState> stepStates = null;

  public PipelineExecutionEmbedded stepStates(List<PipelineExecutionStepState> stepStates) {
    this.stepStates = stepStates;
    return this;
  }

  public PipelineExecutionEmbedded addStepStatesItem(PipelineExecutionStepState stepStatesItem) {
    if (this.stepStates == null) {
      this.stepStates = new ArrayList<>();
    }
    this.stepStates.add(stepStatesItem);
    return this;
  }

   /**
   * Get stepStates
   * @return stepStates
  **/
  @Schema(description = "")
  public List<PipelineExecutionStepState> getStepStates() {
    return stepStates;
  }

  public void setStepStates(List<PipelineExecutionStepState> stepStates) {
    this.stepStates = stepStates;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PipelineExecutionEmbedded pipelineExecutionEmbedded = (PipelineExecutionEmbedded) o;
    return Objects.equals(this.stepStates, pipelineExecutionEmbedded.stepStates);
  }

  @Override
  public int hashCode() {
    return Objects.hash(stepStates);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PipelineExecutionEmbedded {\n");
    
    sb.append("    stepStates: ").append(toIndentedString(stepStates)).append("\n");
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
