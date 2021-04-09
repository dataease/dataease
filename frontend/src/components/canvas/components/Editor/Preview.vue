<template>
  <div id="canvasInfo" class="bg">
    <ComponentWrapper
      v-for="(item, index) in componentDataInfo"
      :key="index"
      :config="item"
    />
  </div>
</template>

<script>
import { getStyle } from '@/components/canvas/utils/style'
import { mapState } from 'vuex'
import ComponentWrapper from './ComponentWrapper'
import { changeStyleWithScale } from '@/components/canvas/utils/translate'
import { uuid } from 'vue-uuid'
import { deepCopy } from '@/components/canvas/utils/utils'
import eventBus from '@/components/canvas/utils/eventBus'
import elementResizeDetectorMaker from 'element-resize-detector'

export default {
  components: { ComponentWrapper },
  model: {
    prop: 'show',
    event: 'change'
  },
  props: {
    show: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      isShowPreview: false,
      panelId: '',
      needToChangeHeight: [
        'top',
        'height',
        'fontSize',
        'borderWidth'
      ],
      needToChangeWidth: [
        'left',
        'width'
      ],
      scaleWidth: '100',
      scaleHeight: '100',
      timer: null,
      componentDataShow: []
    }
  },
  computed: {
    // 此处单独计算componentData的值 不放入全局mapState中
    componentDataInfo() {
      return this.componentDataShow
    },
    ...mapState([
      'componentData',
      'canvasStyleData'
    ])
  },
  mounted() {
    debugger
    const _this = this
    const erd = elementResizeDetectorMaker()
    // 监听div变动事件
    erd.listenTo(document.getElementById('canvasInfo'), element => {
      _this.$nextTick(() => {
        debugger
        _this.restore()
      })
    })
  },
  methods: {
    changeStyleWithScale,
    getStyle,
    restore() {
      const canvasHeight = document.getElementById('canvasInfo').offsetHeight
      const canvasWidth = document.getElementById('canvasInfo').offsetWidth
      this.scaleWidth = canvasWidth * 100 / parseInt(this.canvasStyleData.width)// 获取宽度比
      this.scaleHeight = canvasHeight * 100 / parseInt(this.canvasStyleData.height)// 获取高度比
      this.handleScaleChange()
    },
    resetID(data) {
      data.forEach(item => {
        item.id = uuid.v1()
      })
      return data
    },
    format(value, scale) {
      return value * parseInt(scale) / 100
    },
    handleScaleChange() {
      const componentData = deepCopy(this.componentData)
      componentData.forEach(component => {
        Object.keys(component.style).forEach(key => {
          if (this.needToChangeHeight.includes(key)) {
            component.style[key] = this.format(component.style[key], this.scaleHeight)
          }
          if (this.needToChangeWidth.includes(key)) {
            component.style[key] = this.format(component.style[key], this.scaleWidth)
          }
        })
      })
      this.componentDataShow = componentData
      eventBus.$emit('resizing', '')
    }
  }
}
</script>

<style lang="scss" scoped>
.bg {
    min-width: 600px;
    min-height: 300px;
    width: 100%;
    height: 100%;
    border: 1px solid #E6E6E6;
    .canvas-container {
        width: 100%;
        height: 100%;
      .canvas {
            position: relative;
            margin: auto;
        }
    }
}
</style>
