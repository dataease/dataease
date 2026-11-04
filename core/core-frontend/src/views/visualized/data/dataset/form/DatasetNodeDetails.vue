<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { Icon } from '@/components/icon-custom'
import referenceTable from '@/assets/svg/reference-table.svg'
import icon_sql_outlined_1 from '@/assets/svg/icon_sql_outlined_1.svg'
import icon_searchOutline_outlined from '@/assets/svg/icon_search-outline_outlined.svg'
import { cloneDeep } from 'lodash-es'
import { getPreviewData, getTableField } from '@/api/dataset'
import { useI18n } from '@/hooks/web/useI18n'
import type { CascaderOption, CascaderValue } from 'element-plus-secondary'
import UnionFieldList from './UnionFieldList.vue'
import DatasetNodeMenu from './DatasetNodeMenu.vue'
import DatasetPanelFullscreen from './DatasetPanelFullscreen.vue'
import type { Field, Node } from './util'

type FieldTypePath = (number | string)[]
type DatasetField = Field & { dateFormatType?: string; deTypeArr?: FieldTypePath }
const props = defineProps<{
  nodes: Node[]
  isCross: boolean
  fullscreen: boolean
  allFields: DatasetField[]
  fieldOptions: CascaderOption[]
  fieldOptionsText: CascaderOption[]
  applyFields: (node: Node, fields: Field[]) => Promise<boolean>
}>()
const emit = defineEmits<{
  select: [id: string]
  command: [node: Node, command: string]
  toggleFullscreen: []
  fieldName: [id: string, value: string]
  fieldType: [id: string, value: FieldTypePath]
}>()
const { t } = useI18n()
const search = ref('')
const fieldSearch = ref('')
const selectedId = ref('')
const activeTab = ref('fields')
const fields = ref<Field[]>([])
const selectedFields = ref<Field[]>([])
const loading = ref(false)
const applying = ref(false)
const fieldsLoaded = ref(false)
const previewLoading = ref(false)
const previewFields = ref<{ dataeaseName: string; name: string }[]>([])
const previewRows = ref<Record<string, unknown>[]>([])
let fieldRequest = 0
let previewRequest = 0
const nodeList = computed(() => {
  const result: Node[] = []
  const visit = (nodes: Node[]) => {
    nodes.forEach(node => {
      if (!node.isShadow) result.push(node)
      visit(node.children || [])
    })
  }
  visit(props.nodes)
  return result
})
const filteredNodes = computed(() =>
  nodeList.value.filter(node =>
    node.tableName.toLowerCase().includes(search.value.trim().toLowerCase())
  )
)
const selectedNode = computed(() => nodeList.value.find(node => node.id === selectedId.value))
const dirty = computed(() => {
  const saved = selectedNode.value?.currentDsFields.map(field => field.originName) || []
  return (
    saved.length !== selectedFields.value.length ||
    selectedFields.value.some(field => !saved.includes(field.originName))
  )
})

type FieldDraft = { name?: string; typePath?: FieldTypePath }
const nodeFieldDrafts = new Map<string, Record<string, FieldDraft>>()
const fieldDrafts = ref<Record<string, FieldDraft>>({})
const getDatasetField = (field: Field) =>
  props.allFields.find(item => field.id && item.id === field.id) || field
const getDatasetFieldName = (field: Field) =>
  fieldDrafts.value[field.originName]?.name ?? getDatasetField(field).name
const getDatasetFieldType = (field: Field): FieldTypePath => {
  const draft = fieldDrafts.value[field.originName]?.typePath
  if (draft) return draft
  const saved = getDatasetField(field) as DatasetField
  return saved.deTypeArr?.length
    ? saved.deTypeArr
    : saved.deType === 1 && saved.deExtractType === 0
    ? [1, saved.dateFormatType || 'yyyy-MM-dd']
    : [saved.deType]
}
const getFieldTypeOptions = (field: Field) => {
  const options =
    getDatasetField(field).deExtractType === 0 ? props.fieldOptions : props.fieldOptionsText
  return field.checked
    ? options
    : options.map(option => ({
        ...option,
        children: option.children?.map(child =>
          child.value === 'custom' ? { ...child, disabled: true } : child
        )
      }))
}
const changeFieldName = (field: Field, value: string) => {
  if (field.checked && field.id) emit('fieldName', field.id, value)
  else fieldDrafts.value[field.originName] = { ...fieldDrafts.value[field.originName], name: value }
}
const changeFieldType = (field: Field, value: CascaderValue = []) => {
  if (!Array.isArray(value) || !value.length) return
  const path = value.filter(
    (part): part is number | string => typeof part === 'number' || typeof part === 'string'
  )
  if (path.length !== value.length) return
  if (field.checked && field.id) emit('fieldType', field.id, path)
  else
    fieldDrafts.value[field.originName] = { ...fieldDrafts.value[field.originName], typePath: path }
}
const sourceTableName = computed(() => {
  const node = selectedNode.value
  if (!node) return ''
  if (node.type === 'sql') return t('data_set.custom_sql')
  try {
    return JSON.parse(node.info)?.table || node.tableName
  } catch {
    return node.tableName
  }
})
const hasPendingChanges = computed(() => fieldsLoaded.value && dirty.value)
const resetFields = () => {
  const savedFields = selectedNode.value?.currentDsFields || []
  fields.value = fields.value.map(field => {
    const saved = savedFields.find(item => item.originName === field.originName)
    return { ...field, ...(saved || {}), id: saved?.id, checked: !!saved }
  })
  selectedFields.value = fields.value.filter(field => field.checked)
}

