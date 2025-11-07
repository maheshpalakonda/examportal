import { Section } from './section.model';

export interface Exam {
  id: number;
  examName: string;
  isActive: boolean;
  sections: Section[];
}
