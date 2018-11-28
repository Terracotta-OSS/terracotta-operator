package org.terracotta.k8s.operator.app.crd;

import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionBuilder;

public class TerracottaCRD {
  public static final String TERRACOTTA_GROUP = "cluster.terracotta.com";

  public static final String CLUSTER_SINGULAR_NAME = "TerracottaDBCluster";
  public static final String CLUSTER_PLURAL_NAME = "terracottadbclusters";
  public static final String CLUSTER_FULL_NAME = CLUSTER_PLURAL_NAME + "." + TERRACOTTA_GROUP;
  public static final String CLUSTER_SHORT_NAME = "tcdb";

  public static final String LICENSE_SINGULAR_NAME = "License";
  public static final String LICENSE_PLURAL_NAME = "licenses";
  public static final String LICENSE_FULL_NAME = LICENSE_PLURAL_NAME + "." + TERRACOTTA_GROUP;

  public static final CustomResourceDefinition CLUSTER_DEFINITION = new CustomResourceDefinitionBuilder()
    .withApiVersion("apiextensions.k8s.io/v1beta1")
    .withNewMetadata()
      .withName(CLUSTER_FULL_NAME)
    .endMetadata()
    .withNewSpec()
      .withGroup(TERRACOTTA_GROUP)
      .withVersion("v1")
      .withScope("Namespaced")
      .withNewNames()
        .withKind(CLUSTER_SINGULAR_NAME)
        .withShortNames(CLUSTER_SINGULAR_NAME.toLowerCase())
        .withPlural(CLUSTER_PLURAL_NAME)
        .withShortNames(CLUSTER_SHORT_NAME)
      .endNames()
    .endSpec()
    .build();

  public static final CustomResourceDefinition LICENSE_DEFINITION = new CustomResourceDefinitionBuilder()
    .withApiVersion("apiextensions.k8s.io/v1beta1")
    .withNewMetadata()
      .withName(LICENSE_FULL_NAME)
    .endMetadata()
    .withNewSpec()
     .withGroup(TERRACOTTA_GROUP)
     .withVersion("v1")
     .withScope("Namespaced")
     .withNewNames()
       .withKind(LICENSE_SINGULAR_NAME)
        .withShortNames(LICENSE_SINGULAR_NAME.toLowerCase())
        .withPlural(LICENSE_PLURAL_NAME)
        .endNames()
    .endSpec()
    .build();
}
