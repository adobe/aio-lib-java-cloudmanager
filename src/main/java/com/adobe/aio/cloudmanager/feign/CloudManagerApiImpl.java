package com.adobe.aio.cloudmanager.feign;

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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.adobe.aio.cloudmanager.CloudManagerApi;
import com.adobe.aio.cloudmanager.CloudManagerApiException;
import com.adobe.aio.cloudmanager.Environment;
import com.adobe.aio.cloudmanager.EnvironmentLog;
import com.adobe.aio.cloudmanager.LogOption;
import com.adobe.aio.cloudmanager.Metric;
import com.adobe.aio.cloudmanager.Pipeline;
import com.adobe.aio.cloudmanager.PipelineExecution;
import com.adobe.aio.cloudmanager.PipelineExecutionStepState;
import com.adobe.aio.cloudmanager.PipelineUpdate;
import com.adobe.aio.cloudmanager.Program;
import com.adobe.aio.cloudmanager.Repository;
import com.adobe.aio.cloudmanager.Variable;
import com.adobe.aio.cloudmanager.event.PipelineExecutionEndEvent;
import com.adobe.aio.cloudmanager.event.PipelineExecutionStartEvent;
import com.adobe.aio.cloudmanager.event.PipelineExecutionStepEndEvent;
import com.adobe.aio.cloudmanager.event.PipelineExecutionStepStartEvent;
import com.adobe.aio.cloudmanager.event.PipelineExecutionStepWaitingEvent;
import com.adobe.aio.cloudmanager.feign.client.EnvironmentApiClient;
import com.adobe.aio.cloudmanager.feign.client.PipelineApiClient;
import com.adobe.aio.cloudmanager.feign.client.PipelineExecutionApiClient;
import com.adobe.aio.cloudmanager.feign.client.ProgramApiClient;
import com.adobe.aio.cloudmanager.feign.client.RepositoryApiClient;
import com.adobe.aio.cloudmanager.feign.client.VariableApiClient;
import com.adobe.aio.cloudmanager.feign.exception.EnvironmentExceptionDecoder;
import com.adobe.aio.cloudmanager.feign.exception.PipelineExceptionDecoder;
import com.adobe.aio.cloudmanager.feign.exception.PipelineExecutionExceptionDecoder;
import com.adobe.aio.cloudmanager.feign.exception.ProgramExceptionDecoder;
import com.adobe.aio.cloudmanager.feign.exception.RepositoryExceptionDecoder;
import com.adobe.aio.cloudmanager.feign.exception.VariableExceptionDecoder;
import com.adobe.aio.cloudmanager.impl.model.EmbeddedProgram;
import com.adobe.aio.cloudmanager.impl.model.EnvironmentList;
import com.adobe.aio.cloudmanager.impl.model.EnvironmentLogs;
import com.adobe.aio.cloudmanager.impl.model.HalLink;
import com.adobe.aio.cloudmanager.impl.model.PipelineExecutionEmbedded;
import com.adobe.aio.cloudmanager.impl.model.PipelineList;
import com.adobe.aio.cloudmanager.impl.model.PipelinePhase;
import com.adobe.aio.cloudmanager.impl.model.PipelineStepMetrics;
import com.adobe.aio.cloudmanager.impl.model.ProgramList;
import com.adobe.aio.cloudmanager.impl.model.Redirect;
import com.adobe.aio.cloudmanager.impl.model.RepositoryBranch;
import com.adobe.aio.cloudmanager.impl.model.RepositoryList;
import com.adobe.aio.cloudmanager.impl.model.VariableList;
import feign.Feign;
import lombok.NonNull;

public class CloudManagerApiImpl implements CloudManagerApi {

  private static final int NOT_FOUND = 404;
  private static final String NO_LOG_REDIRECT_ERROR = "Log [%s] did not contain a redirect. Was: %s.";

  private final ProgramApiClient programApi;
  private final RepositoryApiClient repositoryApi;
  private final PipelineApiClient pipelineApi;
  private final PipelineExecutionApiClient executionsApi;
  private final EnvironmentApiClient environmentApi;
  private final VariableApiClient variableApi;
  
