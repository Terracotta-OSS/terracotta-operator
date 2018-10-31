package org.terracotta.k8s.operator.app.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.EnvVarSourceBuilder;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSetBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.terracotta.k8s.operator.app.KubernetesClientFactory;
import org.terracotta.k8s.operator.app.TerracottaOperatorException;
import org.terracotta.k8s.operator.shared.TerracottaClusterConfiguration;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.IntStream;

@Service
public class TheService {

  private static final Logger log = LoggerFactory.getLogger(TheService.class);

  @Autowired
  private KubernetesClientFactory kubernetesClientFactory;

  @Autowired
  private ObjectMapper objectMapper;


  public Map<String, String> generateTerracottaConfig(TerracottaClusterConfiguration terracottaClusterConfiguration) {

    Map<String, String> terracottaConfigs = new TreeMap<>();

    // starts xml
    StringBuilder sb;
    sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "\n" +
      "<tc-config xmlns=\"http://www.terracotta.org/config\" \n" +
      "           xmlns:ohr=\"http://www.terracotta.org/config/offheap-resource\"\n" +
      "\n" +
      "  <plugins>\n" +
      "\n");

    // offheaps
    if (terracottaClusterConfiguration.getOffheaps().size() > 1) {
      sb.append("    <config>\n" +
        "      <ohr:offheap-resources>\n");

      terracottaClusterConfiguration.getOffheaps().entrySet().stream().forEach(offheapConfig -> {

        int offHeapAmount = retrieveAmountFromString(offheapConfig.getValue());
        String offHeapUnit = retrieveUnitFromString(offheapConfig.getValue());


        sb.append("        <ohr:resource name=\"" + offheapConfig.getKey() + "\" unit=\"" + offHeapUnit + "\">" + offHeapAmount + "</ohr:resource>\n");

      });
      sb.append("      </ohr:offheap-resources>\n" +
        "    </config>\n" +
        "\n");
    }


    // servers
    sb.append("  </plugins>\n" +
      "\n" +
      "  <servers>\n" +
      "\n");

    for (int serverCol = 0; serverCol < terracottaClusterConfiguration.getServersPerStripe(); serverCol++) {
      sb.append("    <server host=\"terracotta-" + serverCol + ".terracotta" + "\" name=\"terracotta-" + serverCol + "\">\n" +
        "      <logs>stdout:</logs>\n" +
        "      <tsa-port>9410</tsa-port>\n" +
        "      <tsa-group-port>9430</tsa-group-port>\n" +
        "    </server>\n\n");
    }

    // reconnect window
    sb.append("    <client-reconnect-window>" + terracottaClusterConfiguration.getClientReconnectWindow() + "</client-reconnect-window>\n\n");
    sb.append("  </servers>\n\n");

    // ends XML
    sb.append("</tc-config>");
    terracottaConfigs.put("terracotta.xml", sb.toString());

