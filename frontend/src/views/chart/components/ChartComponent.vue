<template>
  <div style="display: flex;position:relative">
    <view-track-bar
      ref="viewTrack"
      :track-menu="trackMenu"
      class="track-bar"
      :style="trackBarStyleTime"
      @trackClick="trackClick"
    />
    <div
      :id="chartId"
      style="width: 100%;height: 100%;overflow: hidden;"
      :style="{ borderRadius: borderRadius}"
    />

    <map-controller
      v-if="chart.type === 'map' && showSuspension"
      :chart="chart"
      :button-text-color="buttonTextColor"
      @roam-map="roamMap"
      @reset-zoom="resetZoom"
    />
    <div
      :class="loading ? 'symbol-map-loading' : 'symbol-map-loaded'"
    />
  </div>
</template>

<script>
import {
  BASE_BAR,
  BASE_FUNNEL,
  BASE_GAUGE,
  BASE_LINE,
  BASE_MAP,
  BASE_MIX,
  BASE_PIE,
  BASE_RADAR,
  BASE_SCATTER,
  BASE_TREEMAP,
  HORIZONTAL_BAR
} from '../chart/chart'
import { baseBarOption, horizontalBarOption, horizontalStackBarOption, stackBarOption } from '../chart/bar/bar'
import { baseLineOption, stackLineOption } from '../chart/line/line'
import { basePieOption, rosePieOption } from '../chart/pie/pie'
import { baseMapOption } from '../chart/map/map'
import { baseFunnelOption } from '../chart/funnel/funnel'
import { baseRadarOption } from '../chart/radar/radar'
import { baseGaugeOption } from '../chart/gauge/gauge'
import { baseScatterOption } from '../chart/scatter/scatter'
import { baseTreemapOption } from '../chart/treemap/treemap'
import { baseMixOption } from '@/views/chart/chart/mix/mix'
import { uuid } from 'vue-uuid'
import { geoJson } from '@/api/map/map'
import ViewTrackBar from '@/components/canvas/components/editor/ViewTrackBar'
import { reverseColor } from '../chart/common/common'
import MapController from './map/MapController.vue'
import { mapState } from 'vuex'
import bus from '@/utils/bus'

