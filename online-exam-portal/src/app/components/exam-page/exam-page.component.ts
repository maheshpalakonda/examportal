import {
  Component,
  ElementRef,
  HostListener,
  OnDestroy,
  OnInit,
  ViewChild,
  Inject,
  PLATFORM_ID,
  ChangeDetectorRef,
  NgZone
} from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient, HttpClientModule } from '@angular/common/http';

// --- New CodeMirror Component Import ---
import { CodemirrorEditorComponent } from '../codemirror-editor/codemirror-editor.component';

// Angular Material modules
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatTabsModule } from '@angular/material/tabs';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatRadioModule } from '@angular/material/radio';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

// RxJS for timeout handling
import { timeout } from 'rxjs/operators';
import { TimeoutError } from 'rxjs';

// face-api for proctoring
import * as faceapi from 'face-api.js';

// Layout service
import { LayoutService } from '../../services/layout.service';

@Component({
  selector: 'app-exam-page',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HttpClientModule,
    MatCardModule,
    MatButtonModule,
    MatTabsModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatRadioModule,
    MatGridListModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    CodemirrorEditorComponent
  ],
  templateUrl: './exam-page.component.html',
  styleUrls: ['./exam-page.component.scss']
})
export class ExamPageComponent implements OnInit, OnDestroy {
  // --- UI / data ---
  isLoading = true;
  exam: any = null;
  allQuestions: any[] = [];
  currentQuestionIndex = 0;

  // --- Section Timer Properties ---
  currentSectionTimeLeft: number = 0; // seconds for current section
  sectionTimerInterval: any;
  sectionWarningShown: boolean = false; // to show warning only once per section

  // Map of sectionId -> list of global question indices for that section (based on shuffled allQuestions)
  sectionQuestionIndices: { [key: number]: number[] } = {};
  // Track sections that were explicitly ended (optional use)
  endedSectionIds: Set<number> = new Set();

  // --- Section Navigation ---
  currentSectionIndex = 0;
  sections: any[] = [];
  sectionProgress: { [key: string]: { answered: number; total: number } } = {};

  // --- answers & editors ---
  mcqAnswers: { [key: number]: string } = {};
  codingAnswers: { [key: number]: { [lang: string]: string } } = {};
  submittedCodingAnswers: { [key: number]: { [lang: string]: string } } = {}; // Store the code that passed submission
  lastSelectedLanguage: { [key: number]: 'java' | 'python' | 'c' } = {};
  successfulRuns: { [key: number]: boolean } = {}; // New state to track successful runs/submissions
  codingPassed: { [key: number]: boolean } = {}; // Track which coding questions passed all test cases
  // --- SQL schema & results (for a professional SQL UI) ---
  sqlSchema: any[] = [];
  sqlSchemaLoaded: boolean = false;
  sqlResultHeaders: string[] = [];
  sqlResultRows: any[] = [];

  // --- answer locking ---
  lockedMcqAnswers: { [key: number]: boolean } = {};
  lockedCodingAnswers: { [key: number]: boolean } = {};

  // --- execution results & state ---
  executionResults: { [key: number]: string } = {};
  submissionResult: any = null;
  selectedLanguage: 'java' | 'python' | 'c' = 'java';
  programInput: string = '';
  isRunning: boolean = false;
  isSubmitting: boolean = false;
  isEndingSection: boolean = false;

  // --- proctoring ---
  @ViewChild('videoElement') videoElement?: ElementRef;
  warningCount = 0;
  maxWarnings = 5;
  private proctoringInterval: any;
  private stream: MediaStream | null = null;
  private audioContext: AudioContext | null = null;
  private analyser: AnalyserNode | null = null;
  private microphone: MediaStreamAudioSourceNode | null = null;
  private audioStream: MediaStream | null = null;
  voiceWarningCount = 0;
  maxVoiceWarnings = 3;
  private voiceDetectionInterval: any;

  constructor(
    private http: HttpClient,
    private router: Router,
    private snackBar: MatSnackBar,
    private cdRef: ChangeDetectorRef,
    @Inject(PLATFORM_ID) private platformId: Object,
    private layout: LayoutService
  ) {}

  get currentQuestion(): any {
    const question = this.allQuestions[this.currentQuestionIndex];
    return question;
  }

  // -------- lifecycle --------
  ngOnInit(): void {
    this.layout.setHideNavbar(true);
    if (isPlatformBrowser(this.platformId)) {
      this.restoreExamState(); // Restore saved state first
      this.startProctoringSetup();
      this.loadExamData();
    }
  }

  ngOnDestroy(): void {
    this.layout.setHideNavbar(false);
    clearInterval(this.proctoringInterval);
    clearInterval(this.sectionTimerInterval);
    clearInterval(this.voiceDetectionInterval);
    if (this.stream) {
      this.stream.getTracks().forEach(t => t.stop());
    }
    if (this.audioStream) {
      this.audioStream.getTracks().forEach(t => t.stop());
    }
    if (this.audioContext) {
      this.audioContext.close();
    }
  }

  // -------- proctoring --------
  // Removed reportIncident method and calls
  @HostListener('document:visibilitychange')
  onVisibilityChange(): void {
    if (document.hidden && isPlatformBrowser(this.platformId)) {
	this.snackBar.open('Tab switching detected. Exam terminated.', 'Close', { duration: 5000, panelClass: 'error-snackbar' });
        this.submitExam(true);
      // Commented out tab switching for testing admin panel
    }
  }

