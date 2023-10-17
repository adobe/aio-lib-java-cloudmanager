package io.adobe.cloudmanager.impl.content;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import com.adobe.aio.workspace.Workspace;
import feign.Param;
import feign.RequestLine;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.ContentFlow;
import io.adobe.cloudmanager.ContentSet;
import io.adobe.cloudmanager.ContentSetApi;
import io.adobe.cloudmanager.Environment;
import io.adobe.cloudmanager.impl.FeignUtil;
import io.adobe.cloudmanager.impl.generated.ContentFlowInput;
import io.adobe.cloudmanager.impl.generated.ContentFlowList;
import io.adobe.cloudmanager.impl.generated.ContentSetList;
import io.adobe.cloudmanager.impl.generated.ContentSetPath;
import io.adobe.cloudmanager.impl.generated.NewContentSet;

import static io.adobe.cloudmanager.Constants.*;
import static io.adobe.cloudmanager.ContentSet.*;

public class ContentSetApiImpl implements ContentSetApi {

  private final FeignApi api;

  public ContentSetApiImpl(Workspace workspace, URL url) {
    String baseUrl = url == null ? CLOUD_MANAGER_URL : url.toString();
    api = FeignUtil.getBuilder(workspace).errorDecoder(new ExceptionDecoder()).target(FeignApi.class, baseUrl);
  }

  @Override
  public Collection<ContentSet> list(String programId) throws CloudManagerApiException {
    ContentSetList list = api.list(programId);
    return list.getEmbedded() == null ?
        Collections.emptyList() :
        list.getEmbedded().getContentSets().stream().map(cs -> new ContentSetImpl(cs, this)).collect(Collectors.toList());
  }

  @Override
  public Collection<ContentSet> list(String programId, int limit) throws CloudManagerApiException {
    return list(programId, 0, limit);
  }

  @Override
  public Collection<ContentSet> list(String programId, int start, int limit) throws CloudManagerApiException {
    ContentSetList list = api.list(programId, start, limit);
    return list.getEmbedded() == null ?
        Collections.emptyList() :
        list.getEmbedded().getContentSets().stream().map(cs -> new ContentSetImpl(cs, this)).collect(Collectors.toList());
  }

  @Override
  public ContentSet create(String programId, String name, String description, Collection<PathDefinition> definitions) throws CloudManagerApiException {
    NewContentSet ncs = new NewContentSet()
        .name(name)
        .description(description);

    definitions.forEach((pd) -> {
      ncs.addPathsItem(new ContentSetPath().path(pd.getPath()).excluded(new ArrayList<>(pd.getExcluded())));
    });

    return new ContentSetImpl(api.create(programId, ncs), this);
  }

  @Override
  public ContentSet get(String programId, String id) throws CloudManagerApiException {
    return new ContentSetImpl(api.get(programId, id), this);
  }

  @Override
  public ContentSet update(String programId, String id, String name, String description, Collection<PathDefinition> definitions) throws CloudManagerApiException {
    return new ContentSetImpl(internalUpdate(programId, id, name, description, definitions), this);
  }

  @Override
  public void delete(String programId, String id) throws CloudManagerApiException {
    api.delete(programId, id);
  }

  @Override
  public Collection<ContentFlow> listFlows(String programId) throws CloudManagerApiException {
    ContentFlowList list = api.listFlows(programId);
    return list.getEmbedded() == null ?
        Collections.emptyList() :
        list.getEmbedded().getContentFlows().stream().map(cf -> new ContentFlowImpl(cf, this)).collect(Collectors.toList());
  }

  @Override
  public Collection<ContentFlow> listFlows(String programId, int limit) throws CloudManagerApiException {
    return listFlows(programId, 0, limit);
  }

  @Override
  public Collection<ContentFlow> listFlows(String programId, int start, int limit) throws CloudManagerApiException {
    ContentFlowList list = api.listFlows(programId, start, limit);
    return list.getEmbedded() == null ?
        Collections.emptyList() :
        list.getEmbedded().getContentFlows().stream().map(cf -> new ContentFlowImpl(cf, this)).collect(Collectors.toList());
  }

