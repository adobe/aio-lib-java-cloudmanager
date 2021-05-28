package io.adobe.cloudmanager;

/*-
 * #%L
 * Adobe Cloud Manager Client Library
 * %%
 * Copyright (C) 2020 - 2021 Adobe Inc.
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

import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

public interface Pipeline {

  /**
   * The id of this pipeline.
   *
   * @return the id
   */
  String getId();

  /**
   * The program context of this pipeline.
   *
   * @return the program id
   */
  String getProgramId();

  /**
   * The name of this pipeline.
   *
   * @return the name
   */
  String getName();

  /**
   * The current status of the Pipeline
   *
   * @return the status
   */
  Status getStatusState();

  /**
   * Starts this pipeline.
   *
   * @return the new execution.
   * @throws CloudManagerApiException when any errors occur.
   */
  PipelineExecution startExecution() throws CloudManagerApiException;

  /**
   * Returns the specified execution.
   *
   * @param executionId the id of the execution to retrieve
   * @return the execution details
   * @throws CloudManagerApiException when any error occurs
   */
  PipelineExecution getExecution(String executionId) throws CloudManagerApiException;

  /**
   * Updates this pipeline with the specified changes.
   *
   * @param update the updates to make to this pipeline
   * @return the updated Pipeline.
   * @throws CloudManagerApiException when any errors occur
   */
  Pipeline update(PipelineUpdate update) throws CloudManagerApiException;

  /**
   * Delete this pipeline.
   *
   * @throws CloudManagerApiException when any error occurs.
   */
  void delete() throws CloudManagerApiException;

  /**
   * Retrieve the variables associated with this pipeline.
   *
   * @return the variables in this pipeline
   * @throws CloudManagerApiException when any errors occur
   */
  Set<Variable> listVariables() throws CloudManagerApiException;

  /**
   * Sets the specified variables on this pipeline.
   *
   * @param variables the variables to set
   * @return the complete list of variables in this pipeline
   * @throws CloudManagerApiException when any error occurs.
   */
  Set<Variable> setVariables(Variable... variables) throws CloudManagerApiException;

  /**
   * Link to this pipeline.
   *
   * @return the link to this pipeline.
   */
  String getSelfLink();

  /**
   * Pipeline status values
   *
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#!AdobeDocs/cloudmanager-api-docs/master/swagger-specs/api.yaml">Cloud Manager Pipeline Model</a>
   */
  enum Status {
    IDLE("IDLE"),
    BUSY("BUSY"),
    WAITING("WAITING");

    private String value;

    Status(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    public static Status fromValue(String text) {
      for (Status b : Status.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  /**
   * Predicate for pipelines based on BUSY status.
   */
  Predicate<Pipeline> IS_BUSY = (pipeline -> Status.BUSY == pipeline.getStatusState());

  /**
   * Predicate to use for retrieving a pipeline based on its name.
   */
  final class NamePredicate implements Predicate<Pipeline> {

    private final String name;

    public NamePredicate(String name) {
      this.name = name;
    }

    @Override
    public boolean test(Pipeline pipeline) { return StringUtils.equals(name, pipeline.getName()); }
  }

  /**
   * Predicate to use for retrieving a pipeline based on its id.
   */
  final class IdPredicate implements Predicate<Pipeline> {

    private final String id;

    public IdPredicate(String id) { this.id = id; }

    @Override
    public boolean test(Pipeline pipeline) {
      return StringUtils.equals(id, pipeline.getId());
    }
  }
}
