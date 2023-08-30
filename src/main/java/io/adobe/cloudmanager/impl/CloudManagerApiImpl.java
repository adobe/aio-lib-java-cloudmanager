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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
import io.adobe.cloudmanager.Tenant;
import io.adobe.cloudmanager.Variable;
import io.adobe.cloudmanager.event.PipelineExecutionEndEvent;
import io.adobe.cloudmanager.event.PipelineExecutionStartEvent;
import io.adobe.cloudmanager.event.PipelineExecutionStepEndEvent;
import io.adobe.cloudmanager.event.PipelineExecutionStepStartEvent;
import io.adobe.cloudmanager.event.PipelineExecutionStepWaitingEvent;
import io.adobe.cloudmanager.impl.client.FeignProgramApi;
import io.adobe.cloudmanager.impl.client.FeignRepositoryApi;
import io.adobe.cloudmanager.impl.client.FeignTenantApi;
import io.adobe.cloudmanager.impl.exception.ProgramExceptionDecoder;
import io.adobe.cloudmanager.impl.exception.RepositoryExceptionDecoder;
import io.adobe.cloudmanager.impl.exception.TenantExceptionDecoder;
import io.adobe.cloudmanager.impl.generated.BranchList;
import io.adobe.cloudmanager.impl.generated.EmbeddedProgram;
import io.adobe.cloudmanager.impl.generated.ProgramList;
import io.adobe.cloudmanager.impl.generated.RepositoryBranch;
import io.adobe.cloudmanager.impl.generated.RepositoryList;
import io.adobe.cloudmanager.impl.generated.TenantList;
import lombok.NonNull;

import static com.adobe.aio.util.feign.FeignUtil.*;

public class CloudManagerApiImpl implements CloudManagerApi {

  private final FeignTenantApi tenantApi;
  private final FeignProgramApi programApi;
  private final FeignRepositoryApi repositoryApi;

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
  }

  @Override
  public Program getProgram(String programId) throws CloudManagerApiException {
    EmbeddedProgram program = programApi.get(programId);
    return new ProgramImpl(program,this);
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
  public @NonNull Collection<Repository> listRepositories(@NonNull String programId, int limit) throws CloudManagerApiException {
    return listRepositories(programId, 0, limit);
  }

  @Override
  public @NonNull Collection<Repository> listRepositories(@NonNull Program program, int limit) throws CloudManagerApiException {
    return listRepositories(program.getId(), 0, limit);
  }

  @Override
  public @NonNull Collection<Repository> listRepositories(@NonNull String programId, int start, int limit) throws CloudManagerApiException {
    Map<String, Object> params = new HashMap<>();
    params.put(FeignRepositoryApi.START_PARAM, start);
    params.put(FeignRepositoryApi.LIMIT_PARAM, limit);
    RepositoryList list = repositoryApi.list(programId, params);
    return list.getEmbedded() == null ?
        Collections.emptyList() :
        list.getEmbedded().getRepositories().stream().map(r -> new RepositoryImpl(r, this)).collect(Collectors.toList());
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
    BranchList list = repositoryApi.listBranches(repository.getProgramId(), repository.getId());
    return list.getEmbedded() == null ?
        Collections.emptyList() :
        list.getEmbedded().getBranches().stream().map(RepositoryBranch::getName).collect(Collectors.toList());
  }

  @Override
  public Collection<Pipeline> listPipelines(String programId) throws CloudManagerApiException {
    return null;
  }

  @Override
  public Collection<Pipeline> listPipelines(String programId, Predicate<Pipeline> predicate) throws CloudManagerApiException {
    return null;
  }

  @Override
  public void deletePipeline(String programId, String pipelineId) throws CloudManagerApiException {

  }

  @Override
  public void deletePipeline(Pipeline pipeline) throws CloudManagerApiException {

  }

  @Override
  public Pipeline updatePipeline(String programId, String pipelineId, PipelineUpdate updates) throws CloudManagerApiException {
    return null;
  }

  @Override
  public Pipeline updatePipeline(Pipeline pipeline, PipelineUpdate updates) throws CloudManagerApiException {
    return null;
  }

  @Override
  public Optional<PipelineExecution> getCurrentExecution(String programId, String pipelineId) throws CloudManagerApiException {
    return Optional.empty();
  }

  @Override
  public PipelineExecution startExecution(String programId, String pipelineId) throws CloudManagerApiException {
    return null;
  }

  @Override
  public PipelineExecution startExecution(Pipeline pipeline) throws CloudManagerApiException {
    return null;
  }

  @Override
  public PipelineExecution getExecution(Pipeline pipeline, String executionId) throws CloudManagerApiException {
    return null;
  }

  @Override
  public PipelineExecution getExecution(String programId, String pipelineId, String executionId) throws CloudManagerApiException {
    return null;
  }

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
  public PipelineExecutionStepState getExecutionStepState(PipelineExecution execution, String action) throws CloudManagerApiException {
    return null;
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
  public PipelineExecutionStepStateImpl getWaitingStep(PipelineExecution execution) throws CloudManagerApiException {
    return null;
  }

  @Override
  public void advanceExecution(String programId, String pipelineId, String executionId) throws CloudManagerApiException {

  }

  @Override
  public void advanceExecution(PipelineExecution execution) throws CloudManagerApiException {

  }

  @Override
  public void advanceCurrentExecution(String programId, String pipelineId) throws CloudManagerApiException {

  }

  @Override
  public void cancelExecution(String programId, String pipelineId, String executionId) throws CloudManagerApiException {

  }

  @Override
  public void cancelExecution(PipelineExecution execution) throws CloudManagerApiException {

  }

  @Override
  public void cancelCurrentExecution(String programId, String pipelineId) throws CloudManagerApiException {

  }

  @Override
  public String getExecutionStepLogDownloadUrl(String programId, String pipelineId, String executionId, String action) throws CloudManagerApiException {
    return null;
  }

  @Override
  public String getExecutionStepLogDownloadUrl(String programId, String pipelineId, String executionId, String action, String name) throws CloudManagerApiException {
    return null;
  }

  @Override
  public String getExecutionStepLogDownloadUrl(PipelineExecution execution, String action) throws CloudManagerApiException {
    return null;
  }

  @Override
  public String getExecutionStepLogDownloadUrl(PipelineExecution execution, String action, String name) throws CloudManagerApiException {
    return null;
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
  public Collection<Metric> getQualityGateResults(PipelineExecution execution, String action) throws CloudManagerApiException {
    return null;
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

  protected PipelineExecutionImpl getExecution(PipelineExecutionStepStateImpl step) throws CloudManagerApiException {
    return null;
  }

  protected void downloadExecutionStepLog(PipelineExecutionStepStateImpl step, String filename, OutputStream outputStream) throws CloudManagerApiException {
  }

}
