package io.adobe.cloudmanager;

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

import lombok.Getter;

/**
 * Represents possible regions for deployments, network, and other configurations.
 */
@Getter
public enum Region {
  SOUTHEAST_AUSTRALIA("aus5"),
  CANADA("can2"),
  GERMANY("deu6"),
  SOUTH_UK("gbr9"),
  JAPAN("jpn4"),
  WEST_EUROPE("nld2"),
  SINGAPORE("sgp5"),
  EAST_US("va7"),
  WEST_US("wa1");

  private final String value;

  Region(String value) {
    this.value = value;
  }

  public static Region fromValue(String text) {
    for (Region b : Region.values()) {
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
