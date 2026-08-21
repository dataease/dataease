<script setup lang="ts">
import { Delete, Edit, Plus, Setting } from '@element-plus/icons-vue'
import type { Options } from '@popperjs/core'
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { ElConfigProvider, ElMessage } from 'element-plus-secondary'
import { useLocaleStoreWithOut } from '@/store/modules/locale'
import type {
  SpreadsheetFilterCondition,
  SpreadsheetFilterConfig
} from '../../../types/plugin'
import type { SpreadsheetMode } from '../../../types/mode'
import SpreadsheetFilterConfigDialog from './SpreadsheetFilterConfigDialog.vue'
import SpreadsheetFilterRenderer from './renderers/SpreadsheetFilterRenderer.vue'
import {
  dispatchSpreadsheetFilterDeleteCondition,
  dispatchSpreadsheetFilterDisable,
  dispatchSpreadsheetFilterOpenStyle,
  dispatchSpreadsheetFilterQuery,
  dispatchSpreadsheetFilterRequestConfigContext,
  dispatchSpreadsheetFilterSaveConfig,
  getSpreadsheetFilterConfig,
  getSpreadsheetFilterVisible,
  offSpreadsheetFilterConfigChange,
  offSpreadsheetFilterVisibleChange,
  onSpreadsheetFilterConfigChange,
  type SpreadsheetFilterConfigDialogPayload,
  onSpreadsheetFilterVisibleChange
} from '../utils/events'
import {
  getSpreadsheetFilterEmptyValue,
  getSpreadsheetFilterSelectedValues,
  getSpreadsheetFilterValidationIssue,
  resolveSpreadsheetFilterValues
} from '../utils/filter-values'

const props = withDefaults(defineProps<{ mode?: SpreadsheetMode }>(), {
  mode: 'edit'
})

const localeStore = useLocaleStoreWithOut()
const elLocale = computed(() => localeStore.getCurrentLocale.elLocale)

const visible = ref(false)
const config = ref<SpreadsheetFilterConfig>()
const configDialogVisible = ref(false)
const configDialogPayload = ref<SpreadsheetFilterConfigDialogPayload>()
const configDialogKey = ref(0)
const conditionValues = ref<Record<string, unknown>>({})
const lastAutoQuerySignature = ref('')
const isPreview = computed(() => props.mode === 'preview')
const popperAppendTo = ref('body')

const syncPopperAppendTo = () => {
  // 浏览器全屏只显示全屏元素子树，浮层需要随全屏状态切换挂载目标。
  popperAppendTo.value = document.fullscreenElement ? ':fullscreen' : 'body'
}

const visibleConditions = computed(
  () => config.value?.conditions.filter(condition => condition.visible) || []
)
const styleConfig = computed(() => config.value?.style)
const queryBarStyle = computed(() => {
  const base = styleConfig.value?.base
  if (!base) {
    return {}
  }

  const padding = base.paddingMode === 'uniform'
    ? `${base.padding.top}px`
    : `${base.padding.top}px ${base.padding.right}px ${base.padding.bottom}px ${base.padding.left}px`

  return {
    padding,
    background: base.backgroundEnabled ? base.backgroundColor : '',
    borderRadius: `${base.radius}px`
  }
})
const conditionsStyle = computed(() => {
  const base = styleConfig.value?.base
  const alignMap = {
    left: 'flex-start',
    center: 'center',
    right: 'flex-end'
  } as const
  const align = alignMap[base?.align || 'left']
  const horizontal = base?.layout !== 'vertical'
  return {
    flexDirection: horizontal ? 'row' : 'column',
    justifyContent: horizontal ? align : 'flex-start',
    alignItems: horizontal ? 'flex-start' : align,
    gap: `${base?.gap ?? 16}px`
  }
})
const conditionStyle = computed(() => ({
  width: '235px',
  flexDirection: styleConfig.value?.conditionName?.position === 'left' ? 'row' : 'column',
  alignItems: styleConfig.value?.conditionName?.position === 'left' ? 'center' : 'stretch',
  gap: `${styleConfig.value?.conditionName?.gap ?? 8}px`
}))
const isDoubleTextSearch = (condition: SpreadsheetFilterCondition) =>
  condition.displayType === 'textSearch' && condition.textSearchConditionType !== 'single'
