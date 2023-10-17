package io.adobe.cloudmanager.impl.content;

import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.ContentFlow;
import io.adobe.cloudmanager.ContentSetApi;
import io.adobe.cloudmanager.impl.generated.ContentFlowResultDetails;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;

import static io.adobe.cloudmanager.Environment.*;

public class ContentFlowImpl extends io.adobe.cloudmanager.impl.generated.ContentFlow implements ContentFlow {

  private static final long serialVersionUID = 1L;

  @Delegate
  private final io.adobe.cloudmanager.impl.generated.ContentFlow delegate;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private final ContentSetApi client;

  private Results exportResults;
  private Results importResults;

  public ContentFlowImpl(io.adobe.cloudmanager.impl.generated.ContentFlow delegate, ContentSetApi client) {
    this.delegate = delegate;
    this.client = client;
  }

  @Override
  public Tier getEnvironmentTier() {
    return Tier.fromValue(delegate.getTier());
  }

  @Override
  public Results getExportResults() {
    if (exportResults == null) {
      ContentFlowResultDetails cfrd = delegate.getResultDetails().getExportResult();
      exportResults = new Results(cfrd.getErrorCode(), cfrd.getMessage(), cfrd.getDetails());
    }
    return exportResults;
  }

  @Override
  public Results getImportResults() {
    if (importResults == null) {
      ContentFlowResultDetails cfrd = delegate.getResultDetails().getImportResult();
      importResults = new Results(cfrd.getErrorCode(), cfrd.getMessage(), cfrd.getDetails());

    }
    return importResults;
  }

  @Override
  public void cancel() throws CloudManagerApiException {
    client.cancelFlow(getDestProgramId(), getId());
  }
}
