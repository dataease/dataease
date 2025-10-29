<script setup lang="ts">
import {
  PropType,
  reactive,
  ref,
  toRefs,
  nextTick,
  computed,
  onMounted,
  onBeforeUnmount
} from 'vue'
// ===== 第三方库导入 =====
import { storeToRefs } from 'pinia'
import { get, set, concat, keys } from 'lodash-es'
import { ElMessage } from 'element-plus-secondary'

import { BASE_VIEW_CONFIG, getViewConfig } from '@/views/chart/components/editor/util/chart'
import chartViewManager from '@/views/chart/components/js/panel'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'

import ChartViewGroup from '@/custom-component/component-group/chartViewGroup.vue'
import { Tree } from '@/views/visualized/data/dataset/form/CreatDsGroup.vue'
import ChartStyle from '@/views/chart/components/editor/editor-style/ChartStyle.vue'
import Senior from '@/views/chart/components/editor/editor-senior/Senior.vue'

// hook
import { useI18n } from '@/hooks/web/useI18n'
import { useEmitt } from '@/hooks/web/useEmitt'
const { emitter } = useEmitt()
const { t } = useI18n()

import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'

const dvMainStore = dvMainStoreWithOut()

const {
  canvasCollapse,
  curComponent,
  mobileInPc
} = storeToRefs(dvMainStore)

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
const { view } = toRefs(props)
const tabActive = ref('style')
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
  showSortPriority: false,
  sortPriority: [],
  customSortList: [],
  customSortField: {},
  currEditField: {},
  worldTree: [],
  areaId: '',
  chartTypeOptions: [],
  useless: null
})

const collapseChange = type => {
  canvasCollapse.value[type] = !canvasCollapse.value[type]
}
const renderChart = view => {
  if (mobileInPc.value) {
    //移动端设计
    useEmitt().emitter.emit('onMobileStatusChange', {
      type: 'componentStyleChange',
      value: { type: 'renderChart', component: JSON.parse(JSON.stringify(view)) }
    })
  } else {
    useEmitt().emitter.emit('renderChart-' + view.id, view)
    snapshotStore.recordSnapshotCache('renderChart', view.id)
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
    if (mobileInPc.value) {
      //移动端设计
      useEmitt().emitter.emit('onMobileStatusChange', {
        type: 'componentStyleChange',
        value: { type: 'calcData', component: JSON.parse(JSON.stringify(view)) }
      })
    } else {
      useEmitt().emitter.emit('calcData-' + view.id, view)
      snapshotStore.recordSnapshotCache('renderChart', view.id)
    }
  }
  snapshotStore.recordSnapshotCache('calcData', view.id)
  if (updateQuery === 'updateQuery') {
  //   queryList.value.forEach(ele => {
  //     useEmitt().emitter.emit(`updateQueryCriteria${ele.id}`)
  //   })
  }
}
const onColorChange = val => {
  (view.value.customAttr as any).color = val
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
  const { data, render } = chartForm
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
  if (curComponent.value) {
    curComponent.value.name = view.value.title
    curComponent.value.title = view.value.title
  }
  if (mobileInPc.value) {
    //移动端设计
    useEmitt().emitter.emit('onMobileStatusChange', {
      type: 'componentStyleChange',
      value: { type: 'updateTitle', component: JSON.parse(JSON.stringify(view.value)) }
    })
  } else {
    useEmitt().emitter.emit('updateTitle-' + view.value.id)
    snapshotStore.recordSnapshotCache('renderChart', view.value.id)
  }
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
  // 修复#13299
  if (curComponent.value.id === view.value?.id) {
    curComponent.value.commonBackground = val
    if (mobileInPc.value) {
      //移动端设计
      useEmitt().emitter.emit('onMobileStatusChange', {
        type: 'componentStyleChange',
        value: {
          type: 'commonBackground',
          component: JSON.parse(JSON.stringify(curComponent.value))
        }
      })
    }
  }
}

const onStyleAttrChange = val => {
  curComponent.value.style[val.property] = val.value
  if (mobileInPc.value) {
    //移动端设计
    useEmitt().emitter.emit('onMobileStatusChange', {
      type: 'componentStyleChange',
      value: { type: 'style', component: JSON.parse(JSON.stringify(curComponent.value)) }
    })
  }
}

