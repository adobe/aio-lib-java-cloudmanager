package io.adobe.cloudmanager.impl.client;

import feign.Body;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.impl.generated.PipelineExecution;
import io.adobe.cloudmanager.impl.generated.PipelineExecutionListRepresentation;
import io.adobe.cloudmanager.impl.generated.PipelineStepMetrics;
import io.adobe.cloudmanager.impl.generated.Redirect;

public interface FeignPipelineExecutionApi {

  @RequestLine("GET /api/program/{programId}/pipeline/{pipelineId}/execution")
  PipelineExecution current(@Param("programId") String programId, @Param("pipelineId") String pipelineId) throws CloudManagerApiException;

  @RequestLine("PUT /api/program/{programId}/pipeline/{pipelineId}/execution")
  PipelineExecution start(@Param("programId") String programId, @Param("pipelineId") String pipelineId) throws CloudManagerApiException;

  @RequestLine("GET /api/program/{programId}/pipeline/{pipelineId}/execution/{id}")
  PipelineExecution get(@Param("programId") String programId, @Param("pipelineId") String pipelineId, @Param("id") String id) throws CloudManagerApiException;

  @RequestLine("PUT /api/program/{programId}/pipeline/{pipelineId}/execution/{executionId}/phase/{phaseId}/step/{stepId}/advance")
  @Headers("Content-Type: application/json")
  @Body("{body}")
  void advance(@Param("programId") String programId, @Param("pipelineId") String pipelineId, @Param("executionId") String executionId, @Param("phaseId") String phaseId, @Param("stepId") String stepId, @Param("body") String body) throws CloudManagerApiException;

  @RequestLine("PUT /api/program/{programId}/pipeline/{pipelineId}/execution/{executionId}/phase/{phaseId}/step/{stepId}/cancel")
  @Headers("Content-Type: application/json")
  @Body("{body}")
  void cancel(@Param("programId") String programId, @Param("pipelineId") String pipelineId, @Param("executionId") String executionId, @Param("phaseId") String phaseId, @Param("stepId") String stepId, @Param("body") String body) throws CloudManagerApiException;

  @RequestLine("GET /api/program/{programId}/pipeline/{pipelineId}/execution/{executionId}/phase/{phaseId}/step/{stepId}/logs")
  @Headers("Accept: application/json")
  Redirect getLogs(@Param("programId") String programId, @Param("pipelineId") String pipelineId, @Param("executionId") String executionId, @Param("phaseId") String phaseId, @Param("stepId") String stepId) throws CloudManagerApiException;

  @RequestLine("GET /api/program/{programId}/pipeline/{pipelineId}/execution/{executionId}/phase/{phaseId}/step/{stepId}/logs?file={filename}")
  @Headers("Accept: application/json")
  Redirect getLogs(@Param("programId") String programId, @Param("pipelineId") String pipelineId, @Param("executionId") String executionId, @Param("phaseId") String phaseId, @Param("stepId") String stepId, @Param("filename") String filename) throws CloudManagerApiException;

  @RequestLine("GET /api/program/{programId}/pipeline/{pipelineId}/execution/{executionId}/phase/{phaseId}/step/{stepId}/metrics")
  PipelineStepMetrics getStepMetrics(@Param("programId") String programId, @Param("pipelineId") String pipelineId, @Param("executionId") String executionId, @Param("phaseId") String phaseId, @Param("stepId") String stepId) throws CloudManagerApiException;

  @RequestLine("GET /api/program/{programId}/pipeline/{pipelineId}/executions")
  PipelineExecutionListRepresentation list(@Param("programId") String programId, @Param("pipelineId") String pipelineId) throws CloudManagerApiException;

  @RequestLine("GET /api/program/{programId}/pipeline/{pipelineId}/executions?start={start}&limit={limit}")
  PipelineExecutionListRepresentation list(@Param("programId") String programId, @Param("pipelineId") String pipelineId, @Param("start") int start, @Param("limit") int limit) throws CloudManagerApiException;

}
