import { Component, Inject } from '@angular/core';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { CommonModule } from '@angular/common';
import { MatDividerModule } from '@angular/material/divider';
import { MatChipsModule } from '@angular/material/chips';

export interface JobDetailsData {
  job: any;
  onDownload?: (job: any) => void;
}

@Component({
  selector: 'app-job-details-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatDividerModule,
    MatChipsModule
  ],
  template: `
    <div class="job-details-dialog">
      <div mat-dialog-title class="dialog-header">
        <div class="title-section">
          <mat-icon class="title-icon">work</mat-icon>
          <div>
            <h2>{{data.job.title || 'Job Description'}}</h2>
            <p class="company-name">{{data.job.company || 'Unknown Company'}}</p>
          </div>
        </div>
        <button mat-icon-button mat-dialog-close class="close-button">
          <mat-icon>close</mat-icon>
        </button>
      </div>

      <div mat-dialog-content class="dialog-content">
        <!-- Job Overview -->
        <mat-card class="overview-card">
          <mat-card-header>
            <mat-card-title>
              <mat-icon>info</mat-icon>
              Overview
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="info-grid">
              <div class="info-item" *ngIf="data.job.location">
                <mat-icon>location_on</mat-icon>
                <span class="label">Location:</span>
                <span class="value">{{data.job.location}}</span>
              </div>
              <div class="info-item" *ngIf="data.job.experienceLevel">
                <mat-icon>work_history</mat-icon>
                <span class="label">Experience:</span>
                <span class="value">{{data.job.experienceLevel}}</span>
              </div>
              <div class="info-item" *ngIf="data.job.panelMemberName">
                <mat-icon>person</mat-icon>
                <span class="label">Posted by:</span>
                <span class="value">{{data.job.panelMemberName}}</span>
              </div>
              <div class="info-item" *ngIf="data.job.createdDate">
                <mat-icon>schedule</mat-icon>
                <span class="label">Created:</span>
                <span class="value">{{formatDate(data.job.createdDate)}}</span>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Job Description -->
        <mat-card class="content-card" *ngIf="data.job.description">
          <mat-card-header>
            <mat-card-title>
              <mat-icon>description</mat-icon>
              Job Description
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="content-text">{{data.job.description}}</div>
          </mat-card-content>
        </mat-card>

        <!-- Requirements -->
        <mat-card class="content-card" *ngIf="data.job.requirements">
          <mat-card-header>
            <mat-card-title>
              <mat-icon>checklist</mat-icon>
              Requirements
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="content-text">{{data.job.requirements}}</div>
          </mat-card-content>
        </mat-card>

        <!-- Responsibilities -->
        <mat-card class="content-card" *ngIf="data.job.responsibilities">
          <mat-card-header>
            <mat-card-title>
              <mat-icon>assignment</mat-icon>
              Responsibilities
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="content-text">{{data.job.responsibilities}}</div>
          </mat-card-content>
        </mat-card>

        <!-- File Information -->
        <mat-card class="content-card">
          <mat-card-header>
            <mat-card-title>
              <mat-icon>attach_file</mat-icon>
              File Information
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="file-info">
              <div class="info-row">
                <span class="label">File Name:</span>
                <span class="value">{{data.job.originalFileName || data.job.fileName}}</span>
              </div>
              <div class="info-row" *ngIf="data.job.fileSize">
                <span class="label">File Size:</span>
                <span class="value">{{formatFileSize(data.job.fileSize)}}</span>
              </div>
              <div class="info-row" *ngIf="data.job.contentType">
                <span class="label">File Type:</span>
                <span class="value">{{data.job.contentType}}</span>
              </div>
            </div>
          </mat-card-content>
        </mat-card>
      </div>

      <div mat-dialog-actions class="dialog-actions">
        <button mat-button mat-dialog-close>
          <mat-icon>close</mat-icon>
          Close
        </button>
        <button mat-raised-button color="primary" (click)="downloadFile()" *ngIf="data.onDownload">
          <mat-icon>download</mat-icon>
          Download JD
        </button>
      </div>
    </div>
  `,
  styles: [`
    .job-details-dialog {
      width: 100%;
      max-width: 800px;
    }

    .dialog-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      padding: 24px;
      border-bottom: 1px solid #e0e0e0;
      margin: -24px -24px 0 -24px;
    }

    .title-section {
      display: flex;
      align-items: center;
      gap: 16px;
    }

    .title-icon {
      font-size: 32px;
      width: 32px;
      height: 32px;
      color: #024950;
    }

    .title-section h2 {
      margin: 0;
      font-size: 1.5rem;
      font-weight: 600;
      color: #024950;
    }

    .company-name {
      margin: 4px 0 0 0;
      color: #666;
      font-size: 1rem;
    }

    .close-button {
      color: #666;
    }

    .dialog-content {
      padding: 24px 0;
      max-height: 70vh;
      overflow-y: auto;
    }

    .overview-card, .content-card {
      margin-bottom: 16px;
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }

    .overview-card mat-card-title,
    .content-card mat-card-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 1.1rem;
      color: #024950;
    }

    .overview-card mat-card-title mat-icon,
    .content-card mat-card-title mat-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
    }

    .info-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 16px;
      margin-top: 16px;
    }

    .info-item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px;
      background: #f8f9fa;
      border-radius: 8px;
    }

    .info-item mat-icon {
      color: #024950;
      font-size: 18px;
      width: 18px;
      height: 18px;
    }

    .info-item .label {
      font-weight: 500;
      color: #666;
      min-width: 70px;
    }

    .info-item .value {
      color: #333;
    }

    .content-text {
      margin-top: 16px;
      line-height: 1.6;
      white-space: pre-wrap;
      color: #333;
      background: #f8f9fa;
      padding: 16px;
      border-radius: 8px;
      border-left: 4px solid #024950;
    }

    .file-info {
      margin-top: 16px;
    }

    .info-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 0;
      border-bottom: 1px solid #eee;
    }

    .info-row:last-child {
      border-bottom: none;
    }

    .info-row .label {
      font-weight: 500;
      color: #666;
    }

    .info-row .value {
      color: #333;
      text-align: right;
    }

    .dialog-actions {
      display: flex;
      justify-content: flex-end;
      gap: 8px;
      padding-top: 16px;
      border-top: 1px solid #e0e0e0;
      margin-top: 16px;
    }

    .dialog-actions button {
      min-width: 120px;
    }

    @media (max-width: 600px) {
      .info-grid {
        grid-template-columns: 1fr;
      }
      
      .title-section {
        flex-direction: column;
        align-items: flex-start;
        gap: 8px;
      }
    }
  `]
})
export class JobDetailsDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<JobDetailsDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: JobDetailsData
  ) {}

  downloadFile(): void {
    if (this.data.onDownload) {
      this.data.onDownload(this.data.job);
    }
  }

  formatDate(dateString: string): string {
    if (!dateString) return 'N/A';
    try {
      return new Date(dateString).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      });
    } catch {
      return dateString;
    }
  }

  formatFileSize(bytes: number): string {
    if (!bytes || bytes === 0) return 'N/A';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }
} 