<template>
  <div class="shape">
    <div
      class="shape-inner"
      :class="{ active, 'shape-edit': isEditMode }"
      :style="componentBackgroundStyle"
      @click="selectCurComponent"
      @mousedown="handleMouseDownOnShape"
    >
      <component-edit-bar
        v-if="componentActiveFlag"
        :index="index"
        :element="element"
        :show-position="showPosition"
        @userViewEnlargeOpen="userViewEnlargeOpen"
      ></component-edit-bar>
      <span v-show="element['isLock']" class="iconfont icon-suo"></span>
      <div
        v-for="item in isActive() ? getPointList() : []"
        :key="item"
        class="shape-point"
        :style="getPointStyle(item)"
        @mousedown="handleMouseDownOnPoint(item, $event)"
      ></div>
      <!--边框背景-->
      <Icon
        v-if="svgInnerEnable"
        :style="{ color: element.commonBackground.innerImageColor }"
        class-name="svg-background"
        :name="commonBackgroundSvgInner"
      ></Icon>
      <slot></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import eventBus from '@/utils/eventBus'
import calculateComponentPositionAndSize from '@/utils/calculateComponentPositionAndSize'
import { mod360 } from '@/utils/translate'
import { isPreventDrop } from '@/utils/utils'
import { computed, nextTick, onMounted, ref, toRefs, reactive } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { contextmenuStoreWithOut } from '@/store/modules/data-visualization/contextmenu'
import { composeStoreWithOut } from '@/store/modules/data-visualization/compose'
import { storeToRefs } from 'pinia'
import { hexColorToRGBA } from '@/views/chart/components/js/util'
import { imgUrlTrans } from '@/utils/imgUtils'
import Icon from '@/components/icon-custom/src/Icon.vue'
import ComponentEditBar from '@/components/visualization/ComponentEditBar.vue'
const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const contextmenuStore = contextmenuStoreWithOut()
const composeStore = composeStoreWithOut()

const { curComponent, dvInfo, editMode } = storeToRefs(dvMainStore)
const { editor } = storeToRefs(composeStore)
const emit = defineEmits([
  'userViewEnlargeOpen',
  'onStartResize',
  'onStartMove',
  'onDragging',
  'onResizing',
  'onMouseUp'
])

const isEditMode = computed(() => editMode.value === 'edit')
const state = reactive({
  seriesIdMap: {
    id: ''
  }
})

const showPosition = computed(() => (isEditMode.value ? 'canvas' : 'preview'))

const props = defineProps({
  active: {
    type: Boolean,
    default: false
  },
  element: {
    required: true,
    type: Object,
    default() {
      return {
        component: null,
        propValue: null,
        request: null,
        linkage: null,
        type: null,
        events: null,
        style: null,
        id: null
      }
    }
  },
  defaultStyle: {
    required: true,
    type: Object,
    default() {
      return {}
    }
  },
  baseCellInfo: {
    required: true,
    type: Object,
    default() {
      return {
        baseWidth: 0,
        baseHeight: 0,
        curGap: 0
      }
    }
  },
  index: {
    required: true,
    type: [Number, String],
    default: 0
  }
})

const { active, element, defaultStyle, baseCellInfo, index } = toRefs(props)

const pointList = ['lt', 't', 'rt', 'r', 'rb', 'b', 'lb', 'l']
const pointList2 = ['r', 'l']
const initialAngle = {
  // 每个点对应的初始角度
  lt: 0,
  t: 45,
  rt: 90,
  r: 135,
  rb: 180,
  b: 225,
  lb: 270,
  l: 315
}
const cursors = ref({})

const angleToCursor = [
  // 每个范围的角度对应的光标
  { start: 338, end: 23, cursor: 'nw' },
  { start: 23, end: 68, cursor: 'n' },
  { start: 68, end: 113, cursor: 'ne' },
  { start: 113, end: 158, cursor: 'e' },
  { start: 158, end: 203, cursor: 'se' },
  { start: 203, end: 248, cursor: 's' },
  { start: 248, end: 293, cursor: 'sw' },
  { start: 293, end: 338, cursor: 'w' }
]

const dashboardActive = computed(() => {
  return dvInfo.value.type === 'dashboard'
})

const getPointList = () => {
  return element.value.component === 'line-shape' ? pointList2 : pointList
}

const isActive = () => {
  return active.value && !element.value['isLock'] && isEditMode.value
}

// 处理旋转
const handleRotate = () => {
  //doNothing
}

const userViewEnlargeOpen = () => {
  emit('userViewEnlargeOpen')
}

