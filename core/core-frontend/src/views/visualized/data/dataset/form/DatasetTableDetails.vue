<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { getPreviewData, getTableField, type Table } from '@/api/dataset'
import { useI18n } from '@/hooks/web/useI18n'
import { guid, type Field } from './util'
import { iconFieldMap } from '@/components/icon-group/field-list'
import { fieldType } from '@/utils/attr'
import iconSearch from '@/assets/svg/icon_search-outline_outlined.svg'
import iconRefresh from '@/assets/svg/icon_refresh_outlined.svg'

type SourceField = Field & { description?: string; dataeaseName?: string }
const { t } = useI18n()
const visible = ref(false)
const table = ref<Table>()
const activeTab = ref('preview')
const search = ref('')
const fields = ref<SourceField[]>([])
const previewFields = ref<SourceField[]>([])
const rows = ref<Record<string, unknown>[]>([])
const loading = ref(false)
let requestId = 0
const filteredFields = computed(() => {
  const keyword = search.value.trim().toLocaleLowerCase()
  return fields.value.filter(field => field.originName.toLocaleLowerCase().includes(keyword))
})
const load = async () => {
  const source = table.value
  if (!source) return
  const request = ++requestId
  loading.value = true
  fields.value = []
  previewFields.value = []
  rows.value = []
  const currentDs = {
    id: guid(),
    datasourceId: source.datasourceId,
    tableName: source.tableName,
    type: source.type,
    info: JSON.stringify({ table: source.tableName, sql: '' })
  }
  try {
    const result = await getTableField({ ...currentDs, isCross: false })
    if (request !== requestId) return
    fields.value = ((result as unknown as SourceField[]) || []).map(field => ({
      ...field,
      id: guid(),
      checked: true,
      datasetTableId: currentDs.id,
      datasourceId: currentDs.datasourceId
    }))
    if (!fields.value.length) return
    const preview = await getPreviewData({
      union: [
        {
          currentDs,
          currentDsFields: fields.value,
          childrenDs: [],
          unionToParent: { unionType: 'left', unionFields: [] }
        }
      ],
      allFields: fields.value,
      isCross: false
    })
    if (request !== requestId) return
    previewFields.value = preview?.data?.fields || []
    rows.value = preview?.data?.data || []
  } catch {
    // The shared request interceptor reports errors; keep failed previews empty.
  } finally {
    if (request === requestId) loading.value = false
  }
}
const open = (source: Table) => {
  table.value = { ...source }
  search.value = ''
  activeTab.value = 'preview'
  visible.value = true
  void load()
}
watch(visible, value => {
  if (!value) {
    ++requestId
    loading.value = false
  }
})
onBeforeUnmount(() => ++requestId)
defineExpose({ open })
</script>

<template>
  <el-dialog
    v-model="visible"
    :title="table?.tableName"
    width="800px"
    destroy-on-close
    append-to-body
    class="dataset-table-details"
  >
    <div class="table-details-toolbar">
      <el-tabs v-model="activeTab">
        <el-tab-pane :label="t('chart.data_preview')" name="preview" />
        <el-tab-pane :label="t('dataset.field_details')" name="fields" />
      </el-tabs>
      <el-input
        v-if="activeTab === 'fields'"
        v-model="search"
        :placeholder="t('deDataset.search_fields')"
        clearable
      >
        <template #prefix
          ><el-icon><iconSearch /></el-icon
        ></template>
      </el-input>
      <el-button text :aria-label="t('data_set.refresh_data')" @click="load">
        <el-icon><iconRefresh /></el-icon>
      </el-button>
    </div>
    <div v-loading="loading" class="table-details-content">
      <el-table
        v-if="activeTab === 'fields'"
        :data="filteredFields"
        height="400"
        show-overflow-tooltip
      >
        <el-table-column prop="originName" :label="t('data_set.field_name')" min-width="200">
          <template #default="{ row }">
            <span class="source-field-name">
              <el-icon :class="`field-icon-${fieldType[row.deType]}`">
                <component :is="iconFieldMap[fieldType[row.deType]]" />
              </el-icon>
              {{ row.originName }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="type" :label="t('data_set.field_type')" min-width="150" />
        <el-table-column prop="description" :label="t('data_set.field_notes')" min-width="240" />
      </el-table>
      <el-table v-else :data="rows" height="400" show-overflow-tooltip>
        <el-table-column
          v-for="field in previewFields"
          :key="field.dataeaseName || field.id"
          :prop="field.dataeaseName || field.id"
          :label="field.originName || field.name"
          min-width="150"
        />
      </el-table>
    </div>
  </el-dialog>
</template>

<style scoped lang="less">
.table-details-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;

  .ed-tabs {
    flex: 1;
    min-width: 0;
  }

  .ed-input {
    width: 200px;
  }
}

.table-details-content {
  min-height: 400px;

  :deep(.ed-table .cell) {
    font-size: 14px;
  }
}

.source-field-name {
  display: flex;
  align-items: center;
  gap: 6px;
}
</style>
