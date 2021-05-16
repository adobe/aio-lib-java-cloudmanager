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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.generated.invoker.JSON;

public class CloudManagerEvent {

  public static final String SIGNATURE_HEADER = "x-adobe-signature";
  public static final String STARTED_EVENT_TYPE = "https://ns.adobe.com/experience/cloudmanager/event/started";
  public static final String WAITING_EVENT_TYPE = "https://ns.adobe.com/experience/cloudmanager/event/waiting";
  public static final String ENDED_EVENT_TYPE = "https://ns.adobe.com/experience/cloudmanager/event/ended";
  public static final String PIPELINE_EXECUTION_TYPE = "https://ns.adobe.com/experience/cloudmanager/pipeline-execution";
  public static final String PIPELINE_STEP_STATE_TYPE = "https://ns.adobe.com/experience/cloudmanager/execution-step-state";

  private static final String HMAC_ALG = "HmacSHA256";

  /**
   * Attempts to convert the provided string source into the specified event type T.
   * <p>
   * T must be one of:
   * - {@link PipelineExecutionStartEvent}
   * - {@link PipelineExecutionStepStartEvent}
   * - {@link PipelineExecutionStepWaitingEvent}
   * - {@link PipelineExecutionStepEndEvent}
   * - {@link PipelineExecutionEndEvent}
   *
   * @param source String representation of the event
   * @param type   class type of event
   * @param <T>    the type of event
   * @return a fully populated event of the specified type
   * @throws CloudManagerApiException if an error occurs during parsing
   */
  @NotNull
  public static <T> T parseEvent(String source, Class<T> type) throws CloudManagerApiException {

    if (EventType.from(type) == null ) {
      throw new IllegalArgumentException(String.format("Unknown event type: %s", type));
    }
    try {
      return new JSON().getContext(type).readValue(source, type);
    } catch (JsonProcessingException e) {
      throw new CloudManagerApiException(CloudManagerApiException.ErrorType.PROCESS_EVENT, e.getMessage());
    }
  }

  /**
   * Validates an event against a signature.
   *
   * @param eventBody    the original String representation of the event, in UTF-8 encoding
   * @param signature    the digest from the header
   * @param clientSecret the client secret signature for calculations
   * @return true if the body signature is valid, false otherwise
   * @throws CloudManagerApiException when an error occurs during processing
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/docs.html#!AdobeDocs/cloudmanager-api-docs/master/tutorial/2-webhook-signature-validation.md">Webhook Signature Validation</a>
   */
  public static boolean isValidSignature(@NotNull String eventBody, @NotNull String signature, @NotNull String clientSecret) throws CloudManagerApiException {
    try {
      Mac mac = Mac.getInstance(HMAC_ALG);
      mac.init(new SecretKeySpec(clientSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALG));
      return signature.equals(Base64.getEncoder().encodeToString(mac.doFinal(eventBody.getBytes(StandardCharsets.UTF_8))));
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new CloudManagerApiException(CloudManagerApiException.ErrorType.VALIDATE_EVENT, e.getLocalizedMessage());
    }
  }


  public enum EventType {
    PIPELINE_STARTED(PipelineExecutionStartEvent.class, STARTED_EVENT_TYPE, PIPELINE_EXECUTION_TYPE),
    PIPELINE_ENDED(PipelineExecutionEndEvent.class, ENDED_EVENT_TYPE, PIPELINE_EXECUTION_TYPE),
    STEP_STARTED(PipelineExecutionStepStartEvent.class, STARTED_EVENT_TYPE, PIPELINE_STEP_STATE_TYPE),
    STEP_WAITING(PipelineExecutionStepWaitingEvent.class, WAITING_EVENT_TYPE, PIPELINE_STEP_STATE_TYPE),
    STEP_ENDED(PipelineExecutionStepEndEvent.class, ENDED_EVENT_TYPE, PIPELINE_STEP_STATE_TYPE);

    private final Class clazz;
    private final String eventType;
    private final String objectType;

    EventType(Class<?> clazz, String eventType, String objectType) {
      this.clazz = clazz;
      this.eventType = eventType;
      this.objectType = objectType;
    }

    public Class<?> getClazz() {
      return this.clazz;
    }

    public String getEventType() {
      return this.eventType;
    }

    public String getObjectType() {
      return this.objectType;
    }

    /**
     * Attempts to determine which event type from the provided JSON String.
     *
     * @param source event JSON string
     * @return Event class type or null
     * @throws CloudManagerApiException if an error occurs during parsing
     */
    public static EventType from(String source) throws CloudManagerApiException {
      try {
        // Any object will do, to get the root of the object tree.
        PipelineExecutionStartEvent tester = new JSON().getContext(PipelineExecutionStartEvent.class).readValue(source, PipelineExecutionStartEvent.class);
        PipelineExecutionStartEventEvent event = tester.getEvent();
        return Arrays.stream(EventType.values()).filter(t -> t.getObjectType().equals(event.getXdmEventEnvelopeobjectType()) && t.getEventType().equals(event.getAtType())).findFirst().orElse(null);
      } catch (JsonProcessingException e) {
        throw new CloudManagerApiException(CloudManagerApiException.ErrorType.PROCESS_EVENT, e.getMessage());
      }
    }

    /**
     * Returns the EventType for the specified Class.
     *
     * Class must be one of:
     * <ul>
     *  <li>{@link PipelineExecutionStartEvent}</li>
     *  <li>{@link PipelineExecutionStepStartEvent}</li>
     *  <li>{@link PipelineExecutionStepWaitingEvent}</li>
     *  <li>{@link PipelineExecutionStepEndEvent}</li>
     *  <li>{@link PipelineExecutionEndEvent}</li>
     * </ul>
     *
     * @param clazz Event class type
     * @return event type or null
     */
    public static EventType from(Class<?> clazz) {
      return Arrays.stream(EventType.values()).filter(t -> t.getClazz() == clazz).findFirst().orElse(null);
    }
  }
}