  public CloudManagerApiImpl(Feign.Builder builder, String baseUrl) {
    programApi = builder.errorDecoder(new ProgramExceptionDecoder()).target(ProgramApiClient.class, baseUrl);
    repositoryApi = builder.errorDecoder(new RepositoryExceptionDecoder()).target(RepositoryApiClient.class, baseUrl);
    pipelineApi = builder.errorDecoder(new PipelineExceptionDecoder()).target(PipelineApiClient.class, baseUrl);
    executionsApi = builder.errorDecoder(new PipelineExecutionExceptionDecoder()).target(PipelineExecutionApiClient.class, baseUrl);
    environmentApi = builder.errorDecoder(new EnvironmentExceptionDecoder()).target(EnvironmentApiClient.class, baseUrl);
    variableApi = builder.errorDecoder(new VariableExceptionDecoder()).target(VariableApiClient.class, baseUrl);
  }
  
  @Override
  public @NonNull Collection<Program> listPrograms() throws CloudManagerApiException {
    ProgramList programList = programApi.list();
    return programList.getEmbedded() == null ?
        Collections.emptyList() :
        programList.getEmbedded().getPrograms().stream().map(p -> new ProgramImpl(p, this)).collect(Collectors.toList());
  }

  @Override
  public @NonNull Program getProgram(@NonNull String programId) throws CloudManagerApiException {
    EmbeddedProgram program = programApi.get(programId);
    return new ProgramImpl(program, this);
  }

  @Override
  public void deleteProgram(@NonNull String programId) throws CloudManagerApiException {
    programApi.delete(programId);
  }

  @Override
  public void deleteProgram(@NonNull Program program) throws CloudManagerApiException {
    deleteProgram(program.getId());
  }

  @Override
  public Collection<Program> listPrograms(String tenantId) throws CloudManagerApiException {
    ProgramList programList = programApi.list(tenantId);
    return programList.getEmbedded() == null ?
        Collections.emptyList() :
        programList.getEmbedded().getPrograms().stream().map(p -> new ProgramImpl(p, this)).collect(Collectors.toList());

  }

  @Override
  public @NonNull Collection<Repository> listRepositories(@NonNull String programId) throws CloudManagerApiException {
    RepositoryList list = repositoryApi.list(programId);
    return list.getEmbedded().getRepositories().stream().map(r -> new RepositoryImpl(r, this)).collect(Collectors.toList());
  }

  @Override
  public @NonNull Collection<Repository> listRepositories(@NonNull Program program) throws CloudManagerApiException {
    return listRepositories(program.getId());
  }

  @Override
  public @NonNull Collection<Repository> listRepositories(@NonNull String programId, int limit) throws CloudManagerApiException {
    return listRepositories(programId, 0, limit);
  }

  @Override
  public @NonNull Collection<Repository> listRepositories(@NonNull Program program, int limit) throws CloudManagerApiException {
    return listRepositories(program.getId(), limit);
  }

  @Override
  public @NonNull Collection<Repository> listRepositories(@NonNull String programId, int start, int limit) throws CloudManagerApiException {
    Map<String, Object> params = new HashMap<>();
    params.put(Repository.START_PARAM, start);
    params.put(Repository.LIMIT_PARAM, limit);
    RepositoryList list = repositoryApi.list(programId, params);
    return list.getEmbedded().getRepositories().stream().map(r -> new RepositoryImpl(r, this)).collect(Collectors.toList());
  }

  @Override
  public @NonNull Collection<Repository> listRepositories(@NonNull Program program, int start, int limit) throws CloudManagerApiException {
    return listRepositories(program.getId(), start, limit);
  }

  @Override
  public @NonNull Repository getRepository(@NonNull String programId, @NonNull String repositoryId) throws CloudManagerApiException {
    return new RepositoryImpl(repositoryApi.get(programId, repositoryId), this);
  }

  @Override
  public @NonNull Repository getRepository(@NonNull Program program, @NonNull String repositoryId) throws CloudManagerApiException {
    return getRepository(program.getId(), repositoryId);
  }

  @Override
  public @NonNull Collection<String> listBranches(@NonNull Repository repository) throws CloudManagerApiException {
    return repositoryApi.listBranches(repository.getProgramId(), repository.getId()).getEmbedded().getBranches().stream()
        .map(RepositoryBranch::getName).collect(Collectors.toList());
  }

  @Override
  public @NonNull Collection<Pipeline> listPipelines(@NonNull String programId) throws CloudManagerApiException {
    return listPipelines(programId, p -> true);
  }

