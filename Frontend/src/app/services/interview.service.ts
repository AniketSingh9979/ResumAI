import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';

// Updated interfaces to match backend models
export interface CodingQuestion {
  id: string;
  title: string;
  description: string;
  starterCode?: string;
  domain: string;
  difficulty: string;
  experienceLevel: string;
  tags?: string;
}

export interface MCQQuestion {
  id: string;
  title: string;
  description: string;
  options: string;
  questionType: string;
  domain: string;
  multipleSelection: boolean;
  difficulty: string;
}

export interface SubjectiveQuestion {
  id: string;
  title: string;
  description: string;
  domain: string;
  difficulty: string;
  questionType: string;
  expectedAnswer?: string; // Not exposed to frontend, used for similarity comparison
}

export interface ChatRequest {
  questionText: string;    // Actual interview question
  userAnswer: string;      // User's typed answer
  resumeId: string;
  domain: string;
  questionType?: string;
}

export interface ChatResponse {
  botResponse: string;
  similarity?: number;
  correct?: boolean;
  messageId: string;
  timestamp: string;
  error?: string;
}

export interface MCQAnswer {
  questionId: string;
  selectedOptions: string[];
}

export interface CandidateInfo {
  name: string;
  email: string;
  domain: string;
  experience: number;
}



export interface InterviewSubmission {
  resumeId: string;
  candidateName: string;
  candidateEmail?: string;
  domain: string;
  experienceLevel: string;
  totalQuestions: number;
  codingQuestions: number;
  mcqQuestions: number;
  subjectiveQuestions: number;
  correctAnswers: number;
  scorePercentage: number;
  codingScore?: number;
  mcqScore?: number;
  subjectiveScore?: number;
  feedbackSummary?: string;
  interviewDuration?: number;
  completedOnTime?: boolean;
  status: string;
  notes?: string;
}

@Injectable({
  providedIn: 'root'
})
export class InterviewService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  // Get available domains
  getAvailableDomains(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/questions/domains`);
  }











  // Get complete question set for interview
  getCompleteQuestionSet(domain: string, experience: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/questions/complete-set`, {
      params: {
        domain: domain,
        experience: experience.toString()
      }
    });
  }

  // Send Q&A response for evaluation
  sendChatResponse(request: ChatRequest): Observable<ChatResponse> {
    return this.http.post<ChatResponse>(`${this.apiUrl}/chat/respond`, request);
  }



  // Submit complete interview
  submitInterview(submission: InterviewSubmission): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/chat/submitInterview`, submission);
  }

  // Utility methods for session management
  setCandidateInfo(info: CandidateInfo): void {
    sessionStorage.setItem('candidateInfo', JSON.stringify(info));
  }

  getCandidateInfo(): CandidateInfo | null {
    const info = sessionStorage.getItem('candidateInfo');
    return info ? JSON.parse(info) : null;
  }

  generateResumeId(): string {
    return 'RES_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
  }

  // Convert MCQ options string to array
  parseOptions(optionsString: string): string[] {
    try {
      // Try to parse as JSON first
      return JSON.parse(optionsString);
    } catch {
      // Fall back to comma-separated parsing
      return optionsString.split(',').map(opt => opt.trim());
    }
  }

  // Calculate scores for different sections
  calculateMCQScore(answers: MCQAnswer[], questions: MCQQuestion[]): number {
    if (questions.length === 0) return 0;
    
    let correctCount = 0;
    answers.forEach(answer => {
      const question = questions.find(q => q.id === answer.questionId);
      if (question) {
        // Note: Actual scoring would need the correct answers from backend
        // This is a placeholder implementation
        correctCount++; // Simplified for demo
      }
    });
    
    return Math.round((correctCount / questions.length) * 100);
  }

  // Format time remaining
  formatTime(seconds: number): string {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`;
  }

  // Get experience level display text
  getExperienceLevel(years: number): string {
    if (years <= 1) return 'Fresher';
    if (years <= 3) return 'Junior';
    if (years <= 6) return 'Mid-level';
    return 'Senior';
  }
} 