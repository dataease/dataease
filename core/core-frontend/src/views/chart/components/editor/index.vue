<script lang="ts" setup>
import {
  PropType,
  reactive,
  ref,
  watch,
  toRefs,
  computed,
  nextTick,
  onBeforeMount,
  provide,
  h,
  unref,
  getCurrentInstance,
  onMounted
} from 'vue'
import Icon from '@/components/icon-custom/src/Icon.vue'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import { useAppStoreWithOut } from '@/store/modules/app'
import { useI18n } from '@/hooks/web/useI18n'
import { Tree } from '../../../visualized/data/dataset/form/CreatDsGroup.vue'
import { useEmitt } from '@/hooks/web/useEmitt'
import { ElMessage, ElTreeSelect } from 'element-plus-secondary'
import draggable from 'vuedraggable'
import DimensionItem from './drag-item/DimensionItem.vue'
import { fieldType } from '@/utils/attr'
import QuotaItem from '@/views/chart/components/editor/drag-item/QuotaItem.vue'
import DragPlaceholder from '@/views/chart/components/editor/drag-item/DragPlaceholder.vue'
import FilterTree from './filter/FilterTree.vue'
import ChartStyle from '@/views/chart/components/editor/editor-style/ChartStyle.vue'
import VQueryChartStyle from '@/views/chart/components/editor/editor-style/VQueryChartStyle.vue'
import Senior from '@/views/chart/components/editor/editor-senior/Senior.vue'
import QuotaFilterEditor from '@/views/chart/components/editor/filter/QuotaFilterEditor.vue'
import ResultFilterEditor from '@/views/chart/components/editor/filter/ResultFilterEditor.vue'
import { ElIcon } from 'element-plus-secondary'
import DrillItem from '@/views/chart/components/editor/drag-item/DrillItem.vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { BASE_VIEW_CONFIG, getViewConfig } from '@/views/chart/components/editor/util/chart'
import ChartType from '@/views/chart/components/editor/chart-type/ChartType.vue'
import { useRouter, useRoute } from 'vue-router'
import CompareEdit from '@/views/chart/components/editor/drag-item/components/CompareEdit.vue'
import ValueFormatterEdit from '@/views/chart/components/editor/drag-item/components/ValueFormatterEdit.vue'
import CustomSortEdit from '@/views/chart/components/editor/drag-item/components/CustomSortEdit.vue'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import CalcFieldEdit from '@/views/visualized/data/dataset/form/CalcFieldEdit.vue'
import { getFieldName, guid } from '@/views/visualized/data/dataset/form/util'
import { cloneDeep, forEach, get } from 'lodash-es'
import { deleteField, saveField } from '@/api/dataset'
import { getWorldTree } from '@/api/map'
import chartViewManager from '@/views/chart/components/js/panel'
import DatasetSelect from '@/views/chart/components/editor/dataset-select/DatasetSelect.vue'
import { useDraggable } from '@vueuse/core'
import { set, concat, keys } from 'lodash-es'
import { PluginComponent } from '@/components/plugin'
import {
  Field,
  getFieldByDQ,
  copyChartField,
  deleteChartField,
  deleteChartFieldByChartId
} from '@/api/chart'
import ChartTemplateInfo from '@/views/chart/components/editor/common/ChartTemplateInfo.vue'
import { XpackComponent } from '@/components/plugin'
import { useEmbedded } from '@/store/modules/embedded'
const embeddedStore = useEmbedded()
const snapshotStore = snapshotStoreWithOut()
const dvMainStore = dvMainStoreWithOut()
const { canvasCollapse, curComponent, componentData, editMode } = storeToRefs(dvMainStore)
const router = useRouter()
let componentNameEdit = ref(false)
let inputComponentName = ref('')
let componentNameInput = ref(null)

const { t } = useI18n()
const loading = ref(false)
const tabActive = ref('data')
const datasetSelector = ref(null)
const curDatasetWeight = ref(0)
const renameForm = ref<FormInstance>()
const { emitter } = useEmitt({
  name: 'set-table-column-width',
  callback: args => onTableColumnWidthChange(args)
})
const props = defineProps({
  view: {
    type: Object as PropType<ChartObj>,
    required: false,
    default() {
      return { ...BASE_VIEW_CONFIG }
    }
  },
  datasetTree: {
    type: Array as PropType<Tree[]>,
    default: () => []
  },
  themes: {
    type: String as PropType<EditorTheme>,
    default: 'dark'
  }
})

const editCalcField = ref(false)
const isCalcFieldAdd = ref(true)
const calcEdit = ref()
const route = useRoute()

const onComponentNameChange = () => {
  snapshotStore.recordSnapshotCache()
}

const closeEditComponentName = () => {
  componentNameEdit.value = false
  if (!inputComponentName.value || !inputComponentName.value.trim()) {
    return
  }
  if (inputComponentName.value.trim() === view.value.title) {
    return
  }
  if (inputComponentName.value.trim().length > 64 || inputComponentName.value.trim().length < 2) {
    ElMessage.warning('名称字段长度2-64个字符')
    editComponentName()
    return
  }
  view.value.title = inputComponentName.value
  inputComponentName.value = ''
}

const editComponentName = () => {
  componentNameEdit.value = true
  inputComponentName.value = view.value.title
  nextTick(() => {
    componentNameInput.value.focus()
  })
}
const toolTip = computed(() => {
  return props.themes === 'dark' ? 'ndark' : 'dark'
})

const templateStatusShow = computed(() => {
  return view.value['dataFrom'] === 'template'
})

const { view } = toRefs(props)

let cacheId = ''

onBeforeMount(() => {
  cacheId = route.query.id as unknown as string
})

const appStore = useAppStoreWithOut()
const isDataEaseBi = computed(() => appStore.getIsDataEaseBi)
const itemFormRules = reactive<FormRules>({
  chartShowName: [
    { required: true, message: t('commons.input_content'), trigger: 'change' },
    { max: 50, message: t('commons.char_can_not_more_50'), trigger: 'change' }
  ]
})

const state = reactive({
  extData: '',
  moveId: -1,
  dimension: [],
  quota: [],
  dimensionData: [],
  quotaData: [],
  renameItem: false,
  itemForm: {
    name: '',
    chartShowName: '',
    index: 0,
    renameType: ''
  },
  quotaFilterEdit: false,
  quotaItem: {},
  resultFilterEdit: false,
  filterItem: {},
  chartForFilter: {},
  searchField: '',
  quotaItemCompare: {},
  showEditQuotaCompare: false,
  showValueFormatter: false,
  valueFormatterItem: {},
  showCustomSort: false,
  customSortList: [],
  customSortField: {},
  currEditField: {},
  worldTree: [],
  areaId: '',
  chartTypeOptions: [],
  useless: null
})

const filedList = computed(() => {
  return [...state.dimension, ...state.quota].filter(ele => ele.id !== 'count' && !!ele.summary)
})

provide('filedList', () => filedList.value)
watch(
  [() => view.value['tableId']],
  () => {
    if (props.view.id) {
      fieldLoading.value = true
      deleteChartFieldByChartId(props.view.id)
        .then(() => {
          watchDs()
        })
        .catch(() => {
          fieldLoading.value = false
        })
    } else {
      watchDs()
    }
  },
  { deep: true }
)
const watchDs = () => {
  getFields(props.view.tableId, props.view.id, props.view.type)
  const nodeId = view.value['tableId']
  if (!!nodeId) {
    cacheId = nodeId as unknown as string
  }
  const node = datasetSelector?.value?.getNode(nodeId)
  if (node?.data) {
    curDatasetWeight.value = node.data.weight
  }
}
const getFields = (id, chartId, type) => {
  if (id && chartId) {
    fieldLoading.value = true
    getFieldByDQ(id, chartId, { type: type })
      .then(res => {
        state.dimension = (res.dimensionList as unknown as Field[]) || []
        state.quota = (res.quotaList as unknown as Field[]) || []
        state.dimensionData = JSON.parse(JSON.stringify(state.dimension))
        state.quotaData = JSON.parse(JSON.stringify(state.quota))

        fieldLoading.value = false
        emitter.emit('dataset-change')
      })
      .catch(() => {
        state.dimension = []
        state.quota = []
        state.dimensionData = []
        state.quotaData = []

        fieldLoading.value = false
      })
  } else {
    state.dimension = []
    state.quota = []
    state.dimensionData = []
    state.quotaData = []

    fieldLoading.value = false
  }
}

const chartStyleShow = computed(() => {
  return view.value.type !== 'richText'
})

const chartViewInstance = computed(() => {
  return chartViewManager.getChartView(view.value.render, view.value.type)
})
const showAxis = (axis: AxisType) => chartViewInstance.value?.axis?.includes(axis)
const areaSelect = ref()
const expandKeys = ref([])
watch(
  () => [view.value.type, view.value],
  newVal => {
    if (showAxis('area')) {
      if (!state.worldTree?.length) {
        getWorldTree().then(res => {
          state.worldTree.splice(0, state.worldTree.length, res.data)
          state.areaId = view.value?.customAttr?.map?.id
        })
      } else {
        state.areaId = view.value?.customAttr?.map?.id
      }
      areaSelect.value?.blur()
      expandKeys.value.splice(0)
      areaSelect.value?.setCurrentKey(state.areaId)
    }
    state.chartTypeOptions.splice(0, state.chartTypeOptions.length, getViewConfig(newVal[0]))
    state.useless = newVal[0]
  },
  { immediate: true, deep: false }
)
const treeProps = {
  label: 'name',
  children: 'children'
}

const recordSnapshotInfo = type => {
  view.value['dataFrom'] = 'calc'
  snapshotStore.recordSnapshotCache(type, view.value.id)
}

const filterNode = (value, data) => {
  if (!value) {
    return true
  }
  return data.name?.includes(value)
}

const allFields = computed(() => {
  return concat(state.quotaData, state.dimensionData)
})

const queryList = computed(() => {
  let arr = []
  componentData.value.forEach(com => {
    if (com.innerType === 'VQuery') {
      arr.push(com)
    }
    if ('DeTabs' === com.innerType) {
      com.propValue.forEach(itx => {
        arr = [...itx.componentData.filter(item => item.innerType === 'VQuery'), ...arr]
      })
    }
  })
  return arr
})

const quotaData = computed(() => {
  let result = JSON.parse(JSON.stringify(state.quota))
  if (view.value?.type === 'table-info') {
    result = result?.filter(item => item.id !== '-1')
  }
  if (state.searchField) {
    result = result.filter(item =>
      item.name.toLowerCase().includes(state.searchField.toLowerCase())
    )
  }
  return result
})
const dimensionData = computed(() => {
  let result = JSON.parse(JSON.stringify(state.dimensionData))
  if (state.searchField) {
    result = result.filter(item =>
      item.name.toLowerCase().includes(state.searchField.toLowerCase())
    )
  }
  return result
})
const realQuota = computed(() => {
  let result = JSON.parse(JSON.stringify(state.quota))
  if (view.value?.type === 'table-info') {
    result = result?.filter(item => item.id !== '-1')
  }
  return result
})
provide('quotaData', realQuota)

const startToMove = (e: DragEvent, item) => {
  e.dataTransfer.setData(
    'dimension',
    JSON.stringify(
      item
        .filter(ele => ele.id)
        .map(ele => ({ ...cloneDeep(unref(ele)), datasetId: view.value.tableId }))
    )
  )
}

const dimensionItemChange = item => {
  recordSnapshotInfo('calcData')
  // do dimensionItemChange
  if (view.value.type === 'bar-range') {
    if (item.axisType === 'quota') {
      view.value.yAxisExt?.forEach(y => {
        y.dateStyle = item.dateStyle
        y.datePattern = item.datePattern
      })
    } else if (item.axisType === 'quotaExt') {
      view.value.yAxis?.forEach(y => {
        y.dateStyle = item.dateStyle
        y.datePattern = item.datePattern
      })
    }
  }
}
const dimensionItemRemove = item => {
  recordSnapshotInfo('calcData')
  if (item.removeType === 'dimension') {
    view.value.xAxis.splice(item.index, 1)
  } else if (item.removeType === 'dimensionExt') {
    view.value.xAxisExt.splice(item.index, 1)
  } else if (item.removeType === 'dimensionStack') {
    view.value.extStack.splice(item.index, 1)
  } else if (item.removeType === 'quota') {
    view.value.yAxis.splice(item.index, 1)
  } else if (item.removeType === 'quotaExt') {
    view.value.yAxisExt.splice(item.index, 1)
  } else if (item.removeType === 'xAxisExtRight') {
    view.value.extBubble.splice(item.index, 1)
  } else if (item.removeType === 'flowMapStartName') {
    view.value.flowMapStartName.splice(item.index, 1)
  } else if (item.removeType === 'flowMapEndName') {
    view.value.flowMapEndName.splice(item.index, 1)
  }
}

const quotaItemChange = (axis: Axis, axisType: AxisType) => {
  recordSnapshotInfo('calcData')
  // do quotaItemChange
  emitter.emit('updateAxis', { axisType, axis: [axis], editType: 'update' })
}

const aggregateChange = () => {
  recordSnapshotInfo('calcData')
}

const quotaItemRemove = item => {
  recordSnapshotInfo('calcData')
  let axisType: AxisType = item.removeType
  let axis
  if (item.removeType === 'quota') {
    axisType = 'yAxis'
    axis = view.value.yAxis.splice(item.index, 1)
  } else if (item.removeType === 'quotaExt') {
    axisType = 'yAxisExt'
    axis = view.value.yAxisExt.splice(item.index, 1)
  } else if (item.removeType === 'extLabel') {
    axis = view.value.extLabel.splice(item.index, 1)
  } else if (item.removeType === 'extTooltip') {
    axis = view.value.extTooltip.splice(item.index, 1)
  } else if (item.removeType === 'extBubble') {
    axis = view.value.extBubble.splice(item.index, 1)
  }
  useEmitt().emitter.emit('removeAxis', { axisType, axis, editType: 'remove' })
}
const arrowIcon = () => {
  return h(Icon, { name: 'icon_down_outlined-1' })
}

const isFilterActive = computed(() => {
  return !!view.value.customFilter?.items?.length
})

const drillItemChange = () => {
  recordSnapshotInfo('calcData')
  // temp do nothing
}
const drillItemRemove = item => {
  recordSnapshotInfo('calcData')
  view.value.drillFields.splice(item.index, 1)
}

const customSortAxis = ref<AxisType>('xAxis')
const customSort = () => {
  state.showCustomSort = true
}
const customSortChange = val => {
  state.customSortList = val
}
const closeCustomSort = () => {
  state.showCustomSort = false
  state.customSortField = {}
  state.customSortList = []
}
const saveCustomSort = () => {
  view.value[customSortAxis.value].forEach(ele => {
    if (ele.id === state.customSortField.id) {
      ele.sort = 'custom_sort'
      ele.customSort = state.customSortList
    }
  })
  closeCustomSort()
}
const onCustomSort = item => {
  recordSnapshotInfo('render')
  state.customSortField = view.value.xAxis[item.index]
  customSortAxis.value = 'xAxis'
  customSort()
}

const onStackCustomSort = item => {
  recordSnapshotInfo('render')
  state.customSortField = view.value.extStack[item.index]
  customSortAxis.value = 'extStack'
  customSort()
}

const onExtCustomSort = item => {
  recordSnapshotInfo('render')
  state.customSortField = view.value.xAxisExt[item.index]
  customSortAxis.value = 'xAxisExt'
  customSort()
}

const onExtCustomRightSort = item => {
  recordSnapshotInfo('render')
  state.customSortField = view.value.extBubble[item.index]
  customSortAxis.value = 'extBubble'
  customSort()
}

const onCustomFlowMapStartNameSort = item => {
  recordSnapshotInfo('render')
  state.customSortField = view.value.flowMapStartName[item.index]
  customSortAxis.value = 'flowMapStartName'
  customSort()
}

const onCustomFlowMapEndNameSort = item => {
  recordSnapshotInfo('render')
  state.customSortField = view.value.flowMapEndName[item.index]
  customSortAxis.value = 'flowMapEndName'
  customSort()
}

