<script setup lang="ts">
import Shape from './Shape.vue'
import { useEmitt } from '@/hooks/web/useEmitt'
import {
  getStyle,
  getComponentRotatedStyle,
  getShapeItemStyle,
  getCanvasStyle,
  syncShapeItemStyle
} from '@/utils/style'
import $ from 'jquery'
import { _$, isPreventDrop } from '@/utils/utils'
import ContextMenu from './ContextMenu.vue'
import MarkLine from './MarkLine.vue'
import Area from './Area.vue'
import eventBus from '@/utils/eventBus'
import { changeStyleWithScale } from '@/utils/translate'
import { ref, onMounted, computed, toRefs, nextTick, onBeforeUnmount, watch } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { composeStoreWithOut } from '@/store/modules/data-visualization/compose'
import { contextmenuStoreWithOut } from '@/store/modules/data-visualization/contextmenu'
import { storeToRefs } from 'pinia'
import findComponent from '@/utils/components'
import _ from 'lodash'
import DragShadow from '@/components/data-visualization/canvas/DragShadow.vue'
import {
  canvasSave,
  findDragComponent,
  isGroupCanvas,
  isMainCanvas,
  isSameCanvas
} from '@/utils/canvasUtils'
import { guid } from '@/views/visualized/data/dataset/form/util'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import UserViewEnlarge from '@/components/visualization/UserViewEnlarge.vue'
import CanvasOptBar from '@/components/visualization/CanvasOptBar.vue'
import LinkJumpSet from '@/components/visualization/LinkJumpSet.vue'
import { adaptCurThemeCommonStyle } from '@/utils/canvasStyle'
import LinkageSet from '@/components/visualization/LinkageSet.vue'
import PointShadow from '@/components/data-visualization/canvas/PointShadow.vue'
import DragInfo from '@/components/visualization/common/DragInfo.vue'
import { activeWatermark } from '@/components/watermark/watermark'
import { personInfoApi } from '@/api/user'
const snapshotStore = snapshotStoreWithOut()
const dvMainStore = dvMainStoreWithOut()
const composeStore = composeStoreWithOut()
const contextmenuStore = contextmenuStoreWithOut()

