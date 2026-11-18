<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import FixedSizeList from 'element-plus-secondary/es/components/virtual-list/src/components/fixed-size-list.mjs'
import icon_close_outlined from '@/assets/svg/icon_close_outlined.svg'
import icon_searchOutline_outlined from '@/assets/svg/icon_search-outline_outlined.svg'
import DatasourceMetadataAi from './DatasourceMetadataAi.vue'
import { useI18n } from '@/hooks/web/useI18n'
import { listDatasourceTables, type PhysicalTable } from '@/api/datasource'
import {
  addDatasourceSemanticTables,
  getDatasourceSemantics,
  getDatasourceSemanticDetails,
  refreshDatasourceSemanticTable,
  removeDatasourceSemanticTable,
  saveDatasourceFieldSemantic,
  saveDatasourceTableSemantic,
  setDatasourceTableSemanticConfirmed,
  setDatasourceFieldSemanticConfirmed,
  type DatasourceSemantics,
  type FieldSemantic,
  type TableSemantic,
  type TableSemanticDetails
} from '@/api/datasourceSemantic'

const props = defineProps<{ datasourceId: string; datasourceName: string; canManage: boolean }>()
const emit = defineEmits<{ 'show-relations': [tableName: string] }>()
const { t } = useI18n()
const { t: tr } = useI18n('datasourceMetadata')

const semantics = ref<DatasourceSemantics>({ tables: [] })
const tables = ref<PhysicalTable[]>([])
const loading = ref(false)
const loadFailed = ref(false)
const saving = ref(false)
const drawer = ref<'table' | 'field' | 'membership' | ''>('')
const selectedTable = defineModel<string>('selectedTable', { required: true })
const pendingTableNames = ref(new Set<string>())
const membershipSearch = ref('')
const onlySelected = ref(false)
const membershipList = ref<{ scrollTo: (offset: number) => void }>()
const membershipRowHeight = 62
const loadingTables = ref(false)

const tableForm = ref<TableSemantic>()
const fieldForm = ref<FieldSemantic>()
const editingField = ref<TableSemanticDetails['fields'][number]>()

const activeTables = computed(() => semantics.value.tables.filter(table => table.active))
const currentTable = computed(() =>
  activeTables.value.find(table => table.tableName === selectedTable.value)
)
const activeTableNames = computed(() => new Set(activeTables.value.map(table => table.tableName)))
const membershipOptions = computed(() => {
  const options = new Map(tables.value.map(table => [table.tableName, table]))
  for (const table of activeTables.value) {
    if (!options.has(table.tableName)) {
      options.set(table.tableName, {
        tableName: table.tableName,
        name: table.physicalLabel ?? undefined
      })
    }
  }
  return Array.from(options.values())
})
const tableComment = (table: PhysicalTable) => (table.name !== table.tableName ? table.name : '')
const filteredMembershipOptions = computed(() => {
  const keyword = membershipSearch.value.trim().toLowerCase()
  return membershipOptions.value.filter(
    table =>
      (!onlySelected.value ||
        activeTableNames.value.has(table.tableName) ||
        pendingTableNames.value.has(table.tableName)) &&
      (!keyword ||
        table.tableName.toLowerCase().includes(keyword) ||
        tableComment(table)?.toLowerCase().includes(keyword))
  )
})
const selectableResults = computed(() =>
  filteredMembershipOptions.value
    .map(table => table.tableName)
    .filter(name => !activeTableNames.value.has(name))
)
const canSelectResults = computed(() =>
  selectableResults.value.some(name => !pendingTableNames.value.has(name))
)
const canClearResults = computed(() =>
  selectableResults.value.some(name => pendingTableNames.value.has(name))
)

watch([membershipSearch, onlySelected], () => membershipList.value?.scrollTo(0), { flush: 'post' })

const setTableSelected = (tableName: string, selected: boolean) => {
  if (saving.value || activeTableNames.value.has(tableName)) return
  if (selected) {
    pendingTableNames.value.add(tableName)
  } else {
    pendingTableNames.value.delete(tableName)
  }
}

