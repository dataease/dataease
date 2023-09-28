<script lang="ts" setup>
import { PropType, reactive, ref, watch, toRefs, computed, nextTick, onBeforeMount } from 'vue'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import { Field, getFieldByDQ, saveChart } from '@/api/chart'
import { Tree } from '../../../visualized/data/dataset/form/CreatDsGroup.vue'
import { useEmitt } from '@/hooks/web/useEmitt'
import { ElMessage, ElTreeSelect } from 'element-plus-secondary'
import draggable from 'vuedraggable'
import DimensionItem from './drag-item/DimensionItem.vue'
import { fieldType } from '@/utils/attr'
import QuotaItem from '@/views/chart/components/editor/drag-item/QuotaItem.vue'
import DragPlaceholder from '@/views/chart/components/editor/drag-item/DragPlaceholder.vue'
import FilterItem from '@/views/chart/components/editor/drag-item/FilterItem.vue'
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
import { cloneDeep } from 'lodash-es'
import { deleteField, saveField } from '@/api/dataset'
import { getWorldTree } from '@/api/map'
import chartViewManager from '@/views/chart/components/js/panel'
import DatasetSelect from '@/views/chart/components/editor/dataset-select/DatasetSelect.vue'
import { useDraggable } from '@vueuse/core'

const snapshotStore = snapshotStoreWithOut()
const dvMainStore = dvMainStoreWithOut()
const { canvasCollapse, curComponent, componentData } = storeToRefs(dvMainStore)
const router = useRouter()

const { t } = useI18n()
const loading = ref(false)
const tabActive = ref('data')
const tabActiveVQuery = ref('style')
const datasetSelector = ref(null)
const curDatasetWeight = ref(0)
const renameForm = ref<FormInstance>()

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

const { view, datasetTree } = toRefs(props)

let cacheId = ''

onBeforeMount(() => {
  cacheId = route.query.id as unknown as string
})
const dsFieldDragOptions = { group: { name: 'drag', pull: 'clone' }, sort: true }

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

watch(
  [() => props.view.tableId],
  () => {
    getFields(props.view.tableId, props.view.id)
  },
  { deep: true }
)

watch(
  [() => view.value['tableId']],
  () => {
    const nodeId = view.value['tableId']
    if (!!nodeId) {
      cacheId = nodeId as unknown as string
    }
    const node = datasetSelector?.value?.getNode(nodeId)
    if (node?.data) {
      curDatasetWeight.value = node.data.weight
    }
  },
  { deep: true }
)

watch(
  [() => state.searchField],
  newVal => {
    fieldFilter(newVal[0])
  },
  { deep: true }
)

const chartStyleShow = computed(() => {
  return view.value.type !== 'richText'
})

const chartViewInstance = computed(() => {
  return chartViewManager.getChartView(view.value.render, view.value.type)
})
const showAxis = (axis: AxisType) => chartViewInstance.value?.axis?.includes(axis)
watch(
  () => [view.value.type, view.value],
  newVal => {
    if (showAxis('area')) {
      if (!state.worldTree?.length) {
        getWorldTree().then(res => {
          state.worldTree = [res.data]
          state.areaId = view.value?.customAttr?.map?.id
        })
      } else {
        state.areaId = view.value?.customAttr?.map?.id
      }
    }
    state.chartTypeOptions = [getViewConfig(newVal[0])]
    state.useless = newVal[0]
  },
  { immediate: true, deep: false }
)
const treeProps = {
  label: 'name',
  children: 'children'
}
const filterNode = (value, data) => {
  if (!value) {
    return true
  }
  return data.name?.includes(value)
}

const getFields = (id, chartId) => {
  if (id && chartId) {
    getFieldByDQ(id, chartId)
      .then(res => {
        state.dimension = (res.dimensionList as unknown as Field[]) || []
        state.quota = (res.quotaList as unknown as Field[]) || []
        state.dimensionData = JSON.parse(JSON.stringify(state.dimension))
        state.quotaData = JSON.parse(JSON.stringify(state.quota))
      })
      .catch(e => {
        state.dimension = []
        state.quota = []
        state.dimensionData = []
        state.quotaData = []
      })
  } else {
    state.dimension = []
    state.quota = []
    state.dimensionData = []
    state.quotaData = []
  }
}

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
  if (view.value?.type === 'table-info') {
    return state.quotaData?.filter(item => item.id !== '-1')
  }
  return state.quotaData
})

