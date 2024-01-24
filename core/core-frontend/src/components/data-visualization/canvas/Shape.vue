<template>
  <div class="shape" ref="shapeInnerRef" :id="domId" @dblclick="handleDbClick">
    <div
      class="shape-outer"
      v-show="contentDisplay"
      :class="{
        active,
        'shape-lock': shapeLock,
        'shape-edit': isEditMode && !boardMoveActive,
        'linkage-setting': linkageActive,
        'drag-on-tab-collision': dragCollision
      }"
    >
      <component-edit-bar
        ref="componentEditBarRef"
        class="edit-bar"
        :class="{ 'edit-bar-active': editBarShowFlag }"
        :index="index"
        :element="element"
        :show-position="showPosition"
        :canvas-id="canvasId"
        @userViewEnlargeOpen="userViewEnlargeOpen"
        @linkJumpSetOpen="linkJumpSetOpen"
        @linkageSetOpen="linkageSetOpen"
      ></component-edit-bar>
      <div
        class="shape-inner"
        ref="componentInnerRef"
        :style="componentBackgroundStyle"
        @click="selectCurComponent"
        @mousedown="handleInnerMouseDownOnShape"
      >
        <Icon v-show="shapeLock" class="iconfont icon-suo" name="dv-lock"></Icon>
        <!--边框背景-->
        <Icon
          v-if="svgInnerEnable"
          :style="{ color: element.commonBackground.innerImageColor }"
          class-name="svg-background"
          :name="commonBackgroundSvgInner"
        ></Icon>
        <div class="component-slot">
          <slot></slot>
        </div>
      </div>
      <div
        v-for="item in isActive() ? getPointList() : []"
        :key="item"
        class="shape-point"
        :style="getPointStyle(item)"
        @mousedown="handleMouseDownOnPoint(item, $event)"
      ></div>
      <div class="shape-shadow" v-show="batchOptStatus" @mousedown="batchSelected"></div>
      <template v-if="boardMoveActive">
        <div
          v-show="!element.editing"
          class="de-drag-area de-drag-top"
          @mousedown="handleBoardMouseDownOnShape"
        />
        <div
          v-show="!element.editing && element.component !== 'DeTabs'"
          class="de-drag-area de-drag-right"
          @mousedown="handleBoardMouseDownOnShape"
        />
        <div
          v-show="!element.editing && element.component !== 'DeTabs'"
          class="de-drag-area de-drag-bottom"
          @mousedown="handleBoardMouseDownOnShape"
        />
        <div
          v-show="!element.editing && element.component !== 'DeTabs'"
          class="de-drag-area de-drag-left"
          @mousedown="handleBoardMouseDownOnShape"
        />
      </template>
    </div>
    <compose-show :element="element"></compose-show>
  </div>
</template>

<script setup lang="ts">
import eventBus from '@/utils/eventBus'
import calculateComponentPositionAndSize from '@/utils/calculateComponentPositionAndSize'
import { mod360 } from '@/utils/translate'
import { deepCopy } from '@/utils/utils'
import { computed, nextTick, onMounted, ref, toRefs, reactive } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { contextmenuStoreWithOut } from '@/store/modules/data-visualization/contextmenu'
import { composeStoreWithOut } from '@/store/modules/data-visualization/compose'
import { storeToRefs } from 'pinia'
import { downloadCanvas, imgUrlTrans } from '@/utils/imgUtils'
import Icon from '@/components/icon-custom/src/Icon.vue'
import ComponentEditBar from '@/components/visualization/ComponentEditBar.vue'
import { useEmitt } from '@/hooks/web/useEmitt'
import ComposeShow from '@/components/data-visualization/canvas/ComposeShow.vue'
import { groupSizeStyleAdaptor, groupStyleRevert } from '@/utils/style'
import { isGroupCanvas, isMainCanvas } from '@/utils/canvasUtils'
const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const contextmenuStore = contextmenuStoreWithOut()
const composeStore = composeStoreWithOut()
const parentNode = ref(null)
const shapeInnerRef = ref(null)
const componentInnerRef = ref(null)
const componentEditBarRef = ref(null)

