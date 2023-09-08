package io.adobe.cloudmanager.impl.client;

import feign.Param;
import feign.RequestLine;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.impl.generated.BranchList;
import io.adobe.cloudmanager.impl.generated.Repository;
import io.adobe.cloudmanager.impl.generated.RepositoryList;

public interface FeignRepositoryApi {

  @RequestLine("GET /api/program/{programId}/repositories")
  RepositoryList list(@Param("programId") String programId) throws CloudManagerApiException;

  @RequestLine("GET /api/program/{programId}/repositories?start={start}&limit={limit}")
  RepositoryList list(@Param("programId") String programId, @Param("start") int start, @Param("limit") int limit) throws CloudManagerApiException;

  @RequestLine("GET /api/program/{programId}/repository/{id}")
  Repository get(@Param("programId") String programId, @Param("id") String id) throws CloudManagerApiException;

  @RequestLine("GET /api/program/{programId}/repository/{id}/branches")
  BranchList listBranches(@Param("programId") String programId, @Param("id") String id) throws CloudManagerApiException;
}
