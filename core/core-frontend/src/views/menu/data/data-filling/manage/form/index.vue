<script lang="tsx" setup>
import icon_left_outlined from '@/assets/svg/icon_left_outlined.svg'
import icon_copy_outlined from '@/assets/svg/icon_copy_outlined.svg'
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'
import icon_single_line_outlined from '@/assets/svg/icon_single-line_outlined.svg'
import icon_multi_line_outlined from '@/assets/svg/icon_multi-line_outlined.svg'
import icon_down_outlined from '@/assets/svg/icon_down_outlined.svg'
import icon_radio_outlined from '@/assets/svg/icon_radio_outlined.svg'
import icon_todo_outlined from '@/assets/svg/icon_todo_outlined.svg'
import icon_calendar_outlined from '@/assets/svg/icon_calendar_outlined.svg'
import icon_edit_outlined from '@/assets/svg/icon_edit_outlined.svg'
import RowAuth from './data-fill-tree/auth-tree/RowAuth.vue'

import { computed, nextTick, onMounted, provide, ref, watch } from 'vue'
import { ElIcon, ElMessage, ElMessageBox } from 'element-plus-secondary'
import router from '@/router'
import { useI18n } from '@/hooks/web/useI18n'
import DataFillingFormSave from './DataFillingFormSave.vue'
import SelectDetailColumns from './SelectDetailColumns.vue'
import MoreDetailColumns from './MoreDetailColumns.vue'
import { useEmitt } from '@/hooks/web/useEmitt'
import { cloneDeep, filter, find, forEach, groupBy, join, keys, map } from 'lodash-es'
import { uuid } from 'vue-uuid'
import draggable from 'vuedraggable'
import { Icon } from '@/components/icon-custom'
import { EMAIL_REGEX } from '@/utils/validate'
import {
  DfFormItem,
  DfFormSetting,
  FormItemSetting,
  getDataFilling,
  getExtraDetailsApiPreview,
  getTableColumnDataPreview,
  listAllDatasourceList,
  OptionItem,
  SimpleDatasource
} from '../../data-filling'
import { getTableField, listDatasourceTables } from '@/api/datasource'
import { useEmbedded } from '@/store/modules/embedded'
import { useAppStoreWithOut } from '@/store/modules/app'

const { t } = useI18n()

const datasetTableFiled = computed(() => {
  return columnList.value.map(c => {
    return {
      id: c.fieldName,
      name: c.fieldName,
      deType: c.deType,
      fieldType: c.fieldType
    }
  })
})

const currentDsType = computed(() => {
  let type = ''
  datasourceList.value.forEach(c => {
    c.options.find(c => {
      if (c.id === optionFormData.value.optionDatasource) {
        type = c.type
      }
      return c.id === optionFormData.value.optionDatasource
    })
  })
  return type
})

const optionFormDataComputed = computed(() => {
  return {
    optionDatasource: optionFormData.value.optionDatasource,
    optionTable: optionFormData.value.optionTable
  }
})
provide('filedList', datasetTableFiled)
provide('optionFormData', optionFormDataComputed)

const embeddedStore = useEmbedded()
const appStore = useAppStoreWithOut()
const isEmbedded = computed(() => appStore.getIsDataEaseBi || appStore.getIsIframe)

const loading = ref<boolean>(false)
const showInput = ref<boolean>(false)
const editorName = ref()
const mTitleForm = ref()
const mRightForm = ref()
const asyncOptions = ref({})

const isEdit = ref<boolean>(false)
const disableCreateIndex = ref<boolean>(false)

let nodeInfo = {
  id: undefined,
  pid: undefined,
  name: ''
}

const selectedItemId = ref<string | undefined>(undefined)

const selectedComponentItem = computed<DfFormItem | undefined>(() => {
  if (selectedItemId.value) {
    return find(formSettings.value.forms, f => f.id === selectedItemId.value)
  }
  return undefined
})

const formSettings = ref<DfFormSetting>({
  id: undefined,
  name: t('data_fill.form.untitled'),
  pid: undefined,
  datasource: undefined,
  tableName: undefined,
  forms: [],
  createIndex: false,
  useExistsTable: false,
  tableIndexes: []
})

const iconMap = ref({
  icon_single_line_outlined: icon_single_line_outlined,
  icon_multi_line_outlined: icon_multi_line_outlined,
  icon_down_outlined: icon_down_outlined,
  icon_radio_outlined: icon_radio_outlined,
  icon_todo_outlined: icon_todo_outlined,
  icon_calendar_outlined: icon_calendar_outlined
})

const componentList = computed<Array<DfFormItem>>(() => [
  {
    type: 'input',
    typeName: t('common.component.input'),
    icon: 'icon_single_line_outlined',
    order: 0,
    value: undefined,
    id: uuid.v4(),
    settings: {
      name: t('common.component.input'),
      placeholder: '',
      required: false,
      unique: false,
      inputType: 'text',
      mapping: {
        columnName: undefined,
        type: undefined,
        useExistsTable: false
      }
    }
  },
  {
    type: 'textarea',
    typeName: t('common.component.textarea'),
    icon: 'icon_multi_line_outlined',
    order: 1,
    value: undefined,
    id: uuid.v4(),
    settings: {
      name: t('common.component.textarea'),
      placeholder: '',
      required: false,
      mapping: {
        columnName: undefined,
        type: undefined,
        useExistsTable: false
      }
    }
  },
  {
    type: 'select',
    typeName: t('common.component.select'),
    icon: 'icon_down_outlined',
    order: 2,
    value: '',
    id: uuid.v4(),
    settings: {
      name: t('common.component.select'),
      options: [
        {
          name: t('data_fill.form.option') + ' 1',
          value: t('data_fill.form.option') + ' 1'
        },
        {
          name: t('data_fill.form.option') + ' 2',
          value: t('data_fill.form.option') + ' 2'
        }
      ],
      optionSourceType: 1,
      optionDatasource: undefined,
      optionTable: undefined,
      optionColumn: undefined,
      optionOrder: 'asc',
      placeholder: '',
      multiple: false,
      required: false,
      mapping: {
        columnName: undefined,
        type: undefined,
        useExistsTable: false
      }
    }
  },
  {
    type: 'radio',
    typeName: t('common.component.radio'),
    icon: 'icon_radio_outlined',
    order: 3,
    value: undefined,
    id: uuid.v4(),
    settings: {
      name: t('common.component.radio'),
      options: [
        {
          name: t('data_fill.form.option') + ' 1',
          value: t('data_fill.form.option') + ' 1'
        },
        {
          name: t('data_fill.form.option') + ' 2',
          value: t('data_fill.form.option') + ' 2'
        }
      ],
      optionSourceType: 1,
      optionDatasource: undefined,
      optionTable: undefined,
      optionColumn: undefined,
      optionOrder: 'asc',
      required: false,
      mapping: {
        columnName: undefined,
        type: undefined,
        useExistsTable: false
      }
    }
  },
  {
    type: 'checkbox',
    typeName: t('common.component.checkbox'),
    icon: 'icon_todo_outlined',
    order: 4,
    value: [],
    id: uuid.v4(),
    settings: {
      name: t('common.component.checkbox'),
      options: [
        {
          name: t('data_fill.form.option') + ' 1',
          value: t('data_fill.form.option') + ' 1'
        },
        {
          name: t('data_fill.form.option') + ' 2',
          value: t('data_fill.form.option') + ' 2'
        }
      ],
      optionSourceType: 1,
      optionDatasource: undefined,
      optionTable: undefined,
      optionColumn: undefined,
      optionOrder: 'asc',
      required: false,
      mapping: {
        columnName: undefined,
        type: undefined,
        useExistsTable: false
      }
    }
  },
  {
    type: 'date',
    typeName: t('common.component.date'),
    icon: 'icon_calendar_outlined',
    order: 5,
    value: undefined,
    id: uuid.v4(),
    settings: {
      name: t('common.component.date'),
      dateType: 'date',
      placeholder: '',
      required: false,
      mapping: {
        columnName: undefined,
        type: undefined,
        useExistsTable: false
      },
      enableDefaultTime: false,
      enableCurrentTime: true,
      defaultTime: undefined
    }
  },
  {
    type: 'dateRange',
    typeName: t('common.component.dateRange'),
    icon: 'icon_calendar_outlined',
    order: 6,
    value: [],
    id: uuid.v4(),
    settings: {
      name: t('common.component.dateRange'),
      dateType: 'daterange',
      rangeSeparator: '-',
      startPlaceholder: '',
      endPlaceholder: '',
      required: false,
      mapping: {
        columnName1: undefined,
        columnName2: undefined,
        type: undefined,
        useExistsTable: false
      }
    }
  }
])

const componentList1 = computed(() => {
  return filter(componentList.value, c => c.order !== undefined && c.order % 2 === 0)
})
const componentList2 = computed(() => {
  return filter(componentList.value, c => c.order !== undefined && c.order % 2 === 1)
})

const inputTypes = [
  { type: 'text', name: t('data_fill.form.text'), rules: [] },
  { type: 'number', name: t('data_fill.form.number'), rules: [] },
  {
    type: 'tel',
    name: t('data_fill.form.tel'),
    rules: []
  },
  {
    type: 'email',
    name: t('data_fill.form.email'),
    rules: [
      {
        pattern: EMAIL_REGEX,
        message: t('data_fill.form.email_format_is_incorrect'),
        trigger: ['blur', 'change']
      }
    ]
  }
]

const selectedComponentItemInputTypes = computed(() => {
  if (selectedComponentItem.value && selectedComponentItem.value.type === 'input') {
    if (isEdit.value && selectedComponentItem.value.old) {
      if (selectedComponentItem.value.settings.inputType === 'number') {
        return filter(inputTypes, t => t.type === 'number')
      } else {
        return filter(inputTypes, t => t.type !== 'number')
      }
    }
  }
  return inputTypes
})

const checkDuplicateOptionValidator = (rule, value, callback) => {
  if (!value) {
    return callback(new Error(t('common.component_required')))
  }
  const _list = filter(selectedComponentItem.value?.settings.options, f => f.value === value)
  if (_list.length > 1) {
    callback(new Error(t('data_fill.form.duplicate_error')))
  }
  callback()
}

