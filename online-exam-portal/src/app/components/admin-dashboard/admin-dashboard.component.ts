import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { finalize } from 'rxjs/operators';
import { ApiService } from '../../services/api.service';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Exam } from '../../models/exam.model';
import { Section } from '../../models/section.model';

// --- Import all necessary Angular Material Modules ---
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSlideToggleChange, MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatCardModule } from '@angular/material/card';


type AdminTab = 'students' | 'questions' | 'exams' | 'sections' | 'results';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatSidenavModule, MatListModule, MatToolbarModule,
    MatTableModule, MatButtonModule, MatIconModule, MatSnackBarModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatCheckboxModule, MatSlideToggleModule, MatCardModule,
    FormsModule
  ],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss']
})
export class AdminDashboardComponent implements OnInit {
  activeTab: AdminTab = 'students';

  // Data Sources
  students: any[] = [];
  questions: any[] = [];
  exams: Exam[] = [];
  results: any[] = [];
  filteredResults: any[] = [];
  sections: Section[] = [];
  detailedResult: any = null;

  // Judge state for immediate code evaluation
  judging: boolean = false;
  judgeResult: {
    passed?: boolean;
    tests?: Array<{ input: string; expected: string; actual: string; passed: boolean }>;
    error?: string | null;
    language?: string;
    timeMs?: number;
  } | null = null;

  // Table Columns
  studentColumns: string[] = ['name', 'hallTicketNumber', 'email', 'branch', 'cgpa'];
  questionColumns: string[] = ['section', 'questionText', 'actions'];
  examColumns: string[] = ['examName', 'sections', 'isActive', 'actions'];
  resultColumns: string[] = ['studentName', 'studentEmail', 'examId', 'score', 'totalQuestions', 'examDate', 'actions'];
  sectionColumns: string[] = ['name', 'durationInMinutes', 'orderIndex', 'actions'];

  // Forms
  questionForm: FormGroup;
  examForm: FormGroup;
  sectionForm: FormGroup;
  

  // State
  isEditingQuestion = false;
  editingQuestionId: number | null = null;
  isEditingSection = false;
  editingSectionId: number | null = null;
  isEditingExam = false;
  editingExamId: number | null = null;
  isUpdating = false;
  selectedSections: Section[] = [];

