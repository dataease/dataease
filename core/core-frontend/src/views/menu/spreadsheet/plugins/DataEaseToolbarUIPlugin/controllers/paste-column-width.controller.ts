import {
  DEFAULT_WORKSHEET_COLUMN_WIDTH,
  DEFAULT_WORKSHEET_COLUMN_WIDTH_KEY,
  Disposable,
  IConfigService,
  IUniverInstanceService
} from '@univerjs/core'
import { SetWorksheetColWidthMutation, SetWorksheetColWidthMutationFactory } from '@univerjs/sheets'
import { ISheetClipboardService, PREDEFINED_HOOK_NAME_PASTE } from '@univerjs/sheets-ui'

export class PasteColumnWidthController extends Disposable {
  constructor(
    @ISheetClipboardService clipboard: ISheetClipboardService,
    @IConfigService config: IConfigService,
    @IUniverInstanceService instances: IUniverInstanceService
  ) {
    super()
    this.disposeWithMe(
      clipboard.addClipboardHook({
        id: 'dataease-paste-default-column-width',
        onPasteColumns(pasteTo, columns, payload) {
          const empty = { redos: [], undos: [] }
          if (
            payload.pasteType !== PREDEFINED_HOOK_NAME_PASTE.SPECIAL_PASTE_COL_WIDTH ||
            !columns.length
          )
            return empty
          const {
            unitId,
            subUnitId,
            range: { rows, cols }
          } = pasteTo
          const workbook = instances.getUniverSheetInstance(unitId)
          const worksheet = workbook && workbook.getSheetBySheetId(subUnitId)
          if (!worksheet || !rows.length || !cols.length) return empty
          const configuredWidth = config.getConfig<number>(DEFAULT_WORKSHEET_COLUMN_WIDTH_KEY)
          const defaultWidth =
            typeof configuredWidth === 'number' ? configuredWidth : DEFAULT_WORKSHEET_COLUMN_WIDTH
          // Univer 的列宽粘贴钩子会跳过等于默认值的源列宽，导致目标宽度保持不变。
          // 只补充这些被遗漏的列，其余列宽和粘贴类型继续使用原生逻辑。
          const ranges = cols
            .filter((_, index) => Number(columns[index % columns.length].width) === defaultWidth)
            .map(column => ({
              startRow: rows[0],
              endRow: rows[rows.length - 1],
              startColumn: column,
              endColumn: column
            }))
          if (!ranges.length) return empty
          const params = { unitId, subUnitId, ranges, colWidth: defaultWidth }
          return {
            redos: [{ id: SetWorksheetColWidthMutation.id, params }],
            undos: [
              {
                id: SetWorksheetColWidthMutation.id,
                params: SetWorksheetColWidthMutationFactory(params, worksheet)
              }
            ]
          }
        }
      })
    )
  }
}
