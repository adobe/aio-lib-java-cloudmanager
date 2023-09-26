package io.adobe.cloudmanager;

import java.net.URL;
import java.util.Collection;
import javax.validation.constraints.NotNull;

import com.adobe.aio.workspace.Workspace;
import io.adobe.cloudmanager.impl.network.dns.DomainNameApiImpl;

public interface DomainNameApi {

  /**
   * Lists all the domain names in the specified program.
   *
   * @param programId the program context
   * @return the list of domain names
   * @throws CloudManagerApiException when any error occurs
   */
  Collection<DomainName> list(@NotNull String programId) throws CloudManagerApiException;

  /**
   * Lists all the domain names in the specified program, using the specified limit and starting at 0
   *
   * @param programId the program context
   * @param limit     the number of domains to return
   * @return the list of domain names
   * @throws CloudManagerApiException when any error occurs
   */
  Collection<DomainName> list(@NotNull String programId, int limit) throws CloudManagerApiException;

  /**
   * Lists all the domain names in the specified program, using the specified limit and starting at the specified position.
   *
   * @param programId the program context
   * @param start     the starting position of the results
   * @param limit     the number of domains to return
   * @return the list of domain names
   * @throws CloudManagerApiException when any error occurs
   */
  Collection<DomainName> list(@NotNull String programId, int start, int limit) throws CloudManagerApiException;

  /**
   * Creates a new domain name.
   *
   * @param programId     the program context
   * @param name          the name of the domain name
   * @param environmentId the environment in the program
   * @param certificateId the SSL Certificate ID for the domain name
   * @param dnsTxtRecord  the dns txt record for validation
   * @param dnsZone       the dns zone
   * @return the newly created domain name
   * @throws CloudManagerApiException when any error occurs
   */
  DomainName create(@NotNull String programId, @NotNull String name, @NotNull String environmentId, @NotNull String certificateId, @NotNull String dnsTxtRecord, @NotNull String dnsZone) throws CloudManagerApiException;

  /**
   * Returns the specified domain name.
   *
   * @param programId the program id
   * @param id        the id of the domain name
   * @return the domain name details
   * @throws CloudManagerApiException when any error occurs
   */
  DomainName get(@NotNull String programId, @NotNull String id) throws CloudManagerApiException;

  /**
   * Updates the domain name.
   *
   * @param programId the program id context
   * @param id        the id of the domain name to update
   * @param update    the update details
   * @return the updated domain name
   * @throws CloudManagerApiException when any error occurs
   */
  DomainName update(@NotNull String programId, @NotNull String id, @NotNull DomainNameUpdate update) throws CloudManagerApiException;

  /**
   * Deletes the specified domain name.
   *
   * @param programId the program id context
   * @param id        the id of the domain name to delete
   * @throws CloudManagerApiException when any error occurs
   */
  void delete(@NotNull String programId, @NotNull String id) throws CloudManagerApiException;

  /**
   * Performs a `verify` and deploys the specified domain name.
   *
   * @param programId the program id context
   * @param id        the id of the domain to deploy
   * @return the updated domain name
   * @throws CloudManagerApiException when any error occurs
   */
  DomainName deploy(@NotNull String programId, @NotNull String id) throws CloudManagerApiException;

  /**
   * Performs a verification of the specified domain name.
   *
   * @param programId the program id context
   * @param id        the id of the domain to verify
   * @return the updated domain name
   * @throws CloudManagerApiException when any error occurs
   */
  DomainName verify(@NotNull String programId, @NotNull String id) throws CloudManagerApiException;

  /**
   * Validates the specified domain name.
   *
   * @param programId     the program id context
   * @param name          the domain name to verify
   * @param environmentId the environment id
   * @param certificateId the SSL certificate id
   * @return the updated domain name
   * @throws CloudManagerApiException when any error occurs
   */
  DomainName validate(@NotNull String programId, @NotNull String name, @NotNull String environmentId, @NotNull String certificateId) throws CloudManagerApiException;

  static Builder builder() {
    return new Builder();
  }

  class Builder {
    private Workspace workspace;
    private URL url;

    private Builder() {
    }

    public Builder workspace(Workspace workspace) {
      this.workspace = workspace;
      return this;
    }

    public Builder url(URL url) {
      this.url = url;
      return this;
    }

    public DomainNameApi build() {
      if (workspace == null) {
        throw new IllegalStateException("Workspace must be specified.");
      }
      if (workspace.getAuthContext() == null) {
        throw new IllegalStateException("Workspace must specify AuthContext");
      }
      workspace.getAuthContext().validate();
      return new DomainNameApiImpl(workspace, url);
    }
  }
}
