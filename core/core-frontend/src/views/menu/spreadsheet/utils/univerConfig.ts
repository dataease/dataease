import type { IWorkbookData } from '@univerjs/core'
import { LocaleType, Univer, UniverInstanceType } from '@univerjs/core'
import { FUniver } from '@univerjs/core/facade'
import { UniverUIPlugin } from '@univerjs/ui'
import { UniverDocsPlugin } from '@univerjs/docs'
import { UniverDocsUIPlugin } from '@univerjs/docs-ui'
import { UniverFormulaEnginePlugin } from '@univerjs/engine-formula'
import { UniverRenderEnginePlugin } from '@univerjs/engine-render'
import { UniverSheetsPlugin } from '@univerjs/sheets'
import { UniverSheetsConditionalFormattingUIPlugin } from '@univerjs/sheets-conditional-formatting-ui'
import { UniverSheetsCrosshairHighlightPlugin } from '@univerjs/sheets-crosshair-highlight'
import { UniverSheetsDataValidationPlugin } from '@univerjs/sheets-data-validation'
import { UniverSheetsDataValidationUIPlugin } from '@univerjs/sheets-data-validation-ui'
import { UniverSheetsFilterUIPlugin } from '@univerjs/sheets-filter-ui'
import { UniverSheetsFindReplacePlugin } from '@univerjs/sheets-find-replace'
import { UniverSheetsFormulaUIPlugin } from '@univerjs/sheets-formula-ui'
import { UniverSheetsHyperLinkUIPlugin } from '@univerjs/sheets-hyper-link-ui'
import { UniverSheetsNoteUIPlugin } from '@univerjs/sheets-note-ui'
import { UniverSheetsNumfmtUIPlugin } from '@univerjs/sheets-numfmt-ui'
import { UniverSheetsSortUIPlugin } from '@univerjs/sheets-sort-ui'
import { UniverSheetsTableUIPlugin } from '@univerjs/sheets-table-ui'
import { UniverSheetsUIPlugin } from '@univerjs/sheets-ui'
import { UniverWatermarkPlugin } from '@univerjs/watermark'
import { UniverVue3AdapterPlugin } from '@univerjs/ui-adapter-vue3'

import '@univerjs/sheets/facade'
import '@univerjs/sheets-ui/facade'

import { DataEaseDetailTablePlugin } from '../plugins/DataEaseDetailTablePlugin'
import { DataEasePivotTablePlugin } from '../plugins/DataEasePivotTablePlugin'
import { DataEaseTableClipboardPlugin } from '../plugins/DataEaseTableClipboardPlugin'
import { DataEaseFilterPlugin } from '../plugins/DataEaseFilterPlugin'
import { DataEaseDatasetReplacementPlugin } from '../plugins/DataEaseDatasetReplacementPlugin'
import { DataEaseImagePlugin } from '../plugins/DataEaseImagePlugin'
import { DataEaseSlashCellPlugin } from '../plugins/DataEaseSlashCellPlugin'
import { DataEaseToolbarUIPlugin } from '../plugins/DataEaseToolbarUIPlugin'
import {
  DataEaseToolbarUIEnUS,
  DataEaseToolbarUIZhCN,
  DataEaseToolbarUIZhTW,
  mergeLocalePatches
} from '../plugins/DataEaseToolbarUIPlugin/locales'
import { DATAEASE_PROTECTION_MENU_CONFIG } from '../plugins/DataEaseToolbarUIPlugin/config/protection-config'
import { DataEasePreviewModePlugin } from '../plugins/DataEasePreviewModePlugin'
import { DataEaseRuntimePlugin } from '../plugins/DataEaseRuntimePlugin'
import { PREVIEW_MENU_CONFIG } from '../plugins/DataEasePreviewModePlugin/config/preview-menu-config'
import { RangeSelectPlugin } from '../plugins/RangeSelectPlugin'
import type { SpreadsheetRuntimeOptions } from '../types/mode'