const onMove = e => {
  recordSnapshotInfo('calcData')
  state.moveId = e.draggedContext.element.id
  return true
}
// drag
const dragCheckType = (list, type) => {
  if (list && list.length > 0) {
    let valid = true
    for (let i = 0; i < list.length; i++) {
      if (list[i].groupType !== type) {
        list.splice(i, 1)
        valid = false
      }
    }
    if (!valid) {
      ElMessage({
        message: type === 'd' ? t('chart.error_q_2_d') : t('chart.error_d_2_q'),
        type: 'warning'
      })
    }
    return valid
  }
}
const dragMoveDuplicate = (list, e, mode) => {
  if (mode === 'ds') {
    list.splice(e.newDraggableIndex, 1)
  } else {
    const dup = list.filter(function (m) {
      return m.id === state.moveId
    })
    if (dup && dup.length > 1) {
      list.splice(e.newDraggableIndex, 1)
      return dup
    }
  }
}
const dragRemoveAggField = (list, e) => {
  const dup = list.filter(function (m) {
    return m.id === state.moveId
  })
  if (dup && dup.length > 0) {
    if (dup[0].summary === '') {
      list.splice(e.newDraggableIndex, 1)
    }
  }
}

const showAggregate = computed<boolean>(() => {
  if (view.value.type === 'bar-range') {
    const tempYAxis = view.value.yAxis[0]
    const tempYAxisExt = view.value.yAxisExt[0]
    if (
      (tempYAxis && tempYAxis.groupType === 'd') ||
      (tempYAxisExt && tempYAxisExt.groupType === 'd')
    ) {
      return true
    }
  }
  return false
})

const disableUpdate = computed(() => {
  let flag = false
  if (view.value.type === 'table-info') {
    return flag
  }
  if (!chartViewInstance.value) {
    return flag
  }
  const axisConfig = chartViewInstance.value.axisConfig
  if (!axisConfig) {
    return flag
  }
  for (const key in axisConfig) {
    if (Object.prototype.hasOwnProperty.call(axisConfig, key)) {
      const axis = view.value[key]
      if (axis instanceof Array) {
        axis.forEach(a => {
          if (a.desensitized) {
            flag = true
          }
        })
      }
    }
  }
  return flag
})

const addAxis = (e, axis: AxisType) => {
  recordSnapshotInfo('calcData')
  const axisSpec = chartViewInstance.value?.axisConfig[axis]
  if (!axisSpec) {
    return
  }
  const { type, limit, duplicate } = axisSpec
  let typeValid, dup

  if (view.value.type === 'bar-range' && (axis === 'yAxis' || axis === 'yAxisExt')) {
    //区间条形图先排除非时间纬度或者指标的情况
    const list = view.value[axis]
    if (list && list.length > 0) {
      let valid = true
      for (let i = 0; i < list.length; i++) {
        if (!(list[i].groupType === 'q' || (list[i].groupType === 'd' && list[i].deType === 1))) {
          list.splice(i, 1)
          valid = false
        }
      }
      if (!valid) {
        ElMessage({
          message: t('chart.error_d_not_time_2_q'),
          type: 'warning'
        })
      }
      typeValid = valid
    }
  } else if (type) {
    typeValid = dragCheckType(view.value[axis], type)
  }

  // 针对指标卡进行数值类型判断
  if (typeValid && type === 'q' && view.value.type === 'indicator') {
    const list = view.value[axis]
    if (list && list.length > 0) {
      let valid = true
      for (let i = 0; i < list.length; i++) {
        if (list[i].deType !== 2 && list[i].deType !== 3) {
          list.splice(i, 1)
          valid = false
        }
      }
      typeValid = valid
      if (!typeValid) {
        ElMessage({
          message: t('chart.error_not_number'),
          type: 'warning'
        })
      }
    }
  }

  if (!duplicate) {
    dup = dragMoveDuplicate(view.value[axis], e, 'chart')
  }
  if (view.value[axis].length > limit) {
    const removedAxis = view.value[axis].splice(limit)
    if (e.newDraggableIndex + 1 <= limit) {
      emitter.emit('removeAxis', { axisType: axis, axis: removedAxis, editType: 'remove' })
      emitter.emit('addAxis', {
        axisType: axis,
        axis: [view.value[axis][e.newDraggableIndex]],
        editType: 'add'
      })
    }
  } else {
    if (!dup && typeValid) {
      emitter.emit('addAxis', {
        axisType: axis,
        axis: [view.value[axis][e.newDraggableIndex]],
        editType: 'add'
      })
    }
  }
  if (view.value.type === 'line') {
    if (view.value?.xAxisExt?.length && view.value?.yAxis?.length > 1) {
      const axis = view.value.yAxis.splice(1)
      emitter.emit('removeAxis', { axisType: 'yAxis', axis, editType: 'remove' })
    }
  }
  if (view.value.type.includes('chart-mix')) {
    if (axis === 'yAxis') {
      if (view.value.yAxisExt.length > 0) {
        const chartType = view.value.yAxisExt[0].chartType
        forEach(view.value.yAxis, axis => {
          if (chartType === 'bar') {
            axis.chartType = 'line'
          } else if (chartType === 'line') {
            axis.chartType = 'bar'
          }
        })
      }
    } else if (axis === 'yAxisExt') {
      if (view.value.yAxis.length > 0) {
        const chartType = view.value.yAxis[0].chartType
        forEach(view.value.yAxisExt, axis => {
          if (chartType === 'bar') {
            axis.chartType = 'line'
          } else if (chartType === 'line') {
            axis.chartType = 'bar'
          }
        })
      }
    }
  }

  if (typeValid && view.value.type === 'bar-range') {
    //处理某一个轴有数据的情况
    let tempType = null
    let tempDeType = null
    if (axis === 'yAxis' && view.value.yAxisExt[0]) {
      tempType = view.value.yAxisExt[0].groupType
      tempDeType = view.value.yAxisExt[0].deType
    } else if (axis === 'yAxisExt' && view.value.yAxis[0]) {
      tempType = view.value.yAxis[0].groupType
      tempDeType = view.value.yAxis[0].deType
    }
    if (tempType !== null) {
      const list = view.value[axis]
      if (list && list.length > 0) {
        let valid = true
        for (let i = 0; i < list.length; i++) {
          if (
            !(
              list[i].groupType === tempType &&
              (tempType === 'q' || (tempType === 'd' && list[i].deType === tempDeType))
            )
          ) {
            list.splice(i, 1)
            valid = false
          }
        }
        if (!valid) {
          ElMessage({
            message: t('chart.error_bar_range_axis_type_not_equal'),
            type: 'warning'
          })
        }
        typeValid = valid
      }
    }
  }
}

const addXaxis = e => {
  addAxis(e, 'xAxis')
}

const addXaxisExt = e => {
  addAxis(e, 'xAxisExt')
}

const addExtStack = e => {
  addAxis(e, 'extStack')
}

const addYaxis = e => {
  addAxis(e, 'yAxis')
}

const addYaxisExt = e => {
  addAxis(e, 'yAxisExt')
}

const addExtBubble = e => {
  addAxis(e, 'extBubble')
}

const addDrill = e => {
  recordSnapshotInfo('calcData')
  dragCheckType(view.value.drillFields, 'd')
  dragMoveDuplicate(view.value.drillFields, e, '')
  dragRemoveAggField(view.value.drillFields, e)
}

const addFlowMapStartName = e => {
  addAxis(e, 'flowMapStartName')
}

const addFlowMapEndName = e => {
  addAxis(e, 'flowMapEndName')
}

const onAxisChange = (e, axis: AxisType) => {
  if (e.removed) {
    const { element } = e.removed
    emitter.emit('removeAxis', { axisType: axis, axis: [element], editType: 'remove' })
  }
}

const calcData = (view, resetDrill = false, updateQuery = '') => {
  if (
    view.refreshTime === '' ||
    parseFloat(view.refreshTime).toString() === 'NaN' ||
    parseFloat(view.refreshTime) < 1
  ) {
    ElMessage.error(t('chart.only_input_number'))
    return
  }
  if (resetDrill) {
    useEmitt().emitter.emit('resetDrill-' + view.id, 0)
  } else {
    useEmitt().emitter.emit('calcData-' + view.id, view)
  }
  snapshotStore.recordSnapshotCache('calcData', view.id)
  if (updateQuery === 'updateQuery') {
    queryList.value.forEach(ele => {
      useEmitt().emitter.emit(`updateQueryCriteria${ele.id}`)
    })
  }
}

const updateChartData = view => {
  curComponent.value['state'] = 'ready'
  calcData(view, true, 'updateQuery')
}

const renderChart = view => {
  useEmitt().emitter.emit('renderChart-' + view.id, view)
  snapshotStore.recordSnapshotCache('renderChart', view.id)
}

const onAreaChange = val => {
  view.value.customAttr.map = { id: val.id, level: val.level }
  renderChart(view.value)
}

const onTypeChange = (render, type) => {
  const viewConf = getViewConfig(type)
  console.log(view.value)
  if (viewConf.isPlugin) {
    view.value.plugin = {
      isPlugin: true,
      staticMap: viewConf.staticMap
    }
    view.value.isPlugin = true
  } else {
    view.value.isPlugin = false
    delete view.value.plugin
  }
  view.value.render = render
  view.value.type = type
  emitter.emit('chart-type-change')
  // 处理配置项默认值，不同图表的同一配置项默认值不同
  const chartViewInstance = chartViewManager.getChartView(view.value.render, view.value.type)
  if (chartViewInstance) {
    view.value = chartViewInstance.setupDefaultOptions(view.value) as unknown as ChartObj
    // 处理轴
    const axisConfig = chartViewInstance.axisConfig
    keys(axisConfig).forEach((axis: AxisType) => {
      const axisArr = view.value[axis] as Axis[]
      if (!axisArr?.length) {
        return
      }
      const axisSpec = axisConfig[axis]
      const { type, limit } = axisSpec
      const removedAxis = []
      // check type
      if (type) {
        for (let i = axisArr.length - 1; i >= 0; i--) {
          if (axisArr[i].groupType !== type) {
            const [axis] = axisArr.splice(i, 1)
            removedAxis.push(axis)
          }
        }
      }
      // check limit
      if (limit && limit < axisArr.length) {
        axisArr.splice(limit).forEach(i => removedAxis.push(i))
      }
      removedAxis.length &&
        emitter.emit('removeAxis', { axisType: axis, axis: removedAxis, editType: 'remove' })
    })
    if (view.value.type === 'line') {
      if (view.value?.xAxisExt?.length && view.value?.yAxis?.length > 1) {
        const axis = view.value.yAxis.splice(1)
        emitter.emit('removeAxis', { axisType: 'yAxis', axis, editType: 'remove' })
      }
    }
    if (
      view.value.type === 'liquid' ||
      view.value.type === 'gauge' ||
      view.value.type === 'indicator'
    ) {
      removeItems('drillFields')
    }
    if (!['line', 'area', 'bar', 'bar-group'].includes(view.value.type)) {
      // 清除图表标注
      const pointElement = document.getElementById('point_' + view.value.id)
      if (pointElement) {
        pointElement.remove()
        pointElement.parentNode?.removeChild(pointElement)
      }
    }
  }
  curComponent.value.innerType = type
  calcData(view.value, true)
}

const onBasicStyleChange = (chartForm: ChartEditorForm<ChartBasicStyle>) => {
  const { data, requestData } = chartForm
  view.value.customAttr.basicStyle = data
  if (requestData) {
    calcData(view.value)
  } else {
    renderChart(view.value)
  }
}

const onTableHeaderChange = val => {
  view.value.customAttr.tableHeader = val
  renderChart(view.value)
}
const onTableCellChange = val => {
  view.value.customAttr.tableCell = val
  renderChart(view.value)
}
const onTableTotalChange = val => {
  view.value.customAttr.tableTotal = val
  renderChart(view.value)
}

const onColorChange = val => {
  view.value.customAttr.color = val
  renderChart(view.value)
}

const onMiscChange = val => {
  view.value.customAttr.misc = val.data
  if (val.requestData) {
    calcData(view.value)
  } else {
    renderChart(view.value)
  }
}

const onLabelChange = (chartForm: ChartEditorForm<ChartLabelAttr>, prop: string) => {
  const { data, requestData, render } = chartForm
  let labelObj = data
  if (!data) {
    labelObj = chartForm as unknown as ChartLabelAttr
  }
  if (prop) {
    const val = get(labelObj, prop)
    set(view.value.customAttr.label, prop, val)
  } else {
    view.value.customAttr.label = labelObj
  }
  // for compatibility
  if (render !== false) {
    renderChart(view.value)
  }
}

const onIndicatorChange = (val, prop) => {
  if (prop === 'color' || prop === 'suffixColor') {
    view.value.customAttr.basicStyle.alpha = undefined
    if (val.indicatorName !== undefined) {
      view.value.customAttr.indicatorName = val.indicatorName
    }
  }
  view.value.customAttr.indicator = val.indicatorValue
  renderChart(view.value)
}

const onIndicatorNameChange = (val, prop) => {
  if (prop === 'color') {
    view.value.customAttr.basicStyle.alpha = undefined
    if (val.indicatorValue !== undefined) {
      view.value.customAttr.indicator = val.indicatorValue
    }
  }
  view.value.customAttr.indicatorName = val.indicatorName
  renderChart(view.value)
}

const onTooltipChange = (chartForm: ChartEditorForm<ChartTooltipAttr>, prop: string) => {
  const { data, requestData, render } = chartForm
  let tooltipObj = data
  if (!data) {
    tooltipObj = chartForm as unknown as ChartTooltipAttr
  }
  if (prop) {
    const val = get(tooltipObj, prop)
    set(view.value.customAttr.tooltip, prop, val)
  } else {
    view.value.customAttr.tooltip = tooltipObj
  }
  if (requestData) {
    calcData(view.value)
    return
  }
  // for compatibility
  if (render !== false) {
    renderChart(view.value)
  }
}

const onChangeXAxisForm = val => {
  view.value.customStyle.xAxis = val
  renderChart(view.value)
}

const onChangeYAxisForm = val => {
  view.value.customStyle.yAxis = val
  renderChart(view.value)
}

const onChangeYAxisExtForm = val => {
  view.value.customStyle.yAxisExt = val
  renderChart(view.value)
}

const onChangeMiscStyleForm = val => {
  view.value.customStyle.misc = val
  renderChart(view.value)
}

const onTextChange = val => {
  view.value.customStyle.text = val
  useEmitt().emitter.emit('updateTitle-' + view.value.id)
  snapshotStore.recordSnapshotCache('renderChart', view.value.id)
}

const onLegendChange = val => {
  view.value.customStyle.legend = val
  renderChart(view.value)
}

const onFunctionCfgChange = val => {
  view.value.senior.functionCfg = val
  renderChart(view.value)
}

const onBackgroundChange = val => {
  curComponent.value.commonBackground = val
}

const onAssistLineChange = val => {
  view.value.senior.assistLineCfg = val.data
  if (val.requestData) {
    calcData(view.value)
  } else {
    renderChart(view.value)
  }
}

const onThresholdChange = val => {
  view.value.senior.threshold = val
  renderChart(view.value)
}

const onMapMappingChange = val => {
  view.value.senior.areaMapping = val
  renderChart(view.value)
}

const onScrollCfgChange = val => {
  view.value.senior.scrollCfg = val
  renderChart(view.value)
}

const onBubbleAnimateChange = val => {
  view.value.senior.bubbleCfg = val
  renderChart(view.value)
}

const onTableColumnWidthChange = val => {
  if (editMode.value !== 'edit') {
    return
  }
  view.value.customAttr.basicStyle.tableFieldWidth = val
  snapshotStore.recordSnapshotCache('renderChart', view.value.id)
}

const onExtTooltipChange = val => {
  view.value.extTooltip = val
}
const onChangeQuadrantForm = val => {
  view.value.customAttr.quadrant = val
  renderChart(view.value)
}
const onChangeFlowMapLineForm = val => {
  view.value.customAttr.misc.flowMapConfig.lineConfig = val
  renderChart(view.value)
}
const onChangeFlowMapPointForm = val => {
  view.value.customAttr.misc.flowMapConfig.pointConfig = val
  renderChart(view.value)
}

