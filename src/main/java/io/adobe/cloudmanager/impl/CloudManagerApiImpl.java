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
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.adobe.aio.feign.AIOHeaderInterceptor;
import com.adobe.aio.ims.feign.AuthInterceptor;
import com.adobe.aio.workspace.Workspace;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.Feign;
import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;
import io.adobe.cloudmanager.CloudManagerApi;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.Environment;
import io.adobe.cloudmanager.EnvironmentLog;
import io.adobe.cloudmanager.LogOption;
import io.adobe.cloudmanager.Metric;
import io.adobe.cloudmanager.Pipeline;
import io.adobe.cloudmanager.PipelineExecution;
import io.adobe.cloudmanager.PipelineExecutionStepState;
import io.adobe.cloudmanager.PipelineUpdate;
import io.adobe.cloudmanager.Program;
import io.adobe.cloudmanager.Repository;
import io.adobe.cloudmanager.StepAction;
import io.adobe.cloudmanager.Tenant;
import io.adobe.cloudmanager.Variable;
import io.adobe.cloudmanager.event.PipelineExecutionEndEvent;
import io.adobe.cloudmanager.event.PipelineExecutionStartEvent;
import io.adobe.cloudmanager.event.PipelineExecutionStepEndEvent;
import io.adobe.cloudmanager.event.PipelineExecutionStepStartEvent;
import io.adobe.cloudmanager.event.PipelineExecutionStepWaitingEvent;
import io.adobe.cloudmanager.impl.client.FeignPipelineApi;
import io.adobe.cloudmanager.impl.client.FeignPipelineExecutionApi;
import io.adobe.cloudmanager.impl.client.FeignProgramApi;
import io.adobe.cloudmanager.impl.client.FeignRepositoryApi;
import io.adobe.cloudmanager.impl.client.FeignTenantApi;
import io.adobe.cloudmanager.impl.exception.PipelineExceptionDecoder;
import io.adobe.cloudmanager.impl.exception.PipelineExecutionExceptionDecoder;
import io.adobe.cloudmanager.impl.exception.ProgramExceptionDecoder;
import io.adobe.cloudmanager.impl.exception.RepositoryExceptionDecoder;
import io.adobe.cloudmanager.impl.exception.TenantExceptionDecoder;
import io.adobe.cloudmanager.impl.generated.BranchList;
import io.adobe.cloudmanager.impl.generated.EmbeddedProgram;
import io.adobe.cloudmanager.impl.generated.PipelineExecutionEmbedded;
import io.adobe.cloudmanager.impl.generated.PipelineExecutionListRepresentation;
import io.adobe.cloudmanager.impl.generated.PipelineList;
import io.adobe.cloudmanager.impl.generated.PipelinePhase;
import io.adobe.cloudmanager.impl.generated.PipelineStepMetrics;
import io.adobe.cloudmanager.impl.generated.ProgramList;
import io.adobe.cloudmanager.impl.generated.Redirect;
import io.adobe.cloudmanager.impl.generated.RepositoryBranch;
import io.adobe.cloudmanager.impl.generated.RepositoryList;
import io.adobe.cloudmanager.impl.generated.TenantList;

import static com.adobe.aio.util.feign.FeignUtil.*;

public class CloudManagerApiImpl implements CloudManagerApi {
  private static final String NO_LOG_REDIRECT_ERROR = "Log redirect for execution %s, action '%s' did not exist.";

  private final FeignTenantApi tenantApi;
  private final FeignProgramApi programApi;
  private final FeignRepositoryApi repositoryApi;
  private final FeignPipelineApi pipelineApi;
  private final FeignPipelineExecutionApi executionApi;

