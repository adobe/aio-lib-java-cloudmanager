package com.adobe.aio.cloudmanager.feign;

/*-
 * #%L
 * Adobe Cloud Manager Client Library
 * %%
 * Copyright (C) 2020 - 2022 Adobe Inc.
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

import com.adobe.aio.cloudmanager.CloudManagerApi;
import com.adobe.aio.cloudmanager.CloudManagerApiException;
import com.adobe.aio.cloudmanager.Program;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;

@ToString
@EqualsAndHashCode(callSuper = true)
public class ProgramImpl extends io.adobe.cloudmanager.generated.model.EmbeddedProgram implements Program {
  private static final long serialVersionUID = 1L;

  @Delegate
  private final io.adobe.cloudmanager.generated.model.EmbeddedProgram delegate;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private final CloudManagerApi client;
  
  public ProgramImpl(io.adobe.cloudmanager.generated.model.EmbeddedProgram delegate, CloudManagerApi client) {
    this.delegate = delegate;
    this.client = client;
  }

  public void delete() throws CloudManagerApiException {
    client.deleteProgram(this);
  }
}
