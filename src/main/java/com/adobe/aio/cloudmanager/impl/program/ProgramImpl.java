package com.adobe.aio.cloudmanager.impl.program;

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

import java.util.Collection;

import com.adobe.aio.cloudmanager.Region;
import com.adobe.aio.cloudmanager.impl.generated.EmbeddedProgram;
import com.adobe.aio.cloudmanager.CloudManagerApiException;
import com.adobe.aio.cloudmanager.Program;
import com.adobe.aio.cloudmanager.ProgramApi;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;

/**
 * Extension to the Swagger generated Embedded Program. Provides convenience methods for frequently used APIs
 */
@ToString
@EqualsAndHashCode(callSuper = false)
public class ProgramImpl extends EmbeddedProgram implements Program {
  private static final long serialVersionUID = 1L;

  @Delegate
  private final EmbeddedProgram delegate;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private final ProgramApi client;

  public ProgramImpl(EmbeddedProgram delegate, ProgramApi client) {
    this.delegate = delegate;
    this.client = client;
  }
  public void delete() throws CloudManagerApiException {
    client.delete(this);
  }

  @Override
  public Collection<Region> listRegions() throws CloudManagerApiException {
    return client.listRegions(getId());
  }
}
