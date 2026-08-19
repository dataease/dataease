import { Disposable } from '@univerjs/core'
import type { SpreadsheetMode } from '../types/mode'

export class SpreadsheetModeService extends Disposable {
  private systemWriteDepth = 0

  constructor(private readonly mode: SpreadsheetMode) {
    super()
  }

  getMode(): SpreadsheetMode {
    return this.mode
  }

  isPreview(): boolean {
    return this.mode === 'preview'
  }

  isSystemWrite(): boolean {
    return this.systemWriteDepth > 0
  }

  runAsSystemWrite<T>(handler: () => T): T {
    this.systemWriteDepth += 1
    try {
      const result = handler()
      if (result instanceof Promise) {
        return result.finally(() => {
          this.systemWriteDepth -= 1
        }) as T
      }

      this.systemWriteDepth -= 1
      return result
    } catch (error) {
      this.systemWriteDepth -= 1
      throw error
    }
  }
}