  // Filter properties
  selectedExamId: number | null = null;
  showOnlyPassed: boolean = false;
  minTotalScore: number | null = null;
  sectionMinMarks: Map<number, number> = new Map();
  selectedExamSections: any[] = [];



  
constructor(
    private api: ApiService,
    private snackBar: MatSnackBar,
    private fb: FormBuilder,
    private router: Router
  ) {
this.questionForm = this.fb.group({
      questionText: ['', Validators.required],
      section: ['', Validators.required],
      option1: [''],
      option2: [''],
      option3: [''],
      option4: [''],
      correctAnswer: [''],
      boilerplateJava: [''],
      boilerplatePython: [''],
      boilerplateC: [''],
      boilerplateSql: [''],
      setupSql: [''],
      isCodingQuestion: [false],
      isSqlQuestion: [false],
      testCases: ['']
    });

    // Update validators based on selected section and coding checkbox
    this.questionForm.get('section')?.valueChanges.subscribe(section => {
      const isSectionCoding = !!section && section.name?.toLowerCase() === 'coding';
      const isSectionSql = !!section && section.name?.toLowerCase() === 'sql';
      // Auto-enable coding checkbox when section implies coding
      if (isSectionCoding && !this.questionForm.get('isCodingQuestion')?.value) {
        this.questionForm.get('isCodingQuestion')?.setValue(true, { emitEvent: true });
      }
      // Auto-enable SQL checkbox when section implies SQL
      if (isSectionSql && !this.questionForm.get('isSqlQuestion')?.value) {
        this.questionForm.get('isSqlQuestion')?.setValue(true, { emitEvent: true });
      }
      this.updateCodingValidators();
    });

    // React to the explicit coding checkbox as well
    this.questionForm.get('isCodingQuestion')?.valueChanges.subscribe((checked: boolean) => {
      this.updateCodingValidators();
      if (checked) {
        // Prefill boilerplate and empty test cases when toggled on
        const patch: any = {};
        if (!this.questionForm.get('boilerplateJava')?.value) patch.boilerplateJava = 'public class Solution {\n  public static void main(String[] args) {\n    // your code here\n  }\n}';
        if (!this.questionForm.get('boilerplatePython')?.value) patch.boilerplatePython = 'def solve():\n    pass\n\nif __name__ == "__main__":\n    solve()';
        if (!this.questionForm.get('boilerplateC')?.value) patch.boilerplateC = '#include <stdio.h>\nint main() {\n    // your code here\n    return 0;\n}';
        if (!this.questionForm.get('testCases')?.value) patch.testCases = '[]';
        if (Object.keys(patch).length) this.questionForm.patchValue(patch);
      }
    });

    // React to the explicit SQL checkbox as well
    this.questionForm.get('isSqlQuestion')?.valueChanges.subscribe((checked: boolean) => {
      this.updateCodingValidators();
      if (checked) {
        // Prefill boilerplate SQL and empty test cases when toggled on
        const patch: any = {};
        if (!this.questionForm.get('boilerplateSql')?.value) patch.boilerplateSql = '-- Write your SQL here';
        if (!this.questionForm.get('testCases')?.value) patch.testCases = '[]';
        if (Object.keys(patch).length) this.questionForm.patchValue(patch);
      }
    });

    this.examForm = this.fb.group({
      examName: ['', Validators.required]
    });

    this.sectionForm = this.fb.group({
      name: ['', Validators.required],
      durationInMinutes: [30, [Validators.required, Validators.min(1)]],
      orderIndex: [1, [Validators.required, Validators.min(1)]],
      marks: [10, [Validators.required, Validators.min(0)]],
      hasMinPassMarks: [false],
      minPassMarks: [0, Validators.min(0)],
      numQuestionsToSelect: [null]
    });

    
  }

  ngOnInit(): void {
    this.loadAllData();
  }

  // --- Data Loading ---
  loadAllData(): void {
    this.loadStudents();
    this.loadQuestions();
    this.loadExams();
    this.loadResults();
    this.loadSections();
  }

  loadStudents = () => this.api.getStudents().subscribe(data => this.students = data);
  loadQuestions = () => this.api.getAllQuestions().subscribe(data => {
    // Transform questions from backend format to frontend format
    this.questions = data.map(q => this.api.transformQuestionForFrontend(q));
    // Clear previous judge results when reloading questions
    this.judgeResult = null;
  });
  loadExams = () => this.api.getAllExams().subscribe(data => this.exams = data);
  loadResults = () => this.api.getAllResults().subscribe(data => {
    this.results = data;
    this.filteredResults = data;
  });
  loadSections = () => this.api.getAllSections().subscribe(data => this.sections = data.filter(s => !s.examId));
  // loadSections = () => this.api.getAllSections().subscribe(data => this.sections = data);
  //loadSections = () => this.api.getAllSections().subscribe(data => this.sections = data.filter(s => !s.examId));




  // --- Navigation & General ---
  setActiveTab(tab: AdminTab) {
    this.activeTab = tab;
  }

  logout(): void {
    if (typeof sessionStorage !== 'undefined') {
      sessionStorage.removeItem('adminAuthToken');
    }
    this.router.navigate(['/admin-login']);
  }



