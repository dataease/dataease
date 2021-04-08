<template>
  <el-row style="height: 100%;overflow-y: hidden;width: 100%;">
    <span v-show="false">{{ vId }}</span>
    <el-row style="height: 40px;background-color: white" class="padding-lr">
      <span style="line-height: 40px;">{{ view.name }}</span>
      <span style="float: right;line-height: 40px;">
        <el-button size="mini" @click="closeEdit">
          {{ $t('chart.close') }}
        </el-button>
        <!--        <el-button type="primary" size="mini" @click="save">-->
        <!--          {{ $t('chart.confirm') }}-->
        <!--        </el-button>-->
      </span>
    </el-row>
    <el-row class="view-panel">
      <el-col
        style="height: 100%;width: 20%;min-width: 180px;max-width:220px;border: 1px solid #E6E6E6;border-left: 0 solid;"
      >
        <div style="height: 50%;border-bottom: 1px solid #E6E6E6;" class="padding-lr">
          <span>{{ $t('chart.dimension') }}</span>
          <draggable
            v-model="dimension"
            :options="{group:{name: 'dimension',pull:'clone'},sort: true}"
            animation="300"
            :move="onMove"
            class="drag-list"
            @end="end1"
            @start="start1"
          >
            <transition-group>
              <span v-for="item in dimension" :key="item.id" class="item">{{ item.name }}</span>
            </transition-group>
          </draggable>
        </div>
        <div style="height: 50%;" class="padding-lr">
          <span>{{ $t('chart.quota') }}</span>
          <draggable
            v-model="quota"
            :options="{group:{name: 'quota',pull:'clone'},sort: true}"
            animation="300"
            :move="onMove"
            class="drag-list"
            @end="end1"
            @start="start1"
          >
            <transition-group>
              <span v-for="item in quota" :key="item.id" class="item">{{ item.name }}</span>
            </transition-group>
          </draggable>
        </div>
      </el-col>

      <el-col
        style="height: 100%;width: 30%;min-width: 200px;max-width:220px;border: 1px solid #E6E6E6;border-left: 0 solid;"
      >
        <div style="border-bottom: 1px solid #E6E6E6;overflow-y:hidden;height: 62px;" class="padding-lr">
          <el-row>
            <span>{{ $t('chart.title') }}</span>
            <el-button style="float: right;padding: 0;margin: 8px 0 0 0;font-size: 12px;" type="text" @click="save">{{ $t('chart.confirm') }}</el-button>
          </el-row>
          <el-form>
            <el-form-item class="form-item">
              <el-input
                v-model="view.title"
                size="mini"
                :placeholder="$t('chart.title')"
                prefix-icon="el-icon-search"
                clearable
              />
            </el-form-item>
          </el-form>
        </div>
        <div style="height: 30%;overflow:auto" class="padding-lr">
          <span>{{ $t('chart.chart_type') }}</span>
          <el-row>
            <div class="chart-type">
              <!--TODO 这里要替换好看点的图标，UI标签可以重新定义-->
              <el-radio-group
                v-model="view.type"
                style="width: 100%"
                @change="save"
              >
                <div style="width: 100%;display: flex;display: -webkit-flex;justify-content: space-between;flex-direction: row;flex-wrap: wrap;">
                  <el-radio value="bar" label="bar"><svg-icon icon-class="bar" class="chart-icon" /></el-radio>
                  <el-radio value="bar-stack" label="bar-stack"><svg-icon icon-class="bar-stack" class="chart-icon" /></el-radio>
                  <el-radio value="bar-horizontal" label="bar-horizontal"><svg-icon icon-class="bar-horizontal" class="chart-icon" /></el-radio>
                  <el-radio value="bar-stack-horizontal" label="bar-stack-horizontal"><svg-icon icon-class="bar-stack-horizontal" class="chart-icon" /></el-radio>
                  <el-radio value="line" label="line"><svg-icon icon-class="line" class="chart-icon" /></el-radio>
                </div>
                <div style="width: 100%;display: flex;display: -webkit-flex;justify-content: space-between;flex-direction: row;flex-wrap: wrap;">
                  <el-radio value="line-stack" label="line-stack"><svg-icon icon-class="line-stack" class="chart-icon" /></el-radio>
                  <el-radio value="pie" label="pie"><svg-icon icon-class="pie" class="chart-icon" /></el-radio>
                  <el-radio value="pie-rose" label="pie-rose"><svg-icon icon-class="pie-rose" class="chart-icon" /></el-radio>
                  <el-radio value="funnel" label="funnel"><svg-icon icon-class="funnel" class="chart-icon" /></el-radio>
                  <el-radio value="radar" label="radar"><svg-icon icon-class="radar" class="chart-icon" /></el-radio>
                </div>
                <div>
                  <el-radio value="" label="" disabled class="disabled-none-cursor"><svg-icon icon-class="" class="chart-icon" /></el-radio>
                  <el-radio value="" label="" disabled class="disabled-none-cursor"><svg-icon icon-class="" class="chart-icon" /></el-radio>
                  <el-radio value="" label="" disabled class="disabled-none-cursor"><svg-icon icon-class="" class="chart-icon" /></el-radio>
                  <el-radio value="" label="" disabled class="disabled-none-cursor"><svg-icon icon-class="" class="chart-icon" /></el-radio>
                  <el-radio value="" label="" disabled class="disabled-none-cursor"><svg-icon icon-class="" class="chart-icon" /></el-radio>
                </div>
              </el-radio-group>
            </div>
          </el-row>
        </div>
        <div style="height: 40%;overflow:hidden;border-top: 1px solid #e6e6e6">
          <el-tabs type="card" :stretch="true" class="tab-header">
            <el-tab-pane :label="$t('chart.shape_attr')" class="padding-lr">
              <color-selector class="attr-selector" :chart="chart" @onColorChange="onColorChange" />
              <size-selector class="attr-selector" :chart="chart" @onSizeChange="onSizeChange" />
              <label-selector class="attr-selector" :chart="chart" @onLabelChange="onLabelChange" />
              <tooltip-selector class="attr-selector" :chart="chart" @onTooltipChange="onTooltipChange" />
            </el-tab-pane>
            <el-tab-pane :label="$t('chart.module_style')" class="padding-lr">
              <title-selector class="attr-selector" :chart="chart" @onTextChange="onTextChange" />
              <legend-selector class="attr-selector" :chart="chart" @onLegendChange="onLegendChange" />
              <x-axis-selector v-if="view.type.includes('bar') || view.type.includes('line')" class="attr-selector" :chart="chart" @onChangeXAxisForm="onChangeXAxisForm" />
              <y-axis-selector v-if="view.type.includes('bar') || view.type.includes('line')" class="attr-selector" :chart="chart" @onChangeYAxisForm="onChangeYAxisForm" />
              <background-color-selector class="attr-selector" :chart="chart" @onChangeBackgroundForm="onChangeBackgroundForm" />
            </el-tab-pane>
          </el-tabs>
        </div>
        <div style="overflow:auto;border-top: 1px solid #e6e6e6" class="padding-lr filter-class">
          <span>{{ $t('chart.result_filter') }}</span>
          <div style="margin: 8px" class="filter-inner-class">
            <draggable
              v-model="view.customFilter"
              group="drag"
              animation="300"
              :move="onMove"
              style="height:100%;margin:0;overflow-x: auto;background-color: white;"
              @end="end2"
            >
              <transition-group class="draggable-group">
                <filter-item v-for="(item,index) in view.customFilter" :key="item.id" :index="index" :item="item" />
              </transition-group>
            </draggable>
          </div>
        </div>
      </el-col>

      <el-col style="height: 100%;min-width: 500px;border-top: 1px solid #E6E6E6;">
        <el-row style="width: 100%;height: 100%;" class="padding-lr">
          <el-row style="margin-top: 10px;">
            <el-row style="display:flex;height: 32px;">
              <span style="line-height: 32px;width: 60px;text-align: right;">{{ $t('chart.dimension') }}</span>
              <draggable
                v-model="view.xaxis"
                group="dimension"
                animation="300"
                :move="onMove"
                style="width:100%;height: 100%;margin:0 10px;border-radius: 4px;border: 1px solid #DCDFE6;overflow-x: auto;display: flex;align-items: center;background-color: white;"
                @end="end2"
              >
                <transition-group class="draggable-group">
                  <dimension-item v-for="(item,index) in view.xaxis" :key="item.id" :index="index" :item="item" @onDimensionItemChange="dimensionItemChange" @onDimensionItemRemove="dimensionItemRemove" @onNameEdit="showRename" />
                </transition-group>
              </draggable>
            </el-row>
            <el-row style="display:flex;height: 32px;margin-top: 10px;">
              <span style="line-height: 32px;width: 60px;text-align: right;">{{ $t('chart.quota') }}</span>
              <draggable
                v-model="view.yaxis"
                group="quota"
                animation="300"
                :move="onMove"
                style="width:100%;height: 100%;margin:0 10px;border-radius: 4px;border: 1px solid #DCDFE6;overflow-x: auto;display: flex;align-items: center;background-color: white;"
                @end="end2"
              >
                <transition-group class="draggable-group">
                  <quota-item v-for="(item,index) in view.yaxis" :key="item.id" :index="index" :item="item" @onQuotaItemChange="quotaItemChange" @onQuotaItemRemove="quotaItemRemove" @editItemFilter="showEditFilter" @onNameEdit="showRename" />
                </transition-group>
              </draggable>
            </el-row>
          </el-row>

          <chart-component :chart-id="chart.id" :chart="chart" class="chart-class" />
        </el-row>
      </el-col>
    </el-row>

    <!--显示名修改-->
    <el-dialog :title="$t('chart.show_name_set')" :visible="renameItem" :show-close="false" width="30%">
      <el-form ref="itemForm" :model="itemForm" :rules="itemFormRules">
        <el-form-item :label="$t('commons.name')" prop="name">
          <el-input v-model="itemForm.name" size="mini" clearable />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeRename()">{{ $t('chart.cancel') }}</el-button>
        <el-button type="primary" size="mini" @click="saveRename">{{ $t('chart.confirm') }}</el-button>
      </div>
    </el-dialog>

    <!--指标过滤器-->
    <el-dialog
      :title="$t('chart.add_filter')"
      :visible="filterEdit"
      :show-close="false"
      width="800px"
      class="dialog-css"
    >
      <quota-filter-editor :item="quotaItem" />
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeQuotaFilter">{{ $t('chart.cancel') }}</el-button>
        <el-button type="primary" size="mini" @click="saveQuotaFilter">{{ $t('chart.confirm') }}</el-button>
      </div>
    </el-dialog>
  </el-row>
