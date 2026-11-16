export type SpreadsheetMode = 'edit' | 'preview'

export interface SpreadsheetRuntimeOptions {
  mode?: SpreadsheetMode
  // 仅正式只读预览传入，编辑页及编辑器内预览不加载外参运行态。
  spreadsheetId?: string | number
  onReady?: () => void
  onError?: (error: unknown) => void
}
