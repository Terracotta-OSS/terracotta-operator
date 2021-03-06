import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { WorkerNodeComponent } from './worker-node.component';

describe('WorkerNodeComponent', () => {
  let component: WorkerNodeComponent;
  let fixture: ComponentFixture<WorkerNodeComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ WorkerNodeComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(WorkerNodeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
