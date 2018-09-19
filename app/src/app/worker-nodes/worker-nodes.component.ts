import { Component, OnInit } from '@angular/core';
import { WORKERNODES } from '../mock-workerNodes';
import {WorkerNode} from "../model/workerNode";
import { HttpClient, HttpHeaders } from "@angular/common/http";

@Component({
  selector: 'app-worker-nodes',
  templateUrl: './worker-nodes.component.html',
  styleUrls: ['./worker-nodes.component.css']
})
export class WorkerNodesComponent implements OnInit {

  workerNodes = WORKERNODES;

  constructor(private http: HttpClient) {

  }

  ngOnInit() {}

  getWorkerNodes() {
    this.http.get('http://localhost:8080/api/info').subscribe(workerNodes => this.workerNodes = workerNodes.workerNodes);
  }

}
