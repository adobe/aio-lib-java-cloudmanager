package io.adobe.cloudmanager;

import java.util.List;

import lombok.Value;

public interface ContentFlow {

  /**
   * The id of this content flow.
   *
   * @return the id
   */
  String getId();

  /**
   * The id of the content set used for this flow.
   *
   * @return the content set id
   */
  String getContentSetId();

  /**
   * The id of the Environment from which content is copied
   *
   * @return the source environment id
   */
  String getSrcEnvironmentId();

  /**
   * The name of the Environment from which content is copied
   *
   * @return the source environment name
   */
  String getSrcEnvironmentName();

  /**
   * The id of the Environment to which content is copied
   *
   * @return the source environment id
   */
  String getDestEnvironmentId();

  /**
   * The name of the Environment to which content is copied
   *
   * @return the source environment name
   */
  String getDestEnvironmentName();

  /**
   * The tier of the source and destination environments
   *
   * @return the environment tier
   */
  Environment.Tier getEnvironmentTier();

  /**
   * The status of the content flow process.
   * <p>
   * See <a href="https://experienceleague.adobe.com/docs/experience-manager-cloud-service/content/implementing/developer-tools/content-copy.html?lang=en#copy-activity">the documentation</a>.
   *
   * @return the status
   */
  String getStatus();

  /**
   * The results of the content flow export process.
   *
   * @return the export results
   */
  Results getExportResults();

  /**
   * The results of the content flow import process
   *
   * @return the import results
   */
  Results getImportResults();

  /**
   * Cancels this content flow
   *
   * @throws CloudManagerApiException when any error occurs
   */
  void cancel() throws CloudManagerApiException;

  @Value
  class Results {
    String errorCode;
    String message;
    List<String> details;
  }
}
