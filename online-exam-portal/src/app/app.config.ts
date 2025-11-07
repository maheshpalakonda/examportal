import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
// Add Monaco configuration
export const monacoConfig = {
  baseUrl: '/assets/monaco', // This must match your angular.json assets path
  defaultOptions: { 
    scrollBeyondLastLine: false,
    automaticLayout: true 
  }
};

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(withFetch()),
    provideAnimations(),
     // Add the configuration here
  ]
};