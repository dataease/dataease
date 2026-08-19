import type { PivotTableConfig } from '../types'

export class PivotTableInstanceService {
  private readonly _pluginsByUnitId = new Map<string, PivotTableConfig[]>()

  get(unitId: string): PivotTableConfig[] {
    return this._pluginsByUnitId.get(unitId) || []
  }

  set(unitId: string, instances: PivotTableConfig[]): void {
    this._pluginsByUnitId.set(unitId, instances)
  }

  delete(unitId: string): void {
    this._pluginsByUnitId.delete(unitId)
  }

  addOrUpdate(unitId: string, config: PivotTableConfig): void {
    const instances = [...this.get(unitId)]
    const index = instances.findIndex(plugin => plugin.id === config.id)
    if (index >= 0) {
      instances[index] = config
    } else {
      instances.push(config)
    }
    this._pluginsByUnitId.set(unitId, instances)
  }

  remove(unitId: string, pluginId: string): void {
    this._pluginsByUnitId.set(
      unitId,
      this.get(unitId).filter(plugin => plugin.id !== pluginId)
    )
  }
}
