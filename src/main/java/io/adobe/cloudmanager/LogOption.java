package io.adobe.cloudmanager;

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

import javax.validation.constraints.NotNull;

import io.adobe.cloudmanager.impl.environment.LogOptionImpl;
import io.adobe.cloudmanager.impl.generated.LogOptionRepresentation;

public interface LogOption {

  /**
   * Name of the service in environment. Example: author
   *
   * @return service
   **/
  String getService();

  /**
   * Name of the log for service in environment. Example: aemerror
   *
   * @return name
   **/
  String getName();

  /**
   * Builder to create new instances of a LogOption
   *
   * @return a LogOption builder
   */
  static Builder builder() {
    return new Builder();
  }

  /**
   * Builds new instances of LogOptions
   */
  class Builder {
    private final LogOptionRepresentation delegate;

    private Builder() {
      delegate = new LogOptionRepresentation();
    }

    public Builder service(@NotNull String service) {
      delegate.service(service);
      return this;
    }

    public Builder name(@NotNull String name) {
      delegate.name(name);
      return this;
    }

    public LogOption build() {
      return new LogOptionImpl(delegate);
    }
  }
}