  // --- Question Management (CRUD) ---
  /**
   * Updated submitQuestion method to work with dynamic sections
   */
submitQuestion(): void {
    if (this.questionForm.invalid) {
      return;
    }

    const formValue = this.questionForm.getRawValue();
    const isCoding = !!formValue.isCodingQuestion || !!formValue.isSqlQuestion || (formValue.section && (formValue.section.name === 'Coding' || formValue.section.name === 'SQL'));

    // Validate testCases JSON if present and coding question
    if (isCoding && formValue.testCases) {
      try {
        JSON.parse(formValue.testCases);
      } catch (e) {
        this.snackBar.open('Test Cases must be valid JSON.', 'Close', { duration: 3000, panelClass: 'error-snackbar' });
        return;
      }
    }

    // Build a clean payload object to send to the backend
    const payload = {
      questionText: formValue.questionText,
      section: formValue.section,
      isCodingQuestion: isCoding,
      ...(isCoding ? {
        // For coding questions, include boilerplate, setup SQL (for SQL problems), and test cases
        boilerplateJava: formValue.boilerplateJava,
        boilerplatePython: formValue.boilerplatePython,
        boilerplateC: formValue.boilerplateC,
        boilerplateSql: formValue.boilerplateSql,
        setupSql: formValue.setupSql,
        testCases: formValue.testCases,
      } : {
        // For MCQ questions, include options and correct answer
        option1: formValue.option1,
        option2: formValue.option2,
        option3: formValue.option3,
        option4: formValue.option4,
        correctAnswer: formValue.correctAnswer,
      }),
    };

    const operation = this.isEditingQuestion && this.editingQuestionId
      ? this.api.updateQuestion(this.editingQuestionId, payload)
      : this.api.addQuestion(payload);

    operation.subscribe({
      next: () => {
        const message = this.isEditingQuestion ? 'Question updated successfully!' : 'Question added successfully!';
        this.snackBar.open(message, 'Close', { duration: 2500 });
        this.loadQuestions();
        this.cancelEdit();
      },
      error: (err) => {
        const serverMessage = err?.error?.message || (typeof err?.error === 'string' ? err.error : null) || err?.statusText || 'Could not save the question.';
        this.snackBar.open(`Error: ${serverMessage}`, 'Close', { duration: 4000 });
        console.error('Error saving question:', err);
      }
    });
  }

  // Immediately judge code against test cases
  async judgeSubmissionForQuestion(questionId: number, language: 'java' | 'python' | 'c' | 'sql', code: string) {
    if (!questionId || !language || !code || !code.trim()) {
      this.snackBar.open('Provide question, language, and code to run tests', 'Close', { duration: 3000 });
      return;
    }

    const body = {
      questionId: String(questionId),
      language,
      code
    } as any;

    try {
      this.judging = true;
      this.judgeResult = null;
      // Use ApiService if it has a generic POST; otherwise add a method for this endpoint.
      const anyApi = this.api as any;
      const res = await (anyApi.post ? anyApi.post('/api/compile/judge', body).toPromise() : Promise.reject('ApiService.post not available'));
      this.judging = false;

      if (!res) {
        this.snackBar.open('No response from judge', 'Close', { duration: 3000 });
        return;
      }

      this.judgeResult = res;
      if (res.passed === true) {
        this.snackBar.open('All tests passed', 'Close', { duration: 2500 });
      } else if (res.passed === false) {
        this.snackBar.open('Some tests failed', 'Close', { duration: 3000 });
        if (res.tests) { console.table(res.tests); }
      } else {
        this.snackBar.open('Judging completed', 'Close', { duration: 2500 });
      }
    } catch (e: any) {
      this.judging = false;
      const message = e?.error?.message || (typeof e?.error === 'string' ? e.error : null) || e?.statusText || 'Judging failed';
      console.error('Judging error:', e);
      this.snackBar.open(message, 'Close', { duration: 4000 });
    }
  }

