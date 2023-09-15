package io.adobe.cloudmanager.impl.pipeline.execution;

import org.apache.commons.io.FilenameUtils;

import io.adobe.cloudmanager.Artifact;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.PipelineExecutionApi;
import io.adobe.cloudmanager.PipelineExecutionStepState;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class ArtifactImpl implements Artifact {

  private final io.adobe.cloudmanager.impl.generated.Artifact delegate;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private final PipelineExecutionApi client;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private final PipelineExecutionStepState step;

  public ArtifactImpl(io.adobe.cloudmanager.impl.generated.Artifact delegate, PipelineExecutionApi client, PipelineExecutionStepState step) {
    this.delegate = delegate;
    this.client = client;
    this.step = step;
  }

  @Override
  public String getId() {
    return delegate.getId();
  }

  @Override
  public String getFileName() {
    return FilenameUtils.getName(delegate.getFile());
  }

  @Override
  public String getType() {
    return delegate.getType().name();
  }

  @Override
  public String getMd5() {
    return delegate.getMd5();
  }

  @Override
  public String getDownloadUrl() throws CloudManagerApiException {
    return client.getArtifactDownloadUrl(step, getId());
  }
}
