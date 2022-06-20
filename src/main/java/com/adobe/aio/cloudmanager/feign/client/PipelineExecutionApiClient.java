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

import java.util.Map;

import com.adobe.aio.cloudmanager.CloudManagerApiException;
import com.adobe.aio.cloudmanager.impl.model.PipelineExecution;
import com.adobe.aio.cloudmanager.impl.model.PipelineExecutionStepState;
import com.adobe.aio.cloudmanager.impl.model.PipelineStepMetrics;
import com.adobe.aio.cloudmanager.impl.model.Redirect;
import feign.Body;
import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;

public interface PipelineExecutionApiClient {
  
  @RequestLine("GET /api/program/{programId}/pipeline/{pipelineId}/execution")
  PipelineExecution current(@Param("programId") String programId, @Param("pipelineId") String pipelineId) throws CloudManagerApiException;
  
  @RequestLine("PUT /api/program/{programId}/pipeline/{pipelineId}/execution")
  PipelineExecution start(@Param("programId") String programId, @Param("pipelineId") String pipelineId) throws CloudManagerApiException;

  @RequestLine("GET /api/program/{programId}/pipeline/{pipelineId}/execution/{executionId}")
  PipelineExecution get(@Param("programId") String programId, @Param("pipelineId") String pipelineId, @Param("executionId") String executionId) throws CloudManagerApiException;
  
  @RequestLine("GET {uriPath}")
  PipelineExecution get(@Param("uriPath") String uriPath) throws CloudManagerApiException;
  
  @RequestLine("GET {uriPath}")
  PipelineExecutionStepState getStepState(@Param("uriPath") String uriPath) throws CloudManagerApiException;

  @RequestLine("PUT {uriPath}")
  @Headers("Content-Type: application/json")
  @Body("{body}")
  void advance(@Param("uriPath") String uriPath, @Param("body") String body) throws CloudManagerApiException;

  @RequestLine("PUT {uriPath}")
  @Headers("Content-Type: application/json")
  @Body("{body}")
  void cancel(@Param("uriPath") String uriPath, @Param("body") String body) throws CloudManagerApiException;

  @RequestLine("GET {uriPath}")
  @Headers("Accept: application/json")
  Redirect getLogs(@Param("uriPath") String uriPath, @QueryMap Map<String, Object> params) throws CloudManagerApiException;
  
  @RequestLine("GET {uriPath}")
  PipelineStepMetrics getStepMetrics(@Param("uriPath") String uriPath) throws CloudManagerApiException;
}
