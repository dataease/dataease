<template>
  <div style="z-index:-1" :style="styleInfo" />
</template>

<script>
import { mapState } from 'vuex'
export default {
  replace: true,
  name: 'Shadow',
  computed: {
    styleInfo() {
      // console.log('styleInfo==>')
      // debugger
      // console.log('dragComponentInfo==>' + this.dragComponentInfo.shadowStyle.x)
      let left = 0
      let top = 0
      let width = 0
      let height = 0
      if (this.dragComponentInfo) {
        // 组件移入
        left = this.dragComponentInfo.shadowStyle.x
        top = this.dragComponentInfo.shadowStyle.y
        width = this.dragComponentInfo.style.width
        height = this.dragComponentInfo.style.height
        // console.log('left:' + left + 'top:' + top + 'width:' + width + 'height:' + height)
      } else {
        left = this.curComponent.style.left * this.curCanvasScale.scaleWidth / 100
        top = this.curComponent.style.top * this.curCanvasScale.scaleHeight / 100
        width = this.curComponent.style.width * this.curCanvasScale.scaleWidth / 100
        height = this.curComponent.style.height * this.curCanvasScale.scaleHeight / 100
        // console.log('curComponent left:' + left + 'top:' + top + 'width:' + width + 'height:' + height)
      }
      // 当前默认为自适应
      if (this.canvasStyleData.auxiliaryMatrix) {
        left = Math.round(left / this.curCanvasScale.matrixStyleWidth) * this.curCanvasScale.matrixStyleWidth
        width = Math.round(width / this.curCanvasScale.matrixStyleWidth) * this.curCanvasScale.matrixStyleWidth
        top = Math.round(top / this.curCanvasScale.matrixStyleHeight) * this.curCanvasScale.matrixStyleHeight
        height = Math.round(height / this.curCanvasScale.matrixStyleHeight) * this.curCanvasScale.matrixStyleHeight
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
        height: height + 'px',
        opacity: 0.4,
        background: 'gray',
        position: 'absolute'
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
      return h * 100 / this.curCanvasScale.scaleHeight
    },
    scaleW(w) {
      return w * 100 / this.curCanvasScale.scaleWidth
    }
  }

}
</script>
<style scoped>

</style>
