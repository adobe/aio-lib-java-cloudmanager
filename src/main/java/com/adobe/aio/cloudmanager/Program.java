package com.adobe.aio.cloudmanager;

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

/**
 * A Program definition.
 */
public interface Program {

  /**
   * Identifier of the program. Unique within the space.
   *
   * @return id
   **/
  String getId();

  /**
   * Name of the program.
   *
   * @return name
   **/
  String getName();

  /**
   * Delete this program.
   *
   * @throws CloudManagerApiException when an error occurs
   */
  void delete() throws CloudManagerApiException;

  /**
   * List all regions which can be used to create environments for this program.
   *
   * @return the list of regions
   * @throws CloudManagerApiException when any error occurs
   */
  Collection<Region> listRegions() throws CloudManagerApiException;
}
