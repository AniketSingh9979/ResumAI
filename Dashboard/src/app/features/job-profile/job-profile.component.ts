import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { JobProfileService } from './job-profile.service';

export interface JobDescription {
  id: number;
  title: string;
  company: string;
  description: string;
  requirements: string;
  responsibilities: string;
  location: string;
  experienceLevel: string;
  panelistName: string;
  createdAt: string;
  updatedAt: string;
}

export interface JobDescriptionResponse {
  success: boolean;
  message: string;
  jobs: JobDescription[];
  currentPage: number;
  totalPages: number;
  totalElements: number;
  pageSize: number;
  hasNext: boolean;
  hasPrevious: boolean;
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
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './job-profile.component.html',
  styleUrls: ['./job-profile.component.scss']
})
export class JobProfileComponent implements OnInit {
  
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;
  
  // Upload form data
  selectedFile: File | null = null;
  uploadForm = {
    title: '',
    company: '',
    description: '',
    requirements: '',
    responsibilities: '',
    location: '',
    experienceLevel: '',
    panelistName: ''
  };

  // Grid data
  jobs: JobDescription[] = [];
  loading = false;
  
  // Pagination
  pageSize = 10;
  currentPage = 0;
  totalElements = 0;
  totalPages = 0;
  
  // Sorting
  sortBy = 'createdAt';
  sortDirection = 'desc';
  
  // Filtering
  filters = {
    title: '',
    company: '',
    location: '',
    experienceLevel: '',
    panelistName: ''
  };

  // Grid columns
  displayedColumns: string[] = ['title', 'company', 'location', 'experienceLevel', 'panelistName', 'createdAt', 'actions'];

  constructor(
    private jobProfileService: JobProfileService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.loadJobs();
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
    }
  }

  uploadJobDescription() {
    if (!this.uploadForm.panelistName.trim()) {
      this.snackBar.open('Panelist name is required', 'Close', { duration: 3000 });
      return;
    }

    this.loading = true;
    const formData = new FormData();
    
    if (this.selectedFile) {
      formData.append('file', this.selectedFile);
    }
    
    Object.keys(this.uploadForm).forEach(key => {
      const value = this.uploadForm[key as keyof typeof this.uploadForm];
      if (value) {
        formData.append(key, value);
      }
    });

    this.jobProfileService.uploadJobDescription(formData).subscribe({
      next: (response) => {
        if (response.success) {
          this.snackBar.open('Job description uploaded successfully!', 'Close', { duration: 3000 });
          this.resetForm();
          this.loadJobs(); // Refresh the grid
        } else {
          this.snackBar.open(response.message || 'Upload failed', 'Close', { duration: 3000 });
        }
        this.loading = false;
      },
      error: (error) => {
        console.error('Upload error:', error);
        this.snackBar.open('Upload failed. Please try again.', 'Close', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  resetForm() {
    this.selectedFile = null;
    this.uploadForm = {
      title: '',
      company: '',
      description: '',
      requirements: '',
      responsibilities: '',
      location: '',
      experienceLevel: '',
      panelistName: ''
    };
    // Reset file input
    if (this.fileInput) {
      this.fileInput.nativeElement.value = '';
    }
  }

  loadJobs() {
    this.loading = true;
    
    const params = {
      page: this.currentPage,
      size: this.pageSize,
      sortBy: this.sortBy,
      sortDir: this.sortDirection,
      ...this.filters
    };

    this.jobProfileService.getJobsPaginated(params).subscribe({
      next: (response) => {
        if (response.success) {
          this.jobs = response.jobs;
          this.totalElements = response.totalElements;
          this.totalPages = response.totalPages;
        } else {
          this.snackBar.open(response.message || 'Failed to load jobs', 'Close', { duration: 3000 });
        }
        this.loading = false;
      },
      error: (error) => {
        console.error('Load jobs error:', error);
        this.snackBar.open('Failed to load jobs. Please try again.', 'Close', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  onPageChange(event: PageEvent) {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadJobs();
  }

  onSortChange(sort: Sort) {
    this.sortBy = sort.active;
    this.sortDirection = sort.direction || 'asc';
    this.currentPage = 0; // Reset to first page when sorting
    this.loadJobs();
  }

  applyFilters() {
    this.currentPage = 0; // Reset to first page when filtering
    this.loadJobs();
  }

  clearFilters() {
    this.filters = {
      title: '',
      company: '',
      location: '',
      experienceLevel: '',
      panelistName: ''
    };
    this.applyFilters();
  }

  viewJobDetails(job: JobDescription) {
    // Implementation for viewing job details
    console.log('View job details:', job);
  }

  deleteJob(job: JobDescription) {
    // Implementation for deleting job
    console.log('Delete job:', job);
  }
} 