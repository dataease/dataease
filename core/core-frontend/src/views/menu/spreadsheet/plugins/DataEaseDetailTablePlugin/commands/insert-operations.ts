import { CommandType, Injector } from '@univerjs/core'
import { type ICommand, type IAccessor, ICommandService } from '@univerjs/core'
import { FUniver } from '@univerjs/core/facade'
import { IDialogService } from '@univerjs/ui'
import { ElMessage } from 'element-plus-secondary'
import { useEmitt } from '@/hooks/web/useEmitt'
import type {
  ITableCreateDialogParams,
  ITableCreateResult,
  PluginEditPayload
} from '../../../types/editor'
import type { IRangeSelectResult } from '../../RangeSelectPlugin/type'
import { SPREADSHEET_EVENTS } from '../../../utils/events'
import { getCurrentRangeSelection } from '../../../utils/current-range-selection'
import Adapter from '../adapter'
import { DetailTableInstanceService } from '../services/detail-table-instance.service'
import { DetailTableInsertionService } from '../services/detail-table-insertion.service'
import { DetailTableRangeService } from '../services/detail-table-range.service'
import type { DetailTableConfig } from '../types'

const { emitter } = useEmitt()

export const InsertDetailTableOperation: ICommand = {
  id: 'dataease.operation.insert-detail-table',
  type: CommandType.OPERATION,
  handler: async (accessor: IAccessor) => {
    const commandService = accessor.get(ICommandService)
    const insertionService = accessor.get(DetailTableInsertionService)
    const initialRange = getCurrentRangeSelection(accessor.get(Injector))
    if (!insertionService.start()) {
      return false
    }

    emitter.emit(SPREADSHEET_EVENTS.CLOSE_PLUGIN_EDITOR)
    await commandService.executeCommand(OpenDetailTableCreateDialogOperation.id, {
      pluginType: 'detail',
      initialRange,
      callback: (result: ITableCreateResult) => {
        if (result.placement === 'new') {
          handleCreateNewSheet(accessor, result.resultLimit)
        } else if (result.placement === 'existing' && result.range) {
          handleUseExistingRange(accessor, result.range, result.resultLimit)
        } else {
          insertionService.cancel()
        }
      },
      onClose: () => {
        insertionService.cancel()
      },
      validateRange: (range: IRangeSelectResult, resultLimit: number, silent = false) => {
        const message = accessor.get(DetailTableRangeService).validateSelectedRange(range, resultLimit)
        if (message && !silent) {
          ElMessage.warning(message)
        }
        return message
      }
    } as ITableCreateDialogParams)

    return true
  }
}

function handleCreateNewSheet(accessor: IAccessor, resultLimit?: number) {
  const config = Adapter.getDefaultConfig()
  config.placement.startCell = 'A1'
  config.placement.sheetId = ''
  config.data.resultLimit = normalizeResultLimit(resultLimit)

  const injector = accessor.get(Injector)
  const univerApi = FUniver.newAPI(injector)
  const fWorkbook = univerApi.getActiveWorkbook()

  let newSheetName = 'Sheet2'

  if (fWorkbook) {
    const sheetNames = fWorkbook.getSheets().map((sheet: any) => sheet.getSheetName())
    let newSheetNum = 2
    newSheetName = `Sheet${newSheetNum}`
    while (sheetNames.includes(newSheetName)) {
      newSheetNum++
      newSheetName = `Sheet${newSheetNum}`
    }

    const newSheet = fWorkbook.create(newSheetName, 1000, 26)
    fWorkbook.setActiveSheet(newSheet)
    config.placement.sheetId = newSheet.getSheetId()
  }

  config.placement.sheetName = newSheetName
  registerDetailTable(accessor, config as DetailTableConfig)
  accessor.get(DetailTableInsertionService).finish()

  emitter.emit(SPREADSHEET_EVENTS.OPEN_PLUGIN_EDITOR, {
    config,
    isNewSheet: false
  } as PluginEditPayload)
}

function handleUseExistingRange(accessor: IAccessor, range: IRangeSelectResult, resultLimit?: number) {
  const config = Adapter.getDefaultConfig()
  config.placement.sheetId = range.sheetId
  config.placement.sheetName = range.sheetName
  config.placement.startCell = `${toColumnName(range.startColumnNumber)}${range.startRowNumber + 1}`
  config.data.resultLimit = normalizeResultLimit(resultLimit)
  registerDetailTable(accessor, config as DetailTableConfig)
  accessor.get(DetailTableInsertionService).finish()

  emitter.emit(SPREADSHEET_EVENTS.OPEN_PLUGIN_EDITOR, {
    config,
    isNewSheet: false
  } as PluginEditPayload)
}

function registerDetailTable(accessor: IAccessor, config: DetailTableConfig): void {
  const injector = accessor.get(Injector)
  const univerApi = FUniver.newAPI(injector)
  const workbook = univerApi.getActiveWorkbook()
  const unitId = workbook?.getId?.()

  if (!unitId) {
    return
  }

  const activeSheet = workbook.getActiveSheet?.()
  if (activeSheet && config.placement.sheetId === activeSheet.getSheetId?.()) {
    config.placement.sheetName =
      activeSheet.getName?.() || activeSheet.getSheetName?.() || config.placement.sheetName
  }

  accessor.get(DetailTableInstanceService).addOrUpdate(unitId, config)
}

function toColumnName(columnIndex: number): string {
  let colStr = ''
  let tempCol = columnIndex

  do {
    colStr = String.fromCharCode(65 + (tempCol % 26)) + colStr
    tempCol = Math.floor(tempCol / 26) - 1
  } while (tempCol >= 0)

  return colStr
}

function normalizeResultLimit(resultLimit?: number): number {
  return Number.isFinite(resultLimit) && Number(resultLimit) > 0 ? Math.floor(Number(resultLimit)) : 1000
}

export const OpenDetailTableCreateDialogOperation: ICommand = {
  id: 'dataease.operation.open-detail-table-create-dialog',
  type: CommandType.OPERATION,
  handler: async (accessor: IAccessor, params?: ITableCreateDialogParams) => {
    const dialogService = accessor.get(IDialogService)
    const injector = accessor.get(Injector)

    dialogService.open({
      id: 'DetailTableCreateDialog',
      children: {
        label: {
          name: 'DetailTableCreateDialog',
          props: {
            injector,
            params
          }
        }
      },
      title: {
        label: '插入明细表'
      },
      draggable: true,
      closable: true,
      mask: false,
      maskClosable: false,
      width: 400,
      onClose: () => {
        if (params?.canClose?.() === false) {
          return
        }
        dialogService.close('DetailTableCreateDialog')
        dialogService.close('RangeSelectDialog')
        params?.onClose?.()
      }
    })

    return true
  }
}