  public CloudManagerApiImpl(Workspace workspace, URL url) {

    ObjectMapper mapper = JsonMapper.builder()
        .serializationInclusion(JsonInclude.Include.NON_NULL)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
        .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
        .addModule(new JavaTimeModule())
        .build();

    RequestInterceptor authInterceptor = AuthInterceptor.builder().workspace(workspace).build();
    RequestInterceptor aioHeaderInterceptor = AIOHeaderInterceptor.builder().workspace(workspace).build();
    Request.Options options = new Request.Options(DEFAULT_CONNECT_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS, DEFAULT_READ_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS, true);
    Feign.Builder builder = Feign.builder()
        .client(new OkHttpClient())
        .logger(new Slf4jLogger())
        .logLevel(Logger.Level.BASIC)
        .requestInterceptor(authInterceptor)
        .requestInterceptor(aioHeaderInterceptor)
        .encoder(new JacksonEncoder(mapper))
        .decoder(new JacksonDecoder(mapper))
        .options(options);

    String baseUrl = url == null ? CLOUD_MANAGER_URL : url.toString();
    tenantApi = builder.errorDecoder(new TenantExceptionDecoder()).target(FeignTenantApi.class, baseUrl);
    programApi = builder.errorDecoder(new ProgramExceptionDecoder()).target(FeignProgramApi.class, baseUrl);
    repositoryApi = builder.errorDecoder(new RepositoryExceptionDecoder()).target(FeignRepositoryApi.class, baseUrl);
    pipelineApi = builder.errorDecoder(new PipelineExceptionDecoder()).target(FeignPipelineApi.class, baseUrl);
    executionApi = builder.errorDecoder(new PipelineExecutionExceptionDecoder()).target(FeignPipelineExecutionApi.class, baseUrl);
  }

  @Override
  public Program getProgram(String programId) throws CloudManagerApiException {
    EmbeddedProgram program = programApi.get(programId);
    return new ProgramImpl(program, this);
  }

  @Override
  public void deleteProgram(String programId) throws CloudManagerApiException {
    programApi.delete(programId);
  }

  @Override
  public void deleteProgram(Program program) throws CloudManagerApiException {
    deleteProgram(program.getId());
  }

  @Override
  public Collection<Program> listPrograms(String tenantId) throws CloudManagerApiException {
    ProgramList list = programApi.list(tenantId);
    return list.getEmbedded() == null ?
        Collections.emptyList() :
        list.getEmbedded().getPrograms().stream().map(p -> new ProgramImpl(p, this)).collect(Collectors.toList());
  }

  public Collection<Program> listPrograms(Tenant tenant) throws CloudManagerApiException {
    return listPrograms(tenant.getId());
  }

  @Override
  public Collection<Repository> listRepositories(String programId) throws CloudManagerApiException {
    RepositoryList list = repositoryApi.list(programId);
    return list.getEmbedded() == null ?
        Collections.emptyList() :
        list.getEmbedded().getRepositories().stream().map(r -> new RepositoryImpl(r, this)).collect(Collectors.toList());
  }

  @Override
  public Collection<Repository> listRepositories(Program program) throws CloudManagerApiException {
    return listRepositories(program.getId());
  }

  @Override
  public Collection<Repository> listRepositories(String programId, int limit) throws CloudManagerApiException {
    return listRepositories(programId, 0, limit);
  }

  @Override
  public Collection<Repository> listRepositories(Program program, int limit) throws CloudManagerApiException {
    return listRepositories(program.getId(), limit);
  }

  @Override
  public Collection<Repository> listRepositories(String programId, int start, int limit) throws CloudManagerApiException {
    RepositoryList list = repositoryApi.list(programId, start, limit);
    return list.getEmbedded() == null ?
        Collections.emptyList() :
        list.getEmbedded().getRepositories().stream().map(r -> new RepositoryImpl(r, this)).collect(Collectors.toList());
  }

  @Override
  public Collection<Repository> listRepositories(Program program, int start, int limit) throws CloudManagerApiException {
    return listRepositories(program.getId(), start, limit);
  }

  @Override
  public Repository getRepository(String programId, String repositoryId) throws CloudManagerApiException {
    return new RepositoryImpl(repositoryApi.get(programId, repositoryId), this);
  }

  @Override
  public Repository getRepository(Program program, String repositoryId) throws CloudManagerApiException {
    return getRepository(program.getId(), repositoryId);
  }

  @Override
  public Collection<String> listBranches(Repository repository) throws CloudManagerApiException {
    BranchList list = repositoryApi.listBranches(repository.getProgramId(), repository.getId());
    return list.getEmbedded() == null ?
        Collections.emptyList() :
        list.getEmbedded().getBranches().stream().map(RepositoryBranch::getName).collect(Collectors.toList());
  }

  @Override
  public Collection<Pipeline> listPipelines(String programId) throws CloudManagerApiException {
    return listPipelines(programId, p -> true);
  }

