import { Disposable, Inject, IUniverInstanceService, UniverInstanceType } from '@univerjs/core'
import { cloneDeep } from 'lodash-es'
import { ElMessage } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import type { SpreadsheetRuntimeOptions } from '../../../types/mode'
import type { SpreadsheetOuterParamsRuntime } from '../../../types/outer-params'
import type { SpreadsheetFilterConfig, SpreadsheetQueryFilterItem } from '../../../types/plugin'
import { loadSpreadsheetOuterParams } from '../utils/load-outer-params'
import {
  applySpreadsheetOuterParamsToFilterConfig,
  resolveSpreadsheetOuterParams
} from '../utils/outer-params'

export class SpreadsheetOuterParamsService extends Disposable {
  private ready?: Promise<void>
  private params?: SpreadsheetOuterParamsRuntime
  private abortController?: AbortController
  private onReady?: () => void
  private onError?: (error: unknown) => void
  private errorNotified = false

  constructor(
    @Inject(IUniverInstanceService) private readonly univerInstanceService: IUniverInstanceService
  ) {
    super()
  }

  initialize({ mode, spreadsheetId, onReady, onError }: SpreadsheetRuntimeOptions): void {
    this.onReady = onReady
    this.onError = onError
    if (mode !== 'preview' || spreadsheetId == null || spreadsheetId === '') {
      this.completeInitialization()
      return
    }
    const abortController = new AbortController()
    this.abortController = abortController
    // Univer 生命周期不等待异步钩子，先保存同一个 Promise，查询组件和字段取数均等待它。
    this.ready = loadSpreadsheetOuterParams(spreadsheetId, abortController.signal).then(input => {
      if (this._disposed || abortController.signal.aborted) {
        throw new Error(useI18n().t('spreadsheet.load_error'))
      }
      if (!input) return
      this.params = resolveSpreadsheetOuterParams(input.config, input.attachParams)
    })
    // 无查询资源或数据区域时仍须提示失败，不替换 ready，避免错误被当作无参数放行。
    void this.ready.catch(error => this.notifyError(error))
  }

  completeInitialization(valuesReady?: Promise<void>): void {
    void Promise.all([this.ready, valuesReady])
      .then(() => {
        if (this._disposed || this.errorNotified) return
        this.onReady?.()
      })
      .catch(error => this.notifyError(error))
  }

  applyToFilterConfig(
    config: SpreadsheetFilterConfig
  ): Promise<SpreadsheetFilterConfig> | undefined {
    // 编辑页及编辑器内预览没有外参运行态，保留原有同步初始化顺序。
    if (!this.ready) return undefined
    return this.ready
      .then(() => {
        if (this._disposed) throw new Error(useI18n().t('spreadsheet.load_error'))
        if (!this.params) return config
        return applySpreadsheetOuterParamsToFilterConfig(config, this.params)
      })
      .catch(error => {
        this.notifyError(error)
        throw error
      })
  }

  async getFieldFilters(unitId: string, pluginId: string): Promise<SpreadsheetQueryFilterItem[]> {
    await this.ready
    if (
      this._disposed ||
      !this.univerInstanceService.getUnit(unitId, UniverInstanceType.UNIVER_SHEET)
    ) {
      throw new Error(useI18n().t('spreadsheet.load_error'))
    }
    return cloneDeep(this.params?.fieldFilters[pluginId] || [])
  }

  private notifyError(error: unknown): void {
    if (this._disposed || this.abortController?.signal.aborted || this.errorNotified) return
    this.errorNotified = true
    if (this.onError) {
      this.onError(error)
      return
    }
    ElMessage.error(error instanceof Error ? error.message : useI18n().t('spreadsheet.load_error'))
  }

  override dispose(): void {
    this.abortController?.abort()
    this.abortController = undefined
    this.params = undefined
    this.onReady = undefined
    this.onError = undefined
    super.dispose()
  }
}
