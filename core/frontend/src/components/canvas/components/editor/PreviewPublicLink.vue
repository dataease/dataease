<template>
  <div
    :id="previewMainDomId"
    :ref="previewOutRefId"
    class="bg"
    :style="customStyle"
  >
    <link-opt-bar />
    <Preview />
  </div>
</template>

<script>
import { mapState } from 'vuex'
import { imgUrlTrans } from '@/components/canvas/utils/utils'
import { hasDataPermission } from '@/utils/permission'
import { hexColorToRGBA } from '@/views/chart/chart/util'

export default {
  model: {
    prop: 'show',
    event: 'change'
  },
  props: {
  },
  data() {
    return {
      backToTopBtnShow: false,
      imageDownloading: false,
      chartDetailsVisible: false,
      canvasMain: null,
      tempCanvas: null,
      showChartInfo: {},
      showChartTableInfo: {},
      showChartInfoType: 'details',
      mainHeightCount: null,
      userInfo: null,
      previewMainDomId: 'preview-main-' + this.canvasId,
      previewDomId: 'preview-' + this.canvasId,
      previewRefId: 'preview-ref-' + this.canvasId,
      previewOutRefId: 'preview-out-ref-' + this.canvasId,
      previewTempDomId: 'preview-temp-' + this.canvasId,
      previewTempRefId: 'preview-temp-ref-' + this.canvasId,
      isShowPreview: false,
      panelId: '',
      needToChangeHeight: [
        'top',
        'height'
      ],
      needToChangeWidth: [
        'left',
        'width'
      ],
      needToChangeInnerWidth: [
        'fontSize',
        'activeFontSize',
        'borderWidth',
        'letterSpacing'
      ],
      scaleWidth: '100',
      scaleHeight: '100',
      timer: null,
      componentDataShow: null,
      mainWidth: '100%',
      mainHeight: '100%',
      searchCount: 0,
      filterMapCache: {},
      // 布局展示 1.pc pc端布局 2.mobile 移动端布局
      terminal: 'pc',
      buttonFilterMap: null,
      pdfExportShow: false,
      dataLoading: false,
      exporting: false,
      snapshotInfo: '',
      pdfTemplateSelectedIndex: 0,
      pdfTemplateContent: '',
      templateInfo: {},
      pdfTemplateAll: [],
      pixel: '1280 * 720'
    }
  },
  computed: {
    screenShotStatues() {
      return this.exporting || this.screenShot || this.backScreenShot
    },
    mainActiveName() {
      return this.$store.state.panel.mainActiveName
    },
    showUnpublishedArea() {
      if (this.canvasId !== 'canvas-main') {
        return false
      }
      if (this.showPosition === 'edit') {
        return false
      } else if (this.panelInfo && this.panelInfo.showType === 'view') {
        return false
      } else if ((this.mainActiveName === 'PanelMain' && this.activeTab === 'PanelList') || this.showPosition.includes('multiplexing')) {
        return this.panelInfo.status === 'unpublished' && !hasDataPermission('manage', this.panelInfo.privileges)
      } else {
        return this.panelInfo.status === 'unpublished'
      }
    },
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
      if (this.canvasStyleData.openCommonStyle && this.isMainCanvas()) {
        const styleInfo = this.terminal === 'mobile' && this.canvasStyleData.panel.mobileSetting && this.canvasStyleData.panel.mobileSetting.customSetting
          ? this.canvasStyleData.panel.mobileSetting : this.canvasStyleData.panel
        if (styleInfo.backgroundType === 'image' && typeof (styleInfo.imageUrl) === 'string') {
          style = {
            background: `url(${imgUrlTrans(styleInfo.imageUrl)}) no-repeat`
          }
        } else if (styleInfo.backgroundType === 'color') {
          const colorRGBA = hexColorToRGBA(styleInfo.color, styleInfo.alpha === undefined ? 100 : styleInfo.alpha)
          style = {
            background: colorRGBA
          }
        } else {
          style = {
            background: '#f7f8fa'
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
      return this.componentDataShow || []
    },
    ...mapState([
      'previewCanvasScale',
      'isClickComponent'
    ])
  }
}
</script>

<style lang="scss" scoped>
.bg {
  min-width: 200px;
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
  line-height: 30px;
  width: 100%;
  z-index: 100;
  height: 100%;
  text-align: center;
  cursor: not-allowed;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  flex-flow: row nowrap;
  color: #9ea6b2;
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
  width: 0px !important;
  height: 0px !important;
}

::v-deep .el-tabs__nav {
  z-index: 0;
}

</style>
