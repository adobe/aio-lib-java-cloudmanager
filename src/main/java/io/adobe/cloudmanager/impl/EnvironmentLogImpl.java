package io.adobe.cloudmanager.impl;

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

import io.adobe.cloudmanager.EnvironmentLog;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Delegate;

@ToString
@EqualsAndHashCode
public class EnvironmentLogImpl extends io.adobe.cloudmanager.generated.model.EnvironmentLog implements EnvironmentLog {

  private static final long serialVersionUID = 1L;

  public EnvironmentLogImpl(io.adobe.cloudmanager.generated.model.EnvironmentLog log) {
    this.delegate = log;
  }

  @Delegate
  io.adobe.cloudmanager.generated.model.EnvironmentLog delegate;
  @Getter @Setter int index;
  @Getter @Setter String path;
  @Getter @Setter String url;
}