const showRename = val => {
  recordSnapshotInfo('render')
  state.itemForm = JSON.parse(JSON.stringify(val))
  if (!state.itemForm.chartShowName) {
    state.itemForm.chartShowName = state.itemForm.name
  }
  state.renameItem = true
}

const closeRename = () => {
  state.renameItem = false
}

const removeItems = (
  _type:
    | 'xAxis'
    | 'xAxisExt'
    | 'extStack'
    | 'yAxis'
    | 'yAxisExt'
    | 'extBubble'
    | 'customFilter'
    | 'drillFields'
    | 'flowMapStartName'
    | 'flowMapEndName'
) => {
  recordSnapshotInfo('calcData')
  let axis = []
  switch (_type) {
    case 'xAxis':
      axis = view.value.xAxis?.splice(0)
      break
    case 'xAxisExt':
      axis = view.value.xAxisExt?.splice(0)
      break
    case 'extStack':
      axis = view.value.extStack?.splice(0)
      break
    case 'yAxis':
      axis = view.value.yAxis?.splice(0)
      break
    case 'yAxisExt':
      axis = view.value.yAxisExt?.splice(0)
      break
    case 'extBubble':
      axis = view.value.extBubble?.splice(0)
      break
    case 'customFilter':
      view.value.customFilter = {}
      return
      break
    case 'drillFields':
      axis = view.value.drillFields?.splice(0)
      break
    case 'flowMapStartName':
      axis = view.value.flowMapStartName?.splice(0)
      break
    case 'flowMapEndName':
      axis = view.value.flowMapEndName?.splice(0)
      break
  }
  axis?.length && emitter.emit('removeAxis', { axisType: _type, axis, editType: 'remove' })
}

const saveRename = ref => {
  if (!ref) return
  ref.validate(valid => {
    if (valid) {
      const { renameType, index, chartShowName } = state.itemForm
      let axisType, axis
      switch (renameType) {
        case 'quota':
          axisType = 'yAxis'
          axis = view.value.yAxis[index]
          view.value.yAxis[index].chartShowName = chartShowName
          break
        case 'dimension':
          view.value.xAxis[index].chartShowName = chartShowName
          break
        case 'quotaExt':
          axisType = 'yAxisExt'
          axis = view.value.yAxisExt[index]
          view.value.yAxisExt[index].chartShowName = chartShowName
          break
        case 'dimensionExt':
          view.value.xAxisExt[index].chartShowName = chartShowName
          break
        case 'xAxisExtRight':
          view.value.extBubble[index].chartShowName = chartShowName
          break
        case 'dimensionStack':
          view.value.extStack[index].chartShowName = chartShowName
          break
        case 'extBubble':
          axisType = 'extBubble'
          axis = view.value.extBubble[index]
          view.value.extBubble[index].chartShowName = chartShowName
          break
        case 'extLabel':
          view.value.extLabel[index].chartShowName = chartShowName
          break
        case 'extTooltip':
          view.value.extTooltip[index].chartShowName = chartShowName
        case 'flowMapStartName':
          axisType = 'flowMapStartName'
          axis = view.value.flowMapStartName[index]
          view.value.flowMapStartName[index].chartShowName = chartShowName
          break
        case 'flowMapEndName':
          axisType = 'flowMapEndName'
          axis = view.value.flowMapEndName[index]
          view.value.flowMapEndName[index].chartShowName = chartShowName
          break
        default:
          break
      }
      axisType && emitter.emit('updateAxis', { axisType, axis: [axis], editType: 'update' })
      closeRename()
    } else {
      return false
    }
  })
}

const showQuotaEditFilter = item => {
  recordSnapshotInfo('calcData')
  state.quotaItem = JSON.parse(JSON.stringify(item))
  if (!state.quotaItem.logic) {
    state.quotaItem.logic = 'and'
  }
  state.quotaFilterEdit = true
}
const closeQuotaFilter = () => {
  state.quotaFilterEdit = false
}
const saveQuotaFilter = () => {
  for (let i = 0; i < state.quotaItem.filter.length; i++) {
    const f = state.quotaItem.filter[i]
    if (!f.term.includes('null') && !f.term.includes('empty') && (!f.value || f.value === '')) {
      ElMessage.error(t('chart.filter_value_can_null'))
      return
    }
    if (!f.term.includes('null') && !f.term.includes('empty') && isNaN(f.value)) {
      ElMessage.error(t('chart.filter_value_can_not_str'))
      return
    }
  }
  if (state.quotaItem.filterType === 'quota') {
    view.value.yAxis[state.quotaItem.index].filter = state.quotaItem.filter
    view.value.yAxis[state.quotaItem.index].logic = state.quotaItem.logic
  } else if (state.quotaItem.filterType === 'quotaExt') {
    view.value.yAxisExt[state.quotaItem.index].filter = state.quotaItem.filter
    view.value.yAxisExt[state.quotaItem.index].logic = state.quotaItem.logic
  }
  closeQuotaFilter()
}
const changeFilterData = customFilter => {
  view.value.customFilter = cloneDeep(customFilter)
}
const filterTree = ref()

const openTreeFilter = () => {
  filterTree.value.init(cloneDeep(view.value.customFilter))
}
const closeResultFilter = () => {
  state.resultFilterEdit = false
}
const saveResultFilter = () => {
  if (
    ((state.filterItem.deType === 0 || state.filterItem.deType === 5) &&
      state.filterItem.filterType !== 'enum') ||
    state.filterItem.deType === 1 ||
    state.filterItem.deType === 2 ||
    state.filterItem.deType === 3
  ) {
    for (let i = 0; i < state.filterItem.filter.length; i++) {
      const f = state.filterItem.filter[i]
      if (!f.term.includes('null') && !f.term.includes('empty') && (!f.value || f.value === '')) {
        ElMessage.error(t('chart.filter_value_can_null'))
        return
      }
      if (state.filterItem.deType === 2 || state.filterItem.deType === 3) {
        if (!f.term.includes('null') && !f.term.includes('empty') && isNaN(f.value)) {
          ElMessage.error(t('chart.filter_value_can_not_str'))
          return
        }
      }
    }
  }
  view.value.customFilter[state.filterItem.index].filter = state.filterItem.filter
  view.value.customFilter[state.filterItem.index].logic = state.filterItem.logic
  view.value.customFilter[state.filterItem.index].filterType = state.filterItem.filterType
  view.value.customFilter[state.filterItem.index].enumCheckField = state.filterItem.enumCheckField
  closeResultFilter()
}

const collapseChange = type => {
  canvasCollapse.value[type] = !canvasCollapse.value[type]
}
const openHandler = ref(null)
const initOpenHandler = newWindow => {
  if (openHandler?.value) {
    const pm = {
      methodName: 'initOpenHandler',
      args: newWindow
    }
    openHandler.value.invokeMethod(pm)
  }
}
const addDsWindow = () => {
  const path =
    embeddedStore.getToken && appStore.getIsIframe ? 'dataset-embedded-form' : '/dataset-form'
  let routeData = router.resolve(path)
  const newWindow = window.open(routeData.href, '_blank')
  initOpenHandler(newWindow)
}
const editDs = () => {
  const path =
    embeddedStore.getToken && appStore.getIsIframe ? 'dataset-embedded-form' : '/dataset-form'
  let routeData = router.resolve({
    path: path,
    query: {
      id: view.value.tableId
    }
  })
  const newWindow = window.open(routeData.href, '_blank')
  initOpenHandler(newWindow)
}

const showQuotaEditCompare = item => {
  recordSnapshotInfo('calcData')
  state.quotaItemCompare = JSON.parse(JSON.stringify(item))
  state.showEditQuotaCompare = true
}

const closeQuotaEditCompare = () => {
  state.showEditQuotaCompare = false
}

const saveQuotaEditCompare = () => {
  // 更新指标
  if (state.quotaItemCompare.calcType === 'quota') {
    view.value.yAxis[state.quotaItemCompare.index].compareCalc = state.quotaItemCompare.compareCalc
  } else if (state.quotaItemCompare.calcType === 'quotaExt') {
    view.value.yAxisExt[state.quotaItemCompare.index].compareCalc =
      state.quotaItemCompare.compareCalc
  } else if (state.quotaItemCompare.calcType === 'extLabel') {
    view.value.extLabel[state.quotaItemCompare.index].compareCalc =
      state.quotaItemCompare.compareCalc
  } else if (state.quotaItemCompare.calcType === 'extTooltip') {
    view.value.extTooltip[state.quotaItemCompare.index].compareCalc =
      state.quotaItemCompare.compareCalc
  }
  closeQuotaEditCompare()
}

const valueFormatter = item => {
  recordSnapshotInfo('render')
  state.valueFormatterItem = JSON.parse(JSON.stringify(item))
  state.showValueFormatter = true
}
const closeValueFormatter = () => {
  state.showValueFormatter = false
}
const saveValueFormatter = () => {
  const ele = state.valueFormatterItem.formatterCfg.decimalCount
  if (
    ele === undefined ||
    ele.toString().indexOf('.') > -1 ||
    parseInt(ele).toString() === 'NaN' ||
    parseInt(ele) < 0 ||
    parseInt(ele) > 10
  ) {
    ElMessage.error(t('chart.formatter_decimal_count_error'))
    return
  }
  // 更新指标
  if (state.valueFormatterItem.formatterType === 'quota') {
    view.value.yAxis[state.valueFormatterItem.index].formatterCfg =
      state.valueFormatterItem.formatterCfg
  } else if (state.valueFormatterItem.formatterType === 'quotaExt') {
    view.value.yAxisExt[state.valueFormatterItem.index].formatterCfg =
      state.valueFormatterItem.formatterCfg
  } else if (state.valueFormatterItem.formatterType === 'dimension') {
    view.value.xAxis[state.valueFormatterItem.index].formatterCfg =
      state.valueFormatterItem.formatterCfg
  }
  closeValueFormatter()
}

const addCalcField = groupType => {
  editCalcField.value = true
  isCalcFieldAdd.value = true
  nextTick(() => {
    calcEdit.value.initEdit(
      { groupType, id: guid() },
      state.dimension,
      state.quota.filter(ele => ele.id !== '-1')
    )
  })
}
const editField = item => {
  editCalcField.value = true
  isCalcFieldAdd.value = false
  nextTick(() => {
    calcEdit.value.initEdit(
      item,
      state.dimension,
      state.quota.filter(ele => ele.id !== '-1')
    )
  })
}
const closeEditCalc = () => {
  editCalcField.value = false
}
const confirmEditCalc = () => {
  calcEdit.value.setFieldForm()
  const obj = cloneDeep(calcEdit.value.fieldForm)
  setFieldDefaultValue(obj)
  saveField(obj).then(() => {
    getFields(view.value.tableId, view.value.id, view.value.type)
    closeEditCalc()
  })
}

const chartFieldEdit = param => {
  state.currEditField = JSON.parse(JSON.stringify(param.item))
  switch (param.type) {
    case 'copy':
      setFieldDefaultValue(state.currEditField)
      state.currEditField.id = null
      state.currEditField.originName =
        param.item.extField === 2 ? param.item.originName : '[' + param.item.id + ']'
      state.currEditField.name = getFieldName(state.dimension.concat(state.quota), param.item.name)

      saveField(state.currEditField).then(() => {
        getFields(view.value.tableId, view.value.id, view.value.type)
      })
      break
    case 'edit':
      editField(param.item)
      break
    case 'delete':
      deleteField(param.item?.id).then(() => {
        getFields(view.value.tableId, view.value.id, view.value.type)
      })
      break
  }
}
const handleChartFieldEdit = (item, type) => {
  return {
    type: type,
    item: item
  }
}
const setFieldDefaultValue = field => {
  field.extField = 2
  field.chartId = view.value.id
  field.datasetGroupId = view.value.tableId
  field.dataeaseName = null
  field.lastSyncTime = null
  field.columnIndex = state.dimension.length + state.quota.length
  field.deExtractType = field.deType
}

const el = ref<HTMLElement | null>(null)
const elDrag = ref<HTMLElement | null>(null)
const { y, isDragging } = useDraggable(el, {
  initialValue: { x: 0, y: 0 },
  draggingElement: elDrag
})
const previewHeight = ref(0)
const calcEle = () => {
  nextTick(() => {
    previewHeight.value = (elDrag.value as HTMLDivElement).offsetHeight
    y.value = previewHeight.value / 2 + 200
  })
}

const setCacheId = () => {
  nextTick(() => {
    if (!cacheId || !!view.value.tableId || templateStatusShow.value) return
    view.value.tableId = cacheId as unknown as number
  })
}
watch(
  () => curComponent.value,
  val => {
    if (!val || !!previewHeight.value) return
    calcEle()
  }
)

watch(
  () => curComponent.value,
  val => {
    if (!val) return
    setCacheId()
  }
)

const fieldDHeight = computed(() => {
  const h = y.value - 200
  if (h < 53) {
    return 53
  }
  return h > previewHeight.value - 50 ? previewHeight.value - 50 : h
})

const dragVerticalTop = computed(() => {
  const h = y.value - 200
  if (h < 50) {
    return 50
  }
  return h > previewHeight.value - 53 ? previewHeight.value - 53 : h
})

const onRefreshChange = val => {
  recordSnapshotInfo('render')
  if (val === '' || parseFloat(val).toString() === 'NaN' || parseFloat(val) < 1) {
    ElMessage.error(t('chart.only_input_number'))
    return
  }
}

const isCtrl = ref(false)

const isDraggingItem = ref(false)

const activeDimension = ref<Axis[]>([])
const activeQuota = ref<Axis[]>([])

const setActive = (ele, type = 'dimension') => {
  if (isCtrl.value) {
    isCtrl.value = false
  }
  const activeChild = type === 'dimension' ? activeDimension : activeQuota
  const deactivateChild = type === 'quota' ? activeDimension : activeQuota
  deactivateChild.value = []
  activeChild.value = activeChild.value.some(item => item.id === ele.id) ? [] : [ele]
}

const setActiveCtrl = (ele, type = 'dimension') => {
  isCtrl.value = true
  const activeChild = type === 'dimension' ? activeDimension : activeQuota
  const deactivateChild = type === 'quota' ? activeDimension : activeQuota
  deactivateChild.value = []
  const index = activeChild.value.findIndex(item => item.id === ele.id)
  if (index !== -1) {
    activeChild.value.splice(index, 1)
    return
  }
  activeChild.value.push(ele)
}

const setActiveShift = (ele, type = 'dimension') => {
  const activeChild = type === 'dimension' ? activeDimension : activeQuota
  const deactivateChild = type === 'quota' ? activeDimension : activeQuota
  const dataArr = type === 'dimension' ? dimensionData : quotaData
  deactivateChild.value = []
  const dimensionDataId = dataArr.value.map(ele => ele.id)
  const dimensionDataActiveChild = activeChild.value.filter(ele => dimensionDataId.includes(ele.id))
  if (!dimensionDataActiveChild.length) {
    const index = activeChild.value.findIndex(item => item.id === ele.id)
    if (index !== -1) {
      activeChild.value.splice(index, 1)
      return
    }
    activeChild.value.push(ele)
  } else {
    const startItx = dataArr.value.findIndex(
      item => item.id === dimensionDataActiveChild[dimensionDataActiveChild.length - 1].id
    )
    const endItx = dataArr.value.findIndex(item => item.id === ele.id)
    if (startItx === endItx) return
    if (startItx > endItx) {
      activeChild.value = [...activeChild.value, ...dataArr.value.slice(endItx, startItx)]
    }
    if (startItx < endItx) {
      activeChild.value = [...activeChild.value, ...dataArr.value.slice(startItx + 1, endItx + 1)]
    }
  }
}

const isDrag = ref(false)

const dragStartD = (e: DragEvent) => {
  isDrag.value = true
  setTimeout(() => {
    isDraggingItem.value = true
  }, 0)
}

const singleDragStartD = (e: DragEvent, ele, type) => {
  const activeChild = type === 'dimension' ? activeDimension : activeQuota
  const deactivateChild = type === 'quota' ? activeDimension : activeQuota
  deactivateChild.value = []
  if (!activeChild.value.length) {
    activeChild.value = [unref(ele)]
  }
  startToMove(e, unref(activeDimension.value))
}

const dragStart = (e: DragEvent) => {
  isDrag.value = true
  setTimeout(() => {
    isDraggingItem.value = true
  }, 0)
}

