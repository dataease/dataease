<template>
  <div ref="tableContainer" :style="bg_class" style="padding: 8px;width: 100%;height: 100%;overflow: hidden;">
    <el-row style="height: 100%;">
      <p v-show="title_show" ref="title" :style="title_class">{{ chart.title }}</p>
      <ux-grid
        ref="plxTable"
        size="mini"
        style="width: 100%;"
        :height="height"
        :checkbox-config="{highlight: true}"
        :width-resize="true"
        :header-row-style="table_header_class"
        :row-style="getRowStyle"
        class="table-class"
        :class="chart.id"
        :show-summary="showSummary"
        :summary-method="summaryMethod"
      >
        <ux-table-column
          v-for="field in fields"
          :key="Math.random()"
          :field="field.datainsName"
          :resizable="true"
          sortable
          :title="field.name"
        >
          <!--        <template slot="header">-->
          <!--          <span>{{ field.name }}</span>-->
          <!--        </template>-->
        </ux-table-column>
      </ux-grid>

      <el-row v-show="chart.type === 'table-info'" class="table-page">
        <!-- <span class="total-style">
          {{ $t('chart.total') }}
          <span>{{ (chart.data && chart.data.tableRow)?chart.data.tableRow.length:0 }}</span>
          {{ $t('chart.items') }}
        </span> -->
        <!-- <el-pagination
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
        /> -->
      </el-row>
    </el-row>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import { hexColorToRGBA } from '../../chart/util'
import eventBus from '@/components/canvas/utils/eventBus'

