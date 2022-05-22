<template>
  <div style="z-index:-1" :style="style" />
</template>

<script>
import { mapState } from 'vuex'
export default {
  replace: true,
  name: 'MoveInShadow',
  props: {
    w: {
      type: Number,
      required: true
    },
    h: {
      type: Number,
      required: true
    },
    x: {
      type: Number,
      required: true
    },
    y: {
      type: Number,
      required: true
    },
    z: {
      type: Number,
      required: true
    }
  },
  computed: {
    style() {
      // 当前默认为自适应
      let left = this.x
      let top = this.y
      let width = this.w
      let height = this.h
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
