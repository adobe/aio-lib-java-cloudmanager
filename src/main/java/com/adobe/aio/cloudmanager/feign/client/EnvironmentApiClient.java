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
import com.adobe.aio.cloudmanager.impl.model.EnvironmentList;
import com.adobe.aio.cloudmanager.impl.model.EnvironmentLogs;
import com.adobe.aio.cloudmanager.impl.model.Redirect;
import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;

public interface EnvironmentApiClient {

  @RequestLine("GET /api/program/{programId}/environments")
  EnvironmentList list(@Param("programId") String programId, @QueryMap Map<String, Object> type) throws CloudManagerApiException;

  @RequestLine("DELETE /api/program/{programId}/environment/{environmentId}")
  void delete(@Param("programId") String programId, @Param("environmentId") String environmentId) throws CloudManagerApiException;
  
  @RequestLine("GET /api/program/{programId}/environment/{environmentId}/logs")
  EnvironmentLogs listLogs(@Param("programId") String programId, @Param("environmentId") String environmentId, @QueryMap Map<String, Object> params) throws CloudManagerApiException;

  @RequestLine("GET /api/program/{programId}/environment/{environmentId}/logs/download")
  @Headers("Accept: application/json")
  Redirect downloadLog(@Param("programId") String programId, @Param("environmentId") String environmentId,  @QueryMap Map<String, Object> params) throws CloudManagerApiException;

}
