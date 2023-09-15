package io.adobe.cloudmanager.impl.environment;

import io.adobe.cloudmanager.CloudManagerApi;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.EnvironmentApi;
import io.adobe.cloudmanager.RegionDeployment;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;

@ToString
@EqualsAndHashCode(callSuper = false)
public class RegionDeploymentImpl extends io.adobe.cloudmanager.impl.generated.RegionDeployment implements RegionDeployment {

  private static final long serialVersionUID = 1L;

  @Delegate
  private final io.adobe.cloudmanager.impl.generated.RegionDeployment delegate;
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private final EnvironmentApi client;

  public RegionDeploymentImpl(io.adobe.cloudmanager.impl.generated.RegionDeployment delegate, EnvironmentApi client) {
    this.delegate = delegate;
    this.client = client;
  }

  @Override
  public Type getDeployType() {
    return Type.valueOf(delegate.getType().name());
  }

}
