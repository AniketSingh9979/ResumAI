import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

@Component({
  selector: 'app-resume-upload-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule
  ],
  templateUrl: './resume-upload-dialog.component.html',
  styleUrls: ['./resume-upload-dialog.component.scss']
})
export class ResumeUploadDialogComponent {
  isDragging = false;
  selectedFile: File | null = null;
  
  constructor(
    public dialogRef: MatDialogRef<ResumeUploadDialogComponent>,
    private snackBar: MatSnackBar
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
    if (this.selectedFile) {
      // Here you would typically upload the file to your backend
      // For now, we'll just simulate the upload
      setTimeout(() => {
        this.snackBar.open('Resume uploaded successfully!', 'Close', {
          duration: 3000,
          horizontalPosition: 'center',
          verticalPosition: 'bottom'
        });
        this.dialogRef.close(this.selectedFile);
      }, 1000);
    }
  }
}
