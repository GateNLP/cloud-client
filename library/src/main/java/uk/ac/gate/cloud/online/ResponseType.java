package uk.ac.gate.cloud.online;

public enum ResponseType {
  JSON("application/json"), GATE_XML("application/gate+xml"), FINF("application/fastinfoset");

  private ResponseType(String contentType) {
    this.contentType = contentType;
  }

  private final String contentType;

  public String getContentType() {
    return contentType;
  }

  public String getContentType(boolean includeText) {
    if(!includeText) {
      return contentType + "; includeText=no";
    } else {
      return contentType;
    }
  }
}
