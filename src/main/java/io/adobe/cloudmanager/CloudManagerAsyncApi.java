package io.adobe.cloudmanager;

import java.io.File;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.Future;

import io.adobe.cloudmanager.swagger.model.EmbeddedProgram;
import io.adobe.cloudmanager.swagger.model.Environment;
import io.adobe.cloudmanager.swagger.model.LogOptionRepresentation;
import io.adobe.cloudmanager.swagger.model.Pipeline;
import io.adobe.cloudmanager.swagger.model.PipelineExecution;
import io.adobe.cloudmanager.swagger.model.PipelineStepMetrics;
import io.adobe.cloudmanager.swagger.model.Variable;

public interface CloudManagerAsyncApi {

    Future<List<EmbeddedProgram>> listPrograms();

    Future<List<Pipeline>> listPipelines(int programId, boolean busy);

    Future<String> startExecution(int programId, int pipelineId);

    Future<PipelineExecution> getCurrentExecution(int programId, int pipelineId);

    Future<PipelineExecution> getExecution(int programId, int pipelineId, int executionId);

    Future<PipelineStepMetrics> getQualityGateResults(int programId, int pipelineId, int executionId,
                                                      StepStateAction action);

    Future<Void> cancelCurrentExecution(int programId, int pipelineId);

    Future<Void> advanceCurrentExecution(int programId, int pipelineId);

    Future<List<Environment>> listEnvironments(int programId);

    Future<Void> getExecutionStepLog(int programId, int pipelineId, int executionId, StepStateAction action,
                                     String logFile, OutputStream outputStream);

    Future<List<LogOptionRepresentation>> listAvailableLogOptions(int programId, int environmentId);

    Future<List<DownloadedLog>> downloadLogs(int programId, int environmentId, Service service, LogName name, File dir);

    Future<Void> deletePipeline(int programId, int pipelineId);

    Future<Pipeline> updatePipeline(int programId, int pipelineId, PipelineUpdate updates);

    Future<String> getDeveloperConsoleUrl(int programId, int environmentId);

    Future<List<Variable>> getEnvironmentVariables(int programId, int environmentId);

    Future<Void> setEnvironmentVariables(int programId, int environmentId, Variable ...variables);

    Future<List<Variable>> getPipelineVariables(int programId, int pipelineId);

    Future<Void> setPipelineVariables(int programId, int pipelineId, Variable ...variables);

    Future<Void> deleteProgram(int programId);

    Future<Void> deleteEnvironment(int programId, int environmentId);

}
