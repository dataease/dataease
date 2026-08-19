import DataValidationZhCN from '@univerjs/data-validation/locale/zh-CN'
import SheetsConditionalFormattingUIZhCN from '@univerjs/sheets-conditional-formatting-ui/locale/zh-CN'
import SheetsDataValidationZhCN from '@univerjs/sheets-data-validation/locale/zh-CN'
import SheetsFilterZhCN from '@univerjs/sheets-filter/locale/zh-CN'
import SheetsHyperLinkZhCN from '@univerjs/sheets-hyper-link/locale/zh-CN'
import SheetsNumfmtUIZhCN from '@univerjs/sheets-numfmt-ui/locale/zh-CN'
import SheetsTableZhCN from '@univerjs/sheets-table/locale/zh-CN'
import SheetsUIZhCN from '@univerjs/sheets-ui/locale/zh-CN'
import SheetsZhCN from '@univerjs/sheets/locale/zh-CN'
import { createUniverLocaleCompatibilityPatch } from './create-locale-compat'

const conditionalFormattingIconSet =
  SheetsConditionalFormattingUIZhCN['sheets-conditional-formatting-ui'].iconSet
const sheetsUIPermissionDialog = SheetsUIZhCN['sheets-ui'].permission.dialog
const numberFormatInfo = SheetsNumfmtUIZhCN['sheets-numfmt-ui'].info
const sheetsMergeConfirm = SheetsZhCN.sheets.merge.confirm

export const DataEaseToolbarUIZhCN = createUniverLocaleCompatibilityPatch({
  officialLocales: [
    DataValidationZhCN,
    SheetsDataValidationZhCN,
    SheetsFilterZhCN,
    SheetsHyperLinkZhCN,
    SheetsTableZhCN
  ],
  conditionalFormattingIconSet,
  permissionDialog: sheetsUIPermissionDialog,
  numberFormatInfo,
  mergeConfirm: sheetsMergeConfirm,
  missingText: {
    findReplacePartialSuccess: '已替换 {0} 个匹配项，{1} 个替换失败',
    formulaProgress: {
      analyzing: '正在分析公式',
      calculating: '正在计算公式',
      arrayAnalysis: '正在分析数组公式',
      arrayCalculation: '正在计算数组公式',
      done: '公式计算完成'
    },
    filterConfirm: {
      error: '无法执行此操作',
      notAllowedToInsertRange: '该范围包含筛选行，无法执行此操作。'
    },
    ribbonMenu: '功能区菜单'
  }
})