</template>

<script>
import { post } from '@/api/dataset/dataset'
import draggable from 'vuedraggable'
import DimensionItem from '../components/drag-item/DimensionItem'
import QuotaItem from '../components/drag-item/QuotaItem'
import FilterItem from '../components/drag-item/FilterItem'
import ChartComponent from '../components/ChartComponent'
import bus from '@/utils/bus'

// shape attr,component style
import {
  DEFAULT_COLOR_CASE,
  DEFAULT_SIZE,
  DEFAULT_TITLE_STYLE,
  DEFAULT_LEGEND_STYLE,
  DEFAULT_LABEL,
  DEFAULT_TOOLTIP,
  DEFAULT_XAXIS_STYLE,
  DEFAULT_YAXIS_STYLE,
  DEFAULT_BACKGROUND_COLOR
} from '../chart/chart'
import ColorSelector from '../components/shape-attr/ColorSelector'
import SizeSelector from '../components/shape-attr/SizeSelector'
import LabelSelector from '../components/shape-attr/LabelSelector'
import TitleSelector from '../components/component-style/TitleSelector'
import LegendSelector from '../components/component-style/LegendSelector'
import TooltipSelector from '../components/shape-attr/TooltipSelector'
import XAxisSelector from '../components/component-style/XAxisSelector'
import YAxisSelector from '../components/component-style/YAxisSelector'
import BackgroundColorSelector from '../components/component-style/BackgroundColorSelector'
import QuotaFilterEditor from '../components/filter/QuotaFilterEditor'

