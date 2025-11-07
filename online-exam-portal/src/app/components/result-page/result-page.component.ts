import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common'; // <-- Import isPlatformBrowser
import { MatCardModule } from '@angular/material/card';
import { Router } from '@angular/router';

@Component({
  selector: 'app-result-page',
  standalone: true,
  imports: [CommonModule, MatCardModule],
  templateUrl: './result-page.component.html',
  styleUrls: ['./result-page.component.scss']
})
export class ResultPageComponent implements OnInit {
  result: any;
  isAdmin: boolean = false;

  constructor(
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object // Inject PLATFORM_ID to check the environment
  ) {}

  ngOnInit(): void {
    // --- THIS IS THE SSR FIX ---
    // Only try to access sessionStorage if the code is running in a browser environment
    if (isPlatformBrowser(this.platformId)) {
      this.isAdmin = !!sessionStorage.getItem('adminAuthToken');
      const resultData = sessionStorage.getItem('lastResult');
      if (resultData) {
        const parsedData = JSON.parse(resultData);
        // The backend returns a nested structure, so we need to extract the actual result
        this.result = parsedData.result || parsedData;
        console.log('Result data loaded:', this.result);
      } else {
        // If there's no result data in the session, the user probably didn't
        // just finish an exam, so we redirect them to the login page.
        this.router.navigate(['/student-login']);
      }
    }
    // --------------------------
  }
}
