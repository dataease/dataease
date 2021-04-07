<template>
  <div ref="element" class="bg">
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
      componentData: {},
      canvasStyleData: {}

    }
  },
  computed: {
    componentDataInfo() {
      return this.componentData
    }
  },
  mounted() {
    // 加载数据
    this.restore()
    window.onresize = () => {
      debugger
      this.resize()
    }
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
      debugger
      this.panelId = this.$route.path.split('/')[2]
      // 加载视图数据
      get('panel/group/findOne/' + this.panelId).then(response => {
        this.componentData = this.resetID(JSON.parse(response.data.panelData))
        this.canvasStyleData = JSON.parse(response.data.panelStyle)
        this.resize()
      })
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
      this.componentData = componentData
      eventBus.$emit('resizing', '')
    }
  }
}
</script>

<style lang="scss" scoped>
  .bg {
    width: 100%;
    height: 100%;
    overflow: auto;
    position: relative;
    margin: 0;
  }
</style>
