package org.terracotta.k8s.operator.app.model;

import java.util.Map;
import java.util.TreeMap;

public class Dataroots {

  private Map<String, String> dataroots =  new TreeMap<>();

  public Map<String, String> getDataroots() {
    return dataroots;
  }

  public void setDataroots(Map<String, String> dataroots) {
    this.dataroots = dataroots;
  }
}
