import {
  CommandType,
  IUniverInstanceService,
  UniverInstanceType,
  type IAccessor,
  type ICommand
} from '@univerjs/core'
import { FilterInstanceService } from '../services/filter-instance.service'
import { dispatchSpreadsheetFilterVisibleChange } from '../utils/events'

export const TOGGLE_SPREADSHEET_FILTER_OPERATION_ID = 'dataease.operation.toggle-spreadsheet-filter'

export const createToggleSpreadsheetFilterOperation = (
  onHide: (unitId: string) => void
): ICommand => ({
  id: TOGGLE_SPREADSHEET_FILTER_OPERATION_ID,
  type: CommandType.OPERATION,
  handler: (accessor: IAccessor) => {
    const workbook = accessor
      .get(IUniverInstanceService)
      .getCurrentUnitOfType(UniverInstanceType.UNIVER_SHEET)
    const unitId = (workbook as any)?.getUnitId?.() || (workbook as any)?.getId?.()
    const filterInstanceService = accessor.get(FilterInstanceService)

    const visible = filterInstanceService.toggleVisible()
    if (unitId) {
      filterInstanceService.setVisibleForUnit(unitId, visible)
      // 隐藏后首查使用空筛选，直接放行正在等待下拉首项的表格。
      if (!visible) onHide(unitId)
    }
    dispatchSpreadsheetFilterVisibleChange(visible)
    return true
  }
})