const setResultsSelected = (selected: boolean) => {
  for (const name of selectableResults.value) setTableSelected(name, selected)
}

const detail = ref<TableSemanticDetails>()
const detailLoading = ref(false)
const detailFailed = ref(false)
let detailRequest = 0

const loadDetail = async () => {
  const request = ++detailRequest
  const tableId = currentTable.value?.id
  detail.value = undefined
  detailFailed.value = false
  detailLoading.value = !!tableId
  if (!tableId) return
  try {
    const { data } = await getDatasourceSemanticDetails(props.datasourceId, tableId)
    if (request === detailRequest) detail.value = data
  } catch {
    if (request === detailRequest) detailFailed.value = true
  } finally {
    if (request === detailRequest) detailLoading.value = false
  }
}

watch(currentTable, loadDetail)

const fields = computed(() => detail.value?.fields ?? [])
const confirmedCount = computed(() => fields.value.filter(field => field.confirmed).length)
const relationCount = computed(() => detail.value?.relationCount ?? 0)
const refreshSemantics = async () => {
  const { data } = await getDatasourceSemantics(props.datasourceId)
  semantics.value = data
  if (!currentTable.value) selectedTable.value = activeTables.value[0]?.tableName ?? ''
}

const load = async () => {
  loading.value = true
  loadFailed.value = false
  try {
    await refreshSemantics()
  } catch {
    loadFailed.value = true
  } finally {
    loading.value = false
  }
}

onMounted(load)

const loadTables = async () => {
  loadingTables.value = true
  try {
    const { data } = await listDatasourceTables({ datasourceId: props.datasourceId })
    tables.value = data
    return true
  } catch {
    return false
  } finally {
    loadingTables.value = false
  }
}

const runSave = async (action: () => Promise<unknown>) => {
  if (saving.value) return
  saving.value = true
  try {
    await action()
    drawer.value = ''
    await load()
  } catch {
    // The shared request interceptor displays the error.
  } finally {
    saving.value = false
  }
}

const openMembership = async () => {
  if (!(await loadTables())) return
  pendingTableNames.value.clear()
  membershipSearch.value = ''
  onlySelected.value = false
  drawer.value = 'membership'
}

const addTables = () =>
  runSave(async () => {
    const selected = [...pendingTableNames.value]
    if (!selected.length) return
    await addDatasourceSemanticTables(props.datasourceId, selected)
    selectedTable.value = selected[0]
  })

const removeTable = async (tableName: string) => {
  try {
    await ElMessageBox.confirm(tr('removeTablePrompt', { name: tableName }), tr('remove'), {
      type: 'warning',
      confirmButtonText: tr('remove'),
      cancelButtonText: t('common.cancel')
    })
  } catch {
    return
  }
  await runSave(() => removeDatasourceSemanticTable(props.datasourceId, tableName))
}

const refreshTable = async (tableName: string) => {
  try {
    await ElMessageBox.confirm(tr('refreshTablePrompt', { name: tableName }), tr('refreshTable'), {
      type: 'warning',
      confirmButtonText: tr('refreshTable'),
      cancelButtonText: t('common.cancel')
    })
  } catch {
    return
  }
  await runSave(async () => {
    const { data: available } = await refreshDatasourceSemanticTable(props.datasourceId, tableName)
    if (!available) ElMessage.warning(tr('tableUnavailableNamed', { table: tableName }))
  })
}

const openTable = () => {
  if (!currentTable.value) return
  tableForm.value = {
    ...currentTable.value,
    description: currentTable.value.description ?? '',
    grain: currentTable.value.grain ?? ''
  }
  drawer.value = 'table'
}

const saveTable = (form: TableSemantic) =>
  runSave(() => saveDatasourceTableSemantic(props.datasourceId, form))

