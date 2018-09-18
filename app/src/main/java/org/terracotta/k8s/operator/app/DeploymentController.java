package org.terracotta.k8s.operator.app;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DeploymentController {

  private static final Logger log = LoggerFactory.getLogger(DeploymentController.class);

  @PostMapping("/deployment/{connectionName}")
  @ResponseBody
  public void createDeployment() {

    Config config = new ConfigBuilder().build();
    try (KubernetesClient client = new DefaultKubernetesClient(config)) {
      // Create a namespace for all our stuff
      Namespace ns = new NamespaceBuilder().withNewMetadata().withName("thisisatest").addToLabels("this", "rocks").endMetadata().build();
      log.info("Created namespace : " + client.namespaces().createOrReplace(ns));

//      ServiceAccount fabric8 = new ServiceAccountBuilder()
//          .withNewMetadata()
//          .withName("fabric8")
//          .endMetadata().build();
//
//      client.serviceAccounts().inNamespace("thisisatest").createOrReplace(fabric8);
//      for (int i = 0; i < 2; i++) {
//        System.err.println("Iteration:" + (i + 1));
      Deployment deployment = new DeploymentBuilder()
        .withNewMetadata()
        .withName("terracotta")
        .endMetadata()
        .withNewSpec()
        .withReplicas(1)
        .withNewTemplate()
        .withNewMetadata()
        .addToLabels("app", "terracotta")
        .endMetadata()
        .withNewSpec()
        .addNewContainer()
        .withName("terracotta")
        .withImage("terracotta/terracotta-server-oss")
        .addNewPort()
        .withContainerPort(9410)
        .endPort()
        .endContainer()
        .endSpec()
        .endTemplate()
        .withNewSelector()
        .addToMatchLabels("app", "terracotta")
        .endSelector()
        .endSpec()
        .build();


      deployment = client.apps().deployments().inNamespace("thisisatest").create(deployment);
      log.info("Created deployment : " + deployment);

//      System.err.println("Scaling up:" + deployment.getMetadata().getName());
//      client.apps().deployments().inNamespace("thisisatest").withName("nginx").scale(2, true);
//      log.info("Created replica sets:", client.apps().replicaSets().inNamespace("thisisatest").list().getItems());
//				System.err.println("Deleting:" + deployment.getMetadata().getName());
//				client.resource(deployment).delete();
//      }
//      log.info("Done.");


//      Deployment deployment = client.apps().deployments().inNamespace("thisisatest").withName("nginx").get();
//      log.info("Deleting:" + deployment.getMetadata().getName());
//      client.resource(deployment).delete();
    }

  }

  @DeleteMapping("/deployment/{connectionName}")
  @ResponseBody
  public void deleteDeployment() {

    Config config = new ConfigBuilder().build();
    try (KubernetesClient client = new DefaultKubernetesClient(config)) {
      log.info("Deleting terracotta");
      boolean deleted = client.apps().deployments().inNamespace("thisisatest").withName("terracotta").delete();
      if(deleted) {
        log.info("Successfully deleted terracotta");
      } else {
        log.warn("Could not delete terracotta");
      }
    }

  }

}
