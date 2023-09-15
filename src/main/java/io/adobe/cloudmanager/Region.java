package io.adobe.cloudmanager;

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
