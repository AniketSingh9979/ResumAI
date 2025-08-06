import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { PanelService } from '../../shared/services/panel.service';
import { CandidateService, Candidate } from '../../shared/services/candidate.service';
import { Subscription } from 'rxjs';

interface TimeSlot {
  value: string;
  display: string;
}

interface InterviewFormData {
  candidate: string;
  panelMember: number;
  date: Date;
  timeSlot: string;
  duration: string;
  type: 'technical' | 'hr' | 'managerial' | 'cultural';
  notes?: string;
}

@Component({
  selector: 'app-interviews',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSnackBarModule,
    MatChipsModule,
    MatAutocompleteModule
  ],
  templateUrl: './interviews.component.html',
  styleUrls: ['./interviews.component.scss']
})
export class InterviewsComponent implements OnInit, OnDestroy {
  interviewForm: FormGroup;
  candidates: Candidate[] = [];
  panelMembers: any[] = [];
  timeSlots: TimeSlot[] = [];
  minDate = new Date();
  private candidatesSubscription?: Subscription;

  constructor(
    private fb: FormBuilder,
    private panelService: PanelService,
    private candidateService: CandidateService,
    private snackBar: MatSnackBar
  ) {
    this.interviewForm = this.fb.group({
      candidate: ['', Validators.required],
      panelMember: ['', Validators.required],
      date: ['', Validators.required],
      timeSlot: ['', Validators.required],
      duration: ['60', Validators.required],
      type: ['technical', Validators.required],
      notes: ['']
    });

    // Generate time slots from 9 AM to 6 PM
    const startHour = 9;
    const endHour = 18;
    for (let hour = startHour; hour < endHour; hour++) {
      const time24 = `${hour}:00`;
      const time12 = `${hour > 12 ? hour - 12 : hour}:00 ${hour >= 12 ? 'PM' : 'AM'}`;
      this.timeSlots.push({ value: time24, display: time12 });
      
      // Add 30-minute slots
      const time24Half = `${hour}:30`;
      const time12Half = `${hour > 12 ? hour - 12 : hour}:30 ${hour >= 12 ? 'PM' : 'AM'}`;
      this.timeSlots.push({ value: time24Half, display: time12Half });
    }
  }

  ngOnInit(): void {
    this.initializeData();
  }

  ngOnDestroy(): void {
    if (this.candidatesSubscription) {
      this.candidatesSubscription.unsubscribe();
    }
  }

  private initializeData(): void {
    this.candidatesSubscription = this.candidateService.getCandidates()
      .subscribe(candidates => {
        this.candidates = candidates;
      });

    this.panelMembers = this.panelService.getPanels();
  }

  onSubmit(): void {
    if (this.interviewForm.valid) {
      const formData = this.interviewForm.value as InterviewFormData;
      
      // Simulate API call to schedule interview
      setTimeout(() => {
        this.sendInterviewEmail(formData);
        this.snackBar.open('Interview scheduled successfully!', 'Close', {
          duration: 3000,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        });
        this.interviewForm.reset({
          duration: '60',
          type: 'technical'
        });
      }, 1000);
    } else {
      this.snackBar.open('Please fill in all required fields', 'Close', {
        duration: 3000,
        horizontalPosition: 'end',
        verticalPosition: 'top'
      });
    }
  }

  private sendInterviewEmail(formData: InterviewFormData): void {
    const candidate = this.candidates.find(c => c.id === formData.candidate);
    const panelMember = this.panelMembers.find(p => p.id === formData.panelMember);
    const timeSlot = this.timeSlots.find(t => t.value === formData.timeSlot);
    
    // Simulate sending email
    console.log('Sending interview invitation email:', {
      to: [
        candidate?.email, 
        panelMember?.email
      ].filter((email): email is string => email !== undefined),
      subject: `Interview Scheduled: ${formData.type.charAt(0).toUpperCase() + formData.type.slice(1)} Interview`,
      date: formData.date,
      time: timeSlot?.display,
      duration: formData.duration + ' minutes'
    });
  }

  getErrorMessage(fieldName: string): string {
    const control = this.interviewForm.get(fieldName);
    if (control?.hasError('required')) {
      return `${fieldName.charAt(0).toUpperCase() + fieldName.slice(1)} is required`;
    }
    return '';
  }

  resetForm(): void {
    this.interviewForm.reset({
      duration: '60',
      type: 'technical'
    });
  }
}
