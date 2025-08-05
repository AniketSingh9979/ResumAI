import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

interface InterviewSummary {
  candidateName: string;
  domain: string;
  experienceLevel: string;
  codingQuestions: number;
  mcqQuestions: number;
  subjectiveQuestions: number;
  totalQuestions: number;
  interviewDuration: string;
}

@Component({
  selector: 'app-thank-you',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './thank-you.component.html',
  styleUrls: ['./thank-you.component.css']
})
export class ThankYouComponent implements OnInit {
  interviewSummary: InterviewSummary | null = null;

  constructor(private router: Router) {}

  ngOnInit(): void {
    this.loadInterviewSummary();
  }

  private loadInterviewSummary(): void {
    // Get summary data from session storage
    const summaryData = sessionStorage.getItem('interviewSummary');
    
    if (summaryData) {
      this.interviewSummary = JSON.parse(summaryData);
    } else {
      // Fallback if no summary data found
      this.interviewSummary = {
        candidateName: 'Candidate',
        domain: 'Unknown',
        experienceLevel: 'Unknown',
        codingQuestions: 0,
        mcqQuestions: 0,
        subjectiveQuestions: 0,
        totalQuestions: 0,
        interviewDuration: '0 minutes'
      };
    }

    // Clear session storage
    this.clearSessionData();
  }

  private clearSessionData(): void {
    // Clear all interview-related session data
    sessionStorage.removeItem('candidateInfo');
    sessionStorage.removeItem('interviewSummary');
    sessionStorage.removeItem('interviewStartTime');
  }

  // Go back to home page
  goToHomePage(): void {
    this.router.navigate(['/']);
  }
} 