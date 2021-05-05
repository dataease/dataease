<template>
  <div v-loading="$store.getters.loadingMap[$store.getters.currentPath]" class="rect-shape">
    <div v-if="requestStatus==='error'" style=";width: 100%;height: 100%;background-color: #ece7e7; text-align: center">
      <div style="font-size: 12px; color: #9ea6b2;">
        获取数据出错 请联系管理员<br>
        {{ message }}
      </div>
    </div>
    <chart-component v-if="requestStatus==='success'&&chart.type && !chart.type.includes('table')" :ref="element.propValue.id" class="chart-class" :chart="chart" />
    <table-normal v-if="requestStatus==='success'&&chart.type && chart.type.includes('table')" :chart="chart" class="table-class" />

  </div>
</template>

<script>

import { post } from '@/api/panel/panel'
import ChartComponent from '@/views/chart/components/ChartComponent.vue'
import TableNormal from '@/views/chart/components/table/TableNormal'

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
  components: { ChartComponent, TableNormal },
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
      },
      requestStatus: 'waiting',
      message: null
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
        this.requestStatus = 'waiting'
        this.message = null
        post('/chart/view/getData/' + id, this.filter).then(response => {
          // 将视图传入echart组件
          debugger
          if (response.success) {
            this.chart = response.data
            this.requestStatus = 'success'
          } else {
            this.requestStatus = 'error'
            this.message = response.massage
          }
          return true
        }).catch(err => {
          this.requestStatus = 'error'
          this.message = err
          return true
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
.table-class{
  height: 100%;
}
</style>