const getConditionWidth = (condition: SpreadsheetFilterCondition) => {
  if (isDoubleTextSearch(condition)) return '502px'
  // 时间区间保留完整输入空间，但不再占用两个普通条件的宽度。
  if (condition.displayType === 'timeRange') return '350px'
  return '235px'
}
const getConditionStyle = (condition: SpreadsheetFilterCondition) => ({
  ...conditionStyle.value,
  width: getConditionWidth(condition),
  flexShrink: condition.displayType === 'timeRange' ? 0 : undefined
})
const conditionNameStyle = computed(() => ({
  color: styleConfig.value?.conditionName?.color || '#1f2329',
  fontSize: `${styleConfig.value?.conditionName?.fontSize ?? 12}px`,
  fontWeight: styleConfig.value?.conditionName?.fontWeight || 'normal',
  fontStyle: styleConfig.value?.conditionName?.fontStyle || 'normal'
}))
const conditionNameVisible = computed(() => styleConfig.value?.conditionName?.show !== false)
const conditionControlStyle = computed(() => ({
  flex: styleConfig.value?.conditionName?.position === 'left' ? '1 1 0' : undefined,
  minWidth: 0
}))
const controlStyle = computed(() => ({
  width: styleConfig.value?.conditionName?.position === 'left'
    ? 'auto'
    : '235px',
  '--ed-input-height': '32px',
  '--ed-fill-color-blank': styleConfig.value?.condition?.fillEnabled
    ? styleConfig.value.condition.fillColor
    : '#ffffff',
  '--ed-border-color': styleConfig.value?.condition?.borderEnabled
    ? styleConfig.value.condition.borderColor
    : '#dcdfe6',
  '--ed-border-color-hover': styleConfig.value?.condition?.borderEnabled
    ? styleConfig.value.condition.borderColor
    : '#dcdfe6',
  '--dataease-filter-border-width': `${styleConfig.value?.condition?.borderEnabled ? styleConfig.value.condition.borderWidth : 1}px`,
  '--dataease-filter-border-color': styleConfig.value?.condition?.borderEnabled
    ? styleConfig.value.condition.borderColor
    : '#dcdfe6',
  '--dataease-filter-control-color': styleConfig.value?.condition?.color || '#1f2329',
  '--dataease-filter-control-font-size': `${styleConfig.value?.condition?.fontSize ?? 12}px`,
  '--dataease-filter-control-font-weight': styleConfig.value?.condition?.fontWeight || 'normal',
  '--dataease-filter-control-font-style': styleConfig.value?.condition?.fontStyle || 'normal',
  '--ed-input-placeholder-color': styleConfig.value?.condition?.color || '#1f2329',
  '--ed-text-color-placeholder': styleConfig.value?.condition?.color || '#1f2329',
  '--dataease-filter-primary-color': styleConfig.value?.button?.primaryColor || '#3370ff'
}))
const controlPopperVariables = computed(() => ({
  '--ed-bg-color-overlay': styleConfig.value?.condition?.fillEnabled
    ? styleConfig.value.condition.fillColor
    : '#ffffff',
  '--ed-fill-color-blank': styleConfig.value?.condition?.fillEnabled
    ? styleConfig.value.condition.fillColor
    : '#ffffff',
  '--ed-text-color-regular': styleConfig.value?.condition?.color || '#1f2329',
  '--ed-font-size-base': `${styleConfig.value?.condition?.fontSize ?? 12}px`,
  '--dataease-filter-control-font-weight': styleConfig.value?.condition?.fontWeight || 'normal',
  '--dataease-filter-control-font-style': styleConfig.value?.condition?.fontStyle || 'normal'
}))
const controlPopperOptions = computed<Partial<Options>>(() => {
  const variables = controlPopperVariables.value
  return {
    modifiers: [
      {
        name: 'dataeaseFilterControlStyle',
        enabled: true,
        phase: 'write' as const,
        requires: ['applyStyles'],
        fn: ({ state }) => {
          const popper = state.elements.popper
          if (!(popper instanceof HTMLElement)) return
          // 弹层通过 Teleport 脱离查询组件 DOM，需要将当前选框样式写入弹层根节点。
          Object.entries(variables).forEach(([name, value]) => {
            popper.style.setProperty(name, value)
          })
        }
      }
    ]
  }
})
const getControlStyle = (condition: SpreadsheetFilterCondition) => ({
  ...controlStyle.value,
  width: styleConfig.value?.conditionName?.position === 'left'
    ? 'auto'
    : getConditionWidth(condition)
})
const getButtonTypography = () => ({
  fontSize: `${styleConfig.value?.button?.fontSize ?? 12}px`,
  fontWeight: styleConfig.value?.button?.fontWeight || 'normal',
  fontStyle: styleConfig.value?.button?.fontStyle || 'normal'
})
const getButtonStyle = (color: string) => ({
  ...getButtonTypography(),
  backgroundColor: color,
  borderColor: color,
  color: styleConfig.value?.button?.textColor || '#ffffff'
})
const primaryButtonStyle = computed(() =>
  getButtonStyle(styleConfig.value?.button?.primaryColor || '#3370ff')
)
const secondaryButtonStyle = computed(() => {
  const color = styleConfig.value?.button?.primaryColor || '#3370ff'
  return {
    ...getButtonTypography(),
    color,
    borderColor: color,
    backgroundColor: '#ffffff',
    '--dataease-filter-secondary-button-color': color
  }
})
const buttonList = computed(() => styleConfig.value?.button?.btnList || ['sure'])
const autoQueryEnabled = computed(() => !buttonList.value.includes('sure'))

