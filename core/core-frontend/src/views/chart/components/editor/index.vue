<script lang="ts" setup>
import { PropType, reactive, ref, watch, toRefs, computed, nextTick, shallowRef, toRaw } from 'vue'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import { Field, getFieldByDQ, saveChart } from '@/api/chart'
import { Tree } from '../../../visualized/data/dataset/form/CreatDsGroup.vue'
import { useEmitt } from '@/hooks/web/useEmitt'
import { ElMessage, ElTreeSelect } from 'element-plus-secondary'
import draggable from 'vuedraggable'
import DimensionLabel from './drag-label/DimensionLabel.vue'
import DimensionItem from './drag-item/DimensionItem.vue'
import { fieldType } from '@/utils/attr'
import QuotaLabel from './drag-label/QuotaLabel.vue'
import QuotaItem from '@/views/chart/components/editor/drag-item/QuotaItem.vue'
import DragPlaceholder from '@/views/chart/components/editor/drag-item/DragPlaceholder.vue'
import FilterItem from '@/views/chart/components/editor/drag-item/FilterItem.vue'
import ChartStyle from '@/views/chart/components/editor/editor-style/ChartStyle.vue'
import VQueryChartStyle from '@/views/chart/components/editor/editor-style/VQueryChartStyle.vue'
import Senior from '@/views/chart/components/editor/editor-senior/Senior.vue'
import QuotaFilterEditor from '@/views/chart/components/editor/filter/QuotaFilterEditor.vue'
import ResultFilterEditor from '@/views/chart/components/editor/filter/ResultFilterEditor.vue'
import { ElIcon, ElRow } from 'element-plus-secondary'
import DrillItem from '@/views/chart/components/editor/drag-item/DrillItem.vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { BASE_VIEW_CONFIG, getViewConfig } from '@/views/chart/components/editor/util/chart'
import ChartType from '@/views/chart/components/editor/chart-type/ChartType.vue'
import { useRouter } from 'vue-router'
import CompareEdit from '@/views/chart/components/editor/drag-item/components/CompareEdit.vue'
import ValueFormatterEdit from '@/views/chart/components/editor/drag-item/components/ValueFormatterEdit.vue'
import CustomSortEdit from '@/views/chart/components/editor/drag-item/components/CustomSortEdit.vue'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import CalcFieldEdit from '@/views/visualized/data/dataset/form/CalcFieldEdit.vue'
import { getFieldName, guid } from '@/views/visualized/data/dataset/form/util'
import { cloneDeep } from 'lodash-es'
import { deleteField, deleteFieldByChartId, saveField } from '@/api/dataset'
import LabelSelector from '@/views/chart/components/editor/editor-style/components/LabelSelector.vue'
import { getWorldTree } from '@/api/map'
import chartViewManager from '@/views/chart/components/js/panel'

const snapshotStore = snapshotStoreWithOut()
const dvMainStore = dvMainStoreWithOut()
const { canvasCollapse, curComponent } = storeToRefs(dvMainStore)
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

const { view, datasetTree } = toRefs(props)

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
    chartShowName: ''
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
  areaNode: {
    id: view.value.customAttr.map.id,
    name: ''
  },
  areaId: view.value.customAttr.map.id,
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
    const node = datasetSelector?.value.getNode(nodeId)
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
  () => view.value.type,
  newVal => {
    if (showAxis('area') && !state.worldTree?.length) {
      getWorldTree().then(res => {
        state.worldTree = [res.data]
      })
    }
    state.chartTypeOptions = [getViewConfig(newVal)]
    state.useless = newVal
  },
  { immediate: true }
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
  if (id) {
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

const dsSelectProps = {
  label: 'name',
  children: 'children',
  value: 'id',
  isLeaf: node => !node.children?.length
}

const dsClick = (data: Tree) => {
  if (data.leaf) {
    // deleteFieldByChartId(view.value.id).then(() => {
    getFields(data.id, view.value.id)
    // })
  }
}

const reset = () => {
  console.log('click reset')
}

const dimensionItemChange = item => {
  // this.calcData(true)
  // console.log(item)
  // console.log(view.value.xaxis)
  // calcData(view.value)
}
const dimensionItemRemove = item => {
  if (item.removeType === 'dimension') {
    view.value.xAxis.splice(item.index, 1)
  } else if (item.removeType === 'dimensionExt') {
    view.value.xAxisExt.splice(item.index, 1)
  } else if (item.removeType === 'dimensionStack') {
    view.value.extStack.splice(item.index, 1)
  }
  // calcData(view.value)
}

const quotaItemChange = item => {
  // this.calcData(true)
  // console.log(item)
  // console.log(view.value.xaxis)
  // calcData(view.value)
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
  calcData(view.value)
}
const drillItemRemove = item => {
  view.value.drillFields.splice(item.index, 1)
  calcData(view.value)
}

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
  view.value.xAxis.forEach(ele => {
    if (ele.id === state.customSortField.id) {
      ele.sort = 'custom_sort'
      ele.customSort = state.customSortList
    }
  })
  closeCustomSort()
}
const onCustomSort = item => {
  state.customSortField = view.value.xAxis[item.index]
  customSort()
}

