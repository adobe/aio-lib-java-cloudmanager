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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;

import org.mockserver.logging.MockServerLogger;
import org.mockserver.mock.Expectation;
import org.mockserver.serialization.ExpectationSerializer;
import org.mockserver.server.initialize.ExpectationInitializer;

public class CloudManagerExpectationInitializer implements ExpectationInitializer {

  @Override
  public Expectation[] initializeExpectations() {

    final List<Expectation> expectations = new ArrayList<>();
    ExpectationSerializer serializer = new ExpectationSerializer(new MockServerLogger((CloudManagerExpectationInitializer.class)));

    List<String> files = new ArrayList<>();
    files.add("general-program-setup.json");
    files.addAll(ProgramsTest.getTestExpectationFiles());
    files.addAll(EnvironmentsTest.getTestExpectationFiles());
    files.addAll(PipelinesTest.getTestExpectationFiles());
    files.addAll(ExecutionsTest.getTestExpectationFiles());

    files.stream().forEach(s -> {
      try {
        InputStream is = CloudManagerExpectationInitializer.class.getClassLoader().getResourceAsStream(s);
        String json = IOUtils.toString(is, "UTF-8");
        Arrays.stream(serializer.deserializeArray(json, false)).forEach(expectations::add);
      } catch (IOException e) {
        e.printStackTrace(); // Do anything more?
      }

    });
    return expectations.toArray(new Expectation[]{});
  }
}
