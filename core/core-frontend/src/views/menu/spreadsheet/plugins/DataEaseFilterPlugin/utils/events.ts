import { useEmitt } from '@/hooks/web/useEmitt'
import type { SpreadsheetFilterConfig } from '../../../types/plugin'

export interface SpreadsheetFilterQueryPayload {
  unitId?: string
  config?: SpreadsheetFilterConfig
  values: Record<string, unknown>
  affectedPluginIds?: string[]
}

export interface SpreadsheetFilterConfigDialogOptions {
  selectedConditionId?: string
  initialAction?: 'add'
}

export interface SpreadsheetFilterAvailableField {
  fieldId: string | number
  fieldName: string
  groupType: 'd' | 'q'
  deType?: number
  desensitized?: boolean
}

export interface SpreadsheetFilterAvailablePlugin {
  pluginId: string
  pluginName: string
  pluginType: 'detail' | 'pivot'
  datasetId?: string | number
  datasetName: string
  fields: SpreadsheetFilterAvailableField[]
}

export interface SpreadsheetFilterConfigDialogPayload extends SpreadsheetFilterConfigDialogOptions {
  config: SpreadsheetFilterConfig
  availablePlugins: SpreadsheetFilterAvailablePlugin[]
}

export interface SpreadsheetFilterConfigContextRequestPayload extends SpreadsheetFilterConfigDialogOptions {
  onReady: (payload: SpreadsheetFilterConfigDialogPayload) => void
}

export interface SpreadsheetFilterSaveConfigPayload {
  config: SpreadsheetFilterConfig
}

export interface SpreadsheetFilterOpenStylePayload {
  skipWhenSidebarVisible?: boolean
}

export const SPREADSHEET_FILTER_EVENTS = {
  VISIBLE_CHANGE: 'spreadsheet-filter-visible-change',
  CONFIG_CHANGE: 'spreadsheet-filter-config-change',
  REQUEST_CONFIG_CONTEXT: 'spreadsheet-filter-request-config-context',
  SAVE_CONFIG: 'spreadsheet-filter-save-config',
  QUERY: 'spreadsheet-filter-query',
  CLEAR: 'spreadsheet-filter-clear',
  RESET: 'spreadsheet-filter-reset',
  DELETE_CONDITION: 'spreadsheet-filter-delete-condition',
  DISABLE: 'spreadsheet-filter-disable',
  OPEN_STYLE: 'spreadsheet-filter-open-style'
} as const

let currentVisible = false
let currentConfig: SpreadsheetFilterConfig | undefined
const { emitter } = useEmitt()

export const getSpreadsheetFilterVisible = () => currentVisible
export const getSpreadsheetFilterConfig = () => currentConfig

export const resetSpreadsheetFilterEventState = () => {
  currentVisible = false
  currentConfig = undefined
}

export const dispatchSpreadsheetFilterVisibleChange = (visible: boolean) => {
  currentVisible = visible
  emitter.emit(SPREADSHEET_FILTER_EVENTS.VISIBLE_CHANGE, visible)
}

export const dispatchSpreadsheetFilterConfigChange = (config: SpreadsheetFilterConfig) => {
  currentConfig = config
  currentVisible = config.visible
  emitter.emit(SPREADSHEET_FILTER_EVENTS.CONFIG_CHANGE, config)
}

export const dispatchSpreadsheetFilterRequestConfigContext = (
  payload: SpreadsheetFilterConfigContextRequestPayload
) => {
  emitter.emit(SPREADSHEET_FILTER_EVENTS.REQUEST_CONFIG_CONTEXT, payload)
}

export const dispatchSpreadsheetFilterSaveConfig = (payload: SpreadsheetFilterSaveConfigPayload) => {
  emitter.emit(SPREADSHEET_FILTER_EVENTS.SAVE_CONFIG, payload)
}

export const dispatchSpreadsheetFilterQuery = (payload: SpreadsheetFilterQueryPayload) => {
  emitter.emit(SPREADSHEET_FILTER_EVENTS.QUERY, payload)
}

export const dispatchSpreadsheetFilterClear = (payload: SpreadsheetFilterQueryPayload) => {
  emitter.emit(SPREADSHEET_FILTER_EVENTS.CLEAR, payload)
}

export const dispatchSpreadsheetFilterReset = (payload: SpreadsheetFilterQueryPayload) => {
  emitter.emit(SPREADSHEET_FILTER_EVENTS.RESET, payload)
}

export const dispatchSpreadsheetFilterDeleteCondition = (conditionId: string) => {
  emitter.emit(SPREADSHEET_FILTER_EVENTS.DELETE_CONDITION, conditionId)
}

export const dispatchSpreadsheetFilterDisable = () => {
  emitter.emit(SPREADSHEET_FILTER_EVENTS.DISABLE)
}

export const dispatchSpreadsheetFilterOpenStyle = (payload?: SpreadsheetFilterOpenStylePayload) => {
  emitter.emit(SPREADSHEET_FILTER_EVENTS.OPEN_STYLE, payload)
}

