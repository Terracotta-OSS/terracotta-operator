package org.terracotta.k8s.operator.app.crd;

import io.fabric8.kubernetes.client.CustomResource;

import org.terracotta.k8s.operator.shared.TerracottaClusterConfiguration;

public class TerracottaOSSCluster extends CustomResource {
  private TerracottaClusterConfiguration spec;

  public TerracottaClusterConfiguration getSpec() {
    return spec;
  }

  public void setSpec(TerracottaClusterConfiguration spec) {
    this.spec = spec;
  }

  @Override
  public String toString() {
    return "TerracottaOSSCluster{" +
           "spec=" + spec +
           '}';
  }
}
