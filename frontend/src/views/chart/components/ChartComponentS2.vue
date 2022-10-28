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
      v-if="chart.type"
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
      ref="tableContainer"
      style="width: 100%;overflow: hidden;"
      :style="{background:container_bg_class.background}"
    >
      <div
        v-if="chart.type === 'table-normal'"
        :id="chartId"
        style="width: 100%;overflow: hidden;"
        :class="chart.drill ? 'table-dom-normal-drill' : 'table-dom-normal'"
      />
      <div
        v-if="chart.type === 'table-info'"
        :id="chartId"
        style="width: 100%;overflow: hidden;"
        :class="chart.drill ? (showPage ? 'table-dom-info-drill' : 'table-dom-info-drill-pull') : (showPage ? 'table-dom-info' : 'table-dom-info-pull')"
      />
      <div
        v-if="chart.type === 'table-pivot'"
        :id="chartId"
        style="width: 100%;overflow: hidden;"
        class="table-dom-normal"
      />
      <el-row
        v-show="showPage"
        class="table-page"
      >
        <span class="total-style">
          {{ $t('chart.total') }}
          <span>{{ (chart.data && chart.data.tableRow)?chart.data.tableRow.length:0 }}</span>
          {{ $t('chart.items') }}
        </span>
        <el-pagination
          small
          :current-page="currentPage.page"
          :page-sizes="[10,20,50,100]"
          :page-size="currentPage.pageSize"
          :pager-count="5"
          layout="prev, pager, next"
          :total="currentPage.show"
          class="page-style"
          @current-change="pageClick"
          @size-change="pageChange"
        />
      </el-row>
    </div>
  </div>
</template>

