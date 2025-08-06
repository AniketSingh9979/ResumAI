import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface Candidate {
  id: string;
  name: string;
  email?: string;
  resumeFile: File;
  uploadDate: Date;
  status: 'pending' | 'processing' | 'reviewed';
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

@Injectable({
  providedIn: 'root'
})
export class CandidateService {
  private candidates: Candidate[] = [];
  private candidatesSubject = new BehaviorSubject<Candidate[]>([]);

  getCandidates() {
    return this.candidatesSubject.asObservable();
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
    }
  }
}
