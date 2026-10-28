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

const compatibilityPatch = createUniverLocaleCompatibilityPatch({
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

export const DataEaseToolbarUIZhTW = {
  ...compatibilityPatch,
  'dataease-spreadsheet': {
    menu: {
      add: '新增',
      insert: '插入',
      pivotTable: '樞紐分析表',
      insertPivotTable: '插入樞紐分析表',
      detailTable: '明細表',
      insertDetailTable: '插入明細表',
      link: '連結',
      note: '註解',
      queryControl: '查詢控制項',
      queryControlShort: '查詢控件',
      pasteTable: '貼上表格',
      freeze: '凍結',
      freezeToRow: '凍結至目前列',
      freezeToColumn: '凍結至目前欄',
      freezeToRowColumn: '凍結至目前列與欄',
      unfreeze: '取消凍結',
      function: '函數',
      noFunctions: '暫無可用函數',
      slashCell: '斜線儲存格',
      slashCellTwo: '二分斜線儲存格',
      slashCellThree: '三分斜線儲存格',
      removeSlashCell: '取消斜線儲存格',
      insertImage: '插入圖片',
      floatingImage: '浮動圖片',
      cellImage: '儲存格圖片'
    }
  },
  'dataease-conditional-formatting': {
    colorScaleTwoValues: '色階數值應符合：最小值 < 最大值',
    colorScaleThreeValues: '色階數值應符合：最小值 < 中間值 < 最大值'
  }
}
