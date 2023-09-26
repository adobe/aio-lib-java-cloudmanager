package io.adobe.cloudmanager.impl.network.dns;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import com.adobe.aio.workspace.Workspace;
import feign.Body;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.DomainName;
import io.adobe.cloudmanager.DomainNameApi;
import io.adobe.cloudmanager.DomainNameUpdate;
import io.adobe.cloudmanager.impl.FeignUtil;
import io.adobe.cloudmanager.impl.generated.DomainNameList;
import io.adobe.cloudmanager.impl.generated.NewDomainName;

import static io.adobe.cloudmanager.Constants.*;

public class DomainNameApiImpl implements DomainNameApi {

  private final FeignApi api;

  public DomainNameApiImpl(Workspace workspace, URL url) {
    String baseUrl = url == null ? CLOUD_MANAGER_URL : url.toString();
    api = FeignUtil.getBuilder(workspace).errorDecoder(new ExceptionDecoder()).target(FeignApi.class, baseUrl);
  }

  @Override
  public Collection<DomainName> list(String programId) throws CloudManagerApiException {
    DomainNameList list = api.list(programId);
    return list.getEmbedded().getDomainNames() == null ?
        Collections.emptyList() :
        list.getEmbedded().getDomainNames().stream().map(d -> new DomainNameImpl(d, this)).collect(Collectors.toList());
  }

  @Override
  public Collection<DomainName> list(String programId, int limit) throws CloudManagerApiException {
    return list(programId, 0, limit);
  }

  @Override
  public Collection<DomainName> list(String programId, int start, int limit) throws CloudManagerApiException {
    DomainNameList list = api.list(programId, start, limit);
    return list.getEmbedded().getDomainNames() == null ?
        Collections.emptyList() :
        list.getEmbedded().getDomainNames().stream().map(d -> new DomainNameImpl(d, this)).collect(Collectors.toList());
  }

  @Override
  public DomainName create(String programId, String name, String environmentId, String certificateId, String dnsTxtRecord, String dnsZone) throws CloudManagerApiException {
    NewDomainName toCreate = new NewDomainName().name(name)
        .environmentId(Long.parseLong(environmentId))
        .certificateId(Long.parseLong(certificateId))
        .dnsTxtRecord(dnsTxtRecord)
        .dnsZone(dnsZone);
    return new DomainNameImpl(api.create(programId, toCreate), this);
  }

  @Override
  public DomainName get(String programId, String id) throws CloudManagerApiException {
    return new DomainNameImpl(api.get(programId, id), this);
  }

  @Override
  public DomainName update(String programId, String id, DomainNameUpdate update) throws CloudManagerApiException {
    io.adobe.cloudmanager.impl.generated.DomainName toUpdate = new io.adobe.cloudmanager.impl.generated.DomainName();
    toUpdate
        .id(Long.valueOf(id))
        .name(update.getName())
        .environmentId(Long.valueOf(update.getEnvironmentId()))
        .tier(io.adobe.cloudmanager.impl.generated.DomainName.TierEnum.fromValue(update.getTier().getValue()))
        .certificateId(Long.valueOf(update.getCertificateId()))
        .dnsTxtRecord(update.getTxtRecord())
        .dnsZone(update.getZone());

    return new DomainNameImpl(api.update(programId, id, toUpdate), this);
  }

  @Override
  public void delete(String programId, String id) throws CloudManagerApiException {
    api.delete(programId, id);
  }

  @Override
  public DomainName deploy(String programId, String id) throws CloudManagerApiException {
    return new DomainNameImpl(api.deploy(programId, id), this);
  }

  @Override
  public DomainName verify(String programId, String id) throws CloudManagerApiException {
    return new DomainNameImpl(api.verify(programId, id), this);
  }

  @Override
  public DomainName validate(String programId, String name, String environmentId, String certificateId) throws CloudManagerApiException {
    return new DomainNameImpl(api.validate(programId, name, Integer.parseInt(environmentId), Integer.parseInt(certificateId)), this);
  }

  private interface FeignApi {
    @RequestLine("GET /api/program/{programId}/domainNames")
    DomainNameList list(@Param("programId") String programId) throws CloudManagerApiException;

    @RequestLine("GET /api/program/{programId}/domainNames?start={start}&limit={limit}")
    DomainNameList list(@Param("programId") String programId, @Param("start") int start, @Param("limit") int limit) throws CloudManagerApiException;

    @RequestLine("POST /api/program/{programId}/domainNames")
    @Headers("Content-Type: application/json")
    io.adobe.cloudmanager.impl.generated.DomainName create(@Param("programId") String programId, NewDomainName domainName) throws CloudManagerApiException;

    @RequestLine("GET /api/program/{programId}/domainName/{id}")
    io.adobe.cloudmanager.impl.generated.DomainName get(@Param("programId") String programId, @Param("id") String id) throws CloudManagerApiException;

    @RequestLine("PUT /api/program/{programId}/domainName/{id}")
    @Headers("Content-Type: application/json")
    io.adobe.cloudmanager.impl.generated.DomainName update(@Param("programId") String programId, @Param("id") String id, io.adobe.cloudmanager.impl.generated.DomainName updates) throws CloudManagerApiException;

    @RequestLine("DELETE /api/program/{programId}/domainName/{id}")
    io.adobe.cloudmanager.impl.generated.DomainName delete(@Param("programId") String programId, @Param("id") String id) throws CloudManagerApiException;

    @RequestLine("POST /api/program/{programId}/domainName/{id}/deploy")
    io.adobe.cloudmanager.impl.generated.DomainName deploy(@Param("programId") String programId, @Param("id") String id) throws CloudManagerApiException;

    @RequestLine("POST /api/program/{programId}/domainName/{id}/verify")
    io.adobe.cloudmanager.impl.generated.DomainName verify(@Param("programId") String programId, @Param("id") String id) throws CloudManagerApiException;

    @RequestLine("POST /api/program/{programId}/domainNames/validate")
    @Headers("Content-Type: application/json")
    @Body("%7B \"name\": \"{name}\", \"environmentId\": {environmentId}, \"certificateId\": {certificateId} %7D")
    io.adobe.cloudmanager.impl.generated.DomainName validate(@Param("programId") String programId, @Param("name") String name, @Param("environmentId") int environmentId, @Param("certificateId") int certificateId) throws CloudManagerApiException;

  }
}
