<template>
  <de-container
    v-loading="$store.getters.loadingMap[$store.getters.currentPath] || linkLoading"
    :class="isAbsoluteContainer ? 'abs-container' : ''"
  >
    <de-main-container
      v-if="showChartCanvas"
      v-loading="exportLoading"
      style="overflow: hidden"
      :element-loading-text="$t('panel.data_loading')"
      element-loading-spinner="el-icon-loading"
      element-loading-background="rgba(220,220,220,1)"
    >
      <div
        id="chartCanvas"
        class="canvas-class"
        :style="customStyle"
      >
        <div
          class="canvas-class"
          :style="commonStyle"
        >
          <plugin-com
            v-if="chart.isPlugin"
            :ref="element.propValue.id"
            :component-name="chart.type + '-view'"
            :obj="{chart: mapChart || chart}"
            :chart="mapChart || chart"
            :theme-style="element.commonBackground"
            :canvas-style-data="canvasStyleData"
            class="chart-class"
          />
          <chart-component
            v-else-if="!chart.type.includes('text') && chart.type !== 'label' && !chart.type.includes('table') && renderComponent() === 'echarts'"
            :ref="element.propValue.id"
            :theme-style="element.commonBackground"
            class="chart-class"
            :chart="mapChart || chart"
          />
          <chart-component-g2
            v-else-if="!chart.type.includes('text') && chart.type !== 'label' && !chart.type.includes('table') && renderComponent() === 'antv'"
            :ref="element.propValue.id"
            class="chart-class"
            :chart="chart"
          />
          <chart-component-s2
            v-else-if="chart.type.includes('table') && renderComponent() === 'antv'"
            :ref="element.propValue.id"
            class="chart-class"
            :chart="chart"
          />
          <label-normal
            v-else-if="chart.type.includes('text')"
            :ref="element.propValue.id"
            :chart="chart"
            class="table-class"
          />
          <label-normal-text
            v-else-if="chart.type === 'label'"
            :ref="element.propValue.id"
            :chart="chart"
            class="table-class"
          />
          <table-normal
            v-else-if="chart.type.includes('table') && renderComponent() === 'echarts'"
            :ref="element.propValue.id"
            :chart="chart"
            class="table-class"
          />
        </div>
      </div>
    </de-main-container>
    <de-main-container v-else>
      <table-normal
        id="chartCanvas"
        :enable-scroll="false"
        :chart="chartTable"
        :show-summary="false"
        class="table-class-dialog"
      />
    </de-main-container>
  </de-container>
</template>

<script>

import ChartComponent from '@/views/chart/components/ChartComponent.vue'
import TableNormal from '@/views/chart/components/table/TableNormal'
import LabelNormal from '@/views/chart/components/normal/LabelNormal'
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
import { mapState } from 'vuex'
import ChartComponentG2 from '@/views/chart/components/ChartComponentG2'
import PluginCom from '@/views/system/plugin/PluginCom'
import ChartComponentS2 from '@/views/chart/components/ChartComponentS2'
import LabelNormalText from '@/views/chart/components/normal/LabelNormalText'
import html2canvas from 'html2canvasde'
import { hexColorToRGBA } from '@/views/chart/chart/util'
import { deepCopy, exportExcelDownload, exportImg, exportImgNew, imgUrlTrans } from '@/components/canvas/utils/utils'
import { activeWatermark } from '@/components/canvas/tools/watermark'
import { proxyUserLoginInfo, userLoginInfo } from '@/api/systemInfo/userLogin'