const startToMove = (e, item) => {
  e.dataTransfer.setData('dimension', JSON.stringify({ ...item, datasetId: view.value.tableId }))
}

const fieldFilter = val => {
  if (val && val !== '') {
    state.dimensionData = JSON.parse(
      JSON.stringify(
        state.dimension.filter(ele => {
          return ele.name.toLocaleLowerCase().includes(val.toLocaleLowerCase())
        })
      )
    )
    state.quotaData = JSON.parse(
      JSON.stringify(
        state.quota.filter(ele => {
          return ele.name.toLocaleLowerCase().includes(val.toLocaleLowerCase())
        })
      )
    )
  } else {
    state.dimensionData = JSON.parse(JSON.stringify(state.dimension))
    state.quotaData = JSON.parse(JSON.stringify(state.quota))
  }
}

const reset = () => {
  // do reset
}

const dimensionItemChange = item => {
  // do dimensionItemChange
}
const dimensionItemRemove = item => {
  if (item.removeType === 'dimension') {
    view.value.xAxis.splice(item.index, 1)
  } else if (item.removeType === 'dimensionExt') {
    view.value.xAxisExt.splice(item.index, 1)
  } else if (item.removeType === 'dimensionStack') {
    view.value.extStack.splice(item.index, 1)
  }
}

const quotaItemChange = item => {
  // do quotaItemChange
}
const quotaItemRemove = item => {
  if (item.removeType === 'quota') {
    view.value.yAxis.splice(item.index, 1)
  } else if (item.removeType === 'quotaExt') {
    view.value.yAxisExt.splice(item.index, 1)
  } else if (item.removeType === 'extLabel') {
    view.value.extLabel.splice(item.index, 1)
  } else if (item.removeType === 'extTooltip') {
    view.value.extTooltip.splice(item.index, 1)
  } else if (item.removeType === 'extBubble') {
    view.value.extBubble.splice(item.index, 1)
  }
}

const drillItemChange = item => {
  calcData(view.value, true)
}
const drillItemRemove = item => {
  view.value.drillFields.splice(item.index, 1)
  calcData(view.value, true)
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
  state.customSortField = view.value.xAxis[item.index]
  customSortAxis.value = 'xAxis'
  customSort()
}

const onStackCustomSort = item => {
  state.customSortField = view.value.extStack[item.index]
  customSortAxis.value = 'extStack'
  customSort()
}

const onExtCustomSort = item => {
  state.customSortField = view.value.xAxisExt[item.index]
  customSortAxis.value = 'xAxisExt'
  customSort()
}

