/*
 * Cloud Manager API
 * This API allows access to Cloud Manager programs, pipelines, and environments by an authorized technical account created through the Adobe I/O Console. The base url for this API is https://cloudmanager.adobe.io, e.g. to get the list of programs for an organization, you would make a GET request to https://cloudmanager.adobe.io/api/programs (with the correct set of headers as described below). This swagger file can be downloaded from https://raw.githubusercontent.com/AdobeDocs/cloudmanager-api-docs/master/swagger-specs/api.yaml.
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

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
/**
 * A sourcecode repository
 */
@Schema(description = "A sourcecode repository")

public class Repository implements Serializable{
  private static final long serialVersionUID = 1L;
  @JsonProperty("repo")
  private String repo = null;

  @JsonProperty("description")
  private String description = null;

  @JsonProperty("repositoryUrl")
  private String repositoryUrl = null;

  @JsonProperty("_links")
  private RepositoryLinks _links = null;

  public Repository repo(String repo) {
    this.repo = repo;
    return this;
  }

   /**
   * Repository name. Once set it cannot be updated
   * @return repo
  **/
  @Schema(example = "Adobe-Marketing-Cloud", description = "Repository name. Once set it cannot be updated")
  public String getRepo() {
    return repo;
  }

  public void setRepo(String repo) {
    this.repo = repo;
  }

  public Repository description(String description) {
    this.description = description;
    return this;
  }

   /**
   * description
   * @return description
  **/
  @Schema(example = "Description", description = "description")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Repository repositoryUrl(String repositoryUrl) {
    this.repositoryUrl = repositoryUrl;
    return this;
  }

   /**
   * Repository Url.
   * @return repositoryUrl
  **/
  @Schema(example = "https://git.cloudmanager.adobe.com/weretailprod/we-retail-global", description = "Repository Url.")
  public String getRepositoryUrl() {
    return repositoryUrl;
  }

  public void setRepositoryUrl(String repositoryUrl) {
    this.repositoryUrl = repositoryUrl;
  }

  public Repository _links(RepositoryLinks _links) {
    this._links = _links;
    return this;
  }

   /**
   * Get _links
   * @return _links
  **/
  @Schema(description = "")
  public RepositoryLinks getLinks() {
    return _links;
  }

  public void setLinks(RepositoryLinks _links) {
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
    Repository repository = (Repository) o;
    return Objects.equals(this.repo, repository.repo) &&
        Objects.equals(this.description, repository.description) &&
        Objects.equals(this.repositoryUrl, repository.repositoryUrl) &&
        Objects.equals(this._links, repository._links);
  }

  @Override
  public int hashCode() {
    return Objects.hash(repo, description, repositoryUrl, _links);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Repository {\n");
    
    sb.append("    repo: ").append(toIndentedString(repo)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    repositoryUrl: ").append(toIndentedString(repositoryUrl)).append("\n");
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