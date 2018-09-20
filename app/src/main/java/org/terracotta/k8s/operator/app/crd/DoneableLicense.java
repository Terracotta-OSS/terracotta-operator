package org.terracotta.k8s.operator.app.crd;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class DoneableLicense extends CustomResourceDoneable<License> {
  public DoneableLicense(License resource, Function<License, License> function) {
    super(resource, function);
  }
}
