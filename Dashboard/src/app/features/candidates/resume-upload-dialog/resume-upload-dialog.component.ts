import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-resume-upload-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule
  ],
  template: `
    <div class="upload-dialog">
      <h2 mat-dialog-title>Upload Resume</h2>
      
      <mat-dialog-content>
        <div class="upload-area" 
             (dragover)="onDragOver($event)"
             (dragleave)="onDragLeave($event)"
             (drop)="onDrop($event)"
             [class.dragging]="isDragging">
          <input type="file" 
                 #fileInput 
                 (change)="onFileSelected($event)"
                 accept=".pdf,.doc,.docx"
                 style="display: none">
          
          <mat-icon>cloud_upload</mat-icon>
          <p>Drag and drop your resume here</p>
          <p>or</p>
          <button mat-raised-button color="primary" (click)="fileInput.click()">
            Browse Files
          </button>
          <p class="supported-formats">Supported formats: PDF, DOC, DOCX</p>
        </div>
      </mat-dialog-content>
      
      <mat-dialog-actions align="end">
        <button mat-button (click)="dialogRef.close()">Cancel</button>
      </mat-dialog-actions>
    </div>
  `,
  styles: [`
    .upload-dialog {
      padding: 16px;
    }

    .upload-area {
      border: 2px dashed var(--surface-3);
      border-radius: var(--border-radius);
      padding: 32px;
      text-align: center;
      background: var(--surface-2);
      transition: all 0.3s ease;
      
      &.dragging {
        border-color: var(--primary-gradient-start);
        background: var(--surface-1);
      }

      mat-icon {
        font-size: 48px;
        width: 48px;
        height: 48px;
        margin-bottom: 16px;
        color: var(--primary-gradient-start);
      }

      p {
        margin: 8px 0;
        color: var(--text-secondary);

        &.supported-formats {
          font-size: 0.875rem;
          margin-top: 16px;
          color: var(--text-tertiary);
        }
      }

      button {
        margin: 16px 0;
      }
    }
  `]
})
export class ResumeUploadDialogComponent {
  isDragging = false;

  constructor(public dialogRef: MatDialogRef<ResumeUploadDialogComponent>) {}

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = true;
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;

    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.handleFile(files[0]);
    }
  }

  onFileSelected(event: Event): void {
    const element = event.target as HTMLInputElement;
    const files = element.files;
    if (files && files.length > 0) {
      this.handleFile(files[0]);
    }
  }

  private handleFile(file: File): void {
    // Validate file type
    const validTypes = ['.pdf', '.doc', '.docx'];
    const fileExtension = file.name.substring(file.name.lastIndexOf('.')).toLowerCase();
    
    if (!validTypes.includes(fileExtension)) {
      alert('Please upload a valid resume file (PDF, DOC, or DOCX)');
      return;
    }

    // TODO: Implement file upload logic here
    console.log('File to upload:', file);
    this.dialogRef.close(file);
  }
}
