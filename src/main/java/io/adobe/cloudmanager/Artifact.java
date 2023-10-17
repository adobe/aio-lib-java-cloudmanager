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

public interface Artifact {

  /**
   * Returns the id for this artifact;
   *
   * @return the id
   */
  String getId();

  /**
   * Returns the file name for this artifacts
   *
   * @return the file name
   */
  String getFileName();

  /**
   * Returns the type of this artifact
   *
   * @return the type
   */
  String getType();

  /**
   * Returns the md5 hash for the artifact.
   *
   * @return the md5
   */
  String getMd5();

  /**
   * Returns the fully qualified download url for this artifact
   *
   * @return the download url
   */
  String getDownloadUrl() throws CloudManagerApiException;
}
