package org.terracotta.k8s.operator.shared;

import java.util.Map;
import java.util.TreeMap;

public class Offheaps {

  private Map<String, String> offheaps =  new TreeMap<>();

  public Map<String, String> getOffheaps() {
    return offheaps;
  }

  public void setOffheaps(Map<String, String> offheaps) {
    this.offheaps = offheaps;
  }
}