const {
  curComponent,
  dvInfo,
  editMode,
  batchOptStatus,
  linkageSettingStatus,
  curLinkageView,
  tabCollisionActiveId,
  tabMoveInActiveId,
  tabMoveOutComponentId
} = storeToRefs(dvMainStore)
const { editorMap, areaData, isCtrlOrCmdDown } = storeToRefs(composeStore)
const emit = defineEmits([
  'userViewEnlargeOpen',
  'onStartResize',
  'onStartMove',
  'onDragging',
  'onResizing',
  'onMouseUp',
  'linkJumpSetOpen',
  'linkageSetOpen'
])
const isEditMode = computed(() => editMode.value === 'edit')
const state = reactive({
  seriesIdMap: {
    id: ''
  },
  // 禁止移入Tab中的组件
  ignoreTabMoveComponent: ['de-button', 'de-reset-button', 'DeTabs'],
  // 当画布在tab中是 宽度左右拓展的余量
  parentWidthTabOffset: 40,
  canvasChangeTips: 'none',
  tabMoveInYOffset: 70,
  tabMoveInXOffset: 40,
  collisionGap: 10 // 碰撞深度有效区域,
})

const contentDisplay = ref(true)

const shapeLock = computed(() => {
  return element.value['isLock'] && isEditMode.value
})

const showPosition = computed(() => {
  let position
  if (batchOptStatus.value) {
    position = 'batchOpt'
  } else if (isEditMode.value) {
    position = dvInfo.value.type === 'dashboard' ? 'canvas' : 'canvasDataV'
  } else {
    position = 'preview'
  }
  return position
})

const props = defineProps({
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
  },
  // tab 移入检测
  isTabMoveCheck: {
    type: Boolean,
    default: true
  },
  canvasId: {
    type: String,
    default: 'canvas-main'
  },
  scale: {
    type: Number,
    required: false,
    default: 1
  },
  canvasActive: {
    type: Boolean,
    required: false,
    default: true
  }
})

const {
  element,
  defaultStyle,
  baseCellInfo,
  index,
  isTabMoveCheck,
  canvasId,
  scale,
  canvasActive
} = toRefs(props)
const domId = ref('shape-id-' + element.value.id)
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

const active = computed(() => {
  return curComponent.value?.id === element.value.id
})

const boardMoveActive = computed(() => {
  return ['map', 'table-info', 'table-normal', 'table-pivot'].includes(element.value.innerType)
})

const dashboardActive = computed(() => {
  return dvInfo.value.type === 'dashboard'
})

const getPointList = () => {
  return element.value.component === 'line-shape' ? pointList2 : pointList
}

const isActive = () => {
  return active.value && !element.value['isLock'] && isEditMode.value
}

