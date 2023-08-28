package io.adobe.cloudmanager.impl.client;

import feign.Param;
import feign.RequestLine;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.impl.generated.Tenant;
import io.adobe.cloudmanager.impl.generated.TenantList;

public interface FeignTenantApi {

  @RequestLine("GET /api/tenants")
  TenantList list() throws CloudManagerApiException;

  @RequestLine("GET /api/tenant/{id}")
  Tenant get(@Param("id") String id) throws CloudManagerApiException;
}