watch(autoQueryEnabled, (enabled, prevEnabled) => {
  if (enabled && !prevEnabled) {
    void nextTick(autoQuery)
  }
})

const handleVisibleChange = (nextVisible: boolean) => {
  visible.value = nextVisible
}

const handleConfigChange = (nextConfig: SpreadsheetFilterConfig) => {
  config.value = nextConfig
  syncConditionValues(nextConfig)
}

const closeConfigDialog = () => {
  configDialogVisible.value = false
}

const clearConfigDialog = () => {
  configDialogPayload.value = undefined
}

const saveConfigDialog = (nextConfig: SpreadsheetFilterConfig) => {
  if (isPreview.value) return
  dispatchSpreadsheetFilterSaveConfig({ config: nextConfig })
  closeConfigDialog()
}

const openConfig = (payload: Pick<SpreadsheetFilterConfigDialogPayload, 'selectedConditionId' | 'initialAction'> = {}) => {
  if (isPreview.value) return
  dispatchSpreadsheetFilterRequestConfigContext({
    ...payload,
    onReady: nextPayload => {
      configDialogPayload.value = nextPayload
      configDialogKey.value += 1
      configDialogVisible.value = true
    }
  })
}

const addCondition = () => {
  openConfig({ initialAction: 'add' })
}

const editCondition = (conditionId: string) => {
  openConfig({ selectedConditionId: conditionId })
}

const deleteCondition = (conditionId: string) => {
  if (isPreview.value) return
  dispatchSpreadsheetFilterDeleteCondition(conditionId)
}

const openStyle = () => {
  if (isPreview.value) return
  dispatchSpreadsheetFilterOpenStyle()
}

const openStyleFromBlankArea = () => {
  if (isPreview.value) return
  dispatchSpreadsheetFilterOpenStyle({ skipWhenSidebarVisible: true })
}

const INTERACTIVE_TARGET_SELECTOR = [
  'button',
  'input',
  'textarea',
  'select',
  'a',
  '[role="button"]',
  '[role="combobox"]',
  '[contenteditable="true"]',
  '.spreadsheet-query-bar__control-wrap',
  '.spreadsheet-query-bar__toolbar'
].join(',')

const handleContainerClick = (event: MouseEvent) => {
  if (isPreview.value) return
  const target = event.target
  if (!(target instanceof Element) || target.closest(INTERACTIVE_TARGET_SELECTOR)) return
  openStyleFromBlankArea()
}

const disable = () => {
  if (isPreview.value) return
  dispatchSpreadsheetFilterDisable()
}

const syncConditionValues = (nextConfig?: SpreadsheetFilterConfig) => {
  conditionValues.value = getSpreadsheetFilterSelectedValues(nextConfig)
  lastAutoQuerySignature.value = ''
}

const getQuerySignature = () => JSON.stringify(
  config.value?.conditions.map(condition => [condition.id, conditionValues.value[condition.id]]) || []
)