  // -------- Keyboard Navigation --------
  @HostListener('document:keydown', ['$event'])
  onKeyDown(event: KeyboardEvent): void {
    if (isPlatformBrowser(this.platformId)) {
      // Only handle keyboard shortcuts when exam is active and not loading
      if (this.isLoading || !this.exam) return;

      // Prevent default behavior for our custom shortcuts
      switch (event.key) {
        case 'ArrowRight':
          if (event.ctrlKey || event.metaKey) {
            event.preventDefault();
            this.nextSection();
          }
          break;
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
          if (event.altKey) {
            event.preventDefault();
            const sectionIndex = parseInt(event.key) - 1;
            if (sectionIndex >= 0 && sectionIndex < this.sections.length) {
              this.goToSection(sectionIndex);
            }
          }
          break;
      }
    }
  }

  async startProctoringSetup(): Promise<void> {
    try {
      await faceapi.nets.tinyFaceDetector.loadFromUri('/assets/models');
      this.stream = await navigator.mediaDevices.getUserMedia({ video: true });
      if (this.videoElement) {
        (this.videoElement.nativeElement as HTMLVideoElement).srcObject = this.stream;
        this.startFaceDetection();
      }
      // Start voice detection
      await this.startVoiceDetection();
    } catch (err) {
      console.error('Proctoring setup failed:', err);
      // Don't navigate away, just show warning and continue without proctoring
      this.snackBar.open('Camera/microphone access failed. Proctoring disabled.', 'Close', { duration: 3000 });
    }
  }

  startFaceDetection(): void {
    this.proctoringInterval = setInterval(async () => {
      if (!this.videoElement?.nativeElement) return;
      try {
        const detections = await faceapi.detectAllFaces(this.videoElement.nativeElement, new faceapi.TinyFaceDetectorOptions());
        if (detections.length === 0) {
          this.warningCount++;
          this.snackBar.open(`Warning ${this.warningCount}/${this.maxWarnings}: Face not detected!`, 'Close', { duration: 2000, panelClass: 'warn-snackbar' });

          // Check if we've reached the maximum warnings
          if (this.warningCount >= this.maxWarnings) {
            this.snackBar.open('Proctoring Violation: Maximum face detection warnings exceeded. Exam has been automatically submitted.', 'Close', { duration: 5000, panelClass: 'error-snackbar' });
            this.submitExam(true);
          }
        }
        // Removed reset of warning count to make it cumulative
      } catch (e) {
        console.error('Face detection error:', e);
      }
    }, 4000);
  }

  async startVoiceDetection(): Promise<void> {
    try {
      this.audioStream = await navigator.mediaDevices.getUserMedia({ audio: true });
      this.audioContext = new (window.AudioContext || (window as any).webkitAudioContext)();
      this.analyser = this.audioContext.createAnalyser();
      this.microphone = this.audioContext.createMediaStreamSource(this.audioStream);
      this.microphone.connect(this.analyser);
      this.analyser.fftSize = 256;
      const bufferLength = this.analyser.frequencyBinCount;
      const dataArray = new Uint8Array(bufferLength);

      this.voiceDetectionInterval = setInterval(() => {
        if (!this.analyser) return;
        this.analyser.getByteFrequencyData(dataArray);
        const average = dataArray.reduce((a, b) => a + b) / dataArray.length;
        // Medium sensitivity threshold for voice detection
        const maxValue = Math.max(...dataArray);
        if (average > 40 || maxValue > 150) { // Medium sensitivity threshold
          this.voiceWarningCount++;
          this.snackBar.open(`Proctoring Alert: Voice Detected (${this.voiceWarningCount}/${this.maxVoiceWarnings})`, 'Close', { duration: 2000, panelClass: 'warn-snackbar' });

          // Check if we've reached the maximum voice warnings
          if (this.voiceWarningCount >= this.maxVoiceWarnings) {
            this.snackBar.open('Proctoring Violation: Maximum voice detection warnings exceeded. Exam has been automatically submitted.', 'Close', { duration: 5000, panelClass: 'error-snackbar' });
            this.submitExam(true);
          }
        }
        // Removed reset of voice warning count to make it cumulative
      }, 2000);
    } catch (err) {
      console.error('Voice detection setup failed:', err);
      this.snackBar.open('Microphone access failed. Voice detection disabled.', 'Close', { duration: 3000 });
    }
  }



  // -------- session management --------
  updateSession(): void {
    if (!this.exam || !isPlatformBrowser(this.platformId)) return;

    const studentEmail = sessionStorage.getItem('studentEmail');
    if (!studentEmail) return;

    const currentSection = this.getCurrentSection();
    const payload = {
      studentEmail: studentEmail,
      examId: this.exam.id,
      currentSection: currentSection?.name || 'Unknown',
      timeRemaining: this.currentSectionTimeLeft
    };

    this.http.post('/api/student/exam/start-session', payload).subscribe({
      next: (response: any) => {
      },
      error: (err) => {
        console.error('Failed to update session (continuing exam):', err);
        // Do not show snackbar or navigate away during exam to avoid interrupting
      }
    });
  }

  // -------- session persistence --------
  private getExamStateKey(): string {
    const studentEmail = isPlatformBrowser(this.platformId) ? sessionStorage.getItem('studentEmail') : null;
    return `exam_state_${studentEmail}`;
  }