  @Override
  public @NonNull Collection<Pipeline> listPipelines(@NonNull String programId, @NonNull Predicate<Pipeline> predicate) throws CloudManagerApiException {
    return new ArrayList<>(listPipelineDetails(programId, predicate));
  }

  @Override
  public void deletePipeline(@NonNull String programId, @NonNull String pipelineId) throws CloudManagerApiException {
    pipelineApi.delete(programId, pipelineId);
  }

  @Override
  public void deletePipeline(@NonNull Pipeline pipeline) throws CloudManagerApiException {
    deletePipeline(pipeline.getProgramId(), pipeline.getId());
  }

  @Override
  public @NonNull Pipeline updatePipeline(@NonNull String programId, @NonNull String pipelineId, @NonNull PipelineUpdate updates) throws CloudManagerApiException {
    return updatePipeline(pipelineApi.get(programId, pipelineId), updates);
  }

  @Override
  public @NonNull Pipeline updatePipeline(@NonNull Pipeline pipeline, @NonNull PipelineUpdate updates) throws CloudManagerApiException {
    return updatePipeline(pipelineApi.get(pipeline.getProgramId(), pipeline.getId()), updates);
  }
  
  @Override
  public void invalidatePipelineCache(@NonNull String programId, @NonNull String pipelineId) throws CloudManagerApiException {
    pipelineApi.invalidateCache(programId, pipelineId);
  }

  @Override
  public void invalidatePipelineCache(@NonNull Pipeline pipeline) throws CloudManagerApiException {
    invalidatePipelineCache(pipeline.getProgramId(), pipeline.getId());
  }

  @Override
  public @NonNull Optional<PipelineExecution> getCurrentExecution(@NonNull String programId, @NonNull String pipelineId) throws CloudManagerApiException {
    try {
      com.adobe.aio.cloudmanager.impl.model.PipelineExecution current = executionsApi.current(programId, pipelineId);
      return Optional.of(new PipelineExecutionImpl(current, this));
    } catch (CloudManagerApiException ex) {
      if (ex.getErrorCode() == NOT_FOUND) {
        return Optional.empty();
      }
      throw ex;
    }
  }

  @Override
  public @NonNull Optional<PipelineExecution> getCurrentExecution(@NonNull Pipeline pipeline) throws CloudManagerApiException {
    return getCurrentExecution(pipeline.getProgramId(), pipeline.getId());
  }

  @Override
  public @NonNull PipelineExecution startExecution(@NonNull String programId, @NonNull String pipelineId) throws CloudManagerApiException {
    return new PipelineExecutionImpl(executionsApi.start(programId, pipelineId), this);
  }

  @Override
  public @NonNull PipelineExecution startExecution(@NonNull Pipeline pipeline) throws CloudManagerApiException {
    return startExecution(pipeline.getProgramId(), pipeline.getId());
  }

  @Override
  public @NonNull PipelineExecutionImpl getExecution(@NonNull String programId, @NonNull String pipelineId, @NonNull String executionId) throws CloudManagerApiException {
    return new PipelineExecutionImpl(executionsApi.get(programId, pipelineId, executionId), this);
  }

  @Override
  public @NonNull PipelineExecution getExecution(@NonNull Pipeline pipeline, @NonNull String executionId) throws CloudManagerApiException {
    return getExecution(pipeline.getProgramId(), pipeline.getId(), executionId);
  }

  @Override
  public @NonNull PipelineExecution getExecution(@NonNull PipelineExecutionStartEvent event) throws CloudManagerApiException {
    return getExecution(event.getEvent().getActivitystreamsobject().getAtId());
  }

  @Override
  public @NonNull PipelineExecution getExecution(@NonNull PipelineExecutionEndEvent event) throws CloudManagerApiException {
    return getExecution(event.getEvent().getActivitystreamsobject().getAtId());
  }

  @Override
  public boolean isExecutionRunning(@NonNull String programId, @NonNull String pipelineId, @NonNull String executionId) throws CloudManagerApiException {
    PipelineExecution execution = getExecution(programId, pipelineId, executionId);
    PipelineExecution.Status current = execution.getStatusState();
    return current == PipelineExecution.Status.NOT_STARTED ||
        current == PipelineExecution.Status.RUNNING ||
        current == PipelineExecution.Status.CANCELLING;
  }