  @Override
  public Collection<Pipeline> listPipelines(Program program) throws CloudManagerApiException {
    return listPipelines(program.getId());
  }

  @Override
  public Collection<Pipeline> listPipelines(String programId, Predicate<Pipeline> predicate) throws CloudManagerApiException {
    return listPipelineDetails(programId, predicate);
  }

  @Override
  public PipelineImpl getPipeline(String programId, String pipelineId) throws CloudManagerApiException {
    return new PipelineImpl(pipelineApi.get(programId, pipelineId), this);
  }

  @Override
  public void deletePipeline(String programId, String pipelineId) throws CloudManagerApiException {
    pipelineApi.delete(programId, pipelineId);
  }

  @Override
  public void deletePipeline(Pipeline pipeline) throws CloudManagerApiException {
    pipelineApi.delete(pipeline.getProgramId(), pipeline.getId());
  }

  @Override
  public Pipeline updatePipeline(String programId, String pipelineId, PipelineUpdate updates) throws CloudManagerApiException {
    PipelineImpl original = getPipeline(programId, pipelineId);

    PipelinePhase buildPhase = original.getPhases().stream()
        .filter(p -> PipelinePhase.TypeEnum.BUILD == p.getType())
        .findFirst()
        .orElseThrow(() -> new CloudManagerApiException(String.format(PipelineExceptionDecoder.ErrorType.NO_BUILD_PHASE.getMessage(), pipelineId)));

    if (updates.getBranch() != null) {
      buildPhase.setBranch(updates.getBranch());
    }

    if (updates.getRepositoryId() != null) {
      buildPhase.setRepositoryId(updates.getRepositoryId());
    }
    io.adobe.cloudmanager.impl.generated.Pipeline toUpdate = new io.adobe.cloudmanager.impl.generated.Pipeline();
    toUpdate.getPhases().add(buildPhase);
    return new PipelineImpl(pipelineApi.update(programId, pipelineId, toUpdate), this);
  }

  @Override
  public Pipeline updatePipeline(Pipeline pipeline, PipelineUpdate updates) throws CloudManagerApiException {
    return updatePipeline(pipeline.getProgramId(), pipeline.getId(), updates);
  }

  @Override
  public void invalidatePipelineCache(String programId, String pipelineId) throws CloudManagerApiException {
    pipelineApi.invalidateCache(programId, pipelineId);
  }

  @Override
  public void invalidatePipelineCache(Pipeline pipeline) throws CloudManagerApiException {
    invalidatePipelineCache(pipeline.getProgramId(), pipeline.getId());
  }

  @Override
  public Optional<PipelineExecution> getCurrentExecution(String programId, String pipelineId) throws CloudManagerApiException {
    try {
      io.adobe.cloudmanager.impl.generated.PipelineExecution current = executionApi.current(programId, pipelineId);
      return Optional.of(new PipelineExecutionImpl(current, this));
    } catch (CloudManagerApiException ex) {
      if (ex.getErrorCode() == 404) {
        return Optional.empty();
      }
      throw ex;
    }
  }

  @Override
  public Optional<PipelineExecution> getCurrentExecution(Pipeline pipeline) throws CloudManagerApiException {
    return getCurrentExecution(pipeline.getProgramId(), pipeline.getId());
  }

  @Override
  public PipelineExecution startExecution(String programId, String pipelineId) throws CloudManagerApiException {
    return new PipelineExecutionImpl(executionApi.start(programId, pipelineId), this);
  }

  @Override
  public PipelineExecution startExecution(Pipeline pipeline) throws CloudManagerApiException {
    return startExecution(pipeline.getProgramId(), pipeline.getId());
  }

  @Override
  public PipelineExecutionImpl getExecution(String programId, String pipelineId, String executionId) throws CloudManagerApiException {
    return new PipelineExecutionImpl(executionApi.get(programId, pipelineId, executionId), this);
  }

  @Override
  public PipelineExecutionImpl getExecution(Pipeline pipeline, String executionId) throws CloudManagerApiException {
    return getExecution(pipeline.getProgramId(), pipeline.getId(), executionId);
  }

  @Override
  public PipelineExecutionStepStateImpl getExecutionStepState(PipelineExecution execution, StepAction action) throws CloudManagerApiException {
    PipelineExecutionImpl actual = getExecution(execution.getProgramId(), execution.getPipelineId(), execution.getId());
    return getExecutionStepState(actual, action);
  }