const openField = (semantic: TableSemanticDetails['fields'][number]) => {
  editingField.value = semantic
  fieldForm.value = {
    tableName: semantic.tableName,
    fieldName: semantic.fieldName,
    businessName: semantic.businessName ?? '',
    description: semantic.description ?? '',
    aliases: semantic.aliases ?? ''
  }
  drawer.value = 'field'
}

const saveField = (form: FieldSemantic) =>
  runSave(() => saveDatasourceFieldSemantic(props.datasourceId, form))

const toggleTableConfirmation = (row: TableSemantic) =>
  runSave(() =>
    setDatasourceTableSemanticConfirmed(props.datasourceId, row.tableName, !row.confirmed)
  )

const toggleFieldConfirmation = (row: TableSemanticDetails['fields'][number]) =>
  runSave(() =>
    setDatasourceFieldSemanticConfirmed(
      props.datasourceId,
      row.tableName,
      row.fieldName,
      !row.confirmed
    )
  )

const showRelations = () => emit('show-relations', selectedTable.value)

const tableLabel = (table: TableSemantic) =>
  table.physicalLabel !== table.tableName ? table.physicalLabel?.trim() : ''
const fieldColumns = computed(() => [
  { key: 'fieldName', title: tr('fieldAndType'), width: 135, flexGrow: 1 },
  { key: 'businessName', title: tr('businessName'), width: 120, flexGrow: 1 },
  { key: 'description', title: tr('description'), width: 190, flexGrow: 2 },
  { key: 'status', title: tr('status'), width: 100 },
  ...(props.canManage ? [{ key: 'actions', title: tr('actions'), width: 180 }] : [])
])
const tableList = ref<{ scrollToItem: (index: number) => void }>()
watch(
  activeTables,
  tables => {
    const index = tables.findIndex(table => table.tableName === selectedTable.value)
    if (index >= 0) tableList.value?.scrollToItem(index)
  },
  { flush: 'post' }
)

const fieldLabel = (field: TableSemanticDetails['fields'][number]) =>
  [`${field.tableName}.${field.fieldName}`, field.physicalType].filter(Boolean).join('·')
</script>

