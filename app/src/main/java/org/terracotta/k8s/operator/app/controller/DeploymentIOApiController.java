package org.terracotta.k8s.operator.app.controller;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.AppsV1Api;
import io.kubernetes.client.models.V1DeleteOptions;
import io.kubernetes.client.models.V1Deployment;
import io.kubernetes.client.models.V1DeploymentBuilder;
import io.kubernetes.client.models.V1Status;
import io.kubernetes.client.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RequestMapping("/deprecated")
@RestController
public class DeploymentIOApiController {

  private static final Logger log = LoggerFactory.getLogger(DeploymentIOApiController.class);

  @Value("${application.namespace}")
  String namespace;

  @PostMapping("/io/{connectionName}")
  @ResponseBody
  public void createDeployment() throws IOException, ApiException {

    ApiClient client = Config.defaultClient();
    Configuration.setDefaultApiClient(client);

    AppsV1Api api = new AppsV1Api();

    V1Deployment v1Deployment = new V1DeploymentBuilder()
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

    api.createNamespacedDeployment(namespace, v1Deployment, null, "true", "false");
    log.info("Created deployment : " + v1Deployment);
  }

  @DeleteMapping("/io/{connectionName}")
  @ResponseBody
  public void deleteDeployment() throws ApiException, IOException {

    ApiClient client = Config.defaultClient();
    Configuration.setDefaultApiClient(client);

    AppsV1Api api = new AppsV1Api();
    log.info("Deleting terracotta");

    V1Status v1Status = api.deleteNamespacedDeployment("terracotta", namespace, new V1DeleteOptions(), "true", "false", 0, false, null);

    System.out.println(v1Status);

  }

}
