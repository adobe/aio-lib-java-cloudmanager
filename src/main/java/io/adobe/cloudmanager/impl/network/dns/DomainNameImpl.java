package io.adobe.cloudmanager.impl.network.dns;

import java.util.Set;
import java.util.stream.Collectors;

import io.adobe.cloudmanager.DomainNameApi;
import io.adobe.cloudmanager.DomainName;
import io.adobe.cloudmanager.Environment;
import io.adobe.cloudmanager.SSLCertificate;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;

@ToString
@EqualsAndHashCode(callSuper = false)
public class DomainNameImpl implements DomainName {

  private final io.adobe.cloudmanager.impl.generated.DomainName delegate;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private final DomainNameApi client;

  public DomainNameImpl(io.adobe.cloudmanager.impl.generated.DomainName delegate, DomainNameApi client) {
    this.delegate = delegate;
    this.client = client;
  }

  @Override
  public String getId() {
    return delegate.getId().toString();
  }

  @Override
  public String getName() {
    return delegate.getName();
  }

  @Override
  public Type getRecordType() {
    return Type.valueOf(delegate.getType().name());
  }

  @Override
  public Status getStatusState() {
    return Status.fromValue(delegate.getStatus().getValue());
  }

  @Override
  public Set<Target> getTargets() {
    return delegate.getDnsResolution().stream().map(t -> new Target(t.getTarget(), t.isDetected())).collect(Collectors.toSet());
  }

  @Override
  public String getTxtRecord() {
    return delegate.getDnsTxtRecord();
  }

  @Override
  public String getZone() {
    return delegate.getDnsZone();
  }

  @Override
  public Environment.Tier getEnvironmentTier() {
    return Environment.Tier.fromValue(delegate.getTier().getValue());
  }

  @Override
  public String getEnvironmentId() {
    return delegate.getEnvironmentId().toString();
  }

  @Override
  public SSLCertificate getCertificate() {
    return null;
  }
}
