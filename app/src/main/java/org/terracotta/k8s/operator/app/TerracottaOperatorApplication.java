package org.terracotta.k8s.operator.app;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.terracotta.k8s.operator.app.crd.DoneableLicense;
import org.terracotta.k8s.operator.app.crd.DoneableTerracottaDBCluster;
import org.terracotta.k8s.operator.app.crd.License;
import org.terracotta.k8s.operator.app.crd.LicenseList;
import org.terracotta.k8s.operator.app.crd.TerracottaCRD;
import org.terracotta.k8s.operator.app.crd.TerracottaDBCluster;
import org.terracotta.k8s.operator.app.crd.TerracottaDBClusterList;
import org.terracotta.k8s.operator.app.service.TheService;
import org.terracotta.k8s.operator.app.watcher.ClusterWatcher;
import org.terracotta.k8s.operator.app.watcher.LicenseWatcher;

import java.util.function.Predicate;

import javax.annotation.PostConstruct;

@SpringBootApplication
@RequestMapping("/api")
public class TerracottaOperatorApplication {

  private static final Logger log = LoggerFactory.getLogger(TerracottaOperatorApplication.class);

  private static final String NAMESPACE = "thisisatest";


  public static void main(String[] args) {
    SpringApplication.run(TerracottaOperatorApplication.class, args);
  }

  @Autowired
  private KubernetesClientFactory kubernetesClientFactory;

  @Autowired
  private TheService theService;

  @PostConstruct
  public void initApplication() {
    try (KubernetesClient client = kubernetesClientFactory.retrieveKubernetesClient()) {
      // Create a namespace for all our stuff
      Namespace ns = new NamespaceBuilder().withNewMetadata().withName(NAMESPACE).addToLabels("this", "rocks").endMetadata().build();
      if (client != null) {
        log.info("Created or replaced namespace : " + client.namespaces().createOrReplace(ns));

        client.apps().deployments().inNamespace(NAMESPACE).list().getItems().forEach(deployment -> {
          log.warn("Found this deployment : " + deployment);
          client.apps().deployments().inNamespace(NAMESPACE).withName(deployment.getMetadata().getName()).delete();
          log.warn("Deleted this deployment : " + deployment);

        });

        startTerracottaCRDWatcher(client);
      }
    }
  }

  private void startTerracottaCRDWatcher(KubernetesClient client) {
    createCRDIfNotExists(client, TerracottaCRD.CLUSTER_DEFINITION, TerracottaCRD.CLUSTER_FULL_NAME::equals);
    createCRDIfNotExists(client, TerracottaCRD.LICENSE_DEFINITION, TerracottaCRD.LICENSE_FULL_NAME::equals);
    // Fixing a fabric8 bug
    KubernetesDeserializer.registerCustomKind(TerracottaCRD.TERRACOTTA_GROUP + "/v1",
                                              TerracottaCRD.CLUSTER_SINGULAR_NAME,
                                              TerracottaDBCluster.class);
    KubernetesDeserializer.registerCustomKind(TerracottaCRD.TERRACOTTA_GROUP + "/v1",
                                              TerracottaCRD.LICENSE_SINGULAR_NAME,
                                              License.class);
    NonNamespaceOperation<TerracottaDBCluster, TerracottaDBClusterList, DoneableTerracottaDBCluster, Resource<TerracottaDBCluster, DoneableTerracottaDBCluster>> clusterClient
      = client.customResources(TerracottaCRD.CLUSTER_DEFINITION,
                               TerracottaDBCluster.class,
                               TerracottaDBClusterList.class,
                               DoneableTerracottaDBCluster.class).inNamespace(NAMESPACE);
    clusterClient.watch(new ClusterWatcher(theService));

    NonNamespaceOperation<License, LicenseList, DoneableLicense, Resource<License, DoneableLicense>> licenseClient
      = client.customResources(TerracottaCRD.LICENSE_DEFINITION,
                               License.class,
                               LicenseList.class,
                               DoneableLicense.class).inNamespace(NAMESPACE);
    licenseClient.watch(new LicenseWatcher(theService));
  }

  private static void createCRDIfNotExists(KubernetesClient kubernetesClient,
                                           CustomResourceDefinition definition,
                                           Predicate<String> matcher) {
    boolean exists = kubernetesClient.customResourceDefinitions().list().getItems().stream()
                                     .map(CustomResourceDefinition::getMetadata)
                                     .map(ObjectMeta::getName)
                                     .anyMatch(matcher);
    if (!exists) {
      kubernetesClient.customResourceDefinitions().create(definition);
    }
  }
}