const query = (silent = false, deduplicate = false) => {
  const issue = getSpreadsheetFilterValidationIssue(config.value, conditionValues.value)
  if (issue) {
    if (!silent) {
      if (issue.type === 'invalid-number-range') {
        ElMessage.error(`查询条件“${issue.condition.name}”的最小值不能大于最大值`)
      } else if (issue.type === 'incomplete-range' || issue.type === 'incomplete-text') {
        ElMessage.warning(`查询条件“${issue.condition.name}”未填写完整`)
      } else {
        ElMessage.warning(`查询条件“${issue.condition.name}”不能为空`)
      }
    }
    return false
  }
  const signature = getQuerySignature()
  if (deduplicate && signature === lastAutoQuerySignature.value) {
    return false
  }
  dispatchSpreadsheetFilterQuery({
    config: config.value,
    values: { ...conditionValues.value }
  })
  lastAutoQuerySignature.value = signature
  return true
}

const autoQuery = () => {
  if (!autoQueryEnabled.value) return
  query(true, true)
}

const updateConditionValue = (condition: SpreadsheetFilterCondition, value: unknown) => {
  conditionValues.value[condition.id] = value
  if (condition.displayType !== 'textSearch') {
    void nextTick(autoQuery)
  }
}

const commitConditionValue = () => {
  void nextTick(autoQuery)
}

const clear = () => {
  const nextValues: Record<string, unknown> = {}
  visibleConditions.value.forEach(condition => {
    nextValues[condition.id] = getSpreadsheetFilterEmptyValue(condition)
  })
  conditionValues.value = {
    ...conditionValues.value,
    ...nextValues
  }
  if (autoQueryEnabled.value) {
    query()
  }
}

const reset = async () => {
  if (!config.value) return
  conditionValues.value = await resolveSpreadsheetFilterValues(config.value, {}, false)
  if (autoQueryEnabled.value) {
    query()
  }
}

onMounted(() => {
  syncPopperAppendTo()
  document.addEventListener('fullscreenchange', syncPopperAppendTo)
  visible.value = getSpreadsheetFilterVisible()
  config.value = getSpreadsheetFilterConfig()
  syncConditionValues(config.value)
  onSpreadsheetFilterVisibleChange(handleVisibleChange)
  onSpreadsheetFilterConfigChange(handleConfigChange)
})

onBeforeUnmount(() => {
  document.removeEventListener('fullscreenchange', syncPopperAppendTo)
  offSpreadsheetFilterVisibleChange(handleVisibleChange)
  offSpreadsheetFilterConfigChange(handleConfigChange)
})
</script>

