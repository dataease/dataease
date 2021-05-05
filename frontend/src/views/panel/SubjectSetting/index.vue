<template>
  <el-row class="slider-container">
    <div style="height: 40px; line-height: 40px; padding-left: 15px; text-align: left; white-space: pre; text-overflow: ellipsis; left: 0px; right: 0px; top: 0px; font-weight: 700">仪表盘主题</div>
    <div style="height: 1px; position: absolute; left: 15px; right: 15px; top: 40px; box-sizing:border-box;border-bottom: 1px solid #e8eaed" />
    <div>
      <slider />
    </div>
    <!--折叠面板-->
    <div style="margin: 10px;overflow-y: auto">
      <el-collapse v-model="activeNames" @change="handleChange">
        <el-collapse-item title="仪表盘" name="panel">
          <el-row style="background-color: #f7f8fa; margin: 5px">
            <background-selector class="attr-selector" />
            <component-gap class="attr-selector" />
          </el-row>
        </el-collapse-item>
        <el-collapse-item title="组件样式" name="component" @click="testClick">
          <el-row style="background-color: #f7f8fa; margin: 5px">
            <title-selector class="attr-selector" :chart="chart" @onTextChange="onTextChange" />
            <legend-selector class="attr-selector" :chart="chart" @onLegendChange="onLegendChange" />
            <x-axis-selector class="attr-selector" :chart="chart" @onChangeXAxisForm="onChangeXAxisForm" />
            <y-axis-selector class="attr-selector" :chart="chart" @onChangeYAxisForm="onChangeYAxisForm" />
            <background-color-selector class="attr-selector" :chart="chart" @onChangeBackgroundForm="onChangeBackgroundForm" />
          </el-row>
        </el-collapse-item>
        <el-collapse-item title="图形属性" name="graphical">
          <el-row style="background-color: #f7f8fa; margin: 5px">
            <color-selector class="attr-selector" :chart="chart" @onColorChange="onColorChange" />
            <label-selector class="attr-selector" :chart="chart" @onLabelChange="onLabelChange" />
            <tooltip-selector class="attr-selector" :chart="chart" @onTooltipChange="onTooltipChange" />
          </el-row>
        </el-collapse-item>
        <el-collapse-item title="表格" name="table">
          <el-row style="background-color: #f7f8fa; margin: 5px">
            <label-selector class="attr-selector" :chart="chart" @onLabelChange="onLabelChange" />
            <tooltip-selector class="attr-selector" :chart="chart" @onTooltipChange="onTooltipChange" />
          </el-row>
        </el-collapse-item>
        <el-collapse-item title="过滤组件" name="filter">
          <el-row style="background-color: #f7f8fa; margin: 5px">
            <background-selector class="attr-selector" @onChangePanelStyle="onChangePanelStyle" />
            <component-gap class="attr-selector" @onChangePanelStyle="onChangePanelStyle" />
          </el-row>
        </el-collapse-item>
      </el-collapse>
    </div>
  </el-row>
</template>

<script>
import slider from './PreSubject/Slider'
import BackgroundSelector from './PanelStyle/BackgroundSelector'
import ComponentGap from './PanelStyle/ComponentGap'

import ColorSelector from '@/views/chart/components/shape-attr/ColorSelector'
import SizeSelector from '@/views/chart/components/shape-attr/SizeSelector'
import LabelSelector from '@/views/chart/components/shape-attr/LabelSelector'
import TitleSelector from '@/views/chart/components/component-style/TitleSelector'
import LegendSelector from '@/views/chart/components/component-style/LegendSelector'
import TooltipSelector from '@/views/chart/components/shape-attr/TooltipSelector'
import XAxisSelector from '@/views/chart/components/component-style/XAxisSelector'
import YAxisSelector from '@/views/chart/components/component-style/YAxisSelector'
import BackgroundColorSelector from '@/views/chart/components/component-style/BackgroundColorSelector'
import QuotaFilterEditor from '@/views/chart/components/filter/QuotaFilterEditor'
import DimensionFilterEditor from '@/views/chart/components/filter/DimensionFilterEditor'
import TableNormal from '@/views/chart/components/table/TableNormal'
import { mapState } from 'vuex'
import { deepCopy } from '@/components/canvas/utils/utils'