  startEditQuestion(question: any): void {
    this.isEditingQuestion = true;
    this.editingQuestionId = question.id;
    const matchingSection = this.sections.find(s => s.id === question.section?.id);
    const derivedCoding = !!(question.section?.name && question.section.name.toLowerCase() === 'coding');
    const derivedSql = !!(question.section?.name && question.section.name.toLowerCase() === 'sql');
    this.questionForm.patchValue({
      questionText: question.questionText,
      section: matchingSection || question.section,
      option1: question.option1,
      option2: question.option2,
      option3: question.option3,
      option4: question.option4,
      correctAnswer: question.correctAnswer,
      boilerplateJava: question.boilerplateJava || question.boilerplateCode?.java || '',
      boilerplatePython: question.boilerplatePython || question.boilerplateCode?.python || '',
      boilerplateC: question.boilerplateC || question.boilerplateCode?.c || '',
      boilerplateSql: question.boilerplateSql || question.boilerplateCode?.sql || '',
      setupSql: question.setupSql || '',
      testCases: question.testCases || '',
      isCodingQuestion: question.isCodingQuestion || derivedCoding,
      isSqlQuestion: question.isSqlQuestion || derivedSql
    });
  }

  cancelEdit(): void {
    this.isEditingQuestion = false;
    this.editingQuestionId = null;
    this.questionForm.reset();
  }

  deleteQuestion(id: number): void {
    if (confirm('Are you sure you want to delete this question? This cannot be undone.')) {
      this.api.deleteQuestion(id).subscribe({
        next: () => {
          this.snackBar.open('Question deleted!', 'Close', { duration: 2000 });
          this.questions = this.questions.filter(q => q.id !== id);
        },
        error: (err) => {
          this.snackBar.open('Error: Could not delete the question.', 'Close', { duration: 3000 });
          console.error('Error deleting question:', err);
        }
      });
    }
  }

  private updateCodingValidators(): void {
    const section = this.questionForm.get('section')?.value;
    const isSectionCoding = !!section && section.name?.toLowerCase() === 'coding';
    const isSectionSql = !!section && section.name?.toLowerCase() === 'sql';
    const isCoding = !!this.questionForm.get('isCodingQuestion')?.value || !!this.questionForm.get('isSqlQuestion')?.value || isSectionCoding || isSectionSql;

    if (isCoding) {
      this.questionForm.get('option1')?.clearValidators();
      this.questionForm.get('option2')?.clearValidators();
      this.questionForm.get('correctAnswer')?.clearValidators();
    } else {
      this.questionForm.get('option1')?.setValidators(Validators.required);
      this.questionForm.get('option2')?.setValidators(Validators.required);
      this.questionForm.get('correctAnswer')?.setValidators(Validators.required);
    }

    this.questionForm.get('option1')?.updateValueAndValidity();
    this.questionForm.get('option2')?.updateValueAndValidity();
    this.questionForm.get('correctAnswer')?.updateValueAndValidity();
  }

  // --- Round (Exam) Management ---
  addExam(): void {
    if (this.examForm.invalid || this.selectedSections.length === 0) {
      this.snackBar.open('Please enter exam name and select at least one section', 'Close', { duration: 3000 });
      return;
    }

    // Transform selected sections to only include necessary data for creation
    const sectionsData = this.selectedSections.map(section => ({
      id: section.id, // <-- This is the crucial addition
      name: section.name, // Name is still useful for logging/debugging on backend
      durationInMinutes: section.durationInMinutes,
      orderIndex: section.orderIndex
    }));

    const examData = {
      examName: this.examForm.value.examName,
      sections: sectionsData
    };

    this.api.createExamWithSections(examData).subscribe({
      next: () => {
        this.snackBar.open('Round created successfully!', 'Close', { duration: 2000 });
        this.loadExams();
        this.examForm.reset();
        this.selectedSections = [];
      },
      error: (err: any) => {
        this.snackBar.open('Error creating round', 'Close', { duration: 3000 });
        console.error('Error creating exam:', err);
      }
    });
  }

  onSectionSelectionChange(event: any, section: Section): void {
    if (event.checked) {
      this.selectedSections.push(section);
    } else {
      this.selectedSections = this.selectedSections.filter(s => s.id !== section.id);
    }
  }

  editExamSections(exam: Exam): void {
    this.startEditExam(exam);
  }

  startEditExam(exam: Exam): void {
    this.isEditingExam = true;
    this.editingExamId = exam.id;
    this.examForm.patchValue({ examName: exam.examName });
    this.selectedSections = exam.sections ? [...exam.sections] : [];
  }

