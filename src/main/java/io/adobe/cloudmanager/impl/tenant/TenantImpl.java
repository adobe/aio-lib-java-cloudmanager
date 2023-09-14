package io.adobe.cloudmanager.impl.tenant;

import java.util.Collection;

import io.adobe.cloudmanager.CloudManagerApi;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.Program;
import io.adobe.cloudmanager.Tenant;
import io.adobe.cloudmanager.TenantApi;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;

@ToString
@EqualsAndHashCode(callSuper = false)
public class TenantImpl extends io.adobe.cloudmanager.impl.generated.Tenant implements Tenant {

  private static final long serialVersionUID = 1L;

  @Delegate
  private final io.adobe.cloudmanager.impl.generated.Tenant delegate;

  public TenantImpl(io.adobe.cloudmanager.impl.generated.Tenant delegate) {
    this.delegate = delegate;
  }

  @Override
  public String getOrganizationName() {
    return delegate.getOrganisation().getOrganizationName();
  }
}
