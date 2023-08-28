package io.adobe.cloudmanager.impl;

import java.util.Collection;

import io.adobe.cloudmanager.CloudManagerApi;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.Program;
import io.adobe.cloudmanager.Tenant;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;

@ToString
@EqualsAndHashCode(callSuper = false)
public class TenantImpl extends io.adobe.cloudmanager.impl.generated.Tenant implements Tenant {

  private static final long serialVersionUID = 1L;

  public TenantImpl(io.adobe.cloudmanager.impl.generated.Tenant delegate, CloudManagerApi client) {
    this.delegate = delegate;
    this.client = client;
  }

  @Delegate
  private final io.adobe.cloudmanager.impl.generated.Tenant delegate;
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private final CloudManagerApi client;

  @Override
  public String getOrganizationName() {
    return delegate.getOrganisation().getOrganizationName();
  }

  @Override
  public Collection<Program> listPrograms() throws CloudManagerApiException {
    return client.listPrograms(this);
  }
}