  saveExamState(): void {
    if (!isPlatformBrowser(this.platformId) || !this.exam) return;

    const state = {
      currentSectionIndex: this.currentSectionIndex,
      currentQuestionIndex: this.currentQuestionIndex,
      currentSectionTimeLeft: this.currentSectionTimeLeft,
      mcqAnswers: this.mcqAnswers,
      codingAnswers: this.codingAnswers,
      submittedCodingAnswers: this.submittedCodingAnswers,
      lastSelectedLanguage: this.lastSelectedLanguage,
      successfulRuns: this.successfulRuns,
      codingPassed: this.codingPassed,
      lockedMcqAnswers: this.lockedMcqAnswers,
      lockedCodingAnswers: this.lockedCodingAnswers,
      executionResults: this.executionResults,
      warningCount: this.warningCount,
      voiceWarningCount: this.voiceWarningCount,
      sectionWarningShown: this.sectionWarningShown,
      endedSectionIds: Array.from(this.endedSectionIds),
      selectedLanguage: this.selectedLanguage,
      programInput: this.programInput,
      sqlSchemaLoaded: this.sqlSchemaLoaded,
      sqlResultHeaders: this.sqlResultHeaders,
      sqlResultRows: this.sqlResultRows,
      timestamp: Date.now()
    };

    try {
      sessionStorage.setItem(this.getExamStateKey(), JSON.stringify(state));
      console.log('Exam state saved');
    } catch (e) {
      console.error('Failed to save exam state:', e);
    }
  }

  restoreExamState(): void {
    if (!isPlatformBrowser(this.platformId)) return;

    try {
      const savedState = sessionStorage.getItem(this.getExamStateKey());
      if (!savedState) return;

      const state = JSON.parse(savedState);

      // Only restore if state is recent (within last 24 hours)
      const age = Date.now() - (state.timestamp || 0);
      if (age > 24 * 60 * 60 * 1000) {
        console.log('Saved exam state is too old, ignoring');
        sessionStorage.removeItem(this.getExamStateKey());
        return;
      }

      // Restore state variables
      this.currentSectionIndex = state.currentSectionIndex || 0;
      this.currentQuestionIndex = state.currentQuestionIndex || 0;
      this.currentSectionTimeLeft = state.currentSectionTimeLeft || 0;
      this.mcqAnswers = state.mcqAnswers || {};
      this.codingAnswers = state.codingAnswers || {};
      this.submittedCodingAnswers = state.submittedCodingAnswers || {};
      this.lastSelectedLanguage = state.lastSelectedLanguage || {};
      this.successfulRuns = state.successfulRuns || {};
      this.codingPassed = state.codingPassed || {};
      this.lockedMcqAnswers = state.lockedMcqAnswers || {};
      this.lockedCodingAnswers = state.lockedCodingAnswers || {};
      this.executionResults = state.executionResults || {};
      this.warningCount = state.warningCount || 0;
      this.voiceWarningCount = state.voiceWarningCount || 0;
      this.sectionWarningShown = state.sectionWarningShown || false;
      this.endedSectionIds = new Set(state.endedSectionIds || []);
      this.selectedLanguage = state.selectedLanguage || 'java';
      this.programInput = state.programInput || '';
      this.sqlSchemaLoaded = state.sqlSchemaLoaded || false;
      this.sqlResultHeaders = state.sqlResultHeaders || [];
      this.sqlResultRows = state.sqlResultRows || [];

      console.log('Exam state restored from session');
    } catch (e) {
      console.error('Failed to restore exam state:', e);
      sessionStorage.removeItem(this.getExamStateKey());
    }
  }

  clearExamState(): void {
    if (isPlatformBrowser(this.platformId)) {
      sessionStorage.removeItem(this.getExamStateKey());
      console.log('Exam state cleared');
    }
  }

  // -------- exam data & initialization --------
  loadExamData(): void {
    console.log('Loading exam data...');
    this.isLoading = true;
    this.cdRef.detectChanges();

    this.http.get('/api/student/exam/active').subscribe({
      next: (res: any) => {

        // Validate response structure
        if (!res) {
          console.error('Empty response received from API');
          this.showError('No exam data received. Please try again.');
          return;
        }

        // Set exam data
        this.exam = res.exam || null;
        this.sections = res.exam?.sections || [];
        this.allQuestions = [];

        // Transform questions from backend format to frontend format
        if (res.questions && Array.isArray(res.questions)) {
          this.allQuestions = res.questions.map((q: any) => {
            let parsedTestCases: any[] = [];
            if (q.testCases) {
              try {
                parsedTestCases = JSON.parse(q.testCases);
              } catch (e) {
                console.warn('Invalid testCases JSON for question', q.id, e);
              }
            }

            // const isCoding = q.isCodingQuestion || (q.section && (q.section.name.toLowerCase() === 'coding' || q.section.name.toLowerCase() === 'sql'));
            // if (isCoding) {
            if (q.boilerplateJava !== undefined || q.boilerplatePython !== undefined ||
                q.boilerplateC !== undefined || q.boilerplateSql !== undefined) {
              return {
                ...q,
                boilerplateCode: {
                  java: q.boilerplateJava || '',
                  python: q.boilerplatePython || '',
                  c: q.boilerplateC || '',
                  sql: q.boilerplateSql || ''
                },
                parsedTestCases
              };
            }
            return { ...q, parsedTestCases };
          });
        }

        // Enrich questions inside sections so UI can access boilerplateCode and parsedTestCases
        if (this.sections && Array.isArray(this.sections)) {
          const byId: { [key: number]: any } = {};
          this.allQuestions.forEach((q: any) => { if (q && q.id != null) byId[q.id] = q; });
          this.sections.forEach((section: any) => {
            if (section && Array.isArray(section.questions)) {
              section.questions = section.questions.map((q: any) => {
                let parsedTc: any[] = [];
                if (q?.testCases) {
                  try {
                    parsedTc = JSON.parse(q.testCases);
                  } catch (e) {
                    console.warn('Invalid testCases JSON for section question', q?.id, e);
                  }
                }
                const enriched = byId[q?.id];
                return {
                  ...q,
                  // Provide minimal section meta so type detection works (Coding vs SQL)
                  section: q?.section ?? { id: section?.id, name: section?.name },
                  boilerplateCode: enriched?.boilerplateCode ?? {
                    java: q?.boilerplateJava || '',
                    python: q?.boilerplatePython || '',
                    c: q?.boilerplateC || '',
                    sql: q?.boilerplateSql || ''
                  },
                  parsedTestCases: enriched?.parsedTestCases ?? parsedTc
                };
              });
            }
          });
        }

        console.log('All questions loaded:', this.allQuestions.length);
        console.log('Sections loaded:', this.sections.length);

        // Validate exam data
        if (!this.exam) {
          console.error('No exam data found');
          this.showError('No active exam found. Please contact administrator.');
          return;
        }

        // Shuffle questions for randomization
        this.shuffleQuestions();

        // Initialize coding answers (merge with restored state)
        this.initializeCodingAnswers();

        // Initialize sections from backend data
        this.initializeSectionsFromBackend();

        // Restore section and question position if available, otherwise set initial
        if (this.sections.length > 0) {
          console.log('Using sections from backend:', this.sections.length);

          // Try to restore previous position, but validate it exists
          let validSectionIndex = 0;
          let validQuestionIndex = 0;

          if (this.currentSectionIndex >= 0 && this.currentSectionIndex < this.sections.length) {
            validSectionIndex = this.currentSectionIndex;
            const section = this.sections[validSectionIndex];
            if (section && section.id != null) {
              const indices = this.sectionQuestionIndices[section.id] || [];
              if (indices.includes(this.currentQuestionIndex)) {
                validQuestionIndex = this.currentQuestionIndex;
              } else if (indices.length > 0) {
                validQuestionIndex = indices[0];
              }
            }
          }

          this.currentSectionIndex = validSectionIndex;
          this.selectQuestion(validQuestionIndex);
        } else if (this.allQuestions.length > 0) {
          console.log('No sections found, creating fallback section');
          // Fallback: If no sections but we have questions, create a default section
          this.sections.push({
            name: 'Questions',
            questions: this.allQuestions,
            startIndex: 0,
            endIndex: this.allQuestions.length - 1
          });
          this.currentSectionIndex = 0;
          this.selectQuestion(this.currentQuestionIndex || 0);
        } else {
          console.error('No sections or questions found');
          this.showError('No questions available for this exam. Please contact administrator.');
          return;
        }

        this.isLoading = false;
        // Start only the per-section timer (global timer removed)
        this.startSectionTimer();
        // Start session tracking
        this.updateSession();
        this.cdRef.detectChanges();
        console.log('Exam data loaded successfully');
      },
      error: (err) => {
        console.error('Error loading exam:', err);
        this.isLoading = false;
        this.showError(err?.error?.message || 'Could not load active exam. Please try again.');
      }
    });
  }