    return terracottaConfigs;

  }

  int retrieveAmountFromString(String value) {
    String amountAsString = value.replaceAll("[^0-9]+", "");
    return Integer.valueOf(amountAsString);
  }

  String retrieveUnitFromString(String value) {
    return value.replaceAll("[0-9]+", "");
  }

  public void storeTcConfigConfigMap(Map<String, String> tcConfigs) {
    try (KubernetesClient client = kubernetesClientFactory.retrieveKubernetesClient()) {

      ConfigMap configMap = new ConfigMapBuilder()
        .withNewMetadata()
        .withName("tc-configs")
        .endMetadata()
        .withData(tcConfigs)
        .build();


      client.configMaps().inNamespace("thisisatest").createOrReplace(configMap);
    }


  }

  public boolean configMapExists(String configMapName) {
    try (KubernetesClient client = kubernetesClientFactory.retrieveKubernetesClient()) {
      return client.configMaps()
        .inNamespace("thisisatest")
        .withName(configMapName)
        .get() != null;
    }
  }

  public boolean deleteConfigMap(String configMapName) {
    try (KubernetesClient client = kubernetesClientFactory.retrieveKubernetesClient()) {
      ConfigMap configMap = new ConfigMapBuilder()
        .withNewMetadata()
        .withName(configMapName)
        .endMetadata()
        .build();

      return client.configMaps().inNamespace("thisisatest").delete(configMap);
    }
  }

  public void checkLicense() {
    try (KubernetesClient client = kubernetesClientFactory.retrieveKubernetesClient()) {
      ConfigMap configMap = client.configMaps()
        .inNamespace("thisisatest")
        .withName("license")
        .get();
      if (configMap == null) {
        throw new TerracottaOperatorException("You first need to upload a license - none was found under the ConfigMap 'license' in this namespace");
      }
    }

  }

  public void storeTerracottaClusterConfigurationConfigMap(TerracottaClusterConfiguration terracottaClusterConfiguration) {
    try {
      String serializedCLusterConfig = objectMapper.writeValueAsString(terracottaClusterConfiguration);
      try (KubernetesClient client = kubernetesClientFactory.retrieveKubernetesClient()) {
        ConfigMap configMap = new ConfigMapBuilder()
          .withNewMetadata()
          .withName("terracotta-operator-config")
          .endMetadata()
          .withData(new HashMap<String, String>() {{
            put("terracottaClusterConfiguration.json", serializedCLusterConfig);
          }})
          .build();

        client.configMaps().inNamespace("thisisatest").createOrReplace(configMap);
      }
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Problem serializing terracottaClusterConfiguration", e);
    }

  }

  public void deleteCluster() {

    Thread deleteTMCThread = new Thread(() -> {
      try (KubernetesClient client = kubernetesClientFactory.retrieveKubernetesClient()) {
        client.services().inNamespace("thisisatest").withName("tmc").withGracePeriod(0L).delete();
        client.apps().statefulSets().inNamespace("thisisatest").withName("tmc").withGracePeriod(0L).delete();
        client.batch().jobs().inNamespace("thisisatest").withName("cluster-tool").withGracePeriod(0L).delete();
      }
    });
    deleteTMCThread.start();

    try (KubernetesClient client = kubernetesClientFactory.retrieveKubernetesClient()) {
      client.services().inNamespace("thisisatest").withName("terracotta").withGracePeriod(0L).delete();
      client.apps().statefulSets().inNamespace("thisisatest").withName("terracotta-0").withGracePeriod(0L).delete();
    }

    deleteConfigMap("terracotta-operator-config");
    deleteConfigMap("tc-configs");

  }

  public void createCluster(TerracottaClusterConfiguration terracottaClusterConfiguration) {
    try (KubernetesClient client = kubernetesClientFactory.retrieveKubernetesClient()) {

      io.fabric8.kubernetes.api.model.Service terracottaService = new ServiceBuilder()
        .withNewMetadata()
        .withName("terracotta")
        .addToLabels("app", "terracotta")
        .addToAnnotations("terracottaService.alpha.kubernetes.io/tolerate-unready-endpoints", "true")
        .endMetadata()
        .withNewSpec()
        .addNewPort()
        .withName("terracotta-port")
        .withPort(9410)
        .endPort()
        .addNewPort()
        .withName("sync-port")
        .withPort(9430)
        .endPort()
        .withClusterIP("None")
        .addToSelector("app", "terracotta")
        .endSpec()
        .build();

      io.fabric8.kubernetes.api.model.Service resultService = client.services().inNamespace("thisisatest").create(terracottaService);


      StatefulSet statefulSet = new StatefulSetBuilder()
        .withNewMetadata()
        .withName("terracotta")
        .endMetadata()
        .withNewSpec()
        .withNewSelector()
        .addToMatchLabels("app", "terracotta")
        .endSelector()
        .withServiceName("terracotta")
        .withReplicas(terracottaClusterConfiguration.getServersPerStripe())
        .withNewTemplate()
        .withNewMetadata()
        .addToLabels("app", "terracotta")
        .endMetadata()
        .withNewSpec()
        .addNewContainer()
        .withName("terracotta")
        .withImage("terracotta/terracotta-server-oss:5.5.1")
        .withCommand("bin/start-tc-server.sh")
        .withArgs(Arrays.asList("-f", "/configs/stripe-0.xml", "-n", "$(POD_NAME)"))
        .withImagePullPolicy("Never")
        .addNewEnv()
//        .withName("ACCEPT_EULA")
//        .withValue("Y")
        .endEnv()
        .addNewEnv()
        .withName("POD_NAME")
        .withValueFrom(new EnvVarSourceBuilder().withNewFieldRef().withFieldPath("metadata.name").endFieldRef().build())
        .endEnv()
        .addNewPort()
        .withName("terracotta-port")
        .withContainerPort(9410)
        .endPort()
        .addNewPort()
        .withName("sync-port")
        .withContainerPort(9430)
        .endPort()
        .addNewVolumeMount()
        .withName("config-volume")
        .withMountPath("/configs")
        .endVolumeMount()
        .endContainer()
        .addNewVolume()
        .withName("config-volume")
        .withNewConfigMap()
        .withName("tc-configs")
        .endConfigMap()
        .endVolume()
        .endSpec()
        .endTemplate()
        .endSpec()
        .build();

      StatefulSet resultStatefulSet = client.apps().statefulSets().inNamespace("thisisatest").create(statefulSet);

    }
  }

  public String constructTerracottaServerUrl(TerracottaClusterConfiguration terracottaClusterConfiguration) {
    String terracottaUrl;
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("terracotta://");
    IntStream.range(0, terracottaClusterConfiguration.getServersPerStripe()).forEach(serverNumber -> {
      stringBuilder.append("terracotta-0-" + serverNumber + "terracotta:9410");
      stringBuilder.append(",");
    });
    terracottaUrl = stringBuilder.toString().substring(0, stringBuilder.toString().length() - 1);
    return terracottaUrl;
  }

  public TerracottaClusterConfiguration retrieveTerracottaClusterConfigurationConfigMap() {
    try (KubernetesClient client = kubernetesClientFactory.retrieveKubernetesClient()) {
      ConfigMap configMap = client.configMaps()
        .inNamespace("thisisatest")
        .withName("terracotta-operator-config")
        .get();
      if (configMap != null) {
        try {
          return objectMapper.readValue(configMap.getData().get("terracottaClusterConfiguration.json"), TerracottaClusterConfiguration.class);
        } catch (IOException e) {
          throw new TerracottaOperatorException("Could not retrieve the terracottaClusterConfiguration", e);
        }
      }
      return null;
    }
  }
}
