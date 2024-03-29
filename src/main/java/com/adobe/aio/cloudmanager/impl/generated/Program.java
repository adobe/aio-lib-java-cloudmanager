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
import java.time.OffsetDateTime;
import java.io.Serializable;
/**
 * A representation of a Program
 */
@Schema(description = "A representation of a Program")


public class Program implements Serializable{
  private static final long serialVersionUID = 1L;
  @JsonProperty("id")
  private String id = null;

  @JsonProperty("name")
  private String name = null;

  @JsonProperty("enabled")
  private Boolean enabled = false;

  @JsonProperty("tenantId")
  private String tenantId = null;

  @JsonProperty("imsOrgId")
  private String imsOrgId = null;

  /**
   * Status of the program
   */
  public enum StatusEnum {
    CREATING("creating"),
    READY("ready"),
    DELETING("deleting"),
    DELETED("deleted"),
    DELETED_FAILED("deleted_failed"),
    FAILED("failed");

    private String value;

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
    public static StatusEnum fromValue(String input) {
      for (StatusEnum b : StatusEnum.values()) {
        if (b.value.equals(input)) {
          return b;
        }
      }
      return null;
    }

  }  @JsonProperty("status")
  private StatusEnum status = null;

  /**
   * The type of program
   */
  public enum TypeEnum {
    AEM_MANAGED_SERVICES("aem_managed_services"),
    AEM_CLOUD_SERVICE("aem_cloud_service"),
    MEDIA_LIBRARY("media_library");

    private String value;

    TypeEnum(String value) {
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
    public static TypeEnum fromValue(String input) {
      for (TypeEnum b : TypeEnum.values()) {
        if (b.value.equals(input)) {
          return b;
        }
      }
      return null;
    }

  }  @JsonProperty("type")
  private TypeEnum type = null;

  @JsonProperty("capabilities")
  private ProgramCapabilities capabilities = null;

  @JsonProperty("createdAt")
  private OffsetDateTime createdAt = null;

  @JsonProperty("updatedAt")
  private OffsetDateTime updatedAt = null;

  @JsonProperty("_links")
  private ProgramLinks _links = null;

  public Program id(String id) {
    this.id = id;
    return this;
  }

   /**
   * Identifier of the program. Unique within the space.
   * @return id
  **/
  @Schema(example = "14", description = "Identifier of the program. Unique within the space.")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Program name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Name of the program
   * @return name
  **/
  @Schema(example = "AcmeCorp Main Site", required = true, description = "Name of the program")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Program enabled(Boolean enabled) {
    this.enabled = enabled;
    return this;
  }

   /**
   * Whether this Program has been enabled for Cloud Manager usage
   * @return enabled
  **/
  @Schema(description = "Whether this Program has been enabled for Cloud Manager usage")
  public Boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public Program tenantId(String tenantId) {
    this.tenantId = tenantId;
    return this;
  }

   /**
   * Tenant Id
   * @return tenantId
  **/
  @Schema(example = "acmeCorp", description = "Tenant Id")
  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public Program imsOrgId(String imsOrgId) {
    this.imsOrgId = imsOrgId;
    return this;
  }

   /**
   * Organisation Id
   * @return imsOrgId
  **/
  @Schema(example = "6522A55453334E247F120101@AdobeOrg", description = "Organisation Id")
  public String getImsOrgId() {
    return imsOrgId;
  }

  public void setImsOrgId(String imsOrgId) {
    this.imsOrgId = imsOrgId;
  }

  public Program status(StatusEnum status) {
    this.status = status;
    return this;
  }

   /**
   * Status of the program
   * @return status
  **/
  @Schema(description = "Status of the program")
  public StatusEnum getStatus() {
    return status;
  }

  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  public Program type(TypeEnum type) {
    this.type = type;
    return this;
  }

   /**
   * The type of program
   * @return type
  **/
  @Schema(example = "aem_cloud_service", required = true, description = "The type of program")
  public TypeEnum getType() {
    return type;
  }

  public void setType(TypeEnum type) {
    this.type = type;
  }

  public Program capabilities(ProgramCapabilities capabilities) {
    this.capabilities = capabilities;
    return this;
  }

   /**
   * Get capabilities
   * @return capabilities
  **/
  @Schema(description = "")
  public ProgramCapabilities getCapabilities() {
    return capabilities;
  }

  public void setCapabilities(ProgramCapabilities capabilities) {
    this.capabilities = capabilities;
  }

  public Program createdAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

   /**
   * Created time
   * @return createdAt
  **/
  @Schema(description = "Created time")
  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public Program updatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

   /**
   * Date of last change
   * @return updatedAt
  **/
  @Schema(description = "Date of last change")
  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public Program _links(ProgramLinks _links) {
    this._links = _links;
    return this;
  }

   /**
   * Get _links
   * @return _links
  **/
  @Schema(description = "")
  public ProgramLinks getLinks() {
    return _links;
  }

  public void setLinks(ProgramLinks _links) {
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
    Program program = (Program) o;
    return Objects.equals(this.id, program.id) &&
        Objects.equals(this.name, program.name) &&
        Objects.equals(this.enabled, program.enabled) &&
        Objects.equals(this.tenantId, program.tenantId) &&
        Objects.equals(this.imsOrgId, program.imsOrgId) &&
        Objects.equals(this.status, program.status) &&
        Objects.equals(this.type, program.type) &&
        Objects.equals(this.capabilities, program.capabilities) &&
        Objects.equals(this.createdAt, program.createdAt) &&
        Objects.equals(this.updatedAt, program.updatedAt) &&
        Objects.equals(this._links, program._links);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, enabled, tenantId, imsOrgId, status, type, capabilities, createdAt, updatedAt, _links);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Program {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    enabled: ").append(toIndentedString(enabled)).append("\n");
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
    sb.append("    imsOrgId: ").append(toIndentedString(imsOrgId)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    capabilities: ").append(toIndentedString(capabilities)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
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
