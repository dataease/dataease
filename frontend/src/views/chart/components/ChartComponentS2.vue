<template>
  <div ref="chartContainer" style="padding: 0;width: 100%;height: 100%;overflow: hidden;" :style="bg_class">
    <view-track-bar ref="viewTrack" :track-menu="trackMenu" class="track-bar" :style="trackBarStyleTime" @trackClick="trackClick" />
    <span v-if="chart.type" v-show="title_show" ref="title" :style="title_class" style="cursor: default;display: block;">
      <p style="padding:6px 10px 0 10px;margin: 0;overflow: hidden;white-space: pre;text-overflow: ellipsis;">{{ chart.title }}</p>
    </span>
    <div style="width: 100%;overflow: hidden;padding: 8px;" :style="{height:chartHeight,background:container_bg_class.background}">
      <div v-if="chart.type === 'table-normal'" :id="chartId" style="width: 100%;overflow: hidden;" class="table-dom-normal" />
      <div v-if="chart.type === 'table-info'" :id="chartId" style="width: 100%;overflow: hidden;" class="table-dom-info" />
      <el-row v-show="chart.type === 'table-info'" class="table-page">
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
import { hexColorToRGBA } from '@/views/chart/chart/util'
import { baseTableInfo, baseTableNormal } from '@/views/chart/chart/table/table-info'

export default {
  name: 'ChartComponentS2',
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
        background: hexColorToRGBA('#ffffff', 0)
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
      tableData: []
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
    }
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
  methods: {
    initData() {
      let datas = []
      if (this.chart.data) {
        this.fields = JSON.parse(JSON.stringify(this.chart.data.fields))
        const attr = JSON.parse(this.chart.customAttr)
        this.currentPage.pageSize = parseInt(attr.size.tablePageSize ? attr.size.tablePageSize : 20)
        datas = JSON.parse(JSON.stringify(this.chart.data.tableRow))
        if (this.chart.type === 'table-info') {
          // 计算分页
          this.currentPage.show = datas.length
          const pageStart = (this.currentPage.page - 1) * this.currentPage.pageSize
          const pageEnd = pageStart + this.currentPage.pageSize
          datas = datas.slice(pageStart, pageEnd)
        }
      } else {
        this.fields = []
        datas = []
        this.resetPage()
      }
      this.tableData = datas
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
      if (!chart.data || (!chart.data.datas && !chart.data.series)) {
        chart.data = {
          datas: [{}],
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
      }
      this.setBackGroundBorder()
    },

    antVAction(param) {
      const cell = this.myChart.getCell(param.target)
      const meta = cell.getMeta()
      console.log(meta)
      // if (this.chart.type === 'treemap') {
      //   this.pointParam = param.data.data
      // } else {
      //   this.pointParam = param.data
      // }
      // if (this.trackMenu.length < 2) { // 只有一个事件直接调用
      //   this.trackClick(this.trackMenu[0])
      // } else { // 视图关联多个事件
      //   this.trackBarStyle.left = param.x + 'px'
      //   this.trackBarStyle.top = (param.y + 10) + 'px'
      //   this.$refs.viewTrack.trackButtonClick()
      // }

      let xAxis = []
      if (this.chart.xaxis) {
        xAxis = JSON.parse(this.chart.xaxis)
      }
      let field = {}
      if (meta.colIndex < xAxis.length) {
        field = xAxis[meta.colIndex]
      }
      const dimensionList = []
      dimensionList.push({ id: field.id, value: meta.fieldValue })
      this.pointParam = {
        data: {
          dimensionList: dimensionList
        }
      }
      console.log(this.pointParam)

      if (this.trackMenu.length < 2) { // 只有一个事件直接调用
        this.trackClick(this.trackMenu[0])
      } else { // 视图关联多个事件
        this.trackBarStyle.left = 50 + 'px'
        this.trackBarStyle.top = (50 + 10) + 'px'
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
        }
        if (customStyle.background) {
          this.title_class.background = hexColorToRGBA(customStyle.background.color, customStyle.background.alpha)
          this.borderRadius = (customStyle.background.borderRadius || 0) + 'px'

          this.container_bg_class.background = hexColorToRGBA(customStyle.background.color, customStyle.background.alpha)
        }
      }
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
      this.calcHeightRightNow()
      setTimeout(() => {
        this.calcHeightRightNow()
      }, 100)
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
    }
  }
}
</script>

<style scoped lang="scss">
.table-dom-info{
  height:calc(100% - 36px);
}
.table-dom-normal{
  height:100%;
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
.page-style >>> .el-input__inner{
  height: 24px;
}
</style>
