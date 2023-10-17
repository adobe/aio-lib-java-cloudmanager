package io.adobe.cloudmanager.impl.pipeline.execution;

import java.time.OffsetDateTime;

import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.PipelineExecution;
import io.adobe.cloudmanager.PipelineExecutionEndEvent;
import io.adobe.cloudmanager.impl.generated.event.PipelineExecutionEndEventEvent;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = false)
public class PipelineExecutionEndEventImpl implements PipelineExecutionEndEvent {

  private final PipelineExecutionEndEventEvent delegate;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private final PipelineExecutionApiImpl client;

  PipelineExecutionEndEventImpl(PipelineExecutionEndEventEvent delegate, PipelineExecutionApiImpl client) {
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
  public PipelineExecution getExecution() throws CloudManagerApiException {
    return client.get(delegate.getActivitystreamsobject());
  }

}
