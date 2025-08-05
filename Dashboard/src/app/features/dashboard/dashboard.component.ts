import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { PanelService } from '../../shared/services/panel.service';

interface Activity {
  id: number;
  type: 'interview' | 'candidate' | 'feedback' | 'panel' | 'status';
  description: string;
  timestamp: string;
  icon: string;
  color: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: false,
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  panelCount: number = 0;
  totalInterviews: number = 0;
  recentActivities: Activity[] = [
    {
      id: 1,
      type: 'interview',
      description: 'Technical Interview scheduled for Sarah Johnson - Senior Developer position',
      timestamp: '10 minutes ago',
      icon: 'event',
      color: '#4CAF50'
    },
    {
      id: 2,
      type: 'candidate',
      description: 'New candidate added: Rajesh Kumar - Full Stack Developer',
      timestamp: '1 hour ago',
      icon: 'person_add',
      color: '#2196F3'
    },
    {
      id: 3,
      type: 'feedback',
      description: 'Interview feedback submitted by Arun Patel for Michael Chen',
      timestamp: '2 hours ago',
      icon: 'rate_review',
      color: '#9C27B0'
    },
    {
      id: 4,
      type: 'panel',
      description: 'New panel member added: Priya Sharma - UI/UX Expert',
      timestamp: '3 hours ago',
      icon: 'group_add',
      color: '#FF9800'
    },
    {
      id: 5,
      type: 'status',
      description: 'Status updated: David Wilson cleared technical round',
      timestamp: '4 hours ago',
      icon: 'check_circle',
      color: '#00BCD4'
    }
  ];

  constructor(private panelService: PanelService) {}

  ngOnInit() {
    this.panelCount = this.panelService.getPanelCount();
    this.totalInterviews = this.panelService.getTotalInterviews();
  }

}

