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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.ws.rs.core.GenericType;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.adobe.aio.cloudmanager.CloudManagerApi;
import com.adobe.aio.cloudmanager.CloudManagerApiException;
import com.adobe.aio.cloudmanager.Metric;
import com.adobe.aio.cloudmanager.Pipeline;
import com.adobe.aio.cloudmanager.PipelineExecution;
import com.adobe.aio.cloudmanager.PipelineExecutionStepState;
import com.adobe.aio.cloudmanager.PipelineUpdate;
import com.adobe.aio.cloudmanager.Program;
import com.adobe.aio.cloudmanager.Repository;
import com.adobe.aio.cloudmanager.event.PipelineExecutionEndEvent;
import com.adobe.aio.cloudmanager.event.PipelineExecutionStartEvent;
import com.adobe.aio.cloudmanager.event.PipelineExecutionStepEndEvent;
import com.adobe.aio.cloudmanager.event.PipelineExecutionStepStartEvent;
import com.adobe.aio.cloudmanager.event.PipelineExecutionStepWaitingEvent;
import com.adobe.aio.cloudmanager.feign.client.PipelineApiClient;
import com.adobe.aio.cloudmanager.feign.client.PipelineExecutionApiClient;
import com.adobe.aio.cloudmanager.feign.client.ProgramApiClient;
import com.adobe.aio.cloudmanager.feign.exception.PipelineExceptionDecoder;
import com.adobe.aio.cloudmanager.feign.exception.PipelineExecutionExceptionDecoder;
import com.adobe.aio.cloudmanager.generated.model.EmbeddedProgram;
import com.adobe.aio.cloudmanager.generated.model.HalLink;
import com.adobe.aio.cloudmanager.generated.model.PipelineExecutionEmbedded;
import com.adobe.aio.cloudmanager.generated.model.PipelineList;
import com.adobe.aio.cloudmanager.generated.model.PipelinePhase;
import com.adobe.aio.cloudmanager.generated.model.PipelineStepMetrics;
import com.adobe.aio.cloudmanager.generated.model.ProgramList;
import lombok.NonNull;

public class CloudManagerApiImpl implements CloudManagerApi {

  private static final int NOT_FOUND = 404;

  private final ProgramApiClient programApi;
  private final PipelineApiClient pipelineApi;
  private final PipelineExecutionApiClient executionsApi;

