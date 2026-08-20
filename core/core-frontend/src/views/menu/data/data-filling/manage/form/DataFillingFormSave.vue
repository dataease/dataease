<script setup lang="ts">
import dvFolder from '@/assets/svg/dv-folder.svg'
import icon_info_outlined from '@/assets/svg/icon_info_outlined.svg'
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'

import { useI18n } from '@/hooks/web/useI18n'
import { computed, onMounted, ref, watch } from 'vue'
import {
  DfFormItem,
  DfFormSetting,
  listBuiltInDatasourceTables,
  getBuiltInTableField,
  listDataFillingForms,
  save,
  SimpleDatasource,
  Tree,
  update
} from '../../data-filling'
import { getTableField, listDatasourceTables } from '@/api/datasource'
import {
  forEach,
  cloneDeep,
  find,
  filter,
  keys,
  groupBy,
  split,
  get,
  includes,
  map
} from 'lodash-es'
import { Icon } from '@/components/icon-custom'
import { ElButton, ElIcon, ElMessage, ElOption, ElSelect } from 'element-plus-secondary'

const { t } = useI18n()

const props = withDefaults(
  defineProps<{
    isEdit?: boolean
    disableCreateIndex?: boolean
    form: DfFormSetting
    datasourceList: Array<SimpleDatasource>
    showDrawer: boolean
    dsLoading?: boolean
    willBack?: boolean
  }>(),
  {
    isEdit: false,
    disableCreateIndex: false,
    dsLoading: false,
    willBack: false
  }
)

const loading = ref<boolean>(false)

const mRightForm = ref()

const folder = ref<Array<Tree>>([])

const emit = defineEmits([
  'update:form',
  'update:datasourceList',
  'close',
  'saved',
  'refreshDatasource'
])

const formData = computed<DfFormSetting>({
  get() {
    return props.form
  },
  set(value) {
    emit('update:form', value)
  }
})

const allDatasourceList = computed<Array<SimpleDatasource>>(() => {
  return filter(props.datasourceList, ds => ds.enableDataFill)
})

const _datasourceList = computed(() => {
  const dsMap = groupBy(allDatasourceList.value, d => d.type)
  /*const _types = [{
    name: t('data_fill.form.default'),
    type: 'default',
    options: [{
      id: '-1',
      name: t('data_fill.form.default_built_in')
    }]
  }]*/
  const _types: Array<{
    name: string
    type: string
    options: Array<{ id: string; name: string }>
  }> = []
  if (dsMap) {
    forEach(keys(dsMap), type => {
      _types.push({
        name: dsMap[type][0]?.typeAlias,
        type: type,
        options: dsMap[type]
      })
    })
  }
  return _types
})

const computedFormList = computed<Array<DfFormItem>>(() => {
  if (props.isEdit) {
    const _list: Array<DfFormItem> = []
    const columnIds: Array<string> = []
    for (let i = 0; i < formData.value.forms.length; i++) {
      const row = cloneDeep(formData.value.forms[i])
      if (row.id) {
        columnIds.push(row.id)
        _list.push(row)
      }
    }
    for (let i = 0; i < formData.value.oldForms.length; i++) {
      const row = cloneDeep(formData.value.oldForms[i])
      if (includes(columnIds, row.id)) {
        continue
      }
      row.deleted = true
      _list.push(row)
    }
    return _list
  } else {
    return formData.value.forms
  }
})

const computedTableIndexList = computed(() => {
  if (props.isEdit) {
    const _list: Array<DfFormItem> = []
    const columnIds: Array<string> = []
    for (let i = 0; i < formData.value.tableIndexes.length; i++) {
      const row = formData.value.tableIndexes[i]
      columnIds.push(row.id)
      _list.push(row)
    }
    for (let i = 0; i < formData.value.oldTableIndexes.length; i++) {
      const row = formData.value.oldTableIndexes[i]
      if (includes(columnIds, row.id)) {
        continue
      }
      columnIds.push(row.id)
      _list.push(row)
    }
    return _list
  } else {
    return formData.value.tableIndexes
  }
})

interface ColumnItem {
  name: string
  value: string
  deleted: boolean
}

