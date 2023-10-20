package io.adobe.cloudmanager;

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

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;

/**
 * A Pipeline definition.
 */
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
   * Delete this pipeline.
   *
   * @throws CloudManagerApiException when any error occurs
   */
  void delete() throws CloudManagerApiException;

  /**
   * Update this pipeline with the specified changes.
   *
   * @param update the updates to make to this pipeline
   * @return the updated Pipeline.
   * @throws CloudManagerApiException when any errors occur
   */
  Pipeline update(PipelineUpdate update) throws CloudManagerApiException;

  /**
   * Invalidate the build cache for this pipeline.
   *
   * @throws CloudManagerApiException when any error occurs
   */
  void invalidateCache() throws CloudManagerApiException;

  /**
   * Get the current execution of this pipeline, if it exists.
   *
   * @return An optional containing the execution details of the pipeline
   * @throws CloudManagerApiException when any error occurs
   */
  Optional<PipelineExecution> getCurrentExecution() throws CloudManagerApiException;

  /**
   * Start this pipeline.
   *
   * @return the new execution.
   * @throws CloudManagerApiException when any errors occur.
   */
  PipelineExecution startExecution() throws CloudManagerApiException;

  /**
   * Get the execution.
   *
   * @param executionId the id of the execution to retrieve
   * @return the execution details
   * @throws CloudManagerApiException when any error occurs
   */
  PipelineExecution getExecution(String executionId) throws CloudManagerApiException;

  /**
   * Retrieve the variables associated with this pipeline.
   *
   * @return the variables in this pipeline
   * @throws CloudManagerApiException when any errors occur
   */
  Set<Variable> getVariables() throws CloudManagerApiException;

  /**
   * Set the variables on this pipeline.
   *
   * @param variables the variables to set
   * @return the complete list of variables in this pipeline
   * @throws CloudManagerApiException when any error occurs
   */
  Set<Variable> setVariables(Variable... variables) throws CloudManagerApiException;

  /**
   * Pipeline status values
   */
  enum Status {
    IDLE,
    BUSY,
    WAITING
  }

  /**
   * Predicate for pipelines which are BUSY.
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
    public boolean test(Pipeline pipeline) {
      return StringUtils.equals(name, pipeline.getName());
    }
  }

  /**
   * Predicate to use for retrieving a pipeline based on its id.
   */
  final class IdPredicate implements Predicate<Pipeline> {

    private final String id;

    public IdPredicate(String id) {
      this.id = id;
    }

    @Override
    public boolean test(Pipeline pipeline) {
      return StringUtils.equals(id, pipeline.getId());
    }
  }
}
