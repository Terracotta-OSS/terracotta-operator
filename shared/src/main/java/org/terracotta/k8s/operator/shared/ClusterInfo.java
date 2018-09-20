package org.terracotta.k8s.operator.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClusterInfo {

  private final List<WorkerNode> workerNodes =  new ArrayList<>();

  public void addWorkerNode(WorkerNode workerNode) {
    workerNodes.add(workerNode);
  }

  public List<WorkerNode> getWorkerNodes() {
    return workerNodes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ClusterInfo that = (ClusterInfo) o;
    return Objects.equals(workerNodes, that.workerNodes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(workerNodes);
  }
}
