import { RedoCommand, UndoCommand } from '@univerjs/core'
import { OpenFindDialogOperation } from '@univerjs/find-replace'
import {
  AddDecimalCommand,
  SetCurrencyCommand,
  SetPercentCommand,
  SubtractDecimalCommand
} from '@univerjs/sheets-numfmt'
import {
  InsertFunctionOperation,
  MoreFunctionsOperation
} from '@univerjs/sheets-formula-ui'
import { OpenConditionalFormattingOperator } from '@univerjs/sheets-conditional-formatting-ui'
import {
  CancelFrozenCommand,
  ClearSelectionAllCommand,
  AddWorksheetMergeCommand,
  SetBackgroundColorCommand,
  SetBorderBasicCommand,
  SetHorizontalTextAlignCommand,
  SetTextRotationCommand,
  SetTextWrapCommand,
  SetVerticalTextAlignCommand,
  TextToNumberCommand
} from '@univerjs/sheets'
import {
  SetColumnFrozenCommand,
  SetRangeBoldCommand,
  SetRangeFontDecreaseCommand,
  SetRangeFontFamilyCommand,
  SetRangeFontIncreaseCommand,
  SetRangeFontSizeCommand,
  SetRangeItalicCommand,
  SetRangeStrickThroughCommand,
  SetRangeTextColorCommand,
  SetRangeUnderlineCommand,
  SetOnceFormatPainterCommand,
  SetRowFrozenCommand,
  SetSelectionFrozenCommand
} from '@univerjs/sheets-ui'
import {
  RibbonPosition,
  RibbonStartGroup,
  ToggleShortcutPanelOperation
} from '@univerjs/ui'
import { DATAEASE_SLASH_CELL_DROPDOWN_ID } from '../../DataEaseSlashCellPlugin/controllers/menu'

export const DATAEASE_RIBBON_POSITION_ORDER = [
  RibbonPosition.START,
  RibbonPosition.INSERT,
  RibbonPosition.FORMULAS,
  RibbonPosition.DATA,
  RibbonPosition.VIEW,
  RibbonPosition.OTHERS
]

export const DATAEASE_INSERT_DROPDOWN_MENU_ID = 'dataease.operation.insert-dropdown'
export const DATAEASE_QUERY_CONTROL_MENU_ID = 'dataease.operation.toggle-spreadsheet-filter'
export const DATAEASE_QUERY_CONTROL_GROUP_KEY = 'dataease.ribbon.query-control'
export const DATAEASE_HISTORY_TOOL_GROUP_KEY = RibbonStartGroup.HISTORY
export const DATAEASE_FONT_TOOL_GROUP_KEY = RibbonStartGroup.FORMAT
export const DATAEASE_LAYOUT_TOOL_GROUP_KEY = RibbonStartGroup.LAYOUT
export const DATAEASE_NUMFMT_TOOL_GROUP_KEY = 'dataease.ribbon.numfmt-tools'
export const DATAEASE_DATA_TOOL_GROUP_KEY = 'dataease.ribbon.data-tools'
export const DATAEASE_UTILITY_TOOL_GROUP_KEY = 'dataease.ribbon.utility-tools'
export const DATAEASE_FREEZE_DROPDOWN_MENU_ID = 'dataease.operation.freeze-dropdown'
export const DATAEASE_FUNCTION_DROPDOWN_MENU_ID = 'dataease.operation.function-dropdown'
export const DATAEASE_FUNCTION_DROPDOWN_COMPONENT = 'DataEaseFunctionDropdown'
export const DATAEASE_INSERT_DROPDOWN_COMPONENT = 'DataEaseInsertDropdown'

export const DATAEASE_SET_ROW_FROZEN_COMMAND_ID = SetRowFrozenCommand.id
export const DATAEASE_SET_COLUMN_FROZEN_COMMAND_ID = SetColumnFrozenCommand.id
export const DATAEASE_SET_SELECTION_FROZEN_COMMAND_ID = SetSelectionFrozenCommand.id
export const DATAEASE_CANCEL_FROZEN_COMMAND_ID = CancelFrozenCommand.id
// Source: @univerjs/sheets-numfmt-ui registers this private operation in its ribbon schema.
export const DATAEASE_NUMFMT_SELECTOR_MENU_ID = 'sheet.operation.open.numfmt.panel'
// SmartToggleSheetsFilterCommand is not exported by @univerjs/sheets-filter-ui.
export const DATAEASE_FILTER_MENU_ID = 'sheet.command.smart-toggle-filter'
// Source: @univerjs/sheets-sort-ui registers this private menu in RibbonDataGroup.ORGANIZATION.
export const DATAEASE_SORT_MENU_ID = 'sheet.menu.sheets-sort'
// Source: @univerjs/sheets-data-validation-ui registers this private menu in RibbonDataGroup.RULES.
export const DATAEASE_DATA_VALIDATION_MENU_ID = 'sheet.menu.data-validation'
export const DATAEASE_CONDITIONAL_FORMATTING_MENU_ID = OpenConditionalFormattingOperator.id
// Source: @univerjs/sheets-hyper-link-ui registers InsertHyperLinkToolbarOperation in RibbonInsertGroup.MEDIA.
export const DATAEASE_INSERT_LINK_MENU_ID = 'sheet.operation.insert-hyper-link-toolbar'
// Source: @univerjs/sheets-note-ui AddNotePopupOperation.
export const DATAEASE_ADD_NOTE_MENU_ID = 'sheet.operation.add-note-popup'
export const DATAEASE_SLASH_CELL_MENU_ID = DATAEASE_SLASH_CELL_DROPDOWN_ID
// Source: @univerjs/sheets-table-ui registers OpenTableSelectorOperation in RibbonDataGroup.ORGANIZATION.
export const DATAEASE_TABLE_MENU_ID = 'sheet.operation.open-table-selector'
export const DATAEASE_SHORTCUT_PANEL_MENU_ID = ToggleShortcutPanelOperation.id
export const DATAEASE_FIND_REPLACE_MENU_ID = OpenFindDialogOperation.id
export const DATAEASE_INSERT_FUNCTION_MENU_ID_PREFIX = InsertFunctionOperation.id
export const DATAEASE_MORE_FUNCTIONS_MENU_ID = MoreFunctionsOperation.id