  @Override
  public boolean isExecutionRunning(@NonNull PipelineExecution execution) throws CloudManagerApiException {
    return isExecutionRunning(execution.getProgramId(), execution.getPipelineId(), execution.getId());
  }

  @Override
  public @NonNull PipelineExecutionStepStateImpl getExecutionStepState(@NonNull PipelineExecution execution, @NonNull String action) throws CloudManagerApiException {
    return getExecutionStepState(getExecution(execution.getProgramId(), execution.getPipelineId(), execution.getId()), action);
  }

  @Override
  public @NonNull PipelineExecutionStepState getExecutionStepState(@NonNull PipelineExecutionStepStartEvent event) throws CloudManagerApiException {
    return getExecutionStepState(event.getEvent().getActivitystreamsobject().getAtId());
  }

  @Override
  public @NonNull PipelineExecutionStepState getExecutionStepState(@NonNull PipelineExecutionStepWaitingEvent event) throws CloudManagerApiException {
    return getExecutionStepState(event.getEvent().getActivitystreamsobject().getAtId());
  }

  @Override
  public @NonNull PipelineExecutionStepState getExecutionStepState(@NonNull PipelineExecutionStepEndEvent event) throws CloudManagerApiException {
    return getExecutionStepState(event.getEvent().getActivitystreamsobject().getAtId());
  }

  @Override
  public @NonNull PipelineExecutionStepStateImpl getCurrentStep(@NonNull PipelineExecution execution) throws CloudManagerApiException {
    return getCurrentStep(getExecution(execution.getProgramId(), execution.getPipelineId(), execution.getId()));
  }

  @Override
  public @NonNull PipelineExecutionStepStateImpl getWaitingStep(@NonNull PipelineExecution execution) throws CloudManagerApiException {
    return getWaitingStep(getExecution(execution.getProgramId(), execution.getPipelineId(), execution.getId()));
  }

  @Override
  public void advanceCurrentExecution(@NonNull String programId, @NonNull String pipelineId) throws CloudManagerApiException {
    Optional<PipelineExecution> execution = getCurrentExecution(programId, pipelineId);
    if (execution.isPresent()) {
      advanceExecution((PipelineExecutionImpl) execution.get());
    }
  }

  @Override
  public void advanceExecution(@NonNull String programId, @NonNull String pipelineId, @NonNull String executionId) throws CloudManagerApiException {
    advanceExecution(getExecution(programId, pipelineId, executionId));
  }

  @Override
  public void advanceExecution(@NonNull PipelineExecution execution) throws CloudManagerApiException {
    advanceExecution(execution.getProgramId(), execution.getPipelineId(), execution.getId());
  }

  @Override
  public void cancelCurrentExecution(@NonNull String programId, @NonNull String pipelineId) throws CloudManagerApiException {
    Optional<PipelineExecution> execution = getCurrentExecution(programId, pipelineId);
    if (execution.isPresent()) {
      cancelExecution((PipelineExecutionImpl) execution.get());
    }
  }

  @Override
  public void cancelExecution(@NonNull String programId, @NonNull String pipelineId, @NonNull String executionId) throws CloudManagerApiException {
    cancelExecution(getExecution(programId, pipelineId, executionId));
  }

  @Override
  public void cancelExecution(@NonNull PipelineExecution execution) throws CloudManagerApiException {
    cancelExecution(execution.getProgramId(), execution.getPipelineId(), execution.getId());
  }

  @Override
  public @NonNull String getExecutionStepLogDownloadUrl(@NonNull String programId, @NonNull String pipelineId, @NonNull String executionId, @NonNull String action) throws CloudManagerApiException {
    PipelineExecutionImpl execution = getExecution(programId, pipelineId, executionId);
    return getExecutionStepLogDownloadUrl(execution, action, null);
  }

  @Override
  public @NonNull String getExecutionStepLogDownloadUrl(@NonNull String programId, @NonNull String pipelineId, @NonNull String executionId, @NonNull String action, @NonNull String filename) throws CloudManagerApiException {
    PipelineExecutionImpl execution = getExecution(programId, pipelineId, executionId);
    return getExecutionStepLogDownloadUrl(execution, action, filename);
  }

  @Override
  public @NonNull String getExecutionStepLogDownloadUrl(@NonNull PipelineExecution execution, @NonNull String action) throws CloudManagerApiException {
    return getExecutionStepLogDownloadUrl(execution.getProgramId(), execution.getPipelineId(), execution.getId(), action);
  }

