<template>
  <div class="rect-shape">
    <chart-component :ref="element.propValue.id" class="chart-class" :chart="chart" />
  </div>
</template>

<script>

import { post } from '@/api/panel/panel'
import ChartComponent from '@/views/chart/components/ChartComponent.vue'
import { mapState } from 'vuex'
import { deepCopy } from '@/components/canvas/utils/utils'

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
} from '@/views/chart/chart/chart'

export default {
  name: 'UserView',
  components: { ChartComponent },
  props: {
    element: {
      type: Object
    },
    filter: {
      type: Object,
      required: false,
      default: function() {
        return {
          filter: []
        }
      }
    }
  },
  watch: {
    filter(val) {
      this.getData(this.element.propValue.viewId)
    },
    // deep监听panel 如果改变 提交到 store
    canvasStyleData: {
      handler(newVal, oldVla) {
        debugger
        // this.chart.viewFirst == false 优先使用仪表盘样式
        if (!this.chart.viewFirst) {
          this.chart = {
            ...this.chart,
            customAttr: this.canvasStyleData.chart.customAttr,
            customStyle: this.canvasStyleData.chart.customStyle
          }
        }
      },
      deep: true
    }
  },
  computed: mapState([
    'canvasStyleData'
  ]),
  // computed: mapState({
  //   canvasStyleData: function(state) {
  //     debugger
  //     // this.chart.viewFirst == false 优先使用仪表盘样式
  //     if (!this.chart.viewFirst) {
  //       this.chart.customAttr = state.canvasStyleData.chart.customAttr
  //       this.chart.customStyle = state.canvasStyleData.chart.customStyle
  //     }
  //   }
  //
  // }),
  data() {
    return {
      chart: {
        viewFirst: false,
        xaxis: [],
        yaxis: [],
        show: true,
        type: 'panel',
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
      }
    }
  },
  created() {
    this.getData(this.element.propValue.viewId)
  },
  mounted() {

  },
  methods: {
    getData(id) {
      if (id) {
        post('/chart/view/getData/' + id, this.filter).then(response => {
          // 将视图传入echart组件
          this.chart = response.data
        })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.rect-shape {
    width: 100%;
    height: 100%;
    overflow: auto;
}
.chart-class{
  height: 100%;
}
</style>
