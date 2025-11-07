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
import { MatSelectModule } from '@angular/material/select';
import { MatToolbarModule } from '@angular/material/toolbar';
import { FooterComponent } from '../../shared/footer/footer.component';

@Component({
  selector: 'app-student-registration',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatCardModule, MatFormFieldModule,
    MatInputModule, MatButtonModule, MatProgressSpinnerModule, MatSelectModule, MatToolbarModule, FooterComponent
  ],
  templateUrl: './student-registration.component.html',
  styleUrls: ['./student-registration.component.scss']
})
export class StudentRegistrationComponent {
  registrationForm: FormGroup;
  errorMessage: string = '';
  isLoading: boolean = false;

  branches = [
    { value: 'cse', label: 'CSE' },
    { value: 'it', label: 'IT' },
    { value: 'csm', label: 'CSM' },
    { value: 'csd', label: 'CSD' },
    { value: 'ds', label: 'AIDS' },
    { value: 'mech', label: 'MECH' },
    { value: 'aiml', label: 'AIML' },
    { value: 'civil', label: 'CIVIL' },
    { value: 'ece', label: 'ECE' }
  ];

  genders = [
    { value: 'male', label: 'Male' },
    { value: 'female', label: 'Female' },
    { value: 'other', label: 'Other' }
  ];

  constructor(
    private fb: FormBuilder,
    private api: ApiService,
    private router: Router
  ) {
    this.registrationForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      hallTicketNumber: ['', [Validators.required, Validators.pattern(/^[A-Za-z0-9]+$/)]],
      email: ['', [Validators.required, Validators.email]],
      mobileNumber: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
      collegeName: ['', Validators.required],
      branch: ['', Validators.required],
      cgpa: ['', [Validators.required, Validators.min(0), Validators.max(10)]],
      dateOfBirth: ['', Validators.required],
      gender: ['', Validators.required],
      skills: ['', Validators.required]
    });
  }

  onSubmit() {
    if (this.registrationForm.invalid) return;
    this.isLoading = true;
    this.errorMessage = '';

    this.api.studentRegistration(this.registrationForm.value).subscribe({
      next: (res: any) => {
        alert('Registration successful! Thank you for applying.');
        this.router.navigate(['/student-login']);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Registration failed. Please try again.';
        this.isLoading = false;
      }
    });
  }
}
