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

const compatibilityPatch = createUniverLocaleCompatibilityPatch({
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
      notAllowedToInsertRange:
        'This operation is not allowed because the range contains filtered rows.'
    },
    ribbonMenu: 'Ribbon menu'
  }
})

export const DataEaseToolbarUIEnUS = {
  ...compatibilityPatch,
  'dataease-spreadsheet': {
    menu: {
      add: 'Add',
      insert: 'Insert',
      pivotTable: 'Pivot table',
      insertPivotTable: 'Insert pivot table',
      detailTable: 'Detail table',
      insertDetailTable: 'Insert detail table',
      link: 'Link',
      note: 'Note',
      queryControl: 'Query control',
      queryControlShort: 'Query',
      pasteTable: 'Paste table',
      freeze: 'Freeze',
      freezeToRow: 'Freeze up to current row',
      freezeToColumn: 'Freeze up to current column',
      freezeToRowColumn: 'Freeze up to current row and column',
      unfreeze: 'Unfreeze',
      function: 'Function',
      noFunctions: 'No functions available',
      slashCell: 'Diagonal cell',
      slashCellTwo: 'Two-part diagonal cell',
      slashCellThree: 'Three-part diagonal cell',
      removeSlashCell: 'Remove diagonal lines',
      insertImage: 'Insert image',
      floatingImage: 'Floating image',
      cellImage: 'In-cell image'
    }
  },
  'dataease-conditional-formatting': {
    colorScaleTwoValues: 'Color scale values must satisfy: minimum < maximum',
    colorScaleThreeValues: 'Color scale values must satisfy: minimum < midpoint < maximum'
  }
}