const loadFields = async () => {
  const request = ++fieldRequest
  const node = selectedNode.value
  fieldsLoaded.value = false
  fieldSearch.value = ''
  const draftKey = JSON.stringify([node?.id, node?.info])
  fieldDrafts.value = nodeFieldDrafts.get(draftKey) || {}
  nodeFieldDrafts.set(draftKey, fieldDrafts.value)
  fields.value = []
  selectedFields.value = []
  previewFields.value = []
  previewRows.value = []
  ++previewRequest
  previewLoading.value = false
  if (!node) {
    loading.value = false
    return
  }
  loading.value = true
  try {
    const result = await getTableField({
      datasourceId: node.datasourceId,
      id: node.id,
      info: node.info,
      tableName: node.tableName,
      type: node.type,
      sqlVariableDetails: node.sqlVariableDetails,
      isCross: props.isCross
    })
    if (request !== fieldRequest) return
    fields.value = (result as unknown as Field[]).map(field => {
      const saved = node.currentDsFields.find(item => item.originName === field.originName)
      return { ...field, ...(saved || {}), checked: !!saved }
    })
    selectedFields.value = fields.value.filter(field => field.checked)
    fieldsLoaded.value = true
  } catch {
    // API errors are reported by the shared request interceptor. Keep edits blocked until retry.
  } finally {
    if (request === fieldRequest) loading.value = false
  }
}

let pendingCommit: Promise<boolean> | undefined
const commit = (): Promise<boolean> => {
  if (pendingCommit) return pendingCommit
  if (loading.value || !fieldsLoaded.value) return Promise.resolve(!selectedNode.value)
  if (!selectedNode.value || !dirty.value) return Promise.resolve(true)
  applying.value = true
  pendingCommit = (async () => {
    try {
      const applied = await props.applyFields(selectedNode.value, selectedFields.value)
      await nextTick()
      resetFields()
      if (!applied)
        fields.value
          .filter(field => field.checked)
          .forEach(field => delete fieldDrafts.value[field.originName])
      if (applied) {
        fields.value
          .filter(field => field.checked)
          .forEach(field => {
            const draft = fieldDrafts.value[field.originName]
            if (!draft || !field.id) return
            if (draft.name !== undefined) emit('fieldName', field.id, draft.name)
            if (draft.typePath) emit('fieldType', field.id, draft.typePath)
            delete fieldDrafts.value[field.originName]
          })
        ++previewRequest
        previewFields.value = []
        previewRows.value = []
        previewLoading.value = false
      }
      return applied
    } finally {
      applying.value = false
      pendingCommit = undefined
    }
  })()
  return pendingCommit
}
const changeFields = (value: Field[]) => {
  if (applying.value || !fieldsLoaded.value) return
  selectedFields.value
    .filter(field => !value.some(item => item.originName === field.originName))
    .forEach(field => {
      fieldDrafts.value[field.originName] = {
        name: getDatasetFieldName(field),
        typePath: getDatasetFieldType(field)
      }
    })
  selectedFields.value = value
  if (dirty.value) void commit()
}
const selectNode = async (id: string) => {
  if (id !== selectedId.value && fieldsLoaded.value && !(await commit())) {
    emit('select', selectedId.value)
    return
  }
  selectedId.value = id
  activeTab.value = 'fields'
  emit('select', id)
}
const handleNodeCommand = async (node: Node, command: string) => {
  if ((applying.value || hasPendingChanges.value) && !(await commit())) return
  emit('command', node, command)
}
const preview = async () => {
  if (!(await commit())) {
    activeTab.value = 'fields'
    return
  }
  const node = selectedNode.value
  if (!node || !node.currentDsFields.length) return
  const request = ++previewRequest
  previewLoading.value = true
  previewFields.value = []
  previewRows.value = []
  const { sqlVariableDetails, tableName, type, datasourceId, id, info, currentDsFields } =
    cloneDeep(node)
  try {
    const result = await getPreviewData({
      union: [
        {
          currentDs: { sqlVariableDetails, tableName, type, datasourceId, id, info },
          currentDsFields,
          childrenDs: [],
          unionToParent: { unionType: 'left', unionFields: [] }
        }
      ],
      allFields: cloneDeep(currentDsFields.map(field => getDatasetField(field))),
      isCross: props.isCross
    })
    if (request !== previewRequest) return
    previewFields.value = result.data.fields || []
    previewRows.value = result.data.data || []
  } catch {
    // Leave the preview empty after a failed request instead of showing another node's data.
  } finally {
    if (request === previewRequest) previewLoading.value = false
  }
}
// Drag previews replace the cloned node tree without changing the field query inputs.
const fieldSourceKey = computed(() => {
  const node = selectedNode.value
  return node
    ? JSON.stringify([
        node.id,
        node.datasourceId,
        node.tableName,
        node.type,
        node.info,
        node.sqlVariableDetails,
        props.isCross
      ])
    : ''
})
watch(fieldSourceKey, () => {
  void loadFields()
})
watch(
  nodeList,
  nodes => {
    if (!nodes.some(node => node.id === selectedId.value)) {
      selectedId.value = nodes[0]?.id || ''
      emit('select', selectedId.value)
    }
  },
  { immediate: true }
)
watch(activeTab, tab => {
  if (tab === 'preview') void preview()
})
onBeforeUnmount(() => {
  ++fieldRequest
  ++previewRequest
})
defineExpose({ commit, selectNode, hasPendingChanges })
</script>

