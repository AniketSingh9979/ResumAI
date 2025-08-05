import { Component, OnInit } from '@angular/core';
import { Candidate } from '../../core/models/candidate.interface';
import { Interview } from '../../core/models/interview.interface';
import { Panel } from '../../core/models/panel.interface';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  candidates: Candidate[] = [];
  interviews: Interview[] = [];
  panels: Panel[] = [];
  dateFilter: Date | null = null;
  skillFilter: string = '';
  statusFilter: string = '';

  constructor() {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  private loadDashboardData(): void {
    // TODO: Implement API calls to fetch data
  }

  applyFilters(): void {
    // TODO: Implement filtering logic
  }
}

