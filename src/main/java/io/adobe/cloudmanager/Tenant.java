package io.adobe.cloudmanager;

import java.util.Collection;

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

  /**
   * List the programs associated with this tenant.
   *
   * @return a list of programs
   * @throws CloudManagerApiException when an error occurs
   */
  Collection<Program> listPrograms() throws CloudManagerApiException;
}
