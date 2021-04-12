package io.adobe.cloudmanager.impl;

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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;

import io.adobe.cloudmanager.CloudManagerApi;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.Pipeline;
import io.adobe.cloudmanager.PipelineUpdate;
import io.adobe.cloudmanager.Program;
import io.adobe.cloudmanager.generated.invoker.ApiClient;
import io.adobe.cloudmanager.generated.invoker.ApiException;
import io.adobe.cloudmanager.generated.invoker.Pair;
import io.adobe.cloudmanager.generated.model.EnvironmentList;
import io.adobe.cloudmanager.generated.model.EnvironmentLogs;
import io.adobe.cloudmanager.generated.model.HalLink;
import io.adobe.cloudmanager.generated.model.LogOptionRepresentation;
import io.adobe.cloudmanager.generated.model.PipelineExecutionEmbedded;
import io.adobe.cloudmanager.generated.model.PipelineList;
import io.adobe.cloudmanager.generated.model.PipelinePhase;
import io.adobe.cloudmanager.generated.model.PipelineStepMetrics;
import io.adobe.cloudmanager.generated.model.ProgramList;
import io.adobe.cloudmanager.generated.model.Redirect;
import io.adobe.cloudmanager.generated.model.VariableList;
import io.adobe.cloudmanager.model.PipelineImpl;
import io.adobe.cloudmanager.model.ProgramImpl;
import io.adobe.cloudmanager.model.Environment;
import io.adobe.cloudmanager.model.EnvironmentLog;
import io.adobe.cloudmanager.model.PipelineExecution;
import io.adobe.cloudmanager.model.PipelineExecutionStepState;
import io.adobe.cloudmanager.model.Variable;
import io.adobe.cloudmanager.util.Predicates;
import static io.adobe.cloudmanager.CloudManagerApiException.*;

public class CloudManagerApiImpl implements CloudManagerApi {

  private final ApiClient apiClient = new ConfiguredApiClient();
  private final String orgId;
  private final String apiKey;
  private final String accessToken;
  private final String baseUrl;

  public CloudManagerApiImpl(String orgId, String apiKey, String accessToken) {
    this(orgId, apiKey, accessToken, null);
  }

  public CloudManagerApiImpl(String orgId, String apiKey, String accessToken, String baseUrl) {
    this.orgId = orgId;
    this.apiKey = apiKey;
    this.accessToken = accessToken;
    if (baseUrl == null) {
      baseUrl = apiClient.getBasePath();
    }

    baseUrl = StringUtils.removeEnd(baseUrl, "/");
    apiClient.setBasePath(baseUrl);
    this.baseUrl = baseUrl;
  }

  @Override
  public List<Program> listPrograms() throws CloudManagerApiException {
    ProgramList programList;
    try {
      programList = get("/api/programs", new GenericType<ProgramList>() {});
    } catch (ApiException e) {
      throw new CloudManagerApiException(ErrorType.LIST_PROGRAMS, baseUrl, "/api/programs", e);
    }
    return programList.getEmbedded() == null ?
        Collections.emptyList() :
        programList.getEmbedded().getPrograms().stream().map(p -> new ProgramImpl(p, this)).collect(Collectors.toList());
  }

  @Override
  public void deleteProgram(String programId) throws CloudManagerApiException {
    List<Program> programs = listPrograms();
    Program program = programs.stream().filter(p -> programId.equals(p.getId())).findFirst().
        orElseThrow(() -> new CloudManagerApiException(ErrorType.FIND_PROGRAM, programId));
    deleteProgram(program);
  }

  @Override
  public void deleteProgram(Program p) throws CloudManagerApiException {
    io.adobe.cloudmanager.generated.model.Program program = getProgramDetail(p.getId());
    String href = program.getLinks().getSelf().getHref();
    try {
      delete(href);
    } catch (ApiException e) {
      throw new CloudManagerApiException(ErrorType.DELETE_PROGRAM, baseUrl, href, e);
    }
  }

  @Override
  public List<Pipeline> listPipelines(String programId) throws CloudManagerApiException {
    return listPipelines(programId, p -> true);
  }

