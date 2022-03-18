<template>
  <de-container>
    <de-main-container v-if="chart.type !== 'table-normal' && chart.type !== 'table-info'" :style="customStyle" class="full-div">
      <chart-component v-if="!chart.type.includes('text') && chart.type !== 'label' && !chart.type.includes('table') && renderComponent() === 'echarts'" class="chart-class" :chart="chart" />
      <chart-component-g2 v-else-if="!chart.type.includes('text') && chart.type !== 'label' && !chart.type.includes('table') && renderComponent() === 'antv'" class="chart-class" :chart="chart" />
      <chart-component-s2 v-else-if="chart.type === 'table-pivot' && renderComponent() === 'antv'" class="chart-class" :chart="chart" />
      <label-normal v-else-if="chart.type.includes('text')" :chart="chart" class="table-class" />
      <label-normal-text v-else-if="chart.type === 'label'" :chart="chart" class="table-class" />
    </de-main-container>
    <de-main-container v-else>
      <table-normal :chart="chartTable" :show-summary="false" class="table-class" />
    </de-main-container>
  </de-container>
</template>

<script>

import ChartComponent from '@/views/chart/components/ChartComponent.vue'
import TableNormal from '@/views/chart/components/table/TableNormal'
import LabelNormal from '@/views/chart/components/normal/LabelNormal'
import { mapState } from 'vuex'
import ChartComponentG2 from '@/views/chart/components/ChartComponentG2'
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
import LabelNormalText from '@/views/chart/components/normal/LabelNormalText'
import ChartComponentS2 from '@/views/chart/components/ChartComponentS2'

export default {
  name: 'UserViewMobileDialog',
  components: { ChartComponentS2, LabelNormalText, DeContainer, DeMainContainer, ChartComponentG2, ChartComponent, TableNormal, LabelNormal },
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

    renderComponent() {
      return this.chart.render
    }
  }
}
</script>

<style lang="scss" scoped>
  .ms-main-container {
    border: 0px;
  }
  .chart-class{
    height: 100%;
  }
  .table-class{
    height: 100%;
  }
  .full-div{
    background-size:100% 100% !important;
    padding: 5px 0px 5px 0px !important;
  }
</style>
