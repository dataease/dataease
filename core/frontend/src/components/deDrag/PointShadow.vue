<template>
  <div
    class="point-shadow"
    :style="styleInfo"
  >
    <div class="point-shadow-content">
      <div
        id="point-shadow-component"
        class="point-shadow-component"
      />
      <div
        class="point-shadow-tips"
        :style="tipsStyleInfo"
      >
        <div style="width: 100%;text-align: center">组件将被移出Tab</div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'

export default {
  replace: true,
  name: 'PointShadow',
  props: {
    canvasId: {
      type: String,
      required: true
    }
  },
  computed: {
    tipsStyleInfo() {
      return {
        width: this.mousePointShadowMap.width + 'px',
        height: this.mousePointShadowMap.height + 'px'
      }
    },
    curCanvasScaleSelf() {
      return this.curCanvasScaleMap[this.canvasId]
    },
    componentCanvasId() {
      if (this.curComponent.type === 'view') {
        return 'user-view-' + this.curComponent.propValue.viewId
      } else {
        return 'components-' + this.curComponent.id
      }
    },
    styleInfo() {
      return {
        left: this.mousePointShadowMap.mouseX - (this.mousePointShadowMap.width) + 'px',
        top: this.mousePointShadowMap.mouseY - (this.mousePointShadowMap.height / 2) + 'px',
        width: this.mousePointShadowMap.width + 'px',
        height: this.mousePointShadowMap.height + 'px'
      }
    },
    ...mapState([
      'mousePointShadowMap',
      'curComponent'
    ])
  },
  mounted() {
    this.init()
  },
  beforeDestroy() {
  },
  methods: {
    init() {
      // 将拖拽的元素内容复制到shadow区域展示
      // const targetComponent = document.getElementById(this.componentCanvasId)
      // document.getElementById('point-shadow-component').appendChild(targetComponent)
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
.point-shadow {
  z-index: 1000;
  position: absolute;
}

.point-shadow-content {
  position: relative;
}

.point-shadow-component {
  opacity: 0.6;
  background-color: rgba(179, 212, 252);
}

.point-shadow-tips {
  left: 0px;
  top: 0px;
  box-sizing: border-box;
  z-index: 10001;
  display: flex;
  align-items: center;
  position: absolute;
  color: #33ef08;
  font-weight: bold;
  background-color: rgba(179, 212, 252);
}

</style>
