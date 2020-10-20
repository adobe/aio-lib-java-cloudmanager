package io.adobe.cloudmanager.model;

import io.adobe.cloudmanager.CloudManagerApi;
import lombok.ToString;
import lombok.experimental.Delegate;

@ToString
public class EmbeddedProgram extends io.adobe.cloudmanager.swagger.model.EmbeddedProgram {

  public EmbeddedProgram(io.adobe.cloudmanager.swagger.model.EmbeddedProgram delegate, CloudManagerApi client) {
    this.delegate = delegate;
    this.client = client;
  }

  public String getSelfLink() {
    return delegate.getLinks().getSelf().getHref();
  }

  @Delegate
  private final io.adobe.cloudmanager.swagger.model.EmbeddedProgram delegate;

  @ToString.Exclude
  private final CloudManagerApi client;

}
