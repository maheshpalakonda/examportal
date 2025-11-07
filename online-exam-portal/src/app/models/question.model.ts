// src/app/models/question.model.ts
import { Section } from './section.model';

export interface Question {
  id: number;
  questionText: string;
  section?: Section;
  option1?: string;
  option2?: string;
  option3?: string;
  option4?: string;
  correctAnswer?: string;
  boilerplateJava?: string;
  boilerplatePython?: string;
  boilerplateC?: string;
  boilerplateSql?: string;
  testCases?: string;
  
  // Helper properties for backward compatibility
  category?: string;
  isCodingQuestion?: boolean;
  boilerplateCode?: {
    java?: string;
    python?: string;
    c?: string;
    sql?: string;
  };
}
