<template>
  <el-scrollbar class="style-tab-scrollbar">
    <component
        v-for="(schema, index) in styleSchemas"
        :is="schema.component"
        :key="index"
        :config="currentConfig"
        @updateConfig="updateConfig"
    />
  </el-scrollbar>
</template>

<script setup lang="ts">
import { computed, inject, type Ref } from "vue"
import type { TablePluginConfig } from "../../types/plugin"
import { PluginAdapterManager, TablePluginAdapter } from "../../types/adapter"

const emit = defineEmits<{
    'updateConfig': [key: string, value: any]
}>()
const pluginConfig = inject<Ref<TablePluginConfig>>('pluginConfig')
if (!pluginConfig) {
    throw new Error('style-tab: missing required injections')
}
const currentConfig = computed(() => pluginConfig.value)
const styleSchemas = computed(() => {
    if (!pluginConfig.value?.type) {
        return []
    }
    const adapter = PluginAdapterManager.getAdapter(pluginConfig.value.type) as TablePluginAdapter
    if (!adapter) {
        return []
    }
    return adapter.getStyleSchema()
})
const updateConfig = (key: string, value: any) => {
    emit('updateConfig', key, value)
}
</script>

<style scoped>
.style-tab-scrollbar {
  height: 100%;
}
:deep(.ed-collapse-item__wrap) {
    border: none;
}
</style>
