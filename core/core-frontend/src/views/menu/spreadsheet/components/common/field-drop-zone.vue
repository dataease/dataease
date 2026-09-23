<script lang="ts" setup>
import { ref, computed, watch } from 'vue'
import draggable from 'vuedraggable'
import { ElMessage } from 'element-plus-secondary'
import DefaultDropZoneFieldItem from './default-drop-zone-field-item.vue'
import FieldDisplayNameDialog from './field-display-name-dialog.vue'
import type { FieldItemData, FieldZoneSchema, PluginDataConfig } from '../../types/plugin'

interface Props {
  modelValue: FieldItemData[]
  zoneSchema?: FieldZoneSchema
  pluginType?: string
  dataConfig?: PluginDataConfig
  title?: string
  placeholder?: string
  required?: boolean
  maxFields?: number
  acceptTypes?: ('d' | 'q')[]
  validateUpdate?: (fields: FieldItemData[]) => string | undefined
}

const props = withDefaults(defineProps<Props>(), {
  title: '字段',
  placeholder: '拖动字段至此处',
  required: false,
  maxFields: Infinity,
  acceptTypes: () => ['d', 'q']
})

const emit = defineEmits<{
  'update:modelValue': [fields: FieldItemData[]]
  'fieldAdd': [field: FieldItemData]
  'fieldRemove': [field: FieldItemData, index: number]
  'fieldConfig': [field: FieldItemData, index: number]
  'fieldRename': [field: FieldItemData, index: number]
}>()

const fieldItemComponent = computed(() =>
  props.zoneSchema?.fieldItemComponent || DefaultDropZoneFieldItem
)

const localFields = ref<FieldItemData[]>([...props.modelValue])
const chartShowNameDialogVisible = ref(false)
const editingField = ref<FieldItemData | null>(null)
const editingIndex = ref(-1)

watch(
  () => props.modelValue,
  val => {
    localFields.value = [...val]
  }
)

const updateFields = (
  fields: FieldItemData[],
  options?: { syncLocal?: boolean; skipValidation?: boolean }
) => {
  if (!options?.skipValidation) {
    const validateMessage = props.validateUpdate?.(fields)
    if (validateMessage) {
      localFields.value = [...props.modelValue]
      ElMessage.warning(validateMessage)
      return false
    }
  }
  if (options?.syncLocal) {
    localFields.value = [...fields]
  }
  emit('update:modelValue', [...fields])
  return true
}

const isDragOver = ref(false)

const handleDragEnter = (e: DragEvent) => {
  e.preventDefault()
  isDragOver.value = true
}

const handleDragLeave = (e: DragEvent) => {
  e.preventDefault()
  isDragOver.value = false
}

const handleDrop = (e: DragEvent) => {
  e.preventDefault()
  isDragOver.value = false

  const data = e.dataTransfer?.getData('application/json')
  if (!data) return

  try {
    // 解析拖拽数据，可能是单个字段或字段数组
    const parsed = JSON.parse(data)
    const fields: FieldItemData[] = Array.isArray(parsed) ? parsed : [parsed]

    // 获取目标位置（鼠标释放位置对应的字段）
    const targetId = (e.target as HTMLElement)?.closest('[data-id]')?.getAttribute('data-id')
    let insertIndex = -1
    if (targetId) {
      insertIndex = localFields.value.findIndex(f => f.id === targetId)
    }

    // 过滤和添加字段
    const newFields = [...localFields.value]
    const addedFields: FieldItemData[] = []

    for (const field of fields) {
      // 检查类型是否接受
      if (!props.acceptTypes.includes(field.groupType)) {
        continue
      }

      // 检查是否已达上限
      if (newFields.length >= props.maxFields) {
        break
      }

      // 检查是否已存在
      const exists = newFields.some(f => f.id === field.id)
      if (exists) {
        continue
      }

      // 字段首次进入插件配置时显式设置默认排序，避免依赖界面显示兜底。
      const normalizedField: FieldItemData = {
        ...field,
        sort: field.sort ?? 'none'
      }

      // 添加到指定位置或末尾
      if (insertIndex !== -1) {
        newFields.splice(insertIndex + 1, 0, normalizedField)
        insertIndex++ // 更新插入位置，后续字段依次插入到后面
      } else {
        newFields.push(normalizedField)
      }
      addedFields.push(normalizedField)
    }

    // 更新字段列表
    if (addedFields.length > 0) {
      updateFields(newFields)
      addedFields.forEach(field => emit('fieldAdd', field))
    }
  } catch (error) {
  }
}

