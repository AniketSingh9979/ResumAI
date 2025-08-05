import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { PanelService } from '../../shared/services/panel.service';

@Component({
  selector: 'app-dashboard',
  standalone: false,
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  panelCount: number = 0;
  totalInterviews: number = 0;

  constructor(private panelService: PanelService) {}

  ngOnInit() {
    this.panelCount = this.panelService.getPanelCount();
    this.totalInterviews = this.panelService.getTotalInterviews();
  }

}

