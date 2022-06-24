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

import java.util.Objects;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
/**
 * A change (create, update, delete) of a named value on a Pipeline
 */
@Schema(description = "A change (create, update, delete) of a named value on a Pipeline")

public class PipelineVariableUpdate implements Serializable{
  private static final long serialVersionUID = 1L;
  @JsonProperty("name")
  private String name = null;

  @JsonProperty("value")
  private String value = null;

  /**
   * Type of the variable. Default &#x60;string&#x60; if missing. &#x60;secretString&#x60; variables are encrypted at rest. The type of a variable be changed after creation; the variable must be deleted and recreated.
   */
  public enum TypeEnum {
    STRING("string"),
    SECRETSTRING("secretString");

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
    public static TypeEnum fromValue(String text) {
      for (TypeEnum b : TypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }

  }  @JsonProperty("type")
  private TypeEnum type = null;

  public PipelineVariableUpdate name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Name of the variable. Can only consist of a-z, A-Z, _ and 0-9 and cannot begin with a number.
   * @return name
  **/
  @Schema(example = "MY_VAR1", description = "Name of the variable. Can only consist of a-z, A-Z, _ and 0-9 and cannot begin with a number.")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public PipelineVariableUpdate value(String value) {
    this.value = value;
    return this;
  }

   /**
   * Value of the variable. Read-Write for non-secrets, write-only for secrets. The length of &#x60;secretString&#x60; values must be less than 500 characters. An empty value causes a variable to be deleted.
   * @return value
  **/
  @Schema(example = "myValue", description = "Value of the variable. Read-Write for non-secrets, write-only for secrets. The length of `secretString` values must be less than 500 characters. An empty value causes a variable to be deleted.")
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public PipelineVariableUpdate type(TypeEnum type) {
    this.type = type;
    return this;
  }

   /**
   * Type of the variable. Default &#x60;string&#x60; if missing. &#x60;secretString&#x60; variables are encrypted at rest. The type of a variable be changed after creation; the variable must be deleted and recreated.
   * @return type
  **/
  @Schema(example = "string", description = "Type of the variable. Default `string` if missing. `secretString` variables are encrypted at rest. The type of a variable be changed after creation; the variable must be deleted and recreated.")
  public TypeEnum getType() {
    return type;
  }

  public void setType(TypeEnum type) {
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
    PipelineVariableUpdate pipelineVariableUpdate = (PipelineVariableUpdate) o;
    return Objects.equals(this.name, pipelineVariableUpdate.name) &&
        Objects.equals(this.value, pipelineVariableUpdate.value) &&
        Objects.equals(this.type, pipelineVariableUpdate.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, value, type);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PipelineVariableUpdate {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
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
