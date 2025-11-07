import { Routes } from '@angular/router';
import { StudentLoginComponent } from './components/student-login/student-login.component';
import { InstructionsComponent } from './components/instructions/instructions.component';
import { ExamPageComponent } from './components/exam-page/exam-page.component';
import { ResultPageComponent } from './components/result-page/result-page.component';
import { AdminLoginComponent } from './components/admin-login/admin-login.component';
import { AdminDashboardComponent } from './components/admin-dashboard/admin-dashboard.component';
import { StudentRegistrationComponent } from './components/student-registration/student-registration.component';
import { HomeComponent } from './components/home/home.component';
// import { AdminAuthGuard } from './guards/admin-auth.guard';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'student-login', component: StudentLoginComponent },
  { path: 'student-registration', component: StudentRegistrationComponent },
  { path: 'instructions', component: InstructionsComponent },
  { path: 'exam', component: ExamPageComponent },
  { path: 'result', component: ResultPageComponent },
  { path: 'admin-login', component: AdminLoginComponent },
  { path: 'admin-dashboard', component: AdminDashboardComponent},
  { path: '**', redirectTo: '' }
];