export default {
  name: 'TableNormal',
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
    }
  },
  data() {
    return {
      fields: [],
      height: 'auto',
      title_class: {
        margin: '0 0',
        width: '100%',
        fontSize: '18px',
        color: '#303133',
        textAlign: 'left',
        fontStyle: 'normal',
        fontWeight: 'normal',
        fontFamily: '',
      },
      //   bg_class: {
      //     background: hexColorToRGBA('#ffffff', 0),
      //     borderRadius: this.borderRadius
      //   },
      table_header_class: {
        fontSize: '12px',
        color: '#606266',
        background: '#e8eaec',
        height: '36px',
        fontFamily: '',
      },
      table_item_class: {
        fontSize: '12px',
        color: '#606266',
        background: '#ffffff',
        height: '36px',
        fontFamily: '',
      },
      table_item_class_stripe: {
        fontSize: '12px',
        color: '#606266',
        background: '#ffffff',
        height: '36px',
        fontFamily: '',
      },
      title_show: true,
      borderRadius: '0px',
      currentPage: {
        page: 1,
        pageSize: 20,
        show: 0
      }
    }
  },
  computed: {
    bg_class() {
      return {
        background: hexColorToRGBA('#ffffff', 0),
        borderRadius: this.borderRadius
      }
    },
    ...mapState([
      'canvasStyleData',
    ])
  },
  watch: {
    chart: function() {
      console.log('this.chart.data', this.chart.data)
      this.resetPage()
      this.init()
    }
  },
  mounted() {
    this.init()
    // 监听元素变动事件
    eventBus.$on('resizing', (componentId) => {
      this.chartResize()
    })
  },
  methods: {
    changeColumnWidth({ column, columnIndex }) {
      console.log('23123213213231232132121', column, columnIndex)
      // if (column.width !== column.renderWidth) {
      //   this.tableHeadList[columnIndex - 2].width = column.renderWidth
      //   // this.saveHeadConfig()
      // }
    },
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
      const that = this
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
      this.$refs.plxTable.reloadData(datas)
      this.$nextTick(() => {
        this.initStyle()
      })
      window.onresize = function() {
        that.calcHeightDelay()
      }
    },
    calcHeightRightNow() {
      this.$nextTick(() => {
        if (this.$refs.tableContainer) {
          let pageHeight = 0
          if (this.chart.type === 'table-info') {
            pageHeight = 36
          }
          const currentHeight = this.$refs.tableContainer.offsetHeight
          const tableMaxHeight = currentHeight - this.$refs.title.offsetHeight - 16 - pageHeight
          let tableHeight
          if (this.chart.data) {
            if (this.chart.type === 'table-info') {
              tableHeight = (this.currentPage.pageSize + 2) * 36 - pageHeight
            } else {
              tableHeight = (this.chart.data.tableRow.length + 2) * 36 - pageHeight
            }
          } else {
            tableHeight = 0
          }
          if (tableHeight > tableMaxHeight) {
            this.height = tableMaxHeight + 'px'
          } else {
            this.height = 'auto'
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
          this.table_header_class.color = customAttr.color.tableFontColor
          this.table_header_class.background = hexColorToRGBA(customAttr.color.tableHeaderBgColor, customAttr.color.alpha)
          this.table_header_class.fontFamily = this.canvasStyleData.fontFamily? this.canvasStyleData.fontFamily : ''
          this.table_item_class.color = customAttr.color.tableFontColor
          this.table_item_class.background = hexColorToRGBA(customAttr.color.tableItemBgColor, customAttr.color.alpha)
          this.table_item_class.fontFamily = this.canvasStyleData.fontFamily? this.canvasStyleData.fontFamily : ''
        }
        if (customAttr.size) {
          this.table_header_class.fontSize = customAttr.size.tableTitleFontSize + 'px'
          this.table_item_class.fontSize = customAttr.size.tableItemFontSize + 'px'
          this.table_header_class.height = customAttr.size.tableTitleHeight + 'px'
          this.table_item_class.height = customAttr.size.tableItemHeight + 'px'
          this.table_header_class.fontFamily = this.canvasStyleData.fontFamily? this.canvasStyleData.fontFamily : ''
          this.table_item_class.fontFamily = this.canvasStyleData.fontFamily? this.canvasStyleData.fontFamily : ''
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
        console.log('TableNormal...style',customStyle)
        if (customStyle.text) {
          this.title_show = customStyle.text.show
          this.title_class.fontSize = customStyle.text.fontSize + 'px'
          this.title_class.color = customStyle.text.color
          this.title_class.textAlign = customStyle.text.hPosition
          this.title_class.fontStyle = customStyle.text.isItalic ? 'italic' : 'normal'
          this.title_class.fontWeight = customStyle.text.isBolder ? 'bold' : 'normal'
          this.title_class.fontFamily = customStyle.text.fontFamily? customStyle.text.fontFamily : this.canvasStyleData.fontFamily? this.canvasStyleData.fontFamily : ''
        }
        if (customStyle.background) {
          this.bg_class.background = hexColorToRGBA(customStyle.background.color, customStyle.background.alpha)
        }
      }
      // 修改footer合计样式
      const table = document.getElementsByClassName(this.chart.id)
      for (let i = 0; i < table.length; i++) {
        const s_table = table[i].getElementsByClassName('elx-table--footer')
        // console.log(s_table)
        let s = ''
        for (const i in this.table_header_class) {
          s += (i === 'fontSize' ? 'font-size' : i) + ':' + this.table_header_class[i] + ';'
        }
        // console.log(s_table)
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
      columns.forEach((column, columnIndex) => {
        const x = JSON.parse(that.chart.xaxis)
        if (columnIndex === 0 && x.length > 0) {
          means.push('合计')
        } else {
          if (columnIndex >= x.length) {
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
      this.init()
    },

    pageClick(val) {
      this.currentPage.page = val
      this.init()
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

<style scoped>
  .table-class>>>.body--wrapper{
    background: rgba(1,1,1,0);
  }
  .table-class>>>.elx-cell{
    max-height: none!important;
    line-height: normal!important;
  }
  .table-page{
    position: absolute;
    bottom: 0;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    width: 100%;
    overflow: hidden;
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