const userViewEnlargeOpen = opt => {
  emit('userViewEnlargeOpen', opt)
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

const handleBoardMouseDownOnShape = e => {
  if (!canvasActive.value) {
    return
  }
  dvMainStore.setCurComponent({ component: element.value, index: index.value })
  handleMouseDownOnShape(e)
}

const areaDataPush = component => {
  if (component && !component.isLock && component.isShow && component.canvasId === 'canvas-main') {
    areaData.value.components.push(component)
  }
}
const handleDbClick = e => {
  if (element.value.canvasId !== 'canvas-main') {
    dvMainStore.setCurComponent({ component: element.value, index: index.value })
  }
}

const handleInnerMouseDownOnShape = e => {
  if (!canvasActive.value) {
    return
  }
  if (dvMainStore.batchOptStatus) {
    componentEditBarRef.value.batchOptCheckOut()
    e.stopPropagation()
    e.preventDefault()
    return
  }
  // ctrl or command 按下时 鼠标点击为选择需要组合的组件(取消需要组合的组件在ComposeShow组件中)
  if (isCtrlOrCmdDown.value && !areaData.value.components.includes(element)) {
    areaDataPush(element.value)
    if (curComponent.value && curComponent.value.id !== element.value.id) {
      areaDataPush(curComponent.value)
    }
    dvMainStore.setCurComponent({ component: null, index: null })
    e.stopPropagation()
    return
  }
  dvMainStore.setCurComponent({ component: element.value, index: index.value })
  // 边界区域拖拽 返回
  if (boardMoveActive.value) {
    dvMainStore.setInEditorStatus(true)
    dvMainStore.setClickComponentStatus(true)
    e.stopPropagation()
    return
  }
  handleMouseDownOnShape(e)
}

const handleMouseDownOnShape = e => {
  if (element.value['editing']) {
    // e.preventDefault()
    e.stopPropagation()
    return
  }
  dashboardActive.value && emit('onStartMove', e)
  // 将当前点击组件的事件传播出去
  nextTick(() => eventBus.emit('componentClick'))
  dvMainStore.setInEditorStatus(true)
  dvMainStore.setClickComponentStatus(true)
  // if (isPreventDrop(element.value.component)) {
  //   e.preventDefault()
  // }

  e.stopPropagation()
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
  let isFirst = true

  // 画布宽高
  const canvasWidth = parentNode.value.offsetWidth
  //当前组件宽高 定位
  const componentWidth = shapeInnerRef.value.offsetWidth
  const componentHeight = shapeInnerRef.value.offsetHeight
  const move = moveEvent => {
    hasMove = true
    const curX = moveEvent.clientX
    const curY = moveEvent.clientY
    const top = curY - startY + startTop
    const left = curX - startX + startLeft
    pos['top'] = top
    pos['left'] = left
    // 非主画布非分组画布的情况 需要检测是否从Tab中移除组件(向左移除30px 或者向右移除30px)
    if (
      !isMainCanvas(canvasId.value) &&
      !isGroupCanvas(canvasId.value) &&
      (left < -30 || left + componentWidth - canvasWidth > 30)
    ) {
      contentDisplay.value = false
      dvMainStore.setMousePointShadowMap({
        mouseX: curX,
        mouseY: curY,
        width: componentWidth,
        height: componentHeight
      })
      const tabComponentId = element.value.canvasId.split('--')[0]
      dvMainStore.setTabMoveOutComponentId(tabComponentId)
    } else {
      dvMainStore.setTabMoveOutComponentId(null)
      contentDisplay.value = true
    }
    // 仪表板进行Tab碰撞检查
    dashboardActive.value && tabMoveInCheck()
    // 仪表板模式 会造成移动现象 当检测组件正在碰撞有效区内或者移入有效区内 则周边组件不进行移动
    if (
      dashboardActive.value &&
      (isFirst || (!tabMoveInActiveId.value && !tabCollisionActiveId.value))
    ) {
      emit('onDragging', e)
    }

    //如果当前组件是Group分组 则要进行内部组件深度计算
    element.value.component === 'Group' && groupSizeStyleAdaptor(element.value)
    //如果当前画布是Group内部画布 则对应组件定位在resize时要还原到groupStyle中
    if (isGroupCanvas(canvasId.value)) {
      groupStyleRevert(element.value, {
        width: parentNode.value.offsetWidth,
        height: parentNode.value.offsetHeight
      })
    }

    // 防止首次组件在tab旁边无法触发矩阵移动
    if (isFirst) {
      isFirst = false
    }
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
    hasMove && snapshotStore.recordSnapshotCache('shape-handleMouseDownOnShape-up')
    // 触发元素停止移动事件，用于隐藏标线
    eventBus.emit('unMove')
    document.removeEventListener('mousemove', move)
    document.removeEventListener('mouseup', up)

    //如果当前存在移入的Tab 则将该组件加入到tab中 同时将该组件在主画布中进行删除
    if (tabMoveInActiveId.value) {
      eventBus.emit('onTabMoveIn-' + tabMoveInActiveId.value, element.value)
      dvMainStore.setTabMoveInActiveId(null)
      dvMainStore.setTabCollisionActiveId(null)
    }

    //如果当前存在移出的Tab 则将该组件加入到主画布中 同时将该组件在tab画布中进行删除
    if (tabMoveOutComponentId.value) {
      eventBus.emit('onTabMoveOut-' + tabMoveOutComponentId.value, deepCopy(element.value))
      dvMainStore.setTabMoveOutComponentId(null)
    }
    handleGroupComponent()
  }

  document.addEventListener('mousemove', move)
  document.addEventListener('mouseup', up)
}

const selectCurComponent = e => {
  if (!canvasActive.value) {
    return
  }
  // 阻止向父组件冒泡
  if (dvInfo.value.type === 'dataV') {
    e.stopPropagation()
    e.preventDefault()
    contextmenuStore.hideContextMenu()
  }
}

const batchSelected = e => {
  if (dvMainStore.batchOptStatus) {
    componentEditBarRef.value.batchOptCheckOut()
    e.stopPropagation()
    e.preventDefault()
    return
  }
}

const handleMouseDownOnPoint = (point, e) => {
  if (!canvasActive.value) {
    return
  }
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
  const editorRectInfo = editorMap.value[canvasId.value].getBoundingClientRect()

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
    //Temp dataV坐标偏移
    offsetDataVAdaptor(style, point)
    dvMainStore.setShapeStyle(style)
    // 矩阵逻辑 如果当前是仪表板（矩阵模式）则要进行矩阵重排
    dashboardActive.value && emit('onResizing', moveEvent)
    //如果当前组件是Group分组 则要进行内部组件深度计算
    element.value.component === 'Group' && groupSizeStyleAdaptor(element.value)
    //如果当前画布是Group内部画布 则对应组件定位在resize时要还原到groupStyle中
    if (isGroupCanvas(canvasId.value)) {
      groupStyleRevert(element.value, {
        width: parentNode.value.offsetWidth,
        height: parentNode.value.offsetHeight
      })
    }
  }

  const up = () => {
    dashboardActive.value && emit('onMouseUp')
    document.removeEventListener('mousemove', move)
    document.removeEventListener('mouseup', up)
    needSave && snapshotStore.recordSnapshotCache('shape-handleMouseDownOnPoint-up')
    handleGroupComponent()
  }

  document.addEventListener('mousemove', move)
  document.addEventListener('mouseup', up)
}

