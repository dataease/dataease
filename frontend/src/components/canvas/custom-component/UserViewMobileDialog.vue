<template>
  <de-container>
    <de-main-container v-if="chart.type !== 'table-normal' && chart.type !== 'table-info'" :style="customStyle" class="full-div">
      <div class="canvas-class" :style="commonStyle">
        <plugin-com
          v-if="chart.isPlugin"
          :component-name="chart.type + '-view'"
          :obj="{chart: mapChart || chart}"
          :chart="mapChart || chart"
          :theme-style="element.commonBackground"
          :canvas-style-data="canvasStyleData"
          class="chart-class"
        />
        <chart-component v-else-if="!chart.type.includes('text') && chart.type !== 'label' && !chart.type.includes('table') && renderComponent() === 'echarts'" :theme-style="element.commonBackground" class="chart-class" :chart="mapChart || chart" />
        <chart-component-g2 v-else-if="!chart.type.includes('text') && chart.type !== 'label' && !chart.type.includes('table') && renderComponent() === 'antv'" class="chart-class" :chart="chart" />
        <chart-component-s2 v-else-if="chart.type === 'table-pivot' && renderComponent() === 'antv'" class="chart-class" :chart="chart" />
        <label-normal v-else-if="chart.type.includes('text')" :chart="chart" class="table-class" />
        <label-normal-text v-else-if="chart.type === 'label'" :chart="chart" class="table-class" />
      </div>
    </de-main-container>
    <de-main-container v-else>
      <table-normal :chart="chartTable" :show-summary="false" class="table-class" />
    </de-main-container>
  </de-container>
</template>

<script>

import ChartComponent from '@/views/chart/components/ChartComponent.vue'
import TableNormal from '@/views/chart/components/table/TableNormal'
import LabelNormal from '@/views/chart/components/normal/LabelNormal'
import { mapState } from 'vuex'
import ChartComponentG2 from '@/views/chart/components/ChartComponentG2'
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
import LabelNormalText from '@/views/chart/components/normal/LabelNormalText'
import ChartComponentS2 from '@/views/chart/components/ChartComponentS2'
import PluginCom from '@/views/system/plugin/PluginCom'
import { deepCopy, imgUrlTrans } from '@/components/canvas/utils/utils'
import { hexColorToRGBA } from '@/views/chart/chart/util'
export default {
  name: 'UserViewMobileDialog',
  components: { ChartComponentS2, LabelNormalText, DeContainer, DeMainContainer, ChartComponentG2, ChartComponent, TableNormal, LabelNormal, PluginCom },
  props: {
    chart: {
      type: Object,
      default: null
    },
    chartTable: {
      type: Object,
      default: null
    }
    
  },
  data() {
    return {
      refId: null,
      element: {},
      lastMapChart: null
    }
  },
  created() {
    this.element = deepCopy(this.curComponent)
  },
  computed: {
    customStyle() {
      let style = {
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
      'canvasStyleData'
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
        const result = { ...temp, ...{ DetailAreaCode: DetailAreaCode }}
        this.setLastMapChart(result)
        return result
      }
      return null
    }
  },
  methods: {

    renderComponent() {
      return this.chart.render
    },
    setLastMapChart(data) {
      this.lastMapChart = JSON.parse(JSON.stringify(data))
    }
  }
}
</script>

<style lang="scss" scoped>
  .ms-main-container {
    border: 0px;
  }
  .chart-class{
    height: 100%;
  }
  .table-class{
    height: 100%;
  }
  .full-div{
    background-size:100% 100% !important;
    padding: 5px 0px 5px 0px !important;
  }
  .canvas-class{
    position: relative;
    width: 100%;
    height: 100%;
    background-size: 100% 100% !important;
  }
</style>
