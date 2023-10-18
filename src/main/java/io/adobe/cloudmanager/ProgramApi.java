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

public interface ProgramApi {

  /**
   * Returns the program with the specified id
   *
   * @param programId the id of the program
   * @return the program
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Program get(@NotNull String programId) throws CloudManagerApiException;

  /**
   * Delete the specified program.
   *
   * @param programId the id of the program to delete.
   * @throws CloudManagerApiException when any error occurs
   */
  void delete(@NotNull String programId) throws CloudManagerApiException;

  /**
   * Delete the specified program.
   *
   * @param program the program to delete
   * @throws CloudManagerApiException when any error occurs
   */
  void delete(@NotNull Program program) throws CloudManagerApiException;

  // TODO: Add NewRelic API?

  /**
   * List all programs for a Tenant
   *
   * @param tenantId the id tenant
   * @return a list of {@link Program}s
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<Program> list(@NotNull String tenantId) throws CloudManagerApiException;

  /**
   * List all programs for the Tenant
   *
   * @param tenant the tenant
   * @return a list of {@link Program}s
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<Program> list(@NotNull Tenant tenant) throws CloudManagerApiException;

  /**
   * List all regions which can be used to create environments for the specified program.
   *
   * @param programId the id of the program
   * @return the list of regions
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<Region> listRegions(@NotNull String programId) throws CloudManagerApiException;

  // TODO: Add Creation Operation?
}
