import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { PanelService } from '../../shared/services/panel.service';
import { AddPanelDialogComponent } from './add-panel-dialog/add-panel-dialog.component';
import { EditPanelDialogComponent } from './edit-panel-dialog/edit-panel-dialog.component';
import { take } from 'rxjs/operators';

// Interface for API request
interface PanelMemberRequest {
  name: string;
  email: string;
  employeeId: string;
  designation: string;
  department: string;
  location: string;
  expertise: string;
  mobileNumber: string;
  availabilityStatus: string;
}

// Interface for Panel Member response from API
interface PanelMemberResponse {
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

// Interface for transformed panel member data
interface PanelMemberDisplay extends PanelMemberResponse {
  experience: string;
  availability: string;
  rating: number;
  interviewsDone: number;
  image: string;
}

@Component({
  selector: 'app-panels',
  templateUrl: './panels.component.html',
  styleUrls: ['./panels.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    MatDialogModule
  ]
})
export class PanelsComponent implements OnInit {
  // Panel members list
  panelists: PanelMemberDisplay[] = [];
  loading = false;
  
  // Form properties
  addPanelForm: FormGroup;
  showAddForm = false;
  isSubmitting = false;
  
  // Message properties
  showMessage = false;
  message = '';
  messageType: 'success' | 'error' = 'success';

  constructor(
    private dialog: MatDialog,
    private panelService: PanelService,
    private formBuilder: FormBuilder,
    private http: HttpClient
  ) {
    this.addPanelForm = this.createForm();
  }

  ngOnInit(): void {
    this.loadPanelMembers();
  }

  /**
   * Get total number of available panelists
   */
  getAvailablePanelists(): number {
    return this.panelists.filter(p => p.availability === 'Available').length;
  }

  /**
   * Get total interviews conducted
   */
  getTotalInterviews(): number {
    return this.panelists.reduce((sum, p) => sum + (p.interviewsDone || 0), 0);
  }

  /**
   * Open add panel dialog
   */
  openAddPanelDialog(): void {
    this.showAddForm = false;
    const dialogRef = this.dialog.open(AddPanelDialogComponent, {
      width: '700px',
      minWidth: '700px',
      maxWidth: '90vw',
      maxHeight: '85vh',
      disableClose: false,
      panelClass: ['custom-dialog-container'],
      autoFocus: false,
      hasBackdrop: true,
      backdropClass: 'custom-backdrop',
      position: undefined
    });

    dialogRef.afterClosed().pipe(take(1)).subscribe((result: PanelMemberRequest | undefined) => {
      if (result) {
        this.addPanel(result);
      }
    });
  }

  /**
   * Edit panel member
   */
  editPanel(panel: PanelMemberDisplay): void {
    const dialogRef = this.dialog.open(EditPanelDialogComponent, {
      width: '600px',
      data: panel,
      disableClose: true,
      panelClass: ['custom-dialog-container', 'compact-dialog']
    });

    dialogRef.afterClosed().pipe(take(1)).subscribe((result: PanelMemberRequest | undefined) => {
      if (result) {
        this.updatePanel(panel.id, result);
      }
    });
  }

  /**
   * Delete panel member
   */
  deletePanel(panel: PanelMemberDisplay): void {
    if (confirm(`Are you sure you want to delete ${panel.name}?`)) {
      this.panelService.deletePanel(panel.id).subscribe({
        next: () => {
          this.showSuccessMessage('Panel member deleted successfully');
          this.loadPanelMembers();
        },
        error: (error: HttpErrorResponse) => {
          this.showErrorMessage('Failed to delete panel member. Please try again.');
          console.error('Error deleting panel:', error);
        }
      });
    }
  }

