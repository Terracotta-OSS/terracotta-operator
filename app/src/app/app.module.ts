import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { AppComponent } from './app.component';
import { WorkerNodeComponent } from './worker-node/worker-node.component';
import { WorkerNodesComponent } from './worker-nodes/worker-nodes.component';
import { LicenseManagerComponent } from './license-manager/license-manager.component';
import { ClusterManagerComponent } from './cluster-manager/cluster-manager.component';

@NgModule({
  declarations: [
    AppComponent,
    WorkerNodeComponent,
    WorkerNodesComponent,
    LicenseManagerComponent,
    ClusterManagerComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
