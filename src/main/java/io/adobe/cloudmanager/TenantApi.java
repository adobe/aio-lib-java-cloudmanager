package io.adobe.cloudmanager;

import java.util.Collection;
import javax.validation.constraints.NotNull;

public interface TenantApi {
  /**
   * Lists the tenants associated with the IMS Org in the API Context
   *
   * @return list of tenants
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<Tenant> list() throws CloudManagerApiException;

  /**
   * Gets the tenant with the specified identifier.
   *
   * @param tenantId the id of the tenant
   * @return the tenant
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Tenant get(@NotNull String tenantId) throws CloudManagerApiException;
}
