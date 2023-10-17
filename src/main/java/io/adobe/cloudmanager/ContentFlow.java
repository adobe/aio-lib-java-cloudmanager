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

import java.util.List;

import lombok.Value;

public interface ContentFlow {

  /**
   * The id of this content flow.
   *
   * @return the id
   */
  String getId();

  /**
   * The id of the content set used for this flow.
   *
   * @return the content set id
   */
  String getContentSetId();

  /**
   * The id of the Environment from which content is copied
   *
   * @return the source environment id
   */
  String getSrcEnvironmentId();

  /**
   * The name of the Environment from which content is copied
   *
   * @return the source environment name
   */
  String getSrcEnvironmentName();

  /**
   * The id of the Environment to which content is copied
   *
   * @return the source environment id
   */
  String getDestEnvironmentId();

  /**
   * The name of the Environment to which content is copied
   *
   * @return the source environment name
   */
  String getDestEnvironmentName();

  /**
   * The tier of the source and destination environments
   *
   * @return the environment tier
   */
  Environment.Tier getEnvironmentTier();

  /**
   * The status of the content flow process.
   * <p>
   * See <a href="https://experienceleague.adobe.com/docs/experience-manager-cloud-service/content/implementing/developer-tools/content-copy.html?lang=en#copy-activity">the documentation</a>.
   *
   * @return the status
   */
  String getStatus();

  /**
   * The results of the content flow export process.
   *
   * @return the export results
   */
  Results getExportResults();

  /**
   * The results of the content flow import process
   *
   * @return the import results
   */
  Results getImportResults();

  /**
   * Cancels this content flow
   *
   * @throws CloudManagerApiException when any error occurs
   */
  void cancel() throws CloudManagerApiException;

  @Value
  class Results {
    String errorCode;
    String message;
    List<String> details;
  }
}