  cancelEditExam(): void {
    this.isEditingExam = false;
    this.editingExamId = null;
    this.examForm.reset();
    this.selectedSections = [];
  }

  deleteExam(exam: Exam): void {
    if (confirm('Are you sure you want to delete this round? This will permanently delete the round and all its sections and questions. This action cannot be undone.')) {
      this.api.deleteExam(exam.id).subscribe(() => {
        this.snackBar.open('Round deleted!', 'Close', { duration: 2000 });
        this.exams = this.exams.filter(e => e.id !== exam.id);
      
      });
    }
  }

  /**
   * Handles activating/deactivating a round with a confirmation dialog.
   * If the admin cancels, the slide toggle visually reverts to its previous state.
   */
  updateExamStatus(examToUpdate: Exam, event: MatSlideToggleChange): void {
    console.log('updateExamStatus called', examToUpdate.id, event.checked);
    if (this.isUpdating) return;

    const newStatus = event.checked;
    const actionText = newStatus ? 'activate' : 'deactivate';
    const warningText = newStatus ? '\n\nNote: Activating this round will deactivate all others.' : '';

    if (!confirm(`Are you sure you want to ${actionText} the round "${examToUpdate.examName}"?${warningText}`)) {
      event.source.checked = !newStatus; // Revert the visual toggle state
      return;
    }

    this.isUpdating = true;
    this.api.updateExam(examToUpdate.id, newStatus)
      .pipe(finalize(() => {
        this.isUpdating = false;
        this.loadExams(); // Always reload to reflect the true state
      }))
      .subscribe({
        next: () => {
          console.log('updateExam success');
          this.snackBar.open(`Round '${examToUpdate.examName}' status updated.`, 'Close', { duration: 3000 });
          this.loadExams(); // Reload exams to show the new state
        },
        error: (err) => {
          console.log('updateExam error', err);
          this.snackBar.open('Error: Could not update round status.', 'Close', { duration: 3000 });
        }
      });
  }

  // --- Section Management ---
  addSection(): void {
    if (this.sectionForm.invalid) return;
    
    const sectionData = this.sectionForm.value;
    this.api.createStandaloneSection(sectionData).subscribe({
      next: () => {
        this.snackBar.open('Section created successfully!', 'Close', { duration: 2000 });
        this.loadSections();
        this.sectionForm.reset({ durationInMinutes: 30, orderIndex: 1 });
      },
      error: (err) => {
        this.snackBar.open('Error: Could not create section.', 'Close', { duration: 3000 });
        console.error('Error creating section:', err);
      }
    });
  }

  startEditSection(section: Section): void {
    this.isEditingSection = true;
    this.editingSectionId = section.id!;
    this.sectionForm.patchValue({
      name: section.name,
      durationInMinutes: section.durationInMinutes,
      orderIndex: section.orderIndex,
      marks: section.marks,
      hasMinPassMarks: section.hasMinPassMarks,
      minPassMarks: section.minPassMarks,
      numQuestionsToSelect: section.numQuestionsToSelect
    });
  }

  updateSection(): void {
    if (this.sectionForm.invalid || !this.editingSectionId) return;

    const sectionData = this.sectionForm.value;
    this.api.updateStandaloneSection(this.editingSectionId, sectionData).subscribe({
      next: () => {
        this.snackBar.open('Section updated successfully!', 'Close', { duration: 2000 });
        this.loadSections();
        this.cancelSectionEdit();
      },
      error: (err) => {
        this.snackBar.open('Error: Could not update section.', 'Close', { duration: 3000 });
        console.error('Error updating section:', err);
      }
    });
  }

  cancelSectionEdit(): void {
    this.isEditingSection = false;
    this.editingSectionId = null;
    this.sectionForm.reset({ durationInMinutes: 30, orderIndex: 1, marks: 10, hasMinPassMarks: false, minPassMarks: 0, numQuestionsToSelect: null });
  }

