<script lang="tsx" setup>
import { PropType, reactive, ref, watch, toRefs } from 'vue'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import { Field, getFieldByDQ, saveChart } from '@/api/chart'
import { Tree } from '../../../visualized/data/dataset/form/CreatDsGroup.vue'
import { useEmitt } from '@/hooks/web/useEmitt'
import { ElMessage } from 'element-plus-secondary'
import draggable from 'vuedraggable'
import DimensionLabel from './drag-label/DimensionLabel.vue'
import DimensionItem from './drag-item/DimensionItem.vue'
import QuotaLabel from './drag-label/QuotaLabel.vue'
import QuotaItem from '@/views/chart/components/editor/drag-item/QuotaItem.vue'
import DragPlaceholder from '@/views/chart/components/editor/drag-item/DragPlaceholder.vue'
import FilterItem from '@/views/chart/components/editor/drag-item/FilterItem.vue'
import ChartStyle from '@/views/chart/components/editor/editor-style/ChartStyle.vue'
import Senior from '@/views/chart/components/editor/editor-senior/Senior.vue'
import QuotaFilterEditor from '@/views/chart/components/editor/filter/QuotaFilterEditor.vue'
import ResultFilterEditor from '@/views/chart/components/editor/filter/ResultFilterEditor.vue'
import { ElIcon, ElRow } from 'element-plus-secondary'
import DrillItem from '@/views/chart/components/editor/drag-item/DrillItem.vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { BASE_VIEW_CONFIG } from '@/views/chart/components/editor/util/chart'
import ChartType from '@/views/chart/components/editor/chart-type/ChartType.vue'
import { useRouter } from 'vue-router'

const dvMainStore = dvMainStoreWithOut()
const { canvasCollapse } = storeToRefs(dvMainStore)
const router = useRouter()

const { t } = useI18n()
const loading = ref(false)
const tabActive = ref('data')

const renameForm = ref<FormInstance>()

const props = defineProps({
  view: {
    type: Object,
    required: false,
    default() {
      return { ...BASE_VIEW_CONFIG }
    }
  },
  datasetTree: {
    type: Array as PropType<Tree[]>,
    default: () => []
  }
})

const { view, datasetTree } = toRefs(props)

const fieldType = (deType: number) => {
  return ['text', 'time', 'value', 'value', 'location'][deType]
}

const dsFieldDragOptions = { group: { name: 'drag', pull: 'clone' }, sort: true }

const itemFormRules = reactive<FormRules>({
  chartShowName: [
    { required: true, message: t('commons.input_content'), trigger: 'change' },
    { max: 50, message: t('commons.char_can_not_more_50'), trigger: 'change' }
  ]
})

const state = reactive({
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
  searchField: ''
})

// watch(
//   [() => props.view],
//   () => {
//     getFields(props.view.tableId)
//   },
//   { deep: true }
// )

watch(
  [() => state.searchField],
  newVal => {
    fieldFilter(newVal[0])
  },
  { deep: true }
)

const getFields = id => {
  if (id) {
    getFieldByDQ(id)
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
    getFields(data.id)
  }
}

const reset = () => {
  console.log('click reset')
}

const dimensionItemChange = item => {
  // this.calcData(true)
  // console.log(item)
  // console.log(view.value.xaxis)
  calcData(view.value)
}
const dimensionItemRemove = item => {
  if (item.removeType === 'dimension') {
    view.value.xAxis.splice(item.index, 1)
  } else if (item.removeType === 'dimensionExt') {
    view.value.xaxisExt.splice(item.index, 1)
  }
  calcData(view.value)
}

const quotaItemChange = item => {
  // this.calcData(true)
  // console.log(item)
  // console.log(view.value.xaxis)
  calcData(view.value)
}
const quotaItemRemove = item => {
  if (item.removeType === 'quota') {
    view.value.yAxis.splice(item.index, 1)
  } else if (item.removeType === 'quotaExt') {
    view.value.yAxisExt.splice(item.index, 1)
  }
  calcData(view.value)
}

