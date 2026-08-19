<script setup lang="ts">
import { ArrowDown, Check } from '@element-plus/icons-vue'
import {
  AllBorderIcon,
  DownBorderDoubleIcon,
  HorizontalBorderDoubleIcon,
  InnerBorderDoubleIcon,
  LeftBorderDoubleIcon,
  NoBorderIcon,
  OuterBorderDoubleIcon,
  RightBorderDoubleIcon,
  UpBorderDoubleIcon,
  VerticalBorderDoubleIcon
} from '@univerjs/icons-vue'
import { computed, ref, watch, type Component } from 'vue'
import TableBorderLinePreview from './TableBorderLinePreview.vue'
import {
  TABLE_BORDER_STYLE_OPTIONS,
  applyTableBorderPreset,
  clearTableBorders,
  normalizeTableBorderConfig,
  toggleTableBorderPosition,
  updateTableBorderColor,
  updateTableBorderStyle,
  type TableBorderConfig,
  type TableBorderIconName,
  type TableBorderPosition,
  type TableBorderPreset
} from './border-config'

const props = withDefaults(
  defineProps<{
    modelValue?: Partial<TableBorderConfig>
    disabled?: boolean
  }>(),
  {
    modelValue: undefined,
    disabled: false
  }
)

const emit = defineEmits<{
  'update:modelValue': [value: TableBorderConfig]
}>()

interface ShortcutItem {
  preset: TableBorderPreset
  icon: TableBorderIconName
  label: string
}

interface PositionItem {
  position: TableBorderPosition
  icon: TableBorderIconName
  label: string
}

const shortcuts: ShortcutItem[] = [
  { preset: 'all', icon: 'all', label: '全边框' },
  { preset: 'inside', icon: 'inside', label: '内部边框' },
  { preset: 'outside', icon: 'outside', label: '外侧边框' }
]

const positionButtons: PositionItem[] = [
  { position: 'left', icon: 'left', label: '左边框' },
  { position: 'vertical', icon: 'vertical', label: '内部竖边框' },
  { position: 'right', icon: 'right', label: '右边框' },
  { position: 'top', icon: 'top', label: '上边框' },
  { position: 'horizontal', icon: 'horizontal', label: '内部横边框' },
  { position: 'bottom', icon: 'bottom', label: '下边框' }
]

const TABLE_BORDER_ICONS: Record<TableBorderIconName, Component> = {
  all: AllBorderIcon,
  inside: InnerBorderDoubleIcon,
  outside: OuterBorderDoubleIcon,
  left: LeftBorderDoubleIcon,
  vertical: VerticalBorderDoubleIcon,
  right: RightBorderDoubleIcon,
  top: UpBorderDoubleIcon,
  horizontal: HorizontalBorderDoubleIcon,
  bottom: DownBorderDoubleIcon
}

const BORDER_ICON_EXTEND = { colorChannel1: '#3370ff' }

const normalizedValue = computed(() => normalizeTableBorderConfig(props.modelValue))
const borderPopoverVisible = ref(false)
const borderStylePopoverVisible = ref(false)

watch(borderPopoverVisible, visible => {
  if (!visible) {
    borderStylePopoverVisible.value = false
  }
})

const emitValue = (value: TableBorderConfig) => {
  if (!props.disabled) {
    emit('update:modelValue', value)
  }
}

const handlePreset = (preset: TableBorderPreset) => {
  emitValue(applyTableBorderPreset(normalizedValue.value, preset))
}

const handlePosition = (position: TableBorderPosition) => {
  emitValue(toggleTableBorderPosition(normalizedValue.value, position))
}

const handleClear = () => {
  emitValue(clearTableBorders(normalizedValue.value))
}

const handleColorChange = (value: string | null) => {
  if (value) {
    emitValue(updateTableBorderColor(normalizedValue.value, value))
  }
}

const handleStyleChange = (value: TableBorderConfig['style']) => {
  emitValue(updateTableBorderStyle(normalizedValue.value, value))
  borderStylePopoverVisible.value = false
  borderPopoverVisible.value = false
}
</script>

