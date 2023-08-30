package io.adobe.cloudmanager.impl;

import java.util.Collection;

import io.adobe.cloudmanager.CloudManagerApi;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.Repository;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;

@ToString
@EqualsAndHashCode(callSuper = false)
public class RepositoryImpl extends io.adobe.cloudmanager.impl.generated.Repository implements Repository {
  private static final long serialVersionUID = 1L;

  public RepositoryImpl(io.adobe.cloudmanager.impl.generated.Repository delegate, CloudManagerApi client) {
    this.delegate = delegate;
    this.client = client;
  }

  @Delegate
  private final io.adobe.cloudmanager.impl.generated.Repository delegate;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private final CloudManagerApi client;

  @Override
  public String getName() {
    return delegate.getRepo();
  }

  @Override
  public String getUrl() {
    return delegate.getRepositoryUrl();
  }

  @Override
  public Collection<String> listBranches() throws CloudManagerApiException {
    return client.listBranches(this);
  }
}
