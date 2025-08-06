import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

export interface JobDescription {
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
  panelMember?: any;
}

export interface UploadResponse {
  success: boolean;
  message: string;
  jobId?: number;
  job?: JobDescription;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data?: T;
  jobs?: T;
  errors?: string[];
  timestamp: string;
}

@Injectable({
  providedIn: 'root'
})
export class JobProfileService {
  private readonly API_BASE_URL = 'http://localhost:8081/api/matching';

  constructor(private http: HttpClient) {}

  /**
   * Upload a job description file with panel member assignment
   */
  uploadJobDescription(file: File, panelMemberId: number, panelMemberName: string, panelMemberEmail: string): Observable<UploadResponse> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('panelMemberId', panelMemberId.toString());
    formData.append('panelMemberName', panelMemberName);
    formData.append('panelMemberEmail', panelMemberEmail);

    return this.http.post<UploadResponse>(`${this.API_BASE_URL}/uploadJD`, formData)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Get all job descriptions
   */
  getJobs(): Observable<JobDescription[]> {
    return this.http.get<ApiResponse<JobDescription[]>>(`${this.API_BASE_URL}/jobs`)
      .pipe(
        map(response => {
          if (response.success && (response.jobs || response.data)) {
            return response.jobs || response.data || [];
          }
          return [];
        }),
        catchError(this.handleError)
      );
  }

  /**
   * Get a specific job description by ID
   */
  getJobById(id: number): Observable<JobDescription> {
    return this.http.get<ApiResponse<JobDescription>>(`${this.API_BASE_URL}/jobs/${id}`)
      .pipe(
        map(response => {
          if (response.success && response.data) {
            return response.data;
          }
          throw new Error('Job not found');
        }),
        catchError(this.handleError)
      );
  }

  /**
   * Delete a job description
   */
  deleteJob(id: number): Observable<any> {
    return this.http.delete(`${this.API_BASE_URL}/jobs/${id}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Download a job description file
   */
  downloadJobFile(id: number): Observable<Blob> {
    return this.http.get(`${this.API_BASE_URL}/download/${id}`, { 
      responseType: 'blob' 
    }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Search job descriptions
   */
  searchJobs(searchTerm: string): Observable<JobDescription[]> {
    return this.http.get<ApiResponse<JobDescription[]>>(`${this.API_BASE_URL}/jobs/search`, {
      params: { q: searchTerm }
    }).pipe(
      map(response => {
        if (response.success && (response.jobs || response.data)) {
          return response.jobs || response.data || [];
        }
        return [];
      }),
      catchError(this.handleError)
    );
  }

  /**
   * Update job description
   */
  updateJob(id: number, jobData: Partial<JobDescription>): Observable<JobDescription> {
    return this.http.put<ApiResponse<JobDescription>>(`${this.API_BASE_URL}/jobs/${id}`, jobData)
      .pipe(
        map(response => {
          if (response.success && response.data) {
            return response.data;
          }
          throw new Error('Failed to update job');
        }),
        catchError(this.handleError)
      );
  }

  /**
   * Handle HTTP errors
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An unknown error occurred';
    
    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Error: ${error.error.message}`;
    } else {
      // Server-side error
      switch (error.status) {
        case 0:
          errorMessage = 'Unable to connect to server. Please check your connection.';
          break;
        case 400:
          errorMessage = error.error?.message || 'Bad request. Please check your input.';
          break;
        case 401:
          errorMessage = 'Unauthorized. Please log in again.';
          break;
        case 403:
          errorMessage = 'Access forbidden. You do not have permission.';
          break;
        case 404:
          errorMessage = 'Resource not found.';
          break;
        case 413:
          errorMessage = 'File is too large. Please select a smaller file.';
          break;
        case 415:
          errorMessage = 'File type not supported. Please upload PDF, DOC, DOCX, or TXT files.';
          break;
        case 500:
          errorMessage = 'Internal server error. Please try again later.';
          break;
        default:
          errorMessage = error.error?.message || `Server error: ${error.status}`;
      }
    }

    console.error('JobProfileService Error:', errorMessage, error);
    return throwError(() => new Error(errorMessage));
  }
} 