export default {
  name: 'UserViewDialog',
  components: {
    LabelNormalText,
    ChartComponentS2,
    ChartComponentG2,
    DeMainContainer,
    DeContainer,
    ChartComponent,
    TableNormal,
    LabelNormal,
    PluginCom
  },
  props: {
    chart: {
      type: Object,
      default: null
    },
    chartTable: {
      type: Object,
      default: null
    },
    openType: {
      type: String,
      default: 'details'
    },
    userId: {
      type: String,
      require: false
    }

  },
  data() {
    return {
      refId: null,
      element: {},
      lastMapChart: null,
      linkLoading: false,
      exporting: false,
      exportLoading: false,
      pixel: '1280 * 720'
    }
  },
  computed: {
    panelInfo() {
      return this.$store.state.panel.panelInfo
    },
    isAbsoluteContainer() {
      return this.showChartCanvas && this.chart.type === 'symbol-map'
    },
    showChartCanvas() {
      return this.openType === 'enlarge'
    },
    isOnlyDetails() {
      return this.chart.type === 'table-normal' || this.chart.type === 'table-info'
    },
    customStyle() {
      let style = {}
      if (this.exporting) {
        const bashStyle = this.pixel.split(' * ')
        style = {
          width: bashStyle[0] + 'px!important',
          height: bashStyle[1] + 'px!important'
        }
      }
      if (this.canvasStyleData.openCommonStyle) {
        if (this.canvasStyleData.panel.backgroundType === 'image' && this.canvasStyleData.panel.imageUrl) {
          style = {
            background: `url(${imgUrlTrans(this.canvasStyleData.panel.imageUrl)}) no-repeat`,
            ...style
          }
        } else if (this.canvasStyleData.panel.backgroundType === 'color') {
          const colorRGBA = hexColorToRGBA(this.canvasStyleData.panel.color, this.canvasStyleData.panel.alpha === undefined ? 100 : this.canvasStyleData.panel.alpha)
          style = {
            background: colorRGBA,
            ...style
          }
        }
      }
      if (!style.background) {
        style.background = '#FFFFFF'
      }
      return style
    },

    svgInnerEnable() {
      return !this.screenShot && this.element.commonBackground.enable && this.element.commonBackground.backgroundType === 'innerImage' && typeof this.element.commonBackground.innerImage === 'string'
    },
    mainSlotSvgInner() {
      if (this.svgInnerEnable) {
        return this.element.commonBackground.innerImage.replace('board/', '').replace('.svg', '')
      } else {
        return null
      }
    },
    commonStyle() {
      const style = {
        width: '100%',
        height: '100%'
      }
      if (this.element.commonBackground) {
        style['padding'] = (this.element.commonBackground.innerPadding || 0) + 'px'
        style['border-radius'] = (this.element.commonBackground.borderRadius || 0) + 'px'
        let colorRGBA = ''
        if (this.element.commonBackground.backgroundColorSelect) {
          colorRGBA = hexColorToRGBA(this.element.commonBackground.color, this.element.commonBackground.alpha)
        }
        if (this.element.commonBackground.enable) {
          if (this.screenShot && this.element.commonBackground.backgroundType === 'innerImage' && typeof this.element.commonBackground.innerImage === 'string') {
            const innerImage = this.element.commonBackground.innerImage.replace('svg', 'png')
            style['background'] = `url(${imgUrlTrans(innerImage)}) no-repeat ${colorRGBA}`
          } else if (this.element.commonBackground.backgroundType === 'outerImage' && typeof this.element.commonBackground.outerImage === 'string') {
            style['background'] = `url(${imgUrlTrans(this.element.commonBackground.outerImage)}) no-repeat ${colorRGBA}`
          } else {
            style['background-color'] = colorRGBA
          }
        } else {
          style['background-color'] = colorRGBA
        }
        style['overflow'] = 'hidden'
      }
      return style
    },
    ...mapState([
      'isClickComponent',
      'curComponent',
      'componentData',
      'canvasStyleData',
      'lastViewRequestInfo'
    ]),
    mapChart() {
      if (this.chart.type && (this.chart.type === 'map' || this.chart.type === 'buddle-map')) {
        const temp = JSON.parse(JSON.stringify(this.chart))
        let DetailAreaCode = null
        if (this.curComponent && this.curComponent.DetailAreaCode && this.curComponent.DetailAreaCode.length) {
          DetailAreaCode = this.curComponent.DetailAreaCode
        }
        if (!this.curComponent && this.lastMapChart) {
          return this.lastMapChart
        }
        if (this.curComponent && this.curComponent.options && this.curComponent.options.tabList && this.curComponent.options.tabList.length) {
          const tabList = JSON.parse(JSON.stringify(this.curComponent.options.tabList))
          tabList.forEach(tab => {
            if (tab.content &&
              tab.content.propValue &&
              tab.content.propValue.viewId &&
              tab.content.propValue.viewId === this.chart.id &&
              tab.content.DetailAreaCode &&
              tab.content.DetailAreaCode.length) {
              DetailAreaCode = tab.content.DetailAreaCode
            }
          })
        }
        const result = { ...temp, ...{ DetailAreaCode: DetailAreaCode }}
        this.setLastMapChart(result)
        return result
      }

      return null
    }
  },
  created() {
    this.element = deepCopy(this.curComponent)
  },
  mounted() {
    this.initWatermark()
  },
  methods: {
    initWatermark(waterDomId = 'chartCanvas') {
      if (this.panelInfo.watermarkInfo) {
        if (this.userInfo) {
          activeWatermark(this.panelInfo.watermarkInfo.settingContent, this.userInfo, waterDomId, 'canvas-main', this.panelInfo.watermarkOpen, 'de-watermark-view')
        } else {
          const method = this.userId ? proxyUserLoginInfo : userLoginInfo
          method().then(res => {
            this.userInfo = res.data
            activeWatermark(this.panelInfo.watermarkInfo.settingContent, this.userInfo, waterDomId, 'canvas-main', this.panelInfo.watermarkOpen, 'de-watermark-view')
          })
        }
      }
    },
    exportExcel(callBack) {
      this.exportExcelDownload(null, null, null, callBack)
    },
    exportSourceDetails(callBack) {
      const loadingWrapper = { val: this.linkLoading }
      exportExcelDownload(this.chart, null, null, null, loadingWrapper, { downloadType: 'dataset' }, callBack)
    },
    exportViewImg(pixel, callback) {
      this.pixel = pixel
      this.exportLoading = true
      this.$nextTick(() => {
        this.exporting = true
        this.resizeChart()
        setTimeout(() => {
          this.initWatermark()
          exportImgNew(this.chart.name, (params) => {
            this.exporting = false
            this.resizeChart()
            setTimeout(() => {
              this.exportLoading = false
            }, 500)
            callback(params)
          })
        }, 500)
      })
    },
    setLastMapChart(data) {
      this.lastMapChart = JSON.parse(JSON.stringify(data))
    },
    exportExcelDownload(snapshot, width, height, callBack) {
      const loadingWrapper = { val: this.linkLoading }
      exportExcelDownload(this.chart, snapshot, width, height, loadingWrapper, null, callBack)
    },

    renderComponent() {
      return this.chart.render
    },
    resizeChart() {
      if (this.$refs[this.element.propValue.id]) {
        this.chart.isPlugin
          ? this.$refs[this.element.propValue.id].callPluginInner({ methodName: 'chartResize' })
          : this.$refs[this.element.propValue.id].chartResize()
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.ms-aside-container {
  height: 70vh;
  min-width: 400px;
  max-width: 400px;
  padding: 0 0;
}

.ms-main-container {
  height: 70vh;
  border: 1px solid #E6E6E6;
}

.chart-class {
  height: 100%;
}

.table-class-dialog {
  height: 100%;
  overflow-y: auto !important;
}

.canvas-class {
  position: relative;
  width: 100%;
  height: 100%;
  background-size: 100% 100% !important;
}
.canvas-class-exporting {
  width: 1980px!important;
  height: 860px!important;
}

.abs-container {
  position: absolute;
  width: 100%;
  margin-left: -20px;

  .ms-main-container {
    padding: 0px !important;
  }
}

.svg-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

</style>
