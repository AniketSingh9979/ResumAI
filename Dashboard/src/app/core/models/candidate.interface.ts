export interface Candidate {
  id: string;
  name: string;
  email: string;
  phone?: string;
  resumeUrl?: string;
  skills: string[];
  experience: number;
  status: 'new' | 'in-progress' | 'interviewed' | 'selected' | 'rejected';
  appliedDate: Date;
}
