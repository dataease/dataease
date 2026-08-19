import {
  CommandType,
  IUniverInstanceService,
  UniverInstanceType,
  type IAccessor,
  type ICommand
} from '@univerjs/core'
import { FilterInstanceService } from '../services/filter-instance.service'
import { dispatchSpreadsheetFilterVisibleChange } from '../utils/events'

export const ToggleSpreadsheetFilterOperation: ICommand = {
  id: 'dataease.operation.toggle-spreadsheet-filter',
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
    }
    dispatchSpreadsheetFilterVisibleChange(visible)
    return true
  }
}
