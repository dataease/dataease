<script lang="ts" setup>
import { computed } from 'vue'
import { fieldType } from '@/utils/attr'
import type { FieldItemData } from '../../types/plugin'
import { getSpreadsheetFieldIcon } from '../../utils/field-icon'

interface Props {
  field: FieldItemData
  index?: number
  showRemove?: boolean
  showConfig?: boolean
  dragged?: boolean
  selected?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  index: -1,
  showRemove: true,
  showConfig: true,
  dragged: false,
  selected: false
})

const emit = defineEmits<{
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
}
</style>