const onMove = (e, originalEvent) => {
  state.moveId = e.draggedContext.element.id
  return true
}
// drag
const dragCheckType = (list, type) => {
  if (list && list.length > 0) {
    for (let i = 0; i < list.length; i++) {
      if (list[i].groupType !== type) {
        list.splice(i, 1)
      }
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

const addExtLabel = e => {
  dragCheckType(view.value.extLabel, 'q')
  dragMoveDuplicate(view.value.extLabel, e, '')
}

const addExtTooltip = e => {
  dragCheckType(view.value.extTooltip, 'q')
  dragMoveDuplicate(view.value.extTooltip, e, '')
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

const moveToDimension = e => {
  dragMoveDuplicate(state.dimensionData, e, 'ds')
}
const moveToQuota = e => {
  dragMoveDuplicate(state.quotaData, e, 'ds')
}

const calcData = (view, resetDrill = false) => {
  if (resetDrill) {
    useEmitt().emitter.emit('resetDrill-' + view.id, 0)
  } else {
    useEmitt().emitter.emit('calcData-' + view.id, view)
  }
  snapshotStore.recordSnapshotCache('calcData', view.id)
}

const renderChart = view => {
  useEmitt().emitter.emit('renderChart-' + view.id, view)
  snapshotStore.recordSnapshotCache('renderChart', view.id)
}

const onAreaChange = val => {
  view.value.customAttr.map = { id: val.id, level: val.level }
  renderChart(view.value)
}

const onTypeChange = val => {
  view.value.type = val
  // 处理配置项默认值，不同视图的同一配置项默认值不同
  const chartViewInstance = chartViewManager.getChartView(view.value.render, view.value.type)
  if (chartViewInstance) {
    view.value = chartViewInstance.setupDefaultOptions(view.value) as unknown as ChartObj
  }
  calcData(view.value)
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
  renderChart(view.value)
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

const saveRename = ref => {
  if (!ref) return
  ref.validate(valid => {
    if (valid) {
      if (state.itemForm.renameType === 'quota') {
        view.value.yAxis[state.itemForm.index].chartShowName = state.itemForm.chartShowName
      } else if (state.itemForm.renameType === 'dimension') {
        view.value.xAxis[state.itemForm.index].chartShowName = state.itemForm.chartShowName
      } else if (state.itemForm.renameType === 'quotaExt') {
        view.value.yAxisExt[state.itemForm.index].chartShowName = state.itemForm.chartShowName
      } else if (state.itemForm.renameType === 'dimensionExt') {
        view.value.xAxisExt[state.itemForm.index].chartShowName = state.itemForm.chartShowName
      } else if (state.itemForm.renameType === 'extLabel') {
        view.value.extLabel[state.itemForm.index].chartShowName = state.itemForm.chartShowName
      } else if (state.itemForm.renameType === 'extTooltip') {
        view.value.extTooltip[state.itemForm.index].chartShowName = state.itemForm.chartShowName
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
    if (isNaN(f.value)) {
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
  console.log(view)
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
        if (isNaN(f.value)) {
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
</script>

<template>
  <div class="chart-edit" :class="'editor-' + themes">
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
        <div class="collapse-title" v-show="canvasCollapse.chartAreaCollapse">
          <span style="font-size: 14px">{{ view.title }}</span>
        </div>
        <div v-show="!canvasCollapse.chartAreaCollapse" style="width: 240px" class="view-panel-row">
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
                <VQueryChartStyle :chart="view" :themes="themes"></VQueryChartStyle>
              </div>
            </div>
            <el-tabs v-else v-model="tabActive" :stretch="true" class="tab-header">
              <el-tab-pane name="data" :label="t('chart.chart_data')" class="padding-tab">
                <el-col>
                  <div class="drag_main_area attr-style theme-border-class">
                    <el-row style="height: 100%">
                      <div style="height: calc(100% - 80px); overflow: auto">
                        <el-row v-if="props.themes !== 'dark'" class="drag-data padding-lr">
                          <span class="data-area-label">{{ t('chart.switch_chart') }}</span>
                          <el-popover
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
                        <el-row class="padding-lr drag-data" v-show="showAxis('area')">
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
                              @node-click="onAreaChange"
                              empty-text="请选择区域"
                              node-key="id"
                              check-strictly
                              filterable
                              :persistent="false"
                            />
                          </div>
                        </el-row>

                        <!--xAxis-->
                        <el-row class="padding-lr drag-data" v-if="showAxis('xAxis')">
                          <span class="data-area-label">
                            {{ chartViewInstance.axisConfig.xAxis.name }}
                          </span>
                          <draggable
                            :list="view.xAxis"
                            :move="onMove"
                            item-key="id"
                            group="drag"
                            animation="300"
                            class="drag-block-style"
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
                              />
                            </template>
                          </draggable>
                          <drag-placeholder :drag-list="view.xAxis" />
                        </el-row>

                        <!--xAxisExt-->
                        <el-row class="padding-lr drag-data" v-if="showAxis('xAxisExt')">
                          <span class="data-area-label">
                            {{ chartViewInstance.axisConfig.xAxisExt.name }}
                          </span>
                          <draggable
                            :list="view.xAxisExt"
                            :move="onMove"
                            item-key="id"
                            group="drag"
                            animation="300"
                            class="drag-block-style"
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
                                @onCustomSort="onCustomSort"
                              />
                            </template>
                          </draggable>
                          <drag-placeholder :drag-list="view.xAxisExt" />
                        </el-row>

                        <!--extStack-->
                        <el-row class="padding-lr drag-data" v-if="showAxis('extStack')">
                          <span class="data-area-label">
                            {{ chartViewInstance.axisConfig.extStack.name }}
                          </span>
                          <draggable
                            :list="view.extStack"
                            :move="onMove"
                            item-key="id"
                            group="drag"
                            animation="300"
                            class="drag-block-style"
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
                                @onCustomSort="onCustomSort"
                              />
                            </template>
                          </draggable>
                          <drag-placeholder :drag-list="view.extStack" />
                        </el-row>

                        <!--yAxis-->
                        <el-row class="padding-lr drag-data" v-if="showAxis('yAxis')">
                          <span class="data-area-label">
                            {{ chartViewInstance.axisConfig.yAxis.name }}
                          </span>
                          <draggable
                            :list="view.yAxis"
                            :move="onMove"
                            item-key="id"
                            group="drag"
                            animation="300"
                            class="drag-block-style"
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
                          <span class="data-area-label">
                            {{ chartViewInstance.axisConfig.extBubble.name }}
                          </span>
                          <draggable
                            :list="view.extBubble"
                            :move="onMove"
                            item-key="id"
                            group="drag"
                            animation="300"
                            class="drag-block-style"
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
                          <draggable
                            :list="view.drillFields"
                            item-key="id"
                            group="drag"
                            animation="300"
                            :move="onMove"
                            class="drag-block-style"
                            @add="addDrill"
                            @update="calcData(view)"
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
                        <el-row class="padding-lr drag-data">
                          <span>{{ t('chart.result_filter') }}</span>
                          <draggable
                            :list="view.customFilter"
                            :move="onMove"
                            item-key="id"
                            group="drag"
                            animation="300"
                            class="drag-block-style"
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

                        <!--extLabel等-->
                        <el-collapse
                          v-if="showAxis('extLabel') || showAxis('extTooltip')"
                          v-model="state.extData"
                          class="style-collapse"
                        >
                          <el-collapse-item
                            :effect="themes"
                            name="extLabel"
                            :title="t('chart.more_settings')"
                          >
                            <!--extLabel-->
                            <el-row class="padding-lr drag-data" v-if="showAxis('extLabel')">
                              <span class="data-area-label">
                                <span>{{ t('chart.label') }}</span>
                                <el-popover placement="left-start" :width="400" trigger="click">
                                  <template #reference>
                                    <el-icon class="icon-setting label-icon"><Setting /></el-icon>
                                  </template>
                                  <div>
                                    <el-checkbox
                                      v-model="view.customAttr.label.show"
                                      :label="t('commons.show')"
                                      size="small"
                                      @change="dynamicLabelShow"
                                    />
                                    <label-selector
                                      :property-inner="
                                        chartViewInstance.propertyInner['label-selector']
                                      "
                                      themes="light"
                                      class="attr-selector"
                                      :chart="view"
                                      @onLabelChange="onLabelChange"
                                    />
                                    <el-input
                                      id="dynamic-label"
                                      v-model="view.customAttr.label.formatter"
                                      :autosize="{ minRows: 2, maxRows: 4 }"
                                      type="textarea"
                                      placeholder="Please input"
                                      style="padding: 0 16px"
                                    />
                                  </div>
                                </el-popover>
                              </span>
                              <draggable
                                :list="view.extLabel"
                                :move="onMove"
                                item-key="id"
                                group="drag"
                                animation="300"
                                class="drag-block-style"
                                @add="addExtLabel"
                              >
                                <template #item="{ element, index }">
                                  <quota-item
                                    :dimension-data="state.dimension"
                                    :quota-data="state.quota"
                                    :chart="view"
                                    :item="element"
                                    :index="index"
                                    type="extLabel"
                                    :themes="props.themes"
                                    @onQuotaItemChange="quotaItemChange"
                                    @onQuotaItemRemove="quotaItemRemove"
                                    @onNameEdit="showRename"
                                    @editItemFilter="showQuotaEditFilter"
                                    @editItemCompare="showQuotaEditCompare"
                                    @valueFormatter="valueFormatter"
                                    @click="autoInsert(element)"
                                  />
                                </template>
                              </draggable>
                              <drag-placeholder :drag-list="view.extLabel" />
                            </el-row>

                            <!--extTooltip-->
                            <el-row class="padding-lr drag-data" v-if="showAxis('extTooltip')">
                              <span class="data-area-label">
                                <span>{{ t('chart.tooltip') }}</span>
                              </span>
                              <draggable
                                :list="view.extTooltip"
                                :move="onMove"
                                item-key="id"
                                group="drag"
                                animation="300"
                                class="drag-block-style"
                                @add="addExtTooltip"
                              >
                                <template #item="{ element, index }">
                                  <quota-item
                                    :dimension-data="state.dimension"
                                    :quota-data="state.quota"
                                    :chart="view"
                                    :item="element"
                                    :index="index"
                                    type="extTooltip"
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
                              <drag-placeholder :drag-list="view.extTooltip" />
                            </el-row>
                          </el-collapse-item>
                        </el-collapse>
                      </div>

                      <el-row class="result-style" :class="'result-style-' + themes">
                        <div class="result-style-input">
                          <span v-show="view.type !== 'richTextView'">
                            {{ t('chart.result_count') }}
                          </span>
                          <span v-show="view.type !== 'richTextView'">
                            <el-radio-group
                              :effect="themes"
                              v-model="view.resultMode"
                              class="radio-span"
                              size="small"
                            >
                              <el-radio label="all"
                                ><span>{{ t('chart.result_mode_all') }}</span></el-radio
                              >
                              <el-radio label="custom">
                                <el-input-number
                                  :min="1"
                                  :controls="false"
                                  :effect="themes"
                                  :step-strictly="true"
                                  v-model="view.resultCount"
                                  class="result-count"
                                  size="small"
                                />
                              </el-radio>
                            </el-radio-group>
                          </span>
                        </div>
                        <el-button
                          type="primary"
                          class="result-style-button"
                          @click="calcData(view)"
                        >
                          <span style="font-size: 12px">
                            {{ t('chart.update_chart_data') }}
                          </span>
                        </el-button>
                      </el-row>
                    </el-row>
                  </div>
                </el-col>
              </el-tab-pane>

              <el-tab-pane
                name="style"
                :label="t('chart.chart_style')"
                class="padding-tab"
                style="width: 100%"
              >
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
              </el-tab-pane>

              <el-tab-pane
                name="senior"
                :label="t('chart.senior')"
                class="padding-tab"
                style="width: 100%"
              >
                <senior
                  :chart="view"
                  :quota-data="view.yAxis"
                  :themes="themes"
                  @onFunctionCfgChange="onFunctionCfgChange"
                  @onAssistLineChange="onAssistLineChange"
                  @onScrollCfgChange="onScrollCfgChange"
                  @onThresholdChange="onThresholdChange"
                />
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
        <div class="collapse-title" v-show="canvasCollapse.datasetAreaCollapse">
          <span style="font-size: 14px">数据集</span>
        </div>
        <div v-show="!canvasCollapse.datasetAreaCollapse" class="dataset-area view-panel-row">
          <el-row class="editor-title">
            <span style="font-size: 14px">数据集</span>
          </el-row>
          <el-row class="dataset-select">
            <el-tree-select
              ref="datasetSelector"
              node-key="id"
              v-model="view.tableId"
              :data="datasetTree"
              :teleported="false"
              :props="dsSelectProps"
              :render-after-expand="false"
              filterable
              @node-click="dsClick"
              class="dataset-selector"
            >
              <template #default="{ node, data }">
                <el-icon v-if="!data.leaf">
                  <Icon name="dv-folder"></Icon>
                </el-icon>
                <el-icon v-if="data.leaf">
                  <Icon name="icon_dataset"></Icon>
                </el-icon>
                <span style="margin-left: 4px" :title="node.label">
                  {{ node.label }}
                </span>
              </template>
            </el-tree-select>
            <el-icon
              :style="{ color: '#a6a6a6', cursor: 'pointer', marginLeft: '6px' }"
              @click="editDs"
              v-if="curDatasetWeight >= 7"
            >
              <Icon name="icon_edit_outlined" class="el-icon-arrow-down el-icon-delete"></Icon>
            </el-icon>
          </el-row>
          <el-row class="dataset-search padding-lr">
            <div class="dataset-search-label">
              <span>{{ t('chart.field') }}</span>
              <span>
                <el-icon
                  :style="{ color: '#a6a6a6', cursor: 'pointer', marginRight: '6px' }"
                  @click="getFields(view.tableId, view.id)"
                >
                  <Icon
                    name="icon_refresh_outlined"
                    class="el-icon-arrow-down el-icon-delete"
                  ></Icon>
                </el-icon>
                <el-icon
                  v-if="false"
                  :style="{ color: '#a6a6a6', cursor: 'pointer', marginRight: '6px' }"
                  @click="addCalcField('d')"
                >
                  <Icon name="icon_add_outlined" class="el-icon-arrow-down el-icon-delete"></Icon>
                </el-icon>
              </span>
            </div>
            <el-input
              v-model="state.searchField"
              :class="'dataset-search-input-' + themes"
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
          <div style="height: calc(100% - 123px)">
            <div class="padding-lr field-height">
              <span>{{ t('chart.dimension') }}</span>
              <draggable
                :list="state.dimensionData"
                :group="dsFieldDragOptions.group"
                :move="onMove"
                item-key="id"
                animation="300"
                class="drag-list"
                @add="moveToDimension"
              >
                <template #item="{ element }">
                  <span
                    @dragstart="$event => startToMove($event, element)"
                    :draggable="true"
                    class="item-dimension father"
                    :title="element.name"
                  >
                    <el-icon>
                      <Icon
                        :className="`field-icon-${fieldType[element.deType]}`"
                        :name="`field_${fieldType[element.deType]}`"
                      ></Icon>
                    </el-icon>
                    <span class="field-name">{{ element.name }}</span>
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
                  </span>
                </template>
              </draggable>
            </div>
            <div class="padding-lr field-height">
              <span>{{ t('chart.quota') }}</span>
              <draggable
                :list="state.quotaData"
                :group="dsFieldDragOptions.group"
                :move="onMove"
                item-key="id"
                animation="300"
                class="drag-list"
                @add="moveToQuota"
              >
                <template #item="{ element }">
                  <span class="item-dimension father" :title="element.name">
                    <el-icon>
                      <Icon
                        :className="`field-icon-${fieldType[element.deType]}`"
                        :name="`field_${fieldType[element.deType]}`"
                      ></Icon>
                    </el-icon>
                    <span class="field-name">{{ element.name }}</span>
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
                  </span>
                </template>
              </draggable>
            </div>
          </div>
        </div>
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
      <el-form
        ref="renameForm"
        label-width="80px"
        require-asterisk-position="right"
        :model="state.itemForm"
        :rules="itemFormRules"
        label-position="top"
      >
        <el-form-item :label="t('dataset.field_origin_name')" class="form-item">
          <span>{{ state.itemForm.name }}</span>
        </el-form-item>
        <el-form-item :label="t('chart.show_name')" class="form-item" prop="chartShowName">
          <el-input v-model="state.itemForm.chartShowName" clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeRename(renameForm)">{{ t('chart.cancel') }} </el-button>
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
      width="600px"
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
      :title="t('chart.custom_sort')"
      :visible="state.showCustomSort"
      :close-on-click-modal="false"
      width="500px"
      class="dialog-css"
    >
      <custom-sort-edit
        :chart="view"
        field-type="xAxis"
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
    font-size: 12px !important;
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
  }

  .view-panel-row :deep(.ed-collapse-item__header) {
    padding: 0 !important;
    font-size: 12px !important;
    font-weight: 400 !important;
  }

  .tab-header {
    height: 100%;
    :deep(.ed-tabs__header) {
      border-top: solid 1px @side-outline-border-color;
    }
  }

  .tab-header :deep(.ed-tabs__item) {
    font-size: 12px;
    padding: 0 20px !important;
    color: @canvas-main-font-color;
  }
  .tab-header :deep(.is-active) {
    color: #3370ff;
  }

  .tab-header :deep(.ed-tabs__nav-scroll) {
    padding-left: 0 !important;
  }

  .tab-header :deep(.ed-tabs__header) {
    margin: 0 !important;
  }

  .tab-header :deep(.ed-tabs__content) {
    height: calc(100% - 47px);
    overflow-y: auto;
    overflow-x: hidden;
  }

  .field-height {
    height: 50%;
  }
  .field-height:nth-child(n + 2) {
    border-top: 1px solid #363636;
  }

  .drag-list {
    height: calc(100% - 26px);
    overflow: auto;
    padding: 2px 0;
  }

  .item-dimension {
    padding: 4px 10px;
    margin: 2px 2px 0 2px;
    text-align: left;
    color: #606266;
    display: block;
    word-break: break-all;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    position: relative;
    cursor: pointer;
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
    color: #a6a6a6;
  }

  .padding-tab {
    padding: 0;
    height: 100%;
    width: 100%;
    display: flex;
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

  .drag-block-style {
    padding: 2px 0 0 0;
    width: 100%;
    min-height: 32px;
    border-radius: 4px;
    border: 1px dashed #5f5f5f;
    overflow-x: hidden;
    overflow-y: hidden;
    display: block;
    align-items: center;
    background: rgba(255, 255, 255, 0.05);
    margin-top: 8px;
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
  }

  .drag-data:nth-child(n + 2) {
    border-top: 1px solid @side-outline-border-color;
  }

  .editor-title {
    color: @dv-canvas-main-font-color;
    font-weight: 500;
    height: @component-toolbar-height;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 8px;
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
  .switch-chart-light {
  }

  .dataset-search {
    height: 47px;
    width: 100%;
  }
  .dataset-search-label {
    height: 22px;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .dataset-search-input-dark {
    height: 22px;
    background-color: @side-area-background;
    :deep(.ed-input__inner) {
      height: 20px;
      background-color: @side-area-background;
      color: #ffffff;
    }
    :deep(.ed-input__wrapper) {
      box-shadow: none !important;
      border-bottom: 1px solid hsla(0, 0%, 100%, 0.15);
      background-color: @side-area-background;
      border-radius: 0;
      padding: 1px 4px;
    }
  }

  .dataset-search-input-light {
    height: 22px;
    background-color: @side-area-background-light;
    :deep(.ed-input__inner) {
      height: 20px;
      background-color: @side-area-background-light;
      color: @canvas-main-font-color-light;
    }
    :deep(.ed-input__wrapper) {
      box-shadow: none !important;
      border-bottom: @side-outline-border-color-light;
      background-color: @side-area-background-light;
      border-radius: 0;
      padding: 1px 4px;
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
  height: 100%;
}

.collapse-title {
  color: @dv-canvas-main-font-color;
  font-width: 500;
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
:deep(.ed-form-item__label) {
  color: @canvas-main-font-color;
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

.dataset-select {
  padding: 2px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
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
</style>

<style lang="less">
.ed-select-dropdown__item {
  display: flex;
  align-items: center;
}
.chart-type-hide-options {
  display: none;
}
</style>
