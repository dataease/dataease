import type { Dependency } from '@univerjs/core'
import { Inject, Injector, Plugin, touchDependencies, UniverInstanceType } from '@univerjs/core'
import {
  PluginRenderHoverLayerService,
  PluginRenderHoverService,
  PluginRenderLoadingService,
  PluginRenderStatusService,
  TableInsertionService,
  TableRangeConflictService,
  TableUserStyleService
} from './services/table'

export const DATAEASE_RUNTIME_PLUGIN = 'DATAEASE_RUNTIME_PLUGIN'

/**
 * DataEase 电子表格运行时基础插件。
 * 这里只注册跨业务插件共享的无业务归属服务，明细表和透视表复用同一套实例。
 */
export class DataEaseRuntimePlugin extends Plugin {
  static override type = UniverInstanceType.UNIVER_SHEET
  static override pluginName = DATAEASE_RUNTIME_PLUGIN

  constructor(@Inject(Injector) private readonly injector: Injector) {
    super()
  }

  override onStarting(): void {
    const dependencies: Dependency[] = [
      [PluginRenderStatusService],
      [PluginRenderHoverService],
      [PluginRenderHoverLayerService],
      [PluginRenderLoadingService],
      [TableInsertionService],
      [TableRangeConflictService],
      [TableUserStyleService]
    ]
    dependencies.forEach(dependency => this.injector.add(dependency))
  }

  override onRendered(): void {
    touchDependencies(this.injector, [
      [PluginRenderStatusService],
      [PluginRenderHoverService],
      [PluginRenderHoverLayerService],
      [PluginRenderLoadingService],
      [TableInsertionService],
      [TableRangeConflictService],
      [TableUserStyleService]
    ])
  }
}
