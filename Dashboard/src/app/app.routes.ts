import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { 
    path: 'dashboard', 
    loadChildren: () => import('./features/dashboard/dashboard.module').then(m => m.DashboardModule)
  },
  {
    path: 'panels',
    loadChildren: () => import('./features/panels/panels.module').then(m => m.PanelsModule)
  },
  {
    path: 'candidates',
    loadComponent: () => import('./features/candidates/candidates.component').then(m => m.CandidatesComponent)
  },
  { 
    path: 'job-profile', 
    loadComponent: () => import('./features/job-profile/job-profile.component').then(m => m.JobProfileComponent)
  },
  {
    path: 'interviews',
    loadComponent: () => import('./features/interviews/interviews.component').then(m => m.InterviewsComponent)
  },
  { path: '**', redirectTo: '/dashboard' }
];