  @Override
  public List<Pipeline> listPipelines(String programId, Predicate<Pipeline> predicate) throws CloudManagerApiException {
    return new ArrayList<>(listPipelineDetails(programId, predicate));
  }

  @Override
  public void deletePipeline(String programId, String pipelineId) throws CloudManagerApiException {
    Pipeline original = getPipeline(programId, pipelineId);
    deletePipeline(original);
  }

  @Override
  public void deletePipeline(Pipeline pipeline) throws CloudManagerApiException {
    try {
      delete(pipeline.getSelfLink());
    } catch (ApiException e) {
      throw new CloudManagerApiException(CloudManagerApiException.ErrorType.DELETE_PIPELINE, baseUrl, pipeline.getSelfLink(), e);
    }
  }

  @Override
  public Pipeline updatePipeline(String programId, String pipelineId, PipelineUpdate updates) throws CloudManagerApiException {
    Pipeline original = getPipeline(programId, pipelineId);
    return updatePipeline(original, updates);
  }

  @Override
  public Pipeline updatePipeline(Pipeline original, PipelineUpdate updates) throws CloudManagerApiException {
    PipelineImpl pipeline = getPipeline(original);
    io.adobe.cloudmanager.generated.model.Pipeline toUpdate = new io.adobe.cloudmanager.generated.model.Pipeline();
    PipelinePhase buildPhase = pipeline.getPhases().stream().filter(p -> PipelinePhase.TypeEnum.BUILD == p.getType())
        .findFirst().orElseThrow(() -> new CloudManagerApiException(ErrorType.NO_BUILD_PHASE, original.getId()));
    if (updates.getBranch() != null) {
      buildPhase.setBranch(updates.getBranch());
    }

    if (updates.getRepositoryId() != null) {
      buildPhase.setRepositoryId(updates.getRepositoryId());
    }
    toUpdate.getPhases().add(buildPhase);

    String pipelinePath = pipeline.getSelfLink();
    try {
      io.adobe.cloudmanager.generated.model.Pipeline result = patch(pipelinePath, toUpdate, new GenericType<io.adobe.cloudmanager.generated.model.Pipeline>() {});
      return new PipelineImpl(result, this);
    } catch (ApiException e) {
      throw new CloudManagerApiException(ErrorType.UPDATE_PIPELINE, baseUrl, pipelinePath, e);
    }
  }

  @Override
  public Optional<PipelineExecution> getCurrentExecution(String programId, String pipelineId) throws CloudManagerApiException {
    PipelineImpl pipeline = getPipeline(programId, pipelineId);
    String executionHref = pipeline.getLinks().getHttpnsAdobeComadobecloudrelexecution().getHref();
    try {
      io.adobe.cloudmanager.generated.model.PipelineExecution execution = get(executionHref, new GenericType<io.adobe.cloudmanager.generated.model.PipelineExecution>() {});
      return Optional.of(new PipelineExecution(execution, this));
    } catch (ApiException e) {
      if (Response.Status.NOT_FOUND.getStatusCode() == e.getCode()) {
        return Optional.empty();
      }
      throw new CloudManagerApiException(ErrorType.GET_EXECUTION, baseUrl, executionHref, e);
    }
  }

  @Override
  public PipelineExecution startExecution(String programId, String pipelineId) throws CloudManagerApiException {
    PipelineImpl pipeline = getPipeline(programId, pipelineId, ErrorType.FIND_PIPELINE_START);
    return startExecution(pipeline);
  }

  @Override
  public PipelineExecution startExecution(Pipeline p) throws CloudManagerApiException {
    PipelineImpl pipeline = getPipeline(p);
    String executionHref = pipeline.getLinks().getHttpnsAdobeComadobecloudrelexecution().getHref();
    io.adobe.cloudmanager.generated.model.PipelineExecution execution = null;
    try {
      execution = put(executionHref, new GenericType<io.adobe.cloudmanager.generated.model.PipelineExecution>() {});
    } catch (ApiException e) {
      if (412 == e.getCode()) {
        throw new CloudManagerApiException(ErrorType.PIPELINE_START_RUNNING);
      }
      throw new CloudManagerApiException(ErrorType.PIPELINE_START, baseUrl, executionHref, e);
    }
    return new PipelineExecution(execution, this);
  }

