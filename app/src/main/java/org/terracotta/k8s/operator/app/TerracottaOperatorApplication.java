package org.terracotta.k8s.operator.app;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.terracotta.k8s.operator.shared.ServerStatus;
import org.terracotta.k8s.operator.shared.ServerStatusResponse;

import javax.annotation.PostConstruct;

@SpringBootApplication
@RequestMapping("/api")
public class TerracottaOperatorApplication {

  private static final Logger log = LoggerFactory.getLogger(TerracottaOperatorApplication.class);


  public static void main(String[] args) {
    SpringApplication.run(TerracottaOperatorApplication.class, args);
  }


  @ResponseBody
  public ServerStatusResponse status() {
    return new ServerStatusResponse(ServerStatus.OK);
  }

  @Autowired
  private KubernetesClientFactory kubernetesClientFactory;

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
