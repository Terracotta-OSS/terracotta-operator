package org.terracotta.k8s.operator.app.model;

import java.util.Map;
import java.util.Objects;

public class TerracottaClusterConfiguration {

//  {
//    offheaps={
//        offheap1=256MB,
//        offheap2=100GB
//    },
//        dataroots={
//            dataroot1=EBS,
//            dataroot2=local
//        }
//    stripes=2,
//        serversPerStripe=2,
//        clientReconnectWindowSeconds=20
//  }

  private Map<String, String> offheaps;
  private Map<String, String> dataroots;
  private int stripes;
  private int serversPerStripe;
  private int clientReconnectWindow;

  public Map<String, String> getOffheaps() {
    return offheaps;
  }

  public void setOffheaps(Map<String, String> offheaps) {
    this.offheaps = offheaps;
  }

  public Map<String, String> getDataroots() {
    return dataroots;
  }

  public void setDataroots(Map<String, String> dataroots) {
    this.dataroots = dataroots;
  }

  public int getStripes() {
    return stripes;
  }

  public void setStripes(int stripes) {
    this.stripes = stripes;
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
    return stripes == that.stripes &&
        serversPerStripe == that.serversPerStripe &&
        clientReconnectWindow == that.clientReconnectWindow &&
        Objects.equals(offheaps, that.offheaps) &&
        Objects.equals(dataroots, that.dataroots);
  }

  @Override
  public int hashCode() {
    return Objects.hash(offheaps, dataroots, stripes, serversPerStripe, clientReconnectWindow);
  }
}
