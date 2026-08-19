import DataValidationZhTW from '@univerjs/data-validation/locale/zh-TW'
import SheetsConditionalFormattingUIZhTW from '@univerjs/sheets-conditional-formatting-ui/locale/zh-TW'
import SheetsDataValidationZhTW from '@univerjs/sheets-data-validation/locale/zh-TW'
import SheetsFilterZhTW from '@univerjs/sheets-filter/locale/zh-TW'
import SheetsHyperLinkZhTW from '@univerjs/sheets-hyper-link/locale/zh-TW'
import SheetsNumfmtUIZhTW from '@univerjs/sheets-numfmt-ui/locale/zh-TW'
import SheetsTableZhTW from '@univerjs/sheets-table/locale/zh-TW'
import SheetsUIZhTW from '@univerjs/sheets-ui/locale/zh-TW'
import SheetsZhTW from '@univerjs/sheets/locale/zh-TW'
import { createUniverLocaleCompatibilityPatch } from './create-locale-compat'

const conditionalFormattingIconSet =
  SheetsConditionalFormattingUIZhTW['sheets-conditional-formatting-ui'].iconSet
const sheetsUIPermissionDialog = SheetsUIZhTW['sheets-ui'].permission.dialog
const numberFormatInfo = SheetsNumfmtUIZhTW['sheets-numfmt-ui'].info
const sheetsMergeConfirm = SheetsZhTW.sheets.merge.confirm

export const DataEaseToolbarUIZhTW = createUniverLocaleCompatibilityPatch({
  officialLocales: [
    DataValidationZhTW,
    SheetsDataValidationZhTW,
    SheetsFilterZhTW,
    SheetsHyperLinkZhTW,
    SheetsTableZhTW
  ],
  conditionalFormattingIconSet,
  permissionDialog: sheetsUIPermissionDialog,
  numberFormatInfo,
  mergeConfirm: sheetsMergeConfirm,
  missingText: {
    findReplacePartialSuccess: '已取代 {0} 個相符項目，{1} 個取代失敗',
    formulaProgress: {
      analyzing: '正在分析公式',
      calculating: '正在計算公式',
      arrayAnalysis: '正在分析陣列公式',
      arrayCalculation: '正在計算陣列公式',
      done: '公式計算完成'
    },
    filterConfirm: {
      error: '無法執行此操作',
      notAllowedToInsertRange: '此範圍包含篩選列，無法執行此操作。'
    },
    ribbonMenu: '功能區選單'
  }
})
