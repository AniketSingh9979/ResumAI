import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
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
  designation?: string;
  department?: string;
  location?: string;
  mobileNumber?: string;
  employeeId?: string;
  availabilityStatus?: string;
}

// Interface for API response from backend
interface PanelMemberApiResponse {
  id: number;
  name: string;
  email: string;
  employeeId: string;
  designation: string;
  department: string;
  location: string;
  expertise: string;
  mobileNumber: string;
  availabilityStatus: string;
  availabilityStatusDisplay: string;
  createdDate: string;
  updatedDate: string;
  isActive: boolean;
}

// Interface for API response wrapper
interface ApiResponse<T> {
  success: boolean;
  message: string;
  data?: T;
  errors?: string[];
  timestamp: string;
}

export interface JobDescription {
  id: number;
  fileName: string;
  fileSize: number;
  uploadDate: string;
  title?: string;
  company?: string;
  location?: string;
  experienceLevel?: string;
  requirements?: string;
  responsibilities?: string;
  description?: string;
  panelMember?: PanelMember; // Make optional to handle cases where it might not be set
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
  panelMembers: PanelMember[] = []; // Initialize as empty array
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
  
  // Upload section visibility
  showUploadSection = false;

  constructor(
    private jobProfileService: JobProfileService,
    private panelService: PanelService,
    private snackBar: MatSnackBar,
    private http: HttpClient
  ) {}

  ngOnInit() {
    this.loadPanelMembers();
    this.loadJobs();
  }

  /**
   * TrackBy function for panel members to improve performance
   */
  trackByMemberId(index: number, member: PanelMember): number {
    return member?.id || index;
  }

  /**
   * Handle avatar loading errors by setting a fallback avatar
   */
  onAvatarError(event: any, name: string): void {
    const target = event.target as HTMLImageElement;
    const encodedName = encodeURIComponent(name || 'User');
    target.src = `https://ui-avatars.com/api/?name=${encodedName}&background=7367f0&color=fff&size=150&bold=true`;
  }

  loadPanelMembers() {
    this.loading = true;
    
    // API endpoint for panel members
    const apiUrl = 'http://localhost:8081/api/panel-members';
    
    this.http.get<ApiResponse<PanelMemberApiResponse[]>>(apiUrl).subscribe({
      next: (response: ApiResponse<PanelMemberApiResponse[]>) => {
        if (response.success && response.data && Array.isArray(response.data)) {
          this.panelMembers = response.data
            .filter(panel => panel && panel.name && panel.email) // Filter out invalid entries
            .map(panel => ({
              id: panel.id,
              name: panel.name,
              email: panel.email,
              expertise: panel.expertise || 'No expertise specified',
              avatar: this.generateAvatarUrl(panel.name),
              experience: this.calculateExperience(panel.createdDate),
              availability: panel.availabilityStatusDisplay || 'Unknown',
              rating: this.generateRandomRating(),
              interviewsDone: this.generateRandomInterviews(),
              designation: panel.designation || 'No designation',
              department: panel.department || 'No department',
              location: panel.location || 'No location',
              mobileNumber: panel.mobileNumber || 'No phone',
              employeeId: panel.employeeId || 'No ID',
              availabilityStatus: panel.availabilityStatus || 'UNKNOWN',
            }));
        } else {
          console.warn('Invalid API response or no data:', response);
          this.snackBar.open('Failed to load panel members.', 'Close', { duration: 3000 });
          this.loadFallbackPanelMembers();
        }
        this.loading = false;
      },
      error: (error: HttpErrorResponse) => {
        console.error('Error loading panel members:', error);
        this.snackBar.open('Unable to connect to server. Loading sample data.', 'Close', { duration: 3000 });
        this.loadFallbackPanelMembers();
        this.loading = false;
      }
    });
  }

  /**
   * Generate avatar URL using UI Avatars service
   */
  private generateAvatarUrl(name: string): string {
    return `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=random&color=fff&size=150&bold=true`;
  }

  /**
   * Calculate experience based on created date
   */
  private calculateExperience(createdDate: string): string {
    const created = new Date(createdDate);
    const now = new Date();
    const diffYears = now.getFullYear() - created.getFullYear();
    
    if (diffYears < 1) {
      return '< 1 year';
    } else if (diffYears === 1) {
      return '1 year';
    } else {
      return `${diffYears} years`;
    }
  }

  /**
   * Generate random rating for display
   */
  private generateRandomRating(): number {
    return Math.floor(Math.random() * 2) + 4; // Random between 4.0 and 5.0
  }

  /**
   * Generate random interview count
   */
  private generateRandomInterviews(): number {
    return Math.floor(Math.random() * 50) + 5; // Random between 5 and 55
  }