  @Override
  public PipelineExecution getExecution(String programId, String pipelineId, String executionId) throws CloudManagerApiException {
    PipelineImpl pipeline = getPipeline(programId, pipelineId);
    return getExecution(pipeline, executionId);
  }

  @Override
  public PipelineExecution getExecution(Pipeline p, String executionId) throws CloudManagerApiException {
    PipelineImpl pipeline = getPipeline(p);
    Map<String, String> values = new HashMap<>();
    values.put("executionId", executionId);
    String executionHref = processTemplate(pipeline.getLinks().getHttpnsAdobeComadobecloudrelexecutionid().getHref(), values);
    try {
      io.adobe.cloudmanager.generated.model.PipelineExecution execution = get(executionHref, new GenericType<io.adobe.cloudmanager.generated.model.PipelineExecution>() {});
      return new PipelineExecution(execution, this);
    } catch (ApiException e) {
      throw new CloudManagerApiException(ErrorType.GET_EXECUTION, baseUrl, executionHref, e);
    }
  }

  @Override
  public PipelineExecutionStepState getExecutionStepState(PipelineExecution execution, String action) throws CloudManagerApiException {
    io.adobe.cloudmanager.generated.model.PipelineExecutionStepState stepState = execution.getEmbedded().getStepStates()
        .stream()
        .filter(s -> s.getAction().equals(action))
        .findFirst()
        .orElseThrow(() -> new CloudManagerApiException(ErrorType.FIND_STEP_STATE, action, execution.getId()));
    return new PipelineExecutionStepState(stepState, this);
  }

  @Override
  public PipelineExecutionStepState getCurrentStep(PipelineExecution execution) throws CloudManagerApiException {
    PipelineExecutionEmbedded embeddeds = execution.getEmbedded();
    if (embeddeds == null || embeddeds.getStepStates().isEmpty()) {
      throw new CloudManagerApiException(ErrorType.FIND_CURRENT_STEP, execution.getPipelineId());
    }
    io.adobe.cloudmanager.generated.model.PipelineExecutionStepState step = embeddeds.getStepStates().stream().filter(Predicates.IS_CURRENT)
        .findFirst().orElseThrow(() -> new CloudManagerApiException(ErrorType.FIND_CURRENT_STEP, execution.getPipelineId()));
    return new PipelineExecutionStepState(step, this);
  }

  @Override
  public PipelineExecutionStepState getWaitingStep(PipelineExecution execution) throws CloudManagerApiException {
    PipelineExecutionEmbedded embeddeds = execution.getEmbedded();
    if (embeddeds == null || embeddeds.getStepStates().isEmpty()) {
      throw new CloudManagerApiException(ErrorType.FIND_WAITING_STEP, execution.getPipelineId());
    }
    io.adobe.cloudmanager.generated.model.PipelineExecutionStepState step = embeddeds.getStepStates().stream().filter(Predicates.IS_WAITING)
        .findFirst().orElseThrow(() -> new CloudManagerApiException(ErrorType.FIND_WAITING_STEP, execution.getPipelineId()));
    return new PipelineExecutionStepState(step, this);
  }

  @Override
  public void advanceExecution(String programId, String pipelineId, String executionId) throws CloudManagerApiException {
    advanceExecution(getExecution(programId, pipelineId, executionId));
  }

  @Override
  public void advanceExecution(PipelineExecution execution) throws CloudManagerApiException {
    String href = execution.getAdvanceLink();
    try {
      put(href, execution.getAdvanceBody());
    } catch (ApiException e) {
      throw new CloudManagerApiException(ErrorType.ADVANCE_EXECUTION, baseUrl, href, e);
    }
  }

