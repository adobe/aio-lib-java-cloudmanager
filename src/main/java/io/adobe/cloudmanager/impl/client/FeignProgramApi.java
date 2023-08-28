package io.adobe.cloudmanager.impl.client;

import feign.Param;
import feign.RequestLine;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.impl.generated.ProgramList;

public interface FeignProgramApi {

  @RequestLine("DELETE /api/program/{id}")
  void delete(@Param("id") String id) throws CloudManagerApiException;

  @RequestLine("GET /api/tenant/{tenantId}/programs")
  ProgramList list(@Param("tenantId") String tenantId) throws CloudManagerApiException;
}