  @Override
  public void advanceExecution(String programId, String pipelineId, String executionId) throws CloudManagerApiException {
    advance(getExecution(programId, pipelineId, executionId));
  }

  @Override
  public void advanceExecution(PipelineExecution execution) throws CloudManagerApiException {
    advanceExecution(execution.getProgramId(), execution.getPipelineId(), execution.getId());
  }

  @Override
  public void cancelExecution(String programId, String pipelineId, String executionId) throws CloudManagerApiException {
    cancel(getExecution(programId, pipelineId, executionId));
  }

  @Override
  public void cancelExecution(PipelineExecution execution) throws CloudManagerApiException {
    cancelExecution(execution.getProgramId(), execution.getPipelineId(), execution.getId());
  }


  @Override
  public String getExecutionStepLogDownloadUrl(String programId, String pipelineId, String executionId, StepAction action) throws CloudManagerApiException {
    return getExecutionStepLogDownloadUrl(programId, pipelineId, executionId, action, null);
  }

  @Override
  public String getExecutionStepLogDownloadUrl(String programId, String pipelineId, String executionId, StepAction action, String name) throws CloudManagerApiException {
    PipelineExecutionImpl actual = getExecution(programId, pipelineId, executionId);
    return getExecutionStepLogDownloadUrl(actual, action, name);
  }

  @Override
  public String getExecutionStepLogDownloadUrl(PipelineExecution execution, StepAction action) throws CloudManagerApiException {
    return getExecutionStepLogDownloadUrl(execution.getProgramId(), execution.getPipelineId(), execution.getId(), action);
  }

  @Override
  public String getExecutionStepLogDownloadUrl(PipelineExecution execution, StepAction action, String name) throws CloudManagerApiException {
    return getExecutionStepLogDownloadUrl(execution.getProgramId(), execution.getPipelineId(), execution.getId(), action, name);
  }

  @Override
  public Collection<Metric> getQualityGateResults(PipelineExecution execution, StepAction action) throws CloudManagerApiException {
    PipelineExecutionStepStateImpl step = getExecutionStepState(execution, action);
    PipelineStepMetrics psm = executionApi.getStepMetrics(execution.getProgramId(), execution.getPipelineId(), execution.getId(), step.getPhaseId(), step.getStepId());
    return psm.getMetrics().isEmpty() ?
        Collections.emptyList() :
        psm.getMetrics().stream().map(MetricImpl::new).collect(Collectors.toList());
  }

  @Override
  public Collection<PipelineExecution> listExecutions(String programId, String pipelineId) throws CloudManagerApiException {
    PipelineExecutionListRepresentation list = executionApi.list(programId, pipelineId);

    return list.getEmbedded() == null ?
        Collections.emptyList() :
        list.getEmbedded().getExecutions().stream().map(pe -> new PipelineExecutionImpl(pe, this)).collect(Collectors.toList());
  }

  @Override
  public Collection<PipelineExecution> listExecutions(Pipeline pipeline) throws CloudManagerApiException {
    return listExecutions(pipeline.getProgramId(), pipeline.getId());
  }

  @Override
  public Collection<PipelineExecution> listExecutions(String programId, String pipelineId, int limit) throws CloudManagerApiException {
    return listExecutions(programId, pipelineId, 0, limit);
  }

  @Override
  public Collection<PipelineExecution> listExecutions(Pipeline pipeline, int limit) throws CloudManagerApiException {
    return listExecutions(pipeline.getProgramId(), pipeline.getId(), limit);
  }

  @Override
  public Collection<PipelineExecution> listExecutions(String programId, String pipelineId, int start, int limit) throws CloudManagerApiException {
    PipelineExecutionListRepresentation list = executionApi.list(programId, pipelineId, start, limit);
    return list.getEmbedded() == null ?
        Collections.emptyList() :
        list.getEmbedded().getExecutions().stream().map(pe -> new PipelineExecutionImpl(pe, this)).collect(Collectors.toList());
  }

  @Override
  public Collection<PipelineExecution> listExecutions(Pipeline pipeline, int start, int limit) throws CloudManagerApiException {
    return listExecutions(pipeline.getProgramId(), pipeline.getId(), start, limit);
  }

