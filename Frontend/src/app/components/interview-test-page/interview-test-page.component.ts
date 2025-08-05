import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, FormArray, FormControl } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { InterviewService, CodingQuestion, MCQQuestion, CandidateInfo, InterviewSubmission } from '../../services/interview.service';
import { SubjectiveQAComponent } from '../subjective-qa/subjective-qa.component';

@Component({
  selector: 'app-interview-test-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, SubjectiveQAComponent],
  templateUrl: './interview-test-page.component.html',
  styleUrls: ['./interview-test-page.component.css']
})
export class InterviewTestPageComponent implements OnInit, OnDestroy {
  activeSection: 'coding' | 'mcq' | 'qa' = 'coding';
  candidateInfo: CandidateInfo | null = null;
  codingForm: FormGroup;
  mcqForm: FormGroup;
  
  // Timer properties
  timeRemaining = 45 * 60; // 45 minutes in seconds
  timerInterval: any;
  
  // Question data
  codingQuestions: CodingQuestion[] = [];
  mcqQuestions: MCQQuestion[] = [];
  currentCodingIndex = 0;
  
  // Resume ID for tracking
  resumeId: string;
  
  // Navigation
  codingAnswers: { [questionId: string]: string } = {};
  mcqAnswers: any[] = [];
  subjectiveAnswers: any[] = [];

  // Loading states
  isLoading = true;
  isSubmitting = false;

  constructor(
    private fb: FormBuilder,
    private interviewService: InterviewService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.codingForm = this.fb.group({
      code: ['']
    });

    this.mcqForm = this.fb.group({
      answers: this.fb.array([])
    });

    this.resumeId = this.interviewService.generateResumeId();
  }

  ngOnInit() {
    this.loadCandidateInfo();
    this.loadQuestions();
    this.startTimer();
  }

