package com.adobe.aio.cloudmanager;

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

/**
 * Possible values of the Step State's Action proprerty.
 *
 * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/docs.html#!AdobeDocs/cloudmanager-api-docs/master/receiving-events.md">Adobe IO Events</a>
 */
public enum StepAction {
  validate,
  build,
  codeQuality,
  buildImage,
  securityTest,
  loadTest,
  assetsTest,
  reportPerformanceTest,
  productTest,
  functionalTest,
  uiTest,
  contentAudit,
  approval,
  schedule,
  managed,
  deploy
}