  @Override
  public void advanceCurrentExecution(String programId, String pipelineId) throws CloudManagerApiException {
    Optional<PipelineExecution> execution = getCurrentExecution(programId, pipelineId);
    if (execution.isPresent()) {
      advanceExecution(execution.get());
    }
  }

  @Override
  public void cancelExecution(String programId, String pipelineId, String executionId) throws CloudManagerApiException {
    cancelExecution(getExecution(programId, pipelineId, executionId));
  }

  @Override
  public void cancelExecution(PipelineExecution execution) throws CloudManagerApiException {
    String href = execution.getCancelLink();
    try {
      put(href, execution.getCancelBody());
    } catch (ApiException e) {
      throw new CloudManagerApiException(ErrorType.CANCEL_EXECUTION, baseUrl, href, e);
    }
  }

  @Override
  public void cancelCurrentExecution(String programId, String pipelineId) throws CloudManagerApiException {
    Optional<PipelineExecution> execution = getCurrentExecution(programId, pipelineId);
    if (execution.isPresent()) {
      cancelExecution(execution.get());
    }
  }

  @Override
  public void getExecutionStepLog(String programId, String pipelineId, String executionId, String action, OutputStream outputStream) throws CloudManagerApiException {
    getExecutionStepLog(programId, pipelineId, executionId, action, null, outputStream);
  }

  @Override
  public void getExecutionStepLog(String programId, String pipelineId, String executionId, String action, String name, OutputStream outputStream) throws CloudManagerApiException {
    PipelineExecution execution = getExecution(programId, pipelineId, executionId);
    PipelineExecutionStepState stepState = getExecutionStepState(execution, action);
    getExecutionStepLog(stepState, name, outputStream);
  }

  @Override
  public void getExecutionStepLog(PipelineExecutionStepState action, OutputStream outputStream) throws CloudManagerApiException {
    HalLink link = action.getLinks().getHttpnsAdobeComadobecloudrelpipelinelogs();
    if (link == null) {
      throw new CloudManagerApiException(ErrorType.FIND_LOGS_LINK_EXECUTION, action.getAction());
    }
    String url = getLogRedirect(link, null);
    streamLog(outputStream, url);
  }

  @Override
  public void getExecutionStepLog(PipelineExecutionStepState action, String name, OutputStream outputStream) throws CloudManagerApiException {
    HalLink link = action.getLinks().getHttpnsAdobeComadobecloudrelpipelinelogs();
    if (link == null) {
      throw new CloudManagerApiException(ErrorType.FIND_LOGS_LINK_EXECUTION, action.getAction());
    }
    String url = getLogRedirect(link, name);
    streamLog(outputStream, url);
  }

  @Override
  public PipelineStepMetrics getQualityGateResults(PipelineExecutionStepState step) throws CloudManagerApiException {
    String href = step.getLinks().getHttpnsAdobeComadobecloudrelpipelinemetrics().getHref();
    try {
      return get(href, new GenericType<PipelineStepMetrics>() {});
    } catch (ApiException e) {
      throw new CloudManagerApiException(ErrorType.GET_METRICS, baseUrl, href, e);
    }
  }

  @Override
  public List<Environment> listEnvironments(String programId) throws CloudManagerApiException {
    io.adobe.cloudmanager.generated.model.Program program = getProgramDetail(programId);
    String environmentsHref = program.getLinks().getHttpnsAdobeComadobecloudrelenvironments().getHref();
    EnvironmentList environmentList;
    try {
      environmentList = get(environmentsHref, new GenericType<EnvironmentList>() {});
    } catch (ApiException e) {
      throw new CloudManagerApiException(ErrorType.RETRIEVE_ENVIRONMENTS, baseUrl, environmentsHref, e);
    }
    if (environmentList == null ||
        environmentList.getEmbedded() == null ||
        environmentList.getEmbedded().getEnvironments() == null) {
      throw new CloudManagerApiException(ErrorType.FIND_ENVIRONMENTS, programId);
    }
    return environmentList.getEmbedded().getEnvironments().stream().map(e -> new Environment(e, this)).collect(Collectors.toList());
  }