  ngOnDestroy() {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
    }
  }

  loadCandidateInfo() {
    this.candidateInfo = this.interviewService.getCandidateInfo();
    if (!this.candidateInfo) {
      // Redirect to landing page if no candidate info
      this.router.navigate(['/']);
      return;
    }
  }

  loadQuestions() {
    if (!this.candidateInfo) return;

    const { domain, experience } = this.candidateInfo;

    // Load complete question set
    this.interviewService.getCompleteQuestionSet(domain, experience).subscribe({
      next: (data) => {
        this.codingQuestions = data.codingQuestions || [];
        this.mcqQuestions = data.mcqQuestions || [];
        this.setupForms();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading questions:', error);
        this.loadFallbackQuestions();
        this.isLoading = false;
      }
    });
  }

  loadFallbackQuestions() {
    // Fallback questions if backend is not available
    this.codingQuestions = [
      {
        id: 'coding-1',
        title: 'Two Sum Problem',
        description: 'Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target.',
        starterCode: 'function twoSum(nums, target) {\n    // Your code here\n}',
        domain: this.candidateInfo?.domain || 'General',
        difficulty: 'EASY',
        experienceLevel: 'FRESHER'
      }
    ];

    // Simple MCQ data with pre-parsed options
    this.mcqQuestions = [
      {
        id: 'mcq-1',
        title: 'Programming Fundamentals',
        description: 'Which of the following is a programming language?',
        options: '["Java", "HTML", "CSS", "SQL"]',
        questionType: 'MCQ',
        domain: this.candidateInfo?.domain || 'General',
        multipleSelection: false,
        difficulty: 'EASY'
      },
      {
        id: 'mcq-2',
        title: 'Database Concepts',
        description: 'Which SQL command is used to retrieve data from a database?',
        options: '["INSERT", "SELECT", "UPDATE", "DELETE"]',
        questionType: 'MCQ',
        domain: this.candidateInfo?.domain || 'General',
        multipleSelection: false,
        difficulty: 'EASY'
      },
      {
        id: 'mcq-3',
        title: 'Web Development',
        description: 'Which of the following are frontend technologies? (Select multiple)',
        options: '["Angular", "React", "Node.js", "MongoDB"]',
        questionType: 'MCQ',
        domain: this.candidateInfo?.domain || 'General',
        multipleSelection: true,
        difficulty: 'MEDIUM'
      }
    ];

    this.setupForms();
  }

  setupForms() {
    // Setup coding form
    if (this.codingQuestions.length > 0) {
      this.codingForm.patchValue({
        code: this.codingQuestions[0].starterCode || ''
      });
    }

    // Setup MCQ form
    const answersArray = this.mcqForm.get('answers') as FormArray;
    answersArray.clear();
    
    this.mcqQuestions.forEach(question => {
      if (question.multipleSelection) {
        const checkboxGroup = this.fb.group({});
        const options = this.interviewService.parseOptions(question.options);
        options.forEach(option => {
          checkboxGroup.addControl(option, new FormControl(false));
        });
        answersArray.push(checkboxGroup);
      } else {
        answersArray.push(new FormControl(''));
      }
    });
  }

  startTimer() {
    this.timerInterval = setInterval(() => {
      this.timeRemaining--;
      if (this.timeRemaining <= 0) {
        this.submitInterview();
      }
    }, 1000);
  }

  formatTime(seconds: number): string {
    return this.interviewService.formatTime(seconds);
  }

  selectSection(section: 'coding' | 'mcq' | 'qa') {
    this.saveCurrentSectionData();
    this.activeSection = section;
  }

  saveCurrentSectionData() {
    if (this.activeSection === 'coding' && this.codingQuestions.length > 0) {
      const currentQuestion = this.codingQuestions[this.currentCodingIndex];
      this.codingAnswers[currentQuestion.id] = this.codingForm.value.code;
    }
    
    if (this.activeSection === 'mcq') {
      this.saveMCQAnswers();
    }
  }

  saveMCQAnswers() {
    const formAnswers = this.mcqForm.value.answers;
    this.mcqAnswers = [];
    
    this.mcqQuestions.forEach((question, index) => {
      const options = this.interviewService.parseOptions(question.options);
      
      if (question.multipleSelection) {
        const selectedOptions: string[] = [];
        const checkboxGroup = formAnswers[index];
        Object.keys(checkboxGroup).forEach(option => {
          if (checkboxGroup[option]) {
            selectedOptions.push(option);
          }
        });
        if (selectedOptions.length > 0) {
          this.mcqAnswers.push({
            questionId: question.id,
            selectedOptions
          });
        }
      } else {
        if (formAnswers[index]) {
          this.mcqAnswers.push({
            questionId: question.id,
            selectedOptions: [formAnswers[index]]
          });
        }
      }
    });
  }

  navigateCoding(direction: 'prev' | 'next') {
    this.saveCurrentSectionData();
    
    if (direction === 'next' && this.currentCodingIndex < this.codingQuestions.length - 1) {
      this.currentCodingIndex++;
    } else if (direction === 'prev' && this.currentCodingIndex > 0) {
      this.currentCodingIndex--;
    }
    
    const currentQuestion = this.codingQuestions[this.currentCodingIndex];
    const savedAnswer = this.codingAnswers[currentQuestion.id];
    this.codingForm.patchValue({
      code: savedAnswer || currentQuestion.starterCode || ''
    });
  }



  onSubjectiveAnswersUpdate(answers: any[]) {
    this.subjectiveAnswers = answers;
  }

  async submitInterview() {
    // Show confirmation dialog with current progress
    const attemptedCount = this.getAttemptedCount();
    const codingTotal = this.codingQuestions.length;
    const mcqTotal = this.mcqQuestions.length;
    const subjectiveTotal = 4; // Fixed number of subjective questions
    const totalQuestions = codingTotal + mcqTotal + subjectiveTotal;
    
    const confirmMessage = `Are you sure you want to submit your interview?\n\n` +
                          `Progress Summary:\n` +
                          `• Coding: ${this.getCodingAttemptedCount()}/${codingTotal} questions attempted\n` +
                          `• MCQ: ${this.getMCQAttemptedCount()}/${mcqTotal} questions attempted\n` +
                          `• Subjective: ${this.getSubjectiveAttemptedCount()}/${subjectiveTotal} questions attempted\n` +
                          `• Total: ${attemptedCount}/${totalQuestions} questions attempted\n\n` +
                          `Time remaining: ${this.formatTime(this.timeRemaining)}\n\n` +
                          `Once submitted, you cannot make any changes.`;

    if (!confirm(confirmMessage)) {
      return; // User cancelled submission
    }

    this.saveCurrentSectionData();
    this.isSubmitting = true;
    
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
    }

    if (!this.candidateInfo) {
      alert('Candidate information is missing. Please restart the interview.');
      this.router.navigate(['/']);
      return;
    }

    try {
      const submission: InterviewSubmission = {
        resumeId: this.resumeId,
        candidateName: this.candidateInfo.name,
        candidateEmail: this.candidateInfo.email,
        domain: this.candidateInfo.domain,
        experienceLevel: this.interviewService.getExperienceLevel(this.candidateInfo.experience),
        totalQuestions: this.codingQuestions.length + this.mcqQuestions.length + this.subjectiveAnswers.length,
        codingQuestions: this.codingQuestions.length,
        mcqQuestions: this.mcqQuestions.length,
        subjectiveQuestions: this.subjectiveAnswers.length,
        correctAnswers: this.calculateCorrectAnswers(),
        scorePercentage: this.calculateOverallScore(),
        codingScore: this.calculateCodingScore(),
        mcqScore: this.calculateMCQScore(),
        subjectiveScore: this.calculateSubjectiveScore(),
        interviewDuration: Math.round((45 * 60 - this.timeRemaining) / 60),
        completedOnTime: this.timeRemaining > 0,
        status: 'COMPLETED',
        feedbackSummary: this.generateFeedbackSummary()
      };

      const response = await this.interviewService.submitInterview(submission).toPromise();
      
      if (response && response.success) {
        // Save interview summary for thank you page
        const interviewSummary = {
          candidateName: this.candidateInfo.name,
          domain: this.candidateInfo.domain,
          experienceLevel: this.interviewService.getExperienceLevel(this.candidateInfo.experience),
          codingQuestions: this.getCodingAttemptedCount(),
          mcqQuestions: this.getMCQAttemptedCount(),
          subjectiveQuestions: this.getSubjectiveAttemptedCount(),
          totalQuestions: submission.totalQuestions,
          interviewDuration: `${submission.interviewDuration} minutes`
        };
        
        sessionStorage.setItem('interviewSummary', JSON.stringify(interviewSummary));
        
        // Navigate to thank you page
        this.router.navigate(['/thank-you']);
      } else {
        throw new Error('Submission failed');
      }
    } catch (error) {
      console.error('Submission error:', error);
      alert('There was an error submitting your interview. Please try again.');
    } finally {
      this.isSubmitting = false;
    }
  }

  calculateCorrectAnswers(): number {
    const subjectiveCorrect = this.subjectiveAnswers.filter(a => a.correct).length;
    // MCQ and coding scoring would be implemented based on backend responses
    return subjectiveCorrect;
  }

  calculateOverallScore(): number {
    const scores = [
      this.calculateCodingScore(),
      this.calculateMCQScore(),
      this.calculateSubjectiveScore()
    ].filter(score => score > 0);
    
    return scores.length > 0 ? Math.round(scores.reduce((a, b) => a + b, 0) / scores.length) : 0;
  }

  calculateCodingScore(): number {
    // Placeholder - would be calculated by backend
    return Object.keys(this.codingAnswers).length > 0 ? 75 : 0;
  }

  calculateMCQScore(): number {
    // Placeholder - would be calculated by backend
    return this.mcqAnswers.length > 0 ? 80 : 0;
  }

  calculateSubjectiveScore(): number {
    if (this.subjectiveAnswers.length === 0) return 0;
    
    // Only count evaluated answers for scoring
    const evaluatedAnswers = this.subjectiveAnswers.filter(answer => 
      answer.evaluated && answer.similarity !== undefined
    );
    
    if (evaluatedAnswers.length === 0) {
      // If no answers evaluated yet, return 0
      return 0;
    }
    
    const avgSimilarity = evaluatedAnswers.reduce((sum, answer) => 
      sum + (answer.similarity || 0), 0) / evaluatedAnswers.length;
    return Math.round(avgSimilarity * 100);
  }

  generateFeedbackSummary(): string {
    const sections = [];
    if (Object.keys(this.codingAnswers).length > 0) {
      sections.push('Coding: Demonstrated problem-solving abilities');
    }
    if (this.mcqAnswers.length > 0) {
      sections.push('MCQ: Good technical knowledge');
    }
    if (this.subjectiveAnswers.length > 0) {
      sections.push('Subjective: Clear communication and understanding');
    }
    return sections.join('. ') + '.';
  }

  get currentCodingQuestion(): CodingQuestion | null {
    return this.codingQuestions[this.currentCodingIndex] || null;
  }

  get answersArray(): FormArray {
    return this.mcqForm.get('answers') as FormArray;
  }

  // Simple pre-parsed MCQ options (no caching needed)
  mcqOptionsMap: { [questionId: string]: string[] } = {
    'mcq-1': ['Java', 'HTML', 'CSS', 'SQL'],
    'mcq-2': ['INSERT', 'SELECT', 'UPDATE', 'DELETE'],
    'mcq-3': ['Angular', 'React', 'Node.js', 'MongoDB']
  };

  // Current question tracking for single-question view
  currentMCQIndex: number = 0;
  currentSubjectiveIndex: number = 0;

  getCurrentMCQQuestion(): MCQQuestion | null {
    return this.mcqQuestions[this.currentMCQIndex] || null;
  }

  getCurrentMCQOptions(): string[] {
    const question = this.getCurrentMCQQuestion();
    return question ? (this.mcqOptionsMap[question.id] || []) : [];
  }

  // Simple MCQ answer storage
  mcqAnswerStorage: { [questionId: string]: string | string[] } = {};

  selectSingleOption(option: string): void {
    const question = this.getCurrentMCQQuestion();
    if (question) {
      this.mcqAnswerStorage[question.id] = option;
    }
  }

  toggleMultipleOption(option: string, event: any): void {
    const question = this.getCurrentMCQQuestion();
    if (question) {
      let currentAnswers = this.mcqAnswerStorage[question.id] as string[] || [];
      if (event.target.checked) {
        if (!currentAnswers.includes(option)) {
          currentAnswers.push(option);
        }
      } else {
        currentAnswers = currentAnswers.filter(ans => ans !== option);
      }
      this.mcqAnswerStorage[question.id] = currentAnswers;
    }
  }

  clearCurrentMCQResponse(): void {
    const question = this.getCurrentMCQQuestion();
    if (question) {
      delete this.mcqAnswerStorage[question.id];
      // Clear form inputs
      const inputs = document.querySelectorAll(`input[name="mcq_${question.id}"]`) as NodeListOf<HTMLInputElement>;
      inputs.forEach(input => input.checked = false);
      
      const checkboxes = document.querySelectorAll('.option-item input[type="checkbox"]') as NodeListOf<HTMLInputElement>;
      checkboxes.forEach(checkbox => checkbox.checked = false);
    }
  }

  getCurrentSubjectiveQuestion() {
    return this.subjectiveAnswers[this.currentSubjectiveIndex] || null;
  }

  // Navigation methods for single question view
  canNavigateMCQPrevious(): boolean {
    return this.currentMCQIndex > 0;
  }

  canNavigateMCQNext(): boolean {
    return this.currentMCQIndex < this.mcqQuestions.length - 1;
  }

  navigateMCQPrevious(): void {
    if (this.canNavigateMCQPrevious()) {
      this.currentMCQIndex--;
    }
  }

  navigateMCQNext(): void {
    if (this.canNavigateMCQNext()) {
      this.currentMCQIndex++;
    }
  }

  // Get total questions for current section
  getTotalQuestionsForSection(): number {
    switch (this.activeSection) {
      case 'coding': return this.codingQuestions.length;
      case 'mcq': return this.mcqQuestions.length;
      case 'qa': return 4; // Fixed number of subjective questions
      default: return 0;
    }
  }

  // Get current question index for active section
  getCurrentQuestionIndex(): number {
    switch (this.activeSection) {
      case 'coding': return this.currentCodingIndex;
      case 'mcq': return this.currentMCQIndex;
      case 'qa': return this.currentSubjectiveIndex;
      default: return 0;
    }
  }

  // Get attempted questions count for active section (questions with actual answers)
  getAttemptedCount(): number {
    switch (this.activeSection) {
      case 'coding': return this.getCodingAttemptedCount();
      case 'mcq': return this.getMCQAttemptedCount();
      case 'qa': return this.getSubjectiveAttemptedCount();
      default: return 0;
    }
  }

  getCodingAttemptedCount(): number {
    let count = 0;
    this.codingQuestions.forEach(question => {
      const answer = this.codingAnswers[question.id];
      // Count as attempted if user typed something different from starter code
      if (answer && answer.trim() && answer.trim() !== (question.starterCode || '').trim()) {
        count++;
      }
    });
    return count;
  }

  getMCQAttemptedCount(): number {
    let count = 0;
    this.mcqQuestions.forEach(question => {
      const answer = this.mcqAnswerStorage[question.id];
      if (answer) {
        if (question.multipleSelection) {
          // For multiple selection, check if array has any items
          if (Array.isArray(answer) && answer.length > 0) {
            count++;
          }
        } else {
          // For single selection, check if string is not empty
          if (typeof answer === 'string' && answer.trim()) {
            count++;
          }
        }
      }
    });
    return count;
  }

  getSubjectiveAttemptedCount(): number {
    // Count subjective answers that have actual text content
    return this.subjectiveAnswers.filter(answer => 
      answer.answer && answer.answer.trim().length > 0
    ).length;
  }

  // Universal navigation methods
  canNavigatePrevious(): boolean {
    switch (this.activeSection) {
      case 'coding': return this.currentCodingIndex > 0;
      case 'mcq': return this.canNavigateMCQPrevious();
      case 'qa': return this.currentSubjectiveIndex > 0;
      default: return false;
    }
  }

  canNavigateNext(): boolean {
    switch (this.activeSection) {
      case 'coding': return this.currentCodingIndex < this.codingQuestions.length - 1;
      case 'mcq': return this.canNavigateMCQNext();
      case 'qa': return this.currentSubjectiveIndex < 3; // 4 questions (0-3)
      default: return false;
    }
  }

  navigatePreviousQuestion(): void {
    switch (this.activeSection) {
      case 'coding': 
        if (this.currentCodingIndex > 0) {
          this.navigateCoding('prev');
        }
        break;
      case 'mcq': this.navigateMCQPrevious(); break;
      case 'qa': this.currentSubjectiveIndex = Math.max(0, this.currentSubjectiveIndex - 1); break;
    }
  }

  navigateNextQuestion(): void {
    switch (this.activeSection) {
      case 'coding': 
        if (this.currentCodingIndex < this.codingQuestions.length - 1) {
          this.navigateCoding('next');
        }
        break;
      case 'mcq': this.navigateMCQNext(); break;
      case 'qa': this.currentSubjectiveIndex = Math.min(3, this.currentSubjectiveIndex + 1); break; // 4 questions (0-3)
    }
  }

  goToQuestion(index: number): void {
    switch (this.activeSection) {
      case 'coding': 
        if (index >= 0 && index < this.codingQuestions.length) {
          this.currentCodingIndex = index;
          const currentQuestion = this.codingQuestions[this.currentCodingIndex];
          const savedAnswer = this.codingAnswers[currentQuestion.id];
          this.codingForm.patchValue({
            code: savedAnswer || currentQuestion.starterCode || ''
          });
        }
        break;
      case 'mcq': 
        if (index >= 0 && index < this.mcqQuestions.length) {
          this.currentMCQIndex = index;
        }
        break;
      case 'qa': 
        if (index >= 0 && index < 4) { // 4 subjective questions
          this.currentSubjectiveIndex = index;
        }
        break;
    }
  }

  getQuestionNumbers(): number[] {
    const total = this.getTotalQuestionsForSection();
    return Array.from({ length: total }, (_, i) => i);
  }
} 