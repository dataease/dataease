<script setup lang="ts">
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
  unref,
  onBeforeUnmount,
  onMounted
} from 'vue'
// ===== 第三方库导入 =====
import { storeToRefs } from 'pinia'
import { cloneDeep, forEach, get, debounce, set, concat, keys } from 'lodash-es'
import { ElMessage, ElTreeSelect } from 'element-plus-secondary'

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

const { t } = useI18n()

import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'

const dvMainStore = dvMainStoreWithOut()

const {
  canvasCollapse,
  curComponent,
  componentData,
  editMode,
  mobileInPc,
  fullscreenFlag,
  dvInfo
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
  // if (updateQuery === 'updateQuery') {
  //   queryList.value.forEach(ele => {
  //     useEmitt().emitter.emit(`updateQueryCriteria${ele.id}`)
  //   })
  // }
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

// 计算属性
const chartStyleShow = computed(() => {
  return (
    !['richText', 'Picture'].includes(view.value.type) &&
    curComponent.value &&
    curComponent.value.component === 'UserView'
  )
})

const chartViewInstance = computed(() => {
  return chartViewManager.getChartView(view.value.render, view.value.type)
})

const allFields = computed(() => {
  return concat(state.quotaData, state.dimensionData)
})

</script>

<template>
<div class="left-sidebar" :class="{ collapsed: canvasCollapse.chartAreaCollapse }">
    <div class="sidebar-header">
      <span :class="{'collapsed-header': canvasCollapse.chartAreaCollapse}">图表</span>
      <ToggleButton :collapsed="canvasCollapse.chartAreaCollapse" @toggle="collapseChange('chartAreaCollapse')" />
    </div>
    <div class="sidebar-content" v-if="!canvasCollapse.chartAreaCollapse">
      <ChartViewGroup></ChartViewGroup>
      <el-tabs v-model="tabActive" class="tab-header" :class="{ dark: themes === 'dark' }">
        <el-tab-pane name="style" :label="t('chart.chart_style')" style="width: 100%">
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
        </el-tab-pane>
        <el-tab-pane name="senior" :label="t('chart.senior')" style="width: 100%">
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
        </el-tab-pane>
      </el-tabs>
    </div>
</div>
</template>

<style lang="less" scoped>
.sidebar-container {
  display: flex;
  height: 100vh;
  background-color: #f5f5f5;
}
.left-sidebar{
  width: 200px;
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
