import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ResumeUploadDialogComponent } from './resume-upload-dialog/resume-upload-dialog.component';
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
    MatSnackBarModule
  ],
  templateUrl: './candidates.component.html',
  styleUrls: ['./candidates.component.scss']
})
export class CandidatesComponent implements OnInit {
  candidates: Candidate[] = [];
  isDragging = false;

  constructor(
    private dialog: MatDialog,
    private candidateService: CandidateService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.candidateService.getCandidates().subscribe(candidates => {
      this.candidates = candidates;
    });
  }

  openUploadDialog(): void {
    const dialogRef = this.dialog.open(ResumeUploadDialogComponent, {
      width: '400px',
      disableClose: false,
      panelClass: 'compact-dialog',
      autoFocus: false
    });

    dialogRef.afterClosed().subscribe((file: File | undefined) => {
      if (file) {
        // Simulate file upload and candidate creation
        this.snackBar.open('Processing resume...', '', { duration: 2000 });
        
        setTimeout(() => {
          const newCandidate: Candidate = {
            id: Math.random().toString(36).substr(2, 9),
            name: file.name.split('.')[0].replace(/-|_/g, ' '),
            status: 'pending',
            uploadDate: new Date(),
            resumeFile: file
          };
          
          this.candidateService.addCandidate(newCandidate).subscribe({
            next: (candidates: Candidate[]) => {
              this.candidates = candidates;
              this.snackBar.open('Resume processed successfully!', 'Close', { duration: 3000 });
              
              // Start processing the resume after adding
              this.candidateService.processResume(newCandidate.id);
            },
            error: (err: Error) => {
              console.error('Error adding candidate:', err);
              this.snackBar.open('Error processing resume', 'Close', { duration: 3000 });
            }
          });
        }, 2000);
      }
    });
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