<template>
  <el-popover
    v-model:visible="borderPopoverVisible"
    placement="bottom-start"
    :width="108"
    trigger="click"
    :disabled="disabled"
    popper-class="de-table-border-popper"
  >
    <template #reference>
      <button
        type="button"
        class="table-border-trigger"
        :disabled="disabled"
        title="边框"
        aria-label="边框"
        data-testid="border-trigger"
      >
        <AllBorderIcon />
      </button>
    </template>

    <div class="table-border-control" @click.stop>
      <div class="border-grid">
        <button
          v-for="item in shortcuts"
          :key="item.preset"
          type="button"
          class="border-action border-shortcut"
          :disabled="disabled"
          :title="item.label"
          :aria-label="item.label"
          :data-testid="`border-preset-${item.preset}`"
          @click="handlePreset(item.preset)"
        >
          <component :is="TABLE_BORDER_ICONS[item.icon]" :extend="BORDER_ICON_EXTEND" />
        </button>

        <button
          v-for="item in positionButtons"
          :key="item.position"
          type="button"
          class="border-action border-position"
          :class="{ active: normalizedValue[item.position] }"
          :disabled="disabled"
          :title="item.label"
          :aria-label="item.label"
          :aria-pressed="normalizedValue[item.position]"
          :data-testid="`border-position-${item.position}`"
          @click="handlePosition(item.position)"
        >
          <component :is="TABLE_BORDER_ICONS[item.icon]" :extend="BORDER_ICON_EXTEND" />
        </button>
      </div>

      <div class="border-separator" />

      <button
        type="button"
        class="border-none-action"
        :disabled="disabled"
        title="无边框"
        aria-label="无边框"
        data-testid="border-none"
        @click="handleClear"
      >
        <NoBorderIcon />
        <span>无边框</span>
      </button>

      <div class="border-separator" />

      <div class="border-detail-actions">
        <el-color-picker
          class="border-color-picker"
          :model-value="normalizedValue.color"
          :disabled="disabled"
          :is-custom="false"
          size="small"
          title="边框颜色"
          @change="handleColorChange"
        />

        <el-popover
          v-model:visible="borderStylePopoverVisible"
          placement="right-start"
          :width="150"
          trigger="click"
          :disabled="disabled"
          popper-class="de-table-border-style-popper"
        >
          <template #reference>
            <button
              type="button"
              class="border-style-trigger"
              :disabled="disabled"
              title="边框线型"
              aria-label="边框线型"
            >
              <TableBorderLinePreview :line-style="normalizedValue.style" :width="38" />
              <el-icon class="border-more-icon"><ArrowDown /></el-icon>
            </button>
          </template>

          <div class="border-style-options" @click.stop>
            <button
              v-for="lineStyle in TABLE_BORDER_STYLE_OPTIONS"
              :key="lineStyle"
              type="button"
              class="border-style-option"
              :class="{ active: normalizedValue.style === lineStyle }"
              :disabled="disabled"
              :aria-label="`边框线型 ${lineStyle}`"
              :data-testid="`border-style-${lineStyle}`"
              @click="handleStyleChange(lineStyle)"
            >
              <el-icon
                v-if="normalizedValue.style === lineStyle"
                class="border-check-icon"
              >
                <Check />
              </el-icon>
              <span v-else class="border-check-placeholder" />
              <TableBorderLinePreview :line-style="lineStyle" :width="104" />
            </button>
          </div>
        </el-popover>
      </div>
    </div>
  </el-popover>
</template>

<style scoped lang="less">
@button-size: 28px;
@active-color: #3370ff;
@active-bg: #e8f0ff;
@hover-bg: #f2f3f5;
@disabled-color: #c9cdd4;

.table-border-trigger,
.border-action,
.border-none-action,
.border-style-trigger,
.border-style-option {
  border: 0;
  background: transparent;
  color: #1f2329;
  cursor: pointer;

  &:disabled {
    color: @disabled-color;
    cursor: not-allowed;
  }
}

.border-none-action {
  padding: 0 7px !important;
  span {
    margin-left: 12px;
  }
}

.table-border-trigger,
.border-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: @button-size;
  height: @button-size;
  padding: 0;
  border-radius: 4px;
  font-size: 16px;

  &:hover:not(:disabled) {
    background: @hover-bg;
  }
}

.table-border-trigger.active,
.border-action.active {
  background: @active-bg;
}

.table-border-trigger.active {
  color: @active-color;
}

.border-action.active {
  color: #1f2329;
}

.table-border-control {
  box-sizing: border-box;
  width: 94px;
}

.border-grid {
  display: grid;
  grid-template-columns: repeat(3, @button-size);
  gap: 4px;
  justify-content: center;
}

.border-action {
  color: #1f2329;
}

.border-separator {
  height: 1px;
  margin: 4px 0;
  background: #e5e6eb;
}

.border-none-action {
  display: flex;
  align-items: center;
  gap: 4px;
  width: 100%;
  height: @button-size;
  padding: 0 4px;
  border-radius: 4px;
  box-sizing: border-box;
  font-size: 13px;

  svg {
    flex: 0 0 auto;
    font-size: 16px;
  }

  &:hover:not(:disabled) {
    background: @hover-bg;
  }
}

.border-detail-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}


.border-more-icon {
  flex: 0 0 auto;
  width: 10px;
  height: 10px;
}

.border-style-trigger {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2px;
  height: @button-size;
  min-width: 62px;
  padding: 0 4px;
  border-radius: 4px;

  &:hover:not(:disabled) {
    background: @hover-bg;
  }
}

.border-style-options {
  display: grid;
  gap: 2px;
  padding: 2px;
}

.border-style-option {
  position: relative;
  display: flex;
  align-items: center;
  gap: 6px;
  width: 100%;
  min-height: 28px;
  padding: 4px 6px;
  border-radius: 4px;

  &:hover:not(:disabled),
  &.active {
    background: @hover-bg;
  }
}

.border-check-icon,
.border-check-placeholder {
  flex: 0 0 auto;
  width: 14px;
  height: 14px;
}

.border-check-icon {
  color: @active-color;
}

</style>
<style lang="less">
.de-table-border-popper {
  min-width: 108px !important;
  padding: 6px !important;
}
.border-color-picker {
  .is-icon-arrow-down_custom,
  .is-icon-arrow-down {
    display: none;
  }
}
</style>
