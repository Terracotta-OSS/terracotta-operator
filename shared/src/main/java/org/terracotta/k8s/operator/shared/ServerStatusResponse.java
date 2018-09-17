package org.terracotta.k8s.operator.shared;

/**
 * @author Henri Tremblay
 */
public class ServerStatusResponse {
  private ServerStatus status;

  private ServerStatusResponse() {}

  public ServerStatusResponse(ServerStatus status) {
    this.status = status;
  }

  public ServerStatus getStatus() {
    return status;
  }

  public void setStatus(ServerStatus status) {
    this.status = status;
  }

  @Override
  public String toString() {
    return "status=" + status;
  }
}
