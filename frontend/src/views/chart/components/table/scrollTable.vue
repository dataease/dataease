<template>
  <div ref="tableContainer" :style="bg_class" style="width: 100%;height: 100%;overflow: hidden;">
    <el-row style="height: 100%;padding-bottom:10px">
      <p v-show="title_show" ref="title" :style="title_class">{{ chart.title }}</p>
      <!-- <el-table class="hidden-tbody" style="width: 100%;margina-top: 15px;">
        <el-table-column v-for="(item,index) in fields" :key="index" :prop="item.datainsName" :label="item.name" />
      </el-table> -->
      <div class="table_new_header" :style="table_header_class">
        <div v-for="(item,index) in fields" :key="index" class="header_title">{{ item.name }}</div>
      </div>
      <div class="content">
        <ul id="infinite" ref="ulLis" class="bgHeightLight" :style="table_item_class" style="position: relative;">
          <el-popover
            v-model="isVisible"
            width="400"
            trigger="manual"
            :disabled="!isPopShow"
            :placement="popOpen.position"
            @show="popShow"
            @hide="popHide"
          >
            <p :style="pop_title" style="margin: 0px;position: relative;">
              <span>详情</span>
              <i class="el-icon-close" style="position: absolute;right: 0px;font-size: 20px;" @click="closePop" />
            </p>
            <el-row>
              <el-col v-for="(obj,num) in infoForm" :key="num" :style="pop_content">
                <el-col :span="8" style="text-align: right;">{{ obj.name }}：</el-col>
                <el-col :span="16">{{ obj.value }}</el-col>
              </el-col>
            </el-row>
            <div slot="reference" class="pop_position" :style="{left: popOpen.left,top: popOpen.top}" />
          </el-popover>

          <li v-for="(items,inde) in dataInfo" v-show="inde<=tableRowsNumber-1" :key="inde" :style="(numberLine === ''? inde === (highlight-1) : numberLine === inde) ? scrollId:newHeight" class="table_bode_li" @click.stop="showDialogInfo(items,inde)">
            <div v-for="(item,index) in fields" :key="index" class="body_info">
              <!-- {{ inde }} -->
              {{ items[item.datainsName] }}
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