const allColumnsList = computed<Array<ColumnItem>>(() => {
  const _list: Array<ColumnItem> = []
  for (let i = 0; i < computedFormList.value.length; i++) {
    const row = computedFormList.value[i]
    if (row.type === 'dateRange') {
      if (
        row.settings?.mapping?.columnName1 !== undefined &&
        row.settings.mapping.columnName1 !== ''
      ) {
        _list.push({
          name: !row.deleted ? row.settings.mapping.columnName1 : row.id + '_1',
          value: row.id + '_1',
          deleted: !!row.deleted
        } as ColumnItem)
      }
      if (
        row.settings?.mapping?.columnName2 !== undefined &&
        row.settings.mapping.columnName2 !== ''
      ) {
        _list.push({
          name: !row.deleted ? row.settings.mapping.columnName2 : row.id + '_2',
          value: row.id + '_2',
          deleted: !!row.deleted
        } as ColumnItem)
      }
    } else {
      if (
        row.settings?.mapping?.columnName !== undefined &&
        row.settings.mapping.columnName !== '' &&
        row.settings.mapping.type !== 'text'
      ) {
        _list.push({
          name: !row.deleted ? row.settings.mapping.columnName : row.id,
          value: row.id,
          deleted: !!row.deleted
        } as ColumnItem)
      }
    }
  }

  return _list
})
const columnsList = computed(() => {
  return filter(allColumnsList.value, c => !c.deleted)
})

const checkSelectExistsColumnValidator = (rule, value, callback) => {
  if (!value) {
    return callback(new Error(t('common.required')))
  }
  const f = split(rule.field, '.')[0]
  const _index = get(formData.value, f)
  const _list = getExistsColumnsToSelect(_index)
  const _c = find(_list, c => c.originName === value)
  if (_c === undefined || !!_c.disabled) {
    return callback(new Error(t('data_fill.form.please_select_valid_column')))
  }
  callback()
}

const checkDuplicateNameValidator = (rule, value, callback) => {
  if (!value) {
    return callback(new Error(t('common.required')))
  }
  let count = 0
  forEach(computedFormList.value, f => {
    if (!f.deleted) {
      if (f.type === 'dateRange') {
        if (f.settings.mapping.columnName1 === value) {
          count++
        }
        if (f.settings.mapping.columnName2 === value) {
          count++
        }
      } else {
        if (f.settings.mapping.columnName === value) {
          count++
        }
      }
    } else {
      // 后台会将删除的字段名处理成uuid，正常不会有重复的
    }
  })
  if (count > 1) {
    callback(new Error(t('data_fill.form.duplicate_error')))
  }
  callback()
}
const checkDuplicateIndexNameValidator = (rule, value, callback) => {
  if (!value) {
    return callback(new Error(t('common.required')))
  }
  let count = 0
  forEach(computedTableIndexList.value, f => {
    if (f.name === value) {
      count++
    }
  })
  if (count > 1) {
    callback(new Error(t('data_fill.form.duplicate_error')))
  }
  callback()
}
const checkInvalidColumnValidator = (rule, value, callback) => {
  const f = split(rule.field, '.')[0]
  const _index = get(formData.value, f)
  if (_index.old) {
    // 旧的 index 跳过校验
    callback()
  }
  if (!value) {
    return callback(new Error(t('common.required')))
  }
  if (columnsList.value.length === 0) {
    return callback(new Error(t('data_fill.form.value_not_exists')))
  }
  if (find(columnsList.value, c => c.value === value) === undefined) {
    callback(new Error(t('data_fill.form.value_not_exists')))
  }
  callback()
}
const checkDuplicateIndexColumnValidator = (rule, value, callback, source) => {
  if (!value) {
    return callback(new Error(t('common.required')))
  }
  const f = split(rule.field, '.')[0]
  const _list = get(formData.value, f)

  let count = 0
  forEach(_list.columns, f => {
    if (f.column === value) {
      count++
    }
  })
  if (count > 1) {
    callback(new Error(t('data_fill.form.duplicate_error')))
  }
  callback()
}

const dfs = (arr: Tree[]) => {
  arr.forEach(ele => {
    ele.value = ele.id
    if (ele.children?.length) {
      dfs(ele.children)
    }
  })
}

const findNodeInTree = (nodes: Tree[], id: string | number): Tree | null => {
  for (const node of nodes) {
    if (node.id === id) {
      return node
    }
    if (node.children?.length) {
      const found = findNodeInTree(node.children, id)
      if (found) return found
    }
  }
  return null
}

const treeProps = {
  label: 'name',
  children: 'children',
  isLeaf: node => !node.children?.length
}

function trimName(obj) {
  obj.name = obj.name.trim()
}

