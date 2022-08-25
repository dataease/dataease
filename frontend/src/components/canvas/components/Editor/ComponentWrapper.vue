<template>
  <div
    :style="getOutStyleDefault(config.style)"
    class="component"
    @click="handleClick"
    @mousedown="elementMouseDown"
  >
    <edit-bar v-if="componentActiveFlag" :source-element="sourceConfig" :terminal="terminal" :element="config" :show-position="showPosition" @showViewDetails="showViewDetails" />
    <div :id="componentCanvasId" :style="commonStyle" class="main_view">
      <svg-icon v-if="svgInnerEnable" :style="{'color':this.config.commonBackground.innerImageColor}" class="svg-background" :icon-class="mainSlotSvgInner" />
      <close-bar v-if="previewVisible" @closePreview="closePreview" />
      <de-out-widget
        v-if="config.type==='custom'"
        :id="'component' + config.id"
        ref="deOutWidget"
        class="component-custom"
        :style="getComponentStyleDefault(config.style)"
        style="overflow: hidden"
        :out-style="config.style"
        :is-relation="isRelation"
        :element="config"
        :in-screen="inScreen"
        :edit-mode="'preview'"
        :h="config.style.height"
      />
      <component
        :is="config.component"
        v-else
        ref="wrapperChild"
        class="component"
        :out-style="config.style"
        :style="getComponentStyleDefault(config.style)"
        :prop-value="config.propValue"
        :is-edit="false"
        :in-screen="inScreen"
        :active="componentActiveFlag"
        :element="config"
        :search-count="searchCount"
        :h="config.style.height"
        :edit-mode="'preview'"
        :filters="filters"
        :terminal="terminal"
        :screen-shot="screenShot"
        :canvas-style-data="canvasStyleData"
        :show-position="showPosition"
      />
    </div>
  </div>
</template>

<script>
import { getStyle } from '@/components/canvas/utils/style'
import runAnimation from '@/components/canvas/utils/runAnimation'
import { mixins } from '@/components/canvas/utils/events'
import { mapState } from 'vuex'
import DeOutWidget from '@/components/dataease/DeOutWidget'
import EditBar from '@/components/canvas/components/Editor/EditBar'
import MobileCheckBar from '@/components/canvas/components/Editor/MobileCheckBar'
import CloseBar from '@/components/canvas/components/Editor/CloseBar'
import { hexColorToRGBA } from '@/views/chart/chart/util'
import {imgUrlTrans} from "@/components/canvas/utils/utils";

