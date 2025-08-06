import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { MatDialog } from '@angular/material/dialog';
import { PanelService } from '../../shared/services/panel.service';
import { AddPanelDialogComponent } from './add-panel-dialog/add-panel-dialog.component';

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

// Interface for API response
interface ApiResponse<T> {
  success: boolean;
  message: string;
  data?: T;
  errors?: string[];
  timestamp: string;
}

@Component({
  selector: 'app-panels',
  templateUrl: './panels.component.html',
  styleUrls: ['./panels.component.scss'],
  standalone: false
})
export class PanelsComponent implements OnInit {
  panelists: any[] = [];
  loading = false; // Add loading state
  
  // Form properties
  addPanelForm: FormGroup;
  showAddForm = false;
  isSubmitting = false;
  
  // Message properties
  showMessage = false;
  message = '';
  messageType: 'success' | 'error' = 'success';
  
  // API endpoint
  private readonly API_BASE_URL = 'http://localhost:8081/api/panel-members';

  constructor(
    private dialog: MatDialog,
    private panelService: PanelService,
    private formBuilder: FormBuilder,
    private http: HttpClient
  ) {
    this.addPanelForm = this.createForm();
  }

  ngOnInit() {
    this.loadPanelMembers();
  }

  /**
   * Load panel members from API
   */
  private loadPanelMembers() {
    this.loading = true;
    
    this.http.get<ApiResponse<PanelMemberResponse[]>>(this.API_BASE_URL)
      .subscribe({
        next: (response) => {
          if (response.success && response.data) {
            // Transform API response to match existing panelist structure
            this.panelists = response.data.map(member => this.transformPanelMember(member));
          } else {
            console.error('Failed to load panel members:', response.message);
            this.showErrorMessage('Failed to load panel members');
            // Fallback to mock data if API fails
            this.loadFallbackData();
          }
          this.loading = false;
        },
        error: (error: HttpErrorResponse) => {
          console.error('Error loading panel members:', error);
          this.showErrorMessage('Unable to connect to server. Loading sample data.');
          // Fallback to mock data if API fails
          this.loadFallbackData();
          this.loading = false;
        }
      });
  }

  /**
   * Transform API panel member response to match UI structure
   */
  private transformPanelMember(member: PanelMemberResponse): any {
    return {
      id: member.id,
      name: member.name,
      email: member.email,
      expertise: member.expertise,
      experience: this.calculateExperience(member.createdDate),
      availability: member.availabilityStatusDisplay || this.formatAvailabilityStatus(member.availabilityStatus),
      rating: this.generateRandomRating(), // Since rating is not in API, generate random
      interviewsDone: this.generateRandomInterviews(), // Since this is not in API, generate random
      image: this.generateAvatarUrl(member.name), // Generate avatar URL
      designation: member.designation,
      department: member.department,
      location: member.location,
      mobileNumber: member.mobileNumber,
      employeeId: member.employeeId,
      availabilityStatus: member.availabilityStatus,
      createdDate: member.createdDate,
      updatedDate: member.updatedDate,
      isActive: member.isActive
    };
  }

  /**
   * Calculate experience based on created date (mock calculation)
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
   * Generate random rating for display (since not in API)
   */
  private generateRandomRating(): number {
    return Math.floor(Math.random() * 2) + 4; // Random between 4.0 and 5.0
  }

  /**
   * Generate random interview count (since not in API)
   */
  private generateRandomInterviews(): number {
    return Math.floor(Math.random() * 50) + 5; // Random between 5 and 55
  }

  /**
   * Generate avatar URL using a service like UI Avatars
   */
  private generateAvatarUrl(name: string): string {
    const initials = name.split(' ').map(n => n[0]).join('').toUpperCase();
    return `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=random&color=fff&size=150&bold=true`;
  }

  /**
   * Fallback to mock data if API is not available
   */
  private loadFallbackData() {
    this.panelists = this.panelService.getPanels();
  }

  /**
   * Create reactive form with validation
   */
  private createForm(): FormGroup {
    return this.formBuilder.group({
      name: [
        '', 
        [
          Validators.required, 
          Validators.minLength(2), 
          Validators.maxLength(100)
        ]
      ],
      email: [
        '', 
        [
          Validators.required, 
          Validators.email,
          Validators.maxLength(150)
        ]
      ],
      employeeId: [
        '', 
        [
          Validators.required, 
          Validators.minLength(3), 
          Validators.maxLength(50)
        ]
      ],
      designation: [
        '', 
        [
          Validators.required, 
          Validators.maxLength(100)
        ]
      ],
      department: [
        '', 
        [Validators.required]
      ],
      location: [
        '', 
        [Validators.required]
      ],
      expertise: [
        '', 
        [
          Validators.required, 
          Validators.maxLength(200)
        ]
      ],
      mobileNumber: [
        '', 
        [
          Validators.required,
          Validators.pattern(/^[+]?[0-9]{10,15}$/)
        ]
      ],
      availabilityStatus: [
        '', 
        [Validators.required]
      ]
    });
  }