export default {
  components: {
    slider,
    BackgroundSelector,
    ComponentGap,
    ColorSelector,
    SizeSelector,
    LabelSelector,
    TitleSelector,
    LegendSelector,
    TooltipSelector,
    XAxisSelector,
    YAxisSelector,
    BackgroundColorSelector,
    QuotaFilterEditor,
    DimensionFilterEditor,
    TableNormal
  },
  data() {
    return {
      panelInfo: this.$store.state.panel.panelInfo,
      activeNames: ['panel'],
      chart: null
    }
  },
  computed: mapState([
    'canvasStyleData'
  ]),

  watch: {
    // deep监听panel 如果改变 提交到 store
    chart: {
      handler(newVal, oldVla) {
        debugger
        const canvasStyleData = deepCopy(this.canvasStyleData)
        const chart = deepCopy(this.chart)
        chart.xaxis = JSON.stringify(this.chart.xaxis)
        chart.yaxis = JSON.stringify(this.chart.yaxis)
        chart.customAttr = JSON.stringify(this.chart.customAttr)
        chart.customStyle = JSON.stringify(this.chart.customStyle)
        chart.customFilter = JSON.stringify(this.chart.customFilter)
        canvasStyleData.chart = chart
        this.$store.commit('setCanvasStyle', canvasStyleData)
      },
      deep: true
    }
  },
  created() {
    debugger
    // 初始化赋值
    const chart = deepCopy(this.canvasStyleData.chart)
    chart.xaxis = JSON.parse(chart.xaxis)
    chart.yaxis = JSON.parse(chart.yaxis)
    chart.customAttr = JSON.parse(chart.customAttr)
    chart.customStyle = JSON.parse(chart.customStyle)
    chart.customFilter = JSON.parse(chart.customFilter)
    this.chart = chart
  },

  methods: {
    testClick(val) {
      debugger
      console.log(JSON.stringify(this.chart))
      console.log('message+>')
    },
    handleChange(val) {
      console.log(val)
    },
    onChangePanelStyle(parma) {
      console.log('parma:' + JSON.stringify(parma))
    },

    dimensionItemChange(item) {
      this.save()
    },

    dimensionItemRemove(item) {
      this.chart.xaxis.splice(item.index, 1)
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
      this.chart.yaxis.splice(item.index, 1)
      this.save()
    },

    onColorChange(val) {
      this.chart.customAttr.color = val
      this.save()
    },

    onSizeChange(val) {
      this.chart.customAttr.size = val
      this.save()
    },
    onTextChange(val) {
      this.chart.customStyle.text = val
      // this.save()
    },

    onLegendChange(val) {
      this.chart.customStyle.legend = val
      this.save()
    },

    onLabelChange(val) {
      this.chart.customAttr.label = val
      this.save()
    },

    onTooltipChange(val) {
      this.chart.customAttr.tooltip = val
      this.save()
    },

    onChangeXAxisForm(val) {
      this.chart.customStyle.xAxis = val
      this.save()
    },

    onChangeYAxisForm(val) {
      this.chart.customStyle.yAxis = val
      this.save()
    },

    onChangeBackgroundForm(val) {
      this.chart.customStyle.background = val
      this.save()
    },
    save() {
      console.log('save')
    }
  }
}
</script>

<style lang="scss" scoped>
  .slider-container {
    width: 100%;
    overflow: hidden auto;
    min-height: 24px;
    padding-top: 0px;
    padding-bottom: 0px;
    position: relative;
    max-height: 976px;
    color: #3d4d66;
    font-size: 12px;
  }
  .attr-selector{
    background-color: white;
    height: 32px;
    margin: 5px 5px 5px 5px;
    padding:0 4px;
    display: flex;
    align-items: center;
    z-index: 10001;
  }

</style>
