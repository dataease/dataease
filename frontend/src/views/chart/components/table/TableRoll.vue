<template>
  <div ref="tableContainer" :style="bg_class" style="width: 100%;height: 100%;overflow: hidden;">
    <el-row style="height: 100%;padding-bottom:10px">
      <p v-show="title_show" ref="title" :style="title_class">{{ chart.title }}</p>
      <div class="content">
        <ul id="infinite" ref="ulLis" class="item bgHeightLight" :style="table_item_class">
          <li v-for="(items,inde) in dataInfo" :key="inde" :style="newHeight" class="table_bode_li">
            <div :style="table_header_class">
              <div v-for="(item,index) in fields" :key="index" :style="inde == 2?scrollId:''" class="header_title">
                <div :style="haderStyle" class="tite_left">{{ item.name }} :</div>{{ items[item.datainsName] }}
                <!-- <el-row>
                  <el-col :span="2"><el-col>
                    <el-col :span="2"><el-col>
                </el-row> -->
              </div>
            </div>
          </li>
        </ul>
      </div>

    </el-row>
  </div>
</template>

<script>
import { hexColorToRGBA } from '../../chart/util'
import { mapState } from 'vuex'
import vueSeamlessScroll from 'vue-seamless-scroll'
import eventBus from '@/components/canvas/utils/eventBus'

