<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import FixedSizeList from 'element-plus-secondary/es/components/virtual-list/src/components/fixed-size-list.mjs'
import icon_close_outlined from '@/assets/svg/icon_close_outlined.svg'
import icon_searchOutline_outlined from '@/assets/svg/icon_search-outline_outlined.svg'
import { useI18n } from '@/hooks/web/useI18n'
import { listDatasourceTables, type PhysicalTable } from '@/api/datasource'
import {
  addDatasourceSemanticTables,
  getDatasourceSemantics,
  refreshDatasourceSemanticTable,
  removeDatasourceSemanticTable,
  saveDatasourceFieldSemantic,
  saveDatasourceTableSemantic,
  type DatasourceSemantics,
  type FieldSemantic,
  type TableSemantic
} from '@/api/datasourceSemantic'

const props = defineProps<{ datasourceId: string; canManage: boolean }>()
const emit = defineEmits<{ 'show-relations': [tableName: string] }>()
const { t } = useI18n()
const { t: tr } = useI18n('datasourceMetadata')

const semantics = ref<DatasourceSemantics>({ relationCounts: {}, tables: [], fields: [] })
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
const editingField = ref<DatasourceSemantics['fields'][number]>()

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

const fields = computed(() =>
  semantics.value.fields.filter(field => field.tableName === selectedTable.value)
)
const confirmedCount = computed(() => fields.value.filter(field => field.confirmed).length)
const relationCount = computed(() => {
  const counts = semantics.value.relationCounts
  return Object.prototype.hasOwnProperty.call(counts, selectedTable.value)
    ? counts[selectedTable.value]
    : 0
})
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

const refreshTable = (tableName: string) =>
  runSave(async () => {
    const { data: available } = await refreshDatasourceSemanticTable(props.datasourceId, tableName)
    if (!available) ElMessage.warning(tr('tableUnavailableNamed', { table: tableName }))
  })

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

const openField = (semantic: DatasourceSemantics['fields'][number]) => {
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

const showRelations = () => emit('show-relations', selectedTable.value)

const tableLabel = (table: TableSemantic) =>
  table.physicalLabel !== table.tableName ? table.physicalLabel : ''
const fieldLabel = (field: DatasourceSemantics['fields'][number]) =>
  [`${field.tableName}.${field.fieldName}`, field.physicalType].filter(Boolean).join('·')
</script>

<template>
  <section v-loading="loading" class="metadata-panel">
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
        <div v-for="table in activeTables" :key="table.tableName" class="metadata-table-row">
          <button
            type="button"
            class="metadata-table-button"
            :class="{ selected: table.tableName === selectedTable }"
            :aria-pressed="table.tableName === selectedTable"
            @click="selectedTable = table.tableName"
          >
            <strong class="metadata-label">{{ table.tableName }}</strong>
            <div class="metadata-secondary">{{ tableLabel(table) }}</div>
          </button>
          <button
            v-if="canManage"
            class="metadata-remove-table"
            type="button"
            :aria-label="tr('removeNamedTable', { table: table.tableName })"
            :title="tr('remove')"
            :disabled="saving"
            @click="removeTable(table.tableName)"
          >
            <Icon name="icon_close_outlined"
              ><icon_close_outlined class="svg-icon" aria-hidden="true"
            /></Icon>
          </button>
        </div>
      </aside>
      <div v-if="currentTable" class="metadata-detail">
        <div class="metadata-heading metadata-detail-header">
          <strong>{{ tableLabel(currentTable) || currentTable.tableName }}</strong>
          <span v-if="tableLabel(currentTable)" class="metadata-secondary">{{
            currentTable.tableName
          }}</span>
          <div v-if="canManage" class="metadata-actions metadata-table-actions">
            <el-button :disabled="saving" @click="openTable">{{ tr('edit') }}</el-button>
            <el-button :disabled="saving" @click="refreshTable(currentTable.tableName)">
              {{ tr('refreshTable') }}
            </el-button>
            <el-button>{{ tr('chat') }}</el-button>
            <el-button type="primary">{{ tr('complete') }}</el-button>
          </div>
        </div>
        <div class="metadata-secondary metadata-section">{{ tr('description') }}</div>
        <div class="metadata-label metadata-description">
          {{ currentTable.description || tr('noDescription') }}
        </div>
        <div class="metadata-secondary metadata-section">
          {{ tr('grain') }} · {{ currentTable.grain || tr('pending') }}
        </div>
        <div class="metadata-heading metadata-field-heading">
          <strong class="metadata-label">{{ tr('fieldSemantics') }} {{ fields.length }}</strong>
          <el-button class="metadata-actions" link type="primary" @click="showRelations"
            >{{ tr('showRelations') }} {{ relationCount }}</el-button
          >
        </div>
        <el-alert
          v-if="!currentTable.available"
          :title="tr('tableUnavailable')"
          type="warning"
          :closable="false"
        />
        <div class="metadata-secondary metadata-section">
          {{ tr('progress', { confirmed: confirmedCount, total: fields.length }) }}
        </div>
        <el-table :data="fields" :empty-text="tr('noFields')">
          <el-table-column :label="tr('fieldAndType')" min-width="135">
            <template #default="{ row }">
              <div>{{ row.fieldName }}</div>
              <div class="metadata-secondary">{{ row.physicalType }}</div>
            </template>
          </el-table-column>
          <el-table-column :label="tr('businessName')" min-width="120">
            <template #default="{ row }">{{ row.businessName || tr('incomplete') }}</template>
          </el-table-column>
          <el-table-column :label="tr('description')" min-width="190">
            <template #default="{ row }">
              <div v-if="row.description?.trim()" class="metadata-description">
                {{ row.description }}
              </div>
              <div
                v-else-if="row.physicalComment"
                class="metadata-description metadata-physical-comment"
              >
                {{ row.physicalComment }}
              </div>
              <div v-if="row.aliases" class="metadata-secondary">
                {{ tr('aliases') }} · {{ row.aliases }}
              </div>
            </template>
          </el-table-column>
          <el-table-column :label="tr('status')" min-width="100">
            <template #default="{ row }">
              <el-tag :type="row.confirmed ? 'success' : 'info'">{{
                tr(row.confirmed ? 'confirmed' : 'incomplete')
              }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="canManage" :label="tr('actions')" width="80">
            <template #default="{ row }">
              <el-button
                link
                type="primary"
                :disabled="saving || !currentTable.available"
                @click="openField(row)"
                >{{ tr('edit') }}</el-button
              >
            </template>
          </el-table-column>
        </el-table>
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
          {{ t('common.save') }}
        </el-button>
        <el-button
          v-else-if="drawer === 'field' && fieldForm"
          type="primary"
          :disabled="!fieldForm.businessName.trim()"
          :loading="saving"
          @click="saveField(fieldForm)"
        >
          {{ t('common.save') }}
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

.metadata-semantics {
  display: flex;
  min-height: 100%;
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
  position: relative;
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
  justify-content: flex-end;
  gap: 8px;

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
