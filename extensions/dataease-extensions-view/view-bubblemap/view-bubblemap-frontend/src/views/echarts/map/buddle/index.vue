<template>
  <div style="display: flex;position:relative" class="chart-class">
    <view-track-bar ref="viewTrack" :track-menu="trackMenu" class="track-bar" :style="trackBarStyleTime"
      @trackClick="trackClick" />
    <div :id="chartId" style="width: 100%;height: 100%;overflow: hidden;" :style="{ borderRadius: borderRadius}" />
    <div class="map-zoom-box" v-if="showSuspension">
      <div style="margin-bottom: 0.5em;">
        <el-button
          :style="{'background': buttonTextColor ? 'none' : '', 'opacity': buttonTextColor ? '0.75': '', 'color': buttonTextColor, 'borderColor': buttonTextColor}"
          size="mini" icon="el-icon-plus" circle @click="roamMap(true)" />
      </div>

      <div style="margin-bottom: 0.5em;">
        <el-button
          :style="{'background': buttonTextColor ? 'none' : '', 'opacity': buttonTextColor ? '0.75': '', 'color': buttonTextColor, 'borderColor': buttonTextColor}"
          size="mini" icon="el-icon-refresh" circle @click="resetZoom()" />
      </div>

      <div>
        <el-button
          :style="{'background': buttonTextColor ? 'none' : '', 'opacity': buttonTextColor ? '0.75': '', 'color': buttonTextColor, 'borderColor': buttonTextColor}"
          size="mini" icon="el-icon-minus" circle @click="roamMap(false)" />
      </div>

    </div>
  </div>
</template>

