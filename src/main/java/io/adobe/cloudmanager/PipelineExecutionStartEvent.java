package io.adobe.cloudmanager;

import java.time.OffsetDateTime;
import javax.validation.constraints.NotNull;

public interface PipelineExecutionStartEvent extends PipelineExecutionEvent {
  /**
   * The Pipeline Execution associated with the event.
   *
   * @return the pipeline execution
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  PipelineExecution getExecution() throws CloudManagerApiException;
}