  @Override
  public Collection<Tenant> listTenants() throws CloudManagerApiException {
    TenantList tenantList = tenantApi.list();
    return tenantList.getEmbedded() == null ?
        Collections.emptyList() :
        tenantList.getEmbedded().getTenants().stream().map(t -> new TenantImpl(t, this)).collect(Collectors.toList());
  }

  @Override
  public Tenant getTenant(String tenantId) throws CloudManagerApiException {
    return new TenantImpl(tenantApi.get(tenantId), this);
  }

  // Non-API convenience methods.

  // Below are still in work.
  @Override
  public PipelineExecution getExecution(PipelineExecutionStartEvent event) throws CloudManagerApiException {
    return null;
  }

  @Override
  public PipelineExecution getExecution(PipelineExecutionEndEvent event) throws CloudManagerApiException {
    return null;
  }

  @Override
  public boolean isExecutionRunning(PipelineExecution execution) throws CloudManagerApiException {
    return false;
  }

  @Override
  public boolean isExecutionRunning(String programId, String pipelineId, String executionId) throws CloudManagerApiException {
    return false;
  }

  @Override
  public PipelineExecutionStepState getExecutionStepState(PipelineExecutionStepStartEvent event) throws CloudManagerApiException {
    return null;
  }

  @Override
  public PipelineExecutionStepState getExecutionStepState(PipelineExecutionStepWaitingEvent event) throws CloudManagerApiException {
    return null;
  }

  @Override
  public PipelineExecutionStepState getExecutionStepState(PipelineExecutionStepEndEvent event) throws CloudManagerApiException {
    return null;
  }

  @Override
  public PipelineExecutionStepStateImpl getCurrentStep(PipelineExecution execution) throws CloudManagerApiException {
    return null;
  }

  @Override
  public void advanceCurrentExecution(String programId, String pipelineId) throws CloudManagerApiException {

  }

  @Override
  public void cancelCurrentExecution(String programId, String pipelineId) throws CloudManagerApiException {

  }


  @Override
  public void downloadExecutionStepLog(String programId, String pipelineId, String executionId, String action, OutputStream outputStream) throws CloudManagerApiException {

  }

  @Override
  public void downloadExecutionStepLog(String programId, String pipelineId, String executionId, String action, String name, OutputStream outputStream) throws CloudManagerApiException {

  }

  @Override
  public void downloadExecutionStepLog(PipelineExecution execution, String action, OutputStream outputStream) throws CloudManagerApiException {

  }

  @Override
  public void downloadExecutionStepLog(PipelineExecution execution, String action, String filename, OutputStream outputStream) throws CloudManagerApiException {

  }

  @Override
  public Collection<Environment> listEnvironments(String programId) throws CloudManagerApiException {
    return null;
  }

  @Override
  public void deleteEnvironment(String programId, String environmentId) throws CloudManagerApiException {

  }

  @Override
  public void deleteEnvironment(Environment environment) throws CloudManagerApiException {

  }

  @Override
  public Collection<EnvironmentLog> downloadLogs(String programId, String environmentId, LogOption logOption, int days, File dir) throws CloudManagerApiException {
    return null;
  }

  @Override
  public Collection<EnvironmentLog> downloadLogs(Environment environment, LogOption logOption, int days, File dir) throws CloudManagerApiException {
    return null;
  }

  @Override
  public Set<Variable> listEnvironmentVariables(String programId, String environmentId) throws CloudManagerApiException {
    return null;
  }

  @Override
  public Set<Variable> listEnvironmentVariables(Environment environment) throws CloudManagerApiException {
    return null;
  }

  @Override
  public Set<Variable> setEnvironmentVariables(String programId, String environmentId, Variable... variables) throws CloudManagerApiException {
    return null;
  }

  @Override
  public Set<Variable> setEnvironmentVariables(Environment environment, Variable... variables) throws CloudManagerApiException {
    return null;
  }

  @Override
  public Set<Variable> listPipelineVariables(String programId, String pipelineId) throws CloudManagerApiException {
    return null;
  }

  @Override
  public Set<Variable> listPipelineVariables(Pipeline pipeline) throws CloudManagerApiException {
    return null;
  }

  @Override
  public Set<Variable> setPipelineVariables(String programId, String pipelineId, Variable... variables) throws CloudManagerApiException {
    return null;
  }

