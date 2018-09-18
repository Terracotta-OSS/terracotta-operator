package org.terracotta.k8s.operator.app;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.terracotta.k8s.operator.shared.ServerStatus;
import org.terracotta.k8s.operator.shared.ServerStatusResponse;

import javax.annotation.PostConstruct;

/**
 * @author Henri Tremblay
 */
@SpringBootApplication
@RestController
@RequestMapping("/api")
public class TerracottaOperatorApp {

  private static final Logger log = LoggerFactory.getLogger(TerracottaOperatorApp.class);

  @GetMapping("/status")
  @ResponseBody
  public ServerStatusResponse status() {
    return new ServerStatusResponse(ServerStatus.OK);
  }

  public static void main(String[] args) {
    SpringApplication.run(TerracottaOperatorApp.class, args);
  }

  @PostConstruct
  public void initApplication() {
    Config config = new ConfigBuilder().build();

//    try (KubernetesClient client = new DefaultKubernetesClient(config)) {
//      // Create a namespace for all our stuff
//      Namespace ns = new NamespaceBuilder().withNewMetadata().withName("thisisatest").addToLabels("this", "rocks").endMetadata().build();
//      log.info("Created or replaced namespace : " + client.namespaces().createOrReplace(ns));
//
//      client.apps().deployments().inNamespace("thisisatest").list().getItems().forEach(deployment -> {
//        log.warn("Found this deployment : " + deployment);
//        client.apps().deployments().inNamespace("thisisatest").withName(deployment.getMetadata().getName()).delete();
//        log.warn("Deleted this deployment : " + deployment);
//
//      });
//
//    }
  }
}
