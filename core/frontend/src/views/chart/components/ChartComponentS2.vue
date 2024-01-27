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
      :class="titleIsRight"
      :style="title_class"
      style="cursor: default;display: block;"
    >
      <div style="padding:4px 4px 0;margin: 0;">
        <chart-title-update
          :title-class="title_class"
          :chart-info="chartInfo"
        />
        <title-remark
          v-if="remarkCfg.show"
          style="text-shadow: none!important;margin-left: 4px;"
          :remark-cfg="remarkCfg"
        />
      </div>
    </span>
    <div
      ref="tableContainer"
      style="width: 100%;overflow: hidden;"
      :style="{background:container_bg_class.background, height: chartHeight}"
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
        style="position: relative;"
      >
        <el-row
          class="table-page"
          :style="autoStyle"
        >
          <span
            class="total-style"
            :style="totalStyle"
          >
            {{ $t('chart.total') }}
            <span>{{
              (chart.datasetMode === 0 && !not_support_page_dataset.includes(chart.datasourceType)) ? chart.totalItems : ((chart.data && chart.data.tableRow) ? chart.data.tableRow.length : 0)
            }}</span>
            {{ $t('chart.items') }}
          </span>
          <de-pagination
            small
            :current-page="currentPage.page"
            :page-size="currentPage.pageSize"
            :pager-count="5"
            :custom-style="{
              color: title_class.color
            }"
            layout="prev, pager, next"
            :total="currentPage.show"
            class="page-style"
            @current-change="pageClick"
            @size-change="pageChange"
          />
        </el-row>
      </el-row>
    </div>
  </div>
</template>

<script>
import { uuid } from 'vue-uuid'
import ViewTrackBar from '@/components/canvas/components/editor/ViewTrackBar'
import { getRemark, hexColorToRGBA } from '@/views/chart/chart/util'
import { baseTableInfo, baseTableNormal, baseTablePivot } from '@/views/chart/chart/table/table-info'
import TitleRemark from '@/views/chart/view/TitleRemark'
import { CHART_CONT_FAMILY_MAP, DEFAULT_TITLE_STYLE, NOT_SUPPORT_PAGE_DATASET } from '@/views/chart/chart/chart'
import ChartTitleUpdate from './ChartTitleUpdate.vue'
import { mapState } from 'vuex'
import DePagination from '@/components/deCustomCm/pagination.js'

