import {
  Inject,
  Injector,
  IUniverInstanceService,
  Plugin,
  UniverInstanceType
} from '@univerjs/core'
import type { SpreadsheetRuntimeOptions } from '../../types/mode'
import { SpreadsheetFilterRuntimeService } from '../DataEaseFilterPlugin/services/filter-runtime.service'
import { SpreadsheetOuterParamsService } from './services/outer-params.service'

export class DataEaseOuterParamsPlugin extends Plugin {
  static override type = UniverInstanceType.UNIVER_SHEET
  static override pluginName = 'DATAEASE_OUTER_PARAMS_PLUGIN'

  constructor(
    private readonly options: SpreadsheetRuntimeOptions = {},
    @Inject(Injector) protected readonly _injector: Injector
  ) {
    super()
  }

  override onStarting(): void {
    this._injector.add([SpreadsheetOuterParamsService])
    this._injector.get(SpreadsheetOuterParamsService).initialize(this.options)
  }

  override onRendered(): void {
    const { mode, spreadsheetId } = this.options
    if (mode !== 'preview' || spreadsheetId == null || spreadsheetId === '') return
    // onRendered 按注册顺序同步执行，下一微任务再读取查询插件已登记的默认值等待。
    queueMicrotask(() => {
      if (this._disposed) return
      const workbook = this._injector
        .get(IUniverInstanceService)
        .getCurrentUnitOfType(UniverInstanceType.UNIVER_SHEET)
      if (!workbook) return
      const valuesReady = this._injector
        .get(SpreadsheetFilterRuntimeService)
        .waitForValues(workbook.getUnitId())
      this._injector.get(SpreadsheetOuterParamsService).completeInitialization(valuesReady)
    })
  }
}
