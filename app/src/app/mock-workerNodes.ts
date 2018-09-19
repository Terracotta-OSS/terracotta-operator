import { WorkerNode } from './model/workerNode';

export const WORKERNODES: WorkerNode[] = [
  {
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
  },
  {
    labels: [
      'beta.kubernetes.io/arch->amd64',
      'beta.kubernetes.io/os->linux',
      'kubernetes.io/hostname->k8s-002',
      'kubevirt.io/schedulable->true',
      'terracotta->server'
    ],
    cpuNumber: 4,
    availableMemory: '32781268Ki',
    podsCurrentlyRunning: [
      'terracotta-1-1',
      'terracotta-2-1'
    ]
  }
];
