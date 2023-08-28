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

import io.adobe.cloudmanager.Metric;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;

@ToString
@EqualsAndHashCode(callSuper = false)
public class MetricImpl extends io.adobe.cloudmanager.impl.generated.Metric implements Metric {

  @Delegate
  private final io.adobe.cloudmanager.impl.generated.Metric delegate;

  public MetricImpl(io.adobe.cloudmanager.impl.generated.Metric delegate) {
    this.delegate = delegate;
  }

  @Override
  public Severity getSev() {
    return Severity.fromValue(getSeverity().getValue());
  }

  @Override
  public Comparator getComp() {
    return Comparator.fromValue(getComparator().getValue());
  }
}
