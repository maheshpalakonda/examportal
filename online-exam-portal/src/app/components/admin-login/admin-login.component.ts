import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';

// Import all necessary Angular Material Modules
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';


@Component({
  selector: 'app-admin-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './admin-login.component.html',
  styleUrls: ['./admin-login.component.scss']
})
export class AdminLoginComponent {
  loginForm: FormGroup;
  errorMessage: string = '';
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private api: ApiService
  ) {
    this.loginForm = this.fb.group({
      username: ['admin', Validators.required],
      password: ['', Validators.required]
    });
  }

  onSubmit() {
    if (this.loginForm.invalid) { return; }
    this.isLoading = true;
    this.errorMessage = '';

    const username = this.loginForm.value.username;
    const password = this.loginForm.value.password;
    
    // btoa() is a built-in browser function for Base64 encoding.
    // This creates the token that Spring Security's Basic Auth expects.
    const token = btoa(`${username}:${password}`);

    // --- THIS IS THE MOST IMPORTANT STEP ---
    // We save the token in the browser's session storage.
    sessionStorage.setItem('adminAuthToken', token);
    // ------------------------------------

    // Now, we test the token by trying to fetch a protected resource.
    // The ApiService will automatically read the token from sessionStorage.
    this.api.getAllExams().subscribe({
      next: () => {
        // If the API call succeeds, the token is valid. Navigate to the dashboard.
        this.router.navigate(['/admin-dashboard']);
      },
      error: (err) => {
        this.isLoading = false;
        if (err.status === 401) {
          this.errorMessage = "Invalid username or password.";
        } else {
          console.error("An unexpected admin login error occurred:", err);
          this.errorMessage = "An error occurred. Please check the server connection.";
        }
        // If there was any error, clear the bad token.
        sessionStorage.removeItem('adminAuthToken');
      }
    });
  }
}