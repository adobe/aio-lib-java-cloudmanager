package io.adobe.cloudmanager;

import java.util.Collection;
import javax.validation.constraints.NotNull;

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
}
