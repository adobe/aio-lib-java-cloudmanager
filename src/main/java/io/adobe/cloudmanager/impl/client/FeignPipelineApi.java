package io.adobe.cloudmanager.impl.client;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.impl.generated.Pipeline;
import io.adobe.cloudmanager.impl.generated.PipelineList;

public interface FeignPipelineApi {
  @RequestLine("GET /api/program/{programId}/pipelines")
  PipelineList list(@Param("programId") String programId) throws CloudManagerApiException;

  @RequestLine("GET /api/program/{programId}/pipeline/{id}")
  Pipeline get(@Param("programId")String programId, @Param("id") String id) throws CloudManagerApiException;

  @RequestLine("DELETE /api/program/{programId}/pipeline/{id}")
  void delete(@Param("programId")String programId, @Param("id") String id) throws CloudManagerApiException;

  @RequestLine("PATCH /api/program/{programId}/pipeline/{id}")
  @Headers("Content-Type: application/json")
  Pipeline update(@Param("programId") String programId, @Param("id") String id, Pipeline update) throws CloudManagerApiException;

  @RequestLine("DELETE /api/program/{programId}/pipeline/{id}/cache")
  void invalidateCache(@Param("programId") String programId, @Param("id") String id) throws CloudManagerApiException;
}
