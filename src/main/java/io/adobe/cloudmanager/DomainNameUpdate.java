package io.adobe.cloudmanager;

import lombok.Builder;
import lombok.Value;

@Value
@Builder()
public class DomainNameUpdate {
  String name;
  DomainName.Type type;
  String txtRecord;
  String zone;
  String environmentId;
  Environment.Tier tier;
  String certificateId;
}
