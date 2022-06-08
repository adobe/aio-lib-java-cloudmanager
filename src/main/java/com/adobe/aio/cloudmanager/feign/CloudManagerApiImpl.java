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

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import com.adobe.aio.cloudmanager.CloudManagerApi;
import com.adobe.aio.cloudmanager.CloudManagerApiException;
import com.adobe.aio.cloudmanager.Program;
import io.adobe.cloudmanager.generated.model.ProgramList;
import lombok.NonNull;

public class CloudManagerApiImpl implements CloudManagerApi {

  private final String baseUrl;
  private final ProgramApiImpl programApi;
  
  public CloudManagerApiImpl(String baseUrl, ProgramApiImpl programApi) {
    this.baseUrl = baseUrl;
    this.programApi = programApi;
  }

  @Override
  public @NonNull Collection<Program> listPrograms() throws CloudManagerApiException {
    ProgramList programList = programApi.listPrograms();
    return programList.getEmbedded() == null ?
        Collections.emptyList() :
        programList.getEmbedded().getPrograms().stream().map(p -> new ProgramImpl(p, this)).collect(Collectors.toList());
  }

  @Override
  public void deleteProgram(String programId) throws CloudManagerApiException {
    programApi.deleteProgram(programId);
  }

  @Override
  public void deleteProgram(@NonNull Program program) throws CloudManagerApiException {
    deleteProgram(program.getId());
  }
}
