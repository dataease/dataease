<script setup lang="ts">
import { Loading } from '@element-plus/icons-vue'
import { ref, watch } from 'vue'
import draggable from 'vuedraggable'
import { enumSpreadsheetFilterValueObj } from '../../../../api/filter-option'
import type { SpreadsheetFilterCondition } from '../../../../types/plugin'
import { formatCustomSortValue, type CustomSortValue } from '../../../../utils/custom-sort'

interface SortValueItem {
  id: string
  value: CustomSortValue
}

const props = defineProps<{
  modelValue: boolean
  condition: SpreadsheetFilterCondition
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  confirm: [values: CustomSortValue[]]
}>()

const loading = ref(false)
const loadFailed = ref(false)
const sortValues = ref<SortValueItem[]>([])
let loadVersion = 0

const getItemId = (value: CustomSortValue, index: number) =>
  `filter-custom-sort-${String(value).toLowerCase()}-${index}`

const closeDialog = () => {
  emit('update:modelValue', false)
}

const setSortValues = (values: CustomSortValue[]) => {
  sortValues.value = values.map((value, index) => ({
    id: getItemId(value, index),
    value
  }))
}

const loadSortValues = async () => {
  const sortFieldId = props.condition.sortFieldId
  if (!sortFieldId) return

  const currentVersion = ++loadVersion
  loading.value = true
  loadFailed.value = false
  if (props.condition.sortList?.length) {
    // 与仪表板一致，编辑时直接回显已保存顺序，只有首次配置才查询候选值。
    setSortValues([...props.condition.sortList])
    loading.value = false
    return
  }
  try {
    const rows = await enumSpreadsheetFilterValueObj({
      queryId: sortFieldId,
      displayId: sortFieldId,
      resultMode: 1,
      searchText: ''
    })
    if (currentVersion !== loadVersion || !props.modelValue) return

    const values: CustomSortValue[] = []
    const valueKeys = new Set<string>()
    rows.forEach(row => {
      const value = row[sortFieldId]
      if (typeof value !== 'string' && typeof value !== 'number') return
      const valueKey = `${typeof value}:${String(value).toLowerCase()}`
      if (valueKeys.has(valueKey)) return
      valueKeys.add(valueKey)
      values.push(value)
    })
    setSortValues(values)
  } catch {
    if (currentVersion === loadVersion) {
      loadFailed.value = true
      sortValues.value = []
    }
  } finally {
    if (currentVersion === loadVersion) loading.value = false
  }
}

watch(
  () => props.modelValue,
  visible => {
    if (visible) {
      void loadSortValues()
      return
    }
    loadVersion += 1
    sortValues.value = []
    loading.value = false
    loadFailed.value = false
  }
)

const confirm = () => {
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
    append-to-body
    destroy-on-close
    @update:model-value="value => emit('update:modelValue', value)"
  >
    <div class="spreadsheet-filter-custom-sort">
      <div class="spreadsheet-filter-custom-sort__field-name">
        {{ condition.sortFieldName || '' }}
      </div>
      <div v-if="loading" class="spreadsheet-filter-custom-sort__state">
        <el-icon class="is-loading"><Loading /></el-icon>
        正在加载字段值...
      </div>
      <div v-else-if="loadFailed" class="spreadsheet-filter-custom-sort__state is-error">
        <span>字段值加载失败</span>
        <el-button link type="primary" @click="loadSortValues">重试</el-button>
      </div>
      <div v-else-if="!sortValues.length" class="spreadsheet-filter-custom-sort__state">
        暂无字段值
      </div>
      <draggable
        v-else
        v-model="sortValues"
        item-key="id"
        class="spreadsheet-filter-custom-sort__list"
        ghost-class="is-ghost"
      >
        <template #item="{ element }">
          <div class="spreadsheet-filter-custom-sort__item">
            {{ formatCustomSortValue(element.value) }}
          </div>
        </template>
      </draggable>
    </div>
    <template #footer>
      <el-button @click="closeDialog">取消</el-button>
      <el-button type="primary" :disabled="loading || loadFailed" @click="confirm">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped lang="less">
.spreadsheet-filter-custom-sort {
  &__field-name {
    margin-bottom: 12px;
    color: #1f2329;
    font-size: 13px;
    font-weight: 500;
  }

  &__list {
    max-height: 320px;
    display: flex;
    flex-direction: column;
    gap: 6px;
    overflow: auto;
  }

  &__item {
    padding: 7px 10px;
    border: 1px solid #dee0e3;
    border-radius: 4px;
    background: #fff;
    color: #1f2329;
    cursor: move;
  }

  &__state {
    min-height: 120px;
    border: 1px dashed #dee0e3;
    border-radius: 4px;
    color: #8f959e;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;

    &.is-error {
      color: #f54a45;
    }
  }

  :deep(.is-ghost) {
    opacity: 0.5;
  }
}
</style>