export default {
  name: 'ChartEdit',
  components: { QuotaFilterEditor, BackgroundColorSelector, FilterItem, XAxisSelector, YAxisSelector, TooltipSelector, LabelSelector, LegendSelector, TitleSelector, SizeSelector, ColorSelector, ChartComponent, QuotaItem, DimensionItem, draggable },
  data() {
    return {
      table: {},
      dimension: [],
      quota: [],
      view: {
        xaxis: [],
        yaxis: [],
        show: true,
        type: 'bar',
        title: '',
        customAttr: {
          color: DEFAULT_COLOR_CASE,
          size: DEFAULT_SIZE,
          label: DEFAULT_LABEL,
          tooltip: DEFAULT_TOOLTIP
        },
        customStyle: {
          text: DEFAULT_TITLE_STYLE,
          legend: DEFAULT_LEGEND_STYLE,
          xAxis: DEFAULT_XAXIS_STYLE,
          yAxis: DEFAULT_YAXIS_STYLE,
          background: DEFAULT_BACKGROUND_COLOR
        },
        customFilter: []
      },
      moveId: -1,
      chart: {
        id: 'echart'
      },
      filterEdit: false,
      quotaItem: {},
      renameItem: false,
      itemForm: {
        name: ''
      },
      itemFormRules: {
        name: [
          { required: true, message: this.$t('commons.input_content'), trigger: 'change' }
        ]
      }
    }
  },
  computed: {
    vId() {
      // console.log(this.$store.state.chart.viewId);
      this.getData(this.$store.state.chart.viewId)
      return this.$store.state.chart.viewId
    }

  },
  watch: {
  },
  created() {
    // this.get(this.$store.state.chart.viewId);
  },
  mounted() {
    // this.get(this.$store.state.chart.viewId);
    this.getData(this.$store.state.chart.viewId)
    // this.myEcharts();
  },
  activated() {
  },
  methods: {
    initTableData(id) {
      if (id != null) {
        post('/dataset/table/get/' + id, null).then(response => {
          this.table = response.data
          this.initTableField(id)
        })
      }
    },
    initTableField(id) {
      post('/dataset/table/getFieldsFromDE', this.table).then(response => {
        this.dimension = response.data.dimension
        this.quota = response.data.quota
      })
    },
    save() {
      const view = JSON.parse(JSON.stringify(this.view))
      view.id = this.view.id
      view.sceneId = this.view.sceneId
      view.name = this.view.name ? this.view.name : this.table.name
      view.tableId = this.view.tableId
      // view.xaxis.forEach(function(ele) {
      //   if (!ele.summary || ele.summary === '') {
      //     ele.summary = 'sum'
      //   }
      // })
      view.yaxis.forEach(function(ele) {
        if (!ele.summary || ele.summary === '') {
          ele.summary = 'sum'
        }
        if (!ele.sort || ele.sort === '') {
          ele.sort = 'none'
        }
        if (!ele.filter) {
          ele.filter = []
        }
      })
      if (view.type.startsWith('pie') || view.type.startsWith('funnel')) {
        if (view.yaxis.length > 1) {
          view.yaxis.splice(1, view.yaxis.length)
        }
      }
      view.xaxis = JSON.stringify(view.xaxis)
      view.yaxis = JSON.stringify(view.yaxis)
      view.customAttr = JSON.stringify(view.customAttr)
      view.customStyle = JSON.stringify(view.customStyle)
      view.customFilter = JSON.stringify(view.customFilter)
      post('/chart/view/save', view).then(response => {
        // this.get(response.data.id);
        this.getData(response.data.id)
        this.$store.dispatch('chart/setChartSceneData', null)
        this.$store.dispatch('chart/setChartSceneData', this.sceneId)
      })
    },
    closeEdit() {
      // 从仪表盘入口关闭
      bus.$emit('PanelSwitchComponent', { name: 'PanelEdit' })
      this.$emit('switchComponent', { name: '' })
    },
    getData(id) {
      if (id) {
        post('/chart/view/getData/' + id, null).then(response => {
          this.initTableData(response.data.tableId)
          this.view = JSON.parse(JSON.stringify(response.data))
          this.view.xaxis = this.view.xaxis ? JSON.parse(this.view.xaxis) : []
          this.view.yaxis = this.view.yaxis ? JSON.parse(this.view.yaxis) : []
          this.view.customAttr = this.view.customAttr ? JSON.parse(this.view.customAttr) : {}
          this.view.customStyle = this.view.customStyle ? JSON.parse(this.view.customStyle) : {}
          this.view.customFilter = this.view.customFilter ? JSON.parse(this.view.customFilter) : {}
          // 将视图传入echart组件
          this.chart = response.data
        })
      } else {
        this.view = {}
      }
    },

    // 左边往右边拖动时的事件
    start1(e) {
      console.log(e)
      e.clone.className = 'item-on-move'
      e.item.className = 'item-on-move'
    },
    end1(e) {
      console.log(e)
      e.clone.className = 'item'
      e.item.className = 'item'
      this.refuseMove(e)
      this.removeCheckedKey(e)
      this.save()
    },
    // 右边往左边拖动时的事件
    start2(e) {
      console.log(e)
    },
    end2(e) {
      console.log(e)
      this.removeDuplicateKey(e)
      this.save()
    },
    removeCheckedKey(e) {
      const that = this
      const xItems = this.view.xaxis.filter(function(m) {
        return m.id === that.moveId
      })
      const yItems = this.view.yaxis.filter(function(m) {
        return m.id === that.moveId
      })
      if (xItems && xItems.length > 1) {
        this.view.xaxis.splice(e.newDraggableIndex, 1)
      }
      if (yItems && yItems.length > 1) {
        this.view.yaxis.splice(e.newDraggableIndex, 1)
      }
    },
    refuseMove(e) {
      const that = this
      const xItems = this.dimension.filter(function(m) {
        return m.id === that.moveId
      })
      const yItems = this.quota.filter(function(m) {
        return m.id === that.moveId
      })
      if (xItems && xItems.length > 1) {
        this.dimension.splice(e.newDraggableIndex, 1)
      }
      if (yItems && yItems.length > 1) {
        this.quota.splice(e.newDraggableIndex, 1)
      }
    },
    removeDuplicateKey(e) {
      const that = this
      const xItems = this.dimension.filter(function(m) {
        return m.id === that.moveId
      })
      const yItems = this.quota.filter(function(m) {
        return m.id === that.moveId
      })
      if (xItems && xItems.length > 1) {
        this.dimension.splice(e.newDraggableIndex, 1)
      }
      if (yItems && yItems.length > 1) {
        this.quota.splice(e.newDraggableIndex, 1)
      }
    },
    // move回调方法
    onMove(e, originalEvent) {
      console.log(e)
      this.moveId = e.draggedContext.element.id
      // //不允许停靠
      // if (e.relatedContext.element.id == 1) return false;
      // //不允许拖拽
      // if (e.draggedContext.element.id == 4) return false;
      // if (e.draggedContext.element.id == 11) return false;
      return true
    },

    dimensionItemChange(item) {
      this.save()
    },

    dimensionItemRemove(item) {
      this.view.xaxis.splice(item.index, 1)
      this.save()
    },

    quotaItemChange(item) {
      // 更新item
      // this.view.yaxis.forEach(function(ele) {
      //   if (ele.id === item.id) {
      //     ele.summary = item.summary
      //   }
      // })
      this.save()
    },

    quotaItemRemove(item) {
      this.view.yaxis.splice(item.index, 1)
      this.save()
    },

    onColorChange(val) {
      this.view.customAttr.color = val
      this.save()
    },

    onSizeChange(val) {
      this.view.customAttr.size = val
      this.save()
    },

    onTextChange(val) {
      this.view.customStyle.text = val
      this.save()
    },

    onLegendChange(val) {
      this.view.customStyle.legend = val
      this.save()
    },

    onLabelChange(val) {
      this.view.customAttr.label = val
      this.save()
    },

    onTooltipChange(val) {
      this.view.customAttr.tooltip = val
      this.save()
    },

    onChangeXAxisForm(val) {
      this.view.customStyle.xAxis = val
      this.save()
    },

    onChangeYAxisForm(val) {
      this.view.customStyle.yAxis = val
      this.save()
    },

    onChangeBackgroundForm(val) {
      this.view.customStyle.background = val
      this.save()
    },

    showEditFilter(item) {
      this.quotaItem = JSON.parse(JSON.stringify(item))
      this.filterEdit = true
    },
    closeQuotaFilter() {
      this.filterEdit = false
    },
    saveQuotaFilter() {
      this.view.yaxis[this.quotaItem.index].filter = this.quotaItem.filter
      this.save()
      this.closeQuotaFilter()
    },

    showRename(val) {
      this.itemForm = JSON.parse(JSON.stringify(val))
      this.renameItem = true
    },
    saveRename() {
      if (this.itemForm.renameType === 'quota') {
        this.view.yaxis[this.itemForm.index].name = this.itemForm.name
      } else if (this.itemForm.renameType === 'dimension') {
        this.view.xaxis[this.itemForm.index].name = this.itemForm.name
      }
      this.save()
      this.closeRename()
    },
    closeRename() {
      this.renameItem = false
      this.resetRename()
    },
    resetRename() {
      // this.itemForm = {}
    }
  }
}
</script>

