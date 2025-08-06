import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { 
    path: 'dashboard', 
    loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
  },
  { 
    path: 'job-profile', 
    loadComponent: () => import('./features/job-profile/job-profile.component').then(m => m.JobProfileComponent)
  },
  { path: '**', redirectTo: '/dashboard' }
];
