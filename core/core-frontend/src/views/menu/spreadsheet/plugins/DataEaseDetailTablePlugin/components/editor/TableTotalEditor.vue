<template>
  <el-collapse>
    <SwitchCollapseItem
      title="总计"
      name="tableTotal"
      :model-value="totalStyle.enable"
      @change="value => updateTotalStyle('enable', value)"
    >
      <el-form size="small">
        <div class="table-total-editor">
          <div class="field-block">
            <div class="field-label">总计标签</div>
            <el-input
              v-model="totalStyle.label"
              :disabled="!totalStyle.enable"
              placeholder="总计"
              @change="value => updateTotalStyle('label', value.trim() || defaultTotalStyle.label)"
            />
          </div>

          <div class="field-block">
            <div class="field-label">指标聚合</div>
            <div class="aggregation-row">
              <el-select
                class="metric-select"
                :model-value="selectedMetric"
                :disabled="!totalStyle.enable || metricFields.length === 0"
                placeholder="指标字段"
                @change="handleMetricChange"
              >
                <el-option
                  v-for="field in metricFields"
                  :key="getFieldKey(field)"
                  :label="field.chartShowName || field.name"
                  :value="getFieldKey(field)"
                />
              </el-select>
              <el-select
                class="aggregation-select"
                :model-value="selectedAggregation"
                :disabled="!totalStyle.enable || !selectedMetric"
                placeholder="聚合方式"
                @change="handleAggregationChange"
              >
                <el-option
                  v-for="option in aggregationOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
              <el-button
                v-if="selectedAggregation === 'CUSTOM'"
                class="setting-button"
                :disabled="!totalStyle.enable"
                text
                title="自定义聚合设置"
                @click="showCustomAggregationEditor"
              >
                <el-icon><Setting /></el-icon>
              </el-button>
            </div>
          </div>

          <div class="custom-style-config">
            <el-checkbox
              :model-value="totalStyle.customStyle"
              :disabled="!totalStyle.enable"
              @change="value => updateTotalStyle('customStyle', value)"
              >自定义样式</el-checkbox
            >
          </div>

          <div v-if="totalStyle.customStyle" class="custom-style-panel">
            <div class="field-block">
              <div class="field-label">背景颜色</div>
              <div class="background-color-control">
                <el-color-picker
                  is-custom
                  :model-value="totalStyle.backgroundColor"
                  :disabled="!totalStyle.enable"
                  @change="value => updateTotalStyle('backgroundColor', value || '')"
                />
                <button
                  v-if="totalStyle.enable && totalStyle.backgroundColor"
                  type="button"
                  class="background-color-clear"
                  title="清除背景颜色"
                  @click.stop="updateTotalStyle('backgroundColor', '')"
                >
                  <el-icon><CircleCloseFilled /></el-icon>
                </button>
              </div>
            </div>

            <div class="text-config">
              <div class="field-label">文本</div>
              <div class="text-toolbar-row">
                <el-color-picker
                  is-custom
                  :model-value="totalStyle.textColor"
                  :disabled="!totalStyle.enable"
                  @change="value => updateTotalStyle('textColor', value)"
                />
                <el-select
                  class="font-size-select"
                  :model-value="totalStyle.fontSize"
                  :disabled="!totalStyle.enable"
                  @change="value => updateTotalStyle('fontSize', value)"
                >
                  <el-option
                    v-for="fontSize in fontSizeOptions"
                    :key="fontSize"
                    :label="fontSize"
                    :value="fontSize"
                  />
                </el-select>
              </div>

              <div class="text-toolbar-row">
                <button
                  v-for="item in textStyleOptions"
                  :key="item.key"
                  type="button"
                  class="style-button"
                  :class="[{ active: totalStyle[item.key] }, item.className]"
                  :disabled="!totalStyle.enable"
                  :title="item.label"
                  @click="updateTotalStyle(item.key, !totalStyle[item.key])"
                >
                  {{ item.text }}
                </button>
                <TableBorderControl
                  :model-value="totalStyle.border"
                  :disabled="!totalStyle.enable || !totalStyle.customStyle"
                  @update:model-value="value => updateTotalStyle('border', value)"
                />
              </div>

              <div class="text-toolbar-row alignment-row">
                <div class="button-group">
                  <button
                    v-for="item in horizontalAlignOptions"
                    :key="item.value"
                    type="button"
                    class="style-button"
                    :class="{ active: totalStyle.textAlign === item.value }"
                    :disabled="!totalStyle.enable"
                    :title="item.label"
                    @click="updateTotalStyle('textAlign', item.value)"
                  >
                    <span class="horizontal-icon" :class="`align-${item.value}`"
                      ><i /><i /><i
                    /></span>
                  </button>
                </div>
                <el-divider direction="vertical" />
                <div class="button-group">
                  <button
                    v-for="item in verticalAlignOptions"
                    :key="item.value"
                    type="button"
                    class="style-button"
                    :class="{ active: totalStyle.verticalAlign === item.value }"
                    :disabled="!totalStyle.enable"
                    :title="item.label"
                    @click="updateTotalStyle('verticalAlign', item.value)"
                  >
                    <span class="vertical-icon" :class="`align-${item.value}`"><i /><i /></span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-form>
    </SwitchCollapseItem>
  </el-collapse>

  <el-dialog
    v-model="customAggregationVisible"
    title="自定义聚合公式"
    width="1000px"
    append-to-body
    destroy-on-close
    :close-on-click-modal="false"
  >
    <CustomAggrEdit ref="customAggregationEditor" />
    <template #footer>
      <el-button @click="customAggregationVisible = false">取消</el-button>
      <el-button type="primary" @click="confirmCustomAggregation">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { nextTick, ref, watch } from 'vue'
