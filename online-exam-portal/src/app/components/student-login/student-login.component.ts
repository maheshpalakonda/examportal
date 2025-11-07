import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { FooterComponent } from '../../shared/footer/footer.component';

@Component({
  selector: 'app-student-login',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatCardModule, MatFormFieldModule,
    MatInputModule, MatButtonModule, MatProgressSpinnerModule, FooterComponent
  ],
  templateUrl: './student-login.component.html',
  styleUrls: ['./student-login.component.scss']
})
export class StudentLoginComponent {
  loginForm: FormGroup;
  errorMessage: string = '';
  isLoading: boolean = false;

  constructor(
    private fb: FormBuilder,
    private api: ApiService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      hallTicketNumber: ['', Validators.required]
    });
  }

  onSubmit() {
    if (this.loginForm.invalid) return;
    this.isLoading = true;
    this.errorMessage = '';

    this.api.studentLogin(this.loginForm.value).subscribe({
      next: (res: any) => {
        sessionStorage.setItem('studentEmail', this.loginForm.value.email);
        if (res.alreadySubmitted) {
          this.router.navigate(['/result']);
        } else {
          this.router.navigate(['/instructions']);
        }
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Login failed. Please check credentials.';
        this.isLoading = false;
      }
    });
  }
}
