<script setup lang="ts">
import { computed, ref } from 'vue'
import { QuestionFilled } from '@element-plus/icons-vue'
import type { SpreadsheetFilterConfig } from '../../../../types/plugin'
import SwitchCollapseItem from '../../../../components/SwitchCollapseItem.vue'

const props = defineProps<{ config: SpreadsheetFilterConfig }>()

const emit = defineEmits<{
  'updateConfig': [key: string, value: any]
}>()

const baseStyle = computed(() => props.config.style.base)
const conditionStyle = computed(() => props.config.style.condition)
const conditionNameStyle = computed(() => props.config.style.conditionName)
const buttonStyle = computed(() => props.config.style.button)
const activePanels = ref(['base', 'condition', 'conditionName', 'button'])

const buttonOptions = [
  { label: '查询', value: 'sure' },
  { label: '清空', value: 'clear' },
  { label: '重置', value: 'reset' }
] as const

const update = (key: string, value: any) => {
  if (value !== undefined && value !== null) {
    emit('updateConfig', key, value)
  }
}

const updatePadding = (side: 'top' | 'right' | 'bottom' | 'left', value?: number) => {
  if (value === undefined) return
  if (baseStyle.value.paddingMode === 'uniform') {
    const sides = ['top', 'right', 'bottom', 'left'] as const
    sides.forEach(item => {
      update(`style.base.padding.${item}`, value)
    })
    return
  }
  update(`style.base.padding.${side}`, value)
}

const updatePaddingMode = (mode: unknown) => {
  const nextMode = mode === 'custom' ? 'custom' : 'uniform'
  update('style.base.paddingMode', nextMode)
  if (nextMode === 'uniform') {
    updatePadding('top', baseStyle.value.padding.top)
  }
}
</script>

