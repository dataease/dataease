<template>
  <div
    ref="chartContainer"
    style="padding: 0;width: 100%;height: 100%;overflow: hidden;"
    :style="bg_class"
  >
    <view-track-bar
      ref="viewTrack"
      :track-menu="trackMenu"
      class="track-bar"
      :style="trackBarStyleTime"
      @trackClick="trackClick"
    />
    <span
      v-if="chart.type && antVRenderStatus"
      v-show="title_show"
      ref="title"
      :style="title_class"
      style="cursor: default;display: block;"
    >
      <div style="padding:6px 4px 0;margin: 0;">
        <input
          v-if="chartTitleEditer"
          ref="chartTitle"
          v-model.lazy="chartTitleUpdate"
          v-clickoutside="lostFocus"
          type="text"
          class="chart-input-title"
          @blur="changeEditStatus"
        >
        <p
          v-else
          style="overflow: hidden;white-space: pre;text-overflow: ellipsis;display: inline;min-width: 30px"
          @click.stop="handleTitleEditer"
        >{{ chart.title }}</p>
        <title-remark
          v-if="remarkCfg.show"
          style="text-shadow: none!important;"
          :remark-cfg="remarkCfg"
        />
      </div>
    </span>
    <div
      :id="chartId"
      style="width: 100%;overflow: hidden;"
      :style="{height:chartHeight}"
    />
  </div>
</template>

<script>
import { baseLiquid } from '@/views/chart/chart/liquid/liquid'
import { uuid } from 'vue-uuid'
import ViewTrackBar from '@/components/canvas/components/Editor/ViewTrackBar'
import { getRemark, hexColorToRGBA } from '@/views/chart/chart/util'
import { baseBarOptionAntV, hBaseBarOptionAntV } from '@/views/chart/chart/bar/bar_antv'
import { baseAreaOptionAntV, baseLineOptionAntV } from '@/views/chart/chart/line/line_antv'
import { basePieOptionAntV, basePieRoseOptionAntV } from '@/views/chart/chart/pie/pie_antv'
import { baseScatterOptionAntV } from '@/views/chart/chart/scatter/scatter_antv'
import { baseGaugeOptionAntV } from '@/views/chart/chart/gauge/gauge_antv'
import { baseFunnelOptionAntV } from '@/views/chart/chart/funnel/funnel_antv'
import { baseTreemapOptionAntV } from '@/views/chart/chart/treemap/treemap_antv'
import { baseRadarOptionAntV } from '@/views/chart/chart/radar/radar_antv'
import { baseWaterfallOptionAntV } from '@/views/chart/chart/waterfall/waterfall'
import { baseWordCloudOptionAntV } from '@/views/chart/chart/wordCloud/word_cloud'
import TitleRemark from '@/views/chart/view/TitleRemark'
import { DEFAULT_TITLE_STYLE } from '@/views/chart/chart/chart'
import { baseMixOptionAntV } from '@/views/chart/chart/mix/mix_antv'
import { compareItem } from '@/views/chart/chart/compare'