const getPointStyle = point => {
  let { width, height } = defaultStyle.value
  const { sizeX, sizeY } = element.value
  if (dashboardActive.value && !element.value['isPlayer']) {
    width = sizeX * baseCellInfo.value.baseWidth - 2 * baseCellInfo.value.curGap
    height = sizeY * baseCellInfo.value.baseHeight - 2 * baseCellInfo.value.curGap
  } else {
    width = width - 2 * baseCellInfo.value.curGap
    height = height - 2 * baseCellInfo.value.curGap
  }
  const hasT = /t/.test(point)
  const hasB = /b/.test(point)
  const hasL = /l/.test(point)
  const hasR = /r/.test(point)
  let newLeft = 0
  let newTop = 0

  // 四个角的点
  if (point.length === 2) {
    newLeft = hasL ? 0 : width
    newTop = hasT ? 0 : height
  } else {
    // 上下两点的点，宽度居中
    if (hasT || hasB) {
      newLeft = width / 2
      newTop = hasT ? 0 : height
    }

    // 左右两边的点，高度居中
    if (hasL || hasR) {
      newLeft = hasL ? 0 : width
      newTop = Math.floor(height / 2)
    }
  }

  const style = {
    marginLeft: '-4px',
    marginTop: '-4px',
    left: `${newLeft}px`,
    top: `${newTop}px`,
    cursor: cursors.value[point]
  }

  return style
}

const getCursor = () => {
  const rotate = mod360(curComponent.value.style.rotate) // 取余 360
  const result = {}
  let lastMatchIndex = -1 // 从上一个命中的角度的索引开始匹配下一个，降低时间复杂度

  pointList.forEach(point => {
    const angle = mod360(initialAngle[point] + rotate)
    const len = angleToCursor.length
    // eslint-disable-next-line no-constant-condition
    while (true) {
      lastMatchIndex = (lastMatchIndex + 1) % len
      const angleLimit = angleToCursor[lastMatchIndex]
      if (angle < 23 || angle >= 338) {
        result[point] = 'nw-resize'

        return
      }

      if (angleLimit.start <= angle && angle < angleLimit.end) {
        result[point] = angleLimit.cursor + '-resize'

        return
      }
    }
  })

  return result
}

const handleMouseDownOnShape = e => {
  dashboardActive.value && emit('onStartMove', e)
  // 将当前点击组件的事件传播出去
  nextTick(() => eventBus.emit('componentClick'))
  dvMainStore.setInEditorStatus(true)
  dvMainStore.setClickComponentStatus(true)
  if (isPreventDrop(element.value.component)) {
    e.preventDefault()
  }

  e.stopPropagation()
  dvMainStore.setCurComponent({ component: element.value, index: index.value })
  if (element.value['isLock'] || !isEditMode.value) return

  cursors.value = getCursor() // 根据旋转角度获取光标位置

  const pos = { ...defaultStyle.value }
  const startY = e.clientY
  const startX = e.clientX
  // 如果直接修改属性，值的类型会变为字符串，所以要转为数值型
  const startTop = Number(pos['top'])
  const startLeft = Number(pos['left'])

  // 如果元素没有移动，则不保存快照
  let hasMove = false
  const move = moveEvent => {
    dashboardActive.value && emit('onDragging', e)
    hasMove = true
    const curX = moveEvent.clientX
    const curY = moveEvent.clientY
    pos['top'] = curY - startY + startTop
    pos['left'] = curX - startX + startLeft

    // 修改当前组件样式
    dvMainStore.setShapeStyle(pos)
    // 等更新完当前组件的样式并绘制到屏幕后再判断是否需要吸附
    // 如果不使用 $nextTick，吸附后将无法移动
    nextTick(() => {
      // 触发元素移动事件，用于显示标线、吸附功能
      // 后面两个参数代表鼠标移动方向
      // curY - startY > 0 true 表示向下移动 false 表示向上移动
      // curX - startX > 0 true 表示向右移动 false 表示向左移动
      eventBus.emit('move', { isDownward: curY - startY > 0, isRightward: curX - startX > 0 })
    })
  }

  const up = () => {
    dashboardActive.value && emit('onMouseUp')
    hasMove && snapshotStore.recordSnapshot()
    // 触发元素停止移动事件，用于隐藏标线
    eventBus.emit('unMove')
    document.removeEventListener('mousemove', move)
    document.removeEventListener('mouseup', up)
  }

  document.addEventListener('mousemove', move)
  document.addEventListener('mouseup', up)
}

const selectCurComponent = e => {
  // 阻止向父组件冒泡
  e.stopPropagation()
  e.preventDefault()
  contextmenuStore.hideContextMenu()
}