  private showError(message: string): void {
    console.error('Exam Error:', message);
    this.snackBar.open(message, 'Close', {
      duration: 5000,
      panelClass: 'error-snackbar'
    });
    // Optionally redirect to login after a delay
    setTimeout(() => {
      this.router.navigate(['/student-login']);
    }, 3000);
  }

  initializeCodingAnswers(): void {
    this.allQuestions.forEach(q => {
      // Check if it's a coding question using section information or category
      const sectionName = q.section?.name?.toLowerCase() || q.category?.toLowerCase();
      const isCoding = q.isCodingQuestion || ['coding', 'sql'].includes(sectionName);
      if (!isCoding) return;

      if (!this.codingAnswers[q.id]) this.codingAnswers[q.id] = {};

      if (sectionName === 'sql' || q.category === 'SQL') {
        this.codingAnswers[q.id]['sql'] = this.codingAnswers[q.id]['sql'] ?? q.boilerplateCode?.sql ?? q.boilerplateSql ?? '';
      } else {
        // Correctly access the language-specific boilerplate code from the boilerplateCode object. Fallback to raw backend fields.
        this.codingAnswers[q.id]['java'] = this.codingAnswers[q.id]['java'] ?? q.boilerplateCode?.java ?? q.boilerplateJava ?? '';
        this.codingAnswers[q.id]['python'] = this.codingAnswers[q.id]['python'] ?? q.boilerplateCode?.python ?? q.boilerplatePython ?? '';
        this.codingAnswers[q.id]['c'] = this.codingAnswers[q.id]['c'] ?? q.boilerplateCode?.c ?? q.boilerplateC ?? '';
      }
      this.executionResults[q.id] = ''; // Initialize execution results
    });
  }

  // -------- timer & navigation --------
  // Start or reset the timer for the current section
  startSectionTimer(): void {
    clearInterval(this.sectionTimerInterval);
    this.sectionWarningShown = false; // reset warning flag for new section
    const currentSection = this.getCurrentSection();
    const durationMins = currentSection?.durationInMinutes ?? 0;
    // Fallback to 30 mins if not configured
    this.currentSectionTimeLeft = (durationMins > 0 ? durationMins : 30) * 60;

    this.sectionTimerInterval = setInterval(() => {
      if (this.currentSectionTimeLeft > 0) {
        this.currentSectionTimeLeft--;
        if (this.currentSectionTimeLeft <= 10 && !this.sectionWarningShown) {
          this.snackBar.open('Warning: Only 10 seconds left for this section!', 'Close', { duration: 10000, panelClass: 'warn-snackbar' });
          this.sectionWarningShown = true;
        }
        // Update session every minute
        if (this.currentSectionTimeLeft % 60 === 0) {
          this.updateSession();
        }
        this.cdRef.detectChanges();
      } else {
        clearInterval(this.sectionTimerInterval);
        const nextSection = this.sections[this.currentSectionIndex + 1];
        if (nextSection) {
          let countdown = 5;
          const showCountdown = () => {
            if (countdown > 0) {
              this.snackBar.open(`Section "${currentSection?.name ?? ''}" time is up! Moving to "${nextSection?.name ?? ''}" in ${countdown} seconds...`, 'Close', { duration: 1000 });
              countdown--;
              setTimeout(showCountdown, 1000);
            } else {
              this.endSection(true);
            }
          };
          showCountdown();
        } else {
          this.snackBar.open(`Section "${currentSection?.name ?? ''}" time is up! Submitting exam...`, 'Close', { duration: 3000 });
          setTimeout(() => {
            this.endSection(true);
          }, 3000);
        }
      }
    }, 1000);
  }

