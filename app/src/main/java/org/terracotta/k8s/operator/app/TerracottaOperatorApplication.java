package org.terracotta.k8s.operator.app;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class TerracottaOperatorApplication {

  private static final Logger log = LoggerFactory.getLogger(TerracottaOperatorApplication.class);


  public static void main(String[] args) {
    SpringApplication.run(TerracottaOperatorApplication.class, args);
  }

  @Autowired
  private org.terracotta.cloud.terracottaoperator.KubernetesClientFactory kubernetesClientFactory;

  @PostConstruct
  public void initApplication() {
    try (KubernetesClient client = kubernetesClientFactory.retrieveKubernetesClient()) {
      // Create a namespace for all our stuff
      Namespace ns = new NamespaceBuilder().withNewMetadata().withName("thisisatest").addToLabels("this", "rocks").endMetadata().build();
      if (client != null) {
        log.info("Created or replaced namespace : " + client.namespaces().createOrReplace(ns));

        client.apps().deployments().inNamespace("thisisatest").list().getItems().forEach(deployment -> {
          log.warn("Found this deployment : " + deployment);
          client.apps().deployments().inNamespace("thisisatest").withName(deployment.getMetadata().getName()).delete();
          log.warn("Deleted this deployment : " + deployment);

        });

      }

    }
  }

}
