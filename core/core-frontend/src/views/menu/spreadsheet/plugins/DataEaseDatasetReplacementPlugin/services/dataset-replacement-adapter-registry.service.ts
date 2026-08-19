import { Disposable } from '@univerjs/core'
import type { DatasetReplacementAdapter } from '../types'

export class DatasetReplacementAdapterRegistry extends Disposable {
  private readonly _adapters = new Map<string, DatasetReplacementAdapter>()

  register(adapter: DatasetReplacementAdapter): void {
    this._adapters.set(adapter.type, adapter)
  }

  unregister(type: string): void {
    this._adapters.delete(type)
  }

  get(type: string): DatasetReplacementAdapter | undefined {
    return this._adapters.get(type)
  }

  getAll(): DatasetReplacementAdapter[] {
    return Array.from(this._adapters.values())
  }

  override dispose(): void {
    this._adapters.clear()
    super.dispose()
  }
}