  /**
   * Load panel members from API
   */
  private loadPanelMembers(): void {
    this.loading = true;
    
    this.panelService.getPanelMembers().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.panelists = response.data.map(member => this.transformPanelMember(member));
        } else {
          console.error('Failed to load panel members:', response.message);
          this.showErrorMessage('Failed to load panel members');
          this.loadFallbackData();
        }
        this.loading = false;
      },
      error: (error: HttpErrorResponse) => {
        console.error('Error loading panel members:', error);
        this.showErrorMessage('Unable to connect to server. Loading sample data.');
        this.loadFallbackData();
        this.loading = false;
      }
    });
  }

  /**
   * Transform API panel member response to match UI structure
   */
  private transformPanelMember(member: PanelMemberResponse): PanelMemberDisplay {
    return {
      ...member,
      experience: this.calculateExperience(member.createdDate),
      availability: member.availabilityStatusDisplay || this.formatAvailabilityStatus(member.availabilityStatus),
      rating: this.generateRandomRating(),
      interviewsDone: this.generateRandomInterviews(),
      image: this.generateAvatarUrl(member.name)
    };
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
   * Format availability status for display
   */
  private formatAvailabilityStatus(status: string): string {
    switch (status) {
      case 'AVAILABLE': return 'Available';
      case 'BUSY': return 'Busy';
      case 'ON_LEAVE': return 'On Leave';
      case 'UNAVAILABLE': return 'Unavailable';
      default: return status;
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
   * Generate avatar URL using UI Avatars service
   */
  private generateAvatarUrl(name: string): string {
    return `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=random&color=fff&size=150&bold=true`;
  }

  /**
   * Fallback to mock data if API is not available
   */
  private loadFallbackData(): void {
    this.panelists = this.panelService.getPanels();
  }

  /**
   * Create reactive form with validation
   */
  private createForm(): FormGroup {
    return this.formBuilder.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(150)]],
      employeeId: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
      designation: ['', [Validators.required, Validators.maxLength(100)]],
      department: ['', [Validators.required]],
      location: ['', [Validators.required]],
      expertise: ['', [Validators.required, Validators.maxLength(200)]],
      mobileNumber: ['', [Validators.required, Validators.pattern(/^[+]?[0-9]{10,15}$/)]],
      availabilityStatus: ['', [Validators.required]]
    });
  }

  /**
   * Add new panel member
   */
  private addPanel(panelData: PanelMemberRequest): void {
    this.panelService.addPanel(panelData).subscribe({
      next: () => {
        this.showSuccessMessage('Panel member added successfully');
        this.loadPanelMembers();
      },
      error: (error: HttpErrorResponse) => {
        this.handleApiError(error);
      }
    });
  }

  /**
   * Update panel member
   */
  private updatePanel(id: number, updatedData: PanelMemberRequest): void {
    this.panelService.updatePanel(id, updatedData).subscribe({
      next: () => {
        this.showSuccessMessage('Panel member updated successfully');
        this.loadPanelMembers();
      },
      error: (error: HttpErrorResponse) => {
        this.handleApiError(error);
      }
    });
  }

  /**
   * Show success message
   */
  private showSuccessMessage(msg: string): void {
    this.message = msg;
    this.messageType = 'success';
    this.showMessage = true;
    setTimeout(() => this.hideMessage(), 5000);
  }

  /**
   * Show error message
   */
  private showErrorMessage(msg: string): void {
    this.message = msg;
    this.messageType = 'error';
    this.showMessage = true;
    setTimeout(() => this.hideMessage(), 7000);
  }

  /**
   * Hide message
   */
  hideMessage(): void {
    this.showMessage = false;
    this.message = '';
  }

  /**
   * Get error message for form field
   */
  getFieldError(fieldName: string): string {
    const field = this.addPanelForm.get(fieldName);
    if (!field) return '';

    if (field.hasError('required')) {
      return `${fieldName} is required`;
    }
    if (field.hasError('email')) {
      return 'Please enter a valid email address';
    }
    if (field.hasError('minlength')) {
      const minLength = field.errors?.['minlength']?.requiredLength;
      return `${fieldName} must be at least ${minLength} characters`;
    }
    if (field.hasError('maxlength')) {
      const maxLength = field.errors?.['maxlength']?.requiredLength;
      return `${fieldName} cannot exceed ${maxLength} characters`;
    }
    if (field.hasError('pattern')) {
      return 'Please enter a valid mobile number';
    }
    return '';
  }

  /**
   * Handle API error response
   */
  private handleApiError(error: HttpErrorResponse, customMessage?: string): void {
    let errorMessage = customMessage || 'An error occurred while processing your request.';
    
    if (error) {
      if (error.status === 400) {
        if (error.error?.errors && Array.isArray(error.error.errors)) {
          errorMessage = error.error.errors.join(', ');
        } else if (error.error?.message) {
          errorMessage = error.error.message;
        } else {
          errorMessage = 'Invalid data provided. Please check your input.';
        }
      } else if (error.status === 409) {
        errorMessage = 'Email or Employee ID already exists. Please use different values.';
      } else if (error.status === 500) {
        errorMessage = 'Server error. Please try again later.';
      } else if (error.status === 0) {
        errorMessage = 'Cannot connect to server. Please check your internet connection.';
      }
    }
    
    this.showErrorMessage(errorMessage);
  }
}