const onAssistLineChange = val => {
  view.value.senior.assistLineCfg = val.data
  if (val.requestData) {
    calcData(view.value)
  } else {
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
const onBasicStyleChange = (chartForm: ChartEditorForm<ChartBasicStyle>, prop: string) => {
  const { data, requestData, render } = chartForm
  const val = get(data, prop)
  set(view.value.customAttr.basicStyle, prop, val)
  if (requestData) {
    calcData(view.value)
  }
  if (render !== false) {
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

const onExtTooltipChange = val => {
  view.value.extTooltip = val
}
const onChangeQuadrantForm = val => {
  view.value.customAttr.quadrant = val
  renderChart(view.value)
}
const onChangeFlowMapLineForm = (val, prop) => {
  const value = get(val, prop)
  set(view.value.customAttr.misc.flowMapConfig.lineConfig, prop, value)
  renderChart(view.value)
}
const onChangeFlowMapPointForm = val => {
  view.value.customAttr.misc.flowMapConfig.pointConfig = val
  renderChart(view.value)
}

const onScrollCfgChange = val => {
  view.value.senior.scrollCfg = val
  renderChart(view.value)
}
const onThresholdChange = val => {
  view.value.senior.threshold = val
  let type = undefined
  view.value.senior.threshold?.tableThreshold?.some(item => {
    if (item.conditions.some(i => i.type === 'dynamic')) {
      type = 'calcData'
      return true
    }
    return false
  })
  if (type || view.value.type === 'rich-text') {
    calcData(view.value)
  } else {
    renderChart(view.value)
  }
}

const onMapMappingChange = val => {
  view.value.senior.areaMapping = val
  renderChart(view.value)
}
const onBubbleAnimateChange = val => {
  view.value.senior.bubbleCfg = val
  renderChart(view.value)
}

const snapshotStore = snapshotStoreWithOut()
const recordSnapshotInfo = type => {
  view.value['dataFrom'] = 'calc'
  snapshotStore.recordSnapshotCache(type, view.value.id)
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
    | 'extColor'
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
    case 'extColor':
      axis = view.value.extColor?.splice(0)
      break
  }
  axis?.length && emitter.emit('removeAxis', { axisType: _type, axis, editType: 'remove' })
}

const onTypeChange = (render, type) => {
  const viewConf = getViewConfig(type)
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
  emitter.emit('chart-type-change-' + view.value.id)
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
// ===== 动态高度计算 =====
const elTabsHeight = ref('auto')

const calculateElTabsHeight = () => {
  nextTick(() => {
    const edMainElement = document.querySelector('.sidebar-content')
    const chartHeaderElement = document.querySelector('.chart-view')

    if (edMainElement && chartHeaderElement) {
      const edMainHeight = edMainElement.clientHeight
      const chartHeaderHeight = chartHeaderElement.clientHeight
      const remainingHeight = edMainHeight - chartHeaderHeight - 54
      elTabsHeight.value = `${remainingHeight}px`
    }
  })
}

// computed
const chartViewInstance = computed(() => {
  return chartViewManager.getChartView(view.value.render, view.value.type)
})

const allFields = computed(() => {
  return concat(state.quotaData, state.dimensionData)
})
// ===== 生命周期钩子 =====
onMounted(() => {
  calculateElTabsHeight()
  // 监听窗口大小变化
  window.addEventListener('resize', calculateElTabsHeight)
})

onBeforeUnmount(() => {
  // 清理事件监听器
  window.removeEventListener('resize', calculateElTabsHeight)
})
</script>

<template>
<div class="left-sidebar" :class="{ collapsed: canvasCollapse.chartAreaCollapse }">
  <div class="sidebar-header">
    <span :class="{'collapsed-header': canvasCollapse.chartAreaCollapse}">图表</span>
    <ToggleButton :collapsed="canvasCollapse.chartAreaCollapse" @toggle="collapseChange('chartAreaCollapse')" />
  </div>
  <div class="sidebar-content" v-if="!canvasCollapse.chartAreaCollapse">
    <chart-view-group
      class="chart-view"
      :themes="themes"
      :chart-type="view.type"
      @on-type-change="onTypeChange"
    ></chart-view-group>
    <el-tabs v-model="tabActive" class="tab-header"  :class="{ dark: themes === 'dark' }" >
      <el-tab-pane name="style" :label="t('chart.chart_style')" style="width: 100%">
        <div class="tab-content" :style="{height: elTabsHeight }">
          <chart-style
            :properties="chartViewInstance.properties"
            :property-inner-all="chartViewInstance.propertyInner"
            :selector-spec="chartViewInstance.selectorSpec"
            :common-background-pop="curComponent?.commonBackground"
            :common-border-pop="curComponent?.style"
            :event-info="curComponent?.events"
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
            @onStyleAttrChange="onStyleAttrChange"
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
        </div>
      </el-tab-pane>
      <el-tab-pane name="senior" :label="t('chart.senior')" style="width: 100%">
        <div class="tab-content" :style="{height: elTabsHeight }">
          <senior
            :chart="view"
            :quota-data="view.yAxis"
            :quota-ext-data="view.yAxisExt"
            :fields-data="allFields"
            :themes="themes"
            :properties="chartViewInstance.properties"
            :property-inner-all="chartViewInstance.propertyInner"
            :event-info="curComponent?.events"
            @onFunctionCfgChange="onFunctionCfgChange"
            @onAssistLineChange="onAssistLineChange"
            @onScrollCfgChange="onScrollCfgChange"
            @onThresholdChange="onThresholdChange"
            @onMapMappingChange="onMapMappingChange"
            @onBubbleAnimateChange="onBubbleAnimateChange"
          />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</div>
</template>

<style lang="less" scoped>
.sidebar-content{
  :deep(.ed-tabs__header .ed-tabs__item){
    font-weight: 400;
    font-size: 12px;
    padding: 0 8px !important;
    margin-right: 12px;
  }
}
.sidebar-content {
  flex: 1;
  overflow: hidden;
}
.tab-content{
  overflow-y: auto;
  :deep(.ed-collapse-item__content){
    padding: 12px 6px 2px 6px !important;
  }
  .ed-form-item__label{
    color: #646A73;
    font-size: 12px;
    font-style: normal;
    font-weight: 400;
    height: unset;
    line-height: 20px;
   }
  /* 针对 el-scrollbar 的垂直滚动条宽度设置 */
  ::-webkit-scrollbar {
    width: 6px!important;
  }
}
.left-sidebar{
  width: 240px;
  height: 100%;
  border-right: 1px solid #e0e0e0;
  border-left: 1px solid #e0e0e0;
  background-color: #ffffff;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  position: relative;

  &.collapsed {
    width: 50px;
  }
}
.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding: 12px 16px;
  border-bottom: 1px solid #e0e0e0;
  background-color: #fafafa;
  min-height: 50px;
  font-size: 13px;
  position: relative;
}
.dataset-main-top{
  padding: 10px;
}
.chart-dashline{
  border-bottom: solid 1px #e0e0e0;
}

</style>