  @Override
  public void deleteEnvironment(Environment environment) throws CloudManagerApiException {
    String environmentPath = environment.getLinks().getSelf().getHref();
    try {
      delete(environmentPath);
    } catch (ApiException e) {
      throw new CloudManagerApiException(ErrorType.DELETE_ENVIRONMENT, baseUrl, environmentPath, e);
    }
  }

  @Override
  public void deleteEnvironment(String programId, String environmentId) throws CloudManagerApiException {
    deleteEnvironment(getEnvironment(programId, environmentId));
  }

  @Override
  public List<EnvironmentLog> downloadLogs(String programId, String environmentId, LogOptionRepresentation logOptions, int days, File dir) throws CloudManagerApiException {
    return downloadLogs(getEnvironment(programId, environmentId), logOptions, days, dir);
  }

  @Override
  public List<EnvironmentLog> downloadLogs(Environment environment, LogOptionRepresentation logOptions, int days, File dir) throws CloudManagerApiException {
    HalLink logLink = environment.getLinks().getHttpnsAdobeComadobecloudrellogs();
    if (logLink == null) {
      throw new CloudManagerApiException(ErrorType.FIND_LOGS_LINK_ENVIRONMENT, environment.getId(), environment.getProgramId());
    }

    Map<String, String> values = new HashMap<>();
    values.put("service", logOptions.getService());
    values.put("name", logOptions.getName());
    values.put("days", Integer.toString(days));
    String logHref = processTemplate(logLink.getHref(), values);
    EnvironmentLogs logs = getLogs(logHref);

    List<io.adobe.cloudmanager.generated.model.EnvironmentLog> downloads = logs.getEmbedded().getDownloads();
    List<EnvironmentLog> downloaded = new ArrayList<>();

    if (downloads == null || downloads.isEmpty()) {
      return Collections.emptyList();
    } else {
      for (io.adobe.cloudmanager.generated.model.EnvironmentLog d : downloads) {
        EnvironmentLog log = new EnvironmentLog(d);
        String logfileName = String.format("%d-%s-%s-%s.log.gz", log.getEnvironmentId(), log.getService(), log.getName(), log.getDate());
        log.setPath(dir.getPath() + "/" + logfileName);
        log.setUrl(log.getLinks().getHttpnsAdobeComadobecloudrellogsdownload().getHref());
        downloadLog(log);
        downloaded.add(log);
      }
    }
    return downloaded;
  }

  @Override
  public List<Variable> listEnvironmentVariables(String programId, String environmentId) throws CloudManagerApiException {
    return listEnvironmentVariables(getEnvironment(programId, environmentId));
  }

  @Override
  public List<Variable> listEnvironmentVariables(Environment environment) throws CloudManagerApiException {
    HalLink variableLink = environment.getLinks().getHttpnsAdobeComadobecloudrelvariables();
    if (variableLink == null) {
      throw new CloudManagerApiException(ErrorType.FIND_VARIABLES_LINK_ENVIRONMENT, environment.getId(), environment.getProgramId());
    }
    return listVariables(variableLink);
  }

  @Override
  public List<Variable> setEnvironmentVariables(String programId, String environmentId, Variable... variables) throws CloudManagerApiException {
    return setEnvironmentVariables(getEnvironment(programId, environmentId), variables);
  }

  @Override
  public List<Variable> setEnvironmentVariables(Environment environment, Variable... variables) throws CloudManagerApiException {
    HalLink variableLInk = environment.getLinks().getHttpnsAdobeComadobecloudrelvariables();
    if (variableLInk == null) {
      throw new CloudManagerApiException(ErrorType.FIND_VARIABLES_LINK_ENVIRONMENT, environment.getId(), environment.getProgramId());
    }
    return setVariables(variableLInk, variables);
  }

  @Override
  public List<Variable> listPipelineVariables(String programId, String pipelineId) throws CloudManagerApiException {
    return listPipelineVariables(getPipeline(programId, pipelineId));
  }

