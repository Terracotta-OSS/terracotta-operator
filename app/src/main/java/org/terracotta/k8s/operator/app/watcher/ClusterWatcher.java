package org.terracotta.k8s.operator.app.watcher;

import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terracotta.k8s.operator.app.crd.TerracottaOSSCluster;
import org.terracotta.k8s.operator.app.service.TheService;
import org.terracotta.k8s.operator.shared.TerracottaClusterConfiguration;

import java.util.Map;

public class ClusterWatcher implements Watcher<TerracottaOSSCluster> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClusterWatcher.class);

  private final TheService theService;

  public ClusterWatcher(TheService theService) {
    this.theService = theService;
  }

  @Override
  public void eventReceived(Action action, TerracottaOSSCluster resource) {
    LOGGER.info("Received an event with action : " + action.toString() + " and the following resource : " + resource.toString());
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
    LOGGER.error("onClose was called, ", cause);
  }
}
