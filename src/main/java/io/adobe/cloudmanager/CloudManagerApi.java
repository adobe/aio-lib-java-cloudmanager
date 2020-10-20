package io.adobe.cloudmanager;

import java.io.File;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Predicate;

import io.adobe.cloudmanager.model.Pipeline;
import io.adobe.cloudmanager.model.EmbeddedProgram;

public interface CloudManagerApi {

    List<EmbeddedProgram> listPrograms() throws CloudManagerApiException;

    List<Pipeline> listPipelines(String programId) throws CloudManagerApiException;

    List<Pipeline> listPipelines(String programId, Predicate<Pipeline> predicate) throws CloudManagerApiException;

    /*

    Future<String> startExecution(String programId, String pipelineId);

    Future<PipelineExecution> getCurrentExecution(String programId, String pipelineId);

    Future<PipelineExecution> getExecution(String programId, String pipelineId, String executionId);

    Future<PipelineStepMetrics> getQualityGateResults(String programId, String pipelineId, String executionId,
                                                      StepStateAction action);

    Future<Void> cancelCurrentExecution(String programId, String pipelineId);

    Future<Void> advanceCurrentExecution(String programId, String pipelineId);

    Future<List<Environment>> listEnvironments(String programId);

    Future<Void> getExecutionStepLog(String programId, String pipelineId, String executionId, StepStateAction action,
                                     String logFile, OutputStream outputStream);

    Future<List<LogOptionRepresentation>> listAvailableLogOptions(String programId, String environmentId);

    Future<List<DownloadedLog>> downloadLogs(String programId, String environmentId, Service service, LogName name, File dir);

    Future<Void> deletePipeline(String programId, String pipelineId);

    Future<Pipeline> updatePipeline(String programId, String pipelineId, PipelineUpdate updates);

    Future<String> getDeveloperConsoleUrl(String programId, String environmentId);

    Future<List<Variable>> getEnvironmentVariables(String programId, String environmentId);

    Future<Void> setEnvironmentVariables(String programId, String environmentId, Variable ...variables);

    Future<List<Variable>> getPipelineVariables(String programId, String pipelineId);

    Future<Void> setPipelineVariables(String programId, String pipelineId, Variable ...variables);

    Future<Void> deleteProgram(String programId);

    Future<Void> deleteEnvironment(String programId, String environmentId);
*/

}
