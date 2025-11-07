import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Observable } from 'rxjs'; // <-- Import Observable if it's not already there
import { Exam } from '../models/exam.model'; // <-- Import the Exam interface
import { Section } from '../models/section.model';


@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private BASE_URL = '/api';

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  // --- Student Methods ---
  studentLogin(data: any) { return this.http.post(`${this.BASE_URL}/student/login`, data); }
  studentRegistration(data: any) { return this.http.post(`${this.BASE_URL}/student/register`, data); }
  getActiveExam() { return this.http.get(`${this.BASE_URL}/student/exam/active`); }
  submitResult(result: any) { return this.http.post(`${this.BASE_URL}/student/exam/submit`, result); }
  getStudents() { return this.http.get<any[]>(`${this.BASE_URL}/admin/students`, { headers: this.getAuthHeaders() }); }

  // --- Compiler/Execution Methods ---
  runCode(payload: any): Observable<any> {
    return this.http.post(`${this.BASE_URL}/compiler/run`, payload);
  }

  submitCode(payload: any): Observable<any> {
    return this.http.post(`${this.BASE_URL}/compiler/submit`, payload);
  }

  runSql(payload: any): Observable<any> {
    return this.http.post(`${this.BASE_URL}/run-sql`, payload);
  }

  // --- Admin Methods ---
  private getAuthHeaders(): HttpHeaders {
    let token: string | null = null;
    if (isPlatformBrowser(this.platformId)) {
      token = sessionStorage.getItem('adminAuthToken');
    }
    if (!token) {
      return new HttpHeaders();
    }
    return new HttpHeaders({ 'Authorization': 'Basic ' + token });
  }

  // Admin: Student Roster

  // Admin: Question Management
  addQuestion(q: any) {
    const transformedQuestion = this.transformQuestionForBackend(q);
    return this.http.post(`${this.BASE_URL}/admin/questions`, transformedQuestion, { headers: this.getAuthHeaders() });
  }

  getAllQuestions() {
    return this.http.get<any[]>(`${this.BASE_URL}/admin/questions`, { headers: this.getAuthHeaders() });
  }

  updateQuestion(id: number, q: any) {
    const transformedQuestion = this.transformQuestionForBackend(q);
    return this.http.put(`${this.BASE_URL}/admin/questions/${id}`, transformedQuestion, { headers: this.getAuthHeaders() });
  }

  deleteQuestion(id: number) { return this.http.delete(`${this.BASE_URL}/admin/questions/${id}`, { headers: this.getAuthHeaders() }); }

  // --- Admin: Exam Management (THIS IS THE CORRECTED SECTION) ---
  createExam(exam: any): Observable<Exam> {
    return this.http.post<Exam>(`${this.BASE_URL}/admin/exams`, exam, { headers: this.getAuthHeaders() });
  }

  createExamWithSections(examData: any): Observable<Exam> {
    return this.http.post<Exam>(`${this.BASE_URL}/admin/exams/with-sections`, examData, { headers: this.getAuthHeaders() });
  }

  getAllExams(): Observable<Exam[]> { // Returns an array of Exams
    return this.http.get<Exam[]>(`${this.BASE_URL}/admin/exams`, { headers: this.getAuthHeaders() });
  }

  updateExam(id: number, newStatus: boolean): Observable<Exam> {
    // We send an empty body {} and add the new status as a URL query parameter.
    return this.http.put<Exam>(`${this.BASE_URL}/admin/exams/${id}?isActive=${newStatus}`, {}, { headers: this.getAuthHeaders() });
  }

  updateExamDetails(id: number, examData: any): Observable<Exam> {
    return this.http.put<Exam>(`${this.BASE_URL}/admin/exams/${id}/details`, examData, { headers: this.getAuthHeaders() });
  }

  deleteExam(id: number): Observable<any> {
    return this.http.delete(`${this.BASE_URL}/admin/exams/${id}`, { headers: this.getAuthHeaders() });
  }

  // Admin: Results
  getAllResults() { return this.http.get<any[]>(`${this.BASE_URL}/admin/results`, { headers: this.getAuthHeaders() }); }
  getDetailedResult(id: number) { return this.http.get<any>(`${this.BASE_URL}/admin/results/${id}/details`, { headers: this.getAuthHeaders() }); }

  // --- Admin: Section Management ---

  // Standalone section management
  getAllSections(): Observable<Section[]> {
    return this.http.get<Section[]>(`${this.BASE_URL}/admin/sections`, { headers: this.getAuthHeaders() });
  }

  createStandaloneSection(section: Section): Observable<Section> {
    return this.http.post<Section>(`${this.BASE_URL}/admin/sections`, section, { headers: this.getAuthHeaders() });
  }

  updateStandaloneSection(sectionId: number, section: Section): Observable<Section> {
    return this.http.put<Section>(`${this.BASE_URL}/admin/sections/${sectionId}`, section, { headers: this.getAuthHeaders() });
  }

  deleteStandaloneSection(sectionId: number): Observable<any> {
    return this.http.delete(`${this.BASE_URL}/admin/sections/${sectionId}`, { headers: this.getAuthHeaders() });
  }

  // Exam-specific section management
  createSection(examId: number, section: any) {
    return this.http.post(`${this.BASE_URL}/admin/exams/${examId}/sections`, section, { headers: this.getAuthHeaders() });
  }

  getSectionsByExam(examId: number) {
    return this.http.get<any[]>(`${this.BASE_URL}/admin/exams/${examId}/sections`, { headers: this.getAuthHeaders() });
  }

  updateSection(examId: number, sectionId: number, section: any) {
    return this.http.put(`${this.BASE_URL}/admin/exams/${examId}/sections/${sectionId}`, section, { headers: this.getAuthHeaders() });
  }

  deleteSection(examId: number, sectionId: number) {
    return this.http.delete(`${this.BASE_URL}/admin/exams/${examId}/sections/${sectionId}`, { headers: this.getAuthHeaders() });
  }

  // --- Admin: Proctoring Management ---
  getLiveExamSessions(): Observable<any> {
    return this.http.get<any>(`${this.BASE_URL}/admin/proctoring/live-sessions`, { headers: this.getAuthHeaders() });
  }

  getRecentIncidents(): Observable<any> {
    return this.http.get<any>(`${this.BASE_URL}/admin/proctoring/incidents`, { headers: this.getAuthHeaders() });
  }

  terminateSession(data: any): Observable<any> {
    return this.http.post(`${this.BASE_URL}/admin/proctoring/terminate`, data, { headers: this.getAuthHeaders() });
  }

  getRecordings(studentEmail: string, examId: number): Observable<any> {
    return this.http.get<any>(`${this.BASE_URL}/admin/proctoring/recordings/${studentEmail}/${examId}`, { headers: this.getAuthHeaders() });
  }

  reportIncident(data: any): Observable<any> {
    return this.http.post(`${this.BASE_URL}/admin/proctoring/report-incident`, data, { headers: this.getAuthHeaders() });
  }

  // --- Data Transformation Methods ---

  /**
   * Transforms question data from frontend format to backend format
   * Frontend: boilerplateCode: { java: string, python: string, c: string }
   * Backend: boilerplateJava: string, boilerplatePython: string, boilerplateC: string
   */
  private transformQuestionForBackend(question: any): any {
    const transformed = { ...question };

    // Transform boilerplate code from nested object to individual fields
    if (question.boilerplateCode) {
      if (question.boilerplateCode.java !== undefined) {
        transformed.boilerplateJava = question.boilerplateCode.java;
      }
      if (question.boilerplateCode.python !== undefined) {
        transformed.boilerplatePython = question.boilerplateCode.python;
      }
      if (question.boilerplateCode.c !== undefined) {
        transformed.boilerplateC = question.boilerplateCode.c;
      }
      if (question.boilerplateCode.sql !== undefined) {
        transformed.boilerplateSql = question.boilerplateCode.sql;
      }
      // Remove the nested object as backend doesn't expect it
      delete transformed.boilerplateCode;
    }

    return transformed;
  }

  /**
   * Transforms question data from backend format to frontend format
   * Backend: boilerplateJava: string, boilerplatePython: string, boilerplateC: string
   * Frontend: boilerplateCode: { java: string, python: string, c: string }
   */
  transformQuestionForFrontend(question: any): any {
    const transformed = { ...question };

    // Transform boilerplate code from individual fields to nested object
    if (question.boilerplateJava !== undefined || question.boilerplatePython !== undefined ||
        question.boilerplateC !== undefined || question.boilerplateSql !== undefined) {

      transformed.boilerplateCode = {
        java: question.boilerplateJava || '',
        python: question.boilerplatePython || '',
        c: question.boilerplateC || '',
        sql: question.boilerplateSql || ''
      };

      // Remove individual fields as frontend expects nested object
      delete transformed.boilerplateJava;
      delete transformed.boilerplatePython;
      delete transformed.boilerplateC;
      delete transformed.boilerplateSql;
    }

    return transformed;
  }
}
