package org.terracotta.k8s.operator.app;

import com.google.gson.reflect.TypeToken;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CustomObjectsApi;
import io.kubernetes.client.models.V1Namespace;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Watch;
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
import java.util.HashSet;
import java.util.Set;

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


        // Create custom resource definition of tcdb if not already exist.
        if (client.customResourceDefinitions().list().getItems().stream().anyMatch(crd -> crd.getMetadata().getName().equals("tcdbs.terracotta.com"))) {
          System.out.println("TCDB -- CRD already present");
          return;
        }

        client.customResourceDefinitions().create(new CustomResourceDefinitionBuilder()
          .withApiVersion("apiextensions.k8s.io/v1beta1")
          .withNewMetadata().withName("tcdbs.terracotta.com").endMetadata()
          .withNewSpec().withGroup("terracotta.com").withVersion("v1").withScope("Namespaced").withNewNames().withKind("TCDB").withShortNames("tcdb").withPlural("tcdbs").endNames().endSpec()
          .build());

        System.out.println("TCDB -- CRD created.");
      }

    }
  }

  @PostConstruct
  public void watchK8s()  {
    Set<String> clusterNames = new HashSet<>();

    new Thread(() -> {
      System.out.println("Starting watcher thread to watch for TCDB creation...");

      while (true) {
        try {
          ApiClient client = Config.defaultClient();
          Configuration.setDefaultApiClient(client);

          CustomObjectsApi api = new CustomObjectsApi();
          Watch<V1Namespace> watch = Watch.createWatch(
            client,
            api.listNamespacedCustomObjectCall("terracotta.com", "v1", "thisisatest", "tcdbs", "tcdb", null, null, Boolean.TRUE, null, null),
            new TypeToken<Watch.Response<V1Namespace>>() {
            }.getType());

          watch.forEach(item -> {
            System.out.printf("%s : %s%n", item.type, item.object.getMetadata().getName());
            if (!item.type.equals("ADDED") || clusterNames.contains(item.object.getMetadata().getName())) return;

            System.out.println("starting cluster deployment....");
          });
        } catch (Exception e) {
          System.out.println(e);
        }
      }
    }).start();
  }
}
