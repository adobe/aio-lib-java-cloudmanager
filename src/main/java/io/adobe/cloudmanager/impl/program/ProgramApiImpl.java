package io.adobe.cloudmanager.impl.program;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import com.adobe.aio.workspace.Workspace;
import feign.Param;
import feign.RequestLine;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.Program;
import io.adobe.cloudmanager.ProgramApi;
import io.adobe.cloudmanager.Region;
import io.adobe.cloudmanager.Tenant;
import io.adobe.cloudmanager.impl.FeignUtil;
import io.adobe.cloudmanager.impl.generated.EmbeddedProgram;
import io.adobe.cloudmanager.impl.generated.ProgramList;
import io.adobe.cloudmanager.impl.generated.RegionsList;

import static io.adobe.cloudmanager.Constants.*;

public class ProgramApiImpl implements ProgramApi {

  private final FeignApi api;

  public ProgramApiImpl(Workspace workspace, URL url) {
    String baseUrl = url == null ? CLOUD_MANAGER_URL : url.toString();
    api = FeignUtil.getBuilder(workspace).errorDecoder(new ExceptionDecoder()).target(FeignApi.class, baseUrl);
  }

  @Override
  public Program get(String programId) throws CloudManagerApiException {
    EmbeddedProgram program = api.get(programId);
    return new ProgramImpl(program, this);
  }

  @Override
  public void delete(String programId) throws CloudManagerApiException {
    api.delete(programId);
  }

  @Override
  public void delete(Program program) throws CloudManagerApiException {
    delete(program.getId());
  }

  @Override
  public Collection<Program> list(String tenantId) throws CloudManagerApiException {
    ProgramList list = api.list(tenantId);
    return list.getEmbedded() == null ?
        Collections.emptyList() :
        list.getEmbedded().getPrograms().stream().map(p -> new ProgramImpl(p, this)).collect(Collectors.toList());
  }

  public Collection<Program> list(Tenant tenant) throws CloudManagerApiException {
    return list(tenant.getId());
  }

  @Override
  public Collection<Region> listRegions(String programId) throws CloudManagerApiException {
    RegionsList list = api.listRegions(programId);
    return list.getEmbedded() == null ?
        Collections.emptySet() :
        list.getEmbedded().getRegions().stream().map(r -> Region.fromValue(r.getName())).collect(Collectors.toList());
  }

  private interface FeignApi {
    @RequestLine("GET /api/program/{id}")
    EmbeddedProgram get(@Param("id") String id) throws CloudManagerApiException;

    @RequestLine("DELETE /api/program/{id}")
    void delete(@Param("id") String id) throws CloudManagerApiException;

    @RequestLine("GET /api/tenant/{tenantId}/programs")
    ProgramList list(@Param("tenantId") String tenantId) throws CloudManagerApiException;

    @RequestLine("GET /api/program/{id}/regions")
    RegionsList listRegions(@Param("id") String id) throws CloudManagerApiException;
  }
}
