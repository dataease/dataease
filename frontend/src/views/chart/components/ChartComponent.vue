<template>
  <div style="display: flex;">
    <view-track-bar ref="viewTrack" :track-menu="trackMenu" class="track-bar" :style="trackBarStyleTime" @trackClick="trackClick" />
    <div :id="chartId" style="width: 100%;height: 100%;overflow: hidden;" :style="{ borderRadius: borderRadius}" />
  </div>
</template>

<script>
import {
  BASE_BAR,
  BASE_LINE,
  HORIZONTAL_BAR,
  BASE_PIE,
  BASE_FUNNEL,
  BASE_RADAR,
  BASE_GAUGE,
  BASE_MAP,
  BASE_SCATTER,
  BASE_TREEMAP,
  BASE_MIX
} from '../chart/chart'
import { baseBarOption, stackBarOption, horizontalBarOption, horizontalStackBarOption } from '../chart/bar/bar'
import { baseLineOption, stackLineOption } from '../chart/line/line'
import { basePieOption, rosePieOption } from '../chart/pie/pie'
import { baseMapOption } from '../chart/map/map'
import { baseFunnelOption } from '../chart/funnel/funnel'
import { baseRadarOption } from '../chart/radar/radar'
import { baseGaugeOption } from '../chart/gauge/gauge'
import { baseScatterOption } from '../chart/scatter/scatter'
import { baseTreemapOption } from '../chart/treemap/treemap'
import { baseMixOption } from '@/views/chart/chart/mix/mix'
// import eventBus from '@/components/canvas/utils/eventBus'
import { uuid } from 'vue-uuid'
import { geoJson } from '@/api/map/map'
import ViewTrackBar from '@/components/canvas/components/Editor/ViewTrackBar'