  @Override
  public ContentFlow startFlow(String programId, String id, String srcEnvironmentId, String destEnvironmentId, boolean includeAcl) throws CloudManagerApiException {
    ContentFlowInput cfi = new ContentFlowInput()
        .contentSetId(id)
        .destProgramId(programId)
        .destEnvironmentId(destEnvironmentId)
        .tier(Environment.Tier.AUTHOR.getValue())
        .includeACL(includeAcl);
    return new ContentFlowImpl(api.createFlow(programId, srcEnvironmentId, cfi), this);
  }

  @Override
  public ContentFlow getFlow(String programId, String id) throws CloudManagerApiException {
    return new ContentFlowImpl(api.getFlow(programId, id), this);
  }

  @Override
  public ContentFlow cancelFlow(String programId, String id) throws CloudManagerApiException {
    return new ContentFlowImpl(api.cancelFlow(programId, id), this);
  }

  public io.adobe.cloudmanager.impl.generated.ContentSet internalUpdate(String programId, String id, String name, String description, Collection<PathDefinition> definitions) throws CloudManagerApiException {
    io.adobe.cloudmanager.impl.generated.ContentSet current = api.get(programId, id);
    NewContentSet ncs = new NewContentSet();
    ncs.setName(name != null ? name : current.getName());
    ncs.setDescription(description != null ? description : current.getDescription());
    if (definitions != null && !definitions.isEmpty()) {
      definitions.forEach(pd -> ncs.addPathsItem(new ContentSetPath().path(pd.getPath()).excluded(new ArrayList<>(pd.getExcluded()))));
    } else {
      ncs.setPaths(current.getPaths());
    }
    return api.update(programId, id, ncs);
  }

  interface FeignApi {
    @RequestLine("GET /api/program/{programId}/contentSets")
    ContentSetList list(@Param("programId") String programId) throws CloudManagerApiException;

    @RequestLine("GET /api/program/{programId}/contentSets?start={start}&limit={limit}")
    ContentSetList list(@Param("programId") String programId, @Param("start") int start, @Param("limit") int limit) throws CloudManagerApiException;

    @RequestLine("POST /api/program/{programId}/contentSets")
    io.adobe.cloudmanager.impl.generated.ContentSet create(@Param("programId") String programId, NewContentSet contentSet) throws CloudManagerApiException;

    @RequestLine("GET /api/program/{programId}/contentSet/{id}")
    io.adobe.cloudmanager.impl.generated.ContentSet get(@Param("programId") String programId, @Param("id") String id) throws CloudManagerApiException;

    @RequestLine("PUT /api/program/{programId}/contentSet/{id}")
    io.adobe.cloudmanager.impl.generated.ContentSet update(@Param("programId") String programId, @Param("id") String id, NewContentSet contentSet) throws CloudManagerApiException;

    @RequestLine("DELETE /api/program/{programId}/contentSet/{id}")
    io.adobe.cloudmanager.impl.generated.ContentSet delete(@Param("programId") String programId, @Param("id") String id) throws CloudManagerApiException;

    @RequestLine("GET /api/program/{programId}/contentFlows")
    ContentFlowList listFlows(@Param("programId") String programId) throws CloudManagerApiException;

    @RequestLine("GET /api/program/{programId}/contentFlows?start={start}&limit={limit}")
    ContentFlowList listFlows(@Param("programId") String programId, @Param("start") int start, @Param("limit") int limit) throws CloudManagerApiException;

    @RequestLine("POST /api/program/{programId}/environment/{environmentId}/contentFlow")
    io.adobe.cloudmanager.impl.generated.ContentFlow createFlow(@Param("programId") String programId, @Param("environmentId") String environmentId, ContentFlowInput input) throws CloudManagerApiException;

    @RequestLine("GET /api/program/{programId}/contentFlow/{id}")
    io.adobe.cloudmanager.impl.generated.ContentFlow getFlow(@Param("programId") String programId, @Param("id") String id) throws CloudManagerApiException;

    @RequestLine("DELETE /api/program/{programId}/contentFlow/{id}")
    io.adobe.cloudmanager.impl.generated.ContentFlow cancelFlow(@Param("programId") String programId, @Param("id") String id) throws CloudManagerApiException;
  }
}