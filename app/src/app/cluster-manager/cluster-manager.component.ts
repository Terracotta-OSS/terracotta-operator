import {Component, ElementRef, OnInit, Renderer, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {HttpClient} from "@angular/common/http";
import {SERVER_API_URL} from "../app.constants";
import {Cluster} from "../model/cluster";

@Component({
  selector: 'app-cluster-manager',
  templateUrl: './cluster-manager.component.html',
  styleUrls: ['./cluster-manager.component.css']
})
export class ClusterManagerComponent implements OnInit {

  form: FormGroup;

  loading: boolean = false;

  clusterConfig: string;

  @ViewChild('offheaps') offheaps: ElementRef;

  @ViewChild('dataroots') dataroots: ElementRef;

  constructor(private fb: FormBuilder, private http: HttpClient) {
    this.createForm();
  }

  ngOnInit() {
  }


  createForm() {
    this.form = this.fb.group({
      clusterName: ['', Validators.required],
      stripes: [2],
      serversPerStripe: [2],
      clientReconnectWindow: [20]
    });
  }

  onSubmit() {
    this.loading = true;
    const clusterName = this.form.get('clusterName').value;
    this.http.post(SERVER_API_URL + `/api/cluster/${clusterName}`, {...this.form.value, ...this.assembleResourceMap()}).subscribe(resp => {
      this.loading = false;
      this.showCluster(clusterName);
    }, err => { this.loading = false; });
  }

  fetchCluster() {
    this.showCluster(this.form.get('clusterName').value);
  }

  showCluster(clusterName: string) {
    this.loading = true;
    clusterName = clusterName ? clusterName : this.form.get('clusterName').value;
    this.http.get(SERVER_API_URL + `/api/cluster/${clusterName}`).subscribe(resp => {
      this.loading = false;
      this.clusterConfig = JSON.stringify(resp);
    }, err => { this.loading = false; });
  }

  removeCluster() {
    this.loading = true;
    const clusterName = this.form.get('clusterName').value;
    this.http.delete(SERVER_API_URL + `/api/cluster/${clusterName}`).subscribe(resp => {
      this.loading = false;
    }, err => { this.loading = false; });
  }

  assembleResourceMap() {
    const result = { offheaps: {}, dataroots: {}};
    for (var i = 0; i < this.offheaps.nativeElement.childNodes.length; i++) {
      const li = this.offheaps.nativeElement.childNodes[i];
      let name;
      let value;
      for (var j = 0; j < li.childNodes.length; j++) {
        const child = li.childNodes[j];
        if (child.className === 'name') {
          name = child.value;
        } else {
          value = child.value;
        }
      }
      if (name && value) {
        result.offheaps[name] = value;
      }
    }

    for (var i = 0; i < this.dataroots.nativeElement.childNodes.length; i++) {
      const li = this.dataroots.nativeElement.childNodes[i];
      let name;
      let value;
      for (var j = 0; j < li.childNodes.length; j++) {
        const child = li.childNodes[j];
        if (child.className === 'name') {
          name = child.value;
        } else {
          value = child.value;
        }
      }
      if (name && value) {
        result.dataroots[name] = value;
      }
    }
    return result;
  }

  addOffheap() {
    var frag = document.createDocumentFragment();
    var li = document.createElement("li");
    li.innerHTML = `<input type="text" class="name" placeholder="offheap1">
      <input type="text" class="value" placeholder="256MB">`;
    frag.appendChild(li);
    this.offheaps.nativeElement.appendChild(frag);
  }

  addDataroot() {
    var frag = document.createDocumentFragment();
    var li = document.createElement("li");
    li.innerHTML = `<input type="text" class="name" placeholder="dataroot1">
      <input type="text" class="value" placeholder="local">`;
    frag.appendChild(li);
    this.dataroots.nativeElement.appendChild(frag);
  }

  removeOffheap() {
    this.offheaps.nativeElement.removeChild(this.offheaps.nativeElement.lastElementChild);
  }

  removeDataroot() {
    this.dataroots.nativeElement.removeChild(this.dataroots.nativeElement.lastElementChild);
  }
}
