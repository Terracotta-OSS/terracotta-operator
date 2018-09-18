package org.terracotta.k8s.operator.app;

import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.terracotta.k8s.operator.app.model.KubernetesClusterInfo;
import org.terracotta.k8s.operator.app.model.WorkerNode;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class KubernetesClusterInfoController {

  private static final Logger log = LoggerFactory.getLogger(KubernetesClusterInfoController.class);

  @GetMapping("/info")
  @ResponseBody
  public KubernetesClusterInfo retrieveClusterInfo () {

    KubernetesClusterInfo kubernetesClusterInfo = new KubernetesClusterInfo();


    Config config = new ConfigBuilder().build();
    try (KubernetesClient client = new DefaultKubernetesClient(config)) {
      List<Node> items = client.nodes().list().getItems();
      items.forEach(node -> {
        WorkerNode workerNode = new WorkerNode();
        workerNode.setCpuNumber(Integer.valueOf(node.getStatus().getCapacity().get("cpu").getAmount()));
        workerNode.setAvailableMemory(node.getStatus().getCapacity().get("memory").getAmount());
        workerNode.setLabels(node.getMetadata().getLabels().entrySet().stream().map(stringStringEntry -> stringStringEntry.getKey() + "->" + stringStringEntry.getValue()).collect(Collectors.toList()));
        workerNode.setPodsCurrentlyRunning(client.pods().inNamespace("default").list().getItems().stream().filter(pod -> pod.getSpec().getNodeName().equals(node.getMetadata().getName())).map(pod -> pod.getMetadata().getName())  .collect(Collectors.toList()));
        kubernetesClusterInfo.addWorkerNode(workerNode);
      });
    }
    return kubernetesClusterInfo;
  }

}
