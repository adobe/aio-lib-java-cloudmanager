package io.adobe.cloudmanager.model;

import io.adobe.cloudmanager.CloudManagerApi;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.PipelineUpdate;
import lombok.ToString;
import lombok.experimental.Delegate;

@ToString
public class Pipeline extends io.adobe.cloudmanager.swagger.model.Pipeline {

  public Pipeline(io.adobe.cloudmanager.swagger.model.Pipeline delegate, CloudManagerApi client) {
    this.delegate = delegate;
    this.client = client;
  }

  @Delegate
  private final io.adobe.cloudmanager.swagger.model.Pipeline delegate;

  @ToString.Exclude
  private final CloudManagerApi client;

  public String startExecution() throws CloudManagerApiException {
    return client.startExecution(this);
  }

  public Pipeline update(PipelineUpdate update) throws CloudManagerApiException {
    return client.updatePipeline(this, update);
  }

  public void delete() throws CloudManagerApiException {
    client.deletePipeline(this);
  }
}