  // End the current section and move to the next (or submit on last section)
  endSection(auto: boolean = false): void {
    if (this.isEndingSection) return; // Prevent multiple clicks
    this.isEndingSection = true;
    const section = this.getCurrentSection();
    if (section?.id != null) {
      this.endedSectionIds.add(section.id);
    }
    clearInterval(this.sectionTimerInterval);

    if (this.currentSectionIndex < this.sections.length - 1) {
      // Check pass mark before moving to next section
      if (!this.checkSectionPass()) {
        return;
      }
      const nextSection = this.sections[this.currentSectionIndex + 1];
      let countdown = 5;
      const showCountdown = () => {
        if (countdown > 0) {
          this.snackBar.open(`Moving to next section: "${nextSection?.name ?? ''}". Starting in ${countdown} seconds...`, 'Close', { duration: 1000 });
          countdown--;
          setTimeout(showCountdown, 1000);
        } else {
          this.currentSectionIndex++;
          this.goToSection(this.currentSectionIndex);
          // Update session immediately when moving to new section
          this.updateSession();
        }
      };
      showCountdown();
    } else {
      // Last section ended -> submit exam automatically
      if (auto) {
        this.submitExam(false);
      } else {
        this.snackBar.open('All sections completed. Submitting your exam.', 'Close', { duration: 3000 });
        this.submitExam(false);
      }
    }
  }

  formatTime(seconds: number): string {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
  }

  selectQuestion(index: number): void {
    this.currentQuestionIndex = index;
    const question = this.currentQuestion;
    const questionType = this.getQuestionType(question);

    if (questionType === 'SQL') {
      this.selectedLanguage = 'sql' as any; // Cast to 'sql'
      if (!this.sqlSchemaLoaded) {
        this.loadSqlSchema();
      }
    } else if (questionType === 'Coding') {
      // Restore the last used language for this question, or default to 'java'
      this.selectedLanguage = this.lastSelectedLanguage[question.id] || 'java';
    }

    this.programInput = ''; // Clear program input when changing questions
    this.submissionResult = null; // Clear previous submission result
    this.saveExamState(); // Save state after question change
    this.cdRef.detectChanges(); // Manually trigger change detection
  }

  // -------- Section Navigation Methods --------
  initializeSectionsFromBackend(): void {
    console.log('Initializing sections from backend data...');

    // Keep sections from backend as-is (they already contain their questions),
    // and build a mapping from section -> indices of its questions within the shuffled allQuestions list.
    const idToIndex: { [id: number]: number } = {};
    this.allQuestions.forEach((q, idx) => {
      idToIndex[q.id] = idx;
    });

    this.sectionQuestionIndices = {};
    this.sections.forEach((section: any) => {
      const indices: number[] = [];
      const sQuestions: any[] = Array.isArray(section.questions) ? section.questions : [];
      sQuestions.forEach((q: any) => {
        const idx = idToIndex[q.id];
        if (idx !== undefined) {
          indices.push(idx);
        }
      });
      // Sort indices by their order in allQuestions to make in-section navigation consistent
      indices.sort((a, b) => a - b);
      if (section.id != null) {
        this.sectionQuestionIndices[section.id] = indices;
      }
    });

    console.log('Sections initialized:', this.sections.length);

    // Update section progress
    this.updateSectionProgress();
  }

  updateSectionProgress(): void {
    this.sections.forEach(section => {
      const answeredCount = section.questions.filter((q: any) => this.isQuestionAnswered(q)).length;
      this.sectionProgress[section.name] = {
        answered: answeredCount,
        total: section.questions.length
      };
    });
  }

  nextSection(): void {
    if (this.currentSectionIndex < this.sections.length - 1 && this.isCurrentSectionComplete()) {
      if (!this.checkSectionPass()) {
        return;
      }
      this.currentSectionIndex++;
      this.goToSection(this.currentSectionIndex);
    }
  }



  goToSection(sectionIndex: number): void {
    if (sectionIndex >= 0 && sectionIndex < this.sections.length) {
      this.currentSectionIndex = sectionIndex;
      const section = this.sections[sectionIndex];
      if (section && Array.isArray(section.questions) && section.questions.length > 0) {
        // Go to the first question of this section using mapped indices
        const mapped = section.id != null ? this.sectionQuestionIndices[section.id] : [];
        if (mapped && mapped.length > 0) {
          this.selectQuestion(mapped[0]);
        }
      }
      // Reset and start the section timer when entering a section
      this.startSectionTimer();
      this.isEndingSection = false; // Reset the flag
      this.saveExamState(); // Save state after section change
    }
  }

  getCurrentSection(): any {
    const section = this.sections[this.currentSectionIndex];
    return section;
  }

  getCurrentSectionProgress(): { answered: number; total: number } {
    const currentSection = this.getCurrentSection();
    return currentSection ? this.sectionProgress[currentSection.name] || { answered: 0, total: 0 } : { answered: 0, total: 0 };
  }

  isCurrentSectionComplete(): boolean {
    const progress = this.getCurrentSectionProgress();
    return progress.answered === progress.total && progress.total > 0;
  }

  canGoToNextSection(): boolean {
    return this.currentSectionIndex < this.sections.length - 1 && this.isCurrentSectionComplete();
  }