  @Override
  public List<Variable> listPipelineVariables(Pipeline p) throws CloudManagerApiException {
    PipelineImpl pipeline = getPipeline(p);
    HalLink variableLink = pipeline.getLinks().getHttpnsAdobeComadobecloudrelvariables();
    if (variableLink == null) {
      throw new CloudManagerApiException(ErrorType.FIND_VARIABLES_LINK_PIPELINE, pipeline.getId(), pipeline.getProgramId());
    }
    return listVariables(variableLink);
  }

  @Override
  public List<Variable> setPipelineVariables(String programId, String pipelineId, Variable... variables) throws CloudManagerApiException {
    return setPipelineVariables(getPipeline(programId, pipelineId), variables);
  }

  @Override
  public List<Variable> setPipelineVariables(Pipeline p, Variable... variables) throws CloudManagerApiException {
    PipelineImpl pipeline = getPipeline(p);
    HalLink variableLink = pipeline.getLinks().getHttpnsAdobeComadobecloudrelvariables();
    if (variableLink == null) {
      throw new CloudManagerApiException(ErrorType.FIND_VARIABLES_LINK_PIPELINE, pipeline.getId(), pipeline.getProgramId());
    }
    return setVariables(variableLink, variables);
  }

  private String getLogRedirect(HalLink link, String name) throws CloudManagerApiException {
    List<Pair> params = new ArrayList<>();
    if (StringUtils.isNotBlank(name)) {
      params.add(new Pair("file", name));
    }
    String url;
    try {
      url = get(link.getHref(), params, new GenericType<Redirect>() {}).getRedirect();
      if (StringUtils.isBlank(url)) {
        throw new CloudManagerApiException(ErrorType.NO_LOG_REDIRECT, String.format("%s%s", baseUrl, link.getHref()), url);
      }
    } catch (ApiException e) {
      throw new CloudManagerApiException(ErrorType.GET_LOGS, baseUrl, link.getHref(), e);
    }
    return url;
  }

  private void streamLog(OutputStream outputStream, String url) throws CloudManagerApiException {
    try (InputStream is = new URL(url).openStream()) {
      IOUtils.copy(is, outputStream);
    } catch (IOException e) {
      throw new CloudManagerApiException(ErrorType.GET_LOGS, e.getMessage());
    } finally {
      try {
        IOUtils.close(outputStream);
      } catch (IOException e) {
        // Do nothing on failed to close
      }
    }
  }


  // Private Implementation detail methods.

  private io.adobe.cloudmanager.generated.model.Program getProgramDetail(String programId) throws CloudManagerApiException {
    Program program = listPrograms().stream().filter(p -> programId.equals(p.getId())).findFirst()
        .orElseThrow(() -> new CloudManagerApiException(ErrorType.FIND_PROGRAM, programId));
    try {
      return get(program.getSelfLink(), new GenericType<io.adobe.cloudmanager.generated.model.Program>() {});
    } catch (ApiException e) {
      throw new CloudManagerApiException(ErrorType.GET_PROGRAM, baseUrl, program.getSelfLink(), e);
    }
  }

  private List<PipelineImpl> listPipelineDetails(String programId, Predicate<Pipeline> predicate) throws CloudManagerApiException {
    io.adobe.cloudmanager.generated.model.Program program = getProgramDetail(programId);
    String pipelinesHref = program.getLinks().getHttpnsAdobeComadobecloudrelpipelines().getHref();
    PipelineList pipelineList;
    try {
      pipelineList = get(pipelinesHref, new GenericType<PipelineList>() {});
    } catch (ApiException e) {
      throw new CloudManagerApiException(ErrorType.LIST_PIPELINES, baseUrl, pipelinesHref, e);
    }

    if (pipelineList == null ||
        pipelineList.getEmbedded() == null ||
        pipelineList.getEmbedded().getPipelines() == null) {
      throw new CloudManagerApiException(ErrorType.FIND_PIPELINES, programId);
    }
    return pipelineList.getEmbedded().getPipelines().stream().map(p -> new PipelineImpl(p, this)).filter(predicate).collect(Collectors.toList());
  }

  private PipelineImpl getPipeline(Pipeline p) throws CloudManagerApiException {
    return getPipeline(p.getProgramId(), p.getId());
  }

