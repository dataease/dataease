import { CommandType, Injector } from '@univerjs/core'
import type { ICommand, IAccessor } from '@univerjs/core'
import { FUniver } from '@univerjs/core/facade'
import { IDialogService } from '@univerjs/ui'
import { ElMessage } from 'element-plus-secondary'
import { useEmitt } from '@/hooks/web/useEmitt'
import { useI18n } from '@/hooks/web/useI18n'
import type {
  ITableCreateDialogParams,
  ITableCreateResult,
  PluginEditPayload
} from '../../../types/editor'
import type { IRangeSelectResult } from '../../RangeSelectPlugin/type'
import { SPREADSHEET_EVENTS } from '../../../utils/events'
import { getCurrentRangeSelection } from '../../../utils/current-range-selection'
import Adapter from '../adapter'
import type { PivotTableConfig } from '../types'
import { PivotTableInstanceService } from '../services/pivot-table-instance.service'
import { PivotTableInsertionService } from '../services/pivot-table-insertion.service'
import { PivotTableRangeService } from '../services/pivot-table-range.service'
import { PluginRenderStatusService } from '../../DataEaseRuntimePlugin/services/table'

const { emitter } = useEmitt()
const { t } = useI18n()

export const InsertPivotTableOperation: ICommand = {
  id: 'dataease.operation.insert-pivot-table',
  type: CommandType.OPERATION,
  handler: async (accessor: IAccessor) => {
    // 工具栏禁用之外保留命令级校验，避免直接调用命令绕过草稿互斥规则。
    if (accessor.get(PluginRenderStatusService).hasDraft()) {
      return false
    }

    const insertionService = accessor.get(PivotTableInsertionService)
    const initialRange = getCurrentRangeSelection(accessor.get(Injector))
    if (!insertionService.start()) {
      return false
    }
    emitter.emit(SPREADSHEET_EVENTS.CLOSE_PLUGIN_EDITOR)

    const dialogParams: ITableCreateDialogParams = {
      pluginType: 'pivot',
      initialRange,
      callback: (result: ITableCreateResult) => handleCreateResult(accessor, result),
      onClose: () => insertionService.cancel(),
      validateRange: (range: IRangeSelectResult, _resultLimit: number, silent = false) => {
        const message = accessor.get(PivotTableRangeService).validateSelectedRange(range)
        if (message && !silent) {
          ElMessage.warning(message)
        }
        return message
      }
    }

    await accessor.get(IDialogService).open({
      id: 'PivotTableCreateDialog',
      children: {
        label: {
          name: 'PivotTableCreateDialog',
          props: {
            injector: accessor.get(Injector),
            params: dialogParams
          }
        }
      },
      title: {
        label: '插入透视表'
      },
      draggable: true,
      closable: true,
      mask: false,
      maskClosable: false,
      width: 400,
      onClose: () => {
        // 区域选择期间保留插入位置弹窗，避免两个流程状态脱节。
        if (dialogParams.canClose?.() === false) {
          return
        }
        accessor.get(IDialogService).close('PivotTableCreateDialog')
        accessor.get(IDialogService).close('RangeSelectDialog')
        insertionService.cancel()
      }
    })
    return true
  }
}

function handleCreateResult(accessor: IAccessor, result: ITableCreateResult): void {
  if (result.placement === 'new') {
    createOnNewSheet(accessor, result.resultLimit)
    return
  }
  if (result.placement === 'existing' && result.range) {
    createOnExistingSheet(accessor, result.range, result.resultLimit)
    return
  }
  accessor.get(PivotTableInsertionService).cancel()
}

function createOnNewSheet(accessor: IAccessor, resultLimit?: number): void {
  const config = Adapter.getDefaultConfig() as PivotTableConfig
  const univerApi = FUniver.newAPI(accessor.get(Injector))
  const workbook = univerApi.getActiveWorkbook()
  const sheetPrefix = t('spreadsheet.sheet_prefix', '工作表')
  let sheetNumber = 2
  let sheetName = `${sheetPrefix}${sheetNumber}`

  if (workbook) {
    const sheetNames = workbook.getSheets().map((sheet: any) => sheet.getSheetName())
    while (sheetNames.includes(sheetName)) {
      sheetName = `${sheetPrefix}${++sheetNumber}`
    }
    const worksheet = workbook.create(sheetName, 1000, 26)
    workbook.setActiveSheet(worksheet)
    config.placement.sheetId = worksheet.getSheetId()
  }

  config.placement.sheetName = sheetName
  config.placement.startCell = 'A1'
  config.data.resultLimit = normalizeResultLimit(resultLimit)
  finishInsertion(accessor, config)
}

function createOnExistingSheet(
  accessor: IAccessor,
  range: IRangeSelectResult,
  resultLimit?: number
): void {
  const config = Adapter.getDefaultConfig() as PivotTableConfig
  config.placement.sheetId = range.sheetId
  config.placement.sheetName = range.sheetName
  config.placement.startCell = `${toColumnName(range.startColumnNumber)}${range.startRowNumber + 1}`
  config.data.resultLimit = normalizeResultLimit(resultLimit)
  finishInsertion(accessor, config)
}

function finishInsertion(accessor: IAccessor, config: PivotTableConfig): void {
  const univerApi = FUniver.newAPI(accessor.get(Injector))
  const unitId = univerApi.getActiveWorkbook()?.getId?.()
  if (unitId) {
    accessor.get(PivotTableInstanceService).addOrUpdate(unitId, config)
    accessor.get(PluginRenderStatusService).set({
      pluginId: config.id,
      type: 'pivot',
      status: 'draft',
      unitId,
      sheetId: config.placement.sheetId,
      startCell: config.placement.startCell,
      updatedAt: Date.now()
    })
  }
  accessor.get(PivotTableInsertionService).finish()
  emitter.emit(SPREADSHEET_EVENTS.OPEN_PLUGIN_EDITOR, {
    config,
    isNewSheet: false
  } as PluginEditPayload)
}

function normalizeResultLimit(resultLimit?: number): number {
  return Number.isFinite(resultLimit) && Number(resultLimit) > 0
    ? Math.floor(Number(resultLimit))
    : 1000
}

function toColumnName(columnIndex: number): string {
  let columnName = ''
  let current = columnIndex
  do {
    columnName = String.fromCharCode(65 + (current % 26)) + columnName
    current = Math.floor(current / 26) - 1
  } while (current >= 0)
  return columnName
}