  @Override
  public @NonNull String getExecutionStepLogDownloadUrl(@NonNull PipelineExecution execution, @NonNull String action, @NonNull String filename) throws CloudManagerApiException {
    return getExecutionStepLogDownloadUrl(execution.getProgramId(), execution.getPipelineId(), execution.getId(), action, filename);
  }

  @Override
  public void downloadExecutionStepLog(@NonNull String programId, @NonNull String pipelineId, @NonNull String executionId, @NonNull String action, @NonNull OutputStream outputStream) throws CloudManagerApiException {
    streamLog(outputStream, getExecutionStepLogDownloadUrl(programId, pipelineId, executionId, action));
  }

  @Override
  public void downloadExecutionStepLog(@NonNull String programId, @NonNull String pipelineId, @NonNull String executionId, @NonNull String action, @NonNull String name, @NonNull OutputStream outputStream) throws CloudManagerApiException {
    streamLog(outputStream, getExecutionStepLogDownloadUrl(programId, pipelineId, executionId, action, name));
  }

  @Override
  public void downloadExecutionStepLog(@NonNull PipelineExecution execution, @NonNull String action, @NonNull OutputStream outputStream) throws CloudManagerApiException {
    downloadExecutionStepLog(execution.getProgramId(), execution.getPipelineId(), execution.getId(), action, outputStream);
  }

  @Override
  public void downloadExecutionStepLog(@NonNull PipelineExecution execution, @NonNull String action, @NonNull String filename, @NonNull OutputStream outputStream) throws CloudManagerApiException {
    downloadExecutionStepLog(execution.getProgramId(), execution.getPipelineId(), execution.getId(), action, filename, outputStream);
  }

  @Override
  public @NonNull Collection<Metric> getQualityGateResults(@NonNull PipelineExecution execution, @NonNull String action) throws CloudManagerApiException {
    PipelineExecutionStepStateImpl stepState = getExecutionStepState(execution, action);
    HalLink link = stepState.getLinks().getHttpnsAdobeComadobecloudrelpipelinemetrics();
    String href = link != null ? link.getHref() : null;
    if (StringUtils.isBlank(href)) {
      throw new CloudManagerApiException(String.format("Could not find metric link for action (%s) on pipeline %s", action, execution.getPipelineId()));
    }
    PipelineStepMetrics psm = executionsApi.getStepMetrics(href);
    return psm.getMetrics().stream().map(MetricImpl::new).collect(Collectors.toList());
  }

  @Override
  public @NonNull Collection<PipelineExecution> listExecutions(@NonNull Pipeline pipeline) throws CloudManagerApiException {
    throw new IllegalStateException("Not Implemented.");
  }

  @Override
  public Collection<Environment> listEnvironments(@NonNull String programId) throws CloudManagerApiException {
    return listEnvironments(programId, null);
  }

  @Override
  public Collection<Environment> listEnvironments(@NonNull String programId, Environment.Type type) throws CloudManagerApiException {
    Map<String, Object> params = new HashMap<>();
    if (type != null) {
      params.put(Environment.Type.TYPE_PARAM, type.toString().toLowerCase());
    }
    EnvironmentList environments = environmentApi.list(programId, params);
    return environments.getEmbedded().getEnvironments().stream().map(e -> new EnvironmentImpl(e, this)).collect(Collectors.toList());
  }

  @Override
  public void createEnvironment(@NonNull String programId, Environment.Type type) throws CloudManagerApiException {
    throw new IllegalStateException("Not Implemented.");
  }

  @Override
  public @NonNull Environment getEnvironment(@NonNull String programId, Predicate<Environment> predicate) throws CloudManagerApiException {
    Collection<Environment> environments = listEnvironments(programId, null);
    return environments.stream()
        .filter(predicate)
        .findFirst()
        .orElseThrow(() -> new CloudManagerApiException(String.format(EnvironmentExceptionDecoder.ErrorType.FIND_ENVIRONMENT.getMessage(), predicate, programId)));
  }

  @Override
  public void deleteEnvironment(@NonNull String programId, @NonNull String environmentId) throws CloudManagerApiException {
    environmentApi.delete(programId, environmentId);
  }

  @Override
  public void deleteEnvironment(@NonNull Environment environment) throws CloudManagerApiException {
    deleteEnvironment(environment.getProgramId(), environment.getId());
  }

