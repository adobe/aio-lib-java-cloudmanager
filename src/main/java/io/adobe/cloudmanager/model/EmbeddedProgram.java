package io.adobe.cloudmanager.model;

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

import io.adobe.cloudmanager.CloudManagerApi;
import io.adobe.cloudmanager.CloudManagerApiException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;

/**
 * Extension to the Swagger generated Embedded Program. Provides convenience methods for frequently used APIs
 */
@ToString
@EqualsAndHashCode
public class EmbeddedProgram extends io.adobe.cloudmanager.generated.model.EmbeddedProgram {
  private static final long serialVersionUID = 1L;

  public EmbeddedProgram(io.adobe.cloudmanager.generated.model.EmbeddedProgram delegate, CloudManagerApi client) {
    this.delegate = delegate;
    this.client = client;
  }

  /**
   * Link to this program.
   *
   * @return the link to this program.
   */
  public String getSelfLink() {
    return delegate.getLinks().getSelf().getHref();
  }

  @Delegate
  private final io.adobe.cloudmanager.generated.model.EmbeddedProgram delegate;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private final CloudManagerApi client;

  public void delete() throws CloudManagerApiException {
    client.deleteProgram(this);
  }
}
