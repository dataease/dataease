<template>
  <div
    class="dragAndResize"
    ref="container"
    @mousedown="containerMouseDown($event)"
    @mouseup="endMove($event)"
    @mousemove="moving($event)"
  >
    <div v-if="renderOk">
      <div
        :class="{
          item: true,
          moveAnimation: moveAnimate,
          movingItem: item.isPlayer,
          canNotDrag: !draggable
        }"
        @mousedown="startMove($event, item, index)"
        :ref="'item' + index"
        v-for="(item, index) in yourList"
        :key="'item' + index"
        :style="nowItemStyle(item, index)"
      >
        <slot :name="'slot' + index"></slot>
        <span
          class="resizeHandle"
          v-show="resizable"
          @mousedown="startResize($event, item, index)"
        ></span>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import _ from 'lodash'
import $ from 'jquery'
import { toRefs, ref, onMounted, nextTick, getCurrentInstance } from 'vue'

let positionBox = []
let coordinates = [] //坐标点集合

let lastTask = undefined
let isOverlay = false //是否正在交换位置
let moveTime = 80 //移动动画时间

let itemMaxY = 0
let itemMaxX = 0
let currentInstance

const props = defineProps({
  yourList: {
    required: true,
    type: Array //String,Number,Boolean,Function,Object,Array
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

const renderOk = ref(false)
const moveAnimate = ref(false)
const list = ref([])
const cellWidth = ref(0)
const cellHeight = ref(0)
const maxCell = ref(0)
const infoBox = ref(null)

const {
  yourList,
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
  resizeEnd
} = toRefs(props)

onMounted(() => {
  currentInstance = getCurrentInstance()
})

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
 *
 * @param {any} item
 */
function addItemToPositionBox(item) {
  let pb = positionBox
  if (item.x <= 0 || item.y <= 0) return

  for (let i = item.x - 1; i < item.x - 1 + item.sizex; i++) {
    for (let j = item.y - 1; j < item.y - 1 + item.sizey; j++) {
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
  $(currentInstance.$el).css('height', (itemMaxY + 2) * cellHeight.value + 'px')
}

function removeItemFromPositionBox(item) {
  let pb = positionBox
  if (item.x <= 0 || item.y <= 0) return
  for (let i = item.x - 1; i < item.x - 1 + item.sizex; i++) {
    for (let j = item.y - 1; j < item.y - 1 + item.sizey; j++) {
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
function recalcCellWidth() {
  //problem
  let containerNode = currentInstance.$refs['container']
  let containerWidth = containerNode.offsetWidth

  let cells = Math.round(containerWidth / cellWidth.value)
  maxCell.value = cells
}

function init() {
  cellWidth.value = baseWidth.value + baseMarginLeft.value
  cellHeight.value = baseHeight.value + baseMarginTop.value
  positionBox = []
  coordinates = [] //坐标点集合
  lastTask = undefined
  isOverlay = false //是否正在交换位置
  moveTime = 80 //移动动画时间
  itemMaxY = 0
  itemMaxX = 0

  recalcCellWidth()
  resetPositionBox()

  let i = 0
  let timeId = setInterval(function () {
    if (i >= yourList.value.length) {
      clearInterval(timeId)
      nextTick(() => {
        moveAnimate.value = true
      })
    } else {
      let item = yourList.value[i]
      addItem(item, i)
      i++
    }
  }, 1)
  renderOk.value = true
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

  item.sizex = newSize.sizex
  item.sizey = newSize.sizey

  if (item.sizex + item.x - 1 > itemMaxX) {
    item.sizex = itemMaxX - item.x + 1
  }

  if (item.sizey + item.y > itemMaxY) {
    fillPositionBox(item.y + item.sizey)
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
  if (item.sizex > itemMaxX) {
    item.sizex = itemMaxX
  }

  if (item.sizex < 1) {
    item.sizex = 1
  }

  if (item.x + item.sizex - 1 > itemMaxX) {
    item.x = itemMaxX - item.sizex + 1
    if (item.x < 1) {
      item.x = 1
    }
  }

  if (item.y < 1) {
    item.y = 1
  }

  if (item.sizey < 1) {
    item.sizey = 1
  }

  if (item.y + item.sizey > itemMaxY - 1) {
    fillPositionBox(item.y + item.sizey - 1)
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
  let item = yourList.value[index]
  removeItemFromPositionBox(item)
  let belowItems = findBelowItems(item)
  _.forEach(belowItems, function (upItem) {
    let canGoUpRows = canItemGoUp(upItem)
    if (canGoUpRows > 0) {
      moveItemUp(upItem, canGoUpRows)
    }
  })
  yourList.value.splice(index, 1, {})
}

function addItem(item, index) {
  if (index < 0) {
    index = yourList.value.length
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
  movePlayer.call(this, item, {
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
  let width = cellWidth.value * item.sizex - baseMarginLeft.value
  let height = cellHeight.value * item.sizey - baseMarginTop.value
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
  let width = cellWidth.value * item.sizex - baseMarginLeft.value
  let height = cellHeight.value * item.sizey - baseMarginTop.value
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
    let moveSize = item.y + item.sizey - downItem['y']
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
    for (let cell = item.x - 1; cell < item.x - 1 + item.sizex; cell++) {
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
  if (item.y + item.sizey > itemMaxY) {
    itemMaxY = item.y + item.sizey
  }
}

/**
 * 寻找子元素到父元素的最大距离
 */
function calcDiff(parent, son, size) {
  let diffs = []
  for (let i = son.x - 1; i < son.x - 1 + son.sizex; i++) {
    let temp_y = 0
    for (let j = parent.y - 1 + parent.sizey; j < son.y - 1; j++) {
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
  for (let cell = item.x - 1; cell < item.x - 1 + item.sizex; cell++) {
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
  resizeStart.value(null, e, item, index)
  // e.preventDefault();
  let target = $(e.target)

  if (!infoBox.value) {
    infoBox.value = {}
  }
  let itemNode = target.parents('.item')
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

const startMove = (e, item, index) => {
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
    dragStart.value.call(null, e, item, index)
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

  $(currentInstance.$el).append(infoBoxTemp.cloneItem)

  infoBoxTemp.orignX = infoBoxTemp.cloneItem.position().left //克隆对象原始X位置
  infoBoxTemp.orignY = infoBoxTemp.cloneItem.position().top
  infoBoxTemp.oldX = item.x //实际对象原始X位置
  infoBoxTemp.oldY = item.y
  infoBoxTemp.oldSizeX = item.sizex
  infoBoxTemp.oldSizeY = item.sizey
  infoBoxTemp.orignWidth = infoBoxTemp.cloneItem.prop('offsetWidth')
  infoBoxTemp.orignHeight = infoBoxTemp.cloneItem.prop('offsetHeight')

  function itemMouseMove(e) {
    let moveItem = _.get(infoBoxTemp, 'moveItem')
    let resizeItem = _.get(infoBoxTemp, 'resizeItem')

    if (resizeItem) {
      //调整大小时
      resizing.value.call(null, e, resizeItem, resizeItem._dragId)

      currentInstance.$set(resizeItem, 'isPlayer', true)
      let nowItemIndex = infoBoxTemp.resizeItemIndex
      let cloneItem = infoBoxTemp.cloneItem
      let startX = infoBoxTemp.startX
      let startY = infoBoxTemp.startY
      let oldSizeX = infoBoxTemp.oldSizeX
      let oldSizeY = infoBoxTemp.oldSizeY
      let orignWidth = infoBoxTemp.orignWidth
      let orignHeight = infoBoxTemp.orignHeight

      let moveXSize = e.pageX - startX //X方向移动的距离
      let moveYSize = e.pageY - startY //Y方向移动的距离

      let addSizex =
        moveXSize % cellWidth.value > (cellWidth.value / 4) * 1
          ? Math.floor(moveXSize / cellWidth.value + 1)
          : Math.floor(moveXSize / cellWidth.value)
      let addSizey =
        moveYSize % cellHeight.value > (cellHeight.value / 4) * 1
          ? Math.floor(moveYSize / cellHeight.value + 1)
          : Math.floor(moveYSize / cellHeight.value)

      let nowX = oldSizeX + addSizex > 0 ? oldSizeX + addSizex : 1
      let nowY = oldSizeY + addSizey > 0 ? oldSizeY + addSizey : 1

      debounce(
        (function (addSizex, addSizey) {
          return function () {
            resizePlayer(resizeItem, {
              sizex: nowX,
              sizey: nowY
            })
          }
        })(addSizex, addSizey),
        10
      )

      let nowWidth = orignWidth + moveXSize
      nowWidth = nowWidth <= baseWidth.value ? baseWidth.value : nowWidth
      let nowHeight = orignHeight + moveYSize
      nowHeight = nowHeight <= baseHeight.value ? baseHeight.value : nowHeight
      //克隆元素实时改变大小
      cloneItem.css({
        width: nowWidth,
        height: nowHeight
      })
    } else if (moveItem) {
      scrollScreen(e)
      if (!draggable.value) return
      dragging.value.call(null, e, moveItem, moveItem._dragId)

      currentInstance.$set(moveItem, 'isPlayer', true)
      // this.$set(moveItem, "show", false);
      let nowItemIndex = infoBoxTemp.moveItemIndex
      let cloneItem = infoBoxTemp.cloneItem
      let startX = infoBoxTemp.startX
      let startY = infoBoxTemp.startY
      let orignX = infoBoxTemp.orignX
      let orignY = infoBoxTemp.orignY
      let oldX = infoBoxTemp.oldX
      let oldY = infoBoxTemp.oldY

      let moveXSize = e.pageX - startX //X方向移动的距离
      let moveYSize = e.pageY - startY //Y方向移动的距离

      let nowCloneItemX = orignX + moveXSize
      let nowCloneItemY = orignY + moveYSize

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

  $(window).mousemove(itemMouseMove)

  $(window).mouseup(function itemMouseUp(e) {
    if (_.isEmpty(infoBox.value)) return
    if (infoBox.value.cloneItem) {
      infoBox.value.cloneItem.remove()
    }
    if (infoBox.value.resizeItem) {
      currentInstance.$delete(infoBox.value.resizeItem, 'isPlayer')
      resizeEnd.value.call(null, e, infoBox.value.resizeItem, infoBox.value.resizeItem._dragId)
    }
    if (infoBox.value.moveItem) {
      dragEnd?.value(null, e, infoBox.value.moveItem, infoBox.value.moveItem._dragId)
      currentInstance.$set(infoBox.value.moveItem, 'show', true)
      currentInstance.$delete(infoBox.value.moveItem, 'isPlayer')
    }
    infoBox.value = {}

    $(currentInstance).off('mousemove', itemMouseMove)
    $(currentInstance).off('mouseup', itemMouseUp)
  })
}

const endMove = e => {
  return {}
}

const moving = e => {
  return {}
}

/**
 * 计算当前item的位置和大小
 */
const nowItemStyle = (item, index) => {
  return {
    width: cellWidth.value * item.sizex - baseMarginLeft.value + 'px',
    height: cellHeight.value * item.sizey - baseMarginTop.value + 'px',
    left: cellWidth.value * (item.x - 1) + baseMarginLeft.value + 'px',
    top: cellHeight.value * (item.y - 1) + baseMarginTop.value + 'px'
  }
}

const getList = () => {
  let returnList = _.sortBy(_.cloneDeep(yourList.value), 'y')
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
  yourList.value.push(item)
  nextTick(function () {
    addItem(item, yourList.value.length - 1)
  })
}
</script>

<style lang="less">
.dragAndResize {
  position: relative;

  user-select: none;

  * {
    box-sizing: border-box;
    margin: 0;
    padding: 0;
  }

  .item {
    position: absolute;

    width: 100px;
    height: 100px;

    cursor: move;

    border: 1px solid;
    background-color: #fff;

    .resizeHandle {
      position: absolute;
      right: 2px;
      bottom: 2px;

      width: 0;
      height: 0;

      cursor: nw-resize;

      opacity: 0.5;
      border-bottom: 10px solid black;
      border-left: 10px solid transparent;
    }
  }

  .moveAnimation {
    transition: top 80ms ease;
  }

  .canNotDrag {
    cursor: default !important;
  }

  .cloneNode {
    z-index: 3;

    transition: none;

    opacity: 0.5;
  }

  .movingItem {
    position: absolute;

    border: none;
    &:before {
      position: absolute;
      z-index: 2;
      top: 0;
      left: 0;

      width: 100%;
      height: 100%;

      content: '';

      background-color: #bbb;
    }
  }

  .positionBox {
    position: fixed;
    top: 0;
    right: 100px;

    overflow: auto;

    width: 500px;
    height: 500px;

    border: 1px solid;
  }

  .coords {
    position: fixed;
    right: 100px;
    bottom: 200px;

    overflow: auto;

    width: 200px;
    height: 200px;

    border: 1px solid;
  }
}
</style>