export default {
  components: { CloseBar, MobileCheckBar, DeOutWidget, EditBar },
  mixins: [mixins],
  props: {
    sourceConfig: {
      type: Object,
      require: true,
      default: null
    },
    config: {
      type: Object,
      require: true,
      default: null
    },
    filter: {
      type: Object,
      require: false,
      default: null
    },
    searchCount: {
      type: Number,
      required: false,
      default: 0
    },
    inScreen: {
      type: Boolean,
      required: false,
      default: true
    },
    terminal: {
      type: String,
      default: 'pc'
    },
    filters: {
      type: Array,
      default: () => []
    },
    screenShot: {
      type: Boolean,
      default: false
    },
    canvasStyleData: {
      type: Object,
      required: false,
      default: function() {
        return {}
      }
    },
    showPosition: {
      type: String,
      required: false,
      default: 'NotProvided'
    },
    isRelation: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      previewVisible: false
    }
  },
  computed: {
    componentCanvasId() {
      if (this.config.type === 'view') {
        return 'user-view-' + this.config.propValue.viewId
      } else {
        return 'components-' + this.config.id
      }
    },
    svgInnerEnable() {
      return !this.screenShot&&this.config.commonBackground.enable && this.config.commonBackground.backgroundType === 'innerImage' && typeof this.config.commonBackground.innerImage === 'string'
    },
    mainSlotSvgInner() {
      if (this.svgInnerEnable) {
        return this.config.commonBackground.innerImage.replace('board/', '').replace('.svg', '')
      } else {
        return null
      }
    },
    commonStyle() {
      const style = {
        width: '100%',
        height: '100%'
      }
      if (this.config.commonBackground) {
        style['padding'] = (this.config.commonBackground.innerPadding || 0) + 'px'
        style['border-radius'] = (this.config.commonBackground.borderRadius || 0) + 'px'
        let colorRGBA = ''
        if (this.config.commonBackground.backgroundColorSelect) {
          colorRGBA = hexColorToRGBA(this.config.commonBackground.color, this.config.commonBackground.alpha)
        }
        if (this.config.commonBackground.enable) {
          if (this.screenShot && this.config.commonBackground.backgroundType === 'innerImage' && typeof this.config.commonBackground.innerImage === 'string') {
            let innerImage = this.config.commonBackground.innerImage.replace('svg', 'png')
            style['background'] = `url(${imgUrlTrans(innerImage)}) no-repeat ${colorRGBA}`
          } else if (this.config.commonBackground.backgroundType === 'outerImage' && typeof this.config.commonBackground.outerImage === 'string') {
            style['background'] = `url(${imgUrlTrans(this.config.commonBackground.outerImage)}) no-repeat ${colorRGBA}`
          } else {
            style['background-color'] = colorRGBA
          }
        } else {
          style['background-color'] = colorRGBA
        }
        style['overflow'] = 'hidden'
      }
      return style
    },
    componentActiveFlag() {
      return (this.curComponent && this.config === this.curComponent && !this.previewVisible && !this.showPosition.includes('email-task')) || this.showPosition.includes('multiplexing')
    },
    curGap() {
      return (this.canvasStyleData.panel.gap === 'yes' && this.config.auxiliaryMatrix) ? this.componentGap : 0
    },
    ...mapState([
      'mobileLayoutStatus',
      'curComponent',
      'componentGap'
    ])
  },
  mounted() {
    runAnimation(this.$el, this.config.animations)
  },
  methods: {
    getStyle,
    getShapeStyleIntDeDrag(style, prop) {
      if (prop === 'rotate') {
        return style['rotate']
      }
      if (prop === 'width') {
        return this.format(style['width'], this.scaleWidth)
      }
      if (prop === 'left') {
        return this.format(style['left'], this.scaleWidth)
      }
      if (prop === 'height') {
        return this.format(style['height'], this.scaleHeight)
      }
      if (prop === 'top') {
        const top = this.format(style['top'], this.scaleHeight)
        return top
      }
    },
    format(value, scale) {
      // 自适应画布区域 返回原值
      return value * scale / 100
    },
    getOutStyleDefault(style) {
      const result = {
        padding: this.curGap + 'px'
      }
      // 移动端编辑状态 且 未被移动端选中的组件 放满容器
      if (this.mobileLayoutStatus && !this.config.mobileSelected) {
        result.width = '100%'
        result.height = '100%'
      } else {
        ['width', 'left'].forEach(attr => {
          result[attr] = style[attr] + 'px'
        });
        ['height', 'top'].forEach(attr => {
          result[attr] = style[attr] + 'px'
        })
        result['rotate'] = style['rotate']
      }
      return result
    },

    getComponentStyleDefault(style) {
      // 移动端编辑状态 且 未被移动端选中的组件 放满容器
      if (this.mobileLayoutStatus && !this.config.mobileSelected) {
        return {
          width: '100%',
          height: '100%'
        }
      } else {
        return {
          ...
          getStyle(style, ['top', 'left', 'width', 'height', 'rotate']),
          position: 'relative'
        }
      }
    },

    handleClick() {
      const events = this.config.events
      if (events) {
        Object.keys(events).forEach(event => {
          this[event](events[event])
        })
      }
    },
    elementMouseDown(e) {
      // private 设置当前组件数据及状态
      this.$store.commit('setClickComponentStatus', true)
      if (this.config.component !== 'v-text' && this.config.component !== 'rect-shape' && this.config.component !== 'de-input-search' && this.config.component !== 'de-select-grid' && this.config.component !== 'de-number-range' && this.config.component !== 'de-date') {
        e.preventDefault()
      }
      // 阻止冒泡事件
      e.stopPropagation()
      this.$store.commit('setCurComponent', { component: this.config, index: this.index })
    },
    showViewDetails(params) {
      this.$refs.wrapperChild.openChartDetailsDialog(params)
    },
    closePreview() {
      this.previewVisible = false
    },
    getCondition() {
      if (this.$refs.deOutWidget && this.$refs.deOutWidget.getCondition) {
        return this.$refs.deOutWidget.getCondition()
      } else {
        return null
      }
    },
    clearHandler() {
      if (this.$refs.deOutWidget && this.$refs.deOutWidget.clearHandler) {
        this.$refs.deOutWidget.clearHandler()
      }
    }
  }
}
</script>

<style lang="scss" scoped>
  .component {
    position: absolute;
  }

  .component:hover {
    box-shadow: 0px 0px 3px #0a7be0;
  }

  .gap_class {
    padding: 5px;
  }

  .component-custom {
    outline: none;
    width: 100% !important;
    height: 100%;
  }
  .main_view{
    position: relative;
    background-size: 100% 100%!important;
  }
  .component{
    //position: relative;
  }

  .svg-background {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
  }
</style>