const handleRemove = (index: number) => {
  const field = localFields.value[index]
  const newFields = localFields.value.filter((_, i) => i !== index)
  updateFields(newFields, { syncLocal: true, skipValidation: true })
  emit('fieldRemove', field, index)
}

const handleUpdateField = (index: number, field: FieldItemData) => {
  const newFields = [...localFields.value]
  newFields[index] = field
  updateFields(newFields, { syncLocal: true })
}

const handleConfig = (field: FieldItemData, index: number) => {
  emit('fieldConfig', field, index)
}

const handleRename = (field: FieldItemData, index: number) => {
  editingField.value = field
  editingIndex.value = index
  chartShowNameDialogVisible.value = true
  emit('fieldRename', field, index)
}

const handleDisplayNameConfirm = (field: FieldItemData) => {
  if (editingIndex.value < 0) {
    return
  }

  handleUpdateField(editingIndex.value, field)
  editingField.value = null
  editingIndex.value = -1
}

const handleDragEnd = (e: any) => {
  // vuedraggable 的拖拽结束事件
  updateFields(localFields.value, { syncLocal: true })
}

const isEmpty = computed(() => localFields.value.length === 0)
</script>

<template>
  <div
    class="field-drop-zone"
    :class="{
      'is-drag-over': isDragOver,
      'is-empty': isEmpty
    }"
    @dragenter="handleDragEnter"
    @dragleave="handleDragLeave"
    @dragover.prevent
    @drop="handleDrop"
  >
    <div v-if="isEmpty" class="drop-placeholder">
      {{ placeholder }}
    </div>

    <draggable
      v-else
      v-model="localFields"
      item-key="id"
      class="field-list"
      ghost-class="ghost"
      drag-class="dragging"
      @end="handleDragEnd"
    >
      <template #item="{ element, index }">
        <div class="field-wrapper" :data-id="element.id">
          <component
            :is="fieldItemComponent"
            :field="element"
            :index="index"
            :zone-schema="zoneSchema"
            :plugin-type="pluginType"
            :data-config="dataConfig"
            @remove="handleRemove"
            @update-field="handleUpdateField"
            @config="handleConfig"
            @rename="handleRename"
          />
        </div>
      </template>
    </draggable>

    <div v-if="localFields.length >= maxFields" class="limit-hint">
      最多添加 {{ maxFields }} 个字段
    </div>

    <FieldDisplayNameDialog
      v-model="chartShowNameDialogVisible"
      :field="editingField"
      @confirm="handleDisplayNameConfirm"
    />
  </div>
</template>

<style lang="less" scoped>
.field-drop-zone {
  min-height: 40px;
  padding: 0 4px;
  border: 1px dashed #c9cdd4;
  border-radius: 4px;
  background: #f5f7fa;
  transition: all 0.2s;
  display: flex;
  align-items: center;

  &.is-drag-over {
    border-color: var(--ed-color-primary, #3370ff);
    background: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1));
  }

  &.is-empty {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 40px;
    height: 40px;
  }

  .drop-placeholder {
    color: #bbbfc4;
    font-size: 13px;
    text-align: center;
    line-height: 40px;
  }

    .field-list {
    display: flex;
    flex-direction: column;
    gap: 4px;
    min-height: 32px;
    width: 100%;
    padding: 4px 0;

      .field-wrapper {
        background: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1));
        border: 1px solid var(--ed-color-primary, #3370ff);
        border-radius: 4px;
        overflow: hidden;

      :deep(.ed-dropdown) {
        display: block;
        width: 100%;
      }

      &:hover {
        background: var(--ed-color-primary-33, rgba(51, 112, 255, 0.2));
      }
    }
  }

  .limit-hint {
    margin-top: 8px;
    font-size: 12px;
    color: #bbbfc4;
    text-align: center;
  }

  :deep(.ghost) {
    opacity: 0.5;
    background: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1));
  }

  :deep(.dragging) {
    opacity: 0.8;
  }
}
</style>
