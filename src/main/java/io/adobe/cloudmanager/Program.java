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

public interface Program {

  /**
   * Identifier of the program. Unique within the space.
   *
   * @return id
   **/
  String getId();

  /**
   * Name of the program
   *
   * @return name
   **/
  String getName();

  /**
   * Link to this program.
   *
   * @return the link to this program.
   */
  String getSelfLink();

  /**
   * Delete this program.
   *
   * @throws CloudManagerApiException when an error occurs
   */
  void delete() throws CloudManagerApiException;

}