export const onSpreadsheetFilterVisibleChange = (handler: (visible: boolean) => void) => {
  emitter.on(SPREADSHEET_FILTER_EVENTS.VISIBLE_CHANGE, handler as (visible: unknown) => void)
}

export const offSpreadsheetFilterVisibleChange = (handler: (visible: boolean) => void) => {
  emitter.off(SPREADSHEET_FILTER_EVENTS.VISIBLE_CHANGE, handler as (visible: unknown) => void)
}

export const onSpreadsheetFilterConfigChange = (
  handler: (config: SpreadsheetFilterConfig) => void
) => {
  emitter.on(SPREADSHEET_FILTER_EVENTS.CONFIG_CHANGE, handler as (config: unknown) => void)
}

export const offSpreadsheetFilterConfigChange = (
  handler: (config: SpreadsheetFilterConfig) => void
) => {
  emitter.off(SPREADSHEET_FILTER_EVENTS.CONFIG_CHANGE, handler as (config: unknown) => void)
}

export const onSpreadsheetFilterRequestConfigContext = (
  handler: (payload: SpreadsheetFilterConfigContextRequestPayload) => void
) => {
  emitter.on(SPREADSHEET_FILTER_EVENTS.REQUEST_CONFIG_CONTEXT, handler as (payload: unknown) => void)
}

export const offSpreadsheetFilterRequestConfigContext = (
  handler: (payload: SpreadsheetFilterConfigContextRequestPayload) => void
) => {
  emitter.off(SPREADSHEET_FILTER_EVENTS.REQUEST_CONFIG_CONTEXT, handler as (payload: unknown) => void)
}

export const onSpreadsheetFilterSaveConfig = (
  handler: (payload: SpreadsheetFilterSaveConfigPayload) => void
) => {
  emitter.on(SPREADSHEET_FILTER_EVENTS.SAVE_CONFIG, handler as (payload: unknown) => void)
}

export const offSpreadsheetFilterSaveConfig = (
  handler: (payload: SpreadsheetFilterSaveConfigPayload) => void
) => {
  emitter.off(SPREADSHEET_FILTER_EVENTS.SAVE_CONFIG, handler as (payload: unknown) => void)
}

export const onSpreadsheetFilterQuery = (handler: (payload: SpreadsheetFilterQueryPayload) => void) => {
  emitter.on(SPREADSHEET_FILTER_EVENTS.QUERY, handler as (payload: unknown) => void)
}

export const offSpreadsheetFilterQuery = (handler: (payload: SpreadsheetFilterQueryPayload) => void) => {
  emitter.off(SPREADSHEET_FILTER_EVENTS.QUERY, handler as (payload: unknown) => void)
}

export const onSpreadsheetFilterClear = (handler: (payload: SpreadsheetFilterQueryPayload) => void) => {
  emitter.on(SPREADSHEET_FILTER_EVENTS.CLEAR, handler as (payload: unknown) => void)
}

export const offSpreadsheetFilterClear = (handler: (payload: SpreadsheetFilterQueryPayload) => void) => {
  emitter.off(SPREADSHEET_FILTER_EVENTS.CLEAR, handler as (payload: unknown) => void)
}

export const onSpreadsheetFilterReset = (handler: (payload: SpreadsheetFilterQueryPayload) => void) => {
  emitter.on(SPREADSHEET_FILTER_EVENTS.RESET, handler as (payload: unknown) => void)
}

export const offSpreadsheetFilterReset = (handler: (payload: SpreadsheetFilterQueryPayload) => void) => {
  emitter.off(SPREADSHEET_FILTER_EVENTS.RESET, handler as (payload: unknown) => void)
}

export const onSpreadsheetFilterDeleteCondition = (handler: (conditionId: string) => void) => {
  emitter.on(SPREADSHEET_FILTER_EVENTS.DELETE_CONDITION, handler as (conditionId: unknown) => void)
}

export const offSpreadsheetFilterDeleteCondition = (handler: (conditionId: string) => void) => {
  emitter.off(SPREADSHEET_FILTER_EVENTS.DELETE_CONDITION, handler as (conditionId: unknown) => void)
}

export const onSpreadsheetFilterDisable = (handler: () => void) => {
  emitter.on(SPREADSHEET_FILTER_EVENTS.DISABLE, handler)
}

export const offSpreadsheetFilterDisable = (handler: () => void) => {
  emitter.off(SPREADSHEET_FILTER_EVENTS.DISABLE, handler)
}

export const onSpreadsheetFilterOpenStyle = (
  handler: (payload?: SpreadsheetFilterOpenStylePayload) => void
) => {
  emitter.on(SPREADSHEET_FILTER_EVENTS.OPEN_STYLE, handler as (payload: unknown) => void)
}

export const offSpreadsheetFilterOpenStyle = (
  handler: (payload?: SpreadsheetFilterOpenStylePayload) => void
) => {
  emitter.off(SPREADSHEET_FILTER_EVENTS.OPEN_STYLE, handler as (payload: unknown) => void)
}
