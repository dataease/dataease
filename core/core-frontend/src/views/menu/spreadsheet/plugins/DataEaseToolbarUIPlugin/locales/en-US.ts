import DataValidationEnUS from '@univerjs/data-validation/locale/en-US'
import SheetsConditionalFormattingUIEnUS from '@univerjs/sheets-conditional-formatting-ui/locale/en-US'
import SheetsDataValidationEnUS from '@univerjs/sheets-data-validation/locale/en-US'
import SheetsFilterEnUS from '@univerjs/sheets-filter/locale/en-US'
import SheetsHyperLinkEnUS from '@univerjs/sheets-hyper-link/locale/en-US'
import SheetsNumfmtUIEnUS from '@univerjs/sheets-numfmt-ui/locale/en-US'
import SheetsTableEnUS from '@univerjs/sheets-table/locale/en-US'
import SheetsUIEnUS from '@univerjs/sheets-ui/locale/en-US'
import SheetsEnUS from '@univerjs/sheets/locale/en-US'
import { createUniverLocaleCompatibilityPatch } from './create-locale-compat'

const conditionalFormattingIconSet =
  SheetsConditionalFormattingUIEnUS['sheets-conditional-formatting-ui'].iconSet
const sheetsUIPermissionDialog = SheetsUIEnUS['sheets-ui'].permission.dialog
const numberFormatInfo = SheetsNumfmtUIEnUS['sheets-numfmt-ui'].info
const sheetsMergeConfirm = SheetsEnUS.sheets.merge.confirm

export const DataEaseToolbarUIEnUS = createUniverLocaleCompatibilityPatch({
  officialLocales: [
    DataValidationEnUS,
    SheetsDataValidationEnUS,
    SheetsFilterEnUS,
    SheetsHyperLinkEnUS,
    SheetsTableEnUS
  ],
  conditionalFormattingIconSet,
  permissionDialog: sheetsUIPermissionDialog,
  numberFormatInfo,
  mergeConfirm: sheetsMergeConfirm,
  missingText: {
    findReplacePartialSuccess: 'Replaced {0} matches; {1} replacements failed',
    formulaProgress: {
      analyzing: 'Analyzing formulas',
      calculating: 'Calculating formulas',
      arrayAnalysis: 'Analyzing array formulas',
      arrayCalculation: 'Calculating array formulas',
      done: 'Formula calculation complete'
    },
    filterConfirm: {
      error: 'Cannot perform this operation',
      notAllowedToInsertRange: 'This operation is not allowed because the range contains filtered rows.'
    },
    ribbonMenu: 'Ribbon menu'
  }
})
