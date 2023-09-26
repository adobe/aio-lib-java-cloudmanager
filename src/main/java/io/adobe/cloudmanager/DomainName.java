package io.adobe.cloudmanager;

import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;

public interface DomainName {

  String getId();

  String getName();

  Type getRecordType();

  Status getStatusState();

  Set<Target> getTargets();

  String getTxtRecord();

  String getZone();

  String getEnvironmentId();

  Environment.Tier getEnvironmentTier();

  SSLCertificate getCertificate();

  @Getter
  enum Status {
    NOT_VERIFIED("not_verified"),
    PENDING_DEPLOYMENT("pending_deployment"),
    DEPLOYMENT_FAILED("deployment_failed"),
    DEPLOYED("deployed"),
    PENDING_READINESS_CHECK("pending_readiness_check"),
    READY("ready"),
    DELETING("deleting"),
    DELETED("deleted"),
    DELETE_FAILED("delete_failed");

    private final String value;

    Status(String value) {
      this.value = value;
    }

    public static Status fromValue(String input) {
      for (Status s : Status.values()) {
        if (s.value.equals(input)) {
          return s;
        }
      }
      return null;
    }
  }

  @Getter
  enum Type {
    A, CNAME
  }

  @Value
  class Target {
    String target;
    @EqualsAndHashCode.Exclude
    boolean detected;
  }
}
