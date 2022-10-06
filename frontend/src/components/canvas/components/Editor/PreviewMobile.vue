<template>
  <div class="bg" :style="customStyle">
    <canvas-opt-bar />
    <div id="canvasInfoMain" ref="canvasInfoMain" style="width: 100%;height: 100%">
      <div
        id="canvasInfoTemp"
        ref="canvasInfoTemp"
        :style="[{height:mainHeight},screenShotStyle]"
        class="main-class"
        @mouseup="deselectCurComponent"
        @mousedown="handleMouseDown"
      >
        <el-row v-if="componentDataShow.length===0" style="height: 100%;" class="custom-position">
          {{ $t('panel.panelNull') }}
        </el-row>
        <ComponentWrapper
          v-for="(item, index) in componentDataInfo"
          :key="index"
          :config="item"
          :search-count="searchCount"
          :canvas-style-data="canvasStyleData"
          :in-screen="inScreen"
        />
        <!--视图详情-->
        <el-dialog
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
          <UserViewDialog ref="userViewDialog" :canvas-style-data="canvasStyleData" :chart="showChartInfo" :chart-table="showChartTableInfo" />
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
import {deepCopy, imgUrlTrans} from '@/components/canvas/utils/utils'
import eventBus from '@/components/canvas/utils/eventBus'
import elementResizeDetectorMaker from 'element-resize-detector'
import UserViewDialog from '@/components/canvas/custom-component/UserViewDialog'
import CanvasOptBar from '@/components/canvas/components/Editor/CanvasOptBar'

export default {
  components: { ComponentWrapper, UserViewDialog, CanvasOptBar },
  model: {
    prop: 'show',
    event: 'change'
  },
  props: {
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
      showChartInfo: {},
      showChartTableInfo: {}
    }
  },
  computed: {
    customStyle() {
      let style = {
        width: '100%'
      }
      if (this.canvasStyleData.openCommonStyle) {
        if (this.canvasStyleData.panel.backgroundType === 'image' && this.canvasStyleData.panel.imageUrl) {
          style = {
            background: `url(${imgUrlTrans(this.canvasStyleData.panel.imageUrl)}) no-repeat`,
            ...style
          }
        } else if (this.canvasStyleData.panel.backgroundType === 'color') {
          style = {
            background: this.canvasStyleData.panel.color,
            ...style
          }
        }
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
    ])
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
    const _this = this
    const erd = elementResizeDetectorMaker()
    // 监听div变动事件
    const mainDom = document.getElementById('canvasInfoMain')
    erd.listenTo(mainDom, element => {
      _this.$nextTick(() => {
        _this.restore()
      })
    })
    // 监听div变动事件
    const tempCanvas = document.getElementById('canvasInfoTemp')
    erd.listenTo(tempCanvas, element => {
      _this.$nextTick(() => {
        // 将mainHeight 修改为px 临时解决html2canvas 截图不全的问题
        _this.mainHeight = tempCanvas.scrollHeight + 'px!important'
      })
    })
    eventBus.$on('openChartDetailsDialog', this.openChartDetailsDialog)
    _this.$store.commit('clearLinkageSettingInfo', false)
    _this.canvasStyleDataInit()
  },
  beforeDestroy() {
    eventBus.$off('openChartDetailsDialog', this.openChartDetailsDialog)
    clearInterval(this.timer)
  },
  methods: {
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
      this.scaleWidth = canvasWidth * 100 / parseInt(this.canvasStyleData.width)// 获取宽度比
      this.scaleHeight = canvasHeight * 100 / parseInt(this.canvasStyleData.height)// 获取高度比
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
      return value * parseInt(scale) / 100
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
              component.style[key] = this.format(component.style[key], this.scaleWidth)
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
      this.chartDetailsVisible = true
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
    }
  }
}
</script>

<style lang="scss" scoped>
  .bg {
    padding: 5px;
    min-width: 600px;
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
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    flex-flow: row nowrap;
    color: #9ea6b2;
  }

  .gap_class {
    padding: 5px;
  }

  .dialog-css ::v-deep .el-dialog__title {
    font-size: 14px;
  }

  .dialog-css ::v-deep .el-dialog__header {
    padding: 40px 20px 0;
  }

  .dialog-css ::v-deep .el-dialog__body {
    padding: 10px 20px 20px;
  }

  ::-webkit-scrollbar {
    width: 0px!important;
    height: 0px!important;
  }

</style>