<style scoped>
  .padding-lr {
    padding: 0 6px;
  }

  .itxst {
    margin: 10px;
    text-align: left;
  }

  .col {
    width: 40%;
    flex: 1;
    padding: 10px;
    border: solid 1px #eee;
    border-radius: 5px;
    float: left;
  }

  .col + .col {
    margin-left: 10px;
  }

  .view-panel {
    display: flex;
    height: calc(100% - 40px);
    background-color: #f7f8fa;
  }

  .drag-list {
    height: calc(100% - 26px);
    overflow:auto;
  }

  .item {
    padding: 2px 10px;
    margin: 2px 2px 0 2px;
    border: solid 1px #eee;
    text-align: left;
    color: #606266;
    /*background-color: rgba(35,46,64,.05);*/
    background-color: white;
    display: block;
  }

  .item-on-move {
    padding: 2px 10px;
    margin: 2px 2px 0 2px;
    border: solid 1px #eee;
    text-align: left;
    color: #606266;
    /*background-color: rgba(35,46,64,.05);*/
    background-color: white;
  }

  .item + .item {
    margin-top: 2px;
  }

  .item:hover {
    color: #1890ff;
    background: #e8f4ff;
    border-color: #a3d3ff;
    cursor: pointer;
  }

  .el-form-item {
    margin-bottom: 0;
  }

  span {
    font-size: 12px;
  }

  .tab-header>>>.el-tabs__item{
    font-size: 12px;
    background-color: #E8EAED;
  }

  .tab-header>>>.is-active{
    background-color: #f7f8fa;
    border-bottom-color: #f7f8fa!important;
  }
  .tab-header>>>.el-tabs__nav-scroll{
    padding-left: 0!important;
  }

  .draggable-group {
    display: inline-block;
    width: 100%;
    height: calc(100% - 6px);
  }

  .chart-type{
    padding: 4px;
    display: flex;
    flex-wrap: wrap;
    justify-content: flex-start;
  }

  .chart-icon{
    width: 20px;
    height: 20px;
  }

  .chart-type>>>.el-radio__input{
    display: none;
  }

  .el-radio{
    margin:6px;
  }

  .el-radio>>>.el-radio__label{
    padding-left: 0;
  }

  .attr-selector{
    width:100%;
    height: 32px;
    margin:0 0 6px 0;
    padding:0 4px;
    display: flex;
    align-items: center;
    background-color: white
  }

  .disabled-none-cursor{
    cursor: not-allowed;
    pointer-events:none;
  }

  .filter-class{
    height: calc(35% - 102px);
  }

  .filter-class>>>.filter-inner-class{
    height: calc(100% - 40px);
  }

  .chart-class{
    height: calc(100% - 84px);
    padding: 10px;
  }

  .dialog-css>>>.el-dialog__title {
    font-size: 14px;
  }
  .dialog-css >>> .el-dialog__header {
    padding: 20px 20px 0;
  }
  .dialog-css >>> .el-dialog__body {
    padding: 10px 20px 20px;
  }
</style>
