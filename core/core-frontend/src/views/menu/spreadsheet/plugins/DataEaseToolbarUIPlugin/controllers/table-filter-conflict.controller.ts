import { Disposable, ICommandService, Inject } from '@univerjs/core'
import {
  type ISetSheetTableParams,
  SetSheetTableFilterCommand,
  TableManager
} from '@univerjs/sheets-table'
import { ElMessage } from 'element-plus-secondary'

export class TableFilterConflictController extends Disposable {
  constructor(
    @ICommandService commandService: ICommandService,
    @Inject(TableManager) tableManager: TableManager
  ) {
    super()
    this.disposeWithMe(
      commandService.onCommandExecuted(command => {
        if (command.id !== SetSheetTableFilterCommand.id) return
        const params = command.params as ISetSheetTableParams | undefined
        if (!params?.tableFilter) return
        const table = tableManager.getTable(params.unitId, params.tableId)
        const hiddenRows = table?.getTableFilters().getFilterOutRows()
        if (!table || !hiddenRows?.size) return
        // 按实际被筛掉的行判断；仅范围相交、没有隐藏行时不提示。
        const rows = Array.from(hiddenRows)
        const affected = tableManager
          .getTablesBySubunitId(params.unitId, table.getSubunitId())
          .filter(other => {
            if (other.getId() === table.getId()) return false
            const range = other.getRange()
            return rows.some(row => row >= range.startRow && row <= range.endRow)
          })
        if (!affected.length) return
        const names = affected
          .slice(0, 3)
          .map(other => `“${other.getDisplayName()}”`)
          .join('、')
        const suffix = affected.length > 3 ? `等 ${affected.length} 个表格` : ''
        ElMessage.warning({
          message: `筛选“${table.getDisplayName()}”会隐藏整个工作表行，${names}${suffix}的部分行也会被隐藏，数据未删除。建议将独立表格放在不同工作表，或上下排列且行范围不重叠。`,
          duration: 8000,
          showClose: true,
          grouping: true
        })
      })
    )
  }
}
