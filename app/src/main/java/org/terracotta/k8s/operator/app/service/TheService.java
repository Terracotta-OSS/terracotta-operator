package org.terracotta.k8s.operator.app.service;


import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.terracotta.k8s.operator.app.KubernetesClientFactory;
import org.terracotta.k8s.operator.app.model.TerracottaClusterConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Service
public class TheService {

  @Autowired
  private KubernetesClientFactory kubernetesClientFactory;

  public Map<String, String> generateTerracottaConfigs(TerracottaClusterConfiguration terracottaClusterConfiguration) {

    Map<String, String> terracottaConfigs = new TreeMap<>();

    for (int stripeRow = 0; stripeRow < terracottaClusterConfiguration.getStripes() ; stripeRow++) {

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
          sb.append("        <ohr:resource name=\"" + offheapConfig.getKey() + "\" unit=\"MB\">" + offheapConfig.getValue() + "</ohr:resource>\n");

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
        if(datarootConfig.getKey().equals("PLATFORM")) {
          sb.append("        <data:directory name=\"" + datarootConfig.getKey() + "\" use-for-platform=\"true\">" + datarootConfig.getValue() + "</data:directory>\n");
        } else {
          sb.append("        <data:directory name=\"" + datarootConfig.getKey() + "\" use-for-platform=\"false\">" + datarootConfig.getValue() + "</data:directory>\n");
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
        String serverName = "terracotta-" + serverCol;
        sb.append("    <server host=\"" + serverName + ".stripe-" + stripeRow + "\" name=\"" + serverName + "\">\n" +
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


  public void createStripesConfigMap(Map<String, String> tcConfigs) {
    try(KubernetesClient client = kubernetesClientFactory.retrieveKubernetesClient()) {

      ConfigMap configMap = new ConfigMapBuilder()
          .withNewMetadata()
          .withName("tc-configs")
          .endMetadata()
          .withData(tcConfigs)
          .build();


      client.configMaps().inNamespace("thisisatest").createOrReplace(configMap);
    }


  }

  public void createLicenseConfigMap(String license) {
    try(KubernetesClient client = kubernetesClientFactory.retrieveKubernetesClient()) {

      ConfigMap configMap = new ConfigMapBuilder()
          .withNewMetadata()
          .withName("license")
          .endMetadata()
          .withData(new HashMap<String, String>() {{put("license.xml",license);}})
          .build();


      client.configMaps().inNamespace("thisisatest").createOrReplace(configMap);
    }


  }

  public void deleteLicenseConfigMap() {
    try(KubernetesClient client = kubernetesClientFactory.retrieveKubernetesClient()) {

      ConfigMap configMap = new ConfigMapBuilder()
        .withNewMetadata()
        .withName("license")
        .endMetadata()
        .build();

      client.configMaps().inNamespace("thisisatest").delete(configMap);
    }

  }

}