const checkValidDatasourceValidator = (rule, value, callback) => {
  if (!value) {
    return callback(new Error(t('common.required')))
  }
  const ds = find(allDatasourceList.value, d => d.id === value)
  if (!ds) {
    return callback(new Error(t('common.required')))
  }
  if (ds.status === 'Error') {
    callback(new Error(t('data_set.invalid_data_source')))
  }
  callback()
}

const requiredRule = {
  required: true,
  message: t('common.required'),
  trigger: ['blur', 'change']
}
const duplicateOptionRule = {
  validator: checkDuplicateOptionValidator,
  trigger: ['blur', 'change']
}
const checkValidDatasourceRule = {
  validator: checkValidDatasourceValidator,
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

const dateTypes = [
  { name: t('chart.y'), value: 'year' },
  { name: t('chart.y_M'), value: 'month' },
  { name: t('chart.y_M_d'), value: 'date' },
  { name: t('chart.y_M_d_H_m_s'), value: 'datetime' }
]

const dateRangeTypes = [
  { name: t('chart.y_M'), value: 'monthrange' },
  { name: t('chart.y_M_d'), value: 'daterange' },
  { name: t('chart.y_M_d_H_m_s'), value: 'datetimerange' }
]

const group = {
  componentListGroup1: {
    name: 'mFormGroup',
    pull: 'clone', // B组拖拽时克隆到A组
    put: false
  },
  componentListGroup2: {
    name: 'mFormGroup',
    pull: 'clone', // B组拖拽时克隆到A组
    put: false
  },
  formGroup: {
    name: 'mFormGroup',
    put: true
  }
}

const onMoveInComponentList = (e, originalEvent) => {
  if (e.to?.id === 'form-drag-place') {
    return true
  }
  return false
}

const addComponentItem = (item: DfFormItem) => {
  const _item = cloneDeep(item)
  delete _item.order
  _item.id = uuid.v4()
  formSettings.value.forms.push(_item)

  selectedItemId.value = _item.id
  nextTick(() => {
    mRightForm.value?.validate()
  })
}

const cloneDraggable = (item: DfFormItem) => {
  const res = {
    ...item
  }
  delete res.order
  delete res.id
  res.id = undefined

  return res
}

const addComponent = e => {
  formSettings.value.forms = cloneDeep(formSettings.value.forms)

  formSettings.value.forms.forEach(f => {
    if (f.order !== undefined) {
      delete f.order
    }
    if (f.id === undefined) {
      f.id = uuid.v4()
      selectedItemId.value = f.id
      nextTick(() => {
        mRightForm.value?.validate()
      })
    }
  })
}

function selectItem(id: string, showError?: boolean) {
  selectedItemId.value = id
  nextTick(() => {
    mRightForm.value?.validate((valid, invalidFields) => {
      if (showError && !valid) {
        ElMessage({
          message: t('data_fill.form.component_setting_error'),
          type: 'error',
          showClose: true
        })
      }
    })
  })
}

function copyItem(item: DfFormItem, index: number) {
  const copyItem = cloneDeep(item)
  copyItem.id = uuid.v4()
  delete copyItem.old
  delete copyItem.settings.mapping.columnName
  delete copyItem.settings.mapping.columnName1
  delete copyItem.settings.mapping.columnName2
  formSettings.value.forms.splice(index + 1, 0, copyItem)

  selectedItemId.value = copyItem.id
  nextTick(() => {
    mRightForm.value?.validate()

    if (
      selectedComponentItem.value &&
      selectedComponentItem.value.id &&
      selectedComponentItem.value.settings &&
      selectedComponentItem.value.settings.optionSourceType === 2 &&
      selectedComponentItem.value.settings.optionDatasource &&
      selectedComponentItem.value.settings.optionTable &&
      selectedComponentItem.value.settings.optionColumn &&
      selectedComponentItem.value.settings.optionOrder
    ) {
      const id = selectedComponentItem.value.id

      getTableColumnDataPreview(
        selectedComponentItem.value.settings.optionDatasource,
        selectedComponentItem.value.settings.optionTable,
        selectedComponentItem.value.settings.optionColumn,
        selectedComponentItem.value.settings.optionOrder,
        selectedComponentItem.value.settings.permissionsTree
      ).then(data => {
        asyncOptions.value[id] = data.data
      })
    }
  })
}

function removeItem(item: DfFormItem, index: number) {
  formSettings.value.forms.splice(index, 1)
}

const lostFocus = () => {
  selectedItemId.value = undefined
  nextTick(() => {
    mRightForm.value?.validate()
  })
}

const handleClick = () => {
  showInput.value = true
  nextTick(() => {
    editorName.value.focus()
  })
}

function onOptionValueChange(item, value) {
  item.name = value
}

function addOption(list) {
  list.push({ name: '', value: '' })
}

function removeOption(list, index: number) {
  list.splice(index, 1)
}

function changeSelectMultiple(item: DfFormItem, multiple?: boolean) {
  if (multiple) {
    item.value = []
  } else {
    item.value = ''
  }
  item.settings.mapping.type = undefined
}

function onOptionSourceTypeChange(type?: 1 | 2, itemSettings) {
  if (type === 2) {
    getAsyncOption({
      optionSourceType: type,
      optionDatasource: itemSettings.optionDatasource,
      optionTable: itemSettings.optionTable,
      optionColumn: itemSettings.optionColumn,
      optionOrder: itemSettings.optionOrder
    })
  }
}

function getAsyncOption(itemSettings, callback?: any) {
  if (
    selectedComponentItem.value &&
    selectedComponentItem.value.id &&
    itemSettings.optionSourceType === 2 &&
    itemSettings.optionDatasource &&
    itemSettings.optionTable &&
    itemSettings.optionColumn &&
    itemSettings.optionOrder
  ) {
    const id = selectedComponentItem.value.id

    getTableColumnDataPreview(
      itemSettings.optionDatasource,
      itemSettings.optionTable,
      itemSettings.optionColumn,
      itemSettings.optionOrder,
      itemSettings.permissionsTree
    )
      .then(data => {
        asyncOptions.value[id] = data.data
      })
      .finally(() => {
        if (callback) {
          callback()
        }
      })
  }
}

const tableList = ref([])
const columnList = ref([])
const optionFormData = ref<FormItemSetting>({})
const showEditBindColumn = ref(false)

const allDatasourceList = ref<Array<SimpleDatasource>>([])

const plugins = ref([])

/*const enabledDsTypes = computed(() => {
  const base = ['mysql', 'mariadb']
  for (let i = 0; i < plugins.value.length; i++) {
    base.push(plugins.value[i])
  }
  return base;
})*/

const datasourceList = computed(() => {
  const dsMap = groupBy(
    filter(allDatasourceList.value, d => d.id !== '-1'),
    d => d.type
  )
  const _types: Array<any> = []
  if (dsMap) {
    forEach(keys(dsMap), type => {
      //if (enabledDsTypes.value.includes(type)) {
      _types.push({
        name: dsMap[type][0]?.typeAlias,
        type: type,
        options: dsMap[type]
      })
      //}
    })
  }
  return _types
})

function openEditBindColumn(settings, enableExtraColumns) {
  tableList.value = []
  columnList.value = []
  optionFormData.value = cloneDeep({
    optionSourceType: 2,
    optionDatasource: settings.optionDatasource,
    optionTable: settings.optionTable,
    optionColumn: settings.optionColumn,
    optionOrder: settings.optionOrder ?? 'asc',
    extraColumns: settings.extraColumns ?? [],
    enableExtraColumns: enableExtraColumns,
    permissionsTree: settings.permissionsTree ?? {}
  })
  const p1 = settings.optionDatasource
    ? listDatasourceTables({ datasourceId: settings.optionDatasource })
    : undefined
  const p2 =
    settings.optionDatasource && settings.optionTable
      ? getTableField({
          datasourceId: settings.optionDatasource,
          tableName: settings.optionTable
        })
      : undefined
  const promiseList = []
  if (p1) {
    promiseList.push(p1)
    if (p2) {
      promiseList.push(p2)
    }
  }
  if (promiseList.length > 1) {
    loading.value = true
    Promise.all(promiseList)
      .then(val => {
        tableList.value = map(val[0].data, t => {
          return { value: t.tableName, label: t.tableName }
        })
        if (find(tableList.value, t => t.value === optionFormData.value.optionTable)) {
          if (promiseList.length > 1) {
            columnList.value = map(val[1].data, c => {
              return {
                fieldName: c.originName,
                displayName: c.name,
                deType: c.deType,
                fieldType: c.fieldType
              }
            })
            if (!find(columnList.value, t => t.fieldName === optionFormData.value.optionColumn)) {
              optionFormData.value.optionColumn = undefined
            }
          }
        } else {
          optionFormData.value.optionTable = undefined
          optionFormData.value.optionColumn = undefined
        }
      })
      .finally(() => {
        loading.value = false
      })
  }

  optionForm.value?.resetFields()

  showEditBindColumn.value = true
  nextTick(() => {
    rowAuth.value?.init(selectedComponentItem.value?.settings?.permissionsTree ?? {})
  })
}

function onDataSourceChange(datasource) {
  tableList.value = []
  columnList.value = []
  if (datasource) {
    loading.value = true
    listDatasourceTables({ datasourceId: datasource })
      .then(res => {
        tableList.value = map(res.data, t => {
          return { value: t.tableName, label: t.tableName }
        })

        if (optionFormData.value.optionTable) {
          if (find(tableList.value, t => t.value === optionFormData.value.optionTable)) {
            onTableChange(datasource, optionFormData.value.optionTable)
          } else {
            optionFormData.value.optionTable = undefined
            optionFormData.value.optionColumn = undefined
            optionFormData.value.permissionsTree = {}
            nextTick(() => {
              rowAuth.value?.init(optionFormData.value.permissionsTree ?? {})
            })
          }
        } else {
          nextTick(() => {
            rowAuth.value?.init(optionFormData.value.permissionsTree ?? {})
          })
        }
      })
      .finally(() => {
        loading.value = false
      })
  }
}

function onTableChange(datasource, table) {
  columnList.value = []
  if (datasource && table) {
    loading.value = true
    getTableField({ datasourceId: datasource, tableName: table })
      .then(res => {
        columnList.value = map(res.data, c => {
          return {
            fieldName: c.originName,
            displayName: c.name,
            deType: c.deType,
            fieldType: c.fieldType
          }
        })

        if (optionFormData.value.optionColumn) {
          if (!find(columnList.value, t => t.fieldName === optionFormData.value.optionColumn)) {
            optionFormData.value.optionColumn = undefined
            optionFormData.value.permissionsTree = {}
          }
        }
        nextTick(() => {
          rowAuth.value?.init(optionFormData.value.permissionsTree ?? {})
        })
      })
      .finally(() => {
        loading.value = false
      })
  }
}

function closeEditBindColumn() {
  showEditBindColumn.value = false
}

const optionForm = ref()

const selectDetailColumnsRef = ref()

function openSelectDetailColumns() {
  selectDetailColumnsRef.value?.init(columnList.value, optionFormData.value.extraColumns)
}

function onSelectDetailColumnsClose(item: Array<OptionItem>) {
  optionFormData.value.extraColumns = JSON.parse(JSON.stringify(item))
}

const showMoreDetails = ref(false)
const moreDetails = ref([])

const extraColumns = computed(() => {
  const list: Array<OptionItem> = []
  optionFormData.value.extraColumns?.forEach((i: OptionItem) => {
    if (find(columnList.value, o => o.fieldName === i.fieldName)) {
      list.push(i)
    }
  })
  return list
})

function deleteAllExtraColumns() {
  optionFormData.value.extraColumns = []
}

const extraColumnsStr = computed(() => {
  const _list: Array<string> = []
  extraColumns.value.forEach(e => {
    if (e.displayName && e.displayName.trim().length > 0) {
      _list.push(e.displayName)
    } else {
      _list.push(e.fieldName)
    }
  })
  return join(_list, ', ')
})

function doEditBindColumn() {
  optionForm.value?.validate((valid, invalidFields) => {
    if (valid) {
      loading.value = true
      getAsyncOption(optionFormData.value, () => {
        selectedComponentItem.value.settings.optionSourceType =
          optionFormData.value.optionSourceType
        selectedComponentItem.value.settings.optionDatasource =
          optionFormData.value.optionDatasource
        selectedComponentItem.value.settings.optionTable = optionFormData.value.optionTable
        selectedComponentItem.value.settings.optionColumn = optionFormData.value.optionColumn
        selectedComponentItem.value.settings.optionOrder = optionFormData.value.optionOrder
        selectedComponentItem.value.settings.extraColumns = optionFormData.value.extraColumns
        selectedComponentItem.value.settings.permissionsTree = optionFormData.value.permissionsTree
        loading.value = false

        getExtraDetails(selectedComponentItem.value)

        closeEditBindColumn()
      })
    }
  })
}

function trimName(obj) {
  obj.name = obj.name?.trim()
}

const handleDfName = () => {
  formSettings.value.name = formSettings.value.name?.trim()
  mTitleForm.value?.validate(valid => {
    showInput.value = !valid
  })
}

const showAllDetails = list => {
  showMoreDetails.value = true
  moreDetails.value = list
}

const getExtraDetails = (element: DfFormItem) => {
  element.extraDetails = []
  if (
    (element.type === 'radio' || (element.type === 'select' && !element.settings.multiple)) &&
    element.value !== undefined &&
    element.settings?.optionSourceType === 2 &&
    element.settings?.extraColumns &&
    element.settings?.extraColumns.length > 0 &&
    element.settings?.optionDatasource &&
    element.settings?.optionTable
  ) {
    //查询值
    getExtraDetailsApiPreview({
      optionDatasource: element.settings.optionDatasource,
      optionTable: element.settings.optionTable,
      optionColumn: element.settings.optionColumn,
      extraColumns: element.settings?.extraColumns,
      value: element.value,
      columnId: element.id,
      formId: formSettings.value.id,
      permissionsTree: element.settings.permissionsTree
    }).then(res => {
      element.extraDetails = res.data
    })
  }
}

const backToMain = () => {
  if (isUpdate) {
    ElMessageBox.confirm(t('data_fill.task.confirm_exit_without_save'), {
      confirmButtonText: t('dataset.confirm'),
      cancelButtonText: t('common.cancel'),
      showCancelButton: true,
      confirmButtonType: 'primary',
      type: 'warning',
      autofocus: false,
      showClose: false
    }).then(() => {
      gotoDf()
    })
  } else {
    gotoDf()
  }
}

const enableBack = ref(false)

const dfSaveAndBack = () => {
  enableBack.value = true
  dfSave()
}

const saveAndBack = (_willBack: boolean) => {
  if (!_willBack) return
  gotoDf()
}

const showDrawer = ref(false)

const dfSave = () => {
  if (formSettings.value.name === undefined || formSettings.value.name.trim() === '') {
    ElMessage({
      message: t('data_fill.form.form_name_cannot_none'),
      type: 'error',
      showClose: true
    })
    lostFocus()
    showInput.value = true
    handleDfName()
    return
  }
  if (formSettings.value.forms.length === 0) {
    ElMessage({
      message: t('data_fill.form.form_components_cannot_null'),
      type: 'warning',
      showClose: true
    })
    return
  }
  for (let i = 0; i < formSettings.value.forms.length; i++) {
    const f = formSettings.value.forms[i]
    if (f.settings.name === undefined || f.settings.name.trim() === '') {
      selectItem(f.id, true)
      return
    }
    if (f.type === 'dateRange') {
      if (f.settings.rangeSeparator === undefined || f.settings.rangeSeparator.trim() === '') {
        selectItem(f.id, true)
        return
      }
    }
    if (f.type === 'date') {
      if (
        (f.settings.enableDefaultTime &&
          !f.settings.enableCurrentTime &&
          f.settings.defaultTime === undefined) ||
        f.settings.defaultTime === null
      ) {
        selectItem(f.id, true)
        return
      }
    }
    if (f.type === 'select' || f.type === 'radio' || f.type === 'checkbox') {
      if (f.settings.optionSourceType === 1) {
        if (f.settings.options.length === 0) {
          selectItem(f.id)
          ElMessage({
            message: t('data_fill.form.option_list_cannot_empty'),
            type: 'error',
            showClose: true
          })
          return
        } else {
          for (let j = 0; j < f.settings.options.length; j++) {
            const o = f.settings.options[j]
            const value = o.value
            if (value === undefined || value === '') {
              selectItem(f.id, true)
              return
            }
            const _list = filter(f.settings.options, f => f.value === value)
            if (_list.length > 1) {
              selectItem(f.id, true)
              return
            }
          }
        }
      } else {
        if (
          f.settings.optionDatasource == undefined ||
          f.settings.optionTable == undefined ||
          f.settings.optionColumn == undefined
        ) {
          selectItem(f.id)
          ElMessage({
            message: t('data_fill.form.option_list_datasource_cannot_empty'),
            type: 'error',
            showClose: true
          })
          return
        }
      }
    }
  }

  showDrawer.value = true
}

function closeDrawer() {
  showDrawer.value = false
  enableBack.value = false
  handleDfName()
}

const gotoDf = () => {
  if (isEmbedded.value) {
    embeddedStore.clearState()
    embeddedStore.setDfId(nodeInfo.id)
    useEmitt().emitter.emit('changeCurrentComponent', 'DataFilling')
    return
  }
  const path = '/data/data-filling-manage'
  router.push({
    path: path,
    query: nodeInfo.id
      ? {
          id: nodeInfo.id
        }
      : {}
  })
}

const finish = res => {
  const { id, pid, name, willBack } = res
  console.log(willBack)
  nodeInfo = {
    id,
    pid,
    name
  }
  copy.value = false

  hasDataInit(res)
  closeDrawer()
  saveAndBack(willBack)
}

function hasDataInit(res) {
  startToRecordUpdate.value = false
  isUpdate = false

  isEdit.value = true
  loading.value = true
  mainLoading.value = true

  const tempData = cloneDeep(res)

  if (copy.value) {
    isEdit.value = false
    tempData.id = undefined
    tempData.name = tempData.name + '-copy'
    tempData.tableName = undefined
    tempData.createIndex = false
    tempData.useExistsTable = false
    tempData.tableIndexes = '[]'
  }

  formSettings.value = tempData
  initData(res, () => {
    if (res.createIndex && !copy.value) {
      forEach(formSettings.value.tableIndexes, f => {
        f.old = true
      })
      formSettings.value.oldTableIndexes = JSON.parse(res.tableIndexes)
    } else {
      formSettings.value.oldTableIndexes = []
    }

    if (copy.value) {
      forEach(formSettings.value.tableIndexes, f => {
        f.old = false
      })
      forEach(formSettings.value.forms, f => {
        f.settings.mapping.useExistsTable = false
      })
    }

    disableCreateIndex.value = copy.value ? false : res.createIndex
    loading.value = false

    nextTick(() => {
      startToRecordUpdate.value = true
      mainLoading.value = false
    })
  })
}

function initData(data, callback) {
  const tempForms = filter(JSON.parse(data.forms), f => !f.removed)
  forEach(tempForms, f => {
    if (!copy.value) {
      f.old = true
    } else {
      f.old = false
    }
    if (f.type === 'checkbox' || (f.type === 'select' && f.settings.multiple)) {
      f.value = []
    }
  })

  initFormOptionsData(tempForms, () => {
    formSettings.value.forms = tempForms
    formSettings.value.oldForms = JSON.parse(data.forms)
    formSettings.value.tableIndexes = JSON.parse(data.tableIndexes)

    forEach(formSettings.value.forms, f => {
      getExtraDetails(f)
      f.settings.enableDefaultTime = !!f.settings.enableDefaultTime
      f.settings.enableCurrentTime = !!f.settings.enableCurrentTime
      if (f.settings.mapping) {
        f.settings.mapping.useExistsTable = !!f.settings.mapping.useExistsTable
      }
    })

    if (callback) {
      callback()
    }
  })
}

function initFormOptionsData(forms, callback) {
  const queries = []
  const queryIds = []
  forEach(forms, f => {
    if (f.type === 'checkbox' || f.type === 'select' || f.type === 'radio') {
      if (
        f.settings &&
        f.settings.optionSourceType === 2 &&
        f.settings.optionDatasource &&
        f.settings.optionTable &&
        f.settings.optionColumn &&
        f.settings.optionOrder
      ) {
        const id = f.id

        const p = getTableColumnDataPreview(
          f.settings.optionDatasource,
          f.settings.optionTable,
          f.settings.optionColumn,
          f.settings.optionOrder,
          f.settings.permissionsTree
        )
        queries.push(p)
        queryIds.push(id)
      }
    }
  })

  if (queries.length > 0) {
    Promise.all(queries)
      .then(val => {
        for (let i = 0; i < queryIds.length; i++) {
          const id = queryIds[i]
          asyncOptions.value[id] = val[i].data
        }
      })
      .finally(() => {
        if (callback) {
          callback()
        }
      })
  } else {
    if (callback) {
      callback()
    }
  }
}

let startToRecordUpdate = ref(false)
let isUpdate = false
const changeUpdate = () => {
  isUpdate = true
}

watch(
  () => formSettings.value,
  () => {
    if (startToRecordUpdate.value) {
      changeUpdate()
    }
  },
  { deep: true }
)

const mainLoading = ref(false)

const copy = ref(false)

const dsLoading = ref(false)

function getDatasourceList(finalFunc) {
  dsLoading.value = true
  listAllDatasourceList()
    .then(res => {
      allDatasourceList.value = res
    })
    .finally(() => {
      dsLoading.value = false
      if (finalFunc && typeof finalFunc === 'function') {
        finalFunc()
      }
    })
}

onMounted(() => {
  if (
    router.currentRoute.value.query?.id ||
    router.currentRoute.value.query?.copyId ||
    embeddedStore.getDfId
  ) {
    if (
      router.currentRoute.value.query?.copyId ||
      (embeddedStore.getDfId && embeddedStore.opt === 'copy')
    ) {
      copy.value = true
    }

    const id =
      embeddedStore.getDfId ||
      (copy.value ? router.currentRoute.value.query?.copyId : router.currentRoute.value.query.id)

    mainLoading.value = true

    getDataFilling(id).then(res => {
      nodeInfo = {
        id,
        pid: res.pid,
        name: res.name
      }
      if (copy.value) {
        nodeInfo.id = undefined
        nodeInfo.name = res.name + '-copy'
      }

      hasDataInit(res)
    })
  } else {
    startToRecordUpdate.value = true

    const pid = embeddedStore.getPid || router.currentRoute.value.query?.pid

    if (pid) {
      nodeInfo.pid = pid

      formSettings.value.pid = pid
    }
  }

  getDatasourceList(() => {})

  /*listDfPlugins().then(res => {
    if (res && res.data) {
      for (let i = 0; i < res.data.length; i++) {
        plugins.value.push(res.data[i].type)
      }
    }
  })*/

  useEmitt({
    name: 'onDfSave',
    callback: saveAndBack
  })
})

function getOptionList(element, needLabel?) {
  const tempId = element.id ?? 'unset'
  const list =
    element.settings.optionSourceType === 1
      ? element.settings.options
      : asyncOptions.value[tempId]
      ? asyncOptions.value[tempId]
      : []
  return needLabel
    ? map(list, i => {
        return {
          label: i.name,
          value: i.value
        }
      })
    : list
}

const rowAuth = ref()

const save = ({ logic, items, errorMessage }) => {
  if (errorMessage) {
    ElMessage({
      message: errorMessage,
      type: 'error',
      showClose: true
    })
    return
  }
  optionFormData.value.permissionsTree = { logic, items }
  doEditBindColumn()
}

const confirmEditBindColumn = () => {
  if (currentDsType.value === 'es') {
    doEditBindColumn()
    return
  }
  if (!rowAuth.value) {
    doEditBindColumn()
    return
  } else {
    rowAuth.value.submit()
  }
}
const scrollbar = ref()
const scrollbarClick = () => {
  nextTick(() => {
    scrollbar.value?.update()
  })
}
provide('scrollbarClick', scrollbarClick)
</script>

<template>
  <div class="df-dataset-form" v-loading="loading">
    <div class="top">
      <span class="name">
        <el-icon @click="backToMain">
          <Icon name="icon_left_outlined"><icon_left_outlined /></Icon>
        </el-icon>
        <el-form
          v-show="showInput"
          ref="mTitleForm"
          label-position="top"
          :model="formSettings"
          hide-required-asterisk
          @submit.native.prevent
        >
          <el-form-item
            prop="name"
            class="df-name-input"
            :rules="[requiredRule, maxLengthRule(64), minLengthRule(1)]"
          >
            <el-input
              ref="editorName"
              v-model="formSettings.name"
              @blur="handleDfName"
              maxlength="64"
            />
          </el-form-item>
        </el-form>
        <span
          v-show="!showInput"
          @click="handleClick"
          class="dataset-name ellipsis"
          style="margin-left: 12px"
        >
          {{ formSettings.name }}
        </span>
      </span>
      <span class="operate">
        <el-button :disabled="showInput" type="primary" @click="dfSaveAndBack">
          {{ t('data_set.save_and_return') }}
        </el-button>
        <el-button :disabled="showInput" type="primary" @click="dfSave">
          {{ t('commons.save') }}
        </el-button>
      </span>
    </div>

    <div class="container">
      <div class="tools-window-left">
        <el-header class="sub-title-header">{{ t('data_fill.form.component') }} </el-header>
        <div style="display: flex; width: 100%">
          <div style="flex: 1; padding: 8px 4px 8px 8px">
            <draggable
              :list="componentList1"
              :group="group.componentListGroup1"
              :clone="cloneDraggable"
              animation="300"
              ghost-class="ghostClass"
              chosen-class="chosenClass"
              item-key="id"
              :move="onMoveInComponentList"
            >
              <template #item="{ element }">
                <div class="m-item base-component-item" @click="addComponentItem(element)">
                  <el-icon style="margin-right: 8px; font-size: 16px">
                    <Icon>
                      <component :is="iconMap[element.icon]"></component>
                    </Icon>
                  </el-icon>
                  {{ element.typeName }}
                </div>
              </template>
            </draggable>
          </div>
          <div style="flex: 1; padding: 8px 8px 8px 4px">
            <draggable
              :list="componentList2"
              :group="group.componentListGroup2"
              :clone="cloneDraggable"
              animation="300"
              ghost-class="ghostClass"
              chosen-class="chosenClass"
              item-key="id"
              :move="onMoveInComponentList"
            >
              <template #item="{ element }">
                <div class="m-item base-component-item" @click="addComponentItem(element)">
                  <el-icon style="margin-right: 8px; font-size: 16px">
                    <Icon>
                      <component :is="iconMap[element.icon]"></component>
                    </Icon>
                  </el-icon>
                  {{ element.typeName }}
                </div>
              </template>
            </draggable>
          </div>
        </div>
      </div>

      <el-main class="center-main">
        <div
          v-if="!mainLoading"
          style="
            flex: 1;
            background: white;
            display: flex;
            flex-direction: column;
            position: relative;
            overflow-x: hidden;
          "
          @click="lostFocus"
        >
          <div style="width: 100%; min-height: 60px"></div>
          <el-form
            ref="mForm"
            label-position="top"
            hide-required-asterisk
            class="form-drag-form"
            @submit.native.prevent
          >
            <div v-if="formSettings.forms.length === 0" class="drag-placeholder">
              {{ t('common.component.add_component_hint') }}
            </div>

            <draggable
              id="form-drag-place"
              :list="formSettings.forms"
              group="mFormGroup"
              animation="300"
              class="form-drag-class"
              ghost-class="ghostClass"
              chosen-class="chosenClass"
              @add="addComponent"
              item-key="id"
            >
              <template #item="{ element, index }">
                <div
                  class="m-item m-form-item"
                  :class="{ selectedClass: element.id === selectedItemId }"
                  @click.stop="selectItem(element.id)"
                >
                  <div class="m-label-container">
                    <span style="width: unset">
                      {{ element.settings.name }}
                      <span v-if="element.settings.required" class="df-input-require">*</span>
                    </span>
                    <span class="btn-container">
                      <el-tooltip effect="dark" :content="t('common.copy')" placement="top">
                        <div class="btn-item" @click.prevent.stop="copyItem(element, index)">
                          <el-icon style="font-size: 16px">
                            <Icon name="icon_copy_outlined"><icon_copy_outlined /></Icon>
                          </el-icon>
                        </div>
                      </el-tooltip>
                      <el-tooltip effect="dark" :content="t('common.delete')" placement="top">
                        <div class="btn-item" @click.prevent.stop="removeItem(element, index)">
                          <el-icon style="font-size: 16px">
                            <Icon name="icon_delete-trash_outlined"
                              ><icon_deleteTrash_outlined
                            /></Icon>
                          </el-icon>
                        </div>
                      </el-tooltip>
                    </span>
                  </div>
                  <el-form-item prop="value" class="form-item no-margin-bottom">
                    <el-input
                      v-if="element.type === 'input' && element.settings.inputType !== 'number'"
                      :key="element.id + element.settings.inputType"
                      v-model="element.value"
                      :type="element.settings.inputType"
                      :required="element.settings.required"
                      :placeholder="element.settings.placeholder"
                    />
                    <el-input-number
                      v-if="element.type === 'input' && element.settings.inputType === 'number'"
                      :key="element.id + element.settings.inputType"
                      v-model="element.value"
                      :required="element.settings.required"
                      :placeholder="element.settings.placeholder"
                      style="width: 100%"
                      title=""
                      controls-position="right"
                    />
                    <el-input
                      v-else-if="element.type === 'textarea'"
                      :key="element.id + 'textarea'"
                      v-model="element.value"
                      type="textarea"
                      :required="element.settings.required"
                      :placeholder="element.settings.placeholder"
                    />
                    <el-select-v2
                      v-else-if="element.type === 'select'"
                      :key="element.id + 'select'"
                      v-model="element.value"
                      :required="element.settings.required"
                      :placeholder="
                        (element.settings.placeholder ? element.settings.placeholder : '') + ' '
                      "
                      style="width: 100%"
                      filterable
                      :multiple="element.settings.multiple"
                      clearable
                      :options="getOptionList(element, true)"
                      @change="getExtraDetails(element)"
                    >
                    </el-select-v2>
                    <el-radio-group
                      v-else-if="element.type === 'radio'"
                      :key="element.id + 'radio'"
                      v-model="element.value"
                      :required="element.settings.required"
                      style="width: 100%"
                      @change="getExtraDetails(element)"
                    >
                      <el-radio
                        v-for="(x, $index) in getOptionList(element)"
                        :key="$index"
                        :label="x.value"
                        ><span :title="x.name">{{ x.name }}</span>
                      </el-radio>
                    </el-radio-group>
                    <el-checkbox-group
                      v-else-if="element.type === 'checkbox'"
                      :key="element.id + 'checkbox'"
                      v-model="element.value"
                      :required="element.settings.required"
                    >
                      <el-checkbox
                        v-for="(x, $index) in getOptionList(element)"
                        :key="$index"
                        :label="x.value"
                        ><span :title="x.name">{{ x.name }}</span>
                      </el-checkbox>
                    </el-checkbox-group>
                    <el-date-picker
                      v-else-if="element.type === 'date'"
                      :key="element.id + 'date'"
                      v-model="element.value"
                      :required="element.settings.required"
                      :type="element.settings.dateType"
                      :placeholder="element.settings.placeholder"
                      style="width: 100%"
                    />
                    <el-date-picker
                      v-else-if="element.type === 'dateRange'"
                      :key="element.id + 'dateRange'"
                      v-model="element.value"
                      :required="element.settings.required"
                      :type="element.settings.dateType"
                      :range-separator="element.settings.rangeSeparator"
                      :start-placeholder="element.settings.startPlaceholder"
                      :end-placeholder="element.settings.endPlaceholder"
                      style="width: 100%"
                    />
                    <template
                      v-if="
                        element.type === 'radio' ||
                        (element.type === 'select' && !element.settings.multiple)
                      "
                    >
                      <div
                        style="width: 100%; display: flex; padding: 8px; margin-top: 8px"
                        v-if="element.extraDetails?.length > 0"
                      >
                        <div class="df-ex-detail">
                          <div class="df-ex-row">
                            <div
                              class="df-ex-col"
                              :title="
                                element.extraDetails[0].name + ' : ' + element.extraDetails[0].value
                              "
                              v-if="element.extraDetails[0]"
                            >
                              <span class="label-no-warp">{{ element.extraDetails[0].name }}</span>
                              <span class="desc-column">{{ element.extraDetails[0].value }}</span>
                            </div>
                            <div
                              class="df-ex-col"
                              :title="
                                element.extraDetails[1].name + ' : ' + element.extraDetails[1].value
                              "
                              v-if="element.extraDetails[1]"
                            >
                              <span class="label-no-warp">{{ element.extraDetails[1].name }}</span>
                              <span class="desc-column">{{ element.extraDetails[1].value }}</span>
                            </div>
                            <div
                              class="df-ex-col"
                              :title="
                                element.extraDetails[2].name + ' : ' + element.extraDetails[2].value
                              "
                              v-if="element.extraDetails[2]"
                            >
                              <span class="label-no-warp">{{ element.extraDetails[2].name }}</span>
                              <span class="desc-column">{{ element.extraDetails[2].value }}</span>
                            </div>
                          </div>
                          <div class="df-ex-row">
                            <div
                              class="df-ex-col"
                              :title="
                                element.extraDetails[3].name + ' : ' + element.extraDetails[3].value
                              "
                              v-if="element.extraDetails[3]"
                            >
                              <span class="label-no-warp">{{ element.extraDetails[3].name }}</span>
                              <span class="desc-column">{{ element.extraDetails[3].value }}</span>
                            </div>
                            <div
                              class="df-ex-col"
                              :title="
                                element.extraDetails[4].name + ' : ' + element.extraDetails[4].value
                              "
                              v-if="element.extraDetails[4]"
                            >
                              <span class="label-no-warp">{{ element.extraDetails[4].name }}</span>
                              <span class="desc-column">{{ element.extraDetails[4].value }}</span>
                            </div>
                            <div
                              class="df-ex-col"
                              :title="
                                element.extraDetails[5].name + ' : ' + element.extraDetails[5].value
                              "
                              v-if="element.extraDetails[5]"
                            >
                              <span class="label-no-warp">{{ element.extraDetails[5].name }}</span>
                              <span class="desc-column">{{ element.extraDetails[5].value }}</span>
                            </div>
                          </div>
                        </div>
                        <el-button
                          text
                          type="primary"
                          @click="showAllDetails(element.extraDetails)"
                        >
                          {{ t('data_fill.form.show_more_detail') }}
                        </el-button>
                      </div>
                    </template>
                  </el-form-item>
                </div>
              </template>
            </draggable>
          </el-form>
          <div style="width: 100%; min-height: 60px"></div>
        </div>
      </el-main>

      <div
        class="tools-window-right"
        v-if="selectedItemId !== undefined && selectedComponentItem !== undefined"
      >
        <el-header class="sub-title-header">{{ t('data_fill.form.component_setting') }} </el-header>
        <el-main style="height: calc(100vh - 60px - 56px)">
          <el-form
            class="right-form"
            ref="mRightForm"
            :model="selectedComponentItem.settings"
            label-position="top"
            hide-required-asterisk
            @submit.native.prevent
          >
            <el-form-item prop="name" class="form-item" :rules="[requiredRule, maxLengthRule(50)]">
              <template #label>
                {{ t('data_fill.form.title') }}
                <span class="df-input-require">*</span>
              </template>
              <el-input
                v-model="selectedComponentItem.settings.name"
                :placeholder="t('data_fill.form.input_limit_max', [50])"
                @blur="trimName(selectedComponentItem.settings)"
                required
                maxlength="50"
                class="m-right-form"
              />
            </el-form-item>

            <el-form-item
              v-if="selectedComponentItem.type === 'dateRange'"
              prop="rangeSeparator"
              class="form-item"
              :rules="[requiredRule]"
            >
              <template #label>
                {{ t('data_fill.form.range_separator') }}
                <span class="df-input-require">*</span>
              </template>
              <el-select
                v-model="selectedComponentItem.settings.rangeSeparator"
                style="width: 100%"
                required
              >
                <el-option label="-" value="-" />
                <el-option label="~" value="~" />
              </el-select>
            </el-form-item>
            <el-form-item
              v-if="
                selectedComponentItem.type === 'input' ||
                selectedComponentItem.type === 'textarea' ||
                selectedComponentItem.type === 'select' ||
                selectedComponentItem.type === 'date'
              "
              prop="placeholder"
              class="form-item"
              :label="t('data_fill.form.hint')"
              :rules="[maxLengthRule(50)]"
            >
              <el-input
                v-model="selectedComponentItem.settings.placeholder"
                :placeholder="t('data_fill.form.input_limit_max', [50])"
                class="m-right-form"
                maxlength="50"
              />
            </el-form-item>
            <el-form-item
              v-if="selectedComponentItem.type === 'dateRange'"
              prop="startPlaceholder"
              class="form-item"
              :label="t('data_fill.form.start_hint_word')"
              :rules="[maxLengthRule(50)]"
            >
              <el-input
                v-model="selectedComponentItem.settings.startPlaceholder"
                :placeholder="t('data_fill.form.input_limit_max', [50])"
                maxlength="50"
              />
            </el-form-item>
            <el-form-item
              v-if="selectedComponentItem.type === 'dateRange'"
              prop="endPlaceholder"
              class="form-item"
              :label="t('data_fill.form.end_hint_word')"
              :rules="[maxLengthRule(50)]"
            >
              <el-input
                v-model="selectedComponentItem.settings.endPlaceholder"
                :placeholder="t('data_fill.form.input_limit_max', [50])"
                maxlength="50"
              />
            </el-form-item>

            <div class="m-splitter" />

            <el-form-item
              v-if="selectedComponentItem.type === 'input'"
              prop="inputType"
              class="form-item"
              :label="t('data_fill.form.input_type')"
              :rules="[requiredRule]"
            >
              <el-select
                v-model="selectedComponentItem.settings.inputType"
                style="width: 100%"
                required
                @change="selectedComponentItem.settings.mapping.type = undefined"
              >
                <el-option
                  v-for="x in selectedComponentItemInputTypes"
                  :key="x.type"
                  :label="x.name"
                  :value="x.type"
                />
              </el-select>
            </el-form-item>

            <el-form-item
              v-if="
                selectedComponentItem.type === 'date' || selectedComponentItem.type === 'dateRange'
              "
              prop="dateType"
              class="form-item"
              :label="t('data_fill.form.date_type')"
              :rules="[requiredRule]"
            >
              <el-select
                v-model="selectedComponentItem.settings.dateType"
                style="width: 100%"
                required
              >
                <el-option
                  v-for="x in selectedComponentItem.type === 'date' ? dateTypes : dateRangeTypes"
                  :key="x.value"
                  :label="x.name"
                  :value="x.value"
                />
              </el-select>
            </el-form-item>

            <div class="right-check-div">
              <div class="m-label-container" style="margin-bottom: 0">
                <span style="width: unset">
                  {{ t('data_fill.form.check') }}
                </span>
              </div>
              <el-form-item prop="required" class="form-item">
                <el-checkbox v-model="selectedComponentItem.settings.required">
                  {{ t('data_fill.form.set_required') }}
                </el-checkbox>
              </el-form-item>
              <el-form-item
                v-if="selectedComponentItem.type === 'input'"
                prop="unique"
                class="form-item"
              >
                <el-checkbox v-model="selectedComponentItem.settings.unique">
                  {{ t('data_fill.form.set_unique') }}
                </el-checkbox>
              </el-form-item>
              <el-form-item
                v-if="selectedComponentItem.type === 'select'"
                prop="multiple"
                class="form-item"
              >
                <el-checkbox
                  v-model="selectedComponentItem.settings.multiple"
                  :disabled="selectedComponentItem.old"
                  @change="
                    changeSelectMultiple(
                      selectedComponentItem,
                      selectedComponentItem.settings.multiple
                    )
                  "
                >
                  {{ t('data_fill.form.set_multiple') }}
                </el-checkbox>
              </el-form-item>

              <template v-if="selectedComponentItem.type === 'date'">
                <el-form-item prop="enableDefaultTime" class="form-item">
                  <el-checkbox v-model="selectedComponentItem.settings.enableDefaultTime">
                    {{ t('data_fill.form.set_enableDefaultTime') }}
                  </el-checkbox>
                </el-form-item>
                <div
                  v-if="selectedComponentItem.settings.enableDefaultTime"
                  style="padding-left: 24px"
                >
                  <el-form-item prop="enableCurrentTime" class="form-item">
                    <el-radio-group v-model="selectedComponentItem.settings.enableCurrentTime">
                      <el-radio :label="true">
                        {{ t('data_fill.form.currentTime') }}
                      </el-radio>
                      <el-radio :label="false">
                        {{ t('data_fill.form.defaultTime') }}
                      </el-radio>
                    </el-radio-group>
                  </el-form-item>
                  <el-form-item
                    v-if="!selectedComponentItem.settings.enableCurrentTime"
                    style="margin-top: 8px"
                    prop="defaultTime"
                    class="form-item"
                    :rules="[requiredRule]"
                  >
                    <el-date-picker
                      v-model="selectedComponentItem.settings.defaultTime"
                      :type="selectedComponentItem.settings.dateType"
                      style="width: 100%"
                      value-format="x"
                    />
                  </el-form-item>
                </div>
              </template>
            </div>

            <div
              v-if="
                selectedComponentItem.type === 'select' ||
                selectedComponentItem.type === 'radio' ||
                selectedComponentItem.type === 'checkbox'
              "
            >
              <div class="m-splitter" />

              <el-form-item
                prop="optionSourceType"
                :label="t('data_fill.form.option_value')"
                class="form-item no-margin-bottom"
              >
                <el-radio-group
                  v-model="selectedComponentItem.settings.optionSourceType"
                  @change="
                    onOptionSourceTypeChange(
                      selectedComponentItem.settings.optionSourceType,
                      selectedComponentItem.settings
                    )
                  "
                >
                  <el-radio :label="1">
                    {{ t('data_fill.form.custom') }}
                  </el-radio>
                  <el-radio :label="2">
                    {{ t('data_fill.form.use_datasource') }}
                  </el-radio>
                </el-radio-group>
              </el-form-item>

              <template v-if="selectedComponentItem.settings.optionSourceType === 1">
                <el-button type="text" @click="addOption(selectedComponentItem.settings.options)"
                  >+ {{ t('data_fill.form.add_option') }}
                </el-button>

                <div
                  v-for="(x, $index) in selectedComponentItem.settings.options"
                  :key="$index"
                  class="option-list-div"
                >
                  <el-form-item
                    :prop="'options[' + $index + '].value'"
                    class="form-item no-margin-bottom"
                    style="width: 100%"
                    :rules="[
                      requiredRule,
                      duplicateOptionRule,
                      maxLengthRule(50),
                      minLengthRule(1)
                    ]"
                  >
                    <el-input
                      v-model="x.value"
                      required
                      minlength="1"
                      maxlength="50"
                      @change="onOptionValueChange(x, x.value)"
                    />
                  </el-form-item>
                  <div
                    class="btn-item"
                    @click.prevent.stop="
                      removeOption(selectedComponentItem.settings.options, $index)
                    "
                  >
                    <el-icon style="font-size: 16px">
                      <Icon name="icon_delete-trash_outlined">
                        <icon_deleteTrash_outlined />
                      </Icon>
                    </el-icon>
                  </div>
                </div>
              </template>
              <template v-else>
                <el-button
                  v-if="!selectedComponentItem.settings.optionColumn"
                  type="text"
                  @click="
                    openEditBindColumn(
                      {},
                      selectedComponentItem.type === 'radio' ||
                        (selectedComponentItem.type === 'select' &&
                          !selectedComponentItem.settings.multiple)
                    )
                  "
                  >+ {{ t('data_fill.form.bind_column') }}
                </el-button>
                <div
                  v-else
                  style="display: flex; flex-direction: row; align-items: center; font-size: 14px"
                >
                  <div style="width: 28px" />
                  <div
                    style="flex: 2; overflow: hidden; text-overflow: ellipsis"
                    :title="
                      selectedComponentItem.settings.optionTable +
                      ' (' +
                      selectedComponentItem.settings.optionColumn +
                      ')'
                    "
                  >
                    {{ selectedComponentItem.settings.optionTable }} ({{
                      selectedComponentItem.settings.optionColumn
                    }})
                  </div>
                  <div style="flex: 1; color: #8f959e">
                    {{ t('data_fill.form.bind_complete') }}
                  </div>
                  <el-button
                    text
                    @click="
                      openEditBindColumn(
                        selectedComponentItem.settings,
                        selectedComponentItem.type === 'radio' ||
                          (selectedComponentItem.type === 'select' &&
                            !selectedComponentItem.settings.multiple)
                      )
                    "
                    >{{ t('common.edit') }}
                  </el-button>
                </div>
              </template>
            </div>
          </el-form>
        </el-main>
      </div>
    </div>
  </div>
  <el-drawer
    :title="t('data_fill.form.save_form')"
    :close-on-click-modal="false"
    size="calc(100% - 100px)"
    v-model="showDrawer"
    direction="btt"
    :show-close="false"
    :with-header="false"
    append-to-body
    modal-class="save-form-edit-drawer"
  >
    <data-filling-form-save
      v-if="showDrawer"
      :is-edit="isEdit"
      :disable-create-index="disableCreateIndex"
      :datasource-list="allDatasourceList"
      @refresh-datasource="getDatasourceList"
      :ds-loading="dsLoading"
      :form="formSettings"
      :show-drawer="showDrawer"
      :will-back="enableBack"
      @close="closeDrawer"
      @saved="finish"
    />
  </el-drawer>

  <el-dialog
    :append-to-body="true"
    :title="t('data_fill.form.use_datasource')"
    v-model="showEditBindColumn"
    destroy-on-close
    :show-close="true"
    width="600px"
    class="m-dialog select-content-dialog"
  >
    <el-main v-loading="loading" direction="vertical">
      <el-scrollbar :max-height="'calc(100vh - 200px)'">
        <el-form
          ref="optionForm"
          class="m-form"
          :model="optionFormData"
          label-position="top"
          @submit.native.prevent
        >
          <el-form-item
            prop="optionDatasource"
            class="form-item hide-asterisk"
            :rules="[requiredRule, checkValidDatasourceRule]"
          >
            <template #label>
              <div class="label-row">
                <div>
                  {{ t('data_fill.form.datasource') }}
                  <span class="asterisk">*</span>
                </div>
                <el-button type="primary" text @click="getDatasourceList" :disabled="dsLoading">
                  {{ t('commons.refresh') }}
                </el-button>
              </div>
            </template>
            <el-select
              v-model="optionFormData.optionDatasource"
              required
              style="width: 100%"
              filterable
              @change="onDataSourceChange"
              :loading="dsLoading"
            >
              <el-option-group v-for="(x, $index) in datasourceList" :key="$index" :label="x.name">
                <el-option
                  v-for="d in x.options"
                  :key="d.id"
                  :value="d.id"
                  :label="d.name"
                  :disabled="d.status === 'Error'"
                >
                  <div
                    :title="
                      d.name + ' ' + (d.status === 'Error' ? t('data_set.invalid_data_source') : '')
                    "
                    style="display: flex; align-items: center"
                  >
                    <span
                      style="
                        display: inline-block;
                        overflow: hidden;
                        text-overflow: ellipsis;
                        white-space: nowrap;
                      "
                      :style="{
                        maxWidth: d.status === 'Error' ? '400px' : '460px'
                      }"
                    >
                      {{ d.name }}
                    </span>
                    <span
                      style="padding-left: 14px; font-size: 10px; color: red"
                      v-if="d.status === 'Error'"
                    >
                      {{ t('data_set.invalid_data_source') }}
                    </span>
                  </div>
                </el-option>
              </el-option-group>
            </el-select>
          </el-form-item>
          <el-form-item
            prop="optionTable"
            class="form-item"
            :label="t('data_fill.form.table')"
            :rules="[requiredRule]"
          >
            <el-select-v2
              v-model="optionFormData.optionTable"
              :options="tableList"
              filterable
              :placeholder="t('common.please_select')"
              style="width: 100%"
              @change="onTableChange(optionFormData.optionDatasource, optionFormData.optionTable)"
            />
          </el-form-item>
          <div
            class="tree-cont"
            v-if="
              optionFormData.optionDatasource &&
              optionFormData.optionTable &&
              currentDsType !== 'es'
            "
          >
            <el-scrollbar ref="scrollbar" max-height="500px">
              <RowAuth @save="save" ref="rowAuth" />
            </el-scrollbar>
          </div>
          <el-form-item
            prop="optionColumn"
            class="form-item"
            :label="t('data_fill.form.column_name')"
            :rules="[requiredRule]"
          >
            <el-select
              v-model="optionFormData.optionColumn"
              required
              style="width: 100%"
              filterable
            >
              <el-option
                v-for="d in columnList"
                :key="d.fieldName"
                :value="d.fieldName"
                :label="d.fieldName"
                >{{ d.fieldName }}
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item
            prop="optionOrder"
            class="form-item"
            :label="t('data_fill.form.order')"
            :rules="[requiredRule]"
          >
            <el-radio-group v-model="optionFormData.optionOrder" required style="width: 100%">
              <el-radio label="asc">{{ t('data_fill.form.order_asc') }}</el-radio>
              <el-radio label="desc">{{ t('data_fill.form.order_desc') }} </el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item
            v-if="optionFormData.enableExtraColumns"
            prop="extraColumns"
            class="form-item"
            :label="t('data_fill.form.detail_columns')"
          >
            <el-row
              :gutter="8"
              v-if="extraColumns.length > 0"
              style="align-items: center; width: 100%"
            >
              <el-col :span="19"> {{ extraColumnsStr }}</el-col>
              <el-col :span="3" style="color: var(--ed-color-info)">
                {{ t('auth.added') }}
              </el-col>
              <el-col :span="1">
                <el-button
                  text
                  secondary
                  type="info"
                  style="color: var(--ed-color-info)"
                  @click="deleteAllExtraColumns"
                >
                  <template #icon>
                    <Icon name="icon_delete-trash_outlined">
                      <icon_deleteTrash_outlined />
                    </Icon>
                  </template>
                </el-button>
              </el-col>
              <el-col :span="1">
                <el-button text @click="openSelectDetailColumns">
                  <template #icon>
                    <Icon name="icon_edit_outlined">
                      <icon_edit_outlined />
                    </Icon>
                  </template>
                </el-button>
              </el-col>
            </el-row>
            <el-button v-else text @click="openSelectDetailColumns"
              >+{{ t('data_fill.form.add_detail_columns') }}
            </el-button>
          </el-form-item>
        </el-form>
      </el-scrollbar>
    </el-main>
    <el-footer class="de-footer">
      <el-button @click="closeEditBindColumn">{{ t('common.cancel') }} </el-button>
      <el-button type="primary" @click="confirmEditBindColumn">
        {{ t('commons.confirm') }}
      </el-button>
    </el-footer>
  </el-dialog>

  <SelectDetailColumns ref="selectDetailColumnsRef" @close="onSelectDetailColumnsClose" />

  <MoreDetailColumns v-model:show="showMoreDetails" :details="moreDetails" />
