<template>
  <el-collapse>
    <SwitchCollapseItem
      :title="title"
      :name="styleKey"
      :model-value="headerStyle.enable"
      @change="value => updateHeaderStyle('enable', value)"
    >
      <el-form size="small">
        <div class="table-style-editor">
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
                  :class="{ active: headerStyle.verticalAlign === item.value }"
                  :disabled="!headerStyle.enable"
                  :title="item.label"
                  @click="updateHeaderStyle('verticalAlign', item.value)"
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
import { ref, watch } from 'vue'
import SwitchCollapseItem from '../../../../components/SwitchCollapseItem.vue'
import TableBorderControl from '../../../../components/table-border/TableBorderControl.vue'
import {
  createDefaultTableBorderConfig,
  normalizeTableBorderConfig
} from '../../../../components/table-border/border-config'
import type { PivotTableHeaderStyle, PivotTableConfig } from '../../types'

type HeaderStyleKey = 'rowHeader' | 'columnHeader' | 'cornerHeader'

const props = defineProps<{
  config: PivotTableConfig
  title: string
  styleKey: HeaderStyleKey
}>()

const emit = defineEmits<{
  updateConfig: [key: string, value: any]
}>()

const defaultHeaderStyle: PivotTableHeaderStyle = {
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
  verticalAlign: 'middle'
}

const headerStyle = ref<PivotTableHeaderStyle>({ ...defaultHeaderStyle })

const initHeaderStyle = () => {
  const configuredStyle = props.config?.style?.[props.styleKey]
  headerStyle.value = {
    ...defaultHeaderStyle,
    ...configuredStyle,
    border: normalizeTableBorderConfig(configuredStyle?.border)
  }
}

initHeaderStyle()

watch(
  () => props.config?.style?.[props.styleKey],
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

const updateHeaderStyle = (key: keyof PivotTableHeaderStyle, value: any) => {
  if (value === undefined || value === null) return
  headerStyle.value = { ...headerStyle.value, [key]: value }
  emit('updateConfig', `style.${props.styleKey}.${key}`, value)
}
</script>

<style scoped lang="less">
@import './table-style-editor.less';
</style>