  // -------- Question Navigation within Section --------
  nextQuestion(): void {
    const currentSection = this.getCurrentSection();
    if (!currentSection) return;
    const indices = currentSection.id != null ? (this.sectionQuestionIndices[currentSection.id] || []) : [];
    const pos = indices.indexOf(this.currentQuestionIndex);
    if (pos >= 0 && pos < indices.length - 1) {
      this.selectQuestion(indices[pos + 1]);
    }
  }

  previousQuestion(): void {
    const currentSection = this.getCurrentSection();
    if (!currentSection) return;
    const indices = currentSection.id != null ? (this.sectionQuestionIndices[currentSection.id] || []) : [];
    const pos = indices.indexOf(this.currentQuestionIndex);
    if (pos > 0) {
      this.selectQuestion(indices[pos - 1]);
    }
  }

  canGoToNextQuestion(): boolean {
    const currentSection = this.getCurrentSection();
    if (!currentSection) return false;
    const indices = currentSection.id != null ? (this.sectionQuestionIndices[currentSection.id] || []) : [];
    const pos = indices.indexOf(this.currentQuestionIndex);
    return pos >= 0 && pos < indices.length - 1;
  }

  canGoToPreviousQuestion(): boolean {
    const currentSection = this.getCurrentSection();
    if (!currentSection) return false;
    const indices = currentSection.id != null ? (this.sectionQuestionIndices[currentSection.id] || []) : [];
    const pos = indices.indexOf(this.currentQuestionIndex);
    return pos > 0;
  }

  // -------- Auto-Advance Feature --------
  checkAutoAdvance(): void {
    // Only auto-advance if the feature is desired and current section is complete
    if (this.isCurrentSectionComplete() && this.canGoToNextSection()) {
      // Add a small delay to let the user see the completion
      setTimeout(() => {
        this.snackBar.open(`Section "${this.getCurrentSection()?.name}" completed! Moving to next section.`, 'Close', {
          duration: 2000,
          panelClass: 'success-snackbar'
        });
        this.nextSection();
      }, 1000);
    }
  }

  // -------- answers & helpers --------
  isQuestionAnswered(question: any): boolean {
    if (!question) { return false; }
    if (this.getQuestionType(question) === 'MCQ') {
      return !!this.mcqAnswers[question.id];
    }
    // A coding question is considered answered if it has been successfully run or submitted.
    return this.successfulRuns[question.id] === true;
  }

  handleMcqAnswer(questionId: number, option: string): void {
    this.mcqAnswers[questionId] = option;
    this.lockedMcqAnswers[questionId] = true; // Lock the answer once selected
    this.updateSectionProgress();
    this.saveExamState(); // Save state after answer change
    this.checkAutoAdvance();
  }

  onCodeChange(language: 'java' | 'python' | 'c' | 'sql', newCode: string) {
    if (this.currentQuestion) {
      this.codingAnswers[this.currentQuestion.id][language] = newCode;
      this.saveExamState(); // Save state after code change
    }
  }

  onTabChangeTabLabel(event: any): void {
    const label = event?.tab?.textLabel?.toLowerCase();
    if (label === 'java' || label === 'python' || label === 'c') {
      this.selectedLanguage = label;
      // Store the newly selected language for the current question
      if (this.currentQuestion) {
        this.lastSelectedLanguage[this.currentQuestion.id] = label;
        this.saveExamState(); // Save state after language change
      }
    }
  }

  // -------- Code Execution & Submission --------

  private handleExecutionError(questionId: number, err: any, type: 'run' | 'submit' | 'sql'): void {
    this.isRunning = false;
    let message: string;
    let timeoutSeconds: number;

    switch (type) {
      case 'run': timeoutSeconds = 20; break;
      case 'submit': timeoutSeconds = 65; break;
      case 'sql': timeoutSeconds = 20; break;
    }

    if (err instanceof TimeoutError) {
      message = `Client-side Error: Request timed out after ${timeoutSeconds} seconds. The server might be taking too long.`;
    } else {
      const serverMessage = err?.error?.message ?? `${type.charAt(0).toUpperCase() + type.slice(1)} failed!`;
      if (serverMessage.includes('Execution timed out')) {
        message = 'Execution timed out. This can happen if your code has an infinite loop or is too slow. Please review your logic.';
      } else if (serverMessage.includes('NoSuchElementException')) {
        message = 'Execution Error: Your code expects input, but none was provided. Please enter some input in the \'Program Input (stdin)\' box and try again.';
      } else if (type === 'sql' && serverMessage.includes('Query timed out')) {
        message = 'Query timed out. Your SQL query is taking too long to execute, which might indicate an inefficient query.';
      } else {
        message = serverMessage;
      }
    }

    this.executionResults[questionId] = message;
    if (type === 'submit') {
      this.submissionResult = { passed: false, message };
    }

    this.snackBar.open(message, 'Close', { duration: 5000, panelClass: 'error-snackbar' });
    this.cdRef.detectChanges();
  }

  runCode(questionId: number, lang: 'java' | 'python' | 'c'): void {
    const code = this.codingAnswers[questionId]?.[lang];
    if (!code) {
      this.snackBar.open('Please enter some code to run.', 'Close', { duration: 2000 });
      return;
    }
    this.isRunning = true;
    this.submissionResult = null;
    this.executionResults[questionId] = 'Running...';
    const payload = { language: lang, code, stdin: this.programInput || '' };
    this.http.post('/api/compiler/run', payload).pipe(
      // Timeout should be slightly longer than the server's timeout (15s).
      timeout(20000)
    ).subscribe({
      next: (res: any) => {
        this.isRunning = false;
        this.executionResults[questionId] = res?.output ?? JSON.stringify(res, null, 2);
        // Do not mark as successful run here; only mark after submission passes test cases
        this.cdRef.detectChanges();
      },
      error: (err) => this.handleExecutionError(questionId, err, 'run')
    });
  }