import { CircleCloseFilled, Setting } from '@element-plus/icons-vue'
import CustomAggrEdit from '@/views/chart/components/editor/editor-style/components/table/CustomAggrEdit.vue'
import SwitchCollapseItem from '../../../../components/SwitchCollapseItem.vue'
import TableBorderControl from '../../../../components/table-border/TableBorderControl.vue'
import {
  createDefaultTableBorderConfig,
  normalizeTableBorderConfig
} from '../../../../components/table-border/border-config'
import type { FieldItemData } from '../../../../types/plugin'
import type {
  DetailTableConfig,
  DetailTableTotalStyle,
  DetailTotalAggregation,
  DetailTotalFieldConfig
} from '../../types'

const props = defineProps<{
  config: DetailTableConfig
}>()

const emit = defineEmits<{
  'updateConfig': [key: string, value: any]
}>()

const defaultTotalStyle: DetailTableTotalStyle = {
  enable: false,
  label: '总计',
  fieldConfig: [],
  customStyle: false,
  backgroundColor: '',
  textColor: '#333333',
  fontSize: 12,
  bold: true,
  italic: false,
  underline: false,
  strikethrough: false,
  border: createDefaultTableBorderConfig(),
  textAlign: 'left',
  verticalAlign: 'middle'
}

const totalStyle = ref<DetailTableTotalStyle>({ ...defaultTotalStyle })
const metricFields = ref<FieldItemData[]>([])
const selectedMetric = ref('')
const selectedAggregation = ref<DetailTotalAggregation>('SUM')
const customAggregationVisible = ref(false)
const customAggregationEditor = ref()
const editingMetric = ref('')

const aggregationOptions: Array<{ label: string; value: DetailTotalAggregation }> = [
  { label: '最大', value: 'MAX' },
  { label: '最小', value: 'MIN' },
  { label: '平均', value: 'AVG' },
  { label: '求和', value: 'SUM' },
  { label: '自定义', value: 'CUSTOM' }
]

