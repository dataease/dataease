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

const compatibilityPatch = createUniverLocaleCompatibilityPatch({
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

export const DataEaseToolbarUIZhCN = {
  ...compatibilityPatch,
  'dataease-spreadsheet': {
    menu: {
      add: '添加',
      insert: '插入',
      pivotTable: '透视表',
      insertPivotTable: '插入透视表',
      detailTable: '明细表',
      insertDetailTable: '插入明细表',
      link: '链接',
      note: '批注',
      queryControl: '查询控件',
      queryControlShort: '查询控件',
      pasteTable: '粘贴表格',
      freeze: '冻结',
      freezeToRow: '冻结至当前行',
      freezeToColumn: '冻结至当前列',
      freezeToRowColumn: '冻结至当前行列',
      unfreeze: '取消冻结',
      function: '函数',
      noFunctions: '暂无可用函数',
      slashCell: '斜线单元格',
      slashCellTwo: '二分斜线单元格',
      slashCellThree: '三分斜线单元格',
      removeSlashCell: '取消斜线单元格',
      insertImage: '插入图片',
      floatingImage: '浮动图片',
      cellImage: '单元格图片'
    }
  },
  'dataease-conditional-formatting': {
    colorScaleTwoValues: '色阶数值应满足：最小值 < 最大值',
    colorScaleThreeValues: '色阶数值应满足：最小值 < 中间值 < 最大值'
  }
}
