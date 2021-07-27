<template>
  <el-row class="slider-container">
    <div style="height: 40px; line-height: 40px; padding-left: 15px; text-align: left; white-space: pre; text-overflow: ellipsis; left: 0px; right: 0px; top: 0px; font-weight: 700">{{ $t('panel.dashboard_theme') }} </div>
    <div style="height: 1px; position: absolute; left: 15px; right: 15px; top: 40px; box-sizing:border-box;border-bottom: 1px solid #e8eaed" />
    <div>
      <slider />
    </div>
    <!--折叠面板-->
    <div v-if="collapseShow" style="margin: 10px;overflow-y: auto">
      <el-collapse v-model="activeNames" @change="handleChange">
        <el-collapse-item :title="$t('panel.panel')" name="panel">
          <el-row style="background-color: #f7f8fa; margin: 5px">
            <background-selector class="attr-selector" />
            <component-gap class="attr-selector" />
          </el-row>
        </el-collapse-item>
        <el-collapse-item :title="$t('chart.module_style')" name="component">
          <el-row style="background-color: #f7f8fa; margin: 5px">
            <!--            <title-selector class="attr-selector" :chart="chart" @onTextChange="onTextChange" />-->
            <panel-background-color-selector v-if="chart" class="attr-selector" :chart="chart" @onChangeBackgroundForm="onChangeBackgroundForm" />
          </el-row>
        </el-collapse-item>
        <el-collapse-item :title="$t('chart.shape_attr')" name="graphical">
          <el-row style="background-color: #f7f8fa; margin: 5px">
            <panel-color-selector :source-type="'panelEchart'" class="attr-selector" :chart="chart" @onColorChange="onColorChange" />
          </el-row>
        </el-collapse-item>
        <el-collapse-item :title="$t('panel.table')" name="table">
          <el-row style="background-color: #f7f8fa; margin: 5px">
            <panel-color-selector index="10002" :source-type="'panelTable'" class="attr-selector" :chart="tableChart" @onColorChange="onTableColorChange" />
          </el-row>
        </el-collapse-item>
      </el-collapse>
    </div>
  </el-row>
</template>

<script>
import slider from './PreSubject/Slider'
import BackgroundSelector from './PanelStyle/BackgroundSelector'
import PanelBackgroundColorSelector from './PanelStyle/PanelBackgroundColorSelector'
import PanelColorSelector from './PanelStyle/PanelColorSelector'
import ComponentGap from './PanelStyle/ComponentGap'

import { mapState } from 'vuex'
import { deepCopy } from '@/components/canvas/utils/utils'
import bus from '@/utils/bus'

export default {
  components: {
    slider,
    BackgroundSelector,
    ComponentGap,
    PanelColorSelector,
    PanelBackgroundColorSelector
  },
  data() {
    return {
      panelInfo: this.$store.state.panel.panelInfo,
      activeNames: ['panel'],
      chart: null,
      tableChart: null,
      collapseShow: true
    }
  },
  computed: mapState([
    'canvasStyleData'
  ]),

  watch: {
  },

  mounted() {
    bus.$on('onSubjectChange', () => {
      this.collapseShow = false
      this.$nextTick(() => {
        this.init()
        this.collapseShow = true
      })
    })
  },
  created() {
    this.init()
  },

  methods: {

    init() {
      // 初始化赋值
      const chart = deepCopy(this.canvasStyleData.chart)
      if (chart.xaxis) {
        chart.xaxis = JSON.parse(chart.xaxis)
      }
      if (chart.yaxis) {
        chart.yaxis = JSON.parse(chart.yaxis)
      }
      chart.customAttr = JSON.parse(chart.customAttr)
      chart.customStyle = JSON.parse(chart.customStyle)
      chart.customFilter = JSON.parse(chart.customFilter)
      this.chart = chart

      // 因为 table 的color 设置和view的共用 所以单独设置一个对象
      this.tableChart = deepCopy(this.chart)
      this.tableChart.customAttr.color = this.tableChart.customAttr.tableColor
    },
    handleChange(val) {
      // console.log(val)
    },
    onChangePanelStyle(parma) {
      // console.log('parma:' + JSON.stringify(parma))
    },
    onColorChange(val) {
      this.chart.customAttr.color = val
      this.save()
    },
    onTableColorChange(val) {
      this.chart.customAttr.tableColor = val
      this.save()
    },
    onTextChange(val) {
      this.chart.customStyle.text = val
      this.save()
    },
    onChangeBackgroundForm(val) {
      this.chart.customStyle.background = val
      this.save()
    },
    save() {
      const canvasStyleData = deepCopy(this.canvasStyleData)
      const chart = deepCopy(this.chart)
      chart.xaxis = JSON.stringify(this.chart.xaxis)
      chart.yaxis = JSON.stringify(this.chart.yaxis)
      chart.customAttr = JSON.stringify(this.chart.customAttr)
      chart.customStyle = JSON.stringify(this.chart.customStyle)
      chart.customFilter = JSON.stringify(this.chart.customFilter)
      canvasStyleData.chart = chart
      this.$store.commit('setCanvasStyle', canvasStyleData)
      this.$store.commit('recordSnapshot')
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
