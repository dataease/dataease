<script lang="tsx" setup>
import { ref, nextTick, reactive, shallowRef, computed, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { ElIcon, ElMessageBox, ElMessage } from 'element-plus-secondary'
import type { Action } from 'element-plus-secondary'
import FieldMore from './FieldMore.vue'
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
import { Icon } from '@/components/icon-custom'
import CalcFieldEdit from './CalcFieldEdit.vue'
import { useRoute, useRouter } from 'vue-router'
import UnionEdit from './UnionEdit.vue'
import type { FormInstance } from 'element-plus-secondary'
import CreatDsGroup from './CreatDsGroup.vue'
import { guid, getFieldName, timeTypes } from './util'
import { fieldType } from '@/utils/attr'
import {
  getDatasourceList,
  getTables,
  getPreviewData,
  getDatasetDetails,
  saveDatasetTree
} from '@/api/dataset'
import type { Table } from '@/api/dataset'
import DatasetUnion from './DatasetUnion.vue'
import { cloneDeep, debounce } from 'lodash-es'
import { useDraggable } from '@vueuse/core'

interface DragEvent extends MouseEvent {
  dataTransfer: DataTransfer
}
export interface Position {
  x: number
  y: number
}

export type PointerType = 'mouse' | 'touch' | 'pen'

export interface DataSource {
  id: string
  name: string
  children?: DataSource[]
}

interface Field {
  fieldShortName: string
  name: string
  dataeaseName: string
  originName: string
  deType: number
}

const { t } = useI18n()
const route = useRoute()
const { push } = useRouter()
const creatDsFolder = ref()
const editCalcField = ref(false)
const calcEdit = ref()
const editUnion = ref(false)
const datasetDrag = ref()
const datasetName = ref('未命名数据集')
const tabActive = ref('preview')
const originName = ref('')
const activeName = ref('')
const dataSource = ref('')
const searchTable = ref('')
const showInput = ref(false)
const dsLoading = ref(false)
const LeftWidth = ref(240)
const offsetX = ref(0)
const offsetY = ref(0)
const showLeft = ref(true)
const maskShow = ref(false)
const loading = ref(false)
const updateCustomTime = ref(false)
const editerName = ref()
const currentField = ref({
  dateFormat: '',
  id: '',
  dateFormatType: '',
  name: '',
  idArr: []
})

const fieldTypes = index => {
  return [
    t('dataset.text'),
    t('dataset.time'),
    t('dataset.value'),
    t('dataset.value') + '(' + t('dataset.float') + ')',
    t('dataset.value'),
    t('dataset.location')
  ][index]
}

const fieldOptions = [
  { label: t('dataset.text'), value: 0 },
  {
    label: t('dataset.time'),
    value: 1,
    children: [
      {
        value: 'yyyy-MM-dd',
        label: 'yyyy-MM-dd'
      },
      {
        value: 'yyyy/MM/dd',
        label: 'yyyy/MM/dd'
      },
      {
        value: 'yyyyMMdd',
        label: 'yyyyMMdd'
      },
      {
        value: 'yyyy-MM-dd HH:mm:ss',
        label: 'yyyy-MM-dd HH:mm:ss'
      },
      {
        value: 'yyyy/MM/dd HH:mm:ss',
        label: 'yyyy/MM/dd HH:mm:ss'
      },
      {
        value: 'yyyyMMdd HH:mm:ss',
        label: 'yyyyMMdd HH:mm:ss'
      },
      {
        value: 'custom',
        label: t('visualization.custom')
      }
    ]
  },
  { label: t('dataset.location'), value: 5 },
  { label: t('dataset.value'), value: 2 },
  {
    label: t('dataset.value') + '(' + t('dataset.float') + ')',
    value: 3
  }
]

const fieldOptionsText = [
  { label: t('dataset.text'), value: 0 },
  {
    label: t('dataset.time'),
    value: 1
  },
  { label: t('dataset.location'), value: 5 },
  { label: t('dataset.value'), value: 2 },
  {
    label: t('dataset.value') + '(' + t('dataset.float') + ')',
    value: 3
  }
]

const ruleFormRef = ref<FormInstance>()
const ruleFormFieldRef = ref<FormInstance>()

const rules = {
  name: [{ required: true, message: '自定义时间格式不能为空', trigger: 'blur' }]
}

const fieldRules = {
  name: [{ required: true, message: t('dataset.input_edit_name'), trigger: 'blur' }]
}

const sqlNode = reactive<Table>({
  datasourceId: '',
  name: '',
  tableName: '自定义SQL',
  type: 'sql'
})

let nodeInfo = {
  id: '',
  pid: '',
  name: ''
}

const defaultProps = {
  children: 'children',
  label: 'label'
}
const dragHeight = ref(260)

const previewHeight = ref(0)
const calcEle = () => {
  previewHeight.value = (document.querySelector('.table-preview') as HTMLDivElement).offsetHeight
}

const getDragHeight = debounce(calcEle, 1000)

watch(
  () => dragHeight.value,
  () => {
    getDragHeight()
  },
  { immediate: true }
)

const fieldDHeight = computed(() => {
  const h = y.value - dragHeight.value - 102
  if (h < 53) {
    return 53
  }
  return h > previewHeight.value - 50 ? previewHeight.value - 50 : h
})

const dragVerticalTop = computed(() => {
  const h = y.value - dragHeight.value - 106
  if (h < 50) {
    return 50
  }
  return h > previewHeight.value - 53 ? previewHeight.value - 53 : h
})

let tableList = []

const getDsName = (id: string) => {
  return (state.dataSourceList.find(ele => ele.id === id) || {}).name
}

const pushDataset = () => {
  push({
    name: 'dataset',
    params: {
      id: nodeInfo.id
    }
  })
}

const backToMain = () => {
  if (!nodeInfo.id) {
    ElMessageBox.confirm('数据集未保存,确认退出吗?', {
      confirmButtonText: t('dataset.confirm'),
      cancelButtonText: t('common.cancel'),
      showCancelButton: true,
      confirmButtonType: 'primary',
      type: 'warning',
      autofocus: false,
      showClose: false
    }).then(() => {
      pushDataset()
    })
  } else {
    pushDataset()
  }
}

const closeCustomTime = () => {
  if (!!currentField.value.idArr.length) {
    const { idArr } = currentField.value
    allfields.value.forEach(ele => {
      if (idArr.includes(ele.id)) {
        Object.assign(ele, { deTypeArr: [1, 'custom'] })
      }
    })
    delete currentField.value.name
    recoverSelection()
  } else {
    dimensions.value.concat(quota.value).some(ele => {
      if (ele.id === currentField.value.id) {
        delete currentField.value.name
        Object.assign(ele, currentField.value)
        return true
      }
      return false
    })
  }
  currentField.value.idArr = []
  currentField.value.id = ''
  updateCustomTime.value = false
}

const confirmCustomTime = () => {
  if (!!currentField.value.idArr.length) {
    const { name, idArr } = currentField.value
    allfields.value.forEach(ele => {
      if (idArr.includes(ele.id)) {
        Object.assign(ele, { dateFormat: name, deTypeArr: [1, 'custom'] })
      }
    })
    delete currentField.value.name
    recoverSelection()
  } else {
    ruleFormRef.value.validate(valid => {
      if (valid) {
        dimensions.value.concat(quota.value).some(ele => {
          if (ele.id === currentField.value.id) {
            ele.dateFormat = currentField.value.name
            return true
          }
          return false
        })
        updateCustomTime.value = false
      }
    })
  }
  updateCustomTime.value = false
}

watch(searchTable, val => {
  state.tableData = tableList.filter(ele => ele.name.includes(val))
})
const editeSave = () => {
  const union = []
  loading.value = true
  dfsNodeList(union, datasetDrag.value.nodeList)
  saveDatasetTree({
    ...nodeInfo,
    name: datasetName.value,
    union,
    allFields: allfields.value,
    nodeType: 'dataset'
  })
    .then(() => {
      ElMessage.success('保存成功')
    })
    .finally(() => {
      loading.value = false
    })
}

const handleFieldMore = (ele, type) => {
  if (tabActive.value === 'manage') {
    dimensionsSelection.value = dimensionsTable.value.getSelectionRows().map(ele => ele.id)
    quotaSelection.value = quotaTable.value.getSelectionRows().map(ele => ele.id)
  }
  const arr = ['text', 'time', 'value', 'float', 'value', 'location']
  if (arr.includes(type as string)) {
    ele.deType = arr.indexOf(type)
    ele.dateFormat = ''
    return
  }
  if (timeTypes.includes(type as string)) {
    currentField.value.dateFormat = ele.dateFormat
    currentField.value.dateFormatType = ele.dateFormatType

    ele.deType = 1
    ele.dateFormatType = type
    ele.dateFormat = type
  }
  switch (type) {
    case 'copy':
      copyField(ele)
      break
    case 'delete':
      deleteField(ele)
      break
    case 'translate':
      dqTrans(ele.id)
      break
    case 'editor':
      editField(ele)
      break
    case 'custom':
      currentField.value.id = ele.id
      updateCustomTime.value = true
      break
    default:
      break
  }

  if (tabActive.value === 'manage') {
    recoverSelection()
  }
}

const dqTrans = id => {
  const obj = allfields.value.find(ele => ele.id === id)
  obj.groupType = obj.groupType === 'd' ? 'q' : 'd'
}

const dqTransArr = groupType => {
  const idArr = fieldSelection.value.map(ele => ele.id)
  allfields.value.forEach(ele => {
    if (idArr.includes(ele.id)) {
      ele.groupType = groupType
    }
  })
  recoverSelection()
}

const copyField = item => {
  const param = cloneDeep(item)
  param.id = guid()
  param.extField = 2
  param.originName = item.extField === 2 ? item.originName : '[' + item.id + ']'
  param.name = getFieldName(dimensions.value.concat(quota.value), item.name)
  param.dataeaseName = null
  param.lastSyncTime = null
  allfields.value.push(param)
}

const deleteField = item => {
  ElMessageBox.confirm(t('dataset.confirm_delete'), {
    confirmButtonText: t('dataset.confirm'),
    cancelButtonText: t('common.cancel'),
    showCancelButton: true,
    confirmButtonType: 'danger',
    type: 'warning',
    autofocus: false,
    showClose: false,
    callback: (action: Action) => {
      if (action === 'confirm') {
        allfields.value = allfields.value.filter(ele => ele.id !== item.id)
        datasetDrag.value.dfsNodeFieldBack(datasetDrag.value.nodeList, item)
        ElMessage({
          message: t('chart.delete_success'),
          type: 'success'
        })
      }
    }
  })
}

const addCalcField = groupType => {
  editCalcField.value = true
  nextTick(() => {
    calcEdit.value.initEdit({ groupType, id: guid() }, dimensions.value, quota.value)
  })
}

const editNormalField = ref(false)
const currentNormalField = ref({
  id: '',
  name: ''
})

const editField = item => {
  if (item.extField === 2) {
    editCalcField.value = true
    nextTick(() => {
      calcEdit.value.initEdit(item, dimensions.value, quota.value)
    })
    return
  }
  const { id, name } = item
  currentNormalField.value = {
    id,
    name
  }
  editNormalField.value = true
}

const closeNormalField = () => {
  currentNormalField.value.id = ''
  currentNormalField.value.name = ''
  editNormalField.value = false
}

const confirmNormalField = () => {
  ruleFormFieldRef.value.validate(val => {
    if (val) {
      allfields.value.some(ele => {
        if (ele.id === currentNormalField.value.id) {
          ele.name = currentNormalField.value.name
          return true
        }
        return false
      })
      closeNormalField()
    }
  })
}

const closeEditCalc = () => {
  editCalcField.value = false
}

const confirmEditCalc = () => {
  calcEdit.value.formField.validate(val => {
    if (val) {
      calcEdit.value.setFieldForm()
      if (!calcEdit.value.fieldForm.originName.trim()) {
        ElMessage.error('表达式不能为空!')
        return
      }
      const obj = cloneDeep(calcEdit.value.fieldForm)
      const result = allfields.value.findIndex(ele => obj.id === ele.id)
      if (result !== -1) {
        allfields.value.splice(result, 1, obj)
      } else {
        allfields.value.push(obj)
      }
      editCalcField.value = false
    }
  })
}

const generateColumns = (arr: Field[]) =>
  arr.map(ele => ({
    key: ele.dataeaseName,
    deType: ele.deType,
    dataKey: ele.dataeaseName,
    title: ele.name,
    width: 150,
    headerCellRenderer: ({ column }) => (
      <div class="flex-align-center">
        <ElIcon>
          <Icon
            name={`field_${fieldType[column.deType]}`}
            className={`field-icon-${fieldType[column.deType]}`}
          ></Icon>
        </ElIcon>
        <span class="ellipsis" title={column.title} style={{ width: '120px' }}>
          {column.title}
        </span>
      </div>
    )
  }))

const dsChange = (val: string) => {
  dsLoading.value = true
  sqlNode.datasourceId = dataSource.value
  return getTables({ datasourceId: val })
    .then(res => {
      tableList = res || []
      state.tableData = [...tableList]
    })
    .finally(() => {
      dsLoading.value = false
    })
}

const getTableName = async (datasourceId, tableName) => {
  await dsChange(datasourceId)
  if (!!tableName) {
    searchTable.value = tableName
  }
}

const initEdite = () => {
  const { id, datasourceId, tableName } = route.query
  if (datasourceId) {
    dataSource.value = datasourceId as string
    getTableName(datasourceId as string, tableName)
  }
  if (!id) return
  loading.value = true
  getDatasetDetails(id)
    .then(res => {
      let arr = []
      const { id, pid, name } = res || {}
      nodeInfo = {
        id,
        pid,
        name
      }
      datasetName.value = name
      originName.value = name
      allfields.value = res.allFields || []
      dfsUnion(arr, res.union || [])
      const [fir] = res.union as { currentDs: { datasourceId: string } }[]
      dataSource.value = fir?.currentDs?.datasourceId
      dsChange(dataSource.value)
      datasetDrag.value.initState(arr)
    })
    .finally(() => {
      loading.value = false
    })
}

const el = ref<HTMLElement | null>(null)
const elDrag = ref<HTMLElement | null>(null)

const { y, isDragging } = useDraggable(el, {
  initialValue: { x: 0, y: 567 },
  draggingElement: elDrag
})

initEdite()

const joinEditor = (arr: []) => {
  state.editArr = cloneDeep(arr)
  editUnion.value = true
  nextTick(() => {
    fieldUnion.value.initState()
  })
}

const columns = shallowRef([])
const tableData = shallowRef([])
const quota = computed(() => {
  return allfields.value.filter(ele => ele.groupType === 'q')
})

const dimensions = computed(() => {
  return allfields.value.filter(ele => ele.groupType === 'd')
})

const tabChange = val => {
  if (val === 'preview') return
  allfields.value.forEach(ele => {
    if (!Array.isArray(ele.deTypeArr)) {
      ele.deTypeArr =
        ele.deType === 1 && ele.deExtractType === 0
          ? [ele.deType, ele.dateFormatType]
          : [ele.deType]
    }
  })
}

const addComplete = () => {
  state.nodeNameList = [...datasetDrag.value.nodeNameList]
  if (!state.nodeNameList) {
    columns.value = []
    tableData.value = []
  }
}

const state = reactive({
  nameList: [],
  nodeNameList: [],
  editArr: [],
  nodeList: [],
  dataSourceList: [],
  tableData: [],
  fieldCollapse: ['dimension', 'quota']
})

const getIconName = (type: number) => {
  if (type === 1) {
    return 'time'
  }

  if (type === 0) {
    return 'text'
  }

  if ([2, 3, 4].includes(type)) {
    return 'value'
  }
  if (type === 5) {
    return 'location'
  }
}

const allfields = ref([])

let num = +new Date()

const expandedD = ref(true)
const expandedQ = ref(true)
const setGuid = (arr, id, datasourceId) => {
  arr.forEach(ele => {
    if (!ele.id) {
      ele.id = `${++num}`
      ele.datasetTableId = id
      ele.datasourceId = datasourceId
    }
  })
}

const dfsFields = (arr, list) => {
  list.forEach(ele => {
    if (ele.children?.length) {
      dfsFields(arr, ele.children)
    }
    const { currentDsFields } = ele
    arr.push(...cloneDeep(currentDsFields))
  })
}

const diffArr = (newArr, oldArr) => {
  const idMapNew = newArr.map(ele => ele.id)
  const idMapOld = oldArr.map(ele => ele.id)
  const arr = newArr.filter(ele => !idMapOld.includes(ele.id))
  return cloneDeep([
    ...oldArr.filter(ele => ele.extField === 2),
    ...arr,
    ...oldArr.filter(ele => idMapNew.includes(ele.id))
  ])
}

const closeEditUnion = () => {
  notConfirmEditUnion()
  fieldUnion.value.clearState()
  editUnion.value = false
}
const fieldUnion = ref()
const confirmEditUnion = () => {
  const { node, parent } = fieldUnion.value

  let unionFieldsLost = node.unionFields.some(ele => {
    const { currentField, parentField } = ele
    return !currentField || !parentField
  })

  if (unionFieldsLost) {
    ElMessage.error('关联关系不正确!')
    return
  }

  setGuid(node.currentDsFields, node.id, node.datasourceId)
  setGuid(parent.currentDsFields, parent.id, parent.datasourceId)
  const top = cloneDeep(node)
  const bottom = cloneDeep(parent)
  datasetDrag.value.setStateBack(top, bottom)
  const arr = []
  dfsFields(arr, datasetDrag.value.nodeList)
  allfields.value = diffArr(arr, allfields.value)
  tabChange('manage')
  fieldUnion.value.clearState()
  editUnion.value = false
  addComplete()
}

const updateAllfields = () => {
  const arr = []
  dfsFields(arr, datasetDrag.value.nodeList)
  allfields.value = diffArr(arr, allfields.value)
  tabChange('manage')
  fieldUnion.value?.clearState()
  if (!previewHeight.value) {
    nextTick(() => {
      calcEle()
    })
  }
}

const notConfirmEditUnion = () => {
  datasetDrag.value.notConfirm()
}

const dragstart = (e: DragEvent, ele) => {
  offsetX.value = e.offsetX
  offsetY.value = e.offsetY
  e.dataTransfer.setData('text/plain', JSON.stringify(ele))
  maskShow.value = true
}
const setActiveName = (data: Table) => {
  if (data.unableCheck) return
  activeName.value = data.tableName
}

const mousedownDrag = () => {
  document.querySelector('.dataset-db').addEventListener('mousemove', calculateWidth)
}
const mouseupDrag = () => {
  const dom = document.querySelector('.dataset-db')
  dom.removeEventListener('mousemove', calculateWidth)
  dom.removeEventListener('mousemove', calculateHeight)
}
const calculateWidth = (e: MouseEvent) => {
  if (e.pageX < 240) {
    LeftWidth.value = 240
    return
  }
  if (e.pageX > 500) {
    LeftWidth.value = 500
    return
  }
  LeftWidth.value = e.pageX
}

const mousedownDragH = () => {
  document.querySelector('.dataset-db').addEventListener('mousemove', calculateHeight)
}
const calculateHeight = (e: MouseEvent) => {
  if (e.pageY - 56 < 64) {
    dragHeight.value = 64
    return
  }
  if (e.pageY > document.documentElement.clientHeight - 57) {
    dragHeight.value = document.documentElement.clientHeight - 113
    return
  }
  dragHeight.value = e.pageY - 56
}

const getDatasource = () => {
  getDatasourceList().then(res => {
    const _list = (res as unknown as DataSource[]) || []
    if (_list && _list.length > 0 && _list[0].id === '0') {
      state.dataSourceList = _list[0].children
    } else {
      state.dataSourceList = _list
    }
  })
}

getDatasource()

const datasetSave = () => {
  if (nodeInfo.id) {
    editeSave()
    return
  }
  const union = []
  dfsNodeList(union, datasetDrag.value.nodeList)
  const { pid } = route.query
  if (!union.length) {
    ElMessage.error('数据集不能为空')
    return
  }

  creatDsFolder.value.createInit(
    'dataset',
    { id: pid || '0', union, allfields: allfields.value },
    '',
    datasetName.value
  )
}

const datasetPreviewLoading = ref(false)

const datasetPreview = () => {
  const arr = []
  dfsNodeList(arr, datasetDrag.value.nodeList)
  datasetPreviewLoading.value = true
  getPreviewData({ union: arr, allFields: allfields.value })
    .then(res => {
      columns.value = generateColumns((res.data.fields as Field[]) || [])
      tableData.value = (res.data.data as Array<{}>) || []
    })
    .finally(() => {
      datasetPreviewLoading.value = false
    })
}

const dfsNodeList = (arr, list) => {
  list.forEach(ele => {
    const childrenDs = []
    if (ele.children?.length) {
      dfsNodeList(childrenDs, ele.children)
    }
    const {
      tableName,
      type,
      datasourceId,
      id,
      info,
      unionType,
      unionFields,
      currentDsFields,
      sqlVariableDetails
    } = ele
    arr.push({
      currentDs: {
        sqlVariableDetails,
        tableName,
        type,
        datasourceId,
        id,
        info
      },
      currentDsFields,
      childrenDs,
      unionToParent: {
        unionType,
        unionFields
      }
    })
  })
}

const quotaTable = ref()
const dimensionsTable = ref()

const dimensionsSelection = ref([])
const quotaSelection = ref([])

const deTypeSelection = ref([])
const fieldSelection = ref([])

const showCascaderBatch = computed(() => {
  return !!deTypeSelection.value.length && Array.from(new Set(deTypeSelection.value)).length === 1
})

const clearSelection = () => {
  dimensionsTable.value.clearSelection()
  quotaTable.value.clearSelection()
}
const deTypeArr = ref([])

const setDeTypeSelection = () => {
  fieldSelection.value = [
    ...dimensionsTable.value.getSelectionRows(),
    ...quotaTable.value.getSelectionRows()
  ]
  deTypeArr.value = []
  deTypeSelection.value = fieldSelection.value.map(ele => ele.deExtractType)
}

const cascaderChangeArr = val => {
  const [deType, dateFormat] = val
  dimensionsSelection.value = dimensionsTable.value.getSelectionRows().map(ele => ele.id)
  quotaSelection.value = quotaTable.value.getSelectionRows().map(ele => ele.id)
  const arr = [...quotaSelection.value, ...dimensionsSelection.value]
  allfields.value.forEach(ele => {
    if (arr.includes(ele.id)) {
      ele.deType = deType
      ele.dateFormat = deType === 1 ? dateFormat : ''
      ele.dateFormatType = deType === 1 ? dateFormat : ''
      ele.deTypeArr = deType === 1 && ele.deExtractType === 0 ? [deType, dateFormat] : [deType]
    }
  })
  if (dateFormat === 'custom') {
    currentField.value.id = ''
    currentField.value.idArr = [...arr]
    currentField.value.dateFormat = ''
    currentField.value.dateFormatType = dateFormat
    updateCustomTime.value = true
  }
  recoverSelection()
}

const recoverSelection = () => {
  nextTick(() => {
    quota.value.forEach(ele => {
      if (quotaSelection.value.includes(ele.id)) {
        quotaTable.value.toggleRowSelection(ele, true)
      }
    })
    dimensions.value.forEach(ele => {
      if (dimensionsSelection.value.includes(ele.id)) {
        dimensionsTable.value.toggleRowSelection(ele, true)
      }
    })
  })
}

const dragEnd = () => {
  maskShow.value = false
}

const cascaderChange = (row, val) => {
  const [deType, dateFormat] = val
  row.deType = deType
  row.dateFormat = deType === 1 ? dateFormat : ''
  row.dateFormatType = deType === 1 ? dateFormat : ''
  if (dateFormat === 'custom') {
    currentField.value.id = row.id
    updateCustomTime.value = true
  }
}

const dfsUnion = (arr, list) => {
  list.forEach(ele => {
    const children = []
    if (ele.childrenDs?.length) {
      dfsUnion(children, ele.childrenDs)
    }
    const { unionToParent, currentDsFields, currentDs } = ele
    const { tableName, type, datasourceId, id, info, sqlVariableDetails } = currentDs || {}
    const { unionType, unionFields } = unionToParent || {}
    arr.push({
      sqlVariableDetails,
      tableName,
      type,
      datasourceId,
      id,
      info,
      currentDsFields,
      children,
      unionType,
      unionFields
    })
  })
}
const handleClick = () => {
  showInput.value = true
  nextTick(() => {
    editerName.value.focus()
  })
}

const finish = res => {
  const { id, pid, name } = res
  datasetName.value = name
  nodeInfo = {
    id,
    pid,
    name
  }
}

const treeProps = {
  children: 'children',
  label: 'name'
}
</script>

<template>
  <div class="de-dataset-form" v-loading="loading">
    <div class="top">
      <span class="name">
        <el-icon @click="backToMain">
          <Icon name="icon_left_outlined"></Icon>
        </el-icon>
        <template v-if="showInput">
          <el-input ref="editerName" v-model="datasetName" @blur="showInput = false" />
        </template>
        <template v-else>
          <span @click="handleClick" class="dataset-name ellipsis" style="margin: 0 5px">{{
            datasetName
          }}</span>
        </template>
      </span>
      <span class="oprate">
        <el-button type="primary" @click="datasetSave">保存</el-button>
      </span>
    </div>
    <div class="container dataset-db" @mouseup="mouseupDrag">
      <p v-show="!showLeft" class="arrow-right" @click="showLeft = true">
        <el-icon>
          <Icon name="icon_down-right_outlined"></Icon>
        </el-icon>
      </p>
      <div
        v-show="showLeft"
        :style="{ left: LeftWidth + 'px' }"
        class="drag-left"
        @mousedown="mousedownDrag"
      />
      <div
        v-loading="dsLoading"
        v-show="showLeft"
        class="table-list"
        :style="{ width: LeftWidth + 'px' }"
      >
        <div class="table-list-top">
          <p class="select-ds">
            选择数据源
            <el-icon class="left-outlined" @click="showLeft = false">
              <Icon name="group-3400"></Icon>
            </el-icon>
          </p>
          <el-tree-select
            :check-strictly="false"
            @change="dsChange"
            :placeholder="t('dataset.pls_slc_data_source')"
            class="ds-list"
            popper-class="tree-select-ds_popper"
            v-model="dataSource"
            node-key="id"
            :props="treeProps"
            :data="state.dataSourceList"
            :render-after-expand="false"
          />
          <p class="select-ds table-num">
            {{ t('datasource.data_table') }}
            <span class="num">
              <el-icon class="icon-color">
                <Icon name="reference-table"></Icon>
              </el-icon>
              {{ state.tableData.length }}
            </span>
          </p>
          <el-input
            v-model="searchTable"
            class="search"
            :placeholder="t('deDataset.by_table_name')"
            clearable
          >
            <template #prefix>
              <el-icon>
                <Icon name="icon_search-outline_outlined"></Icon>
              </el-icon>
            </template>
          </el-input>
        </div>
        <div v-if="!state.tableData.length && searchTable !== ''" class="el-empty">
          <div
            class="el-empty__description"
            style="margin-top: 80px; color: #5e6d82; text-align: center"
          >
            没有找到相关内容
          </div>
        </div>
        <div v-else class="table-checkbox-list">
          <div
            class="list-item_primary"
            v-if="dataSource"
            @dragstart="$event => dragstart($event, sqlNode)"
            @dragend="dragEnd"
            :draggable="true"
            @click="setActiveName(sqlNode)"
          >
            <el-icon class="icon-color">
              <Icon name="icon_sql_outlined_1"></Icon>
            </el-icon>
            <span class="label">自定义SQL</span>
          </div>
          <template v-for="ele in state.tableData" :key="ele.name">
            <div
              :class="[
                {
                  'not-allow': state.nodeNameList.includes(ele.tableName)
                }
              ]"
              class="list-item_primary"
              :title="ele.name"
              @dragstart="$event => dragstart($event, ele)"
              @dragend="maskShow = false"
              :draggable="!state.nodeNameList.includes(ele.tableName)"
              @click="setActiveName(ele)"
            >
              <el-icon class="icon-color">
                <Icon name="reference-table"></Icon>
              </el-icon>
              <span class="label">{{ ele.tableName }}</span>
            </div>
          </template>
        </div>
      </div>
      <div class="drag-right" :style="{ width: `calc(100vw - ${showLeft ? LeftWidth : 0}px)` }">
        <dataset-union
          @join-editor="joinEditor"
          :maskShow="maskShow"
          :dragHeight="dragHeight"
          :getDsName="getDsName"
          :offsetX="offsetX"
          :offsetY="offsetY"
          ref="datasetDrag"
          @updateAllfields="updateAllfields"
          @addComplete="addComplete"
        ></dataset-union>
        <div class="sql-result" :style="{ height: `calc(100% - ${dragHeight}px)` }">
          <div class="sql-title">
            <span class="drag" @mousedown="mousedownDragH" />
            <div class="field-data">
              <el-button :disabled="!allfields.length" @click="addCalcField('q')" secondary>
                <template #icon>
                  <el-icon>
                    <Icon name="icon_add_outlined"></Icon>
                  </el-icon>
                </template>
                {{ t('dataset.add_calc_field') }}
              </el-button>
              <el-button
                style="min-width: 70px"
                :disabled="!allfields.length"
                v-loading="datasetPreviewLoading"
                @click="datasetPreview"
                secondary
              >
                <template #icon>
                  <el-icon>
                    <Icon name="icon_replace_outlined"></Icon>
                  </el-icon>
                </template>
                刷新数据
              </el-button>
            </div>
          </div>
          <el-tabs class="padding-24" v-model="tabActive" @tab-change="tabChange">
            <el-tab-pane :label="t('chart.data_preview')" name="preview" />
            <el-tab-pane :label="t('dataset.batch_manage')" name="manage" />
          </el-tabs>
          <div
            ref="elDrag"
            v-show="tabActive === 'preview' && !!allfields.length"
            class="table-preview"
          >
            <div class="preview-field">
              <div
                class="field-d"
                :style="{
                  height: fieldDHeight + 'px'
                }"
                :class="isDragging && 'user-select'"
              >
                <div :class="['title', { expanded: expandedD }]" @click="expandedD = !expandedD">
                  <ElIcon class="expand">
                    <Icon name="icon_expand-right_filled"></Icon>
                  </ElIcon>
                  &nbsp;{{ t('chart.dimension') }}
                </div>
                <el-tree v-if="expandedD" :data="dimensions" :props="defaultProps">
                  <template #default="{ data }">
                    <span class="custom-tree-node father">
                      <el-icon>
                        <Icon
                          :name="`field_${fieldType[data.deType]}`"
                          :className="`field-icon-${fieldType[data.deType]}`"
                        ></Icon>
                      </el-icon>
                      <span :title="data.name" class="label-tooltip">{{ data.name }}</span>
                      <div class="operate child">
                        <field-more
                          :extField="data.extField"
                          trans-type="转换为指标"
                          :show-time="data.deExtractType === 0"
                          @handle-command="type => handleFieldMore(data, type)"
                        ></field-more>
                      </div>
                    </span>
                  </template>
                </el-tree>
                <div
                  ref="el"
                  :style="{
                    top: dragVerticalTop + 'px'
                  }"
                  :class="['drag-vertical', isDragging && 'is-hovering']"
                ></div>
              </div>
              <div
                class="field-q"
                :style="{
                  height: `calc(100% - ${fieldDHeight}px)`
                }"
              >
                <div :class="['title', { expanded: expandedQ }]" @click="expandedQ = !expandedQ">
                  <ElIcon class="expand">
                    <Icon name="icon_expand-right_filled"></Icon>
                  </ElIcon>
                  &nbsp;{{ t('chart.quota') }}
                </div>
                <el-tree v-if="expandedQ" :data="quota" :props="defaultProps">
                  <template #default="{ data }">
                    <span class="custom-tree-node father">
                      <el-icon>
                        <Icon
                          :name="`field_${fieldType[data.deType]}`"
                          :className="`field-icon-${fieldType[data.deType]}`"
                        ></Icon>
                      </el-icon>
                      <span :title="data.name" class="label-tooltip">{{ data.name }}</span>
                      <div class="operate child">
                        <field-more
                          trans-type="转换为维度"
                          typeColor="green-color"
                          :show-time="data.deExtractType === 0"
                          :extField="data.extField"
                          @handle-command="type => handleFieldMore(data, type)"
                        ></field-more>
                      </div>
                    </span>
                  </template>
                </el-tree>
              </div>
            </div>
            <div class="preview-data">
              <el-auto-resizer>
                <template #default="{ height, width }">
                  <el-table-v2
                    :columns="columns"
                    header-class="header-cell"
                    :data="tableData"
                    :width="width"
                    :height="height"
                    fixed
                    ><template #empty>
                      <empty-background description="暂无数据" img-type="noneWhite" />
                    </template>
                  </el-table-v2>
                </template>
              </el-auto-resizer>
            </div>
          </div>
          <div v-show="tabActive !== 'preview' && !!allfields.length" class="batch-area">
            <div class="manage-container">
              <el-collapse v-model="state.fieldCollapse" class="style-collapse">
                <el-collapse-item
                  name="dimension"
                  :title="t('chart.dimension')"
                  class="dimension-manage-header manage-header"
                >
                  <el-table
                    @selection-change="setDeTypeSelection"
                    ref="dimensionsTable"
                    :data="dimensions"
                    style="width: 100%"
                  >
                    <el-table-column type="selection" width="40" />
                    <el-table-column prop="name" :label="t('dataset.field_name')" width="264">
                      <template #default="scope">
                        <div class="column-style">
                          <el-input
                            v-model="scope.row.name"
                            :placeholder="t('commons.input_content')"
                          />
                        </div>
                      </template>
                    </el-table-column>

                    <el-table-column
                      prop="originName"
                      :label="t('dataset.origin_name')"
                      width="240"
                    >
                      <template #default="scope">
                        <div class="column-style">
                          <span v-if="scope.row.extField === 0">{{ scope.row.originName }}</span>
                          <span style="color: #8d9199" v-else>{{ t('dataset.calc_field') }}</span>
                        </div>
                      </template>
                    </el-table-column>

                    <el-table-column prop="deType" :label="t('dataset.field_type')" width="200">
                      <template #default="scope">
                        <el-cascader
                          class="select-type"
                          popper-class="cascader-panel"
                          v-model="scope.row.deTypeArr"
                          @change="val => cascaderChange(scope.row, val)"
                          :options="scope.row.deExtractType === 0 ? fieldOptions : fieldOptionsText"
                        >
                          <template v-slot="{ data }">
                            <el-icon>
                              <Icon
                                className="primary-color"
                                :name="`field_${getIconName(data.value)}`"
                              ></Icon>
                            </el-icon>
                            <span>{{ data.label }}</span>
                          </template>
                        </el-cascader>
                        <span class="select-svg-icon">
                          <el-icon>
                            <Icon
                              className="primary-color"
                              :name="`field_${getIconName(scope.row.deType)}`"
                            ></Icon>
                          </el-icon>
                        </span>
                      </template>
                    </el-table-column>

                    <el-table-column
                      prop="deExtraType"
                      :label="t('dataset.origin_type')"
                      width="168"
                    >
                      <template #default="scope">
                        <div class="column-style">
                          <span class="flex-align-center" v-if="scope.row.extField === 0">
                            <el-icon>
                              <Icon
                                className="primary-color"
                                :name="`field_${getIconName(scope.row.deExtractType)}`"
                              ></Icon>
                            </el-icon>
                            {{ fieldTypes(scope.row.deExtractType) }}
                          </span>
                          <span v-else style="color: #8d9199">{{ t('dataset.calc_field') }}</span>
                        </div>
                      </template>
                    </el-table-column>

                    <el-table-column fixed="right" :label="t('chart.dimension')">
                      <template #default="scope">
                        <el-tooltip effect="dark" content="转换为指标" placement="top">
                          <template #default>
                            <el-button text @click="handleFieldMore(scope.row, 'translate')">
                              <template #icon>
                                <Icon name="icon_switch_outlined"></Icon>
                              </template>
                            </el-button>
                          </template>
                        </el-tooltip>
                      </template>
                    </el-table-column>

                    <el-table-column fixed="right" width="168" :label="t('dataset.operator')">
                      <template #default="scope">
                        <el-tooltip effect="dark" :content="t('dataset.copy')" placement="top">
                          <template #default>
                            <el-button text @click="handleFieldMore(scope.row, 'copy')">
                              <template #icon>
                                <Icon name="icon_copy_outlined"></Icon>
                              </template>
                            </el-button>
                          </template>
                        </el-tooltip>

                        <el-tooltip effect="dark" :content="t('dataset.delete')" placement="top">
                          <template #default>
                            <el-button text @click="handleFieldMore(scope.row, 'delete')">
                              <template #icon>
                                <Icon name="icon_delete-trash_outlined"></Icon>
                              </template>
                            </el-button>
                          </template>
                        </el-tooltip>

                        <el-tooltip effect="dark" :content="t('dataset.edit')" placement="top">
                          <template #default>
                            <el-button
                              v-if="scope.row.extField === 2"
                              text
                              @click="handleFieldMore(scope.row, 'editor')"
                            >
                              <template #icon>
                                <Icon name="icon_edit_outlined"></Icon>
                              </template>
                            </el-button>
                          </template>
                        </el-tooltip>
                      </template>
                    </el-table-column>
                  </el-table>
                </el-collapse-item>
                <el-collapse-item
                  name="quota"
                  :title="t('chart.quota')"
                  class="quota-manage-header manage-header"
                >
                  <el-table
                    @selection-change="setDeTypeSelection"
                    ref="quotaTable"
                    :data="quota"
                    style="width: 100%"
                  >
                    <el-table-column type="selection" width="40" />
                    <el-table-column prop="name" :label="t('dataset.field_name')" width="264">
                      <template #default="scope">
                        <div class="column-style">
                          <el-input
                            v-model="scope.row.name"
                            :placeholder="t('commons.input_content')"
                          />
                        </div>
                      </template>
                    </el-table-column>

                    <el-table-column
                      prop="originName"
                      :label="t('dataset.origin_name')"
                      width="240"
                    >
                      <template #default="scope">
                        <div class="column-style">
                          <span v-if="scope.row.extField === 0">{{ scope.row.originName }}</span>
                          <span v-else style="color: #8d9199">{{ t('dataset.calc_field') }}</span>
                        </div>
                      </template>
                    </el-table-column>

                    <el-table-column prop="deType" :label="t('dataset.field_type')" width="200">
                      <template #default="scope">
                        <el-cascader
                          class="select-type"
                          popper-class="cascader-panel"
                          v-model="scope.row.deTypeArr"
                          @change="val => cascaderChange(scope.row, val)"
                          :options="scope.row.deExtractType === 0 ? fieldOptions : fieldOptionsText"
                        >
                          <template v-slot="{ data }">
                            <el-icon>
                              <Icon
                                className="green-color"
                                :name="`field_${getIconName(data.value)}`"
                              ></Icon>
                            </el-icon>
                            <span>{{ data.label }}</span>
                          </template>
                        </el-cascader>
                        <span class="select-svg-icon">
                          <el-icon>
                            <Icon
                              className="green-color"
                              :name="`field_${getIconName(scope.row.deType)}`"
                            ></Icon>
                          </el-icon>
                        </span>
                      </template>
                    </el-table-column>

                    <el-table-column
                      prop="deExtraType"
                      :label="t('dataset.origin_type')"
                      width="168"
                    >
                      <template #default="scope">
                        <div class="column-style">
                          <span v-if="scope.row.extField === 0">
                            <el-icon>
                              <Icon
                                className="green-color"
                                :name="`field_${getIconName(scope.row.deExtractType)}`"
                              ></Icon>
                            </el-icon>
                            {{ fieldTypes(scope.row.deExtractType) }}
                          </span>
                          <span v-else style="color: #8d9199">{{ t('dataset.calc_field') }}</span>
                        </div>
                      </template>
                    </el-table-column>

                    <el-table-column fixed="right" :label="t('chart.quota')">
                      <template #default="scope">
                        <el-tooltip effect="dark" content="转换为维度" placement="top">
                          <template #default>
                            <el-button text @click="handleFieldMore(scope.row, 'translate')">
                              <template #icon>
                                <Icon name="icon_switch_outlined"></Icon>
                              </template>
                            </el-button>
                          </template>
                        </el-tooltip>
                      </template>
                    </el-table-column>

                    <el-table-column fixed="right" width="168" :label="t('dataset.operator')">
                      <template #default="scope">
                        <el-tooltip effect="dark" :content="t('dataset.copy')" placement="top">
                          <template #default>
                            <el-button text @click="handleFieldMore(scope.row, 'copy')">
                              <template #icon>
                                <Icon name="icon_copy_outlined"></Icon>
                              </template>
                            </el-button>
                          </template>
                        </el-tooltip>

                        <el-tooltip effect="dark" :content="t('dataset.delete')" placement="top">
                          <template #default>
                            <el-button text @click="handleFieldMore(scope.row, 'delete')">
                              <template #icon>
                                <Icon name="icon_delete-trash_outlined"></Icon>
                              </template>
                            </el-button>
                          </template>
                        </el-tooltip>

                        <el-tooltip effect="dark" :content="t('dataset.edit')" placement="top">
                          <template #default>
                            <el-button
                              v-if="scope.row.extField === 2"
                              text
                              @click="handleFieldMore(scope.row, 'editor')"
                            >
                              <template #icon>
                                <Icon name="icon_edit_outlined"></Icon>
                              </template>
                            </el-button>
                          </template>
                        </el-tooltip>
                      </template>
                    </el-table-column>
                  </el-table>
                </el-collapse-item>
              </el-collapse>
            </div>
            <div class="batch-operate flex-align-center" v-if="!!deTypeSelection.length">
              <div class="flex-align-center">
                已选择
                <span class="num">{{ deTypeSelection.length }}</span>
                条
                <el-button @click="clearSelection" text style="margin-left: 16px">{{
                  t('commons.clear')
                }}</el-button>
              </div>
              <div class="cascader-batch" v-if="showCascaderBatch">
                <el-cascader
                  class="select-type"
                  v-model="deTypeArr"
                  @change="cascaderChangeArr"
                  popper-class="cascader-panel"
                  :options="
                    deTypeSelection.every(ele => ele === 0) ? fieldOptions : fieldOptionsText
                  "
                >
                  <template v-slot="{ data }">
                    <el-icon>
                      <Icon
                        className="primary-color"
                        :name="`field_${getIconName(data.value)}`"
                      ></Icon>
                    </el-icon>
                    <span>{{ data.label }}</span>
                  </template>
                </el-cascader>
                <span class="select-svg-icon">
                  <el-icon>
                    <Icon
                      :className="`field-icon-${getIconName(deTypeArr[0])}`"
                      :name="`field_${getIconName(deTypeArr[0])}`"
                    ></Icon>
                  </el-icon>
                </span>
              </div>
              <el-button
                @click="dqTransArr('q')"
                v-if="fieldSelection.every(ele => ele.groupType === 'd')"
                plain
                style="margin-left: 200px"
              >
                转换为指标
              </el-button>
              <el-button
                @click="dqTransArr('d')"
                v-else-if="fieldSelection.every(ele => ele.groupType === 'q')"
                plain
                style="margin-left: 200px"
              >
                转换为维度
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
    <el-drawer
      :title="t('dataset.edit_union_relation')"
      v-model="editUnion"
      custom-class="union-dataset-drawer"
      size="840px"
      :before-close="closeEditUnion"
      direction="rtl"
    >
      <union-edit ref="fieldUnion" :editArr="state.editArr" />
      <template #footer>
        <el-button secondary @click="closeEditUnion()">{{ t('dataset.cancel') }} </el-button>
        <el-button type="primary" @click="confirmEditUnion()"
          >{{ t('dataset.confirm') }}
        </el-button>
      </template>
    </el-drawer>
  </div>
  <creat-ds-group @finish="finish" ref="creatDsFolder"></creat-ds-group>
  <el-dialog
    custom-class="calc-field-edit-dialog"
    v-model="editCalcField"
    width="1000px"
    :title="t('dataset.add_calc_field')"
  >
    <calc-field-edit ref="calcEdit" />
    <template #footer>
      <el-button secondary @click="closeEditCalc()">{{ t('dataset.cancel') }} </el-button>
      <el-button type="primary" @click="confirmEditCalc()">{{ t('dataset.confirm') }} </el-button>
    </template>
  </el-dialog>
  <el-dialog v-model="updateCustomTime" width="1000px">
    <el-form ref="ruleFormRef" :rules="rules" :model="currentField" label-width="120px">
      <el-form-item prop="name" label="自定义时间格式">
        <el-input v-model="currentField.name" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button secondary @click="closeCustomTime()">{{ t('dataset.cancel') }} </el-button>
      <el-button type="primary" @click="confirmCustomTime()">{{ t('dataset.confirm') }} </el-button>
    </template>
  </el-dialog>
  <el-dialog
    class="create-dialog"
    :title="t('dataset.field_edit')"
    v-model="editNormalField"
    width="420px"
  >
    <el-form
      ref="ruleFormFieldRef"
      :rules="fieldRules"
      :model="currentNormalField"
      label-position="top"
      label-width="120px"
    >
      <el-form-item prop="name" :label="t('dataset.field_name')">
        <el-input v-model="currentNormalField.name" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button secondary @click="closeNormalField()">{{ t('dataset.cancel') }} </el-button>
      <el-button type="primary" @click="confirmNormalField()"
        >{{ t('dataset.confirm') }}
      </el-button>
    </template>
  </el-dialog>
