<script lang="tsx" setup>
import {
  ref,
  toRaw,
  unref,
  nextTick,
  reactive,
  shallowRef,
  computed,
  watch,
  provide,
  onMounted,
  onBeforeUnmount
} from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { useEmitt } from '@/hooks/web/useEmitt'
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
import { cancelMap } from '@/config/axios/service'
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
const quotaTableHeight = ref(238)
const creatDsFolder = ref()
const editCalcField = ref(false)
const calcEdit = ref()
const editUnion = ref(false)
const datasetDrag = ref()
const datasetName = ref('未命名数据集')
const tabActive = ref('preview')
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

let tableList = []

const dfsName = (arr, id) => {
  let name = ''
  arr.some(ele => {
    if (ele.id === id) {
      name = ele.name
      return true
    }

    if (!!ele.children?.length) {
      name = dfsName(ele.children, id) || name
    }
    return false
  })

  return name
}

const dfsChild = arr => {
  return arr.filter(ele => {
    if (ele.leaf) {
      return true
    }
    if (!!ele.children?.length) {
      ele.children = dfsChild(ele.children || [])
    }
    return !!ele.children?.length
  })
}

const getDsName = (id: string) => {
  return dfsName(state.dataSourceList, id)
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
        Object.assign(ele, { deTypeArr: [...oldArrValue] })
      }
    })
    delete currentField.value.name
    recoverSelection()
  } else {
    dimensions.value.concat(quota.value).some(ele => {
      if (ele.id === currentField.value.id) {
        delete currentField.value.name
        Object.assign(ele, { deTypeArr: [...oldArrValue] })
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
        Object.assign(ele, {
          deType: 1,
          dateFormatType: 'custom',
          dateFormat: name,
          deTypeArr: [1, 'custom']
        })
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
            ele.deType = 1
            ele.dateFormatType = 'custom'
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
      if (willBack) {
        pushDataset()
      }
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
    case 'rename':
      renameField(ele)
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
  const index = allfields.value.findIndex(ele => ele.id === item.id)
  allfields.value.splice(index + 1, 0, param)
}

const delFieldById = arr => {
  const delId = [...arr]
  while (delId.length) {
    const [targetId] = delId
    delId.shift()
    allfields.value = allfields.value.filter(ele => ele.id !== targetId)
    const allfieldsId = allfields.value.map(ele => ele.id)
    allfields.value = allfields.value.filter(ele => {
      if (ele.extField !== 2) return true
      const idMap = ele.originName.match(/\[(.+?)\]/g)
      if (!idMap) return true
      const result = idMap.every(itm => {
        const id = itm.slice(1, -1)
        return allfieldsId.includes(id)
      })
      if (result) return true
      delId.push(ele.id)
      return false
    })
  }
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
        delFieldById([item.id])
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
  calcTitle.value = t('dataset.add_calc_field')
  nextTick(() => {
    calcEdit.value.initEdit({ groupType, id: guid() }, dimensions.value, quota.value)
  })
}

const editNormalField = ref(false)
const currentNormalField = ref({
  id: '',
  name: ''
})

const renameField = item => {
  const { id, name } = item
  currentNormalField.value = {
    id,
    name
  }
  editNormalField.value = true
}

const calcTitle = ref('')

const editField = item => {
  editCalcField.value = true
  nextTick(() => {
    calcTitle.value = t('dataset.edit_calc_field')
    calcEdit.value.initEdit(item, dimensions.value, quota.value)
  })
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
      const { deType, dateFormat, deExtractType } = obj
      obj.dateFormat = deType === 1 ? dateFormat : ''
      obj.dateFormatType = deType === 1 ? dateFormat : ''
      obj.deTypeArr = deType === 1 && deExtractType === 0 ? [deType, dateFormat] : [deType]
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
        <span class="ellipsis" title={column.title} style={{ width: '120px', marginLeft: '4px' }}>
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
  const { id: copyId } = route.params
  if (datasourceId) {
    dataSource.value = datasourceId as string
    getTableName(datasourceId as string, tableName)
  }
  if (!id && !copyId) return
  loading.value = true
  getDatasetDetails(copyId || id)
    .then(res => {
      let arr = []
      const { id, pid, name } = res || {}
      nodeInfo = {
        id,
        pid,
        name: copyId ? '复制数据集' : name
      }
      if (copyId) {
        nodeInfo.id = ''
      }
      datasetName.value = nodeInfo.name
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
  if (!state.nodeNameList?.length) {
    columns.value = []
    tableData.value = []
  }
  cancelMap['/datasetData/previewData']?.()
  datasetPreviewLoading.value = false
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

provide('allfields', allfields)

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

const getDelIdArr = (newArr, oldArr) => {
  const idMapNew = newArr.map(ele => ele.id)
  return [
    ...oldArr.filter(ele => ele.extField !== 2).filter(ele => !idMapNew.includes(ele.id)),
    ...oldArr.filter(ele => ele.extField === 2)
  ]
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

const setFieldAll = () => {
  const arr = []
  dfsFields(arr, datasetDrag.value.nodeList)
  const delIdArr = getDelIdArr(arr, allfields.value)
  allfields.value = diffArr(arr, allfields.value)
  delFieldById(delIdArr)
  tabChange('manage')
  fieldUnion.value?.clearState()
}
const confirmEditUnion = () => {
  const { node, parent } = fieldUnion.value

  let unionFieldsLost = node.unionFields.some(ele => {
    const { currentField, parentField } = ele
    return !currentField || !parentField
  })

  if (unionFieldsLost) {
    ElMessage.error('关联字段不能为空!')
    return
  }

  setGuid(node.currentDsFields, node.id, node.datasourceId)
  setGuid(parent.currentDsFields, parent.id, parent.datasourceId)
  const top = cloneDeep(node)
  const bottom = cloneDeep(parent)
  datasetDrag.value.setStateBack(top, bottom)
  setFieldAll()
  editUnion.value = false
  addComplete()
}

const updateAllfields = () => {
  setFieldAll()
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

const isDragging = ref(false)

const mousedownDrag = () => {
  isDragging.value = true
  document.querySelector('body').style.userSelect = 'none'
  document.querySelector('.dataset-db').addEventListener('mousemove', calculateWidth)
}
const mouseupDrag = () => {
  isDragging.value = false
  document.querySelector('body').style.userSelect = 'auto'
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
  const clientHeight = document.documentElement.clientHeight
  if (e.pageY - 56 < 64) {
    dragHeight.value = 64
    sqlResultHeight.value = clientHeight - dragHeight.value - 56
    return
  }
  if (e.pageY > clientHeight - 57) {
    dragHeight.value = clientHeight - 113
    sqlResultHeight.value = clientHeight - dragHeight.value - 56
    return
  }
  dragHeight.value = e.pageY - 56
  sqlResultHeight.value = clientHeight - dragHeight.value - 56
  quotaTableHeight.value = sqlResultHeight.value - 242
}

const sqlResultHeight = ref(0)
const handleResize = debounce(() => {
  const clientHeight = document.documentElement.clientHeight
  if (clientHeight - sqlResultHeight.value - 56 < 64) {
    dragHeight.value = 64
    sqlResultHeight.value = clientHeight - dragHeight.value - 56
    return
  }
  dragHeight.value = clientHeight - sqlResultHeight.value - 56
}, 60)
let willBack = false
const saveAndBack = () => {
  if (!willBack) return
  pushDataset()
}

onMounted(() => {
  useEmitt({
    name: 'onDatasetSave',
    callback: saveAndBack
  })
  window.addEventListener('resize', handleResize)
  getSqlResultHeight()
  quotaTableHeight.value = sqlResultHeight.value - 242
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})
const getSqlResultHeight = () => {
  sqlResultHeight.value = (document.querySelector('.sql-result') as HTMLElement).offsetHeight
}
const getDatasource = () => {
  getDatasourceList().then(res => {
    const _list = (res as unknown as DataSource[]) || []
    if (_list && _list.length > 0 && _list[0].id === '0') {
      state.dataSourceList = dfsChild(_list[0].children)
    } else {
      state.dataSourceList = dfsChild(_list)
    }
  })
}

getDatasource()

const resetDfsFields = (arr, idMap) => {
  for (let i in arr) {
    const id = guid()
    idMap[arr[i].currentDs.id] = id
    arr[i].currentDs.id = id
    if (!!arr[i].childrenDs?.length) {
      resetDfsFields(arr[i].childrenDs, idMap)
    }
  }
}

const resetAllfieldsId = arr => {
  const idMap = {}
  for (let i in allfields.value) {
    const id = guid()
    idMap[allfields.value[i].id] = id
    allfields.value[i].id = id
    allfields.value[i].datasetGroupId = ''
  }
  resetDfsFields(arr, idMap)
  return idMap
}

const resetAllfieldsUnionId = (arr, idMap) => {
  let strUnion = JSON.stringify(arr) as string
  let strNodeList = JSON.stringify(toRaw(datasetDrag.value.nodeList)) as string
  let strAllfields = JSON.stringify(unref(allfields.value)) as string
  Object.entries(idMap).forEach(([key, value]) => {
    strUnion = strUnion.replaceAll(key, value as string)
    strAllfields = strAllfields.replaceAll(key, value as string)
    strNodeList = strNodeList.replaceAll(key, value as string)
  })
  allfields.value = JSON.parse(strAllfields)
  datasetDrag.value.initState(JSON.parse(strNodeList))
  return JSON.parse(strUnion)
}

const datasetSave = () => {
  if (nodeInfo.id) {
    editeSave()
    return
  }
  let union = []
  dfsNodeList(union, datasetDrag.value.nodeList)
  const pid = route.query.pid || nodeInfo.pid
  if (!union.length) {
    ElMessage.error('数据集不能为空')
    return
  }
  if (nodeInfo.pid && !nodeInfo.id) {
    union = resetAllfieldsUnionId(union, resetAllfieldsId(union))
  }

  creatDsFolder.value.createInit(
    'dataset',
    { id: pid || '0', union, allfields: allfields.value },
    '',
    datasetName.value
  )
}
const datasetSaveAndBack = () => {
  willBack = true
  datasetSave()
}

const datasetPreviewLoading = ref(false)

const datasetPreview = () => {
  if (datasetPreviewLoading.value) return
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
  deTypeSelection.value = fieldSelection.value.map(ele => ele.deExtractType)
  let deTypes = fieldSelection.value.map(ele => ele.deType)
  const [obj] = fieldSelection.value
  nextTick(() => {
    dimensionsSelection.value = dimensionsTable.value.getSelectionRows().map(ele => ele.id)
    quotaSelection.value = quotaTable.value.getSelectionRows().map(ele => ele.id)
  })
  if (Array.from(new Set(deTypes)).length !== 1) {
    deTypeArr.value = []
    return
  }
  deTypeArr.value =
    obj.deType === 1 && obj.deExtractType === 0 ? [1, obj.dateFormatType] : [obj.deType]
}

let oldArrValue = []

const cascaderChangeArr = val => {
  const [deType, dateFormat] = val
  dimensionsSelection.value = dimensionsTable.value.getSelectionRows().map(ele => ele.id)
  quotaSelection.value = quotaTable.value.getSelectionRows().map(ele => ele.id)

  const arr = [...quotaSelection.value, ...dimensionsSelection.value]
  if (dateFormat === 'custom') {
    const [obj] = allfields.value.filter(ele => arr.includes(ele.id))
    oldArrValue = obj.deType === 1 ? [1, obj.dateFormatType] : [obj.deType]
    currentField.value.id = ''
    currentField.value.idArr = [...arr]
    currentField.value.dateFormat = ''
    currentField.value.dateFormatType = dateFormat
    updateCustomTime.value = true
    recoverSelection()
    return
  }
  allfields.value.forEach(ele => {
    if (arr.includes(ele.id)) {
      ele.deType = deType
      ele.dateFormat = deType === 1 ? dateFormat : ''
      ele.dateFormatType = deType === 1 ? dateFormat : ''
      ele.deTypeArr = deType === 1 && ele.deExtractType === 0 ? [deType, dateFormat] : [deType]
    }
  })
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
  if (dateFormat === 'custom') {
    oldArrValue = row.deType === 1 ? [1, row.dateFormatType] : [row.deType]
    currentField.value.id = row.id
    updateCustomTime.value = true
    return
  }
  row.deType = deType
  row.dateFormat = deType === 1 ? dateFormat : ''
  row.dateFormatType = deType === 1 ? dateFormat : ''
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
  allfields.value = res.allFields || []
}

const errorTips = ref('')

const handleDatasetName = () => {
  errorTips.value = ''
  if (!datasetName.value.trim()) {
    errorTips.value = t('commons.input_content')
  }

  if (datasetName.value.trim().length < 2) {
    errorTips.value = t('datasource.input_limit_2_25', [2, 64])
  }
  showInput.value = !!errorTips.value
}

const treeProps = {
  children: 'children',
  label: 'name',
  disabled: data => {
    return (!data.children?.length && !data.leaf) || data.extraFlag < 0
  }
}
const getDsIconName = data => {
  if (!data.leaf) return 'dv-folder'
  return `${data.type}-ds`
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
          <el-input
            maxlength="64"
            ref="editerName"
            v-model="datasetName"
            @blur="handleDatasetName"
          />
          <div class="ed-form-item__error" v-if="errorTips">{{ errorTips }}</div>
        </template>
        <template v-else>
          <span @click="handleClick" class="dataset-name ellipsis" style="margin-left: 12px">{{
            datasetName
          }}</span>
        </template>
      </span>
      <span class="oprate">
        <el-button :disabled="showInput" type="primary" @click="datasetSaveAndBack"
          >保存并返回</el-button
        >
        <el-button :disabled="showInput" type="primary" @click="datasetSave">保存</el-button>
      </span>
    </div>
    <div class="container dataset-db" @mouseup="mouseupDrag">
      <p v-show="!showLeft" class="arrow-right" @click="showLeft = true">
        <el-icon>
          <Icon name="icon_right_outlined"></Icon>
        </el-icon>
      </p>
      <div
        v-show="showLeft"
        :style="{ left: LeftWidth + 'px' }"
        class="drag-left"
        :class="isDragging && 'is-dragging'"
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
            filterable
            popper-class="tree-select-ds_popper"
            v-model="dataSource"
            node-key="id"
            :props="treeProps"
            :data="state.dataSourceList"
            :render-after-expand="false"
          >
            <template #default="{ data: { name, leaf, type, extraFlag } }">
              <div class="flex-align-center icon">
                <el-icon>
                  <icon :name="getDsIconName({ leaf, type })"></icon>
                </el-icon>
                <span v-if="!leaf || extraFlag > -1">{{ name }}</span>
                <el-tooltip effect="dark" v-else :content="`无效数据源:${name}`" placement="top">
                  <span>{{ name }}</span>
                </el-tooltip>
              </div>
            </template>
          </el-tree-select>
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
                  'not-allow': state.nodeNameList.includes(`${ele.tableName}${dataSource}`)
                }
              ]"
              class="list-item_primary"
              :title="ele.name"
              @dragstart="$event => dragstart($event, ele)"
              @dragend="maskShow = false"
              :draggable="!state.nodeNameList.includes(`${ele.tableName}${dataSource}`)"
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
        <div
          class="sql-result"
          :style="{
            height: sqlResultHeight ? `${sqlResultHeight}px` : `calc(100% - ${dragHeight}px)`
          }"
        >
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
          <div v-show="tabActive === 'preview' && !!allfields.length" class="table-preview">
            <div class="preview-field">
              <div :class="['field-d', { open: expandedD }]">
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
                          :className="`field-icon-${
                            fieldType[[2, 3].includes(data.deType) ? 2 : 0]
                          }`"
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
              </div>
              <div :class="['field-q', { open: expandedQ }]">
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
                          :className="`field-icon-${
                            fieldType[[2, 3].includes(data.deType) ? 2 : 0]
                          }`"
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
                    :height="quotaTableHeight"
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
                          :class="
                            !!scope.row.deTypeArr && !!scope.row.deTypeArr.length && 'select-type'
                          "
                          popper-class="cascader-panel"
                          v-model="scope.row.deTypeArr"
                          @change="val => cascaderChange(scope.row, val)"
                          :options="scope.row.deExtractType === 0 ? fieldOptions : fieldOptionsText"
                        >
                          <template v-slot="{ data }">
                            <el-icon>
                              <Icon
                                :className="`field-icon-${
                                  fieldType[[2, 3].includes(data.value) ? 2 : 0]
                                }`"
                                :name="`field_${getIconName(data.value)}`"
                              ></Icon>
                            </el-icon>
                            <span>{{ data.label }}</span>
                          </template>
                        </el-cascader>
                        <span class="select-svg-icon">
                          <el-icon>
                            <Icon
                              :className="`field-icon-${
                                fieldType[[2, 3].includes(scope.row.deType) ? 2 : 0]
                              }`"
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
                          <span class="flex-align-center icon" v-if="scope.row.extField === 0">
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
                    :height="quotaTableHeight"
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
                          :class="
                            !!scope.row.deTypeArr && !!scope.row.deTypeArr.length && 'select-type'
                          "
                          popper-class="cascader-panel"
                          v-model="scope.row.deTypeArr"
                          @change="val => cascaderChange(scope.row, val)"
                          :options="scope.row.deExtractType === 0 ? fieldOptions : fieldOptionsText"
                        >
                          <template v-slot="{ data }">
                            <el-icon>
                              <Icon
                                :className="`field-icon-${
                                  fieldType[[2, 3].includes(data.value) ? 2 : 0]
                                }`"
                                :name="`field_${getIconName(data.value)}`"
                              ></Icon>
                            </el-icon>
                            <span>{{ data.label }}</span>
                          </template>
                        </el-cascader>
                        <span class="select-svg-icon">
                          <el-icon>
                            <Icon
                              :className="`field-icon-${
                                fieldType[[2, 3].includes(scope.row.deType) ? 2 : 0]
                              }`"
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
                          <span class="flex-align-center icon" v-if="scope.row.extField === 0">
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
                  :class="!!deTypeArr.length && 'select-type'"
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
                        :className="`field-icon-${fieldType[[2, 3].includes(data.value) ? 2 : 0]}`"
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
    :title="calcTitle"
  >
    <calc-field-edit ref="calcEdit" />
    <template #footer>
      <el-button secondary @click="closeEditCalc()">{{ t('dataset.cancel') }} </el-button>
      <el-button type="primary" @click="confirmEditCalc()">{{ t('dataset.confirm') }} </el-button>
    </template>
  </el-dialog>
  <el-dialog class="create-dialog" title="格式编辑" v-model="updateCustomTime" width="1000px">
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
    :title="t('datasource.field_rename')"
    v-model="editNormalField"
    width="420px"
  >
    <el-form
      ref="ruleFormFieldRef"
      :rules="fieldRules"
      :model="currentNormalField"
      require-asterisk-position="right"
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
      font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
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
        background: #3370ff;
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

      font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
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
          z-index: 5;
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
        font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
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
              border-radius: 4px;
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
                font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
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
</style>