export const DATAEASE_BUSINESS_MENU_IDS = new Set([
  DATAEASE_INSERT_DROPDOWN_MENU_ID,
  DATAEASE_QUERY_CONTROL_MENU_ID
])

export const DATAEASE_HIDDEN_RIBBON_MENU_IDS = new Set([
  TextToNumberCommand.id,
  SetRangeFontIncreaseCommand.id,
  SetRangeFontDecreaseCommand.id,
  SetTextRotationCommand.id,
  DATAEASE_MORE_FUNCTIONS_MENU_ID
])

type DataEaseRibbonGroupBaseConfig = {
  key: string
  className?: string
}

export type DataEaseRibbonGroupConfig = DataEaseRibbonGroupBaseConfig & (
  | { items: string[]; rows?: never }
  | { rows: string[][]; items?: never }
)

const DATAEASE_FONT_TOOL_ITEM_ROWS = [
  [
    SetRangeFontFamilyCommand.id,
    SetRangeFontSizeCommand.id,
    SetBorderBasicCommand.id
  ],
  [
    SetRangeBoldCommand.id,
    SetRangeItalicCommand.id,
    SetRangeStrickThroughCommand.id,
    SetRangeUnderlineCommand.id,
    SetRangeTextColorCommand.id,
    SetBackgroundColorCommand.id,
    DATAEASE_SLASH_CELL_MENU_ID
  ]
]

const DATAEASE_HISTORY_TOOL_ITEM_ROWS = [
  [
    UndoCommand.id,
    RedoCommand.id
  ],
  [
    SetOnceFormatPainterCommand.id,
    ClearSelectionAllCommand.id
  ]
]

const DATAEASE_LAYOUT_TOOL_ITEM_ROWS = [
  [
    SetHorizontalTextAlignCommand.id,
    SetVerticalTextAlignCommand.id,
    SetTextWrapCommand.id
  ],
  [
    AddWorksheetMergeCommand.id
  ]
]

const DATAEASE_NUMFMT_TOOL_ITEM_ROWS = [
  [
    DATAEASE_NUMFMT_SELECTOR_MENU_ID
  ],
  [
    SetPercentCommand.id,
    SetCurrencyCommand.id,
    AddDecimalCommand.id,
    SubtractDecimalCommand.id
  ]
]

const DATAEASE_DATA_TOOL_ITEM_ROWS = [
  [
    DATAEASE_FREEZE_DROPDOWN_MENU_ID,
    DATAEASE_FILTER_MENU_ID,
    DATAEASE_SORT_MENU_ID
  ],
  [
    DATAEASE_DATA_VALIDATION_MENU_ID,
    DATAEASE_CONDITIONAL_FORMATTING_MENU_ID,
    DATAEASE_FUNCTION_DROPDOWN_MENU_ID
  ]
]

const DATAEASE_UTILITY_TOOL_ITEM_ROWS = [
  [
    DATAEASE_TABLE_MENU_ID
  ],
  [
    DATAEASE_SHORTCUT_PANEL_MENU_ID,
    DATAEASE_FIND_REPLACE_MENU_ID
  ]
]

export const DATAEASE_RIBBON_GROUPS: DataEaseRibbonGroupConfig[] = [
  {
    key: RibbonStartGroup.OTHERS,
    items: [DATAEASE_INSERT_DROPDOWN_MENU_ID]
  },
  {
    key: DATAEASE_HISTORY_TOOL_GROUP_KEY,
    className: 'dataease-spreadsheet-ribbon__group--history-tools',
    rows: DATAEASE_HISTORY_TOOL_ITEM_ROWS
  },
  {
    key: DATAEASE_FONT_TOOL_GROUP_KEY,
    className: 'dataease-spreadsheet-ribbon__group--font-tools',
    rows: DATAEASE_FONT_TOOL_ITEM_ROWS
  },
  {
    key: DATAEASE_LAYOUT_TOOL_GROUP_KEY,
    className: 'dataease-spreadsheet-ribbon__group--layout-tools',
    rows: DATAEASE_LAYOUT_TOOL_ITEM_ROWS
  },
  {
    key: DATAEASE_NUMFMT_TOOL_GROUP_KEY,
    className: 'dataease-spreadsheet-ribbon__group--numfmt-tools',
    rows: DATAEASE_NUMFMT_TOOL_ITEM_ROWS
  },
  {
    key: DATAEASE_DATA_TOOL_GROUP_KEY,
    className: 'dataease-spreadsheet-ribbon__group--data-tools',
    rows: DATAEASE_DATA_TOOL_ITEM_ROWS
  },
  {
    key: DATAEASE_UTILITY_TOOL_GROUP_KEY,
    className: 'dataease-spreadsheet-ribbon__group--utility-tools',
    rows: DATAEASE_UTILITY_TOOL_ITEM_ROWS
  },
  {
    key: DATAEASE_QUERY_CONTROL_GROUP_KEY,
    items: [DATAEASE_QUERY_CONTROL_MENU_ID]
  }
]
