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
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;
/**
 * Filtering and sorting page details
 */
@Schema(description = "Filtering and sorting page details")


public class RequestedPageDetails implements Serializable{
  private static final long serialVersionUID = 1L;
  @JsonProperty("start")
  private Integer start = null;

  @JsonProperty("limit")
  private Integer limit = null;

  @JsonProperty("next")
  private Integer next = null;

  @JsonProperty("prev")
  private Integer prev = null;

  @JsonProperty("orderBy")
  private String orderBy = null;

  @JsonProperty("property")
  private List<String> property = null;

  @JsonProperty("type")
  private String type = null;

  public RequestedPageDetails start(Integer start) {
    this.start = start;
    return this;
  }

   /**
   * The start index for the current page of results
   * @return start
  **/
  @Schema(description = "The start index for the current page of results")
  public Integer getStart() {
    return start;
  }

  public void setStart(Integer start) {
    this.start = start;
  }

  public RequestedPageDetails limit(Integer limit) {
    this.limit = limit;
    return this;
  }

   /**
   * The item limit for the current page of results
   * @return limit
  **/
  @Schema(description = "The item limit for the current page of results")
  public Integer getLimit() {
    return limit;
  }

  public void setLimit(Integer limit) {
    this.limit = limit;
  }

  public RequestedPageDetails next(Integer next) {
    this.next = next;
    return this;
  }

   /**
   * The start index for the next page of results
   * @return next
  **/
  @Schema(description = "The start index for the next page of results")
  public Integer getNext() {
    return next;
  }

  public void setNext(Integer next) {
    this.next = next;
  }

  public RequestedPageDetails prev(Integer prev) {
    this.prev = prev;
    return this;
  }

   /**
   * The start index for the previous page of results
   * @return prev
  **/
  @Schema(description = "The start index for the previous page of results")
  public Integer getPrev() {
    return prev;
  }

  public void setPrev(Integer prev) {
    this.prev = prev;
  }

  public RequestedPageDetails orderBy(String orderBy) {
    this.orderBy = orderBy;
    return this;
  }

   /**
   * Get orderBy
   * @return orderBy
  **/
  @Schema(description = "")
  public String getOrderBy() {
    return orderBy;
  }

  public void setOrderBy(String orderBy) {
    this.orderBy = orderBy;
  }

  public RequestedPageDetails property(List<String> property) {
    this.property = property;
    return this;
  }

  public RequestedPageDetails addPropertyItem(String propertyItem) {
    if (this.property == null) {
      this.property = new ArrayList<>();
    }
    this.property.add(propertyItem);
    return this;
  }

   /**
   * Get property
   * @return property
  **/
  @Schema(description = "")
  public List<String> getProperty() {
    return property;
  }

  public void setProperty(List<String> property) {
    this.property = property;
  }

  public RequestedPageDetails type(String type) {
    this.type = type;
    return this;
  }

   /**
   * Get type
   * @return type
  **/
  @Schema(description = "")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RequestedPageDetails requestedPageDetails = (RequestedPageDetails) o;
    return Objects.equals(this.start, requestedPageDetails.start) &&
        Objects.equals(this.limit, requestedPageDetails.limit) &&
        Objects.equals(this.next, requestedPageDetails.next) &&
        Objects.equals(this.prev, requestedPageDetails.prev) &&
        Objects.equals(this.orderBy, requestedPageDetails.orderBy) &&
        Objects.equals(this.property, requestedPageDetails.property) &&
        Objects.equals(this.type, requestedPageDetails.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(start, limit, next, prev, orderBy, property, type);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RequestedPageDetails {\n");
    
    sb.append("    start: ").append(toIndentedString(start)).append("\n");
    sb.append("    limit: ").append(toIndentedString(limit)).append("\n");
    sb.append("    next: ").append(toIndentedString(next)).append("\n");
    sb.append("    prev: ").append(toIndentedString(prev)).append("\n");
    sb.append("    orderBy: ").append(toIndentedString(orderBy)).append("\n");
    sb.append("    property: ").append(toIndentedString(property)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
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
