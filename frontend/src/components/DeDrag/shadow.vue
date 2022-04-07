<template>
  <div class="main-shadow" style="z-index:-1" :style="styleInfo">
    <!--    {{ curComponent }}-->
  </div>
</template>

<script>
import { mapState } from 'vuex'
export default {
  replace: true,
  name: 'Shadow',
  computed: {
    styleInfo() {
      // console.log('styleInfo==>')
      // console.log('dragComponentInfo==>' + this.dragComponentInfo.shadowStyle.x)
      let left = 0
      let top = 0
      let width = 0
      let height = 0
      let transition = 0
      // if (this.dragComponentInfo && !this.dragComponentInfo.auxiliaryMatrix) {
      if (this.dragComponentInfo) {
        // console.log('shadowDrag=')
        // 组件移入
        if (this.dragComponentInfo.auxiliaryMatrix) {
          left = (this.dragComponentInfo.x - 1) * this.curCanvasScale.matrixStyleWidth
          top = (this.dragComponentInfo.y - 1) * this.curCanvasScale.matrixStyleHeight

          width = this.dragComponentInfo.sizex * this.curCanvasScale.matrixStyleWidth
          height = this.dragComponentInfo.sizey * this.curCanvasScale.matrixStyleHeight
          transition = 0.1
        } else {
          left = this.dragComponentInfo.shadowStyle.x
          top = this.dragComponentInfo.shadowStyle.y
          width = this.dragComponentInfo.style.width
          height = this.dragComponentInfo.style.height
        }

        // console.log('left:' + left + 'top:' + top + 'width:' + width + 'height:' + height)
      } else {
        // temp 临时测试
        // left = this.curComponent.style.left * this.curCanvasScale.scaleWidth / 100
        // top = this.curComponent.style.top * this.curCanvasScale.scaleHeight / 100
        left = (this.curComponent.x - 1) * this.curCanvasScale.matrixStyleWidth
        top = (this.curComponent.y - 1) * this.curCanvasScale.matrixStyleHeight

        width = this.curComponent.style.width * this.curCanvasScale.scalePointWidth
        height = this.curComponent.style.height * this.curCanvasScale.scalePointHeight
        if (this.curComponent.optStatus.dragging) {
          transition = 0.1
        }
        // console.log('curComponent left:' + left + 'top:' + top + 'width:' + width + 'height:' + height)
      }

      // 防止阴影区超出边界
      const xGap = left + width - this.canvasWidth
      // console.log('canvasWidth:' + this.canvasWidth + ';xGap:' + xGap)
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
      // console.log('style=>' + JSON.stringify(style))
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
      const scaleWidth = this.curCanvasScale.scaleWidth / 100
      return this.canvasStyleData.width * scaleWidth
    },
    ...mapState([
      'curComponent',
      'editor',
      'curCanvasScale',
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
      return h / this.curCanvasScale.scalePointHeight
    },
    scaleW(w) {
      return w / this.curCanvasScale.scalePointWidth
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
