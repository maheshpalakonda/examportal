
// import { Injectable } from '@angular/core';
// import { CanActivate, Router } from '@angular/router';
// import { Inject, PLATFORM_ID } from '@angular/core';
// import { isPlatformBrowser } from '@angular/common';

// @Injectable({
//   providedIn: 'root'
// })
// export class AdminAuthGuard implements CanActivate {

//   constructor(
//     private router: Router,
//     @Inject(PLATFORM_ID) private platformId: Object
//   ) {}

//   canActivate(): boolean {
//     if (isPlatformBrowser(this.platformId)) {
//       const token = sessionStorage.getItem('adminAuthToken');
//       if (token) {
//         return true;
//       } else {
//         this.router.navigate(['/admin-login']);
//         return false;
//       }
//     }
//     return false; // For SSR, deny access
//   }
// }
