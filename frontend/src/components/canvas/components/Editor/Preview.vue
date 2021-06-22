<template>
  <div id="canvasInfoTemp" :style="customStyle" class="bg">
    <el-row v-if="componentDataShow.length===0" style="height: 100%;" class="custom-position">
      {{ $t('panel.panelNull') }}
    </el-row>
    <ComponentWrapper
      v-for="(item, index) in componentDataInfo"
      :key="index"
      :config="item"
      :search-count="searchCount"
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
    },
    showType: {
      type: String,
      required: false,
      default: 'full'
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
      componentDataShow: [],
      mainWidth: '100%',
      mainHeight: '100%',
      searchCount: -1
    }
  },
  computed: {
    customStyle() {
      let style = {
        margin: 'auto',
        width: this.mainWidth,
        height: this.mainHeight
      }
      if (this.canvasStyleData.openCommonStyle) {
        if (this.canvasStyleData.panel.backgroundType === 'image' && this.canvasStyleData.panel.imageUrl) {
          style = {
            background: `url(${this.canvasStyleData.panel.imageUrl}) no-repeat`,
            ...style
          }
        } else if (this.canvasStyleData.panel.backgroundType === 'color') {
          style = {
            background: this.canvasStyleData.panel.color,
            ...style
          }
        }
      }
      return style
    },
    // 此处单独计算componentData的值 不放入全局mapState中
    componentDataInfo() {
      return this.componentDataShow
    },
    ...mapState([
      'componentData',
      'canvasStyleData'
    ])
  },
  watch: {
    componentData: {
      handler(newVal, oldVla) {
        this.restore()
      },
      deep: true
    }
  },
  mounted() {
    const _this = this
    const erd = elementResizeDetectorMaker()
    // 监听div变动事件
    erd.listenTo(document.getElementById('canvasInfoTemp'), element => {
      _this.$nextTick(() => {
        _this.restore()
      })
    })
    // 数据刷新计时器
    let refreshTime = 300000
    if (this.canvasStyleData.refreshTime && this.canvasStyleData.refreshTime > 0) {
      refreshTime = this.canvasStyleData.refreshTime * 1000
    }
    this.timer = setInterval(() => {
      this.searchCount++
    }, refreshTime)
  },
  beforeDestroy() {
    clearInterval(this.timer)
  },
  methods: {
    changeStyleWithScale,
    getStyle,
    restore() {
      const canvasHeight = document.getElementById('canvasInfoTemp').offsetHeight
      const canvasWidth = document.getElementById('canvasInfoTemp').offsetWidth
      this.scaleWidth = canvasWidth * 100 / parseInt(this.canvasStyleData.width)// 获取宽度比
      this.scaleHeight = canvasHeight * 100 / parseInt(this.canvasStyleData.height)// 获取高度比
      if (this.showType === 'width') {
        this.scaleHeight = this.scaleWidth
        this.mainHeight = this.canvasStyleData.height * this.scaleHeight / 100 + 'px'
      }
      this.handleScaleChange()
    },
    resetID(data) {
      if (data) {
        data.forEach(item => {
          item.type !== 'custom' && (item.id = uuid.v1())
        })
      }
      return data
    },
    format(value, scale) {
      return value * parseInt(scale) / 100
    },
    handleScaleChange() {
      if (this.componentData) {
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
        this.$nextTick(() => (eventBus.$emit('resizing', '')))
      }
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
    /*border: 1px solid #E6E6E6;*/
    background-size: 100% 100% !important;
    .canvas-container {
        width: 100%;
        height: 100%;
      .canvas {
            position: relative;
            margin: auto;
        }
    }
}
.custom-position {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  flex-flow: row nowrap;
  color: #9ea6b2;
}
.gap_class{
  padding:3px;
}
</style>
