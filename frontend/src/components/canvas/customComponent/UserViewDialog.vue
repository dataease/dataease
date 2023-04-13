<template>
  <de-container
    v-loading="$store.getters.loadingMap[$store.getters.currentPath] || linkLoading"
    :class="isAbsoluteContainer ? 'abs-container' : ''"
  >
    <de-main-container
      v-show="showChartCanvas"
      class=""
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
            :component-name="chart.type + '-view'"
            :obj="{chart: mapChart || chart}"
            :chart="mapChart || chart"
            :theme-style="element.commonBackground"
            :canvas-style-data="canvasStyleData"
            class="chart-class"
          />
          <chart-component
            v-else-if="!chart.type.includes('text') && chart.type !== 'label' && !chart.type.includes('table') && renderComponent() === 'echarts'"
            :theme-style="element.commonBackground"
            class="chart-class"
            :chart="mapChart || chart"
          />
          <chart-component-g2
            v-else-if="!chart.type.includes('text') && chart.type !== 'label' && !chart.type.includes('table') && renderComponent() === 'antv'"
            class="chart-class"
            :chart="chart"
          />
          <chart-component-s2
            v-else-if="chart.type.includes('table') && renderComponent() === 'antv'"
            class="chart-class"
            :chart="chart"
          />
          <label-normal
            v-else-if="chart.type.includes('text')"
            :chart="chart"
            class="table-class"
          />
          <label-normal-text
            v-else-if="chart.type === 'label'"
            :chart="chart"
            class="table-class"
          />
          <table-normal
            v-else-if="chart.type.includes('table') && renderComponent() === 'echarts'"
            :chart="chart"
            class="table-class"
          />
        </div>
      </div>
    </de-main-container>
    <de-main-container v-show="!showChartCanvas">
      <table-normal
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
import { exportDetails, innerExportDetails } from '@/api/panel/panel'
import html2canvas from 'html2canvasde'
import { hexColorToRGBA } from '@/views/chart/chart/util'
import { deepCopy, exportImg, imgUrlTrans } from '@/components/canvas/utils/utils'
import { getLinkToken, getToken } from '@/utils/auth'

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
    }

  },
  data() {
    return {
      refId: null,
      element: {},
      lastMapChart: null,
      linkLoading: false
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
        const result = { ...temp, ...{ DetailAreaCode: DetailAreaCode } }
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
  },
  methods: {
    exportExcel(callBack) {
      const _this = this
      if (this.isOnlyDetails) {
        _this.exportExcelDownload(null, null, null, callBack)
      } else {
        if (this.showChartCanvas) {
          html2canvas(document.getElementById('chartCanvas')).then(canvas => {
            const snapshot = canvas.toDataURL('image/jpeg', 1)
            _this.exportExcelDownload(snapshot, canvas.width, canvas.height, callBack)
          })
        } else {
          _this.exportExcelDownload(null, null, null, callBack)
        }
      }
    },
    exportViewImg(callback) {
      exportImg(this.chart.name, callback)
    },
    setLastMapChart(data) {
      this.lastMapChart = JSON.parse(JSON.stringify(data))
    },
    exportExcelDownload(snapshot, width, height, callBack) {
      const excelHeader = JSON.parse(JSON.stringify(this.chart.data.fields)).map(item => item.name)
      const excelTypes = JSON.parse(JSON.stringify(this.chart.data.fields)).map(item => item.deType)
      const excelHeaderKeys = JSON.parse(JSON.stringify(this.chart.data.fields)).map(item => item.dataeaseName)
      let excelData = JSON.parse(JSON.stringify(this.chart.data.tableRow)).map(item => excelHeaderKeys.map(i => item[i]))
      const excelName = this.chart.name
      let detailFields = []
      if (this.chart.data.detailFields?.length) {
        detailFields = this.chart.data.detailFields.map(item => {
          const temp = {
            name: item.name,
            deType: item.deType,
            dataeaseName: item.dataeaseName
          }
          return temp
        })
        excelData = JSON.parse(JSON.stringify(this.chart.data.tableRow)).map(item => {
          const temp = excelHeaderKeys.map(i => {
            if (i === 'detail' && !item[i] && Array.isArray(item['details'])) {
              const arr = item['details']
              if (arr?.length) {
                return arr.map(ele => detailFields.map(field => ele[field.dataeaseName]))
              }
              return null
            }
            return item[i]
          })
          return temp
        })
      }
      const request = {
        proxy: null,
        viewId: this.chart.id,
        viewName: excelName,
        header: excelHeader,
        details: excelData,
        excelTypes: excelTypes,
        snapshot: snapshot,
        snapshotWidth: width,
        snapshotHeight: height,
        componentFilterInfo: this.lastViewRequestInfo[this.chart.id],
        excelHeaderKeys: excelHeaderKeys,
        detailFields
      }
      let method = innerExportDetails
      const token = this.$store.getters.token || getToken()
      const linkToken = this.$store.getters.linkToken || getLinkToken()
      if (!token && linkToken) {
        method = exportDetails
        this.linkLoading = true
      }

      if (this.panelInfo.proxy) {
        request.proxy = { userId: this.panelInfo.proxy }
      }
      method(request).then((res) => {
        const blob = new Blob([res], { type: 'application/vnd.ms-excel' })
        const link = document.createElement('a')
        link.style.display = 'none'
        link.href = URL.createObjectURL(blob)
        link.download = excelName + '.xlsx' // 下载的文件名
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        this.linkLoading = false
        callBack && callBack()
      }).catch(() => {
        this.linkLoading = false
        callBack && callBack()
      })
    },

    renderComponent() {
      return this.chart.render
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
