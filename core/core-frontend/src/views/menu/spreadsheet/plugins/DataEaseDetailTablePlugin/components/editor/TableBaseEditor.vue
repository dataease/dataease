<template>
  <el-collapse>
    <el-collapse-item name="tableBase" title="基础配置">
      <el-form size="small">
        <div class="table-base-editor">
          <el-checkbox
            :model-value="baseStyle.customBlockName"
            @change="handleCustomBlockNameChange"
            >自定义区块名称</el-checkbox
          >

          <div class="block-name-field">
            <el-input
              v-model="baseStyle.blockName"
              :maxlength="50"
              :disabled="!baseStyle.customBlockName"
              :placeholder="defaultBlockName"
              @change="value => updateBaseStyle('blockName', value.trim() || defaultBlockName)"
            />
          </div>

          <el-checkbox
            :model-value="baseStyle.hideHeader"
            @change="value => updateBaseStyle('hideHeader', value)"
            >不显示表头</el-checkbox
          >

          <div class="merge-config">
            <el-checkbox
              :model-value="baseStyle.mergeCell"
              @change="value => updateBaseStyle('mergeCell', value)"
              >合并单元格</el-checkbox
            >
            <el-tooltip
              content="合并单元格后，斑马纹会失效，当前页的序号会从 1 开始"
              placement="top"
            >
              <el-icon class="info-icon"><InfoFilled /></el-icon>
            </el-tooltip>
          </div>
        </div>
      </el-form>
    </el-collapse-item>
  </el-collapse>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { InfoFilled } from '@element-plus/icons-vue'
import type { DetailTableBaseStyle, DetailTableConfig } from '../../types'

const props = defineProps<{
  config: DetailTableConfig
}>()

const emit = defineEmits<{
  'updateConfig': [key: string, value: any]
}>()

const baseStyle = ref<DetailTableBaseStyle>({
  customBlockName: false,
  blockName: '',
  hideHeader: false,
  mergeCell: false
})
const defaultBlockName = ref('')

const initBaseStyle = () => {
  defaultBlockName.value = `${props.config?.placement?.sheetName || ''}!${props.config?.placement?.startCell || ''}`
  const customBlockName = props.config?.style?.base?.customBlockName ?? false
  const mergeCell = props.config?.style?.base?.mergeCell ?? false
  baseStyle.value = {
    customBlockName,
    blockName: customBlockName
      ? props.config?.style?.base?.blockName || defaultBlockName.value
      : defaultBlockName.value,
    hideHeader: props.config?.style?.base?.hideHeader ?? false,
    mergeCell
  }
}

initBaseStyle()

watch(
  () => [props.config?.style?.base, props.config?.placement],
  () => initBaseStyle(),
  { deep: true }
)

const updateBaseStyle = (key: keyof DetailTableBaseStyle, value: any) => {
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
.table-base-editor {
  padding: 4px 8px 12px;
}

.block-name-field {
  margin: 12px 0 16px;
}

.field-label {
  color: #4b5563;
  font-size: 13px;
  line-height: 20px;
  margin-bottom: 8px;
}

.merge-config {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 12px;
}
</style>
