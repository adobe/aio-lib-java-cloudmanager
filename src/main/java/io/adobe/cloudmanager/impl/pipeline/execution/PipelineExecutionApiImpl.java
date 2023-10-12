package io.adobe.cloudmanager.impl.pipeline.execution;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.adobe.aio.workspace.Workspace;
import feign.Body;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import io.adobe.cloudmanager.Artifact;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.Metric;
import io.adobe.cloudmanager.Pipeline;
import io.adobe.cloudmanager.PipelineExecution;
import io.adobe.cloudmanager.PipelineExecutionApi;
import io.adobe.cloudmanager.PipelineExecutionStepState;
import io.adobe.cloudmanager.StepAction;
import io.adobe.cloudmanager.impl.FeignUtil;
import io.adobe.cloudmanager.impl.MetricImpl;
import io.adobe.cloudmanager.impl.generated.ArtifactList;
import io.adobe.cloudmanager.impl.generated.PipelineExecutionEmbedded;
import io.adobe.cloudmanager.impl.generated.PipelineExecutionListRepresentation;
import io.adobe.cloudmanager.impl.generated.PipelineStepMetrics;
import io.adobe.cloudmanager.impl.generated.Redirect;

import static io.adobe.cloudmanager.Constants.*;

public class PipelineExecutionApiImpl implements PipelineExecutionApi {
  private static final String EXECUTION_LOG_REDIRECT_ERROR = "Log redirect for execution %s, action '%s' did not exist.";
  private static final String ARTIFACT_REDIRECT_ERROR = "Artifact redirect for execution %s, phase %s, step %s did not exist.";

  private final FeignApi api;

  public PipelineExecutionApiImpl(Workspace workspace, URL url) {
    String baseUrl = url == null ? CLOUD_MANAGER_URL : url.toString();
    api = FeignUtil.getBuilder(workspace).errorDecoder(new ExceptionDecoder()).target(FeignApi.class, baseUrl);
  }

  @Override
  public Optional<PipelineExecution> getCurrent(String programId, String pipelineId) throws CloudManagerApiException {
    try {
      io.adobe.cloudmanager.impl.generated.PipelineExecution current = api.current(programId, pipelineId);
      return Optional.of(new PipelineExecutionImpl(current, this));
    } catch (CloudManagerApiException ex) {
      if (ex.getErrorCode() == 404) {
        return Optional.empty();
      }
      throw ex;
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
    return psm.getMetrics().isEmpty() ?
        Collections.emptyList() :
        psm.getMetrics().stream().map(MetricImpl::new).collect(Collectors.toList());
  }

  @Override
  public Collection<PipelineExecution> list(String programId, String pipelineId) throws CloudManagerApiException {
    PipelineExecutionListRepresentation list = api.list(programId, pipelineId);

    return list.getEmbedded() == null ?
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
    return list.getEmbedded() == null ?
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
    return list.getEmbedded() == null ?
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
        s -> s.getAction().equals(action.name()),
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
    io.adobe.cloudmanager.impl.generated.PipelineExecution current(@Param("programId") String programId, @Param("pipelineId") String pipelineId) throws CloudManagerApiException;

    @RequestLine("PUT /api/program/{programId}/pipeline/{pipelineId}/execution")
    io.adobe.cloudmanager.impl.generated.PipelineExecution start(@Param("programId") String programId, @Param("pipelineId") String pipelineId) throws CloudManagerApiException;

    @RequestLine("GET /api/program/{programId}/pipeline/{pipelineId}/execution/{id}")
    io.adobe.cloudmanager.impl.generated.PipelineExecution get(@Param("programId") String programId, @Param("pipelineId") String pipelineId, @Param("id") String id) throws CloudManagerApiException;

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

  }
}
