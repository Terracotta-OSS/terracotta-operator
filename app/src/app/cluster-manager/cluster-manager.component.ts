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
    Array.from(this.offheaps.nativeElement.childNodes).forEach(li => {
      let name;
      let value;
      // @ts-ignore
      Array.from(li.children).forEach(child => {
        // @ts-ignore
        if (child.className === 'name') {
          // @ts-ignore
          name = child.value;
        } else {
          // @ts-ignore
          value = child.value;
        }
      })
      result.offheaps[name] = value;
    });
    Array.from(this.dataroots.nativeElement.children).forEach(li => {
      let name;
      let value;
      // @ts-ignore
      Array.from(li.children).forEach(child => {
        // @ts-ignore
        if (child.className === 'name') {
          // @ts-ignore
          name = child.value;
        } else {
          // @ts-ignore
          value = child.value;
        }
      })
      if (name && value) {
        result.dataroots[name] = value;
      }
    });
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

  removeResource() {

  }
}
