<template>
  <el-collapse>
    <SwitchCollapseItem
      title="表头"
      name="tableHeader"
      :model-value="headerStyle.enable"
      @change="value => updateHeaderStyle('enable', value)"
    >
      <el-form size="small">
        <div class="table-header-editor" :class="{ disabled: !headerStyle.enable }">
          <div class="standalone-field">
            <div class="field-label">背景颜色</div>
            <div class="background-color-control">
              <el-color-picker
                is-custom
                :model-value="headerStyle.backgroundColor"
                :disabled="!headerStyle.enable"
                @change="value => updateHeaderStyle('backgroundColor', value || '')"
              />
              <button
                v-if="headerStyle.enable && headerStyle.backgroundColor"
                type="button"
                class="background-color-clear"
                title="清除背景颜色"
                @click.stop="updateHeaderStyle('backgroundColor', '')"
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
                :model-value="headerStyle.textColor"
                :disabled="!headerStyle.enable"
                @change="value => updateHeaderStyle('textColor', value)"
              />
              <el-select
                class="font-size-select"
                :model-value="headerStyle.fontSize"
                :disabled="!headerStyle.enable"
                @change="value => updateHeaderStyle('fontSize', value)"
              >
                <el-option
                  v-for="(fontSize, index) in fontSizeOptions"
                  :key="`${fontSize}-${index}`"
                  :label="fontSize"
                  :value="fontSize"
                />
              </el-select>
            </div>

            <div class="text-toolbar-row">
              <button
                type="button"
                class="style-button font-bold"
                :class="{ active: headerStyle.bold }"
                :disabled="!headerStyle.enable"
                title="加粗"
                @click="updateHeaderStyle('bold', !headerStyle.bold)"
              >
                B
              </button>
              <button
                type="button"
                class="style-button font-italic"
                :class="{ active: headerStyle.italic }"
                :disabled="!headerStyle.enable"
                title="斜体"
                @click="updateHeaderStyle('italic', !headerStyle.italic)"
              >
                I
              </button>
              <button
                type="button"
                class="style-button font-underline"
                :class="{ active: headerStyle.underline }"
                :disabled="!headerStyle.enable"
                title="下划线"
                @click="updateHeaderStyle('underline', !headerStyle.underline)"
              >
                U
              </button>
              <button
                type="button"
                class="style-button font-strikethrough"
                :class="{ active: headerStyle.strikethrough }"
                :disabled="!headerStyle.enable"
                title="删除线"
                @click="updateHeaderStyle('strikethrough', !headerStyle.strikethrough)"
              >
                S
              </button>
              <TableBorderControl
                :model-value="headerStyle.border"
                :disabled="!headerStyle.enable"
                @update:model-value="value => updateHeaderStyle('border', value)"
              />
            </div>

            <div class="text-toolbar-row alignment-row">
              <div class="button-group">
                <button
                  v-for="item in horizontalAlignOptions"
                  :key="item.value"
                  type="button"
                  class="style-button align-button"
                  :class="{ active: headerStyle.textAlign === item.value }"
                  :disabled="!headerStyle.enable"
                  :title="item.label"
                  @click="updateHeaderStyle('textAlign', item.value)"
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
                  :class="{ active: headerStyle.verticalAlign === item.value }"
                  :disabled="!headerStyle.enable"
                  :title="item.label"
                  @click="updateHeaderStyle('verticalAlign', item.value)"
                >
                  <span class="vertical-icon" :class="`align-${item.value}`"> <i /><i /> </span>
                </button>
              </div>
            </div>
          </div>

          <div class="index-config">
            <el-checkbox
              :model-value="headerStyle.showIndex"
              :disabled="!headerStyle.enable"
              @change="value => updateHeaderStyle('showIndex', value)"
              >显示序号</el-checkbox
            >

            <div v-if="headerStyle.enable && headerStyle.showIndex" class="index-label-field">
              <div class="field-label">序号标签</div>
              <el-input
                v-model="headerStyle.indexLabel"
                placeholder="序号"
                :maxlength="50"
                @change="
                  value =>
                    updateHeaderStyle('indexLabel', value.trim() || defaultHeaderStyle.indexLabel)
                "
              />
            </div>
          </div>
        </div>
      </el-form>
    </SwitchCollapseItem>
  </el-collapse>
</template>

<script setup lang="ts">
import { CircleCloseFilled } from '@element-plus/icons-vue'
import { ref, watch } from 'vue'
import SwitchCollapseItem from '../../../../components/SwitchCollapseItem.vue'
import TableBorderControl from '../../../../components/table-border/TableBorderControl.vue'
import {
  createDefaultTableBorderConfig,
  normalizeTableBorderConfig
} from '../../../../components/table-border/border-config'
import type { DetailTableConfig, DetailTableHeaderStyle } from '../../types'

const props = defineProps<{
  config: DetailTableConfig
}>()

const emit = defineEmits<{
  'updateConfig': [key: string, value: any]
}>()

const defaultHeaderStyle: DetailTableHeaderStyle = {
  enable: false,
  backgroundColor: '',
  textColor: '#333333',
  fontSize: 12,
  bold: true,
  italic: false,
  underline: false,
  strikethrough: false,
  border: createDefaultTableBorderConfig(),
  textAlign: 'left',
  verticalAlign: 'middle',
  showIndex: false,
  indexLabel: '序号'
}

const headerStyle = ref<DetailTableHeaderStyle>({ ...defaultHeaderStyle })

const initHeaderStyle = () => {
  headerStyle.value = {
    ...defaultHeaderStyle,
    ...props.config?.style?.header,
    border: normalizeTableBorderConfig(props.config?.style?.header?.border)
  }
}

initHeaderStyle()

watch(
  () => props.config?.style?.header,
  () => initHeaderStyle(),
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

const updateHeaderStyle = (key: keyof DetailTableHeaderStyle, value: any) => {
  if (value === undefined || value === null) {
    return
  }
  headerStyle.value = {
    ...headerStyle.value,
    [key]: value
  }
  emit('updateConfig', `style.header.${key}`, value)
}

</script>

<style scoped lang="less">
.table-header-editor {
  padding: 4px 8px 12px;

  .field-label {
    color: #4b5563;
    font-size: 13px;
    line-height: 20px;
    margin-bottom: 8px;
  }
}

.standalone-field {
  margin-bottom: 16px;
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

.index-config {
  margin-top: 12px;
}

.index-label-field {
  margin-top: 8px;
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

.button-group {
  display: flex;
  align-items: center;
  gap: 4px;
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
