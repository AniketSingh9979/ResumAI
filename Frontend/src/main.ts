import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import { importProvidersFrom } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterModule } from '@angular/router';
import { InterviewLandingComponent } from './app/components/interview-landing/interview-landing.component';
import { InterviewTestPageComponent } from './app/components/interview-test-page/interview-test-page.component';
import { ThankYouComponent } from './app/components/thank-you/thank-you.component';

const routes = [
  { path: '', component: InterviewLandingComponent },
  { path: 'interview-test', component: InterviewTestPageComponent },
  { path: 'thank-you', component: ThankYouComponent },
  { path: '**', redirectTo: '' }
];

bootstrapApplication(AppComponent, {
  providers: [
    importProvidersFrom(
      ReactiveFormsModule,
      HttpClientModule,
      BrowserAnimationsModule,
      RouterModule.forRoot(routes)
    )
  ]
}).catch(err => console.error(err)); 