const onMove = (e, originalEvent) => {
  state.moveId = e.draggedContext.element.id
  return true
}
// drag
const dragCheckType = (list, type) => {
  if (list && list.length > 0) {
    var valid = true
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

const addAxis = (e, axis: AxisType) => {
  const axisSpec = chartViewInstance.value.axisConfig[axis]
  if (!axisSpec) {
    return
  }
  const { type, limit, duplicate } = axisSpec
  if (type) {
    dragCheckType(view.value[axis], type)
  }
  if (!duplicate) {
    dragMoveDuplicate(view.value[axis], e, 'chart')
  }
  if (limit) {
    view.value[axis] = view.value[axis].splice(0, limit)
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

const addExtBubble = e => {
  addAxis(e, 'extBubble')
}

const addDrill = e => {
  dragCheckType(view.value.drillFields, 'd')
  dragMoveDuplicate(view.value.drillFields, e, '')
  dragRemoveAggField(view.value.drillFields, e)
}

const addCustomFilter = e => {
  // 记录数等自动生成字段不做为过滤条件
  if (view.value.customFilter && view.value.customFilter.length > 0) {
    for (let i = 0; i < view.value.customFilter.length; i++) {
      if (view.value.customFilter[i].id === 'count') {
        view.value.customFilter.splice(i, 1)
      }
    }
  }
  view.value.customFilter[e.newDraggableIndex].filter = []
  dragMoveDuplicate(view.value.customFilter, e, '')
  dragRemoveAggField(view.value.customFilter, e)
}
const filterItemRemove = item => {
  view.value.customFilter.splice(item.index, 1)
}

const cloneItem = param => {
  return cloneDeep(param)
}
const moveToDimension = e => {
  dragMoveDuplicate(state.dimensionData, e, 'ds')
}
const moveToQuota = e => {
  dragMoveDuplicate(state.quotaData, e, 'ds')
}

const calcData = (view, resetDrill = false, updateQuery = '') => {
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

const renderChart = view => {
  useEmitt().emitter.emit('renderChart-' + view.id, view)
  snapshotStore.recordSnapshotCache('renderChart', view.id)
}

const onAreaChange = val => {
  view.value.customAttr.map = { id: val.id, level: val.level }
  renderChart(view.value)
}

const onTypeChange = (render, type) => {
  view.value.render = render
  view.value.type = type
  // 处理配置项默认值，不同视图的同一配置项默认值不同
  const chartViewInstance = chartViewManager.getChartView(view.value.render, view.value.type)
  if (chartViewInstance) {
    view.value = chartViewInstance.setupDefaultOptions(view.value) as unknown as ChartObj
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

const onLabelChange = val => {
  view.value.customAttr.label = val
  renderChart(view.value)
}

const onTooltipChange = val => {
  view.value.customAttr.tooltip = val
  renderChart(view.value)
}

const onChangeXAxisForm = val => {
  view.value.customStyle.xAxis = val
  renderChart(view.value)
}

const onChangeYAxisForm = val => {
  view.value.customStyle.yAxis = val
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
  view.value.senior.assistLine = val.data
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

const onScrollCfgChange = val => {
  view.value.senior.scrollCfg = val
  renderChart(view.value)
}

const showRename = val => {
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
  _type: 'xAxis' | 'xAxisExt' | 'extStack' | 'yAxis' | 'extBubble' | 'customFilter' | 'drillFields'
) => {
  switch (_type) {
    case 'xAxis':
      view.value.xAxis = []
      break
    case 'xAxisExt':
      view.value.xAxisExt = []
      break
    case 'extStack':
      view.value.extStack = []
      break
    case 'yAxis':
      view.value.yAxis = []
      break
    case 'extBubble':
      view.value.extBubble = []
      break
    case 'customFilter':
      view.value.customFilter = []
      break
    case 'drillFields':
      view.value.drillFields = []
      break
  }
}

const saveRename = ref => {
  if (!ref) return
  ref.validate(valid => {
    if (valid) {
      const { renameType, index, chartShowName } = state.itemForm
      switch (renameType) {
        case 'quota':
          view.value.yAxis[index].chartShowName = chartShowName
          break
        case 'dimension':
          view.value.xAxis[index].chartShowName = chartShowName
          break
        case 'quotaExt':
          view.value.yAxisExt[index].chartShowName = chartShowName
          break
        case 'dimensionExt':
          view.value.xAxisExt[index].chartShowName = chartShowName
          break
        case 'dimensionStack':
          view.value.extStack[index].chartShowName = chartShowName
          break
        case 'extBubble':
          view.value.extBubble[index].chartShowName = chartShowName
          break
        case 'extLabel':
          view.value.extLabel[index].chartShowName = chartShowName
          break
        case 'extTooltip':
          view.value.extTooltip[index].chartShowName = chartShowName
          break
        default:
          break
      }
      closeRename()
    } else {
      return false
    }
  })
}

const save = () => {
  saveChart(view.value)
}

const showQuotaEditFilter = item => {
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

const showEditFilter = item => {
  state.filterItem = JSON.parse(JSON.stringify(item))
  state.chartForFilter = JSON.parse(JSON.stringify(view.value))
  if (!state.filterItem.logic) {
    state.filterItem.logic = 'and'
  }
  if (!state.filterItem.filterType) {
    state.filterItem.filterType = 'logic'
  }
  if (!state.filterItem.enumCheckField) {
    state.filterItem.enumCheckField = []
  }
  state.resultFilterEdit = true
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

const editDs = () => {
  let routeData = router.resolve({
    path: '/dataset-form',
    query: {
      id: view.value.tableId
    }
  })
  window.open(routeData.href, '_blank')
}

const showQuotaEditCompare = item => {
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
  saveField(obj).then(res => {
    getFields(view.value.tableId, view.value.id)
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

      saveField(state.currEditField).then(res => {
        getFields(view.value.tableId, view.value.id)
      })
      break
    case 'edit':
      editField(param.item)
      break
    case 'delete':
      deleteField(param.item?.id).then(res => {
        getFields(view.value.tableId, view.value.id)
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

const dynamicLabelShow = () => {
  onLabelChange(view.value.customAttr.label)
}

const autoInsert = element => {
  var myValue = '[' + element.id + ']'
  const myField = document.querySelector('#dynamic-label')
  if (myField.selectionStart || myField.selectionStart === 0) {
    var startPos = myField.selectionStart //选区开始位置
    var endPos = myField.selectionEnd //选区结束位置
    view.value.customAttr.label.formatter =
      myField.value.substring(0, startPos) +
      myValue +
      myField.value.substring(endPos, myField.value.length)
    nextTick() //修改数据之后立即使用这个方法获取更新后的DOM。
    myField.focus()
    myField.setSelectionRange(endPos + myValue.length, endPos + myValue.length)
  } else {
    view.value.customAttr.label.formatter += myValue
  }
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
    if (!cacheId || !!view.value.tableId) return
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
          <Fold class="collapse-icon" v-if="canvasCollapse.chartAreaCollapse" />
          <Expand class="collapse-icon" v-else />
        </el-icon>
        <div class="collapse-title" v-if="canvasCollapse.chartAreaCollapse">
          <span style="font-size: 14px">{{ view.title }}</span>
        </div>
        <div v-if="!canvasCollapse.chartAreaCollapse" style="width: 240px" class="view-panel-row">
          <el-row class="editor-title">
            <span style="font-size: 14px">{{ view.title }}</span>
          </el-row>

          <el-row style="height: calc(100vh - 110px)">
            <div class="query-style-tab" v-if="view.type === 'VQuery'">
              <div class="tab-container" style="width: 100%">
                <el-tabs v-model="tabActiveVQuery">
                  <el-tab-pane name="style" :label="t('chart.chart_style')"> </el-tab-pane>
                </el-tabs>
              </div>
              <div style="padding-top: 1px">
                <VQueryChartStyle :chart="view" :themes="themes" />
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
                    <el-row
                      v-if="props.themes !== 'dark' && view.type !== 'rich-text'"
                      class="drag-data padding-lr"
                    >
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
                            popper-class="chart-type-hide-options"
                            class="chart-type-select"
                            v-model="state.useless"
                            size="small"
                          >
                            <template #prefix>
                              <Icon
                                class-name="chart-type-select-icon"
                                :name="state.chartTypeOptions[0].icon"
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
                            @onTypeChange="onTypeChange"
                          />
                        </template>
                      </el-popover>
                    </el-row>
                    <!--area-->
                    <el-row class="padding-lr drag-data" v-if="showAxis('area')">
                      <span class="data-area-label">
                        {{ t('chart.area') }}
                      </span>
                      <div class="area-tree-select">
                        <el-tree-select
                          v-model="state.areaId"
                          :effect="themes"
                          :data="state.worldTree"
                          :props="treeProps"
                          :filterNodeMethod="filterNode"
                          :current-node-key="state.areaId"
                          @node-click="onAreaChange"
                          empty-text="请选择区域"
                          node-key="id"
                          check-strictly
                          filterable
                          :teleported="false"
                        />
                      </div>
                    </el-row>

                    <!--xAxis-->
                    <el-row class="padding-lr drag-data" v-if="showAxis('xAxis')">
                      <div class="form-draggable-title">
                        <span>
                          {{ chartViewInstance.axisConfig.xAxis.name }}
                        </span>
                        <el-tooltip effect="dark" placement="top" :content="t('common.delete')">
                          <el-icon class="remove-icon" @click="removeItems('xAxis')">
                            <Icon name="icon_delete-trash_outlined" />
                          </el-icon>
                        </el-tooltip>
                      </div>
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
                      <drag-placeholder :drag-list="view.xAxis" />
                    </el-row>

                    <!--xAxisExt-->
                    <el-row class="padding-lr drag-data" v-if="showAxis('xAxisExt')">
                      <div class="form-draggable-title">
                        <span>
                          {{ chartViewInstance.axisConfig.xAxisExt.name }}
                        </span>
                        <el-tooltip effect="dark" placement="top" :content="t('common.delete')">
                          <el-icon class="remove-icon" @click="removeItems('xAxisExt')">
                            <Icon name="icon_delete-trash_outlined" />
                          </el-icon>
                        </el-tooltip>
                      </div>
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
                    </el-row>

                    <!--extStack-->
                    <el-row class="padding-lr drag-data" v-if="showAxis('extStack')">
                      <div class="form-draggable-title">
                        <span>
                          {{ chartViewInstance.axisConfig.extStack.name }}
                        </span>
                        <el-tooltip effect="dark" placement="top" :content="t('common.delete')">
                          <el-icon class="remove-icon" @click="removeItems('extStack')">
                            <Icon name="icon_delete-trash_outlined" />
                          </el-icon>
                        </el-tooltip>
                      </div>
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
                    </el-row>

                    <!--yAxis-->
                    <el-row class="padding-lr drag-data" v-if="showAxis('yAxis')">
                      <div class="form-draggable-title">
                        <span>
                          {{ chartViewInstance.axisConfig.yAxis.name }}
                        </span>
                        <el-tooltip effect="dark" placement="top" :content="t('common.delete')">
                          <el-icon class="remove-icon" @click="removeItems('yAxis')">
                            <Icon name="icon_delete-trash_outlined" />
                          </el-icon>
                        </el-tooltip>
                      </div>
                      <draggable
                        :list="view.yAxis"
                        :move="onMove"
                        item-key="id"
                        group="drag"
                        animation="300"
                        class="drag-block-style"
                        :class="{ dark: themes === 'dark' }"
                        @add="addYaxis"
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
                            @onQuotaItemChange="quotaItemChange"
                            @onQuotaItemRemove="quotaItemRemove"
                            @onNameEdit="showRename"
                            @editItemFilter="showQuotaEditFilter"
                            @editItemCompare="showQuotaEditCompare"
                            @valueFormatter="valueFormatter"
                          />
                        </template>
                      </draggable>
                      <drag-placeholder :drag-list="view.yAxis" />
                    </el-row>
                    <!-- extBubble -->
                    <el-row class="padding-lr drag-data" v-if="showAxis('extBubble')">
                      <div class="form-draggable-title">
                        <span>
                          {{ chartViewInstance.axisConfig.extBubble.name }}
                        </span>
                        <el-tooltip effect="dark" placement="top" :content="t('common.delete')">
                          <el-icon class="remove-icon" @click="removeItems('extBubble')">
                            <Icon name="icon_delete-trash_outlined" />
                          </el-icon>
                        </el-tooltip>
                      </div>
                      <draggable
                        :list="view.extBubble"
                        :move="onMove"
                        item-key="id"
                        group="drag"
                        animation="300"
                        class="drag-block-style"
                        :class="{ dark: themes === 'dark' }"
                        @add="addExtBubble"
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
                            @onQuotaItemChange="quotaItemChange"
                            @onQuotaItemRemove="quotaItemRemove"
                            @onNameEdit="showRename"
                            @editItemFilter="showQuotaEditFilter"
                            @editItemCompare="showQuotaEditCompare"
                            @valueFormatter="valueFormatter"
                          />
                        </template>
                      </draggable>
                      <drag-placeholder :drag-list="view.extBubble" />
                    </el-row>

                    <!--drill-->
                    <el-row class="padding-lr drag-data" v-if="showAxis('drill')">
                      <div class="form-draggable-title">
                        <span class="data-area-label">
                          <span>{{ t('chart.drill') }}</span>
                          /
                          <span>{{ t('chart.dimension') }}</span>
                          <el-tooltip class="item" :effect="themes" placement="bottom">
                            <template #content>
                              <div>
                                {{ t('chart.drill_dimension_tip') }}
                              </div>
                            </template>
                            <i
                              class="el-icon-info"
                              :style="{ cursor: 'pointer', color: '#606266' }"
                            />
                          </el-tooltip>
                        </span>
                        <el-tooltip effect="dark" placement="top" :content="t('common.delete')">
                          <el-icon class="remove-icon" @click="removeItems('drillFields')">
                            <Icon name="icon_delete-trash_outlined" />
                          </el-icon>
                        </el-tooltip>
                      </div>
                      <draggable
                        :list="view.drillFields"
                        item-key="id"
                        group="drag"
                        animation="300"
                        :move="onMove"
                        class="drag-block-style"
                        :class="{ dark: themes === 'dark' }"
                        @add="addDrill"
                        @update="calcData(view, true)"
                      >
                        <template #item="{ element, index }">
                          <drill-item
                            :key="element.id"
                            :index="index"
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
                    </el-row>

                    <!--filter-->
                    <el-row class="padding-lr drag-data no-top-border no-top-padding">
                      <div class="form-draggable-title">
                        <span>
                          {{ t('chart.result_filter') }}
                        </span>
                        <el-tooltip effect="dark" placement="top" :content="t('common.delete')">
                          <el-icon class="remove-icon" @click="removeItems('customFilter')">
                            <Icon name="icon_delete-trash_outlined" />
                          </el-icon>
                        </el-tooltip>
                      </div>
                      <draggable
                        :list="view.customFilter"
                        :move="onMove"
                        item-key="id"
                        group="drag"
                        animation="300"
                        class="drag-block-style"
                        :class="{ dark: themes === 'dark' }"
                        @add="addCustomFilter"
                      >
                        <template #item="{ element, index }">
                          <filter-item
                            :dimension-data="state.dimension"
                            :quota-data="state.quota"
                            :item="element"
                            :index="index"
                            :themes="props.themes"
                            @onFilterItemRemove="filterItemRemove"
                            @editItemFilter="showEditFilter"
                          />
                        </template>
                      </draggable>
                      <drag-placeholder :drag-list="view.customFilter" />
                    </el-row>
                  </el-scrollbar>
                  <el-footer :class="{ 'refresh-active-footer': view.refreshViewEnable }">
                    <el-row class="refresh-area">
                      <el-form-item
                        class="form-item no-margin-bottom"
                        :class="'form-item-' + themes"
                      >
                        <el-checkbox :effect="themes" size="small" v-model="view.refreshViewEnable">
                          {{ t('visualization.refresh_frequency') }}
                        </el-checkbox>
                      </el-form-item>
                      <el-row v-if="view.refreshViewEnable">
                        <el-form-item
                          class="form-item no-margin-bottom select-append"
                          :class="'form-item-' + themes"
                        >
                          <el-input
                            v-model="view.refreshTime"
                            :effect="themes"
                            size="small"
                            :min="1"
                            :max="3600"
                            :disabled="!view.refreshViewEnable"
                          >
                            <template #append>
                              <el-select
                                :effect="themes"
                                v-model="view.refreshUnit"
                                size="small"
                                placeholder="Select"
                                style="width: 80px; padding: 0 9px"
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
                            :effect="themes"
                            v-model="view.resultMode"
                            class="radio-span"
                            size="small"
                          >
                            <el-radio label="all" :effect="themes">
                              <span class="result-count-label" :class="{ dark: themes === 'dark' }">
                                {{ t('chart.result_mode_all') }}
                              </span>
                            </el-radio>
                            <el-radio label="custom">
                              <el-input-number
                                :min="1"
                                :controls="false"
                                :effect="themes"
                                :step-strictly="true"
                                v-model="view.resultCount"
                                class="result-count"
                                size="small"
                                :value-on-clear="1000"
                                :disabled="view.resultMode === 'all'"
                              />
                            </el-radio>
                          </el-radio-group>
                        </span>
                      </div>

                      <el-button
                        type="primary"
                        class="result-style-button"
                        @click="calcData(view, true, 'updateQuery')"
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
                    <chart-style
                      v-if="chartStyleShow"
                      :properties="chartViewInstance.properties"
                      :property-inner-all="chartViewInstance.propertyInner"
                      :chart="view"
                      :themes="themes"
                      :dimension-data="state.dimension"
                      :quota-data="state.quota"
                      @onColorChange="onColorChange"
                      @onMiscChange="onMiscChange"
                      @onLabelChange="onLabelChange"
                      @onTooltipChange="onTooltipChange"
                      @onChangeXAxisForm="onChangeXAxisForm"
                      @onChangeYAxisForm="onChangeYAxisForm"
                      @onTextChange="onTextChange"
                      @onLegendChange="onLegendChange"
                      @onBackgroundChange="onBackgroundChange"
                      @onBasicStyleChange="onBasicStyleChange"
                      @onTableHeaderChange="onTableHeaderChange"
                      @onTableCellChange="onTableCellChange"
                      @onTableTotalChange="onTableTotalChange"
                      @onChangeMiscStyleForm="onChangeMiscStyleForm"
                    />
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
                    <senior
                      :chart="view"
                      :quota-data="view.yAxis"
                      :themes="themes"
                      :properties="chartViewInstance.properties"
                      :property-inner-all="chartViewInstance.propertyInner"
                      @onFunctionCfgChange="onFunctionCfgChange"
                      @onAssistLineChange="onAssistLineChange"
                      @onScrollCfgChange="onScrollCfgChange"
                      @onThresholdChange="onThresholdChange"
                    />
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
          <Fold class="collapse-icon" v-if="canvasCollapse.datasetAreaCollapse" />
          <Expand class="collapse-icon" v-else />
        </el-icon>
        <div class="collapse-title" v-if="canvasCollapse.datasetAreaCollapse">
          <span style="font-size: 14px">数据集</span>
        </div>
        <el-container
          direction="vertical"
          v-if="!canvasCollapse.datasetAreaCollapse"
          class="dataset-area view-panel-row"
        >
          <el-header class="editor-title">
            <span style="font-size: 14px">数据集</span>
          </el-header>
          <el-main class="dataset-main-top">
            <el-row class="dataset-select">
              <dataset-select
                ref="datasetSelector"
                style="flex: 1"
                :view-id="view.id"
                :state-obj="state"
                v-model="view.tableId"
                :themes="themes"
              />
              <el-icon
                class="field-search-icon-btn"
                :class="{ dark: themes === 'dark' }"
                style="margin-left: 6px"
                @click="editDs"
                v-if="curDatasetWeight >= 7"
              >
                <Icon name="icon_edit_outlined" class="el-icon-arrow-down el-icon-delete" />
              </el-icon>
            </el-row>
            <el-row class="dataset-search padding-lr">
              <div class="dataset-search-label" :class="{ dark: themes === 'dark' }">
                <span>{{ t('chart.field') }}</span>
                <span>
                  <el-icon
                    class="field-search-icon-btn"
                    :class="{ dark: themes === 'dark' }"
                    @click="getFields(view.tableId, view.id)"
                  >
                    <Icon name="icon_refresh_outlined" class="el-icon-arrow-down el-icon-delete" />
                  </el-icon>
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
                size="middle"
                :effect="themes"
                v-model="state.searchField"
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
            <div ref="elDrag" style="height: calc(100% - 137px); min-height: 120px">
              <div
                class="padding-lr field-height first"
                :class="{ dark: themes === 'dark', 'user-select': isDragging }"
                :style="{
                  height: fieldDHeight + 'px'
                }"
              >
                <label>{{ t('chart.dimension') }}</label>
                <el-scrollbar class="drag-list">
                  <draggable
                    :list="state.dimensionData"
                    :group="dsFieldDragOptions.group"
                    :move="onMove"
                    item-key="id"
                    animation="300"
                    :clone="cloneItem"
                    @add="moveToDimension"
                  >
                    <template #item="{ element }">
                      <span
                        @dragstart="$event => startToMove($event, element)"
                        :draggable="true"
                        class="item-dimension father"
                        :class="'item-dimension--' + themes"
                        :title="element.name"
                      >
                        <el-icon>
                          <Icon
                            :className="`field-icon-${fieldType[element.deType]}`"
                            :name="`field_${fieldType[element.deType]}`"
                          />
                        </el-icon>
                        <span class="field-name" :class="{ dark: themes === 'dark' }">{{
                          element.name
                        }}</span>
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
                                <el-dropdown-item
                                  :command="handleChartFieldEdit(element, 'delete')"
                                >
                                  {{ t('common.delete') }}
                                </el-dropdown-item>
                              </span>
                            </el-dropdown-menu>
                          </template>
                        </el-dropdown>
                      </span>
                    </template>
                  </draggable>
                </el-scrollbar>
                <div
                  ref="el"
                  :style="{
                    top: dragVerticalTop + 'px'
                  }"
                  :class="['drag-vertical', isDragging && 'is-hovering']"
                ></div>
              </div>
              <div class="padding-lr field-height" :class="{ dark: themes === 'dark' }">
                <label>{{ t('chart.quota') }}</label>
                <el-scrollbar class="drag-list">
                  <draggable
                    :list="quotaData"
                    :group="dsFieldDragOptions.group"
                    :move="onMove"
                    item-key="id"
                    animation="300"
                    :clone="cloneItem"
                    @add="moveToQuota"
                  >
                    <template #item="{ element }">
                      <span
                        @dragstart="$event => startToMove($event, element)"
                        :draggable="true"
                        class="item-dimension father"
                        :class="'item-dimension--' + themes"
                        :title="element.name"
                      >
                        <el-icon>
                          <Icon
                            :className="`field-icon-${fieldType[element.deType]}`"
                            :name="`field_${fieldType[element.deType]}`"
                          ></Icon>
                        </el-icon>
                        <span class="field-name" :class="{ dark: themes === 'dark' }">{{
                          element.name
                        }}</span>
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
                                <el-dropdown-item
                                  :command="handleChartFieldEdit(element, 'delete')"
                                >
                                  {{ t('common.delete') }}
                                </el-dropdown-item>
                              </span>
                            </el-dropdown-menu>
                          </template>
                        </el-dropdown>
                      </span>
                    </template>
                  </draggable>
                </el-scrollbar>
              </div>
            </div>
          </el-main>
        </el-container>
      </div>
    </el-row>

    <!--显示名修改-->
    <el-dialog
      :title="t('chart.show_name_set')"
      :visible="state.renameItem"
      v-model="state.renameItem"
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
          @submit.prevent
          label-position="top"
        >
          <el-form-item :label="t('dataset.field_origin_name')" class="name-edit-form">
            <span style="font-size: 14px">{{ state.itemForm.name }}</span>
          </el-form-item>
          <el-form-item :label="t('chart.show_name')" class="name-edit-form" prop="chartShowName">
            <el-input v-model="state.itemForm.chartShowName" clearable />
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
      v-model="state.quotaFilterEdit"
      v-if="state.quotaFilterEdit"
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
      v-model="state.resultFilterEdit"
      v-if="state.resultFilterEdit"
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
      v-model="state.showEditQuotaCompare"
      v-if="state.showEditQuotaCompare"
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
      v-model="state.showValueFormatter"
      v-if="state.showValueFormatter"
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
      v-model="state.showCustomSort"
      v-if="state.showCustomSort"
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
        @onSortChange="customSortChange"
      />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeCustomSort">{{ t('chart.cancel') }} </el-button>
          <el-button type="primary" @click="saveCustomSort">{{ t('chart.confirm') }} </el-button>
        </div>
      </template>
    </el-dialog>

    <!--视图计算字段-->
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
</template>

<style lang="less" scoped>
@import '@/style/mixin.less';
.collapse-icon {
  color: @canvas-main-font-color;
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
  :deep(.field-height) {
    border-top: 1px solid @side-outline-border-color-light !important;
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
        color: #3370ff;
        font-family: PingFang SC;
        font-size: 12px;
        font-style: normal;
        font-weight: 500;
      }
    }
  }
}

// editor form 全局样式
.editor-dark .dataset-selector {
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
    box-shadow: 0 0 0 1px var(--ed-color-primary) inset !important;
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
    padding: 0 8px;

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

      &.is-active {
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
      padding: 16px 8px 0;
      border: none;
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
      color: #3370ff;
    }

    :deep(.ed-tabs__nav-scroll) {
      padding-left: 0 !important;
    }

    :deep(.ed-tabs__header) {
      margin: 0 !important;
    }

    :deep(.ed-tabs__content) {
      height: calc(100% - 33px);
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

      &.is-hovering,
      &:hover {
        background: #3370ff;
      }
    }

    &.dark {
      label {
        color: #a6a6a6;
      }
    }

    &:nth-child(n + 2) {
      border-top: 1px solid #363636;
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
  }

  :deep(.sortable-chosen) {
    border: 1px solid #3370ff;
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
    position: absolute;
    top: 2px;
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
    display: inline-block;
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
      color: #a6a6a6;
      cursor: pointer;
      padding: 2px;
      font-size: 16px;
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
      background-color: var(--ed-color-primary);
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
      border: 1px solid #3370ff;
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

    &.dark {
      color: #a6a6a6;
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
      height: 26px !important;
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
}
:deep(.form-item-dark) {
  .ed-form-item__label {
    color: @canvas-main-font-color-dark;
  }

  &.select-append {
    .ed-input-group__append {
      background-color: transparent;
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
  width: 180px;
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
  border-top: 1px solid #363636;
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
  :deep(.ed-select__prefix--light) {
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
  margin-bottom: 8px !important;

  :deep(.ed-form-item__label) {
    color: #1f2329;
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
  min-height: 336px;

  display: flex;
  flex-direction: column;
  margin: 0;
  position: absolute;
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
</style>
