import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { WorkerNodesComponent } from './worker-nodes.component';

describe('WorkerNodesComponent', () => {
  let component: WorkerNodesComponent;
  let fixture: ComponentFixture<WorkerNodesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ WorkerNodesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(WorkerNodesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
