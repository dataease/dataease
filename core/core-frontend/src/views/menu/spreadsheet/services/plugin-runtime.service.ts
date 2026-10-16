import type { FUniver } from '@univerjs/core/facade'
import type { PluginConfig } from '../types/plugin'

export interface SpreadsheetPluginRuntime<T extends PluginConfig = PluginConfig> {
  type: T['type']
  ensureFieldOwnerId?(ctx: { univerApi: FUniver; config: T }): string | undefined
  refreshData?(ctx: { univerApi: FUniver; config: T }): Promise<boolean | void>
  applyStyle?(ctx: { univerApi: FUniver; config: T }): Promise<boolean | void>
  validateConfigUpdate?(ctx: {
    univerApi: FUniver
    config: T
    key: string
    value: any
  }): string | undefined | Promise<string | undefined>
  removeDraft?(ctx: { univerApi: FUniver; config: T }): Promise<void>
}

export class SpreadsheetPluginRuntimeRegistry {
  private readonly _runtimes = new Map<string, SpreadsheetPluginRuntime>()

  register(runtime: SpreadsheetPluginRuntime): void {
    this._runtimes.set(runtime.type, runtime)
  }

  get(type: string): SpreadsheetPluginRuntime | undefined {
    return this._runtimes.get(type)
  }
}

export const pluginRuntimeRegistry = new SpreadsheetPluginRuntimeRegistry()