const fontSizeOptions = [10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34, 36, 38, 40, 50, 60, 70, 80, 100]
const textStyleOptions = [
  { key: 'bold' as const, label: '加粗', text: 'B', className: 'font-bold' },
  { key: 'italic' as const, label: '斜体', text: 'I', className: 'font-italic' },
  { key: 'underline' as const, label: '下划线', text: 'U', className: 'font-underline' },
  { key: 'strikethrough' as const, label: '删除线', text: 'S', className: 'font-strikethrough' }
]
const horizontalAlignOptions = [
  { value: 'left' as const, label: '左对齐' },
  { value: 'center' as const, label: '居中对齐' },
  { value: 'right' as const, label: '右对齐' }
]
const verticalAlignOptions = [
  { value: 'top' as const, label: '顶部对齐' },
  { value: 'middle' as const, label: '垂直居中' },
  { value: 'bottom' as const, label: '底部对齐' }
]

const getFieldKey = (field: FieldItemData) => String(field.dataeaseName || field.id)

const syncSelectedAggregation = () => {
  const config = totalStyle.value.fieldConfig.find(item => item.dataeaseName === selectedMetric.value)
  selectedAggregation.value = config?.aggregation || 'SUM'
}

const initTotalStyle = () => {
  totalStyle.value = {
    ...defaultTotalStyle,
    ...props.config?.style?.total,
    border: normalizeTableBorderConfig(props.config?.style?.total?.border),
    fieldConfig: [...(props.config?.style?.total?.fieldConfig || [])]
  }
  metricFields.value = (props.config?.data?.zones?.fields || []).filter(field => field.groupType === 'q')
  if (!metricFields.value.some(field => getFieldKey(field) === selectedMetric.value)) {
    selectedMetric.value = metricFields.value[0] ? getFieldKey(metricFields.value[0]) : ''
  }
  syncSelectedAggregation()
}

initTotalStyle()

watch(
  () => [props.config?.style?.total, props.config?.data?.zones?.fields],
  () => initTotalStyle(),
  { deep: true }
)

const updateTotalStyle = (key: keyof DetailTableTotalStyle, value: any) => {
  if (value === undefined || value === null) return
  totalStyle.value = { ...totalStyle.value, [key]: value }
  emit('updateConfig', `style.total.${key}`, value)
}

const handleMetricChange = (value: string) => {
  selectedMetric.value = value
  syncSelectedAggregation()
}

const handleAggregationChange = (aggregation: DetailTotalAggregation) => {
  selectedAggregation.value = aggregation
  const field = metricFields.value.find(item => getFieldKey(item) === selectedMetric.value)
  if (!field) return

  const currentConfig = totalStyle.value.fieldConfig.find(
    item => item.dataeaseName === selectedMetric.value
  )
  const nextConfig = totalStyle.value.fieldConfig.filter(
    item => item.dataeaseName !== selectedMetric.value
  )
  const fieldConfig: DetailTotalFieldConfig = {
    ...currentConfig,
    fieldId: field.id,
    dataeaseName: selectedMetric.value,
    aggregation
  }
  nextConfig.push(fieldConfig)
  updateTotalStyle('fieldConfig', nextConfig)
}

const showCustomAggregationEditor = () => {
  const field = metricFields.value.find(item => getFieldKey(item) === selectedMetric.value)
  if (!field) return

  const fieldConfig = totalStyle.value.fieldConfig.find(
    item => item.dataeaseName === selectedMetric.value
  )
  editingMetric.value = selectedMetric.value
  customAggregationVisible.value = true
  nextTick(() => {
    customAggregationEditor.value?.initEdit(
      {
        ...field,
        dataeaseName: selectedMetric.value,
        originName: fieldConfig?.customExpression || ''
      },
      metricFields.value.filter(item => String(item.id) !== '-1')
    )
  })
}

