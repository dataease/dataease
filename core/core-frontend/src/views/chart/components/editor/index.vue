<script lang="tsx" setup>
import {
  DEFAULT_COLOR_CASE,
  DEFAULT_SIZE,
  DEFAULT_LABEL,
  DEFAULT_TOOLTIP,
  DEFAULT_TOTAL,
  DEFAULT_TITLE_STYLE,
  DEFAULT_LEGEND_STYLE,
  DEFAULT_XAXIS_STYLE,
  DEFAULT_YAXIS_STYLE,
  DEFAULT_YAXIS_EXT_STYLE,
  DEFAULT_SPLIT,
  DEFAULT_FUNCTION_CFG,
  DEFAULT_THRESHOLD,
  DEFAULT_SCROLL
} from './util/chart'
import { reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import { getDatasetTree } from '@/api/dataset'
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

const { t } = useI18n()
const loading = ref(false)
const tabActive = ref('data')

const renameForm = ref<FormInstance>()

const props = {
  label: 'name',
  children: 'children',
  isLeaf: node => !node.children?.length
}

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
  chartAreaCollapse: false,
  datasetAreaCollapse: false,
  moveId: -1,
  view: {
    id: '1683789298247', // 视图id
    title: '图表',
    sceneId: 0, // 仪表板id
    tableId: '', // 数据集id
    type: 'bar',
    render: 'antv',
    resultCount: 100,
    resultMode: 'all',
    refreshViewEnable: false,
    refreshTime: 5,
    refreshUnit: 'minute',
    xaxis: [],
    xaxisExt: [],
    yaxis: [],
    yaxisExt: [],
    extStack: [],
    drillFields: [],
    viewFields: [],
    extBubble: [],
    customFilter: [],
    customAttr: {
      color: DEFAULT_COLOR_CASE,
      size: DEFAULT_SIZE,
      label: DEFAULT_LABEL,
      tooltip: DEFAULT_TOOLTIP,
      totalCfg: DEFAULT_TOTAL
    },
    customStyle: {
      text: DEFAULT_TITLE_STYLE,
      legend: DEFAULT_LEGEND_STYLE,
      xAxis: DEFAULT_XAXIS_STYLE,
      yAxis: DEFAULT_YAXIS_STYLE,
      yAxisExt: DEFAULT_YAXIS_EXT_STYLE,
      split: DEFAULT_SPLIT
    },
    senior: {
      functionCfg: DEFAULT_FUNCTION_CFG,
      assistLine: [],
      threshold: DEFAULT_THRESHOLD,
      scrollCfg: DEFAULT_SCROLL
    }
  },
  datasetTree: [],
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
  chartForFilter: {}
})

const initDataset = () => {
  getDatasetTree({}).then(res => {
    state.datasetTree = (res as unknown as Tree[]) || []
  })
}

const getFields = id => {
  getFieldByDQ(id).then(res => {
    state.dimensionData = (res.dimensionList as unknown as Field[]) || []
    state.quotaData = (res.quotaList as unknown as Field[]) || []
  })
}

const dsSelectProps = {
  label: 'name',
  children: 'children',
  value: 'id',
  isLeaf: node => !node.children?.length
}

const dsClick = (data: Tree) => {
  if (data.nodeType === 'dataset') {
    getFields(data.id)
  }
}

const reset = () => {
  console.log('click reset')
}

const dimensionItemChange = item => {
  // this.calcData(true)
  // console.log(item)
  // console.log(state.view.xaxis)
  calcData(state.view)
}
const dimensionItemRemove = item => {
  if (item.removeType === 'dimension') {
    state.view.xaxis.splice(item.index, 1)
  } else if (item.removeType === 'dimensionExt') {
    state.view.xaxisExt.splice(item.index, 1)
  }
  calcData(state.view)
}

const quotaItemChange = item => {
  // this.calcData(true)
  // console.log(item)
  // console.log(state.view.xaxis)
  calcData(state.view)
}
const quotaItemRemove = item => {
  if (item.removeType === 'quota') {
    state.view.yaxis.splice(item.index, 1)
  } else if (item.removeType === 'quotaExt') {
    state.view.yaxisExt.splice(item.index, 1)
  }
  calcData(state.view)
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
  if (state.view.type !== 'table-info') {
    dragCheckType(state.view.xaxis, 'd')
  }
  dragMoveDuplicate(state.view.xaxis, e, 'chart')
  if (
    (state.view.type === 'map' ||
      state.view.type === 'word-cloud' ||
      state.view.type === 'label') &&
    state.view.xaxis.length > 1
  ) {
    state.view.xaxis = [state.view.xaxis[0]]
  }
  calcData(state.view)
}

