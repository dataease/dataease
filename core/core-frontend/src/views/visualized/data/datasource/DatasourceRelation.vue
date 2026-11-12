<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessageBox } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import {
  getTableField,
  listDatasourceTables,
  type PhysicalField,
  type PhysicalTable
} from '@/api/datasource'
import {
  listDatasourceRelations,
  removeDatasourceRelation,
  saveDatasourceRelation,
  type Relation
} from '@/api/datasourceRelation'

const props = defineProps<{ datasourceId: string; canManage: boolean }>()
const tableFilter = defineModel<string>('tableFilter', { required: true })
const { t } = useI18n()
const { t: tr } = useI18n('datasourceMetadata')

const relations = ref<Relation[]>([])
const tables = ref<PhysicalTable[]>([])
const fieldsByTable = reactive(new Map<string, PhysicalField[]>())
const loadingFields = reactive(new Set<string>())
const loading = ref(false)
const loadFailed = ref(false)
const saving = ref(false)
const drawer = ref(false)
const query = ref('')
const statusFilter = ref('')
const loadingTables = ref(false)

const cardinalities = ['ONE_TO_ONE', 'ONE_TO_MANY', 'MANY_TO_ONE', 'MANY_TO_MANY'] as const
const joinTypes = ['LEFT', 'INNER', 'RIGHT', 'FULL'] as const
const newRelation = (): Relation => ({
  name: '',
  leftTable: '',
  rightTable: '',
  conditions: [{ leftField: '', rightField: '' }],
  cardinality: 'MANY_TO_ONE',
  joinType: 'LEFT',
  description: '',
  confirmed: true,
  valid: true,
  missingReferences: []
})
const relation = ref(newRelation())
const linked = (row: Relation, table: string) => row.leftTable === table || row.rightTable === table
const filteredRelations = computed(() => {
  const keyword = query.value.trim().toLowerCase()
  return relations.value.filter(
    row =>
      (!tableFilter.value || linked(row, tableFilter.value)) &&
      (!statusFilter.value || row.confirmed === (statusFilter.value === 'confirmed')) &&
      [row.name, row.leftTable, row.rightTable].some(value => value.toLowerCase().includes(keyword))
  )
})
const tableOptions = computed(() =>
  Array.from(
    new Set([
      ...tables.value.map(table => table.tableName),
      ...[tableFilter.value].filter(Boolean),
      ...relations.value.flatMap(row => [row.leftTable, row.rightTable])
    ])
  )
)
const relationValid = computed(() => {
  const { name, leftTable, rightTable, conditions } = relation.value
  const leftFields = fieldsByTable.get(leftTable)
  const rightFields = fieldsByTable.get(rightTable)
  return (
    !!name.trim() &&
    !!leftFields &&
    !!rightFields &&
    conditions.length > 0 &&
    conditions.every(
      condition =>
        leftFields.some(field => field.originName === condition.leftField) &&
        rightFields.some(field => field.originName === condition.rightField)
    )
  )
})

const refreshRelations = async () => {
  const { data } = await listDatasourceRelations(props.datasourceId)
  relations.value = data
}

const load = async () => {
  loading.value = true
  loadFailed.value = false
  try {
    await refreshRelations()
  } catch {
    loadFailed.value = true
  } finally {
    loading.value = false
  }
}

const loadFields = async (tableName: string) => {
  if (
    !tableName ||
    fieldsByTable.has(tableName) ||
    loadingFields.has(tableName) ||
    !tables.value.some(table => table.tableName === tableName)
  )
    return
  loadingFields.add(tableName)
  try {
    const { data } = await getTableField({ datasourceId: props.datasourceId, tableName })
    fieldsByTable.set(tableName, data)
  } catch {
    // The shared request interceptor displays the error.
  } finally {
    loadingFields.delete(tableName)
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
    drawer.value = false
    await load()
  } catch {
    // The shared request interceptor displays the error.
  } finally {
    saving.value = false
  }
}

const openRelation = async (row?: Relation) => {
  if (loadingTables.value || saving.value || !(await loadTables())) return
  fieldsByTable.clear()
  relation.value = row
    ? { ...row, conditions: row.conditions.map(condition => ({ ...condition })) }
    : newRelation()
  drawer.value = true
  void loadFields(relation.value.leftTable)
  void loadFields(relation.value.rightTable)
}

