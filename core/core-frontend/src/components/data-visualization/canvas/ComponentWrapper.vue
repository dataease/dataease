<script setup lang="ts">
import { getStyle } from '@/utils/style'
import eventBus from '@/utils/eventBus'
import { ref, onMounted, toRefs, getCurrentInstance, computed } from 'vue'
import findComponent from '@/utils/components'
import { downloadCanvas, imgUrlTrans } from '@/utils/imgUtils'
import ComponentEditBar from '@/components/visualization/ComponentEditBar.vue'
import { useEmitt } from '@/hooks/web/useEmitt'

const componentWrapperInnerRef = ref(null)
const componentEditBarRef = ref(null)

const props = defineProps({
  active: {
    type: Boolean,
    default: false
  },
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
  },
  scale: {
    type: Number,
    required: false,
    default: 100
  }
})
const { config, showPosition, index, canvasStyleData, canvasViewInfo, dvInfo, searchCount, scale } =
  toRefs(props)
let currentInstance
const component = ref(null)
const emits = defineEmits(['userViewEnlargeOpen'])

const htmlToImage = () => {
  setTimeout(() => {
    const vueDom = componentWrapperInnerRef.value
    downloadCanvas('img', vueDom, '图表')
  }, 200)
}

const handleInnerMouseDown = e => {
  // do setCurComponent
  if (showPosition.value.includes('multiplexing')) {
    componentEditBarRef.value.multiplexingCheckOut()
    e.stopPropagation()
    e.preventDefault()
  }
}

onMounted(() => {
  currentInstance = getCurrentInstance()
  useEmitt({
    name: 'componentImageDownload-' + config.value.id,
    callback: () => {
      htmlToImage()
    }
  })
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
      backgroundImageEnable,
      backgroundType,
      outerImage,
      innerPadding,
      borderRadius
    } = config.value.commonBackground
    const style = {
      padding: innerPadding * deepScale.value + 'px',
      borderRadius: borderRadius + 'px'
    }
    let colorRGBA = ''
    if (backgroundColorSelect && backgroundColor) {
      colorRGBA = backgroundColor
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

const deepScale = computed(() => scale.value / 100)
</script>

<template>
  <div
    class="wrapper-outer"
    :class="showPosition + '-' + config.component"
    @click="onClick"
    @mousedown="handleInnerMouseDown"
    @mouseenter="onMouseEnter"
  >
    <component-edit-bar
      v-if="!showPosition.includes('canvas') && dvInfo.type === 'dashboard'"
      class="wrapper-edit-bar"
      ref="componentEditBarRef"
      :class="{ 'wrapper-edit-bar-active': active }"
      :canvas-id="canvasId"
      :index="index"
      :element="config"
      :show-position="showPosition"
      @userViewEnlargeOpen="opt => emits('userViewEnlargeOpen', opt)"
    ></component-edit-bar>
    <div class="wrapper-inner" ref="componentWrapperInnerRef" :style="componentBackgroundStyle">
      <!--边框背景-->
      <Icon
        v-if="svgInnerEnable"
        :style="{ color: config.commonBackground.innerImageColor }"
        class-name="svg-background"
        :name="commonBackgroundSvgInner"
      ></Icon>
      <div class="wrapper-inner-adaptor">
        <component
          :is="findComponent(config['component'])"
          :view="viewInfo"
          ref="component"
          class="component"
          :canvas-style-data="canvasStyleData"
          :dv-info="dvInfo"
          :dv-type="dvInfo.type"
          :canvas-view-info="canvasViewInfo"
          :style="getComponentStyleDefault(config?.style)"
          :prop-value="config?.propValue"
          :element="config"
          :request="config?.request"
          :linkage="config?.linkage"
          :show-position="showPosition"
          :search-count="searchCount"
          :scale="deepScale"
          :disabled="true"
          :is-edit="false"
        />
      </div>
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
  .wrapper-inner-adaptor {
    position: relative;
    width: 100%;
    height: 100%;
  }
}

.wrapper-edit-bar-active {
  display: inherit !important;
}
.preview-UserView {
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
  width: 100% !important;
  height: 100% !important;
  overflow: hidden;
}

.svg-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100% !important;
  height: 100% !important;
}
</style>