<script>
  import {
    BASE_MAP,
    baseMapOption,
    uuid,
    reverseColor
  } from '@/utils/map'
  import { centerPointJson } from '@/utils/nationalCenterPoint'
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
      },
      themeStyle: {
        type: Object,
        required: false,
        default: null
      },
      canvasStyleData: {
        type: Object,
        required: false,
        default: function() {
          return {}
        }
      }
    },
    data() {
      return {
        myChart: {},
        chartId: uuid(),
        showTrackBar: true,
        trackBarStyle: {
          position: 'absolute',
          left: '0px',
          top: '0px'
        },
        pointParam: null,

        dynamicAreaCode: null,
        borderRadius: '0px',
        mapCenter: null,
        buttonTextColor: null,
        showSuspension: true
      }
    },

    computed: {
      trackBarStyleTime() {
        return this.trackBarStyle
      },
      active() {
        return this.obj.active
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
      active: {
        handler(newVal, oldVla) {
          this.scrollStatusChange(newVal)
        }
      },
      chart: {
        handler(newVal, oldVal) {
          if (newVal && oldVal && JSON.stringify(newVal) === JSON.stringify(oldVal)) {
            return
          }
          this.preDraw()
        },
        deep: true
      },
      resize() {
        this.drawEcharts()
      },
      'themeStyle.color'(value, old) {
        if (value !== old) {
          this.preDraw()
        }
      },
      'themeStyle.backgroundColorSelect'(value, old) {
        if (value !== old) {
          this.preDraw()
        }
      }
    },
    mounted() {
      this.preDraw()
    },
    destroyed() {
      this.myChart.dispose()
    },
    created() {
      this.loadThemeStyle()
    },
    methods: {
      scrollStatusChange() {
          const opt = this.myChart.getOption()
          this.adaptorOpt(opt)
          this.myChart.setOption(opt)
      },
      adaptorOpt(opt) {
        //地图
        if (opt.geo) {
          if (opt.geo instanceof Array) {
            opt.geo[0].roam = this.active
          } else {
            opt.geo.roam = this.active
          }
        }
      },
      executeAxios(url, type, data, callBack, hideMsg) {
        const param = {
          url: url,
          type: type,
          data: data,
          callBack: callBack,
          hideMsg: hideMsg
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
          this.myChart = this.$echarts.getInstanceByDom(document.getElementById(this.chartId))
          if (!this.myChart) {
            this.myChart = this.$echarts.init(document.getElementById(this.chartId))
          }
          this.drawEcharts()

          this.myChart.off('click')
          this.myChart.on('click', function (param) {
            that.pointParam = param
            if (that.trackMenu.length < 2) {
              that.trackClick(that.trackMenu[0])
            } else {
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

        if (this.myChart && this.searchCount > 0) {
          chart_option.animation = false
        }

        const customAttr = JSON.parse(chart.customAttr)
        if (customAttr.suspension) {
          this.showSuspension = !!customAttr.suspension.show
        }
        if (!customAttr.areaCode) {
          this.myChart.clear()
          return
        }
        const cCode = this.chart.DetailAreaCode || this.dynamicAreaCode || customAttr.areaCode

        const geoMap = !!localStorage.getItem('geoMap') ? JSON.parse(localStorage.getItem('geoMap')) : {}
        if (geoMap[cCode]) {
          this.initMapChart(cCode, geoMap[cCode], chart)
          return
        }
        const countryCode = cCode.substring(0, 3)
        const url = '/geo/full/' + countryCode + '/' + cCode + '_full.json'
        this.executeAxios(url, 'get', null, res => {
          if (res && Object.keys(res).length > 0) {
            geoMap[cCode] = res
            localStorage.setItem("geoMap", JSON.stringify(geoMap))
            this.initMapChart(cCode, res, chart)
          }

        })
      },
      loadThemeStyle() {
        let themeStyle = null
        if (this.themeStyle) {
          themeStyle = JSON.parse(JSON.stringify(this.themeStyle))
          if (themeStyle && themeStyle.backgroundColorSelect) {
            const panelColor = themeStyle.color
            if (panelColor !== '#FFFFFF') {
              const reverseValue = reverseColor(panelColor)
              this.buttonTextColor = reverseValue
            } else {
              this.buttonTextColor = null
            }
          } else if(this.canvasStyleData.openCommonStyle && this.canvasStyleData.panel.backgroundType === 'color') {
              const panelColor = this.canvasStyleData.panel.color
              if (panelColor !== '#FFFFFF') {
                const reverseValue = reverseColor(panelColor)
                this.buttonTextColor = reverseValue
              } else {
                this.buttonTextColor = null
              }
            }
          } else {
            this.buttonTextColor = null
          }
      },

      registerDynamicMap(areaCode) {
        this.dynamicAreaCode = areaCode
      },



      initMapChart(cCode, geoJson, chart) {
        this.$echarts.registerMap('BUDDLE_MAP', geoJson)
        const base_json = JSON.parse(JSON.stringify(BASE_MAP))
        let mapData = {}
        if (!geoJson || !geoJson.features || !geoJson.features.length === 0) {
          return
        }
        let hasCenter = false

        geoJson.features.map(function (item) {
          mapData[item.properties.name] = item.properties.centroid || item.properties.center || centerPointJson[item.properties.name]
          if (mapData[item.properties.name]) {
            hasCenter = true
          }
        })
        if (!hasCenter) {
          const msg = this.$t('chart.map_center_lost')
          this.$error(msg)
        }
        let themeStyle = null
        if (this.themeStyle) {
          themeStyle = JSON.parse(JSON.stringify(this.themeStyle))

          if (themeStyle && themeStyle.backgroundColorSelect) {
            const panelColor = themeStyle.color
            if (panelColor !== '#FFFFFF') {
              const reverseValue = reverseColor(panelColor)
              this.buttonTextColor = reverseValue
            } else {
              this.buttonTextColor = null
            }
          } else if(this.canvasStyleData.openCommonStyle && this.canvasStyleData.panel.backgroundType === 'color') {
            const panelColor = this.canvasStyleData.panel.color
            if (panelColor !== '#FFFFFF') {
              const reverseValue = reverseColor(panelColor)
              this.buttonTextColor = reverseValue
            } else {
              this.buttonTextColor = null
            }
          } else {
            this.buttonTextColor = null
          }
        }
        const chart_option = baseMapOption(base_json, chart, mapData, this.terminalType, this.buttonTextColor, cCode)
        this.myEcharts(chart_option)
        const opt = this.myChart.getOption()
        if (opt && opt.series) {
          const center = opt.series[0].center
          this.mapCenter = center
        }
      },
      myEcharts(option) {
        this.adaptorOpt(option)
        const chart = this.myChart
        this.setBackGroundBorder()
        setTimeout(chart.setOption(option, true), 500)
        window.onresize = function () {
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
        const chart = this.myChart
        chart.resize()
        this.reDrawMap()
      },
      reDrawMap() {
        const chart = this.chart
        this.preDraw()
      },
      trackClick(trackAction) {
        this.pointParam.viewId = this.chart.id
        const param = this.pointParam
        if (!param || !param.data || !param.data.dimensionList) {
          const zoom = this.myChart.getOption().geo[0].zoom
          if (zoom <= 1) {
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
      roamMap(flag) {
        let targetZoom = 1
        const len = this.myChart.getOption().geo.length
        const index = len - 1
        const zoom = this.myChart.getOption().geo[index].zoom
        if (flag) {
          targetZoom = zoom * 1.2
        } else {
          targetZoom = zoom / 1.2
        }
        const options = JSON.parse(JSON.stringify(this.myChart.getOption()))
        options.geo.forEach(item => {
          item.zoom = targetZoom
        })
        // options.geo[0].zoom = targetZoom
        this.myChart.setOption(options)
      },
      resetZoom() {
        const options = JSON.parse(JSON.stringify(this.myChart.getOption()))
        /* options.geo[0].zoom = 1
        options.geo[0].center = this.mapCenter */
        options.geo.forEach(item => {
          item.zoom = 1
          item.center = this.mapCenter
        })
        this.myChart.setOption(options)
      }
    }
  }

</script>

<style lang="scss" scoped>
  .map-zoom-box {
    position: absolute;
    z-index: 999;
    left: 2%;
    bottom: 30px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    text-align: center;
    padding: 2px;
    border-radius: 5px
  }

  .chart-class {
    height: 100%;
    padding: 10px;
  }

  .track-bar>>>ul {
    width: 80px !important;
  }

</style>
