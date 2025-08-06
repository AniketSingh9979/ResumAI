import { Component, OnInit } from '@angular/core';
import { PanelService } from '../../shared/services/panel.service';

@Component({
  selector: 'app-panels',
  templateUrl: './panels.component.html',
  styleUrls: ['./panels.component.scss'],
  standalone: false
})
export class PanelsComponent implements OnInit {
  panelists: any[] = [];

  constructor(private panelService: PanelService) {}

  ngOnInit() {
    this.panelists = this.panelService.getPanels();
  }
}
