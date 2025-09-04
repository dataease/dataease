<script setup lang="ts">
import { getStyle } from '@/utils/style'
import eventBus from '@/utils/eventBus'
import { ref, toRefs, computed, nextTick } from 'vue'
import findComponent from '@/utils/components'
import { downloadCanvas2, imgUrlTrans } from '@/utils/imgUtils'
import ComponentEditBar from '@/components/visualization/ComponentEditBar.vue'
import ComponentSelector from '@/components/visualization/ComponentSelector.vue'
import { useEmitt } from '@/hooks/web/useEmitt'
import Board from '@/components/de-board/Board.vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { activeWatermarkCheckUser, removeActiveWatermark } from '@/components/watermark/watermark'
import { isMobile } from '@/utils/utils'
import { isDashboard, isMainCanvas } from '@/utils/canvasUtils'
import { XpackComponent } from '@/components/plugin'
import DePreviewPopDialog from '@/components/visualization/DePreviewPopDialog.vue'
import Icon from '../../icon-custom/src/Icon.vue'
import replaceOutlined from '@/assets/svg/icon_replace_outlined.svg'

const componentWrapperInnerRef = ref(null)
const componentEditBarRef = ref(null)
const dvMainStore = dvMainStoreWithOut()
const downLoading = ref(false)
const commonFilterAttrs = ['width', 'height', 'top', 'left', 'rotate']
const dePreviewPopDialogRef = ref(null)
const commonFilterAttrsFilterBorder = [
  'width',
  'height',
  'top',
  'left',
  'rotate',
  'borderActive',
  'borderWidth',
  'borderRadius',
  'borderStyle',
  'borderColor'
]

const props = defineProps({
  curStyle: {
    type: Object
  },
  active: {
    type: Boolean,
    default: false
  },
  popActive: {
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
  },
  isSelector: {
    type: Boolean,
    default: false
  },
  //图表渲染id后缀
  suffixId: {
    type: String,
    required: false,
    default: 'common'
  },
  // 字体
  fontFamily: {
    type: String,
    required: false,
    default: 'inherit'
  },
  optType: {
    type: String,
    required: false
  },
  // 画布滚动距离
  scrollMain: {
    type: Number,
    default: 0
  }
})
const {
  config,
  showPosition,
  index,
  canvasStyleData,
  canvasViewInfo,
  dvInfo,
  searchCount,
  scale,
  suffixId,
  scrollMain
} = toRefs(props)
const component = ref(null)
const emits = defineEmits(['userViewEnlargeOpen', 'datasetParamsInit', 'onPointClick'])
const wrapperId = 'wrapper-outer-id-' + config.value.id

const viewDemoInnerId = computed(() => 'enlarge-inner-content-' + config.value.id)
const htmlToImage = () => {
  useEmitt().emitter.emit('l7-prepare-picture', config.value.id)
  downLoading.value = true
  setTimeout(() => {
    const vueDom = document.getElementById(viewDemoInnerId.value)
    activeWatermarkCheckUser(viewDemoInnerId.value, 'canvas-main', scale.value / 100)
    downloadCanvas2('img', vueDom, '图表', () => {
      // do callback
      removeActiveWatermark(viewDemoInnerId.value)
      downLoading.value = false
      useEmitt().emitter.emit('l7-unprepare-picture', config.value.id)
    })
  }, 1000)
}

const handleInnerMouseDown = e => {
  // do setCurComponent
  if (showPosition.value.includes('multiplexing')) {
    componentEditBarRef.value.multiplexingCheckOut()
    e?.stopPropagation()
    e?.preventDefault()
  }
  if (
    (!['rich-text'].includes(config.value.innerType) &&
      ['popEdit', 'preview'].includes(showPosition.value)) ||
    dvMainStore.mobileInPc
  ) {
    onClick(e)
    if (e.target?.className?.includes('ed-input__inner')) return
    e?.stopPropagation()
    e?.preventDefault()
  }
}

const onClick = () => {
  // 将当前点击组件的事件传播出去
  eventBus.emit('componentClick')
  dvMainStore.setInEditorStatus(true)
  dvMainStore.setClickComponentStatus(true)
  dvMainStore.setCurComponent({ component: config.value, index: index.value })
}

const getComponentStyleDefault = style => {
  if (config.value.component.includes('Svg')) {
    return getStyle(style, [
      'top',
      'left',
      'width',
      'height',
      'rotate',
      'backgroundColor',
      'borderWidth',
      'borderStyle',
      'borderColor'
    ])
  } else {
    return getStyle(style, style.borderActive ? commonFilterAttrs : commonFilterAttrsFilterBorder)
  }
}

