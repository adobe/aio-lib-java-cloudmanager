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

/**
 * An Artifact for a build step.
 */
public interface Artifact {

  /**
   * The unique identifier for this artifact.
   *
   * @return the id
   */
  String getId();

  /**
   * The original file name for this artifact, as referenced in the Adobe systems.
   *
   * @return the file name
   */
  String getFileName();

  /**
   * The type of this artifact. See the <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#tag/Execution-Artifacts/operation/listStepArtifacts">List Step API</a> for possible values.
   *
   * @return the type
   */
  String getType();

  /**
   * The md5 hash for the artifact.
   *
   * @return the md5
   */
  String getMd5();

  /**
   * The fully qualified download url for this artifact.
   *
   * @return the download url
   * @throws CloudManagerApiException when any error occurs
   */
  String getDownloadUrl() throws CloudManagerApiException;
}
