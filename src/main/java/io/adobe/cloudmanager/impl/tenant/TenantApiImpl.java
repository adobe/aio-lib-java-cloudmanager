package io.adobe.cloudmanager.impl.tenant;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import com.adobe.aio.workspace.Workspace;
import feign.Param;
import feign.RequestLine;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.Tenant;
import io.adobe.cloudmanager.TenantApi;
import io.adobe.cloudmanager.impl.FeignUtil;
import io.adobe.cloudmanager.impl.generated.TenantList;

import static io.adobe.cloudmanager.Constants.*;

public class TenantApiImpl implements TenantApi {

  private final FeignApi api;

  public TenantApiImpl(Workspace workspace, URL url) {
    String baseUrl = url == null ? CLOUD_MANAGER_URL : url.toString();
    api = FeignUtil.getBuilder(workspace).errorDecoder(new ExceptionDecoder()).target(FeignApi.class, baseUrl);
  }

  @Override
  public Collection<Tenant> list() throws CloudManagerApiException {
    TenantList tenantList = api.list();
    return tenantList.getEmbedded() == null ?
        Collections.emptyList() :
        tenantList.getEmbedded().getTenants().stream().map(TenantImpl::new).collect(Collectors.toList());

  }

  @Override
  public Tenant get(String tenantId) throws CloudManagerApiException {
    return new TenantImpl(api.get(tenantId));
  }

  private interface FeignApi {
    @RequestLine("GET /api/tenants")
    TenantList list() throws CloudManagerApiException;

    @RequestLine("GET /api/tenant/{id}")
    io.adobe.cloudmanager.impl.generated.Tenant get(@Param("id") String id) throws CloudManagerApiException;
  }
}
