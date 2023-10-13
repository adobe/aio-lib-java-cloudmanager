package io.adobe.cloudmanager.impl.environment;

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
import lombok.Value;
import lombok.experimental.Delegate;

@ToString
@EqualsAndHashCode(callSuper = false)
public class EnvironmentLogImpl extends io.adobe.cloudmanager.impl.generated.EnvironmentLog implements EnvironmentLog {

  private static final long serialVersionUID = 1L;

  @Delegate
  private final io.adobe.cloudmanager.impl.generated.EnvironmentLog delegate;
  private final String path;

  public EnvironmentLogImpl(io.adobe.cloudmanager.impl.generated.EnvironmentLog delegate) {
    this.delegate = delegate;
    this.path = null;
  }

  public EnvironmentLogImpl(io.adobe.cloudmanager.impl.generated.EnvironmentLog delegate, String downloadPath) {
    this.delegate = delegate;
    this.path = downloadPath;
  }

  public String getDownloadPath() {
    return this.path;
  }

  public String getUrl() {
    return delegate.getLinks().getHttpnsAdobeComadobecloudrellogsdownload().getHref();
  }
}