export default {
  name: 'TableNormal',
  components: {
    vueSeamlessScroll
  },
  props: {
    element: {
      type: Object,
      required: true
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
    showSummary: {
      type: Boolean,
      required: false,
      default: true
    }
  },
  data() {
    return {
      fields: [],
      timer: null,
      info: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
      dataInfo: [],
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
        background: '#fff',
        // height: '36px',
        textAlign: 'left'
      },
      table_item_class: {
        fontSize: '12px',
        color: '#606266',
        // background: '#ffffff',
        // height: '36px',a
        textAlign: 'left'
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
      setStyle: {
        opacity: 1,
        backgroundColor: '#fff',
        top: '40px',
        height: '40px'
      },
      scrollId: {
        color: '#333',
        backgroundColor: '#fff',
        opacity: 1,
        // height: '30px',
        fontSize: 12
        // marginBottom: '10px'
      },
      bodyHeight: 30,
      rollingRate: 30,
      scrolleTime: 1000,
      heightLightLine: 3,
      heightWidth: 80
    }
  },
  computed: {
    ...mapState([
      'curComponent',
      'componentData',
      'canvasStyleData',
      'previewCanvasScale'
    ]),
    newHeight() {
      const style = {}
      // style.height = this.bodyHeight + 'px'
      style.marginBottom = this.bodyHeight + 'px'
      return style
    },
    haderStyle() {
      const style = {}
      // style.height = this.bodyHeight + 'px'
      style.width = this.heightWidth + 'px'
      return style
    },
    classOption() {
      return {
        // 滚动速度
        step: this.rollingRate / 100,
        // 鼠标悬停停止滚动
        hoverStop: true,
        // 滚动组数
        limitMoveNum: 5,
        singleHeight: this.bodyHeight, // 单行停顿
        // 监听刷新
        openWatch: true
      }
    },
    bg_class() {
      return {
        background: hexColorToRGBA('#ffffff', 0),
        borderRadius: this.borderRadius
      }
    }
  },
  watch: {
    chart: function() {
      console.log('this.chart.data----------！！！！！！', this.chart.data)
      console.log('this.chart.data----------2222', this.chart)

      if (this.chart.data) {
        clearInterval(this.timer)
        this.prossData()
      } else {
        this.fields = []
        this.dataInfo = []
        clearInterval(this.timer)
      }
    }
  },
  mounted() {
    console.log('this.fields', this.fields)
    console.log('获取边框数据', this.element)
    console.log('this.chart---', this.chart)
    if (this.chart.data) {
      this.prossData()
      // this.tableScroll()
    }
  },
  destroyed() {
    clearInterval(this.timer)
  },
  methods: {
    scorllEvent() {
      var isScroll = true // 也可以定义到data里
      this.$nextTick(() => {
        const div = document.getElementsByClassName('el-table__body-wrapper')[0]
        div.style.height = '110px'
        div.addEventListener('mouseenter', () => {
          isScroll = false
        })
        div.addEventListener('mouseleave', () => {
          isScroll = true
        })
        const t = document.getElementByClassName('el-table__body')[0]
        setInterval(() => {
          if (isScroll) {
            const data = this.dataInfo[0]
            setTimeout(() => {
              this.dataInfo.push(data)
              t.style.transition = 'all .5s'
              t.style.marginTop = '-41px'
            }, 500)
            setTimeout(() => {
              this.dataInfo.splice(0, 1)
              t.style.transition = 'all 0s ease 0s'
              t.style.marginTop = '0'
            }, 1000)
          }
        }, 2500)
      })
    },
    tableScroll() {
      this.timer = setInterval(() => {
        console.log('2222')
        const data = this.dataInfo[0]
        setTimeout(() => {
          this.dataInfo.splice(0, 1)
        }, 500)
        setTimeout(() => {
          this.dataInfo.push(data)
        }, 500)
      }, this.scrolleTime) // 滚动速度
    },
    prossData() {
      this.fields = JSON.parse(JSON.stringify(this.chart.data.fields))
      this.dataInfo = JSON.parse(JSON.stringify(this.chart.data.tableRow))
      console.log('有数据才会去执行操作---------', this.dataInfo)
      // this.initStyle()

      this.$nextTick(() => {
        this.initStyle()
      })
    },
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
      // this.$refs.plxTable.reloadData(datas)
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
        console.log('是否触发此处修改------------2222222', customAttr)
        if (customAttr.color) {
          this.table_header_class.color = customAttr.color.tableFontColor
          this.table_header_class.background = hexColorToRGBA(customAttr.color.tableHeaderBgColor, customAttr.color.alpha)
          this.table_item_class.color = customAttr.color.tableInfoFontColor
          // this.table_item_class.background = hexColorToRGBA(customAttr.color.tableItemBgColor, customAttr.color.alpha)
          this.scrollId.backgroundColor = hexColorToRGBA(customAttr.color.tableHeightColor, customAttr.color.alpha)
          this.scrollId.color = customAttr.color.tableHeightFontColor
        }
        if (customAttr.size) {
          this.table_header_class.textAlign = customAttr.size.tableHeaderAlign
          // this.table_header_class.fontSize = ((customAttr.size.tableTitleFontSize - 4) * this.previewCanvasScale.scalePointWidth) + 'px'

          this.heightWidth = (Math.ceil(+customAttr.size.tableHeightWidth * this.previewCanvasScale.scalePointWidth) + 5)

          // this.table_item_class.fontSize = ((customAttr.size.tableItemFontSize - 4) * this.previewCanvasScale.scalePointWidth) + 'px'
          this.table_header_class.fontSize = customAttr.size.tableTitleFontSize + 'px'
          this.table_item_class.fontSize = customAttr.size.tableItemFontSize + 'px'
          // this.table_header_class.height = customAttr.size.tableTitleHeight + 'px'
          this.scrollId.fontSize = customAttr.size.tableItemFontSize + 'px'
          // this.scrollId.marginBottom = customAttr.size.tableSpacing + 'px'
          this.setStyle.top = (customAttr.size.tableItemHeight) + 'px'
          this.setStyle.height = customAttr.size.tableItemHeight + 'px'
          this.rollingRate = customAttr.size.tableRollingRate
          console.log('customAttr.size.tableItemHeight', customAttr.size.tableItemHeight)
          this.bodyHeight = customAttr.size.tableSpacing
          // this.scrollId.height = customAttr.size.tableItemHeight + 'px'
          this.table_item_class.textAlign = customAttr.size.tableItemAlign
          this.scrolleTime = customAttr.size.automaticTime
        }
        this.table_item_class_stripe = JSON.parse(JSON.stringify(this.table_item_class))
        this.tableScroll()
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
      console.log('this.scrollId', this.scrollId, this.table_item_class)
      // 修改footer合计样式
      // const table = document.getElementsByClassName(this.chart.id)
      // for (let i = 0; i < table.length; i++) {
      //   const s_table = table[i].getElementsByClassName('elx-table--footer')
      //   // console.log(s_table)
      //   let s = ''
      //   for (const i in this.table_header_class) {
      //     s += (i === 'fontSize' ? 'font-size' : i) + ':' + this.table_header_class[i] + ';'
      //   }
      //   // console.log(s_table)
      //   for (let i = 0; i < s_table.length; i++) {
      //     s_table[i].setAttribute('style', s)
      //   }
      // }
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

<style lang="scss" scoped>
.table_new_header{
  display:flex;
  align-items:center;

}
.header_title{
    display:flex;
  }
  .heade_clsass{
    min-width:120px;
  }
  .tite_left{
    padding-left:5px;
  }
#scrollId{
  // background:#f99;
  font-weight: 600;
  color:#f99;
}
.content{
  position:relative;
}
.purchaseActive{
  position:absolute;
  top:40%;
  width:100%;
  height:30px;
  background-color:rgba(153,153,153);

  // z-index:10;
    // rgba(153,153,153,0.9) 0%,
    // rgba(153,153,153,0.8) 5%,
    // rgba(153,153,153,0.7) 10%,
    // rgba(153,153,153,0.6) 15%,
    // rgba(153,153,153,0.5) 20%,
    // rgba(153,153,153,0.4) 25%,
    // rgba(153,153,153,0.3) 30%,
    // rgba(153,153,153,0.2) 35%,
    // rgba(153,153,153,0.1) 40%,
    // rgba(153,153,153,0)  45%,
    // rgba(153,153,153,0) 50%,
    // rgba(153,153,153,0) 55%,
    // rgba(153,153,153,0.1) 60%,
    // rgba(153,153,153,0.2) 65%,
    // rgba(153,153,153,0.3) 70%,
    // rgba(153,153,153,0.4) 75%,
    // rgba(153,153,153,0.5) 80%,
    // rgba(153,153,153,0.6)  85%,
    // rgba(153,153,153,0.7)  90%,
    // rgba(153,153,153,0.8)  95%,
    // rgba(153,153,153,0.9) 100%,
  // background-image: linear-gradient(
  //   to bottom,
  //   rgba(153,153,153,0) 0%,
  //   rgba(153,153,153,0) 5%,
  //   rgba(153,153,153,0) 10%,
  //   rgba(153,153,153,0) 15%,
  //   rgba(153,153,153,0) 20%,
  //   rgba(153,153,153,0) 25%,
  //   rgba(153,153,153,0) 30%,
  //   rgba(153,153,153,0) 35%,
  //   rgba(153,153,153,0) 40%,
  //   rgba(153,153,153,1) 45%,
  //   rgba(153,153,153,1) 50%,
  //   rgba(153,153,153,1) 55%,
  //   rgba(153,153,153,0) 60%,
  //   rgba(153,153,153,0) 65%,
  //   rgba(153,153,153,0) 70%,
  //   rgba(153,153,153,0) 75%,
  //   rgba(153,153,153,0) 80%,
  //   rgba(153,153,153,0)  85%,
  //   rgba(153,153,153,0)  90%,
  //   rgba(153,153,153,0)  95%,
  //   rgba(153,153,153,0) 100%,
  // );
}
.table_body_class{
  // display:flex;
}
.bgHeightLight{
    // background-image: linear-gradient(rgb(6 26 85), rgba(6, 26, 85, 0), rgb(6 26 85));
}
.table_bode_li{
  // display:flex;
  align-items:center;
}
.body_info{
  flex:1;
  white-space: nowrap;
	overflow: hidden;
  text-overflow: ellipsis;

  // .child{

	// }
}
 .hidden-tbody.el-table {
    height: 34px;
    box-sizing: border-box;
    tbody { //隐藏上面表格的tbody
      display: none;
      overflow: hidden;
    }
  }
.hidden-thead.el-table {
    border-top: none; //防止边框重叠
    thead { //隐藏下面表格的thead
      display: none;
      overflow: hidden;
    }
  }
 ::v-deep .hidden-thead{
     .el-table__header-wrapper{
        display:none
      }
  }

  //   .hidden-thead.el-table {
  //   border-top: none; //防止边框重叠
  //   thead { //隐藏下面表格的thead
  //     display: none;
  //     overflow: hidden;
  //   }
  // }
  .mytable_header .el-table__empty-block{
    display: none;
  }
  .mytable_header_no .has-gutter{
    display: none;
  }

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
