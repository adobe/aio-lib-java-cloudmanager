package io.adobe.cloudmanager;

import java.util.Collection;
import java.util.Optional;

import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.Value;

/**
 * Represents a network IP Allow list; ranges of IPs allowed to connect to a program.
 */
public interface IPAllowList {

  /**
   * Returns the id of this IP Allow List
   *
   * @return the id
   */
  String getId();

  /**
   * The program context of this IP Allow List.
   *
   * @return the program id
   */
  String getProgramId();


  /**
   * The name of this IP Allow List
   *
   * @return the name
   */
  String getName();

  /**
   * Returns the list of CIDRs that are allowed in this IP Allow List
   *
   * @return the CIDR list
   */
  Collection<Cidr> listCidr();

  /**
   * Returns all the bindings for this IP Allow List
   *
   * @return the list of bindings
   */
  Collection<Binding> listBindings();

  /**
   * Returns the binding by id.
   *
   * @param id the id of the binding
   * @return the binding
   * @throws CloudManagerApiException when any error occurs
   */
  Binding getBinding(@NotNull String id) throws CloudManagerApiException;

  /**
   * Returns the binding in the specified environment and tier, if it exists
   *
   * @param environmentId the environment id context
   * @param tier the tier of in the environment
   * @return the binding if it exists
   */
  Optional<Binding> getBinding(@NotNull String environmentId, @NotNull Environment.Tier tier);

  /**
   * Removes the specified binding.
   *
   * @param binding the binding to remove
   */
  void delete(@NotNull Binding binding) throws CloudManagerApiException;

  /**
   * Retry binding the AllowList to the specified environment and tier.
   *
   * @param binding the binding to retry
   * @throws CloudManagerApiException when any error occurs
   */
  void retry(@NotNull Binding binding) throws CloudManagerApiException;

  /**
   * Binds this IP Allow list to the specified environment tier.
   *
   * @param environmentId the environment id context
   * @param tier          the tier in the environment
   * @return the new binding
   * @throws CloudManagerApiException when any error occurs
   */
  Binding createBinding(@NotNull String environmentId, @NotNull Environment.Tier tier) throws CloudManagerApiException;

  /**
   * Remove the binding for the specified environment and tier, if it exists.
   *
   * @param environmentId the environment id context
   * @param tier the tier of in the environment
   * @throws CloudManagerApiException when any error occurs
   */
  void deleteBinding(@NotNull String environmentId, @NotNull Environment.Tier tier) throws CloudManagerApiException;

  @Value
  class Cidr {
    String address;
    String mask;

    public String getSignature() {
      return String.format("%s/%s", address, mask);
    }
  }

  @Data
  class Binding {
    private String id;
    private final String environmentId;
    private final Environment.Tier tier;
  }
}
