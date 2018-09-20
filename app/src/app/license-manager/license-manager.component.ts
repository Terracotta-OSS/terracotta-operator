import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {HttpClient} from "@angular/common/http";
import {SERVER_API_URL} from "../app.constants";

@Component({
  selector: 'app-license-manager',
  templateUrl: './license-manager.component.html',
  styleUrls: ['./license-manager.component.css']
})
export class LicenseManagerComponent implements OnInit {

  form: FormGroup;

  loading: boolean = false;

  licenseFile: string;

  @ViewChild('fileInput') fileInput: ElementRef;

  constructor(private fb: FormBuilder, private http: HttpClient) {
    this.createForm();
  }

  ngOnInit() {
  }

  createForm() {
    this.form = this.fb.group({
      licenseFile: null
    });
  }

  onFileChange(event) {
    let reader = new FileReader();
    if(event.target.files && event.target.files.length > 0) {
      let file = event.target.files[0];
      reader.readAsDataURL(file);
      reader.onload = () => {
        this.form.get('licenseFile').setValue(reader.result.split(',')[1])
      };
    }
  }

  onSubmit() {
    this.loading = true;
    this.http.put(SERVER_API_URL + '/api/config/license', this.form.value.licenseFile, {responseType: 'text'}).subscribe(resp => {
      this.loading = false;
      this.showFile();
    });
  }

  clearFile() {
    this.form.get('licenseFile').setValue(null);
    this.fileInput.nativeElement.value = '';
  }

  showFile() {
    this.http.get(SERVER_API_URL + '/api/config/license', {responseType: 'text'}).subscribe(resp => { this.licenseFile = resp }, err => { this.licenseFile = '' });
  }

  deleteFile() {
    this.http.delete(SERVER_API_URL + '/api/config/license', {responseType: 'text'}).subscribe(resp => { this.licenseFile = '' });
  }
}
