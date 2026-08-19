import type { IWorkbookData } from '@univerjs/core'
import type { PluginConfig } from '../types/plugin'
import { extractPluginInstancesFromWorkbook } from '../utils/plugin-resource'
import { clearUniverProtectionResources } from '../plugins/DataEaseToolbarUIPlugin/config/protection-config'

export interface PluginSnapshotCleaner<T extends PluginConfig = PluginConfig> {
  type: T['type']
  clean(snapshot: Partial<IWorkbookData>, plugin: T): Promise<void>
}

export class PluginSnapshotCleaningService {
  private readonly cleaners = new Map<string, PluginSnapshotCleaner>()

  constructor(cleaners: PluginSnapshotCleaner[] = []) {
    cleaners.forEach(cleaner => {
      this.register(cleaner)
    })
  }

  register(cleaner: PluginSnapshotCleaner): void {
    this.cleaners.set(cleaner.type, cleaner)
  }

  async clean(
    snapshot: Partial<IWorkbookData>,
    plugins: PluginConfig[] = extractPluginInstancesFromWorkbook(snapshot)
  ): Promise<Partial<IWorkbookData>> {
    // Univer 原生保护已全局禁用，在插件清理器运行前删除其资源，
    // 让加载和保存共用同一套数据边界。
    clearUniverProtectionResources(snapshot)

    for (const plugin of plugins) {
      const cleaner = this.cleaners.get(plugin.type)
      if (!cleaner) {
        continue
      }

      await cleaner.clean(snapshot, plugin as never)
    }

    return snapshot
  }
}

export const pluginSnapshotCleaningService = new PluginSnapshotCleaningService()
