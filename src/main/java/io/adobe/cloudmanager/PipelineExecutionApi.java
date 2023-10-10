package io.adobe.cloudmanager;

import java.net.URL;
import java.util.Collection;
import java.util.Optional;
import javax.validation.constraints.NotNull;

import com.adobe.aio.workspace.Workspace;
import io.adobe.cloudmanager.impl.pipeline.execution.PipelineExecutionApiImpl;

public interface PipelineExecutionApi {

  /**
   * Returns an optional current execution of the specified pipeline.
   *
   * @param programId  the program id context of the pipeline
   * @param pipelineId the pipeline id of to find the execution
   * @return An optional containing the execution details of the pipeline
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Optional<PipelineExecution> getCurrent(@NotNull String programId, @NotNull String pipelineId) throws CloudManagerApiException;

  /**
   * Returns an optional current execution of the specified pipeline.
   *
   * @param pipeline the pipeline reference
   * @return An optional containing the execution details of the pipeline
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Optional<PipelineExecution> getCurrent(@NotNull Pipeline pipeline) throws CloudManagerApiException;

  /**
   * Starts the specified pipeline.
   * <p>
   * Note: This API call may return before the requested action takes effect. i.e. The Pipelines are <i>scheduled</i> to start once called. However, an immediate subsequent call to {@link #getCurrent(String, String)} may not return a result.
   *
   * @param programId  the program id context of the pipeline
   * @param pipelineId the id of the pipeline
   * @return the new execution
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  PipelineExecution start(@NotNull String programId, @NotNull String pipelineId) throws CloudManagerApiException;

  /**
   * Starts the specified pipeline.
   * <p>
   * Note: This API call may return before the requested action takes effect. i.e. The Pipelines are <i>scheduled</i> to start once called. However, an immediate subsequent call to {@link #getCurrent(String, String)} may not return a result.
   *
   * @param pipeline the {@link Pipeline} to start
   * @return the new execution
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  PipelineExecution start(@NotNull Pipeline pipeline) throws CloudManagerApiException;

  /**
   * Returns the specified execution of the pipeline.
   *
   * @param programId   the program id context of the pipeline
   * @param pipelineId  the pipeline id
   * @param executionId the id of the execution to retrieve
   * @return the execution details
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  PipelineExecution get(@NotNull String programId, @NotNull String pipelineId, @NotNull String executionId) throws CloudManagerApiException;

  /**
   * Returns the specified execution of the pipeline.
   *
   * @param pipeline    the pipeline context for the execution
   * @param executionId the id of the execution to retrieve
   * @return the execution details
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  PipelineExecution get(@NotNull Pipeline pipeline, @NotNull String executionId) throws CloudManagerApiException;

  /**
   * Returns the specified action step for the pipeline execution
   *
   * @param execution the execution context
   * @param action    the step state action (see {@link StepAction})
   * @return the step state details
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  PipelineExecutionStepState getStepState(@NotNull PipelineExecution execution, @NotNull StepAction action) throws CloudManagerApiException;

  /**
   * Advances the execution of the specified pipeline execution, if in an appropriate state.
   *
   * @param programId   the program id context of the pipeline
   * @param pipelineId  the id of the pipeline to cancel
   * @param executionId the execution id to be advanced
   * @throws CloudManagerApiException when any error occurs
   */
  void advance(@NotNull String programId, @NotNull String pipelineId, @NotNull String executionId) throws CloudManagerApiException;

  /**
   * Advances the execution of the specified pipeline execution, if in an appropriate state.
   *
   * @param execution the execution to be advanced
   * @throws CloudManagerApiException when any error occurs
   */
  void advance(@NotNull PipelineExecution execution) throws CloudManagerApiException;

  /**
   * Cancels the execution of the specified pipeline execution, if in an appropriate state.
   *
   * @param programId   the program id context of the pipeline
   * @param pipelineId  the id of the pipeline to cancel
   * @param executionId the execution id to be canceled
   * @throws CloudManagerApiException when any error occurs
   */
  void cancel(@NotNull String programId, @NotNull String pipelineId, @NotNull String executionId) throws CloudManagerApiException;

  /**
   * Cancels the execution of the specified pipeline execution, if in an appropriate state.
   *
   * @param execution the execution to be canceled
   * @throws CloudManagerApiException when any error occurs
   */
  void cancel(@NotNull PipelineExecution execution) throws CloudManagerApiException;

  /**
   * Returns the fully qualified URL to the log file for download.
   *
   * @param programId   the program id of the pipeline context
   * @param pipelineId  the pipeline id for the execution context
   * @param executionId the execution id for the logs
   * @param action      the execution step action for the log
   * @return the log file download URL
   * @throws CloudManagerApiException when any error occurs
   */
  String getStepLogDownloadUrl(@NotNull String programId, @NotNull String pipelineId, @NotNull String executionId, @NotNull StepAction action) throws CloudManagerApiException;

