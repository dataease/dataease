import {
  BooleanNumber,
  ICommandService,
  IPermissionService,
  IUndoRedoService,
  IUniverInstanceService,
  mergeWorksheetSnapshotWithDefault,
  UniverInstanceType,
  type Workbook
} from '@univerjs/core'
import {
  InsertSheetMutation,
  RemoveSheetMutation,
  SetWorksheetActiveOperation,
  WorkbookCreateSheetPermission,
  WorkbookEditablePermission
} from '@univerjs/sheets'
import { SpreadsheetModeService } from '../services/spreadsheet-mode.service'
import type { UniverInstance } from '../utils/univerConfig'
import { validateSheetNames, type ImportedSheet } from './protocol'

function currentWorkbook(instance: UniverInstance) {
  return instance.univer
    .__getInjector()
    .get(IUniverInstanceService)
    .getCurrentUnitOfType<Workbook>(UniverInstanceType.UNIVER_SHEET)
}

export function getCurrentSheetNames(instance: UniverInstance): string[] {
  return (
    currentWorkbook(instance)
      ?.getSheets()
      .map(sheet => sheet.getName()) ?? []
  )
}

export function appendImportedSheets(instance: UniverInstance, sheets: ImportedSheet[]): boolean {
  const workbook = currentWorkbook(instance)
  const previous = workbook?.getActiveSheet(true)
  if (!workbook || !previous || !sheets.length) return false

  const injector = instance.univer.__getInjector()
  if (injector.get(SpreadsheetModeService).isPreview()) return false
  const unitId = workbook.getUnitId()
  const permissions = injector.get(IPermissionService)
  const required = [
    new WorkbookEditablePermission(unitId),
    new WorkbookCreateSheetPermission(unitId)
  ]
  if (required.some(point => permissions.getPermissionPoint(point.id)?.value === false))
    return false

  // 对话框打开后工作簿可能发生变化，按写入时的页名和 ID 复核整批数据。
  const existing = workbook.getSheets()
  const names = sheets.map(sheet => sheet.name)
  if (
    validateSheetNames(
      names,
      existing.map(sheet => sheet.getName())
    ).some(Boolean)
  )
    return false
  const ids = new Set(existing.map(sheet => sheet.getSheetId()))
  for (const sheet of sheets) {
    if (!sheet.id || ids.has(sheet.id)) return false
    ids.add(sheet.id)
  }
  const insert = sheets.map((sheet, index) => ({
    id: InsertSheetMutation.id,
    params: {
      unitId,
      index: existing.length + index,
      sheet: mergeWorksheetSnapshotWithDefault({ ...sheet, hidden: BooleanNumber.FALSE })
    }
  }))
  const remove = sheets
    .map(sheet => ({
      id: RemoveSheetMutation.id,
      params: { unitId, subUnitId: sheet.id, subUnitName: sheet.name }
    }))
    .reverse()
  const activate = (subUnitId: string) => ({
    id: SetWorksheetActiveOperation.id,
    params: { unitId, subUnitId }
  })
  const restoreActive = activate(previous.getSheetId())
  const redo = [...insert, activate(sheets[0].id)]
  const commands = injector.get(ICommandService)
  let committed = false
  try {
    for (const command of redo) {
      if (!commands.syncExecuteCommand(command.id, command.params)) return false
    }
    // 全部写入后才登记一次历史，撤销时先切回原工作表，再反序移除导入的页。
    injector.get(IUndoRedoService).pushUndoRedo({
      unitID: unitId,
      undoMutations: [restoreActive, ...remove],
      redoMutations: redo
    })
    committed = true
    return true
  } catch {
    return false
  } finally {
    if (!committed) {
      // 命令监听器可能在写入之后抛错，以模型中实际出现的 ID 为准回滚。
      for (const command of remove) {
        if (workbook.getSheetBySheetId(command.params.subUnitId)) {
          commands.syncExecuteCommand(command.id, command.params)
        }
      }
      if (workbook.getActiveSheet(true)?.getSheetId() !== previous.getSheetId()) {
        commands.syncExecuteCommand(restoreActive.id, restoreActive.params)
      }
    }
  }
}
