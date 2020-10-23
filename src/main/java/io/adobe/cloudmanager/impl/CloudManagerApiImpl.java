package io.adobe.cloudmanager.impl;

/*-
 * #%L
 * Adobe Cloud Manager Client Library
 * %%
 * Copyright (C) 2020 Adobe Inc.
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

import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.CloudManagerApi;
import io.adobe.cloudmanager.PipelineUpdate;
import io.adobe.cloudmanager.model.EmbeddedProgram;
import io.adobe.cloudmanager.model.Pipeline;
import io.adobe.cloudmanager.swagger.invoker.ApiClient;
import io.adobe.cloudmanager.swagger.invoker.ApiException;
import io.adobe.cloudmanager.swagger.invoker.Pair;
import io.adobe.cloudmanager.swagger.model.PipelineExecution;
import io.adobe.cloudmanager.swagger.model.PipelineList;
import io.adobe.cloudmanager.swagger.model.PipelinePhase;
import io.adobe.cloudmanager.swagger.model.Program;
import io.adobe.cloudmanager.swagger.model.ProgramList;

import static io.adobe.cloudmanager.CloudManagerApiException.*;

import javax.ws.rs.core.GenericType;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CloudManagerApiImpl implements CloudManagerApi {

  public CloudManagerApiImpl(String orgId, String apiKey, String accessToken) {
    this(orgId, apiKey, accessToken, null);
  }

  public CloudManagerApiImpl(String orgId, String apiKey, String accessToken, String baseUrl) {
    this.orgId = orgId;
    this.apiKey = apiKey;
    this.accessToken = accessToken;
    if (baseUrl != null) {
      this.baseUrl = baseUrl;
      apiClient.setBasePath(baseUrl);
    } else {
      this.baseUrl = apiClient.getBasePath();
    }
  }

  private final ApiClient apiClient = new ConfiguredApiClient();
  private final String orgId;
  private final String apiKey;
  private final String accessToken;
  private final String baseUrl;

  @Override
  public List<EmbeddedProgram> listPrograms() throws CloudManagerApiException {
    ProgramList programList = null;
    try {
      programList = get("/api/programs", new GenericType<ProgramList>() {});
    } catch (ApiException e) {
      throw new CloudManagerApiException(ErrorType.LIST_PROGRAMS, baseUrl, "/api/programs", e);
    }
    return programList.getEmbedded() == null ?
      Collections.emptyList() :
      programList.getEmbedded().getPrograms().stream().map(p -> new EmbeddedProgram(p, this)).collect(Collectors.toList());
  }

  @Override
  public List<Pipeline> listPipelines(String programId) throws CloudManagerApiException {
    return listPipelines(programId, p -> true);
  }

  @Override
  public List<Pipeline> listPipelines(String programId, Predicate<Pipeline> predicate) throws CloudManagerApiException {
    EmbeddedProgram embeddedProgram = listPrograms().stream().filter(p -> programId.equals(p.getId())).findFirst()
        .orElseThrow(() -> new CloudManagerApiException(ErrorType.FIND_PROGRAM, programId));
    Program program = getProgram(embeddedProgram.getSelfLink());
    String pipelinesHref = program.getLinks().getHttpnsAdobeComadobecloudrelpipelines().getHref();
    try {
      PipelineList pipelineList = get(pipelinesHref, new GenericType<PipelineList>() {});
      return pipelineList.getEmbedded().getPipelines().stream().map(p -> new Pipeline(p, this)).filter(predicate).collect(Collectors.toList());
    } catch (ApiException e) {
      throw new CloudManagerApiException(ErrorType.LIST_PIPELINES, baseUrl, pipelinesHref, e);
    }
  }

  @Override
  public String startExecution(String programId, String pipelineId) throws CloudManagerApiException {
    Pipeline pipeline = getPipeline(programId, pipelineId, ErrorType.FIND_PIPELINE_START);
    return startExecution(pipeline);
  }

  @Override
  public String startExecution(Pipeline pipeline) throws CloudManagerApiException {
    String executionHref = pipeline.getLinks().getHttpnsAdobeComadobecloudrelexecution().getHref();
    Location location = null;
    try {
      location = put(executionHref, new GenericType<Location>() {});
    } catch (ApiException e) {
      if (412 == e.getCode()) {
        throw new CloudManagerApiException(ErrorType.PIPELINE_START_RUNNING);
      }
      throw new CloudManagerApiException(ErrorType.PIPELINE_START, baseUrl, executionHref, e);
    }
    return location.getRewrittenUrl(apiClient.getBasePath());
  }

  @Override
  public Pipeline updatePipeline(String programId, String pipelineId, PipelineUpdate updates) throws CloudManagerApiException {
    Pipeline original = getPipeline(programId, pipelineId);
    return updatePipeline(original, updates);
  }

  @Override
  public Pipeline updatePipeline(Pipeline original, PipelineUpdate updates) throws CloudManagerApiException {
    io.adobe.cloudmanager.swagger.model.Pipeline toUpdate = new io.adobe.cloudmanager.swagger.model.Pipeline();
    PipelinePhase buildPhase = original.getPhases().stream().filter(p -> PipelinePhase.TypeEnum.BUILD == p.getType())
        .findFirst().orElseThrow(() -> new CloudManagerApiException(ErrorType.NO_BUILD_PHASE, original.getId()));
    if (updates.getBranch() != null) {
      buildPhase.setBranch(updates.getBranch());
    }

    if (updates.getRepositoryId() != null) {
      buildPhase.setRepositoryId(updates.getRepositoryId());
    }
    toUpdate.getPhases().add(buildPhase);

    String pipelinePath = original.getLinks().getSelf().getHref();
    try {
      io.adobe.cloudmanager.swagger.model.Pipeline result = patch(pipelinePath, toUpdate, new GenericType<io.adobe.cloudmanager.swagger.model.Pipeline>() {});
      return new Pipeline(result, this);
    } catch (ApiException e) {
      throw new CloudManagerApiException(ErrorType.UPDATE_PIPELINE, baseUrl, pipelinePath, e);
    }
  }

  @Override
  public PipelineExecution getCurrentExecution(String programId, String pipelineId) throws CloudManagerApiException {
    Pipeline pipeline = getPipeline(programId, pipelineId);
    String executionHref = pipeline.getLinks().getHttpnsAdobeComadobecloudrelexecution().getHref();
    try {
      return get(executionHref, new GenericType<PipelineExecution>() {});
    } catch (ApiException e) {
      throw new CloudManagerApiException(ErrorType.GET_EXECUTION, baseUrl, executionHref, e);
    }
  }

  private Program getProgram(String path) throws CloudManagerApiException {
    try {
      return get(path, new GenericType<Program>() {
      });
    } catch (ApiException e) {
      throw new CloudManagerApiException(ErrorType.GET_PROGRAM, baseUrl, path, e);
    }
  }

  @Override
  public void deletePipeline(Pipeline pipeline) throws CloudManagerApiException {
    String pipelinePath = pipeline.getLinks().getSelf().getHref();
    try {
      delete(pipelinePath);
    } catch (ApiException e) {
      throw new CloudManagerApiException(CloudManagerApiException.ErrorType.DELETE_PIPELINE, baseUrl, pipelinePath, e);
    }
  }

  @Override
  public void deletePipeline(String programId, String pipelineId) throws CloudManagerApiException {
    Pipeline original = getPipeline(programId, pipelineId);
    deletePipeline(original);
  }

  private Pipeline getPipeline(String programId, String pipelineId, CloudManagerApiException.ErrorType errorType) throws CloudManagerApiException {
    return listPipelines(programId).stream().filter(p -> pipelineId.equals(p.getId())).findFirst()
        .orElseThrow(() -> new CloudManagerApiException(errorType, pipelineId, programId));
  }

  private Pipeline getPipeline(String programId, String pipelineId) throws CloudManagerApiException {
    return getPipeline(programId, pipelineId, ErrorType.FIND_PIPELINE);
  }

  private <T> T get(String path, GenericType<T> returnType) throws ApiException {
    return doRequest(path, "GET", Collections.emptyList(), null, returnType);
  }

  private <T> T put(String path, GenericType<T> returnType) throws ApiException {
    return doRequest(path, "PUT", Collections.emptyList(), "", returnType);
  }

  private <T> T patch(String path, Object body, GenericType<T> returnType) throws ApiException {
    return doRequest(path, "PATCH", Collections.emptyList(), body, returnType);
  }

  private <T> T delete(String path) throws ApiException {
    return doRequest(path, "DELETE", Collections.emptyList(), null, null);
  }

  private <T> T doRequest(String path, String method, List<Pair> queryParams, Object body, GenericType<T> returnType) throws ApiException {
    Map<String, String> headers = new HashMap<>();
    headers.put("x-gw-ims-org-id", orgId);
    headers.put("Authorization", String.format("Bearer %s", accessToken));
    headers.put("x-api-key", apiKey);

    return apiClient.invokeAPI(path, method, queryParams, body, headers, Collections.emptyMap(), "application/json", "application/json", new String[0], returnType);
  }
}
