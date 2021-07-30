<template>
  <div ref="tableContainer" :style="bg_class" style="padding: 8px;">
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
        :key="field.dataeaseName"
        :field="field.dataeaseName"
        :resizable="true"
        sortable
        :title="field.name"
      >
        <!--        <template slot="header">-->
        <!--          <span>{{ field.name }}</span>-->
        <!--        </template>-->
      </ux-table-column>
    </ux-grid>
  </div>
</template>

<script>
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
        fontWeight: 'normal'
      },
      bg_class: {
        background: hexColorToRGBA('#ffffff', 0)
      },
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
      title_show: true
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
    eventBus.$on('resizing', (componentId) => {
      this.chartResize()
    })
  },
  methods: {
    init() {
      this.resetHeight()
      this.$nextTick(() => {
        this.initData()
        this.calcHeightDelay()
      })
    },
    initData() {
      const that = this
      let datas = []
      if (this.chart.data) {
        this.fields = JSON.parse(JSON.stringify(this.chart.data.fields))
        datas = JSON.parse(JSON.stringify(this.chart.data.tableRow))
      } else {
        this.fields = []
        datas = []
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
          const currentHeight = this.$refs.tableContainer.offsetHeight
          const tableMaxHeight = currentHeight - this.$refs.title.offsetHeight - 16
          let tableHeight
          if (this.chart.data) {
            tableHeight = (this.chart.data.tableRow.length + 2) * 36
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
          this.table_item_class.color = customAttr.color.tableFontColor
          this.table_item_class.background = hexColorToRGBA(customAttr.color.tableItemBgColor, customAttr.color.alpha)
        }
        if (customAttr.size) {
          this.table_header_class.fontSize = customAttr.size.tableTitleFontSize + 'px'
          this.table_item_class.fontSize = customAttr.size.tableItemFontSize + 'px'
          this.table_header_class.height = customAttr.size.tableTitleHeight + 'px'
          this.table_item_class.height = customAttr.size.tableItemHeight + 'px'
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
        if (columnIndex === 0) {
          means.push('合计')
        } else {
          if (columnIndex >= that.chart.data.fields.length - that.chart.data.series.length) {
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
</style>
