import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { JobProfileService } from './job-profile.service';
import { PanelService } from '../../shared/services/panel.service';

export interface PanelMember {
  id: number;
  name: string;
  email: string;
  expertise: string;
  avatar: string;
  experience?: string;
  availability?: string;
  rating?: number;
  interviewsDone?: number;
}

export interface JobDescription {
  id: number;
  fileName: string;
  fileSize: number;
  uploadDate: string;
  panelMember: PanelMember;
}

export interface UploadResponse {
  success: boolean;
  message: string;
  jobId?: number;
  job?: JobDescription;
}

@Component({
  selector: 'app-job-profile',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './job-profile.component.html',
  styleUrls: ['./job-profile.component.scss']
})
export class JobProfileComponent implements OnInit {
  
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;
  
  // Panel members data
  panelMembers: PanelMember[] = [];
  selectedPanelMember: PanelMember | null = null;
  
  // File upload data
  selectedFile: File | null = null;
  isDragOver = false;
  
  // Jobs data
  jobs: JobDescription[] = [];
  filteredJobs: JobDescription[] = [];
  loading = false;
  
  // Search and pagination
  searchTerm = '';
  pageSize = 12;
  currentPage = 0;
  totalJobs = 0;

  constructor(
    private jobProfileService: JobProfileService,
    private panelService: PanelService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.loadPanelMembers();
    this.loadJobs();
  }

  loadPanelMembers() {
    // Convert panel service data to our interface
    const panels = this.panelService.getPanels();
    this.panelMembers = panels.map(panel => ({
      id: panel.id,
      name: panel.name,
      email: `${panel.name.toLowerCase().replace(/\s+/g, '.')}@company.com`, // Generate email
      expertise: panel.expertise,
      avatar: panel.image,
      experience: panel.experience,
      availability: panel.availability,
      rating: panel.rating,
      interviewsDone: panel.interviewsDone
    }));
  }

  loadJobs() {
    this.loading = true;
    // Mock data for demonstration
    setTimeout(() => {
      this.jobs = [
        {
          id: 1,
          fileName: 'Senior_Developer_JD.pdf',
          fileSize: 245760,
          uploadDate: '2024-01-15T10:30:00Z',
          panelMember: this.panelMembers[0]
        },
        {
          id: 2,
          fileName: 'UI_UX_Designer_Requirements.docx',
          fileSize: 189440,
          uploadDate: '2024-01-14T14:20:00Z',
          panelMember: this.panelMembers[1]
        },
        {
          id: 3,
          fileName: 'Backend_Engineer_Specifications.pdf',
          fileSize: 312320,
          uploadDate: '2024-01-13T09:15:00Z',
          panelMember: this.panelMembers[2]
        }
      ];
      this.filteredJobs = [...this.jobs];
      this.totalJobs = this.jobs.length;
      this.loading = false;
    }, 1000);
  }

  onPanelMemberChange(event: any) {
    this.selectedPanelMember = event.value;
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
    }
  }

  onDragOver(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver = true;
  }

  onDragLeave(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver = false;
  }

  onFileDrop(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver = false;
    
    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.selectedFile = files[0];
    }
  }

  removeFile(event: Event) {
    event.stopPropagation();
    this.selectedFile = null;
    if (this.fileInput) {
      this.fileInput.nativeElement.value = '';
    }
  }

  getFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  getTimeAgo(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diffInSeconds = Math.floor((now.getTime() - date.getTime()) / 1000);
    
    if (diffInSeconds < 60) return 'Just now';
    if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)} mins ago`;
    if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)} hours ago`;
    return `${Math.floor(diffInSeconds / 86400)} days ago`;
  }

  uploadJobDescription() {
    if (!this.selectedFile || !this.selectedPanelMember) {
      this.snackBar.open('Please select both a file and panel member', 'Close', { duration: 3000 });
      return;
    }

    this.loading = true;
    
    // Simulate upload
    setTimeout(() => {
      const newJob: JobDescription = {
        id: this.jobs.length + 1,
        fileName: this.selectedFile!.name,
        fileSize: this.selectedFile!.size,
        uploadDate: new Date().toISOString(),
        panelMember: this.selectedPanelMember!
      };
      
      this.jobs.unshift(newJob);
      this.applySearch();
      this.totalJobs = this.jobs.length;
      
      this.snackBar.open('Job description uploaded successfully!', 'Close', { duration: 3000 });
      this.resetForm();
      this.loading = false;
    }, 2000);
  }

  resetForm() {
    this.selectedFile = null;
    this.selectedPanelMember = null;
    this.isDragOver = false;
    if (this.fileInput) {
      this.fileInput.nativeElement.value = '';
    }
  }

  applySearch() {
    if (!this.searchTerm.trim()) {
      this.filteredJobs = [...this.jobs];
    } else {
      const term = this.searchTerm.toLowerCase();
      this.filteredJobs = this.jobs.filter(job => 
        job.fileName.toLowerCase().includes(term) ||
        job.panelMember.name.toLowerCase().includes(term) ||
        job.panelMember.email.toLowerCase().includes(term)
      );
    }
    this.currentPage = 0;
  }

  refreshJobs() {
    this.loadJobs();
  }

  scrollToUpload() {
    document.querySelector('.upload-section')?.scrollIntoView({ behavior: 'smooth' });
  }

  onPageChange(event: PageEvent) {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
  }

  viewJobDetails(job: JobDescription) {
    this.snackBar.open(`Viewing details for ${job.fileName}`, 'Close', { duration: 2000 });
    // Implement view logic
  }

  downloadFile(job: JobDescription) {
    this.snackBar.open(`Downloading ${job.fileName}`, 'Close', { duration: 2000 });
    // Implement download logic
  }

  deleteJob(job: JobDescription) {
    if (confirm(`Are you sure you want to delete "${job.fileName}"?`)) {
      this.jobs = this.jobs.filter(j => j.id !== job.id);
      this.applySearch();
      this.totalJobs = this.jobs.length;
      this.snackBar.open('Job description deleted successfully!', 'Close', { duration: 3000 });
    }
  }
} 