<template>
  <section v-loading="loading" class="metadata-panel metadata-semantic-panel">
    <el-empty v-if="loadFailed" :description="tr('loadFailed')">
      <el-button @click="load">{{ tr('retry') }}</el-button>
    </el-empty>
    <div v-else class="metadata-semantics">
      <aside class="metadata-sidebar">
        <div class="metadata-heading">
          <span>{{ tr('semanticTables') }}</span
          ><span>{{ activeTables.length }}</span>
        </div>
        <el-button v-if="canManage" :disabled="saving || loadingTables" @click="openMembership">{{
          tr('addTables')
        }}</el-button>
        <div class="metadata-virtual-list">
          <el-auto-resizer>
            <template #default="{ height, width }">
              <FixedSizeList
                v-if="height > 0 && width > 0 && activeTables.length"
                ref="tableList"
                :total="activeTables.length"
                :item-size="52"
                :height="height"
                :width="width"
              >
                <template #default="{ index, style }">
                  <div class="metadata-table-row" :style="style">
                    <button
                      type="button"
                      class="metadata-table-button"
                      :class="{ selected: activeTables[index].tableName === selectedTable }"
                      :aria-pressed="activeTables[index].tableName === selectedTable"
                      @click="selectedTable = activeTables[index].tableName"
                    >
                      <strong class="metadata-label" :title="activeTables[index].tableName">{{
                        activeTables[index].tableName
                      }}</strong>
                      <div
                        class="metadata-secondary"
                        :title="tableLabel(activeTables[index]) || undefined"
                      >
                        {{ tableLabel(activeTables[index]) || '-' }}
                      </div>
                    </button>
                    <button
                      v-if="canManage"
                      class="metadata-remove-table"
                      type="button"
                      :aria-label="tr('removeNamedTable', { table: activeTables[index].tableName })"
                      :title="tr('remove')"
                      :disabled="saving"
                      @click="removeTable(activeTables[index].tableName)"
                    >
                      <Icon name="icon_close_outlined"
                        ><icon_close_outlined class="svg-icon" aria-hidden="true"
                      /></Icon>
                    </button>
                  </div>
                </template>
              </FixedSizeList>
            </template>
          </el-auto-resizer>
        </div>
      </aside>
      <div v-if="currentTable" class="metadata-detail metadata-field-pane">
        <div class="metadata-overview">
          <div class="metadata-heading metadata-detail-header">
            <strong>{{ currentTable.tableName }}</strong>
            <span v-if="tableLabel(currentTable)" class="metadata-secondary">{{
              tableLabel(currentTable)
            }}</span>
            <div v-if="canManage" class="metadata-table-actions">
              <el-button :disabled="saving" @click="openTable">{{ tr('edit') }}</el-button>
              <el-button
                :disabled="saving || (!currentTable.available && !currentTable.confirmed)"
                @click="toggleTableConfirmation(currentTable)"
              >
                {{ tr(currentTable.confirmed ? 'unconfirm' : 'confirm') }}
              </el-button>
              <el-button :disabled="saving" @click="refreshTable(currentTable.tableName)">
                {{ tr('refreshTable') }}
              </el-button>
              <DatasourceMetadataAi
                :datasource-id="datasourceId"
                :datasource-name="datasourceName"
                kind="SEMANTIC"
                :semantic-tables="
                  activeTables
                    .filter(table => table.available)
                    .map(table => ({ tableName: table.tableName, name: table.physicalLabel }))
                "
                @refresh="load"
              />
            </div>
          </div>
          <div class="metadata-secondary metadata-section">{{ tr('description') }}</div>
          <div class="metadata-label metadata-description">
            {{ currentTable.description || tr('noDescription') }}
          </div>
          <div class="metadata-secondary metadata-section">
            {{ tr('grain') }} · {{ currentTable.grain?.trim() || tr('incomplete') }}
            <el-tag :type="currentTable.confirmed ? 'success' : 'warning'" size="small">
              {{ tr(currentTable.confirmed ? 'confirmed' : 'pending') }}
            </el-tag>
          </div>
          <div class="metadata-heading metadata-field-heading">
            <strong class="metadata-label"
              >{{ tr('fieldSemantics') }} {{ detail ? fields.length : '' }}</strong
            >
            <el-button class="metadata-actions" link type="primary" @click="showRelations"
              >{{ tr('showRelations') }} {{ detail ? relationCount : '' }}</el-button
            >
          </div>
          <el-alert
            v-if="!currentTable.available"
            :title="tr('tableUnavailable')"
            type="warning"
            :closable="false"
          />
          <div v-if="detail" class="metadata-secondary metadata-section">
            {{ tr('progress', { confirmed: confirmedCount, total: fields.length }) }}
          </div>
        </div>
        <div v-loading="detailLoading" class="metadata-virtual-list">
          <el-empty v-if="detailFailed" :description="tr('loadFailed')" :image-size="64">
            <el-button @click="loadDetail">{{ tr('retry') }}</el-button>
          </el-empty>
          <el-auto-resizer v-else-if="detail">
            <template #default="{ height, width }">
              <el-table-v2
                v-if="height > 0 && width > 0"
                :key="selectedTable"
                :columns="fieldColumns"
                :data="fields"
                :width="width"
                :height="height"
                :estimated-row-height="56"
                row-key="fieldName"
              >
                <template #cell="{ column, rowData: row }">
                  <div v-if="column.key === 'fieldName'" class="metadata-description">
                    <div>{{ row.fieldName }}</div>
                    <div class="metadata-secondary">{{ row.physicalType }}</div>
                  </div>
                  <div v-else-if="column.key === 'businessName'" class="metadata-description">
                    <div v-if="row.businessName?.trim()">{{ row.businessName }}</div>
                    <div v-else class="metadata-physical-comment">{{ tr('incomplete') }}</div>
                  </div>
                  <div v-else-if="column.key === 'description'" class="metadata-description">
                    <div v-if="row.description?.trim()">{{ row.description }}</div>
                    <div v-else class="metadata-physical-comment">{{ row.physicalComment }}</div>
                    <div v-if="row.aliases" class="metadata-secondary">
                      {{ tr('aliases') }} · {{ row.aliases }}
                    </div>
                  </div>
                  <el-tag
                    v-else-if="column.key === 'status'"
                    :type="
                      row.confirmed
                        ? 'success'
                        : row.businessName || row.description || row.aliases
                        ? 'warning'
                        : 'info'
                    "
                  >
                    {{
                      tr(
                        row.confirmed
                          ? 'confirmed'
                          : row.businessName || row.description || row.aliases
                          ? 'pending'
                          : 'incomplete'
                      )
                    }}
                  </el-tag>
                  <template v-else>
                    <el-button
                      text
                      type="primary"
                      :disabled="saving || !currentTable.available"
                      @click="openField(row)"
                    >
                      {{ tr('edit') }}
                    </el-button>
                    <el-button
                      text
                      type="primary"
                      :disabled="
                        saving ||
                        (!row.confirmed && (!currentTable.available || !row.businessName?.trim()))
                      "
                      @click="toggleFieldConfirmation(row)"
                    >
                      {{ tr(row.confirmed ? 'unconfirm' : 'confirm') }}
                    </el-button>
                  </template>
                </template>
                <template #empty>
                  <el-empty :description="tr('noFields')" :image-size="64" />
                </template>
              </el-table-v2>
            </template>
          </el-auto-resizer>
        </div>
      </div>
      <el-empty v-else class="metadata-detail" :description="tr('noSemanticTables')" />
    </div>

    <el-drawer
      :model-value="!!drawer"
      :title="tr(drawer === 'membership' ? 'addTables' : drawer || 'table')"
      :size="drawer === 'membership' ? 'min(760px, 100%)' : 'min(720px, 100%)'"
      :close-on-click-modal="!saving"
      :close-on-press-escape="!saving"
      :show-close="!saving"
      @update:model-value="
        visible => {
          if (!visible) drawer = ''
        }
      "
    >
      <div v-if="drawer === 'membership'" class="metadata-membership">
        <el-alert :title="tr('addTablesHint')" type="info" show-icon :closable="false" />
        <el-input
          v-model="membershipSearch"
          clearable
          :disabled="saving"
          :placeholder="tr('searchPhysicalTables')"
          :aria-label="tr('searchPhysicalTables')"
        >
          <template #prefix>
            <Icon name="icon_search-outline_outlined">
              <icon_searchOutline_outlined class="svg-icon" />
            </Icon>
          </template>
        </el-input>
        <div class="metadata-membership-actions">
          <el-checkbox v-model="onlySelected" :disabled="saving">{{
            tr('onlySelectedTables')
          }}</el-checkbox>
          <div class="metadata-actions">
            <el-button
              link
              type="primary"
              :disabled="saving || !canSelectResults"
              @click="setResultsSelected(true)"
            >
              {{ tr('selectTableResults') }}
            </el-button>
            <el-button
              link
              type="primary"
              :disabled="saving || !canClearResults"
              @click="setResultsSelected(false)"
            >
              {{ tr('clearTableResults') }}
            </el-button>
          </div>
        </div>
        <div class="metadata-membership-grid metadata-membership-header">
          <span>{{ tr('tableNameAndComment') }}</span>
          <span>{{ tr('status') }}</span>
        </div>
        <div v-if="filteredMembershipOptions.length" class="metadata-membership-list">
          <el-auto-resizer>
            <template #default="{ height, width }">
              <FixedSizeList
                v-if="height && width"
                ref="membershipList"
                :data="filteredMembershipOptions"
                :total="filteredMembershipOptions.length"
                :item-size="membershipRowHeight"
                :height="height"
                :width="width"
              >
                <template #default="{ data, index, style }">
                  <div class="metadata-membership-grid metadata-membership-row" :style="style">
                    <el-checkbox
                      :model-value="
                        activeTableNames.has(data[index].tableName) ||
                        pendingTableNames.has(data[index].tableName)
                      "
                      :disabled="saving || activeTableNames.has(data[index].tableName)"
                      :aria-label="data[index].tableName"
                      @change="value => setTableSelected(data[index].tableName, value === true)"
                    />
                    <div class="metadata-membership-name">
                      <div :title="data[index].tableName">{{ data[index].tableName }}</div>
                      <div
                        v-if="tableComment(data[index])"
                        class="metadata-secondary"
                        :title="tableComment(data[index])"
                      >
                        {{ tableComment(data[index]) }}
                      </div>
                    </div>
                    <div>
                      <el-tag v-if="activeTableNames.has(data[index].tableName)" size="small">
                        {{ tr('tableAlreadyAdded') }}
                      </el-tag>
                      <span v-else class="metadata-secondary">{{ tr('physicalTableOnly') }}</span>
                    </div>
                  </div>
                </template>
              </FixedSizeList>
            </template>
          </el-auto-resizer>
        </div>
        <el-empty
          v-else
          :image-size="72"
          :description="
            tr(membershipOptions.length ? 'noMatchingPhysicalTables' : 'noPhysicalTables')
          "
        />
      </div>

      <el-form
        v-else-if="drawer === 'table' && tableForm"
        label-position="top"
        :disabled="saving"
        @submit.prevent
      >
        <p class="metadata-label">{{ tableForm.tableName }}</p>
        <el-form-item :label="tr('description')">
          <el-input v-model="tableForm.description" type="textarea" :rows="5" maxlength="4000" />
        </el-form-item>
        <el-form-item :label="tr('grain')">
          <el-input v-model="tableForm.grain" maxlength="512" :placeholder="tr('grainExample')" />
        </el-form-item>
      </el-form>

      <el-form
        v-else-if="drawer === 'field' && fieldForm && editingField"
        label-position="top"
        :disabled="saving"
        @submit.prevent
      >
        <p class="metadata-label">{{ fieldLabel(editingField) }}</p>
        <el-form-item :label="tr('businessName')" required>
          <el-input v-model="fieldForm.businessName" maxlength="255" />
        </el-form-item>
        <el-form-item :label="tr('description')">
          <el-input
            v-model="fieldForm.description"
            type="textarea"
            :rows="5"
            maxlength="4000"
            :placeholder="editingField.physicalComment ?? undefined"
          />
        </el-form-item>
        <el-form-item :label="tr('aliases')">
          <el-input
            v-model="fieldForm.aliases"
            maxlength="1000"
            :placeholder="tr('aliasesExample')"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span v-if="drawer === 'membership'" class="metadata-membership-count metadata-secondary">
          {{
            tr('tableSelectionCount', {
              total: activeTableNames.size + pendingTableNames.size,
              added: pendingTableNames.size
            })
          }}
        </span>
        <el-button secondary :disabled="saving" @click="drawer = ''">{{
          t('common.cancel')
        }}</el-button>
        <el-button
          v-if="drawer === 'membership'"
          type="primary"
          :disabled="!pendingTableNames.size"
          :loading="saving"
          @click="addTables"
        >
          {{ t('common.sure') }}
        </el-button>
        <el-button
          v-else-if="drawer === 'table' && tableForm"
          type="primary"
          :loading="saving"
          @click="saveTable(tableForm)"
        >
          {{ tr('saveConfirmed') }}
        </el-button>
        <el-button
          v-else-if="drawer === 'field' && fieldForm"
          type="primary"
          :disabled="!fieldForm.businessName.trim()"
          :loading="saving"
          @click="saveField(fieldForm)"
        >
          {{ tr('saveConfirmed') }}
        </el-button>
      </template>
    </el-drawer>
  </section>
