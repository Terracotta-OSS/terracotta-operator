import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';

import { AppComponent } from './app.component';
import { WorkerNodeComponent } from './worker-node/worker-node.component';
import { WorkerNodesComponent } from './worker-nodes/worker-nodes.component';

@NgModule({
  declarations: [
    AppComponent,
    WorkerNodeComponent,
    WorkerNodesComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
