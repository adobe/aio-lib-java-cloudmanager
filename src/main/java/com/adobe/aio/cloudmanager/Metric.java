package com.adobe.aio.cloudmanager;

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

public interface Metric {

  /**
   * Severity of the metric
   *
   * @return severity
   **/
  Severity getSev();

  /**
   * Whether metric is considered passed
   *
   * @return passed
   **/
  Boolean isPassed();

  /**
   * Whether user override the failed metric
   *
   * @return override
   **/
  Boolean isOverride();

  /**
   * Expected value for the metric
   *
   * @return actualValue
   **/
  String getActualValue();

  /**
   * The metrics comparator
   *
   * @return the comparator
   */
  Comparator getComp();

  /**
   * KPI identifier
   *
   * @return kpi
   **/
  String getKpi();

  /**
   * Represents the severity of the metric.
   */
  enum Severity {
    CRITICAL("critical"),
    IMPORTANT("important"),
    INFORMATIONAL("informational");

    private String value;

    Severity(String value) {
      this.value = value;
    }

    public static Severity fromValue(String text) {
      for (Severity b : Severity.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }

    public String getValue() { return value; }

    @Override
    public String toString() { return String.valueOf(value); }
  }

  /**
   * The comparator to use for the metric.
   */
  enum Comparator {
    GT("GT"),
    GTE("GTE"),
    LT("LT"),
    LTE("LTE"),
    EQ("EQ"),
    NEQ("NEQ");

    private String value;

    Comparator(String value) {
      this.value = value;
    }

    public static Comparator fromValue(String text) {
      for (Comparator b : Comparator.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }

    public String getValue() { return value; }

    public String toString() { return String.valueOf(value); }
  }
}
