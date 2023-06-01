<script lang="tsx" setup>
import { PropType, reactive, ref, toRefs } from 'vue'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import { Field, getFieldByDQ, saveChart } from '@/api/chart'
import { Tree } from '../../../visualized/data/dataset/form/CreatDsGroup.vue'
import { useEmitt } from '@/hooks/web/useEmitt'
import draggable from 'vuedraggable'
import DimensionLabel from './drag-label/DimensionLabel.vue'
import DimensionItem from './drag-item/DimensionItem.vue'
import QuotaLabel from './drag-label/QuotaLabel.vue'
import QuotaItem from '@/views/chart/components/editor/drag-item/QuotaItem.vue'
import DragPlaceholder from '@/views/chart/components/editor/drag-item/DragPlaceholder.vue'
import FilterItem from '@/views/chart/components/editor/drag-item/FilterItem.vue'
import ChartStyle from '@/views/chart/components/editor/editor-style/ChartStyle.vue'
import Senior from '@/views/chart/components/editor/editor-senior/Senior.vue'
import { ElIcon, ElRow } from 'element-plus-secondary'

const { t } = useI18n()
const loading = ref(false)
const tabActive = ref('data')

const renameForm = ref<FormInstance>()

const props = defineProps({
  view: {
    type: Object,
    required: true
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
  chartAreaCollapse: false,
  datasetAreaCollapse: false,
  dimensionData: [],
  quotaData: [],
  renameItem: false,
  itemForm: {
    name: '',
    chartShowName: ''
  },
  quotaFilterEdit: false,
  quotaItem: {}
})

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
  // console.log(view.value.xaxis)
  calcData(view.value)
}

const quotaItemChange = item => {
  // this.calcData(true)
  // console.log(item)
  // console.log(view.value.xaxis)
  calcData(view.value)
}

const addXaxis = e => {
  // if (this.view.type !== 'table-info') {
  //   this.dragCheckType(this.view.xaxis, 'd')
  // }
  // this.dragMoveDuplicate(this.view.xaxis, e)
  // if ((this.view.type === 'map' || this.view.type === 'word-cloud' || this.view.type === 'label') && this.view.xaxis.length > 1) {
  //   this.view.xaxis = [this.view.xaxis[0]]
  // }
  // this.calcData(true)
  calcData(view.value)
}

const addYaxis = e => {
  // this.dragCheckType(this.view.yaxis, 'q')
  // this.dragMoveDuplicate(this.view.yaxis, e)
  // if ((this.view.type === 'waterfall' || this.view.type === 'word-cloud' || this.view.type.includes('group')) && this.view.yaxis.length > 1) {
  //   this.view.yaxis = [this.view.yaxis[0]]
  // }
  // this.calcData(true)
  calcData(view.value)
}

const calcData = view => {
  useEmitt().emitter.emit('calcData', view)
}