export default {
  name: 'ChartComponent',
  components: { ViewTrackBar },
  props: {
    chart: {
      type: Object,
      required: true
    },
    filter: {
      type: Object,
      required: false,
      default: function() {
        return {}
      }
    },
    trackMenu: {
      type: Array,
      required: false,
      default: function() {
        return ['drill']
      }
    },
    searchCount: {
      type: Number,
      required: false,
      default: 0
    }
  },
  data() {
    return {
      myChart: {},
      chartId: uuid.v1(),
      showTrackBar: true,
      trackBarStyle: {
        position: 'absolute',
        left: '0px',
        top: '0px'
      },
      pointParam: null,

      dynamicAreaCode: null,
      borderRadius: '0px'
    }
  },

  computed: {
    trackBarStyleTime() {
      return this.trackBarStyle
    }
  },
  watch: {
    chart: {
      handler(newVal, oldVla) {
        this.preDraw()
      },
      deep: true
    },
    resize() {
      this.drawEcharts()
    }
  },
  mounted() {
    this.preDraw()
  },
  destroyed() {
    this.myChart.dispose()
  },
  methods: {
    preDraw() {
      // 基于准备好的dom，初始化echarts实例
      // 渲染echart等待dom加载完毕,渲染之前先尝试销毁具有相同id的echart 放置多次切换仪表板有重复id情况
      const that = this
      new Promise((resolve) => { resolve() }).then(() => {
        //	此dom为echarts图标展示dom
        this.myChart = this.$echarts.getInstanceByDom(document.getElementById(this.chartId))
        if (!this.myChart) {
          this.myChart = this.$echarts.init(document.getElementById(this.chartId))
        }
        this.drawEcharts()

        this.myChart.off('click')
        this.myChart.on('click', function(param) {
          that.pointParam = param
          if (that.trackMenu.length < 2) { // 只有一个事件直接调用
            that.trackClick(that.trackMenu[0])
          } else { // 视图关联多个事件
            that.trackBarStyle.left = param.event.offsetX + 'px'
            that.trackBarStyle.top = (param.event.offsetY - 15) + 'px'
            that.$refs.viewTrack.trackButtonClick()
          }
        })
      })
    },
    drawEcharts() {
      const chart = this.chart
      let chart_option = {}
      // type
      if (chart.type === 'bar') {
        chart_option = baseBarOption(JSON.parse(JSON.stringify(BASE_BAR)), chart)
      } else if (chart.type === 'bar-stack') {
        chart_option = stackBarOption(JSON.parse(JSON.stringify(BASE_BAR)), chart)
      } else if (chart.type === 'bar-horizontal') {
        chart_option = horizontalBarOption(JSON.parse(JSON.stringify(HORIZONTAL_BAR)), chart)
      } else if (chart.type === 'bar-stack-horizontal') {
        chart_option = horizontalStackBarOption(JSON.parse(JSON.stringify(HORIZONTAL_BAR)), chart)
      } else if (chart.type === 'line') {
        chart_option = baseLineOption(JSON.parse(JSON.stringify(BASE_LINE)), chart)
      } else if (chart.type === 'line-stack') {
        chart_option = stackLineOption(JSON.parse(JSON.stringify(BASE_LINE)), chart)
      } else if (chart.type === 'pie') {
        chart_option = basePieOption(JSON.parse(JSON.stringify(BASE_PIE)), chart)
      } else if (chart.type === 'pie-rose') {
        chart_option = rosePieOption(JSON.parse(JSON.stringify(BASE_PIE)), chart)
      } else if (chart.type === 'funnel') {
        chart_option = baseFunnelOption(JSON.parse(JSON.stringify(BASE_FUNNEL)), chart)
      } else if (chart.type === 'radar') {
        chart_option = baseRadarOption(JSON.parse(JSON.stringify(BASE_RADAR)), chart)
      } else if (chart.type === 'gauge') {
        chart_option = baseGaugeOption(JSON.parse(JSON.stringify(BASE_GAUGE)), chart)
      } else if (chart.type === 'scatter') {
        chart_option = baseScatterOption(JSON.parse(JSON.stringify(BASE_SCATTER)), chart)
      } else if (chart.type === 'treemap') {
        chart_option = baseTreemapOption(JSON.parse(JSON.stringify(BASE_TREEMAP)), chart)
      } else if (chart.type === 'chart-mix') {
        chart_option = baseMixOption(JSON.parse(JSON.stringify(BASE_MIX)), chart)
      }
      // console.log(JSON.stringify(chart_option))
      if (this.myChart && this.searchCount > 0) {
        chart_option.animation = false
      }

      if (chart.type === 'map') {
        const customAttr = JSON.parse(chart.customAttr)
        if (!customAttr.areaCode) return
        const cCode = this.dynamicAreaCode || customAttr.areaCode
        if (this.$store.getters.geoMap[cCode]) {
          const json = this.$store.getters.geoMap[cCode]
          this.initMapChart(json, chart)
          return
        }

        geoJson(cCode).then(res => {
          this.$store.dispatch('map/setGeo', {
            key: cCode,
            value: res
          }).then(() => {
            this.initMapChart(res, chart)
          })
        })
        return
      }
      this.myEcharts(chart_option)
    },
    registerDynamicMap(areaCode) {
      this.dynamicAreaCode = areaCode
    //   if (this.$store.getters.geoMap[areaCode]) {
    //     const json = this.$store.getters.geoMap[areaCode]
    //     this.myChart.dispose()
    //     this.myChart = this.$echarts.getInstanceByDom(document.getElementById(this.chartId))
    //     this.$echarts.registerMap('MAP', json)
    //     return
    //   }
    //   geoJson(areaCode).then(res => {
    //     this.$store.dispatch('map/setGeo', {
    //       key: areaCode,
    //       value: res
    //     }).then(() => {
    //       this.myChart.dispose()
    //       this.myChart = this.$echarts.getInstanceByDom(document.getElementById(this.chartId))
    //       this.$echarts.registerMap('MAP', res)
    //     })
    //   }).catch(() => {
    //     this.downOrUp = true
    //   })
    },

    initMapChart(geoJson, chart) {
      this.$echarts.registerMap('MAP', geoJson)
      // this.$echarts.getMap('MAP') || this.$echarts.registerMap('MAP', geoJson)
      const base_json = JSON.parse(JSON.stringify(BASE_MAP))
      const chart_option = baseMapOption(base_json, chart)
      this.myEcharts(chart_option)
    },
    myEcharts(option) {
      // 指定图表的配置项和数据
      const chart = this.myChart
      this.setBackGroundBorder()
      setTimeout(chart.setOption(option, true), 500)
      window.onresize = function() {
        chart.resize()
      }
    },
    setBackGroundBorder() {
      if (this.chart.customStyle) {
        const customStyle = JSON.parse(this.chart.customStyle)
        if (customStyle.background) {
          this.borderRadius = (customStyle.background.borderRadius || 0) + 'px'
        }
      }
    },
    chartResize() {
      // 指定图表的配置项和数据
      const chart = this.myChart
      chart.resize()
      this.reDrawMap()
    },
    reDrawMap() {
      const chart = this.chart
      if (chart.type === 'map') {
        this.preDraw()
      }
    },
    trackClick(trackAction) {
      const param = this.pointParam
      if (!param || !param.data || !param.data.dimensionList) {
        // 地图提示没有关联字段 其他没有维度信息的 直接返回
        if (this.chart.type === 'map') {
          this.$warning(this.$t('panel.no_drill_field'))
        }
        return
      }
      const linkageParam = {
        option: 'linkage',
        viewId: this.chart.id,
        dimensionList: this.pointParam.data.dimensionList,
        quotaList: this.pointParam.data.quotaList
      }
      const jumpParam = {
        option: 'jump',
        viewId: this.chart.id,
        dimensionList: this.pointParam.data.dimensionList,
        quotaList: this.pointParam.data.quotaList
      }
      switch (trackAction) {
        case 'drill':
          this.$emit('onChartClick', this.pointParam)
          break
        case 'linkage':
          this.$store.commit('addViewTrackFilter', linkageParam)
          break
        case 'jump':
          this.$emit('onJumpClick', jumpParam)
          break
        default:
          break
      }
    }
  }
}
</script>

<style scoped>

</style>