<template>
  <ElConfigProvider :locale="elLocale" namespace="ed">
    <div
      v-if="visible"
      :class="[
        'spreadsheet-query-bar',
        visibleConditions.length ? 'has-conditions' : 'is-empty',
        !isPreview && 'is-editing'
      ]"
      :style="queryBarStyle"
      @click="handleContainerClick"
    >
      <div v-if="!isPreview" class="spreadsheet-query-bar__toolbar">
        <button class="spreadsheet-query-bar__tool" title="添加查询条件" @click="addCondition">
          <el-icon><Plus /></el-icon>
        </button>
        <button class="spreadsheet-query-bar__tool" title="打开查询条件配置" @click="openConfig()">
          <el-icon><Edit /></el-icon>
        </button>
        <button class="spreadsheet-query-bar__tool" title="删除查询组件" @click="disable">
          <el-icon><Delete /></el-icon>
        </button>
        <span class="spreadsheet-query-bar__tool-divider" />
        <button class="spreadsheet-query-bar__tool" title="打开样式面板" @click="openStyle">
          <el-icon><Setting /></el-icon>
        </button>
      </div>
      <div
        v-if="visibleConditions.length"
        class="spreadsheet-query-bar__conditions"
        :style="conditionsStyle"
      >
        <div
          v-for="condition in visibleConditions"
          :key="condition.id"
          class="spreadsheet-query-bar__condition"
          :style="getConditionStyle(condition)"
        >
          <div
            v-if="conditionNameVisible"
            class="spreadsheet-query-bar__condition-label"
            :style="conditionNameStyle"
          >
            {{ condition.name }}
            <span v-if="condition.required" class="spreadsheet-query-bar__required">*</span>
          </div>
          <div
            class="spreadsheet-query-bar__control-wrap"
            :style="conditionControlStyle"
          >
            <div
              class="spreadsheet-query-bar__control-anchor"
              :style="getControlStyle(condition)"
            >
              <SpreadsheetFilterRenderer
                :model-value="conditionValues[condition.id]"
                :condition="condition"
                :popper-append-to="popperAppendTo"
                :popper-options="controlPopperOptions"
                class="spreadsheet-query-bar__control"
                @update:model-value="value => updateConditionValue(condition, value)"
                @commit="commitConditionValue"
              />
              <div
                v-if="!isPreview"
                class="spreadsheet-query-bar__condition-toolbar"
              >
                <button
                  class="spreadsheet-query-bar__condition-tool"
                  title="编辑查询条件"
                  @click.stop="editCondition(condition.id)"
                >
                  <el-icon><Edit /></el-icon>
                </button>
                <button
                  class="spreadsheet-query-bar__condition-tool"
                  title="删除查询条件"
                  @click.stop="deleteCondition(condition.id)"
                >
                  <el-icon><Delete /></el-icon>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-if="visibleConditions.length" class="spreadsheet-query-bar__query-actions">
        <el-button
          v-if="buttonList.includes('clear')"
          class="spreadsheet-query-bar__query is-secondary"
          :style="secondaryButtonStyle"
          @click="clear"
        >
          清空
        </el-button>
        <el-button
          v-if="buttonList.includes('reset')"
          class="spreadsheet-query-bar__query is-secondary"
          :style="secondaryButtonStyle"
          @click="reset"
        >
          重置
        </el-button>
        <el-button
          v-if="buttonList.includes('sure')"
          class="spreadsheet-query-bar__query is-primary"
          type="primary"
          :style="primaryButtonStyle"
          @click="query()"
        >
          查询
        </el-button>
      </div>
      <el-button
        v-if="!isPreview && !visibleConditions.length"
        class="spreadsheet-query-bar__empty-action"
        text
        type="primary"
        @click="openConfig()"
      >
        <el-icon><Plus /></el-icon>
        添加查询条件
      </el-button>
    </div>
    <el-dialog
      v-if="!isPreview"
      v-model="configDialogVisible"
      class="spreadsheet-filter-config-dialog-host"
      title="过滤组件配置"
      width="1280px"
      append-to-body
      destroy-on-close
      align-center
      :draggable="false"
      :modal="true"
      :close-on-click-modal="false"
      :close-on-press-escape="true"
      @closed="clearConfigDialog"
    >
      <SpreadsheetFilterConfigDialog
        v-if="configDialogPayload"
        :key="configDialogKey"
        :config="configDialogPayload.config"
        :available-plugins="configDialogPayload.availablePlugins"
        :selected-condition-id="configDialogPayload.selectedConditionId"
        :initial-action="configDialogPayload.initialAction"
        :on-save="saveConfigDialog"
        :on-cancel="closeConfigDialog"
      />
    </el-dialog>
  </ElConfigProvider>
</template>

