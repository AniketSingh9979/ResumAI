import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Candidate } from '../../../shared/services/candidate.service';

@Component({
  selector: 'app-edit-candidate-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatSnackBarModule
  ],
  template: `
    <div class="edit-candidate-dialog">
      <h2 mat-dialog-title>Edit Candidate Details</h2>
      
      <mat-dialog-content>
        <form [formGroup]="editForm" class="edit-form">
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Name</mat-label>
            <input matInput formControlName="name" placeholder="Enter candidate name">
            <mat-error *ngIf="editForm.get('name')?.hasError('required')">
              Name is required
            </mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Email</mat-label>
            <input matInput formControlName="email" placeholder="Enter email address">
            <mat-error *ngIf="editForm.get('email')?.hasError('email')">
              Please enter a valid email address
            </mat-error>
          </mat-form-field>

          <div *ngIf="candidate.extractedInfo" class="extracted-info">
            <h3>Extracted Information</h3>
            
            <!-- Skills -->
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Skills</mat-label>
              <input matInput formControlName="skills" placeholder="Enter skills (comma-separated)">
            </mat-form-field>

            <!-- Experience -->
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Experience</mat-label>
              <textarea matInput formControlName="experience" rows="3" 
                        placeholder="Enter work experience"></textarea>
            </mat-form-field>

            <!-- Education -->
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Education</mat-label>
              <textarea matInput formControlName="education" rows="3" 
                        placeholder="Enter education details"></textarea>
            </mat-form-field>
          </div>
        </form>
      </mat-dialog-content>

      <mat-dialog-actions align="end">
        <button mat-button (click)="onCancel()">Cancel</button>
        <button mat-flat-button color="primary" 
                (click)="onSave()"
                [disabled]="editForm.invalid || editForm.pristine">
          Save Changes
        </button>
      </mat-dialog-actions>
    </div>
  `,
  styles: [`
    .edit-candidate-dialog {
      padding: 20px;
      
      h2 {
        color: #024950;
        margin: 0 0 20px 0;
      }

      .edit-form {
        display: flex;
        flex-direction: column;
        gap: 16px;
      }

      .full-width {
        width: 100%;
      }

      .extracted-info {
        margin-top: 20px;
        
        h3 {
          color: #0FA4AF;
          font-size: 1rem;
          margin: 0 0 16px 0;
        }
      }

      mat-dialog-actions {
        margin-top: 24px;
        padding: 0;

        button {
          &[color="primary"] {
            background: linear-gradient(135deg, #0FA4AF, #024950);
          }
        }
      }
    }
  `]
})
export class EditCandidateDialogComponent {
  editForm: FormGroup;
  candidate: Candidate;

  constructor(
    private dialogRef: MatDialogRef<EditCandidateDialogComponent>,
    @Inject(MAT_DIALOG_DATA) data: { candidate: Candidate },
    private fb: FormBuilder,
    private snackBar: MatSnackBar
  ) {
    this.candidate = data.candidate;
    this.editForm = this.fb.group({
      name: [this.candidate.name, Validators.required],
      email: [this.candidate.email, [Validators.email]],
      skills: [this.candidate.extractedInfo?.skills?.join(', ') || ''],
      experience: [this.candidate.extractedInfo?.experience?.join('\n') || ''],
      education: [this.candidate.extractedInfo?.education?.join('\n') || '']
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    if (this.editForm.valid && this.editForm.dirty) {
      const formValue = this.editForm.value;
      
      // Prepare updated candidate data
      const updatedCandidate: Candidate = {
        ...this.candidate,
        name: formValue.name,
        email: formValue.email,
        extractedInfo: {
          ...this.candidate.extractedInfo,
          skills: formValue.skills.split(',').map((s: string) => s.trim()).filter(Boolean),
          experience: formValue.experience.split('\n').map((e: string) => e.trim()).filter(Boolean),
          education: formValue.education.split('\n').map((e: string) => e.trim()).filter(Boolean)
        }
      };

      this.dialogRef.close(updatedCandidate);
    }
  }
}
