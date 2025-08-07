import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { FormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { CandidateService, Candidate } from '../../../shared/services/candidate.service';

@Component({
  selector: 'app-candidate-details-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    FormsModule,
    MatInputModule,
    MatFormFieldModule
  ],
  template: `
    <h2 mat-dialog-title>Candidate Details</h2>
    <mat-dialog-content>
      <div class="candidate-details">
        <div class="status-section">
          <div class="status-badge" [class]="candidate.status">
            {{candidate.status | titlecase}}
          </div>
          <mat-progress-bar
            *ngIf="candidate.status === 'processing'"
            mode="indeterminate">
          </mat-progress-bar>
        </div>

        <div class="info-section">
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Name</mat-label>
            <input matInput [(ngModel)]="candidate.name" placeholder="Candidate Name">
          </mat-form-field>

          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Email</mat-label>
            <input matInput [(ngModel)]="candidate.email" placeholder="Candidate Email">
          </mat-form-field>

          <div class="file-info" *ngIf="candidate.resumeFile">
            <mat-icon>attach_file</mat-icon>
            <span>{{candidate.resumeFile.name}}</span>
          </div>

          <div class="extracted-info" *ngIf="candidate.extractedInfo">
            <h3>Extracted Information</h3>
            <pre>{{candidate.extractedInfo | json}}</pre>
          </div>
        </div>
      </div>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="dialogRef.close()">Close</button>
      <button mat-raised-button color="primary" 
              [disabled]="candidate.status === 'processing'"
              (click)="processResume()">
        <mat-icon>auto_awesome</mat-icon>
        Process Resume
      </button>
      <button mat-raised-button color="accent" 
              (click)="saveChanges()">
        Save Changes
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .candidate-details {
      min-width: 400px;
      padding: 16px;
    }
    .status-section {
      margin-bottom: 24px;
    }
    .status-badge {
      display: inline-block;
      padding: 4px 8px;
      border-radius: 4px;
      font-weight: 500;
      margin-bottom: 8px;
    }
    .status-badge.pending {
      background-color: #fff3e0;
      color: #f57c00;
    }
    .status-badge.processing {
      background-color: #e3f2fd;
      color: #1976d2;
    }
    .status-badge.reviewed {
      background-color: #e8f5e9;
      color: #388e3c;
    }
    .info-section {
      margin-top: 16px;
    }
    .full-width {
      width: 100%;
      margin-bottom: 16px;
    }
    .file-info {
      display: flex;
      align-items: center;
      gap: 8px;
      margin: 16px 0;
      padding: 8px;
      background-color: #f5f5f5;
      border-radius: 4px;
    }
    .extracted-info {
      margin-top: 24px;
      padding: 16px;
      background-color: #f5f5f5;
      border-radius: 4px;
    }
    .extracted-info pre {
      margin: 0;
      white-space: pre-wrap;
    }
  `]
})
export class CandidateDetailsDialogComponent implements OnInit {
  constructor(
    public dialogRef: MatDialogRef<CandidateDetailsDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public candidate: Candidate,
    private candidateService: CandidateService
  ) {}

  ngOnInit() {}

  processResume() {
    this.candidateService.processResume(this.candidate.id);
  }

  saveChanges() {
    this.candidateService.updateCandidate(this.candidate);
    this.dialogRef.close(this.candidate);
  }
}
