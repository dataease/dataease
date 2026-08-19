import {
  type Injector,
  IUniverInstanceService,
  UniverInstanceType
} from '@univerjs/core'
import { serializeRange, serializeRangeWithSheet } from '@univerjs/engine-formula'
import { SheetsSelectionsService } from '@univerjs/sheets'
import type { IRangeSelectResult } from '../plugins/RangeSelectPlugin/type'

export function getCurrentRangeSelection(injector: Injector): IRangeSelectResult | undefined {
  const workbook = injector
    .get(IUniverInstanceService)
    .getCurrentUnitOfType(UniverInstanceType.UNIVER_SHEET)
  const worksheet = workbook?.getActiveSheet()
  const range = injector.get(SheetsSelectionsService).getCurrentSelections()?.[0]?.range

  if (!worksheet || !range) {
    return undefined
  }

  const sheetName = worksheet.getName()
  return {
    startRowNumber: range.startRow,
    startColumnNumber: range.startColumn,
    endRowNumber: range.endRow,
    endColumnNumber: range.endColumn,
    sheetId: worksheet.getSheetId(),
    sheetName,
    range: serializeRange(range),
    fullRange: serializeRangeWithSheet(sheetName, range)
  }
}