<script>
import { uuid } from 'vue-uuid'
import ViewTrackBar from '@/components/canvas/components/Editor/ViewTrackBar'
import { getRemark, hexColorToRGBA } from '@/views/chart/chart/util'
import { baseTableInfo, baseTableNormal, baseTablePivot } from '@/views/chart/chart/table/table-info'
import TitleRemark from '@/views/chart/view/TitleRemark'
import { DEFAULT_TITLE_STYLE } from '@/views/chart/chart/chart'
import clickoutside from 'element-ui/src/utils/clickoutside.js'
import bus from '@/utils/bus'
import { mapState } from 'vuex'
import { compareItem } from '@/views/chart/chart/compare'
import {
  getChartDetails
} from '@/api/chart/chart'
import {
  viewEditSave
} from '@/api/chart/chart'
export default {
  name: 'ChartComponentS2',
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
      chartTitleEditer: false,
      pointParam: null,
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
      container_bg_class: {
        background: hexColorToRGBA('#ffffff', 0)
      },
      title_show: true,
      antVRenderStatus: false,
      chartTitleUpdate: '',
      currentPage: {
        page: 1,
        pageSize: 20,
        show: 0
      },
      tableData: [],
      showPage: false,
      scrollTimer: null,
      scrollTop: 0,
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
    },
    ...mapState([
      'mobileLayoutStatus',
      'previewVisible'
    ])
  },
  watch: {
    chart: {
      handler(newVal, oldVla) {
        this.initData()
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
  beforeDestroy() {
    clearInterval(this.scrollTimer)
  },
  methods: {
    initData() {
      let data = []
      this.showPage = false
      if (this.chart.data && this.chart.data.fields) {
        this.fields = JSON.parse(JSON.stringify(this.chart.data.fields))
        const attr = JSON.parse(this.chart.customAttr)
        this.currentPage.pageSize = parseInt(attr.size.tablePageSize ? attr.size.tablePageSize : 20)
        data = JSON.parse(JSON.stringify(this.chart.data.tableRow))
        if (this.chart.type === 'table-info' && (attr.size.tablePageMode === 'page' || !attr.size.tablePageMode) && data.length > this.currentPage.pageSize) {
          // 计算分页
          this.currentPage.show = data.length
          const pageStart = (this.currentPage.page - 1) * this.currentPage.pageSize
          const pageEnd = pageStart + this.currentPage.pageSize
          data = data.slice(pageStart, pageEnd)
          this.showPage = true
        }
      } else {
        this.fields = []
        data = []
        this.resetPage()
      }
      this.tableData = data
    },
    preDraw() {
      this.initData()
      this.initTitle()
      this.calcHeightDelay()
      new Promise((resolve) => { resolve() }).then(() => {
        this.drawView()
      })
      const that = this
      window.onresize = function() {
        that.initData()
        that.initTitle()
        that.calcHeightDelay()
        new Promise((resolve) => { resolve() }).then(() => {
          that.drawView()
        })
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
      if (chart.type === 'table-info') {
        this.myChart = baseTableInfo(this.myChart, this.chartId, chart, this.antVAction, this.tableData)
      } else if (chart.type === 'table-normal') {
        this.myChart = baseTableNormal(this.myChart, this.chartId, chart, this.antVAction, this.tableData)
      } else if (chart.type === 'table-pivot') {
        this.myChart = baseTablePivot(this.myChart, this.chartId, chart, this.antVAction, this.tableData)
      } else {
        if (this.myChart) {
          this.antVRenderStatus = false
          this.myChart.destroy()
        }
      }

      if (this.myChart && this.searchCount > 0) {
        this.myChart.options.animation = false
      }

      if (this.myChart && this.antVRenderStatus) {
        this.myChart.render()
        this.initScroll()
      }
      this.setBackGroundBorder()
    },

    antVAction(param) {
      const cell = this.myChart.getCell(param.target)
      const meta = cell.getMeta()
      const nameIdMap = this.chart.data.fields.reduce((pre, next) => {
        pre[next['dataeaseName']] = next['id']
        return pre
      }, {})

      let rowData
      if (this.chart.type === 'table-pivot') {
        rowData = { ...meta.rowQuery, ...meta.colQuery }
        rowData[meta.valueField] = meta.fieldValue
      } else if (this.showPage) {
        const rowIndex = (this.currentPage.page - 1) * this.currentPage.pageSize + meta.rowIndex
        rowData = this.chart.data.tableRow[rowIndex]
      } else {
        rowData = this.chart.data.tableRow[meta.rowIndex]
      }
      const dimensionList = []
      for (const key in rowData) {
        if (meta.fieldValue === rowData[key]) {
          dimensionList.push({ id: nameIdMap[key], value: rowData[key] })
        }
      }

      this.pointParam = {
        data: {
          dimensionList: dimensionList,
          quotaList: [],
          name: meta.fieldValue || 'null'
        }
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
      this.initData()
      this.initTitle()
      this.calcHeightDelay()
      new Promise((resolve) => { resolve() }).then(() => {
        this.drawView()
      })
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
        name: this.pointParam.data.name,
        viewId: this.chart.id,
        dimensionList: this.pointParam.data.dimensionList,
        quotaList: this.pointParam.data.quotaList
      }
      const jumpParam = {
        option: 'jump',
        name: this.pointParam.data.name,
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
    },
    // title and bg
    initTitle() {
      if (this.chart.customStyle) {
        const customStyle = JSON.parse(this.chart.customStyle)
        if (customStyle.text) {
          this.title_show = customStyle.text.show
          // this.title_class.fontSize = customStyle.text.fontSize + 'px'
          this.title_class.color = customStyle.text.color
          this.title_class.textAlign = customStyle.text.hPosition
          this.title_class.fontStyle = customStyle.text.isItalic ? 'italic' : 'normal'
          this.title_class.fontWeight = customStyle.text.isBolder ? 'bold' : 'normal'

          if (this.$refs.title) {
            this.$refs.title.style.fontSize = customStyle.text.fontSize + 'px'
          }

          this.title_class.fontFamily = customStyle.text.fontFamily ? customStyle.text.fontFamily : DEFAULT_TITLE_STYLE.fontFamily
          this.title_class.letterSpacing = (customStyle.text.letterSpace ? customStyle.text.letterSpace : DEFAULT_TITLE_STYLE.letterSpace) + 'px'
          this.title_class.textShadow = customStyle.text.fontShadow ? '2px 2px 4px' : 'none'
        }
        if (customStyle.background) {
          this.title_class.background = hexColorToRGBA(customStyle.background.color, customStyle.background.alpha)
          this.borderRadius = (customStyle.background.borderRadius || 0) + 'px'

          this.container_bg_class.background = hexColorToRGBA(customStyle.background.color, customStyle.background.alpha)
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
            this.$refs.tableContainer.style.height = this.chartHeight
          }
        }
      })
    },
    calcHeightDelay() {
      this.calcHeightRightNow()
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
      if (this.mainActiveName !== 'PanelEdit' || this.mobileLayoutStatus || this.previewVisible) return
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
    pageChange(val) {
      this.currentPage.pageSize = val
      this.initData()
      this.drawView()
    },

    pageClick(val) {
      this.currentPage.page = val
      this.initData()
      this.drawView()
    },

    resetPage() {
      this.currentPage = {
        page: 1,
        pageSize: 20,
        show: 0
      }
    },

    initScroll() {
      clearInterval(this.scrollTimer)
      // 首先回到最顶部，然后计算行高*行数作为top，最后判断：如果top<数据量*行高，继续滚动，否则回到顶部
      const customAttr = JSON.parse(this.chart.customAttr)
      const senior = JSON.parse(this.chart.senior)

      this.scrollTop = 0
      this.myChart.store.set('scrollY', this.scrollTop)
      this.myChart.render()

      if (senior && senior.scrollCfg && senior.scrollCfg.open && (this.chart.type === 'table-normal' || (this.chart.type === 'table-info' && !this.showPage))) {
        const rowHeight = customAttr.size.tableItemHeight
        const headerHeight = customAttr.size.tableTitleHeight

        this.scrollTimer = setInterval(() => {
          const top = rowHeight * senior.scrollCfg.row
          const dom = document.getElementById(this.chartId)
          if ((dom.offsetHeight - headerHeight + this.scrollTop) < rowHeight * this.chart.data.tableRow.length) {
            this.scrollTop += top
          } else {
            this.scrollTop = 0
          }
          this.myChart.store.set('scrollY', this.scrollTop)
          this.myChart.render()
        }, senior.scrollCfg.interval)
      }
    },
    initRemark() {
      this.remarkCfg = getRemark(this.chart)
    }
  }
}
</script>

<style scoped lang="scss">
.table-dom-info{
  height:calc(100% - 36px);
}
.table-dom-info-pull{
  height:calc(100%);
}
.table-dom-normal{
  height:100%;
}
.table-dom-info-drill{
  height:calc(100% - 36px - 12px);
}
.table-dom-info-drill-pull{
  height:calc(100% - 12px);
}
.table-dom-normal-drill{
  height:calc(100% - 12px);
}
.table-page{
  display: flex;
  align-items: center;
  justify-content: flex-end;
  width: 100%;
  overflow: hidden;
  margin-top: 8px;
}
.page-style{
  margin-right: auto;
}
.total-style{
  flex: 1;
  font-size: 12px;
  color: #606266;
  white-space:nowrap;
}
.page-style ::v-deep .el-input__inner{
  height: 24px;
}
.page-style ::v-deep button{
  background: transparent!important;
}
.page-style ::v-deep li{
  background: transparent!important;
}

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
