<template>
  <el-row
    id="canvasInfoMobile"
    class="this_mobile_canvas_main"
    :style="mobileCanvasStyle"
  >
    <canvas-opt-bar />
    <de-canvas
      ref="canvasMainRef"
      :canvas-style-data="canvasStyleData"
      :component-data="mainCanvasComponentData"
      :canvas-id="canvasId"
      :canvas-pid="'0'"
      :mobile-layout-status="true"
    />
  </el-row>
</template>
<script>
import DeCanvas from '@/components/canvas/DeCanvas'
import CanvasOptBar from '@/components/canvas/components/editor/CanvasOptBar'
import {
  imgUrlTrans
} from '@/components/canvas/utils/utils'
import { mapState } from 'vuex'
import { hexColorToRGBA } from '@/views/chart/chart/util'

export default {
  components: { DeCanvas, CanvasOptBar },
  data() {
    return {
      canvasId: 'canvas-main',
      previewVisible: false
    }
  },
  computed: {
    ...mapState(['canvasStyleData', 'mobileLayoutStatus', 'componentData']),
    mainCanvasComponentData() {
      return this.componentData.filter(item => item.canvasId === 'canvas-main')
    },
    mobileCanvasStyle() {
      let style
      if (this.canvasStyleData.openCommonStyle) {
        const styleInfo =
          this.canvasStyleData.panel.mobileSetting &&
          this.canvasStyleData.panel.mobileSetting.customSetting
            ? this.canvasStyleData.panel.mobileSetting
            : this.canvasStyleData.panel
        if (
          styleInfo.backgroundType === 'image' &&
          typeof styleInfo.imageUrl === 'string'
        ) {
          style = {
            background: `url(${imgUrlTrans(styleInfo.imageUrl)}) no-repeat`
          }
        } else if (styleInfo.backgroundType === 'color') {
          const colorRGBA = hexColorToRGBA(
            styleInfo.color,
            styleInfo.alpha === undefined ? 100 : styleInfo.alpha
          )
          style = {
            background: colorRGBA
          }
        } else {
          style = {
            background: '#f7f8fa'
          }
        }
      }
      return style
    }
  },
  mounted() {
    this.$store.commit('setMobileLayoutStatus', true)
    this.$store.commit('setMobileStatus', true)
    window.addEventListener('message', (event) => {
      if (event.data.type === 'addComponent') {
        this.$store.commit('addComponent', event.data.value)
      }
      if (event.data.type === 'setCanvasStyle') {
        this.$store.commit('setCanvasStyle', event.data.value)
      }
      if (event.data.type === 'editSave') {
        window.top.postMessage({ type: 'setComponentData', value: this.componentData }, '*')
      }
      if (event.data.type === 'reset') {
        this.$store.commit('setComponentData', event.data.value)
        this.$store.commit('openMobileLayout')
      }

      if (event.data.type === 'openMobileLayout') {
        this.$store.commit('setComponentData', event.data.value.componentData)
        this.$store.commit('setCanvasStyle', event.data.value.canvasStyleData)
        this.$store.dispatch('panel/setPanelInfo', event.data.value.panelInfo)
        this.$store.commit('openMobileLayout')
      }
    })
  },
  methods: {
    cancel() {
      this.show = false
    },
    confirm() {
      this.show = false
    },
    showPopup() {
      this.show = true
    }
  }
}
</script>
<style lang="less" scoped>
.this_mobile_canvas_main {
  overflow-x: hidden;
  overflow-y: auto;
  height: 100vh;
  background-size: 100% 100% !important;
}
.mobile-container {
  -webkit-tap-highlight-color: transparent;
  color: #323233;
  font-size: 16px;
  font-family:
    "Open Sans",
    -apple-system,
    BlinkMacSystemFont,
    "Helvetica Neue",
    Helvetica,
    Segoe UI,
    Arial,
    Roboto,
    "AlibabaPuHuiTi",
    "miui",
    "Hiragino Sans GB",
    "Microsoft Yahei",
    sans-serif;
  -webkit-font-smoothing: antialiased;
  height: 570px;
  box-sizing: border-box;
  width: 360px;
  min-width: 360px;
  overflow: hidden;
  background: #fafafa;
}
</style>
