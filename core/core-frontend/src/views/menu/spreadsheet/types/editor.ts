import type { IRangeSelectResult } from '../plugins/RangeSelectPlugin/type'
import type { PluginConfig } from './plugin'

export interface PluginEditPayload {
  config: PluginConfig
  [key: string]: any
}

export type PluginActionToolbarType = 'detail' | 'pivot'

export interface PluginActionToolbarPayload {
  type: PluginActionToolbarType
  pluginId: string
  unitId: string
  sheetId: string
  startCell: string
  rowCount: number
  columnCount: number
  config: PluginConfig
  position: {
    left: number
    top: number
  }
  /** 需要禁用的操作（如渲染失败的占位符禁用复制/剪切）。 */
  disabledActions?: Array<'copy' | 'cut'>
}

export interface ITableCreateResult {
  pluginType: string
  placement: 'new' | 'existing'
  range?: IRangeSelectResult
  resultLimit?: number
}

export interface ITableCreateDialogParams {
  pluginType: string
  callback: (result: ITableCreateResult) => void
  initialRange?: IRangeSelectResult
  onClose?: () => void
  canClose?: () => boolean
  validateRange?: (
    range: IRangeSelectResult,
    resultLimit: number,
    silent?: boolean
  ) => string | undefined
}
