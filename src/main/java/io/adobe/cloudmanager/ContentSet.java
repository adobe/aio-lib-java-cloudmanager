package io.adobe.cloudmanager;

import java.util.Collection;
import java.util.Set;
import javax.validation.constraints.NotNull;

import lombok.Value;

public interface ContentSet {

  /**
   * The ID of this Content Set.
   *
   * @return the id
   */
  String getId();

  /**
   * The id of the associated program context
   *
   * @return the program id
   */
  String getProgramId();

  /**
   * User-friendly name of this Content Set
   *
   * @return the name
   */
  String getName();

  /**
   * Description of the content set.
   *
   * @return the description
   */
  String getDescription();

  /**
   * The path definitions within this content set
   *
   * @return the path definitions
   */
  Collection<PathDefinition> getPathDefinitions();

  /**
   * Updates this content set with the specified changes
   *
   * @param name        the new name, or {@code null} to leave unchanged
   * @param description the new description, or {@code null} to leave unchanged
   * @param definitions the new definitions, or {@code null} to leave unchanged
   * @throws CloudManagerApiException when any error occurs
   */
  void update(String name, String description, Collection<PathDefinition> definitions) throws CloudManagerApiException;

  /**
   * Deletes this content set definition
   *
   * @throws CloudManagerApiException when any error occurs
   */
  void delete() throws CloudManagerApiException;

  /**
   * Creates a Content Flow with this content set, between the specified environments.
   *
   * @return the content flow
   */
  @NotNull
  ContentFlow startFlow(@NotNull String srcEnvironmentId, @NotNull String destEnvironment, boolean includeAcl) throws CloudManagerApiException;

  @Value
  class PathDefinition {
    String path;
    Set<String> excluded;
  }

}