</template>

<style scoped lang="less">
@import './datasource-metadata.less';

.metadata-heading {
  display: flex;
  align-items: center;
  gap: 12px;
}

.metadata-semantic-panel {
  overflow: hidden;
}

.metadata-semantics {
  display: flex;
  height: 100%;
}

.metadata-field-pane {
  display: flex;
  flex-direction: column;
}

.metadata-overview {
  flex-shrink: 0;
  max-height: 65%;
  overflow: auto;
}

.metadata-virtual-list {
  flex: 1;
  min-height: 0;

  :deep(.ed-table-v2__row-cell > .metadata-description) {
    min-width: 0;
    padding-block: 8px;
  }
}

.metadata-sidebar {
  display: flex;
  flex: 0 0 248px;
  flex-direction: column;
  gap: 8px;
  padding-right: 16px;
  border-right: 1px solid var(--ed-border-color);

  .metadata-heading {
    justify-content: space-between;
  }
}

.metadata-table-row {
  box-sizing: border-box;
  padding-bottom: 4px;
}

.metadata-table-button > * {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.metadata-remove-table {
  position: absolute;
  top: 6px;
  right: 6px;
  display: grid;
  width: 18px;
  height: 18px;
  padding: 0;
  color: var(--ed-color-danger);
  cursor: pointer;
  background: transparent;
  border: 0;
  border-radius: 4px;
  place-items: center;

  &:hover:not(:disabled),
  &:focus-visible {
    background: var(--ed-color-danger-light-9);
  }

  &:disabled {
    cursor: not-allowed;
  }

  .svg-icon {
    width: 12px;
    height: 12px;
  }
}

@media (hover: hover) {
  .metadata-remove-table {
    opacity: 0;
    pointer-events: none;
  }

  .metadata-table-row:hover .metadata-remove-table,
  .metadata-table-row:focus-within .metadata-remove-table {
    opacity: 1;
    pointer-events: auto;
  }
}

.metadata-detail-header,
.metadata-table-actions {
  flex-wrap: wrap;
}

.metadata-table-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  margin-inline-start: auto;

  > .ed-button + .ed-button {
    margin-left: 0;
  }
}