  @Override
  public @NonNull Collection<EnvironmentLog> downloadEnvironmentLogs(@NonNull String programId, @NonNull String environmentId, @NonNull LogOption logOption, int days, @NonNull File dir) throws CloudManagerApiException {
    Map<String, Object> params = new HashMap<>();
    params.put(LogOption.SERVICE_PARAM, logOption.getService());
    params.put(LogOption.NAME_PARAM, logOption.getName());
    params.put(LogOption.DAYS_PARAM, Integer.toString(days));
    EnvironmentLogs logs = environmentApi.listLogs(programId, environmentId, params);
    List<com.adobe.aio.cloudmanager.impl.model.EnvironmentLog> downloads = logs.getEmbedded().getDownloads();
    
    if (downloads == null || downloads.isEmpty()) {
      return Collections.emptyList();
    }
    List<EnvironmentLog> downloaded = new ArrayList<>();
    for (com.adobe.aio.cloudmanager.impl.model.EnvironmentLog d : downloads) {
      EnvironmentLogImpl log = new EnvironmentLogImpl(d);
      String logfileName = String.format("%d-%s-%s-%s.log.gz", log.getEnvironmentId(), log.getService(), log.getName(), log.getDate());
      log.setPath(dir.getPath() + "/" + logfileName);
      log.setUrl(log.getLinks().getHttpnsAdobeComadobecloudrellogsdownload().getHref());
      downloadLog(log);
      downloaded.add(log);
    }
    
    return downloaded;
  }

  @Override
  public @NonNull Collection<EnvironmentLog> downloadEnvironmentLogs(@NonNull Environment environment, @NonNull LogOption logOption, int days, @NonNull File dir) throws CloudManagerApiException {
    return downloadEnvironmentLogs(environment.getProgramId(), environment.getId(), logOption, days, dir);
  }

  @Override
  public @NonNull Set<Variable> listEnvironmentVariables(@NonNull String programId, @NonNull String environmentId) throws CloudManagerApiException {
    return transform(variableApi.listEnvironment(programId, environmentId));
  }

  @Override
  public @NonNull Set<Variable> listEnvironmentVariables(@NonNull Environment environment) throws CloudManagerApiException {
    return listEnvironmentVariables(environment.getProgramId(), environment.getId());
  }

  @Override
  public @NonNull Set<Variable> setEnvironmentVariables(@NonNull String programId, @NonNull String environmentId, Variable... variables) throws CloudManagerApiException {
    return transform(variableApi.setEnvironment(programId, environmentId, variables));
  }

  @Override
  public @NonNull Set<Variable> setEnvironmentVariables(@NonNull Environment environment, Variable... variables) throws CloudManagerApiException {
    return setEnvironmentVariables(environment.getProgramId(), environment.getId(), variables);
  }

  @Override
  public @NonNull Set<Variable> listPipelineVariables(@NonNull String programId, @NonNull String pipelineId) throws CloudManagerApiException {
    return transform(variableApi.listPipeline(programId, pipelineId));
  }

  @Override
  public @NonNull Set<Variable> listPipelineVariables(@NonNull Pipeline pipeline) throws CloudManagerApiException {
    return listPipelineVariables(pipeline.getProgramId(), pipeline.getId());
  }

  @Override
  public @NonNull Set<Variable> setPipelineVariables(@NonNull String programId, @NonNull String pipelineId, Variable... variables) throws CloudManagerApiException {
    return transform(variableApi.setPipeline(programId, pipelineId, variables));
  }

  @Override
  public @NonNull Set<Variable> setPipelineVariables(@NonNull Pipeline pipeline, Variable... variables) throws CloudManagerApiException {
    return setPipelineVariables(pipeline.getProgramId(), pipeline.getId(), variables);
  }
  
  protected @NonNull PipelineExecution getExecution(@NonNull PipelineExecutionStepStateImpl step) throws CloudManagerApiException {
    HalLink link = step.getLinks().getHttpnsAdobeComadobecloudrelexecution();
    if (link == null) {
      throw new CloudManagerApiException(String.format(PipelineExecutionExceptionDecoder.ErrorType.FIND_EXECUTION_LINK.getMessage(), step.getLinks().getSelf().getHref()));
    }
    return new PipelineExecutionImpl(executionsApi.get(link.getHref()), this);
  }

