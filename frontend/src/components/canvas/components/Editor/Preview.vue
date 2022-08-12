<template>
  <div id="vaeryBigBox" class="bg" :style="customStyle" @scroll="canvasScroll">
    <!-- <div id="vaeryBigBox" class="bg" style="width:100%;height:100%" @scroll="canvasScroll"> -->
    <div id="canvasInfoMain" ref="canvasInfoMain" :style="canvasInfoMainStyle">
      <!-- <div>数据问题</div> -->
      <div
        id="canvasInfoTemp"
        ref="canvasInfoTemp"
        :style="[canvasInfoTempStyle,screenShotStyle]"
        class="main-class"
        @mouseup="deselectCurComponent"
        @mousedown="handleMouseDown"
      >
        <!-- <el-row v-if="shareKey" class="custom-position">
          2121111111111111111111111
        </el-row> -->
        <el-row v-if="componentDataShow.length===0" class="custom-position">
          <span v-if="shareKey">{{ $t('panel.panelLoading') }} </span>
          <span v-else>{{ $t('panel.panelNull') }}</span>

        </el-row>
        <canvas-opt-bar />
        <!-- v-if="exhibition(item)" -->
        <!-- display:displayClass(item) -->
        <ComponentWrapper
          v-for="(item, index) in componentDataInfo"

          :key="index"
          :style="{opacity:opacityClass(item),visibility:displayClass(item)}"
          :config="item"
          :search-count="searchCount"
          :in-screen="inScreen"
          :terminal="terminal"
          :filters="filterMap[item.propValue && item.propValue.viewId]"
          :screen-shot="screenShot"
        />
        <!--视图详情-->
        <el-dialog
          :title="'['+showChartInfo.name+']'+$t('chart.chart_details')"
          :visible.sync="chartDetailsVisible"
          width="70%"
          class="dialog-css"
          :destroy-on-close="true"
        >
          <span style="position: absolute;right: 70px;top:15px">
            <el-button size="mini" @click="exportExcel">
              <svg-icon icon-class="ds-excel" class="ds-icon-excel" />
              {{ $t('chart.export_details') }}
            </el-button>
          </span>
          <UserViewDialog ref="userViewDialog" :chart="showChartInfo" :chart-table="showChartTableInfo" />
        </el-dialog>

        <!--手机视图详情-->
        <el-dialog
          :visible.sync="mobileChartDetailsVisible"
          :fullscreen="true"
          class="mobile-dialog-css"
          :destroy-on-close="true"
        >
          <UserViewMobileDialog :chart="showChartInfo" :chart-table="showChartTableInfo" />
        </el-dialog>
      </div>
    </div>
  </div>
</template>

