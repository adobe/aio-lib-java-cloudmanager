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
import io.adobe.cloudmanager.impl.generated.PipelineExecutionListRepresentationEmbedded;
import io.adobe.cloudmanager.impl.generated.ProgramListLinks;
import io.adobe.cloudmanager.impl.generated.RequestedPageDetails;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
/**
 * List of pipeline executions
 */
@Schema(description = "List of pipeline executions")


public class PipelineExecutionListRepresentation implements Serializable{
  private static final long serialVersionUID = 1L;
  @JsonProperty("_totalNumberOfItems")
  private Integer _totalNumberOfItems = null;

  @JsonProperty("_page")
  private RequestedPageDetails _page = null;

  @JsonProperty("_embedded")
  private PipelineExecutionListRepresentationEmbedded _embedded = null;

  @JsonProperty("_links")
  private ProgramListLinks _links = null;

  public PipelineExecutionListRepresentation _totalNumberOfItems(Integer _totalNumberOfItems) {
    this._totalNumberOfItems = _totalNumberOfItems;
    return this;
  }

   /**
   * The total number of embedded items
   * @return _totalNumberOfItems
  **/
  @Schema(description = "The total number of embedded items")
  public Integer getTotalNumberOfItems() {
    return _totalNumberOfItems;
  }

  public void setTotalNumberOfItems(Integer _totalNumberOfItems) {
    this._totalNumberOfItems = _totalNumberOfItems;
  }

  public PipelineExecutionListRepresentation _page(RequestedPageDetails _page) {
    this._page = _page;
    return this;
  }

   /**
   * Get _page
   * @return _page
  **/
  @Schema(description = "")
  public RequestedPageDetails getPage() {
    return _page;
  }

  public void setPage(RequestedPageDetails _page) {
    this._page = _page;
  }

  public PipelineExecutionListRepresentation _embedded(PipelineExecutionListRepresentationEmbedded _embedded) {
    this._embedded = _embedded;
    return this;
  }

   /**
   * Get _embedded
   * @return _embedded
  **/
  @Schema(description = "")
  public PipelineExecutionListRepresentationEmbedded getEmbedded() {
    return _embedded;
  }

  public void setEmbedded(PipelineExecutionListRepresentationEmbedded _embedded) {
    this._embedded = _embedded;
  }

  public PipelineExecutionListRepresentation _links(ProgramListLinks _links) {
    this._links = _links;
    return this;
  }

   /**
   * Get _links
   * @return _links
  **/
  @Schema(description = "")
  public ProgramListLinks getLinks() {
    return _links;
  }

  public void setLinks(ProgramListLinks _links) {
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
    PipelineExecutionListRepresentation pipelineExecutionListRepresentation = (PipelineExecutionListRepresentation) o;
    return Objects.equals(this._totalNumberOfItems, pipelineExecutionListRepresentation._totalNumberOfItems) &&
        Objects.equals(this._page, pipelineExecutionListRepresentation._page) &&
        Objects.equals(this._embedded, pipelineExecutionListRepresentation._embedded) &&
        Objects.equals(this._links, pipelineExecutionListRepresentation._links);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_totalNumberOfItems, _page, _embedded, _links);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PipelineExecutionListRepresentation {\n");
    
    sb.append("    _totalNumberOfItems: ").append(toIndentedString(_totalNumberOfItems)).append("\n");
    sb.append("    _page: ").append(toIndentedString(_page)).append("\n");
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