import { save2Cache } from '@/api/chart/chart'
import { viewData } from '@/api/panel/panel'

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
      bannerLinkageKey: false,
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
        background: '#e8eaec',
        height: '36px',
        textAlign: 'left'
      },
      table_item_class: {
        fontSize: '12px',
        color: '#606266',
        background: '#ffffff',
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
        height: '30px',
        fontSize: 12
      },
      highlight: 2,
      bodyHeight: 30,
      rollingRate: 30,
      scrolleTime: 1000,
      heightLightLine: 3,
      oldData: null,
      newData: null,
      dialogVisible: false,
      infoForm: [],
      isPopShow: false,
      numberLine: '',
      tableRowsNumber: 5,
      popOpen: {
        position: '',
        left: '0px',
        top: '0px'
      },
      isVisible: false,
      pop_title: {
        textAlign: 'center',
        backgroundColor: '#082456',
        lineHeight: '30px',
        color: '#ffffff'
      },

      pop_content: {
        backgroundColor: '#1b2642',
        color: '#ffffff',
        lineHeight: '25px',
        borderBottomStyle: 'dashed',
        borderBottomWidth: '1px',
        borderBottomColor: '#ffffff'
      }
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
      style.height = this.bodyHeight + 'px'
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
    this.oldData = JSON.parse(JSON.stringify(this.chart))
    if (this.chart.data) {
      this.prossData()
      // this.tableScroll()
    }
  },
  destroyed() {
    clearInterval(this.timer)
  },
  methods: {
    popShow() {
      console.log('show')
      clearInterval(this.timer)
    },
    popHide() {
      console.log('hide', this.highlight)

      const keyObj = this.dataInfo[this.highlight - 1]
      const keyValue = []
      keyValue.push(keyObj[this.chart.data.fields[0].datainsName])
      if (this.bannerLinkageKey === true) {
        this.setCondition(keyValue)
      }
      this.numberLine = ''
      this.tableScroll()
      const datas = JSON.parse(JSON.stringify(this.oldData))
      datas.drillFields = JSON.stringify(datas.drillFields)
      datas.drillFilters = JSON.stringify(datas.drillFilters)
      delete datas.data
      save2Cache(this.newData.sceneId, datas)
    },
    handleClose() {
      this.dialogVisible = false
      this.popHide()
    },
    showDialogInfo(info, num) {
      console.log('-----', 1111, info, num)
      //
      const keyObj = this.dataInfo[num]
      const keyValue = []
      keyValue.push(keyObj[this.chart.data.fields[0].datainsName])
      if (this.bannerLinkageKey === true) {
        this.setCondition(keyValue)
      }

      //

      if (!this.isPopShow) {
        return
      }
      console.log(num)
      this.numberLine = num
      this.newData = JSON.parse(JSON.stringify(this.chart))
      console.log('newdata', this.newData)
      let drillList = []
      if (typeof this.newData.drillFields === 'object') {
        drillList = JSON.parse(JSON.stringify(this.newData.drillFields))
      } else if (typeof this.newData.drillFields === 'string') {
        drillList = JSON.parse(this.newData.drillFields)
      }
      let xaxisList = []
      if (typeof this.newData.xaxis === 'object') {
        xaxisList = JSON.parse(JSON.stringify(this.newData.xaxis))
      } else if (typeof this.newData.xaxis === 'string') {
        xaxisList = JSON.parse(this.newData.xaxis)
      }
      drillList.map(item => {
        xaxisList.push(item)
      })
      console.log(xaxisList)
      this.newData.xaxis = JSON.stringify(xaxisList)
      this.newData.drillFields = '[]'
      this.newData.drillFilters = '[]'
      delete this.newData.data
      console.log(this.newData)
      const obj = {
        cache: false,
        drill: [],
        filter: [],
        linkageFilter: [],
        outerParamsFilters: undefined,
        queryFrom: 'panel_edit',
        resultCount: 1000,
        resultMode: 'all'
      }
      // 缓存对组件的数据维度进行处理的操作为了之后查询的数据
      save2Cache(this.newData.sceneId, this.newData).then(() => {
        viewData(this.newData.id, this.newData.sceneId, obj).then(res => {
          console.log('response', res)
          const data = res.data.data
          const fields = data.fields
          const tableRow = []
          data.tableRow.map((item, index) => {
            tableRow.push({
              ...item,
              isClick: index
            })
          })
          // console.log(info,fields,tableRow)
          let obj = {}
          tableRow.map(item => {
            if (item.isClick === info.isClick) {
              obj = item
            }
          })
          // console.log(obj)
          const arr = []
          for (const k in obj) {
            const a = k
            fields.map((item, index) => {
              if (a === item.datainsName) {
                arr.push({
                  name: item.name,
                  value: obj[a],
                  num: index
                })
              }
            })
          }
          console.log(arr)
          const hash = {}
          const arr1 = arr.reduceRight((item, next) => {
            hash[next.name] ? '' : hash[next.name] = true && item.push(next)
            return item
          }, [])
          this.infoForm = arr1.sort((a, b) => {
            return a.num - b.num
          })
          console.log(this.infoForm)

          this.isVisible = true
          // this.dialogVisible = true
          // this.popShow()
        })
      })

      // console.log('行----信息', info, this.fields)
      // // this.dialogVisible = true
      // let arr = []
      // for(let k in info) {
      //   let a = k
      //   this.fields.map(item => {
      //     if(a === item.datainsName) {
      //       arr.push({
      //         name: item.name,
      //         value: info[a]
      //       })
      //     }
      //   })
      // }
      // console.log('arr...',arr)

      // this.infoForm = arr
    },
    closePop() {
      this.isVisible = false
    },
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
        // console.log('轮播表格2222', this.chart.data, this.element)
        this.element.options = { attrs: {
          fieldId: this.chart.data.fields[0].id,
          viewIds: []
        }, manualModify: true }
        const data = this.dataInfo[0]
        setTimeout(() => {
          this.dataInfo.splice(0, 1)
        }, 500)
        setTimeout(() => {
          this.dataInfo.push(data)
        }, 500)
        // console.log('存储数据', this.dataInfo[3])
        const keyObj = this.dataInfo[this.highlight]
        // const objArr = []
        // for (const key in keyObj) {
        //   console.log('数据', key, keyObj[key])
        //   objArr.push(keyObj[key])
        // }
        // console.log('objArr', objArr)
        const keyValue = []
        // let keys = this.chart.data.fields[0].datainsName
        keyValue.push(keyObj[this.chart.data.fields[0].datainsName])
        // console.log('keyValue', keyValue)
        if (this.bannerLinkageKey === true) {
          this.setCondition(keyValue)
        }
      }, this.scrolleTime) // 滚动速度
    },
    setCondition(key) {
      const param = {
        component: this.element,
        value: key,
        operator: 'eq'
      }
      this.$store.commit('addViewFilter', param)
    },
    prossData() {
      this.fields = JSON.parse(JSON.stringify(this.chart.data.fields))

      const arr = []
      this.chart.data.tableRow.map((item, index) => {
        arr.push({
          ...item,
          isClick: index
        })
      })
      // this.dataInfo = JSON.parse(JSON.stringify(this.chart.data.tableRow))
      this.dataInfo = arr
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
          this.table_item_class.background = hexColorToRGBA(customAttr.color.tableItemBgColor, customAttr.color.alpha)
          this.scrollId.backgroundColor = customAttr.color.tableHeightColor
          this.scrollId.color = customAttr.color.tableHeightFontColor
        }
        if (customAttr.size) {
          this.table_header_class.textAlign = customAttr.size.tableHeaderAlign
          if (customAttr.size.bannerLinkage || customAttr.size.bannerLinkage === false) {
            this.bannerLinkageKey = customAttr.size.bannerLinkage
          }
          // this.table_header_class.fontSize = ((customAttr.size.tableTitleFontSize - 4) * this.previewCanvasScale.scalePointWidth) + 'px'
          // this.table_item_class.fontSize = ((customAttr.size.tableItemFontSize - 4) * this.previewCanvasScale.scalePointWidth) + 'px'
          this.table_header_class.fontSize = (customAttr.size.tableTitleFontSize * this.previewCanvasScale.scalePointWidth) + 'px'
          this.table_item_class.fontSize = (customAttr.size.tableItemFontSize * this.previewCanvasScale.scalePointWidth) + 'px'
          this.table_header_class.height = customAttr.size.tableTitleHeight + 'px'
          this.highlight = customAttr.size.highlightNumber ? customAttr.size.highlightNumber : 2
          this.tableRowsNumber = customAttr.size.tableRowsNumber ? customAttr.size.tableRowsNumber : 5
          // this.scrollId.fontSize = (Math.ceil(+customAttr.size.heightLightFontSize * this.previewCanvasScale.scalePointWidth)) + 'px'
          console.log('customAttr.size.heightLightFontSize', customAttr.size.heightLightFontSize, this.previewCanvasScale.scalePointWidth)
          this.scrollId.fontSize = (+customAttr.size.heightLightFontSize * this.previewCanvasScale.scalePointWidth) + 'px'
          this.setStyle.top = (customAttr.size.tableItemHeight) + 'px'
          this.setStyle.height = customAttr.size.tableItemHeight + 'px'
          this.rollingRate = customAttr.size.tableRollingRate
          console.log('customAttr.size.tableItemHeight', customAttr.size.tableItemHeight)
          this.bodyHeight = customAttr.size.tableItemHeight
          this.scrollId.height = customAttr.size.tableItemHeight + 'px'
          this.table_item_class.textAlign = customAttr.size.tableItemAlign
          this.scrolleTime = customAttr.size.automaticTime
        }
        if (customAttr.label) {
          console.log('label数据，，，，，', customAttr.label)
          this.isPopShow = customAttr.label.popShow
          this.popOpen.position = customAttr.label.popOpen
          this.popOpen.left = customAttr.label.popLeft !== undefined ? customAttr.label.popLeft + 'px' : '0px'
          this.popOpen.top = customAttr.label.popTop !== undefined ? customAttr.label.popTop + 'px' : '0px'
          this.pop_title.color = customAttr.label.popTitleColor
          this.pop_title.backgroundColor = customAttr.label.popTitleBackground
          this.pop_title.textAlign = customAttr.label.popPosition
          this.pop_title.lineHeight = customAttr.label.popHeight + 'px'
          this.pop_content.color = customAttr.label.popContentColor
          this.pop_content.backgroundColor = customAttr.label.popContentBackground
          this.pop_content.lineHeight = customAttr.label.popContentHeight + 'px'
          // this.pop_content.borderBottomStyle = customAttr.label.popContentBorderBottomStyle
          // this.pop_content.borderBottomWidth = customAttr.label.popContentBorderBottomWidth + 'px'
          this.pop_content.borderBottomColor = customAttr.label.popContentBorderBottomColor
        }
        this.table_item_class_stripe = JSON.parse(JSON.stringify(this.table_item_class))
        this.tableScroll()
      }
      if (this.chart.customStyle) {
        const customStyle = JSON.parse(this.chart.customStyle)
        if (customStyle.text) {
          this.title_show = customStyle.text.show
          this.title_class.fontSize = (customStyle.text.fontSize * this.previewCanvasScale.scalePointWidth) + 'px'
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

.pop_position {
  width: 3%;
  height: 3%;
  position: absolute;
  pointer-events: none;
  z-index: 0;
}

.table_new_header{
  display:flex;
  align-items:center;
  .header_title{
    flex:1
  }
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
  padding:0;
  margin: 0;
    // background-image: linear-gradient(rgb(6 26 85), rgba(6, 26, 85, 0), rgb(6 26 85));
}
.table_bode_li{
  display:flex;
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
