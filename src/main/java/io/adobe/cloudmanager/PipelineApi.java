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

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import javax.validation.constraints.NotNull;

public interface PipelineApi {

  /**
   * Lists all pipelines within the specified program.
   *
   * @param programId the program id
   * @return the list of pipelines
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<Pipeline> list(@NotNull String programId) throws CloudManagerApiException;

  /**
   * Lists all pipelines within the specified program.
   *
   * @param program the program
   * @return the list of pipelines
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<Pipeline> list(@NotNull Program program) throws CloudManagerApiException;

  /**
   * Lists all pipelines in the program that meet the predicate clause.
   *
   * @param programId the program id
   * @param predicate a predicate used to filter the pipelines
   * @return a list of pipelines
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<Pipeline> list(@NotNull String programId, @NotNull Predicate<Pipeline> predicate) throws CloudManagerApiException;

  /**
   * Returns the pipeline within the specified program, with the specified id.
   *
   * @param programId  the program id
   * @param pipelineId the pipeline id
   * @return the {@link Pipeline}
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Pipeline get(@NotNull String programId, String pipelineId) throws CloudManagerApiException;

  /**
   * Delete the specified pipeline.
   *
   * @param programId  the program context for the pipeline
   * @param pipelineId the id of the pipeline to delete
   * @throws CloudManagerApiException when any error occurs
   */
  void delete(@NotNull String programId, @NotNull String pipelineId) throws CloudManagerApiException;

  /**
   * Delete the specified pipeline.
   *
   * @param pipeline the pipeline to delete.
   * @throws CloudManagerApiException when any error occurs
   */
  void delete(@NotNull Pipeline pipeline) throws CloudManagerApiException;

  /**
   * Changes details about a pipeline.
   *
   * @param programId  the program id for pipeline context
   * @param pipelineId the id of the pipeline to change
   * @param updates    the updates to make to the pipeline
   * @return the updated pipeline
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Pipeline update(@NotNull String programId, @NotNull String pipelineId, @NotNull PipelineUpdate updates) throws CloudManagerApiException;

  /**
   * Changes details about a pipeline.
   *
   * @param pipeline the pipeline to update
   * @param updates  the updates to make to the pipeline
   * @return the updated pipeline
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Pipeline update(@NotNull Pipeline pipeline, @NotNull PipelineUpdate updates) throws CloudManagerApiException;

  /**
   * Invalidates the build cache for the specified pipeline.
   *
   * @param programId  the program id for the pipeline context
   * @param pipelineId the id of the pipeline
   * @throws CloudManagerApiException when any error occurs
   */
  void invalidateCache(@NotNull String programId, @NotNull String pipelineId) throws CloudManagerApiException;

  /**
   * Invalidates the build cache for the specified pipeline.
   *
   * @param pipeline the pipeline
   * @throws CloudManagerApiException when any error occurs
   */
  void invalidateCache(@NotNull Pipeline pipeline) throws CloudManagerApiException;

  /**
   * Lists all variables associated with the specified pipeline
   *
   * @param programId  the program id of the pipeline
   * @param pipelineId the pipeline id
   * @return set of variables in the pipeline
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Set<Variable> getVariables(@NotNull String programId, @NotNull String pipelineId) throws CloudManagerApiException;

  /**
   * Lists all variables associated with the specified pipeline
   *
   * @param pipeline the pipeline context
   * @return set of variables in the pipeline
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Set<Variable> getVariables(@NotNull Pipeline pipeline) throws CloudManagerApiException;

  /**
   * Sets the specified variables in the pipeline
   *
   * @param programId  the program context for the pipeline
   * @param pipelineId the pipeline id
   * @param variables  the variables to set
   * @return updated list of variables in the pipeline
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Set<Variable> setVariables(@NotNull String programId, @NotNull String pipelineId, Variable... variables) throws CloudManagerApiException;

  /**
   * Sets the specified variables in the pipeline
   *
   * @param pipeline  the pipeline context
   * @param variables the variables to set
   * @return updated list of variables in the pipeline
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Set<Variable> setVariables(@NotNull Pipeline pipeline, Variable... variables) throws CloudManagerApiException;
}
