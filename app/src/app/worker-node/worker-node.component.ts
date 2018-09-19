import { Component, OnInit } from '@angular/core';
import { WorkerNode } from '../model/workerNode';

@Component({
  selector: 'app-worker-node',
  templateUrl: './worker-node.component.html',
  styleUrls: ['./worker-node.component.css']
})
export class WorkerNodeComponent implements OnInit {

  workerNode: WorkerNode = {
    labels: [
      'beta.kubernetes.io/arch->amd64',
      'beta.kubernetes.io/os->linux',
      'kubernetes.io/hostname->k8s-001',
      'kubevirt.io/schedulable->true',
      'terracotta->server'
    ],
    cpuNumber: 4,
    availableMemory: '32781268Ki',
    podsCurrentlyRunning: [
      'terracotta-1-0',
      'terracotta-2-0'
    ]
  };

  constructor() { }

  ngOnInit() {
  }

}
