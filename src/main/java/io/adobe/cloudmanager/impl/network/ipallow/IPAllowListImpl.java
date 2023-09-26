package io.adobe.cloudmanager.impl.network.ipallow;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.net.util.SubnetUtils;

import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.Environment;
import io.adobe.cloudmanager.IPAllowList;
import io.adobe.cloudmanager.IPAllowApi;
import io.adobe.cloudmanager.impl.generated.IPAllowedList;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;

@ToString
@EqualsAndHashCode(callSuper = false)
public class IPAllowListImpl extends IPAllowedList implements IPAllowList {

  private static final long serialVersionUID = 1L;

  private Collection<Cidr> cidrs;
  private Collection<Binding> bindings;

  @Delegate
  private IPAllowedList delegate;
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private final IPAllowApi client;

  public IPAllowListImpl(IPAllowedList delegate, IPAllowApi client) {
    this.delegate = delegate;
    this.client = client;
  }

  @Override
  public Collection<Cidr> listCidr() {
    if (cidrs == null) {
      cidrs = delegate.getIpCidrSet().stream().map((c) -> {
        SubnetUtils su = new SubnetUtils(c);
        return new Cidr(su.getInfo().getAddress(), su.getInfo().getNetmask());
      }).collect(Collectors.toList());
    }
    return cidrs;
  }

  @Override
  public Collection<Binding> listBindings() {
    if (bindings == null) {
      bindings = delegate.getBindings().stream().map(b -> {
        Binding binding = new Binding(b.getEnvironmentId(), Environment.Tier.fromValue(b.getTier().getValue()));
        binding.setId(b.getId());
        return binding;
      }).collect(Collectors.toList());
    }
    return bindings;
  }

  @Override
  public Binding getBinding(String id) throws CloudManagerApiException {
    return listBindings().stream().filter(b -> b.getId().equals(id)).findFirst().orElseThrow(() -> new CloudManagerApiException(String.format("Cannot find Binding with id '%s' in IP Allow List.", id)));
  }

  @Override
  public Optional<Binding> getBinding(String environmentId, Environment.Tier tier) {
    return listBindings().stream().filter(b -> b.getEnvironmentId().equals(environmentId) && b.getTier().equals(tier)).findFirst();
  }

  @Override
  public void delete(Binding binding) throws CloudManagerApiException {
    client.deleteBinding(this, binding);
  }

  @Override
  public void retry(Binding binding) throws CloudManagerApiException {
    client.retryBinding(delegate.getProgramId(), delegate.getId(), binding);
  }

  @Override
  public Binding createBinding(String environmentId, Environment.Tier tier) throws CloudManagerApiException {
    return client.createBinding(this, environmentId, tier);
  }

  @Override
  public void deleteBinding(String environmentId, Environment.Tier tier) throws CloudManagerApiException {
    Optional<Binding> binding = getBinding(environmentId, tier);
    if (binding.isPresent()) {
      client.deleteBinding(this, binding.get());
    }
  }
}