const singleDragStart = (e: DragEvent, ele, type) => {
  const activeChild = type === 'dimension' ? activeDimension : activeQuota
  const deactivateChild = type === 'quota' ? activeDimension : activeQuota
  deactivateChild.value = []
  if (!activeChild.value.length) {
    activeChild.value = [ele]
  }
  e.dataTransfer.setData(
    'quota',
    JSON.stringify(
      activeQuota.value
        .filter(ele => ele.id)
        .map(ele => ({ ...cloneDeep(unref(ele)), datasetId: view.value.tableId }))
    )
  )
}

const dragEnd = () => {
  isDrag.value = false
  isDraggingItem.value = false
}

const singleDragEnd = () => {
  activeDimension.value = []
  activeQuota.value = []
  dragEnd()
}

const dragEnter = (ev: MouseEvent) => {
  ev.preventDefault()
}

const dragOver = (ev: MouseEvent) => {
  ev.preventDefault()
}

const drop = (ev: MouseEvent, type = 'xAxis') => {
  ev.preventDefault()
  const arr = activeDimension.value.length ? activeDimension.value : activeQuota.value
  for (let i = 0; i < arr.length; i++) {
    const obj = cloneDeep(arr[i])
    state.moveId = obj.id as unknown as number
    view.value[type] ??= []
    view.value[type].push(obj)
    const e = { newDraggableIndex: view.value[type].length - 1 }

    if ('drillFields' === type) {
      addDrill(e)
    } else {
      addAxis(e, type as AxisType)
    }
  }
}

const fieldLoading = ref(false)

const copyChartFieldItem = id => {
  fieldLoading.value = true
  copyChartField(id, view.value.id)
    .then(() => {
      getFields(view.value.tableId, view.value.id, view.value.type)
    })
    .catch(() => {
      fieldLoading.value = false
    })
}

const deleteChartFieldItem = id => {
  fieldLoading.value = true
  deleteChartField(id)
    .then(() => {
      getFields(view.value.tableId, view.value.id, view.value.type)
    })
    .catch(() => {
      fieldLoading.value = false
    })
}
</script>

