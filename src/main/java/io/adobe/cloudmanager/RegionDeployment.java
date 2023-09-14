package io.adobe.cloudmanager;

public interface RegionDeployment {
  /**
   * Returns the id of this deployment.
   *
   * @return the id
   */
  String getId();

  /**
   * Returns the region name for this deployment
   *
   * @return the region
   */
  String getRegion();

  /**
   * Return the type of deployment
   *
   * @return the deployment type
   */
  Type getDeployType();

  /**
   * Delete this region deployment
   *
   * @throws CloudManagerApiException when any error occurs
   */
  void delete() throws CloudManagerApiException;

  enum Type {
    PRIMARY,
    SECONDARY;
  }
}
