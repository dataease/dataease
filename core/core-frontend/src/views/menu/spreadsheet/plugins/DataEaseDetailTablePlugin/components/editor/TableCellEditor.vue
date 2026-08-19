<template>
  <el-collapse>
    <SwitchCollapseItem
      title="单元格"
      name="tableCell"
      :model-value="cellStyle.enable"
      @change="value => updateCellStyle('enable', value)"
    >
      <el-form size="small">
        <div class="table-cell-editor">
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
            >斑马纹</el-checkbox>

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
              >B</button>
              <button
                type="button"
                class="style-button font-italic"
                :class="{ active: cellStyle.italic }"
                :disabled="!cellStyle.enable"
                title="斜体"
                @click="updateCellStyle('italic', !cellStyle.italic)"
              >I</button>
              <button
                type="button"
                class="style-button font-underline"
                :class="{ active: cellStyle.underline }"
                :disabled="!cellStyle.enable"
                title="下划线"
                @click="updateCellStyle('underline', !cellStyle.underline)"
              >U</button>
              <button
                type="button"
                class="style-button font-strikethrough"
                :class="{ active: cellStyle.strikethrough }"
                :disabled="!cellStyle.enable"
                title="删除线"
                @click="updateCellStyle('strikethrough', !cellStyle.strikethrough)"
              >S</button>
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
                  <span class="horizontal-icon" :class="`align-${item.value}`">
                    <i /><i /><i />
                  </span>
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
                  <span class="vertical-icon" :class="`align-${item.value}`">
                    <i /><i />
                  </span>
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
import type { DetailTableCellStyle, DetailTableConfig } from '../../types'

const props = defineProps<{
  config: DetailTableConfig
}>()

const emit = defineEmits<{
  'updateConfig': [key: string, value: any]
}>()

const defaultCellStyle: DetailTableCellStyle = {
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

const cellStyle = ref<DetailTableCellStyle>({ ...defaultCellStyle })

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

const updateCellStyle = (key: keyof DetailTableCellStyle, value: any) => {
  if (value === undefined || value === null) {
    return
  }
  cellStyle.value = {
    ...cellStyle.value,
    [key]: value
  }
  emit('updateConfig', `style.cell.${key}`, value)
}
</script>

<style scoped lang="less">
.table-cell-editor {
  padding: 4px 8px 12px;

  .field-label {
    color: #4b5563;
    font-size: 13px;
    line-height: 20px;
    margin-bottom: 8px;
  }
}

.standalone-field,
.zebra-config {
  margin-bottom: 16px;
}

.zebra-color-field {
  margin-top: 8px;
}

.background-color-control {
  display: flex;
  align-items: center;
  gap: 4px;

  &:hover .background-color-clear {
    opacity: 1;
    pointer-events: auto;
  }
}

.background-color-clear {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  padding: 0;
  border: 0;
  background: transparent;
  color: #8f959e;
  cursor: pointer;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.15s ease;

  &:hover,
  &:focus-visible {
    color: #1f2329;
    opacity: 1;
    pointer-events: auto;
  }
}

.text-config {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.text-toolbar-row {
  display: flex;
  align-items: center;
  gap: 4px;
  min-height: 28px;
}

.font-size-select {
  width: 72px;
}

.alignment-row {
  gap: 6px;

  :deep(.el-divider--vertical) {
    height: 20px;
    margin: 0 2px;
    border-color: #dee0e3;
  }
}

.button-group,
.merge-config {
  display: flex;
  align-items: center;
  gap: 4px;
}

.merge-config {
  margin-top: 12px;
}

.info-icon {
  color: #8f959e;
  cursor: help;
}

.style-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  padding: 0;
  border: 0;
  border-radius: 4px;
  background: transparent;
  color: #1f2329;
  font-size: 16px;
  cursor: pointer;

  &:hover:not(:disabled) {
    background: #f2f3f5;
  }

  &.active {
    color: #3370ff;
    background: #e8f0ff;
  }

  &:disabled {
    color: #c9cdd4;
    cursor: not-allowed;
  }
}

.font-bold {
  font-weight: 700;
}

.font-italic {
  font-family: serif;
  font-style: italic;
}

.font-underline {
  text-decoration: underline;
}

.font-strikethrough {
  text-decoration: line-through;
}

.horizontal-icon {
  display: flex;
  flex-direction: column;
  gap: 2px;
  width: 15px;

  i {
    display: block;
    width: 12px;
    height: 1.5px;
    border-radius: 1px;
    background: currentColor;
  }

  i:nth-child(2) {
    width: 15px;
  }

  &.align-center {
    align-items: center;
  }

  &.align-right {
    align-items: flex-end;
  }
}

.vertical-icon {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 2px;
  width: 15px;
  height: 15px;

  &::before {
    position: absolute;
    left: 1px;
    right: 1px;
    height: 1.5px;
    border-radius: 1px;
    background: currentColor;
    content: '';
  }

  i {
    display: block;
    width: 9px;
    height: 1.5px;
    margin-left: 3px;
    border-radius: 1px;
    background: currentColor;
  }

  &.align-top {
    justify-content: flex-end;

    &::before {
      top: 1px;
    }
  }

  &.align-middle {
    gap: 4px;

    &::before {
      top: 7px;
    }
  }

  &.align-bottom {
    justify-content: flex-start;

    &::before {
      bottom: 1px;
    }
  }
}
</style>
