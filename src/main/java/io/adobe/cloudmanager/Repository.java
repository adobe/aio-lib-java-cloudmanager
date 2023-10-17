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

public interface Repository {

  /**
   * Identifier of the repository. Unique within the space.
   *
   * @return id
   **/
  String getId();

  /**
   * The id of the associated program context
   *
   * @return the program id
   */
  String getProgramId();

  /**
   * Name of the repository
   *
   * @return name
   */
  String getName();

  /**
   * Description of this repository.
   *
   * @return description
   */
  String getDescription();

  /**
   * URL to the repository.
   *
   * @return url
   */
  String getUrl();

  /**
   * Lists all the branches associated with the repository.
   *
   * @return branches
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getBranches">List Branches API</a>
   */
  Collection<String> listBranches() throws CloudManagerApiException;
}