import clickoutside from 'element-ui/src/utils/clickoutside.js'
import bus from '@/utils/bus'
import {
  getChartDetails
} from '@/api/chart/chart'
import {
  viewEditSave
} from '@/api/chart/chart'
export default {
  name: 'ChartComponentG2',
  components: { TitleRemark, ViewTrackBar },
  directives: {
    clickoutside
  },
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
    },
    scale: {
      type: Number,
      required: false,
      default: 1
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
      chartTitleEditer: false,
      dynamicAreaCode: null,
      borderRadius: '0px',
      chartHeight: '100%',
      title_class: {
        margin: '0 0',
        width: '100%',
        fontSize: '18px',
        color: '#303133',
        textAlign: 'left',
        fontStyle: 'normal',
        fontWeight: 'normal',
        background: ''
      },
      title_show: true,
      antVRenderStatus: false,
      linkageActiveParam: null,
      linkageActiveHistory: false,
      remarkCfg: {
        show: false,
        content: ''
      }

    }
  },

  computed: {
    trackBarStyleTime() {
      return this.trackBarStyle
    },
    bg_class() {
      return {
        borderRadius: this.borderRadius
      }
    },
    panelInfo() {
      return this.$store.state.panel.panelInfo
    },
    mainActiveName() {
      return this.$store.state.panel.mainActiveName
    }
  },
  watch: {
    chart: {
      handler(newVal, oldVla) {
        this.initTitle()
        this.calcHeightDelay()
        new Promise((resolve) => { resolve() }).then(() => {
          this.drawView()
        })
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
    reDrawView() {
      this.linkageActiveHistory = false
      this.myChart.render()
    },
    linkageActivePre() {
      if (this.linkageActiveHistory) {
        this.reDrawView()
      }
      this.$nextTick(() => {
        this.linkageActive()
      })
    },
    linkageActive() {
      this.linkageActiveHistory = true
      this.myChart.setState('active', (param) => {
        if (Array.isArray(param)) {
          return false
        } else {
          if (this.checkSelected(param)) {
            return true
          }
        }
      })
      this.myChart.setState('inactive', (param) => {
        if (Array.isArray(param)) {
          return false
        } else {
          if (!this.checkSelected(param)) {
            return true
          }
        }
      })
    },
    checkSelected(param) {
      return (this.linkageActiveParam.name.indexOf(param.name) > -1) &&
        (this.linkageActiveParam.category === param.category)
    },
    preDraw() {
      this.initTitle()
      this.calcHeightDelay()
      new Promise((resolve) => { resolve() }).then(() => {
        this.drawView()
      })
      const that = this
      window.onresize = function() {
        that.calcHeightDelay()
      }
    },
    drawView() {
      const chart = this.chart
      // type
      // if (chart.data) {
      this.antVRenderStatus = true
      if (!chart.data || (!chart.data.data && !chart.data.series)) {
        chart.data = {
          data: [{}],
          series: [
            {
              data: [0]
            }
          ]
        }
      }
      if (chart.type === 'bar') {
        this.myChart = baseBarOptionAntV(this.myChart, this.chartId, chart, this.antVAction, true, false)
      } else if (chart.type === 'bar-group') {
        this.myChart = baseBarOptionAntV(this.myChart, this.chartId, chart, this.antVAction, true, false)
      } else if (chart.type === 'bar-stack' || chart.type === 'percentage-bar-stack') {
        this.myChart = baseBarOptionAntV(this.myChart, this.chartId, chart, this.antVAction, false, true)
      } else if (chart.type === 'bar-group-stack') {
        this.myChart = baseBarOptionAntV(this.myChart, this.chartId, chart, this.antVAction, true, true)
      } else if (chart.type === 'bar-horizontal') {
        this.myChart = hBaseBarOptionAntV(this.myChart, this.chartId, chart, this.antVAction, true, false)
      } else if (chart.type === 'bar-stack-horizontal') {
        this.myChart = hBaseBarOptionAntV(this.myChart, this.chartId, chart, this.antVAction, false, true)
      } else if (chart.type === 'line') {
        this.myChart = baseLineOptionAntV(this.myChart, this.chartId, chart, this.antVAction)
      } else if (chart.type === 'area') {
        this.myChart = baseAreaOptionAntV(this.myChart, this.chartId, chart, this.antVAction, false)
      } else if (chart.type === 'line-stack') {
        this.myChart = baseAreaOptionAntV(this.myChart, this.chartId, chart, this.antVAction, true)
      } else if (chart.type === 'scatter') {
        this.myChart = baseScatterOptionAntV(this.myChart, this.chartId, chart, this.antVAction)
      } else if (chart.type === 'radar') {
        this.myChart = baseRadarOptionAntV(this.myChart, this.chartId, chart, this.antVAction)
      } else if (chart.type === 'gauge') {
        this.myChart = baseGaugeOptionAntV(this.myChart, this.chartId, chart, this.antVAction, this.scale)
      } else if (chart.type === 'pie' || chart.type === 'pie-donut') {
        this.myChart = basePieOptionAntV(this.myChart, this.chartId, chart, this.antVAction)
      } else if (chart.type === 'pie-rose' || chart.type === 'pie-donut-rose') {
        this.myChart = basePieRoseOptionAntV(this.myChart, this.chartId, chart, this.antVAction)
      } else if (chart.type === 'funnel') {
        this.myChart = baseFunnelOptionAntV(this.myChart, this.chartId, chart, this.antVAction)
      } else if (chart.type === 'treemap') {
        this.myChart = baseTreemapOptionAntV(this.myChart, this.chartId, chart, this.antVAction)
      } else if (chart.type === 'liquid') {
        this.myChart = baseLiquid(this.myChart, this.chartId, chart)
      } else if (chart.type === 'waterfall') {
        this.myChart = baseWaterfallOptionAntV(this.myChart, this.chartId, chart, this.antVAction)
      } else if (chart.type === 'word-cloud') {
        this.myChart = baseWordCloudOptionAntV(this.myChart, this.chartId, chart, this.antVAction)
      } else if (chart.type === 'chart-mix') {
        this.myChart = baseMixOptionAntV(this.myChart, this.chartId, chart, this.antVAction)
      } else {
        if (this.myChart) {
          this.antVRenderStatus = false
          this.myChart.destroy()
        }
      }

      if (this.myChart && chart.type !== 'liquid' && this.searchCount > 0) {
        this.myChart.options.animation = false
      }

      if (this.antVRenderStatus) {
        this.myChart.render()
        if (this.linkageActiveHistory) {
          this.linkageActive()
        }
      }
      this.setBackGroundBorder()
    },

    antVAction(param) {
      if (this.chart.type === 'treemap') {
        this.pointParam = param.data.data
      } else {
        this.pointParam = param.data
      }
      this.linkageActiveParam = {
        category: this.pointParam.data.category ? this.pointParam.data.category : 'NO_DATA',
        name: this.pointParam.data.name ? this.pointParam.data.name : 'NO_DATA'
      }
      if (this.trackMenu.length < 2) { // 只有一个事件直接调用
        this.trackClick(this.trackMenu[0])
      } else { // 视图关联多个事件
        this.trackBarStyle.left = param.x + 'px'
        this.trackBarStyle.top = (param.y + 10) + 'px'
        this.$refs.viewTrack.trackButtonClick()
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
      this.calcHeightDelay()
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

      switch (trackAction) {
        case 'drill':
          this.$emit('onChartClick', this.pointParam)
          break
        case 'linkage':
          this.linkageActivePre()
          this.$store.commit('addViewTrackFilter', linkageParam)
          break
        case 'jump':
          this.$emit('onJumpClick', jumpParam)
          break
        default:
          break
      }
    },

    initTitle() {
      if (this.chart.customStyle) {
        const customStyle = JSON.parse(this.chart.customStyle)
        if (customStyle.text) {
          this.title_show = customStyle.text.show
          this.title_class.fontSize = customStyle.text.fontSize + 'px'
          this.title_class.color = customStyle.text.color
          this.title_class.textAlign = customStyle.text.hPosition
          this.title_class.fontStyle = customStyle.text.isItalic ? 'italic' : 'normal'
          this.title_class.fontWeight = customStyle.text.isBolder ? 'bold' : 'normal'

          this.title_class.fontFamily = customStyle.text.fontFamily ? customStyle.text.fontFamily : DEFAULT_TITLE_STYLE.fontFamily
          this.title_class.letterSpacing = (customStyle.text.letterSpace ? customStyle.text.letterSpace : DEFAULT_TITLE_STYLE.letterSpace) + 'px'
          this.title_class.textShadow = customStyle.text.fontShadow ? '2px 2px 4px' : 'none'
        }
        if (customStyle.background) {
          this.title_class.background = hexColorToRGBA(customStyle.background.color, customStyle.background.alpha)
          this.borderRadius = (customStyle.background.borderRadius || 0) + 'px'
        }
      }
      this.initRemark()
    },

    calcHeightRightNow() {
      this.$nextTick(() => {
        if (this.$refs.chartContainer) {
          const currentHeight = this.$refs.chartContainer.offsetHeight
          if (this.$refs.title) {
            const titleHeight = this.$refs.title.offsetHeight
            this.chartHeight = (currentHeight - titleHeight) + 'px'
          }
        }
      })
    },
    calcHeightDelay() {
      setTimeout(() => {
        this.calcHeightRightNow()
      }, 100)
    },
    lostFocus() {
      this.chartTitleEditer = false
    },
    getChartDetail() {
      getChartDetails(this.chart.id, this.panelInfo.id, { queryFrom: 'panel_edit' }).then(response => {
        const chartView = JSON.parse(JSON.stringify(response.data))
        chartView.viewFields = chartView.viewFields ? JSON.parse(chartView.viewFields) : []
        chartView.xaxis = chartView.xaxis ? JSON.parse(chartView.xaxis) : []
        chartView.xaxisExt = chartView.xaxisExt ? JSON.parse(chartView.xaxisExt) : []
        chartView.yaxis = chartView.yaxis ? JSON.parse(chartView.yaxis) : []
        chartView.yaxisExt = chartView.yaxisExt ? JSON.parse(chartView.yaxisExt) : []
        chartView.extStack = chartView.extStack ? JSON.parse(chartView.extStack) : []
        chartView.drillFields = chartView.drillFields ? JSON.parse(chartView.drillFields) : []
        chartView.extBubble = chartView.extBubble ? JSON.parse(chartView.extBubble) : []
        chartView.customAttr = chartView.customAttr ? JSON.parse(chartView.customAttr) : {}
        chartView.customStyle = chartView.customStyle ? JSON.parse(chartView.customStyle) : {}
        chartView.customFilter = chartView.customFilter ? JSON.parse(chartView.customFilter) : {}
        chartView.senior = chartView.senior ? JSON.parse(chartView.senior) : {}
        const viewSave = this.buildParam(chartView, true, 'chart', false, false)
        if (!viewSave) return
        viewEditSave(this.panelInfo.id, viewSave).then(() => {
          bus.$emit('aside-set-title', this.chart.title)
        })
        this.$store.commit('recordViewEdit', { viewId: this.chart.id, hasEdit: true })
      })
    },
    handleTitleEditer() {
      if (this.mainActiveName !== 'PanelEdit') return
      this.chartTitleEditer = true
      this.chartTitleUpdate = this.chart.title
      this.$nextTick(() => {
        this.$refs.chartTitle.focus()
      })
    },
    buildParam(chartView, getData, trigger, needRefreshGroup = false, switchType = false, switchRender = false) {
      if (!chartView.resultCount ||
        chartView.resultCount === '' ||
        isNaN(Number(chartView.resultCount)) ||
        String(chartView.resultCount).includes('.') ||
        parseInt(chartView.resultCount) < 1) {
        chartView.resultCount = '1000'
      }
      const view = JSON.parse(JSON.stringify(chartView))
      view.id = chartView.id
      view.sceneId = chartView.sceneId
      view.name = this.chartTitleUpdate
      view.title = this.chartTitleUpdate
      view.tableId = chartView.tableId
      if (view.type === 'map' && view.xaxis.length > 1) {
        view.xaxis = [view.xaxis[0]]
      }
      view.xaxis.forEach(function(ele) {
        if (!ele.dateStyle || ele.dateStyle === '') {
          ele.dateStyle = 'y_M_d'
        }
        if (!ele.datePattern || ele.datePattern === '') {
          ele.datePattern = 'date_sub'
        }
        if (!ele.sort || ele.sort === '') {
          ele.sort = 'none'
        }
        if (!ele.filter) {
          ele.filter = []
        }
      })
      if (view.type === 'table-pivot' || view.type === 'bar-group') {
        view.xaxisExt.forEach(function(ele) {
          if (!ele.dateStyle || ele.dateStyle === '') {
            ele.dateStyle = 'y_M_d'
          }
          if (!ele.datePattern || ele.datePattern === '') {
            ele.datePattern = 'date_sub'
          }
          if (!ele.sort || ele.sort === '') {
            ele.sort = 'none'
          }
          if (!ele.filter) {
            ele.filter = []
          }
        })
      }
      if (view.type === 'map' && view.yaxis.length > 1) {
        view.yaxis = [view.yaxis[0]]
      }
      view.yaxis.forEach(function(ele) {
        if (!ele.chartType) {
          ele.chartType = 'bar'
        }
        if (ele.chartId) {
          ele.summary = ''
        } else {
          if (!ele.summary || ele.summary === '') {
            if (ele.id === 'count' || ele.deType === 0 || ele.deType === 1) {
              ele.summary = 'count'
            } else {
              ele.summary = 'sum'
            }
          }
        }

        if (!ele.sort || ele.sort === '') {
          ele.sort = 'none'
        }
        if (!ele.filter) {
          ele.filter = []
        }
        if (!ele.compareCalc) {
          ele.compareCalc = compareItem
        }
      })
      if (view.type === 'chart-mix') {
        view.yaxisExt.forEach(function(ele) {
          if (!ele.chartType) {
            ele.chartType = 'bar'
          }
          if (ele.chartId) {
            ele.summary = ''
          } else {
            if (!ele.summary || ele.summary === '') {
              if (ele.id === 'count' || ele.deType === 0 || ele.deType === 1) {
                ele.summary = 'count'
              } else {
                ele.summary = 'sum'
              }
            }
          }

          if (!ele.sort || ele.sort === '') {
            ele.sort = 'none'
          }
          if (!ele.filter) {
            ele.filter = []
          }
          if (!ele.compareCalc) {
            ele.compareCalc = compareItem
          }
        })
      }
      view.extStack.forEach(function(ele) {
        if (!ele.dateStyle || ele.dateStyle === '') {
          ele.dateStyle = 'y_M_d'
        }
        if (!ele.datePattern || ele.datePattern === '') {
          ele.datePattern = 'date_sub'
        }
        if (!ele.sort || ele.sort === '') {
          ele.sort = 'none'
        }
      })
      view.extBubble.forEach(function(ele) {
        if (!ele.summary || ele.summary === '') {
          if (ele.id === 'count' || ele.deType === 0 || ele.deType === 1) {
            ele.summary = 'count'
          } else {
            ele.summary = 'sum'
          }
        }
      })
      if (view.type === 'label') {
        if (view.xaxis.length > 1) {
          view.xaxis.splice(1, view.xaxis.length)
        }
      }
      if (view.type.startsWith('pie') ||
        view.type.startsWith('funnel') ||
        view.type.startsWith('text') ||
        view.type.startsWith('gauge') ||
        view.type === 'treemap' ||
        view.type === 'liquid' ||
        view.type === 'word-cloud' ||
        view.type === 'waterfall' ||
        view.type.includes('group')) {
        if (view.yaxis.length > 1) {
          view.yaxis.splice(1, view.yaxis.length)
        }
      }
      if (view.type === 'line-stack' && trigger === 'chart') {
        view.customAttr.size.lineArea = true
      }
      if (view.type === 'line' && trigger === 'chart') {
        view.customAttr.size.lineArea = false
      }
      if (view.type === 'treemap' && trigger === 'chart') {
        view.customAttr.label.show = true
      }
      if (view.type === 'liquid' ||
        (view.type.includes('table') && view.render === 'echarts') ||
        view.type.includes('text') ||
        view.type.includes('gauge') ||
        view.type === 'table-pivot') {
        view.drillFields = []
      }
      view.customFilter.forEach(function(ele) {
        if (ele && !ele.filter) {
          ele.filter = []
        }
      })
      view.xaxis = JSON.stringify(view.xaxis)
      view.viewFields = JSON.stringify(view.viewFields)
      view.xaxisExt = JSON.stringify(view.xaxisExt)
      view.yaxis = JSON.stringify(view.yaxis)
      view.yaxisExt = JSON.stringify(view.yaxisExt)
      view.customAttr = JSON.stringify(view.customAttr)
      view.customStyle = JSON.stringify(view.customStyle)
      view.customFilter = JSON.stringify(view.customFilter)
      view.extStack = JSON.stringify(view.extStack)
      view.drillFields = JSON.stringify(view.drillFields)
      view.extBubble = JSON.stringify(view.extBubble)
      view.senior = JSON.stringify(view.senior)
      delete view.data
      return view
    },
    changeEditStatus() {
      this.lostFocus()
      if (this.chartTitleUpdate.length > 50) {
        this.$error(this.$t('chart.title_limit'))
        return
      }
      if (this.chartTitleUpdate === this.chart.title) return
      this.chart.title = this.chartTitleUpdate
      this.getChartDetail()
    },
    initRemark() {
      this.remarkCfg = getRemark(this.chart)
    }
  }
}
</script>

<style scoped lang="scss">

.chart-input-title{
    word-break: break-word;
    font: 12px / 1.231 -apple-system, BlinkMacSystemFont, "Helvetica Neue", Arial, "Microsoft YaHei", "PingFang SC", sans-serif, "Segoe UI Symbol";
    overflow: visible;
    margin: 0;
    padding: 0;
    font-weight: 400;
    font-family: inherit;
    border-radius: 2px;
    color: #182b50;
    font-size: 12px;
    line-height: 26px;
    padding-left: 10px;
    padding-right: 10px;
    background: transparent;
    outline: none;
    border-width: 0px 0px 1px;
    border-image: initial;
    border-bottom: 1px solid rgb(200, 203, 204);
    z-index: 2;
    height: 21px;
    min-width: 100px;
}
</style>