export default {
  name: 'ChartComponent',
  components: {
    ViewTrackBar,
    MapController
  },
  props: {
    active: {
      type: Boolean,
      required: false,
      default: false
    },
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
    },
    terminalType: {
      type: String,
      default: 'pc'
    },
    scale: {
      type: Number,
      required: false,
      default: 1
    },
    themeStyle: {
      type: Object,
      required: false,
      default: null
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
      borderRadius: '0px',
      mapCenter: null,
      linkageActiveParam: null,
      buttonTextColor: null,
      loading: true,
      showSuspension: true,
      currentSeriesId: null,
      haveScrollType: [
        'treemap',
        'map',
        'chart-mix',
        'bar',
        'bar-stack',
        'bar-horizontal',
        'bar-stack-horizontal',
        'line',
        'line-stack',
        'scatter'
      ]
    }
  },

  computed: {
    trackBarStyleTime() {
      return this.trackBarStyle
    },
    ...mapState([
      'canvasStyleData'
    ])
  },
  watch: {
    active: {
      handler(newVal, oldVla) {
        this.scrollStatusChange(newVal)
      }
    },
    currentSeriesId(value, old) {
      if (value !== old) {
        this.preDraw()
      }
    },
    chart: {
      handler(newVal, oldVla) {
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
    bus.$on('change-series-id', this.changeSeriesId)
    this.preDraw()
  },
  beforeDestroy() {
    bus.$off('change-series-id', this.changeSeriesId)
    window.removeEventListener('resize', this.myChart.resize)
    this.myChart.dispose()
    this.myChart = null
  },
  created() {
    this.loadThemeStyle()
  },
  methods: {
    scrollStatusChange() {
      if (this.haveScrollType.includes(this.chart.type)) {
        const opt = this.myChart.getOption()
        this.adaptorOpt(opt)
        if (this.chart.type === 'treemap') {
          this.myChart.dispose()
          this.myChart = null
          this.preDraw()
        } else {
          this.myChart.setOption(opt)
        }
      }
    },
    adaptorOpt(opt) {
      const disabledStatus = !this.active
      if (opt.dataZoom) {
        opt.dataZoom.forEach(function(s) {
          if (s.type === 'inside') {
            s.disabled = disabledStatus
          }
        })
      }
      if (opt.geo) {
        if (opt.geo instanceof Array) {
          opt.geo[0].roam = this.active
        } else {
          opt.geo.roam = this.active
        }
      }
      if (this.chart.type === 'treemap' && opt.series) {
        opt.series[0].roam = this.active
      }
    },
    changeSeriesId(param) {
      const { id, seriesId } = param
      if (id !== this.chart.id) {
        return
      }
      this.currentSeriesId = seriesId
    },
    reDrawView() {
      if (this.linkageActiveParam) {
        this.myChart.dispatchAction({
          type: 'unselect',
          seriesIndex: this.linkageActiveParam.seriesIndex,
          name: this.linkageActiveParam.name
        })
        this.myChart.dispatchAction({
          type: 'downplay',
          seriesIndex: this.linkageActiveParam.seriesIndex,
          name: this.linkageActiveParam.name
        })
      }
      this.linkageActiveParam = null
    },
    linkageActive() {
      if (this.linkageActiveParam) {
        this.myChart.dispatchAction({
          type: 'select',
          seriesIndex: this.linkageActiveParam.seriesIndex,
          name: this.linkageActiveParam.name
        })
        this.myChart.dispatchAction({
          type: 'highlight',
          seriesIndex: this.linkageActiveParam.seriesIndex,
          name: this.linkageActiveParam.name
        })
      }
    },
    preDraw() {
      this.loading = true
      // 基于准备好的dom，初始化echarts实例
      // 渲染echart等待dom加载完毕,渲染之前先尝试销毁具有相同id的echart 放置多次切换仪表板有重复id情况
      const that = this
      new Promise((resolve) => {
        resolve()
      }).then(() => {
        //	此dom为echarts图标展示dom
        this.myChart = this.$echarts.getInstanceByDom(document.getElementById(this.chartId))
        if (!this.myChart) {
          this.myChart = this.$echarts.init(document.getElementById(this.chartId))
        }
        this.drawEcharts()

        this.myChart.off('click')
        this.myChart.on('click', function(param) {
          that.pointParam = param
          if (that.linkageActiveParam) {
            that.reDrawView()
          }
          if (that.trackMenu.length < 2) { // 只有一个事件直接调用
            that.trackClick(that.trackMenu[0])
          } else { // 视图关联多个事件
            that.trackBarStyle.left = param.event.offsetX + 'px'
            that.trackBarStyle.top = (param.event.offsetY - 15) + 'px'
            that.$refs.viewTrack.trackButtonClick()
          }
        })
        this.myChart.off('finished')
        this.myChart.on('finished', () => {
          this.loading = false
        })
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
        } else if (this.canvasStyleData.openCommonStyle && this.canvasStyleData.panel.backgroundType === 'color') {
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
    },
    drawEcharts(activeParam) {
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
      } else if (chart.type === 'pie' || chart.type === 'pie-donut') {
        chart_option = basePieOption(JSON.parse(JSON.stringify(BASE_PIE)), chart)
      } else if (chart.type === 'pie-rose' || chart.type === 'pie-donut-rose') {
        chart_option = rosePieOption(JSON.parse(JSON.stringify(BASE_PIE)), chart)
      } else if (chart.type === 'funnel') {
        chart_option = baseFunnelOption(JSON.parse(JSON.stringify(BASE_FUNNEL)), chart)
      } else if (chart.type === 'radar') {
        chart_option = baseRadarOption(JSON.parse(JSON.stringify(BASE_RADAR)), chart)
      } else if (chart.type === 'gauge') {
        chart_option = baseGaugeOption(JSON.parse(JSON.stringify(BASE_GAUGE)), chart, this.terminalType === 'pc' ? this.scale : '0.7')
      } else if (chart.type === 'scatter') {
        chart_option = baseScatterOption(JSON.parse(JSON.stringify(BASE_SCATTER)), chart, this.terminalType)
      } else if (chart.type === 'treemap') {
        chart_option = baseTreemapOption(JSON.parse(JSON.stringify(BASE_TREEMAP)), chart)
      } else if (chart.type === 'chart-mix') {
        chart_option = baseMixOption(JSON.parse(JSON.stringify(BASE_MIX)), chart)
      }
      if (this.myChart && this.searchCount > 0) {
        chart_option.animation = false
      }
      if (chart.type === 'map') {
        const customAttr = JSON.parse(chart.customAttr)
        if (customAttr.suspension) {
          this.showSuspension = customAttr.suspension.show
        }
        if (!customAttr.areaCode) {
          this.myChart.clear()
          return
        }
        const cCode = this.chart.DetailAreaCode || this.dynamicAreaCode || customAttr.areaCode
        if (this.$store.getters.geoMap[cCode]) {
          const json = this.$store.getters.geoMap[cCode]
          this.initMapChart(json, chart, cCode)
          return
        }

        geoJson(cCode).then(res => {
          this.$store.dispatch('map/setGeo', {
            key: cCode,
            value: res
          }).then(() => {
            this.initMapChart(res, chart, cCode)
          })
        })
        return
      }
      if (chart_option.legend) {
        if (this.canvasStyleData.panel.themeColor === 'dark') {
          chart_option.legend['pageIconColor'] = '#ffffff'
          chart_option.legend['pageIconInactiveColor'] = '#8c8c8c'
        } else {
          chart_option.legend['pageIconColor'] = '#000000'
          chart_option.legend['pageIconInactiveColor'] = '#8c8c8c'
        }
      }
      if (chart_option.tooltip) {
        chart_option.tooltip.appendToBody = true
      }
      this.myEcharts(chart_option)
      this.$nextTick(() => (this.linkageActive()))
    },
    registerDynamicMap(areaCode) {
      this.dynamicAreaCode = areaCode
    },
    formatGeoJson(geoGson) {
      geoGson.features.forEach(feature => {
        Object.keys(feature.properties).forEach(property => {
          feature.properties[property.toLocaleLowerCase()] = feature.properties[property]
        })
      })
    },
    initMapChart(geoJson, chart, curAreaCode) {
      this.formatGeoJson(geoJson)
      const mapId = 'MAP' + this.chartId
      this.$echarts.registerMap(mapId, geoJson)
      const base_json = JSON.parse(JSON.stringify(BASE_MAP))
      base_json.geo.map = mapId
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
        } else if (this.canvasStyleData.openCommonStyle && this.canvasStyleData.panel.backgroundType === 'color') {
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
      const chart_option = baseMapOption(base_json, geoJson, chart, this.buttonTextColor, curAreaCode, this.currentSeriesId)
      this.myEcharts(chart_option)
      const opt = this.myChart.getOption()
      if (opt && opt.series) {
        const center = opt.series[0].center
        this.mapCenter = center
      }
    },
    myEcharts(option) {
      this.adaptorOpt(option)
      // 指定图表的配置项和数据
      const chart = this.myChart
      this.setBackGroundBorder()
      setTimeout(chart.setOption(option, true), 500)
      window.removeEventListener('resize', chart.resize)
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
      const quotaList = this.pointParam.data.quotaList
      quotaList[0]['value'] = this.pointParam.data.value
      const linkageParam = {
        option: 'linkage',
        name: this.pointParam.data.name,
        viewId: this.chart.id,
        dimensionList: this.pointParam.data.dimensionList,
        quotaList: quotaList
      }
      const jumpParam = {
        option: 'jump',
        name: this.pointParam.data.name,
        viewId: this.chart.id,
        dimensionList: this.pointParam.data.dimensionList,
        quotaList: quotaList
      }
      jumpParam.quotaList[0]['value'] = this.pointParam.data.value
      switch (trackAction) {
        case 'drill':
          this.$emit('onChartClick', this.pointParam)
          break
        case 'linkage':
          this.linkageActiveParam = {
            seriesIndex: param.seriesIndex,
            name: param.name
          }
          this.linkageActive()
          this.$store.commit('addViewTrackFilter', linkageParam)
          break
        case 'jump':
          this.$emit('onJumpClick', jumpParam)
          break
        default:
          break
      }
    },
    roamMap(flag) {
      let targetZoom = 1
      const zoom = this.myChart.getOption().geo[0].zoom
      if (flag) {
        targetZoom = zoom * 1.2
      } else {
        targetZoom = zoom / 1.2
      }
      const options = JSON.parse(JSON.stringify(this.myChart.getOption()))
      options.geo[0].zoom = targetZoom
      this.myChart.setOption(options)
    },
    resetZoom() {
      const options = JSON.parse(JSON.stringify(this.myChart.getOption()))
      options.geo[0].zoom = 1
      options.geo[0].center = this.mapCenter
      this.myChart.setOption(options)
    }
  }
}

</script>

<style scoped>
.map-zoom-box {
  position: absolute;
  z-index: 0;
  left: 2%;
  bottom: 30px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  text-align: center;
  padding: 2px;
  border-radius: 5px
}

</style>