.metadata-membership {
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: 100%;

  > :not(.metadata-membership-list) {
    flex-shrink: 0;
  }
}

.metadata-membership-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px 12px;
}

.metadata-membership-grid {
  box-sizing: border-box;
  display: grid;
  grid-template-columns: 32px minmax(0, 1fr) minmax(104px, 28%);
  align-items: center;
  gap: 8px;
  padding: 0 12px;
}

.metadata-membership-header {
  min-height: 42px;
  margin-bottom: -12px;
  background: var(--ed-fill-color-light);
  border-bottom: 1px solid var(--ed-border-color);

  > :first-child {
    grid-column: 2;
  }
}

.metadata-membership-list {
  flex: 1;
  min-height: 156px;
}

.metadata-membership-row {
  border-bottom: 1px solid var(--ed-border-color);
}

.metadata-membership-name > div {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.metadata-membership-count {
  float: left;
  line-height: 32px;
}

.metadata-table-button {
  box-sizing: border-box;
  display: block;
  width: 100%;
  padding: 8px 30px 8px 12px;
  overflow-wrap: anywhere;
  color: inherit;
  text-align: left;
  cursor: pointer;
  background: transparent;
  border: 0;
  border-radius: 4px;

  &:hover,
  &.selected {
    color: var(--ed-color-primary);
    background: var(--ed-color-primary-light-9);
  }
}

.metadata-detail {
  flex: 1;
  min-width: 0;
  padding-left: 24px;
}

.metadata-physical-comment {
  color: var(--ed-text-color-secondary);
}

.metadata-section {
  margin-top: 12px;
  margin-bottom: 8px;
}

.metadata-field-heading {
  margin-top: 20px;
  padding: 12px 0;
  border-top: 1px solid var(--ed-border-color);
  border-bottom: 1px solid var(--ed-border-color);
}

.metadata-label {
  font-size: 14px;
}
</style>
