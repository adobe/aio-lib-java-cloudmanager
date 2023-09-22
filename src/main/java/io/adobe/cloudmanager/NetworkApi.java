package io.adobe.cloudmanager;

import java.net.URL;
import java.util.Collection;
import javax.validation.constraints.NotNull;

import com.adobe.aio.workspace.Workspace;
import io.adobe.cloudmanager.impl.network.IPAllowListImpl;
import io.adobe.cloudmanager.impl.network.NetworkApiImpl;

public interface NetworkApi {

  /**
   * Retrieves the specified IP Allow List
   *
   * @param programId program id context
   * @param id        the id of the Allow List
   * @return the Allow list
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  IPAllowList get(@NotNull String programId, @NotNull String id) throws CloudManagerApiException;

  /**
   * Updates the IP Allow List, changing the CIDR blocks.
   *
   * @param programId program id context
   * @param id        the id of the Allow List
   * @param name      the name to set
   * @param cidrs     the CIDR list to set
   * @param bindings  the bindings to set
   * @return the Allow list
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  IPAllowList update(@NotNull String programId, @NotNull String id, String name, Collection<IPAllowList.Cidr> cidrs, Collection<IPAllowList.Binding> bindings) throws CloudManagerApiException;

  /**
   * Deletes the specified IP Allow List
   *
   * @param programId program id context
   * @param id        the id of the Allow List
   * @throws CloudManagerApiException when any error occurs
   */
  void delete(@NotNull String programId, @NotNull String id) throws CloudManagerApiException;

  /**
   * List all the Allow Lists for the specified program.
   *
   * @param programId the program id
   * @return the list of IP Allow Lists
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<IPAllowList> list(@NotNull String programId) throws CloudManagerApiException;

  /**
   * List all the Allow Lists for the specified program.
   *
   * @param program the program
   * @return the list of IP Allow Lists
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<IPAllowList> list(@NotNull Program program) throws CloudManagerApiException;

  /**
   * Creates a new IP Allow List.
   *
   * @param programId the program id
   * @param name      the name
   * @param cidrs     the CIDR list
   * @param bindings  the binding list
   * @return the created IP Allow List
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  IPAllowList create(@NotNull String programId, @NotNull String name, @NotNull Collection<IPAllowList.Cidr> cidrs, @NotNull Collection<IPAllowList.Binding> bindings) throws CloudManagerApiException;

  /**
   * Creates a new IP Allow List.
   *
   * @param program  the program
   * @param name     the name
   * @param cidrs    the CIDR list
   * @param bindings the binding list
   * @return the created IP Allow List
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  IPAllowList create(@NotNull Program program, @NotNull String name, @NotNull Collection<IPAllowList.Cidr> cidrs, @NotNull Collection<IPAllowList.Binding> bindings) throws CloudManagerApiException;

  /**
   * Returns the specified Binding for the IP Allow list
   *
   * @param programId     the program id context
   * @param ipAllowListId the ip allow list context
   * @param id            the id of the binding
   * @return the binding
   * @throws CloudManagerApiException when any error occurs
   */
  IPAllowList.Binding getBinding(@NotNull String programId, @NotNull String ipAllowListId, @NotNull String id) throws CloudManagerApiException;

  /**
   * Returns the specified Binding for the IP Allow list
   *
   * @param ipAllowList the ip allow list context
   * @param id          the id of the binding
   * @return the binding
   * @throws CloudManagerApiException when any error occurs
   */
  IPAllowList.Binding getBinding(@NotNull IPAllowList ipAllowList, @NotNull String id) throws CloudManagerApiException;

  /**
   * Removes the IP Allow list binding
   *
   * @param programId     the program context
   * @param ipAllowListId the IP Allow List context
   * @param id            the id of the binding
   * @throws CloudManagerApiException when any error occurs
   */
  void deleteBinding(@NotNull String programId, @NotNull String ipAllowListId, @NotNull String id) throws CloudManagerApiException;

