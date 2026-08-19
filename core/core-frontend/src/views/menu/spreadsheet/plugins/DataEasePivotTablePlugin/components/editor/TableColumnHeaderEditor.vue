<template>
  <TableHeaderEditor
    :config="config"
    title="列头配置"
    style-key="columnHeader"
    @update-config="forwardUpdate"
  />
</template>

<script setup lang="ts">
import type { PivotTableConfig } from '../../types'
import TableHeaderEditor from './TableHeaderEditor.vue'

defineProps<{
  config: PivotTableConfig
}>()

const emit = defineEmits<{
  updateConfig: [key: string, value: any]
}>()

const forwardUpdate = (key: string, value: any) => {
  emit('updateConfig', key, value)
}
</script>