import DesignZhCN from '@univerjs/design/locale/zh-CN'
import DocsUIZhCN from '@univerjs/docs-ui/locale/zh-CN'
import FindReplaceZhCN from '@univerjs/find-replace/locale/zh-CN'
import SheetsConditionalFormattingUIZhCN from '@univerjs/sheets-conditional-formatting-ui/locale/zh-CN'
import SheetsCrosshairHighlightZhCN from '@univerjs/sheets-crosshair-highlight/locale/zh-CN'
import SheetsDataValidationUIZhCN from '@univerjs/sheets-data-validation-ui/locale/zh-CN'
import SheetsFilterUIZhCN from '@univerjs/sheets-filter-ui/locale/zh-CN'
import SheetsFormulaUIZhCN from '@univerjs/sheets-formula-ui/locale/zh-CN'
import SheetsFormulaZhCN from '@univerjs/sheets-formula/locale/zh-CN'
import SheetsHyperLinkUIZhCN from '@univerjs/sheets-hyper-link-ui/locale/zh-CN'
import SheetsNoteUIZhCN from '@univerjs/sheets-note-ui/locale/zh-CN'
import SheetsNumfmtUIZhCN from '@univerjs/sheets-numfmt-ui/locale/zh-CN'
import SheetsSortUIZhCN from '@univerjs/sheets-sort-ui/locale/zh-CN'
import SheetsTableUIZhCN from '@univerjs/sheets-table-ui/locale/zh-CN'
import SheetsUIZhCN from '@univerjs/sheets-ui/locale/zh-CN'
import SheetsZhCN from '@univerjs/sheets/locale/zh-CN'
import UIZhCN from '@univerjs/ui/locale/zh-CN'

import DesignZhTW from '@univerjs/design/locale/zh-TW'
import DocsUIZhTW from '@univerjs/docs-ui/locale/zh-TW'
import FindReplaceZhTW from '@univerjs/find-replace/locale/zh-TW'
import SheetsConditionalFormattingUIZhTW from '@univerjs/sheets-conditional-formatting-ui/locale/zh-TW'
import SheetsCrosshairHighlightZhTW from '@univerjs/sheets-crosshair-highlight/locale/zh-TW'
import SheetsDataValidationUIZhTW from '@univerjs/sheets-data-validation-ui/locale/zh-TW'
import SheetsFilterUIZhTW from '@univerjs/sheets-filter-ui/locale/zh-TW'
import SheetsFormulaZhTW from '@univerjs/sheets-formula/locale/zh-TW'
import SheetsFormulaUIZhTW from '@univerjs/sheets-formula-ui/locale/zh-TW'
import SheetsHyperLinkUIZhTW from '@univerjs/sheets-hyper-link-ui/locale/zh-TW'
import SheetsNoteUIZhTW from '@univerjs/sheets-note-ui/locale/zh-TW'
import SheetsNumfmtUIZhTW from '@univerjs/sheets-numfmt-ui/locale/zh-TW'
import SheetsSortUIZhTW from '@univerjs/sheets-sort-ui/locale/zh-TW'
import SheetsTableUIZhTW from '@univerjs/sheets-table-ui/locale/zh-TW'
import SheetsUIZhTW from '@univerjs/sheets-ui/locale/zh-TW'
import SheetsZhTW from '@univerjs/sheets/locale/zh-TW'
import UIZhTW from '@univerjs/ui/locale/zh-TW'

import DesignEnUS from '@univerjs/design/locale/en-US'
import DocsUIEnUS from '@univerjs/docs-ui/locale/en-US'
import FindReplaceEnUS from '@univerjs/find-replace/locale/en-US'
import SheetsConditionalFormattingUIEnUS from '@univerjs/sheets-conditional-formatting-ui/locale/en-US'
import SheetsCrosshairHighlightEnUS from '@univerjs/sheets-crosshair-highlight/locale/en-US'
import SheetsDataValidationUIEnUS from '@univerjs/sheets-data-validation-ui/locale/en-US'
import SheetsFilterUIEnUS from '@univerjs/sheets-filter-ui/locale/en-US'
import SheetsFormulaUIEnUS from '@univerjs/sheets-formula-ui/locale/en-US'
import SheetsFormulaEnUS from '@univerjs/sheets-formula/locale/en-US'
import SheetsHyperLinkUIEnUS from '@univerjs/sheets-hyper-link-ui/locale/en-US'
import SheetsNoteUIEnUS from '@univerjs/sheets-note-ui/locale/en-US'
import SheetsNumfmtUIEnUS from '@univerjs/sheets-numfmt-ui/locale/en-US'
import SheetsSortUIEnUS from '@univerjs/sheets-sort-ui/locale/en-US'
import SheetsTableUIEnUS from '@univerjs/sheets-table-ui/locale/en-US'
import SheetsUIEnUS from '@univerjs/sheets-ui/locale/en-US'
import SheetsEnUS from '@univerjs/sheets/locale/en-US'
import UIEnUS from '@univerjs/ui/locale/en-US'