const handleMouseDownOnPoint = (point, e) => {
  dashboardActive.value && emit('onStartResize', e)
  dvMainStore.setInEditorStatus(true)
  dvMainStore.setClickComponentStatus(true)
  e.stopPropagation()
  e.preventDefault()

  const style = { ...defaultStyle.value }

  // 组件宽高比
  const proportion = style['width'] / style['height']

  // 组件中心点
  const center = {
    x: style['left'] + style['width'] / 2,
    y: style['top'] + style['height'] / 2
  }

  // 获取画布位移信息
  const editorRectInfo = editor.value.getBoundingClientRect()

  // 获取 point 与实际拖动基准点的差值
  const pointRect = e.target.getBoundingClientRect()
  // 当前点击圆点相对于画布的中心坐标
  const curPoint = {
    x: Math.round(
      pointRect.left -
        editorRectInfo.left +
        e.target.offsetWidth / 2 +
        offsetGapAdaptor('x', point) / 2
    ),
    y: Math.round(
      pointRect.top -
        editorRectInfo.top +
        e.target.offsetHeight / 2 +
        offsetGapAdaptor('y', point) / 2
    )
  }

  // 获取对称点的坐标 problem point
  const symmetricPoint = {
    x: center.x - (curPoint.x - center.x) - offsetGapAdaptor('x', point) / 4,
    y: center.y - (curPoint.y - center.y) - offsetGapAdaptor('y', point) / 4
  }

  // 是否需要保存快照
  let needSave = false
  let isFirst = true

  const needLockProportion = isNeedLockProportion()
  const move = moveEvent => {
    // 第一次点击时也会触发 move，所以会有“刚点击组件但未移动，组件的大小却改变了”的情况发生
    // 因此第一次点击时不触发 move 事件
    if (isFirst) {
      isFirst = false
      return
    }

    needSave = true
    const curPosition = {
      x: moveEvent.clientX - Math.round(editorRectInfo.left) + offsetGapAdaptor('x', point),
      y: moveEvent.clientY - Math.round(editorRectInfo.top) + offsetGapAdaptor('y', point)
    }
    calculateComponentPositionAndSize(point, style, curPosition, proportion, needLockProportion, {
      center,
      curPoint,
      symmetricPoint
    })
    dvMainStore.setShapeStyle(style)
    dashboardActive.value && emit('onResizing', moveEvent)
  }

  const up = () => {
    dashboardActive.value && emit('onMouseUp')
    document.removeEventListener('mousemove', move)
    document.removeEventListener('mouseup', up)
    needSave && snapshotStore.recordSnapshot()
  }

  document.addEventListener('mousemove', move)
  document.addEventListener('mouseup', up)
}

// resize算法适配，根据9个拖转点的位置 调整curGap 引起的中心点centerPoint 圆点curPoint 对称点 symmetricPoint引起的偏移
const offsetGapAdaptor = (dimension, point) => {
  const curGap = baseCellInfo.value.curGap
  if (dimension === 'x') {
    return point.indexOf('r') > -1 ? curGap : -1 * curGap
  } else {
    return point.indexOf('b') > -1 ? curGap : -1 * curGap
  }
}

const isNeedLockProportion = () => {
  if (element.value.component != 'Group') return false
  const rotates = [0, 90, 180, 360]
  for (const component of element.value.propValue) {
    if (!rotates.includes(mod360(parseInt(component.style.rotate)))) {
      return true
    }
  }

  return false
}

const svgInnerEnable = computed(() => {
  const { backgroundImageEnable, backgroundType, innerImage } = element.value.commonBackground
  return backgroundImageEnable && backgroundType === 'innerImage' && typeof innerImage === 'string'
})

const commonBackgroundSvgInner = computed(() => {
  if (svgInnerEnable.value) {
    return element.value.commonBackground.innerImage.replace('board/', '').replace('.svg', '')
  } else {
    return null
  }
})

const componentBackgroundStyle = computed(() => {
  const style = {}
  if (element.value.commonBackground) {
    const {
      backgroundColorSelect,
      backgroundColor,
      alpha,
      backgroundImageEnable,
      backgroundType,
      outerImage
    } = element.value.commonBackground
    let colorRGBA = ''
    if (backgroundColorSelect && backgroundColor && backgroundColor.indexOf('rgb') === -1) {
      colorRGBA = hexColorToRGBA(backgroundColor, alpha)
    } else {
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
  }
  return style
})

const componentActiveFlag = computed(() => {
  return active.value && dashboardActive.value
})

const showViewDetails = () => {
  return null
}

onMounted(() => {
  // 用于 Group 组件
  if (curComponent.value) {
    cursors.value = getCursor() // 根据旋转角度获取光标位置
  }
  eventBus.on('runAnimation', () => {
    if (element.value == curComponent.value) {
      // runAnimation(this.$el, curComponent.value.animations)
    }
  })
  eventBus.on('stopAnimation', () => {
    // this.$el.classList.remove('animated', 'infinite')
  })
})
</script>

<style lang="less" scoped>
.shape {
  position: absolute;
}

.shape-inner {
  width: 100%;
  height: 100%;
  position: relative;
  background-size: 100% 100% !important;
}

.shape-edit {
  &:hover {
    cursor: move;
    outline: 1px dashed #70c0ff;
  }
}

.active {
  outline: 1px solid #70c0ff !important;
  user-select: none;
}

.shape-point {
  position: absolute;
  background: #fff;
  border: 1px solid #59c7f9;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  z-index: 1;
}

.icon-xiangyouxuanzhuan {
  position: absolute;
  top: -34px;
  left: 50%;
  transform: translateX(-50%);
  cursor: grab;
  color: #59c7f9;
  font-size: 20px;
  font-weight: 600;

  &:active {
    cursor: grabbing;
  }
}

.icon-suo {
  position: absolute;
  top: 0;
  right: 0;
}

.svg-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100% !important;
  height: 100% !important;
}
</style>
