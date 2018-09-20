package org.terracotta.k8s.operator.app.crd;

import io.fabric8.kubernetes.client.CustomResource;
import org.terracotta.k8s.operator.app.model.TerracottaClusterConfiguration;

import java.util.Map;

public class License extends CustomResource {
  private Map<String, String> spec;

  public Map<String, String> getSpec() {
    return spec;
  }

  public void setSpec(Map<String, String> spec) {
    this.spec = spec;
  }

  @Override
  public String toString() {
    return "TerracottaDBCluster{" +
           "spec=" + spec +
           '}';
  }
}
