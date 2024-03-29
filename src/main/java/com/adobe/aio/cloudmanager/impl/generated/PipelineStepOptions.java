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
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;
/**
 * Options for a pipeline step. Will vary based on step action and platform.
 */
@Schema(description = "Options for a pipeline step. Will vary based on step action and platform.")


public class PipelineStepOptions implements Serializable{
  private static final long serialVersionUID = 1L;
  @JsonProperty("skipDetachingDispatchers")
  private Boolean skipDetachingDispatchers = null;

  @JsonProperty("dispatcherCacheInvalidationPaths")
  private List<String> dispatcherCacheInvalidationPaths = null;

  @JsonProperty("dispatcherCacheFlushPaths")
  private List<String> dispatcherCacheFlushPaths = null;

  /**
   * For managed steps on AMS pipelines, which CSE will be responsible for CSE oversight.
   */
  public enum CseEnum {
    MY_CSE("MY_CSE"),
    ANY_CSE("ANY_CSE");

    private String value;

    CseEnum(String value) {
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
    public static CseEnum fromValue(String input) {
      for (CseEnum b : CseEnum.values()) {
        if (b.value.equals(input)) {
          return b;
        }
      }
      return null;
    }

  }  @JsonProperty("cse")
  private CseEnum cse = null;

  @JsonProperty("popularPagesWeight")
  private Integer popularPagesWeight = null;

  @JsonProperty("newPagesWeight")
  private Integer newPagesWeight = null;

  @JsonProperty("otherPagesWeight")
  private Integer otherPagesWeight = null;

  @JsonProperty("imageAssetsWeight")
  private Integer imageAssetsWeight = null;

  @JsonProperty("pdfAssetsWeight")
  private Integer pdfAssetsWeight = null;

  public PipelineStepOptions skipDetachingDispatchers(Boolean skipDetachingDispatchers) {
    this.skipDetachingDispatchers = skipDetachingDispatchers;
    return this;
  }

   /**
   * For deploy steps on AMS pipelines, if true will not detach dispatcher from the load balancer.
   * @return skipDetachingDispatchers
  **/
  @Schema(description = "For deploy steps on AMS pipelines, if true will not detach dispatcher from the load balancer.")
  public Boolean isSkipDetachingDispatchers() {
    return skipDetachingDispatchers;
  }

  public void setSkipDetachingDispatchers(Boolean skipDetachingDispatchers) {
    this.skipDetachingDispatchers = skipDetachingDispatchers;
  }

  public PipelineStepOptions dispatcherCacheInvalidationPaths(List<String> dispatcherCacheInvalidationPaths) {
    this.dispatcherCacheInvalidationPaths = dispatcherCacheInvalidationPaths;
    return this;
  }

  public PipelineStepOptions addDispatcherCacheInvalidationPathsItem(String dispatcherCacheInvalidationPathsItem) {
    if (this.dispatcherCacheInvalidationPaths == null) {
      this.dispatcherCacheInvalidationPaths = new ArrayList<>();
    }
    this.dispatcherCacheInvalidationPaths.add(dispatcherCacheInvalidationPathsItem);
    return this;
  }

   /**
   * For deploy steps on AMS pipelines, list of paths to invalidate on dispatchers after package installation.
   * @return dispatcherCacheInvalidationPaths
  **/
  @Schema(description = "For deploy steps on AMS pipelines, list of paths to invalidate on dispatchers after package installation.")
  public List<String> getDispatcherCacheInvalidationPaths() {
    return dispatcherCacheInvalidationPaths;
  }

  public void setDispatcherCacheInvalidationPaths(List<String> dispatcherCacheInvalidationPaths) {
    this.dispatcherCacheInvalidationPaths = dispatcherCacheInvalidationPaths;
  }

  public PipelineStepOptions dispatcherCacheFlushPaths(List<String> dispatcherCacheFlushPaths) {
    this.dispatcherCacheFlushPaths = dispatcherCacheFlushPaths;
    return this;
  }

  public PipelineStepOptions addDispatcherCacheFlushPathsItem(String dispatcherCacheFlushPathsItem) {
    if (this.dispatcherCacheFlushPaths == null) {
      this.dispatcherCacheFlushPaths = new ArrayList<>();
    }
    this.dispatcherCacheFlushPaths.add(dispatcherCacheFlushPathsItem);
    return this;
  }

   /**
   * For deploy steps on AMS pipelines, list of paths to flush on dispatchers after package installation.
   * @return dispatcherCacheFlushPaths
  **/
  @Schema(description = "For deploy steps on AMS pipelines, list of paths to flush on dispatchers after package installation.")
  public List<String> getDispatcherCacheFlushPaths() {
    return dispatcherCacheFlushPaths;
  }

  public void setDispatcherCacheFlushPaths(List<String> dispatcherCacheFlushPaths) {
    this.dispatcherCacheFlushPaths = dispatcherCacheFlushPaths;
  }

  public PipelineStepOptions cse(CseEnum cse) {
    this.cse = cse;
    return this;
  }

   /**
   * For managed steps on AMS pipelines, which CSE will be responsible for CSE oversight.
   * @return cse
  **/
  @Schema(description = "For managed steps on AMS pipelines, which CSE will be responsible for CSE oversight.")
  public CseEnum getCse() {
    return cse;
  }

  public void setCse(CseEnum cse) {
    this.cse = cse;
  }

  public PipelineStepOptions popularPagesWeight(Integer popularPagesWeight) {
    this.popularPagesWeight = popularPagesWeight;
    return this;
  }

