export interface Section {
  id?: number;
  name: string;
  durationInMinutes: number;
  orderIndex: number;
  marks: number;
  hasMinPassMarks: boolean;
  minPassMarks: number;
  numQuestionsToSelect?: number;
  examId?: number;
}

export interface SectionCreateRequest {
  name: string;
  durationInMinutes: number;
  orderIndex: number;
  hasMinPassMarks: boolean;
  minPassMarks: number;
  numQuestionsToSelect?: number;
}

export interface SectionUpdateRequest {
  name: string;
  durationInMinutes: number;
  orderIndex: number;
  hasMinPassMarks: boolean;
  minPassMarks: number;
  numQuestionsToSelect?: number;
}
