<template>
  <div style="display: flex;">
    <view-track-bar ref="viewTrack" :track-menu="trackMenu" class="track-bar" :style="trackBarStyleTime" @trackClick="trackClick" />
    <div :id="chartId" style="width: 100%;height: 100%;overflow: hidden;" :style="{ borderRadius: borderRadius}" />
  </div>
</template>

<script>
import { baseLiquid } from '@/views/chart/chart/liquid/liquid'
// import eventBus from '@/components/canvas/utils/eventBus'
import { uuid } from 'vue-uuid'
import ViewTrackBar from '@/components/canvas/components/Editor/ViewTrackBar'

export default {
  name: 'ChartComponentG2',
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
    }
  },
  data() {
    return {
      myChart: null,
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
  methods: {
    preDraw() {
      // 基于准备好的dom，初始化echarts实例
      // 渲染echart等待dom加载完毕,渲染之前先尝试销毁具有相同id的echart 放置多次切换仪表板有重复id情况
      new Promise((resolve) => { resolve() }).then(() => {
        //	此dom为echarts图标展示dom
        // this.myChart = this.$echarts.getInstanceByDom(document.getElementById(this.chartId))
        // if (!this.myChart) {
        //   this.myChart = this.$echarts.init(document.getElementById(this.chartId))
        // }
        this.drawEcharts()

        // this.myChart.off('click')
        // this.myChart.on('click', function(param) {
        //   that.pointParam = param
        //   if (that.trackMenu.length < 2) { // 只有一个事件直接调用
        //     that.trackClick(that.trackMenu[0])
        //   } else { // 视图关联多个事件
        //     that.trackBarStyle.left = param.event.offsetX + 'px'
        //     that.trackBarStyle.top = (param.event.offsetY - 15) + 'px'
        //     that.$refs.viewTrack.trackButtonClick()
        //   }
        // })
      })
    },
    drawEcharts() {
      const chart = this.chart
      // type
      if (chart.type === 'liquid') {
        this.myChart = baseLiquid(this.myChart, this.chartId, chart)
      }

      this.setBackGroundBorder()
      // console.log(JSON.stringify(chart_option))
    },

    // myEcharts(option) {
    //   // 指定图表的配置项和数据
    //   const chart = this.myChart
    //   this.setBackGroundBorder()
    //   setTimeout(chart.setOption(option, true), 500)
    //   window.onresize = function() {
    //     chart.resize()
    //   }
    // },
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
      // const chart = this.myChart
      // chart.resize()
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
        default:
          break
      }
    }
  }
}
</script>

<style scoped>

</style>
