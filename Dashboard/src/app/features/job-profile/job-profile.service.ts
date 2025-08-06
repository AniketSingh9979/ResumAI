import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface JobDescription {
  id: number;
  fileName: string;
  fileSize: number;
  uploadDate: string;
  panelMember: {
    id: number;
    name: string;
    email: string;
    expertise: string;
    avatar: string;
  };
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

export interface JobPaginationParams {
  page: number;
  size: number;
  sortBy: string;
  sortDir: string;
  title?: string;
  company?: string;
  location?: string;
  experienceLevel?: string;
  panelistName?: string;
}

export interface UploadResponse {
  success: boolean;
  message: string;
  jobId?: number;
  job?: JobDescription;
}

@Injectable({
  providedIn: 'root'
})
export class JobProfileService {
  
  private readonly baseUrl = 'http://localhost:8081/api/matching';

  constructor(private http: HttpClient) {}

  /**
   * Upload job description file or data
   */
  uploadJobDescription(formData: FormData): Observable<UploadResponse> {
    return this.http.post<UploadResponse>(`${this.baseUrl}/uploadJD`, formData);
  }

  /**
   * Get paginated job descriptions with filtering and sorting
   */
  getJobsPaginated(params: JobPaginationParams): Observable<JobDescriptionResponse> {
    let httpParams = new HttpParams()
      .set('page', params.page.toString())
      .set('size', params.size.toString())
      .set('sortBy', params.sortBy)
      .set('sortDir', params.sortDir);

    // Add filter parameters if they exist
    if (params.title && params.title.trim()) {
      httpParams = httpParams.set('title', params.title.trim());
    }
    if (params.company && params.company.trim()) {
      httpParams = httpParams.set('company', params.company.trim());
    }
    if (params.location && params.location.trim()) {
      httpParams = httpParams.set('location', params.location.trim());
    }
    if (params.experienceLevel && params.experienceLevel.trim()) {
      httpParams = httpParams.set('experienceLevel', params.experienceLevel.trim());
    }
    if (params.panelistName && params.panelistName.trim()) {
      httpParams = httpParams.set('panelistName', params.panelistName.trim());
    }

    return this.http.get<JobDescriptionResponse>(`${this.baseUrl}/jobs/paginated`, { params: httpParams });
  }

  /**
   * Get all job descriptions (non-paginated)
   */
  getAllJobs(): Observable<JobDescription[]> {
    return this.http.get<JobDescription[]>(`${this.baseUrl}/jobs`);
  }

  /**
   * Get a specific job description by ID
   */
  getJobById(jobId: number): Observable<JobDescription> {
    return this.http.get<JobDescription>(`${this.baseUrl}/jobs/${jobId}`);
  }

  /**
   * Delete a job description
   */
  deleteJob(jobId: number): Observable<UploadResponse> {
    return this.http.delete<UploadResponse>(`${this.baseUrl}/jobs/${jobId}`);
  }
} 