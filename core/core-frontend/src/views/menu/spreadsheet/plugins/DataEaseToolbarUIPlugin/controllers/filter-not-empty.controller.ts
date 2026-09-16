import { Disposable, ICommandService } from '@univerjs/core'
import {
  CustomFilterOperator,
  notEquals,
  SetSheetsFilterCriteriaCommand
} from '@univerjs/sheets-filter'

// Univer 的筛选计算和条件回显使用单个空格作为“不为空”的内部标记。
const NOT_EMPTY_VALUE = ' '
let users = 0
let originalNotEquals: typeof notEquals.fn
const matchesNotEmpty: typeof notEquals.fn = (value, compare) =>
  compare === NOT_EMPTY_VALUE ? value != null && value !== '' : originalNotEquals(value, compare)

export class FilterNotEmptyController extends Disposable {
  constructor(@ICommandService commandService: ICommandService) {
    super()
    // 通过公开的比较器适配空字符串（包括公式返回的空字符串）；其他不等于条件不变。
    // 同一页面可能存在多个 Univer 实例，最后一个实例销毁时才恢复比较器。
    if (users++ === 0) {
      originalNotEquals = notEquals.fn
      notEquals.fn = matchesNotEmpty
    }
    this.disposeWithMe({
      dispose: () => {
        if (--users === 0 && notEquals.fn === matchesNotEmpty) notEquals.fn = originalNotEquals
      }
    })
    commandService.unregisterCommand(SetSheetsFilterCriteriaCommand.id)
    this.disposeWithMe(
      commandService.registerCommand({
        ...SetSheetsFilterCriteriaCommand,
        handler: (
          accessor,
          params: Parameters<typeof SetSheetsFilterCriteriaCommand.handler>[1],
          options
        ) => {
          if (!params?.criteria)
            return SetSheetsFilterCriteriaCommand.handler(accessor, params, options)
          const criteria = params.criteria
          const filters = criteria.customFilters?.customFilters
          if (
            filters?.length === 1 &&
            filters[0].operator === CustomFilterOperator.NOT_EQUALS &&
            filters[0].val === ''
          ) {
            return SetSheetsFilterCriteriaCommand.handler(
              accessor,
              {
                ...params,
                criteria: {
                  ...criteria,
                  customFilters: {
                    ...criteria.customFilters,
                    customFilters: [{ ...filters[0], val: NOT_EMPTY_VALUE }]
                  }
                }
              },
              options
            )
          }
          return SetSheetsFilterCriteriaCommand.handler(accessor, params, options)
        }
      })
    )
  }
}