const changeRelationTable = (side: 'left' | 'right') => {
  const fieldKey = side === 'left' ? 'leftField' : 'rightField'
  relation.value.conditions.forEach(condition => (condition[fieldKey] = ''))
  void loadFields(side === 'left' ? relation.value.leftTable : relation.value.rightTable)
}

const saveRelation = (row: Relation) =>
  runSave(() => saveDatasourceRelation(props.datasourceId, row))

const removeRelation = async (row: Relation) => {
  if (!row.id) return
  const id = row.id
  try {
    await ElMessageBox.confirm(tr('removeRelationPrompt', { name: row.name }), tr('remove'), {
      type: 'warning',
      confirmButtonText: tr('remove'),
      cancelButtonText: t('common.cancel')
    })
  } catch {
    return
  }
  await runSave(() => removeDatasourceRelation(props.datasourceId, id))
}

const fieldType = (field: PhysicalField) => field.fieldType || field.type
const fieldLabel = (field: PhysicalField) =>
  [field.originName, fieldType(field)].filter(Boolean).join(' · ')
</script>

<template>
  <section v-loading="loading" class="metadata-panel">
    <el-empty v-if="loadFailed" :description="tr('loadFailed')">
      <el-button @click="load">{{ tr('retry') }}</el-button>
    </el-empty>
    <template v-else>
      <div class="metadata-toolbar">
        <el-input v-model="query" clearable :placeholder="tr('search')" />
        <el-select v-model="tableFilter" clearable filterable :placeholder="tr('allTables')">
          <el-option v-for="table in tableOptions" :key="table" :label="table" :value="table" />
        </el-select>
        <el-select v-model="statusFilter" clearable :placeholder="tr('allStatuses')">
          <el-option :label="tr('confirmed')" value="confirmed" />
          <el-option :label="tr('pending')" value="pending" />
        </el-select>
        <div v-if="canManage" class="metadata-actions">
          <el-button>{{ tr('discover') }}</el-button>
          <el-button
            type="primary"
            :disabled="saving"
            :loading="loadingTables"
            @click="openRelation()"
            >{{ tr('addRelation') }}</el-button
          >
        </div>
      </div>
      <el-table :data="filteredRelations" :empty-text="tr('noRelations')">
        <el-table-column :label="tr('relationName')" min-width="150">
          <template #default="{ row }">
            <strong>{{ row.name }}</strong>
            <div class="metadata-secondary">{{ row.description }}</div>
          </template>
        </el-table-column>
        <el-table-column :label="tr('tables')" min-width="135">
          <template #default="{ row }">
            <div>{{ row.leftTable }}</div>
            <div class="metadata-secondary">{{ row.rightTable }}</div>
          </template>
        </el-table-column>
        <el-table-column :label="tr('conditions')" min-width="160">
          <template #default="{ row }">
            {{ row.conditions.map(item => item.leftField + ' = ' + item.rightField).join(' AND ') }}
          </template>
        </el-table-column>
        <el-table-column :label="tr('cardinalityAndJoin')" min-width="140">
          <template #default="{ row }">
            <div>{{ tr(row.cardinality) }}</div>
            <div class="metadata-secondary">{{ tr(row.joinType) }}</div>
          </template>
        </el-table-column>
        <el-table-column :label="tr('status')" min-width="100">
          <template #default="{ row }">
            <el-tag :type="row.confirmed ? 'success' : 'warning'">
              {{ tr(row.confirmed ? 'confirmed' : 'pending') }}
            </el-tag>
            <div v-if="!row.valid">
              <el-tag type="danger">{{ tr('invalidRelation') }}</el-tag>
              <div
                v-for="missing in row.missingReferences"
                :key="JSON.stringify(missing)"
                class="metadata-secondary"
              >
                {{
                  tr(missing.fieldName ? 'missingField' : 'missingTable', {
                    table: missing.tableName,
                    field: missing.fieldName || ''
                  })
                }}
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column v-if="canManage" :label="tr('actions')" min-width="180">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              :disabled="saving || loadingTables"
              @click="openRelation(row)"
              >{{ tr('edit') }}</el-button
            >
            <el-button
              v-if="row.valid && !row.confirmed"
              link
              type="primary"
              :disabled="saving"
              @click="saveRelation(row)"
            >
              {{ tr('confirm') }}
            </el-button>
            <el-button link type="primary" :disabled="saving" @click="removeRelation(row)">{{
              tr('remove')
            }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <el-drawer
      :model-value="drawer"
      :title="tr(relation.id ? 'relation' : 'addRelation')"
      size="min(720px, 100%)"
      :close-on-click-modal="!saving"
      :close-on-press-escape="!saving"
      :show-close="!saving"
      @update:model-value="
        visible => {
          if (!visible) drawer = false
        }
      "
    >
      <el-form label-position="top" :disabled="saving" @submit.prevent>
        <el-form-item :label="tr('relationName')" required>
          <el-input v-model="relation.name" maxlength="255" />
        </el-form-item>
        <div class="metadata-columns">
          <el-form-item :label="tr('leftTable')" required>
            <el-select
              v-model="relation.leftTable"
              filterable
              @change="changeRelationTable('left')"
            >
              <el-option v-for="table in tables" :key="table.tableName" :value="table.tableName" />
            </el-select>
          </el-form-item>
          <el-form-item :label="tr('rightTable')" required>
            <el-select
              v-model="relation.rightTable"
              filterable
              @change="changeRelationTable('right')"
            >
              <el-option v-for="table in tables" :key="table.tableName" :value="table.tableName" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item :label="tr('conditions')" required>
          <div
            v-for="(condition, index) in relation.conditions"
            :key="index"
            class="metadata-condition"
          >
            <el-select
              v-model="condition.leftField"
              filterable
              :placeholder="tr('leftField')"
              :loading="loadingFields.has(relation.leftTable)"
              @visible-change="visible => visible && loadFields(relation.leftTable)"
            >
              <el-option
                v-for="field in fieldsByTable.get(relation.leftTable)"
                :key="field.originName"
                :value="field.originName"
                :label="fieldLabel(field)"
              />
            </el-select>
            <span>=</span>
            <el-select
              v-model="condition.rightField"
              filterable
              :placeholder="tr('rightField')"
              :loading="loadingFields.has(relation.rightTable)"
              @visible-change="visible => visible && loadFields(relation.rightTable)"
            >
              <el-option
                v-for="field in fieldsByTable.get(relation.rightTable)"
                :key="field.originName"
                :value="field.originName"
                :label="fieldLabel(field)"
              />
            </el-select>
            <el-button
              link
              :disabled="relation.conditions.length === 1"
              @click="relation.conditions.splice(index, 1)"
              >{{ tr('remove') }}</el-button
            >
          </div>
          <el-button
            link
            type="primary"
            :disabled="relation.conditions.length >= 100"
            @click="relation.conditions.push({ leftField: '', rightField: '' })"
            >{{ tr('addCondition') }}</el-button
          >
        </el-form-item>
        <div class="metadata-columns">
          <el-form-item :label="tr('cardinality')" required>
            <el-select v-model="relation.cardinality">
              <el-option
                v-for="value in cardinalities"
                :key="value"
                :value="value"
                :label="tr(value)"
              />
            </el-select>
          </el-form-item>
          <el-form-item :label="tr('joinType')" required>
            <el-select v-model="relation.joinType">
              <el-option
                v-for="value in joinTypes"
                :key="value"
                :value="value"
                :label="tr(value)"
              />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item :label="tr('description')">
          <el-input v-model="relation.description" type="textarea" :rows="3" maxlength="4000" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button secondary :disabled="saving" @click="drawer = false">{{
          t('common.cancel')
        }}</el-button>
        <el-button
          type="primary"
          :disabled="!relationValid"
          :loading="saving"
          @click="saveRelation(relation)"
        >
          {{ tr('saveConfirmed') }}
        </el-button>
      </template>
    </el-drawer>
  </section>
</template>

<style scoped lang="less">
@import './datasource-metadata.less';

.metadata-toolbar,
.metadata-condition {
  display: flex;
  align-items: center;
  gap: 12px;
}

.metadata-toolbar {
  flex-wrap: wrap;
  margin-bottom: 16px;

  > .ed-input,
  > .ed-select {
    width: 160px;
  }
}

.metadata-columns {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.metadata-condition {
  width: 100%;
  margin-bottom: 8px;
}
</style>
