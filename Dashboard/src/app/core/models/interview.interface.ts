import { Candidate } from './candidate.interface';
import { Panel } from './panel.interface';

export interface Interview {
  id: string;
  candidate: Candidate;
  panel: Panel;
  scheduledDate: Date;
  startTime: string;
  endTime: string;
  status: 'scheduled' | 'completed' | 'cancelled';
  feedback?: {
    technicalSkills: number;
    communicationSkills: number;
    problemSolving: number;
    comments: string;
  };
}