const { curComponent, dvInfo, editMode, tabMoveOutComponentId } = storeToRefs(dvMainStore)
const { editorMap } = storeToRefs(composeStore)
const emits = defineEmits(['scrollCanvasToTop'])
const props = defineProps({
  isEdit: {
    type: Boolean,
    default: true
  },
  canvasStyleData: {
    type: Object,
    required: true
  },
  componentData: {
    type: Array,
    required: true
  },
  canvasViewInfo: {
    type: Object,
    required: true
  },
  canvasId: {
    type: String,
    default: 'canvas-main'
  },
  dvModel: {
    type: String,
    default: 'dv'
  },
  baseWidth: {
    required: false,
    type: Number,
    default: 100
  },
  baseHeight: {
    required: false,
    type: Number,
    default: 50
  },
  baseMarginLeft: {
    required: false,
    type: Number,
    default: 20
  },
  baseMarginTop: {
    required: false,
    type: Number,
    default: 20
  },
  draggable: {
    required: false,
    default: true,
    type: Boolean
  },
  dragStart: {
    required: false,
    type: Function,
    default() {
      return {}
    }
  },
  dragging: {
    required: false,
    type: Function,
    default() {
      return {}
    }
  },
  dragEnd: {
    required: false,
    type: Function,
    default() {
      return {}
    }
  },
  resizable: {
    required: false,
    type: Boolean,
    default: true
  },
  resizeStart: {
    required: false,
    type: Function,
    default() {
      return {}
    }
  },
  resizing: {
    required: false,
    type: Function,
    default() {
      return {}
    }
  },
  resizeEnd: {
    required: false,
    type: Function,
    default() {
      return {}
    }
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
const userInfo = ref(null)

const {
  baseWidth,
  baseHeight,
  baseMarginLeft,
  baseMarginTop,
  draggable,
  dragStart,
  dragging,
  dragEnd,
  resizable,
  resizeStart,
  resizing,
  resizeEnd,
  isEdit,
  canvasId,
  canvasStyleData,
  componentData,
  canvasViewInfo
} = toRefs(props)

const editorX = ref(0)
const editorY = ref(0)
const start = ref({
  // 选中区域的起点
  x: 0,
  y: 0
})
const width = ref(0)
const height = ref(0)
const isShowArea = ref(false)
const svgFilterAttrs = ['width', 'height', 'top', 'left', 'rotate']
const userViewEnlargeRef = ref(null)
const linkJumpRef = ref(null)
const linkageRef = ref(null)
const mainDomId = ref('editor-' + canvasId.value)

watch(
  () => dvInfo.value,
  () => {
    initWatermark()
  },
  { deep: true }
)

watch(
  () => canvasStyleData.value,
  () => {
    nextTick(() => {
      initWatermark()
    })
  },
  { deep: true }
)

const initWatermark = (waterDomId = 'editor-canvas-main') => {
  if (
    dvInfo.value.watermarkInfo &&
    dvInfo.value.watermarkInfo.settingContent &&
    isMainCanvas(canvasId.value)
  ) {
    const scale = dashboardActive.value ? 1 : curScale.value
    if (userInfo.value) {
      activeWatermark(
        dvInfo.value.watermarkInfo.settingContent,
        userInfo.value,
        waterDomId,
        canvasId.value,
        dvInfo.value.selfWatermarkStatus,
        scale
      )
    } else {
      const method = personInfoApi
      method().then(res => {
        userInfo.value = res.data
        activeWatermark(
          dvInfo.value.watermarkInfo.settingContent,
          userInfo.value,
          waterDomId,
          canvasId.value,
          dvInfo.value.selfWatermarkStatus,
          scale
        )
      })
    }
  }
}

const dragInfoShow = computed(() => {
  return (
    dvInfo.value.type === 'dashboard' &&
    componentData.value.length === 0 &&
    canvasId.value === 'canvas-main'
  )
})

const curComponentId = computed(() => {
  return curComponent.value?.id || ''
})

const { emitter } = useEmitt()

const curScale = computed(() => {
  if (dashboardActive.value) {
    return (canvasStyleData.value.scale * 1.5) / 100
  } else {
    return canvasStyleData.value.scale / 100
  }
})

const curBaseScale = computed(() => {
  if (dashboardActive.value) {
    return (dvMainStore.canvasStyleData.scale * 1.5) / 100
  } else {
    return dvMainStore.canvasStyleData.scale / 100
  }
})

const pointShadowShow = computed(() => {
  return (
    canvasId.value === 'canvas-main' &&
    curComponent.value &&
    curComponent.value.canvasId !== 'canvas-main' &&
    tabMoveOutComponentId.value
  )
})

const curGap = computed(() => {
  return dashboardActive.value && canvasStyleData.value.dashboard.gap === 'yes'
    ? canvasStyleData.value.dashboard.gapSize
    : 0
})

const baseCellInfo = computed(() => {
  return {
    baseWidth: baseWidth.value,
    baseHeight: baseHeight.value,
    curGap: curGap.value
  }
})

const dashboardActive = computed(() => {
  return dvInfo.value.type === 'dashboard'
})

// 融合矩阵设计
const renderOk = ref(false)
const moveAnimate = ref(false)
const cellWidth = ref(0)
const cellHeight = ref(0)
const maxCell = ref(0)
const infoBox = ref(null)
const container = ref(null)
const positionBox = ref([])
const coordinates = ref([]) //坐标点集合

let lastTask = undefined
let isOverlay = false //是否正在交换位置
let moveTime = 200 //移动动画时间

let itemMaxY = 0
let itemMaxX = 0
let snapshotTimer = ref(null)

// 根据需要需要扩充外部scroll区域也可以进行组合的功能 此方法变更为外部组件调用
const handleMouseDown = e => {
  // 仪表板和预览状态不显示菜单和组创建
  if (dashboardActive.value || editMode.value === 'preview') {
    return
  }
  // 右键返回
  if (e.buttons === 2) {
    return
  }
  // 如果没有选中组件 在画布上点击时需要调用 e.preventDefault() 防止触发 drop 事件
  if (!curComponent.value || isPreventDrop(curComponent.value.component)) {
    // e.preventDefault()
  }
  hideArea()
  const rectInfo = editorMap.value[canvasId.value].getBoundingClientRect()
  editorX.value = rectInfo.x
  editorY.value = rectInfo.y

  const startX = e.clientX
  const startY = e.clientY
  start.value.x = startX - editorX.value
  start.value.y = startY - editorY.value
  // 展示选中区域
  isShowArea.value = true

  const move = moveEvent => {
    width.value = Math.abs(moveEvent.clientX - startX)
    height.value = Math.abs(moveEvent.clientY - startY)
    if (moveEvent.clientX < startX) {
      start.value.x = moveEvent.clientX - editorX.value
    }

    if (moveEvent.clientY < startY) {
      start.value.y = moveEvent.clientY - editorY.value
    }
  }

  const up = e => {
    document.removeEventListener('mousemove', move)
    document.removeEventListener('mouseup', up)

    if (e.clientX == startX && e.clientY == startY) {
      hideArea()
      return
    }

    createGroup()
  }

  document.addEventListener('mousemove', move)
  document.addEventListener('mouseup', up)
}

const hideArea = () => {
  isShowArea.value = false
  width.value = 0
  height.value = 0
  composeStore.setAreaData({
    style: {
      left: 0,
      top: 0,
      width: 0,
      height: 0
    },
    components: []
  })
}

const createGroup = () => {
  // 获取选中区域的组件数据
  const areaData = getSelectArea()
  if (areaData.length <= 1) {
    hideArea()
    return
  }

  // 根据选中区域和区域中每个组件的位移信息来创建 Group 组件
  // 要遍历选择区域的每个组件，获取它们的 left top right bottom 信息来进行比较
  let top = Infinity,
    left = Infinity
  let right = -Infinity,
    bottom = -Infinity
  areaData.forEach(component => {
    let style = { left: 0, top: 0, right: 0, bottom: 0 }
    if (component.component == 'Group') {
      component.propValue.forEach(item => {
        const rectInfo = _$(`#component${item.id}`).getBoundingClientRect()
        style.left = rectInfo.left - editorX.value
        style.top = rectInfo.top - editorY.value
        style.right = rectInfo.right - editorX.value
        style.bottom = rectInfo.bottom - editorY.value

        if (style.left < left) left = style.left
        if (style.top < top) top = style.top
        if (style.right > right) right = style.right
        if (style.bottom > bottom) bottom = style.bottom
      })
    } else {
      style = getComponentRotatedStyle(component.style)
    }

    if (style.left < left) left = style.left
    if (style.top < top) top = style.top
    if (style.right > right) right = style.right
    if (style.bottom > bottom) bottom = style.bottom
  })

  start.value.x = left
  start.value.y = top
  width.value = right - left
  height.value = bottom - top

  // 设置选中区域位移大小信息和区域内的组件数据
  composeStore.setAreaData({
    style: {
      left,
      top,
      width: width.value,
      height: height.value
    },
    components: areaData
  })
  // 如果有组件被group选中 取消当前画布选中的组件
  if (areaData.length > 0) {
    dvMainStore.setCurComponent({ component: null, index: null })
  }
}

const getSelectArea = () => {
  const result = []
  // 区域起点坐标
  const { x, y } = start.value
  // 计算所有的组件数据，判断是否在选中区域内
  componentData.value.forEach(component => {
    if (component.isLock || !component.isShow) return

    const styleInfo = getComponentRotatedStyle(component.style)
    if (
      x <= styleInfo.left &&
      y <= styleInfo.top &&
      styleInfo.left + styleInfo.width <= x + width.value &&
      styleInfo.top + styleInfo.height <= y + height.value
    ) {
      result.push(component)
    }
  })

  // 返回在选中区域内的所有组件
  return result
}

const handleContextMenu = e => {
  // 仪表板和预览状态不显示菜单和组创建
  if (dashboardActive.value || editMode.value === 'preview') {
    return
  }
  e.stopPropagation()
  e.preventDefault()

  // 计算菜单相对于编辑器的位移
  let target = e.target
  let top = e.offsetY
  let left = e.offsetX
  while (target instanceof SVGElement) {
    target = target.parentNode
  }

  while (!target.className.includes('editor')) {
    left += target.offsetLeft
    top += target.offsetTop
    target = target.parentNode
  }

  // 组件处于编辑状态的时候 如富文本 不弹出右键菜单
  if (!curComponent.value || (curComponent.value && !curComponent.value.editing)) {
    contextmenuStore.showContextMenu({ top, left, position: 'canvasCore' })
    const iconDom = document.getElementById('close-button')
    if (iconDom) {
      iconDom.click()
    }
  }
}

const getComponentStyle = style => {
  return getStyle(style, svgFilterAttrs)
}

const getShapeItemShowStyle = item => {
  return getShapeItemStyle(item, {
    dvModel: dvInfo.value.type,
    cellWidth: cellWidth.value,
    cellHeight: cellHeight.value,
    curGap: curGap.value
  })
}

const handleInput = (element, value) => {
  if (element.component !== 'VText') {
    return
  }
  // 根据文本组件高度调整 shape 高度
  dvMainStore.setShapeStyle({
    top: null,
    left: null,
    width: null,
    height: getTextareaHeight(element, value),
    rotate: null
  })
}

const getTextareaHeight = (element, text) => {
  let { lineHeight, fontSize, height } = element.style
  if (lineHeight === '') {
    lineHeight = 1.5
  }

  const newHeight =
    (text.split('<br>').length - 1) * lineHeight * (fontSize || canvasStyleData.value.fontSize)
  return height > newHeight ? height : newHeight
}

const editStyle = computed(() => {
  if (dashboardActive.value || isGroupCanvas(canvasId.value)) {
    return {
      width: '100%',
      height: '100%'
    }
  } else {
    const result = {
      ...getCanvasStyle(canvasStyleData.value),
      width: changeStyleWithScale(canvasStyleData.value.width) + 'px',
      height: changeStyleWithScale(canvasStyleData.value.height) + 'px'
    }
    return result
  }
})

//融合矩阵设计
function debounce(func, time) {
  if (!isOverlay) {
    ;(function (t) {
      isOverlay = true
      setTimeout(function () {
        t()
        setTimeout(function () {
          isOverlay = false
          if (lastTask != undefined) {
            debounce(lastTask, time)
          }
        }, moveTime)
      }, time)
    })(func)
    lastTask = undefined
  } else {
    lastTask = func
  }
}

function scrollScreen(e) {
  if (e.clientY + 50 >= window.innerHeight) {
    let body = $(document.body)
    body.scrollTop(body.scrollTop() + 20)
  } else if (e.clientY <= 150) {
    let body = $(document.body)
    body.scrollTop(body.scrollTop() - 20)
  }
}

/**
 * 重置位置盒子
 *
 */
function resetPositionBox() {
  //根据当前容器的宽度来决定多少列
  itemMaxX = maxCell.value
  let rows = 36 //初始36行，后面根据需求会自动增加
  for (let i = 0; i < rows; i++) {
    let row = []

    for (let j = 0; j < maxCell.value; j++) {
      row.push({
        el: false
      })
    }
    positionBox.value.push(row)
  }
}

/**
 * 查找位置盒子
 */
function findPositionX(item) {
  const width = item.sizeX
  let resultX = 1
  let checkPointYIndex = -1 // -1 没有占用任何Y方向画布
  // 组件宽度
  let pb = positionBox.value
  if (width <= 0) return
  // 查找组件最高位置索引 component 规则 y最新为1
  componentData.value.forEach((component, index) => {
    const componentYIndex = component.y + component.sizeY - 2
    if (checkPointYIndex < componentYIndex) {
      checkPointYIndex = componentYIndex
    }
  })

  if (checkPointYIndex < 0) {
    return 1
  } else {
    // 获取最后一列X方向数组
    const pbX = pb[checkPointYIndex]
    // 从X i位置索引开始检查 ；
    // 检查宽度为组件宽度width 检查索引终点为checkEndIndex = i + width - 1 ；
    // 退出检查条件为索引终点checkEndIndex 越界 pbX终点索引；
    for (let i = 0, checkEndIndex = width - 1; checkEndIndex < pbX.length; i++, checkEndIndex++) {
      let adaptorCount = 0
      // 定位最后一列占位位置
      for (let k = 0; k < width; k++) {
        // pbX[i + k].el === false 表示当前矩阵点位未被占用，当连续未被占用的矩阵点位宽度为width时 表示改起始点位i可用
        if (!pbX[i + k].el) {
          adaptorCount++
        }
      }
      if (adaptorCount === width) {
        resultX = i + 1
        break
      }
    }
    return resultX
  }
}

/**
 * 填充位置盒子
 */
function addItemToPositionBox(item) {
  let pb = positionBox.value
  if (item.x <= 0 || item.y <= 0) return
  for (let i = item.x - 1; i < item.x - 1 + item.sizeX; i++) {
    for (let j = item.y - 1; j < item.y - 1 + item.sizeY; j++) {
      try {
        if (pb[j][i]) {
          pb[j][i].el = item
        }
      } catch (e) {
        console.error(e)
      }
    }
  }
}

function fillPositionBox(maxY) {
  let pb = positionBox.value
  maxY += 2
  for (let j = 0; j < maxY; j++) {
    if (pb[j] == undefined) {
      let row = []
      for (let i = 0; i < itemMaxX; i++) {
        row.push({
          el: false
        })
      }
      pb.push(row)
    }
  }

  itemMaxY = maxY
  //problem
  $(container.value).css('height', (itemMaxY + 2) * cellHeight.value + 'px')
}

function removeItemFromPositionBox(item) {
  let pb = positionBox.value
  if (item.x <= 0 || item.y <= 0) return
  for (let i = item.x - 1; i < item.x - 1 + item.sizeX; i++) {
    for (let j = item.y - 1; j < item.y - 1 + item.sizeY; j++) {
      try {
        if (pb[j][i]) {
          pb[j][i].el = false
        }
      } catch (e) {
        console.error(e)
      }
    }
  }
}

/**
 * 重新计算宽度，使最小单元格能占满整个容器
 *
 */
function reCalcCellWidth() {
  //problem
  let containerWidth = container.value.offsetWidth
  let cells = Math.round(containerWidth / cellWidth.value)
  maxCell.value = cells
  itemMaxX = maxCell.value
}
function resizePlayer(item, newSize) {
  removeItemFromPositionBox(item)
  let belowItems = findBelowItems(item)
  _.forEach(belowItems, function (upItem) {
    let canGoUpRows = canItemGoUp(upItem)

    if (canGoUpRows > 0) {
      moveItemUp(upItem, canGoUpRows)
    }
  })

  item.sizeX = newSize.sizeX
  item.sizeY = newSize.sizeY

  if (item.sizeX + item.x - 1 > itemMaxX) {
    item.sizeX = itemMaxX - item.x + 1
  }

  if (item.sizeY + item.y > itemMaxY) {
    fillPositionBox(item.y + item.sizeY)
  }
  emptyTargetCell(item)
  addItemToPositionBox(item)
  changeItemCoordinate(item)
  let canGoUpRows = canItemGoUp(item)
  if (canGoUpRows > 0) {
    moveItemUp(item, canGoUpRows)
  }
}

/**
 * 检查移动的位置，如果不合法，会自动修改
 */
function checkItemPosition(item, position) {
  position = position || {}
  position.x = position.x || item.x
  position.y = position.y || item.y

  // 检查位置
  if (item.x < 1) {
    item.x = 1
  }

  // 检查大小
  if (item.sizeX > itemMaxX) {
    item.sizeX = itemMaxX
  }

  if (item.sizeX < 1) {
    item.sizeX = 1
  }

  if (item.x + item.sizeX - 1 > itemMaxX) {
    item.x = itemMaxX - item.sizeX + 1
    if (item.x < 1) {
      item.x = 1
    }
  }

  if (item.y < 1) {
    item.y = 1
  }

  if (item.sizeY < 1) {
    item.sizeY = 1
  }

  if (item.y + item.sizeY > itemMaxY - 1) {
    fillPositionBox(item.y + item.sizeY - 1)
  }
}

/**
 * 移动正在拖动的元素
 */
function movePlayer(item, position) {
  removeItemFromPositionBox(item)
  let belowItems = findBelowItems(item)
  _.forEach(belowItems, function (upItem) {
    let canGoUpRows = canItemGoUp(upItem)
    if (canGoUpRows > 0) {
      moveItemUp(upItem, canGoUpRows)
    }
  })
  item.x = position.x
  item.y = position.y
  checkItemPosition(item, position)
  emptyTargetCell(item)
  addItemToPositionBox(item)
  changeItemCoordinate(item)
  let canGoUpRows = canItemGoUp(item)
  if (canGoUpRows > 0) {
    moveItemUp(item, canGoUpRows)
  }
}

function removeItemById(componentId) {
  if (componentId) {
    componentData.value.forEach((component, index) => {
      if (componentId === component['id']) {
        removeItem(index)
      }
    })
  }
}

function removeItem(index) {
  let item = componentData.value[index]
  if (item && isSameCanvas(item, canvasId.value)) {
    removeItemFromPositionBox(item)
    let belowItems = findBelowItems(item)
    _.forEach(belowItems, function (upItem) {
      let canGoUpRows = canItemGoUp(upItem)
      if (canGoUpRows > 0) {
        moveItemUp(upItem, canGoUpRows)
      }
    })
    let checkedFields = []
    if (item.innerType === 'VQuery') {
      ;(item.propValue || []).forEach(ele => {
        checkedFields = [...ele.checkedFields, ...checkedFields]
      })
    }
    componentData.value.splice(index, 1)
    if (!!checkedFields.length) {
      Array.from(new Set(checkedFields)).forEach(ele => {
        emitter.emit(`query-data-${ele}`)
      })
    }
    snapshotStore.recordSnapshotCache('removeItem')
  }
}

function addItem(item, index) {
  if (index < 0) {
    index = componentData.value.length
  }
  item._dragId = index
  checkItemPosition(item, {
    x: item.x,
    y: item.y
  })
  emptyTargetCell(item)
  addItemToPositionBox(item)
  let canGoUpRows = canItemGoUp(item)
  if (canGoUpRows > 0) {
    moveItemUp(item, canGoUpRows)
  }
}

function changeItemCoordinate(item) {
  let width = cellWidth.value * item.sizeX - baseMarginLeft.value
  let height = cellHeight.value * item.sizeY - baseMarginTop.value
  let left = cellWidth.value * (item.x - 1) + baseMarginLeft.value
  let top = cellHeight.value * (item.y - 1) + baseMarginTop.value

  let coordinate = {
    x1: left,
    x2: left + width,
    y1: top,
    y2: top + height,
    c1: left + width / 2,
    c2: top + height / 2,
    el: item
  }
  let index = _.findIndex(coordinates.value, function (o) {
    return o.el._dragId == item._dragId
  })
  if (index != -1) {
    coordinates.value.splice(index, 1, coordinate)
  }
}

/**
 * 清空目标位置的元素
 *
 */
function emptyTargetCell(item) {
  let belowItems = findBelowItems(item)
  _.forEach(belowItems, function (downItem) {
    if (downItem['_dragId'] == item['_dragId']) return
    let moveSize = item.y + item.sizeY - downItem['y']
    if (moveSize > 0) {
      moveItemDown(downItem, moveSize)
    }
  })
}

/**
 * 当前位置的item能否上浮
 */
function canItemGoUp(item) {
  let upperRows = 0
  for (let row = item.y - 2; row >= 0; row--) {
    for (let cell = item.x - 1; cell < item.x - 1 + item.sizeX; cell++) {
      if (positionBox.value[row][cell] && positionBox.value[row][cell].el) {
        return upperRows
      }
    }
    upperRows++
  }
  return upperRows
}

/**
 * 在移动之前，找到当前下移的元素的下面的元素（递归）
 */
function moveItemDown(item, size) {
  removeItemFromPositionBox(item)
  let belowItems = findBelowItems(item)
  _.forEach(belowItems, function (downItem) {
    if (downItem['_dragId'] == item['_dragId']) return
    let moveSize = calcDiff(item, downItem, size)
    if (moveSize > 0) {
      moveItemDown(downItem, moveSize)
    }
  })
  let targetPosition = {
    y: item.y + size
  }
  setPlayerPosition(item, targetPosition)
  checkItemPosition(item, targetPosition)
  addItemToPositionBox(item)
  changeItemCoordinate(item)
}

function setPlayerPosition(item, position) {
  position = position || {}
  let targetX = position.x || item.x
  let targetY = position.y || item.y
  item.x = targetX
  item.y = targetY
  if (item.y + item.sizeY > itemMaxY) {
    itemMaxY = item.y + item.sizeY
  }
}

/**
 * 寻找子元素到父元素的最大距离
 */
function calcDiff(parent, son, size) {
  let diffs = []
  for (let i = son.x - 1; i < son.x - 1 + son.sizeX; i++) {
    let temp_y = 0
    for (let j = parent.y - 1 + parent.sizeY; j < son.y - 1; j++) {
      if (positionBox.value[j][i] && positionBox.value[j][i].el == false) {
        temp_y++
      }
    }
    diffs.push(temp_y)
  }
  let max_diff = Math.max.apply(Math, diffs)
  size = size - max_diff
  return size > 0 ? size : 0
}

function moveItemUp(item, size) {
  removeItemFromPositionBox(item)
  let belowItems = findBelowItems(item)
  // item.y -= size;
  setPlayerPosition(item, {
    y: item.y - size
  })
  addItemToPositionBox(item)
  changeItemCoordinate(item)
  _.forEach(belowItems, function (upItem) {
    let moveSize = canItemGoUp(upItem)
    if (moveSize > 0) {
      moveItemUp(upItem, moveSize)
    }
  })
}
function findBelowItems(item) {
  let belowItems = {}
  for (let cell = item.x - 1; cell < item.x - 1 + item.sizeX; cell++) {
    for (let row = item.y - 1; row < positionBox.value.length; row++) {
      let target = positionBox.value[row][cell]
      if (target && target.el) {
        belowItems[target.el._dragId] = target.el
        break
      }
    }
  }
  return _.sortBy(_.values(belowItems), 'y')
}

const endItemMove = (_, item, index) => {
  dvMainStore.setCurComponent({ component: item, index: index })
  dvMainStore.setClickComponentStatus(true)
  dvMainStore.setInEditorStatus(true)
}

const handleMouseUp = (e, item, index) => {
  endItemMove(e, item, index)
  clearInfoBox(e)
}

const clearInfoBox = e => {
  if (_.isEmpty(infoBox.value)) return
  if (infoBox.value.cloneItem) {
    infoBox.value.cloneItem.remove()
  }
  if (infoBox.value.resizeItem) {
    delete infoBox.value.resizeItem['isPlayer']
    resizeEnd.value(e, infoBox.value.resizeItem, infoBox.value.resizeItem._dragId)
  }
  if (infoBox.value.moveItem) {
    dragEnd?.value(null, e, infoBox.value.moveItem, infoBox.value.moveItem._dragId)
    //problem
    infoBox.value.moveItem['show'] = true
    delete infoBox.value.moveItem['isPlayer']
  }
  infoBox.value = {}
}

const cellInit = () => {
  // 此处向下取整 保留1位小数,why: 矩阵模式计算 x,y时 会使用 style.left/cellWidth style.top/cellWidth
  // 当初始状态细微的差距(主要是减少)都会导致 x，y 减少一个矩阵大小造成偏移,
  cellWidth.value = Math.floor((baseWidth.value + baseMarginLeft.value) * 10) / 10
  cellHeight.value = Math.floor((baseHeight.value + baseMarginTop.value) * 10) / 10
}

const canvasSizeInit = () => {
  if (isMainCanvas(canvasId.value)) {
    initWatermark()
  }
  cellInit()
  reCalcCellWidth()
}

const canvasInit = () => {
  cellInit()
  positionBox.value = []
  coordinates.value = [] //坐标点集合
  lastTask = undefined
  isOverlay = false //是否正在交换位置
  moveTime = 200 //移动动画时间
  itemMaxY = 0
  itemMaxX = 0

  reCalcCellWidth()
  resetPositionBox()

  let i = 0
  let timeId = setInterval(function () {
    if (i >= componentData.value.length) {
      clearInterval(timeId)
      nextTick(() => {
        moveAnimate.value = true
      })
    } else {
      let item = componentData.value[i]
      addItem(item, i)
      i++
    }
  }, 1)
  renderOk.value = true
}

const afterInitOk = func => {
  let timeId = setInterval(() => {
    if (moveAnimate.value) {
      clearInterval(timeId)
      func()
    }
  }, 100)
}

const forceComputed = () => {
  // 这里强制触发复制添加计算，因为位置计算使用的是method 并没有内部样式属性变化
  // 在一些情况下无法触发重新计算导致位置偏移 cellHeight 属性是在监控中的，这里进行强制计算
  cellHeight.value = cellHeight.value + 0.001
  nextTick(function () {
    cellHeight.value = cellHeight.value - 0.001
  })
}
const addItemBox = item => {
  syncShapeItemStyle(item, baseWidth.value, baseHeight.value)
  forceComputed()
  nextTick(() => {
    addItem(item, componentData.value.length - 1)
  })
}

const onStartResize = (e, item, index) => {
  // 移动时 换算矩阵和悬浮位置大小
  syncShapeItemStyle(item, cellWidth.value, cellHeight.value)
  if (!resizable.value) return
  resizeStart.value(e, item, index)
  if (!infoBox.value) {
    infoBox.value = {}
  }
  infoBox.value.resizeItem = item
  infoBox.value.resizeItemIndex = index

  infoBox.value.moveItem = item
  infoBox.value.moveItemIndex = index

  infoBox.value.originX = 0 // 克隆对象原始X位置
  infoBox.value.originY = 0
  infoBox.value.startX = 0
  infoBox.value.startY = 0

  infoBox.value.oldX = item.x // 实际对象原始X位置
  infoBox.value.oldY = item.y
  infoBox.value.oldSizeX = item.sizex
  infoBox.value.oldSizeY = item.sizey
}

const onStartMove = (e, item, index) => {
  // 移动时 换算矩阵和悬浮位置大小
  syncShapeItemStyle(item, cellWidth.value, cellHeight.value)
  if (!infoBox.value) {
    infoBox.value = {}
  }
  dragStart.value.call(null, e, item, index)
  infoBox.value.moveItem = item
  infoBox.value.moveItemIndex = index

  infoBox.value.originX = 0 // 克隆对象原始X位置
  infoBox.value.originY = 0
  infoBox.value.startX = 0
  infoBox.value.startY = 0

  infoBox.value.oldX = item.x // 实际对象原始X位置
  infoBox.value.oldY = item.y
  infoBox.value.oldSizeX = item.sizex
  infoBox.value.oldSizeY = item.sizey
}

const onDragging = (e, item) => {
  // item 中的 style 为当前实时的位置
  const infoBoxTemp = infoBox.value
  let moveItem = _.get(infoBoxTemp, 'moveItem')
  scrollScreen(e)
  if (!draggable.value) return
  dragging.value(e, moveItem, moveItem._dragId)
  //problem
  moveItem['isPlayer'] = true
  let oldX = infoBoxTemp.oldX
  let oldY = infoBoxTemp.oldY

  let newX = Math.floor(item.style.left / cellWidth.value + 1)
  let newY = Math.floor(item.style.top / cellHeight.value + 1)
  newX = newX > 0 ? newX : 1
  newY = newY > 0 ? newY : 1

  debounce(
    (function (newX, oldX, newY, oldY) {
      return function () {
        if (newX != oldX || oldY != newY) {
          movePlayer(moveItem, {
            x: newX,
            y: newY
          })

          infoBoxTemp.oldX = newX
          infoBoxTemp.oldY = newY
        }
      }
    })(newX, oldX, newY, oldY),
    10
  )
}

const onResizing = (e, item) => {
  const { width, height } = item.style
  // item 中的 style 为当前实时的位置
  const infoBoxTemp = infoBox.value
  let resizeItem = _.get(infoBoxTemp, 'resizeItem')
  //调整大小时
  resizing.value(e, resizeItem, resizeItem._dragId)
  resizeItem['isPlayer'] = true
  let nowSizeX =
    width % cellWidth.value > (cellWidth.value / 4) * 3
      ? Math.floor(width / cellWidth.value + 1)
      : Math.floor(width / cellWidth.value)
  let nowSizeY =
    height % cellHeight.value > (cellHeight.value / 4) * 3
      ? Math.floor(height / cellHeight.value + 1)
      : Math.floor(height / cellHeight.value)

  // 增加5px偏移量 防止resize时向下取整 组件向右偏移
  let newX = Math.floor((item.style.left + 5) / cellWidth.value + 1)
  let newY = Math.floor((item.style.top + 5) / cellHeight.value + 1)
  newX = newX > 0 ? newX : 1
  newY = newY > 0 ? newY : 1

  // 调整大小
  debounce(
    (function (newX, newY) {
      return function () {
        // 调整大小
        resizePlayer(resizeItem, {
          sizeX: nowSizeX,
          sizeY: nowSizeY
        })

        infoBoxTemp.oldSizeX = nowSizeX
        infoBoxTemp.oldSizeY = nowSizeY
        // 调整位置
        movePlayer(resizeItem, {
          x: newX,
          y: newY
        })
        infoBoxTemp.oldX = newX
        infoBoxTemp.oldY = newY
      }
    })(newX, newY),
    10
  )
}

const onMouseUp = e => {
  // startMove 中组织冒泡会导致移动事件无法传播，在这里设置（鼠标抬起）效果一致
  if (_.isEmpty(infoBox.value)) return
  if (infoBox.value.cloneItem) {
    infoBox.value.cloneItem.remove()
  }
  if (infoBox.value.resizeItem) {
    delete infoBox.value.resizeItem['isPlayer']
    resizeEnd.value(e, infoBox.value.resizeItem, infoBox.value.resizeItem._dragId)
  }
  if (infoBox.value.moveItem) {
    dragEnd?.value(null, e, infoBox.value.moveItem, infoBox.value.moveItem._dragId)
    //problem
    infoBox.value.moveItem['show'] = true
    delete infoBox.value.moveItem['isPlayer']
  }
  infoBox.value = {}
}

const handleDragStartMoveIn = componentInfo => {
  const moveInItemInfo = findDragComponent(componentInfo)
  // 初始的移动组件 距离左侧的位置 300 是DbToolbar 最左侧区域宽度
  moveInItemInfo.x = 300 / cellWidth.value
  // 仪表板初始移动的组件暂时不显示 在松开鼠标时再确实该组件的去留
  moveInItemInfo.isShow = false
  moveInItemInfo.id = guid()
  dvMainStore.addComponent({ component: moveInItemInfo, index: undefined })
  const adapt = dvInfo.value.type === 'dashboard' ? true : false
  if (adapt) {
    adaptCurThemeCommonStyle(moveInItemInfo)
  }
  addItemBox(moveInItemInfo)
  emits('scrollCanvasToTop')
  if (!infoBox.value) {
    infoBox.value = {}
  }
  infoBox.value.moveItem = moveInItemInfo
  infoBox.value.moveItemIndex = componentData.value.length - 1
  infoBox.value.oldX = 1 // 实际对象原始X位置
  infoBox.value.oldY = 1
}

const handleDragEnd = e => {
  // 当isShow 是false时，说明未移入画布 则需要进行清理占位
  if (infoBox.value && infoBox.value.moveItem && !infoBox.value.moveItem.isShow) {
    removeItem(componentData.value.length - 1)
    clearInfoBox(e)
  }
}

const handleDragOver = e => {
  if (!infoBox.value || !infoBox.value.moveItem) {
    return
  }
  infoBox.value.moveItem.style.left = e.pageX
  infoBox.value.moveItem.style.top = e.pageY
  onDragging(e, infoBox.value.moveItem, 0)
}

const getMoveItem = () => {
  return infoBox.value.moveItem
}

const userViewEnlargeOpen = (opt, item) => {
  userViewEnlargeRef.value.dialogInit(
    canvasStyleData.value,
    canvasViewInfo.value[item.id],
    item,
    opt
  )
}

const initSnapshotTimer = () => {
  snapshotTimer.value = setInterval(() => {
    snapshotStore.snapshotCatchToStore()
  }, 1000)
}

const linkJumpSetOpen = item => {
  //跳转设置需要先触发保存
  canvasSave(() => {
    linkJumpRef.value.dialogInit(item)
  })
}
const linkageSetOpen = item => {
  //跳转设置需要先触发保存
  canvasSave(() => {
    linkageRef.value.dialogInit(item)
  })
}

const contextMenuShow = computed(() => {
  if (curComponent.value) {
    return curComponent.value.canvasId === canvasId.value
  } else {
    return isMainCanvas(canvasId.value)
  }
})

const markLineShow = computed(() => isMainCanvas(canvasId.value))

onMounted(() => {
  if (isMainCanvas(canvasId.value)) {
    initSnapshotTimer()
    initWatermark()
  }
  // 获取编辑器元素
  composeStore.getEditor(canvasId.value)
  eventBus.on('handleDragStartMoveIn-' + canvasId.value, handleDragStartMoveIn)
  eventBus.on('handleDragEnd-' + canvasId.value, handleDragEnd)
  eventBus.on('hideArea-' + canvasId.value, hideArea)
  eventBus.on('removeMatrixItem-' + canvasId.value, removeItem)
  eventBus.on('removeMatrixItemById-' + canvasId.value, removeItemById)
  eventBus.on('addDashboardItem-' + canvasId.value, addItemBox)
  eventBus.on('snapshotChange-' + canvasId.value, canvasInit)
  eventBus.on('doCanvasInit-' + canvasId.value, canvasInit)
})

onBeforeUnmount(() => {
  if (snapshotTimer.value) {
    clearInterval(snapshotTimer.value)
    snapshotTimer.value = null
  }
  eventBus.off('handleDragStartMoveIn-' + canvasId.value, handleDragStartMoveIn)
  eventBus.off('handleDragEnd-' + canvasId.value, handleDragEnd)
  eventBus.off('hideArea-' + canvasId.value, hideArea)
  eventBus.off('removeMatrixItem-' + canvasId.value, removeItem)
  eventBus.off('removeMatrixItemById-' + canvasId.value, removeItemById)
  eventBus.off('addDashboardItem-' + canvasId.value, addItemBox)
  eventBus.off('snapshotChange-' + canvasId.value, canvasInit)
  eventBus.off('doCanvasInit' + canvasId.value, canvasInit)
})

defineExpose({
  canvasSizeInit,
  canvasInit,
  afterInitOk,
  addItemBox,
  handleDragOver,
  getMoveItem,
  handleMouseUp,
  handleMouseDown,
  findPositionX
})
</script>

<template>
  <div
    :id="mainDomId"
    ref="container"
    class="editor"
    :class="{ edit: isEdit, 'dashboard-editor': dashboardActive }"
    :style="editStyle"
    @contextmenu="handleContextMenu"
  >
    <drag-info v-if="dragInfoShow"></drag-info>
    <canvas-opt-bar
      v-if="dvInfo.type === 'dataV'"
      :canvas-style-data="canvasStyleData"
      :component-data="componentData"
      :canvas-id="canvasId"
    ></canvas-opt-bar>
    <!-- 网格线 -->
    <drag-shadow
      v-if="infoBox && infoBox.moveItem"
      :base-height="baseHeight"
      :base-width="baseWidth"
      :cur-gap="curGap"
      :element="infoBox.moveItem"
    ></drag-shadow>

    <!--切换canvas 拖拽阴影部分-->
    <point-shadow v-if="pointShadowShow" :canvas-id="canvasId" />

    <!--页面组件列表展示-->
    <Shape
      v-for="(item, index) in componentData"
      v-show="item.isShow"
      :canvas-id="canvasId"
      :scale="curScale"
      :key="item.id"
      :default-style="item.style"
      :style="getShapeItemShowStyle(item)"
      :element="item"
      :index="index"
      :class="{ lock: item.isLock && editMode === 'edit' }"
      :base-cell-info="baseCellInfo"
      :canvas-active="canvasActive"
      @onStartResize="onStartResize($event, item, index)"
      @onStartMove="onStartMove($event, item, index)"
      @onMouseUp="onMouseUp($event, item, index)"
      @onDragging="onDragging($event, item, index)"
      @onResizing="onResizing($event, item, index)"
      @userViewEnlargeOpen="userViewEnlargeOpen($event, item)"
      @linkJumpSetOpen="linkJumpSetOpen(item)"
      @linkageSetOpen="linkageSetOpen(item)"
    >
      <!--如果是视图 则动态获取预存的chart-view数据-->
      <component
        :is="findComponent(item.component)"
        v-if="item.component === 'UserView'"
        class="component"
        :id="'component' + item.id"
        :active="item.id === curComponentId"
        :dv-type="dvInfo.type"
        :scale="curBaseScale"
        :style="getComponentStyle(item.style)"
        :prop-value="item.propValue"
        :is-edit="true"
        :view="canvasViewInfo[item.id]"
        :element="item"
        :request="item.request"
        @input="handleInput"
        :dv-info="dvInfo"
        :canvas-active="canvasActive"
      />

      <component
        v-else
        :is="findComponent(item.component)"
        :id="'component' + item.id"
        :scale="curBaseScale"
        class="component"
        :is-edit="true"
        :style="getComponentStyle(item.style)"
        :prop-value="item.propValue"
        :element="item"
        :request="item.request"
        :canvas-style-data="canvasStyleData"
        :canvas-view-info="canvasViewInfo"
        :dv-info="dvInfo"
        :active="item.id === curComponentId"
        :canvas-active="canvasActive"
      />
    </Shape>
    <!-- 右击菜单 -->
    <ContextMenu v-if="contextMenuShow" show-position="canvasCore" />
    <!-- 标线 -->
    <MarkLine v-if="markLineShow" />
    <!-- 选中区域 -->
    <Area v-show="isShowArea" :start="start" :width="width" :height="height" />
    <user-view-enlarge ref="userViewEnlargeRef"></user-view-enlarge>
    <link-jump-set ref="linkJumpRef"></link-jump-set>
    <linkage-set ref="linkageRef"></linkage-set>
  </div>
</template>

<style lang="less" scoped>
.dashboard-editor {
  min-height: 100%;
}
.editor {
  position: relative;
  margin: auto;
  background-size: 100% 100% !important;
  .lock {
    opacity: 0.5;
    &:hover {
      cursor: not-allowed;
    }
  }
}

.edit {
  .component {
    outline: none;
    width: 100%;
    height: 100%;
  }
}
</style>
