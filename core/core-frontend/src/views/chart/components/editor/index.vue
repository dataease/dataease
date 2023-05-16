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
  DEFAULT_THRESHOLD
} from './util/chart'
import { reactive, ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { getDatasetTree } from '@/api/dataset'
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

const { t } = useI18n()
const loading = ref(false)
const tabActive = ref('data')

const props = {
  label: 'name',
  children: 'children',
  isLeaf: node => !node.children?.length
}

const fieldType = (deType: number) => {
  return ['text', 'time', 'value', 'value', 'location'][deType]
}

const dsFieldDragOptions = { group: { name: 'drag', pull: 'clone' }, sort: true }

const state = reactive({
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
      threshold: DEFAULT_THRESHOLD
    }
  },
  datasetTree: [],
  dimensionData: [],
  quotaData: []
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

    state.view.tableId = id
  })
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

const quotaItemChange = item => {
  // this.calcData(true)
  // console.log(item)
  // console.log(state.view.xaxis)
  calcData(state.view)
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
  calcData(state.view)
}

const addYaxis = e => {
  // this.dragCheckType(this.view.yaxis, 'q')
  // this.dragMoveDuplicate(this.view.yaxis, e)
  // if ((this.view.type === 'waterfall' || this.view.type === 'word-cloud' || this.view.type.includes('group')) && this.view.yaxis.length > 1) {
  //   this.view.yaxis = [this.view.yaxis[0]]
  // }
  // this.calcData(true)
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

const save = () => {
  saveChart(state.view)
}

initDataset()
</script>

<template>
  <div>
    <el-row v-loading="loading" class="de-chart-editor">
      <!--      <el-tooltip :content="t('chart.draw_back')">-->
      <!--        <el-button circle secondary class="back-button">-->
      <!--          <template #icon>-->
      <!--            <el-icon>-->
      <!--              <Icon name="icon_down-right_outlined"></Icon>-->
      <!--            </el-icon>-->
      <!--          </template>-->
      <!--        </el-button>-->
      <!--      </el-tooltip>-->

      <!--      <el-row style="height: 40px" class="padding-lr">-->
      <!--        <span class="title-text view-title-name" style="line-height: 40px">{{-->
      <!--          state.view.name-->
      <!--        }}</span>-->
      <!--        <span :style="{ float: 'right', lineHeight: '40px' }">-->
      <!--          <el-button secondary round @click="save"> {{ $t('chart.recover') }}(当保存用) </el-button>-->
      <!--        </span>-->
      <!--      </el-row>-->

      <el-col style="display: flex; height: 100%">
        <div style="width: 280px" class="view-panel-row">
          <el-row class="editor-title">
            <span>chart</span>
            <span>
              <el-icon>
                <Icon name="icon_down-right_outlined"></Icon>
              </el-icon>
            </span>
          </el-row>

          <el-row :style="{ borderTop: '1px solid #e6e6e6' }">
            <el-tabs v-model="tabActive" :stretch="true" class="tab-header">
              <el-tab-pane name="data" :label="t('chart.chart_data')" class="padding-tab">
                <el-col>
                  <div style="height: 60px; overflow: auto" class="padding-lr theme-border-class">
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

                  <div
                    :style="{ overflow: 'auto', height: '100%', borderTop: '1px solid #e6e6e6' }"
                    class="attr-style theme-border-class"
                  >
                    <el-row style="height: 100%">
                      <el-row class="padding-lr">
                        <!--                    <span-->
                        <!--                      v-show="view.type === 'richTextView'"-->
                        <!--                      style="color: #909399; font-size: 8px; width: 80px; text-align: right"-->
                        <!--                    >-->
                        <!--                      Tips:{{ t('chart.rich_text_view_result_tips') }}-->
                        <!--                    </span>-->
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
                          group="drag"
                          animation="300"
                          class="drag-block-style"
                          @add="addXaxis"
                        >
                          <template #item="{ element, index }">
                            <dimension-item
                              :dimension-data="state.dimensionData"
                              :quota-data="state.quotaData"
                              :chart="state.view"
                              :item="element"
                              :index="index"
                              @onDimensionItemChange="dimensionItemChange"
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
                          group="drag"
                          animation="300"
                          class="drag-block-style"
                          @add="addYaxis"
                        >
                          <template #item="{ element, index }">
                            <quota-item
                              :dimension-data="state.dimensionData"
                              :quota-data="state.quotaData"
                              :chart="state.view"
                              :item="element"
                              :index="index"
                              @onQuotaItemChange="quotaItemChange"
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
                />
              </el-tab-pane>

              <el-tab-pane
                name="senior"
                :label="t('chart.senior')"
                class="padding-tab"
                style="width: 100%"
              >
                <senior />
              </el-tab-pane>
            </el-tabs>
          </el-row>
        </div>

        <div :style="{ borderLeft: '1px solid #e6e6e6', width: '200px' }" class="view-panel-row">
          <el-row class="editor-title">
            <span>dataset</span>
            <span>
              <el-icon>
                <Icon name="icon_down-right_outlined"></Icon>
              </el-icon>
            </span>
          </el-row>

          <el-row :style="{ borderTop: '1px solid #e6e6e6' }">
            <el-tree-select
              v-model="state.view.tableId"
              :data="state.datasetTree"
              :props="props"
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
              <span>{{ $t('chart.dimension') }}</span>
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
      </el-col>
    </el-row>
  </div>
</template>

<style lang="less" scoped>
.el-row {
  display: block;
}

span {
  font-size: 14px;
}

.de-chart-editor {
  height: 100%;
  overflow-y: hidden;
  border-left: 1px solid #e6e6e6;
  width: 100%;

  .back-button {
    width: 28px;
    height: 28px;
    min-width: auto;
    position: absolute;
    left: 4px;
    top: 5px;
    z-index: 1;
  }

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
    background-color: #f7f8fa;
    overflow-y: hidden;
    overflow-x: hidden;
    height: 100%;
  }

  .view-panel-row :deep(.el-collapse-item__header) {
    height: 34px !important;
    line-height: 34px !important;
    padding: 0 0 0 6px !important;
    font-size: 12px !important;
    font-weight: 400 !important;
  }

  .tab-header :deep(.el-tabs__header) {
    border-top: solid 1px #eee;
    border-right: solid 1px #eee;
  }

  .tab-header :deep(.el-tabs__item) {
    font-size: 12px;
    padding: 0 20px !important;
  }

  .blackTheme .tab-header :deep(.el-tabs__item) {
    background-color: var(--MainBG);
  }

  .tab-header :deep(.el-tabs__nav-scroll) {
    padding-left: 0 !important;
  }

  .tab-header :deep(.el-tabs__header) {
    margin: 0 !important;
  }

  .tab-header :deep(.el-tabs__content) {
    height: calc(100% - 40px);
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
    /*background-color: rgba(35,46,64,.05);*/
    background-color: white;
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
    background-color: white;
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
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 10px;
  }
}
</style>
