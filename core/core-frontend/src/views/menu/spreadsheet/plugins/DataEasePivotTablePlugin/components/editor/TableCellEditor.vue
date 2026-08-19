<template>
  <el-collapse>
    <SwitchCollapseItem
      title="单元格"
      name="pivotTableCell"
      :model-value="cellStyle.enable"
      @change="value => updateCellStyle('enable', value)"
    >
      <el-form size="small">
        <div class="table-style-editor">
          <div class="standalone-field">
            <div class="field-label">背景颜色</div>
            <div class="background-color-control">
              <el-color-picker
                is-custom
                :model-value="cellStyle.backgroundColor"
                :disabled="!cellStyle.enable"
                @change="value => updateCellStyle('backgroundColor', value || '')"
              />
              <button
                v-if="cellStyle.enable && cellStyle.backgroundColor"
                type="button"
                class="background-color-clear"
                title="清除背景颜色"
                @click.stop="updateCellStyle('backgroundColor', '')"
              >
                <el-icon><CircleCloseFilled /></el-icon>
              </button>
            </div>
          </div>

          <div class="zebra-config">
            <el-checkbox
              :model-value="cellStyle.enableZebra"
              :disabled="!cellStyle.enable || isMergeCell"
              @change="value => updateCellStyle('enableZebra', value)"
              >斑马纹</el-checkbox
            >
            <div class="zebra-color-field">
              <el-color-picker
                is-custom
                :model-value="cellStyle.zebraColor"
                :disabled="!cellStyle.enable || !cellStyle.enableZebra || isMergeCell"
                @change="value => updateCellStyle('zebraColor', value || '')"
              />
            </div>
          </div>

          <div class="text-config">
            <div class="field-label">文本</div>
            <div class="text-toolbar-row">
              <el-color-picker
                is-custom
                :model-value="cellStyle.textColor"
                :disabled="!cellStyle.enable"
                @change="value => updateCellStyle('textColor', value)"
              />
              <el-select
                class="font-size-select"
                :model-value="cellStyle.fontSize"
                :disabled="!cellStyle.enable"
                @change="value => updateCellStyle('fontSize', value)"
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
                type="button"
                class="style-button font-bold"
                :class="{ active: cellStyle.bold }"
                :disabled="!cellStyle.enable"
                title="加粗"
                @click="updateCellStyle('bold', !cellStyle.bold)"
              >
                B
              </button>
              <button
                type="button"
                class="style-button font-italic"
                :class="{ active: cellStyle.italic }"
                :disabled="!cellStyle.enable"
                title="斜体"
                @click="updateCellStyle('italic', !cellStyle.italic)"
              >
                I
              </button>
              <button
                type="button"
                class="style-button font-underline"
                :class="{ active: cellStyle.underline }"
                :disabled="!cellStyle.enable"
                title="下划线"
                @click="updateCellStyle('underline', !cellStyle.underline)"
              >
                U
              </button>
              <button
                type="button"
                class="style-button font-strikethrough"
                :class="{ active: cellStyle.strikethrough }"
                :disabled="!cellStyle.enable"
                title="删除线"
                @click="updateCellStyle('strikethrough', !cellStyle.strikethrough)"
              >
                S
              </button>
              <TableBorderControl
                :model-value="cellStyle.border"
                :disabled="!cellStyle.enable"
                @update:model-value="value => updateCellStyle('border', value)"
              />
            </div>

            <div class="text-toolbar-row alignment-row">
              <div class="button-group">
                <button
                  v-for="item in horizontalAlignOptions"
                  :key="item.value"
                  type="button"
                  class="style-button align-button"
                  :class="{ active: cellStyle.textAlign === item.value }"
                  :disabled="!cellStyle.enable"
                  :title="item.label"
                  @click="updateCellStyle('textAlign', item.value)"
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
                  class="style-button align-button"
                  :class="{ active: cellStyle.verticalAlign === item.value }"
                  :disabled="!cellStyle.enable"
                  :title="item.label"
                  @click="updateCellStyle('verticalAlign', item.value)"
                >
                  <span class="vertical-icon" :class="`align-${item.value}`"><i /><i /></span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </el-form>
    </SwitchCollapseItem>
  </el-collapse>
</template>

<script setup lang="ts">
import { CircleCloseFilled } from '@element-plus/icons-vue'
import { computed, ref, watch } from 'vue'
import SwitchCollapseItem from '../../../../components/SwitchCollapseItem.vue'
import TableBorderControl from '../../../../components/table-border/TableBorderControl.vue'
import {
  createDefaultTableBorderConfig,
  normalizeTableBorderConfig
} from '../../../../components/table-border/border-config'
import type { PivotTableCellStyle, PivotTableConfig } from '../../types'

const props = defineProps<{
  config: PivotTableConfig
}>()

const emit = defineEmits<{
  updateConfig: [key: string, value: any]
}>()

const defaultCellStyle: PivotTableCellStyle = {
  enable: true,
  backgroundColor: '',
  enableZebra: false,
  zebraColor: '#f5f7fa',
  textColor: '#333333',
  fontSize: 12,
  bold: false,
  italic: false,
  underline: false,
  strikethrough: false,
  border: createDefaultTableBorderConfig(),
  textAlign: 'left',
  verticalAlign: 'middle'
}

const cellStyle = ref<PivotTableCellStyle>({ ...defaultCellStyle })

const isMergeCell = computed(
  () => props.config?.style?.base?.mergeCell ?? false
)

const initCellStyle = () => {
  cellStyle.value = {
    ...defaultCellStyle,
    ...props.config?.style?.cell,
    border: normalizeTableBorderConfig(props.config?.style?.cell?.border)
  }
}

initCellStyle()

watch(
  () => props.config?.style?.cell,
  () => initCellStyle(),
  { deep: true }
)

const fontSizeOptions = [
  10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34, 36, 38, 40, 50, 60, 70, 80, 100
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

const updateCellStyle = (key: keyof PivotTableCellStyle, value: any) => {
  if (value === undefined || value === null) return
  cellStyle.value = { ...cellStyle.value, [key]: value }
  emit('updateConfig', `style.cell.${key}`, value)
}

</script>

<style scoped lang="less">
@import './table-style-editor.less';
</style>