const onMouseEnter = () => {
  eventBus.emit('v-hover', config.value.id)
}

const componentBackgroundStyle = computed(() => {
  if (config.value.commonBackground) {
    const {
      backdropFilterEnable,
      backdropFilter,
      backgroundColorSelect,
      backgroundColor,
      backgroundImageEnable,
      backgroundType,
      outerImage,
      innerPadding,
      borderRadius
    } = config.value.commonBackground
    let style = {
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
    if (config.value.component !== 'UserView') {
      style['overflow'] = 'hidden'
    }
    if (backdropFilterEnable) {
      style['backdrop-filter'] = 'blur(' + backdropFilter + 'px)'
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

const slotStyle = computed(() => {
  // 3d效果支持
  if (config.value['multiDimensional'] && config.value['multiDimensional']?.enable) {
    const width = config.value.style.width // 原始元素宽度
    const height = config.value.style.height // 原始元素高度
    const rotateX = config.value['multiDimensional'].x // 旋转X角度
    const rotateY = config.value['multiDimensional'].y // 旋转Y角度

    // 将角度转换为弧度
    const radX = (rotateX * Math.PI) / 180
    const radY = (rotateY * Math.PI) / 180

    // 计算旋转后新宽度和高度
    const newWidth = Math.abs(width * Math.cos(radY)) + Math.abs(height * Math.sin(radX))
    const newHeight = Math.abs(height * Math.cos(radX)) + Math.abs(width * Math.sin(radY))

    // 计算需要的 padding
    const paddingX = (newWidth - width) / 2
    const paddingY = (newHeight - height) / 2
    return {
      padding: `${paddingY}px ${paddingX}px`,
      transform: `rotateX(${config.value['multiDimensional'].x}deg) rotateY(${config.value['multiDimensional'].y}deg) rotateZ(${config.value['multiDimensional'].z}deg)`
    }
  } else {
    return {}
  }
})

const onPointClick = param => {
  emits('onPointClick', param)
}

const eventEnable = computed(
  () =>
    showPosition.value.includes('preview') &&
    (['Picture', 'CanvasIcon', 'CircleShape', 'SvgTriangle', 'RectShape', 'ScrollText'].includes(
      config.value.component
    ) ||
      ['indicator', 'rich-text'].includes(config.value.innerType)) &&
    config.value.events &&
    config.value.events.checked &&
    (isDashboard() || (!isDashboard() && !isMobile())) &&
    showPosition.value !== 'canvas-multiplexing'
)

const onWrapperClickCur = e => {
  // 指标卡为内部触发
  if (['indicator'].includes(config.value.innerType)) {
    return
  }
  onWrapperClick(e)
}

const onWrapperClick = e => {
  if (eventEnable.value) {
    if (config.value.events.type === 'showHidden') {
      // 打开弹框区域
      nextTick(() => {
        dvMainStore.popAreaActiveSwitch()
      })
    } else if (config.value.events.type === 'jump') {
      const url = config.value.events.jump.value
      const jumpType = config.value.events.jump.type
      try {
        if ('newPop' === jumpType) {
          dePreviewPopDialogRef.value.previewInit({ url, size: 'middle' })
        } else if ('_blank' === jumpType) {
          if (window['originOpen']) {
            window['originOpen'](url, '_blank')
          } else {
            window.open(url, '_blank')
          }
        } else {
          initOpenHandler(window.open(url, jumpType))
        }
      } catch (e) {
        console.warn('url 格式错误:' + url)
      }
    } else if (config.value.events.type === 'refreshDataV') {
      useEmitt().emitter.emit('componentRefresh')
    } else if (config.value.events.type === 'fullScreen') {
      useEmitt().emitter.emit('canvasFullscreen')
    } else if (config.value.events.type === 'download') {
      useEmitt().emitter.emit('canvasDownload')
    }
    e?.preventDefault()
    e?.stopPropagation()
  }
}

const openHandler = ref(null)
const initOpenHandler = newWindow => {
  if (openHandler?.value) {
    const pm = {
      methodName: 'initOpenHandler',
      args: newWindow
    }
    openHandler.value.invokeMethod(pm)
  }
}
const deepScale = computed(() => scale.value / 100)
const showActive = computed(() => props.popActive || (dvMainStore.mobileInPc && props.active))

const freezeFlag = computed(() => {
  return (
    isMainCanvas(props.canvasId) &&
    config.value.freeze &&
    !isMobile() &&
    scrollMain.value - config.value.style?.top > 0
  )
})

const commonParams = computed(() => {
  return {
    eventEnable: eventEnable.value,
    eventType: config.value.events.type
  }
})

const showCheck = computed(() => {
  return dvMainStore.mobileInPc && showPosition.value === 'edit'
})

const updateFromMobile = (e, type) => {
  if (type === 'syncPcDesign') {
    e.preventDefault()
    e.stopPropagation()
  }
  useEmitt().emitter.emit('onMobileStatusChange', {
    type: type,
    value: config.value.id
  })
}
</script>

<template>
  <div
    class="wrapper-outer"
    :class="[
      showPosition + '-' + config.component,
      {
        'freeze-component': freezeFlag
      }
    ]"
    :id="wrapperId"
    @mousedown="handleInnerMouseDown"
    @mouseenter="onMouseEnter"
    v-loading="downLoading"
    element-loading-text="导出中..."
    element-loading-background="rgba(255, 255, 255, 1)"
  >
    <div
      :title="$t('visualization.sync_pc_design')"
      v-if="showCheck"
      class="refresh-from-pc"
      @click="updateFromMobile($event, 'syncPcDesign')"
    >
      <el-icon>
        <Icon name="icon_replace_outlined"><replaceOutlined class="svg-icon" /></Icon>
      </el-icon>
    </div>
    <component-edit-bar
      v-if="!showPosition.includes('canvas') && !props.isSelector"
      class="wrapper-edit-bar"
      ref="componentEditBarRef"
      :canvas-id="canvasId"
      :index="index"
      :element="config"
      :show-position="showPosition"
      :class="{ 'wrapper-edit-bar-active': active }"
      @componentImageDownload="htmlToImage"
      @userViewEnlargeOpen="opt => emits('userViewEnlargeOpen', opt)"
      @datasetParamsInit="() => emits('datasetParamsInit')"
    ></component-edit-bar>
    <component-selector
      v-if="
        props.isSelector &&
        config.component === 'UserView' &&
        config.propValue?.innerType !== 'rich-text'
      "
      :resource-id="config.id"
    />
    <div
      class="wrapper-inner"
      ref="componentWrapperInnerRef"
      :id="viewDemoInnerId"
      :style="componentBackgroundStyle"
    >
      <div
        class="wrapper-inner-adaptor"
        :style="slotStyle"
        :class="{ 'pop-wrapper-inner': showActive, 'event-active': eventEnable }"
        @mousedown="onWrapperClickCur"
      >
        <component
          :is="findComponent(config['component'])"
          :view="viewInfo"
          ref="component"
          class="component"
          :canvas-style-data="canvasStyleData"
          :opt-type="optType"
          :dv-info="dvInfo"
          :dv-type="dvInfo.type"
          :canvas-view-info="canvasViewInfo"
          :style="getComponentStyleDefault(config?.style)"
          :curStyle="curStyle"
          :prop-value="config?.propValue"
          :element="config"
          :request="config?.request"
          :linkage="config?.linkage"
          :show-position="showPosition"
          :search-count="searchCount"
          :scale="deepScale"
          :disabled="true"
          :is-edit="false"
          :suffix-id="suffixId"
          :font-family="fontFamily"
          :active="active"
          :common-params="commonParams"
          @onPointClick="onPointClick"
          @onComponentEvent="onWrapperClick"
        />
      </div>
      <!--边框背景-->
      <Board
        v-if="svgInnerEnable"
        :style="{ color: config.commonBackground.innerImageColor, pointerEvents: 'none' }"
        :name="commonBackgroundSvgInner"
      ></Board>
    </div>
    <XpackComponent
      ref="openHandler"
      jsname="L2NvbXBvbmVudC9lbWJlZGRlZC1pZnJhbWUvT3BlbkhhbmRsZXI="
    />
    <DePreviewPopDialog ref="dePreviewPopDialogRef"></DePreviewPopDialog>
  </div>
</template>

<style lang="less" scoped>
.pop-wrapper-inner {
  overflow: hidden;
  outline: 1px solid var(--ed-color-primary) !important;
}
.wrapper-outer {
  position: absolute;
  .refresh-from-pc {
    position: absolute;
    right: 38px;
    top: 12px;
    z-index: 2;
    font-size: 16px;
    cursor: pointer;
    color: var(--ed-color-primary);
  }
}
.wrapper-inner {
  width: 100%;
  height: 100%;
  position: relative;
  background-size: 100% 100% !important;
  .wrapper-inner-adaptor {
    position: relative;
    transform-style: preserve-3d;
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
  z-index: 0;
  width: 100% !important;
  height: 100% !important;
}
.event-active {
  cursor: pointer;
}

.freeze-component {
  position: fixed;
  z-index: 1;
  top: var(--top-show-offset) px !important;
  left: var(--left-show-offset) px !important;
}
</style>
