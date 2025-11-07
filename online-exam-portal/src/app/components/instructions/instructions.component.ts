import { Component, Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-instructions',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatListModule, MatIconModule, MatSnackBarModule, MatCheckboxModule, FormsModule],
  templateUrl: './instructions.component.html',
  styleUrls: ['./instructions.component.scss']
})
export class InstructionsComponent {
  isCheckingPermissions = false;
  permissionsGranted = false;
  agreedToTerms = false;

  constructor(
    private router: Router,
    private snackBar: MatSnackBar,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  async checkPermissions(): Promise<boolean> {
    if (!isPlatformBrowser(this.platformId)) return false;

    try {
      // Check camera permission
      const cameraPermission = await navigator.permissions.query({ name: 'camera' as PermissionName });
      if (cameraPermission.state !== 'granted') {
        // Try to request camera access
        const stream = await navigator.mediaDevices.getUserMedia({ video: true });
        stream.getTracks().forEach(track => track.stop());
      }

      // Check microphone permission
      const micPermission = await navigator.permissions.query({ name: 'microphone' as PermissionName });
      if (micPermission.state !== 'granted') {
        // Try to request microphone access
        const audioStream = await navigator.mediaDevices.getUserMedia({ audio: true });
        audioStream.getTracks().forEach(track => track.stop());
      }

      return true;
    } catch (error) {
      console.error('Permission check failed:', error);
      return false;
    }
  }

  async startExam() {
    if (!isPlatformBrowser(this.platformId)) {
      this.router.navigate(['/exam']);
      return;
    }

    this.isCheckingPermissions = true;

    const permissionsGranted = await this.checkPermissions();

    if (permissionsGranted) {
      this.permissionsGranted = true;
      this.snackBar.open('Permissions granted. Starting exam...', 'Close', { duration: 2000 });
      setTimeout(() => {
        this.router.navigate(['/exam']);
      }, 1000);
    } else {
      this.permissionsGranted = false;
      this.snackBar.open('Camera and microphone permissions are required to start the exam.', 'Close', {
        duration: 5000,
        panelClass: 'error-snackbar'
      });
    }

    this.isCheckingPermissions = false;
  }
}