const renderChart = view => {
  useEmitt().emitter.emit('renderChart', view)
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
        view.value.yaxis[state.itemForm.index].chartShowName = state.itemForm.chartShowName
      } else if (state.itemForm.renameType === 'dimension') {
        view.value.xaxis[state.itemForm.index].chartShowName = state.itemForm.chartShowName
      } else if (state.itemForm.renameType === 'quotaExt') {
        view.value.yaxisExt[state.itemForm.index].chartShowName = state.itemForm.chartShowName
      } else if (state.itemForm.renameType === 'dimensionExt') {
        view.value.xaxisExt[state.itemForm.index].chartShowName = state.itemForm.chartShowName
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

const collapseChange = type => {
  state[type] = !state[type]
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
          <Fold v-if="state.chartAreaCollapse" />
          <Expand v-else />
        </el-icon>
        <div class="collapse-title" v-show="state.chartAreaCollapse">
          <span>{{ view.title }}</span>
        </div>
        <div v-show="!state.chartAreaCollapse" style="width: 280px" class="view-panel-row">
          <el-row class="editor-title">
            <span>{{ view.title }}</span>
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
                          v-show="view.type !== 'richTextView'"
                          style="width: 80px; text-align: right"
                        >
                          {{ t('chart.result_count') }}
                        </span>
                        <el-row v-show="view.type !== 'richTextView'">
                          <el-radio-group v-model="view.resultMode" class="radio-span" size="small">
                            <el-radio label="all"
                              ><span>{{ t('chart.result_mode_all') }}</span></el-radio
                            >
                            <el-radio label="custom">
                              <el-input
                                v-model="view.resultCount"
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
                            v-model="view.refreshViewEnable"
                            class="el-input-refresh-loading"
                          />
                          {{ t('chart.enable_refresh_view') }}
                        </span>
                        <el-row>
                          <el-input
                            v-model="view.refreshTime"
                            class="el-input-refresh-time"
                            type="number"
                            size="small"
                            controls-position="right"
                            :min="1"
                            :max="3600"
                            :disabled="!view.refreshViewEnable"
                          />
                          <el-select
                            v-model="view.refreshUnit"
                            class="el-input-refresh-unit margin-left8"
                            size="small"
                            :disabled="!view.refreshViewEnable"
                          >
                            <el-option :label="t('chart.minute')" :value="'minute'" />
                            <el-option :label="t('chart.second')" :value="'second'" />
                          </el-select>
                        </el-row>
                      </el-row>

                      <!--xAxis-->
                      <el-row class="padding-lr drag-data">
                        <span class="data-area-label">
                          <dimension-label :view="view" />
                        </span>
                        <draggable
                          :list="view.xaxis"
                          group="drag"
                          animation="300"
                          class="drag-block-style"
                          @add="addXaxis"
                        >
                          <template #item="{ element, index }">
                            <dimension-item
                              :dimension-data="state.dimensionData"
                              :quota-data="state.quotaData"
                              :chart="view"
                              :item="element"
                              :index="index"
                              @onDimensionItemChange="dimensionItemChange"
                              @onNameEdit="showRename"
                            />
                          </template>
                        </draggable>
                        <drag-placeholder :drag-list="view.xaxis" />
                      </el-row>

                      <!--yAxis-->
                      <el-row class="padding-lr drag-data">
                        <span class="data-area-label">
                          <quota-label :view="view" />
                        </span>
                        <draggable
                          :list="view.yaxis"
                          group="drag"
                          animation="300"
                          class="drag-block-style"
                          @add="addYaxis"
                        >
                          <template #item="{ element, index }">
                            <quota-item
                              :dimension-data="state.dimensionData"
                              :quota-data="state.quotaData"
                              :chart="view"
                              :item="element"
                              :index="index"
                              @onQuotaItemChange="quotaItemChange"
                              @onNameEdit="showRename"
                              @editItemFilter="showQuotaEditFilter"
                            />
                          </template>
                        </draggable>
                        <drag-placeholder :drag-list="view.yaxis" />
                      </el-row>

                      <!--filter-->
                      <el-row class="padding-lr drag-data">
                        <span>{{ t('chart.result_filter') }}</span>
                        <draggable
                          :list="view.customFilter"
                          group="drag"
                          animation="300"
                          class="drag-block-style"
                        >
                          <template #item="{ element, index }">
                            <filter-item
                              :dimension-data="state.dimensionData"
                              :quota-data="state.quotaData"
                              :item="element"
                              :index="index"
                            />
                          </template>
                        </draggable>
                        <drag-placeholder :drag-list="view.customFilter" />
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
                  :quota-data="view.yaxis"
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
          <span>数据集</span>
        </div>
        <div v-show="!state.datasetAreaCollapse" class="dataset-area view-panel-row">
          <el-row class="editor-title">
            <span>数据集</span>
          </el-row>
          <el-row :style="{ borderTop: '1px solid #e6e6e6' }">
            <el-tree-select
              v-model="view.tableId"
              :data="datasetTree"
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
          <div style="height: 100%">
            <div class="padding-lr field-height">
              <span>{{ t('chart.dimension') }}</span>
              <draggable
                :list="state.dimensionData"
                :group="dsFieldDragOptions.group"
                animation="300"
                class="drag-list"
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
                animation="300"
                class="drag-list"
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
      width="30%"
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
.el-row {
  display: block;
}

span {
  font-size: 14px;
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
    margin-left: 45px;
  }

  .view-panel-row {
    overflow-y: hidden;
    overflow-x: hidden;
    height: 100%;
  }

  .view-panel-row :deep(.el-collapse-item__header) {
    height: 35px !important;
    line-height: 35px !important;
    padding: 0 0 0 6px !important;
    font-size: 12px !important;
    font-weight: 400 !important;
  }

  .tab-header :deep(.el-tabs__header) {
    border-top: solid 1px @side-outline-border-color;
    border-right: solid 1px @side-outline-border-color;
  }

  .tab-header :deep(.el-tabs__item) {
    font-size: 12px;
    padding: 0 20px !important;
    color: @canvas-main-font-color;
  }
  .tab-header :deep(.is-active) {
    color: #3370ff;
  }

  .tab-header :deep(.el-tabs__nav-scroll) {
    padding-left: 0 !important;
  }

  .tab-header :deep(.el-tabs__header) {
    margin: 0 !important;
  }

  .tab-header :deep(.el-tabs__content) {
    height: calc(100vh - 155px);
  }

  .field-height {
    height: 50%;
    border-top: 1px solid #e6e6e6;
  }

  .drag-list {
    height: calc(100% - 26px);
    overflow: auto;
    padding: 2px 0;
  }

  .item-dimension {
    padding: 2px 10px;
    margin: 2px 2px 0 2px;
    border: solid 1px #eee;
    text-align: left;
    color: #606266;
    display: block;
    word-break: break-all;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    position: relative;
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
  }

  .padding-tab {
    padding: 0;
    height: 100%;
    width: 100%;
    display: flex;
  }

  .radio-span :deep(.el-radio__label) {
    margin-left: 4px;
  }

  .result-count {
    width: 50px;

    :deep(.el-input__wrapper) {
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
    border: 1px solid #dcdfe6;
    overflow-x: hidden;
    display: block;
    align-items: center;
  }

  .draggable-group {
    display: block;
    width: 100%;
    height: calc(100% - 6px);
  }

  .el-input-refresh-time {
    width: calc(50% - 4px) !important;
  }

  .el-input-refresh-unit {
    margin-left: 8px;
    width: calc(50% - 4px) !important;
  }

  .el-input-refresh-loading {
    margin-left: 4px;
    font-size: 12px !important;
  }

  .drag-data {
    margin-top: 6px;
  }

  .editor-title {
    height: @component-toolbar-height;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 10px;
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
  top: 12px;
  cursor: pointer;
  z-index: 2;
}

:deep(.el-collapse-item__header) {
  background-color: @side-area-background !important;
  color: #ffffff;
  padding-left: 5px;
  border-bottom: 1px solid rgba(85, 85, 85, 1);
  height: 38px !important;
}
:deep(.el-collapse-item__content) {
  background-color: @side-content-background;
  color: #ffffff;
  padding-left: 5px;
}

:deep(.el-collapse-item__wrap) {
  border-bottom: 1px solid rgba(85, 85, 85, 1);
}
:deep(.el-collapse) {
  width: 100%;
}
:deep(.el-form-item__label) {
  color: @canvas-main-font-color;
  font-size: 12px;
}
:deep(.el-checkbox) {
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
