import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

import { PanelsComponent } from './panels.component';

const routes: Routes = [
  { path: '', component: PanelsComponent }
];

@NgModule({
  declarations: [
    PanelsComponent
  ],
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    RouterModule.forChild(routes)
  ],
  exports: [
    PanelsComponent
  ]
})
export class PanelsModule { }
