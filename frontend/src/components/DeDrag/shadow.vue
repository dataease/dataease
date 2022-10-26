<template>
  <div
    class="main-shadow"
    style="z-index:-1"
    :style="styleInfo"
  />
</template>

<script>
import { mapState } from 'vuex'
export default {
  replace: true,
  // eslint-disable-next-line
  name: 'Shadow',
  props: {
    canvasId: {
      type: String,
      required: true
    }
  },
  computed: {
    curCanvasScaleSelf() {
      return this.curCanvasScaleMap[this.canvasId]
    },
    styleInfo() {
      let left = 0
      let top = 0
      let width = 0
      let height = 0
      let transition = 0
      if (this.dragComponentInfo) {
        // 组件移入
        if (this.dragComponentInfo.auxiliaryMatrix) {
          left = (this.dragComponentInfo.x - 1) * this.curCanvasScaleSelf.matrixStyleWidth
          top = (this.dragComponentInfo.y - 1) * this.curCanvasScaleSelf.matrixStyleHeight

          width = this.dragComponentInfo.sizex * this.curCanvasScaleSelf.matrixStyleWidth
          height = this.dragComponentInfo.sizey * this.curCanvasScaleSelf.matrixStyleHeight
          transition = 0.1
        } else {
          left = this.dragComponentInfo.shadowStyle.x
          top = this.dragComponentInfo.shadowStyle.y
          width = this.dragComponentInfo.style.width
          height = this.dragComponentInfo.style.height
        }
      } else {
        left = (this.curComponent.x - 1) * this.curCanvasScaleSelf.matrixStyleWidth
        top = (this.curComponent.y - 1) * this.curCanvasScaleSelf.matrixStyleHeight
        width = this.curComponent.style.width * this.curCanvasScaleSelf.scalePointWidth
        height = this.curComponent.style.height * this.curCanvasScaleSelf.scalePointHeight
        if (this.curComponent.optStatus.dragging) {
          transition = 0.1
        }
      }

      // 防止阴影区超出边界
      const xGap = left + width - this.canvasWidth
      if (xGap > 0) {
        left = left - xGap
      }
      const style = {
        transform: `translate(${left}px, ${top}px) rotate(0deg)`,
        width: width + 'px',
        height: height + 'px'
      }
      if (transition > 0) {
        style.transition = transition + 's'
      }
      // 记录外部拖拽进入仪表板时阴影区域宽高
      if (this.dragComponentInfo) {
        this.recordShadowStyle(left, top, width, height)
      }
      return style
    },
    dragComponentInfo() {
      return this.$store.state.dragComponentInfo
    },
    canvasWidth() {
      const scaleWidth = this.curCanvasScaleSelf.scaleWidth / 100
      return this.canvasStyleData.width * scaleWidth
    },
    ...mapState([
      'curComponent',
      'editor',
      'curCanvasScaleMap',
      'canvasStyleData',
      'linkageSettingStatus'
    ])
  },
  methods: {
    recordShadowStyle(x, y, width, height) {
      this.dragComponentInfo.shadowStyle.width = this.scaleW(width)
      this.dragComponentInfo.shadowStyle.x = this.scaleW(x)
      this.dragComponentInfo.shadowStyle.height = this.scaleH(height)
      this.dragComponentInfo.shadowStyle.y = this.scaleH(y)
    },
    scaleH(h) {
      return h / this.curCanvasScaleSelf.scalePointHeight
    },
    scaleW(w) {
      return w / this.curCanvasScaleSelf.scalePointWidth
    }
  }

}
</script>
<style scoped>
  .main-shadow{
    opacity: 0.4;
    background: #409eff;
    position: absolute;
  }

</style>
