import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, catchError, of } from 'rxjs';
import { HttpClient } from '@angular/common/http';

export interface Candidate {
  id: string;
  name: string;
  email?: string;
  resumeFile?: File;
  uploadDate: Date;
  status: 'pending' | 'processing' | 'reviewed';
  resumeScore?: number;
  skills?: string[];
  extractedInfo?: {
    skills?: string[];
    experience?: string[];
    education?: string[];
    contact?: {
      email?: string;
      phone?: string;
      location?: string;
    };
  };
}

// Interface for API response from resume-parser-service
export interface ResumeApiResponse {
  success: boolean;
  message: string;
  totalCount: number;
  resumes: ResumeItem[];
}

export interface ResumeItem {
  resumeId: number;
  email: string;
  originalFileName: string;
  uploadTime: string;
  fileSize: number;
  contentType: string;
  score: number;
  skills: string;
  experience: string;
  hasFile: boolean;
  downloadUrl: string;
}

@Injectable({
  providedIn: 'root'
})
export class CandidateService {
  private candidates: Candidate[] = [];
  private candidatesSubject = new BehaviorSubject<Candidate[]>([]);
  private apiUrl = 'http://localhost:8081/api'; // Resume parser service URL

  constructor(private http: HttpClient) {
    this.loadCandidatesFromAPI();
  }

  getCandidates() {
    return this.candidatesSubject.asObservable();
  }

  // Load candidates from the resume parser service API
  loadCandidatesFromAPI() {
    this.http.get<ResumeApiResponse>(`${this.apiUrl}/resumesList`)
      .pipe(
        catchError(error => {
          console.error('Error loading candidates from API:', error);
          return of({ success: false, message: 'Failed to load', totalCount: 0, resumes: [] });
        })
      )
      .subscribe(response => {
        if (response.success) {
          const candidates = this.mapApiResponseToCandidates(response.resumes);
          this.candidates = candidates;
          this.candidatesSubject.next([...this.candidates]);
        }
      });
  }

  // Map API response to Candidate interface
  private mapApiResponseToCandidates(resumes: ResumeItem[]): Candidate[] {
    return resumes.map(resume => ({
      id: resume.resumeId.toString(),
      name: this.extractNameFromFileName(resume.originalFileName) || resume.email || 'Unknown',
      email: resume.email,
      uploadDate: new Date(resume.uploadTime),
      status: 'reviewed' as const,
      resumeScore: resume.score || 0,
      skills: resume.skills ? resume.skills.split(',').map(skill => skill.trim()).filter(skill => skill) : [],
      extractedInfo: {
        experience: resume.experience ? [resume.experience] : [],
        contact: {
          email: resume.email
        }
      }
    }));
  }

  // Extract candidate name from filename
  private extractNameFromFileName(fileName: string): string {
    if (!fileName) return '';
    
    // Remove file extension
    const nameWithoutExt = fileName.replace(/\.[^/.]+$/, '');
    
    // Replace underscores and hyphens with spaces
    const cleanName = nameWithoutExt.replace(/[-_]/g, ' ');
    
    // Convert to title case
    return cleanName.replace(/\w\S*/g, (txt) => 
      txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase()
    );
  }

  // Get detailed resume information including skills
  getResumeDetails(resumeId: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/resumes/${resumeId}`)
      .pipe(
        catchError(error => {
          console.error('Error loading resume details:', error);
          return of(null);
        })
      );
  }

  // Refresh candidates data
  refreshCandidates() {
    this.loadCandidatesFromAPI();
  }

  uploadResume(file: File) {
    const newCandidate: Candidate = {
      id: Date.now().toString(),
      name: file.name.split('.')[0], // Initial name from filename
      resumeFile: file,
      uploadDate: new Date(),
      status: 'pending'
    };

    this.candidates.push(newCandidate);
    this.candidatesSubject.next([...this.candidates]);
    return newCandidate;
  }

  updateCandidateStatus(id: string, status: 'pending' | 'processing' | 'reviewed') {
    const candidate = this.candidates.find(c => c.id === id);
    if (candidate) {
      candidate.status = status;
      this.candidatesSubject.next([...this.candidates]);
    }
  }

  processResume(id: string) {
    const candidate = this.candidates.find(c => c.id === id);
    if (candidate) {
      candidate.status = 'processing';
      this.candidatesSubject.next([...this.candidates]);

      // Simulate API call to process resume
      setTimeout(() => {
        candidate.extractedInfo = {
          skills: ['Angular', 'TypeScript', 'React', 'Node.js'],
          experience: [
            'Senior Frontend Developer at Tech Corp (2020-Present)',
            'Web Developer at StartupX (2018-2020)'
          ],
          education: [
            'Master of Computer Science, University XYZ (2018)',
            'Bachelor of Engineering, ABC University (2016)'
          ],
          contact: {
            email: candidate.email || 'example@email.com',
            phone: '+1 (123) 456-7890',
            location: 'San Francisco, CA'
          }
        };
        // Add a mock resume score between 60 and 98
        candidate.resumeScore = Math.floor(Math.random() * (98 - 60 + 1)) + 60;
        candidate.status = 'reviewed';
        if (!candidate.email && candidate.extractedInfo?.contact?.email) {
          candidate.email = candidate.extractedInfo.contact.email;
        }
        this.candidatesSubject.next([...this.candidates]);
      }, 2000);
    }
  }

  updateCandidate(updatedCandidate: Candidate) {
    const index = this.candidates.findIndex(c => c.id === updatedCandidate.id);
    if (index !== -1) {
      this.candidates[index] = { ...updatedCandidate };
      this.candidatesSubject.next([...this.candidates]);
      return this.candidatesSubject.asObservable();
    }
    return this.candidatesSubject.asObservable();
  }

  addCandidate(candidate: Candidate) {
    this.candidates.push(candidate);
    this.candidatesSubject.next([...this.candidates]);
    return this.candidatesSubject.asObservable();
  }
}
