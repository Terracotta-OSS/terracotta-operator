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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.terracotta.k8s.operator.app.crd.DoneableTerracottaDBCluster;
import org.terracotta.k8s.operator.app.crd.TerracottaCRD;
import org.terracotta.k8s.operator.app.crd.TerracottaOSSCluster;
import org.terracotta.k8s.operator.app.crd.TerracottaDBClusterList;
import org.terracotta.k8s.operator.app.service.TheService;
import org.terracotta.k8s.operator.app.watcher.ClusterWatcher;

import javax.annotation.PostConstruct;
import java.util.function.Predicate;

@SpringBootApplication
@RequestMapping("/api")
public class TerracottaOperatorApplication {

  private static final Logger log = LoggerFactory.getLogger(TerracottaOperatorApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(TerracottaOperatorApplication.class, args);
  }

  @Value("${application.namespace}")
  String namespace;

  @Autowired
  private KubernetesClientFactory kubernetesClientFactory;

  @Autowired
  private TheService theService;

  @PostConstruct
  public void initApplication() {
    try (KubernetesClient client = kubernetesClientFactory.retrieveKubernetesClient()) {
      // Create a namespace for all our stuff
      Namespace ns = new NamespaceBuilder().withNewMetadata().withName(namespace).endMetadata().build();
      if (client != null) {
        log.info("Created or replaced namespace : " + client.namespaces().createOrReplace(ns));
        startTerracottaCRDWatcher(client);
      }
    }
  }

  private void startTerracottaCRDWatcher(KubernetesClient client) {
    createCRDIfNotExists(client, TerracottaCRD.CLUSTER_DEFINITION, TerracottaCRD.CLUSTER_FULL_NAME::equals);
    // Fixing a fabric8 bug
    KubernetesDeserializer.registerCustomKind(TerracottaCRD.TERRACOTTA_GROUP + "/" + TerracottaCRD.VERSION,
                                              TerracottaCRD.CLUSTER_SINGULAR_NAME,
                                              TerracottaOSSCluster.class);
    NonNamespaceOperation<TerracottaOSSCluster, TerracottaDBClusterList, DoneableTerracottaDBCluster, Resource<TerracottaOSSCluster, DoneableTerracottaDBCluster>> clusterClient
      = client.customResources(TerracottaCRD.CLUSTER_DEFINITION,
                               TerracottaOSSCluster.class,
                               TerracottaDBClusterList.class,
                               DoneableTerracottaDBCluster.class).inNamespace(namespace);
    clusterClient.watch(new ClusterWatcher(theService));
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
