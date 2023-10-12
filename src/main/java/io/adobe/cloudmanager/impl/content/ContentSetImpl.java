package io.adobe.cloudmanager.impl.content;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.ContentFlow;
import io.adobe.cloudmanager.ContentSet;
import io.adobe.cloudmanager.ContentSetApi;
import io.adobe.cloudmanager.impl.generated.ContentSetPath;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;

@ToString
@EqualsAndHashCode(callSuper = false)
public class ContentSetImpl extends io.adobe.cloudmanager.impl.generated.ContentSet implements ContentSet {

  private static final long serialVersionUID = 1L;

  @Delegate
  private io.adobe.cloudmanager.impl.generated.ContentSet delegate;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private final ContentSetApiImpl client;

  private Collection<PathDefinition> pathDefinitions;

  public ContentSetImpl(io.adobe.cloudmanager.impl.generated.ContentSet delegate, ContentSetApiImpl client) {
    this.delegate = delegate;
    this.client = client;
  }

  @Override
  public Collection<PathDefinition> getPathDefinitions() {
    if (pathDefinitions == null) {
      pathDefinitions = new ArrayList<>();
      delegate.getPaths().forEach(p -> pathDefinitions.add(new PathDefinition(p.getPath(), new HashSet<>(p.getExcluded()))));
    }
    return pathDefinitions;
  }

  @Override
  public void update(String name, String description, Collection<PathDefinition> definitions) throws CloudManagerApiException {
    delegate = client.internalUpdate(getProgramId(), getId(), name, description, definitions);
    pathDefinitions = null;
  }

  @Override
  public void delete() throws CloudManagerApiException {
    client.delete(getProgramId(), getId());
  }

  @Override
  public ContentFlow startFlow(String srcEnvironmentId, String destEnvironment, boolean includeAcl) throws CloudManagerApiException {
    return client.startFlow(getProgramId(), getId(), srcEnvironmentId, destEnvironment, includeAcl);
  }
}