  /**
   * Returns the fully qualified URL to the log file for download.
   *
   * @param programId   the program id of the pipeline context
   * @param pipelineId  the pipeline id for the execution context
   * @param executionId the execution id for the logs
   * @param action      the execution step action for the log
   * @param name        custom log file name
   * @return the log file download URL
   * @throws CloudManagerApiException when any error occurs
   */
  String getStepLogDownloadUrl(@NotNull String programId, @NotNull String pipelineId, @NotNull String executionId, @NotNull StepAction action, @NotNull String name) throws CloudManagerApiException;

  /**
   * Returns the fully qualified URL to the log file for download.
   *
   * @param execution the execution for the log
   * @param action    the execution step action for the log
   * @return the log file download URL
   * @throws CloudManagerApiException when any error occurs
   */
  String getStepLogDownloadUrl(@NotNull PipelineExecution execution, @NotNull StepAction action) throws CloudManagerApiException;

  /**
   * Returns the fully qualified URL to the log file for download.
   *
   * @param execution the execution for the log
   * @param action    the execution step action for the log
   * @param name      custom log file name
   * @return the log file download URL
   * @throws CloudManagerApiException when any error occurs
   */
  String getStepLogDownloadUrl(@NotNull PipelineExecution execution, @NotNull StepAction action, @NotNull String name) throws CloudManagerApiException;

  /**
   * Retrieves the metrics for the specified execution and step, if any.
   *
   * @param execution the execution step
   * @param action    the action step for which quality metrics are desired
   * @return the metrics for the execution
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<Metric> getQualityGateResults(@NotNull PipelineExecution execution, @NotNull StepAction action) throws CloudManagerApiException;

  /**
   * Lists executions of the specified pipeline, using the default limit and starting at 0.
   *
   * @param programId  the program id context of the pipeline
   * @param pipelineId the pipeline id
   * @return list of executions
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<PipelineExecution> list(@NotNull String programId, @NotNull String pipelineId) throws CloudManagerApiException;

  /**
   * Lists executions of the specified pipeline, using the default limit and starting at 0.
   *
   * @param pipeline the pipeline for the execution search
   * @return list of executions
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<PipelineExecution> list(@NotNull Pipeline pipeline) throws CloudManagerApiException;

  /**
   * Lists executions of the specified pipeline, using the specified limit and starting at 0.
   *
   * @param programId  the program id context of the pipeline
   * @param pipelineId the pipeline id
   * @param limit      the number of executions to return
   * @return list of executions
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<PipelineExecution> list(@NotNull String programId, @NotNull String pipelineId, int limit) throws CloudManagerApiException;

  /**
   * Lists executions of the specified pipeline, using the specified limit and starting at 0.
   *
   * @param pipeline the pipeline for the execution search
   * @param limit    the number of executions to return
   * @return list of executions, if any
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<PipelineExecution> list(@NotNull Pipeline pipeline, int limit) throws CloudManagerApiException;

  /**
   * Lists executions of the specified pipeline, using the specified limit and starting at the specified position.
   *
   * @param programId  the program id context of the pipeline
   * @param pipelineId the pipeline id
   * @param start      the starting position of the results
   * @param limit      the number of executions to return
   * @return list of executions
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<PipelineExecution> list(@NotNull String programId, @NotNull String pipelineId, int start, int limit) throws CloudManagerApiException;

  /**
   * Lists executions of the specified pipeline, using the specified limit and starting at the specified position.
   *
   * @param pipeline the pipeline for the execution search
   * @param start    the starting position of the results
   * @param limit    the number of executions to return
   * @return list of executions
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<PipelineExecution> list(@NotNull Pipeline pipeline, int start, int limit) throws CloudManagerApiException;

  /**
   * Returns a list of all artifacts associated with the Step
   *
   * @param step the pipeline step context
   * @return list of artifacts
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<Artifact> listArtifacts(@NotNull PipelineExecutionStepState step) throws CloudManagerApiException;

  /**
   * Returns the fully qualified URL to the artifact file for download.
   *
   * @param step       the step context for the artifact
   * @param artifactId the id of the artifact
   * @return the artifact file download url
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  String getArtifactDownloadUrl(@NotNull PipelineExecutionStepState step, String artifactId) throws CloudManagerApiException;

  static Builder builder() {
    return new Builder();
  }

  class Builder {
    private Workspace workspace;
    private URL url;

    public Builder() {
    }

    public Builder workspace(@NotNull Workspace workspace) {
      this.workspace = workspace;
      return this;
    }

    public Builder url(@NotNull URL url) {
      this.url = url;
      return this;
    }

    public PipelineExecutionApi build() {
      if (workspace == null) {
        throw new IllegalStateException("Workspace must be specified.");
      }
      if (workspace.getAuthContext() == null) {
        throw new IllegalStateException("Workspace must specify AuthContext");
      }
      workspace.getAuthContext().validate();
      return new PipelineExecutionApiImpl(workspace, url);
    }
  }
}