<template>
  <el-collapse v-model="activePanels" class="filter-style-editor">
    <el-collapse-item title="基础样式" name="base">
      <el-form class="filter-style-editor__form" label-position="top" size="small">
        <div class="filter-style-editor__check-row">
          <el-checkbox
            :model-value="baseStyle.backgroundEnabled"
            @change="value => update('style.base.backgroundEnabled', value)"
          >
            自定义背景颜色
          </el-checkbox>
          <el-color-picker
            is-custom
            :model-value="baseStyle.backgroundColor"
            :disabled="!baseStyle.backgroundEnabled"
            @change="value => update('style.base.backgroundColor', value)"
          />
        </div>

        <el-form-item label="卡片内间距">
          <el-radio-group
            :model-value="baseStyle.paddingMode"
            @change="updatePaddingMode"
          >
            <el-radio label="uniform">统一值</el-radio>
            <el-radio label="custom">逐边</el-radio>
          </el-radio-group>
        </el-form-item>
        <div class="filter-style-editor__number-grid">
          <el-form-item label="上">
            <el-input-number
              :model-value="baseStyle.padding.top"
              :min="0"
              controls-position="right"
              @change="value => updatePadding('top', value)"
            />
          </el-form-item>
          <el-form-item label="右">
            <el-input-number
              :model-value="baseStyle.padding.right"
              :min="0"
              :disabled="baseStyle.paddingMode === 'uniform'"
              controls-position="right"
              @change="value => updatePadding('right', value)"
            />
          </el-form-item>
          <el-form-item label="下">
            <el-input-number
              :model-value="baseStyle.padding.bottom"
              :min="0"
              :disabled="baseStyle.paddingMode === 'uniform'"
              controls-position="right"
              @change="value => updatePadding('bottom', value)"
            />
          </el-form-item>
          <el-form-item label="左">
            <el-input-number
              :model-value="baseStyle.padding.left"
              :min="0"
              :disabled="baseStyle.paddingMode === 'uniform'"
              controls-position="right"
              @change="value => updatePadding('left', value)"
            />
          </el-form-item>
        </div>

        <el-form-item label="圆角">
          <el-input-number
            :model-value="baseStyle.radius"
            :min="0"
            class="filter-style-editor__full-number"
            controls-position="right"
            @change="value => update('style.base.radius', value)"
          />
        </el-form-item>
        <div class="filter-style-editor__divider" />

        <el-form-item label="查询条件排列">
          <el-radio-group
            :model-value="baseStyle.layout"
            @change="value => update('style.base.layout', value)"
          >
            <el-radio label="horizontal">水平</el-radio>
            <el-radio label="vertical">垂直</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="查询条件位置">
          <div class="filter-style-editor__align-group">
            <button
              v-for="align in ['left', 'center', 'right']"
              :key="align"
              type="button"
              :class="['filter-style-editor__align-button', `is-${align}`, { 'is-active': baseStyle.align === align }]"
              @click="update('style.base.align', align)"
            >
              <i /><i /><i />
            </button>
          </div>
        </el-form-item>

        <el-form-item label="条件间距">
          <el-input-number
            :model-value="baseStyle.gap"
            :min="0"
            class="filter-style-editor__full-number"
            controls-position="right"
            @change="value => update('style.base.gap', value)"
          />
        </el-form-item>
      </el-form>
    </el-collapse-item>

    <el-collapse-item title="查询条件选框" name="condition">
      <el-form class="filter-style-editor__form" label-position="top" size="small">
        <div class="filter-style-editor__check-row">
          <el-checkbox
            :model-value="conditionStyle.fillEnabled"
            @change="value => update('style.condition.fillEnabled', value)"
          >
            填充颜色
          </el-checkbox>
          <el-color-picker
            is-custom
            :model-value="conditionStyle.fillColor"
            :disabled="!conditionStyle.fillEnabled"
            @change="value => update('style.condition.fillColor', value)"
          />
        </div>
        <div class="filter-style-editor__border-row">
          <el-checkbox
            :model-value="conditionStyle.borderEnabled"
            @change="value => update('style.condition.borderEnabled', value)"
          >
            边框
          </el-checkbox>
          <div class="filter-style-editor__border-controls">
            <el-color-picker
              is-custom
              :model-value="conditionStyle.borderColor"
              :disabled="!conditionStyle.borderEnabled"
              @change="value => update('style.condition.borderColor', value)"
            />
            <el-input-number
              :model-value="conditionStyle.borderWidth"
              :min="0"
              :disabled="!conditionStyle.borderEnabled"
              controls-position="right"
              @change="value => update('style.condition.borderWidth', value)"
            />
          </div>
        </div>
        <el-form-item label="文本">
          <div class="filter-style-editor__text-row">
            <el-color-picker
              :model-value="conditionStyle.color"
              is-custom
              @change="value => update('style.condition.color', value)"
            />
            <el-select
              :model-value="conditionStyle.fontSize"
              @change="value => update('style.condition.fontSize', value)"
            >
              <el-option v-for="size in [12, 14, 16, 18, 20, 24]" :key="size" :label="String(size)" :value="size" />
            </el-select>
            <el-button
              text
              :class="['filter-style-editor__font-button', { 'is-active': conditionStyle.fontWeight === 'bold' }]"
              @click="update('style.condition.fontWeight', conditionStyle.fontWeight === 'bold' ? 'normal' : 'bold')"
            >B</el-button>
            <el-button
              text
              :class="['filter-style-editor__font-button is-italic', { 'is-active': conditionStyle.fontStyle === 'italic' }]"
              @click="update('style.condition.fontStyle', conditionStyle.fontStyle === 'italic' ? 'normal' : 'italic')"
            >I</el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-collapse-item>

    <SwitchCollapseItem
      title="查询条件名称"
      name="conditionName"
      :model-value="conditionNameStyle.show"
      @change="value => update('style.conditionName.show', value)"
    >
      <el-form class="filter-style-editor__form" label-position="top" size="small">
        <el-form-item label="位置">
          <el-radio-group
            :model-value="conditionNameStyle.position"
            @change="value => update('style.conditionName.position', value)"
          >
            <el-radio label="top">上侧</el-radio>
            <el-radio label="left">左侧</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="文本">
          <div class="filter-style-editor__text-row">
            <el-color-picker
              :model-value="conditionNameStyle.color"
              is-custom
              @change="value => update('style.conditionName.color', value)"
            />
            <el-select
              :model-value="conditionNameStyle.fontSize"
              @change="value => update('style.conditionName.fontSize', value)"
            >
              <el-option v-for="size in [12, 14, 16, 18, 20, 24]" :key="size" :label="String(size)" :value="size" />
            </el-select>
            <el-button
              text
              :class="['filter-style-editor__font-button', { 'is-active': conditionNameStyle.fontWeight === 'bold' }]"
              @click="update('style.conditionName.fontWeight', conditionNameStyle.fontWeight === 'bold' ? 'normal' : 'bold')"
            >B</el-button>
            <el-button
              text
              :class="['filter-style-editor__font-button is-italic', { 'is-active': conditionNameStyle.fontStyle === 'italic' }]"
              @click="update('style.conditionName.fontStyle', conditionNameStyle.fontStyle === 'italic' ? 'normal' : 'italic')"
            >I</el-button>
          </div>
        </el-form-item>
        <el-form-item label="名称与选框间距">
          <el-input-number
            :model-value="conditionNameStyle.gap"
            :min="0"
            class="filter-style-editor__full-number"
            controls-position="right"
            @change="value => update('style.conditionName.gap', value)"
          />
        </el-form-item>
      </el-form>
    </SwitchCollapseItem>

    <el-collapse-item title="按钮" name="button">
      <el-form class="filter-style-editor__form" label-position="top" size="small">
        <el-form-item label="展示按钮">
          <el-checkbox-group
            :model-value="buttonStyle.btnList"
            @change="value => update('style.button.btnList', value)"
          >
            <el-checkbox v-for="option in buttonOptions" :key="option.value" :label="option.value">
              {{ option.label }}
              <el-tooltip
                v-if="option.value === 'sure'"
                content="如果展示查询按钮，需要点击该按钮后才能触发图表查询；如果不展示查询按钮，选择完查询条件后立即触发图表查询"
                placement="top"
              >
                <el-icon class="filter-style-editor__query-tip" @click.stop>
                  <QuestionFilled />
                </el-icon>
              </el-tooltip>
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="按钮颜色">
          <el-color-picker
            :model-value="buttonStyle.primaryColor"
            is-custom
            @change="value => update('style.button.primaryColor', value)"
          />
        </el-form-item>
        <el-form-item label="按钮文本">
          <div class="filter-style-editor__text-row">
            <el-color-picker
              :model-value="buttonStyle.textColor"
              is-custom
              @change="value => update('style.button.textColor', value)"
            />
            <el-select
              :model-value="buttonStyle.fontSize"
              @change="value => update('style.button.fontSize', value)"
            >
              <el-option v-for="size in [12, 14, 16, 18, 20, 24]" :key="size" :label="String(size)" :value="size" />
            </el-select>
            <el-button
              text
              :class="['filter-style-editor__font-button', { 'is-active': buttonStyle.fontWeight === 'bold' }]"
              @click="update('style.button.fontWeight', buttonStyle.fontWeight === 'bold' ? 'normal' : 'bold')"
            >B</el-button>
            <el-button
              text
              :class="['filter-style-editor__font-button is-italic', { 'is-active': buttonStyle.fontStyle === 'italic' }]"
              @click="update('style.button.fontStyle', buttonStyle.fontStyle === 'italic' ? 'normal' : 'italic')"
            >I</el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-collapse-item>
  </el-collapse>