  private PipelineImpl getPipeline(String programId, String pipelineId) throws CloudManagerApiException {
    return getPipeline(programId, pipelineId, ErrorType.FIND_PIPELINE);
  }

  private PipelineImpl getPipeline(String programId, String pipelineId, CloudManagerApiException.ErrorType errorType) throws CloudManagerApiException {
    return listPipelineDetails(programId, new Pipeline.IdPredicate(pipelineId)).stream().findFirst()
        .orElseThrow(() -> new CloudManagerApiException(errorType, pipelineId, programId));
  }

  private Environment getEnvironment(String programId, String environmentId) throws CloudManagerApiException {
    List<Environment> environments = listEnvironments(programId);
    return environments.stream().filter(e -> environmentId.equals(e.getId())).findFirst().
        orElseThrow(() -> new CloudManagerApiException(ErrorType.FIND_ENVIRONMENT, environmentId, programId));
  }

  private List<Variable> listVariables(HalLink variableLink) throws CloudManagerApiException {
    VariableList list;
    try {
      list = get(variableLink.getHref(), new GenericType<VariableList>() {});
    } catch (ApiException e) {
      throw new CloudManagerApiException(ErrorType.GET_VARIABLES, baseUrl, variableLink.getHref(), e);
    }
    if (list.getTotalNumberOfItems().equals(0)) {
      return Collections.emptyList();
    }
    return list.getEmbedded().getVariables().stream().map(Variable::new).collect(Collectors.toList());
  }

  private List<Variable> setVariables(HalLink variableLInk, Variable[] variables) throws CloudManagerApiException {
    VariableList list;
    try {
      list = patch(variableLInk.getHref(), variables, new GenericType<VariableList>() {});
    } catch (ApiException e) {
      throw new CloudManagerApiException(ErrorType.SET_VARIABLES, baseUrl, variableLInk.getHref(), e);
    }
    if (list.getTotalNumberOfItems().equals(0)) {
      return Collections.emptyList();
    }
    return list.getEmbedded().getVariables().stream().map(Variable::new).collect(Collectors.toList());
  }

  private EnvironmentLogs getLogs(String path) throws CloudManagerApiException {
    try {
      return get(path, new GenericType<EnvironmentLogs>() {});
    } catch (ApiException e) {
      throw new CloudManagerApiException(ErrorType.GET_LOGS, baseUrl, path, e);
    }
  }

  private void downloadLog(EnvironmentLog log) throws CloudManagerApiException {
    Redirect redirect;
    try {
      redirect = get(log.getUrl(), new GenericType<Redirect>() {});
      File downloadedFile = new File(log.getPath());
      FileUtils.copyInputStreamToFile(new URL(redirect.getRedirect()).openStream(), downloadedFile);
    } catch (ApiException | MalformedURLException e) {
      throw new CloudManagerApiException(ErrorType.NO_LOG_REDIRECT, log.getUrl(), e.getMessage());
    } catch (IOException e) {
      throw new CloudManagerApiException(ErrorType.LOG_DOWNLOAD, e.getMessage(), log.getPath(), e.getMessage());
    }
  }

  private <T> T get(String path, GenericType<T> returnType) throws ApiException {
    return doRequest(path, "GET", Collections.emptyList(), null, returnType);
  }

  private <T> T get(String path, List<Pair> queryParams, GenericType<T> returnType) throws ApiException {
    return doRequest(path, "GET", queryParams, null, returnType);
  }

  private <T> T put(String path, Object body) throws ApiException {
    return put(path, body, null);
  }

  private <T> T put(String path, GenericType<T> returnType) throws ApiException {
    return put(path, "", returnType);
  }

  private <T> T put(String path, Object body, GenericType<T> returnType) throws ApiException {
    return doRequest(path, "PUT", Collections.emptyList(), body, returnType);
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

  private static String processTemplate(String path, Map<String, String> values) {
    return new StringSubstitutor(values, "{", StringSubstitutor.DEFAULT_VAR_END).replace(path);
  }
}
