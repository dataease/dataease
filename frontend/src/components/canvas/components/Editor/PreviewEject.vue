<template>
  <div id="canvasInfo" :style="customStyle" class="bg">
    <ComponentWrapper
      v-for="(item, index) in componentDataInfo"
      :key="index"
      :config="item"
    />
  </div>
</template>

<script>
import { getStyle } from '@/components/canvas/utils/style'
import ComponentWrapper from './ComponentWrapper'
import { changeStyleWithScale } from '@/components/canvas/utils/translate'
import { uuid } from 'vue-uuid'
import { deepCopy } from '@/components/canvas/utils/utils'
import eventBus from '@/components/canvas/utils/eventBus'
import elementResizeDetectorMaker from 'element-resize-detector'
import { get } from '@/api/panel/panel'

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
      componentDataSource: [],
      componentData: [],
      canvasStyleData: {}

    }
  },
  computed: {
    customStyle() {
      let style = {}
      if (this.canvasStyleData.openCommonStyle) {
        if (this.canvasStyleData.panel.backgroundType === 'image'&&this.canvasStyleData.panel.imageUrl) {
          style = {
            width: '100%',
            height: '100%',
            background: `url(${this.canvasStyleData.panel.imageUrl}) no-repeat`
          }
        } else {
          style = {
            width: '100%',
            height: '100%',
            background: this.canvasStyleData.panel.color
          }
        }
      }
      return style
    },
    componentDataInfo() {
      return this.componentData
    }
  },
  mounted() {
    const _this = this

    // 加载数据
    _this.restore()
    const erd = elementResizeDetectorMaker()
    // 监听div变动事件
    erd.listenTo(document.getElementById('canvasInfo'), element => {
      _this.$nextTick(() => {
        _this.resize()
      })
    })
    // this.resize()
  },
  methods: {
    changeStyleWithScale,
    getStyle,
    close() {
      this.$emit('change', false)
    },
    resize() {
      this.scaleWidth = window.innerWidth * 100 / parseInt(this.canvasStyleData.width)// 获取宽度比
      this.scaleHeight = window.innerHeight * 100 / parseInt(this.canvasStyleData.height)// 获取高度比
      this.handleScaleChange()
    },
    restore() {
      this.panelId = this.$route.path.split('/')[2]
      // 加载视图数据
      get('panel/group/findOne/' + this.panelId).then(response => {
        this.componentDataSource = this.resetID(JSON.parse(response.data.panelData))
        this.canvasStyleData = JSON.parse(response.data.panelStyle)
        this.$store.commit('setCanvasStyle', {
          ...this.canvasStyleData
        })
        this.resize()
      })
    },
    resetID(data) {
      if (data) {
        data.forEach(item => {
          item.id = uuid.v1()
        })
      }
      return data
    },

    format(value, scale) {
      return value * parseInt(scale) / 100
    },
    handleScaleChange() {
      const componentData = deepCopy(this.componentDataSource)
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
      this.componentData = componentData
      this.$nextTick(() => (eventBus.$emit('resizing', '')))
    }
  }
}
</script>

<style lang="scss" scoped>
  .bg {
    min-width: 800px;
    min-height: 600px;
    width: 100%;
    height: 100%;
    background-size: 100% 100% !important;
    overflow: auto;
    position: relative;
    margin: 0;
    background-color: #f7f8fa;
  }
</style>
