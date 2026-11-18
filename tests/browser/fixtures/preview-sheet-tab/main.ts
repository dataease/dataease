import {
  Inject,
  Injector,
  LocaleType,
  Plugin,
  Univer,
  UniverInstanceType,
  mergeLocales
} from '@univerjs/core'
import { UniverUIPlugin } from '@univerjs/ui'
import { UniverDocsPlugin } from '@univerjs/docs'
import { UniverDocsUIPlugin } from '@univerjs/docs-ui'
import { UniverRenderEnginePlugin } from '@univerjs/engine-render'
import { UniverSheetsPlugin } from '@univerjs/sheets'
import { UniverSheetsUIPlugin } from '@univerjs/sheets-ui'
import DesignEnUS from '@univerjs/design/locale/en-US'
import UIEnUS from '@univerjs/ui/locale/en-US'
import DocsUIEnUS from '@univerjs/docs-ui/locale/en-US'
import SheetsEnUS from '@univerjs/sheets/locale/en-US'
import SheetsUIEnUS from '@univerjs/sheets-ui/locale/en-US'
import '@univerjs/design/lib/index.css'
import '@univerjs/ui/lib/index.css'
import '@univerjs/docs-ui/lib/index.css'
import '@univerjs/sheets-ui/lib/index.css'
import { PreviewSheetTabContextMenuController } from 'preview-controller'
import { SpreadsheetModeService } from 'spreadsheet-mode'

// Observe only listeners installed while constructing the production controller.
const nativeAdd = EventTarget.prototype.addEventListener
const nativeRemove = EventTarget.prototype.removeEventListener
const registrations = []
let recording = false
EventTarget.prototype.addEventListener = function (type, listener, options) {
  if (!recording || type !== 'contextmenu') return nativeAdd.call(this, type, listener, options)
  const registration = { target: this, listener, options, calls: 0, active: true, wrapped: null }
  registration.wrapped = function (event) {
    registration.calls++
    return typeof listener === 'function' ? listener.call(this, event) : listener.handleEvent(event)
  }
  registrations.push(registration)
  return nativeAdd.call(this, type, registration.wrapped, options)
}
EventTarget.prototype.removeEventListener = function (type, listener, options) {
  const registration = registrations.find(
    item => item.active && item.target === this && item.listener === listener
  )
  if (type === 'contextmenu' && registration) {
    registration.active = false
    return nativeRemove.call(this, type, registration.wrapped, options)
  }
  return nativeRemove.call(this, type, listener, options)
}

let instance
let workbook
let ready = false
let generation = 0
class FixturePreviewPlugin extends Plugin {
  static override pluginName = 'PREVIEW_SHEET_TAB_FIXTURE'
  static override type = UniverInstanceType.UNIVER_SHEET
  constructor(private config, @Inject(Injector) private injector: Injector) {
    super()
  }
  override onStarting() {
    this.injector.add([
      SpreadsheetModeService,
      { useValue: new SpreadsheetModeService(this.config.mode) }
    ])
    this.injector.add([PreviewSheetTabContextMenuController])
  }
  override onRendered() {
    recording = true
    try {
      this.injector.get(PreviewSheetTabContextMenuController)
    } finally {
      recording = false
    }
    ready = true
  }
}
function mount(mode = 'preview') {
  ready = false
  instance?.dispose()
  document.querySelector('#sheet').replaceChildren()
  instance = new Univer({
    locale: LocaleType.EN_US,
    locales: {
      [LocaleType.EN_US]: mergeLocales(DesignEnUS, UIEnUS, DocsUIEnUS, SheetsEnUS, SheetsUIEnUS)
    }
  })
  instance.registerPlugin(UniverRenderEnginePlugin)
  instance.registerPlugin(UniverUIPlugin, {
    container: document.querySelector('#sheet'),
    header: false,
    toolbar: false
  })
  instance.registerPlugin(UniverDocsPlugin)
  instance.registerPlugin(UniverDocsUIPlugin)
  instance.registerPlugin(UniverSheetsPlugin)
  instance.registerPlugin(UniverSheetsUIPlugin, {
    disableEdit: mode === 'preview',
    formulaBar: false
  })
  instance.registerPlugin(FixturePreviewPlugin, { mode })
  workbook = instance.createUnit(UniverInstanceType.UNIVER_SHEET, {
    id: `preview-sheet-tab-${++generation}`,
    name: 'Preview regression',
    appVersion: '0.25.1',
    locale: LocaleType.EN_US,
    styles: {},
    sheetOrder: ['first', 'second'],
    sheets: Object.fromEntries(
      ['first', 'second'].map((id, index) => [
        id,
        {
          id,
          name: `Sheet ${index + 1}`,
          rowCount: 1000,
          columnCount: 50,
          cellData: { 0: { 0: { v: `Fixture ${index + 1}` } } }
        }
      ])
    )
  })
}
window.fixture = {
  mount,
  get ready() {
    return ready
  },
  get activeSheet() {
    return workbook.getActiveSheet().getSheetId()
  },
  stats() {
    return registrations.map(item => ({
      calls: item.calls,
      active: item.active,
      target: item.target.getAttribute('data-u-comp') || item.target.id,
      capture: typeof item.options === 'boolean' ? item.options : Boolean(item.options?.capture)
    }))
  },
  dispose() {
    const tab = document.querySelector('[data-u-comp="slide-tab-item"]')
    instance.dispose()
    instance = undefined
    ready = false
    const event = new MouseEvent('contextmenu', { bubbles: true, cancelable: true })
    tab.dispatchEvent(event)
    return event.defaultPrevented
  }
}
mount(new URLSearchParams(location.search).get('mode') || 'preview')
