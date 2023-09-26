package io.adobe.cloudmanager.impl.network.ipallow;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import com.adobe.aio.workspace.Workspace;
import feign.Param;
import feign.RequestLine;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.Environment;
import io.adobe.cloudmanager.IPAllowList;
import io.adobe.cloudmanager.IPAllowApi;
import io.adobe.cloudmanager.Program;
import io.adobe.cloudmanager.impl.FeignUtil;
import io.adobe.cloudmanager.impl.generated.IPAllowedList;
import io.adobe.cloudmanager.impl.generated.IPAllowedListBinding;
import io.adobe.cloudmanager.impl.generated.IPAllowlistBindingsList;
import io.adobe.cloudmanager.impl.generated.IPAllowlistList;

import static io.adobe.cloudmanager.Constants.*;

public class IPAllowApiImpl implements IPAllowApi {

  private final FeignApi api;

  public IPAllowApiImpl(Workspace workspace, URL url) {
    String baseUrl = url == null ? CLOUD_MANAGER_URL : url.toString();
    api = FeignUtil.getBuilder(workspace).errorDecoder(new ExceptionDecoder()).target(FeignApi.class, baseUrl);
  }

  @Override
  public IPAllowList get(String programId, String id) throws CloudManagerApiException {
    return new IPAllowListImpl(api.get(programId, id), this);
  }

  @Override
  public IPAllowList update(String programId, String id, String name, Collection<IPAllowList.Cidr> cidrs, Collection<IPAllowList.Binding> bindings) throws CloudManagerApiException {
    IPAllowedList update = new IPAllowedList()
        .programId(programId)
        .id(id)
        .name(name)
        .ipCidrSet(cidrs.stream().map(IPAllowList.Cidr::getSignature).collect(Collectors.toList()))
        .bindings(bindings.stream().map(b -> new IPAllowedListBinding()
            .programId(programId)
            .ipAllowListId(id)
            .environmentId(b.getEnvironmentId())
            .tier(IPAllowedListBinding.TierEnum.fromValue(b.getTier().getValue()))
        ).collect(Collectors.toList()));
    return new IPAllowListImpl(api.update(programId, id, update), this);
  }

  @Override
  public void delete(String programId, String id) throws CloudManagerApiException {
    api.delete(programId, id);
  }

  @Override
  public Collection<IPAllowList> list(String programId) throws CloudManagerApiException {
    IPAllowlistList list = api.list(programId);
    return list.getEmbedded() == null ?
        Collections.emptyList() :
        list.getEmbedded().getIpAllowlists().stream().map(i -> new IPAllowListImpl(i, this)).collect(Collectors.toList());
  }

  @Override
  public Collection<IPAllowList> list(Program program) throws CloudManagerApiException {
    return list(program.getId());
  }

  @Override
  public IPAllowList create(String programId, String name, Collection<IPAllowList.Cidr> cidrs, Collection<IPAllowList.Binding> bindings) throws CloudManagerApiException {
    IPAllowedList update = new IPAllowedList()
        .programId(programId)
        .name(name)
        .ipCidrSet(cidrs.stream().map(IPAllowList.Cidr::getSignature).collect(Collectors.toList()))
        .bindings(bindings.stream().map(b -> new IPAllowedListBinding()
            .programId(programId)
            .environmentId(b.getEnvironmentId())
            .tier(IPAllowedListBinding.TierEnum.fromValue(b.getTier().getValue()))
        ).collect(Collectors.toList()));
    return new IPAllowListImpl(api.create(programId, update), this);
  }

  @Override
  public IPAllowList create(Program program, String name, Collection<IPAllowList.Cidr> cidrs, Collection<IPAllowList.Binding> bindings) throws CloudManagerApiException {
    return create(program.getId(), name, cidrs, bindings);
  }

  @Override
  public IPAllowList.Binding getBinding(String programId, String ipAllowListId, String id) throws CloudManagerApiException {
    IPAllowedListBinding binding = api.getBinding(programId, ipAllowListId, id);
    IPAllowList.Binding b = new IPAllowList.Binding(binding.getEnvironmentId(), Environment.Tier.fromValue(binding.getTier().getValue()));
    b.setId(binding.getId());
    return b;
  }

  @Override
  public IPAllowList.Binding getBinding(IPAllowList ipAllowList, String id) throws CloudManagerApiException {
    return getBinding(ipAllowList.getProgramId(), ipAllowList.getId(), id);
  }

  @Override
  public void deleteBinding(String programId, String ipAllowListId, String id) throws CloudManagerApiException {
    api.deleteBinding(programId, ipAllowListId,id);
  }

  @Override
  public void deleteBinding(String programId, String ipAllowListId, IPAllowList.Binding binding) throws CloudManagerApiException {
    deleteBinding(programId, ipAllowListId, binding.getId());
  }

  @Override
  public void deleteBinding(IPAllowList ipAllowList, String id) throws CloudManagerApiException {
    deleteBinding(ipAllowList.getProgramId(), ipAllowList.getId(), id);
  }

