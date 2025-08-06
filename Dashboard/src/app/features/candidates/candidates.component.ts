import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { CandidateDetailsDialogComponent } from './candidate-details-dialog/candidate-details-dialog.component';
import { EditCandidateDialogComponent } from './edit-candidate-dialog/edit-candidate-dialog.component';
import { CandidateService, Candidate } from '../../shared/services/candidate.service';

@Component({
  selector: 'app-candidates',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatSnackBarModule,
    MatTooltipModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './candidates.component.html',
  styleUrls: ['./candidates.component.scss']
})
export class CandidatesComponent implements OnInit {
  candidates: Candidate[] = [];
  isDragging = false;
  loadingSkills: { [key: string]: boolean } = {};
  selectedFile: File | null = null;
  isUploading = false;
  private apiUrl = 'http://localhost:8081/api';

  constructor(
    private dialog: MatDialog,
    private candidateService: CandidateService,
    private snackBar: MatSnackBar,
    private http: HttpClient
  ) {}

  ngOnInit() {
    this.candidateService.getCandidates().subscribe(candidates => {
      this.candidates = candidates;
    });
  }

  // Load skills for a specific candidate
  loadCandidateSkills(candidate: Candidate) {
    this.loadingSkills[candidate.id] = true;
    
    this.candidateService.getResumeDetails(candidate.id).subscribe({
      next: (response) => {
        this.loadingSkills[candidate.id] = false;
        
        if (response && response.success && response.resume) {
          const resume = response.resume;
          // Parse skills from comma-separated string
          if (resume.skills) {
            candidate.skills = resume.skills.split(',').map((skill: string) => skill.trim()).filter((skill: string) => skill);
          }
          
          // Update candidate information
          if (resume.experience) {
            candidate.extractedInfo = {
              ...candidate.extractedInfo,
              experience: [resume.experience]
            };
          }
          
          this.snackBar.open(`Loaded ${candidate.skills?.length || 0} skills for ${candidate.name}`, 'Close', {
            duration: 3000
          });
        } else {
          this.snackBar.open('Failed to load candidate details', 'Close', {
            duration: 3000
          });
        }
      },
      error: (error) => {
        this.loadingSkills[candidate.id] = false;
        console.error('Error loading candidate skills:', error);
        this.snackBar.open('Error loading candidate details', 'Close', {
          duration: 3000
        });
      }
    });
  }

  // Refresh data for a specific candidate
  refreshCandidateData(candidate: Candidate) {
    this.loadCandidateSkills(candidate);
  }



  // File selection handling
  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.handleFileSelection(file);
    }
  }

  // Drag and drop handling
  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.isDragging = true;
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    this.isDragging = false;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.isDragging = false;
    
    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.handleFileSelection(files[0]);
    }
  }

  // Handle file selection
  private handleFileSelection(file: File): void {
    // Validate file type
    const allowedTypes = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'];
    if (!allowedTypes.includes(file.type)) {
      this.snackBar.open('Please select a PDF, DOC, or DOCX file', 'Close', { duration: 3000 });
      return;
    }

    // Validate file size (10MB limit)
    if (file.size > 10 * 1024 * 1024) {
      this.snackBar.open('File size must be less than 10MB', 'Close', { duration: 3000 });
      return;
    }

    this.selectedFile = file;
    this.snackBar.open(`File selected: ${file.name}`, 'Close', { duration: 2000 });
  }

  // Remove selected file
  removeSelectedFile(): void {
    this.selectedFile = null;
  }

  // Cancel upload
  cancelUpload(): void {
    this.selectedFile = null;
    this.isUploading = false;
  }

  // Upload the selected file
  uploadSelectedFile(): void {
    if (!this.selectedFile) return;

    this.isUploading = true;
    const formData = new FormData();
    formData.append('file', this.selectedFile);

    this.http.post(`${this.apiUrl}/uploadResume`, formData).subscribe({
      next: (response: any) => {
        this.isUploading = false;
        this.selectedFile = null;
        
        if (response && response.success) {
          this.snackBar.open('Resume uploaded and processed successfully!', 'Close', { 
            duration: 3000,
            panelClass: ['success-snackbar']
          });
          
          // Auto-refresh data from API to get the latest uploaded resume
          setTimeout(() => {
            this.candidateService.refreshCandidates();
          }, 1000);
        } else {
          this.snackBar.open('Upload failed: ' + (response.message || 'Unknown error'), 'Close', { duration: 3000 });
        }
      },
      error: (error: HttpErrorResponse) => {
        this.isUploading = false;
        console.error('Upload error:', error);
        this.snackBar.open('Upload failed: ' + (error.error?.message || error.message), 'Close', { duration: 3000 });
      }
    });
  }

  // Helper method to format file size
  getFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
  }

  openCandidateDetails(candidate: Candidate): void {
    this.dialog.open(CandidateDetailsDialogComponent, {
      width: '600px',
      data: candidate,
      disableClose: false
    });
  }

  openEditCandidateDialog(candidate: Candidate): void {
    this.dialog.open(EditCandidateDialogComponent, {
      width: '600px',
      data: candidate,
      disableClose: false
    });
  }

  editCandidate(candidate: Candidate): void {
    const dialogRef = this.dialog.open(EditCandidateDialogComponent, {
      width: '500px',
      data: { ...candidate },  // Pass a copy to prevent direct mutation
      disableClose: false
    });

    dialogRef.afterClosed().subscribe((result: Candidate | undefined) => {
      if (result) {
        // Update the candidate in the service
        this.candidateService.updateCandidate(result).subscribe({
          next: (updatedCandidates: Candidate[]) => {
            // Update the local candidates array with the new data
            this.candidates = updatedCandidates;
            this.snackBar.open('Candidate updated successfully', 'Close', {
              duration: 3000
            });
          },
          error: (error: unknown) => {
            console.error('Error updating candidate:', error);
            this.snackBar.open('Error updating candidate', 'Close', {
              duration: 3000
            });
          }
        });
      }
    });
  }
}