  // This method is for submitting to judge (e.g., running against hidden test cases)
submitCode(questionId: number, lang: 'java' | 'python' | 'c'): void {
    const code = this.codingAnswers[questionId]?.[lang];
    if (!code) {
      this.snackBar.open('Please enter some code to submit.', 'Close', { duration: 2000 });
      return;
    }
    this.isRunning = true;
    this.submissionResult = null;
    this.executionResults[questionId] = 'Submitting...';

    // The backend retrieves test cases from the database, so we only need to send the question ID, language, and code.
    const payload = { questionId: questionId.toString(), language: lang, code };
    this.http.post('/api/compiler/submit', payload).pipe(
      // A generous timeout for running against multiple test cases.
      // The backend has a 2s timeout per test case.
      timeout(65000)
    ).subscribe({
      next: (res: any) => {
        this.isRunning = false;
        this.submissionResult = res; // This should contain pass/fail info from the judge
        this.executionResults[questionId] = res.message; // Message from the judge
        // If the submission passes all test cases, mark the question as answered and passed.
        if (res.passed) {
          this.successfulRuns[questionId] = true;
          this.codingPassed[questionId] = true;
          // Store the submitted code
          if (!this.submittedCodingAnswers[questionId]) {
            this.submittedCodingAnswers[questionId] = {};
          }
          this.submittedCodingAnswers[questionId][lang] = code;
          this.updateSectionProgress();
          this.saveExamState(); // Save state after successful submission
          // Removed auto-advance for coding questions
        }
        this.snackBar.open(res.message, 'Close', { duration: 5000, panelClass: res.passed ? 'success-snackbar' : 'error-snackbar' });
        this.cdRef.detectChanges();
      },
      error: (err) => this.handleExecutionError(questionId, err, 'submit')
    });
  }

  runSql(questionId: number): void {
    const sql = this.codingAnswers[questionId]?.['sql'];
    if (!sql) {
      this.snackBar.open('Please enter some SQL to run.', 'Close', { duration: 2000 });
      return;
    }
    this.isRunning = true;
    this.submissionResult = null;
    this.executionResults[questionId] = 'Running SQL...';
    this.http.post('/api/run-sql', { query: sql, questionId: String(questionId) }).pipe(
      timeout(20000) // Increased to 20s
    ).subscribe({
      next: (res: any) => {
        this.isRunning = false;
        this.executionResults[questionId] = JSON.stringify(res.rows || res, null, 2);
        // Prepare a table-friendly structure for a professional UI
        const rows = res?.rows || (Array.isArray(res) ? res : []);
        this.sqlResultRows = Array.isArray(rows) ? rows : [];
        this.sqlResultHeaders = this.sqlResultRows.length ? Object.keys(this.sqlResultRows[0]) : [];
        // Do not mark as successful run here; only mark after submission passes test cases
        this.cdRef.detectChanges();
      },
      error: (err) => this.handleExecutionError(questionId, err, 'sql')
    });
  }

  // -------- SQL helpers (schema + submit) --------
  loadSqlSchema(): void {
    if (this.sqlSchemaLoaded) return;
    this.http.get('/api/sql/schema').subscribe({
      next: (res: any) => {
        this.sqlSchema = res?.tables || [];
        this.sqlSchemaLoaded = true;
        this.cdRef.detectChanges();
      },
      error: (err) => {
        console.error('Error loading SQL schema:', err);
      }
    });
  }

  submitSql(questionId: number): void {
    const sql = this.codingAnswers[questionId]?.['sql'];
    if (!sql) {
      this.snackBar.open('Please enter a SQL query to submit.', 'Close', { duration: 2000 });
      return;
    }
    this.isRunning = true;
    this.submissionResult = null;
    this.executionResults[questionId] = 'Submitting SQL...';
    this.http.post('/api/sql/submit', { questionId: String(questionId), query: sql }).pipe(
      timeout(20000)
    ).subscribe({
      next: (res: any) => {
        this.isRunning = false;
        this.submissionResult = res;
        this.executionResults[questionId] = res.message || '';
        if (res.passed) {
          this.successfulRuns[questionId] = true;
          // Store the submitted SQL
          if (!this.submittedCodingAnswers[questionId]) {
            this.submittedCodingAnswers[questionId] = {};
          }
          this.submittedCodingAnswers[questionId]['sql'] = sql;
          this.updateSectionProgress();
          this.saveExamState(); // Save state after successful SQL submission
          // Removed auto-advance for SQL questions
        }
        this.snackBar.open(res.message || (res.passed ? 'Passed' : 'Failed'), 'Close', {
          duration: 5000,
          panelClass: res.passed ? 'success-snackbar' : 'error-snackbar'
        });
        this.cdRef.detectChanges();
      },
      error: (err) => this.handleExecutionError(questionId, err, 'sql')
    });
  }

