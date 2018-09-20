package org.terracotta.k8s.operator.app.crd;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class DoneableTerracottaDBCluster extends CustomResourceDoneable<TerracottaDBCluster> {
  public DoneableTerracottaDBCluster(TerracottaDBCluster resource, Function<TerracottaDBCluster, TerracottaDBCluster> function) {
    super(resource, function);
  }
}
