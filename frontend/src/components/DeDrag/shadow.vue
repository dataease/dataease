<template>
  <div style="z-index:-1" :style="style" />
</template>

<script>
import { mapState } from 'vuex'
export default {
  replace: true,
  name: 'Shadow',
  computed: {
    style() {
      // 当前默认为自适应
      let left = this.curComponent.style.left * this.curCanvasScale.scaleWidth / 100
      let top = this.curComponent.style.top * this.curCanvasScale.scaleHeight / 100
      let width = this.curComponent.style.width * this.curCanvasScale.scaleWidth / 100
      let height = this.curComponent.style.height * this.curCanvasScale.scaleHeight / 100
      if (this.canvasStyleData.auxiliaryMatrix) {
        left = Math.round(left / this.curCanvasScale.matrixStyleWidth) * this.curCanvasScale.matrixStyleWidth
        width = Math.round(width / this.curCanvasScale.matrixStyleWidth) * this.curCanvasScale.matrixStyleWidth
        top = Math.round(top / this.curCanvasScale.matrixStyleHeight) * this.curCanvasScale.matrixStyleHeight
        height = Math.round(height / this.curCanvasScale.matrixStyleHeight) * this.curCanvasScale.matrixStyleHeight
      }
      const style = {
        transform: `translate(${left}px, ${top}px) rotate(0deg)`,
        width: width + 'px',
        height: height + 'px',
        opacity: 0.4,
        background: 'gray',
        position: 'absolute'
      }
      console.log('style=>' + JSON.stringify(style))
      return style
    },
    ...mapState([
      'curComponent',
      'editor',
      'curCanvasScale',
      'canvasStyleData',
      'linkageSettingStatus'
    ])
  }

}
</script>
<style scoped>

</style>
