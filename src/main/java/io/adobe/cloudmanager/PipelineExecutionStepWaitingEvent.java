package io.adobe.cloudmanager;

import javax.validation.constraints.NotNull;

public interface PipelineExecutionStepWaitingEvent extends PipelineExecutionEvent {

  /**
   * The Pipeline Execution associated with the event.
   *
   * @return the pipeline execution
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  PipelineExecutionStepState getStepState() throws CloudManagerApiException;
}
