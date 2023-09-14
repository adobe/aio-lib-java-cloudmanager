package io.adobe.cloudmanager;

import java.net.URL;
import java.util.Collection;
import javax.validation.constraints.NotNull;

import com.adobe.aio.workspace.Workspace;
import io.adobe.cloudmanager.impl.tenant.TenantApiImpl;

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


  static Builder builder() {
    return new Builder();
  }

  /**
   * Builds instances of the Tenant API.
   */
  class Builder {
    private Workspace workspace;
    private URL url;

    public Builder() {
    }

    public Builder workspace(@NotNull Workspace workspace) {
      this.workspace = workspace;
      return this;
    }

    public Builder url(@NotNull URL url) {
      this.url = url;
      return this;
    }

    public TenantApi build() {
      if (workspace == null) {
        throw new IllegalStateException("Workspace must be specified.");
      }
      if (workspace.getAuthContext() == null) {
        throw new IllegalStateException("Workspace must specify AuthContext");
      }
      workspace.getAuthContext().validate();
      return new TenantApiImpl(workspace, url);
    }
  }
}