<template>
  <div class="dataset-node-details">
    <aside class="node-list">
      <el-input
        class="node-search"
        v-model="search"
        :placeholder="t('deDataset.by_table_name')"
        clearable
      >
        <template #prefix>
          <el-icon
            ><Icon><icon_searchOutline_outlined class="svg-icon" /></Icon
          ></el-icon>
        </template>
      </el-input>
      <div class="node-items">
        <div
          v-for="node in filteredNodes"
          :key="node.id"
          class="node-item list-item_primary"
          :class="{ active: node.id === selectedId }"
        >
          <button type="button" :title="node.tableName" @click="selectNode(node.id)">
            <el-icon class="node-icon" aria-hidden="true">
              <Icon
                ><component
                  class="svg-icon"
                  :is="node.type === 'sql' ? icon_sql_outlined_1 : referenceTable"
              /></Icon>
            </el-icon>
            <span class="label">{{ node.tableName }}</span>
          </button>
          <dataset-node-menu
            :node-type="node.type"
            @command="command => handleNodeCommand(node, command)"
          />
        </div>
      </div>
    </aside>
    <section v-loading="loading || applying" class="node-content">
      <template v-if="selectedNode">
        <div class="node-title">
          <span class="ellipsis" :title="selectedNode.tableName">{{ selectedNode.tableName }}</span>
          <dataset-panel-fullscreen
            class="details-fullscreen"
            :fullscreen="fullscreen"
            :label="t('dataset.node_details_fullscreen')"
            @toggle="emit('toggleFullscreen')"
          />
        </div>
        <div class="node-tabs-row">
          <el-tabs v-model="activeTab" class="node-tabs">
            <el-tab-pane
              :label="`${t('dataset.field_select')}(${selectedFields.length}/${fields.length})`"
              name="fields"
            />
            <el-tab-pane :label="t('chart.data_preview')" name="preview" />
          </el-tabs>
          <el-input
            v-show="activeTab === 'fields'"
            v-model="fieldSearch"
            class="node-field-search"
            :placeholder="t('auth.search_by_field')"
            clearable
          >
            <template #prefix>
              <el-icon
                ><Icon><icon_searchOutline_outlined class="svg-icon" /></Icon
              ></el-icon>
            </template>
          </el-input>
        </div>
        <div v-show="activeTab === 'fields'" class="node-fields">
          <el-button v-if="!fieldsLoaded && !loading" secondary @click="loadFields">{{
            t('data_set.refresh_data')
          }}</el-button>
          <union-field-list
            v-if="fieldsLoaded"
            :key="selectedNode.id"
            v-model:search="fieldSearch"
            :show-toolbar="false"
            detailed
            :field-list="fields"
            :node="selectedNode"
            :disabled="applying"
            @checked-fields="changeFields"
          >
            <template #columns>
              <el-table-column :label="t('dataset.dataset_field_name')" min-width="180">
                <template #default="{ row }">
                  <el-input
                    class="field-editor"
                    :model-value="getDatasetFieldName(row)"
                    :disabled="applying"
                    :aria-label="`${t('dataset.dataset_field_name')} ${row.originName}`"
                    maxlength="100"
                    @update:model-value="value => changeFieldName(row, value)"
                  />
                </template>
              </el-table-column>
              <el-table-column :label="t('dataset.dataset_field_type')" min-width="160">
                <template #default="{ row }">
                  <el-cascader
                    class="field-editor field-type-editor"
                    :model-value="getDatasetFieldType(row)"
                    :disabled="applying"
                    :aria-label="`${t('dataset.dataset_field_type')} ${row.originName}`"
                    :options="getFieldTypeOptions(row)"
                    :show-all-levels="false"
                    @change="value => changeFieldType(row, value)"
                  />
                </template>
              </el-table-column>
              <el-table-column
                :label="t('dataset.source_physical_table')"
                min-width="160"
                show-overflow-tooltip
              >
                <template #default
                  ><span class="source-table-name">{{ sourceTableName }}</span></template
                >
              </el-table-column>
              <el-table-column
                :label="t('dataset.field_remark')"
                min-width="160"
                show-overflow-tooltip
              >
                <template #default="{ row }"
                  ><span class="field-remark">{{ row.description }}</span></template
                >
              </el-table-column>
            </template>
          </union-field-list>
        </div>
        <div v-show="activeTab === 'preview'" v-loading="previewLoading" class="node-preview">
          <el-button
            :disabled="!selectedNode.currentDsFields.length"
            :loading="previewLoading"
            secondary
            @click="preview"
            >{{ t('data_set.refresh_data') }}</el-button
          >
          <el-table :data="previewRows" height="100%" border>
            <el-table-column
              v-for="field in previewFields"
              :key="field.dataeaseName"
              :prop="field.dataeaseName"
              :label="field.name"
              min-width="150"
            />
          </el-table>
        </div>
      </template>
      <el-empty v-else :description="t('dataset.pls_slc_tbl_left')" />
    </section>
  </div>