export default {
  name: 'ChartComponentS2',
  components: { TitleRemark, ViewTrackBar, ChartTitleUpdate, DePagination },
  props: {
    terminalType: {
      type: String,
      default: 'pc'
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
      },
      totalStyle: {
        color: '#606266'
      },
      not_support_page_dataset: NOT_SUPPORT_PAGE_DATASET,
      resizeTimer: null
    }
  },

  computed: {
    titleIsRight() {
      return this.title_class?.textAlign === 'right' && 'title-is-right'
    },
    scale() {
      return this.previewCanvasScale.scalePointWidth
    },
    autoStyle() {
      if (this.terminalType === 'pc') {
        return {
          height: (100 / this.scale) + '%!important',
          width: (100 / this.scale) + '%!important',
          left: 50 * (1 - 1 / this.scale) + '%', // 放大余量 除以 2
          top: 50 * (1 - 1 / this.scale) + '%', // 放大余量 除以 2
          transform: 'scale(' + this.scale + ')'
        }
      } else {
        return {}
      }
    },
    trackBarStyleTime() {
      return this.trackBarStyle
    },
    bg_class() {
      return {
        borderRadius: this.borderRadius
      }
    },
    chartInfo() {
      const { id, title } = this.chart
      return { id, title }
    },
    ...mapState([
      'previewCanvasScale'
    ])
  },
  watch: {
    chart: {
      handler(newVal, oldVla) {
        this.initData()
        this.initTitle()
        this.calcHeightRightNow(this.drawView)
      },
      deep: true
    }
  },
  mounted() {
    this.preDraw()
  },
  beforeDestroy() {
    clearInterval(this.scrollTimer)
    window.removeEventListener('resize', this.chartResize)
    this.myChart?.destroy?.()
    this.myChart = null
  },
  methods: {
    initData() {
      let data = []
      this.showPage = false
      if (this.chart.data && this.chart.data.fields) {
        this.fields = JSON.parse(JSON.stringify(this.chart.data.fields))
        const attr = JSON.parse(this.chart.customAttr)
        if (this.currentPage.pageSize < attr.size.tablePageSize) {
          this.currentPage.page = 1
        }
        this.currentPage.pageSize = parseInt(attr.size.tablePageSize ? attr.size.tablePageSize : 20)
        data = JSON.parse(JSON.stringify(this.chart.data.tableRow))
        if (this.chart.datasetMode === 0 && !NOT_SUPPORT_PAGE_DATASET.includes(this.chart.datasourceType)) {
          if (this.chart.type === 'table-info' && (attr.size.tablePageMode === 'page' || !attr.size.tablePageMode) && this.chart.totalItems > this.currentPage.pageSize) {
            this.currentPage.show = this.chart.totalItems
            this.showPage = true
          }
        } else {
          if (this.chart.type === 'table-info' && (attr.size.tablePageMode === 'page' || !attr.size.tablePageMode) && data.length > this.currentPage.pageSize) {
            // 计算分页
            this.currentPage.show = data.length
            const pageStart = (this.currentPage.page - 1) * this.currentPage.pageSize
            const pageEnd = pageStart + this.currentPage.pageSize
            data = data.slice(pageStart, pageEnd)
            this.showPage = true
          }
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
      this.calcHeightRightNow(this.drawView)
      window.addEventListener('resize', this.chartResize)
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
        this.myChart = baseTableInfo(this.myChart, this.chartId, chart, this.antVAction, this.tableData, this.currentPage, this)
      } else if (chart.type === 'table-normal') {
        this.myChart = baseTableNormal(this.myChart, this.chartId, chart, this.antVAction, this.tableData, this)
      } else if (chart.type === 'table-pivot') {
        this.myChart = baseTablePivot(this.myChart, this.chartId, chart, this.antVAction, this.tableHeaderClick, this.tableData)
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
      } else if (this.showPage && (this.chart.datasetMode === 1 || (this.chart.datasetMode === 0 && this.not_support_page_dataset.includes(this.chart.datasourceType)))) {
        const rowIndex = (this.currentPage.page - 1) * this.currentPage.pageSize + meta.rowIndex
        rowData = this.chart.data.tableRow[rowIndex]
      } else {
        rowData = this.chart.data.tableRow[meta.rowIndex]
      }
      const dimensionList = []
      for (const key in rowData) {
        if (nameIdMap[key]) {
          dimensionList.push({ id: nameIdMap[key], value: rowData[key] })
        }
      }
      this.antVActionPost(dimensionList, nameIdMap[meta.valueField] || 'null', param)
    },
    antVActionPost(dimensionList, name, param) {
      this.pointParam = {
        data: {
          dimensionList: dimensionList,
          quotaList: [],
          name: name,
          sourceType: this.chart.type
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
    tableHeaderClick(param) {
      const cell = this.myChart.getCell(param.target)
      const meta = cell.getMeta()
      const rowData = meta.query
      const nameIdMap = this.chart.data.fields.reduce((pre, next) => {
        pre[next['dataeaseName']] = next['id']
        return pre
      }, {})
      const dimensionList = []
      for (const key in rowData) {
        if (nameIdMap[key]) {
          dimensionList.push({ id: nameIdMap[key], value: rowData[key] })
        }
      }
      this.antVActionPost(dimensionList, nameIdMap[meta.field] || 'null', param)
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
      this.resizeTimer && clearTimeout(this.resizeTimer)
      this.resizeTimer = setTimeout(() => {
        this.initData()
        this.initTitle()
        this.calcHeightRightNow((width, height) => {
          if (!this.myChart) {
            return
          }
          const { width: chartWidth, height: chartHeight } = this.myChart.options
          if (width !== chartWidth || height !== chartHeight) {
            this.myChart?.changeSheetSize(width, height)
            // 大小变化或者tab变化重新渲染
            if (chartWidth || chartHeight || !(chartHeight || chartWidth)) {
              this.myChart.render()
            }
            this.initScroll()
          }
        })
      }, 100)
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
        quotaList: this.pointParam.data.quotaList,
        sourceType: this.pointParam.data.sourceType
      }
      switch (trackAction) {
        case 'drill':
          this.currentPage.page = 1
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

          this.title_class.fontFamily = customStyle.text.fontFamily ? CHART_CONT_FAMILY_MAP[customStyle.text.fontFamily] : DEFAULT_TITLE_STYLE.fontFamily
          this.title_class.letterSpacing = (customStyle.text.letterSpace ? customStyle.text.letterSpace : DEFAULT_TITLE_STYLE.letterSpace) + 'px'
          this.title_class.textShadow = customStyle.text.fontShadow ? '2px 2px 4px' : 'none'
          // 表格总计与分页颜色，取标题颜色
          this.totalStyle.color = customStyle.text.color
        }
        if (customStyle.background) {
          this.title_class.background = hexColorToRGBA(customStyle.background.color, customStyle.background.alpha)
          this.borderRadius = (customStyle.background.borderRadius || 0) + 'px'

          this.container_bg_class.background = hexColorToRGBA(customStyle.background.color, customStyle.background.alpha)
        }
      }
      this.initRemark()
    },

    calcHeightRightNow(callback) {
      this.$nextTick(() => {
        if (this.$refs.chartContainer) {
          const { offsetWidth, offsetHeight } = this.$refs.chartContainer
          let titleHeight = 0
          if (this.$refs.title) {
            titleHeight = this.$refs.title.offsetHeight
          }
          this.chartHeight = (offsetHeight - titleHeight) + 'px'
          this.$nextTick(() => callback?.(offsetWidth, offsetHeight - titleHeight))
        }
      })
    },
    pageChange(val) {
      this.currentPage.pageSize = val
      if (this.chart.datasetMode === 0 && !NOT_SUPPORT_PAGE_DATASET.includes(this.chart.datasourceType)) {
        this.$emit('onPageChange', this.currentPage)
      } else {
        this.initData()
        this.drawView()
      }
    },

    pageClick(val) {
      this.currentPage.page = val
      if (this.chart.datasetMode === 0 && !NOT_SUPPORT_PAGE_DATASET.includes(this.chart.datasourceType)) {
        this.$emit('onPageChange', this.currentPage)
      } else {
        this.initData()
        this.drawView()
      }
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

      if (senior && senior.scrollCfg && senior.scrollCfg.open && (this.chart.type === 'table-normal' || (this.chart.type === 'table-info' && !this.showPage))) {
        const rowHeight = customAttr.size.tableItemHeight
        const headerHeight = customAttr.size.tableTitleHeight

        this.scrollTimer = setInterval(() => {
          const offsetHeight = document.getElementById(this.chartId).offsetHeight
          const top = rowHeight * senior.scrollCfg.row
          if ((offsetHeight - headerHeight + this.scrollTop) < rowHeight * this.chart.data.tableRow.length) {
            this.scrollTop += top
          } else {
            this.scrollTop = 0
          }
          if (!offsetHeight) {
            return
          }
          this.myChart.facet.scrollWithAnimation({
            offsetY: {
              value: this.scrollTop,
              animate: false
            }
          })
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
.table-dom-info {
  height: calc(100% - 36px);
}

.table-dom-info-pull {
  height: calc(100%);
}

.table-dom-normal {
  height: 100%;
}

.table-dom-info-drill {
  height: calc(100% - 36px - 24px);
}

.table-dom-info-drill-pull {
  height: calc(100% - 24px);
}

.table-dom-normal-drill {
  height: calc(100% - 24px);
}

.table-page {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  width: 100%;
  overflow: hidden;
  margin-top: 8px;
}

.page-style {
  margin-right: auto;
}

.total-style {
  flex: 1;
  font-size: 12px;
  color: #606266;
  white-space: nowrap;
  padding-left: 8px;
}

.page-style ::v-deep .el-input__inner {
  height: 24px;
}

.page-style ::v-deep button {
  background: transparent !important;
}

.page-style ::v-deep li {
  background: transparent !important;
}
</style>
<style>
.antv-s2-tooltip-container {
    padding: 4px 2px;
}
</style>
