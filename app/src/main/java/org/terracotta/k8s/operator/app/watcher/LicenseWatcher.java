package org.terracotta.k8s.operator.app.watcher;

import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terracotta.k8s.operator.app.crd.License;
import org.terracotta.k8s.operator.app.service.TheService;

public class LicenseWatcher implements Watcher<License> {

  private static final Logger LOGGER = LoggerFactory.getLogger(LicenseWatcher.class);

  private final TheService theService;

  public LicenseWatcher(TheService theService) {
    this.theService = theService;
  }

  @Override
  public void eventReceived(Action action, License resource) {
    if (Action.ADDED.equals(action)) {
      theService.storeLicenseConfigMap(resource.getSpec().get("license"));
      LOGGER.info("Successfully stored the license '{}'", resource.getSpec().get("license"));
    } else if (Action.DELETED.equals(action)) {
      boolean deleted = theService.deleteConfigMap("license");
      if (deleted) {
        LOGGER.info("Successfully deleted the license '{}'", resource.getSpec().get("license"));
      } else {
        LOGGER.error("Licensed is not found");
      }
    }
  }

  @Override
  public void onClose(KubernetesClientException cause) {
  }
}
