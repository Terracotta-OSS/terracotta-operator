package org.terracotta.k8s.operator.app.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.DoneableService;
import io.fabric8.kubernetes.api.model.EnvVarSourceBuilder;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSetBuilder;
import io.fabric8.kubernetes.api.model.batch.Job;
import io.fabric8.kubernetes.api.model.batch.JobBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.terracotta.k8s.operator.app.KubernetesClientFactory;
import org.terracotta.k8s.operator.app.TerracottaOperatorException;
import org.terracotta.k8s.operator.shared.TerracottaClusterConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class TheService {

  private static final Logger log = LoggerFactory.getLogger(TheService.class);

  @Autowired
  private KubernetesClientFactory kubernetesClientFactory;

  @Autowired
  private ObjectMapper objectMapper;


  public Map<String, String> generateTerracottaConfigs(TerracottaClusterConfiguration terracottaClusterConfiguration) {

    Map<String, String> terracottaConfigs = new TreeMap<>();

    for (int stripeRow = 0; stripeRow < terracottaClusterConfiguration.getStripes(); stripeRow++) {

      // starts xml
      StringBuilder sb;
      sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
          "\n" +
          "<tc-config xmlns=\"http://www.terracotta.org/config\" \n" +
          "           xmlns:ohr=\"http://www.terracotta.org/config/offheap-resource\"\n" +
          "           xmlns:backup=\"http://www.terracottatech.com/config/backup-restore\"\n" +
          "           xmlns:data=\"http://www.terracottatech.com/config/data-roots\">\n" +
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
      // dataroots
      sb.append("    <config>\n" +
          "      <data:data-directories>\n");
      // platform persistece
//        sb.append("        <data:directory name=\"PLATFORM\" use-for-platform=\"true\">" + path.getValue() + "</data:directory>\n");

      // do not know why but .getComponent(x,y) does not work
//        for (int r = getDataRootFirstRow(); r < dataRootGrid.getRows(); r++) {
//          TextField name = (TextField) dataRootGrid.getComponent(DATAROOT_NAME_COLUMN, r);
//          TextField path = (TextField) dataRootGrid.getComponent(DATAROOT_PATH_COLUMN, r);
//          sb.append("        <data:directory name=\"" + name.getValue() + "\" use-for-platform=\"false\">" + path.getValue() + "</data:directory>\n");
//        }


      terracottaClusterConfiguration.getDataroots().entrySet().stream().forEach(datarootConfig -> {
        if (datarootConfig.getKey().equals("PLATFORM")) {
          sb.append("        <data:directory name=\"" + datarootConfig.getKey() + "\" use-for-platform=\"true\">/data/dataroots/" + datarootConfig.getKey() + "</data:directory>\n");
        } else {
          sb.append("        <data:directory name=\"" + datarootConfig.getKey() + "\" use-for-platform=\"false\">/data/dataroots/" + datarootConfig.getKey() + "</data:directory>\n");
        }

      });


      // end data roots
      sb.append("      </data:data-directories>\n" +
          "    </config>\n" +
          "\n");

      // backup
//          sb.append("    <service>\n" +
//              "      <backup:backup-restore>\n" +
//              "        <backup:backup-location path=\"" + path.getValue() + "\" />\n" +
//              "      </backup:backup-restore>\n" +
//              "    </service>\n" +
//              "\n");
//        }

      // security
//        if (serverSecurityCheckBox.getValue()) {
//
//          sb.append("    <service xmlns:security=\"http://www.terracottatech.com/config/security\">\n" +
//              "      <security:security>\n" +
//              "        <security:security-root-directory>" + serverSecurityField.getValue() + "</security:security-root-directory>\n" +
//              "        <security:ssl-tls/>\n" +
//              "        <security:authentication>\n" +
//              "          <security:file/>\n" +
//              "        </security:authentication>\n" +
//              "      </security:security>\n" +
//              "    </service>\n" +
//              "\n");
//        }
//      }

      // servers
      sb.append("  </plugins>\n" +
          "\n" +
          "  <servers>\n" +
          "\n");

      for (int serverCol = 0; serverCol < terracottaClusterConfiguration.getServersPerStripe(); serverCol++) {
        sb.append("    <server host=\"terracotta-" + stripeRow + "-" + serverCol + ".stripe-" + stripeRow + "\" name=\"terracotta-" + stripeRow + "-" + serverCol + "\">\n" +
            "      <logs>/opt/softwareag/terracotta/server/conf/stdout:</logs>\n" +
            "      <tsa-port>9410</tsa-port>\n" +
            "      <tsa-group-port>9430</tsa-group-port>\n" +
            "    </server>\n\n");
      }

      // reconnect window
      sb.append("    <client-reconnect-window>" + terracottaClusterConfiguration.getClientReconnectWindow() + "</client-reconnect-window>\n\n");
      sb.append("  </servers>\n\n");

//      if (consistencyGroup.isSelected(CONSISTENCY)) {
//        int votersCount = Integer.parseInt(votersCountTextField.getValue());
//        sb.append("  <failover-priority>\n" +
//            "    <consistency>\n" +
//            "      <voter count=\"" + votersCount + "\"/>\n" +
//            "    </consistency>\n" +
//            "  </failover-priority>\n\n");
//      }

      // ends XML
      sb.append("</tc-config>");
      terracottaConfigs.put("stripe-" + stripeRow + ".xml", sb.toString());

//      String filename = "tc-config-stripe-" + stripeRow + ".xml";
//      File location = new File(settings.getKitPath(), filename);
//      tcConfigLocationPerStripe.put("stripe-" + stripeRow, location);
//
//      String xml;
//      if (location.exists() && !skipConfirmOverwrite) {
//        Notification.show("Config already found: " + location.getName());
//        try {
//          xml = new String(Files.readAllBytes(location.toPath()), "UTF-8");
//        } catch (IOException e) {
//          throw new UncheckedIOException(e);
//        }
//      } else {
//        xml = sb.toString();
//        try {
//          Files.write(location.toPath(), xml.getBytes("UTF-8"));
//        } catch (IOException e) {
//          throw new UncheckedIOException(e);
//        }
//      }
//
//      tcConfigXml.setValue(tcConfigXml.getValue() + xml + "\n\n");

    }
    return terracottaConfigs;

  }

  int retrieveAmountFromString(String value) {
    String amountAsString = value.replaceAll("[^0-9]+", "");
    return Integer.valueOf(amountAsString);
  }

  String retrieveUnitFromString(String value) {
    return value.replaceAll("[0-9]+", "");
  }

  public void storeTcConfigsConfigMap(Map<String, String> tcConfigs) {
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

  public void storeLicenseConfigMap(String license) {
    try (KubernetesClient client = kubernetesClientFactory.retrieveKubernetesClient()) {
      ConfigMap configMap = new ConfigMapBuilder()
          .withNewMetadata()
          .withName("license")
          .endMetadata()
          .withData(new HashMap<String, String>() {{
            put("license.xml", license);
          }})
          .build();

      client.configMaps().inNamespace("thisisatest").createOrReplace(configMap);
    }
  }

  public String readLicenseConfigMap() {
    try (KubernetesClient client = kubernetesClientFactory.retrieveKubernetesClient()) {
      ConfigMap configMap = client.configMaps()
          .inNamespace("thisisatest")
          .withName("license")
          .get();
      if (configMap != null) {
        return configMap.getData().get("license.xml");
      }
      return null;
    }
  }

  public boolean configMapExists(String configMapName) {
    try (KubernetesClient client = kubernetesClientFactory.retrieveKubernetesClient()) {
      return  client.configMaps()
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

  public void storeTerracottaClusterConfigurationConfigMap(String clusterName, TerracottaClusterConfiguration terracottaClusterConfiguration) {
    try {
      String serializedCLusterConfig = objectMapper.writeValueAsString(terracottaClusterConfiguration);
      try (KubernetesClient client = kubernetesClientFactory.retrieveKubernetesClient()) {
        ConfigMap configMap = new ConfigMapBuilder()
            .withNewMetadata()
            .withName(clusterName)
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

  public boolean configureCluster(String clusterName, TerracottaClusterConfiguration terracottaClusterConfiguration) {

    List<String> configPaths = IntStream.range(0, terracottaClusterConfiguration.getStripes())
        .mapToObj(stripeNumber -> "/configs/stripe-" + stripeNumber + ".xml").collect(Collectors.toList());
    String[] clusterToolArgs = new ArrayList<String>() {{
      add("configure");
      add("-n");
      add(clusterName);
      addAll(configPaths);
      add("-l");
      add("/licenses/license.xml");
    }}.toArray(new String[]{});

    try (KubernetesClient client = kubernetesClientFactory.retrieveKubernetesClient()) {

      Job clusterToolConfigureJob = new JobBuilder()
          .withNewMetadata()
          .withName("cluster-tool")
          .endMetadata()
          .withNewSpec()
          .withNewTemplate()
          .withNewMetadata()
          .withName("cluster-tool")
          .endMetadata()
          .withNewSpec()
          .addNewContainer()
          .withName("cluster-tool")
          .withImage("terracotta/terracotta-cluster-tool:latest")
          .withImagePullPolicy("Always")
          .addNewEnv()
          .withName("ACCEPT_EULA")
          .withValue("Y")
          .endEnv()
          .withArgs(clusterToolArgs)
          .addNewVolumeMount()
          .withName("config-volume")
          .withMountPath("/configs")
          .endVolumeMount()
          .addNewVolumeMount()
          .withName("license-volume")
          .withMountPath("/licenses")
          .endVolumeMount()
          .endContainer()
          .addNewVolume()
          .withName("config-volume")
          .withNewConfigMap()
          .withName("tc-configs")
          .endConfigMap()
          .endVolume()
          .addNewVolume()
          .withName("license-volume")
          .withNewConfigMap()
          .withName("license")
          .endConfigMap()
          .endVolume()
          .withRestartPolicy("OnFailure")
          .endSpec()
          .endTemplate()
          .endSpec()
          .build();

      client.batch().jobs().inNamespace("thisisatest").create(clusterToolConfigureJob);


      //waiting for the job to complete
      // super cool except the event is never fired :-(
//      final CountDownLatch watchLatch = new CountDownLatch(1);
//      try (Watch watch = client.pods().inNamespace("thisisatest").withLabel("cluster-tool").watch(new Watcher<Pod>() {
//        @Override
//        public void eventReceived(Action action, Pod aPod) {
//          if (aPod.getStatus().getPhase().equals("Succeeded")) {
//            watchLatch.countDown();
//          }
//        }
//
//        @Override
//        public void onClose(KubernetesClientException e) {
//          // Ignore
//        }
//      })) {
//        return watchLatch.await(2, TimeUnit.MINUTES);
//      } catch (KubernetesClientException | InterruptedException e) {
//        log.error("Could not watch pod", e);
//      }
//    } catch (KubernetesClientException exception) {
//      log.error("An error occured while processing cronjobs:", exception.getMessage());
//    }
    }
    return true;

  }

  public void deleteCluster(String connectionName, TerracottaClusterConfiguration terracottaClusterConfiguration) {

    Thread deleteTMCThread =  new Thread(() -> {
      try (KubernetesClient client = kubernetesClientFactory.retrieveKubernetesClient()) {
        client.services().inNamespace("thisisatest").withName("tmc").withGracePeriod(0L).delete();
        client.apps().statefulSets().inNamespace("thisisatest").withName("tmc").withGracePeriod(0L).delete();
        client.batch().jobs().inNamespace("thisisatest").withName("cluster-tool").withGracePeriod(0L).delete();
      }
    });
    deleteTMCThread.start();

    IntStream.range(0, terracottaClusterConfiguration.getStripes()).parallel().forEach(stripeNumber -> {
      try (KubernetesClient client = kubernetesClientFactory.retrieveKubernetesClient()) {
        client.services().inNamespace("thisisatest").withName("stripe-" + stripeNumber).withGracePeriod(0L).delete();
        client.apps().statefulSets().inNamespace("thisisatest").withName("terracotta-" + stripeNumber).withGracePeriod(0L).delete();
      }
    });

    deleteConfigMap(connectionName);
    deleteConfigMap("tc-configs");

  }

  public void createCluster(TerracottaClusterConfiguration terracottaClusterConfiguration) {
    try (KubernetesClient client = kubernetesClientFactory.retrieveKubernetesClient()) {

      io.fabric8.kubernetes.api.model.Service tmcService = new ServiceBuilder()
          .withNewMetadata()
          .withName("tmc")
          .endMetadata()
          .withNewSpec()
          .addNewPort()
          .withName("tmc-port")
          .withPort(9480)
          .endPort()
          .withType("LoadBalancer")
          .addToSelector("app", "tmc")
          .endSpec()
          .build();

      io.fabric8.kubernetes.api.model.Service resultTmcService = client.services().inNamespace("thisisatest").create(tmcService);

      String terracottaUrl = constructTerracottaServerUrl(terracottaClusterConfiguration);

      StatefulSet tmcStatefulSet = new StatefulSetBuilder()
          .withNewMetadata()
          .withName("tmc")
          .endMetadata()
          .withNewSpec()
          .withNewSelector()
          .addToMatchLabels("app", "tmc")
          .endSelector()
          .withServiceName("tmc-internal")
          .withReplicas(1)
          .withNewTemplate()
          .withNewMetadata()
          .addToLabels("app", "tmc")
          .endMetadata()
          .withNewSpec()
          .addNewContainer()
          .withName("tmc")
          .withImage("terracotta/tmc:latest")
          .withImagePullPolicy("Always")
          .addNewEnv()
          .withName("ACCEPT_EULA")
          .withValue("Y")
          .endEnv()
          .addNewEnv()
          .withName("TMS_AUTOCONNECT")
          .withValue("true")
          .endEnv()
          .addNewEnv()
          .withName("TMS_DEFAULTURL")
          .withValue(terracottaUrl)
          .endEnv()
          .addNewPort()
          .withName("tmc-port")
          .withContainerPort(9480)
          .endPort()
          .endContainer()
          .endSpec()
          .endTemplate()
          .endSpec()
          .build();

      StatefulSet resultTmcStatefulSet = client.apps().statefulSets().inNamespace("thisisatest").create(tmcStatefulSet);


      IntStream.range(0, terracottaClusterConfiguration.getStripes()).forEach(stripeNumber -> {

        io.fabric8.kubernetes.api.model.Service service = new ServiceBuilder()
            .withNewMetadata()
            .withName("stripe-" + stripeNumber)
            .addToLabels("app", "stripe-" + stripeNumber)
            .addToAnnotations("service.alpha.kubernetes.io/tolerate-unready-endpoints", "true")
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
            .addToSelector("app", "stripe-" + stripeNumber)
            .endSpec()
            .build();

        io.fabric8.kubernetes.api.model.Service resultService = client.services().inNamespace("thisisatest").create(service);


        StatefulSet statefulSet = new StatefulSetBuilder()
            .withNewMetadata()
            .withName("terracotta-" + stripeNumber)
            .endMetadata()
            .withNewSpec()
            .withNewSelector()
            .addToMatchLabels("app", "stripe-" + stripeNumber)
            .endSelector()
            .withServiceName("stripe-" + stripeNumber)
            .withReplicas(terracottaClusterConfiguration.getServersPerStripe())
            .withNewTemplate()
            .withNewMetadata()
            .addToLabels("app", "stripe-" + stripeNumber)
            .endMetadata()
            .withNewSpec()
            .addNewContainer()
            .withName("terracotta")
            .withImage("terracotta/terracotta-server:latest")
            .withCommand("bin/start-tc-server.sh")
            .withArgs(Arrays.asList("-f", "/configs/stripe-" + stripeNumber + ".xml", "-n", "$(POD_NAME)"))
            .withImagePullPolicy("Always")
            .addNewEnv()
            .withName("ACCEPT_EULA")
            .withValue("Y")
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
      });

    }
  }

  public String constructTerracottaServerUrl(TerracottaClusterConfiguration terracottaClusterConfiguration) {
    String terracottaUrl;
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("terracotta://");
    IntStream.range(0, terracottaClusterConfiguration.getStripes()).forEach(stripeNumber -> {
      IntStream.range(0, terracottaClusterConfiguration.getServersPerStripe()).forEach(serverNumber -> {
        stringBuilder.append("terracotta-" + stripeNumber + "-" + serverNumber + ".stripe-" + stripeNumber + ":9410");
        stringBuilder.append(",");
      });
    });
    terracottaUrl = stringBuilder.toString().substring(0, stringBuilder.toString().length() - 1);
    return terracottaUrl;
  }

  public TerracottaClusterConfiguration retrieveTerracottaClusterConfigurationConfigMap(String connectionName) {
    try (KubernetesClient client = kubernetesClientFactory.retrieveKubernetesClient()) {
      ConfigMap configMap = client.configMaps()
          .inNamespace("thisisatest")
          .withName(connectionName)
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

  public String retrieveTmcUrl() {
    try (KubernetesClient client = kubernetesClientFactory.retrieveKubernetesClient()) {
      Resource<io.fabric8.kubernetes.api.model.Service, DoneableService> tmcService = client.services().inNamespace("thisisatest").withName("tmc");
      if (tmcService != null) {
        int tmcPort = client.services().inNamespace("thisisatest").withName("tmc").get().getSpec().getPorts().get(0).getNodePort();
        String hostName = client.nodes().list().getItems().get(0).getSpec().getExternalID();
        return "http://" + hostName +":" + tmcPort;
      } else {
        return null;
      }
    }
  }
}