const confirmCustomAggregation = () => {
  const editor = customAggregationEditor.value
  if (!editor || !editingMetric.value) return

  editor.setFieldForm()
  const field = metricFields.value.find(item => getFieldKey(item) === editingMetric.value)
  if (!field) return

  const currentConfig = totalStyle.value.fieldConfig.find(
    item => item.dataeaseName === editingMetric.value
  )
  const nextConfig = totalStyle.value.fieldConfig.filter(
    item => item.dataeaseName !== editingMetric.value
  )
  nextConfig.push({
    ...currentConfig,
    fieldId: field.id,
    dataeaseName: editingMetric.value,
    aggregation: 'CUSTOM',
    customExpression: editor.fieldForm.originName || ''
  })
  updateTotalStyle('fieldConfig', nextConfig)
  customAggregationVisible.value = false
}
</script>

<style scoped lang="less">
.table-total-editor { padding: 4px 8px 12px; }
.field-label { color: #4b5563; font-size: 13px; line-height: 20px; margin-bottom: 8px; }
.field-block { margin-bottom: 16px; }
.background-color-control { display: flex; align-items: center; gap: 4px; }
.background-color-control:hover .background-color-clear { opacity: 1; pointer-events: auto; }
.background-color-clear { display: inline-flex; align-items: center; justify-content: center; width: 20px; height: 20px; padding: 0; border: 0; background: transparent; color: #8f959e; cursor: pointer; opacity: 0; pointer-events: none; transition: opacity 0.15s ease; }
.background-color-clear:hover, .background-color-clear:focus-visible { color: #1f2329; opacity: 1; pointer-events: auto; }
.aggregation-row, .text-toolbar-row, .button-group { display: flex; align-items: center; gap: 4px; }
.metric-select, .aggregation-select { flex: 1; min-width: 0; }
.setting-button { width: 28px; height: 28px; padding: 0; }
.custom-style-config { margin-bottom: 12px; }
.custom-style-panel { padding-top: 4px; }
.text-config { display: flex; flex-direction: column; gap: 8px; }
.text-toolbar-row { min-height: 28px; }
.font-size-select { width: 72px; }
.alignment-row { gap: 6px; }
.alignment-row :deep(.el-divider--vertical) { height: 20px; margin: 0 2px; border-color: #dee0e3; }
.style-button { display: inline-flex; align-items: center; justify-content: center; width: 28px; height: 28px; padding: 0; border: 0; border-radius: 4px; background: transparent; color: #1f2329; font-size: 16px; cursor: pointer; }
.style-button:hover:not(:disabled) { background: #f2f3f5; }
.style-button.active { color: #3370ff; background: #e8f0ff; }
.style-button:disabled { color: #c9cdd4; cursor: not-allowed; }
.font-bold { font-weight: 700; }
.font-italic { font-family: serif; font-style: italic; }
.font-underline { text-decoration: underline; }
.font-strikethrough { text-decoration: line-through; }
.horizontal-icon { display: flex; flex-direction: column; gap: 2px; width: 15px; }
.horizontal-icon i { display: block; width: 12px; height: 1.5px; border-radius: 1px; background: currentColor; }
.horizontal-icon i:nth-child(2) { width: 15px; }
.horizontal-icon.align-center { align-items: center; }
.horizontal-icon.align-right { align-items: flex-end; }
.vertical-icon { position: relative; display: flex; flex-direction: column; justify-content: center; gap: 2px; width: 15px; height: 15px; }
.vertical-icon::before { position: absolute; left: 1px; right: 1px; height: 1.5px; border-radius: 1px; background: currentColor; content: ''; }
.vertical-icon i { display: block; width: 9px; height: 1.5px; margin-left: 3px; border-radius: 1px; background: currentColor; }
.vertical-icon.align-top { justify-content: flex-end; }
.vertical-icon.align-top::before { top: 1px; }
.vertical-icon.align-middle { gap: 4px; }
.vertical-icon.align-middle::before { top: 7px; }
.vertical-icon.align-bottom { justify-content: flex-start; }
.vertical-icon.align-bottom::before { bottom: 1px; }
</style>