</template>

<style lang="less" scoped>
@import '@/style/mixin.less';

.m-dialog {
  .ed-main {
    padding: unset;
  }

  .tree-cont {
    min-height: 67px;
    width: 100%;
    padding: 16px;
    border-radius: 6px;
    border: 1px solid var(--deBorderBase, #dcdfe6);
    margin-bottom: 16px;
  }

  .de-footer {
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: flex-end;
    padding: unset;
    height: unset;
  }
}

.ed-table {
  --ed-table-header-bg-color: #f5f6f7;
}

.df-input-require {
  color: red;
  margin-left: 2px;
}

.hide-asterisk {
  :deep(.ed-form-item__label) {
    width: 100%;
    height: auto;

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

.df-dataset-form {
  color: #1f2329;

  .df-name-input {
    margin-bottom: 0;

    :deep(.ed-form-item__error) {
      padding-top: unset;
    }

    :deep(.ed-input__wrapper) {
      background-color: #050e21;
      padding: 0 4px;
    }
  }

  :deep(.ed-radio) {
    max-width: 100%;
  }

  :deep(.ed-checkbox-group) {
    max-width: 100%;
  }

  :deep(.ed-checkbox) {
    max-width: 100%;
  }

  :deep(.ed-radio__label) {
    font-weight: normal;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  :deep(.ed-checkbox__label) {
    font-weight: normal;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .top {
    height: 56px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 24px;
    background: #050e21;
    box-shadow: 0px 2px 4px 0px rgba(31, 35, 41, 0.12);

    .name {
      color: #fff;
      font-family: var(--de-custom_font, 'PingFang');
      font-size: 16px;
      font-weight: 400;
      display: flex;
      align-items: center;
      width: 50%;
      position: relative;

      .ed-form-item__error {
        top: 19px !important;
        left: 16px !important;
      }

      .dataset-name {
        cursor: pointer;
        width: 294px;
        height: 24px;
        line-height: 24px;
      }

      .ed-input {
        width: 302px;
        line-height: 24px;
        height: 24px;

        :deep(.ed-input__inner) {
          color: #fff;
          font-size: 16px;
        }
      }

      i {
        cursor: pointer;
      }
    }
  }

  .tools-window-left {
    width: 280px;
    border-right: 1px rgba(31, 35, 41, 0.15) solid;
  }

  .sub-title-header {
    border-bottom: 1px solid #e6e6e6;

    display: flex;
    flex-direction: row;
    align-items: center;
  }

  .no-margin-bottom {
    margin-bottom: 0;
  }

  .tools-window-right {
    width: 320px;
    background-color: #ffffff;
    border-left: 1px rgba(31, 35, 41, 0.15) solid;

    .form-item {
      margin-bottom: 16px;

      .el-form-item__label {
        line-height: 24px;
        font-size: 14px;
        font-weight: normal;
        color: rgba(31, 35, 41, 1);
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

    .right-check-div {
      .form-item {
        margin-bottom: 0;
      }

      .form-item:last-child {
        margin-bottom: 16px;
      }
    }

    .option-list-div {
      display: flex;
      flex-direction: row;
      align-items: center;

      margin-bottom: 8px;

      .btn-item {
        display: flex;
        flex-direction: row;
        align-items: center;
        justify-content: center;

        width: 24px;
        height: 24px;

        margin-left: 8px;

        border-radius: 6px;

        cursor: pointer;
      }

      .btn-item:hover {
        background: rgba(31, 35, 41, 0.1);
      }
    }
  }

  .center-main {
    background-color: rgb(247, 248, 250);
    height: calc(100vh - 56px);
    flex: 1;
    padding: 20px 20px 0 20px;

    display: flex;

    .drag-placeholder {
      height: 68px;
      background: rgba(245, 246, 247, 1);
      border: 1px dashed rgba(187, 191, 196, 1);
      display: flex;
      border-radius: 6px;
      flex-direction: row;
      align-items: center;
      justify-content: center;
      cursor: default;

      font-weight: normal;
      font-size: 14px;

      position: absolute;
      width: calc(100% - 120px);
      top: 88px;
      left: 50%;
      transform: translate(-50%, 0);
    }
  }

  .container {
    display: flex;
    width: 100%;
    height: calc(100vh - 56px);
    position: relative;

    .form-drag-form {
      flex: 1;
      display: flex;
      min-height: 32px;
      width: 100%;

      z-index: 1;
      flex-direction: column;

      padding: 0 60px;
    }

    .form-drag-class {
      flex: 1;
      display: flex;
      min-height: 32px;
      width: 100%;
      flex-direction: column;

      span:only-child {
        width: 100%;
      }

      .ghostClass {
        min-height: 68px;
        margin-top: 0 !important;
      }
    }

    .ghostClass {
      opacity: 1 !important;
      background-color: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1)) !important;
      border: 1px dashed var(--ed-color-primary, rgba(51, 112, 255, 1)) !important;
    }

    .chosenClass {
      background-color: #f5f6f7 !important;
      opacity: 1;
    }

    .selectedClass {
      background-color: #f5f6f7 !important;
      opacity: 1;
      border: 1px solid var(--ed-color-primary, rgba(51, 112, 255, 1)) !important;
    }

    .dragClass {
      opacity: 1 !important;
      box-shadow: none !important;
      outline: none !important;
      background-image: none !important;
    }

    .m-item {
      width: 100%;
      border: solid 1px #eee;
      background-color: #f5f6f7;
      border-radius: 6px;
    }

    .base-component-item {
      margin: 8px 0;
      cursor: pointer;
      height: 32px;

      padding-left: 8px;
      padding-right: 8px;

      font-weight: normal;
      font-size: 14px;
      line-height: 20px;

      display: flex;
      flex-direction: row;
      align-items: center;
    }

    .base-component-item:hover {
      background: rgba(31, 35, 41, 0.1);
    }

    .m-form-item {
      margin-bottom: 10px;
      border-radius: 6px;

      border: solid 1px transparent;
      background-color: unset;

      padding: 8px 20px;

      :deep(.ed-date-editor) {
        .ed-input__wrapper {
          width: 100%;
        }
      }
    }

    .m-form-item:hover {
      background-color: #f5f6f7;
      border: solid 1px #eee;
      cursor: pointer;

      .m-label-container {
        .btn-container {
          visibility: visible;
        }
      }
    }

    .m-title {
      margin: 40px 80px 20px;

      height: 28px;

      font-weight: 500;
      font-size: 20px;
      line-height: 28px;

      white-space: nowrap;
      text-overflow: ellipsis;
    }

    .m-label-container {
      width: 100%;
      display: flex;
      flex-direction: row;
      align-items: center;
      justify-content: space-between;

      font-weight: normal;
      font-size: 14px;
      line-height: 22px;

      margin-bottom: 8px;

      .btn-container {
        display: flex;
        flex-direction: row;
        align-items: center;

        visibility: hidden;

        .btn-item {
          display: flex;
          flex-direction: row;
          align-items: center;
          justify-content: center;

          width: 24px;
          height: 24px;

          margin-left: 8px;

          border-radius: 6px;

          cursor: pointer;
        }

        .btn-item:first-child {
          margin-left: unset;
        }

        .btn-item:hover {
          background: rgba(31, 35, 41, 0.1);
        }
      }
    }

    .drag-left {
      position: absolute;
      height: calc(100vh - 56px);
      width: 4px;
      top: 0;
      z-index: 2;
      cursor: col-resize;

      &.is-dragging::after,
      &:hover::after {
        width: 1px;
        height: 100%;
        content: '';
        position: absolute;
        left: -1px;
        top: 0;
        background: var(--ed-color-primary);
      }
    }

    .arrow-right {
      position: absolute;
      top: 15px;
      z-index: 2;
      cursor: pointer;
      margin: 0;
      display: flex;
      align-items: center;
      left: 0;
      height: 24px;
      width: 20px;
      box-shadow: 0px 4px 8px rgba(0, 0, 0, 0.1);
      border: 1px solid var(--deCardStrokeColor, #dee0e3);
      border-top-right-radius: 13px;
      border-bottom-right-radius: 13px;
      background: #fff;
      font-size: 12px;

      .ed-icon {
        margin-left: 2px;
      }
    }

    .table-list {
      .list-item_primary {
        padding: 8px;
      }

      .table-list-top {
        padding: 16px;
        padding-bottom: 0;
      }

      height: 100%;
      width: 240px;
      padding-bottom: 16px;

      font-family: var(--de-custom_font, 'PingFang');
      border-right: 1px solid rgba(31, 35, 41, 0.15);

      .select-ds {
        font-size: 14px;
        font-weight: 500;
        display: flex;
        justify-content: space-between;
        align-items: center;
        color: var(--deTextPrimary, #1f2329);
        position: relative;

        i {
          cursor: pointer;
          font-size: 12px;
          color: var(--deTextPlaceholder, rgba(31, 35, 41, 0.15));
        }

        .left-outlined {
          position: absolute;
          font-size: 12px;
          right: -30px;
          top: -5px;
          height: 24px;
          border: 1px solid #dee0e3;
          width: 24px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          background: #fff;
          box-shadow: 0px 5px 10px 0px #1f23291a;
          z-index: 10;
        }
      }

      .table-num {
        .num {
          display: flex;
          align-items: center;
          font-weight: 400;
          font-size: 14px;
          color: #646a73;

          .ed-icon {
            margin-right: 5.33px;
          }
        }

        i {
          cursor: auto;
          font-size: 16px;
          color: var(--deTextPlaceholder, #646a73);
        }
      }

      .search {
        margin: 12px 0;
      }

      .ds-list {
        margin: 12px 0 24px 0;
        width: 100%;
      }

      .table-checkbox-list {
        height: calc(100% - 190px);
        overflow-y: auto;
        padding: 0 8px;

        .not-allow {
          cursor: not-allowed;
          color: var(--deTextDisable, #bbbfc4);
        }
      }
    }
  }

  .dataset-db {
    display: flex;

    .drag-right {
      height: calc(100vh - 56px);

      .different-datasource {
        height: 40px;
        width: 100%;
        background: #ffe7cc;
        color: #1f2329;
        font-size: 14px;
        font-weight: 400;
        line-height: 22px;
        display: flex;
        align-items: center;
        padding: 0 16px;

        .ed-icon {
          font-size: 16px;
          margin-right: 8px;
        }
      }

      .sql-result {
        font-family: var(--de-custom_font, 'PingFang');
        font-size: 14px;
        overflow-y: auto;
        box-sizing: border-box;

        :deep(.ed-tabs) {
          position: relative;
          z-index: 4;
        }

        .sql-title {
          user-select: none;
          height: 10px;
          position: relative;
          z-index: 5;
          color: var(--deTextPrimary, #1f2329);

          .field-data {
            position: absolute;
            right: 24px;
            top: 13px;
            width: 50%;
            z-index: 2;
            text-align: right;
          }

          .drag {
            position: absolute;
            top: 4px;
            left: 0;
            height: 7px;
            width: 100%;
            cursor: row-resize;

            &::after {
              content: '';
              height: 7px;
              width: 100px;
              border-radius: 3.5px;
              position: absolute;
              left: 50%;
              top: 0;
              transform: translateX(-50%);
              background: rgba(31, 35, 41, 0.1);
            }
          }
        }

        .padding-24 {
          .border-bottom-tab(24px);

          :deep(.ed-tabs__header::after) {
            display: none;
          }
        }

        .table-preview {
          height: calc(100% - 56px);
          box-sizing: border-box;

          .preview-data {
            float: right;
            height: 100%;
            width: calc(100% - 260px);

            :deep(.ed-table-v2__header-cell) {
              background-color: #f5f6f7 !important;
            }

            :deep(.header-cell) {
              border-top: none;
            }
          }

          .preview-field {
            float: left;
            width: 260px;
            height: 100%;
            border-right: 1px solid rgba(31, 35, 41, 0.15);
            position: relative;

            :deep(.ed-tree-node__content) {
              border-radius: 6px;

              &:hover {
                background: rgba(31, 35, 41, 0.1);
              }
            }

            :deep(.ed-tree-node.is-current > .ed-tree-node__content:not(.is-menu):after) {
              display: none;
            }

            .custom-tree-node {
              width: calc(100% - 32px);
              display: flex;
              align-items: center;
              padding-right: 8px;
              box-sizing: content-box;

              .label-tooltip {
                margin-left: 5.33px;
                width: 70%;
                overflow: hidden;
                white-space: nowrap;
                text-overflow: ellipsis;
              }

              .operate {
                margin-left: auto;
                position: relative;
                z-index: 5;
              }
            }

            .field-d,
            .field-q {
              padding: 0 8px;
              position: relative;
              height: 49px;

              &.open {
                height: 50%;
              }

              .title {
                cursor: pointer;
                position: sticky;
                margin: 1px;
                top: 1px;
                height: 49px;
                font-family: var(--de-custom_font, 'PingFang');
                font-style: normal;
                font-weight: 500;
                font-size: 14px;
                line-height: 22px;
                color: #1f2329;
                display: flex;
                align-items: center;
                z-index: 10;
                background: #fff;

                .add {
                  margin-left: auto;
                }

                i {
                  color: #646a73;
                }

                .expand {
                  font-size: 10px;
                }

                &.expanded {
                  .expand {
                    transform: rotate(90deg);
                  }
                }
              }

              overflow-y: auto;
            }

            .field-d {
              max-height: calc(100% - 50px);
              border-bottom: 1px solid rgba(31, 35, 41, 0.15);
            }
          }
        }
      }
    }
  }
}

.icon-color {
  color: #646a73;
}

.ed-button.is-secondary.is-disabled {
  color: #bbbfc4 !important;
  border-color: #bbbfc4 !important;
}

.father .child {
  visibility: hidden;
}

.father:hover .child {
  visibility: visible;
}

.manage-container {
  padding: 12px 24px 0;
  flex: 1;
  overflow: auto;
}

.right-form {
  :deep(.ed-date-editor) {
    .ed-input__wrapper {
      width: 100%;
    }
  }
}

.style-collapse {
  :deep(.ed-collapse-item__header),
  :deep(.ed-collapse-item__wrap) {
    border-bottom: none !important;
  }

  :deep(.ed-collapse-item__content) {
    padding: 0 !important;
  }

  &.data-tab-collapse {
    border-bottom: none;
    border-top: 1px solid var(--ed-collapse-border-color);

    :deep(.ed-collapse-item.ed-collapse--dark .ed-collapse-item__wrap) {
      background-color: #1a1a1a;
    }

    :deep(.ed-collapse-item__wrap) {
      border-top: none !important;
    }

    :deep(.ed-collapse-item__content) {
      padding: 0 !important;
      border-top: none !important;
    }

    :deep(.ed-collapse-item__header) {
      background-color: transparent;
      border-bottom: none !important;
    }
  }
}

.column-style {
  display: flex;
  align-items: center;
}

.select-svg-icon {
  position: absolute;
  left: 24px;
  top: 50%;
  height: 14px;
  transform: translateY(-50%);
  line-height: 14px;
}

.cascader-panel {
  .ed-cascader-node__label {
    display: flex;
    align-items: center;

    .ed-icon {
      margin-right: 5px;
    }
  }
}

.batch-operate {
  width: 100%;
  height: 64px;
  padding: 0 24px;
  z-index: 2;
  box-shadow: 0px -2px 4px rgba(31, 35, 41, 0.08);

  .select-svg-icon {
    left: 11px;
  }

  .flex-align-center {
    white-space: nowrap;

    .num {
      margin: 0 4px;
    }

    .is-text {
      margin-left: 16px;
    }
  }

  .cascader-batch {
    position: relative;
    margin-left: 30%;
    width: 176px;
  }
}

.batch-area {
  display: flex;
  flex-direction: column;
  height: calc(100% - 55px);
}

.dimension-manage-header {
  :deep(.ed-collapse-item__header) {
    background: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1));
  }
}

.quota-manage-header {
  :deep(.ed-collapse-item__header) {
    background: #e6f7f5;
  }
}

.manage-header {
  :deep(.ed-collapse-item__header) {
    height: 30px;
  }

  :deep(.ed-table th.ed-table__cell) {
    background: #f5f6f7;
  }
}

:deep(.ed-form-item__error) {
  z-index: 999;
}
</style>

<style lang="less">
.select-content-dialog {
  overflow: hidden;
}
.select-type {
  .ed-input__wrapper {
    padding-left: 32px;
  }
}

.green-color {
  color: #04b49c;
}

.ed-select-dropdown__item {
  display: flex;
  align-items: center;

  .ed-icon {
    font-size: 14px;
    margin-right: 5.25px;
  }
}

.tree-select-ds_popper {
  .ed-tree-node.is-current > .ed-tree-node__content:not(.is-menu):after {
    display: none !important;
  }

  .flex-align-center {
    padding-right: 15px;
  }
}

.calc-field-edit-dialog {
  .ed-dialog__footer {
    padding-top: 24px;
    border: 1px solid rgba(31, 35, 41, 0.15);
  }
}

.save-form-edit-drawer {
  .ed-drawer__body {
    padding: 0;
  }
}

.df-ex-detail {
  flex: 1;
  width: 0;
  display: flex;
  flex-direction: column;

  .df-ex-row {
    flex: 1;
    display: flex;
    flex-direction: row;

    .df-ex-col {
      flex: 1;
      width: 0;
      display: flex;
      padding-right: 8px;

      .label-no-warp {
        white-space: nowrap;
        width: unset;
        overflow: hidden;
        text-overflow: ellipsis;
        min-width: unset !important;
        max-width: 50% !important;
        color: var(--ed-color-info);
        padding-right: 4px;
      }

      .desc-column {
        flex: 1;
        max-height: 64px;
        //white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }
  }
}
</style>
