package org.terracotta.k8s.operator.app.watcher;

import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terracotta.k8s.operator.app.crd.TerracottaDBCluster;
import org.terracotta.k8s.operator.app.service.TheService;
import org.terracotta.k8s.operator.shared.TerracottaClusterConfiguration;

import java.util.Map;

public class ClusterWatcher implements Watcher<TerracottaDBCluster> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClusterWatcher.class);

  private final TheService theService;

  public ClusterWatcher(TheService theService) {
    this.theService = theService;
  }

  @Override
  public void eventReceived(Action action, TerracottaDBCluster resource) {
    String clusterName = resource.getMetadata().getName();
    if (Action.ADDED.equals(action)) {
      if (theService.configMapExists("tc-configs")) {
        LOGGER.info("Cluster is already started");
        return;
      }
      TerracottaClusterConfiguration terracottaClusterConfiguration = resource.getSpec();
      LOGGER.info("Starting the cluster with name '{}' and configuration '{}'",
                  clusterName,
                  terracottaClusterConfiguration);

      // check the license exists
      theService.checkLicense();

      // create the tc configs
      Map<String, String> tcConfigs = theService.generateTerracottaConfig(terracottaClusterConfiguration);

      // store them in a configmap
      theService.storeTcConfigConfigMap(tcConfigs);

      // store the terracottaClusterConfiguration in a configMap
      theService.storeTerracottaClusterConfigurationConfigMap(terracottaClusterConfiguration);

      // create the Terracotta Statefulsets
      theService.createCluster(terracottaClusterConfiguration);

      LOGGER.info("Successfully started the cluster with name '{}' and configuration '{}'",
                  clusterName,
                  terracottaClusterConfiguration);
    } else if (Action.DELETED.equals(action)) {
      LOGGER.info("Deleting the cluster with name '{}'", clusterName);
      TerracottaClusterConfiguration terracottaClusterConfiguration = theService.retrieveTerracottaClusterConfigurationConfigMap();
      if (terracottaClusterConfiguration != null) {
        theService.deleteCluster();
      } else {
        LOGGER.error("No cluster found with name '{}'", clusterName);
      }
      LOGGER.info("Successfully deleted the cluster with name '{}'", clusterName);
    }
  }

  @Override
  public void onClose(KubernetesClientException cause) {

  }
}