</template>

<style scoped lang="less">
.filter-style-editor {
  width: 100%;

  :deep(.ed-collapse-item__header) {
    height: 36px;
    padding: 0 8px;
    background: #f5f6f7;
    color: #1f2329;
    font-size: 12px;
    font-weight: 500;
  }

  :deep(.ed-collapse-item__content) {
    padding: 16px 8px;
  }

  &__form {
    :deep(.ed-form-item) { margin-bottom: 16px; }
    :deep(.ed-form-item__label) {
      margin-bottom: 8px;
      color: #1f2329;
      font-size: 12px;
      line-height: 20px;
    }
    :deep(.ed-radio), :deep(.ed-checkbox) { margin-right: 16px; }
    :deep(.ed-checkbox__label) {
      font-size: 12px;
      line-height: 20px;
    }
  }

  &__check-row,
  &__border-row {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
    margin-bottom: 18px;

    > :deep(.ed-color-picker) {
      margin-left: 22px;
    }
  }

  &__number-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 0 8px;
    :deep(.ed-input-number) { width: 100%; }
  }

  &__full-number { width: 100%; }

  &__border-controls {
    width: calc(100% - 22px);
    margin-left: 22px;
    display: grid;
    grid-template-columns: 50px minmax(0, 1fr);
    gap: 8px;
    :deep(.ed-input-number) { width: 100%; }
  }

  &__text-row {
    width: 100%;
    display: grid;
    grid-template-columns: 50px 56px 24px 24px;
    gap: 8px;
    align-items: center;

    :deep(.ed-select) {
      width: 56px;
    }
  }

  &__font-button {
    width: 24px;
    height: 24px;
    margin-left: 0 !important;
    padding: 0;
    font-size: 16px;
    font-weight: 600;
    &.is-italic { font-style: italic; }
    &.is-active { background: var(--ed-fill-color); }
  }

  &__divider {
    height: 1px;
    margin: 0 0 16px;
    background: var(--ed-border-color-lighter);
  }

  &__query-tip {
    margin-left: 4px;
    color: var(--ed-text-color-secondary);
    font-size: 14px;
    vertical-align: -2px;
    cursor: help;
  }

  &__align-group { display: flex; gap: 8px; }
  &__align-button {
    width: 28px;
    height: 28px;
    padding: 6px;
    border: 0;
    border-radius: 4px;
    background: transparent;
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: 3px;
    color: var(--ed-text-color-regular);
    cursor: pointer;
    i { width: 14px; height: 1px; background: currentColor; }
    &.is-center i { align-self: center; &:nth-child(2) { width: 10px; } }
    &.is-left i:nth-child(2), &.is-right i:nth-child(2) { width: 10px; }
    &.is-right i { align-self: flex-end; }
    &.is-active {
      color: var(--ed-color-primary);
      background-color: var(--ed-color-primary-1a, #3370ff1a);
    }
  }
}
</style>
