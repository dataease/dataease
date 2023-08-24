<template>
  <div
    v-if="chartTitleEditer"
    v-clickoutside="lostFocus"
    class="ipnut-wrap"
  >
    <input
      ref="chartTitle"
      v-model="chartTitleUpdate"
      type="text"
      :style="inputStyle"
      class="chart-input-title"
      @blur="changeEditStatus"
    >
    <i
      v-if="showClose"
      class="el-icon-circle-close"
      @mousedown.prevent
      @click="chartTitleUpdate = ''"
    />
  </div>
  <p
    v-else
    style="
      overflow: hidden;
      white-space: pre;
      text-overflow: ellipsis;
      display: inline;
      min-width: 30px;
    "
    @click.stop="handleTitleEditer"
  >{{ chart.title }}</p>
</template>

<script>
import clickoutside from 'element-ui/src/utils/clickoutside.js'
import { mapState } from 'vuex'
import { checkViewTitle } from '@/components/canvas/utils/utils'
import { getChartDetails, viewEditSave } from '@/api/chart/chart'
import bus from '@/utils/bus'
import { compareItem } from '@/views/chart/chart/compare'

export default {
  directives: {
    clickoutside
  },
  props: {
    chartInfo: {
      type: Object,
      default: () => {}
    },
    titleClass: {
      type: Object,
      default: () => {}
    }
  },
  data() {
    return {
      chartTitleEditer: false,
      chartTitleUpdate: '',
      chart: {},
      showClose: false
    }
  },
  computed: {
    mainActiveName() {
      return this.$store.state.panel.mainActiveName
    },
    ...mapState(['mobileLayoutStatus', 'previewVisible']),
    panelInfo() {
      return this.$store.state.panel.panelInfo
    },
    inputStyle() {
      const { fontSize, color, fontWeight, fontStyle, fontFamily } = this.titleClass
      return { fontSize, color, fontWeight, fontStyle, fontFamily, height: fontSize }
    }
  },
  watch: {
    chartInfo: {
      handler(val) {
        const { id, title } = this.chartInfo
        this.chart = { id, title }
      },
      immediate: true,
      deep: true
    },
    chartTitleUpdate(val) {
      this.showClose = !!val
    }
  },
  methods: {
    changeEditStatus() {
      this.lostFocus()
      if (this.chartTitleUpdate.length > 50) {
        this.$error(this.$t('chart.title_limit'))
        return
      }
      if (this.chartTitleUpdate.length < 1) {
        this.$error(this.$t('chart.title_cannot_empty'))
        this.chartTitleUpdate = this.chart.title
        return
      }
      if (checkViewTitle('update', this.chart.id, this.chartTitleUpdate)) {
        this.$error(this.$t('chart.title_repeat'))
        this.chartTitleUpdate = this.chart.title
        return
      }
      if (this.chartTitleUpdate === this.chart.title) return
      this.chart.title = this.chartTitleUpdate
      this.getChartDetail()
    },
    buildParam(
      chartView,
      getData,
      trigger,
      needRefreshGroup = false,
      switchType = false,
      switchRender = false
    ) {
      if (
        !chartView.resultCount ||
        chartView.resultCount === '' ||
        isNaN(Number(chartView.resultCount)) ||
        String(chartView.resultCount).includes('.') ||
        parseInt(chartView.resultCount) < 1
      ) {
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
      if (
        view.type.startsWith('pie') ||
        view.type.startsWith('funnel') ||
        view.type.startsWith('text') ||
        view.type.startsWith('gauge') ||
        view.type === 'treemap' ||
        view.type === 'liquid' ||
        view.type === 'word-cloud' ||
        view.type === 'waterfall' ||
        view.type.includes('group')
      ) {
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
      if (
        view.type === 'liquid' ||
        (view.type.includes('table') && view.render === 'echarts') ||
        view.type.includes('text') ||
        view.type.includes('gauge') ||
        view.type === 'table-pivot'
      ) {
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
    lostFocus() {
      this.chartTitleEditer = false
      this.showClose = false
    },
    handleTitleEditer() {
      if (
        this.mainActiveName !== 'PanelEdit' ||
        this.mobileLayoutStatus ||
        this.previewVisible
      ) {
        return
      }
      this.showClose = true
      this.chartTitleEditer = true
      this.chartTitleUpdate = this.chart.title
      this.$nextTick(() => {
        this.$refs.chartTitle.focus()
      })
    },
    getChartDetail() {
      getChartDetails(this.chart.id, this.panelInfo.id, {
        queryFrom: 'panel_edit'
      }).then((response) => {
        const chartView = JSON.parse(JSON.stringify(response.data))
        chartView.viewFields = chartView.viewFields
          ? JSON.parse(chartView.viewFields)
          : []
        chartView.xaxis = chartView.xaxis ? JSON.parse(chartView.xaxis) : []
        chartView.xaxisExt = chartView.xaxisExt
          ? JSON.parse(chartView.xaxisExt)
          : []
        chartView.yaxis = chartView.yaxis ? JSON.parse(chartView.yaxis) : []
        chartView.yaxisExt = chartView.yaxisExt
          ? JSON.parse(chartView.yaxisExt)
          : []
        chartView.extStack = chartView.extStack
          ? JSON.parse(chartView.extStack)
          : []
        chartView.drillFields = chartView.drillFields
          ? JSON.parse(chartView.drillFields)
          : []
        chartView.extBubble = chartView.extBubble
          ? JSON.parse(chartView.extBubble)
          : []
        chartView.customAttr = chartView.customAttr
          ? JSON.parse(chartView.customAttr)
          : {}
        chartView.customStyle = chartView.customStyle
          ? JSON.parse(chartView.customStyle)
          : {}
        chartView.customFilter = chartView.customFilter
          ? JSON.parse(chartView.customFilter)
          : {}
        chartView.senior = chartView.senior ? JSON.parse(chartView.senior) : {}
        chartView.customStyle.text.title = this.chartTitleUpdate

        this.$store.commit('recordViewEdit', {
          viewId: this.chart.id,
          hasEdit: true
        })
        const view = JSON.parse(JSON.stringify(chartView))
        view.xaxis = JSON.stringify(chartView.xaxis)
        view.viewFields = JSON.stringify(chartView.viewFields)
        view.xaxisExt = JSON.stringify(chartView.xaxisExt)
        view.yaxis = JSON.stringify(chartView.yaxis)
        view.yaxisExt = JSON.stringify(chartView.yaxisExt)
        view.extStack = JSON.stringify(chartView.extStack)
        view.drillFields = JSON.stringify(chartView.drillFields)
        view.extBubble = JSON.stringify(chartView.extBubble)
        view.customAttr = JSON.stringify(chartView.customAttr)
        view.customStyle = JSON.stringify(chartView.customStyle)
        view.customFilter = JSON.stringify(chartView.customFilter)
        view.senior = JSON.stringify(chartView.senior)
        view.title = this.chartTitleUpdate
        view.name = this.chartTitleUpdate
        view.stylePriority = chartView.stylePriority
        const viewSave = this.buildParam(chartView, true, 'chart', false, false)
        if (!viewSave) return
        viewEditSave(this.panelInfo.id, viewSave).then(() => {
          this.chart.title = this.chartTitleUpdate
        })
        bus.$emit('title-name', this.chart.title, chartView.id)
        bus.$emit('view-in-cache', {
          type: 'styleChange',
          viewId: chartView.id,
          viewInfo: view
        })
      })
    }
  }
}
</script>

<style scoped lang="scss">
.ipnut-wrap {
  min-width: 20%;
  max-width: 50%;
  display: flex;
  align-items: center;
  i {
    cursor: pointer;
    font-size: 12px;
  }
}
.chart-input-title {
  word-break: break-word;
  font: 12px / 1.231 -apple-system, BlinkMacSystemFont, 'Helvetica Neue', Arial,
    'Microsoft YaHei', 'PingFang SC', sans-serif, 'Segoe UI Symbol';
  overflow: visible;
  margin: 0;
  padding: 0;
  font-weight: 400;
  font-family: inherit;
  border-radius: 2px;
  line-height: 26px;
  padding-left: 10px;
  padding-right: 10px;
  position: relative;
  outline: none;
  z-index: 5;
  color: #182b50;
  font-weight: 400;
  font-family: inherit;
  border: none;
  background: transparent;
  border-width: 0px 0px 1px;
  border-image: initial;
  border-bottom: 1px solid rgb(200, 203, 204);
}
</style>
