package io.adobe.cloudmanager;

public interface Tenant {

  /**
   * Identifier of the tenant.
   *
   * @return id
   */
  String getId();

  /**
   * The description for the tenant
   *
   * @return description
   */
  String getDescription();

  /**
   * The name of the Git Repository organization
   *
   * @return git repository organization
   */
  String getOrganizationName();
}