</template>

<style scoped lang="less">
@import '@/style/mixin.less';

.node-tabs-row {
  position: relative;
  flex-shrink: 0;
  margin-bottom: 16px;
}
.node-tabs {
  .border-bottom-tab(24px);
  margin-left: 0;
  :deep(.ed-tabs__header) {
    margin-bottom: 0;
  }
  :deep(.ed-tabs__item) {
    font-size: 14px;
  }
  :deep(.ed-tabs__nav-wrap) {
    padding-right: 256px;
  }
}
.details-fullscreen {
  flex-shrink: 0;
}
.node-field-search {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 240px;
}

.dataset-node-details {
  display: flex;
  min-height: 0;
  height: 100%;
  border-top: 1px solid var(--deCardStrokeColor, #dee0e3);
}
.node-list {
  width: 240px;
  flex-shrink: 0;
  padding: 16px 0;
  font-family: var(--de-custom_font, 'PingFang');
  border-right: 1px solid var(--deCardStrokeColor, #dee0e3);
  display: flex;
  flex-direction: column;
  min-height: 0;
}
.node-items {
  flex: 1;
  overflow: auto;
  margin-top: 12px;
  padding: 0 8px;
  .node-item {
    padding: 8px;
    > :last-child {
      flex-shrink: 0;
    }
  }
  button {
    display: flex;
    align-items: center;
    flex: 1;
    min-width: 0;
    border: 0;
    padding: 0;
    background: transparent;
    color: inherit;
    font: inherit;
    text-align: left;
    cursor: pointer;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    .node-icon {
      flex-shrink: 0;
      color: #646a73;
    }
    .label {
      flex: 1;
      min-width: 0;
    }
  }
}
.node-search {
  margin: 0 16px;
  width: calc(100% - 32px);
}
.node-content {
  flex: 1;
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  padding: 16px 24px;
}
.node-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-height: 24px;
  font-weight: 500;
  flex-shrink: 0;
  > span {
    min-width: 0;
  }
}
.node-fields {
  :deep(.header-cell) {
    padding: 10px 0;
    background-color: #f5f6ff !important;
  }
  :deep(td.ed-table__cell) {
    padding: 8px 0;
  }
  :deep(.field-editor),
  :deep(.ed-cascader) {
    width: 100%;
  }
  :deep(.field-editor .ed-input__wrapper),
  :deep(.ed-cascader .ed-input__wrapper) {
    padding: 0 8px;
    background: transparent;
    box-shadow: none;
    &:hover {
      box-shadow: 0 0 0 1px var(--ed-border-color) inset;
    }
    &.is-focus {
      box-shadow: 0 0 0 1px var(--ed-color-primary) inset;
    }
  }
  .field-remark {
    color: var(--deTextSecondary, #646a73);
  }
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  :deep(.field-block-style) {
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;
    .field-block-body {
      flex: 1;
      min-height: 0;
      height: auto;
    }
  }
}
.node-preview {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: flex-start;
  .ed-table {
    flex: 1;
    min-height: 0;
  }
}
</style>