  protected @NonNull PipelineExecutionStepStateImpl getExecutionStepState(@NonNull PipelineExecutionImpl actual, @NonNull String action) throws CloudManagerApiException {
    return getStep(actual, s -> s.getAction().equals(action), String.format(PipelineExecutionExceptionDecoder.ErrorType.FIND_STEP_STATE.getMessage(), action, actual.getId()));
  }

  protected @NonNull PipelineExecutionStepStateImpl getCurrentStep(@NonNull PipelineExecutionImpl actual) throws CloudManagerApiException {
    return getStep(actual, PipelineExecutionStepStateImpl.IS_CURRENT, String.format(PipelineExecutionExceptionDecoder.ErrorType.FIND_CURRENT_STEP.getMessage(), actual.getPipelineId()));
  }

  protected @NonNull PipelineExecutionStepStateImpl getWaitingStep(@NonNull PipelineExecutionImpl actual) throws CloudManagerApiException {
    return getStep(actual, PipelineExecutionStepStateImpl.IS_WAITING, String.format(PipelineExecutionExceptionDecoder.ErrorType.FIND_WAITING_STEP.getMessage(), actual.getPipelineId()));
  }
  
  protected void advanceExecution(PipelineExecutionImpl execution) throws CloudManagerApiException {
    PipelineExecutionImpl.StepFormData form = execution.getAdvanceLinkAndBody();
    executionsApi.advance(form.getHref(), form.getBody());
  }

  protected void cancelExecution(PipelineExecutionImpl execution) throws CloudManagerApiException {
    PipelineExecutionImpl.StepFormData form = execution.getCancelLinkAndBody();
    executionsApi.cancel(form.getHref(), form.getBody());
  }

  protected @NonNull String getExecutionStepLogDownloadUrl(PipelineExecutionImpl actual, String action, String name) throws CloudManagerApiException {
    return getExecutionStepLogDownloadUrl(getExecutionStepState(actual, action), name);
  }

  protected @NonNull String getExecutionStepLogDownloadUrl(PipelineExecutionStepStateImpl stepState, String name) throws CloudManagerApiException {
    Map<String, Object> query = new HashMap<>();
    if (StringUtils.isNotBlank(name)) {
      query.put(PipelineExecutionStepStateImpl.FILENAME_PARAM, name);
    }
    HalLink link = stepState.getLinks().getHttpnsAdobeComadobecloudrelpipelinelogs();
    if (link == null || StringUtils.isBlank(link.getHref())) {
      throw new CloudManagerApiException(String.format("Could not find logs link for action '%s'.", stepState.getAction()));
    }
    Redirect redirect = executionsApi.getLogs(stepState.getLinks().getHttpnsAdobeComadobecloudrelpipelinelogs().getHref(), query);
    String url = redirect != null ? redirect.getRedirect() : null;
    if (StringUtils.isBlank(url)) {
      throw new CloudManagerApiException(String.format(NO_LOG_REDIRECT_ERROR, link.getHref(), url));
    }
    return url;
  }

  protected void downloadExecutionStepLog(PipelineExecutionStepStateImpl step, String filename, OutputStream os) throws CloudManagerApiException {
    streamLog(os, getExecutionStepLogDownloadUrl(step, filename));
  }

  @NonNull
  private Collection<PipelineImpl> listPipelineDetails(String programId, Predicate<Pipeline> predicate) throws CloudManagerApiException {
    PipelineList list = pipelineApi.list(programId);

    if (list == null || list.getEmbedded() == null || list.getEmbedded().getPipelines() == null) {
      throw new CloudManagerApiException(String.format(PipelineExceptionDecoder.ErrorType.FIND_PIPELINES.getMessage(), programId));
    }

    return list.getEmbedded().getPipelines().stream().map(p -> new PipelineImpl(p, this)).filter(predicate).collect(Collectors.toList());
  }

