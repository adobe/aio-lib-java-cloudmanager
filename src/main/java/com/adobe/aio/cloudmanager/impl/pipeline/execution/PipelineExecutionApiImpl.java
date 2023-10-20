package com.adobe.aio.cloudmanager.impl.pipeline.execution;

/*-
 * #%L
 * Adobe Cloud Manager Client Library
 * %%
 * Copyright (C) 2020 - 2023 Adobe Inc.
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

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;

import com.adobe.aio.cloudmanager.Artifact;
import com.adobe.aio.cloudmanager.CloudManagerApiException;
import com.adobe.aio.cloudmanager.Constants;
import com.adobe.aio.cloudmanager.Metric;
import com.adobe.aio.cloudmanager.Pipeline;
import com.adobe.aio.cloudmanager.PipelineExecution;
import com.adobe.aio.cloudmanager.PipelineExecutionEvent;
import com.adobe.aio.cloudmanager.PipelineExecutionStepState;
import com.adobe.aio.cloudmanager.StepAction;
import com.adobe.aio.cloudmanager.impl.FeignUtil;
import com.adobe.aio.cloudmanager.impl.MetricImpl;
import com.adobe.aio.cloudmanager.impl.generated.event.PipelineExecutionEndEvent;
import com.adobe.aio.cloudmanager.impl.generated.event.PipelineExecutionStartEvent;
import com.adobe.aio.cloudmanager.impl.generated.event.PipelineExecutionStepEndEvent;
import com.adobe.aio.cloudmanager.impl.generated.event.PipelineExecutionStepStartEvent;
import com.adobe.aio.cloudmanager.impl.generated.event.PipelineExecutionStepWaitingEvent;
import com.adobe.aio.event.webhook.service.EventVerifier;
import com.adobe.aio.workspace.Workspace;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import feign.Body;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import com.adobe.aio.cloudmanager.PipelineExecutionApi;
import com.adobe.aio.cloudmanager.impl.generated.ArtifactList;
import com.adobe.aio.cloudmanager.impl.generated.PipelineExecutionEmbedded;
import com.adobe.aio.cloudmanager.impl.generated.PipelineExecutionListRepresentation;
import com.adobe.aio.cloudmanager.impl.generated.PipelineStepMetrics;
import com.adobe.aio.cloudmanager.impl.generated.Redirect;

public class PipelineExecutionApiImpl implements PipelineExecutionApi {

  private static final String STARTED_EVENT_TYPE = "https://ns.adobe.com/experience/cloudmanager/event/started";
  private static final String WAITING_EVENT_TYPE = "https://ns.adobe.com/experience/cloudmanager/event/waiting";
  private static final String ENDED_EVENT_TYPE = "https://ns.adobe.com/experience/cloudmanager/event/ended";
  private static final String PIPELINE_EXECUTION_TYPE = "https://ns.adobe.com/experience/cloudmanager/pipeline-execution";
  private static final String PIPELINE_STEP_STATE_TYPE = "https://ns.adobe.com/experience/cloudmanager/execution-step-state";
  private static final String EXECUTION_LOG_REDIRECT_ERROR = "Log redirect for execution %s, action '%s' did not exist.";
  private static final String ARTIFACT_REDIRECT_ERROR = "Artifact redirect for execution %s, phase %s, step %s did not exist.";

  private final Workspace workspace;
  private final EventVerifier verifier;
  private final ObjectMapper mapper;
  private final FeignApi api;

  public PipelineExecutionApiImpl(Workspace workspace, URL url) {
    this.workspace = workspace;
    mapper = FeignUtil.getMapper();
    verifier = new EventVerifier();
    String baseUrl = url == null ? Constants.CLOUD_MANAGER_URL : url.toString();
    api = FeignUtil.getBuilder(workspace).errorDecoder(new ExceptionDecoder()).target(FeignApi.class, baseUrl);
  }

  @Override
  public Optional<PipelineExecution> getCurrent(String programId, String pipelineId) throws CloudManagerApiException {
    try {
      com.adobe.aio.cloudmanager.impl.generated.PipelineExecution current = api.current(programId, pipelineId);
      return Optional.of(new PipelineExecutionImpl(current, this));
    } catch (CurrentNotFoundException ex) {
      return Optional.empty();
    }
  }

  @Override
  public Optional<PipelineExecution> getCurrent(Pipeline pipeline) throws CloudManagerApiException {
    return getCurrent(pipeline.getProgramId(), pipeline.getId());
  }

  @Override
  public PipelineExecution start(String programId, String pipelineId) throws CloudManagerApiException {
    return new PipelineExecutionImpl(api.start(programId, pipelineId), this);
  }

  @Override
  public PipelineExecution start(Pipeline pipeline) throws CloudManagerApiException {
    return start(pipeline.getProgramId(), pipeline.getId());
  }

  @Override
  public PipelineExecutionImpl get(String programId, String pipelineId, String executionId) throws CloudManagerApiException {
    return new PipelineExecutionImpl(api.get(programId, pipelineId, executionId), this);
  }

  @Override
  public PipelineExecutionImpl get(Pipeline pipeline, String executionId) throws CloudManagerApiException {
    return get(pipeline.getProgramId(), pipeline.getId(), executionId);
  }

  @Override
  public PipelineExecutionStepStateImpl getStepState(PipelineExecution execution, StepAction action) throws CloudManagerApiException {
    PipelineExecutionImpl actual = get(execution.getProgramId(), execution.getPipelineId(), execution.getId());
    return getStepStateDetail(actual, action);
  }

  @Override
  public void advance(String programId, String pipelineId, String executionId) throws CloudManagerApiException {
    internalAdvance(get(programId, pipelineId, executionId));
  }

  @Override
  public void advance(PipelineExecution execution) throws CloudManagerApiException {
    advance(execution.getProgramId(), execution.getPipelineId(), execution.getId());
  }

  @Override
  public void cancel(String programId, String pipelineId, String executionId) throws CloudManagerApiException {
    internalCancel(get(programId, pipelineId, executionId));
  }

  @Override
  public void cancel(PipelineExecution execution) throws CloudManagerApiException {
    cancel(execution.getProgramId(), execution.getPipelineId(), execution.getId());
  }

  @Override
  public String getStepLogDownloadUrl(String programId, String pipelineId, String executionId, StepAction action) throws CloudManagerApiException {
    return getStepLogDownloadUrl(programId, pipelineId, executionId, action, null);
  }

  @Override
  public String getStepLogDownloadUrl(String programId, String pipelineId, String executionId, StepAction action, String name) throws CloudManagerApiException {
    PipelineExecutionImpl actual = get(programId, pipelineId, executionId);
    return getStepLogDownloadUrlDetail(actual, action, name);
  }

  @Override
  public String getStepLogDownloadUrl(PipelineExecution execution, StepAction action) throws CloudManagerApiException {
    return getStepLogDownloadUrl(execution.getProgramId(), execution.getPipelineId(), execution.getId(), action);
  }

  @Override
  public String getStepLogDownloadUrl(PipelineExecution execution, StepAction action, String name) throws CloudManagerApiException {
    return getStepLogDownloadUrl(execution.getProgramId(), execution.getPipelineId(), execution.getId(), action, name);
  }

  @Override
  public Collection<Metric> getQualityGateResults(PipelineExecution execution, StepAction action) throws CloudManagerApiException {
    PipelineExecutionStepStateImpl step = getStepState(execution, action);
    PipelineStepMetrics psm = api.getStepMetrics(execution.getProgramId(), execution.getPipelineId(), execution.getId(), step.getPhaseId(), step.getStepId());
    return psm.getMetrics() == null || psm.getMetrics().isEmpty() ?
        Collections.emptyList() :
        psm.getMetrics().stream().map(MetricImpl::new).collect(Collectors.toList());
  }

  @Override
  public Collection<PipelineExecution> list(String programId, String pipelineId) throws CloudManagerApiException {
    PipelineExecutionListRepresentation list = api.list(programId, pipelineId);

    return list.getEmbedded() == null || list.getEmbedded().getExecutions() == null ?
        Collections.emptyList() :
        list.getEmbedded().getExecutions().stream().map(pe -> new PipelineExecutionImpl(pe, this)).collect(Collectors.toList());
  }

  @Override
  public Collection<PipelineExecution> list(Pipeline pipeline) throws CloudManagerApiException {
    return list(pipeline.getProgramId(), pipeline.getId());
  }

  @Override
  public Collection<PipelineExecution> list(String programId, String pipelineId, int limit) throws CloudManagerApiException {
    return list(programId, pipelineId, 0, limit);
  }

  @Override
  public Collection<PipelineExecution> list(Pipeline pipeline, int limit) throws CloudManagerApiException {
    return list(pipeline.getProgramId(), pipeline.getId(), limit);
  }

  @Override
  public Collection<PipelineExecution> list(String programId, String pipelineId, int start, int limit) throws CloudManagerApiException {
    PipelineExecutionListRepresentation list = api.list(programId, pipelineId, start, limit);
    return list.getEmbedded() == null || list.getEmbedded().getExecutions() == null ?
        Collections.emptyList() :
        list.getEmbedded().getExecutions().stream().map(pe -> new PipelineExecutionImpl(pe, this)).collect(Collectors.toList());
  }

  @Override
  public Collection<PipelineExecution> list(Pipeline pipeline, int start, int limit) throws CloudManagerApiException {
    return list(pipeline.getProgramId(), pipeline.getId(), start, limit);
  }

  @Override
  public Collection<Artifact> listArtifacts(PipelineExecutionStepState step) throws CloudManagerApiException {
    ArtifactList list = api.listArtifacts(step.getExecution().getProgramId(), step.getExecution().getPipelineId(), step.getExecution().getId(), step.getPhaseId(), step.getStepId());
    return list.getEmbedded() == null || list.getEmbedded().getArtifacts() == null ?
        Collections.emptyList() :
        list.getEmbedded().getArtifacts().stream().map(a -> new ArtifactImpl(a, this, step)).collect(Collectors.toList());
  }

  @Override
  public String getArtifactDownloadUrl(PipelineExecutionStepState step, String artifactId) throws CloudManagerApiException {
    Redirect redirect = api.getArtifact(step.getExecution().getProgramId(), step.getExecution().getPipelineId(), step.getExecution().getId(), step.getPhaseId(), step.getStepId(), artifactId);
    if (redirect != null && StringUtils.isNotBlank(redirect.getRedirect())) {
      return redirect.getRedirect();
    }
    throw new CloudManagerApiException(String.format(ARTIFACT_REDIRECT_ERROR, step.getExecution().getId(), step.getPhaseId(), step.getStepId()));
  }

  @Override
  public PipelineExecutionEvent parseEvent(String eventBody) throws CloudManagerApiException {
    String eventType = JsonPath.read(eventBody, "$.event.@type");
    String objType = JsonPath.read(eventBody, "$.event.xdmEventEnvelope:objectType");
    PipelineExecutionEvent event;
    try {
      if (Objects.equals(objType, PIPELINE_EXECUTION_TYPE) && Objects.equals(eventType, STARTED_EVENT_TYPE)) {
        event = new PipelineExecutionStartEventImpl(mapper.readValue(eventBody, PipelineExecutionStartEvent.class).getEvent(), this);
      } else if (Objects.equals(objType, PIPELINE_EXECUTION_TYPE) && Objects.equals(eventType, ENDED_EVENT_TYPE)) {
        event = new PipelineExecutionEndEventImpl(mapper.readValue(eventBody, PipelineExecutionEndEvent.class).getEvent(), this);
      } else if (Objects.equals(objType, PIPELINE_STEP_STATE_TYPE) && Objects.equals(eventType, STARTED_EVENT_TYPE)) {
        event = new PipelineExecutionStepStartEventImpl(mapper.readValue(eventBody, PipelineExecutionStepStartEvent.class).getEvent(), this);
      } else if (Objects.equals(objType, PIPELINE_STEP_STATE_TYPE) && Objects.equals(eventType, WAITING_EVENT_TYPE)) {
        event = new PipelineExecutionStepWaitingEventImpl(mapper.readValue(eventBody, PipelineExecutionStepWaitingEvent.class).getEvent(), this);
      } else if (Objects.equals(objType, PIPELINE_STEP_STATE_TYPE) && Objects.equals(eventType, ENDED_EVENT_TYPE)) {
        event = new PipelineExecutionStepEndEventImpl(mapper.readValue(eventBody, PipelineExecutionStepEndEvent.class).getEvent(), this);
      } else {
        throw new CloudManagerApiException(String.format("Unknown event/object types (Event: '%s', Object: '%s').", eventType, objType));
      }
    } catch (JsonProcessingException e) {
      throw new CloudManagerApiException(String.format("Unable to process event: %s", e.getLocalizedMessage()));
    }

    return event;
  }

  @Override
  public PipelineExecutionEvent parseEvent(String eventBody, Map<String, String> requestHeaders) throws CloudManagerApiException {
    if (!verifier.verify(eventBody, workspace.getApiKey(), requestHeaders)) {
      throw new CloudManagerApiException("Cannot parse event, did not pass signature validation.");
    }
    return parseEvent(eventBody);
  }

  // Helper methods.

  void internalAdvance(PipelineExecutionImpl execution) throws CloudManagerApiException {
    PipelineExecutionStepStateImpl waitingStep = getWaitingStep(execution);
    api.advance(execution.getProgramId(), execution.getPipelineId(), execution.getId(), waitingStep.getPhaseId(), waitingStep.getStepId(), waitingStep.getAdvanceBody());
  }

  void internalCancel(PipelineExecutionImpl execution) throws CloudManagerApiException {
    final String err = String.format("Cannot find a cancelable step for pipeline %s, execution %s.", execution.getPipelineId(), execution.getId());
    PipelineExecutionStepStateImpl step;
    try {
      step = getStep(execution, PipelineExecutionStepStateImpl.IS_RUNNING, err);
    } catch (CloudManagerApiException ex) {
      step = getStep(execution, PipelineExecutionStepStateImpl.IS_WAITING, err);
    }
    api.cancel(execution.getProgramId(), execution.getPipelineId(), execution.getId(), step.getPhaseId(), step.getStepId(), step.getCancelBody());
  }

  @NotNull
  PipelineExecutionImpl get(com.adobe.aio.cloudmanager.impl.generated.event.PipelineExecution pe) throws CloudManagerApiException {
    String path = pe.getAtId();
    Matcher matcher = Pattern.compile("^.*(/api.*)$").matcher(path);
    if (!matcher.matches()) {
      throw new CloudManagerApiException(String.format("Unable to parse Event Object ID: %s.", path));
    }
    return new PipelineExecutionImpl(api.get(matcher.group(1)), this);
  }

  @NotNull
  PipelineExecutionStepStateImpl getStepState(com.adobe.aio.cloudmanager.impl.generated.event.PipelineExecutionStepState pes) throws CloudManagerApiException {
    String path = pes.getAtId();
    Matcher matcher = Pattern.compile("^.*(/api.*)$").matcher(path);
    if (!matcher.matches()) {
      throw new CloudManagerApiException(String.format("Unable to parse Event Object ID: %s.", path));
    }

    com.adobe.aio.cloudmanager.impl.generated.PipelineExecutionStepState delegate = api.getStepState(matcher.group(1));
    PipelineExecution execution = new PipelineExecutionImpl(api.get(delegate.getLinks().getHttpnsAdobeComadobecloudrelexecution().getHref()), this);

    return new PipelineExecutionStepStateImpl(delegate, execution, this);
  }

  String getStepLogDownloadUrlDetail(PipelineExecutionImpl execution, StepAction action, String file) throws CloudManagerApiException {
    PipelineExecutionStepStateImpl step = getStepStateDetail(execution, action);
    Redirect redirect;
    if (StringUtils.isBlank(file)) {
      redirect = api.getLogs(execution.getProgramId(), execution.getPipelineId(), execution.getId(), step.getPhaseId(), step.getStepId());
    } else {
      redirect = api.getLogs(execution.getProgramId(), execution.getPipelineId(), execution.getId(), step.getPhaseId(), step.getStepId(), file);
    }
    if (redirect != null && StringUtils.isNotBlank(redirect.getRedirect())) {
      return redirect.getRedirect();
    }
    throw new CloudManagerApiException(String.format(EXECUTION_LOG_REDIRECT_ERROR, execution.getId(), action.name()));
  }

  private PipelineExecutionStepStateImpl getStepStateDetail(PipelineExecutionImpl execution, StepAction action) throws CloudManagerApiException {
    return getStep(execution,
        s -> s.getStepAction() == action,
        String.format("Cannot find step state for action '%s' on execution %s.", action, execution.getId()));
  }

  private PipelineExecutionStepStateImpl getWaitingStep(PipelineExecutionImpl execution) throws CloudManagerApiException {
    return getStep(execution, PipelineExecutionStepStateImpl.IS_WAITING, String.format("Cannot find a waiting step for pipeline %s, execution %s.", execution.getPipelineId(), execution.getId()));
  }

  private PipelineExecutionStepStateImpl getStep(
      PipelineExecutionImpl actual,
      Predicate<PipelineExecutionStepState> predicate,
      String errorMessage) throws CloudManagerApiException {
    PipelineExecutionEmbedded embeddeds = actual.getEmbedded();
    CloudManagerApiException potential = new CloudManagerApiException(errorMessage);
    if (embeddeds == null || embeddeds.getStepStates() == null || embeddeds.getStepStates().isEmpty()) {
      throw potential;
    }
    return embeddeds.getStepStates()
        .stream()
        .map(s -> new PipelineExecutionStepStateImpl(s, actual, this))
        .filter(predicate)
        .findFirst()
        .orElseThrow(() -> potential);
  }

  private interface FeignApi {

    @RequestLine("GET /api/program/{programId}/pipeline/{pipelineId}/execution")
    com.adobe.aio.cloudmanager.impl.generated.PipelineExecution current(@Param("programId") String programId, @Param("pipelineId") String pipelineId) throws CloudManagerApiException;

    @RequestLine("PUT /api/program/{programId}/pipeline/{pipelineId}/execution")
    com.adobe.aio.cloudmanager.impl.generated.PipelineExecution start(@Param("programId") String programId, @Param("pipelineId") String pipelineId) throws CloudManagerApiException;

    @RequestLine("GET /api/program/{programId}/pipeline/{pipelineId}/execution/{id}")
    com.adobe.aio.cloudmanager.impl.generated.PipelineExecution get(@Param("programId") String programId, @Param("pipelineId") String pipelineId, @Param("id") String id) throws CloudManagerApiException;

    @RequestLine("PUT /api/program/{programId}/pipeline/{pipelineId}/execution/{executionId}/phase/{phaseId}/step/{stepId}/advance")
    @Headers("Content-Type: application/json")
    @Body("{body}")
    void advance(@Param("programId") String programId, @Param("pipelineId") String pipelineId, @Param("executionId") String executionId, @Param("phaseId") String phaseId, @Param("stepId") String stepId, @Param("body") String body) throws CloudManagerApiException;

    @RequestLine("PUT /api/program/{programId}/pipeline/{pipelineId}/execution/{executionId}/phase/{phaseId}/step/{stepId}/cancel")
    @Headers("Content-Type: application/json")
    @Body("{body}")
    void cancel(@Param("programId") String programId, @Param("pipelineId") String pipelineId, @Param("executionId") String executionId, @Param("phaseId") String phaseId, @Param("stepId") String stepId, @Param("body") String body) throws CloudManagerApiException;

    @RequestLine("GET /api/program/{programId}/pipeline/{pipelineId}/execution/{executionId}/phase/{phaseId}/step/{stepId}/logs")
    Redirect getLogs(@Param("programId") String programId, @Param("pipelineId") String pipelineId, @Param("executionId") String executionId, @Param("phaseId") String phaseId, @Param("stepId") String stepId) throws CloudManagerApiException;

    @RequestLine("GET /api/program/{programId}/pipeline/{pipelineId}/execution/{executionId}/phase/{phaseId}/step/{stepId}/logs?file={filename}")
    Redirect getLogs(@Param("programId") String programId, @Param("pipelineId") String pipelineId, @Param("executionId") String executionId, @Param("phaseId") String phaseId, @Param("stepId") String stepId, @Param("filename") String filename) throws CloudManagerApiException;

    @RequestLine("GET /api/program/{programId}/pipeline/{pipelineId}/execution/{executionId}/phase/{phaseId}/step/{stepId}/metrics")
    PipelineStepMetrics getStepMetrics(@Param("programId") String programId, @Param("pipelineId") String pipelineId, @Param("executionId") String executionId, @Param("phaseId") String phaseId, @Param("stepId") String stepId) throws CloudManagerApiException;

    @RequestLine("GET /api/program/{programId}/pipeline/{pipelineId}/executions")
    PipelineExecutionListRepresentation list(@Param("programId") String programId, @Param("pipelineId") String pipelineId) throws CloudManagerApiException;

    @RequestLine("GET /api/program/{programId}/pipeline/{pipelineId}/executions?start={start}&limit={limit}")
    PipelineExecutionListRepresentation list(@Param("programId") String programId, @Param("pipelineId") String pipelineId, @Param("start") int start, @Param("limit") int limit) throws CloudManagerApiException;

    @RequestLine("GET /api/program/{programId}/pipeline/{pipelineId}/execution/{executionId}/phase/{phaseId}/step/{stepId}/artifacts")
    ArtifactList listArtifacts(@Param("programId") String programId, @Param("pipelineId") String pipelineId, @Param("executionId") String executionId, @Param("phaseId") String phaseId, @Param("stepId") String stepId) throws CloudManagerApiException;

    @RequestLine("GET /api/program/{programId}/pipeline/{pipelineId}/execution/{executionId}/phase/{phaseId}/step/{stepId}/artifact/{id}")
    Redirect getArtifact(@Param("programId") String programId, @Param("pipelineId") String pipelineId, @Param("executionId") String executionId, @Param("phaseId") String phaseId, @Param("stepId") String stepId, @Param("id") String id) throws CloudManagerApiException;

    @RequestLine("GET {path}")
    com.adobe.aio.cloudmanager.impl.generated.PipelineExecution get(@Param("path") String url) throws CloudManagerApiException;

    @RequestLine("GET {path}")
    com.adobe.aio.cloudmanager.impl.generated.PipelineExecutionStepState getStepState(@Param("path") String url) throws CloudManagerApiException;

  }
}
