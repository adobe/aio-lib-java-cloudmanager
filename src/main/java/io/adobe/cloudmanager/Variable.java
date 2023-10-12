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
 * limitations under the e.
 * #L%
 */

import javax.validation.constraints.NotNull;

import io.adobe.cloudmanager.impl.VariableImpl;
import lombok.Getter;

public interface Variable {

  /**
   * Returns the name of this variable.
   *
   * @return variable name
   */
  String getName();

  /**
   * Returns the value of this variable, if not a secret. Secrets are always blank when fetch from remote system.
   *
   * @return the value or blank
   */
  String getValue();

  /**
   * Returns the variable type
   *
   * @return the variable type
   */
  Type getVarType();

  /**
   * Returns the service associated with the variable
   *
   * @return the service
   */
  String getService();

  /**
   * Used to build a Variable definition to pass to API creation operations.
   *
   * @return a Variable builder
   */
  static Builder builder() {
    return new Builder();
  }

  class Builder {
    private final io.adobe.cloudmanager.impl.generated.Variable delegate;

    public Builder() {
      delegate = new io.adobe.cloudmanager.impl.generated.Variable();
    }

    public Builder name(@NotNull String name) {
      delegate.name(name);
      return this;
    }

    public Builder value(@NotNull String value) {
      delegate.value(value);
      return this;
    }

    public Builder type(@NotNull Type type) {
      delegate.type(io.adobe.cloudmanager.impl.generated.Variable.TypeEnum.fromValue(type.value));
      return this;
    }

    public Builder service(@NotNull String service) {
      delegate.service(service);
      return this;
    }

    public Variable build() {
      return new VariableImpl(delegate);
    }
  }

  @Getter
  enum Type {
    STRING("string"),
    SECRET("secretString");

    private final String value;

    Type(String value) {
      this.value = value;
    }

    public static Type fromValue(String text) {
      for (Type b : Type.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }

    @Override
    public String toString() {
      return String.valueOf(this.value);
    }
  }
}
