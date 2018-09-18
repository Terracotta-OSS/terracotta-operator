package org.terracotta.k8s.operator.app.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WorkerNode {

  List<String> labels =  new ArrayList<>();
  private int cpuNumber;
  private String availableMemory;
  private List<String> podsCurrentlyRunning =  new ArrayList<>();


  public List<String> getLabels() {
    return labels;
  }

  public void setLabels(List<String> labels) {
    this.labels = labels;
  }

  public int getCpuNumber() {
    return cpuNumber;
  }

  public void setCpuNumber(int cpuNumber) {
    this.cpuNumber = cpuNumber;
  }

  public String getAvailableMemory() {
    return availableMemory;
  }

  public void setAvailableMemory(String availableMemory) {
    this.availableMemory = availableMemory;
  }

  public List<String> getPodsCurrentlyRunning() {
    return podsCurrentlyRunning;
  }

  public void setPodsCurrentlyRunning(List<String> podsCurrentlyRunning) {
    this.podsCurrentlyRunning = podsCurrentlyRunning;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    WorkerNode that = (WorkerNode) o;
    return cpuNumber == that.cpuNumber &&
        Objects.equals(labels, that.labels) &&
        Objects.equals(availableMemory, that.availableMemory) &&
        Objects.equals(podsCurrentlyRunning, that.podsCurrentlyRunning);
  }

  @Override
  public int hashCode() {
    return Objects.hash(labels, cpuNumber, availableMemory, podsCurrentlyRunning);
  }
}
