<script setup lang="ts">
import Shape from './Shape.vue'
import {
  getStyle,
  getComponentRotatedStyle,
  getShapeItemStyle,
  getSVGStyle,
  getCanvasStyle,
  syncShapeItemStyle
} from '@/utils/style'
import $ from 'jquery'
import { _$, isPreventDrop } from '@/utils/utils'
import ContextMenu from './ContextMenu.vue'
import MarkLine from './MarkLine.vue'
import Area from './Area.vue'
import eventBus from '@/utils/eventBus'
import Grid from './Grid.vue'
import { changeStyleWithScale } from '@/utils/translate'
import { ref, onMounted, toRef, computed, toRefs, nextTick, onBeforeUnmount } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { composeStoreWithOut } from '@/store/modules/data-visualization/compose'
import { contextmenuStoreWithOut } from '@/store/modules/data-visualization/contextmenu'
import { storeToRefs } from 'pinia'
import findComponent from '@/utils/components'
import _ from 'lodash'
import DragShadow from '@/components/data-visualization/canvas/DragShadow.vue'
import { findDragComponent } from '@/utils/canvasUtils'
import { guid } from '@/views/visualized/data/dataset/form/util'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import UserViewEnlarge from '@/components/visualization/UserViewEnlarge.vue'
const snapshotStore = snapshotStoreWithOut()
const dvMainStore = dvMainStoreWithOut()
const composeStore = composeStoreWithOut()
const contextmenuStore = contextmenuStoreWithOut()

const { componentData, curComponent, canvasStyleData, canvasViewInfo, dvInfo, editMode } =
  storeToRefs(dvMainStore)
const { editor } = storeToRefs(composeStore)
const props = defineProps({
  isEdit: {
    type: Boolean,
    default: true
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
  }
})

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
  dvModel
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
const showComponentData = computed(() => {
  return componentData.value.filter(component => component.isShow)
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
const list = ref([])
const cellWidth = ref(0)
const cellHeight = ref(0)
const maxCell = ref(0)
const infoBox = ref(null)
const container = ref(null)
let positionBox = []
let coordinates = [] //坐标点集合

let lastTask = undefined
let isOverlay = false //是否正在交换位置
let moveTime = 80 //移动动画时间

let itemMaxY = 0
let itemMaxX = 0
let currentInstance
let snapshotTimer = ref(null)

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
    e.preventDefault()
  }
  hideArea()
  // 获取编辑器的位移信息，每次点击时都需要获取一次。主要是为了方便开发时调试用。
  const rectInfo = editor.value.getBoundingClientRect()
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
    if (component.isLock) return

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

  contextmenuStore.showContextMenu({ top, left })
}

const getComponentStyle = style => {
  return getStyle(style, svgFilterAttrs)
}

