<template>
  <div
    ref="tableContainer"
    :style="bg_class"
    style="padding: 8px;width: 100%;height: 100%;overflow: hidden;position: relative;"
  >
    <view-track-bar
      ref="viewTrack"
      :track-menu="trackMenu"
      :style="trackBarStyle"
      class="track-bar"
      @trackClick="trackClick"
    />
    <el-row
      style="height: 100%;"
      :style="cssVars"
    >
      <p
        v-show="title_show"
        ref="title"
        :style="title_class"
      >{{ chart.title }}</p>
      <ux-grid
        ref="plxTable"
        size="mini"
        class="table-class"
        :style="tableStyle"
        :height="height"
        :checkbox-config="{highlight: true}"
        :width-resize="true"
        :header-row-style="table_header_class"
        :row-style="getRowStyle"
        :class="chart.id"
        :merge-cells="mergeCells"
        :show-summary="showSummary"
        :summary-method="summaryMethod"
        :index-config="{seqMethod}"
        :show-header="showHeader"
        @cell-click="cellClick"
        @row-contextmenu="(_, __, e) => cellRightClick(e)"
        @header-contextmenu="(_, e) => cellRightClick(e)"
      >
        <ux-table-column
          type="index"
          :title="indexLabel"
          :width="columnWidth"
          :resizable="true"
          :fixed="getFixed(-1)"
        />
        <ux-table-column
          v-for="(field, index) in fields"
          :key="field.name"
          :field="field.child ? '' : field.dataeaseName"
          :resizable="true"
          :sortable="(!mergeCells || !mergeCells.length) && (!field.child || !field.child.length)"
          :title="field.name"
          :width="columnWidth"
          :fixed="getFixed(index)"
        >
          <ux-table-column
            v-for="item in field.child"
            :key="field.name + item.name"
            :field="item.dataeaseName"
            :title="item.name"
            :width="columnWidth"
          />
        </ux-table-column>
      </ux-grid>

      <el-row
        v-show="showPage"
        class="table-page"
      >
        <el-row style="position: relative;width:100% ">
          <el-row
            class="table-page-inner"
            :style="autoStyle"
          >
            <span
              class="total-style"
              :style="totalStyle"
            >
              {{ $t('chart.total') }}
              <span>{{
                ((chart.datasetMode === 0 && !not_support_page_dataset.includes(chart.datasourceType)) || chart.datasetMode === 1) ? chart.totalItems : ((chart.data && chart.data.tableRow) ? chart.data.tableRow.length : 0)
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

      </el-row>
    </el-row>
  </div>
</template>

<script>
import { hexColorToRGBA } from '../../chart/util'
import { DEFAULT_COLOR_CASE, DEFAULT_SCROLL, DEFAULT_SIZE, NOT_SUPPORT_PAGE_DATASET } from '@/views/chart/chart/chart'
import { mapState } from 'vuex'
import DePagination from '@/components/deCustomCm/pagination.js'
import ViewTrackBar from '@/components/canvas/components/editor/ViewTrackBar.vue'
import { copyString } from '@/views/chart/chart/common/common'
export default {
  name: 'TableNormal',
  components: { ViewTrackBar, DePagination },
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
    enableScroll: {
      type: Boolean,
      required: false,
      default: true
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
      fields: [],
      detailFields: [],
      height: 'auto',
      title_class: {
        margin: '0 0',
        width: '100%',
        fontSize: '18px',
        color: '#303133',
        textAlign: 'left',
        fontStyle: 'normal',
        fontWeight: 'normal'
      },
      //   bg_class: {
      //     background: hexColorToRGBA('#ffffff', 0),
      //     borderRadius: this.borderRadius
      //   },
      table_header_class: {
        fontSize: '12px',
        color: '#606266',
        background: '#e8eaec',
        height: '36px'
      },
      table_item_class: {
        fontSize: '12px',
        color: '#606266',
        background: '#ffffff',
        height: '36px'
      },
      table_item_class_stripe: {
        fontSize: '12px',
        color: '#606266',
        background: '#ffffff',
        height: '36px'
      },
      title_show: true,
      borderRadius: '0px',
      currentPage: {
        page: 1,
        pageSize: 20,
        show: 0
      },
      showPage: false,
      columnWidth: DEFAULT_SIZE.tableColumnWidth,
      scrollTimer: null,
      scrollTop: 0,
      showIndex: false,
      indexLabel: '序号',
      showHeader: true,
      scrollBarColor: DEFAULT_COLOR_CASE.tableScrollBarColor,
      scrollBarHoverColor: DEFAULT_COLOR_CASE.tableScrollBarHoverColor,
      totalStyle: {
        color: '#606266'
      },
      not_support_page_dataset: NOT_SUPPORT_PAGE_DATASET,
      mergeCells: [],
      cssStyleParams: {
        borderColor: DEFAULT_COLOR_CASE.tableBorderColor,
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        whiteSpace: 'nowrap'
      },
      trackBarStyle: {
        position: 'absolute',
        left: '0px',
        top: '0px'
      },
      pointParam: null,
      showSummary: true,
      resizeTimer: null
    }
  },
  computed: {
    scale() {
      return this.previewCanvasScale.scalePointWidth
    },
    autoStyle() {
      return {
        height: (100 / this.scale) + '%!important',
        width: (100 / this.scale) + '%!important',
        left: 50 * (1 - 1 / this.scale) + '%', // 放大余量 除以 2
        top: 50 * (1 - 1 / this.scale) + '%', // 放大余量 除以 2
        transform: 'scale(' + this.scale + ')'
      }
    },
    bg_class() {
      return {
        background: hexColorToRGBA('#ffffff', 0),
        borderRadius: this.borderRadius
      }
    },
    tableStyle() {
      return {
        width: '100%',
        '--scroll-bar-color': this.scrollBarColor,
        '--footer-font-color': this.table_header_class.color,
        '--footer-bg-color': this.table_header_class.background,
        '--footer-font-size': this.table_header_class.fontSize,
        '--footer-height': this.table_header_class.height
      }
    },
    ...mapState([
      'previewCanvasScale'
    ]),
    cssVars() {
      return {
        '--color': this.cssStyleParams.borderColor,
        '--overflow': this.cssStyleParams.overflow,
        '--text-overflow': this.cssStyleParams.textOverflow,
        '--white-space': this.cssStyleParams.whiteSpace
      }
    }

  },
  watch: {
    chart: function() {
      this.init()
    }
  },
  mounted() {
    this.init()
  },
  beforeDestroy() {
    clearInterval(this.scrollTimer)
    window.removeEventListener('resize', this.calcHeightDelay)
  },
  methods: {
    init() {
      this.resetHeight()
      this.$nextTick(() => {
        this.initData()
        this.calcHeightDelay()
      })
      this.setBackGroundBorder()
    },
    setBackGroundBorder() {
      if (this.chart.customStyle) {
        const customStyle = JSON.parse(this.chart.customStyle)
        if (customStyle.background) {
          this.borderRadius = (customStyle.background.borderRadius || 0) + 'px'
        }
      }
    },
    initData() {
      let data = []
      this.showPage = false
      if (this.chart.data) {
        const fields = JSON.parse(JSON.stringify(this.chart.data.fields))
        if (this.chart.data.detailFields) {
          fields.forEach(field => {
            if (field.id === 'DataEase-Detail' && field.dataeaseName === 'detail') {
              field.child = JSON.parse(JSON.stringify(this.chart.data.detailFields))
            }
          })
        }
        const xAxis = JSON.parse(this.chart.xaxis)
        const axisMap = xAxis?.reduce((p, n) => {
          p[n.dataeaseName] = n
          return p
        }, {})
        this.fields = fields.filter(field => {
          return axisMap?.[field.dataeaseName]?.hidden !== true
        })
        const attr = JSON.parse(this.chart.customAttr)
        if (this.currentPage.pageSize < attr.size.tablePageSize) {
          this.currentPage.page = 1
        }
        this.currentPage.pageSize = parseInt(attr.size.tablePageSize ? attr.size.tablePageSize : 20)

        // column width
        const containerWidth = this.$refs.tableContainer.offsetWidth
        const columnWidth = attr.size.tableColumnWidth ? attr.size.tableColumnWidth : this.columnWidth
        const fieldsLength = attr.size.showIndex ? this.fields.length + 1 : this.fields.length
        if (columnWidth < (containerWidth / fieldsLength)) {
          this.columnWidth = containerWidth / fieldsLength
        } else {
          this.columnWidth = columnWidth
        }

        data = JSON.parse(JSON.stringify(this.chart.data.tableRow))
        if ((this.chart.datasetMode === 0 && !NOT_SUPPORT_PAGE_DATASET.includes(this.chart.datasourceType) || this.chart.datasetMode === 1)) {
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
      if (this.chart.data?.detailFields?.length) {
        let result = []
        let groupRowIndex = 0
        data.forEach(item => {
          const baseObj = JSON.parse(JSON.stringify(item))
          delete baseObj.details

          const details = JSON.parse(JSON.stringify(item.details))
          let colsIndex = this.fields.length - 1
          while (colsIndex--) {
            const mergeItem = {
              row: groupRowIndex,
              col: colsIndex,
              rowspan: details.length,
              colspan: 1
            }
            this.mergeCells.push(mergeItem)
          }
          groupRowIndex += details.length
          result = result.concat(details.map(detail => Object.assign(detail, baseObj)))
        })
        data = result
      } else {
        data.forEach(item => {
          Object.keys(item).forEach(key => {
            if (typeof item[key] === 'object') {
              item[key] = ''
            }
          })
        })
      }

      this.$refs.plxTable.reloadData(data).then(() => {
        this.initStyle()
      })
      window.addEventListener('resize', this.calcHeightDelay)
    },
    calcHeightRightNow() {
      this.$nextTick(() => {
        if (this.$refs.tableContainer) {
          const attr = JSON.parse(this.chart.customAttr)
          let pageHeight = 0
          if (this.showPage) {
            pageHeight = 36
          }
          const currentHeight = this.$refs.tableContainer.offsetHeight
          const tableMaxHeight = currentHeight - this.$refs.title.offsetHeight - 16 - pageHeight
          let tableHeight
          if (this.chart.data) {
            if (this.chart.type === 'table-info') {
              if (this.showPage) {
                tableHeight = this.currentPage.pageSize * attr.size.tableItemHeight + attr.size.tableTitleHeight
              } else {
                tableHeight = this.chart.data.tableRow.length * attr.size.tableItemHeight + attr.size.tableTitleHeight
              }
            } else if (this.chart.data.detailFields?.length) {
              let rowLength = 0
              this.chart.data.tableRow.forEach(row => {
                rowLength += (row?.details?.length || 1)
              })
              tableHeight = rowLength * attr.size.tableItemHeight + 2 * attr.size.tableTitleHeight
            } else {
              tableHeight = this.chart.data.tableRow.length * attr.size.tableItemHeight + 2 * attr.size.tableTitleHeight
            }
          } else {
            tableHeight = 0
          }
          const breakLine = attr.size.tableAutoBreakLine ? attr.size.tableAutoBreakLine : DEFAULT_SIZE.tableAutoBreakLine
          if (breakLine) {
            this.height = tableMaxHeight + 'px'
          } else {
            if (tableHeight > tableMaxHeight) {
              this.height = tableMaxHeight + 'px'
            } else {
              this.height = 'auto'
            }
          }

          if (this.enableScroll) {
            this.$nextTick(() => {
              this.initScroll()
            })
          }
        }
      })
    },
    calcHeightDelay() {
      this.resizeTimer && clearTimeout(this.resizeTimer)
      this.resizeTimer = setTimeout(() => {
        this.calcHeightRightNow()
      }, 100)
    },
    initStyle() {
      if (this.chart.customAttr) {
        const customAttr = JSON.parse(this.chart.customAttr)
        if (customAttr.color) {
          this.table_header_class.color = customAttr.color.tableHeaderFontColor ? customAttr.color.tableHeaderFontColor : customAttr.color.tableFontColor
          this.table_header_class.background = hexColorToRGBA(customAttr.color.tableHeaderBgColor, customAttr.color.alpha)
          this.table_item_class.color = customAttr.color.tableFontColor
          this.table_item_class.background = hexColorToRGBA(customAttr.color.tableItemBgColor, customAttr.color.alpha)
          this.scrollBarColor = customAttr.color.tableScrollBarColor ? customAttr.color.tableScrollBarColor : DEFAULT_COLOR_CASE.tableScrollBarColor
          this.cssStyleParams.borderColor = customAttr.color.tableBorderColor ? customAttr.color.tableBorderColor : DEFAULT_COLOR_CASE.tableBorderColor
        }
        if (customAttr.size) {
          this.table_header_class.fontSize = customAttr.size.tableTitleFontSize + 'px'
          this.table_item_class.fontSize = customAttr.size.tableItemFontSize + 'px'
          this.table_header_class.height = customAttr.size.tableTitleHeight + 'px'
          this.table_item_class.height = customAttr.size.tableItemHeight + 'px'
          const visibleColumn = this.$refs.plxTable.getTableColumn().fullColumn
          for (let i = 0, column = visibleColumn[i]; i < visibleColumn.length; i++) {
            if (column.type === 'index' && column.visible !== customAttr.size.showIndex) {
              column.visible = customAttr.size.showIndex
              break
            }
          }
          this.$refs.plxTable.refreshColumn()
          if (!customAttr.size.indexLabel) {
            this.indexLabel = ' '
          } else {
            this.indexLabel = customAttr.size.indexLabel
          }
          if (customAttr.size.showTableHeader === false) {
            this.showHeader = false
            this.showSummary = false
          } else {
            this.showHeader = true
            this.showSummary = this.chart.type === 'table-normal'
          }

          const autoBreakLine = customAttr.size.tableAutoBreakLine ? customAttr.size.tableAutoBreakLine : DEFAULT_SIZE.tableAutoBreakLine
          if (autoBreakLine) {
            this.cssStyleParams.overflow = 'hidden'
            this.cssStyleParams.textOverflow = 'auto'
            this.cssStyleParams.whiteSpace = 'normal'
          } else {
            this.cssStyleParams.overflow = 'hidden'
            this.cssStyleParams.textOverflow = 'ellipsis'
            this.cssStyleParams.whiteSpace = 'nowrap'
          }
        }
        this.table_item_class_stripe = JSON.parse(JSON.stringify(this.table_item_class))
        if (customAttr.color.enableTableCrossBG) {
          this.table_item_class_stripe.background = hexColorToRGBA(customAttr.color.tableItemSubBgColor, customAttr.color.alpha)
        }
      }
      if (this.chart.customStyle) {
        const customStyle = JSON.parse(this.chart.customStyle)
        if (customStyle.text) {
          this.title_show = customStyle.text.show
          this.title_class.fontSize = customStyle.text.fontSize + 'px'
          this.title_class.color = customStyle.text.color
          this.title_class.textAlign = customStyle.text.hPosition
          this.title_class.fontStyle = customStyle.text.isItalic ? 'italic' : 'normal'
          this.title_class.fontWeight = customStyle.text.isBolder ? 'bold' : 'normal'
          // 表格总计与分页颜色，取标题颜色
          this.totalStyle.color = customStyle.text.color
        }
        if (customStyle.background) {
          this.bg_class.background = hexColorToRGBA(customStyle.background.color, customStyle.background.alpha)
        }
      }
      if (this.showSummary) {
        const footerArr = this.$refs.tableContainer.getElementsByClassName('elx-footer--row')
        if (footerArr.length) {
          const footer = footerArr.item(0)
          footer.addEventListener('contextmenu', this.summaryRightClick)
        }
      }
    },
    getRowStyle({ row, rowIndex }) {
      if (rowIndex % 2 !== 0) {
        return this.table_item_class_stripe
      } else {
        return this.table_item_class
      }
    },
    summaryMethod({ columns, data }) {
      const that = this
      const means = [] // 合计
      const x = JSON.parse(that.chart.xaxis)
      const customAttr = JSON.parse(that.chart.customAttr)
      columns.forEach((column, columnIndex) => {
        if (columnIndex === 0 && x.length > 0) {
          means.push('合计')
        } else {
          // 显示序号就往后推一列
          let requireSumIndex = x.length
          if (customAttr.size.showIndex) {
            requireSumIndex++
          }
          if (columnIndex >= requireSumIndex) {
            const values = data.map(item => Number(item[column.property]))
            // 合计
            if (!values.every(value => isNaN(value))) {
              means[columnIndex] = values.reduce((prev, curr) => {
                const value = Number(curr)
                if (!isNaN(value)) {
                  return prev + curr
                } else {
                  return prev
                }
              }, 0)
              means[columnIndex] = (means[columnIndex] + '').includes('.') ? means[columnIndex].toFixed(2) : means[columnIndex]
            } else {
              means[columnIndex] = ''
            }
          } else {
            means[columnIndex] = ''
          }
        }
      })
      // 返回一个二维数组的表尾合计(不要平均值，就不要在数组中添加)
      return [means]
    },
    seqMethod({ rowIndex, column }) {
      if (column?.type === 'index') {
        return (this.currentPage.pageSize * (this.currentPage.page - 1)) + rowIndex + 1
      }
    },
    chartResize() {
      // 指定图表的配置项和数据
      this.calcHeightDelay()
    },

    initClass() {
      return this.chart.id
    },

    resetHeight() {
      this.height = 100
    },

    pageChange(val) {
      this.currentPage.pageSize = val
      if ((this.chart.datasetMode === 0 && !NOT_SUPPORT_PAGE_DATASET.includes(this.chart.datasourceType)) || this.chart.datasetMode === 1) {
        this.$emit('onPageChange', this.currentPage)
      } else {
        this.init()
      }
    },

    pageClick(val) {
      this.currentPage.page = val
      if ((this.chart.datasetMode === 0 && !NOT_SUPPORT_PAGE_DATASET.includes(this.chart.datasourceType)) || this.chart.datasetMode === 1) {
        this.$emit('onPageChange', this.currentPage)
      } else {
        this.init()
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

      const scrollContainer = document.getElementsByClassName(this.chart.id)[0].getElementsByClassName('elx-table--body-wrapper')[0]

      this.scrollTop = 0

      if (senior && senior.scrollCfg && senior.scrollCfg.open && (this.chart.type === 'table-normal' || (this.chart.type === 'table-info' && !this.showPage))) {
        let rowHeight = customAttr.size.tableItemHeight
        if (rowHeight < 36) {
          rowHeight = 36
        }

        const attr = JSON.parse(this.chart.customAttr)
        const breakLine = attr.size.tableAutoBreakLine ? attr.size.tableAutoBreakLine : DEFAULT_SIZE.tableAutoBreakLine

        this.scrollTimer = setInterval(() => {
          let top = 0
          if (breakLine) {
            top = senior.scrollCfg.step ? senior.scrollCfg.step : DEFAULT_SCROLL.step
          } else {
            top = rowHeight * senior.scrollCfg.row
          }

          const { clientHeight, scrollTop, scrollHeight } = scrollContainer

          if (clientHeight + scrollTop < scrollHeight) {
            this.scrollTop += top
          } else {
            this.scrollTop = 0
          }
          if (!clientHeight) {
            return
          }
          scrollContainer.scrollTo({
            top: this.scrollTop,
            behavior: this.scrollTop === 0 ? 'instant' : 'smooth'
          })
        }, senior.scrollCfg.interval)
      }
    },
    cellClick(row, col, cell, ev) {
      const nameIdMap = this.chart.data.fields.reduce((pre, next) => {
        pre[next['dataeaseName']] = next['id']
        return pre
      }, {})
      const dimensionList = []
      for (const key in row) {
        if (nameIdMap[key]) {
          dimensionList.push({ id: nameIdMap[key], value: row[key] })
        }
      }
      const parent = cell.offsetParent
      // 元素离顶部距离减去滚动距离加上表头高度加上点击位置高度
      const y = cell.offsetTop - parent.scrollTop + parent.offsetTop + ev.offsetY
      const position = {
        x: cell.offsetLeft + ev.offsetX,
        y
      }
      this.antVActionPost(dimensionList, nameIdMap[col.property] || 'null', position)
    },
    cellRightClick(event) {
      if (event.target?.innerText) {
        copyString(event.target.innerText, true)
      }
      event.preventDefault()
    },
    summaryRightClick(event) {
      let targetDom
      if (event.target.classList.contains('elx-cell--item')) {
        targetDom = event.target
      }
      if (!targetDom) {
        const tmp = event.target.getElementsByClassName('elx-cell--item')
        if (tmp.length) {
          targetDom = tmp.item(0)
        }
      }
      if (targetDom) {
        const content = targetDom.innerText
        if (content?.trim()) {
          copyString(content, true)
        }
        event.preventDefault()
      }
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
    trackClick(trackAction) {
      const param = this.pointParam
      if (!param?.data?.dimensionList) {
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
    getFixed(index) {
      const size = JSON.parse(this.chart.customAttr).size
      const { showIndex, tableColumnFreezeHead } = size
      if (showIndex) {
        return index < tableColumnFreezeHead - 1 ? 'left' : ''
      }
      return index < tableColumnFreezeHead ? 'left' : ''
    }
  }
}
</script>

<style scoped lang="scss">
.table-class ::v-deep .body--wrapper {
  background: rgba(1, 1, 1, 0);
}

.table-class ::v-deep .elx-cell {
  max-height: none !important;
  line-height: normal !important;
}

.table-page-inner {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  width: 100%;
  overflow: hidden;
}

.table-page {
  position: absolute;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  width: 100%;
  overflow: hidden;
}

.page-style {
  margin-right: auto;
}

.total-style {
  flex: 1;
  font-size: 12px;
  color: #606266;
  white-space: nowrap;
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
.table-class{
  ::-webkit-scrollbar-thumb {
    background: var(--scroll-bar-color);
  }
}
.table-class{
  scrollbar-color: var(--scroll-bar-color) transparent;
}

.table-class {
  ::v-deep .elx-table.border--full .elx-body--column,
  ::v-deep .elx-table.border--full .elx-footer--column,
  ::v-deep .elx-table.border--full .elx-header--column {
    background-image: linear-gradient(var(--color, #e8eaec), var(--color, #e8eaec)), linear-gradient(var(--color, #e8eaec), var(--color, #e8eaec)) !important;
  }
  ::v-deep .elx-table--border-line {
    border: 1px solid var(--color, #e8eaec) !important;
  }
  ::v-deep .elx-table .elx-table--header-wrapper .elx-table--header-border-line {
    border-bottom: 1px solid var(--color, #e8eaec) !important;
  }
  ::v-deep .elx-table .elx-table--footer-wrapper {
    border-top: 1px solid var(--color, #e8eaec) !important;
  }
  ::v-deep .elx-checkbox .elx-checkbox--label,
  ::v-deep .elx-radio .elx-radio--label,
  ::v-deep .elx-radio-button .elx-radio--label,
  ::v-deep .elx-table .elx-body--column.col--ellipsis:not(.col--actived) > .elx-cell,
  ::v-deep .elx-table .elx-footer--column.col--ellipsis:not(.col--actived) > .elx-cell,
  ::v-deep .elx-table .elx-header--column.col--ellipsis:not(.col--actived) > .elx-cell{
    overflow: var(--overflow, 'hidden');
    text-overflow: var(--text-overflow, 'ellipsis');
    white-space: var(--white-space, 'nowrap');
  }
  ::v-deep .elx-table--footer {
    color: var(--footer-font-color);
    background: var(--footer-bg-color);
    font-size: var(--footer-font-size);
    height: var(--footer-height);
  }
}

</style>