  /**
   * Removes the IP Allow list binding
   *
   * @param programId     the program context
   * @param ipAllowListId the IP Allow List context
   * @param binding       the binding to remove
   * @throws CloudManagerApiException when any error occurs
   */
  void deleteBinding(@NotNull String programId, @NotNull String ipAllowListId, @NotNull IPAllowList.Binding binding) throws CloudManagerApiException;

  /**
   * Removes the IP Allow list binding
   *
   * @param ipAllowList the IP Allow List context
   * @param id          the id of the binding
   * @throws CloudManagerApiException when any error occurs
   */
  void deleteBinding(@NotNull IPAllowList ipAllowList, @NotNull String id) throws CloudManagerApiException;

  /**
   * Removes the IP Allow list binding
   *
   * @param ipAllowList the IP Allow List context
   * @param binding     the binding to remove
   * @throws CloudManagerApiException when any error occurs
   */
  void deleteBinding(@NotNull IPAllowList ipAllowList, @NotNull IPAllowList.Binding binding) throws CloudManagerApiException;

  /**
   * Retry binding the AllowList to the specified environment and tier.
   *
   * @param programId     the program context
   * @param ipAllowListId the IP Allow List context
   * @param binding       the binding to retry
   */
  void retryBinding(@NotNull String programId, @NotNull String ipAllowListId, @NotNull IPAllowList.Binding binding) throws CloudManagerApiException;


  /**
   * Retry binding the AllowList to the specified environment and tier.
   *
   * @param ipAllowList the IP Allow List context
   * @param binding       the binding to retry
   */
  void retryBinding(@NotNull IPAllowList ipAllowList, @NotNull IPAllowList.Binding binding) throws CloudManagerApiException;


  /**
   * Lists the bindings in the IP Allow List
   *
   * @param programId program id context
   * @param id        the id of the Allow List
   * @return the list of bindings
   * @throws CloudManagerApiException when any error occurs
   */
  Collection<IPAllowList.Binding> listBindings(@NotNull String programId, @NotNull String id) throws CloudManagerApiException;

  /**
   * Lists the bindings in the IP Allow List
   *
   * @param ipAllowList the IP Allow list
   * @return the list of bindings
   * @throws CloudManagerApiException when any error occurs
   */
  Collection<IPAllowList.Binding> listBindings(@NotNull IPAllowList ipAllowList) throws CloudManagerApiException;

  /**
   * Create a new binding of the IP Allow List to the specified environment and tier.
   *
   * @param programId     the program id context
   * @param ipAllowListId the ip allow list
   * @param environmentId the environment id to which to bind
   * @param tier          the tier in the environment
   * @return the new binding
   * @throws CloudManagerApiException when any error occurs
   */
  IPAllowList.Binding createBinding(@NotNull String programId, @NotNull String ipAllowListId, @NotNull String environmentId, @NotNull Environment.Tier tier) throws CloudManagerApiException;

  /**
   * Create a new binding of the IP Allow List to the specified environment and tier.
   *
   * @param ipAllowList   the ip allow list context
   * @param environmentId the environment id to which to bind
   * @param tier          the tier in the environment
   * @return the new binding
   * @throws CloudManagerApiException when any error occurs
   */
  IPAllowList.Binding createBinding(@NotNull IPAllowList ipAllowList, @NotNull String environmentId, @NotNull Environment.Tier tier) throws CloudManagerApiException;

  static Builder builder() {
    return new Builder();
  }

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

    public NetworkApi build() {
      if (workspace == null) {
        throw new IllegalStateException("Workspace must be specified.");
      }
      if (workspace.getAuthContext() == null) {
        throw new IllegalStateException("Workspace must specify AuthContext");
      }
      workspace.getAuthContext().validate();
      return new NetworkApiImpl(workspace, url);
    }
  }
}
