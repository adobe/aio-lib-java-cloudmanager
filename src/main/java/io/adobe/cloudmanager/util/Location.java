package io.adobe.cloudmanager.util;

import lombok.Value;

@Value
public class Location {

  String url;

  public String getRewrittenUrl(String baseUrl) {
    return url.replaceFirst("http(s)?://.*\\.adobe\\.io/", baseUrl + "/");
  }
}
