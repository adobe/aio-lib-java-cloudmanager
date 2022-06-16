package com.adobe.aio.cloudmanager.feign.client;

/*-
 * #%L
 * Adobe Cloud Manager Client Library
 * %%
 * Copyright (C) 2020 - 2022 Adobe Inc.
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

import com.adobe.aio.cloudmanager.CloudManagerApiException;
import com.adobe.aio.cloudmanager.Variable;
import com.adobe.aio.cloudmanager.generated.model.VariableList;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface VariableApiClient {
  
  @RequestLine("GET /api/program/{programId}/environment/{environmentId}/variables")
  VariableList listEnvironment(@Param("programId") String programId, @Param("environmentId") String environmentId) throws CloudManagerApiException;
  
  @RequestLine("PATCH /api/program/{programId}/environment/{environmentId}/variables")
  @Headers("Content-Type: application/json")
  VariableList setEnvironment(@Param("programId") String programId, @Param("environmentId") String environmentId, Variable[] variables) throws CloudManagerApiException;

  @RequestLine("GET /api/program/{programId}/pipeline/{pipelineId}/variables")
  VariableList listPipeline(@Param("programId") String programId, @Param("pipelineId") String pipelineId) throws CloudManagerApiException;

  @RequestLine("PATCH /api/program/{programId}/pipeline/{pipelineId}/variables")
  @Headers("Content-Type: application/json")
  VariableList setPipeline(@Param("programId") String programId, @Param("pipelineId") String pipelineId, Variable[] variables) throws CloudManagerApiException;

}
