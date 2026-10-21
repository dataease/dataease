<script lang="ts" setup>
import { computed } from 'vue'
import { fieldType } from '@/utils/attr'
import { useI18n } from '@/hooks/web/useI18n'
import Icon from '@/components/icon-custom/src/Icon.vue'
import icon_copy_outlined from '@/assets/svg/icon_copy_outlined.svg'
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'
import type { FieldItemData } from '../../types/plugin'
import { getSpreadsheetFieldIcon } from '../../utils/field-icon'

interface Props {
  field: FieldItemData
  index?: number
  showRemove?: boolean
  showConfig?: boolean
  dragged?: boolean
  selected?: boolean
  showCopy?: boolean
  showDelete?: boolean
  actionDisabled?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  index: -1,
  showRemove: true,
  showConfig: true,
  dragged: false,
  selected: false,
  showCopy: false,
  showDelete: false,
  actionDisabled: false
})

const { t } = useI18n()

const emit = defineEmits<{
  copy: [field: FieldItemData]
  delete: [field: FieldItemData]
  remove: [index: number]
  config: [field: FieldItemData, index: number]
  click: [field: FieldItemData, index: number, event: MouseEvent]
  dragstart: [field: FieldItemData, index: number, event: DragEvent]
  dragend: []
}>()

// 获取字段类型图标
const fieldIconType = computed(() => {
  return fieldType[props.field.deType ?? 0] || 'text'
})

const fieldTypeIcon = computed(() => {
  return getSpreadsheetFieldIcon(props.field)
})

// 获取字段类型颜色
const fieldTypeColor = computed(() => {
  const { groupType } = props.field
  return groupType === 'd' ? 'var(--ed-color-primary, #3370ff)' : '#34c724'
})

// 获取显示名称
const chartShowName = computed(() => {
  return props.field.chartShowName || props.field.name
})

// 获取字段类型描述
const fieldTypeDesc = computed(() => {
  const { groupType } = props.field
  if (groupType === 'd') {
    const descMap: Record<string, string> = {
      text: '字符串',
      time: '时间',
      location: '地理位置',
      url: 'URL'
    }
    return descMap[fieldIconType.value] || '维度'
  }
  return '指标'
})

const handleDragStart = (e: DragEvent) => {
  emit('dragstart', props.field, props.index, e)
}

const handleClick = (e: MouseEvent) => {
  emit('click', props.field, props.index, e)
}
</script>

<template>
  <div
    class="field-item"
    :class="{
      dragged: props.dragged,
      selected: props.selected
    }"
    :data-id="field.id"
    :tabindex="showCopy || showDelete ? 0 : undefined"
    draggable="true"
    @dragstart="handleDragStart"
    @dragend="emit('dragend')"
    @click="handleClick"
  >
    <el-icon class="field-icon" :title="fieldTypeDesc" :style="{ color: fieldTypeColor }">
      <Icon :class-name="`field-icon-${fieldIconType}`">
        <component
          class="svg-icon"
          :class="`field-icon-${fieldIconType}`"
          :is="fieldTypeIcon"
        />
      </Icon>
    </el-icon>
    <span class="field-name" :title="chartShowName">
      {{ chartShowName }}
    </span>
    <button
      v-if="showCopy"
      type="button"
      class="field-action"
      :disabled="actionDisabled"
      :title="t('commons.copy')"
      :aria-label="t('commons.copy')"
      @mousedown.stop
      @dragstart.stop.prevent
      @click.stop="emit('copy', field)"
    >
      <Icon><icon_copy_outlined /></Icon>
    </button>
    <button
      v-if="showDelete"
      type="button"
      class="field-action"
      :disabled="actionDisabled"
      :title="t('commons.delete')"
      :aria-label="t('commons.delete')"
      @mousedown.stop
      @dragstart.stop.prevent
      @click.stop="emit('delete', field)"
    >
      <Icon><icon_deleteTrash_outlined /></Icon>
    </button>
  </div>
</template>

<style lang="less" scoped>
.field-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  cursor: move;
  transition: background 0.2s;
  border-radius: 4px;

  &:hover {
    background: #f5f7fa;
  }

  // 隐藏时保留占位，避免字段名跳动；键盘聚焦时也能操作。
  &:hover .field-action,
  &:focus-within .field-action {
    visibility: visible;
  }

  &.dragged {
    opacity: 0.5;
  }

  &.selected {
    background: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1));
    border: 1px solid var(--ed-color-primary, #3370ff);
  }

  .field-icon {
    font-size: 14px;
    font-weight: 500;
    flex-shrink: 0;
    width: 16px;
    text-align: center;
  }

  .field-name {
    font-size: 13px;
    color: #4e5969;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    flex: 1;
  }

  .field-action {
    visibility: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    width: 14px;
    height: 14px;
    border: 0;
    padding: 0;
    background: transparent;
    color: #646a73;
    cursor: pointer;

    // SVG 原始尺寸为 24px，随按钮缩放以避免向下溢出。
    :deep(svg) {
      width: 100%;
      height: 100%;
    }

    &:disabled {
      cursor: not-allowed;
      opacity: 0.5;
    }
  }
}
</style>
