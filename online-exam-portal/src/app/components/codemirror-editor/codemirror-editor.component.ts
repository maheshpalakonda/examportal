import {
  Component,
  ElementRef,
  ViewChild,
  AfterViewInit,
  Input,
  Output,
  EventEmitter,
  OnDestroy,
  OnChanges,
  SimpleChanges,
  ViewEncapsulation
} from '@angular/core';
import { EditorState } from '@codemirror/state';
import { EditorView } from '@codemirror/view';
import { basicSetup } from 'codemirror';
import { oneDark } from '@codemirror/theme-one-dark';
import { keymap } from '@codemirror/view';
import { defaultKeymap } from '@codemirror/commands';
import { history, historyKeymap } from '@codemirror/commands';

// Language support
import { java } from '@codemirror/lang-java';
import { python } from '@codemirror/lang-python';
import { cpp } from '@codemirror/lang-cpp';
import { sql } from '@codemirror/lang-sql';

@Component({
  selector: 'app-codemirror-editor',
  standalone: true,
  template: '<div #editorHost></div>',
  styles: [`
    :host {
      display: block;
      width: 100%;
      height: 100%;
      direction: ltr;
      text-align: left;
    }
    .cm-editor {
      height: 100% !important;
      outline: none !important;
      direction: ltr !important;
      text-align: left !important;
      position: relative;
    }
    .cm-content {
      direction: ltr !important;
      text-align: left !important;
      white-space: pre;
    }
    .cm-line {
      direction: ltr !important;
      text-align: left !important;
    }
  `],
  encapsulation: ViewEncapsulation.None,
})
export class CodemirrorEditorComponent implements AfterViewInit, OnDestroy, OnChanges {
  @ViewChild('editorHost') editorHost!: ElementRef;
  @Input() initialCode: string = '';
  @Input() language: 'java' | 'python' | 'c' | 'sql' = 'java';
  @Output() codeChange = new EventEmitter<string>();

  private editorView?: EditorView;
  private preserveCursorPosition = false;

  ngAfterViewInit(): void {
    this.createEditor();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['initialCode'] || changes['language']) {
      if (this.editorView) {
        this.updateEditor();
      }
    }
  }

  ngOnDestroy(): void {
    this.editorView?.destroy();
  }

  // Public method to update editor content while preserving cursor position
  public updateContentWithCursorPreservation(newCode: string): void {
    this.initialCode = newCode;
    this.preserveCursorPosition = true;
    this.updateEditor();
  }

  private createEditor(): void {
    this.editorView?.destroy();

    try {
      const languageExtension = this.getLanguageExtension();
      const startState = EditorState.create({
        doc: this.initialCode,
        extensions: [
          basicSetup,
          oneDark,
          languageExtension,
          keymap.of([...defaultKeymap, ...historyKeymap]),
          history(),
          EditorView.updateListener.of(update => {
            if (update.docChanged) {
              this.codeChange.emit(update.state.doc.toString());
            }
          })
        ]
      });

      this.editorView = new EditorView({
        state: startState,
        parent: this.editorHost.nativeElement
      });
    } catch (e) {
      console.error('FATAL: Failed to create CodeMirror editor instance.', e);
    }
  }

  private updateEditor(): void {
    if (!this.editorView) return;

    const currentState = this.editorView.state;
    const currentSelection = currentState.selection;

    // Only update if the content has actually changed
    if (currentState.doc.toString() !== this.initialCode) {
      const transaction = currentState.update({
        changes: {
          from: 0,
          to: currentState.doc.length,
          insert: this.initialCode
        },
        selection: this.preserveCursorPosition ? currentSelection : undefined
      });

      this.editorView.dispatch(transaction);
    }

    // Reset the flag after update
    this.preserveCursorPosition = false;
  }

  private getLanguageExtension() {
    switch (this.language) {
      case 'java': return java();
      case 'python': return python();
      case 'c': return cpp();
      case 'sql': return sql();
      default: return java();
    }
  }
}
