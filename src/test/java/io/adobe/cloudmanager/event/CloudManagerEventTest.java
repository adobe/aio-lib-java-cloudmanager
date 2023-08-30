package io.adobe.cloudmanager.event;

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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.adobe.cloudmanager.CloudManagerApiException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static io.adobe.cloudmanager.event.CloudManagerEvent.*;
import static org.junit.jupiter.api.Assertions.*;

public class CloudManagerEventTest {

  private ObjectMapper mapper = new ObjectMapper();

  @Test
  public void parseEventInvalidType() {
    assertThrows(IllegalArgumentException.class, () -> parseEvent("", PipelineExecution.class));
  }

  @Test
  public void parseEventInvalidContent() {
    CloudManagerApiException e = assertThrows(CloudManagerApiException.class, () -> parseEvent("", PipelineExecutionStartEvent.class));
    assertTrue(StringUtils.contains(e.getMessage(),"Unable to process event:"));
  }

  @Test
  public void parseEventPipelineExecutionStartEvent() throws Exception {
    PipelineExecutionStartEvent event = new PipelineExecutionStartEvent()
        .event(
            new PipelineExecutionStartEventEvent()
                ._atType(STARTED_EVENT_TYPE)
                .xdmEventEnvelopeobjectType(PIPELINE_EXECUTION_TYPE)
        );
    assertEquals(event, parseEvent(mapper.writeValueAsString(event), PipelineExecutionStartEvent.class));
  }

  @Test
  public void parseEventPipelineExecutionStepStartEvent() throws Exception {
    PipelineExecutionStepStartEvent event = new PipelineExecutionStepStartEvent()
        .event(
            new PipelineExecutionStepStartEventEvent()
                ._atType(STARTED_EVENT_TYPE)
                .xdmEventEnvelopeobjectType(PIPELINE_STEP_STATE_TYPE)
        );
    assertEquals(event, parseEvent(mapper.writeValueAsString(event), PipelineExecutionStepStartEvent.class));
  }

  @Test
  public void parseEventPipelineExecutionStepWaitingEvent() throws Exception {
    PipelineExecutionStepWaitingEvent event = new PipelineExecutionStepWaitingEvent()
        .event(
            new PipelineExecutionStepStartEventEvent()
                ._atType(WAITING_EVENT_TYPE)
                .xdmEventEnvelopeobjectType(PIPELINE_STEP_STATE_TYPE)
        );
    assertEquals(event, parseEvent(mapper.writeValueAsString(event), PipelineExecutionStepWaitingEvent.class));

  }

  @Test
  public void parseEventPipelineExecutionStepEndEvent() throws Exception {
    PipelineExecutionStepEndEvent event = new PipelineExecutionStepEndEvent()
        .event(
            new PipelineExecutionStepStartEventEvent()
                ._atType(ENDED_EVENT_TYPE)
                .xdmEventEnvelopeobjectType(PIPELINE_STEP_STATE_TYPE)
        );
    assertEquals(event, parseEvent(mapper.writeValueAsString(event), PipelineExecutionStepEndEvent.class));
  }

  @Test
  public void parseEventPipelineExecutionEndEvent() throws Exception {
    PipelineExecutionEndEvent event = new PipelineExecutionEndEvent()
        .event(
            new PipelineExecutionEndEventEvent()
                ._atType(ENDED_EVENT_TYPE)
                .xdmEventEnvelopeobjectType(PIPELINE_EXECUTION_TYPE)
        );
    assertEquals(event, parseEvent(mapper.writeValueAsString(event), PipelineExecutionEndEvent.class));
  }


  @Test
  public void validSignature() throws Exception {

    String body = "Body";
    String clientSecret = "Client Secret";
    String sha256 = "HmacSHA256";
    Mac mac = Mac.getInstance(sha256);
    mac.init(new SecretKeySpec(clientSecret.getBytes(StandardCharsets.UTF_8), sha256));
    String signature = Base64.getEncoder().encodeToString(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));

    Assertions.assertTrue(CloudManagerEvent.isValidSignature(body, signature, clientSecret));
  }

  @Test
  public void typeFromInvalidContent() {
    CloudManagerApiException e = assertThrows(CloudManagerApiException.class, () -> EventType.from(""));
    assertTrue(StringUtils.contains(e.getMessage(),"Unable to process event:"));
  }

  @Test
  public void typeFromUnknownData() throws Exception {
    PipelineExecutionStepWaitingEvent event = new PipelineExecutionStepWaitingEvent()
        .event(
            new PipelineExecutionStepStartEventEvent()
            ._atType(WAITING_EVENT_TYPE)
            .xdmEventEnvelopeobjectType(PIPELINE_EXECUTION_TYPE)
        );
    assertNull(EventType.from(mapper.writeValueAsString(event)));
  }

  @Test
  public void typeFromPipelineExecutionStartEvent() throws Exception {
    PipelineExecutionStartEvent event = new PipelineExecutionStartEvent()
        .event(
            new PipelineExecutionStartEventEvent()
              ._atType(STARTED_EVENT_TYPE)
              .xdmEventEnvelopeobjectType(PIPELINE_EXECUTION_TYPE)
        );
    assertEquals(EventType.PIPELINE_STARTED, EventType.from(mapper.writeValueAsString(event)));
  }

  @Test
  public void typeFromPipelineExecutionEndEvent() throws Exception {
    PipelineExecutionEndEvent event = new PipelineExecutionEndEvent()
        .event(
            new PipelineExecutionEndEventEvent()
                ._atType(ENDED_EVENT_TYPE)
                .xdmEventEnvelopeobjectType(PIPELINE_EXECUTION_TYPE)
        );
    assertEquals(EventType.PIPELINE_ENDED, EventType.from(mapper.writeValueAsString(event)));
  }

  @Test
  public void typeFromPipelineExecutionStepStartEvent() throws Exception {
    PipelineExecutionStepStartEvent event = new PipelineExecutionStepStartEvent()
        .event(
            new PipelineExecutionStepStartEventEvent()
                ._atType(STARTED_EVENT_TYPE)
                .xdmEventEnvelopeobjectType(PIPELINE_STEP_STATE_TYPE)
        );
    assertEquals(EventType.STEP_STARTED, EventType.from(mapper.writeValueAsString(event)));
  }

  @Test
  public void typeFromPipelineExecutionStepWaitingEvent() throws Exception {
    PipelineExecutionStepWaitingEvent event = new PipelineExecutionStepWaitingEvent()
        .event(
            new PipelineExecutionStepStartEventEvent()
                ._atType(WAITING_EVENT_TYPE)
                .xdmEventEnvelopeobjectType(PIPELINE_STEP_STATE_TYPE)
        );
    assertEquals(EventType.STEP_WAITING, EventType.from(mapper.writeValueAsString(event)));
  }

  @Test
  public void typeFromPipelineExecutionStepEndEvent() throws Exception {
    PipelineExecutionStepEndEvent event = new PipelineExecutionStepEndEvent()
        .event(
            new PipelineExecutionStepStartEventEvent()
                ._atType(ENDED_EVENT_TYPE)
                .xdmEventEnvelopeobjectType(PIPELINE_STEP_STATE_TYPE)
        );
    assertEquals(EventType.STEP_ENDED, EventType.from(mapper.writeValueAsString(event)));
  }

  @Test
  public void invalidSignature() throws Exception {
    assertFalse(CloudManagerEvent.isValidSignature("Body", "Signature", "Client Secret"));
  }

}
