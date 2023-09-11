<script setup lang="ts">
import { getStyle, getSVGStyle } from '@/utils/style'
import runAnimation from '@/utils/runAnimation'
import eventBus from '@/utils/eventBus'
import { ref, onMounted, toRefs, getCurrentInstance, computed } from 'vue'
import findComponent from '@/utils/components'
import { hexColorToRGBA } from '@/views/chart/components/js/util'
import { imgUrlTrans } from '@/utils/imgUtils'
import ComponentEditBar from '@/components/visualization/ComponentEditBar.vue'

const props = defineProps({
  canvasStyleData: {
    type: Object,
    required: true
  },
  canvasViewInfo: {
    type: Object,
    required: true
  },
  dvInfo: {
    type: Object,
    required: true
  },
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
  },
  index: {
    required: true,
    type: [Number, String],
    default: 0
  },
  showPosition: {
    required: false,
    type: String,
    default: 'preview'
  },
  canvasId: {
    type: String,
    default: 'canvas-main'
  },
  // 仪表板刷新计时器
  searchCount: {
    type: Number,
    required: false,
    default: 0
  }
})
const { config, showPosition, index, canvasStyleData, canvasViewInfo, dvInfo, searchCount } =
  toRefs(props)
let currentInstance
const component = ref(null)
const emits = defineEmits(['userViewEnlargeOpen'])

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
  if (config.value.commonBackground) {
    const {
      backgroundColorSelect,
      backgroundColor,
      alpha,
      backgroundImageEnable,
      backgroundType,
      outerImage,
      innerPadding,
      borderRadius
    } = config.value.commonBackground
    const style = { padding: innerPadding + 'px', borderRadius: borderRadius + 'px' }
    let colorRGBA = ''
    if (backgroundColorSelect && backgroundColor) {
      colorRGBA = hexColorToRGBA(backgroundColor, alpha)
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
    return style
  }
  return {}
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
    <div class="wrapper-inner" :class="showPosition" :style="componentBackgroundStyle">
      <component-edit-bar
        class="wrapper-edit-bar"
        :canvas-id="canvasId"
        :index="index"
        :element="config"
        :show-position="showPosition"
        @userViewEnlargeOpen="() => emits('userViewEnlargeOpen')"
      ></component-edit-bar>
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
        :canvas-style-data="canvasStyleData"
        :dv-info="dvInfo"
        :canvas-view-info="canvasViewInfo"
        :style="getComponentStyleDefault(config?.style)"
        :prop-value="config?.propValue"
        :element="config"
        :request="config?.request"
        :linkage="config?.linkage"
        :show-position="showPosition"
        :search-count="searchCount"
      />
    </div>
  </div>
</template>

<style lang="less" scoped>
.wrapper-outer {
  z-index: 5;
  position: absolute;
  overflow: hidden;
}
.wrapper-inner {
  width: 100%;
  height: 100%;
  position: relative;
  background-size: 100% 100% !important;
}

.preview {
  .wrapper-edit-bar {
    display: none;
  }
  &:hover .wrapper-edit-bar {
    display: inherit !important;
  }
}

.multiplexing {
  .wrapper-edit-bar {
    display: inherit;
  }
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