</template>

<style lang="less" scoped>
@import '@/style/mixin.less';
.de-dataset-form {
  color: #1f2329;
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
      font-family: PingFang SC;
      font-size: 16px;
      font-weight: 400;
      display: flex;
      align-items: center;
      width: 50%;
      position: relative;
      .dataset-name {
        cursor: pointer;
        width: 294px;
      }

      .ed-input {
        width: 302px;
        line-height: 24px;
        height: 24px;
        :deep(.ed-input__wrapper) {
          background-color: #050e21;
          box-shadow: 0 0 0 1px #3370ff;
          padding: 0 4px;
        }
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

  .container {
    width: 100%;
    height: calc(100vh - 56px);
    position: relative;
    .drag-left {
      position: absolute;
      height: calc(100vh - 56px);
      width: 2px;
      top: 0;
      z-index: 5;
      cursor: col-resize;
    }

    .arrow-right {
      position: absolute;
      top: 15px;
      z-index: 2;
      cursor: pointer;
      margin: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      left: 0;
      height: 24px;
      width: 20px;
      box-shadow: 0px 4px 8px rgba(0, 0, 0, 0.1);
      border: 1px solid var(--deCardStrokeColor, #dee0e3);
      border-top-right-radius: 13px;
      border-bottom-right-radius: 13px;
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

      font-family: PingFang SC;
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
          font-size: 36px;
          right: -30px;
          top: -5px;
          z-index: 1;
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
      .sql-result {
        font-family: PingFang SC;
        font-size: 14px;
        overflow-y: auto;
        box-sizing: border-box;

        .sql-title {
          user-select: none;
          height: 10px;
          position: relative;
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
            left: 50%;
            transform: translateX(-50%);
            height: 7px;
            width: 100px;
            border-radius: 3.5px;
            background: rgba(31, 35, 41, 0.1);
            cursor: row-resize;
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

            .drag-vertical {
              width: 100%;
              height: 3px;
              position: absolute;
              left: 0;
              cursor: row-resize;

              &.is-hovering {
                background: #3370ff;
              }
            }

            :deep(.ed-tree-node__content) {
              border-radius: 4px;
              &:hover {
                background: rgba(31, 35, 41, 0.1);
              }
            }

            :deep(.ed-tree-node.is-current > .ed-tree-node__content:not(.is-menu):after) {
              display: none;
            }

            .custom-tree-node {
              flex: 1;
              display: flex;
              align-items: center;
              padding-right: 8px;
              box-sizing: content-box;

              .label-tooltip {
                margin-left: 5.33px;
                width: 60%;
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
              .title {
                cursor: pointer;
                position: sticky;
                margin: 1px;
                top: 1px;
                height: 49px;
                font-family: 'PingFang SC';
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

                &.expanded {
                  .expand {
                    transform: rotate(90deg);
                    font-size: 10px;
                  }
                }
              }
              overflow-y: auto;
              &.user-select {
                user-select: none;
              }
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
  top: 15px;
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
  box-shadow: 0px -2px 4px rgba(31, 35, 41, 0.08);

  .select-svg-icon {
    top: 8px;
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
    background: #ebf1ff;
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
</style>

<style lang="less">
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
}
.calc-field-edit-dialog {
  .ed-dialog__footer {
    padding-top: 24px;
    border: 1px solid rgba(31, 35, 41, 0.15);
  }
}
</style>