  @Override
  public void deleteBinding(IPAllowList ipAllowList, IPAllowList.Binding binding) throws CloudManagerApiException {
    deleteBinding(ipAllowList.getProgramId(), ipAllowList.getId(), binding.getId());
  }

  @Override
  public void retryBinding(String programId, String ipAllowListId, IPAllowList.Binding binding) throws CloudManagerApiException {
    api.retryBinding(programId, ipAllowListId, binding.getId());
  }

  @Override
  public void retryBinding(IPAllowList ipAllowList, IPAllowList.Binding binding) throws CloudManagerApiException {
    retryBinding(ipAllowList.getProgramId(), ipAllowList.getId(), binding);
  }

  @Override
  public Collection<IPAllowList.Binding> listBindings(String programId, String id) throws CloudManagerApiException {
    IPAllowlistBindingsList list = api.listBindings(programId, id);
    return list.getEmbedded() == null ?
        Collections.emptyList() :
        list.getEmbedded().getIpAllowlistBindings().stream()
            .map(b -> {
              IPAllowList.Binding binding = new IPAllowList.Binding(b.getEnvironmentId(), Environment.Tier.fromValue(b.getTier().getValue()));
              binding.setId(binding.getId());
              return binding;
            }).collect(Collectors.toList());
  }

  @Override
  public Collection<IPAllowList.Binding> listBindings(IPAllowList ipAllowList) throws CloudManagerApiException {
    return listBindings(ipAllowList.getProgramId(), ipAllowList.getId());
  }

  @Override
  public IPAllowList.Binding createBinding(String programId, String ipAllowListId, String environmentId, Environment.Tier tier) throws CloudManagerApiException {
    IPAllowedListBinding toCreate = new IPAllowedListBinding()
        .environmentId(environmentId)
        .tier(IPAllowedListBinding.TierEnum.fromValue(tier.getValue()));

    IPAllowedListBinding created = api.createBinding(programId, ipAllowListId, toCreate);
    IPAllowList.Binding binding = new IPAllowList.Binding(created.getEnvironmentId(), Environment.Tier.fromValue(created.getTier().getValue()));
    binding.setId(created.getId());
    return binding;
  }

  @Override
  public IPAllowList.Binding createBinding(IPAllowList ipAllowList, String environmentId, Environment.Tier tier) throws CloudManagerApiException {
    return createBinding(ipAllowList.getProgramId(), ipAllowList.getId(), environmentId, tier);
  }

  private interface FeignApi {

    @RequestLine("GET /api/program/{programId}/ipAllowlist/{id}")
    IPAllowedList get(@Param("programId") String programId, @Param("id") String id) throws CloudManagerApiException;

    @RequestLine("PUT /api/program/{programId}/ipAllowlist/{id}")
    IPAllowedList update(@Param("programId") String programId, @Param("id") String id, IPAllowedList update) throws CloudManagerApiException;

    @RequestLine("DELETE /api/program/{programId}/ipAllowlist/{id}")
    IPAllowedList delete(@Param("programId") String programId, @Param("id") String id) throws CloudManagerApiException;

    @RequestLine("GET /api/program/{programId}/ipAllowlists")
    IPAllowlistList list(@Param("programId") String programId) throws CloudManagerApiException;

    @RequestLine("POST /api/program/{programId}/ipAllowlists")
    IPAllowedList create(@Param("programId") String programId, IPAllowedList update) throws CloudManagerApiException;

    @RequestLine("GET /api/program/{programId}/ipAllowlist/{ipAllowlistId}/binding/{id}")
    IPAllowedListBinding getBinding(@Param("programId") String programId, @Param("ipAllowlistId") String ipAllowlistId, @Param("id") String id) throws CloudManagerApiException;

    @RequestLine("DELETE /api/program/{programId}/ipAllowlist/{ipAllowlistId}/binding/{id}")
    IPAllowedListBinding deleteBinding(@Param("programId") String programId, @Param("ipAllowlistId") String ipAllowlistId, @Param("id") String id) throws CloudManagerApiException;

    @RequestLine("PUT /api/program/{programId}/ipAllowlist/{ipAllowlistId}/binding/{id}/retry")
    IPAllowedListBinding retryBinding(@Param("programId") String programId, @Param("ipAllowlistId") String ipAllowlistId, @Param("id") String id) throws CloudManagerApiException;

    @RequestLine("GET /api/program/{programId}/ipAllowlist/{ipAllowlistId}/bindings")
    IPAllowlistBindingsList listBindings(@Param("programId") String programId, @Param("ipAllowlistId") String ipAllowlistId) throws CloudManagerApiException;

    @RequestLine("POST /api/program/{programId}/ipAllowlist/{ipAllowlistId}/bindings")
    IPAllowedListBinding createBinding(@Param("programId") String programId, @Param("ipAllowlistId") String ipAllowlistId, IPAllowedListBinding binding) throws CloudManagerApiException;

  }
}
