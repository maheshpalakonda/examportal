declare module 'monaco-editor' {
  export interface IEditorConstructionOptions {
    [key: string]: any;
  }
}

// Fix for Monaco loader
declare global {
  interface Window {
    MonacoEnvironment: any;
  }
}