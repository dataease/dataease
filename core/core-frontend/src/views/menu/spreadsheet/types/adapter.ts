import { FieldItemData, FieldZoneSchema, TablePluginConfig } from "./plugin"
import TableEditor from '../components/panel/index.vue'
import type { Component } from "vue"

export type DeepPartial<T> = {
    [P in keyof T]?: T[P] extends object ? DeepPartial<T[P]> : T[P]
}

export class PluginAdapterManager {
    private static ADAPTER_MAP: Map<string, PluginAdapter> = new Map()

    static registerAdapter(pluginType: string, adapter: PluginAdapter) {
        this.ADAPTER_MAP.set(pluginType, adapter)
    }

    static getAdapter(pluginType: string): undefined | PluginAdapter {
        return this.ADAPTER_MAP.get(pluginType)
    }
}

export abstract class PluginAdapter {
    private _type: string
    public get type(): string {
        return this._type
    }
    abstract getEditor(): Component
    constructor(type: string) {
        this._type = type
        PluginAdapterManager.registerAdapter(type, this)
    }
}

export abstract class TablePluginAdapter<T extends TablePluginConfig = TablePluginConfig> extends PluginAdapter {
    abstract getPanelTitle(config: DeepPartial<T>): string
    getEditor() {
        return TableEditor
    }
    constructor(type: string) {
        super(type)
    }
    abstract getZonesSchema(): FieldZoneSchema[]
    validateZoneUpdate?(config: T, zoneId: string, fields: FieldItemData[]): string | undefined
    abstract getStyleSchema(): any
    abstract getSeniorSchema(): any
    abstract getDefaultConfig(): DeepPartial<T>
}