<style scoped lang="less">
.spreadsheet-query-bar {
  width: 100%;
  min-height: 54px;
  padding: 16px;
  border: 1px solid rgba(31, 35, 41, 0.15);
  border-radius: 6px;
  background: #fff;
  display: flex;
  align-items: center;
  gap: 16px;
  box-sizing: border-box;
  position: relative;

  &.has-conditions {
    min-height: 94px;
  }

  &__conditions {
    display: flex;
    align-items: flex-start;
    gap: 16px;
    flex-wrap: wrap;
    flex: 1;
  }

  &__condition {
    width: 235px;
    display: flex;
    flex-direction: column;
    gap: 8px;
    position: relative;
  }

  &__condition-label {
    color: #1f2329;
    font-size: 12px;
    display: flex;
    flex-shrink: 0;
    justify-content: flex-start;
    align-items: center;
    line-height: 22px;
    min-width: 0;
    white-space: nowrap;
  }

  &__required {
    color: #f56c6c;
    margin-left: 2px;
  }

  &__control-anchor {
    min-width: 0;
    position: relative;

    :deep(.ed-input__wrapper),
    :deep(.ed-select__wrapper),
    :deep(.ed-textarea__inner) {
      box-shadow: 0 0 0 var(--dataease-filter-border-width) var(--dataease-filter-border-color) inset;
    }

    :deep(.ed-input__inner),
    :deep(.ed-select__selected-item),
    :deep(.ed-select__placeholder),
    :deep(.ed-textarea__inner),
    :deep(.ed-range-input),
    :deep(.spreadsheet-filter-number-range__separator),
    :deep(.spreadsheet-filter-text-search__logic),
    :deep(.spreadsheet-filter-tile__option) {
      color: var(--dataease-filter-control-color);
      font-size: var(--dataease-filter-control-font-size);
      font-weight: var(--dataease-filter-control-font-weight);
      font-style: var(--dataease-filter-control-font-style);
    }
  }

  &__control {
    width: 100%;
  }

  &__control-wrap {
    min-width: 0;
  }

  &__condition-toolbar {
    position: absolute;
    top: 0;
    right: 0;
    height: 30px;
    padding: 0 6px;
    border: 1px solid rgba(31, 35, 41, 0.15);
    border-radius: 6px;
    box-sizing: border-box;
    display: flex;
    align-items: center;
    gap: 4px;
    background: #fff;
    box-shadow: 0 4px 10px rgba(31, 35, 41, 0.12);
    opacity: 0;
    pointer-events: none;
    transform: translateY(calc(-100% + 8px));
    transition: opacity 0.16s ease;
    z-index: 4;
  }

  &__condition-tool {
    width: 20px;
    height: 20px;
    padding: 0;
    border: 0;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    background: transparent;
    color: #646a73;
    font-size: 16px;
    cursor: pointer;
  }

  &__condition:hover &__condition-toolbar,
  &__control-wrap:focus-within &__condition-toolbar {
    opacity: 1;
    pointer-events: auto;
  }

  &__query-actions {
    display: flex;
    align-items: center;
    gap: 12px;
    align-self: flex-end;
    margin-left: auto;
  }

  &__query {
    width: 80px;
    height: 32px;
    margin-left: 0;
    border-radius: 6px;

    &.is-secondary:hover,
    &.is-secondary:focus,
    &.is-secondary:active {
      color: var(--dataease-filter-secondary-button-color) !important;
      border-color: var(--dataease-filter-secondary-button-color) !important;
      background: #fff !important;
    }
  }

  &__empty-action {
    position: absolute;
    top: 50%;
    left: 50%;
    height: 26px;
    padding: 2px 4px;
    transform: translate(-50%, -50%);
    font-size: 14px;
    line-height: 22px;

    :deep(.ed-icon) {
      width: 16px;
      height: 16px;
      margin-right: 4px;
    }
  }

  &__toolbar {
    position: absolute;
    top: 0;
    left: 0;
    width: 113px;
    height: 24px;
    padding: 4px 8px;
    border-radius: 6px;
    box-sizing: border-box;
    display: flex;
    align-items: center;
    gap: 8px;
    background: var(--ed-color-primary);
    opacity: 0;
    pointer-events: none;
    transition: opacity 0.16s ease;
    z-index: 3;
  }

  &__tool {
    width: 16px;
    height: 16px;
    padding: 0;
    border: 0;
    background: transparent;
    color: #fff;
    font-size: 16px;
    line-height: 16px;
    cursor: pointer;
  }

  &__tool-divider {
    width: 1px;
    height: 16px;
    flex-shrink: 0;
    background: rgba(255, 255, 255, 0.15);
  }

  &.is-editing:hover {
    border-color: var(--ed-color-primary);

    .spreadsheet-query-bar__toolbar {
      opacity: 1;
      pointer-events: auto;
    }
  }
}

:global(.spreadsheet-filter-runtime-popper) {
  font-size: var(--ed-font-size-base);
  font-weight: var(--dataease-filter-control-font-weight);
  font-style: var(--dataease-filter-control-font-style);
}

:global(.spreadsheet-filter-runtime-popper .ed-select-dropdown__item),
:global(.spreadsheet-filter-runtime-popper .ed-tree-node__label),
:global(.spreadsheet-filter-runtime-popper .ed-picker-panel) {
  font-size: var(--ed-font-size-base);
  font-weight: var(--dataease-filter-control-font-weight);
  font-style: var(--dataease-filter-control-font-style);
}

:global(.spreadsheet-filter-runtime-popper .ed-tree) {
  background: var(--ed-bg-color-overlay);
}

:global(.spreadsheet-filter-config-dialog-host) {
  height: 640px;
  max-height: 640px;
  overflow: hidden;
}

:global(.spreadsheet-filter-config-dialog-host .el-dialog__body),
:global(.spreadsheet-filter-config-dialog-host .ed-dialog__body) {
  height: 560px;
  padding: 0;
  overflow: hidden;
}
</style>
