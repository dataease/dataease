<script lang="ts" setup>
import { computed, ref, provide, inject, type Ref } from 'vue'
import ConfigPanel from '../config-panel/index.vue'
import DatasetPanel from '../dataset-panel/index.vue'
import type { FieldItemData, PluginConfig, TablePluginConfig } from '../../types/plugin'
import { ElMessage } from 'element-plus-secondary'
import { pluginRuntimeRegistry } from '../../services/plugin-runtime.service'
import { dispatchSpreadsheetContentChanged } from '../../utils/events'

const univerApi = inject('univerApi') as any

// Use injected pluginConfig
const pluginConfig = inject<Ref<TablePluginConfig>>('pluginConfig')
if (!pluginConfig) {
  throw new Error('Editor requires pluginConfig to be provided')
}

const datasetFields = ref<{
  dimensions: FieldItemData[]
  quotas: FieldItemData[]
}>({
  dimensions: [],
  quotas: []
})

const filedList = computed(() => {
  return [...datasetFields.value.dimensions, ...datasetFields.value.quotas].filter(
    field => field.id !== 'count'
  )
})

const updatePluginConfig = (key: string, value: any) => {
  if (key.includes('.')) {
    const keys = key.split('.')
    let target: any = pluginConfig.value
    for (let i = 0; i < keys.length - 1; i++) {
      if (!target[keys[i]]) {
        target[keys[i]] = {}
      }
      target = target[keys[i]]
    }
    target[keys[keys.length - 1]] = value
  } else {
    pluginConfig.value[key] = value
  }
}

provide('datasetFields', datasetFields)
provide('filedList', () => filedList.value)
provide('univerApi', univerApi)

const queryData = async (config: PluginConfig) => {
  try {
    const api = univerApi?.value
    if (!api) {
      ElMessage.error('Univer instance not ready')
      return
    }

    const runtime = pluginRuntimeRegistry.get(config.type)
    if (!runtime?.refreshData) {
      ElMessage.warning('Current plugin does not support refresh')
      return
    }

    await runtime.refreshData({ univerApi: api, config })

  } catch (error) {
    ElMessage.error('Refresh data failed')
  }
}

const updateConfig = async (key: string, value: any) => {
  try {
    const api = univerApi?.value
    const config = pluginConfig.value as TablePluginConfig
    if (!api || !config?.type) {
      updatePluginConfig(key, value)
      return
    }

    const runtime = pluginRuntimeRegistry.get(config.type)
    const validateMessage = await runtime?.validateConfigUpdate?.({ univerApi: api, config, key, value })
    if (validateMessage) {
      ElMessage.warning(validateMessage)
      return
    }

    updatePluginConfig(key, value)
    dispatchSpreadsheetContentChanged()
    if (key.startsWith('style.')) {
      await runtime?.applyStyle?.({ univerApi: api, config })
    }
  } catch (error) {
    ElMessage.error('Apply style failed')
  }
}

</script>

<template>
  <div class="table-editor">
    <div class="table-editor-container">
      <ConfigPanel
        class="config-panel"
        @queryData="queryData"
        @updateConfig="updateConfig"
      />

      <DatasetPanel class="dataset-panel" @updateConfig="updateConfig" />
    </div>
  </div>
</template>

<style lang="less" scoped>
.table-editor {
  height: 100%;
  width: fit-content;
  background: #fff;
  display: flex;
  overflow: hidden;

  &-container {
    display: flex;
    height: 100%;
    width: fit-content;
    overflow: hidden;
  }

  .config-panel {
    border-right: 1px solid var(--de-border-color, #e5e5e5);
    flex-shrink: 0;
    overflow: hidden;
  }

  .dataset-panel {
    flex-shrink: 0;
    overflow: hidden;
  }
}
</style>
