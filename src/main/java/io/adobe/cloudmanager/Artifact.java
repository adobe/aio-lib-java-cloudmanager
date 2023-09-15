package io.adobe.cloudmanager;

public interface Artifact {

  /**
   * Returns the id for this artifact;
   *
   * @return the id
   */
  String getId();

  /**
   * Returns the file name for this artifacts
   *
   * @return the file name
   */
  String getFileName();

  /**
   * Returns the type of this artifact
   *
   * @return the type
   */
  String getType();

  /**
   * Returns the md5 hash for the artifact.
   *
   * @return the md5
   */
  String getMd5();

  /**
   * Returns the fully qualified download url for this artifact
   *
   * @return the download url
   */
  String getDownloadUrl() throws CloudManagerApiException;
}
