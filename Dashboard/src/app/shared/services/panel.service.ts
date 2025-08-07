import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';

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

// Interface for API response
interface ApiResponse<T> {
  success: boolean;
  message: string;
  data?: T;
  errors?: string[];
  timestamp: string;
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

// Extended panel member data with UI display fields
interface PanelMemberDisplay extends PanelMemberResponse {
  experience: string;
  availability: string;
  rating: number;
  interviewsDone: number;
  image: string;
}

// Sample panel member data - uses the display interface
type PanelMember = PanelMemberDisplay;

@Injectable({
  providedIn: 'root'
})
export class PanelService {
  private readonly API_BASE_URL = 'http://localhost:8081/api/panel-members';

  private panels: PanelMember[] = [
    {
      id: 1,
      name: 'Dr. Rajesh Kumar',
      expertise: 'Full Stack Development',
      experience: '8 years',
      availability: 'Available',
      rating: 4.8,
      interviewsDone: 8,
      image: 'assets/avatars/avatar1.png',
      email: 'rajesh.kumar@example.com',
      employeeId: 'EMP001',
      designation: 'Senior Developer',
      department: 'Engineering',
      location: 'India',
      mobileNumber: '+919876543210',
      availabilityStatus: 'AVAILABLE',
      availabilityStatusDisplay: 'Available',
      createdDate: '2020-01-01T00:00:00Z',
      updatedDate: '2023-08-07T00:00:00Z',
      isActive: true
    },
    {
      id: 2,
      name: 'Anjali Sharma',
      expertise: 'Backend Architecture',
      experience: '10 years',
      availability: 'Busy',
      rating: 4.9,
      interviewsDone: 10,
      image: 'assets/avatars/avatar2.png',
      email: 'anjali.sharma@example.com',
      employeeId: 'EMP002',
      designation: 'Lead Architect',
      department: 'Engineering',
      location: 'India',
      mobileNumber: '+919876543211',
      availabilityStatus: 'BUSY',
      availabilityStatusDisplay: 'Busy',
      createdDate: '2019-01-01T00:00:00Z',
      updatedDate: '2023-08-07T00:00:00Z',
      isActive: true
    },
    {
      id: 3,
      name: 'Arjun Patel',
      expertise: 'Frontend Development',
      experience: '6 years',
      availability: 'Available',
      rating: 4.7,
      interviewsDone: 5,
      image: 'assets/avatars/avatar3.png',
      email: 'arjun.patel@example.com',
      employeeId: 'EMP003',
      designation: 'Senior Frontend Developer',
      department: 'Engineering',
      location: 'India',
      mobileNumber: '+919876543212',
      availabilityStatus: 'AVAILABLE',
      availabilityStatusDisplay: 'Available',
      createdDate: '2021-01-01T00:00:00Z',
      updatedDate: '2023-08-07T00:00:00Z',
      isActive: true
    }
  ];

  constructor(private http: HttpClient) {}

  /**
   * Get all panel members
   */
  getPanelMembers(): Observable<ApiResponse<PanelMemberResponse[]>> {
    // In a real application, this would call the actual API
    // For now, return mock data in the API response format
    return of({
      success: true,
      message: 'Panel members retrieved successfully',
      data: this.panels,
      timestamp: new Date().toISOString()
    });
  }

  /**
   * Add a new panel member
   */
  addPanel(panelData: PanelMemberRequest): Observable<ApiResponse<PanelMemberResponse>> {
    // In a real application, this would call the actual API
    const formattedStatus = this.formatAvailabilityStatus(panelData.availabilityStatus);
    const newPanel: PanelMember = {
      ...panelData,
      id: this.panels.length + 1,
      experience: '0 years',
      availability: formattedStatus,
      availabilityStatusDisplay: formattedStatus,
      rating: 4.0,
      interviewsDone: 0,
      image: this.generateAvatarUrl(panelData.name),
      createdDate: new Date().toISOString(),
      updatedDate: new Date().toISOString(),
      isActive: true
    };
    
    this.panels.push(newPanel);
    
    return of({
      success: true,
      message: 'Panel member added successfully',
      data: newPanel,
      timestamp: new Date().toISOString()
    });
  }

  /**
   * Update an existing panel member
   */
  updatePanel(id: number, updatedData: PanelMemberRequest): Observable<ApiResponse<PanelMemberResponse>> {
    const index = this.panels.findIndex(p => p.id === id);
    if (index === -1) {
      return of({
        success: false,
        message: 'Panel member not found',
        timestamp: new Date().toISOString()
      });
    }

    const formattedStatus = this.formatAvailabilityStatus(updatedData.availabilityStatus);
    this.panels[index] = {
      ...this.panels[index],
      ...updatedData,
      availability: formattedStatus,
      availabilityStatusDisplay: formattedStatus,
      updatedDate: new Date().toISOString()
    };

    return of({
      success: true,
      message: 'Panel member updated successfully',
      data: this.panels[index],
      timestamp: new Date().toISOString()
    });
  }

  /**
   * Delete a panel member
   */
  deletePanel(id: number): Observable<ApiResponse<void>> {
    const index = this.panels.findIndex(p => p.id === id);
    if (index === -1) {
      return of({
        success: false,
        message: 'Panel member not found',
        timestamp: new Date().toISOString()
      });
    }

    this.panels.splice(index, 1);

    return of({
      success: true,
      message: 'Panel member deleted successfully',
      timestamp: new Date().toISOString()
    });
  }

  /**
   * Get mock panel data
   */
  getPanels(): PanelMember[] {
    return this.panels;
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
   * Generate avatar URL using UI Avatars service
   */
  private generateAvatarUrl(name: string): string {
    return `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=random&color=fff&size=150&bold=true`;
  }

  getTotalInterviews(): number {
    // For now, return a mock count - this should be replaced with an actual API call
    return this.panels.reduce((total, panel) => total + (panel.interviewsDone || 0), 0);
  }

  getPanelCount(): number {
    // For now, return the mock count - this should be replaced with an actual API call
    return this.panels.length;
  }

  getMockPanelists(): PanelMember[] {
    return this.panels;
  }
}