function getTypeOptions(formOption: DfFormItem) {
  const _options: Array<{
    value: string
    label: string
  }> = []
  if (
    formOption.type !== 'date' &&
    formOption.type !== 'dateRange' &&
    formOption.settings.inputType !== 'number' &&
    formOption.type !== 'textarea' &&
    formOption.type !== 'checkbox' &&
    !(formOption.type === 'select' && formOption.settings.multiple)
  ) {
    _options.push({
      value: 'nvarchar',
      label: t('data_fill.database.nvarchar')
    })
  }
  if (
    formOption.type === 'checkbox' ||
    (formOption.type === 'select' && formOption.settings.multiple) ||
    formOption.type === 'textarea'
  ) {
    _options.push({ value: 'text', label: t('data_fill.database.text') })
  }

  if (formOption.type === 'input' && formOption.settings.inputType === 'number') {
    _options.push({ value: 'number', label: t('data_fill.database.number') })
    _options.push({ value: 'decimal', label: t('data_fill.database.decimal') })
  }
  if (formOption.type === 'date' || formOption.type === 'dateRange') {
    _options.push({
      value: 'datetime',
      label: t('data_fill.database.datetime')
    })
  }
  return _options
}

function getExistsColumnsToSelect(formOption: DfFormItem) {
  let _list: Array<any> = []
  if (
    formOption.type !== 'date' &&
    formOption.type !== 'dateRange' &&
    formOption.settings.inputType !== 'number' &&
    formOption.type !== 'textarea' &&
    formOption.type !== 'checkbox' &&
    !(formOption.type === 'select' && formOption.settings.multiple)
  ) {
    _list = filter(
      existsTableColumns.value,
      e =>
        e.deType === 0 &&
        e.type !== 'TEXT' &&
        e.type !== 'MEDIUMTEXT' &&
        e.type !== 'LONGTEXT' &&
        !e.primary
    )
  }
  if (
    formOption.type === 'checkbox' ||
    (formOption.type === 'select' && formOption.settings.multiple) ||
    formOption.type === 'textarea'
  ) {
    _list = filter(
      existsTableColumns.value,
      e =>
        e.deType === 0 &&
        (e.type === 'TEXT' || e.type === 'MEDIUMTEXT' || e.type === 'LONGTEXT') &&
        !e.primary
    )
  }

  if (formOption.type === 'input' && formOption.settings.inputType === 'number') {
    _list = filter(
      existsTableColumns.value,
      e => (e.deType === 2 || e.deType === 3 || e.deType === 4) && !e.primary
    )
  }
  if (formOption.type === 'date' || formOption.type === 'dateRange') {
    _list = map(
      filter(existsTableColumns.value, e => e.deType === 1 && !e.primary),
      e => {
        return {
          ...e,
          disabled: e.type === 'YEAR' || e.type === 'TIME' || e.type === 'TIMETZ'
        }
      }
    )
  }
  return _list
}

function onSelectExistsColumn(formOption: DfFormItem) {
  if (formOption.type === 'input' && formOption.settings.inputType === 'number') {
    const c = find(
      existsTableColumns.value,
      e => formOption.settings.mapping.columnName === e.originName
    )
    if (c.deType === 3) {
      formOption.settings.mapping.type = 'decimal'
    } else {
      formOption.settings.mapping.type = 'number'
    }
  }
}

const requiredRule = {
  required: true,
  message: t('common.required'),
  trigger: ['blur', 'change']
}
const duplicateRule = {
  validator: checkDuplicateNameValidator,
  trigger: ['blur', 'change']
}
const checkSelectExistsColumnRule = {
  validator: checkSelectExistsColumnValidator,
  trigger: ['blur', 'change']
}
const duplicateIndexRule = {
  validator: checkDuplicateIndexNameValidator,
  trigger: 'blur'
}
const duplicateIndexColumnRule = {
  validator: checkDuplicateIndexColumnValidator,
  trigger: ['blur', 'change']
}
const invalidColumnRule = {
  validator: checkInvalidColumnValidator,
  trigger: ['blur', 'change']
}
const maxLengthRule = (max = 50) => {
  return {
    max: max,
    message: t('data_fill.form.input_limit_max', [max]),
    trigger: ['blur', 'change']
  }
}
const minLengthRule = (min = 0) => {
  return {
    min: min,
    message: t('data_fill.form.input_limit_min', [min]),
    trigger: ['blur', 'change']
  }
}

const nodeClick = (data: Tree) => {
  formData.value.pid = data.id as string
}

const filterMethod = (value, data) => {
  if (!data) return false
  data.name.includes(value)
}

function removeIndexColumn(list, index) {
  list.splice(index, 1)
}

function addColumn(list) {
  list.push({
    column: undefined,
    order: 'none'
  })
}

function addIndex() {
  formData.value.tableIndexes.push({
    name: undefined,
    columns: [
      {
        column: undefined,
        order: 'none'
      }
    ]
  })
}

