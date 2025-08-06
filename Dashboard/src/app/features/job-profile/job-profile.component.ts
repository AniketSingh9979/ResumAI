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
}

interface JobDescription {
  id: number;
  title: string;
  department: string;
  description: string;
  fileName: string;
  fileSize: number;
  uploadDate: string;
  panelMember: PanelMember;
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
  
  panelMembers: PanelMember[] = [];
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

  constructor(
    private jobProfileService: JobProfileService,
    private panelService: PanelService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadPanelMembers();
    this.loadJobs();
  }

  loadPanelMembers(): void {
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

  onPanelMemberChange(event: { value: PanelMember }): void {
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
    const validTypes = ['.pdf', '.doc', '.docx'];
    const fileExt = file.name.substring(file.name.lastIndexOf('.')).toLowerCase();
    
    if (!validTypes.includes(fileExt)) {
      this.snackBar.open('Please upload a valid document (PDF, DOC, DOCX)', 'Close', {
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

  loadJobs(): void {
    this.loading = true;
    const defaultPanelMember: PanelMember = {
      id: 1,
      name: 'John Doe',
      email: 'john.doe@company.com',
      expertise: 'Backend Development',
      avatar: 'assets/images/default-avatar.png'
    };

    this.jobs = [
      {
        id: 1,
        title: 'Senior Software Engineer',
        department: 'Engineering',
        description: 'Looking for an experienced software engineer with strong backend skills.',
        fileName: 'senior-dev-job.pdf',
        fileSize: 245000,
        uploadDate: new Date().toISOString(),
        panelMember: this.panelMembers[0] || defaultPanelMember
      }
    ];
    
    this.filteredJobs = [...this.jobs];
    this.totalJobs = this.jobs.length;
    this.loading = false;
  }

  uploadProfile(): void {
    if (!this.selectedFile || !this.selectedPanelMember) {
      this.snackBar.open('Please select a file and panel member', 'Close', {
        duration: 3000
      });
      return;
    }

    this.loading = true;
    setTimeout(() => {
      const newJob: JobDescription = {
        id: this.jobs.length + 1,
        title: 'New Position',
        department: 'TBD',
        description: 'Processing job description...',
        fileName: this.selectedFile!.name,
        fileSize: this.selectedFile!.size,
        uploadDate: new Date().toISOString(),
        panelMember: this.selectedPanelMember!
      };

      this.jobs.unshift(newJob);
      this.filteredJobs = [...this.jobs];
      this.totalJobs = this.jobs.length;
      
      this.snackBar.open('Job profile uploaded successfully', 'Close', {
        duration: 3000
      });
      
      this.resetForm();
      this.loading = false;
    }, 1500);
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
        job.title.toLowerCase().includes(searchLower) ||
        job.description.toLowerCase().includes(searchLower) ||
        job.department.toLowerCase().includes(searchLower)
      );
    }
    this.totalJobs = this.filteredJobs.length;
    this.currentPage = 0;
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
  }
}