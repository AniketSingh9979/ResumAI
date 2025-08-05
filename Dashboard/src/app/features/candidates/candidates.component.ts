import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ResumeUploadDialogComponent } from './resume-upload-dialog/resume-upload-dialog.component';
import { CandidateDetailsDialogComponent } from './candidate-details-dialog/candidate-details-dialog.component';
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
    this.dialog.open(ResumeUploadDialogComponent, {
      width: '500px',
      disableClose: false
    });
  }

  openCandidateDetails(candidate: Candidate): void {
    this.dialog.open(CandidateDetailsDialogComponent, {
      width: '600px',
      data: candidate,
      disableClose: false
    });
  }
}