  @NonNull
  private Pipeline updatePipeline(com.adobe.aio.cloudmanager.impl.model.Pipeline original, PipelineUpdate updates) throws CloudManagerApiException {
    com.adobe.aio.cloudmanager.impl.model.Pipeline toUpdate = new com.adobe.aio.cloudmanager.impl.model.Pipeline();
    PipelinePhase buildPhase = original.getPhases().stream().filter(p -> PipelinePhase.TypeEnum.BUILD == p.getType())
        .findFirst().orElseThrow(() -> new CloudManagerApiException(String.format(PipelineExceptionDecoder.ErrorType.NO_BUILD_PHASE.getMessage(), original.getId())));

    if (updates.getBranch() != null) {
      buildPhase.setBranch(updates.getBranch());
    }
    if (updates.getRepositoryId() != null) {
      buildPhase.setRepositoryId(updates.getRepositoryId());
    }
    toUpdate.getPhases().add(buildPhase);
    com.adobe.aio.cloudmanager.impl.model.Pipeline result = pipelineApi.update(original.getProgramId(), original.getId(), toUpdate);
    return new PipelineImpl(result, this);
  }

  @NonNull
  private PipelineExecution getExecution(String url) throws CloudManagerApiException {
    try {
      String path = new URL(url).getPath();
      return new PipelineExecutionImpl(executionsApi.get(path), this);
    } catch (MalformedURLException e) {
      throw new CloudManagerApiException(String.format(PipelineExecutionExceptionDecoder.ErrorType.GET_EXECUTION.getMessage(), e.getLocalizedMessage()));
    }
  }
  
  private @NonNull PipelineExecutionStepStateImpl getStep(@NonNull PipelineExecutionImpl actual,
                                                          Predicate<com.adobe.aio.cloudmanager.impl.model.PipelineExecutionStepState> predicate,
                                                          String errorMessage) throws CloudManagerApiException {
    PipelineExecutionEmbedded embeddeds = actual.getEmbedded();
    CloudManagerApiException potential = new CloudManagerApiException(errorMessage);
    if (embeddeds == null || embeddeds.getStepStates() == null || embeddeds.getStepStates().isEmpty()) {
      throw potential;
    }
    com.adobe.aio.cloudmanager.impl.model.PipelineExecutionStepState step = embeddeds.getStepStates()
        .stream()
        .filter(predicate)
        .findFirst()
        .orElseThrow(() -> potential);
    return new PipelineExecutionStepStateImpl(step, this);
  }

  @NonNull
  private PipelineExecutionStepState getExecutionStepState(String url) throws CloudManagerApiException {
    try {
      String path = new URL(url).getPath();
      return new PipelineExecutionStepStateImpl(executionsApi.getStepState(path), this);
    } catch (MalformedURLException e) {
      throw new CloudManagerApiException(String.format(PipelineExecutionExceptionDecoder.ErrorType.GET_STEP_STATE.getMessage(), e.getLocalizedMessage()));
    }
  }

  private void streamLog(OutputStream os, String url) throws CloudManagerApiException {
    try (InputStream is = new URL(url).openStream()) {
      IOUtils.copy(is, os);
    } catch (IOException e) {
      throw new CloudManagerApiException(String.format(PipelineExecutionExceptionDecoder.ErrorType.GET_LOGS.getMessage(), e.getLocalizedMessage()));
    } finally {
      try {
        IOUtils.close(os);
      } catch (IOException e) {
        // Do nothing on failure to close Output Stream from caller
      }
    }
  }

  private void downloadLog(EnvironmentLogImpl log) throws CloudManagerApiException {
    try {
      Map<String, Object> params = new LinkedHashMap<>();
      params.put(EnvironmentLog.SERVICE_PARAM, log.getService());
      params.put(EnvironmentLog.NAME_PARAM, log.getName());
      params.put(EnvironmentLog.DATE_PARAM, log.getDate());
      Redirect redirect = environmentApi.downloadLog(Long.toString(log.getProgramId()), Long.toString(log.getEnvironmentId()), params);
      File downloadedFile = new File(log.getPath());
      FileUtils.copyInputStreamToFile(new URL(redirect.getRedirect()).openStream(), downloadedFile);
    } catch (MalformedURLException e) {
      throw new CloudManagerApiException(String.format(NO_LOG_REDIRECT_ERROR, log.getUrl(), e.getLocalizedMessage()));
    } catch (IOException e) {
      throw new CloudManagerApiException(String.format("Could not download %s to %s (%s).", log.getUrl(), log.getPath(), e.getLocalizedMessage()));
    }
  }

  private Set<Variable> transform(VariableList variables) {
    if (variables.getTotalNumberOfItems().equals(0)) {
      return Collections.emptySet();
    }
    return variables.getEmbedded().getVariables().stream().map(Variable::new).collect(Collectors.toSet());
  }
}
