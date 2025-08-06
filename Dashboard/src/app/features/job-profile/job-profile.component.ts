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

interface Panel {
  id: number;
  name: string;
  expertise: string;
  experience: string;
  availability: string;
  rating: number;
  interviewsDone: number;
  image: string;
}

interface PanelMember {
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

interface JobDescription {
  id: number;
  title: string;
  department: string;
  description: string;
  fileName: string;
  fileSize: number;
  uploadDate: string;
  company?: string;
  location?: string;
  experienceLevel?: string;
  requirements?: string;
  responsibilities?: string;
  panelMember?: PanelMember; // Make optional to handle cases where it might not be set
}

interface UploadResponse {
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
  selectedFile: File | null = null;
  isDragOver = false;
  
  jobs: JobDescription[] = [];
  filteredJobs: JobDescription[] = [];
  loading = false;
  
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

  ngOnInit(): void {
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
      email: panel.name.toLowerCase().replace(' ', '.') + '@company.com', // Simulated email
      expertise: panel.expertise,
      avatar: panel.image,
      experience: panel.experience,
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

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.handleFile(input.files[0]);
    }
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.isDragOver = false;
    
    if (event.dataTransfer?.files.length) {
      this.handleFile(event.dataTransfer.files[0]);
    }
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.isDragOver = true;
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    this.isDragOver = false;
  }

  handleFile(file: File): void {
    const validTypes = ['.pdf', '.doc', '.docx', '.txt'];
    const fileExt = file.name.substring(file.name.lastIndexOf('.')).toLowerCase();
    
    if (!validTypes.includes(fileExt)) {
      this.snackBar.open('Please upload a valid document (PDF, DOC, DOCX, TXT)', 'Close', {
        duration: 3000
      });
      return;
    }

    this.selectedFile = file;
  }

  removeFile(event: Event): void {
    event.stopPropagation();
    this.selectedFile = null;
    if (this.fileInput?.nativeElement) {
      this.fileInput.nativeElement.value = '';
    }
  }

  // Function removed to resolve duplicate - using the API version instead

