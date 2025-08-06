import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-resume-upload-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './resume-upload-dialog.component.html',
  styleUrls: ['./resume-upload-dialog.component.scss']
})
export class ResumeUploadDialogComponent {
  isDragging = false;
  selectedFile: File | null = null;
  isUploading = false;
  
  constructor(
    public dialogRef: MatDialogRef<ResumeUploadDialogComponent>,
    private snackBar: MatSnackBar,
    private http: HttpClient
  ) {}

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
      this.handleFileSelection(files[0]);
    }
  }

  onFileSelected(event: Event): void {
    const element = event.target as HTMLInputElement;
    const files = element.files;
    if (files && files.length > 0) {
      this.handleFileSelection(files[0]);
    }
  }

  handleFileSelection(file: File): void {
    const allowedTypes = ['.pdf', '.doc', '.docx'];
    const fileExtension = file.name.toLowerCase().substring(file.name.lastIndexOf('.'));
    
    if (allowedTypes.includes(fileExtension)) {
      this.selectedFile = file;
    } else {
      this.snackBar.open('Please upload a PDF, DOC, or DOCX file', 'Close', {
        duration: 3000,
        horizontalPosition: 'center',
        verticalPosition: 'bottom'
      });
    }
  }

  removeFile(): void {
    this.selectedFile = null;
  }

  uploadFile(): void {
    if (!this.selectedFile || this.isUploading) {
      return;
    }

    this.isUploading = true;
    
    // Create FormData for file upload
    const formData = new FormData();
    formData.append('file', this.selectedFile);
    
    // API endpoint for resume upload
    const uploadUrl = 'http://localhost:8081/api/uploadResume';
    
    console.log('🚀 Uploading resume to:', uploadUrl);
    
    this.http.post<any>(uploadUrl, formData).subscribe({
      next: (response) => {
        console.log('✅ Upload successful:', response);
        
        this.snackBar.open('Resume uploaded and processed successfully!', 'Close', {
          duration: 3000,
          horizontalPosition: 'center',
          verticalPosition: 'bottom'
        });
        
        this.isUploading = false;
        this.dialogRef.close({
          success: true,
          file: this.selectedFile,
          response: response
        });
      },
      error: (error: HttpErrorResponse) => {
        console.error('❌ Upload failed:', error);
        
        let errorMessage = 'Upload failed. Please try again.';
        
        if (error.status === 0) {
          errorMessage = 'Unable to connect to server. Please check your connection.';
        } else if (error.status === 413) {
          errorMessage = 'File is too large. Please select a smaller file.';
        } else if (error.status === 415) {
          errorMessage = 'File type not supported. Please upload PDF, DOC, or DOCX files.';
        } else if (error.error?.message) {
          errorMessage = error.error.message;
        }
        
        this.snackBar.open(errorMessage, 'Close', {
          duration: 5000,
          horizontalPosition: 'center',
          verticalPosition: 'bottom'
        });
        
        this.isUploading = false;
      }
    });
  }
}
