package io.adobe.cloudmanager;

import java.util.Collection;
import javax.validation.constraints.NotNull;

public interface RepositoryApi {

  /**
   * Lists all repositories for the specified program.
   *
   * @param programId the program id
   * @return list of repositories
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<Repository> list(@NotNull String programId) throws CloudManagerApiException;

  /**
   * Lists all repositories for the specified program.
   *
   * @param program the program
   * @return list of repositories
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<Repository> list(@NotNull Program program) throws CloudManagerApiException;

  /**
   * Lists all repositories for the specified program, up to the defined limit.
   *
   * @param programId the program id
   * @param limit     the maximum number of repositories to retrieve
   * @return list of repositories
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<Repository> list(@NotNull String programId, int limit) throws CloudManagerApiException;

  /**
   * Lists all repositories for the specified program, up to the defined limit.
   *
   * @param program the program
   * @param limit   the maximum number of repositories to retrieve
   * @return list of repositories
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<Repository> list(@NotNull Program program, int limit) throws CloudManagerApiException;

  /**
   * Lists all repositories for the specified program, from the starting position, up to the defined limit.
   *
   * @param programId the program id
   * @param start     the starting position in the list to retrieve
   * @param limit     the maximum number of repositories to retrieve
   * @return list of repositories
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<Repository> list(@NotNull String programId, int start, int limit) throws CloudManagerApiException;

  /**
   * Lists all repositories for the specified program, from the starting position, up to the defined limit.
   *
   * @param program the program
   * @param start   the starting position in the list to retrieve
   * @param limit   the maximum number of repositories to retrieve
   * @return list of repositories
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<Repository> list(@NotNull Program program, int start, int limit) throws CloudManagerApiException;

  /**
   * Get a specific repository in the program.
   *
   * @param programId    the program id
   * @param repositoryId the repository id
   * @return the repository
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Repository get(@NotNull String programId, @NotNull String repositoryId) throws CloudManagerApiException;

  /**
   * Get a specific repository in the program.
   *
   * @param program      the program
   * @param repositoryId the repository id
   * @return the repository
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Repository get(@NotNull Program program, @NotNull String repositoryId) throws CloudManagerApiException;

  /**
   * Lists all the branches associated with the repository.
   *
   * @param repository the repository
   * @return list of branch names
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getBranches">List Branches API</a>
   */
  @NotNull
  Collection<String> listBranches(@NotNull Repository repository) throws CloudManagerApiException;
}