import '@univerjs/design/lib/index.css'
import '@univerjs/ui/lib/index.css'
import '@univerjs/docs-ui/lib/index.css'
import '@univerjs/sheets-ui/lib/index.css'
import '@univerjs/sheets-formula-ui/lib/index.css'
import '@univerjs/sheets-numfmt-ui/lib/index.css'
import '@univerjs/sheets-sort-ui/lib/index.css'
import '@univerjs/sheets-filter-ui/lib/index.css'
import '@univerjs/find-replace/lib/index.css'
import '@univerjs/sheets-conditional-formatting-ui/lib/index.css'
import '@univerjs/sheets-data-validation-ui/lib/index.css'
import '@univerjs/sheets-hyper-link-ui/lib/index.css'
import '@univerjs/sheets-note-ui/lib/index.css'
import '@univerjs/sheets-table-ui/lib/index.css'
import '@univerjs/sheets-crosshair-highlight/lib/index.css'

export interface UniverInstance {
  univer: Univer
  univerApi: ReturnType<typeof FUniver.newAPI>
  dispose: () => void
}

const LOCALES_MAP = {
  [LocaleType.ZH_CN]: mergeLocalePatches(
    DesignZhCN,
    UIZhCN,
    DocsUIZhCN,
    SheetsZhCN,
    SheetsUIZhCN,
    SheetsFormulaUIZhCN,
    SheetsFormulaZhCN,
    SheetsNumfmtUIZhCN,
    SheetsFilterUIZhCN,
    SheetsConditionalFormattingUIZhCN,
    SheetsDataValidationUIZhCN,
    SheetsSortUIZhCN,
    FindReplaceZhCN,
    SheetsNoteUIZhCN,
    SheetsHyperLinkUIZhCN,
    SheetsTableUIZhCN,
    SheetsCrosshairHighlightZhCN,
    DataEaseToolbarUIZhCN
  ),
  [LocaleType.EN_US]: mergeLocalePatches(
    DesignEnUS,
    UIEnUS,
    DocsUIEnUS,
    SheetsEnUS,
    SheetsUIEnUS,
    SheetsFormulaUIEnUS,
    SheetsFormulaEnUS,
    SheetsNumfmtUIEnUS,
    SheetsFilterUIEnUS,
    SheetsConditionalFormattingUIEnUS,
    SheetsDataValidationUIEnUS,
    SheetsSortUIEnUS,
    FindReplaceEnUS,
    SheetsNoteUIEnUS,
    SheetsHyperLinkUIEnUS,
    SheetsTableUIEnUS,
    SheetsCrosshairHighlightEnUS,
    DataEaseToolbarUIEnUS
  ),
  [LocaleType.ZH_TW]: mergeLocalePatches(
    DesignZhTW,
    UIZhTW,
    DocsUIZhTW,
    SheetsZhTW,
    SheetsUIZhTW,
    SheetsFormulaUIZhTW,
    SheetsFormulaZhTW,
    SheetsNumfmtUIZhTW,
    SheetsFilterUIZhTW,
    SheetsConditionalFormattingUIZhTW,
    SheetsDataValidationUIZhTW,
    SheetsSortUIZhTW,
    FindReplaceZhTW,
    SheetsNoteUIZhTW,
    SheetsHyperLinkUIZhTW,
    SheetsTableUIZhTW,
    SheetsCrosshairHighlightZhTW,
    DataEaseToolbarUIZhTW
  )
}
/**
 * Create Univer instance with all necessary plugins
 * Following official example pattern from:
 * https://docs.univer.ai/playground/sheets/features/sheet-bar
 */