function removeIndex(index) {
  formData.value.tableIndexes.splice(index, 1)
}

function closeSave() {
  emit('close')
}

function doSave() {
  loading.value = true
  mRightForm.value?.validate(valid => {
    if (valid) {
      const _form = JSON.parse(JSON.stringify(formData.value.forms))
      forEach(_form, f => {
        f.extraDetails = undefined
      })
      const data = {
        id: formData.value.id,
        name: formData.value.name,
        tableName: formData.value.tableName,
        datasource: formData.value.datasource,
        pid: formData.value.pid,
        forms: JSON.stringify(_form),
        createIndex: formData.value.createIndex,
        tableIndexes: JSON.stringify(formData.value.tableIndexes),
        nodeType: 'form',
        useExistsTable: formData.value.useExistsTable
      }
      if (!props.isEdit) {
        delete data.id
        save(data)
          .then(res => {
            if (res) {
              emit('saved', { ...res, willBack: props.willBack })
            }
          })
          .finally(() => {
            loading.value = false
          })
      } else {
        update(data)
          .then(res => {
            if (res) {
              emit('saved', { ...res, willBack: props.willBack })
            }
          })
          .finally(() => {
            loading.value = false
          })
      }
    } else {
      loading.value = false
      return false
    }
  })
}

function refreshDatasource() {
  emit('refreshDatasource')
}

const existsTables = ref([])
const existsTableColumns = ref([])

function onChangeDatasource() {
  formData.value.tableName = undefined
  existsTableColumns.value = []
  listTables()
}

function onChangeUseExistsTable() {
  formData.value.tableName = undefined
  existsTableColumns.value = []
  listTables()
}

function listTableColumns() {
  if (formData.value.datasource && formData.value.tableName) {
    if (formData.value.datasource === '-1' || formData.value.datasource === -1) {
      getBuiltInTableField(formData.value.tableName).then(res => {
        if (find(res.data, d => d.primary)) {
          existsTableColumns.value = res.data
        } else {
          ElMessage.error(t('data_fill.form.table_primary_key_not_exists'))
        }
      })
    } else {
      getTableField({
        datasourceId: formData.value.datasource,
        tableName: formData.value.tableName
      }).then(res => {
        if (find(res.data, d => d.primary)) {
          existsTableColumns.value = res.data
        } else {
          ElMessage.error(t('data_fill.form.table_primary_key_not_exists'))
        }
      })
    }
  }
}

function listTables(callback?: any) {
  if (formData.value?.datasource && formData.value?.useExistsTable && !props.isEdit) {
    if (formData.value.datasource === '-1' || formData.value.datasource === -1) {
      listBuiltInDatasourceTables()
        .then(res => {
          existsTables.value = res.data
          if (callback) {
            callback()
          }
        })
        .catch(e => {
          ElMessage.error(e)
        })
    } else {
      listDatasourceTables({ datasourceId: formData.value.datasource })
        .then(res => {
          existsTables.value = res.data
          if (callback) {
            callback()
          }
        })
        .catch(e => {
          ElMessage.error(e)
        })
    }
  }
}

onMounted(() => {
  loading.value = true

  forEach(formData.value.forms, f => {
    f.settings.mapping.typeOptions = getTypeOptions(f)
    if (!f.settings.mapping.type) {
      f.settings.mapping.type = f.settings.mapping.typeOptions[0].value
    }
  })
  const p2 = listDataFillingForms({ leaf: false, id: undefined, weight: 7 })

  Promise.all([p2])
    .then(val => {
      if (val[0]) {
        dfs(val[0] as unknown as Tree[])
      }
      folder.value = (val[0] as unknown as Tree[]) || []
      if (folder.value.length && folder.value[0].name === 'root' && folder.value[0].id === '0') {
        folder.value[0].name = t('data_fill.data_fill')
      }
      if (formData.value.pid && !findNodeInTree(folder.value, formData.value.pid)) {
        formData.value.pid = ''
      }
    })
    .finally(() => {
      loading.value = false
    })

  if (props.isEdit) {
    listTableColumns()
  } else {
    listTables(listTableColumns)
  }
})

watch(
  () => formData.value.pid,
  newPid => {
    if (!folder.value.length) return
    if (newPid && !findNodeInTree(folder.value, newPid)) {
      formData.value.pid = ''
    }
  }
)
</script>

