package io.adobe.cloudmanager.impl.environment;

/*-
 * #%L
 * Adobe Cloud Manager Client Library
 * %%
 * Copyright (C) 2020 - 2023 Adobe Inc.
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

import io.adobe.cloudmanager.EnvironmentApi;
import io.adobe.cloudmanager.RegionDeployment;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;

@ToString
@EqualsAndHashCode(callSuper = false)
public class RegionDeploymentImpl extends io.adobe.cloudmanager.impl.generated.RegionDeployment implements RegionDeployment {

  private static final long serialVersionUID = 1L;

  @Delegate
  private final io.adobe.cloudmanager.impl.generated.RegionDeployment delegate;
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private final EnvironmentApi client;

  public RegionDeploymentImpl(io.adobe.cloudmanager.impl.generated.RegionDeployment delegate, EnvironmentApi client) {
    this.delegate = delegate;
    this.client = client;
  }

  @Override
  public Type getDeployType() {
    return Type.valueOf(delegate.getType().name());
  }

}
