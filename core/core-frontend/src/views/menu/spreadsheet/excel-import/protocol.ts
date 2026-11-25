import type { DateSystem, IWorksheetData } from '@univerjs/core'

export const IMPORT_LIMITS = {
  fileBytes: 20 * 1024 * 1024,
  sheetRows: 10000,
  workbookCells: 200000,
  previewRows: 100,
  previewColumns: 30,
  formulaLength: 1024,
  formulaTokens: 128,
  referenceColumns: 16384,
  sheetNameLength: 31
} as const

export interface ImportSheetInfo {
  id: number
  name: string
  hidden: boolean
  rows: number
  columns: number
}

export type ImportedSheet = Partial<IWorksheetData> & { id: string; name: string }

export const FORMULA_MODES = ['compatible', 'all', 'values'] as const
export type FormulaMode = (typeof FORMULA_MODES)[number]

export interface ImportOptions {
  sheets: { id: number; name: string }[]
  keepFormat: boolean
  formulaMode: FormulaMode
  confirmedFallback: boolean
  dateSystem: DateSystem
}

export interface FormulaIssue {
  sheetName: string
  address: string
  reason: string
  referencedSheet?: string
  missingCache?: boolean
}

export interface ImportAnalysis {
  kept: number
  warnings: FormulaIssue[]
  fallbacks: FormulaIssue[]
  errors: FormulaIssue[]
}

export type ImportRequest = { id: number } & (
  | { type: 'load'; file: File }
  | { type: 'preview'; sheetId: number }
  | { type: 'analyze' | 'convert'; options: ImportOptions }
)

export type ImportResponse = { id: number } & (
  | { type: 'loaded'; sheets: ImportSheetInfo[] }
  | { type: 'preview'; sheetId: number; rows: (string | null)[][] }
  | { type: 'analysis'; analysis: ImportAnalysis }
  | { type: 'converted'; sheets: ImportedSheet[] }
  | { type: 'error'; code: string }
)

export function validateSheetNames(names: string[], existing: string[]): string[] {
  const occupied = new Set(existing.map(name => name.trim().toLowerCase()))
  const counts = new Map<string, number>()
  const trimmed = names.map(name => name.trim())
  for (const name of trimmed) {
    const key = name.toLowerCase()
    counts.set(key, (counts.get(key) || 0) + 1)
  }
  return trimmed.map(name => {
    const key = name.toLowerCase()
    if (!name) return 'nameEmpty'
    if (name.length > IMPORT_LIMITS.sheetNameLength) return 'nameLong'
    if (/[\\/:*?\[\]]|^'|'$/.test(name) || key === 'history') return 'nameInvalid'
    if (occupied.has(key)) return 'nameConflict'
    if (counts.get(key) > 1) return 'nameDuplicate'
    return ''
  })
}

export function suggestSheetNames(names: string[], existing: string[]): string[] {
  const occupied = new Set(existing.map(name => name.trim().toLowerCase()))
  // 优先给源表保留自己的名称，再为同名表生成后缀。
  const reserved = new Set(names.map(name => name.trim().toLowerCase()))
  return names.map(original => {
    const base = original.trim()
    let name = base
    if (occupied.has(name.toLowerCase())) {
      let ordinal = 2
      do {
        const suffix = ' (' + ordinal++ + ')'
        name = base.slice(0, IMPORT_LIMITS.sheetNameLength - suffix.length) + suffix
      } while (occupied.has(name.toLowerCase()) || reserved.has(name.toLowerCase()))
    }
    occupied.add(name.toLowerCase())
    return name
  })
}