  deleteSection(id: number): void {
    if (confirm('Are you sure you want to delete this section? This will also delete all questions in this section.')) {
      this.api.deleteStandaloneSection(id).subscribe({
        next: () => {
          this.snackBar.open('Section deleted!', 'Close', { duration: 2000 });
          this.loadSections();
        },
        error: (err) => {
          this.snackBar.open('Error: Could not delete section.', 'Close', { duration: 3000 });
          console.error('Error deleting section:', err);
        }
      });
    }
  }

  submitSection(): void {
    if (this.isEditingSection) {
      this.updateSection();
    } else {
      this.addSection();
    }
  }

  isSectionSelected(section: Section): boolean {
    return this.selectedSections.some(s => s.id === section.id);
  }

  submitExam(): void {
    if (this.isEditingExam) {
      this.updateExam();
    } else {
      this.addExam();
    }
  }

  updateExam(): void {
    if (!this.editingExamId || this.examForm.invalid || this.selectedSections.length === 0) {
      this.snackBar.open('Please enter exam name and select at least one section', 'Close', { duration: 3000 });
      return;
    }

    const examData = {
      examName: this.examForm.value.examName,
      sections: this.selectedSections.map(section => ({
        id: section.id,
        name: section.name,
        durationInMinutes: section.durationInMinutes,
        orderIndex: section.orderIndex
      }))
    };

    this.api.updateExamDetails(this.editingExamId, examData).subscribe({
      next: () => {
        this.snackBar.open('Round updated successfully!', 'Close', { duration: 2000 });
        this.loadExams();
        this.cancelEditExam();
      },
      error: (err) => {
        this.snackBar.open('Error updating round', 'Close', { duration: 3000 });
      }
    });
  }

  viewDetailedResult(result: any): void {
    this.api.getDetailedResult(result.id).subscribe({
      next: (data) => {
        this.detailedResult = data;
      },
      error: (err) => {
        this.snackBar.open('Error loading detailed result', 'Close', { duration: 3000 });
        console.error(err);
      }
    });
  }

  closeDetailedResult(): void {
    this.detailedResult = null;
  }

  // --- Result Filtering ---
  onExamChange(examId: number | null): void {
    this.selectedExamId = examId;
    if (examId) {
      const selectedExam = this.exams.find(e => e.id === examId);
      this.selectedExamSections = selectedExam ? selectedExam.sections || [] : [];
      this.sectionMinMarks.clear();
      this.selectedExamSections.forEach(section => {
        const defaultMin = section.hasMinPassMarks ? section.minPassMarks : 0;
        this.sectionMinMarks.set(section.id, defaultMin);
      });
    } else {
      this.selectedExamSections = [];
      this.sectionMinMarks.clear();
    }
    this.applyFilters();
  }

  applyFilters(): void {
    let filtered = this.results;

    if (this.selectedExamId) {
      filtered = filtered.filter(r => r.examId === this.selectedExamId);
    }

    if (this.showOnlyPassed) {
      filtered = filtered.filter(r => {
        if (!r.sectionResults) return false;
        return r.sectionResults.every((sr: any) => {
          const minMarks = this.sectionMinMarks.get(sr.sectionId) || (sr.hasMinPassMarks ? sr.minPassMarks : 0);
          return sr.score >= minMarks;
        });
      });
    }

    if (this.minTotalScore !== null && this.minTotalScore > 0) {
      filtered = filtered.filter(r => r.score >= this.minTotalScore!);
    }

    this.filteredResults = filtered;
  }

  clearFilters(): void {
    this.selectedExamId = null;
    this.showOnlyPassed = false;
    this.minTotalScore = null;
    this.sectionMinMarks.clear();
    this.selectedExamSections = [];
    this.filteredResults = this.results;
  }

  onSectionMinMarksChange(sectionId: number, value: string): void {
    this.sectionMinMarks.set(sectionId, parseInt(value, 10) || 0);
    this.applyFilters();
  }




}
