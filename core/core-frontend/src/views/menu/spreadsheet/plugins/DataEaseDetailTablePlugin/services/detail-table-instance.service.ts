import type { DetailTableConfig } from '../types'

export class DetailTableInstanceService {
  private readonly _detailPluginsByUnitId = new Map<string, DetailTableConfig[]>()

  get(unitId: string): DetailTableConfig[] {
    return this._detailPluginsByUnitId.get(unitId) || []
  }

  set(unitId: string, instances: DetailTableConfig[]): void {
    this._detailPluginsByUnitId.set(unitId, instances)
  }

  delete(unitId: string): void {
    this._detailPluginsByUnitId.delete(unitId)
  }

  addOrUpdate(unitId: string, config: DetailTableConfig): void {
    const instances = this.get(unitId)
    const index = instances.findIndex(plugin => plugin.id === config.id)

    if (index >= 0) {
      instances[index] = config
    } else {
      instances.push(config)
    }

    this._detailPluginsByUnitId.set(unitId, instances)
  }

  remove(unitId: string, pluginId: string): void {
    this._detailPluginsByUnitId.set(
      unitId,
      this.get(unitId).filter(plugin => plugin.id !== pluginId)
    )
  }
}
