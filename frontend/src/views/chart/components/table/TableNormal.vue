<template>
  <div
    ref="tableContainer"
    :style="bg_class"
    style="padding: 8px;width: 100%;height: 100%;overflow: hidden;"
  >
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
        :style="tableStyle"
        :height="height"
        :checkbox-config="{highlight: true}"
        :width-resize="true"
        :header-row-style="table_header_class"
        :row-style="getRowStyle"
        class="table-class"
        :class="chart.id"
        :merge-cells="mergeCells"
        :show-summary="showSummary"
        :summary-method="summaryMethod"
        :index-config="{seqMethod}"
      >
        <ux-table-column
          type="index"
          :title="indexLabel"
        />
        <ux-table-column
          v-for="field in fields"
          :key="field.name"
          :field="field.child ? '' : field.dataeaseName"
          :resizable="true"
          :sortable="(!mergeCells || !mergeCells.length) && (!field.child || !field.child.length)"
          :title="field.name"
          :width="columnWidth"
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

      </el-row>
    </el-row>
  </div>
</template>

<script>
import { hexColorToRGBA } from '../../chart/util'
import eventBus from '@/components/canvas/utils/eventBus'
import { DEFAULT_COLOR_CASE, DEFAULT_SCROLL, DEFAULT_SIZE, NOT_SUPPORT_PAGE_DATASET } from '@/views/chart/chart/chart'
import { mapState } from 'vuex'
import DePagination from '@/components/deCustomCm/pagination.js'
export default {
  name: 'TableNormal',
  components: { DePagination },
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
    showSummary: {
      type: Boolean,
      required: false,
      default: true
    },
    enableScroll: {
      type: Boolean,
      required: false,
      default: true
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
      }
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
        '--scroll-bar-color': this.scrollBarColor
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
    // 监听元素变动事件
    eventBus.$on('resizing', this.chartResize)
  },
  beforeDestroy() {
    eventBus.$off('resizing', this.chartResize)
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
        this.fields = fields
        const attr = JSON.parse(this.chart.customAttr)
        this.currentPage.pageSize = parseInt(attr.size.tablePageSize ? attr.size.tablePageSize : 20)

        // column width
        const containerWidth = this.$refs.tableContainer.offsetWidth
        const columnWidth = attr.size.tableColumnWidth ? attr.size.tableColumnWidth : this.columnWidth
        if (columnWidth < (containerWidth / this.fields.length)) {
          this.columnWidth = containerWidth / this.fields
        } else {
          this.columnWidth = columnWidth
        }

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
      if (this.chart.data.detailFields?.length) {
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

      this.$refs.plxTable.reloadData(data)
      this.$nextTick(() => {
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
      setTimeout(() => {
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
            // 有变更才刷新
            if (column.type === 'index' && column.visible !== customAttr.size.showIndex) {
              column.visible = customAttr.size.showIndex
              this.$refs.plxTable.refreshColumn()
              break
            }
          }
          if (!customAttr.size.indexLabel) {
            this.indexLabel = ' '
          } else {
            this.indexLabel = customAttr.size.indexLabel
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
        // 暂不支持斑马纹
        // if (customAttr.color.tableStripe) {
        //   // this.table_item_class_stripe.background = hexColorToRGBA(customAttr.color.tableItemBgColor, customAttr.color.alpha - 40)
        //   if (this.chart.customStyle) {
        //     const customStyle = JSON.parse(this.chart.customStyle)
        //     if (customStyle.background) {
        //       this.table_item_class_stripe.background = hexColorToRGBA(customStyle.background.color, customStyle.background.alpha)
        //     }
        //   }
        // }
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
      // 修改footer合计样式
      const table = document.getElementsByClassName(this.chart.id)
      for (let i = 0; i < table.length; i++) {
        const s_table = table[i].getElementsByClassName('elx-table--footer')
        let s = ''
        for (const i in this.table_header_class) {
          s += (i === 'fontSize' ? 'font-size' : i) + ':' + this.table_header_class[i] + ';'
        }
        for (let i = 0; i < s_table.length; i++) {
          s_table[i].setAttribute('style', s)
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
      if (this.chart.datasetMode === 0 && !NOT_SUPPORT_PAGE_DATASET.includes(this.chart.datasourceType)) {
        this.$emit('onPageChange', this.currentPage)
      } else {
        this.init()
      }
    },

    pageClick(val) {
      this.currentPage.page = val
      if (this.chart.datasetMode === 0 && !NOT_SUPPORT_PAGE_DATASET.includes(this.chart.datasourceType)) {
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
      setTimeout(() => {
        scrollContainer.scrollTo({
          top: this.scrollTop,
          behavior: this.scrollTop === 0 ? 'instant' : 'smooth'
        })
      }, 0)

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

          if (scrollContainer.clientHeight + scrollContainer.scrollTop < scrollContainer.scrollHeight) {
            this.scrollTop += top
          } else {
            this.scrollTop = 0
          }
          scrollContainer.scrollTo({
            top: this.scrollTop,
            behavior: this.scrollTop === 0 ? 'instant' : 'smooth'
          })
        }, senior.scrollCfg.interval)
      }
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
}

</style>