  /**
   * Open add panel form
   */
  openAddPanelForm(): void {
    this.showAddForm = true;
    this.hideMessage();
    this.addPanelForm.reset();
    
    // Scroll form into view
    setTimeout(() => {
      const formElement = document.querySelector('.add-panel-form');
      if (formElement) {
        formElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    }, 100);
  }

  /**
   * Close add panel form
   */
  closeAddForm(): void {
    this.showAddForm = false;
    this.addPanelForm.reset();
    this.hideMessage();
  }

  /**
   * Handle form submission
   */
  onSubmit(): void {
    if (this.addPanelForm.valid && !this.isSubmitting) {
      this.isSubmitting = true;
      this.hideMessage();
      
      const formData: PanelMemberRequest = this.addPanelForm.value;
      
      console.log('Submitting panel member data:', formData);
      
      // Make API call
      this.http.post<ApiResponse<any>>(this.API_BASE_URL, formData)
        .subscribe({
          next: (response) => {
            console.log('API Response:', response);
            this.handleApiSuccess(response);
          },
          error: (error: HttpErrorResponse) => {
            console.error('API Error:', error);
            this.handleApiError(error);
          },
          complete: () => {
            this.isSubmitting = false;
          }
        });
    } else {
      // Mark all fields as touched to show validation errors
      Object.keys(this.addPanelForm.controls).forEach(key => {
        this.addPanelForm.get(key)?.markAsTouched();
      });
      
      this.showErrorMessage('Please fill in all required fields correctly.');
    }
  }

  /**
   * Handle successful API response
   */
  private handleApiSuccess(response: ApiResponse<any>): void {
    if (response.success) {
      this.showSuccessMessage(response.message || 'Panel member added successfully!');
      this.addPanelForm.reset();
      this.closeAddForm();
      
      // Refresh the panels list (you might want to call an API to get updated list)
      this.refreshPanelsList();
      
    } else {
      this.handleApiError(null, response.message || 'Failed to add panel member');
    }
  }

  /**
   * Handle API error response
   */
  private handleApiError(error: HttpErrorResponse | null, customMessage?: string): void {
    let errorMessage = customMessage || 'An error occurred while adding the panel member.';
    
    if (error) {
      if (error.status === 400) {
        // Validation errors
        if (error.error && error.error.errors && Array.isArray(error.error.errors)) {
          errorMessage = error.error.errors.join(', ');
        } else if (error.error && error.error.message) {
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

  /**
   * Show success message
   */
  private showSuccessMessage(message: string): void {
    this.message = message;
    this.messageType = 'success';
    this.showMessage = true;
    
    // Auto-hide after 5 seconds
    setTimeout(() => {
      this.hideMessage();
    }, 5000);
  }

  /**
   * Show error message
   */
  private showErrorMessage(message: string): void {
    this.message = message;
    this.messageType = 'error';
    this.showMessage = true;
    
    // Auto-hide after 7 seconds for errors
    setTimeout(() => {
      this.hideMessage();
    }, 7000);
  }

  /**
   * Hide message
   */
  hideMessage(): void {
    this.showMessage = false;
    this.message = '';
  }

  /**
   * Refresh panels list after successful addition
   */
  private refreshPanelsList(): void {
    // Reload panel members from API to get the updated list
    this.loadPanelMembers();
  }

  /**
   * Get error message for a specific form field
   */
  getFieldError(fieldName: string): string {
    const field = this.addPanelForm.get(fieldName);
    if (field?.hasError('required')) {
      return `${this.getFieldDisplayName(fieldName)} is required`;
    }
    if (field?.hasError('email')) {
      return 'Please enter a valid email address';
    }
    if (field?.hasError('minlength')) {
      const requiredLength = field.errors?.['minlength']?.requiredLength;
      return `${this.getFieldDisplayName(fieldName)} must be at least ${requiredLength} characters`;
    }
    if (field?.hasError('maxlength')) {
      const requiredLength = field.errors?.['maxlength']?.requiredLength;
      return `${this.getFieldDisplayName(fieldName)} cannot exceed ${requiredLength} characters`;
    }
    if (field?.hasError('pattern')) {
      return 'Please enter a valid mobile number';
    }
    return '';
  }

  /**
   * Get display name for form fields
   */
  private getFieldDisplayName(fieldName: string): string {
    const displayNames: { [key: string]: string } = {
      name: 'Name',
      email: 'Email',
      employeeId: 'Employee ID',
      designation: 'Designation',
      department: 'Department',
      location: 'Location',
      expertise: 'Expertise',
      mobileNumber: 'Mobile Number',
      availabilityStatus: 'Availability Status'
    };
    return displayNames[fieldName] || fieldName;
  }

  /**
   * Open add panel dialog
   */
  openAddPanelDialog(): void {
    const dialogRef = this.dialog.open(AddPanelDialogComponent, {
      width: '520px',
      disableClose: true,
      panelClass: ['custom-dialog-container', 'compact-dialog'],
      autoFocus: false,
      maxHeight: '90vh',
      position: { top: '50px' }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // Handle the new panel member data
        console.log('New panel member:', result);
        // Add logic to save the panel member using your service
      }
    });
  }
}