<template>
  <el-container v-loading="loading" class="DataFillingFormSave">
    <el-header class="de-header">
      <div class="panel-info-area">
        <span class="text16 margin-left12">
          {{ t('data_fill.form.save_form') }}
        </span>
      </div>

      <div style="padding-right: 20px">
        <i class="el-icon-close" style="cursor: pointer" @click="closeSave" />
      </div>
    </el-header>
    <el-main class="de-main">
      <el-form
        ref="mRightForm"
        class="m-form"
        size="default"
        :model="formData"
        label-position="top"
        hide-required-asterisk
        @submit.native.prevent
      >
        <el-form-item
          prop="name"
          class="form-item"
          :rules="[requiredRule, maxLengthRule(64), minLengthRule(1)]"
        >
          <template #label>
            {{ t('data_fill.form.form_name') }}
            <span class="df-input-require">*</span>
          </template>
          <el-input v-model="formData.name" @blur="trimName(formData)" required />
        </el-form-item>

        <el-form-item v-if="!isEdit" prop="pid" class="form-item" :rules="[requiredRule]">
          <template #label>
            {{ t('data_fill.form.folder') }}
            <span class="df-input-require">*</span>
          </template>

          <el-tree-select
            v-model="formData.pid"
            :data="folder"
            popper-class="dataset-tree-select"
            style="width: 100%"
            :render-after-expand="false"
            :props="treeProps"
            @node-click="nodeClick"
            :filter-method="filterMethod"
            filterable
          >
            <template #default="{ data: { name } }">
              <el-icon>
                <Icon name="dv-folder">
                  <dvFolder class="svg-icon" />
                </Icon>
              </el-icon>
              <span :title="name">{{ name }}</span>
            </template>
          </el-tree-select>
        </el-form-item>

        <el-form-item
          v-if="!isEdit"
          prop="datasource"
          class="form-item hide-asterisk"
          :rules="[requiredRule]"
        >
          <template #label>
            <div class="label-row">
              <div>
                {{ t('data_fill.form.datasource') }}
                <span class="asterisk">*</span>
              </div>
              <el-button type="primary" text @click="refreshDatasource" :disabled="dsLoading">
                {{ t('commons.refresh') }}
              </el-button>
            </div>
          </template>
          <el-select
            v-model="formData.datasource"
            filterable
            @change="onChangeDatasource"
            style="width: 100%"
            :loading="dsLoading"
          >
            <el-option-group v-for="(x, $index) in _datasourceList" :key="$index" :label="x.name">
              <el-option
                v-for="d in x.options"
                :key="d.id"
                :value="d.id"
                :label="d.name"
                :disabled="d.status === 'Error'"
              >
                <span>
                  {{ d.name }}
                </span>
                <span
                  style="padding-left: 14px; color: red; font-size: 10px"
                  v-if="d.status === 'Error'"
                >
                  {{ t('data_set.invalid_data_source') }}
                </span>
              </el-option>
            </el-option-group>
          </el-select>
        </el-form-item>

        <el-form-item
          v-if="!isEdit"
          prop="useExistsTable"
          class="form-item"
          :rules="[requiredRule]"
        >
          <template #label>
            {{ t('data_fill.form.create_type') }}
          </template>
          <el-radio-group v-model="formData.useExistsTable" @change="onChangeUseExistsTable">
            <el-radio :label="false">{{ t('data_fill.form.create_new_table') }}</el-radio>
            <el-radio :label="true">{{ t('data_fill.form.bind_exists_table') }}</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="!isEdit" prop="tableName" class="form-item" :rules="[requiredRule]">
          <template #label>
            {{ t('data_fill.form.table_name') }}
            <span class="df-input-require">*</span>
          </template>
          <template v-if="!formData.useExistsTable">
            <el-input v-model.trim="formData.tableName" required maxlength="50" show-word-limit />
          </template>
          <template v-else>
            <el-select
              style="width: 100%"
              v-model="formData.tableName"
              @change="listTableColumns"
              filterable
              fit-input-width
            >
              <el-option
                v-for="t in existsTables"
                :key="t.tableName"
                :label="t.tableName"
                :value="t.tableName"
              >
                <span class="ellipsis" style="max-width: 45%" :title="t.tableName">{{
                  t.tableName
                }}</span>
                <span
                  class="ellipsis"
                  v-if="t.name && t.name.trim().length > 0"
                  style="margin-left: 8px; font-size: 12px; color: darkgrey; max-width: 45%"
                  :title="t.name"
                  >{{ t.name }}</span
                >
              </el-option>
            </el-select>
          </template>
        </el-form-item>

        <el-table :data="formData.forms" border stripe style="width: 100%">
          <el-table-column :label="t('data_fill.form.form_column')" width="260">
            <template #default="scope">
              {{ scope.row.settings.name }}
            </template>
          </el-table-column>
          <el-table-column>
            <template #header>
              {{ t('data_fill.form.column_name') }}
            </template>
            <template #default="scope">
              <el-row :gutter="8">
                <el-col
                  :span="8"
                  v-if="(!isEdit && formData.useExistsTable) || (isEdit && !scope.row.old)"
                >
                  <el-form-item class="form-item no-margin-bottom">
                    <el-select
                      v-model="scope.row.settings.mapping.useExistsTable"
                      style="width: 100%"
                    >
                      <el-option :label="t('data_fill.form.create_new_column')" :value="false" />
                      <el-option :label="t('data_fill.form.select_exists_column')" :value="true" />
                    </el-select>
                  </el-form-item>
                </el-col>

                <el-col
                  :span="
                    !((!isEdit && formData.useExistsTable) || (isEdit && !scope.row.old)) ? 24 : 16
                  "
                >
                  <el-form-item
                    v-if="scope.row.type !== 'dateRange'"
                    :prop="'forms[' + scope.$index + '].settings.mapping.columnName'"
                    class="form-item no-margin-bottom"
                    :rules="
                      scope.row.settings.mapping.useExistsTable &&
                      (!isEdit || (isEdit && !scope.row.old))
                        ? [requiredRule, duplicateRule, checkSelectExistsColumnRule]
                        : [requiredRule, duplicateRule]
                    "
                  >
                    <el-input
                      v-if="!scope.row.settings.mapping.useExistsTable || (isEdit && scope.row.old)"
                      v-model.trim="scope.row.settings.mapping.columnName"
                      :disabled="isEdit && scope.row.old"
                      :placeholder="t('common.please_input')"
                      style="width: 100%"
                      maxlength="50"
                      show-word-limit
                      required
                    />
                    <el-select
                      v-else
                      v-model="scope.row.settings.mapping.columnName"
                      style="width: 100%"
                      @change="onSelectExistsColumn(scope.row)"
                    >
                      <el-option
                        v-for="o in getExistsColumnsToSelect(scope.row)"
                        :disabled="o.disabled"
                        :key="o.originName"
                        :label="o.originName"
                        :value="o.originName"
                      >
                        <span>{{ o.originName }}</span>
                        <span
                          v-if="o.name && o.name.trim().length > 0"
                          style="margin-left: 8px; font-size: 12px; color: darkgrey"
                          >{{ o.name }}</span
                        >
                      </el-option>
                    </el-select>
                  </el-form-item>
                  <template v-else>
                    <el-form-item
                      :prop="'forms[' + scope.$index + '].settings.mapping.columnName1'"
                      class="form-item no-margin-bottom"
                      :rules="
                        scope.row.settings.mapping.useExistsTable &&
                        (!isEdit || (isEdit && !scope.row.old))
                          ? [requiredRule, duplicateRule, checkSelectExistsColumnRule]
                          : [requiredRule, duplicateRule]
                      "
                    >
                      <el-input
                        v-if="
                          !scope.row.settings.mapping.useExistsTable || (isEdit && scope.row.old)
                        "
                        v-model.trim="scope.row.settings.mapping.columnName1"
                        :disabled="isEdit && scope.row.old"
                        :placeholder="t('data_fill.form.please_insert_start')"
                        maxlength="50"
                        show-word-limit
                        required
                      />
                      <el-select
                        v-else
                        v-model="scope.row.settings.mapping.columnName1"
                        style="width: 100%"
                        @change="onSelectExistsColumn(scope.row)"
                      >
                        <el-option
                          v-for="o in getExistsColumnsToSelect(scope.row)"
                          :disabled="o.disabled"
                          :key="o.originName"
                          :label="o.originName"
                          :value="o.originName"
                        >
                          <span>{{ o.originName }}</span>
                          <span
                            v-if="o.name && o.name.trim().length > 0"
                            style="margin-left: 8px; font-size: 12px; color: darkgrey"
                            >{{ o.name }}</span
                          >
                        </el-option>
                      </el-select>
                    </el-form-item>
                    <el-form-item
                      :prop="'forms[' + scope.$index + '].settings.mapping.columnName2'"
                      class="form-item no-margin-bottom"
                      style="margin-top: 8px"
                      :rules="
                        scope.row.settings.mapping.useExistsTable &&
                        (!isEdit || (isEdit && !scope.row.old))
                          ? [requiredRule, duplicateRule, checkSelectExistsColumnRule]
                          : [requiredRule, duplicateRule]
                      "
                    >
                      <el-input
                        v-if="
                          !scope.row.settings.mapping.useExistsTable || (isEdit && scope.row.old)
                        "
                        v-model.trim="scope.row.settings.mapping.columnName2"
                        :disabled="isEdit && scope.row.old"
                        :placeholder="t('data_fill.form.please_insert_end')"
                        maxlength="50"
                        show-word-limit
                        required
                      />
                      <el-select
                        v-else
                        v-model="scope.row.settings.mapping.columnName2"
                        style="width: 100%"
                        @change="onSelectExistsColumn(scope.row)"
                      >
                        <el-option
                          v-for="o in getExistsColumnsToSelect(scope.row)"
                          :disabled="o.disabled"
                          :key="o.originName"
                          :label="o.originName"
                          :value="o.originName"
                        >
                          <span>{{ o.originName }}</span>
                          <span
                            v-if="o.name && o.name.trim().length > 0"
                            style="margin-left: 8px; font-size: 12px; color: darkgrey"
                            >{{ o.name }}</span
                          >
                        </el-option>
                      </el-select>
                    </el-form-item>
                  </template>
                </el-col>
              </el-row>
            </template>
          </el-table-column>
          <el-table-column :label="t('data_fill.form.column_type')" width="200">
            <template #default="scope">
              <el-form-item
                :prop="'forms[' + scope.$index + '].settings.mapping.type'"
                class="form-item no-margin-bottom"
                :rules="[requiredRule]"
              >
                <el-select
                  v-model="scope.row.settings.mapping.type"
                  :disabled="
                    (isEdit && scope.row.old) ||
                    (scope.row.settings.mapping.useExistsTable && !scope.row.old)
                  "
                  :placeholder="t('data_fill.form.please_select')"
                  required
                  style="width: 100%"
                >
                  <el-option
                    v-for="o in scope.row.settings.mapping.typeOptions"
                    :key="o.value"
                    :value="o.value"
                    :label="o.label"
                  />
                </el-select>
              </el-form-item>
            </template>
          </el-table-column>
        </el-table>

        <div style="display: flex; align-items: center">
          <el-form-item prop="createIndex" class="form-item no-margin-bottom">
            <el-checkbox
              v-model="formData.createIndex"
              :disabled="disableCreateIndex"
              :label="t('data_fill.form.create_index')"
            />
          </el-form-item>

          <el-button
            v-if="formData.createIndex"
            type="text"
            style="margin-left: 20px"
            @click="addIndex"
            >+ {{ t('data_fill.form.add_index') }}
          </el-button>
        </div>

        <el-table
          v-if="formData.createIndex"
          :data="formData.tableIndexes"
          border
          stripe
          class="df-index-table"
          :class="{ 'no-border-bottom': formData.tableIndexes.length > 0 }"
        >
          <el-table-column :label="t('data_fill.form.index_name')" width="300">
            <template #default="scope">
              <el-form-item
                :prop="'tableIndexes[' + scope.$index + '].name'"
                class="form-item"
                :class="
                  scope.row.columns.length === 1 && isEdit && scope.row.old
                    ? 'no-margin-bottom'
                    : ''
                "
                :rules="[requiredRule, duplicateIndexRule]"
              >
                <el-input
                  v-model="scope.row.name"
                  :disabled="isEdit && scope.row.old"
                  :placeholder="t('common.please_input')"
                  maxlength="50"
                  show-word-limit
                  required
                />
              </el-form-item>
            </template>
          </el-table-column>

          <el-table-column>
            <template #header #default="scope">
              <div style="display: flex; flex-direction: row; align-items: center; height: 20px">
                {{ t('data_fill.form.index_column') }}
                <el-tooltip
                  class="item"
                  effect="dark"
                  placement="bottom"
                  :content="t('data_fill.form.create_index_hint')"
                >
                  <el-icon style="cursor: pointer; margin-bottom: 1px; margin-left: 2px">
                    <Icon name="icon_info_outlined">
                      <icon_info_outlined class="svg-icon" />
                    </Icon>
                  </el-icon>
                </el-tooltip>
              </div>
            </template>
            <template #default="scope">
              <div
                v-for="(indexRow, $index) in scope.row.columns"
                :key="$index"
                class="index-column-row"
              >
                <el-form-item
                  :prop="'tableIndexes[' + scope.$index + '].columns[' + $index + '].column'"
                  class="form-item no-margin-bottom"
                  :rules="[requiredRule, invalidColumnRule, duplicateIndexColumnRule]"
                  style="flex: 1"
                >
                  <el-select
                    v-model="indexRow.column"
                    :disabled="isEdit && scope.row.old"
                    :placeholder="t('data_fill.form.please_select')"
                    required
                    style="width: 100%"
                  >
                    <el-option
                      v-for="(x, $index) in isEdit && scope.row.old ? allColumnsList : columnsList"
                      :key="$index"
                      :value="x.value"
                      :label="x.name"
                    />
                  </el-select>
                </el-form-item>

                <el-form-item
                  :prop="'tableIndexes[' + scope.$index + '].columns[' + $index + '].order'"
                  class="form-item no-margin-bottom"
                  style="width: 150px; margin-left: 8px"
                  :rules="[requiredRule]"
                >
                  <el-select
                    v-model="indexRow.order"
                    :disabled="isEdit && scope.row.old"
                    :placeholder="t('data_fill.form.please_select')"
                    required
                    style="width: 100%"
                  >
                    <el-option value="none" :label="t('data_fill.form.order_none')" />
                    <el-option value="asc" :label="t('data_fill.form.order_asc')" />
                    <el-option value="desc" :label="t('data_fill.form.order_desc')" />
                  </el-select>
                </el-form-item>
                <div
                  v-if="scope.row.columns.length > 1 && !(isEdit && scope.row.old)"
                  class="btn-item"
                  @click="removeIndexColumn(scope.row.columns, $index)"
                >
                  <el-icon>
                    <Icon name="icon_delete-trash_outlined">
                      <icon_deleteTrash_outlined class="svg-icon" />
                    </Icon>
                  </el-icon>
                </div>
              </div>
              <el-button
                v-if="scope.row.columns.length < 5 && !(isEdit && scope.row.old)"
                type="text"
                @click="addColumn(scope.row.columns)"
                >+ {{ t('data_fill.form.add_column') }}
              </el-button>
            </template>
          </el-table-column>

          <el-table-column width="50">
            <template #default="scope">
              <div
                v-if="!(isEdit && scope.row.old)"
                class="btn-item"
                @click="removeIndex(scope.$index)"
              >
                <el-icon>
                  <Icon name="icon_delete-trash_outlined">
                    <icon_deleteTrash_outlined class="svg-icon" />
                  </Icon>
                </el-icon>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-form>
    </el-main>
    <el-footer class="de-footer">
      <el-button @click="closeSave">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" @click="doSave">{{ t('dataset.confirm') }} </el-button>
    </el-footer>
  </el-container>