<script>
import { getStyle } from '@/components/canvas/utils/style'
import { mapState } from 'vuex'
import ComponentWrapper from './ComponentWrapper'
import { changeStyleWithScale } from '@/components/canvas/utils/translate'
import { uuid } from 'vue-uuid'
import { deepCopy } from '@/components/canvas/utils/utils'
import eventBus from '@/components/canvas/utils/eventBus'
import elementResizeDetectorMaker from 'element-resize-detector'
import UserViewDialog from '@/components/canvas/custom-component/UserViewDialog'
import CanvasOptBar from '@/components/canvas/components/Editor/CanvasOptBar'
import UserViewMobileDialog from '@/components/canvas/custom-component/UserViewMobileDialog'
import bus from '@/utils/bus'
import { buildFilterMap } from '@/utils/conditionUtil'
export default {
  components: { UserViewMobileDialog, ComponentWrapper, UserViewDialog, CanvasOptBar },
  model: {
    prop: 'show',
    event: 'change'
  },
  props: {
    shareKey: {
      type: Boolean,
      default: false
    },
    // 后端截图
    backScreenShot: {
      type: Boolean,
      default: false
    },
    screenShot: {
      type: Boolean,
      default: false
    },
    show: {
      type: Boolean,
      default: false
    },
    showType: {
      type: String,
      required: false,
      default: 'full'
    },
    inScreen: {
      type: Boolean,
      required: false,
      default: true
    }
  },
  data() {
    return {
      isShowPreview: false,
      panelId: '',
      needToChangeHeight: [
        'top',
        'height'
      ],
      needToChangeWidth: [
        'left',
        'width',
        'fontSize',
        'borderWidth',
        'letterSpacing'
      ],
      scaleWidth: '100',
      scaleHeight: '100',
      timer: null,
      componentDataShow: [],
      pageRatio: 0,
      mainWidth: '100%',
      mainHeight: '100%',
      searchCount: 0,
      chartDetailsVisible: false,
      mobileChartDetailsVisible: false,
      showChartInfo: {},
      showChartTableInfo: {},
      // 布局展示 1.pc pc端布局 2.mobile 移动端布局
      terminal: 'pc',
      offsetWidth: 1100,
      scaleSize: 1,
      onsizeKey: false
    }
  },
  created() {
  },
  computed: {
    exhibition() {
      return function(value) {
        console.log('value===', value)
        if (this.canvasStyleData.navShowKey && value.showName) {
          if (this.canvasStyleData.navShowKey === value.showName) {
            return true
          } else {
            return false
          }
        } else {
          return true
        }
      }
      // console.log('item', item, this.canvasStyleData)
      // console.log('this.showOrNot', this.showOrNot)
      // return true
    },
    opacityClass() {
      return function(value) {
        // console.log('value-----', value)
        if (value.showName) {
          if (this.canvasStyleData.showArr && value.navModel === 'elementKey') {
            if (this.canvasStyleData.showArr.includes(value.showName)) {
              return 1
            } else {
              return 0
            }
          } else {
            if (this.canvasStyleData.navShowKey === value.showName) {
              return 1
            } else {
              return 0
            }
          }
        } else {
          return 1
        }
      }
    },
    displayClass() {
      return function(value) {
        // console.log('value-----', value)
        // if (value.type === 'de-frame') {
        if (value.showName) {
          if (this.canvasStyleData.showArr && value.navModel === 'elementKey') {
            if (this.canvasStyleData.showArr.includes(value.showName)) {
              return 'visible'
            } else {
              return 'hidden'
            }
          } else {
            if (this.canvasStyleData.navShowKey === value.showName) {
              return 'visible'
            } else {
              return 'hidden'
            }
          }
        } else {
          return 'visible'
        }
        // } else {
        //   return 'visible'
        // }
      }
    },
    scaleNewHeight() {
      var height = 800
      // if (this.offsetWidth <= this.canvasStyleData.width) {
      //   height = this.canvasStyleData.height * (this.offsetWidth / this.canvasStyleData.width)
      // } else {
      //   height = this.canvasStyleData.height
      // }
      console.log('修改高度====')
      height = this.canvasStyleData.height * (this.offsetWidth / this.canvasStyleData.width)
      console.log('修改高度====', height)
      return height
    },
    canvasInfoMainStyle() {
      if (this.backScreenShot) {
        return {
          width: '100%',
          height: this.scaleNewHeight + 'px'
        }
      } else {
        return {
          width: '100%',
          height: this.scaleNewHeight + 'px'
        }
      }
    },
    canvasInfoTempStyle() {
      if (this.screenShot) {
        return {
          width: '100%',
          height: this.scaleNewHeight + 'px'
        }
      } else {
        return {
          width: '100%',
          height: this.scaleNewHeight + 'px'
        }
      }
    },
    showOrNot() {
      console.log('是否触发')
      return this.canvasStyleData
    },
    customStyle() {
      let style = {
        width: '100%'
      }
      // console.log('样式修改库=====', this.canvasStyleData)
      if (this.canvasStyleData.openCommonStyle) {
        if (this.canvasStyleData.panel.backgroundType === 'image' && this.canvasStyleData.panel.imageUrl) {
          style = {
            background: `url(${this.canvasStyleData.panel.imageUrl}) no-repeat`,
            ...style
          }
        } else if (this.canvasStyleData.panel.backgroundType === 'color') {
          style = {
            background: this.canvasStyleData.panel.color,
            ...style
          }
        }
      }
      if (this.backScreenShot) {
        style.height = this.mainHeight
      } else {
        // style.padding = '5px'
      }
      style.height = this.scaleNewHeight + 'px'
      style.fontFamily = this.canvasStyleData.fontFamily
      // console.log('改变的样式库===', style)
      return style
    },
    screenShotStyle() {
      return this.screenShot ? this.customStyle : {}
    },
    // 此处单独计算componentData的值 不放入全局mapState中
    componentDataInfo() {
      // console.log('this.componentDataShow', this.componentDataShow)
      // const style = this.componentDataShow.map(res => {
      //   console.log('res==', res)
      //   Object.keys(res.style).forEach(key => {
      //     console.log(key)
      //     if (this.needToChangeHeight.includes(key)) {
      //       res.style[key] = this.format(res.style[key], this.scaleHeight)
      //     }
      //     if (this.needToChangeWidth.includes(key)) {
      //       res.style[key] = this.format(res.style[key], this.scaleWidth)
      //     }
      //   })
      //   return res
      // })
      const style = this.componentDataShow
      // console.log('this.componentDataShow222222222222222', this.componentDataShow)

      return style
    },
    ...mapState([
      'isClickComponent',
      'curComponent',
      'componentData',
      'canvasStyleData',
      'componentGap'
    ]),
    filterMap() {
      const map = buildFilterMap(this.componentData)
      return map
    }
  },
  watch: {
    componentData: {
      handler(newVal, oldVla) {
        console.log('触发值的变化------')
        // this.restore()
        this.changeData()
      },
      deep: true
    },
    canvasStyleData: {
      handler(newVal, oldVla) {
        this.canvasStyleDataInit()
      },
      deep: true
    }
  },
  mounted() {
    this._isMobile()
    this.onsizeKey = this.inScreen
    const _this = this
    const erd = elementResizeDetectorMaker()
    this.offsetWidth = document.getElementById('canvasInfoMain').offsetWidth
    this.scaleSize = document.getElementById('canvasInfoMain').offsetWidth / this.canvasStyleData.width
    // this.$nextTick(() => {
    //   _this.restore()
    // })
    // if (!this.inScreen) {

    // }

    setTimeout(() => {
      _this.restore()
    })

    // console.log('放大会不会修改这个值---------------------------------')
    // 监听主div变动事件

    // 监听画布div变动事件
    const tempCanvas = document.getElementById('canvasInfoTemp')
    erd.listenTo(document.getElementById('canvasInfoTemp'), element => {
      // console.log('修改状态值')
      _this.$nextTick(() => {
        // 将mainHeight 修改为px 临时解决html2canvas 截图不全的问题
        _this.mainHeight = tempCanvas.scrollHeight + 'px!important'
        this.$emit('mainHeightChange', _this.mainHeight)
      })
    })
    eventBus.$on('openChartDetailsDialog', this.openChartDetailsDialog)
    _this.$store.commit('clearLinkageSettingInfo', false)
    _this.canvasStyleDataInit()
    // 如果当前终端设备是移动端，则进行移动端的布局设计
    // console.log('huoqude1shuju1===')
    if (_this.terminal === 'mobile') {
      _this.initMobileCanvas()
    }
  },
  beforeDestroy() {
    clearInterval(this.timer)
  },
  methods: {
    changeData() {
      this.componentDataShow.forEach((ele, index) => {
        this.componentData.forEach((item, inx) => {
          if (index === inx) {
            ele.filters = item.filters
          }
        })
      })
    },

    detectZoom() {
      var ratio = 0; var // 浏览器当前缩放比
        screen = window.screen; var // 获取屏幕
        ua = navigator.userAgent.toLowerCase()// 判断登陆端是pc还是手机

      if (window.devicePixelRatio !== undefined) {
        ratio = window.devicePixelRatio
      } else if (~ua.indexOf('msie')) {
        if (screen.deviceXDPI && screen.logicalXDPI) {
          ratio = screen.deviceXDPI / screen.logicalXDPI
        }
      } else if (window.outerWidth !== undefined && window.innerWidth !== undefined) {
        ratio = window.outerWidth / window.innerWidth
      }

      if (ratio) {
        ratio = Math.round(ratio * 100)
      }
      this.pageRatio = ratio
      // if (ratio !== 100) {
      //   this.$message({
      //     message: '您当前的窗口缩放比例为' + ratio + '%建议您的窗口比例调为100%，窗口比例不为100%可能导致页面排版错乱',
      //     type: 'warning'
      //   })
      //   // layer.msg('您当前的窗口缩放比例为' + ratio + '%建议您的窗口比例调为100%，窗口比例不为100%可能导致页面排版错乱')/// 这里layer是layui框架自带弹窗，如果不是layui可以使用alert（）代替；
      // }
    },
    _isMobile() {
      const flag = navigator.userAgent.match(/(phone|pad|pod|iPhone|iPod|ios|iPad|Android|Mobile|BlackBerry|IEMobile|MQQBrowser|JUC|Fennec|wOSBrowser|BrowserNG|WebOS|Symbian|Windows Phone)/i)
      this.terminal = flag ? 'mobile' : 'pc'
      // this.terminal = 'mobile'
    },
    canvasStyleDataInit() {
      // 数据刷新计时器
      this.searchCount = 0
      this.timer && clearInterval(this.timer)
      let refreshTime = 300000
      if (this.canvasStyleData.refreshTime && this.canvasStyleData.refreshTime > 0) {
        if (this.canvasStyleData.refreshUnit === 'second') {
          refreshTime = this.canvasStyleData.refreshTime * 1000
        } else {
          refreshTime = this.canvasStyleData.refreshTime * 60000
        }
      }
      this.timer = setInterval(() => {
        this.searchCount++
      }, refreshTime)
    },
    changeStyleWithScale,
    getStyle,
    restore() {
      this.offsetWidth = document.getElementById('canvasInfoMain').offsetWidth
      this.scaleSize = document.getElementById('canvasInfoMain').offsetWidth / this.canvasStyleData.width
      // console.log('计算宽高比----------', this.offsetWidth, this.canvasStyleData.width, document.getElementById('canvasInfoMain').offsetWidth / this.canvasStyleData.width)
      // const canvasHeight = document.getElementById('canvasInfoMain').offsetHeight
      const canvasWidth = document.getElementById('canvasInfoMain').offsetWidth
      this.scaleWidth = (canvasWidth) * 100 / this.canvasStyleData.width // 获取宽度比
      // this.offsetWidth = document.getElementById('canvasInfoMain').offsetWidth
      // this.scaleWidth = this.scaleSize * 100 // 获取宽度比
      // 如果是后端截图方式使用 的高度伸缩比例和宽度比例相同

      // console.log('获取的当前元素宽度', canvasWidth, this.scaleWidth, this.canvasStyleData)
      if (this.backScreenShot) {
        this.scaleHeight = this.scaleWidth * 100
      } else {
        // this.scaleHeight = canvasHeight * 100 / this.canvasStyleData.height// 获取高度比
        this.scaleHeight = this.scaleWidth// 获取高度比
        // this.scaleHeight = this.scaleSize// 获取高度比
      }
      // console.log('原代码中的宽高比例==', this.scaleHeight, this.scaleWidth)
      this.$store.commit('setPreviewCanvasScale', { scaleWidth: (this.scaleWidth / 100), scaleHeight: (this.scaleHeight / 100) })
      this.$nextTick(() => {
        this.handleScaleChange()
      })
    },
    resetID(data) {
      if (data) {
        data.forEach(item => {
          item.type !== 'custom' && (item.id = uuid.v1())
        })
      }
      return data
    },
    format(value, scale) {
      // console.log('value===', value, value * scale / 100, this.pageRatio)
      return (value * scale / 100)
    },
    handleScaleChange() {
      if (this.componentData) {
        const componentData = deepCopy(this.componentData)
        // console.log('componentData====', componentData)
        componentData.forEach(component => {
          // console.log('切割线===================================================')
          Object.keys(component.style).forEach(key => {
            if (this.needToChangeHeight.includes(key)) {
              component.style[key] = this.format(component.style[key], this.scaleHeight)
              // console.log('循环对象得到的key===', key, component.style[key])
            }
            if (this.needToChangeWidth.includes(key)) {
              if (key === 'fontSize' && this.terminal === 'mobile') {
                // do nothing 移动端字符大小无需按照比例缩放，当前保持不变(包括 v-text 和 过滤组件)
              } else {
                component.style[key] = this.format(component.style[key], this.scaleWidth)
              }
              // console.log('循环对象得到的key===222', key, component.style[key])
            }
          })
        })

        if (!this.onsizeKey) {
          setTimeout(() => {
            this.onsizeKey = true
          })
        }
        this.componentDataShow = componentData
        this.$nextTick(() => (eventBus.$emit('resizing', '')))
      }
      // this.scaleNewHeight = this.canvasStyleData.height * (document.getElementById('canvasInfoTemp').offsetWidth / this.canvasStyleData.width)
      // this.$nextTick(() => {
      //   this.offsetWidth = document.getElementById('canvasInfoMain').offsetWidth
      // })
    },
    openChartDetailsDialog(chartInfo) {
      this.showChartInfo = chartInfo.chart
      this.showChartTableInfo = chartInfo.tableChart
      if (this.terminal === 'pc') {
        this.chartDetailsVisible = true
      } else {
        this.mobileChartDetailsVisible = true
      }
    },
    exportExcel() {
      this.$refs['userViewDialog'].exportExcel()
    },
    deselectCurComponent(e) {
      if (!this.isClickComponent) {
        this.$store.commit('setCurComponent', { component: null, index: null })
      }
    },
    handleMouseDown() {
      this.$store.commit('setClickComponentStatus', false)
    },
    initMobileCanvas() {
      this.$store.commit('openMobileLayout')
    },
    canvasScroll() {
      bus.$emit('onScroll')
    }
  }
}
</script>

<style lang="scss" scoped>
  .bg {
    min-width: 200px;
    min-height: 300px;
    width: 100%;
    height: 100%;
    overflow-x: hidden;
    background-size: 100% 100% !important;
    // font-family:'楷体';
  }

  .main-class {
    width: 100%;
    height: 100%;
    background-size: 100% 100% !important;
  }

  .custom-position {
    height: 100%;
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    flex-flow: row nowrap;
    color: #9ea6b2;
  }

  .dialog-css > > > .el-dialog__title {
    font-size: 14px;
  }

  .dialog-css > > > .el-dialog__header {
    padding: 20px 20px 0;
  }

  .dialog-css > > > .el-dialog__body {
    padding: 10px 20px 20px;
  }

  .mobile-dialog-css > > > .el-dialog__headerbtn {
    top: 7px
  }

  .mobile-dialog-css > > > .el-dialog__body {
    padding: 0px;
  }
  ::-webkit-scrollbar {
    width: 0px!important;
    height: 0px!important;
  }

  ::v-deep .el-tabs__nav{
   z-index: 0;
  }

</style>
