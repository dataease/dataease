import {
  CellValueType,
  Disposable,
  ICommandService,
  IUniverInstanceService,
  Range
} from '@univerjs/core'
import { INumfmtService, SheetsSelectionsService, getSheetCommandTarget } from '@univerjs/sheets'
import {
  AddDecimalCommand,
  SetNumfmtCommand,
  SubtractDecimalCommand
} from '@univerjs/sheets-numfmt'
import { adjustDecimalFormat } from '../utils/decimal-format'

// 保留原命令 ID 和菜单的权限控制，仅替换 Univer 0.25 的小数位计算。
export class DecimalCommandController extends Disposable {
  constructor(@ICommandService commandService: ICommandService) {
    super()
    for (const [command, delta] of [
      [AddDecimalCommand, 1],
      [SubtractDecimalCommand, -1]
    ] as const) {
      commandService.unregisterCommand(command.id)
      this.disposeWithMe(
        commandService.registerCommand({
          ...command,
          handler: async accessor => {
            const target = getSheetCommandTarget(accessor.get(IUniverInstanceService))
            const selections = accessor.get(SheetsSelectionsService).getCurrentSelections()
            if (!target || !selections?.length) return false
            const { unitId, subUnitId, worksheet } = target
            const numfmtService = accessor.get(INumfmtService)
            const values = new Map<string, { row: number; col: number; pattern: string }>()
            for (const selection of selections) {
              Range.foreach(
                {
                  ...selection.range,
                  endRow: Math.min(selection.range.endRow, worksheet.getRowCount() - 1),
                  endColumn: Math.min(selection.range.endColumn, worksheet.getColumnCount() - 1)
                },
                (row, col) => {
                  // 渲染值已包含百分号、千分位等，计算精度必须使用原始数值。
                  const cell = worksheet.getCellRaw(row, col)
                  if (!cell || (cell.t != null && cell.t !== CellValueType.NUMBER)) return
                  if (cell.t == null && typeof cell.v !== 'number') return
                  const value = Number(cell.v)
                  if (cell.v === '' || cell.v == null || !Number.isFinite(value)) return
                  const format = numfmtService.getValue(unitId, subUnitId, row, col)
                  const pattern = (format && format.pattern) || 'General'
                  const next = adjustDecimalFormat(pattern, value, delta)
                  if (next !== pattern) values.set(`${row}:${col}`, { row, col, pattern: next })
                }
              )
            }
            if (!values.size) return false
            return commandService.executeCommand(SetNumfmtCommand.id, {
              unitId,
              subUnitId,
              values: [...values.values()]
            })
          }
        })
      )
    }
  }
}
