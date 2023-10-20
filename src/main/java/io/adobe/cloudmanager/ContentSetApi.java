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
import javax.validation.constraints.NotNull;

import static io.adobe.cloudmanager.ContentSet.*;

/**
 * Content Set API
 */
public interface ContentSetApi {

  /**
   * List content sets within the specified program.
   *
   * @param programId the id of the program context
   * @return list of content sets
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<ContentSet> list(@NotNull String programId) throws CloudManagerApiException;

  /**
   * List content sets within the specified program, using the specified limit and starting at 0.
   *
   * @param programId the id of the program context
   * @param limit     the number of flows to return
   * @return list of content sets
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<ContentSet> list(@NotNull String programId, int limit) throws CloudManagerApiException;

  /**
   * List content sets within the specified program, using the specified limit and starting at the specified position.
   *
   * @param programId the id of the program context
   * @param start     the starting position of the results
   * @param limit     the number of flows to return
   * @return list of content sets
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<ContentSet> list(@NotNull String programId, int start, int limit) throws CloudManagerApiException;

  /**
   * Create a new ContentSet in the specified program.
   *
   * @param programId   the id of the program context
   * @param name        name of the content set
   * @param description optional description
   * @param definitions path definitions for the content set
   * @return the created content set
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  ContentSet create(@NotNull String programId, @NotNull String name, String description, @NotNull Collection<PathDefinition> definitions) throws CloudManagerApiException;

  /**
   * Get the content set.
   *
   * @param programId the id of the program context
   * @param id        the content set id
   * @return the content set
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  ContentSet get(@NotNull String programId, @NotNull String id) throws CloudManagerApiException;

  /**
   * Update the content set with the provided details.
   *
   * @param programId   the id of the program context
   * @param id          the content set id
   * @param name        the new name, or {@code null} to leave unchanged
   * @param description the new description, or {@code null} to leave unchanged
   * @param definitions the new definitions, or {@code null} to leave unchanged
   * @return the updated content set
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  ContentSet update(@NotNull String programId, @NotNull String id, String name, String description, Collection<PathDefinition> definitions) throws CloudManagerApiException;

  /**
   * Delete the content set.
   *
   * @param programId the id of the program context
   * @param id        the content set id
   * @throws CloudManagerApiException when any error occurs
   */
  void delete(@NotNull String programId, @NotNull String id) throws CloudManagerApiException;

  /**
   * List the content flows which exist in the specified program.
   *
   * @param programId the id of the program context
   * @return a list of content flows
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<ContentFlow> listFlows(@NotNull String programId) throws CloudManagerApiException;

  /**
   * List the content flows which exist in the specified program, using the specified limit and starting at 0.
   *
   * @param programId the id of the program context
   * @param limit     the number of flows to return
   * @return a list of content flows
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<ContentFlow> listFlows(@NotNull String programId, int limit) throws CloudManagerApiException;

  /**
   * List the content flows which exist in the specified program, using the specified limit and starting at the specified position.
   *
   * @param programId the id of the program context
   * @param start     the starting position of the results
   * @param limit     the number of flows to return
   * @return a list of content flows
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<ContentFlow> listFlows(@NotNull String programId, int start, int limit) throws CloudManagerApiException;

  /**
   * Start a content flow between the specified environments, using the specified content set.
   *
   * @param programId         the id of the program context
   * @param id                the id of the content set to use
   * @param srcEnvironmentId  the id of the source environment
   * @param destEnvironmentId the id of the destination environment
   * @param includeAcl        whether to include the content's ACL definitions
   * @return the content flow
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  ContentFlow startFlow(@NotNull String programId, @NotNull String id, @NotNull String srcEnvironmentId, @NotNull String destEnvironmentId, boolean includeAcl) throws CloudManagerApiException;

  /**
   * Retrieve the content flow.
   *
   * @param programId the id of the program context
   * @param id        the id of the content flow
   * @return the content flow
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  ContentFlow getFlow(@NotNull String programId, @NotNull String id) throws CloudManagerApiException;

  /**
   * Cancel the content flow.
   *
   * @param programId the id of the program context
   * @param id        the id of the content flow
   * @return the content flow
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  ContentFlow cancelFlow(@NotNull String programId, @NotNull String id) throws CloudManagerApiException;
}