<template>
  <div class="chart-edit" :class="'editor-' + themes" @keydown.stop @keyup.stop>
    <el-row v-loading="loading" class="de-chart-editor">
      <div
        class="content-area"
        :class="{
          'content-area-close': canvasCollapse.chartAreaCollapse,
          'content-area-left-open': !canvasCollapse.chartAreaCollapse
        }"
      >
        <el-icon
          :title="view.title"
          class="custom-icon"
          size="20px"
          @click="collapseChange('chartAreaCollapse')"
        >
          <Fold v-if="canvasCollapse.chartAreaCollapse" class="collapse-icon" />
          <Expand v-else class="collapse-icon" />
        </el-icon>
        <div v-if="canvasCollapse.chartAreaCollapse" class="collapse-title">
          <span style="font-size: 14px">{{ view.title }}</span>
        </div>
        <div v-if="!canvasCollapse.chartAreaCollapse" style="width: 240px" class="view-panel-row">
          <el-row class="editor-title">
            <span
              id="component-name"
              class="name-area"
              :class="{ 'component-name-dark': themes === 'dark' }"
              @dblclick="editComponentName"
              >{{ view.title }}</span
            >
          </el-row>

          <el-row style="height: calc(100vh - 110px); overflow-y: auto">
            <div v-if="view.type === 'VQuery' && curComponent" class="query-style-tab">
              <div style="padding-top: 1px">
                <VQueryChartStyle
                  :element="curComponent"
                  :common-background-pop="curComponent?.commonBackground"
                  :chart="view"
                  :themes="themes"
                />
              </div>
            </div>
            <el-tabs
              v-else
              v-model="tabActive"
              class="tab-header"
              :class="{ dark: themes === 'dark' }"
            >
              <el-tab-pane name="data" :label="t('chart.chart_data')" class="padding-tab">
                <el-container direction="vertical">
                  <el-scrollbar class="has-footer drag_main_area attr-style theme-border-class">
                    <el-row v-if="view.type !== 'rich-text'" class="drag-data padding-lr">
                      <span class="data-area-label">{{ t('chart.switch_chart') }}</span>
                      <el-popover
                        :offset="4"
                        placement="bottom-end"
                        width="434"
                        trigger="click"
                        :append-to-body="true"
                        :popper-class="'chart-type-style-' + themes"
                        :persistent="false"
                      >
                        <template #reference>
                          <el-select
                            v-model="state.useless"
                            popper-class="chart-type-hide-options"
                            class="chart-type-select"
                            :suffix-icon="arrowIcon()"
                            :effect="themes"
                            size="small"
                          >
                            <template #prefix>
                              <Icon
                                class-name="chart-type-select-icon"
                                v-if="state.chartTypeOptions[0]?.isPlugin"
                                :static-content="state.chartTypeOptions[0]?.icon"
                              />
                              <Icon
                                v-else
                                class-name="chart-type-select-icon"
                                :name="state.chartTypeOptions[0]['icon']"
                              />
                            </template>
                            <template #default>
                              <el-option
                                v-for="item in state.chartTypeOptions"
                                :key="item.value"
                                :label="item.title"
                                :value="item.value"
                              />
                            </template>
                          </el-select>
                        </template>
                        <template #default>
                          <chart-type
                            :themes="themes"
                            :type="view.type"
                            @on-type-change="onTypeChange"
                          />
                        </template>
                      </el-popover>
                    </el-row>
                    <template v-if="view.plugin?.isPlugin">
                      <plugin-component
                        :jsname="view.plugin.staticMap['editor-data']"
                        :view="view"
                        :dimension="state.dimension"
                        :quota="state.quota"
                        :themes="themes"
                        :emitter="emitter"
                      />
                    </template>
                    <template v-else>
                      <!--area-->
                      <el-row v-if="showAxis('area')" class="padding-lr drag-data">
                        <span class="data-area-label">
                          {{ t('chart.area') }}
                        </span>
                        <div class="area-tree-select">
                          <el-tree-select
                            ref="areaSelect"
                            v-model="state.areaId"
                            :effect="themes"
                            :data="state.worldTree"
                            :props="treeProps"
                            :filterNodeMethod="filterNode"
                            :current-node-key="state.areaId"
                            :teleported="false"
                            :default-expanded-keys="expandKeys"
                            empty-text="请选择区域"
                            node-key="id"
                            check-strictly
                            filterable
                            @node-click="onAreaChange"
                          />
                        </div>
                      </el-row>
                      <!--xAxis-->
                      <el-row v-if="showAxis('xAxis')" class="padding-lr drag-data">
                        <div class="form-draggable-title">
                          <span>
                            {{ chartViewInstance.axisConfig.xAxis.name }}
                          </span>
                          <el-tooltip
                            :effect="toolTip"
                            placement="top"
                            :content="t('common.delete')"
                          >
                            <el-icon
                              class="remove-icon"
                              :class="{ 'remove-icon--dark': themes === 'dark' }"
                              size="14px"
                              @click="removeItems('xAxis')"
                            >
                              <Icon class-name="inner-class" name="icon_delete-trash_outlined" />
                            </el-icon>
                          </el-tooltip>
                        </div>
                        <div
                          class="qw"
                          @drop="$event => drop($event)"
                          @dragenter="dragEnter"
                          @dragover="$event => dragOver($event)"
                        >
                          <draggable
                            :list="view.xAxis"
                            :move="onMove"
                            item-key="id"
                            group="drag"
                            animation="300"
                            class="drag-block-style"
                            :class="{ dark: themes === 'dark' }"
                            @add="addXaxis"
                          >
                            <template #item="{ element, index }">
                              <dimension-item
                                :dimension-data="state.dimension"
                                :quota-data="state.quota"
                                :chart="view"
                                :item="element"
                                :index="index"
                                :themes="props.themes"
                                type="dimension"
                                @onDimensionItemChange="dimensionItemChange"
                                @onDimensionItemRemove="dimensionItemRemove"
                                @onNameEdit="showRename"
                                @onCustomSort="onCustomSort"
                                @valueFormatter="valueFormatter"
                              />
                            </template>
                          </draggable>
                          <drag-placeholder :themes="themes" :drag-list="view.xAxis" />
                        </div>
                      </el-row>

                      <!--xAxisExt-->
                      <el-row v-if="showAxis('xAxisExt')" class="padding-lr drag-data">
                        <div class="form-draggable-title">
                          <span>
                            {{ chartViewInstance.axisConfig.xAxisExt.name }}
                          </span>
                          <el-tooltip
                            :effect="toolTip"
                            placement="top"
                            :content="t('common.delete')"
                          >
                            <el-icon
                              class="remove-icon"
                              :class="{ 'remove-icon--dark': themes === 'dark' }"
                              size="14px"
                              @click="removeItems('xAxisExt')"
                            >
                              <Icon class-name="inner-class" name="icon_delete-trash_outlined" />
                            </el-icon>
                          </el-tooltip>
                        </div>
                        <div
                          @drop="$event => drop($event, 'xAxisExt')"
                          @dragenter="dragEnter"
                          @dragover="$event => dragOver($event)"
                        >
                          <draggable
                            :list="view.xAxisExt"
                            :move="onMove"
                            item-key="id"
                            group="drag"
                            animation="300"
                            class="drag-block-style"
                            :class="{ dark: themes === 'dark' }"
                            @add="addXaxisExt"
                          >
                            <template #item="{ element, index }">
                              <dimension-item
                                :dimension-data="state.dimension"
                                :quota-data="state.quota"
                                :chart="view"
                                :item="element"
                                :index="index"
                                :themes="props.themes"
                                type="dimensionExt"
                                @onDimensionItemChange="dimensionItemChange"
                                @onDimensionItemRemove="dimensionItemRemove"
                                @onNameEdit="showRename"
                                @onCustomSort="onExtCustomSort"
                              />
                            </template>
                          </draggable>
                          <drag-placeholder :drag-list="view.xAxisExt" />
                        </div>
                      </el-row>

                      <!--flowMapStartName-->
                      <el-row v-if="showAxis('flowMapStartName')" class="padding-lr drag-data">
                        <div class="form-draggable-title">
                          <span>
                            {{ chartViewInstance.axisConfig.flowMapStartName.name }}
                          </span>
                          <el-tooltip
                            :effect="toolTip"
                            placement="top"
                            :content="t('common.delete')"
                          >
                            <el-icon
                              class="remove-icon"
                              :class="{ 'remove-icon--dark': themes === 'dark' }"
                              size="14px"
                              @click="removeItems('flowMapStartName')"
                            >
                              <Icon class-name="inner-class" name="icon_delete-trash_outlined" />
                            </el-icon>
                          </el-tooltip>
                        </div>
                        <div
                          class="qw"
                          @drop="$event => drop($event, 'flowMapStartName')"
                          @dragenter="dragEnter"
                          @dragover="$event => dragOver($event)"
                        >
                          <draggable
                            :list="view.flowMapStartName"
                            :move="onMove"
                            item-key="id"
                            group="drag"
                            animation="300"
                            class="drag-block-style"
                            :class="{ dark: themes === 'dark' }"
                            @add="addFlowMapStartName"
                          >
                            <template #item="{ element, index }">
                              <dimension-item
                                :dimension-data="state.dimension"
                                :quota-data="state.quota"
                                :chart="view"
                                :item="element"
                                :index="index"
                                :themes="props.themes"
                                type="flowMapStartName"
                                @onDimensionItemChange="dimensionItemChange"
                                @onDimensionItemRemove="dimensionItemRemove"
                                @onNameEdit="showRename"
                                @onCustomSort="onCustomFlowMapStartNameSort"
                                @valueFormatter="valueFormatter"
                              />
                            </template>
                          </draggable>
                          <drag-placeholder :themes="themes" :drag-list="view.flowMapStartName" />
                        </div>
                      </el-row>

                      <!--flowMapEndName-->
                      <el-row v-if="showAxis('flowMapEndName')" class="padding-lr drag-data">
                        <div class="form-draggable-title">
                          <span>
                            {{ chartViewInstance.axisConfig.flowMapEndName.name }}
                          </span>
                          <el-tooltip
                            :effect="toolTip"
                            placement="top"
                            :content="t('common.delete')"
                          >
                            <el-icon
                              class="remove-icon"
                              :class="{ 'remove-icon--dark': themes === 'dark' }"
                              size="14px"
                              @click="removeItems('flowMapEndName')"
                            >
                              <Icon class-name="inner-class" name="icon_delete-trash_outlined" />
                            </el-icon>
                          </el-tooltip>
                        </div>
                        <div
                          class="qw"
                          @drop="$event => drop($event, 'flowMapEndName')"
                          @dragenter="dragEnter"
                          @dragover="$event => dragOver($event)"
                        >
                          <draggable
                            :list="view.flowMapEndName"
                            :move="onMove"
                            item-key="id"
                            group="drag"
                            animation="300"
                            class="drag-block-style"
                            :class="{ dark: themes === 'dark' }"
                            @add="addFlowMapEndName"
                          >
                            <template #item="{ element, index }">
                              <dimension-item
                                :dimension-data="state.dimension"
                                :quota-data="state.quota"
                                :chart="view"
                                :item="element"
                                :index="index"
                                :themes="props.themes"
                                type="flowMapEndName"
                                @onDimensionItemChange="dimensionItemChange"
                                @onDimensionItemRemove="dimensionItemRemove"
                                @onNameEdit="showRename"
                                @onCustomSort="onCustomFlowMapEndNameSort"
                                @valueFormatter="valueFormatter"
                              />
                            </template>
                          </draggable>
                          <drag-placeholder :themes="themes" :drag-list="view.flowMapEndName" />
                        </div>
                      </el-row>

                      <!--extStack-->
                      <el-row v-if="showAxis('extStack')" class="padding-lr drag-data">
                        <div class="form-draggable-title">
                          <span>
                            {{ chartViewInstance.axisConfig.extStack.name }}
                          </span>
                          <el-tooltip
                            :effect="toolTip"
                            placement="top"
                            :content="t('common.delete')"
                          >
                            <el-icon
                              class="remove-icon"
                              :class="{ 'remove-icon--dark': themes === 'dark' }"
                              size="14px"
                              @click="removeItems('extStack')"
                            >
                              <Icon class-name="inner-class" name="icon_delete-trash_outlined" />
                            </el-icon>
                          </el-tooltip>
                        </div>
                        <div
                          @drop="$event => drop($event, 'extStack')"
                          @dragenter="dragEnter"
                          @dragover="$event => dragOver($event)"
                        >
                          <draggable
                            :list="view.extStack"
                            :move="onMove"
                            item-key="id"
                            group="drag"
                            animation="300"
                            class="drag-block-style"
                            :class="{ dark: themes === 'dark' }"
                            @add="addExtStack"
                          >
                            <template #item="{ element, index }">
                              <dimension-item
                                :dimension-data="state.dimension"
                                :quota-data="state.quota"
                                :chart="view"
                                :item="element"
                                :index="index"
                                :themes="props.themes"
                                type="dimensionStack"
                                @onDimensionItemChange="dimensionItemChange"
                                @onDimensionItemRemove="dimensionItemRemove"
                                @onNameEdit="showRename"
                                @onCustomSort="onStackCustomSort"
                              />
                            </template>
                          </draggable>
                          <drag-placeholder :drag-list="view.extStack" />
                        </div>
                      </el-row>

                      <template v-if="view.type !== 'bar-range'">
                        <!--yAxis-->
                        <el-row v-if="showAxis('yAxis')" class="padding-lr drag-data">
                          <div class="form-draggable-title">
                            <span>
                              {{ chartViewInstance.axisConfig.yAxis.name }}
                            </span>
                            <el-tooltip
                              :effect="toolTip"
                              placement="top"
                              :content="t('common.delete')"
                            >
                              <el-icon
                                class="remove-icon"
                                :class="{ 'remove-icon--dark': themes === 'dark' }"
                                size="14px"
                                @click="removeItems('yAxis')"
                              >
                                <Icon class-name="inner-class" name="icon_delete-trash_outlined" />
                              </el-icon>
                            </el-tooltip>
                          </div>
                          <div
                            @drop="$event => drop($event, 'yAxis')"
                            @dragenter="dragEnter"
                            @dragover="$event => dragOver($event)"
                          >
                            <draggable
                              :list="view.yAxis"
                              :move="onMove"
                              item-key="id"
                              group="drag"
                              animation="300"
                              class="drag-block-style"
                              :class="{ dark: themes === 'dark' }"
                              @add="addYaxis"
                              @change="e => onAxisChange(e, 'yAxis')"
                            >
                              <template #item="{ element, index }">
                                <quota-item
                                  :dimension-data="state.dimension"
                                  :quota-data="state.quota"
                                  :chart="view"
                                  :item="element"
                                  :index="index"
                                  type="quota"
                                  :themes="props.themes"
                                  @onQuotaItemChange="item => quotaItemChange(item, 'yAxis')"
                                  @onQuotaItemRemove="quotaItemRemove"
                                  @onNameEdit="showRename"
                                  @editItemFilter="showQuotaEditFilter"
                                  @editItemCompare="showQuotaEditCompare"
                                  @valueFormatter="valueFormatter"
                                />
                              </template>
                            </draggable>
                            <drag-placeholder :drag-list="view.yAxis" />
                          </div>
                        </el-row>
                        <!-- xAxisExtRight -->
                        <el-row v-if="showAxis('xAxisExtRight')" class="padding-lr drag-data">
                          <div class="form-draggable-title">
                            <span>
                              {{ chartViewInstance.axisConfig.extBubble.name }}
                            </span>
                            <el-tooltip
                              :effect="toolTip"
                              placement="top"
                              :content="t('common.delete')"
                            >
                              <el-icon
                                class="remove-icon"
                                :class="{ 'remove-icon--dark': themes === 'dark' }"
                                size="14px"
                                @click="removeItems('extBubble')"
                              >
                                <Icon class-name="inner-class" name="icon_delete-trash_outlined" />
                              </el-icon>
                            </el-tooltip>
                          </div>
                          <div
                            @drop="$event => drop($event, 'extBubble')"
                            @dragenter="dragEnter"
                            @dragover="$event => dragOver($event)"
                          >
                            <draggable
                              :list="view.extBubble"
                              :move="onMove"
                              item-key="id"
                              group="drag"
                              animation="300"
                              class="drag-block-style"
                              :class="{ dark: themes === 'dark' }"
                              @add="addExtBubble"
                              @change="e => onAxisChange(e, 'extBubble')"
                            >
                              <template #item="{ element, index }">
                                <dimension-item
                                  :dimension-data="state.dimension"
                                  :quota-data="state.quota"
                                  :chart="view"
                                  :item="element"
                                  :index="index"
                                  :themes="props.themes"
                                  type="xAxisExtRight"
                                  @onDimensionItemChange="dimensionItemChange"
                                  @onDimensionItemRemove="dimensionItemRemove"
                                  @onNameEdit="showRename"
                                  @onCustomSort="onExtCustomRightSort"
                                />
                              </template>
                            </draggable>
                            <drag-placeholder :drag-list="view.extBubble" />
                          </div>
                        </el-row>
                        <!--yAxisExt-->
                        <el-row v-if="showAxis('yAxisExt')" class="padding-lr drag-data">
                          <div class="form-draggable-title">
                            <span>
                              {{ chartViewInstance.axisConfig.yAxisExt.name }}
                            </span>
                            <el-tooltip
                              :effect="toolTip"
                              placement="top"
                              :content="t('common.delete')"
                            >
                              <el-icon
                                class="remove-icon"
                                :class="{ 'remove-icon--dark': themes === 'dark' }"
                                size="14px"
                                @click="removeItems('yAxisExt')"
                              >
                                <Icon class-name="inner-class" name="icon_delete-trash_outlined" />
                              </el-icon>
                            </el-tooltip>
                          </div>
                          <div
                            @drop="$event => drop($event, 'yAxisExt')"
                            @dragenter="dragEnter"
                            @dragover="$event => dragOver($event)"
                          >
                            <draggable
                              :list="view.yAxisExt"
                              :move="onMove"
                              item-key="id"
                              group="drag"
                              animation="300"
                              class="drag-block-style"
                              :class="{ dark: themes === 'dark' }"
                              @add="addYaxisExt"
                              @change="e => onAxisChange(e, 'yAxisExt')"
                            >
                              <template #item="{ element, index }">
                                <quota-item
                                  :dimension-data="state.dimension"
                                  :quota-data="state.quota"
                                  :chart="view"
                                  :item="element"
                                  :index="index"
                                  type="quotaExt"
                                  :themes="props.themes"
                                  @onQuotaItemChange="item => quotaItemChange(item, 'yAxisExt')"
                                  @onQuotaItemRemove="quotaItemRemove"
                                  @onNameEdit="showRename"
                                  @editItemFilter="showQuotaEditFilter"
                                  @editItemCompare="showQuotaEditCompare"
                                  @valueFormatter="valueFormatter"
                                />
                              </template>
                            </draggable>
                            <drag-placeholder :drag-list="view.yAxisExt" />
                          </div>
                        </el-row>
                      </template>
                      <template v-else-if="view.type === 'bar-range'">
                        <!--yAxis-->
                        <el-row v-if="showAxis('yAxis')" class="padding-lr drag-data">
                          <div class="form-draggable-title">
                            <span>
                              {{ chartViewInstance.axisConfig.yAxis.name }}
                            </span>
                            <el-tooltip
                              :effect="toolTip"
                              placement="top"
                              :content="t('common.delete')"
                            >
                              <el-icon
                                class="remove-icon"
                                :class="{ 'remove-icon--dark': themes === 'dark' }"
                                size="14px"
                                @click="removeItems('yAxis')"
                              >
                                <Icon class-name="inner-class" name="icon_delete-trash_outlined" />
                              </el-icon>
                            </el-tooltip>
                          </div>
                          <div
                            @drop="$event => drop($event, 'yAxis')"
                            @dragenter="dragEnter"
                            @dragover="$event => dragOver($event)"
                          >
                            <draggable
                              :list="view.yAxis"
                              :move="onMove"
                              item-key="id"
                              group="drag"
                              animation="300"
                              class="drag-block-style"
                              :class="{ dark: themes === 'dark' }"
                              @add="addYaxis"
                              @change="e => onAxisChange(e, 'yAxis')"
                            >
                              <template #item="{ element, index }">
                                <dimension-item
                                  v-if="element.groupType === 'd'"
                                  :dimension-data="state.dimension"
                                  :quota-data="state.quota"
                                  :chart="view"
                                  :item="element"
                                  :index="index"
                                  :themes="props.themes"
                                  type="quota"
                                  @onDimensionItemChange="dimensionItemChange"
                                  @onDimensionItemRemove="dimensionItemRemove"
                                  @onNameEdit="showRename"
                                  @onCustomSort="onExtCustomSort"
                                />
                                <quota-item
                                  v-else-if="element.groupType === 'q'"
                                  :dimension-data="state.dimension"
                                  :quota-data="state.quota"
                                  :chart="view"
                                  :item="element"
                                  :index="index"
                                  type="quota"
                                  :themes="props.themes"
                                  @onQuotaItemChange="item => quotaItemChange(item, 'yAxis')"
                                  @onQuotaItemRemove="quotaItemRemove"
                                  @onNameEdit="showRename"
                                  @editItemFilter="showQuotaEditFilter"
                                  @editItemCompare="showQuotaEditCompare"
                                  @valueFormatter="valueFormatter"
                                />
                              </template>
                            </draggable>
                            <drag-placeholder :drag-list="view.yAxis" />
                          </div>
                        </el-row>
                        <!--yAxisExt-->
                        <el-row v-if="showAxis('yAxisExt')" class="padding-lr drag-data">
                          <div class="form-draggable-title">
                            <span>
                              {{ chartViewInstance.axisConfig.yAxisExt.name }}
                            </span>
                            <el-tooltip
                              :effect="toolTip"
                              placement="top"
                              :content="t('common.delete')"
                            >
                              <el-icon
                                class="remove-icon"
                                :class="{ 'remove-icon--dark': themes === 'dark' }"
                                size="14px"
                                @click="removeItems('yAxisExt')"
                              >
                                <Icon class-name="inner-class" name="icon_delete-trash_outlined" />
                              </el-icon>
                            </el-tooltip>
                          </div>
                          <div
                            @drop="$event => drop($event, 'yAxisExt')"
                            @dragenter="dragEnter"
                            @dragover="$event => dragOver($event)"
                          >
                            <draggable
                              :list="view.yAxisExt"
                              :move="onMove"
                              item-key="id"
                              group="drag"
                              animation="300"
                              class="drag-block-style"
                              :class="{ dark: themes === 'dark' }"
                              @add="addYaxisExt"
                              @change="e => onAxisChange(e, 'yAxisExt')"
                            >
                              <template #item="{ element, index }">
                                <dimension-item
                                  v-if="element.groupType === 'd'"
                                  :dimension-data="state.dimension"
                                  :quota-data="state.quota"
                                  :chart="view"
                                  :item="element"
                                  :index="index"
                                  :themes="props.themes"
                                  type="quotaExt"
                                  @onDimensionItemChange="dimensionItemChange"
                                  @onDimensionItemRemove="dimensionItemRemove"
                                  @onNameEdit="showRename"
                                  @onCustomSort="onExtCustomSort"
                                />
                                <quota-item
                                  v-else-if="element.groupType === 'q'"
                                  :dimension-data="state.dimension"
                                  :quota-data="state.quota"
                                  :chart="view"
                                  :item="element"
                                  :index="index"
                                  type="quotaExt"
                                  :themes="props.themes"
                                  @onQuotaItemChange="item => quotaItemChange(item, 'yAxisExt')"
                                  @onQuotaItemRemove="quotaItemRemove"
                                  @onNameEdit="showRename"
                                  @editItemFilter="showQuotaEditFilter"
                                  @editItemCompare="showQuotaEditCompare"
                                  @valueFormatter="valueFormatter"
                                />
                              </template>
                            </draggable>
                            <drag-placeholder :drag-list="view.yAxisExt" />
                          </div>
                        </el-row>
                      </template>
                      <!-- extBubble -->
                      <el-row v-if="showAxis('extBubble')" class="padding-lr drag-data">
                        <div class="form-draggable-title">
                          <span class="data-area-label">
                            <span style="margin-right: 4px">
                              {{ chartViewInstance.axisConfig.extBubble.name }}
                            </span>
                            <el-tooltip
                              v-if="chartViewInstance.axisConfig.extBubble.tooltip"
                              class="item"
                              :effect="toolTip"
                              placement="top"
                            >
                              <template #content>
                                <span> {{ chartViewInstance.axisConfig.extBubble.tooltip }}</span>
                              </template>
                              <el-icon
                                class="hint-icon"
                                :class="{ 'hint-icon--dark': themes === 'dark' }"
                              >
                                <Icon name="icon_info_outlined" />
                              </el-icon>
                            </el-tooltip>
                          </span>
                          <el-tooltip
                            :effect="toolTip"
                            placement="top"
                            :content="t('common.delete')"
                          >
                            <el-icon
                              class="remove-icon"
                              :class="{ 'remove-icon--dark': themes === 'dark' }"
                              size="14px"
                              @click="removeItems('extBubble')"
                            >
                              <Icon class-name="inner-class" name="icon_delete-trash_outlined" />
                            </el-icon>
                          </el-tooltip>
                        </div>
                        <div
                          @drop="$event => drop($event, 'extBubble')"
                          @dragenter="dragEnter"
                          @dragover="$event => dragOver($event)"
                        >
                          <draggable
                            :list="view.extBubble"
                            :move="onMove"
                            item-key="id"
                            group="drag"
                            animation="300"
                            class="drag-block-style"
                            :class="{ dark: themes === 'dark' }"
                            @add="addExtBubble"
                            @change="e => onAxisChange(e, 'extBubble')"
                          >
                            <template #item="{ element, index }">
                              <quota-item
                                :dimension-data="state.dimension"
                                :quota-data="state.quota"
                                :chart="view"
                                :item="element"
                                :index="index"
                                type="extBubble"
                                :themes="props.themes"
                                @onQuotaItemChange="item => quotaItemChange(item, 'extBubble')"
                                @onQuotaItemRemove="quotaItemRemove"
                                @onNameEdit="showRename"
                                @editItemFilter="showQuotaEditFilter"
                                @editItemCompare="showQuotaEditCompare"
                                @valueFormatter="valueFormatter"
                              />
                            </template>
                          </draggable>
                          <drag-placeholder :drag-list="view.extBubble" />
                        </div>
                      </el-row>

                      <!--drill-->
                      <el-row v-if="showAxis('drill')" class="padding-lr drag-data">
                        <div class="form-draggable-title">
                          <span class="data-area-label">
                            <span style="margin-right: 4px">
                              {{ t('chart.drill') }} / {{ t('chart.dimension') }}
                            </span>
                            <el-tooltip class="item" :effect="toolTip" placement="top">
                              <template #content>
                                <span> {{ t('chart.drill_dimension_tip') }}</span>
                              </template>
                              <el-icon
                                class="hint-icon"
                                :class="{ 'hint-icon--dark': themes === 'dark' }"
                              >
                                <Icon name="icon_info_outlined" />
                              </el-icon>
                            </el-tooltip>
                          </span>
                          <el-tooltip
                            :effect="toolTip"
                            placement="top"
                            :content="t('common.delete')"
                          >
                            <el-icon
                              class="remove-icon"
                              :class="{ 'remove-icon--dark': themes === 'dark' }"
                              size="14px"
                              @click="removeItems('drillFields')"
                            >
                              <Icon class-name="inner-class" name="icon_delete-trash_outlined" />
                            </el-icon>
                          </el-tooltip>
                        </div>
                        <div
                          @drop="$event => drop($event, 'drillFields')"
                          @dragenter="dragEnter"
                          @dragover="$event => dragOver($event)"
                        >
                          <draggable
                            :list="view.drillFields"
                            item-key="id"
                            group="drag"
                            animation="300"
                            :move="onMove"
                            class="drag-block-style"
                            :class="{ dark: themes === 'dark' }"
                            @add="addDrill"
                          >
                            <template #item="{ element, index }">
                              <drill-item
                                :key="element.id"
                                :index="index"
                                :chart="view"
                                :item="element"
                                :dimension-data="state.dimension"
                                :quota-data="state.quota"
                                :themes="props.themes"
                                @onDimensionItemChange="drillItemChange"
                                @onDimensionItemRemove="drillItemRemove"
                              />
                            </template>
                          </draggable>
                          <drag-placeholder :drag-list="view.drillFields" />
                        </div>
                      </el-row>

                      <!--filter-->
                      <el-row class="padding-lr drag-data no-top-border no-top-padding">
                        <div class="form-draggable-title">
                          <span>
                            {{ t('chart.result_filter') }}
                          </span>
                          <el-tooltip
                            :effect="toolTip"
                            placement="top"
                            :content="t('common.delete')"
                          >
                            <el-icon
                              class="remove-icon"
                              :class="{ 'remove-icon--dark': themes === 'dark' }"
                              size="14px"
                              @click="removeItems('customFilter')"
                            >
                              <Icon class-name="inner-class" name="icon_delete-trash_outlined" />
                            </el-icon>
                          </el-tooltip>
                        </div>
                        <div
                          class="tree-btn"
                          :class="{ 'tree-btn--dark': themes === 'dark', active: isFilterActive }"
                          @click="openTreeFilter"
                        >
                          <el-icon>
                            <Icon class="svg-background" name="icon-filter"></Icon>
                          </el-icon>

                          <span>{{ $t('chart.filter') }}</span>
                        </div>
                      </el-row>

                      <el-row v-if="showAggregate" class="refresh-area">
                        <el-form-item
                          class="form-item no-margin-bottom"
                          :class="'form-item-' + themes"
                        >
                          <el-checkbox
                            v-model="view.aggregate"
                            :effect="themes"
                            size="small"
                            @change="aggregateChange"
                          >
                            {{ t('chart.aggregate_time') }}
                          </el-checkbox>
                        </el-form-item>
                      </el-row>
                    </template>
                  </el-scrollbar>
                  <el-footer :class="{ 'refresh-active-footer': view.refreshViewEnable }">
                    <el-row class="refresh-area">
                      <el-form-item
                        class="form-item no-margin-bottom"
                        :class="'form-item-' + themes"
                      >
                        <el-checkbox
                          v-model="view.refreshViewEnable"
                          :effect="themes"
                          size="small"
                          @change="recordSnapshotInfo('render')"
                        >
                          {{ t('visualization.refresh_frequency') }}
                        </el-checkbox>
                      </el-form-item>
                      <el-row v-if="view.refreshViewEnable">
                        <el-form-item
                          class="form-item no-margin-bottom select-append"
                          :class="'form-item-' + themes"
                        >
                          <el-input
                            v-model.number="view.refreshTime"
                            :effect="themes"
                            :class="[themes === 'dark' && 'dv-dark']"
                            size="small"
                            :min="1"
                            :max="3600"
                            :disabled="!view.refreshViewEnable"
                            @change="onRefreshChange"
                          >
                            <template #append>
                              <el-select
                                v-model="view.refreshUnit"
                                :effect="themes"
                                size="small"
                                placeholder="Select"
                                style="width: 80px"
                                @change="recordSnapshotInfo('render')"
                              >
                                <el-option
                                  :effect="themes"
                                  :label="t('visualization.minute')"
                                  :value="'minute'"
                                />
                                <el-option
                                  :effect="themes"
                                  :label="t('visualization.second')"
                                  :value="'second'"
                                />
                              </el-select>
                            </template>
                          </el-input>
                        </el-form-item>
                      </el-row>
                    </el-row>
                    <el-row class="result-style" :class="'result-style-' + themes">
                      <div class="result-style-input">
                        <span v-if="view.type !== 'richTextView'">
                          {{ t('chart.result_count') }}
                        </span>
                        <span v-if="view.type !== 'richTextView'">
                          <el-radio-group
                            v-model="view.resultMode"
                            :effect="themes"
                            class="radio-span"
                            size="small"
                            @change="recordSnapshotInfo('render')"
                          >
                            <el-radio label="all" :effect="themes">
                              <span class="result-count-label" :class="{ dark: themes === 'dark' }">
                                {{ t('chart.result_mode_all') }}
                              </span>
                            </el-radio>
                            <el-radio label="custom">
                              <el-input-number
                                v-model="view.resultCount"
                                :min="1"
                                :max="1000000"
                                :controls="false"
                                :effect="themes"
                                :step-strictly="true"
                                :value-on-clear="1000"
                                :disabled="view.resultMode === 'all'"
                                class="result-count"
                                size="small"
                                @change="recordSnapshotInfo('calcData')"
                              />
                            </el-radio>
                          </el-radio-group>
                        </span>
                      </div>

                      <el-button
                        :disabled="disableUpdate"
                        type="primary"
                        class="result-style-button"
                        @click="updateChartData(view)"
                      >
                        <span style="font-size: 12px">
                          {{ t('chart.update_chart_data') }}
                        </span>
                      </el-button>
                    </el-row>
                  </el-footer>
                </el-container>
              </el-tab-pane>

              <el-tab-pane
                name="style"
                :label="t('chart.chart_style')"
                class="padding-tab"
                style="width: 100%"
              >
                <el-container direction="vertical">
                  <el-scrollbar class="drag_main_area">
                    <template v-if="view.plugin?.isPlugin">
                      <plugin-component
                        :jsname="view.plugin.staticMap['editor-style']"
                        :view="view"
                        :dimension="state.dimension"
                        :quota="state.quota"
                        :themes="themes"
                        :emitter="emitter"
                      />
                    </template>
                    <template v-else>
                      <chart-style
                        v-if="chartStyleShow"
                        :properties="chartViewInstance.properties"
                        :property-inner-all="chartViewInstance.propertyInner"
                        :selector-spec="chartViewInstance.selectorSpec"
                        :common-background-pop="curComponent?.commonBackground"
                        :chart="view"
                        :themes="themes"
                        :dimension-data="state.dimension"
                        :quota-data="state.quota"
                        :all-fields="allFields"
                        @onColorChange="onColorChange"
                        @onMiscChange="onMiscChange"
                        @onLabelChange="onLabelChange"
                        @onTooltipChange="onTooltipChange"
                        @onChangeXAxisForm="onChangeXAxisForm"
                        @onChangeYAxisForm="onChangeYAxisForm"
                        @onChangeYAxisExtForm="onChangeYAxisExtForm"
                        @onTextChange="onTextChange"
                        @onIndicatorChange="onIndicatorChange"
                        @onIndicatorNameChange="onIndicatorNameChange"
                        @onLegendChange="onLegendChange"
                        @onBackgroundChange="onBackgroundChange"
                        @onBasicStyleChange="onBasicStyleChange"
                        @onTableHeaderChange="onTableHeaderChange"
                        @onTableCellChange="onTableCellChange"
                        @onTableTotalChange="onTableTotalChange"
                        @onChangeMiscStyleForm="onChangeMiscStyleForm"
                        @onExtTooltipChange="onExtTooltipChange"
                        @onChangeQuadrantForm="onChangeQuadrantForm"
                        @onChangeFlowMapLineForm="onChangeFlowMapLineForm"
                        @onChangeFlowMapPointForm="onChangeFlowMapPointForm"
                      />
                    </template>
                  </el-scrollbar>
                </el-container>
              </el-tab-pane>

              <el-tab-pane
                name="senior"
                :label="t('chart.senior')"
                class="padding-tab"
                style="width: 100%"
              >
                <el-container direction="vertical">
                  <el-scrollbar class="drag_main_area">
                    <template v-if="view.plugin?.isPlugin">
                      <plugin-component
                        :jsname="view.plugin.staticMap['editor-senior']"
                        :view="view"
                        :dimension="state.dimension"
                        :quota="state.quota"
                        :themes="themes"
                        :emitter="emitter"
                      />
                    </template>
                    <template v-else>
                      <senior
                        :chart="view"
                        :quota-data="view.yAxis"
                        :quota-ext-data="view.yAxisExt"
                        :fields-data="allFields"
                        :themes="themes"
                        :properties="chartViewInstance.properties"
                        :property-inner-all="chartViewInstance.propertyInner"
                        @onFunctionCfgChange="onFunctionCfgChange"
                        @onAssistLineChange="onAssistLineChange"
                        @onScrollCfgChange="onScrollCfgChange"
                        @onThresholdChange="onThresholdChange"
                        @onMapMappingChange="onMapMappingChange"
                        @onBubbleAnimateChange="onBubbleAnimateChange"
                      />
                    </template>
                  </el-scrollbar>
                </el-container>
              </el-tab-pane>
            </el-tabs>
          </el-row>
        </div>
      </div>
      <div
        class="dataset-main content-area"
        :class="{
          'content-area-close': canvasCollapse.datasetAreaCollapse,
          'content-area-right-open': !canvasCollapse.datasetAreaCollapse
        }"
      >
        <el-icon
          :title="'数据集'"
          class="custom-icon"
          size="20px"
          @click="collapseChange('datasetAreaCollapse')"
        >
          <Fold v-if="canvasCollapse.datasetAreaCollapse" class="collapse-icon" />
          <Expand v-else class="collapse-icon" />
        </el-icon>
        <div v-if="canvasCollapse.datasetAreaCollapse" class="collapse-title">
          <span style="font-size: 14px">数据集</span>
        </div>
        <el-container
          v-if="!canvasCollapse.datasetAreaCollapse"
          direction="vertical"
          class="dataset-area view-panel-row"
        >
          <el-header class="editor-title">
            <span style="font-size: 14px">数据集</span>
          </el-header>
          <el-main class="dataset-main-top">
            <el-row class="dataset-select">
              <dataset-select
                ref="datasetSelector"
                v-model="view.tableId"
                style="flex: 1"
                :view-id="view.id"
                :state-obj="state"
                :themes="themes"
                @add-ds-window="addDsWindow"
                @on-dataset-change="recordSnapshotInfo('calcData')"
              />
              <el-tooltip :effect="toolTip" content="编辑数据集" placement="top">
                <el-icon
                  v-if="curDatasetWeight >= 7 && !isDataEaseBi"
                  class="field-search-icon-btn"
                  :class="{ dark: themes === 'dark' }"
                  style="margin-left: 8px"
                  @click="editDs"
                >
                  <Icon name="icon_edit_outlined" class="el-icon-arrow-down el-icon-delete" />
                </el-icon>
              </el-tooltip>
            </el-row>
            <el-row class="dataset-search padding-lr">
              <div class="dataset-search-label" :class="{ dark: themes === 'dark' }">
                <span>{{ t('chart.field') }}</span>
                <span>
                  <el-tooltip :effect="toolTip" content="刷新" placement="top">
                    <el-icon
                      class="field-search-icon-btn"
                      :class="{ dark: themes === 'dark' }"
                      @click="getFields(view.tableId, view.id, view.type)"
                    >
                      <Icon
                        name="icon_refresh_outlined"
                        class="el-icon-arrow-down el-icon-delete"
                      />
                    </el-icon>
                  </el-tooltip>
                  <el-icon
                    v-if="false"
                    class="field-search-icon-btn"
                    :class="{ dark: themes === 'dark' }"
                    @click="addCalcField('d')"
                  >
                    <Icon name="icon_add_outlined" class="el-icon-arrow-down el-icon-delete"></Icon>
                  </el-icon>
                </span>
              </div>
              <el-input
                v-model="state.searchField"
                size="middle"
                :effect="themes"
                class="dataset-search-input"
                :class="{ dark: themes === 'dark' }"
                :placeholder="t('chart.search') + t('chart.field')"
                clearable
              >
                <template #prefix>
                  <el-icon class="el-input__icon">
                    <Icon name="icon_search-outline_outlined"></Icon>
                  </el-icon>
                </template>
              </el-input>
            </el-row>
            <div
              ref="elDrag"
              v-loading="fieldLoading"
              style="height: calc(100% - 137px); min-height: 120px"
            >
              <div
                class="padding-lr field-height first right-dimension"
                :class="{ dark: themes === 'dark', 'user-select': isDragging }"
                :style="{
                  height: fieldDHeight + 'px'
                }"
              >
                <label>{{ t('chart.dimension') }}</label>
                <el-scrollbar class="drag-list">
                  <div
                    v-for="element in dimensionData"
                    :key="element.id"
                    :draggable="true"
                    class="item father"
                    @click.ctrl="setActiveCtrl(element)"
                    @click.meta="setActiveCtrl(element)"
                    @click.exact="setActive(element)"
                    @click.shift="setActiveShift(element)"
                    @dragstart="$event => singleDragStartD($event, element, 'dimension')"
                    @dragend="singleDragEnd"
                  >
                    <div
                      class="items flex-align-center"
                      :class="[
                        'item-dimension--' + themes,
                        isDraggingItem && 'is-dragging-item',
                        activeDimension.map(itx => itx.id).includes(element.id) && 'active'
                      ]"
                    >
                      <el-icon>
                        <Icon
                          :class-name="`field-icon-${fieldType[element.deType]}`"
                          :name="`field_${fieldType[element.deType]}`"
                        />
                      </el-icon>
                      <span
                        class="field-name ellipsis"
                        :class="{ dark: themes === 'dark' }"
                        :title="element.name"
                        >{{ element.name }}</span
                      >
                      <el-icon
                        v-if="element.id !== '-1' && !element.chartId"
                        class="field-setting child"
                        :class="{ 'remove-icon--dark': themes === 'dark' }"
                        size="14px"
                        @click.stop="copyChartFieldItem(element.id)"
                      >
                        <Icon class-name="inner-class" name="icon_copy_outlined" />
                      </el-icon>
                      <el-icon
                        v-if="element.id !== '-1' && element.chartId"
                        class="field-setting child"
                        :class="{ 'remove-icon--dark': themes === 'dark' }"
                        size="14px"
                        @click.stop="deleteChartFieldItem(element.id)"
                      >
                        <Icon class-name="inner-class" name="icon_delete-trash_outlined" />
                      </el-icon>
                      <el-dropdown
                        v-if="element.id !== '-1' && false"
                        :effect="props.themes"
                        placement="right-start"
                        trigger="click"
                        size="small"
                        class="field-setting child"
                        @command="chartFieldEdit"
                      >
                        <span class="el-dropdown-link">
                          <el-icon class="icon-setting"><Setting /></el-icon>
                        </span>
                        <template #dropdown>
                          <el-dropdown-menu :effect="props.themes">
                            <el-dropdown-item :command="handleChartFieldEdit(element, 'copy')">
                              {{ t('common.copy') }}
                            </el-dropdown-item>
                            <span v-if="element.chartId">
                              <el-dropdown-item :command="handleChartFieldEdit(element, 'edit')">
                                {{ t('common.edit') }}
                              </el-dropdown-item>
                              <el-dropdown-item :command="handleChartFieldEdit(element, 'delete')">
                                {{ t('common.delete') }}
                              </el-dropdown-item>
                            </span>
                          </el-dropdown-menu>
                        </template>
                      </el-dropdown>
                    </div>
                    <div
                      v-if="activeDimension.map(itx => itx.id).includes(element.id)"
                      :draggable="true"
                      class="shadow"
                      :class="isDraggingItem && 'is-dragging'"
                      @dragstart="dragStartD"
                      @dragend="dragEnd"
                    >
                      <template v-if="isDrag">
                        <div
                          v-for="ele in activeDimension"
                          :key="ele.id"
                          class="items flex-align-center"
                        >
                          <el-icon>
                            <Icon
                              :class-name="`field-icon-${fieldType[ele.deType]}`"
                              :name="`field_${fieldType[ele.deType]}`"
                            />
                          </el-icon>
                          <span class="field-name ellipsis" :class="{ dark: themes === 'dark' }">{{
                            ele.name
                          }}</span>
                          <el-dropdown
                            v-if="ele.id !== '-1' && false"
                            :effect="props.themes"
                            placement="right-start"
                            trigger="click"
                            size="small"
                            class="field-setting child"
                            @command="chartFieldEdit"
                          >
                            <span class="el-dropdown-link">
                              <el-icon class="icon-setting"><Setting /></el-icon>
                            </span>
                            <template #dropdown>
                              <el-dropdown-menu :effect="props.themes">
                                <el-dropdown-item :command="handleChartFieldEdit(ele, 'copy')">
                                  {{ t('common.copy') }}
                                </el-dropdown-item>
                                <span v-if="ele.chartId">
                                  <el-dropdown-item :command="handleChartFieldEdit(ele, 'edit')">
                                    {{ t('common.edit') }}
                                  </el-dropdown-item>
                                  <el-dropdown-item :command="handleChartFieldEdit(ele, 'delete')">
                                    {{ t('common.delete') }}
                                  </el-dropdown-item>
                                </span>
                              </el-dropdown-menu>
                            </template>
                          </el-dropdown>
                        </div>
                      </template>
                    </div>
                  </div>
                </el-scrollbar>
                <div
                  ref="el"
                  :style="{
                    top: dragVerticalTop + 'px'
                  }"
                  :class="['drag-vertical', isDragging && 'is-hovering']"
                ></div>
              </div>
              <div
                class="padding-lr field-height right-dimension"
                :class="{ dark: themes === 'dark' }"
              >
                <div class="divider"></div>
                <label>{{ t('chart.quota') }}</label>
                <el-scrollbar class="drag-list">
                  <div
                    v-for="element in quotaData"
                    :key="element.id"
                    class="item father"
                    :draggable="true"
                    @click.ctrl="setActiveCtrl(element, 'quota')"
                    @click.meta="setActiveCtrl(element, 'quota')"
                    @click.exact="setActive(element, 'quota')"
                    @click.shift="setActiveShift(element, 'quota')"
                    @dragstart="$event => singleDragStart($event, element, 'quota')"
                    @dragend="singleDragEnd"
                  >
                    <div
                      class="items flex-align-center"
                      :class="[
                        'item-dimension--' + themes,
                        isDraggingItem && 'is-dragging-item',
                        activeQuota.map(itx => itx.id).includes(element.id) && 'active'
                      ]"
                    >
                      <el-icon>
                        <Icon
                          :class-name="`field-icon-${fieldType[element.deType]}`"
                          :name="`field_${fieldType[element.deType]}`"
                        ></Icon>
                      </el-icon>
                      <span
                        class="field-name ellipsis"
                        :class="{ dark: themes === 'dark' }"
                        :title="element.name"
                        >{{ element.name }}</span
                      >
                      <el-icon
                        v-if="element.id !== '-1' && !element.chartId"
                        class="field-setting child"
                        :class="{ 'remove-icon--dark': themes === 'dark' }"
                        size="14px"
                        @click.stop="copyChartFieldItem(element.id)"
                      >
                        <Icon class-name="inner-class" name="icon_copy_outlined" />
                      </el-icon>
                      <el-icon
                        v-if="element.id !== '-1' && element.chartId"
                        class="field-setting child"
                        :class="{ 'remove-icon--dark': themes === 'dark' }"
                        size="14px"
                        @click.stop="deleteChartFieldItem(element.id)"
                      >
                        <Icon class-name="inner-class" name="icon_delete-trash_outlined" />
                      </el-icon>
                      <el-dropdown
                        v-if="element.id !== '-1' && false"
                        :effect="props.themes"
                        placement="right-start"
                        trigger="click"
                        size="small"
                        class="field-setting child"
                        @command="chartFieldEdit"
                      >
                        <span class="el-dropdown-link">
                          <el-icon class="icon-setting"><Setting /></el-icon>
                        </span>
                        <template #dropdown>
                          <el-dropdown-menu :effect="props.themes">
                            <el-dropdown-item :command="handleChartFieldEdit(element, 'copy')">
                              {{ t('common.copy') }}
                            </el-dropdown-item>
                            <span v-if="element.chartId">
                              <el-dropdown-item :command="handleChartFieldEdit(element, 'edit')">
                                {{ t('common.edit') }}
                              </el-dropdown-item>
                              <el-dropdown-item :command="handleChartFieldEdit(element, 'delete')">
                                {{ t('common.delete') }}
                              </el-dropdown-item>
                            </span>
                          </el-dropdown-menu>
                        </template>
                      </el-dropdown>
                    </div>
                    <div
                      v-if="activeQuota.map(itx => itx.id).includes(element.id)"
                      :draggable="true"
                      class="shadow"
                      :class="isDraggingItem && 'is-dragging'"
                      @dragstart="dragStart"
                      @dragend="dragEnd"
                    >
                      <template v-if="isDrag">
                        <div
                          v-for="ele in activeQuota"
                          :key="ele.id"
                          class="items flex-align-center"
                        >
                          <el-icon>
                            <Icon
                              :class-name="`field-icon-${fieldType[ele.deType]}`"
                              :name="`field_${fieldType[ele.deType]}`"
                            ></Icon>
                          </el-icon>
                          <span class="field-name ellipsis" :class="{ dark: themes === 'dark' }">{{
                            ele.name
                          }}</span>
                          <el-dropdown
                            v-if="ele.id !== '-1' && false"
                            :effect="props.themes"
                            placement="right-start"
                            trigger="click"
                            size="small"
                            class="field-setting child"
                            @command="chartFieldEdit"
                          >
                            <span class="el-dropdown-link">
                              <el-icon class="icon-setting"><Setting /></el-icon>
                            </span>
                            <template #dropdown>
                              <el-dropdown-menu :effect="props.themes">
                                <el-dropdown-item :command="handleChartFieldEdit(ele, 'copy')">
                                  {{ t('common.copy') }}
                                </el-dropdown-item>
                                <span v-if="ele.chartId">
                                  <el-dropdown-item :command="handleChartFieldEdit(ele, 'edit')">
                                    {{ t('common.edit') }}
                                  </el-dropdown-item>
                                  <el-dropdown-item :command="handleChartFieldEdit(ele, 'delete')">
                                    {{ t('common.delete') }}
                                  </el-dropdown-item>
                                </span>
                              </el-dropdown-menu>
                            </template>
                          </el-dropdown>
                        </div>
                      </template>
                    </div>
                  </div>
                </el-scrollbar>
              </div>
            </div>
          </el-main>
        </el-container>
      </div>
    </el-row>
    <chart-template-info v-if="templateStatusShow"></chart-template-info>
    <!--显示名修改-->
    <el-dialog
      v-model="state.renameItem"
      :title="t('chart.show_name_set')"
      :visible="state.renameItem"
      width="420px"
      :close-on-click-modal="false"
    >
      <div @keydown.stop @keyup.stop>
        <el-form
          ref="renameForm"
          label-width="80px"
          require-asterisk-position="right"
          :model="state.itemForm"
          :rules="itemFormRules"
          label-position="top"
          @submit.prevent
        >
          <el-form-item :label="t('dataset.field_origin_name')" class="name-edit-form">
            <span class="text">{{ state.itemForm.name }}</span>
          </el-form-item>
          <el-form-item
            :label="t('chart.show_name')"
            class="name-edit-form no-margin-bottom"
            prop="chartShowName"
          >
            <el-input
              v-model="state.itemForm.chartShowName"
              class="text"
              :maxlength="20"
              clearable
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeRename">{{ t('chart.cancel') }} </el-button>
          <el-button type="primary" @click="saveRename(renameForm)"
            >{{ t('chart.confirm') }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!--指标过滤器-->
    <el-dialog
      v-if="state.quotaFilterEdit"
      v-model="state.quotaFilterEdit"
      :title="t('chart.add_filter')"
      :visible="state.quotaFilterEdit"
      :close-on-click-modal="false"
      width="600px"
      class="dialog-css"
    >
      <quota-filter-editor :item="state.quotaItem" />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeQuotaFilter">{{ t('chart.cancel') }} </el-button>
          <el-button type="primary" @click="saveQuotaFilter">{{ t('chart.confirm') }} </el-button>
        </div>
      </template>
    </el-dialog>
    <el-dialog
      v-if="state.resultFilterEdit"
      v-model="state.resultFilterEdit"
      :title="t('chart.add_filter')"
      :visible="state.resultFilterEdit"
      :close-on-click-modal="false"
      width="600px"
      class="dialog-css"
    >
      <result-filter-editor :chart="state.chartForFilter" :item="state.filterItem" />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeResultFilter">{{ t('chart.cancel') }} </el-button>
          <el-button type="primary" @click="saveResultFilter">{{ t('chart.confirm') }} </el-button>
        </div>
      </template>
    </el-dialog>

    <!--同环比设置-->
    <el-dialog
      v-if="state.showEditQuotaCompare"
      v-model="state.showEditQuotaCompare"
      :title="t('chart.yoy_setting')"
      :visible="state.showEditQuotaCompare"
      :close-on-click-modal="false"
      width="600px"
      class="dialog-css"
    >
      <compare-edit :compare-item="state.quotaItemCompare" :chart="view" />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeQuotaEditCompare">{{ t('chart.cancel') }} </el-button>
          <el-button type="primary" @click="saveQuotaEditCompare">
            {{ t('chart.confirm') }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!--数值格式化-->
    <el-dialog
      v-if="state.showValueFormatter"
      v-model="state.showValueFormatter"
      :title="t('chart.value_formatter') + ' - ' + state.valueFormatterItem.name"
      :visible="state.showValueFormatter"
      :close-on-click-modal="false"
      width="420px"
      class="dialog-css"
    >
      <value-formatter-edit :formatter-item="state.valueFormatterItem" :chart="view" />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeValueFormatter">{{ t('chart.cancel') }} </el-button>
          <el-button type="primary" @click="saveValueFormatter"
            >{{ t('chart.confirm') }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!--xAxis自定义排序-->
    <el-dialog
      v-if="state.showCustomSort"
      v-model="state.showCustomSort"
      :title="t('chart.custom_sort') + t('chart.sort')"
      :visible="state.showCustomSort"
      :close-on-click-modal="false"
      width="372px"
      class="dialog-css custom_sort_dialog"
    >
      <custom-sort-edit
        :chart="view"
        :field-type="customSortAxis"
        :field="state.customSortField"
        @on-sort-change="customSortChange"
      />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeCustomSort">{{ t('chart.cancel') }} </el-button>
          <el-button type="primary" @click="saveCustomSort">{{ t('chart.confirm') }} </el-button>
        </div>
      </template>
    </el-dialog>

    <!--图表计算字段-->
    <el-dialog
      v-model="editCalcField"
      width="1000px"
      :title="isCalcFieldAdd ? t('dataset.add_calc_field') : t('dataset.edit_calc_field')"
      :close-on-click-modal="false"
    >
      <calc-field-edit ref="calcEdit" />
      <template #footer>
        <el-button secondary @click="closeEditCalc()">{{ t('dataset.cancel') }} </el-button>
        <el-button type="primary" @click="confirmEditCalc()">{{ t('dataset.confirm') }} </el-button>
      </template>
    </el-dialog>
  </div>
  <FilterTree ref="filterTree" @filter-data="changeFilterData" />
  <XpackComponent ref="openHandler" jsname="L2NvbXBvbmVudC9lbWJlZGRlZC1pZnJhbWUvT3BlbkhhbmRsZXI=" />
  <Teleport v-if="componentNameEdit" :to="'#component-name'">
    <input
      ref="componentNameInput"
      v-model="inputComponentName"
      :effect="themes"
      width="100%"
      @change="onComponentNameChange"
      @blur="closeEditComponentName"
    />
  </Teleport>
</template>

<style lang="less" scoped>
@import '@/style/mixin.less';

.right-dimension {
  .item {
    width: 100%;
    height: 28px;
    position: relative;
    overflow: hidden;

    & + .item {
      margin-top: 2px;
    }

    .items {
      width: 100%;
      height: 28px;
      border-radius: 4px;
      border: 1px solid transparent;
      color: #a6a6a6;
      font-size: 12px;
      font-style: normal;
      font-weight: 400;
      line-height: 20px;
      float: left;
      padding: 4px 10px;
      position: relative;
      cursor: pointer;
      &:hover {
        background: #1f23291a;
      }

      &.item-dimension--dark:hover {
        background: #ebebeb1a;
      }

      .ed-icon {
        font-size: 14px;
      }
      &.active {
        border-color: var(--ed-color-primary, #3370ff);
      }
    }

    .is-dragging-item {
      z-index: 10;
    }

    .shadow {
      position: absolute;
      width: 100%;
      top: 0;
      left: 0;
      cursor: pointer;
      min-height: 100%;
      z-index: 5;
      .items {
        border-color: var(--ed-color-primary, #3370ff);
        & + .items {
          margin-top: 2px;
        }
      }

      &.is-dragging {
        float: left;
        position: relative;
      }
    }
  }
}
.collapse-icon {
  color: @canvas-main-font-color;
}

.hint-icon {
  cursor: pointer;
  font-size: 14px;
  color: #646a73;

  &.hint-icon--dark {
    color: #a6a6a6;
  }
}

.editor-light {
  border-left: solid 1px @side-outline-border-color-light !important;
  color: @canvas-main-font-color-light!important;
  background-color: @side-area-background-light!important;
  :deep(.ed-tabs__header) {
    border-top: solid 1px @side-outline-border-color-light !important;
  }
  :deep(.drag_main_area) {
    border-top: solid 1px @side-outline-border-color-light !important;
  }
  :deep(.drag-data) {
    border-top: solid 1px @side-outline-border-color-light !important;
  }
  :deep(.result-style) {
    border-top: 1px solid @side-outline-border-color-light !important;
  }
  :deep(.dataset-select) {
    border-top: 1px solid @side-outline-border-color-light !important;
  }
  :deep(.dataset-main) {
    border-left: 1px solid @side-outline-border-color-light !important;
  }
  :deep(input) {
    font-size: 12px;
    font-weight: 400;
  }
  :deep(.divider) {
    background-color: @side-outline-border-color-light !important;
  }
  :deep(.item-span-style) {
    color: @canvas-main-font-color-light!important;
  }

  :deep(.editor-title) {
    color: #1f2329 !important;
  }
  :deep(.collapse-title) {
    color: #1f2329 !important;
  }
  :deep(.collapse-icon) {
    color: #646a73 !important;
  }
  .query-style-tab {
    width: 100%;
    border-top: solid 1px @side-outline-border-color-light !important;

    .tab-container {
      .border-bottom-tab(8px);
    }

    margin-left: 0px !important;

    :deep(.ed-tabs__header) {
      border-top: none !important;
    }

    :deep(.ed-tabs__nav-wrap::after) {
      background-color: rgba(31, 35, 41, 0.15);
    }
    :deep(.ed-tabs__nav-scroll) {
      .ed-tabs__item {
        height: 35px;
        line-height: 35px;
        color: var(--ed-color-primary, #3370ff);
        font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
        font-size: 12px;
        font-style: normal;
        font-weight: 500;
      }
    }
  }
  .query-style-tab {
    width: 100%;
    border-top: solid 1px @side-outline-border-color-light !important;

    .tab-container {
      .border-bottom-tab(8px);
    }

    margin-left: 0px !important;

    :deep(.ed-tabs__header) {
      border-top: none !important;
    }

    :deep(.ed-tabs__nav-wrap::after) {
      background-color: rgba(31, 35, 41, 0.15);
    }
    :deep(.ed-tabs__nav-scroll) {
      .ed-tabs__item {
        height: 35px;
        line-height: 35px;
        color: var(--ed-color-primary, #3370ff);
        font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
        font-size: 12px;
        font-style: normal;
        font-weight: 500;
      }
    }
  }
}

// editor form 全局样式
.editor-dark {
  border-left: solid 1px @main-collapse-border-dark !important;
  .dataset-selector {
    :deep(.ed-input__inner),
    :deep(.ed-input__wrapper),
    :deep(.ed-input.is-disabled .ed-input__wrapper) {
      color: var(--ed-color-white);
      background-color: @side-content-background;
      border: none;
    }
    :deep(.ed-input__inner) {
      border: none;
    }
    :deep(.ed-input__wrapper) {
      box-shadow: 0 0 0 1px hsla(0, 0%, 100%, 0.15) inset !important;
    }
    :deep(.ed-input__wrapper:hover) {
      box-shadow: 0 0 0 1px var(--ed-color-primary, #3370ff) inset !important;
    }
  }
  .query-style-tab {
    width: 100%;
    border-top: solid 1px @main-collapse-border-dark !important;

    .tab-container {
      .border-bottom-tab(8px);
    }

    margin-left: 0px !important;

    :deep(.ed-tabs__header) {
      border-top: none !important;
    }

    :deep(.ed-tabs__nav-wrap::after) {
      background-color: rgba(31, 35, 41, 0.15);
    }
    :deep(.ed-tabs__nav-scroll) {
      .ed-tabs__item {
        height: 35px;
        line-height: 35px;
        color: var(--ed-color-primary, #3370ff);
        font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
        font-size: 12px;
        font-style: normal;
        font-weight: 500;
      }
    }
  }
}

.chart-edit {
  position: relative;
  transition: 0.5s;
  height: 100%;
  color: white;
  background-color: @side-area-background;
}
.ed-row {
  display: block;
}

span {
  font-size: 12px;
}

.de-chart-editor {
  height: 100%;
  overflow-y: hidden;
  width: 100%;
  display: flex;
  transition: 0.5s;
  .padding-lr {
    padding: 0 16px;

    &.no-top-border {
      border-top: none !important;
    }
    &.no-top-padding {
      padding-top: 0 !important;

      :deep(.drag-placeholder-style) {
        top: calc(50% - 8px);
      }
    }
  }
  .view-title-name {
    display: -moz-inline-box;
    display: inline-block;
    width: 130px;
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow: hidden;
    margin-left: 38px;
  }

  .view-panel-row {
    overflow-y: auto;
    overflow-x: hidden;
    height: 100%;

    :deep(.ed-collapse-item__header) {
      height: 36px !important;
      line-height: 36px !important;
      font-size: 12px !important;
      padding: 0 !important;
      font-weight: 500 !important;
      border-top: unset;

      &.is-active {
        border-bottom-color: var(--ed-collapse-border-color);
        color: #1f2329;
      }

      .ed-collapse-item__arrow {
        margin: 0 6px 0 8px;

        &.is-active {
          color: #646a73;
        }
      }
    }

    :deep(.ed-collapse-item__content) {
      padding: 16px 10px 0;
      border: none;
      :deep(.ed-checkbox) {
        height: 20px;
      }
      .ed-checkbox {
        height: 20px;
      }
    }

    :deep(.style-dark) {
      .ed-collapse-item__header {
        &.is-active {
          color: #fff;
        }

        .ed-collapse-item__arrow {
          &.is-active {
            color: #a6a6a6;
          }
        }
      }
    }
    :deep(.ed-collapse-item.ed-collapse--dark .ed-collapse-item__header) {
      border-color: rgba(255, 255, 255, 0.15);

      &.is-active {
        color: #fff;
      }
      .ed-collapse-item__arrow {
        &.is-active {
          color: #a6a6a6;
        }
      }
    }
  }

  .tab-header {
    --ed-tabs-header-height: 34px;
    --custom-tab-color: #646a73;

    :deep(.ed-tabs__nav-wrap::after) {
      background-color: unset;
    }

    &.dark {
      --custom-tab-color: #a6a6a6;
    }

    height: 100%;
    :deep(.ed-tabs__header) {
      border-top: solid 1px @side-outline-border-color;
    }
    :deep(.ed-tabs__item) {
      font-weight: 400;
      font-size: 12px;
      padding: 0 8px !important;
      margin-right: 12px;
      color: var(--custom-tab-color);
    }
    :deep(.is-active) {
      font-weight: 500;
      color: var(--ed-color-primary, #3370ff);
    }

    :deep(.ed-tabs__nav-scroll) {
      padding-left: 0 !important;
    }

    :deep(.ed-tabs__header) {
      margin: 0 !important;
    }

    :deep(.ed-tabs__content) {
      height: calc(100% - 35px);
      overflow-y: auto;
      overflow-x: hidden;
    }
  }

  .field-height {
    height: 50%;

    label {
      color: #646a73;
      font-size: 12px;
      font-style: normal;
      font-weight: 500;
      line-height: 20px;
    }

    &.first {
      border-top: none !important;
      position: relative;
    }

    .drag-vertical {
      width: 100%;
      height: 4px;
      position: absolute;
      left: 0;
      cursor: row-resize;

      &.is-hovering::after,
      &:hover::after {
        width: calc(100% - 32px);
        height: 1px;
        content: '';
        position: absolute;
        left: 16px;
        top: 0;
        background: var(--ed-color-primary, #3370ff);
      }
    }

    &.dark {
      label {
        color: #a6a6a6;
      }
    }

    .divider {
      width: 100%;
      height: 1px;
      padding: 0 16px;
      background-color: rgba(255, 255, 255, 0.15);
    }
  }

  .drag-list {
    height: calc(100% - 26px);
    min-height: 24px;
    //overflow: auto;
    padding: 2px 0;
  }

  .item-dimension {
    padding: 4px 10px;
    margin: 0 2px;
    text-align: left;
    color: #606266;
    display: block;
    word-break: break-all;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    position: relative;
    border-radius: 4px;
    border: 1px solid transparent;

    font-size: 12px;
    font-weight: 400;
    line-height: 20px;
    vertical-align: middle;

    height: 28px;

    cursor: pointer;

    &:hover {
      background: rgba(31, 35, 41, 0.1);
    }

    &.item-dimension--dark {
      &:hover {
        background: rgba(235, 235, 235, 0.1);
      }
    }

    &.sortable-chosen {
      border: 1px solid var(--ed-color-primary, #3370ff);
      background: #fff;

      &:hover {
        background: #fff;
      }

      &.item-dimension--dark {
        background: #1a1a1a;
        &:hover {
          background: #1a1a1a;
        }
      }
    }
  }

  .father .child {
    visibility: hidden;
  }

  .father:hover .child {
    visibility: visible;
  }

  .field-name {
    display: inline-block;
    width: 90px;
    word-break: break-all;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    padding-left: 4px;
    font-weight: 400;
    color: #646a73;

    &.dark {
      color: #a6a6a6;
    }
  }

  .padding-tab {
    padding: 0;
    height: 100%;
    width: 100%;
    display: flex;

    :deep(.ed-scrollbar) {
      &.has-footer {
        height: calc(100% - 81px);
      }
    }

    :deep(.ed-footer) {
      padding: 0;
      height: 114px;
    }
  }

  .result-count {
    width: 60px;

    :deep(.ed-input__wrapper) {
      padding: 1px 2px;
    }
  }

  .result-count :deep(input) {
    padding: 0 4px;
  }

  .data-area-label {
    text-align: left;
    position: relative;
    width: 100%;
    display: flex;
    flex-direction: row;
    align-items: center;
  }

  .form-draggable-title {
    display: flex;
    flex-direction: row;
    flex-wrap: nowrap;
    justify-content: space-between;

    span {
      cursor: default;
    }

    .remove-icon {
      color: #646a73;
      cursor: pointer;
      margin-top: 2px;
      margin-right: 2px;

      &.remove-icon--dark {
        color: #a6a6a6;
      }

      .inner-class {
        font-size: 14px;
      }
    }
  }

  .drag-block-style {
    padding: 2px 0 0 0;
    width: 100%;
    min-height: 32px;
    border-radius: 4px;
    overflow-x: hidden;
    overflow-y: hidden;
    display: block;
    align-items: center;
    border: 1px dashed #bbbfc4;
    background-color: rgba(31, 35, 41, 0.05);
    margin-top: 8px;

    &.dark {
      border: 1px dashed #5f5f5f;
      background-color: rgba(235, 235, 235, 0.05);
    }

    &:has(span) {
      background-color: transparent !important;
    }
  }

  .draggable-group {
    display: block;
    width: 100%;
    height: calc(100% - 6px);
  }

  .ed-input-refresh-time {
    width: calc(50% - 4px) !important;
  }

  .ed-input-refresh-unit {
    margin-left: 8px;
    width: calc(50% - 4px) !important;
  }

  .ed-input-refresh-loading {
    margin-left: 4px;
    font-size: 12px !important;
  }

  .drag-data {
    padding-top: 8px;
    padding-bottom: 16px;

    .tree-btn {
      width: 100%;
      margin-top: 8px;
      background: #fff;
      height: 32px;
      border-radius: 4px;
      border: 1px solid #dcdfe6;
      display: flex;
      color: #cccccc;
      align-items: center;
      cursor: pointer;
      justify-content: center;
      font-size: 12px;
      &.tree-btn--dark {
        background: rgba(235, 235, 235, 0.05);
        border-color: #5f5f5f;
      }

      &.active {
        color: #3370ff;
        border-color: #3370ff;
      }
    }

    &.no-top-border {
      border-top: none !important;
    }
    &.no-top-padding {
      padding-top: 0 !important;
    }
    &:nth-child(n + 2) {
      border-top: 1px solid @side-outline-border-color;
    }
    &:first-child {
      border-top: none !important;
    }
  }

  .editor-title {
    color: @dv-canvas-main-font-color;
    font-weight: 500;
    height: @component-toolbar-height;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 8px;

    span {
      width: calc(100% - 24px);
      overflow-x: hidden;
      text-overflow: ellipsis;
      word-break: break-all;
      white-space: nowrap;
    }
  }

  .ed-tabs {
    --el-tabs-header-height: 38px !important;
  }

  .switch-chart {
    display: flex;
    align-items: center;
    height: 100%;
    justify-content: space-between;
  }

  .dataset-selector :deep(.ed-input__inner) {
    height: 24px;
    width: 110px;
  }

  .result-style {
    position: absolute;
    bottom: 0;
    width: 100%;
    border-top: 1px solid @side-outline-border-color;
  }
  .result-style-dark {
    :deep(.ed-button) {
      color: #ffffff;
      background-color: var(--ed-color-primary, #3370ff);
      border: none;
      border-radius: 0;
    }
    :deep(.ed-button:hover) {
      background-color: var(--ed-color-primary-light-3);
    }
    :deep(.ed-button:active) {
      background-color: var(--ed-color-primary);
    }
  }
  .result-style-input {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 40px;
    padding: 0 6px;

    .result-count-label {
      color: #1f2329;
      font-size: 12px;
      font-weight: 400;

      &.dark {
        color: #fff;
      }
    }
  }
  .result-style-button {
    height: 40px;
    width: 100%;
    border-radius: 0;
  }

  .switch-chart-dark {
    :deep(.ed-button) {
      color: #ffffff;
      background-color: #1a1a1a;
      border: 1px solid hsla(0, 0%, 100%, 0.15);
      border-radius: 2px;
    }
    :deep(.ed-button:hover) {
      border: 1px solid var(--ed-color-primary, #3370ff);
    }
  }

  .dataset-search {
    height: 51px;
    width: 100%;
  }
  .dataset-search-label {
    height: 22px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    color: #1f2329;
    font-weight: 500;

    &.dark {
      color: #ebebeb;
    }
  }
  .field-search-icon-btn {
    font-size: 16px;
    color: #646a73;
    cursor: pointer;
    position: relative;
    &:hover {
      &::after {
        content: '';
        position: absolute;
        width: 24px;
        height: 24px;
        border-radius: 4px;
        top: -4px;
        left: -4px;
        background: rgba(31, 35, 41, 0.1);
      }
    }

    &.dark {
      color: #a6a6a6;
      &:hover {
        &::after {
          background: rgba(235, 235, 235, 0.1);
        }
      }
    }
  }

  .dataset-search-input {
    font-size: 12px;

    :deep(.ed-input__inner) {
      background-color: @side-area-background-light;
      color: @canvas-main-font-color-light;
    }
    :deep(.ed-input__wrapper) {
      box-shadow: none !important;
      border-bottom: 1px solid rgba(31, 35, 41, 0.15);
      background-color: @side-area-background-light;
      border-radius: 0;
      padding: 1px 4px;
    }

    &.dark {
      :deep(.ed-input__inner) {
        background-color: @side-area-background;
        color: #ffffff;
      }
      :deep(.ed-input__wrapper) {
        border-bottom: 1px solid hsla(0, 0%, 100%, 0.15);
        background-color: @side-area-background;
      }
    }
  }
}

.chart_type_area {
  height: 54px;
  overflow: auto;
  padding: 8px;
}

.drag_main_area {
  border-top: 1px solid @side-outline-border-color;
  overflow: auto;
  height: calc(100% - 1px);
}

.collapse-title {
  color: @dv-canvas-main-font-color;
  width: 35px;
  text-align: center;
  padding: 5px;
  margin-top: 30px;
}

.custom-icon {
  position: absolute;
  right: 5px;
  top: 10px;
  cursor: pointer;
  z-index: 2;
}
:deep(.ed-collapse) {
  width: 100%;
  border-top: unset;
}
:deep(.ed-form-item) {
  .ed-radio.ed-radio--small .ed-radio__inner {
    width: 14px;
    height: 14px;
  }
  .ed-input__inner {
    font-size: 12px;
    font-weight: 400;
  }
  .ed-input {
    --ed-input-height: 28px;

    .ed-input__suffix {
      height: 26px;
    }
  }
  .ed-input-number {
    width: 100%;

    .ed-input-number__decrease {
      --ed-input-number-controls-height: 13px;
    }
    .ed-input-number__increase {
      --ed-input-number-controls-height: 13px;
    }

    .ed-input__inner {
      text-align: start;
    }
  }
  .ed-select {
    width: 100%;
    .ed-input__inner {
      height: 26px;
    }
  }
  .ed-checkbox {
    .ed-checkbox__label {
      font-size: 12px;
    }
  }
  .ed-color-picker {
    .ed-color-picker__mask {
      height: 26px;
      width: calc(100% - 2px) !important;
    }
  }
  .ed-radio {
    height: 20px;
    .ed-radio__label {
      font-size: 12px;
      font-style: normal;
      font-weight: 400;
      line-height: 20px;
    }
  }
}
:deep(.ed-form-item__label) {
  color: @canvas-main-font-color;
  font-size: 12px;
  font-style: normal;
  font-weight: 400;

  height: unset;
  line-height: 20px;
  margin-bottom: 8px;
}
:deep(.form-item-dark) {
  .ed-form-item__label {
    color: @canvas-main-font-color-dark;
  }

  &.select-append {
    .ed-input-group__append {
      background-color: transparent;
    }
    .dv-dark {
      & > .ed-input__wrapper {
        background-color: #1a1a1a;
      }
      .ed-input-group__append .ed-select {
        margin: 0 -20px;
      }
    }
  }
}

:deep(.form-item-light) {
  &.select-append {
    .ed-input-group__append {
      padding: 0 20px;
    }
  }
}
:deep(.ed-checkbox__label) {
  color: #1f2329;
  font-size: 12px;
  font-style: normal;
  font-weight: 400;
  line-height: 20px;
}
:deep(.ed-checkbox--dark) {
  .ed-checkbox__label {
    color: @dv-canvas-main-font-color;
  }
}
:deep(.ed-checkbox) {
  color: @canvas-main-font-color;
  font-size: 12px;
}
.dataset-area {
  width: 180px;
  position: relative;
}
.dataset-main {
  border-left: 1px solid @side-outline-border-color;
}

.content-area {
  height: 100%;
  position: relative;
  transition: 0.5s;
  overflow-x: hidden;
}

.content-area-close {
  width: 35px;
}

.content-area-left-open {
  width: 240px;
}

.content-area-right-open {
  width: 179px;
}

.dataset-main-top {
  padding: 0;

  &::-webkit-scrollbar {
    display: none;
  }
}

.dataset-select {
  padding: 8px;
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  border-top: 1px solid rgba(255, 255, 255, 0.15);
}
.style-collapse {
  :deep(.ed-collapse-item__header),
  :deep(.ed-collapse-item__wrap) {
    border-bottom: none !important;
  }
  :deep(.ed-collapse-item__content) {
    padding-left: 0 !important;
    padding-bottom: 10px !important;
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
.field-setting {
  position: absolute;
  right: 8px;
}
.father .child {
  visibility: hidden;
}

.father:hover .child {
  visibility: visible;
}

.icon-setting {
  color: #a6a6a6;
}

.label-icon {
  top: 2px;
  margin-left: 6px;
  font-size: 14px;
  cursor: pointer;
}
.area-tree-select {
  margin-top: 8px;
  :deep(.ed-select) {
    display: block;
  }
}

.chart-type-select {
  width: 100%;
  margin-top: 8px;
  :deep(.ed-input__prefix-inner > div) {
    padding: 0;
    margin: 0;
    border: none;
    .chart-type-select-icon {
      width: 23px;
      height: 16px;
    }
  }
  :deep(.ed-input) {
    height: 28px;
  }
}
.name-edit-form {
  margin-bottom: 16px !important;

  .text {
    font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
    font-size: 14px;
    font-style: normal;
    font-weight: 400;
    line-height: 22px;

    --ed-input-height: 32px;

    :deep(.ed-input__inner) {
      font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
      font-size: 14px;
      font-style: normal;
      font-weight: 400;
      line-height: 22px;
    }
  }

  :deep(.ed-form-item__label) {
    color: #1f2329;

    font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
    font-size: 14px;
    font-style: normal;
    font-weight: 400;
    line-height: 22px;
  }

  &.no-margin-bottom {
    margin-bottom: 0 !important;
  }
}
</style>

<style lang="less">
:deep(.ed-select-dropdown__item) {
  display: flex;
  align-items: center;
}
.chart-type-hide-options {
  display: none;
}

.custom_sort_dialog {
  max-height: calc(100vh - 120px);
  min-height: 152px;

  display: flex;
  flex-direction: column;
  margin: 0;
  position: absolute !important;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);

  .ed-dialog__body {
    flex: 1;
  }
}

.refresh-active-footer {
  height: 150px !important;
}

.refresh-area {
  width: 100%;
  padding: 0 8px;

  .no-margin-bottom {
    margin-bottom: 8px;
  }
}

.name-area {
  position: relative;
  line-height: 24px;
  height: 24px;
  font-size: 14px !important;
  width: 300px;
  overflow: hidden;
  cursor: pointer;
  input {
    position: absolute;
    left: 0;
    width: 100%;
    outline: none;
    border: 1px solid #295acc;
    border-radius: 4px;
    padding: 0 4px;
    height: 100%;
  }
}

.component-name-dark {
  input {
    position: absolute;
    left: 0;
    width: 100%;
    color: @dv-canvas-main-font-color;
    background-color: #050e21;
    outline: none;
    border: 1px solid #295acc;
    border-radius: 4px;
    padding: 0 4px;
    height: 100%;
  }
}
</style>