const addYaxis = e => {
  dragCheckType(state.view.yaxis, 'q')
  dragMoveDuplicate(state.view.yaxis, e, '')
  if (
    (state.view.type === 'waterfall' ||
      state.view.type === 'word-cloud' ||
      state.view.type.includes('group')) &&
    state.view.yaxis.length > 1
  ) {
    state.view.yaxis = [state.view.yaxis[0]]
  }
  calcData(state.view)
}

const addCustomFilter = e => {
  // 记录数等自动生成字段不做为过滤条件
  if (state.view.customFilter && state.view.customFilter.length > 0) {
    for (let i = 0; i < state.view.customFilter.length; i++) {
      if (state.view.customFilter[i].id === 'count') {
        state.view.customFilter.splice(i, 1)
      }
    }
  }
  state.view.customFilter[e.newDraggableIndex].filter = []
  dragMoveDuplicate(state.view.customFilter, e, '')
  dragRemoveChartField(state.view.customFilter, e)
  calcData(state.view)
}
const filterItemRemove = item => {
  state.view.customFilter.splice(item.index, 1)
  calcData(state.view)
}

const moveToDimension = e => {
  dragMoveDuplicate(state.dimensionData, e, 'ds')
  calcData(state.view)
}
const moveToQuota = e => {
  dragMoveDuplicate(state.quotaData, e, 'ds')
  calcData(state.view)
}

const calcData = view => {
  useEmitt().emitter.emit('calcData', view)
}

const renderChart = view => {
  useEmitt().emitter.emit('renderChart', view)
}

const onColorChange = val => {
  state.view.customAttr.color = val
  renderChart(state.view)
}

const onSizeChange = val => {
  state.view.customAttr.size = val
  renderChart(state.view)
}

const onLabelChange = val => {
  state.view.customAttr.label = val
  renderChart(state.view)
}

const onTooltipChange = val => {
  state.view.customAttr.tooltip = val
  renderChart(state.view)
}

const onChangeXAxisForm = val => {
  state.view.customStyle.xAxis = val
  renderChart(state.view)
}

const onChangeYAxisForm = val => {
  state.view.customStyle.yAxis = val
  renderChart(state.view)
}

const onTextChange = val => {
  state.view.customStyle.text = val
  renderChart(state.view)
}

const onLegendChange = val => {
  state.view.customStyle.legend = val
  renderChart(state.view)
}

const onFunctionCfgChange = val => {
  state.view.senior.functionCfg = val
  renderChart(state.view)
}

const onAssistLineChange = val => {
  state.view.senior.assistLine = val
  renderChart(state.view)
}

const onThresholdChange = val => {
  state.view.senior.threshold = val
  renderChart(state.view)
}

const onScrollChange = val => {
  state.view.senior.scrollCfg = val
  renderChart(state.view)
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
        state.view.yaxis[state.itemForm.index].chartShowName = state.itemForm.chartShowName
      } else if (state.itemForm.renameType === 'dimension') {
        state.view.xaxis[state.itemForm.index].chartShowName = state.itemForm.chartShowName
      } else if (state.itemForm.renameType === 'quotaExt') {
        state.view.yaxisExt[state.itemForm.index].chartShowName = state.itemForm.chartShowName
      } else if (state.itemForm.renameType === 'dimensionExt') {
        state.view.xaxisExt[state.itemForm.index].chartShowName = state.itemForm.chartShowName
      }
      // this.calcData(true)
      closeRename()
    } else {
      return false
    }
  })
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
    state.view.yaxis[state.quotaItem.index].filter = state.quotaItem.filter
    state.view.yaxis[state.quotaItem.index].logic = state.quotaItem.logic
  } else if (state.quotaItem.filterType === 'quotaExt') {
    state.view.yaxisExt[state.quotaItem.index].filter = state.quotaItem.filter
    state.view.yaxisExt[state.quotaItem.index].logic = state.quotaItem.logic
  }
  calcData(state.view)
  closeQuotaFilter()
}

const showEditFilter = item => {
  state.filterItem = JSON.parse(JSON.stringify(item))
  state.chartForFilter = JSON.parse(JSON.stringify(state.view))
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
  state.view.customFilter[state.filterItem.index].filter = state.filterItem.filter
  state.view.customFilter[state.filterItem.index].logic = state.filterItem.logic
  state.view.customFilter[state.filterItem.index].filterType = state.filterItem.filterType
  state.view.customFilter[state.filterItem.index].enumCheckField = state.filterItem.enumCheckField
  calcData(state.view)
  closeResultFilter()
}

