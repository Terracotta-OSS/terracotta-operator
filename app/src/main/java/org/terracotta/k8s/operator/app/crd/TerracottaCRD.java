package org.terracotta.k8s.operator.app.crd;

import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionBuilder;

public class TerracottaCRD {
  public static final String TERRACOTTA_GROUP = "terracotta.org";

  public static final String CLUSTER_SINGULAR_NAME = "OSSCluster";
  public static final String CLUSTER_PLURAL_NAME = "ossclusters";
  public static final String CLUSTER_FULL_NAME = CLUSTER_PLURAL_NAME + "." + TERRACOTTA_GROUP;
  public static final String VERSION = "v1";


  public static final CustomResourceDefinition CLUSTER_DEFINITION = new CustomResourceDefinitionBuilder()
    .withApiVersion("apiextensions.k8s.io/v1beta1")
    .withNewMetadata()
      .withName(CLUSTER_FULL_NAME)
    .endMetadata()
    .withNewSpec()
      .withGroup(TERRACOTTA_GROUP)
      .withVersion(VERSION)
      .withScope("Namespaced")
      .withNewNames()
        .withKind(CLUSTER_SINGULAR_NAME)
        .withShortNames(CLUSTER_SINGULAR_NAME.toLowerCase())
        .withPlural(CLUSTER_PLURAL_NAME)
      .endNames()
    .endSpec()
    .build();

}
