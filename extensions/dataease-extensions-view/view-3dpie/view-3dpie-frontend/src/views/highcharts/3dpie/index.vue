<template>
  <div style="display: flex;position:relative" class="chart-class">
    <view-track-bar
      ref="viewTrack"
      :track-menu="trackMenu"
      class="track-bar"
      :style="trackBarStyleTime"
      @trackClick="trackClick"
    />
    <div :id="chartId" style="width: 100%;height: 100%;overflow: hidden;" :style="{ borderRadius: borderRadius}" />

  </div>
</template>

<script>
  import * as highcharts from 'highcharts'
  import Highcharts3D from 'highcharts/highcharts-3d';
  Highcharts3D(highcharts);


  import {BASE_PIE, basePieOption, uuid} from '@/utils/3dpie'
  import ViewTrackBar from '@/components/views/ViewTrackBar'
  export default {
    name: 'ChartComponent',
    components: {
      ViewTrackBar
    },
    props: {
      obj: {
        type: Object,
        required: true
      }
    },
    data() {
      return {
        myChart: null,
        chartId: uuid(),
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
      },
      chart() {
        return this.obj.chart
      },
      filter() {
        return this.obj.filter || {}
      },
      trackMenu() {
        return this.obj.trackMenu || ['drill']
      },
      searchCount() {
        return this.obj.searchCount || 0
      },
      terminalType() {
        return this.obj.terminalType || 'pc'
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
    created() {
      if (!this.$highcharts) {
        this.$highcharts = highcharts
      }
    },
    mounted() {
      this.preDraw()
    },
    destroyed() {
      this.myChart.destroy()
    },
    methods: {
      executeAxios (url, type, data, callBack) {
        const param = {
          url: url,
          type: type,
          data: data,
          callBack: callBack
        }
        this.$emit('execute-axios', param)
        if (process.env.NODE_ENV === 'development') {
          execute(param).then(res => {
            if (param.callBack) {
              param.callBack(res)
            }
          }).catch(e => {
            if (param.error) {
              param.error(e)
            }
          })
        }
      },
      preDraw() {
        const that = this
        new Promise((resolve) => {
          resolve()
        }).then(() => {

          if (!this.myChart) {
            this.myChart = this.$highcharts.chart(this.chartId,JSON.parse(JSON.stringify(BASE_PIE)))
          }
          this.drawEcharts()

          /*this.myChart.off('click')
          this.myChart.on('click', function(param) {
            that.pointParam = param
            if (that.trackMenu.length < 2) { // 只有一个事件直接调用
              that.trackClick(that.trackMenu[0])
            } else { // 视图关联多个事件
              that.trackBarStyle.left = param.event.offsetX + 'px'
              that.trackBarStyle.top = (param.event.offsetY - 15) + 'px'
              that.$refs.viewTrack.trackButtonClick()
            }
          })*/
        })
      },
      drawEcharts() {
        const chart = this.chart
        let chart_option = {}

        if (this.myChart && this.searchCount > 0) {
          chart_option.animation = false
        }
        const base_json = JSON.parse(JSON.stringify(BASE_PIE))

        chart_option = basePieOption(base_json, chart, this.terminalType)
        this.myEcharts(chart_option)

      },






      myEcharts(option) {

        // 指定图表的配置项和数据
        const chart = this.myChart
        this.setBackGroundBorder()
        setTimeout(chart.update(option, true), 500)
        window.onresize = function() {
          this.myChart.reflow()
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
        chart.reflow()
      },

      trackClick(trackAction) {
        this.pointParam.viewId = this.chart.id
        const param = this.pointParam
        if (!param || !param.data || !param.data.dimensionList) {
          // 地图提示没有关联字段 其他没有维度信息的 直接返回
          this.$warning(this.$t('panel.no_drill_field'))
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
            this.$emit('plugin-call-back', {
              eventName: 'plugin-chart-click',
              eventParam: this.pointParam
            })

            break
          case 'linkage':
            // this.$store.commit('addViewTrackFilter', linkageParam)
            this.$emit('plugin-call-back', {
              eventName: 'plugin-add-view-track-filter',
              eventParam: linkageParam
            })
            break
          case 'jump':
            this.$emit('plugin-call-back', {
              eventName: 'plugin-jump-click',
              eventParam: jumpParam
            })
            break
          default:
            break
        }
      },

    }
  }

</script>

<style lang="scss" scoped>

  .chart-class {
    height: 100%;
    padding: 10px;
  }
  .track-bar >>> ul {
    width: 80px !important;
  }

</style>