export function createUniverInstance(
  container: HTMLElement,
  locale = 'zh-CN',
  initialData?: Partial<IWorkbookData>,
  options: SpreadsheetRuntimeOptions = {}
): UniverInstance {
  const mode = options.mode ?? 'edit'

  // Map locale string to Univer LocaleType
  let localeType = LocaleType.ZH_CN
  if (locale === 'en-US' || locale === 'en_US' || locale === 'en') {
    localeType = LocaleType.EN_US
  } else if (locale === 'zh-TW' || locale === 'zh_TW') {
    localeType = LocaleType.ZH_TW
  }

  const univer = new Univer({
    locale: localeType,
    locales: LOCALES_MAP
  })

  univer.registerPlugin(UniverRenderEnginePlugin)
  univer.registerPlugin(UniverFormulaEnginePlugin)

  univer.registerPlugin(UniverUIPlugin, {
    container,
    menu: mode === 'preview' ? PREVIEW_MENU_CONFIG : undefined
  })

  univer.registerPlugin(UniverDocsPlugin)
  univer.registerPlugin(UniverDocsUIPlugin)

  univer.registerPlugin(UniverSheetsPlugin)
  univer.registerPlugin(UniverSheetsUIPlugin, {
    menu: DATAEASE_PROTECTION_MENU_CONFIG,
    protectedRangeShadow: false,
    disableEdit: mode === 'preview',
    footer: mode === 'preview'
      ? {
          addSheetButtonConfig: {
            show: false
          }
        }
      : undefined
  })
  univer.registerPlugin(UniverSheetsFormulaUIPlugin)
  univer.registerPlugin(UniverSheetsNumfmtUIPlugin)

  univer.registerPlugin(UniverSheetsDataValidationPlugin)
  univer.registerPlugin(UniverSheetsDataValidationUIPlugin)
  univer.registerPlugin(UniverSheetsConditionalFormattingUIPlugin)
  univer.registerPlugin(UniverSheetsFilterUIPlugin)
  univer.registerPlugin(UniverSheetsSortUIPlugin)
  univer.registerPlugin(UniverSheetsFindReplacePlugin)
  univer.registerPlugin(UniverSheetsHyperLinkUIPlugin)
  univer.registerPlugin(UniverSheetsTableUIPlugin)
  univer.registerPlugin(UniverSheetsNoteUIPlugin)
  /* univer.registerPlugin(UniverWatermarkPlugin, {
    textWatermarkSettings: {
      content: 'Hello, Univer!',
      fontSize: 16,
      color: 'rgb(0,0,0)',
      bold: false,
      italic: false,
      direction: 'ltr',
      x: 60,
      y: 36,
      repeat: true,
      spacingX: 200,
      spacingY: 100,
      rotate: 0,
      opacity: 0.15
    }
  }) */
  univer.registerPlugin(UniverSheetsCrosshairHighlightPlugin)
  univer.registerPlugin(UniverVue3AdapterPlugin)
  univer.registerPlugin(DataEasePreviewModePlugin, { mode })
  // 共享运行时服务必须先于具体业务插件注册，确保同一 Univer 内只创建一套实例。
  univer.registerPlugin(DataEaseRuntimePlugin)
  univer.registerPlugin(DataEaseImagePlugin)
  univer.registerPlugin(DataEaseDatasetReplacementPlugin)
  univer.registerPlugin(DataEaseFilterPlugin)
  univer.registerPlugin(DataEaseSlashCellPlugin)
  univer.registerPlugin(DataEaseDetailTablePlugin)
  univer.registerPlugin(DataEasePivotTablePlugin)
  univer.registerPlugin(DataEaseTableClipboardPlugin)
  univer.registerPlugin(RangeSelectPlugin)
  univer.registerPlugin(DataEaseToolbarUIPlugin)

  // Create unit with initial data or default
  const unitData = initialData || createDefaultWorkbookData()
  univer.createUnit(UniverInstanceType.UNIVER_SHEET, unitData)
  const univerApi = FUniver.newAPI(univer)
  let disposed = false

  return {
    univer,
    univerApi,
    dispose: () => {
      if (disposed) {
        return
      }
      disposed = true
      univer.dispose()
    }
  }
}

/**
 * Create default workbook data
 */
export function createDefaultWorkbookData(sheetName = 'Sheet1'): Partial<IWorkbookData> {
  const sheetId = 'sheet-01'
  return {
    id: 'workbook-01',
    sheetOrder: [sheetId],
    name: 'Workbook',
    locale: LocaleType.ZH_CN,
    sheets: {
      [sheetId]: {
        id: sheetId,
        name: sheetName,
        rowCount: 1000,
        columnCount: 26,
        zoomRatio: 1,
        scrollTop: 0,
        scrollLeft: 0,
        defaultColumnWidth: 93,
        defaultRowHeight: 27,
        mergeData: [],
        cellData: {},
        rowData: {},
        columnData: {},
        showGridlines: 1,
        rowHeader: {
          width: 46,
          hidden: 0
        },
        columnHeader: {
          height: 20,
          hidden: 0
        },
        selections: ['A1'],
        rightToLeft: 0
      }
    }
  } as Partial<IWorkbookData>
}

/**
 * Parse sheet data from JSON string
 */
export function parseSheetData(data: string | undefined): IWorkbookData | undefined {
  if (!data) return undefined
  try {
    return JSON.parse(data) as IWorkbookData
  } catch (e) {
    return undefined
  }
}

/**
 * Serialize sheet data to JSON string
 */
export function serializeSheetData(data: Partial<IWorkbookData>): string {
  return JSON.stringify(data)
}
