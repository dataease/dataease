<template>
  <div class="component-item">
    <mobile-check-bar v-if="mobileCheckBarShow" :element="config" />
    <div :style="commonStyle" class="inner-item">
      <svg-icon v-if="svgInnerEnable" :style="{'color':this.config.commonBackground.innerImageColor}" class="svg-background" :icon-class="mainSlotSvgInner" />
      <de-out-widget
        v-if="config.type==='custom'"
        :id="'component' + config.id"
        class="component-custom"
        :style="getComponentStyleDefault(config.style)"
        :out-style="outStyle"
        :element="config"
        :in-screen="true"
      />
      <component
        :is="config.component"
        v-else
        ref="wrapperChild"
        :out-style="outStyle"
        :prop-value="config.propValue"
        :style="getComponentStyleDefault(config.style)"
        :is-edit="false"
        :element="config"
        :h="outItemHeight"
        :canvas-style-data="canvasStyleData"
      />

    </div>

  </div>
</template>

<script>
import { mapState } from 'vuex'
import MobileCheckBar from '@/components/canvas/components/Editor/MobileCheckBar'
import { getStyle } from '@/components/canvas/utils/style'
import DeOutWidget from '@/components/dataease/DeOutWidget'
import { hexColorToRGBA } from '@/views/chart/chart/util'
import {imgUrlTrans} from "@/components/canvas/utils/utils";

export default {
  name: 'ComponentWaitItem',
  components: { DeOutWidget, MobileCheckBar },
  props: {
    config: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      itemWidth: 280,
      itemHeight: 200,
      outStyle: {
        width: this.itemWidth,
        height: this.itemHeight
      }
    }
  },
  computed: {
    svgInnerEnable() {
      return this.config.commonBackground.enable && this.config.commonBackground.backgroundType === 'innerImage' && typeof this.config.commonBackground.innerImage === 'string'
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
          if (this.config.commonBackground.backgroundType === 'outerImage' && typeof this.config.commonBackground.outerImage === 'string') {
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
    outItemHeight() {
      return this.itemHeight - (4 * this.componentGap)
    },
    // 移动端编辑组件选择按钮显示
    mobileCheckBarShow() {
      // 显示条件：1.当前是移动端画布编辑状态
      return this.mobileLayoutStatus
    },
    componentDataWaite() {
      const result = []
      this.componentData.forEach(item => {
        if (!item.mobileSelected) {
          result.push(item)
        }
      })
      return result
    },
    ...mapState([
      'mobileLayoutStatus',
      'componentData',
      'canvasStyleData',
      'componentGap'
    ])
  },
  methods: {
    getComponentStyleDefault(style) {
      return {
        ...
        getStyle(style, ['top', 'left', 'width', 'height', 'rotate']),
        position: 'relative'
      }
    }
  }
}
</script>

<style scoped>
  .component-custom {
    position: relative!important;
    outline: none;
    width: 100% !important;
    height: 100%;
  }
  .component-item {
    padding: 5px;
    height: 200px!important;
    position: relative;
  }

  .inner-item {
    position: relative;
  }
  .svg-background {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
  }
</style>