const getSVGStyleInner = style => {
  return getSVGStyle(style, svgFilterAttrs)
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
  if (dashboardActive.value) {
    return {
      width: '100%',
      height: '100%'
    }
  } else {
    return {
      ...getCanvasStyle(canvasStyleData.value),
      width: changeStyleWithScale(canvasStyleData.value['width']) + 'px',
      height: changeStyleWithScale(canvasStyleData.value['height']) + 'px'
    }
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
  let rows = 1 //初始100行，后面根据需求会自动增加
  for (let i = 0; i < rows; i++) {
    let row = []

    for (let j = 0; j < maxCell.value; j++) {
      row.push({
        el: false
      })
    }
    positionBox.push(row)
  }
}

/**
 * 填充位置盒子
 */
function addItemToPositionBox(item) {
  let pb = positionBox
  if (item.x <= 0 || item.y <= 0) return
  for (let i = item.x - 1; i < item.x - 1 + item.sizeX; i++) {
    for (let j = item.y - 1; j < item.y - 1 + item.sizeY; j++) {
      if (pb[j][i]) {
        pb[j][i].el = item
      }
    }
  }
}

function fillPositionBox(maxY) {
  let pb = positionBox
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
  let pb = positionBox
  if (item.x <= 0 || item.y <= 0) return
  for (let i = item.x - 1; i < item.x - 1 + item.sizeX; i++) {
    for (let j = item.y - 1; j < item.y - 1 + item.sizeY; j++) {
      if (pb[j][i]) {
        pb[j][i].el = false
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

function removeItem(index) {
  let item = componentData.value[index]
  removeItemFromPositionBox(item)
  let belowItems = findBelowItems(item)
  _.forEach(belowItems, function (upItem) {
    let canGoUpRows = canItemGoUp(upItem)
    if (canGoUpRows > 0) {
      moveItemUp(upItem, canGoUpRows)
    }
  })
  componentData.value.splice(index, 1)
  snapshotStore.recordSnapshot('removeItem')
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

function changeToCoordinate(left, top, width, height) {
  return {
    x1: left,
    x2: left + width,
    y1: top,
    y2: top + height,
    c1: left + width / 2,
    c2: top + height / 2
  }
}

/**
 * 检测有无碰撞，并作出处理
 */
function findClosetCoords(item, tCoordinate) {
  if (isOverlay) return
  let i = coordinates.length
  let collisionsItem = []
  while (i--) {
    let nowCoordinate = coordinates[i]
    if (item._dragId == nowCoordinate.el._dragId) {
      continue
    }
    if (
      tCoordinate.x2 < nowCoordinate.x1 ||
      tCoordinate.x1 > nowCoordinate.x2 ||
      tCoordinate.y2 < nowCoordinate.y1 ||
      tCoordinate.y1 > nowCoordinate.y2
    ) {
      continue
    } else {
      collisionsItem.push({
        centerDistance: Math.sqrt(
          Math.pow(tCoordinate.c1 - nowCoordinate.c1, 2) +
            Math.pow(tCoordinate.c2 - nowCoordinate.c2, 2)
        ),
        coordinate: nowCoordinate
      })
    }
  }
  if (collisionsItem.length <= 0) {
    return
  }
  isOverlay = true
  collisionsItem = _.sortBy(collisionsItem, 'area')
  movePlayer(item, {
    x: collisionsItem[0].coordinate.el.x,
    y: collisionsItem[0].coordinate.el.y
  })
  setTimeout(function () {
    isOverlay = false
  }, 200)
}

/**
 * 生成坐标点
 *
 * @param {any} item
 */
function makeCoordinate(item) {
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
  coordinates.push(coordinate)
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
  let index = _.findIndex(coordinates, function (o) {
    return o.el._dragId == item._dragId
  })
  if (index != -1) {
    coordinates.splice(index, 1, coordinate)
  }
}

/**
 * 清空目标位置的元素
 *
 */
function emptyTargetCell(item) {
  let belowItems = findBelowItems(item)
  _.forEach(belowItems, function (downItem, index) {
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
      if (positionBox[row][cell] && positionBox[row][cell].el) {
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
  _.forEach(belowItems, function (downItem, index) {
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
      if (positionBox[j][i] && positionBox[j][i].el == false) {
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
  _.forEach(belowItems, function (upItem, index) {
    let moveSize = canItemGoUp(upItem)
    if (moveSize > 0) {
      moveItemUp(upItem, moveSize)
    }
  })
}
function findBelowItems(item) {
  let belowItems = {}
  for (let cell = item.x - 1; cell < item.x - 1 + item.sizeX; cell++) {
    for (let row = item.y - 1; row < positionBox.length; row++) {
      let target = positionBox[row][cell]
      if (target && target.el) {
        belowItems[target.el._dragId] = target.el
        break
      }
    }
  }
  return _.sortBy(_.values(belowItems), 'y')
}

const startResize = (e, item, index) => {
  if (!resizable.value) return
  resizeStart.value(e, item, index)
  if (!infoBox.value) {
    infoBox.value = {}
  }
  infoBox.value.resizeItem = item
  infoBox.value.resizeItemIndex = index
}

const containerMouseDown = e => {
  // e.preventDefault();
  if (!infoBox.value) {
    infoBox.value = {}
  }

  infoBox.value.startX = e.pageX
  infoBox.value.startY = e.pageY
}

const endItemMove = (e, item, index) => {
  dvMainStore.setCurComponent({ component: item, index: index })
  dvMainStore.setClickComponentStatus(true)
  dvMainStore.setInEditorStatus(true)
}

const startMove = (e, item, index) => {
  // console.log('startMove...')
  // e.preventDefault();
  if (!infoBox.value) {
    infoBox.value = {}
  }
  let infoBoxTemp = infoBox.value
  let target = $(e.target)

  let className = target.attr('class')
  className = className || ''
  if (
    className.indexOf('dragHandle') == -1 &&
    className.indexOf('item') == -1 &&
    className.indexOf('resizeHandle') == -1
  ) {
    return
  }

  if (className.includes('resizeHandle')) {
  } else if (draggable.value && (className.includes('dragHandle') || className.includes('item'))) {
    dragStart.value(e, item, index)
    infoBoxTemp.moveItem = item
    infoBoxTemp.moveItemIndex = index
  }

  infoBoxTemp.cloneItem = null
  infoBoxTemp.nowItemNode = null

  if (target.attr('class') && target.attr('class').indexOf('item') != -1) {
    infoBoxTemp.nowItemNode = target
    infoBoxTemp.cloneItem = target.clone()
  } else {
    infoBoxTemp.nowItemNode = target.parents('.item')
    infoBoxTemp.cloneItem = infoBoxTemp.nowItemNode.clone()
  }
  infoBoxTemp.cloneItem.addClass('cloneNode')

  //problem
  $(container.value).append(infoBoxTemp.cloneItem)

  infoBoxTemp.originX = infoBoxTemp.cloneItem.position().left //克隆对象原始X位置
  infoBoxTemp.originY = infoBoxTemp.cloneItem.position().top
  infoBoxTemp.oldX = item.x //实际对象原始X位置
  infoBoxTemp.oldY = item.y
  infoBoxTemp.oldSizeX = item.sizeX
  infoBoxTemp.oldSizeY = item.sizeY
  infoBoxTemp.originWidth = infoBoxTemp.cloneItem.prop('offsetWidth')
  infoBoxTemp.originHeight = infoBoxTemp.cloneItem.prop('offsetHeight')

  function itemMouseMove(e) {
    let moveItem = _.get(infoBoxTemp, 'moveItem')
    let resizeItem = _.get(infoBoxTemp, 'resizeItem')

    if (resizeItem) {
      //调整大小时
      resizing.value(e, resizeItem, resizeItem._dragId)
      resizeItem['isPlayer'] = true
      let nowItemIndex = infoBoxTemp.resizeItemIndex
      let cloneItem = infoBoxTemp.cloneItem
      let startX = infoBoxTemp.startX
      let startY = infoBoxTemp.startY
      let oldSizeX = infoBoxTemp.oldSizeX
      let oldSizeY = infoBoxTemp.oldSizeY
      let originWidth = infoBoxTemp.originWidth
      let originHeight = infoBoxTemp.originHeight

      let moveXSize = e.pageX - startX //X方向移动的距离
      let moveYSize = e.pageY - startY //Y方向移动的距离
      let addSizeX =
        moveXSize % cellWidth.value > (cellWidth.value / 4) * 1
          ? Math.floor(moveXSize / cellWidth.value + 1)
          : Math.floor(moveXSize / cellWidth.value)
      let addSizeY =
        moveYSize % cellHeight.value > (cellHeight.value / 4) * 1
          ? Math.floor(moveYSize / cellHeight.value + 1)
          : Math.floor(moveYSize / cellHeight.value)

      let nowX = oldSizeX + addSizeX > 0 ? oldSizeX + addSizeX : 1
      let nowY = oldSizeY + addSizeY > 0 ? oldSizeY + addSizeY : 1

      debounce(
        (function (addSizeX, addSizeY) {
          return function () {
            resizePlayer(resizeItem, {
              sizeX: nowX,
              sizeY: nowY
            })
          }
        })(addSizeX, addSizeY),
        10
      )

      let nowWidth = originWidth + moveXSize
      nowWidth = nowWidth <= baseWidth.value ? baseWidth.value : nowWidth
      let nowHeight = originHeight + moveYSize
      nowHeight = nowHeight <= baseHeight.value ? baseHeight.value : nowHeight
      //克隆元素实时改变大小
      cloneItem.css({
        width: nowWidth,
        height: nowHeight
      })
    } else if (moveItem) {
      scrollScreen(e)
      if (!draggable.value) return
      dragging.value(e, moveItem, moveItem._dragId)
      //problem
      moveItem['isPlayer'] = true
      let nowItemIndex = infoBoxTemp.moveItemIndex
      let cloneItem = infoBoxTemp.cloneItem
      let startX = infoBoxTemp.startX
      let startY = infoBoxTemp.startY
      let originX = infoBoxTemp.originX
      let originY = infoBoxTemp.originY
      let oldX = infoBoxTemp.oldX
      let oldY = infoBoxTemp.oldY

      let moveXSize = e.pageX - startX //X方向移动的距离
      let moveYSize = e.pageY - startY //Y方向移动的距离

      let nowCloneItemX = originX + moveXSize
      let nowCloneItemY = originY + moveYSize

      let newX = Math.floor(
        (nowCloneItemX + cloneItem.width() / 12 - baseMarginLeft.value) / cellWidth.value + 1
      )
      let newY = Math.floor(
        (nowCloneItemY + cloneItem.height() / 12 - baseMarginTop.value) / cellHeight.value + 1
      )
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

      cloneItem.css({
        left: nowCloneItemX + 'px',
        top: nowCloneItemY + 'px'
      })
    }
  }

  const up = () => {
    handleMouseUp(e, item, index)
    document.removeEventListener('mousemove', itemMouseMove)
    document.removeEventListener('mouseup', up)
  }

  document.addEventListener('mousemove', itemMouseMove)
  document.addEventListener('mouseup', up)
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

const endMove = e => {
  // console.log('endMove....')
  return {}
}

const endMoveI = e => {
  return {}
}

const moving = e => {
  return {}
}

const cellInit = () => {
  // 此处向下取整 保留1位小数,why: 矩阵模式计算 x,y时 会使用 style.left/cellWidth style.top/cellWidth
  // 当初始状态细微的差距(主要是减少)都会导致 x，y 减少一个矩阵大小造成偏移,
  cellWidth.value = Math.floor((baseWidth.value + baseMarginLeft.value) * 10) / 10
  cellHeight.value = Math.floor((baseHeight.value + baseMarginTop.value) * 10) / 10
}

const canvasSizeInit = () => {
  cellInit()
  reCalcCellWidth()
}

const canvasInit = () => {
  cellInit()
  positionBox = []
  coordinates = [] //坐标点集合
  lastTask = undefined
  isOverlay = false //是否正在交换位置
  moveTime = 80 //移动动画时间
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

/**
 * 计算当前item的位置和大小
 */
const nowItemStyle = (item, index) => {
  return {
    padding: curGap.value + 'px!important',
    width: cellWidth.value * item.sizeX - baseMarginLeft.value + 'px',
    height: cellHeight.value * item.sizeY - baseMarginTop.value + 'px',
    left: cellWidth.value * (item.x - 1) + baseMarginLeft.value + 'px',
    top: cellHeight.value * (item.y - 1) + baseMarginTop.value + 'px'
  }
}

const getList = () => {
  let returnList = _.sortBy(_.cloneDeep(componentData.value), 'y')
  let finalList = []
  _.forEach(returnList, function (item, index) {
    if (_.isEmpty(item)) return
    delete item['_dragId']
    delete item['show']
    finalList.push(item)
  })
  return finalList
}

/**
 * 获取x最大值
 */
const getMaxCell = () => {
  return maxCell.value
}

/**
 * 获取渲染状态
 */
const getRenderState = () => {
  return moveAnimate.value
}

const afterInitOk = func => {
  let timeId = setInterval(() => {
    if (moveAnimate.value) {
      clearInterval(timeId)
      func()
    }
  }, 100)
}
const addItemBox = item => {
  // componentData.value.push(item)
  nextTick(function () {
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
  // console.log('onStartMove')
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

const onDragging = (e, item, index) => {
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
  // console.log('onDragging=newX=' + newX + ';newY=' + newY)

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

const onResizing = (e, item, index) => {
  const { top, left, width, height } = item.style
  // item 中的 style 为当前实时的位置
  const infoBoxTemp = infoBox.value
  let resizeItem = _.get(infoBoxTemp, 'resizeItem')
  let moveItem = _.get(infoBoxTemp, 'moveItem')
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

  // console.log(
  //   'nowSizeX=' + nowSizeX + ';p1=' + (width % cellWidth.value) + ';p2=' + (cellWidth.value / 4) * 3
  // )
  const addSizeX = 1
  const addSizeY = 1

  // 移动时
  let oldX = infoBoxTemp.oldX
  let oldY = infoBoxTemp.oldY

  // 增加5px偏移量 防止resize时向下取整 组件向右偏移
  let newX = Math.floor((item.style.left + 5) / cellWidth.value + 1)
  let newY = Math.floor((item.style.top + 5) / cellHeight.value + 1)
  newX = newX > 0 ? newX : 1
  newY = newY > 0 ? newY : 1
  console.log(
    'onResizing=nowSizeX=' + nowSizeX + ';nowSizeY=' + nowSizeY + 'newX=' + newX + ';newY=' + newY
  )

  // 调整大小
  debounce(
    (function (newX, oldX, newY, oldY) {
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
    })(newX, oldX, newY, oldY),
    10
  )
}

const onMouseUp = (e, item, index) => {
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
  addItemBox(moveInItemInfo)
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

const userViewEnlargeOpen = item => {
  userViewEnlargeRef.value.dialogInit(canvasStyleData.value, canvasViewInfo.value[item.id], item)
}

const initSnapshotTimer = () => {
  snapshotTimer.value = setInterval(() => {
    snapshotStore.snapshotCatchToStore()
  }, 3000)
}

onMounted(() => {
  initSnapshotTimer()
  // 获取编辑器元素
  composeStore.getEditor()
  eventBus.on('hideArea', hideArea)
  eventBus.on('handleDragStartMoveIn', handleDragStartMoveIn)
  eventBus.on('handleDragEnd', handleDragEnd)
  eventBus.on('removeMatrixItem', removeItem)
  eventBus.on('addDashboardItem', addItemBox)
  eventBus.on('snapshotChange', canvasInit)
})

onBeforeUnmount(() => {
  if (snapshotTimer.value) {
    clearInterval(snapshotTimer.value)
    snapshotTimer.value = null
  }
  eventBus.off('hideArea', hideArea)
  eventBus.off('handleDragStartMoveIn', handleDragStartMoveIn)
  eventBus.off('handleDragEnd', handleDragEnd)
  eventBus.off('removeMatrixItem', removeItem)
  eventBus.off('addDashboardItem', addItemBox)
  eventBus.off('snapshotChange', canvasInit)
})

defineExpose({
  canvasSizeInit,
  canvasInit,
  afterInitOk,
  addItemBox,
  handleDragOver,
  getMoveItem,
  handleMouseUp
})
</script>

<template>
  <div
    id="editor"
    ref="container"
    class="editor"
    :class="{ edit: isEdit }"
    :style="editStyle"
    @contextmenu="handleContextMenu"
    @mousedown="handleMouseDown"
  >
    <!-- 网格线 -->
    <!--    <Grid />-->
    <drag-shadow
      v-if="infoBox && infoBox.moveItem"
      :base-height="baseHeight"
      :base-width="baseWidth"
      :cur-gap="curGap"
      :element="infoBox.moveItem"
    ></drag-shadow>

    <!--页面组件列表展示-->
    <Shape
      v-for="(item, index) in showComponentData"
      :key="item.id"
      :default-style="item.style"
      :style="getShapeItemShowStyle(item)"
      :active="item.id === (curComponent || {})['id']"
      :element="item"
      :index="index"
      :class="{ lock: item.isLock }"
      :base-cell-info="baseCellInfo"
      @onStartResize="onStartResize($event, item, index)"
      @onStartMove="onStartMove($event, item, index)"
      @onMouseUp="onMouseUp($event, item, index)"
      @onDragging="onDragging($event, item, index)"
      @onResizing="onResizing($event, item, index)"
      @userViewEnlargeOpen="userViewEnlargeOpen(item)"
    >
      <!--如果是视图 则动态获取预存的chart-view数据-->
      <component
        :is="findComponent(item.component)"
        v-if="item.component === 'UserView'"
        :id="'component' + item.id"
        class="component"
        :style="getComponentStyle(item.style)"
        :prop-value="item.propValue"
        :view="canvasViewInfo[item.id]"
        :element="item"
        :request="item.request"
        @input="handleInput"
      />

      <component
        :is="findComponent(item.component)"
        v-else-if="item.component != 'VText'"
        :id="'component' + item.id"
        class="component"
        :style="getComponentStyle(item.style)"
        :prop-value="item.propValue"
        :element="item"
        :request="item.request"
      />

      <component
        :is="findComponent(item.component)"
        v-else
        :id="'component' + item.id"
        class="component"
        :style="getComponentStyle(item.style)"
        :prop-value="item.propValue"
        :element="item"
        :request="item.request"
        @input="handleInput"
      />
    </Shape>
    <!-- 右击菜单 -->
    <ContextMenu />
    <!-- 标线 -->
    <MarkLine />
    <!-- 选中区域 -->
    <Area v-show="isShowArea" :start="start" :width="width" :height="height" />
    <user-view-enlarge ref="userViewEnlargeRef"></user-view-enlarge>
  </div>
</template>

<style lang="less" scoped>
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