const collapseChange = type => {
  state[type] = !state[type]
}

initDataset()
</script>

<template>
  <div class="chart-edit">
    <el-row v-loading="loading" class="de-chart-editor">
      <div style="position: relative">
        <el-icon
          :title="state.view.title"
          class="custom-icon"
          size="20px"
          @click="collapseChange('chartAreaCollapse')"
        >
          <Fold v-if="state.chartAreaCollapse" />
          <Expand v-else />
        </el-icon>
        <div class="collapse-title" v-show="state.chartAreaCollapse">
          <span style="font-size: 14px">{{ state.view.title }}</span>
        </div>
        <div v-show="!state.chartAreaCollapse" style="width: 280px" class="view-panel-row">
          <el-row class="editor-title">
            <span style="font-size: 14px">{{ state.view.title }}</span>
          </el-row>
          <el-row>
            <el-tabs v-model="tabActive" :stretch="true" class="tab-header">
              <el-tab-pane name="data" :label="t('chart.chart_data')" class="padding-tab">
                <el-col>
                  <div class="chart_type_area padding-lr theme-border-class">
                    <span class="theme-border-class">
                      <span>{{ t('chart.chart_type') }}</span>
                      <el-row style="padding: 4px 0 4px 10px">
                        <span>
                          <div>svg</div>
                        </span>
                        <span style="float: right">
                          <el-popover
                            placement="bottom-end"
                            width="400"
                            trigger="click"
                            :append-to-body="true"
                          >
                            <template #reference>
                              <el-button size="small" style="padding: 6px">
                                {{ t('chart.change_chart_type') }}
                                <i class="el-icon-caret-bottom" />
                              </el-button>
                            </template>
                            <div class="padding-lr">
                              <el-row>
                                <div>todo chart type</div>
                              </el-row>
                            </div>
                          </el-popover>
                        </span>
                      </el-row>
                    </span>
                  </div>
                  <div class="drag_main_area attr-style theme-border-class">
                    <el-row style="height: 100%">
                      <el-row class="padding-lr">
                        <span
                          v-show="state.view.type !== 'richTextView'"
                          style="width: 80px; text-align: right"
                        >
                          {{ t('chart.result_count') }}
                        </span>
                        <el-row v-show="state.view.type !== 'richTextView'">
                          <el-radio-group
                            v-model="state.view.resultMode"
                            class="radio-span"
                            size="small"
                          >
                            <el-radio label="all"
                              ><span>{{ t('chart.result_mode_all') }}</span></el-radio
                            >
                            <el-radio label="custom">
                              <el-input
                                v-model="state.view.resultCount"
                                class="result-count"
                                size="small"
                              />
                            </el-radio>
                          </el-radio-group>
                        </el-row>
                      </el-row>

                      <el-row class="padding-lr">
                        <span style="width: 80px; text-align: right">
                          {{ t('chart.refresh_frequency') }}
                        </span>
                        <!--                    <el-tooltip class="item" effect="dark" placement="bottom">-->
                        <!--                      <template #slot>-->
                        <!--                        <div>-->
                        <!--                          {{ t('chart.chart_refresh_tips') }}-->
                        <!--                        </div>-->
                        <!--                      </template>-->
                        <!--                      <i-->
                        <!--                        class="el-icon-info"-->
                        <!--                        style="cursor: pointer; color: #606266; font-size: 12px"-->
                        <!--                      />-->
                        <!--                    </el-tooltip>-->
                        <span class="padding-lr">
                          <el-checkbox
                            v-model="state.view.refreshViewEnable"
                            class="el-input-refresh-loading"
                          />
                          {{ t('chart.enable_refresh_view') }}
                        </span>
                        <el-row>
                          <el-input
                            v-model="state.view.refreshTime"
                            class="el-input-refresh-time"
                            type="number"
                            size="small"
                            controls-position="right"
                            :min="1"
                            :max="3600"
                            :disabled="!state.view.refreshViewEnable"
                          />
                          <el-select
                            v-model="state.view.refreshUnit"
                            class="el-input-refresh-unit margin-left8"
                            size="small"
                            :disabled="!state.view.refreshViewEnable"
                          >
                            <el-option :label="t('chart.minute')" :value="'minute'" />
                            <el-option :label="t('chart.second')" :value="'second'" />
                          </el-select>
                        </el-row>
                      </el-row>

                      <!--xAxis-->
                      <el-row class="padding-lr drag-data">
                        <span class="data-area-label">
                          <dimension-label :view="state.view" />
                        </span>
                        <draggable
                          :list="state.view.xaxis"
                          :move="onMove"
                          group="drag"
                          animation="300"
                          class="drag-block-style"
                          @add="addXaxis"
                          @update="calcData(state.view)"
                        >
                          <template #item="{ element, index }">
                            <dimension-item
                              :dimension-data="state.dimensionData"
                              :quota-data="state.quotaData"
                              :chart="state.view"
                              :item="element"
                              :index="index"
                              @onDimensionItemChange="dimensionItemChange"
                              @onDimensionItemRemove="dimensionItemRemove"
                              @onNameEdit="showRename"
                            />
                          </template>
                        </draggable>
                        <drag-placeholder :drag-list="state.view.xaxis" />
                      </el-row>

                      <!--yAxis-->
                      <el-row class="padding-lr drag-data">
                        <span class="data-area-label">
                          <quota-label :view="state.view" />
                        </span>
                        <draggable
                          :list="state.view.yaxis"
                          :move="onMove"
                          group="drag"
                          animation="300"
                          class="drag-block-style"
                          @add="addYaxis"
                          @update="calcData(state.view)"
                        >
                          <template #item="{ element, index }">
                            <quota-item
                              :dimension-data="state.dimensionData"
                              :quota-data="state.quotaData"
                              :chart="state.view"
                              :item="element"
                              :index="index"
                              @onQuotaItemChange="quotaItemChange"
                              @onQuotaItemRemove="quotaItemRemove"
                              @onNameEdit="showRename"
                              @editItemFilter="showQuotaEditFilter"
                            />
                          </template>
                        </draggable>
                        <drag-placeholder :drag-list="state.view.yaxis" />
                      </el-row>

                      <!--filter-->
                      <el-row class="padding-lr drag-data">
                        <span>{{ t('chart.result_filter') }}</span>
                        <draggable
                          :list="state.view.customFilter"
                          :move="onMove"
                          group="drag"
                          animation="300"
                          class="drag-block-style"
                          @add="addCustomFilter"
                          @update="calcData(state.view)"
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
                        <drag-placeholder :drag-list="state.view.customFilter" />
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
                  :chart="state.view"
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
                  :chart="state.view"
                  :quota-data="state.view.yaxis"
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
          <Fold v-if="state.datasetAreaCollapse" />
          <Expand v-else />
        </el-icon>
        <div class="collapse-title" v-show="state.datasetAreaCollapse">
          <span style="font-size: 14px">数据集</span>
        </div>
        <div v-show="!state.datasetAreaCollapse" class="dataset-area view-panel-row">
          <el-row class="editor-title">
            <span style="font-size: 14px">数据集</span>
          </el-row>
          <el-row :style="{ borderTop: '1px solid #363636' }">
            <el-tree-select
              v-model="state.view.tableId"
              :data="state.datasetTree"
              :props="dsSelectProps"
              filterable
              @node-click="dsClick"
            >
              <template #default="{ data: { name } }">
                <el-icon>
                  <Icon name="scene"></Icon>
                </el-icon>
                <span :title="name">{{ name }}</span>
              </template>
            </el-tree-select>
          </el-row>
          <div style="height: calc(100% - 78px)">
            <div class="padding-lr field-height">
              <span>{{ t('chart.dimension') }}</span>
              <draggable
                :list="state.dimensionData"
                :group="dsFieldDragOptions.group"
                :move="onMove"
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
      v-dialogDrag
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
      v-dialogDrag
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
      v-dialogDrag
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
  border-left: 1px solid @side-outline-border-color;
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
    padding: 0 6px;
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

  .tab-header :deep(.ed-tabs__header) {
    border-top: solid 1px @side-outline-border-color;
    border-right: solid 1px @side-outline-border-color;
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
    height: calc(100vh - 150px);
    overflow-y: auto;
    overflow-x: hidden;
  }

  .field-height {
    height: 50%;
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

  .radio-span :deep(.ed-radio__label) {
    margin-left: 4px;
  }

  .result-count {
    width: 50px;

    :deep(.ed-input__wrapper) {
      padding: 0;
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
    margin-top: 12px;
  }

  .editor-title {
    height: @component-toolbar-height;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 10px;
  }

  .ed-tabs {
    --el-tabs-header-height: 38px !important;
  }
}

.chart_type_area {
  height: 80px;
  border-top: 1px solid @side-outline-border-color;
  overflow: auto;
}

.drag_main_area {
  border-top: 1px solid @side-outline-border-color;
  overflow: auto;
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
</style>