  // -------- Final Exam Submission --------
  submitExam(isTerminated: boolean): void {
    if (!this.exam || this.isSubmitting) return;
    this.isSubmitting = true;
    clearInterval(this.proctoringInterval);
    clearInterval(this.voiceDetectionInterval);
    if (isTerminated) {
      this.snackBar.open('Exam terminated due to violation.', 'Close', { duration: 5000, panelClass: 'error-snackbar' });
    }

    const payload = {
      studentEmail: isPlatformBrowser(this.platformId) ? sessionStorage.getItem('studentEmail') : null,
      examId: this.exam.id,
      // The frontend should NOT calculate the score. It should only send the raw answers.
      // The backend will be responsible for judging the answers and calculating the final score.
      mcqAnswers: this.mcqAnswers,
      codingAnswers: this.submittedCodingAnswers, // Store submitted code that passed
      codingPassed: this.codingPassed // Track which coding questions passed all test cases
    };

    this.http.post('/api/student/exam/submit', payload).subscribe({
      next: (result: any) => {
        this.isSubmitting = false;
        // Clear saved exam state after successful submission
        this.clearExamState();
        // The backend should return the final, calculated result object.
        if (isPlatformBrowser(this.platformId)) {
          // Store the official result from the backend, not the client-calculated payload.
          sessionStorage.setItem('lastResult', JSON.stringify(result));
        }
        this.router.navigate(['/result']);
      },
      error: (err) => {
        this.isSubmitting = false;
        const errorMessage = err?.error?.message;
        if (isTerminated && errorMessage === 'Exam already submitted') {
          // For auto-submission due to violations, navigate to result page even if already submitted
          this.clearExamState(); // Clear state even on error
          this.router.navigate(['/result']);
        } else {
          const displayMessage = errorMessage === 'Exam already submitted' ? 'Your exam has already been submitted.' : 'Error submitting exam. Please try again.';
          this.snackBar.open(displayMessage, 'Close', { duration: 5000, panelClass: 'error-snackbar' });
        }
      }
    });
  }

  getQuestionType(question: any): string {
    if (!question) return 'MCQ';

    // Check section information first
    if (question.section && question.section.name) {
      if (question.section.name.toLowerCase() === 'sql') return 'SQL';
      if (question.section.name.toLowerCase() === 'coding') return 'Coding';
    }

    // Fallback to category field
    if (question.category === 'SQL') return 'SQL';
    if (question.category === 'Coding' || question.isCodingQuestion) return 'Coding';

    return 'MCQ';
  }

  // --- NEW HELPER METHODS FOR SECTION-BASED DISPLAY ---
  getQuestionsBySection(sectionId: number): any[] {
    return this.allQuestions.filter(q => {
      if (q.section && q.section.id) {
        return q.section.id === sectionId;
      } else if (q.sectionId) {
        return q.sectionId === sectionId;
      }
      return false;
    });
  }

  getQuestionGlobalIndex(question: any): number {
    return this.allQuestions.findIndex(q => q.id === question.id);
  }

  // Select question by in-section index
  selectQuestionBySection(sectionId: number, localIndex: number): void {
    const indices = this.sectionQuestionIndices[sectionId] || [];
    const globalIdx = indices[localIndex];
    if (globalIdx !== undefined) {
      this.selectQuestion(globalIdx);
    }
  }

  // Local (in-section) question index for current question
  getCurrentSectionLocalIndex(): number {
    const currentSection = this.getCurrentSection();
    if (!currentSection || currentSection.id == null) return 0;
    const indices = this.sectionQuestionIndices[currentSection.id] || [];
    const pos = indices.indexOf(this.currentQuestionIndex);
    return Math.max(0, pos);
  }

  // --- Answer Locking Helper Methods ---
  isMcqAnswerLocked(questionId: number): boolean {
    return this.lockedMcqAnswers[questionId] === true;
  }

  isCodingAnswerLocked(questionId: number): boolean {
    return this.lockedCodingAnswers[questionId] === true;
  }

  // Calculate score for a section (number of correct answers)
  calculateSectionScore(section: any): number {
    let score = 0;
    for (let q of section.questions) {
      if (this.getQuestionType(q) === 'MCQ') {
        if (this.mcqAnswers[q.id] === q.correctAnswer) {
          score++;
        }
      } else {
        if (this.successfulRuns[q.id]) {
          score++;
        }
      }
    }
    return score;
  }

  // Check if the current section pass mark is met
  checkSectionPass(): boolean {
    const currentSection = this.getCurrentSection();
    const score = this.calculateSectionScore(currentSection);
    console.log('Section check:', { section: currentSection.name, score, hasMinPassMarks: currentSection.hasMinPassMarks, minPassMarks: currentSection.minPassMarks });
    if (currentSection.hasMinPassMarks && score < currentSection.minPassMarks) {
      this.snackBar.open('You are not qualified to go to next section. Submitting exam.', 'Close', { duration: 5000 });
      this.submitExam(false);
      return false;
    }
    return true;
  }

  // Shuffle questions for randomization (section-wise)
  shuffleQuestions(): void {
    if (!this.allQuestions || this.allQuestions.length === 0 || !this.sections || this.sections.length === 0) return;

    // Shuffle questions within each section separately
    this.sections.forEach(section => {
      if (section.questions && Array.isArray(section.questions) && section.questions.length > 0) {
        // Fisher-Yates shuffle for questions within this section
        const questions = section.questions;
        for (let i = questions.length - 1; i > 0; i--) {
          const j = Math.floor(Math.random() * (i + 1));
          [questions[i], questions[j]] = [questions[j], questions[i]];
        }
      }
    });

    // Rebuild the allQuestions array in section order after shuffling
    this.allQuestions = [];
    this.sections.forEach(section => {
      if (section.questions && Array.isArray(section.questions)) {
        this.allQuestions.push(...section.questions);
      }
    });

    console.log('Questions shuffled within sections for randomization');
  }

  // Helper method to format test case input for display
  formatTestCaseInput(tc: any): string {
    if (typeof tc.input === 'string') {
      return tc.input;
    } else if (tc.input && typeof tc.input === 'object' && tc.input.nums && tc.input.target !== undefined) {
      // Object format: convert to string
      return tc.input.nums.join(' ') + '\n' + tc.input.target;
    }
    return JSON.stringify(tc.input);
  }

  // Helper method to format test case output for display
  formatTestCaseOutput(tc: any): string {
    if (typeof tc.output === 'string') {
      return tc.output;
    } else if (tc.expected && Array.isArray(tc.expected)) {
      // Object format: expected is array
      return tc.expected.join(' ');
    }
    return JSON.stringify(tc.output || tc.expected);
  }
}
