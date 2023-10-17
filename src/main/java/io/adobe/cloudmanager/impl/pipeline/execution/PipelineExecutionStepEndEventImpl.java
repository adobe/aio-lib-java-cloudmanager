package io.adobe.cloudmanager.impl.pipeline.execution;

import java.time.OffsetDateTime;

import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.PipelineExecutionStepEndEvent;
import io.adobe.cloudmanager.PipelineExecutionStepState;
import io.adobe.cloudmanager.PipelineExecutionStepWaitingEvent;
import io.adobe.cloudmanager.impl.generated.event.PipelineExecutionStepStartEventEvent;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = false)
public class PipelineExecutionStepEndEventImpl implements PipelineExecutionStepEndEvent {

  private final PipelineExecutionStepStartEventEvent delegate;

  private final PipelineExecutionApiImpl client;

  PipelineExecutionStepEndEventImpl(PipelineExecutionStepStartEventEvent delegate, PipelineExecutionApiImpl client) {
    this.delegate = delegate;
    this.client = client;
  }

  @Override
  public String getId() {
    return delegate.getAtId();
  }

  @Override
  public OffsetDateTime getPublished() {
    return delegate.getActivitystreamspublished();
  }

  @Override
  public PipelineExecutionStepState getStepState() throws CloudManagerApiException {
    return client.getStepState(delegate.getActivitystreamsobject());
  }
}
