import {SERVER_API_URL} from "../app.constants";
import { Component, OnInit } from '@angular/core';
import { WORKERNODES } from '../mock-workerNodes';
import {WorkerNode} from "../model/workerNode";
import { HttpClient, HttpHeaders } from "@angular/common/http";
import {ClusterInfo} from "../model/clusterInfo";

@Component({
  selector: 'app-worker-nodes',
  templateUrl: './worker-nodes.component.html',
  styleUrls: ['./worker-nodes.component.css']
})
export class WorkerNodesComponent implements OnInit {

  workerNodes = [];

  constructor(private http: HttpClient) {

  }

  ngOnInit() {}

  getWorkerNodes() {
    this.http.get<ClusterInfo>(SERVER_API_URL + '/api/info').subscribe(workerNodes => this.workerNodes = workerNodes.workerNodes);
  }

}
