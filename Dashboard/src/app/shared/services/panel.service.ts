import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class PanelService {
  private panels = [
    {
      id: 1,
      name: 'Dr. Rajesh Kumar',
      expertise: 'Full Stack Development',
      experience: '8 years',
      availability: 'Morning Sessions',
      rating: 4.8,
      interviewsDone: 8,
      image: 'assets/avatars/avatar1.png'
    },
    {
      id: 2,
      name: 'Anjali Sharma',
      expertise: 'Backend Architecture',
      experience: '10 years',
      availability: 'Flexible Hours',
      rating: 4.9,
      interviewsDone: 10,
      image: 'assets/avatars/avatar2.png'
    },
    {
      id: 3,
      name: 'Arjun Patel',
      expertise: 'Frontend Development',
      experience: '6 years',
      availability: 'Evening Sessions',
      rating: 4.7,
      interviewsDone: 5,
      image: 'assets/avatars/avatar3.png'
    },
    {
      id: 4,
      name: 'Dr. Meera Verma',
      expertise: 'System Architecture',
      experience: '12 years',
      availability: 'Weekends Only',
      rating: 4.9,
      interviewsDone: 9,
      image: 'assets/avatars/avatar4.png'
    },
    {
      id: 5,
      name: 'Rahul Gupta',
      expertise: 'Cloud Computing',
      experience: '7 years',
      availability: 'Flexible Hours',
      rating: 4.8,
      interviewsDone: 7,
      image: 'assets/avatars/avatar5.png'
    }
  ];

  getPanels() {
    return this.panels;
  }

  getPanelCount() {
    return this.panels.length;
  }

  getTotalInterviews() {
    return this.panels.reduce((total, panel) => total + panel.interviewsDone, 0);
  }
}
