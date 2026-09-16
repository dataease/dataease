import type { Dependency } from '@univerjs/core'
import { Inject, Injector, Plugin, touchDependencies, UniverInstanceType } from '@univerjs/core'
import { DataEaseToolbarUIController } from './controllers/toolbar-ui.controller'
import { SheetMenuLayoutController } from './controllers/sheet-menu-layout.controller'
import { DecimalCommandController } from './controllers/decimal-command.controller'
import { ContextMenuLayoutController } from './controllers/context-menu-layout.controller'
import { FreezeCommandController } from './controllers/freeze-command.controller'
import { FormatPainterCursorController } from './controllers/format-painter-cursor.controller'
import { IconSetValueController } from './controllers/icon-set-value.controller'
import { DataValidationDraftController } from './controllers/data-validation-draft.controller'
import { TableFilterConflictController } from './controllers/table-filter-conflict.controller'
import { TableTextToNumberController } from './controllers/table-text-to-number.controller'
import { FilterNotEmptyController } from './controllers/filter-not-empty.controller'
import { MultiSelectionInsertController } from './controllers/multi-selection-insert.controller'
import { PasteColumnWidthController } from './controllers/paste-column-width.controller'
import './styles/toolbar.less'

export const DATAEASE_TOOLBAR_UI_PLUGIN = 'DATAEASE_TOOLBAR_UI_PLUGIN'

export class DataEaseToolbarUIPlugin extends Plugin {
  static override type = UniverInstanceType.UNIVER_SHEET
  static override pluginName = DATAEASE_TOOLBAR_UI_PLUGIN

  constructor(@Inject(Injector) protected readonly _injector: Injector) {
    super()
  }

  override onStarting(): void {
    const dependencies: Dependency[] = [
      [DataEaseToolbarUIController],
      [SheetMenuLayoutController],
      [ContextMenuLayoutController],
      [DecimalCommandController],
      [FreezeCommandController],
      [FormatPainterCursorController],
      [IconSetValueController],
      [DataValidationDraftController],
      [TableFilterConflictController],
      [TableTextToNumberController],
      [FilterNotEmptyController],
      [MultiSelectionInsertController],
      [PasteColumnWidthController]
    ]

    dependencies.forEach(dependency => this._injector.add(dependency))
  }

  override onRendered(): void {
    touchDependencies(this._injector, [
      [DataEaseToolbarUIController],
      [SheetMenuLayoutController],
      [ContextMenuLayoutController],
      [DecimalCommandController],
      [FreezeCommandController],
      [FormatPainterCursorController],
      [IconSetValueController],
      [DataValidationDraftController],
      [TableFilterConflictController],
      [TableTextToNumberController],
      [FilterNotEmptyController],
      [MultiSelectionInsertController],
      [PasteColumnWidthController]
    ])
  }
}