   /**
   * For loadTest steps on AMS pipelines, the percentage of performance test traffic which will be sent to the popular pages bucket.
   * @return popularPagesWeight
  **/
  @Schema(description = "For loadTest steps on AMS pipelines, the percentage of performance test traffic which will be sent to the popular pages bucket.")
  public Integer getPopularPagesWeight() {
    return popularPagesWeight;
  }

  public void setPopularPagesWeight(Integer popularPagesWeight) {
    this.popularPagesWeight = popularPagesWeight;
  }

  public PipelineStepOptions newPagesWeight(Integer newPagesWeight) {
    this.newPagesWeight = newPagesWeight;
    return this;
  }

   /**
   * For loadTest steps on AMS pipelines, the percentage of performance test traffic which will be sent to the new pages bucket.
   * @return newPagesWeight
  **/
  @Schema(description = "For loadTest steps on AMS pipelines, the percentage of performance test traffic which will be sent to the new pages bucket.")
  public Integer getNewPagesWeight() {
    return newPagesWeight;
  }

  public void setNewPagesWeight(Integer newPagesWeight) {
    this.newPagesWeight = newPagesWeight;
  }

  public PipelineStepOptions otherPagesWeight(Integer otherPagesWeight) {
    this.otherPagesWeight = otherPagesWeight;
    return this;
  }

   /**
   * For loadTest steps on AMS pipelines, the percentage of performance test traffic which will be sent to the other pages bucket.
   * @return otherPagesWeight
  **/
  @Schema(description = "For loadTest steps on AMS pipelines, the percentage of performance test traffic which will be sent to the other pages bucket.")
  public Integer getOtherPagesWeight() {
    return otherPagesWeight;
  }

  public void setOtherPagesWeight(Integer otherPagesWeight) {
    this.otherPagesWeight = otherPagesWeight;
  }

  public PipelineStepOptions imageAssetsWeight(Integer imageAssetsWeight) {
    this.imageAssetsWeight = imageAssetsWeight;
    return this;
  }

   /**
   * For assetsTest steps on AMS pipelines, the percentage of asset uploads which will be test images.
   * @return imageAssetsWeight
  **/
  @Schema(description = "For assetsTest steps on AMS pipelines, the percentage of asset uploads which will be test images.")
  public Integer getImageAssetsWeight() {
    return imageAssetsWeight;
  }

  public void setImageAssetsWeight(Integer imageAssetsWeight) {
    this.imageAssetsWeight = imageAssetsWeight;
  }

  public PipelineStepOptions pdfAssetsWeight(Integer pdfAssetsWeight) {
    this.pdfAssetsWeight = pdfAssetsWeight;
    return this;
  }

   /**
   * For assetsTest steps on AMS pipelines, the percentage of asset uploads which will be test PDF files.
   * @return pdfAssetsWeight
  **/
  @Schema(description = "For assetsTest steps on AMS pipelines, the percentage of asset uploads which will be test PDF files.")
  public Integer getPdfAssetsWeight() {
    return pdfAssetsWeight;
  }

  public void setPdfAssetsWeight(Integer pdfAssetsWeight) {
    this.pdfAssetsWeight = pdfAssetsWeight;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PipelineStepOptions pipelineStepOptions = (PipelineStepOptions) o;
    return Objects.equals(this.skipDetachingDispatchers, pipelineStepOptions.skipDetachingDispatchers) &&
        Objects.equals(this.dispatcherCacheInvalidationPaths, pipelineStepOptions.dispatcherCacheInvalidationPaths) &&
        Objects.equals(this.dispatcherCacheFlushPaths, pipelineStepOptions.dispatcherCacheFlushPaths) &&
        Objects.equals(this.cse, pipelineStepOptions.cse) &&
        Objects.equals(this.popularPagesWeight, pipelineStepOptions.popularPagesWeight) &&
        Objects.equals(this.newPagesWeight, pipelineStepOptions.newPagesWeight) &&
        Objects.equals(this.otherPagesWeight, pipelineStepOptions.otherPagesWeight) &&
        Objects.equals(this.imageAssetsWeight, pipelineStepOptions.imageAssetsWeight) &&
        Objects.equals(this.pdfAssetsWeight, pipelineStepOptions.pdfAssetsWeight);
  }

  @Override
  public int hashCode() {
    return Objects.hash(skipDetachingDispatchers, dispatcherCacheInvalidationPaths, dispatcherCacheFlushPaths, cse, popularPagesWeight, newPagesWeight, otherPagesWeight, imageAssetsWeight, pdfAssetsWeight);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PipelineStepOptions {\n");
    
    sb.append("    skipDetachingDispatchers: ").append(toIndentedString(skipDetachingDispatchers)).append("\n");
    sb.append("    dispatcherCacheInvalidationPaths: ").append(toIndentedString(dispatcherCacheInvalidationPaths)).append("\n");
    sb.append("    dispatcherCacheFlushPaths: ").append(toIndentedString(dispatcherCacheFlushPaths)).append("\n");
    sb.append("    cse: ").append(toIndentedString(cse)).append("\n");
    sb.append("    popularPagesWeight: ").append(toIndentedString(popularPagesWeight)).append("\n");
    sb.append("    newPagesWeight: ").append(toIndentedString(newPagesWeight)).append("\n");
    sb.append("    otherPagesWeight: ").append(toIndentedString(otherPagesWeight)).append("\n");
    sb.append("    imageAssetsWeight: ").append(toIndentedString(imageAssetsWeight)).append("\n");
    sb.append("    pdfAssetsWeight: ").append(toIndentedString(pdfAssetsWeight)).append("\n");
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
