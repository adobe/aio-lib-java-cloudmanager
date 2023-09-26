package io.adobe.cloudmanager;

import java.time.OffsetDateTime;
import java.util.List;

public interface SSLCertificate {

  String getId();

  String getProgramId();

  String getName();

  String getSerialNumber();

  String getIssuer();

  OffsetDateTime getExpires();

  String getCommonName();

  List<String> getSubjectAlternativeNames();


}