const drillItemChange = item => {
  calcData(view.value)
}
const drillItemRemove = item => {
  view.value.drillFields.splice(item.index, 1)
  calcData(view.value)
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
const dragRemoveChartField = (list, e) => {
  const dup = list.filter(function (m) {
    return m.id === state.moveId
  })
  if (dup && dup.length > 0) {
    if (dup[0].chartId) {
      list.splice(e.newDraggableIndex, 1)
    }
  }
}

const addXaxis = e => {
  if (view.value.type !== 'table-info') {
    dragCheckType(view.value.xAxis, 'd')
  }
  dragMoveDuplicate(view.value.xAxis, e, 'chart')
  if (
    (view.value.type === 'map' ||
      view.value.type === 'word-cloud' ||
      view.value.type === 'label') &&
    view.value.xAxis.length > 1
  ) {
    view.value.xAxis = [view.value.xAxis[0]]
  }
  calcData(view.value)
}

const addYaxis = e => {
  dragCheckType(view.value.yAxis, 'q')
  dragMoveDuplicate(view.value.yAxis, e, '')
  if (
    (view.value.type === 'waterfall' ||
      view.value.type === 'word-cloud' ||
      view.value.type.includes('group')) &&
    view.value.yAxis.length > 1
  ) {
    view.value.yAxis = [view.value.yAxis[0]]
  }
  calcData(view.value)
}

const addDrill = e => {
  dragCheckType(view.value.drillFields, 'd')
  dragMoveDuplicate(view.value.drillFields, e, '')
  dragRemoveChartField(view.value.drillFields, e)
  calcData(view.value)
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
  dragRemoveChartField(view.value.customFilter, e)
  calcData(view.value)
}
const filterItemRemove = item => {
  view.value.customFilter.splice(item.index, 1)
  calcData(view.value)
}

const moveToDimension = e => {
  dragMoveDuplicate(state.dimensionData, e, 'ds')
  calcData(view.value)
}
const moveToQuota = e => {
  dragMoveDuplicate(state.quotaData, e, 'ds')
  calcData(view.value)
}

const calcData = view => {
  useEmitt().emitter.emit('calcData-' + view.id, view)
}

const renderChart = view => {
  useEmitt().emitter.emit('renderChart-' + view.id, view)
}

const onTypeChange = val => {
  view.value.type = val
  calcData(view.value)
}

const onColorChange = val => {
  view.value.customAttr.color = val
  renderChart(view.value)
}

const onSizeChange = val => {
  view.value.customAttr.size = val
  renderChart(view.value)
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

const onAssistLineChange = val => {
  view.value.senior.assistLine = val
  renderChart(view.value)
}

const onThresholdChange = val => {
  view.value.senior.threshold = val
  renderChart(view.value)
}

const onScrollChange = val => {
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
      }
      // this.calcData(true)
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
  calcData(view.value)
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
  calcData(view.value)
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
</script>

<template>
  <div class="chart-edit">
    <el-row v-loading="loading" class="de-chart-editor">
      <div style="position: relative">
        <el-icon
          :title="view.title"
          class="custom-icon"
          size="20px"
          @click="collapseChange('chartAreaCollapse')"
        >
          <Fold v-if="canvasCollapse.chartAreaCollapse" />
          <Expand v-else />
        </el-icon>
        <div class="collapse-title" v-show="canvasCollapse.chartAreaCollapse">
          <span style="font-size: 14px">{{ view.title }}</span>
        </div>
        <div v-show="!canvasCollapse.chartAreaCollapse" style="width: 240px" class="view-panel-row">
          <el-row class="editor-title">
            <span style="font-size: 14px">{{ view.title }}</span>
          </el-row>

          <el-row style="height: calc(100vh - 110px)">
            <el-tabs v-model="tabActive" :stretch="true" class="tab-header">
              <el-tab-pane name="data" :label="t('chart.chart_data')" class="padding-tab">
                <el-col>
                  <div class="drag_main_area attr-style theme-border-class">
                    <el-row style="height: 100%">
                      <el-row class="chart_type_area padding-lr">
                        <span class="switch-chart">
                          <span>{{ t('chart.switch_chart') }}</span>
                          <span style="float: right; width: 140px">
                            <el-popover
                              placement="bottom-end"
                              width="434"
                              trigger="click"
                              :append-to-body="true"
                              popper-class="chart-type-style"
                            >
                              <template #reference>
                                <el-button size="small" style="width: 100%; padding: 0">
                                  {{ t('chart.change_chart_type') }}
                                  <i class="el-icon-caret-bottom" />
                                </el-button>
                              </template>
                              <template #default>
                                <chart-type :type="view.type" @onTypeChange="onTypeChange" />
                              </template>
                            </el-popover>
                          </span>
                        </span>
                      </el-row>

                      <!--xAxis-->
                      <el-row
                        class="padding-lr drag-data"
                        v-if="
                          view.type !== 'text' && view.type !== 'gauge' && view.type !== 'liquid'
                        "
                      >
                        <span class="data-area-label">
                          <dimension-label :view="view" />
                        </span>
                        <draggable
                          :list="view.xAxis"
                          :move="onMove"
                          item-key="id"
                          group="drag"
                          animation="300"
                          class="drag-block-style"
                          @add="addXaxis"
                          @update="calcData(view)"
                        >
                          <template #item="{ element, index }">
                            <dimension-item
                              :dimension-data="state.dimension"
                              :quota-data="state.quota"
                              :chart="view"
                              :item="element"
                              :index="index"
                              @onDimensionItemChange="dimensionItemChange"
                              @onDimensionItemRemove="dimensionItemRemove"
                              @onNameEdit="showRename"
                            />
                          </template>
                        </draggable>
                        <drag-placeholder :drag-list="view.xAxis" />
                      </el-row>

                      <!--yAxis-->
                      <el-row
                        class="padding-lr drag-data"
                        v-if="
                          view.type !== 'table-info' &&
                          view.type !== 'label' &&
                          view.type !== 'flow-map'
                        "
                      >
                        <span class="data-area-label">
                          <quota-label :view="view" />
                        </span>
                        <draggable
                          :list="view.yAxis"
                          :move="onMove"
                          item-key="id"
                          group="drag"
                          animation="300"
                          class="drag-block-style"
                          @add="addYaxis"
                          @update="calcData(view)"
                        >
                          <template #item="{ element, index }">
                            <quota-item
                              :dimension-data="state.dimension"
                              :quota-data="state.quota"
                              :chart="view"
                              :item="element"
                              :index="index"
                              @onQuotaItemChange="quotaItemChange"
                              @onQuotaItemRemove="quotaItemRemove"
                              @onNameEdit="showRename"
                              @editItemFilter="showQuotaEditFilter"
                            />
                          </template>
                        </draggable>
                        <drag-placeholder :drag-list="view.yAxis" />
                      </el-row>

                      <!--drill-->
                      <el-row
                        class="padding-lr drag-data"
                        v-if="
                          view.type !== 'table-info' &&
                          view.type !== 'text' &&
                          view.type !== 'text-label' &&
                          view.type !== 'liquid' &&
                          view.type !== 'gauge'
                        "
                      >
                        <span class="data-area-label">
                          <span>{{ t('chart.drill') }}</span>
                          /
                          <span>{{ t('chart.dimension') }}</span>
                          <el-tooltip class="item" effect="dark" placement="bottom">
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
                          @update="calcData(view)"
                        >
                          <template #item="{ element, index }">
                            <filter-item
                              :dimension-data="state.dimensionData"
                              :quota-data="state.quotaData"
                              :item="element"
                              :index="index"
                              @onFilterItemRemove="filterItemRemove"
                              @editItemFilter="showEditFilter"
                            />
                          </template>
                        </draggable>
                        <drag-placeholder :drag-list="view.customFilter" />
                      </el-row>

                      <el-row class="result-style">
                        <div class="result-style-input">
                          <span v-show="view.type !== 'richTextView'">
                            {{ t('chart.result_count') }}
                          </span>
                          <span v-show="view.type !== 'richTextView'">
                            <el-radio-group
                              v-model="view.resultMode"
                              class="radio-span dark"
                              size="small"
                              @change="calcData(view)"
                            >
                              <el-radio label="all"
                                ><span>{{ t('chart.result_mode_all') }}</span></el-radio
                              >
                              <el-radio label="custom">
                                <el-input
                                  v-model="view.resultCount"
                                  class="result-count"
                                  size="small"
                                  @change="calcData(view)"
                                />
                              </el-radio>
                            </el-radio-group>
                          </span>
                        </div>
                        <el-button class="result-style-button" @click="calcData(view)">
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
                  :chart="view"
                  @onColorChange="onColorChange"
                  @onSizeChange="onSizeChange"
                  @onLabelChange="onLabelChange"
                  @onTooltipChange="onTooltipChange"
                  @onChangeXAxisForm="onChangeXAxisForm"
                  @onChangeYAxisForm="onChangeYAxisForm"
                  @onTextChange="onTextChange"
                  @onLegendChange="onLegendChange"
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
                  @onFunctionCfgChange="onFunctionCfgChange"
                  @onAssistLineChange="onAssistLineChange"
                />
              </el-tab-pane>
            </el-tabs>
          </el-row>
        </div>
      </div>
      <div class="dataset-main">
        <el-icon
          :title="'数据集'"
          class="custom-icon"
          size="20px"
          @click="collapseChange('datasetAreaCollapse')"
        >
          <Fold v-if="canvasCollapse.datasetAreaCollapse" />
          <Expand v-else />
        </el-icon>
        <div class="collapse-title" v-show="canvasCollapse.datasetAreaCollapse">
          <span style="font-size: 14px">数据集</span>
        </div>
        <div v-show="!canvasCollapse.datasetAreaCollapse" class="dataset-area view-panel-row">
          <el-row class="editor-title">
            <span style="font-size: 14px">数据集</span>
          </el-row>
          <el-row
            :style="{
              padding: '2px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              borderTop: '1px solid #363636'
            }"
            class="dark"
          >
            <el-tree-select
              v-model="view.tableId"
              :data="datasetTree"
              :props="dsSelectProps"
              :render-after-expand="false"
              filterable
              @node-click="dsClick"
              class="dataset-selector"
            >
              <template #default="{ node, data }">
                <el-icon v-if="!data.leaf">
                  <Icon name="scene"></Icon>
                </el-icon>
                <span :title="node.label">{{ node.label }}</span>
              </template>
            </el-tree-select>
            <el-icon
              :style="{ color: '#a6a6a6', cursor: 'pointer', marginRight: '8px' }"
              @click="editDs"
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
                  @click="getFields(view.tableId)"
                >
                  <Icon
                    name="icon_refresh_outlined"
                    class="el-icon-arrow-down el-icon-delete"
                  ></Icon>
                </el-icon>
                <!--                <el-icon :style="{ color: '#a6a6a6', cursor: 'pointer', marginRight: '6px' }">-->
                <!--                  <Icon name="icon_add_outlined" class="el-icon-arrow-down el-icon-delete"></Icon>-->
                <!--                </el-icon>-->
              </span>
            </div>
            <el-input
              v-model="state.searchField"
              class="dataset-search-input"
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
          <div style="height: calc(100% - 122px)">
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
                  <span class="item-dimension father" :title="element.name">
                    <el-icon>
                      <Icon
                        :className="`field-icon-${fieldType(element.deType)}`"
                        :name="`field_${fieldType(element.deType)}`"
                      ></Icon>
                    </el-icon>
                    <span class="field-name">{{ element.name }}</span>
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
                        :className="`field-icon-${fieldType(element.deType)}`"
                        :name="`field_${fieldType(element.deType)}`"
                      ></Icon>
                    </el-icon>
                    <span class="field-name">{{ element.name }}</span>
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
      :show-close="false"
      width="600px"
    >
      <el-form ref="renameForm" label-width="80px" :model="state.itemForm" :rules="itemFormRules">
        <el-form-item :label="t('dataset.field_origin_name')" class="form-item">
          <span style="padding: 0 16px">{{ state.itemForm.name }}</span>
        </el-form-item>
        <el-form-item :label="t('chart.show_name')" class="form-item" prop="chartShowName">
          <el-input
            v-model="state.itemForm.chartShowName"
            style="width: 200px"
            size="small"
            clearable
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button size="mini" @click="closeRename(renameForm)"
            >{{ t('chart.cancel') }}
          </el-button>
          <el-button type="primary" size="mini" @click="saveRename(renameForm)"
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
      :show-close="false"
      width="800px"
      class="dialog-css"
    >
      <quota-filter-editor :item="state.quotaItem" />
      <template #footer>
        <div class="dialog-footer">
          <el-button size="mini" @click="closeQuotaFilter">{{ t('chart.cancel') }} </el-button>
          <el-button type="primary" size="mini" @click="saveQuotaFilter"
            >{{ t('chart.confirm') }}
          </el-button>
        </div>
      </template>
    </el-dialog>
    <el-dialog
      v-model="state.resultFilterEdit"
      v-if="state.resultFilterEdit"
      :title="t('chart.add_filter')"
      :visible="state.resultFilterEdit"
      :show-close="false"
      width="800px"
      class="dialog-css"
    >
      <result-filter-editor :chart="state.chartForFilter" :item="state.filterItem" />
      <template #footer>
        <div class="dialog-footer">
          <el-button size="mini" @click="closeResultFilter">{{ t('chart.cancel') }} </el-button>
          <el-button type="primary" size="mini" @click="saveResultFilter"
            >{{ t('chart.confirm') }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="less" scoped>
.chart-edit {
  position: relative;
  transition: 0.5s;
  color: white;
  background-color: @side-area-background;
  height: 100%;
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
    height: 35px !important;
    line-height: 35px !important;
    padding: 0 0 0 6px !important;
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
    height: calc(100% - 46px);
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
  }

  .switch-chart {
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
    height: 46px;
    width: 100%;
  }
  .dataset-search-label {
    height: 22px;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
  .dataset-search-input {
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

:deep(.ed-collapse-item__header) {
  background-color: @side-area-background !important;
  color: #ffffff;
  padding-left: 5px;
  border-bottom: 1px solid rgba(85, 85, 85, 1);
  height: 38px !important;
}
:deep(.ed-collapse-item__content) {
  background-color: @side-content-background;
  color: #ffffff;
  padding-left: 5px;
}

:deep(.ed-collapse-item__wrap) {
  border-bottom: 1px solid rgba(85, 85, 85, 1);
}
:deep(.ed-collapse) {
  width: 100%;
}
:deep(.ed-form-item__label) {
  color: @canvas-main-font-color;
  font-size: 12px;
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

// editor form 全局样式
.dark {
  :deep(.ed-radio__label) {
    color: var(--ed-color-white);
  }
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
  :deep(input) {
    font-size: 12px !important;
  }
}
</style>
