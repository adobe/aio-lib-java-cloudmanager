package io.adobe.cloudmanager;

import java.net.URL;
import java.util.Collection;
import javax.validation.constraints.NotNull;

import com.adobe.aio.workspace.Workspace;
import io.adobe.cloudmanager.impl.program.ProgramApiImpl;

public interface ProgramApi {

  /**
   * Returns the program with the specified id
   *
   * @param programId the id of the program
   * @return the program
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Program get(@NotNull String programId) throws CloudManagerApiException;

  /**
   * Delete the specified program.
   *
   * @param programId the id of the program to delete.
   * @throws CloudManagerApiException when any error occurs
   */
  void delete(@NotNull String programId) throws CloudManagerApiException;

  /**
   * Delete the specified program.
   *
   * @param program the program to delete
   * @throws CloudManagerApiException when any error occurs
   */
  void delete(@NotNull Program program) throws CloudManagerApiException;

  /**
   * List all programs for a Tenant
   *
   * @param tenantId the id tenant
   * @return a list of {@link Program}s
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<Program> list(@NotNull String tenantId) throws CloudManagerApiException;

  /**
   * List all programs for the Tenant
   *
   * @param tenant the tenant
   * @return a list of {@link Program}s
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<Program> list(@NotNull Tenant tenant) throws CloudManagerApiException;

  /**
   * List all regions which can be used to create environments for the specified program.
   *
   * @param programId the id of the program
   * @return the list of regions
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<Region> listRegions(@NotNull String programId) throws CloudManagerApiException;

  static Builder builder() {
    return new Builder();
  }

  /**
   * Builds instances of the Program API.
   */
  class Builder {
    private Workspace workspace;
    private URL url;

    private Builder() {
    }

    public Builder workspace(@NotNull Workspace workspace) {
      this.workspace = workspace;
      return this;
    }

    public Builder url(@NotNull URL url) {
      this.url = url;
      return this;
    }

    public ProgramApi build() {
      if (workspace == null) {
        throw new IllegalStateException("Workspace must be specified.");
      }
      if (workspace.getAuthContext() == null) {
        throw new IllegalStateException("Workspace must specify AuthContext");
      }
      workspace.getAuthContext().validate();
      return new ProgramApiImpl(workspace, url);
    }
  }
}
