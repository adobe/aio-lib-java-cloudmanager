package com.adobe.aio.cloudmanager.feign;

/*-
 * #%L
 * Adobe Cloud Manager Client Library
 * %%
 * Copyright (C) 2020 - 2021 Adobe Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.stream.Collectors;

import com.adobe.aio.cloudmanager.CloudManagerApiException;
import com.adobe.aio.cloudmanager.Metric;
import com.adobe.aio.cloudmanager.PipelineExecution;
import com.adobe.aio.cloudmanager.StepAction;
import com.adobe.aio.cloudmanager.generated.model.HalLink;
import com.adobe.aio.cloudmanager.generated.model.PipelineExecutionStepState;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.Delegate;

/**
 * Extension to the Swagger generated Pipeline. Provides convenience methods for frequently used APIs
 */
@ToString
@EqualsAndHashCode(callSuper = false)
public class PipelineExecutionImpl extends com.adobe.aio.cloudmanager.generated.model.PipelineExecution implements PipelineExecution {

  private static final long serialVersionUID = 1L;

  public static final String ACTION_APPROVAL = "approval";
  public static final String ACTION_SCHEDULE = "schedule";
  public static final String ACTION_DEPLOY = "deploy";

  private static final String GENERATE_BODY = "Unable to generate request body: %s";
  
  @Delegate
  private final com.adobe.aio.cloudmanager.generated.model.PipelineExecution delegate;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private final CloudManagerApiImpl client;
  
  public PipelineExecutionImpl(com.adobe.aio.cloudmanager.generated.model.PipelineExecution delegate, CloudManagerApiImpl client) {
    this.delegate = delegate;
    this.client = client;
  }

  @Override
  public void advance() throws CloudManagerApiException {
    client.advanceExecution(this);
  }

  @Override
  public void cancel() throws CloudManagerApiException {
    client.cancelExecution(this);
  }

  public StepFormData getAdvanceLinkAndBody() throws CloudManagerApiException {
   PipelineExecutionStepStateImpl waitingStep = client.getWaitingStep(this);
    HalLink link = waitingStep.getLinks().getHttpnsAdobeComadobecloudrelpipelineadvance();
    if (link == null) {
      throw new CloudManagerApiException(String.format("Cannot find an advance link for the current step (%s).", waitingStep.getAction()));
    }
    return new StepFormData(link.getHref(), getAdvanceBody(waitingStep));
  }

  private String getAdvanceBody(PipelineExecutionStepStateImpl waitingStep) throws CloudManagerApiException {
    StringWriter writer = new StringWriter();
    JsonFactory jsonFactory = new JsonFactory();
    try {
      JsonGenerator gen = jsonFactory.createGenerator(writer);
      gen.writeStartObject();
      if (ACTION_APPROVAL.equals(waitingStep.getAction())) {
        gen.writeBooleanField("approved", true);
      } else {
        gen.writeFieldName("metrics");
        gen.writeStartArray();
        buildMetricsOverride(gen);
        gen.writeEndArray();
      }
      gen.writeEndObject();
      gen.close();
      return writer.toString();
    } catch (IOException e) {
      throw new CloudManagerApiException(String.format(GENERATE_BODY, e.getLocalizedMessage()));
    }
  }
  
  public StepFormData getCancelLinkAndBody() throws CloudManagerApiException {
    PipelineExecutionStepStateImpl step = client.getCurrentStep(this);
    HalLink link;
    if (PipelineExecutionStepState.StatusEnum.WAITING.equals(step.getStatus()) &&
        ACTION_DEPLOY.equals(step.getAction())) {
      link = step.getLinks().getHttpnsAdobeComadobecloudrelpipelineadvance();
    } else {
      link = step.getLinks().getHttpnsAdobeComadobecloudrelpipelinecancel();
    }
    if (link == null) {
      throw new CloudManagerApiException(String.format("Cannot find a cancel link for the current step (%s). Step may not be cancellable.", step.getAction()));
    }
    return new StepFormData(link.getHref(), getCancelBody(step));
  }
  private String getCancelBody(PipelineExecutionStepStateImpl step) throws CloudManagerApiException {
    StringWriter writer = new StringWriter();
    JsonFactory jsonFactory = new JsonFactory();

    try {
      JsonGenerator gen = jsonFactory.createGenerator(writer);
      gen.writeStartObject();
      if (ACTION_APPROVAL.equals(step.getAction())) {
        gen.writeBooleanField("approved", false);
      } else if (com.adobe.aio.cloudmanager.generated.model.PipelineExecutionStepState.StatusEnum.WAITING.equals(step.getStatus()) &&
          !ACTION_SCHEDULE.equals(step.getAction()) && !ACTION_DEPLOY.equals(step.getAction())) {
        gen.writeBooleanField("override", false);
      } else if (com.adobe.aio.cloudmanager.generated.model.PipelineExecutionStepState.StatusEnum.WAITING.equals(step.getStatus()) &&
          ACTION_DEPLOY.equals(step.getAction())) {
        gen.writeBooleanField("resume", false);
      } else {
        gen.writeBooleanField("cancel", true);
      }
      gen.writeEndObject();
      gen.close();
      return writer.toString();
    } catch (IOException e) {
      throw new CloudManagerApiException(String.format(GENERATE_BODY, e.getMessage()));
    }
  }

  @Override
  public Status getStatusState() {
    return Status.fromValue(getStatus().getValue());
  }

  /*
   * Builds the body needed to override any blocking metrics for advancing the pipeline.
   */
  private void buildMetricsOverride(JsonGenerator gen) throws CloudManagerApiException, IOException {
    Collection<Metric> metrics = client.getQualityGateResults(this, StepAction.codeQuality.name());
    Collection<Metric> failed = metrics.stream().filter(m -> !m.isPassed() && Metric.Severity.IMPORTANT.equals(m.getSev())).collect(Collectors.toList());
    for (Metric m : failed) {
      gen.writeStartObject();
      gen.writeStringField("kpi", m.getKpi());
      gen.writeBooleanField("override", true);
      gen.writeEndObject();
    }
  }
  
  @Value
  protected class StepFormData {
    String href;
    String body;
    
  } 
}