  public CloudManagerApiImpl(
      ProgramApiClient programApi,
      PipelineApiClient pipelineApi,
      PipelineExecutionApiClient executionApi) {
    this.programApi = programApi;
    this.pipelineApi = pipelineApi;
    this.executionsApi = executionApi;
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
  public @NonNull Collection<Repository> listRepositories(@NonNull String programId) throws CloudManagerApiException {
    throw new IllegalStateException("Not Implemented");
  }

  @Override
  public @NonNull Collection<Repository> listRepositories(@NonNull Program program) throws CloudManagerApiException {
    throw new IllegalStateException("Not Implemented");
  }

  @Override
  public @NonNull Repository getRepository(@NonNull String programId, @NonNull String repositoryId) throws CloudManagerApiException {
    throw new IllegalStateException("Not Implemented");
  }

  @Override
  public @NonNull Repository getRepository(@NonNull Program program, @NonNull String repositoryId) throws CloudManagerApiException {
    throw new IllegalStateException("Not Implemented");
  }

  @Override
  public void listBranches(@NonNull Repository repository) throws CloudManagerApiException {
    throw new IllegalStateException("Not Implemented");
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
    throw new IllegalStateException("Not Implemented");
  }

  @Override
  public void invalidatePipelineCache(@NonNull Pipeline pipeline) throws CloudManagerApiException {
    throw new IllegalStateException("Not Implemented");
  }

  @Override
  public @NonNull Optional<PipelineExecution> getCurrentExecution(@NonNull String programId, @NonNull String pipelineId) throws CloudManagerApiException {
    try {
      com.adobe.aio.cloudmanager.generated.model.PipelineExecution current = executionsApi.current(programId, pipelineId);
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
  
  protected @NonNull PipelineExecution getExecution(@NonNull PipelineExecutionStepStateImpl step) throws CloudManagerApiException {
    HalLink link = step.getLinks().getHttpnsAdobeComadobecloudrelexecution();
    if (link == null) {
      throw new CloudManagerApiException(String.format(PipelineExecutionExceptionDecoder.ErrorType.FIND_EXECUTION_LINK.getMessage(), step.getLinks().getSelf().getHref()));
    }
    return new PipelineExecutionImpl(executionsApi.get(link.getHref()), this);
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
  
  protected @NonNull PipelineExecutionStepStateImpl getExecutionStepState(@NonNull PipelineExecutionImpl actual, @NonNull String action) throws CloudManagerApiException {
    com.adobe.aio.cloudmanager.generated.model.PipelineExecutionStepState stepState = actual.getEmbedded().getStepStates()
        .stream()
        .filter(s -> s.getAction().equals(action))
        .findFirst()
        .orElseThrow(() -> new CloudManagerApiException(String.format(PipelineExecutionExceptionDecoder.ErrorType.FIND_STEP_STATE.getMessage(), action, actual.getId())));
    return new PipelineExecutionStepStateImpl(stepState, this);
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
  
  protected @NonNull PipelineExecutionStepStateImpl getCurrentStep(@NonNull PipelineExecutionImpl actual) throws CloudManagerApiException {
    PipelineExecutionEmbedded embeddeds = actual.getEmbedded();
    CloudManagerApiException potential = new CloudManagerApiException(String.format(PipelineExecutionExceptionDecoder.ErrorType.FIND_CURRENT_STEP.getMessage(), actual.getPipelineId()));

    if (embeddeds == null || embeddeds.getStepStates().isEmpty()) {
      throw potential;
    }
    com.adobe.aio.cloudmanager.generated.model.PipelineExecutionStepState step = embeddeds.getStepStates()
        .stream()
        .filter(PipelineExecutionStepStateImpl.IS_CURRENT)
        .findFirst()
        .orElseThrow(() -> potential);
    return new PipelineExecutionStepStateImpl(step, this);
  }

  @Override
  public @NonNull PipelineExecutionStepStateImpl getWaitingStep(@NonNull PipelineExecution execution) throws CloudManagerApiException {
    return getWaitingStep(getExecution(execution.getProgramId(), execution.getPipelineId(), execution.getId()));
  }
  
  protected @NonNull PipelineExecutionStepStateImpl getWaitingStep(@NonNull PipelineExecutionImpl actual) throws CloudManagerApiException {
    PipelineExecutionEmbedded embeddeds = actual.getEmbedded();
    CloudManagerApiException potential = new CloudManagerApiException(String.format(PipelineExecutionExceptionDecoder.ErrorType.FIND_WAITING_STEP.getMessage(), actual.getPipelineId()));
    if (embeddeds == null || embeddeds.getStepStates().isEmpty()) {
      throw potential;
    }
    com.adobe.aio.cloudmanager.generated.model.PipelineExecutionStepState step = embeddeds.getStepStates()
        .stream()
        .filter(PipelineExecutionStepStateImpl.IS_WAITING)
        .findFirst()
        .orElseThrow(() -> potential);
    return new PipelineExecutionStepStateImpl(step, this);
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

  protected void advanceExecution(PipelineExecutionImpl execution) throws CloudManagerApiException {
    PipelineExecutionImpl.StepFormData form = execution.getAdvanceLinkAndBody();
    executionsApi.advance(form.getHref(), form.getBody());
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

  protected void cancelExecution(PipelineExecutionImpl execution) throws CloudManagerApiException {
    PipelineExecutionImpl.StepFormData form = execution.getCancelLinkAndBody();
    executionsApi.cancel(form.getHref(), form.getBody());
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

  protected @NonNull String getExecutionStepLogDownloadUrl(PipelineExecutionImpl actual, String action, String name) throws CloudManagerApiException {
    return  getExecutionStepLogDownloadUrl(getExecutionStepState(actual, action), name);
  }

  protected @NonNull String getExecutionStepLogDownloadUrl(PipelineExecutionStepStateImpl stepState, String name) throws CloudManagerApiException {
    Map<String, Object> query = new HashMap<>();
    if (StringUtils.isNotBlank(name)) {
      query.put("file", name);
    }
    HalLink link = stepState.getLinks().getHttpnsAdobeComadobecloudrelpipelinelogs();
    if (link == null || StringUtils.isBlank(link.getHref())) {
      throw new CloudManagerApiException(String.format("Could not find logs link for action '%s'.", stepState.getAction()));
    }
    String url = executionsApi.getLogs(stepState.getLinks().getHttpnsAdobeComadobecloudrelpipelinelogs().getHref(), query).getRedirect();
    if (StringUtils.isBlank(url)) {
      throw new CloudManagerApiException(String.format("Log [%s] did not contain a redirect. Was: %s.", link.getHref(), url));
    }
    return url;
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

  protected void downloadExecutionStepLog(PipelineExecutionStepStateImpl step, String filename, OutputStream os) throws CloudManagerApiException {
    streamLog(os, getExecutionStepLogDownloadUrl(step, filename));
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
  
  @Override
  public @NonNull Collection<Metric> getQualityGateResults(@NonNull PipelineExecution execution, @NonNull String action) throws CloudManagerApiException {
    PipelineExecutionStepStateImpl stepState = getExecutionStepState(execution, action);
    String href = stepState.getLinks().getHttpnsAdobeComadobecloudrelpipelinemetrics().getHref();
    PipelineStepMetrics psm =  executionsApi.getStepMetrics(href);
    return psm.getMetrics().stream().map(MetricImpl::new).collect(Collectors.toList());
  }

  @Override
  public @NonNull Collection<PipelineExecution> listExecutions(@NonNull Pipeline pipeline) throws CloudManagerApiException {
    throw new IllegalStateException("Not Implemented.");
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
  private Pipeline updatePipeline(com.adobe.aio.cloudmanager.generated.model.Pipeline original, PipelineUpdate updates) throws CloudManagerApiException {
    com.adobe.aio.cloudmanager.generated.model.Pipeline toUpdate = new com.adobe.aio.cloudmanager.generated.model.Pipeline();
    PipelinePhase buildPhase = original.getPhases().stream().filter(p -> PipelinePhase.TypeEnum.BUILD == p.getType())
        .findFirst().orElseThrow(() -> new CloudManagerApiException(String.format(PipelineExceptionDecoder.ErrorType.NO_BUILD_PHASE.getMessage(), original.getId())));

    if (updates.getBranch() != null) {
      buildPhase.setBranch(updates.getBranch());
    }
    if (updates.getRepositoryId() != null) {
      buildPhase.setRepositoryId(updates.getRepositoryId());
    }
    toUpdate.getPhases().add(buildPhase);
    com.adobe.aio.cloudmanager.generated.model.Pipeline result = pipelineApi.update(original.getProgramId(), original.getId(), toUpdate);
    return new PipelineImpl(result, this);
  }

  @NonNull
  private PipelineExecution getExecution(String url) throws CloudManagerApiException {
    try {
      String path = new URL(url).getPath();
      return new PipelineExecutionImpl(executionsApi.get(path), this);
    } catch (MalformedURLException e) {
      throw new CloudManagerApiException(String.format("Unable to process event: %s", e.getLocalizedMessage()));
    }
  }
  
  @NonNull
  private PipelineExecutionStepState getExecutionStepState(String url) throws CloudManagerApiException {
    try {
      String path = new URL(url).getPath();
      return new PipelineExecutionStepStateImpl(executionsApi.getStepState(path), this);
    } catch (MalformedURLException e) {
      throw new CloudManagerApiException(String.format("Unable to process event: %s", e.getLocalizedMessage()));
    }
  }
}
