<template>
  <de-container>
    <de-aside-container v-if="chart.type !== 'table-normal' && chart.type !== 'table-info'" :style="customStyle">
      <plugin-com
        v-if="chart.isPlugin"

        :component-name="chart.type + '-view'"
        :obj="{chart}"
        class="chart-class"
      />
      <chart-component v-else-if="!chart.type.includes('text') && chart.type !== 'label' && !chart.type.includes('table') && renderComponent() === 'echarts'" class="chart-class" :chart="chart" />
      <progressBar v-else-if="chart.type === 'progress' && renderComponent() === 'echarts'" class="chart-class" :chart="chart" />
      <progressLoop v-else-if="chart.type === 'progress-loop' && renderComponent() === 'echarts'" class="chart-class" :chart="chart" />
      <chart-component-g2 v-else-if="!chart.type.includes('text') && chart.type !== 'label' && !chart.type.includes('table') && renderComponent() === 'antv'" class="chart-class" :chart="chart" />
      <chart-component-s2 v-else-if="chart.type === 'table-pivot' && renderComponent() === 'antv'" class="chart-class" :chart="chart" />
      <chart-component-H3 v-else-if="renderComponent() === 'other'" class="chart-class" :chart="chart"/>
      <chart-component-hc v-else-if="renderComponent() === 'highcharts'" class="chart-class" :chart="chart" />
      <label-normal v-else-if="chart.type.includes('text')" :chart="chart" class="table-class" />
      <label-normal-text v-else-if="chart.type === 'label'" :chart="chart" class="table-class" />
    </de-aside-container>
    <de-main-container>
      <table-normal :chart="chartTable" :show-summary="false" class="table-class" />
    </de-main-container>
  </de-container>
</template>

<script>

import ChartComponent from '@/views/chart/components/ChartComponent.vue'
import progressBar from '@/views/chart/components/progress/progressBar'
import progressLoop from '@/views/chart/components/progress/progressLoop'
import TableNormal from '@/views/chart/components/table/TableNormal'
import LabelNormal from '@/views/chart/components/normal/LabelNormal'
import DeMainContainer from '@/components/datains/DeMainContainer'
import DeContainer from '@/components/datains/DeContainer'
import DeAsideContainer from '@/components/datains/DeAsideContainer'
import { export_json_to_excel } from '@/plugins/Export2Excel'
import { mapState } from 'vuex'
import ChartComponentG2 from '@/views/chart/components/ChartComponentG2'
import PluginCom from '@/views/system/plugin/PluginCom'
import ChartComponentS2 from '@/views/chart/components/ChartComponentS2'
import ChartComponentH3 from '@/views/chart/components/ChartComponentH3'
import ChartComponentHc from '@/views/chart/components/ChartComponentHc.vue'
import LabelNormalText from '@/views/chart/components/normal/LabelNormalText'
export default {
  name: 'UserView',
  components: { 
    LabelNormalText, 
    ChartComponentS2, 
    ChartComponentG2, 
    DeMainContainer, 
    DeContainer, 
    DeAsideContainer, 
    ChartComponent, 
    TableNormal, 
    LabelNormal, 
    PluginCom,
    ChartComponentH3,
    ChartComponentHc,
    progressBar,
    progressLoop
  },
  props: {
    chart: {
      type: Object,
      default: null
    },
    chartTable: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      refId: null
    }
  },
  computed: {
    customStyle() {
      let style = {
      }
      console.log('触发检测0001')
      if (this.canvasStyleData.openCommonStyle) {
        if (this.canvasStyleData.panel.backgroundType === 'image' && this.canvasStyleData.panel.imageUrl) {
          style = {
            background: `url(${this.canvasStyleData.panel.imageUrl}) no-repeat`,
            ...style
          }
        } else if (this.canvasStyleData.panel.backgroundType === 'color') {
          style = {
            background: this.canvasStyleData.panel.color,
            ...style
          }
        }
      }
      if (!style.background) {
        style.background = '#FFFFFF'
      }
      return style
    },
    ...mapState([
      'isClickComponent',
      'curComponent',
      'componentData',
      'canvasStyleData'
    ])
  },
  methods: {
    exportExcel() {
      const excelHeader = JSON.parse(JSON.stringify(this.chart.data.fields)).map(item => item.name)
      const excelHeaderKeys = JSON.parse(JSON.stringify(this.chart.data.fields)).map(item => item.datainsName)
      const excelData = JSON.parse(JSON.stringify(this.chart.data.tableRow)).map(item => excelHeaderKeys.map(i => item[i]))
      const excelName = this.chart.name
      export_json_to_excel(excelHeader, excelData, excelName)
    },

    renderComponent() {
      console.log(this.chart)
      return this.chart.render
    }
  }
}
</script>

<style lang="scss" scoped>
  .ms-aside-container {
    height: 50vh;
    min-width: 400px;
    max-width: 400px;
    padding: 0 0;
  }
  .ms-main-container {
    height: 50vh;
    border: 1px solid #E6E6E6;
    border-left: 0 solid;
  }
  .chart-class{
    height: 100%;
  }
  .table-class{
    height: 100%;
  }
</style>
