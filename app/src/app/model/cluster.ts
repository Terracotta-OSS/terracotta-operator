

export class Cluster {
  clusterName: string;

  offheaps: [string, string][];

  dataroots: [string, string][];

  stripes: number;

  serversPerStripe: number;

  clientReconnectWindow: number;
}
