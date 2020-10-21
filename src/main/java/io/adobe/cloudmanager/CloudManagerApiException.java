package io.adobe.cloudmanager;

public class CloudManagerApiException extends Exception {

  public CloudManagerApiException() {
  }

  public CloudManagerApiException(String message) {
    super(message);
  }

  public CloudManagerApiException(String message, Throwable cause) {
    super(message, cause);
  }

  public CloudManagerApiException(Throwable cause) {
    super(cause);
  }

  public CloudManagerApiException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
