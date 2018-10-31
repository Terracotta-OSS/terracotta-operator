package org.terracotta.k8s.operator.shared;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TerracottaClusterConfiguration {

//  {
//    offheaps={
//        offheap1=256MB,
//        offheap2=100GB
//    }
//    serversPerStripe=2,
//    clientReconnectWindowSeconds=20
//  }

  private Map<String, String> offheaps = new HashMap<>();
  private int serversPerStripe;
  private int clientReconnectWindow;

  public Map<String, String> getOffheaps() {
    return offheaps;
  }

  public void setOffheaps(Map<String, String> offheaps) {
    this.offheaps = offheaps;
  }

  public int getServersPerStripe() {
    return serversPerStripe;
  }

  public void setServersPerStripe(int serversPerStripe) {
    this.serversPerStripe = serversPerStripe;
  }

  public int getClientReconnectWindow() {
    return clientReconnectWindow;
  }

  public void setClientReconnectWindow(int clientReconnectWindow) {
    this.clientReconnectWindow = clientReconnectWindow;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TerracottaClusterConfiguration that = (TerracottaClusterConfiguration) o;
    return serversPerStripe == that.serversPerStripe &&
      clientReconnectWindow == that.clientReconnectWindow &&
      Objects.equals(offheaps, that.offheaps);
  }

  @Override
  public int hashCode() {
    return Objects.hash(offheaps, serversPerStripe, clientReconnectWindow);
  }
}
