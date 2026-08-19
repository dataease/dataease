<script setup lang="ts">
import { ref, watch } from 'vue'
import draggable from 'vuedraggable'
import { Loading } from '@element-plus/icons-vue'
import { formatCustomSortValue, mergeCustomSortValues, type CustomSortValue } from '../../utils/custom-sort'
import type { FieldItemData, PluginDataConfig } from '../../types/plugin'
import { useCustomSortValues } from './use-custom-sort-values'

interface SortValueItem {
  id: string
  value: CustomSortValue
}

const props = defineProps<{
  modelValue: boolean
  pluginType?: string
  dataConfig?: PluginDataConfig
  field: FieldItemData | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  confirm: [values: CustomSortValue[]]
}>()

const sortValues = ref<SortValueItem[]>([])
const { status, values, load, invalidate } = useCustomSortValues({
  type: () => props.pluginType,
  data: () => props.dataConfig,
  field: () => props.field
})
let reloadVersion = 0

const getItemId = (value: CustomSortValue) => `custom-sort-${String(value).toLowerCase()}`

const reload = async () => {
  const version = ++reloadVersion
  const requestedField = props.field
  invalidate()
  await load()

  // A later open, field switch, or retry owns the dialog and its drag order.
  if (
    version !== reloadVersion ||
    !props.modelValue ||
    props.field !== requestedField ||
    status.value !== 'ready'
  ) {
    return
  }

  sortValues.value = mergeCustomSortValues(requestedField?.customSort, values.value).map(value => ({
    id: getItemId(value),
    value
  }))
}

watch(
  () => [props.modelValue, props.field?.id] as const,
  ([visible]) => {
    if (visible) {
      void reload()
    } else {
      reloadVersion += 1
      invalidate()
      sortValues.value = []
    }
  },
  { immediate: true }
)

const closeDialog = () => {
  reloadVersion += 1
  emit('update:modelValue', false)
}

const handleDialogVisibilityChange = (visible: boolean) => {
  if (!visible) {
    reloadVersion += 1
  }
  emit('update:modelValue', visible)
}

const handleConfirm = () => {
  if (status.value !== 'ready') {
    return
  }

  emit('confirm', sortValues.value.map(item => item.value))
  closeDialog()
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    title="自定义排序"
    width="480px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :show-close="true"
    destroy-on-close
    @update:model-value="handleDialogVisibilityChange"
  >
    <div class="custom-sort-dialog">
      <div class="field-label">
        {{ field?.chartShowName || field?.name || '' }}
      </div>

      <div v-if="status === 'loading'" class="dialog-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        正在加载字段值...
      </div>
      <div v-else-if="status === 'empty'" class="dialog-state">暂无字段值</div>
      <div v-else-if="status === 'error'" class="dialog-state error-state">
        <span>字段值加载失败</span>
        <el-button link type="primary" @click="reload">重试</el-button>
      </div>
      <draggable
        v-else-if="status === 'ready'"
        v-model="sortValues"
        item-key="id"
        class="sort-value-list"
        ghost-class="ghost"
      >
        <template #item="{ element }">
          <div class="sort-value-item">
            {{ formatCustomSortValue(element.value) }}
          </div>
        </template>
      </draggable>
    </div>

    <template #footer>
      <el-button @click="closeDialog">取消</el-button>
      <el-button type="primary" :disabled="status !== 'ready'" @click="handleConfirm">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped lang="less">
.custom-sort-dialog {
  .field-label {
    margin-bottom: 12px;
    color: #1f2329;
    font-size: 13px;
    font-weight: 500;
    word-break: break-all;
  }

  .sort-value-list {
    display: flex;
    flex-direction: column;
    gap: 6px;
    max-height: 320px;
    overflow: auto;
  }

  .sort-value-item {
    padding: 7px 10px;
    border: 1px solid #dee0e3;
    border-radius: 4px;
    background: #fff;
    color: #1f2329;
    cursor: move;
  }

  .ghost {
    opacity: 0.5;
  }

  .dialog-state {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    min-height: 120px;
    border: 1px dashed #dee0e3;
    border-radius: 4px;
    color: #8f959e;
    font-size: 13px;
  }

  .error-state {
    color: #f54a45;
  }
}
</style>
