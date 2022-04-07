<template>
  <div class="bg" :style="customStyle" @scroll="canvasScroll">
    <div id="canvasInfoMain" ref="canvasInfoMain" :style="canvasInfoMainStyle">
      <div
        id="canvasInfoTemp"
        ref="canvasInfoTemp"
        :style="[canvasInfoTempStyle,screenShotStyle]"
        class="main-class"
        @mouseup="deselectCurComponent"
        @mousedown="handleMouseDown"
      >
        <el-row v-if="componentDataShow.length===0" class="custom-position">
          {{ $t('panel.panelNull') }}
        </el-row>
        <canvas-opt-bar />
        <ComponentWrapper
          v-for="(item, index) in componentDataInfo"
          :key="index"
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
      mainWidth: '100%',
      mainHeight: '100%',
      searchCount: 0,
      chartDetailsVisible: false,
      mobileChartDetailsVisible: false,
      showChartInfo: {},
      showChartTableInfo: {},
      // 布局展示 1.pc pc端布局 2.mobile 移动端布局
      terminal: 'pc'
    }
  },
  created() {
  },
  computed: {
    canvasInfoMainStyle() {
      if (this.backScreenShot) {
        return {
          width: '100%',
          height: this.mainHeight
        }
      } else {
        return {
          width: '100%',
          height: '100%'
        }
      }
    },
    canvasInfoTempStyle() {
      if (this.screenShot) {
        return {
          width: '100%',
          height: this.mainHeight
        }
      } else {
        return {
          width: '100%',
          height: '100%'
        }
      }
    },
    customStyle() {
      let style = {
        width: '100%'
      }
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
        style.padding = '5px'
      }
      return style
    },
    screenShotStyle() {
      return this.screenShot ? this.customStyle : {}
    },
    // 此处单独计算componentData的值 不放入全局mapState中
    componentDataInfo() {
      return this.componentDataShow
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
        this.restore()
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
    const _this = this
    const erd = elementResizeDetectorMaker()
    // 监听主div变动事件
    erd.listenTo(document.getElementById('canvasInfoMain'), element => {
      _this.$nextTick(() => {
        _this.restore()
      })
    })
    // 监听画布div变动事件
    const tempCanvas = document.getElementById('canvasInfoTemp')
    erd.listenTo(document.getElementById('canvasInfoTemp'), element => {
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
    if (_this.terminal === 'mobile') {
      _this.initMobileCanvas()
    }
  },
  beforeDestroy() {
    clearInterval(this.timer)
  },
  methods: {
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
      const canvasHeight = document.getElementById('canvasInfoMain').offsetHeight
      const canvasWidth = document.getElementById('canvasInfoMain').offsetWidth
      this.scaleWidth = (canvasWidth) * 100 / this.canvasStyleData.width // 获取宽度比
      // 如果是后端截图方式使用 的高度伸缩比例和宽度比例相同
      if (this.backScreenShot) {
        this.scaleHeight = this.scaleWidth
      } else {
        this.scaleHeight = canvasHeight * 100 / this.canvasStyleData.height// 获取高度比
      }
      this.$store.commit('setPreviewCanvasScale', { scaleWidth: (this.scaleWidth / 100), scaleHeight: (this.scaleHeight / 100) })
      this.handleScaleChange()
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
      return value * scale / 100
    },
    handleScaleChange() {
      if (this.componentData) {
        const componentData = deepCopy(this.componentData)
        componentData.forEach(component => {
          Object.keys(component.style).forEach(key => {
            if (this.needToChangeHeight.includes(key)) {
              component.style[key] = this.format(component.style[key], this.scaleHeight)
            }
            if (this.needToChangeWidth.includes(key)) {
              if (key === 'fontSize' && this.terminal === 'mobile') {
                // do nothing 移动端字符大小无需按照比例缩放，当前保持不变(包括 v-text 和 过滤组件)
              } else {
                component.style[key] = this.format(component.style[key], this.scaleWidth)
              }
            }
          })
        })
        this.componentDataShow = componentData
        this.$nextTick(() => (eventBus.$emit('resizing', '')))
      }
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