</template>

<style scoped lang="less">
.DataFillingFormSave {
  .df-input-require {
    color: red;
    margin-left: 2px;
  }

  height: 100%;

  :deep(.el-form-item__error) {
    position: relative;
  }

  .de-header {
    height: 56px !important;
    padding: 0px !important;
    border-bottom: 1px solid #e6e6e6;
    background-color: var(--SiderBG, white);

    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
  }

  .de-footer {
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: flex-end;
    border-top: 1px solid #e6e6e6;
  }

  .panel-info-area {
    padding-left: 20px;
  }

  .de-main {
    display: flex;
    align-items: center;
    flex-direction: column;

    .m-form {
      width: 80%;
    }
  }

  .no-margin-bottom {
    margin-bottom: 0;
  }

  :deep(.ed-form-item.is-error) {
    .ed-form-item__error {
      padding-top: 0;
    }

    &.no-margin-bottom {
      margin-bottom: 22px;
    }
  }

  .btn-item {
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: center;

    width: 24px;
    height: 24px;

    margin-left: 8px;

    border-radius: 4px;

    cursor: pointer;
  }

  .btn-item:first-child {
    margin-left: unset;
  }

  .btn-item:hover {
    background: rgba(31, 35, 41, 0.1);
  }
}

.dataset-filed {
  height: 400px;
  overflow-y: auto;
}

.tree-select-dataset {
  display: none;
}

.dataset-tree-select {
  .ed-select-dropdown__item {
    display: flex;
    align-items: center;

    .ed-icon {
      margin-right: 5px;
    }
  }
}

.df-index-table {
  width: 100%;
  border-bottom: var(--ed-table-border);
  .index-column-row {
    display: flex;
    flex-direction: row;
    align-items: center;
    margin-bottom: 8px;
  }

  &.no-border-bottom {
    border-bottom: unset;
  }
}

.hide-asterisk {
  :deep(.ed-form-item__label) {
    height: auto;
    width: 100%;
    &:after {
      display: none;
    }
  }

  .asterisk {
    color: var(--ed-color-danger);
    margin-left: 2px;
    font-family: var(--de-custom_font, 'PingFang');
    font-size: 14px;
    font-style: normal;
    font-weight: 400;
  }

  .label-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
  }
}
</style>
