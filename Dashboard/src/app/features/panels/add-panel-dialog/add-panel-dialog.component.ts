import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-add-panel-dialog',
  templateUrl: './add-panel-dialog.component.html',
  styleUrls: ['./add-panel-dialog.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule
  ]
})
export class AddPanelDialogComponent implements OnInit {
  panelForm: FormGroup;
  departments = ['Engineering', 'Design', 'Product', 'QA', 'DevOps', 'Management'];
  expertiseAreas = ['Frontend', 'Backend', 'Full Stack', 'UI/UX', 'DevOps', 'Cloud', 'Mobile', 'Data Science'];

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<AddPanelDialogComponent>
  ) {
    this.panelForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      employeeId: ['', Validators.required],
      designation: ['', Validators.required],
      department: ['', Validators.required],
      location: ['', Validators.required],
      expertise: ['', Validators.required],
      mobileNumber: ['', [Validators.required, Validators.pattern('^[0-9]{10}$')]],
      availabilityStatus: ['Available']
    });
  }

  ngOnInit(): void {}

  onSubmit(): void {
    if (this.panelForm.valid) {
      this.dialogRef.close(this.panelForm.value);
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  getErrorMessage(controlName: string): string {
    const control = this.panelForm.get(controlName);
    if (control?.hasError('required')) {
      return 'This field is required';
    }
    if (control?.hasError('email')) {
      return 'Please enter a valid email address';
    }
    if (control?.hasError('minlength')) {
      return 'Name must be at least 3 characters long';
    }
    if (control?.hasError('pattern')) {
      return 'Please enter a valid 10-digit mobile number';
    }
    return '';
  }
}
