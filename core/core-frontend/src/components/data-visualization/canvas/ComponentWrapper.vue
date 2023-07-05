<script setup lang="ts">
import { getStyle, getSVGStyle } from '@/utils/style'
import runAnimation from '@/utils/runAnimation'
import eventBus from '@/utils/eventBus'
import { ref, onMounted, toRefs, getCurrentInstance, computed } from 'vue'
import findComponent from '@/utils/components'
import { hexColorToRGBA } from '@/views/chart/components/js/util'
import { imgUrlTrans } from '@/utils/imgUtils'

const props = defineProps({
  config: {
    type: Object,
    required: true,
    default() {
      return {
        component: null,
        propValue: null,
        request: null,
        linkage: null,
        type: null,
        events: null,
        style: null,
        id: null,
        animations: null
      }
    }
  },
  viewInfo: {
    type: Object,
    required: false
  }
})
const component = ref(null)
const { config } = toRefs(props)
let currentInstance

onMounted(() => {
  runAnimation(component.value.$el, config.value.animations.type)
  currentInstance = getCurrentInstance()
})

const onClick = () => {
  const events = config.value.events
  Object.keys(events).forEach(event => {
    currentInstance.ctx[event](events[event])
  })
  eventBus.emit('v-click', config.value.id)
}

const getComponentStyleDefault = style => {
  return getStyle(style, ['top', 'left', 'width', 'height', 'rotate'])
}

const onMouseEnter = () => {
  eventBus.emit('v-hover', config.value.id)
}

const componentBackgroundStyle = computed(() => {
  const style = {}
  if (config.value.commonBackground) {
    const {
      backgroundColorSelect,
      backgroundColor,
      alpha,
      backgroundImageEnable,
      backgroundType,
      outerImage
    } = config.value.commonBackground
    let colorRGBA = ''
    if (backgroundColorSelect && backgroundColor) {
      colorRGBA =
        backgroundColor.indexOf('rgb') === -1
          ? hexColorToRGBA(backgroundColor, alpha)
          : backgroundColor
    }
    if (backgroundImageEnable) {
      if (backgroundType === 'outerImage' && typeof outerImage === 'string') {
        style['background'] = `url(${imgUrlTrans(outerImage)}) no-repeat ${colorRGBA}`
      } else {
        style['background-color'] = colorRGBA
      }
    } else {
      style['background-color'] = colorRGBA
    }
  }
  return style
})

const svgInnerEnable = computed(() => {
  const { backgroundImageEnable, backgroundType, innerImage } = config.value.commonBackground
  return backgroundImageEnable && backgroundType === 'innerImage' && typeof innerImage === 'string'
})

const commonBackgroundSvgInner = computed(() => {
  if (svgInnerEnable.value) {
    return config.value.commonBackground.innerImage.replace('board/', '').replace('.svg', '')
  } else {
    return null
  }
})
</script>

<template>
  <div class="wrapper-outer" @click="onClick" @mouseenter="onMouseEnter">
    <div class="wrapper-inner" :style="componentBackgroundStyle">
      <!--边框背景-->
      <Icon
        v-if="svgInnerEnable"
        :style="{ color: config.commonBackground.innerImageColor }"
        class-name="svg-background"
        :name="commonBackgroundSvgInner"
      ></Icon>

      <component
        :is="findComponent(config['component'])"
        :view="viewInfo"
        ref="component"
        class="component"
        :style="getComponentStyleDefault(config?.style)"
        :prop-value="config?.propValue"
        :element="config"
        :request="config?.request"
        :linkage="config?.linkage"
      />
    </div>
  </div>
</template>

<style lang="less" scoped>
.wrapper-outer {
  position: absolute;
}
.wrapper-inner {
  width: 100%;
  height: 100%;
  position: relative;
  background-size: 100% 100% !important;
}

.component {
}

.svg-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100% !important;
  height: 100% !important;
}
</style>