  /**
   * Fallback to panel service data if API fails
   */
  private loadFallbackPanelMembers() {
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
    
    // API endpoint for job descriptions from resume-parser-service
    const apiUrl = 'http://localhost:8081/api/matching/jobs';
    
    console.log('🔍 Loading jobs from API:', apiUrl);
    
    this.http.get<any>(apiUrl).subscribe({
      next: (response: any) => {
        console.log('📥 Raw API Response:', response);
        
        if (response.success && response.jobs && Array.isArray(response.jobs)) {
          console.log('✅ API Success - Jobs array length:', response.jobs.length);
          
          this.jobs = response.jobs.map((job: any) => ({
            id: job.id,
            fileName: job.originalFileName || job.fileName || 'Unknown File',
            fileSize: job.fileSize || 0,
            uploadDate: job.createdDate || new Date().toISOString(),
            title: job.title || 'No Title',
            company: job.company || 'No Company',
            location: job.location || 'No Location',
            experienceLevel: job.experienceLevel || 'Not Specified',
            requirements: job.requirements || 'No Requirements',
            responsibilities: job.responsibilities || 'No Responsibilities',
            description: job.description || 'No Description',
            panelMember: job.panelMember ? {
              id: job.panelMember.id,
              name: job.panelMember.name,
              email: job.panelMember.email,
              expertise: job.panelMember.expertise || 'No expertise',
              avatar: this.generateAvatarUrl(job.panelMember.name),
              designation: job.panelMember.designation || 'No designation',
              department: job.panelMember.department || 'No department'
            } : {
              id: 0,
              name: job.panelMemberName || 'Unknown Panel Member',
              email: job.panelMemberEmail || 'No email',
              expertise: 'No expertise specified',
              avatar: this.generateAvatarUrl(job.panelMemberName || 'Unknown'),
              designation: 'No designation',
              department: 'No department'
            }
          }));
          
          console.log('🎯 Mapped jobs:', this.jobs);
          
          this.filteredJobs = [...this.jobs];
          this.totalJobs = this.jobs.length;
          
          console.log('📊 Final state - filteredJobs:', this.filteredJobs.length, 'totalJobs:', this.totalJobs);
        } else {
          console.warn('⚠️ Invalid API response or no jobs found:', response);
          this.jobs = [];
          this.filteredJobs = [];
          this.totalJobs = 0;
        }
        this.loading = false;
      },
      error: (error: HttpErrorResponse) => {
        console.error('❌ Error loading jobs from API:', error);
        console.error('Error details:', {
          status: error.status,
          statusText: error.statusText,
          message: error.message,
          url: error.url
        });
        
        this.snackBar.open('Unable to load job descriptions from server.', 'Close', { duration: 3000 });
        
        // Fallback to empty array instead of mock data
        this.jobs = [];
        this.filteredJobs = [];
        this.totalJobs = 0;
        this.loading = false;
      }
    });
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
    
    // Create FormData for file upload
    const formData = new FormData();
    formData.append('file', this.selectedFile);
    formData.append('panelMemberId', this.selectedPanelMember.id.toString());
    formData.append('panelMemberName', this.selectedPanelMember.name);
    formData.append('panelMemberEmail', this.selectedPanelMember.email);
    
    // API endpoint for job description upload
    const uploadUrl = 'http://localhost:8081/api/matching/uploadJD';
    
    this.http.post<UploadResponse>(uploadUrl, formData).subscribe({
      next: (response: UploadResponse) => {
        if (response.success) {
          this.snackBar.open(response.message || 'Job description uploaded successfully!', 'Close', { duration: 3000 });
          this.resetForm();
          
          // Refresh jobs from API to get the latest data
          this.loadJobs();
        } else {
          this.snackBar.open(response.message || 'Upload failed. Please try again.', 'Close', { duration: 5000 });
        }
        this.loading = false;
      },
      error: (error: HttpErrorResponse) => {
        console.error('Upload error:', error);
        let errorMessage = 'Upload failed. Please try again.';
        
        if (error.status === 0) {
          errorMessage = 'Unable to connect to server. Please check your connection.';
        } else if (error.status === 413) {
          errorMessage = 'File is too large. Please select a smaller file.';
        } else if (error.status === 415) {
          errorMessage = 'File type not supported. Please upload PDF, DOC, DOCX, TXT, or RTF files.';
        } else if (error.error?.message) {
          errorMessage = error.error.message;
        }
        
        this.snackBar.open(errorMessage, 'Close', { duration: 5000 });
        this.loading = false;
      }
    });
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
        (job.panelMember?.name && job.panelMember.name.toLowerCase().includes(term)) ||
        (job.panelMember?.email && job.panelMember.email.toLowerCase().includes(term))
      );
    }
    this.currentPage = 0;
  }

  refreshJobs() {
    this.loadJobs();
  }
  
  /**
   * Toggle upload section visibility
   */
  toggleUploadSection() {
    this.showUploadSection = !this.showUploadSection;
    
    // Reset form when hiding the section
    if (!this.showUploadSection) {
      this.resetForm();
    }
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