const offsetDataVAdaptor = (style, point) => {
  if (!dashboardActive.value) {
    const offset = 1
    if (point.indexOf('r') > -1) {
      style['left'] = style['left'] + offset
    }
    if (point.indexOf('b') > -1) {
      style['top'] = style['top'] + offset
    }
    if (point.indexOf('l') > -1) {
      style['width'] = style['width'] + offset
    }
    if (point.indexOf('t') > -1) {
      style['height'] = style['height'] + offset
    }
  }
}

// resize算法适配，根据9个拖转点的位置 调整curGap 引起的中心点centerPoint 圆点curPoint 对称点 symmetricPoint引起的偏移
const offsetGapAdaptor = (dimension, point) => {
  const offset = baseCellInfo.value.curGap
  if (dimension === 'x') {
    return point.indexOf('r') > -1 ? offset : -1 * offset
  } else {
    return point.indexOf('b') > -1 ? offset : -1 * offset
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
  if (element.value.commonBackground) {
    const {
      backgroundColorSelect,
      backgroundColor,
      backgroundImageEnable,
      backgroundType,
      outerImage,
      innerPadding,
      borderRadius
    } = element.value.commonBackground
    const style = { padding: innerPadding * scale.value + 'px', borderRadius: borderRadius + 'px' }
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

const editBarShowFlag = computed(() => {
  return (
    ((active.value || batchOptStatus.value) &&
      ['canvas', 'canvasDataV', 'batchOpt'].includes(showPosition.value)) ||
    linkageSettingStatus.value
  )
})

const linkageActive = computed(() => {
  return linkageSettingStatus.value && element.value.id === curLinkageView.value.id
})

const linkJumpSetOpen = () => {
  emit('linkJumpSetOpen')
}
const linkageSetOpen = () => {
  emit('linkageSetOpen')
}

// 设置属性(属性跟随所属canvas component类型 要做出改变)
const settingAttribute = () => {
  // 设置Tab移入检测
  shapeInnerRef.value.setAttribute('tab-is-check', `${isTabMoveCheck.value}`)
  // 设置组件类型
  shapeInnerRef.value.setAttribute('component-type', `${element.value.component}`)
  // 设置组件ID
  shapeInnerRef.value.setAttribute('component-id', `${element.value.id}`)
}

const tabMoveInCheck = async () => {
  const curNode = document.querySelector('#' + domId.value)
  const width = curNode.offsetWidth
  const height = curNode.offsetHeight
  const left = curNode.offsetLeft
  const top = curNode.offsetTop
  // tab 移入检测开启 tab组件不能相互移入另一个tab组件
  if (isTabMoveCheck.value && !state.ignoreTabMoveComponent.includes(element.value.component)) {
    const nodes = Array.from(parentNode.value.childNodes) // 获取当前父节点下所有子节点
    for (const item of nodes) {
      if (
        item.className !== undefined &&
        item.className.split(' ').includes('shape') &&
        item.getAttribute('component-id') !== domId.value && // 去掉当前
        item.getAttribute('tab-is-check') !== null &&
        item.getAttribute('tab-is-check') !== 'false' &&
        item.getAttribute('component-type') === 'DeTabs'
      ) {
        const componentId = item.getAttribute('component-id')

        const tw = item.offsetWidth
        const th = item.offsetHeight
        const tl = item.offsetLeft
        const tt = item.offsetTop

        // 碰撞有效区域检查
        const collisionT = tt + state.tabMoveInYOffset
        const collisionL = tl + state.collisionGap - width
        const collisionW = tw + 2 * width - state.collisionGap
        const collisionH = th + height - state.tabMoveInYOffset

        // 左上角靠近左上角区域
        const tfAndTf = collisionT <= top && collisionL <= left
        // 左下角靠近左下角区域
        const bfAndBf = collisionT + collisionH >= top + height && collisionL <= left
        // 右上角靠近右上角区域
        const trAndTr = collisionT <= top && collisionL + collisionW >= left + width
        // 右下角靠近右下角区域
        const brAndBr =
          collisionT + collisionH >= top + height && collisionL + collisionW >= left + width
        if (tfAndTf && bfAndBf && trAndTr && brAndBr) {
          dvMainStore.setTabCollisionActiveId(componentId)
        } else if (tabCollisionActiveId.value === componentId) {
          dvMainStore.setTabCollisionActiveId(null)
        }

        // 移入有效区域检查
        // 碰撞有效区域检查
        const activeT = tt + state.tabMoveInYOffset
        const activeL = tl + state.collisionGap * 10 - width
        const activeW = tw + 2 * width - state.collisionGap * 20
        const activeH = th + height - 2 * state.tabMoveInYOffset

        // 左上角靠近左上角区域
        const activeTfAndTf = activeT <= top && activeL <= left
        // 左下角靠近左下角区域
        const activeBfAndBf = activeT + activeH >= top + height && activeL <= left
        // 右上角靠近右上角区域
        const activeTrAndTr = activeT <= top && activeL + activeW >= left + width
        // 右下角靠近右下角区域
        const activeBrAndBr = activeT + activeH >= top + height && activeL + activeW >= left + width
        if (activeTfAndTf && activeBfAndBf && activeTrAndTr && activeBrAndBr) {
          dvMainStore.setTabMoveInActiveId(componentId)
        } else if (tabMoveInActiveId.value === componentId) {
          dvMainStore.setTabMoveInActiveId(null)
        }
      }
    }
  }
}

const dragCollision = computed(() => {
  return active.value && Boolean(tabCollisionActiveId.value)
})

const htmlToImage = () => {
  setTimeout(() => {
    downloadCanvas('img', componentInnerRef.value, '图表')
  }, 200)
}

const handleGroupComponent = () => {
  if (element.value.canvasId.includes('Group')) {
    composeStore.updateGroupBorder()
  }
}

onMounted(() => {
  parentNode.value = document.querySelector('#editor-' + canvasId.value)
  // 用于 Group 组件
  if (curComponent.value) {
    cursors.value = getCursor() // 根据旋转角度获取光标位置
  }
  eventBus.on('runAnimation', () => {
    if (element.value == curComponent.value) {
    }
  })
  eventBus.on('stopAnimation', () => {
    // do stopAnimation
  })
  settingAttribute()
  useEmitt({
    name: 'componentImageDownload-' + element.value.id,
    callback: () => {
      htmlToImage()
    }
  })
})
</script>

<style lang="less" scoped>
.shape {
  position: absolute;
}

.shape-shadow {
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  position: absolute;
  background-size: 100% 100% !important;
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
    outline: 1px solid rgba(51, 112, 255, 0.6);
  }
}

.shape-lock {
  &:hover {
    cursor: not-allowed !important;
  }
}

.active {
  outline: 1px solid rgba(51, 112, 255, 1) !important;
  user-select: none;
}

.shape-point {
  position: absolute;
  background: #fff;
  border: 1px solid #59c7f9;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  z-index: 0;
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
  width: 22px;
  height: 22px;
  z-index: 1;
  position: absolute;
  top: 0;
  right: 0;
}

.svg-background {
  position: absolute;
  z-index: 0;
  top: 0;
  left: 0;
  width: 100% !important;
  height: 100% !important;
}

.shape-outer {
  width: 100%;
  height: 100%;
  position: relative;
  .edit-bar {
    display: none;
  }
}

.linkage-setting {
  opacity: 0.5;
}

.drag-on-tab-collision {
  z-index: 1000 !important;
}
.edit-bar-active {
  display: inline-block !important;
}

.de-drag-area {
  position: absolute;
}

.de-drag-area:hover {
  cursor: move;
}

.de-drag-top {
  left: 1px;
  top: 1px;
  height: 12px;
  width: calc(100% - 2px);
}

.de-drag-right {
  right: 1px;
  top: 1px;
  width: 16px;
  height: calc(100% - 50px);
}

.de-drag-bottom {
  left: 100px;
  bottom: 1px;
  height: 12px;
  width: calc(100% - 102px);
}

.de-drag-left {
  left: 1px;
  top: 1px;
  width: 16px;
  height: calc(100% - 40px);
}

.component-slot {
  width: 100%;
  height: 100%;
  position: relative;
}
</style>
