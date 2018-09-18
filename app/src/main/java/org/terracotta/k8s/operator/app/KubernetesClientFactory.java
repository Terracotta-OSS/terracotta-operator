package org.terracotta.cloud.terracottaoperator;


import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.stereotype.Service;

@Service
public class KubernetesClientFactory {

  public KubernetesClient retrieveKubernetesClient() {
    Config config = new ConfigBuilder().build();
    return new DefaultKubernetesClient(config);
  }

}
