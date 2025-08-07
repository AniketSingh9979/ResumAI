import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';

@Component({
  selector: 'app-edit-panel-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    FormsModule,
    ReactiveFormsModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule
  ],
  templateUrl: './edit-panel-dialog.component.html',
  styleUrls: ['./edit-panel-dialog.component.scss']
})
export class EditPanelDialogComponent {
  editForm: FormGroup;
  isSubmitting = false;

  constructor(
    private dialogRef: MatDialogRef<EditPanelDialogComponent>,
    private fb: FormBuilder,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.editForm = this.fb.group({
      name: [data.name, [Validators.required, Validators.minLength(2)]],
      email: [data.email, [Validators.required, Validators.email]],
      employeeId: [data.employeeId, [Validators.required, Validators.minLength(3)]],
      mobileNumber: [data.mobileNumber, [Validators.required, Validators.pattern('^[0-9]{10}$')]],
      designation: [data.designation, [Validators.required]],
      department: [data.department, [Validators.required]],
      location: [data.location, [Validators.required]],
      availabilityStatus: [data.availabilityStatus, [Validators.required]],
      expertise: [data.expertise, [Validators.required, Validators.maxLength(200)]]
    });
  }

  onSubmit() {
    if (this.editForm.valid) {
      this.isSubmitting = true;
      this.dialogRef.close(this.editForm.value);
    }
  }

  onCancel() {
    this.dialogRef.close();
  }
}
