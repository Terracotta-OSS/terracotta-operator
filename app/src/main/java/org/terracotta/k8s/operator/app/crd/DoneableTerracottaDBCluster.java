package org.terracotta.k8s.operator.app.crd;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class DoneableTerracottaDBCluster extends CustomResourceDoneable<TerracottaOSSCluster> {
  public DoneableTerracottaDBCluster(TerracottaOSSCluster resource, Function<TerracottaOSSCluster, TerracottaOSSCluster> function) {
    super(resource, function);
  }
}
