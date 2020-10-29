package io.adobe.cloudmanager.model;

import io.adobe.cloudmanager.CloudManagerApi;
import lombok.ToString;
import lombok.experimental.Delegate;

/**
 * Extension to the Swagger generated Pipeline. Provides convenience methods for frequently used APIs
 */
@ToString
public class PipelineExecutionStepState extends io.adobe.cloudmanager.swagger.model.PipelineExecutionStepState {

  public PipelineExecutionStepState(io.adobe.cloudmanager.swagger.model.PipelineExecutionStepState delegate, CloudManagerApi client) {
    this.delegate = delegate;
    this.client = client;
  }

  @Delegate
  private final io.adobe.cloudmanager.swagger.model.PipelineExecutionStepState delegate;

  @ToString.Exclude
  private final CloudManagerApi client;

}
