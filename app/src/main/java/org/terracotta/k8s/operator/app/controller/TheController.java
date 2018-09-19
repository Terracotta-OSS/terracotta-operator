package org.terracotta.k8s.operator.app.controller;

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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.terracotta.k8s.operator.app.model.TerracottaClusterConfiguration;

import java.io.File;

@RestController
@RequestMapping("/api")
public class TheController {

  private static final Logger log = LoggerFactory.getLogger(TheController.class);


  @PutMapping(value="/config/license")
  @ResponseBody
  public void createLicense(File licenseFile) {


  }

  @GetMapping(value="/config/license")
  @ResponseBody
  public ResponseEntity readLicense() {

    return null;
  }



  @PostMapping(value="/cluster/{connectionName}",  consumes = {MediaType.APPLICATION_JSON_VALUE})
  @ResponseBody
  public void createDeployment(@PathVariable("connectionName") String connectionName, @RequestBody TerracottaClusterConfiguration terracottaClusterConfiguration) {

    Config config = new ConfigBuilder().build();
    try (KubernetesClient client = new DefaultKubernetesClient(config)) {
      // Create a namespace for all our stuff
      Namespace ns = new NamespaceBuilder().withNewMetadata().withName("thisisatest").addToLabels("this", "rocks").endMetadata().build();
      log.info("Created namespace : " + client.namespaces().createOrReplace(ns));


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

  @GetMapping("/cluster/{connectionName}")
  @ResponseBody
  public ResponseEntity listDeployment() {
    return null;
  }

  @DeleteMapping("/cluster/{connectionName}")
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