  uploadProfile(): void {
    if (!this.selectedFile || !this.selectedPanelMember) {
      this.snackBar.open('Please select a file and panel member', 'Close', {
        duration: 3000
      });
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

  resetForm(): void {
    this.selectedFile = null;
    this.selectedPanelMember = null;
    if (this.fileInput?.nativeElement) {
      this.fileInput.nativeElement.value = '';
    }
  }

  onSearch(term: string): void {
    this.searchTerm = term;
    this.filterJobs();
  }

  filterJobs(): void {
    if (!this.searchTerm) {
      this.filteredJobs = [...this.jobs];
    } else {
      const searchLower = this.searchTerm.toLowerCase();
      this.filteredJobs = this.jobs.filter(job => 
        (job.fileName && job.fileName.toLowerCase().includes(searchLower)) ||
        (job.panelMember?.name && job.panelMember.name.toLowerCase().includes(searchLower)) ||
        (job.panelMember?.email && job.panelMember.email.toLowerCase().includes(searchLower)) ||
        (job.title && job.title.toLowerCase().includes(searchLower)) ||
        (job.description && job.description.toLowerCase().includes(searchLower)) ||
        (job.department && job.department.toLowerCase().includes(searchLower))
      );
    }
    this.totalJobs = this.filteredJobs.length;
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

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
  }

  /**
   * Convert timestamp to relative time (e.g., "2 hours ago")
   */
  getTimeAgo(timestamp: string): string {
    const date = new Date(timestamp);
    const now = new Date();
    const seconds = Math.floor((now.getTime() - date.getTime()) / 1000);
    
    const intervals = {
      year: 31536000,
      month: 2592000,
      week: 604800,
      day: 86400,
      hour: 3600,
      minute: 60
    };

    for (const [unit, secondsInUnit] of Object.entries(intervals)) {
      const interval = Math.floor(seconds / secondsInUnit);
      if (interval >= 1) {
        return interval === 1 ? `1 ${unit} ago` : `${interval} ${unit}s ago`;
      }
    }
    
    return 'Just now';
  }

  /**
   * View detailed information about a job
   */
  viewJobDetails(job: JobDescription): void {
    // TODO: Implement job details dialog
    console.log('Viewing job details:', job);
    this.snackBar.open('Job details view coming soon', 'Close', {
      duration: 3000
    });
  }

  /**
   * Download the job description file
   */
  downloadFile(job: JobDescription): void {
    if (!job.fileName) {
      this.snackBar.open('No file available for download', 'Close', {
        duration: 3000
      });
      return;
    }

    // API endpoint for file download
    const downloadUrl = `http://localhost:8081/api/matching/download/${job.id}`;
    
    // Create a link element and trigger download
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.download = job.fileName;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    this.snackBar.open('Downloading file...', 'Close', {
      duration: 2000
    });
  }

  /**
   * Delete a job description
   */
  deleteJob(job: JobDescription): void {
    if (confirm(`Are you sure you want to delete "${job.title || job.fileName}"?`)) {
      // API endpoint for job deletion
      const deleteUrl = `http://localhost:8081/api/matching/jobs/${job.id}`;
      
      this.http.delete(deleteUrl).subscribe({
        next: () => {
          this.jobs = this.jobs.filter(j => j.id !== job.id);
          this.filteredJobs = this.filteredJobs.filter(j => j.id !== job.id);
          this.totalJobs = this.jobs.length;
          
          this.snackBar.open('Job description deleted successfully', 'Close', {
            duration: 3000
          });
        },
        error: (error: HttpErrorResponse) => {
          console.error('Error deleting job:', error);
          this.snackBar.open('Error deleting job description', 'Close', {
            duration: 3000
          });
        }
      });
    }
  }

  /**
   * Extract years of experience from experience level text
   */
  getExperienceYears(experienceLevel: string | undefined): string {
    if (!experienceLevel) {
      return 'Any level';
    }
    
    // Look for patterns like "5-8 years", "3+ years", "2 years", etc.
    const yearPattern = /(\d+[-+]?\d*)\s*years?/i;
    const match = experienceLevel.match(yearPattern);
    
    if (match) {
      return match[1] + ' years';
    }
    
    // Look for patterns in parentheses like "(5-8 years)"
    const parenthesesPattern = /\(([^)]*years?[^)]*)\)/i;
    const parenthesesMatch = experienceLevel.match(parenthesesPattern);
    
    if (parenthesesMatch) {
      return parenthesesMatch[1];
    }
    
    // If no specific years found, return the original or a default
    return experienceLevel.includes('Senior') ? '5+ years' :
           experienceLevel.includes('Junior') ? '0-2 years' :
           experienceLevel.includes('Mid') ? '3-5 years' :
           'Any level';
  }

  /**
   * Extract main skill from job description/requirements
   */
  getMainSkill(job: any): string {
    if (!job) {
      return 'General';
    }

    // Check title first for main technology/skill
    const title = job.title || job.fileName || '';
    const titleSkills = this.extractSkillsFromText(title);
    if (titleSkills.length > 0) {
      return titleSkills[0];
    }

    // Check requirements/responsibilities
    const requirements = job.requirements || job.responsibilities || job.description || '';
    const skillsFromReq = this.extractSkillsFromText(requirements);
    if (skillsFromReq.length > 0) {
      return skillsFromReq[0];
    }

    // Fallback based on department or default
    return job.department || 'General';
  }

  /**
   * Extract skills from text using common technology keywords
   */
  private extractSkillsFromText(text: string): string[] {
    if (!text) return [];

    const commonSkills = [
      'Java', 'Python', 'JavaScript', 'TypeScript', 'React', 'Angular', 'Vue',
      'Node.js', 'Spring Boot', 'Django', 'Flask', 'Express',
      'AWS', 'Azure', 'GCP', 'Docker', 'Kubernetes',
      'MySQL', 'PostgreSQL', 'MongoDB', 'Redis',
      'HTML', 'CSS', 'SCSS', 'Bootstrap', 'Tailwind',
      'Git', 'Jenkins', 'CI/CD', 'DevOps',
      'REST API', 'GraphQL', 'Microservices',
      'Machine Learning', 'AI', 'Data Science',
      'C++', 'C#', '.NET', 'PHP', 'Ruby', 'Go', 'Rust',
      'Spring', 'Hibernate', 'JPA', 'Maven', 'Gradle'
    ];

    const foundSkills: string[] = [];
    const lowerText = text.toLowerCase();

    for (const skill of commonSkills) {
      if (lowerText.includes(skill.toLowerCase())) {
        foundSkills.push(skill);
      }
    }

    return foundSkills;
  }
}