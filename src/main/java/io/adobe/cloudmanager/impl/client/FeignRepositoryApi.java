package io.adobe.cloudmanager.impl.client;

import java.util.Map;

import feign.Param;
import feign.QueryMap;
import feign.RequestLine;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.impl.generated.BranchList;
import io.adobe.cloudmanager.impl.generated.Repository;
import io.adobe.cloudmanager.impl.generated.RepositoryList;

public interface FeignRepositoryApi {

  String START_PARAM = "start";
  String LIMIT_PARAM = "limit";

  @RequestLine("GET /api/program/{programId}/repositories")
  RepositoryList list(@Param("programId") String programId) throws CloudManagerApiException;

  @RequestLine("GET /api/program/{programId}/repositories")
  RepositoryList list(@Param("programId") String programId, @QueryMap Map<String, Object> params) throws CloudManagerApiException;

  @RequestLine("GET /api/program/{programId}/repository/{id}")
  Repository get(@Param("programId") String programId, @Param("id") String id) throws CloudManagerApiException;

  @RequestLine("GET /api/program/{programId}/repository/{id}/branches")
  BranchList listBranches(@Param("programId") String programId, @Param("id") String id) throws CloudManagerApiException;
}
