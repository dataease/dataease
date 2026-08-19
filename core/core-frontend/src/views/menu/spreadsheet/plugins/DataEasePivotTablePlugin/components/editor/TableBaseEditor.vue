<template>
  <el-collapse>
    <el-collapse-item name="pivotTableBase" title="基础配置">
      <el-form size="small">
        <div class="table-style-editor">
          <div class="base-switch-row">
            <el-checkbox
              :model-value="baseStyle.customBlockName"
              @change="handleCustomBlockNameChange"
            >自定义区块名称</el-checkbox>
          </div>

          <div class="block-name-field">
            <el-input
              v-model="baseStyle.blockName"
              :maxlength="50"
              :disabled="!baseStyle.customBlockName"
              :placeholder="defaultBlockName"
              @change="value => updateBaseStyle('blockName', value.trim() || defaultBlockName)"
            />
          </div>

          <div class="merge-config">
            <el-checkbox
              :model-value="baseStyle.mergeCell"
              @change="value => updateBaseStyle('mergeCell', value)"
              >合并表头</el-checkbox
            >
          </div>
        </div>
      </el-form>
    </el-collapse-item>
  </el-collapse>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import type { PivotTableBaseStyle, PivotTableConfig } from '../../types'

const props = defineProps<{
  config: PivotTableConfig
}>()

const emit = defineEmits<{
  updateConfig: [key: string, value: any]
}>()

const baseStyle = ref<PivotTableBaseStyle>({
  customBlockName: false,
  blockName: '',
  mergeCell: false
})
const defaultBlockName = ref('')

const initBaseStyle = () => {
  defaultBlockName.value = `${props.config?.placement?.sheetName || ''}!${props.config?.placement?.startCell || ''}`
  const customBlockName = props.config?.style?.base?.customBlockName ?? false
  const mergeCell = props.config?.style?.base?.mergeCell ?? false
  baseStyle.value = {
    ...props.config?.style?.base,
    customBlockName,
    blockName: customBlockName
      ? props.config?.style?.base?.blockName || defaultBlockName.value
      : defaultBlockName.value,
    mergeCell
  }
}

initBaseStyle()

watch(
  () => [props.config?.style?.base, props.config?.placement],
  () => initBaseStyle(),
  { deep: true }
)

const updateBaseStyle = (key: keyof PivotTableBaseStyle, value: any) => {
  if (value === undefined || value === null) return
  baseStyle.value = { ...baseStyle.value, [key]: value }
  emit('updateConfig', `style.base.${key}`, value)
}

const handleCustomBlockNameChange = (value: boolean) => {
  if (value && !props.config?.style?.base?.blockName) {
    updateBaseStyle('blockName', defaultBlockName.value)
  }
  updateBaseStyle('customBlockName', value)
}
</script>

<style scoped lang="less">
@import './table-style-editor.less';

.base-switch-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