  @Override
  public Set<Variable> setPipelineVariables(Pipeline pipeline, Variable... variables) throws CloudManagerApiException {
    return null;
  }

  // Helper Methods

  void advance(PipelineExecutionImpl execution) throws CloudManagerApiException {
    PipelineExecutionStepStateImpl waitingStep = getWaitingStep(execution);
    executionApi.advance(execution.getProgramId(), execution.getPipelineId(), execution.getId(), waitingStep.getPhaseId(), waitingStep.getStepId(), waitingStep.getAdvanceBody());
  }

  void cancel(PipelineExecutionImpl execution) throws CloudManagerApiException {
    final String err = String.format("Cannot find a cancelable step for pipeline %s, execution %s.", execution.getPipelineId(), execution.getId());
    PipelineExecutionStepStateImpl step;
    try {
      step = getStep(execution, PipelineExecutionStepStateImpl.IS_RUNNING, err);
    } catch (CloudManagerApiException ex) {
      step = getStep(execution, PipelineExecutionStepStateImpl.IS_WAITING, err);
    }
    executionApi.cancel(execution.getProgramId(), execution.getPipelineId(), execution.getId(), step.getPhaseId(), step.getStepId(), step.getCancelBody());
  }

  String getExecutionStepLogDownloadUrl(PipelineExecutionImpl execution, StepAction action, String file) throws CloudManagerApiException {
    PipelineExecutionStepStateImpl step = getExecutionStepState(execution, action);
    Redirect redirect;
    if (StringUtils.isBlank(file)) {
      redirect = executionApi.getLogs(execution.getProgramId(), execution.getPipelineId(), execution.getId(), step.getPhaseId(), step.getStepId());
    } else {
      redirect = executionApi.getLogs(execution.getProgramId(), execution.getPipelineId(), execution.getId(), step.getPhaseId(), step.getStepId(), file);
    }
    if (redirect != null && StringUtils.isNotBlank(redirect.getRedirect())) {
      return redirect.getRedirect();
    }
    throw new CloudManagerApiException(String.format(NO_LOG_REDIRECT_ERROR, execution.getId(), action.name()));
  }

  private Collection<Pipeline> listPipelineDetails(String programId, Predicate<Pipeline> predicate) throws CloudManagerApiException {
    PipelineList list = pipelineApi.list(programId);
    if (list == null || list.getEmbedded() == null || list.getEmbedded().getPipelines() == null) {
      throw new CloudManagerApiException(String.format(PipelineExceptionDecoder.ErrorType.FIND_PIPELINES.getMessage(), programId));
    }

    return list.getEmbedded().getPipelines().stream().map(p -> new PipelineImpl(p, this)).filter(predicate).collect(Collectors.toList());
  }


  private PipelineExecutionStepStateImpl getExecutionStepState(PipelineExecutionImpl execution, StepAction action) throws CloudManagerApiException {
    return getStep(execution,
        s -> s.getAction().equals(action.name()),
        String.format(PipelineExecutionExceptionDecoder.ErrorType.FIND_STEP_STATE.getMessage(), action, execution.getId()));
  }

  private PipelineExecutionStepStateImpl getWaitingStep(PipelineExecutionImpl execution) throws CloudManagerApiException {
    return getStep(execution, PipelineExecutionStepStateImpl.IS_WAITING, String.format("Cannot find a waiting step for pipeline %s, execution %s.", execution.getPipelineId(), execution.getId()));
  }

  private PipelineExecutionStepStateImpl getStep(
      PipelineExecutionImpl actual,
      Predicate<io.adobe.cloudmanager.impl.generated.PipelineExecutionStepState> predicate,
      String errorMessage) throws CloudManagerApiException {
    PipelineExecutionEmbedded embeddeds = actual.getEmbedded();
    CloudManagerApiException potential = new CloudManagerApiException(errorMessage);
    if (embeddeds == null || embeddeds.getStepStates() == null || embeddeds.getStepStates().isEmpty()) {
      throw potential;
    }
    io.adobe.cloudmanager.impl.generated.PipelineExecutionStepState step = embeddeds.getStepStates()
        .stream()
        .filter(predicate)
        .findFirst()
        .orElseThrow(() -> potential);
    return new PipelineExecutionStepStateImpl(step, actual, this);
  }
}
