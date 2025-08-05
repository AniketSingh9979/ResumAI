import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { InterviewService } from '../../services/interview.service';

@Component({
  selector: 'app-interview-landing',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './interview-landing.component.html',
  styleUrls: ['./interview-landing.component.css']
})
export class InterviewLandingComponent implements OnInit {
  candidateForm: FormGroup;
  interviewConfig: any = null;
  availableDomains: string[] = [];
  isLoading = false;
  agreed = false;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private interviewService: InterviewService
  ) {
    this.candidateForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      domain: ['', Validators.required],
      experience: [0, [Validators.required, Validators.min(0)]]
    });
  }

  ngOnInit() {
    this.loadAvailableDomains();
    this.setDefaultInterviewConfig();
  }

  loadAvailableDomains() {
    this.interviewService.getAvailableDomains().subscribe({
      next: (domains: string[]) => {
        this.availableDomains = domains;
      },
      error: (error: any) => {
        console.error('Error loading domains:', error);
        // Fallback domains
        this.availableDomains = ['Java Development', 'Python Development', 'System Design'];
      }
    });
  }

  setDefaultInterviewConfig() {
    // Set default interview configuration
    this.interviewConfig = {
      duration: 45, // minutes
      sections: {
        coding: { questions: 2, timeAllocation: '40%' },
        mcq: { questions: 10, timeAllocation: '35%' },
        subjective: { questions: 4, timeAllocation: '25%' }
      },
      totalQuestions: 16,
      passingScore: 60
    };
  }

  onDomainChange() {
    // Domain change doesn't require API call anymore
    // Just update the UI if needed
    const domain = this.candidateForm.get('domain')?.value;
    console.log('Selected domain:', domain);
  }

  toggleAgreement() {
    this.agreed = !this.agreed;
  }

  startInterview() {
    if (this.candidateForm.valid && this.agreed) {
      this.isLoading = true;
      
      // Store candidate information for the interview
      const candidateInfo = this.candidateForm.value;
      sessionStorage.setItem('candidateInfo', JSON.stringify(candidateInfo));
      
      // Navigate to interview test page
      setTimeout(() => {
        this.router.navigate(['/interview-test'], {
          queryParams: {
            domain: candidateInfo.domain,
            experience: candidateInfo.experience
          }
        });
      }, 1000);
    }
  }

  getExperienceLevel(years: number): string {
    if (years <= 1) return 'Fresher (0-1 years)';
    if (years <= 3) return 'Junior (1-3 years)';
    if (years <= 6) return 'Mid-level (3-6 years)';
    return 'Senior (6+ years)';
  }
} 