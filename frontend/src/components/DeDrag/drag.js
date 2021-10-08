import _ from 'lodash'
import $ from 'jquery'

let positionBox = []
let coordinates = [] // 坐标点集合

let lastTask
let isOverlay = false // 是否正在交换位置
let moveTime = 80 // 移动动画时间

let itemMaxY = 0
let itemMaxX = 0

function debounce(func, time) {
  if (!isOverlay) {
    (function(t) {
      isOverlay = true
      setTimeout(function() {
        t()
        setTimeout(function() {
          isOverlay = false
          if (lastTask !== undefined) {
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
    const body = $(document.body)
    body.scrollTop(body.scrollTop() + 20)
  } else if (e.clientY <= 150) {
    const body = $(document.body)
    body.scrollTop(body.scrollTop() - 20)
  }
}

/**
 * 重置位置盒子
 *
 */
function resetPositionBox() {
  // 根据当前容器的宽度来决定多少列
  itemMaxX = this.maxCell
  const rows = 1 // 初始100行，后面根据需求会自动增加
  for (let i = 0; i < rows; i++) {
    const row = []

    for (let j = 0; j < this.maxCell; j++) {
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
  const pb = positionBox
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
  const pb = positionBox
  maxY += 2
  for (let j = 0; j < maxY; j++) {
    if (pb[j] === undefined) {
      const row = []
      for (let i = 0; i < itemMaxX; i++) {
        row.push({
          el: false
        })
      }
      pb.push(row)
    }
  }

  itemMaxY = maxY

  $(this.$el).css('height', ((itemMaxY + 2) * this.cellHeight) + 'px')
}

function removeItemFromPositionBox(item) {
  const pb = positionBox
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
  const containerNode = this.$refs['container']
  const containerWidth = containerNode.offsetWidth

  const cells = Math.round(containerWidth / this.cellWidth)
  this.maxCell = cells

  // if (containerWidth % this.cellWidth !=== 0) {
  //     this.cellWidth += containerWidth % this.cellWidth / cells;
  // }
}

function init() {
  this.cellWidth = this.baseWidth + this.baseMarginLeft
  this.cellHeight = this.baseHeight + this.baseMarginTop

  positionBox = []
  coordinates = [] // 坐标点集合

  lastTask = undefined
  isOverlay = false // 是否正在交换位置
  moveTime = 80 // 移动动画时间

  itemMaxY = 0
  itemMaxX = 0

  const vm = this

  recalcCellWidth.call(this)

  resetPositionBox.call(this)

  let i = 0
  const timeid = setInterval(function() {
    if (i >= vm.yourList.length) {
      clearInterval(timeid)
      vm.$nextTick(function() {
        vm.moveAnimate = true
      })
    } else {
      const item = vm.yourList[i]
      addItem.call(vm, item, i)
      i++
    }
  }, 1)
  vm.renderOk = true
}

function resizePlayer(item, newSize) {
  const vm = this
  removeItemFromPositionBox(item)

  const belowItems = findBelowItems.call(this, item)

  _.forEach(belowItems, function(upItem) {
    const canGoUpRows = canItemGoUp(upItem)

    if (canGoUpRows > 0) {
      moveItemUp.call(vm, upItem, canGoUpRows)
    }
  })

  item.sizex = newSize.sizex
  item.sizey = newSize.sizey

  if (item.sizex + item.x - 1 > itemMaxX) {
    item.sizex = itemMaxX - item.x + 1
  }

  if (item.sizey + item.y > itemMaxY) {
    fillPositionBox.call(this, item.y + item.sizey)
  }

  emptyTargetCell.call(this, item)

  addItemToPositionBox.call(this, item)

  changeItemCoord.call(this, item)

  const canGoUpRows = canItemGoUp(item)

  if (canGoUpRows > 0) {
    moveItemUp.call(this, item, canGoUpRows)
  }
}

/**
 * 检查移动的位置，如果不合法，会自动修改
 *
 * @param {any} item
 * @param {any} position
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
    fillPositionBox.call(this, item.y + item.sizey - 1)
  }
}

/**
 * 移动正在拖动的元素
 *
 * @param {any} item
 * @param {any} position
 */
function movePlayer(item, position) {
  const vm = this
  removeItemFromPositionBox(item)

  const belowItems = findBelowItems.call(this, item)

  _.forEach(belowItems, function(upItem) {
    const canGoUpRows = canItemGoUp(upItem)
    if (canGoUpRows > 0) {
      moveItemUp.call(vm, upItem, canGoUpRows)
    }
  })

  item.x = position.x
  item.y = position.y

  checkItemPosition.call(this, item, position)

  emptyTargetCell.call(this, item)

  addItemToPositionBox.call(this, item)

  changeItemCoord.call(this, item)

  const canGoUpRows = canItemGoUp(item)

  if (canGoUpRows > 0) {
    moveItemUp.call(this, item, canGoUpRows)
  }
}

function removeItem(index) {
  const vm = this
  const item = this.yourList[index]
  removeItemFromPositionBox(item)

  const belowItems = findBelowItems.call(this, item)

  // $(this.$refs['item' + item._dragId][0]).remove();

  _.forEach(belowItems, function(upItem) {
    const canGoUpRows = canItemGoUp(upItem)

    if (canGoUpRows > 0) {
      moveItemUp.call(vm, upItem, canGoUpRows)
    }
  })

  this.yourList.splice(index, 1, {})
}

function addItem(item, index) {
  if (index < 0) {
    index = this.yourList.length
  }
  item._dragId = index

  checkItemPosition.call(this, item, {
    x: item.x,
    y: item.y
  })

  emptyTargetCell.call(this, item)

  addItemToPositionBox.call(this, item)

  const canGoUpRows = canItemGoUp(item)

  if (canGoUpRows > 0) {
    moveItemUp.call(this, item, canGoUpRows)
  }

  // 生成坐标点
  // makeCoordinate.call(this, item);
}

function changeToCoord(left, top, width, height) {
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
 *
 * @param {any} tCoord 比对对象的坐标
 */
function findClosetCoords(item, tCoord) {
  if (isOverlay) return
  let i = coordinates.length
  let collisionsItem = []
  while (i--) {
    const nowCoord = coordinates[i]
    if (item._dragId === nowCoord.el._dragId) {
      continue
    }

    if (tCoord.x2 < nowCoord.x1 || tCoord.x1 > nowCoord.x2 || tCoord.y2 < nowCoord.y1 || tCoord.y1 > nowCoord.y2) {
      continue
    } else {
      collisionsItem.push({
        centerDistance: Math.sqrt(Math.pow(tCoord.c1 - nowCoord.c1, 2) + Math.pow(tCoord.c2 - nowCoord.c2, 2)),
        coord: nowCoord
      })
    }
  }

  if (collisionsItem.length <= 0) {
    return
  }

  isOverlay = true

  collisionsItem = _.sortBy(collisionsItem, 'area')

  movePlayer.call(this, item, {
    x: collisionsItem[0].coord.el.x,
    y: collisionsItem[0].coord.el.y
  })

  setTimeout(function() {
    isOverlay = false
  }, 200)
}

/**
 * 生成坐标点
 *
 * @param {any} item
 */
function makeCoordinate(item) {
  const width = this.cellWidth * (item.sizex) - this.baseMarginLeft
  const height = this.cellHeight * (item.sizey) - this.baseMarginTop
  const left = this.cellWidth * (item.x - 1) + this.baseMarginLeft
  const top = this.cellHeight * (item.y - 1) + this.baseMarginTop

  const coord = {
    x1: left,
    x2: left + width,
    y1: top,
    y2: top + height,
    c1: left + width / 2,
    c2: top + height / 2,
    el: item
  }

  coordinates.push(coord)
}

function changeItemCoord(item) {
  const width = this.cellWidth * (item.sizex) - this.baseMarginLeft
  const height = this.cellHeight * (item.sizey) - this.baseMarginTop
  const left = this.cellWidth * (item.x - 1) + this.baseMarginLeft
  const top = this.cellHeight * (item.y - 1) + this.baseMarginTop

  const coord = {
    x1: left,
    x2: left + width,
    y1: top,
    y2: top + height,
    c1: left + width / 2,
    c2: top + height / 2,
    el: item
  }

  const index = _.findIndex(coordinates, function(o) {
    return o.el._dragId === item._dragId
  })
  if (index !== -1) {
    coordinates.splice(index, 1, coord)
  }
}

/**
 * 清空目标位置的元素
 *
 * @param {any} item
 */
function emptyTargetCell(item) {
  const vm = this
  const belowItems = findBelowItems(item)

  _.forEach(belowItems, function(downItem, index) {
    if (downItem._dragId === item._dragId) return
    const moveSize = item.y + item.sizey - downItem.y
    if (moveSize > 0) {
      moveItemDown.call(vm, downItem, moveSize)
    }
  })
}

/**
 * 当前位置的item能否上浮
 *
 * @param {any} item
 * @returns
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
 *
 * @param {any} items
 * @param {any} size
 */
function moveItemDown(item, size) {
  const vm = this
  removeItemFromPositionBox(item)

  const belowItems = findBelowItems(item)

  _.forEach(belowItems, function(downItem, index) {
    if (downItem._dragId === item._dragId) return
    const moveSize = calcDiff(item, downItem, size)
    if (moveSize > 0) {
      moveItemDown.call(vm, downItem, moveSize)
    }
  })

  const targetPosition = {
    y: item.y + size
  }
  setPlayerPosition.call(this, item, targetPosition)
  checkItemPosition.call(this, item, targetPosition)

  addItemToPositionBox.call(this, item)

  changeItemCoord.call(this, item)
}

function setPlayerPosition(item, position) {
  const vm = this
  position = position || {}

  const targetX = position.x || item.x
  const targetY = position.y || item.y

  item.x = targetX
  item.y = targetY

  if (item.y + item.sizey > itemMaxY) {
    itemMaxY = item.y + item.sizey
  }
}

/**
 * 寻找子元素到父元素的最大距离
 *
 * @param {any} parent
 * @param {any} son
 * @param {any} size
 */
function calcDiff(parent, son, size) {
  const diffs = []

  for (let i = son.x - 1; i < son.x - 1 + son.sizex; i++) {
    let temp_y = 0

    for (let j = parent.y - 1 + parent.sizey; j < son.y - 1; j++) {
      if (positionBox[j][i] && positionBox[j][i].el === false) {
        temp_y++
      }
    }
    diffs.push(temp_y)
  }

  const max_diff = Math.max.apply(Math, diffs)
  size = size - max_diff

  return size > 0 ? size : 0
}

function moveItemUp(item, size) {
  // console.log('moveItemUp')
  const vm = this

  removeItemFromPositionBox(item)

  const belowItems = findBelowItems.call(this, item)

  // item.y -= size;
  setPlayerPosition.call(this, item, {
    y: item.y - size
  })

  addItemToPositionBox.call(this, item)

  changeItemCoord.call(this, item)

  _.forEach(belowItems, function(upItem, index) {
    const moveSize = canItemGoUp(upItem)
    if (moveSize > 0) {
      moveItemUp.call(vm, upItem, moveSize)
    }
  })
}

function findBelowItems(item) {
  const belowItems = {}
  for (let cell = item.x - 1; cell < item.x - 1 + item.sizex; cell++) {
    for (let row = item.y - 1; row < positionBox.length; row++) {
      const target = positionBox[row][cell]
      if (target && target.el) {
        belowItems[target.el._dragId] = target.el
        break
      }
    }
  }

  return _.sortBy(_.values(belowItems), 'y')
}

export default {
  name: 'vue-power-drag',
  props: {
    yourList: {
      required: true,
      type: Array // String,Number,Boolean,Function,Object,Array
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
      default: function() {}
    },
    dragging: {
      required: false,
      type: Function,
      default: function() {}
    },
    dragEnd: {
      required: false,
      type: Function,
      default: function() {}
    },
    resizable: {
      required: false,
      type: Boolean,
      default: true
    },
    resizeStart: {
      required: false,
      type: Function,
      default: function() {}
    },
    resizing: {
      required: false,
      type: Function,
      default: function() {}
    },
    resizeEnd: {
      required: false,
      type: Function,
      default: function() {}
    }
  },
  data() {
    return {
      renderOk: false,
      moveAnimate: false,
      list: [],
      cellWidth: 0,
      cellHeight: 0,
      maxCell: 0
    }
  },
  computed: {
    positionBox() {
      return positionBox
    },
    coordinates() {
      return coordinates
    }
  },
  methods: {
    removeItem: removeItem,
    startResize(e, item, index) {
      console.log('startResize:' + index)
      if (!this.resizable) return
      this.resizeStart.call(null, e, item, index)

      // e.preventDefault();
      const target = $(e.target)

      if (!this.infoBox) {
        this.infoBox = {}
      }

      const itemNode = target.parents('.item')

      this.infoBox.resizeItem = item
      this.infoBox.resizeItemIndex = index
    },
    containerMouseDown(e) {
      console.log('containerMouseDown:')

      // e.preventDefault();
      if (!this.infoBox) {
        this.infoBox = {}
      }

      this.infoBox.startX = e.pageX
      this.infoBox.startY = e.pageY
    },
    startMove(e, item, index) {
      console.log('startMove:' + index)
      const vm = this
      // e.preventDefault();

      if (!this.infoBox) {
        this.infoBox = {}
      }
      const infoBox = this.infoBox
      const target = $(e.target)

      let className = target.attr('class')
      className = className || ''
      if (className.indexOf('dragHandle') === -1 && className.indexOf('item') === -1 && className.indexOf('resizeHandle') === -1) {
        return
      }

      if (className.includes('resizeHandle')) {

      } else if (this.draggable && (className.includes('dragHandle') || className.includes('item'))) {
        this.dragStart.call(null, e, item, index)
        infoBox.moveItem = item
        infoBox.moveItemIndex = index
      }

      infoBox.cloneItem = null
      infoBox.nowItemNode = null

      if (target.attr('class') && target.attr('class').indexOf('item') !== -1) {
        infoBox.nowItemNode = target
        infoBox.cloneItem = target.clone()
      } else {
        infoBox.nowItemNode = target.parents('.item')
        infoBox.cloneItem = infoBox.nowItemNode.clone()
      }
      infoBox.cloneItem.addClass('cloneNode')

      $(this.$el).append(infoBox.cloneItem)

      infoBox.orignX = infoBox.cloneItem.position().left // 克隆对象原始X位置
      infoBox.orignY = infoBox.cloneItem.position().top
      infoBox.oldX = item.x // 实际对象原始X位置
      infoBox.oldY = item.y
      infoBox.oldSizeX = item.sizex
      infoBox.oldSizeY = item.sizey
      infoBox.orignWidth = infoBox.cloneItem.prop('offsetWidth')
      infoBox.orignHeight = infoBox.cloneItem.prop('offsetHeight')

      function itemMouseMove(e) {
        // console.log('itemMouseMove')
        const moveItem = _.get(infoBox, 'moveItem')
        const resizeItem = _.get(infoBox, 'resizeItem')

        if (resizeItem) { // 调整大小时
          console.log('resizeItem')
          vm.resizing.call(null, e, resizeItem, resizeItem._dragId)

          vm.$set(resizeItem, 'isPlayer', true)
          const nowItemIndex = infoBox.resizeItemIndex
          const cloneItem = infoBox.cloneItem
          const startX = infoBox.startX
          const startY = infoBox.startY
          const oldSizeX = infoBox.oldSizeX
          const oldSizeY = infoBox.oldSizeY
          const orignWidth = infoBox.orignWidth
          const orignHeight = infoBox.orignHeight

          const moveXSize = e.pageX - startX // X方向移动的距离
          const moveYSize = e.pageY - startY // Y方向移动的距离

          const addSizex = (moveXSize) % vm.cellWidth > (vm.cellWidth / 4 * 1) ? parseInt(((moveXSize) / vm.cellWidth + 1)) : parseInt(((moveXSize) / vm.cellWidth))
          const addSizey = (moveYSize) % vm.cellHeight > (vm.cellHeight / 4 * 1) ? parseInt(((moveYSize) / vm.cellHeight + 1)) : parseInt(((moveYSize) / vm.cellHeight))

          const nowX = oldSizeX + addSizex > 0 ? oldSizeX + addSizex : 1
          const nowY = oldSizeY + addSizey > 0 ? oldSizeY + addSizey : 1

          debounce((function(addSizex, addSizey) {
            return function() {
              resizePlayer.call(vm, resizeItem, {
                sizex: nowX,
                sizey: nowY
              })
            }
          })(addSizex, addSizey), 10)

          let nowWidth = orignWidth + moveXSize
          nowWidth = nowWidth <= vm.baseWidth ? vm.baseWidth : nowWidth
          let nowHeight = orignHeight + moveYSize
          nowHeight = nowHeight <= vm.baseHeight ? vm.baseHeight : nowHeight
          // 克隆元素实时改变大小
          cloneItem.css({
            width: nowWidth,
            height: nowHeight
          })
        } else if (moveItem) {
          console.log('moveItem')

          scrollScreen(e)
          if (!vm.draggable) return
          vm.dragging.call(null, e, moveItem, moveItem._dragId)

          vm.$set(moveItem, 'isPlayer', true)
          // this.$set(moveItem, "show", false);
          const nowItemIndex = infoBox.moveItemIndex
          const cloneItem = infoBox.cloneItem
          const startX = infoBox.startX
          const startY = infoBox.startY
          const orignX = infoBox.orignX
          const orignY = infoBox.orignY
          const oldX = infoBox.oldX
          const oldY = infoBox.oldY

          const moveXSize = e.pageX - startX // X方向移动的距离
          const moveYSize = e.pageY - startY // Y方向移动的距离

          const nowCloneItemX = orignX + moveXSize
          const nowCloneItemY = orignY + moveYSize

          let newX = parseInt((nowCloneItemX + (cloneItem.width() / 12) - vm.baseMarginLeft) / vm.cellWidth + 1)
          let newY = parseInt((nowCloneItemY + (cloneItem.height() / 12) - vm.baseMarginTop) / vm.cellHeight + 1)
          newX = newX > 0 ? newX : 1
          newY = newY > 0 ? newY : 1

          debounce((function(newX, oldX, newY, oldY) {
            return function() {
              if (newX !== oldX || oldY !== newY) {
                // console.log("move");
                movePlayer.call(vm, moveItem, {
                  x: newX,
                  y: newY
                })

                infoBox.oldX = newX
                infoBox.oldY = newY
              }
            }
          })(newX, oldX, newY, oldY), 10)

          cloneItem.css({
            left: nowCloneItemX + 'px',
            top: nowCloneItemY + 'px'
          })
        }
      }

      $(window).mousemove(itemMouseMove)

      $(window).mouseup(function itemMouseUp(e) {
        if (_.isEmpty(vm.infoBox)) return
        if (vm.infoBox.cloneItem) {
          vm.infoBox.cloneItem.remove()
        }
        if (vm.infoBox.resizeItem) {
          vm.$delete(vm.infoBox.resizeItem, 'isPlayer')
          vm.resizeEnd.call(null, e, vm.infoBox.resizeItem, vm.infoBox.resizeItem._dragId)
        }
        if (vm.infoBox.moveItem) {
          vm.dragEnd.call(null, e, vm.infoBox.moveItem, vm.infoBox.moveItem._dragId)
          vm.$set(vm.infoBox.moveItem, 'show', true)
          vm.$delete(vm.infoBox.moveItem, 'isPlayer')
        }
        vm.infoBox = {}

        $(this).off('mousemove', itemMouseMove)
        $(this).off('mouseup', itemMouseUp)
      })
    },
    endMove(e) {

    },
    moving(e) {

    },
    /**
         * 计算当前item的位置和大小
         *
         * @param {any} item
         * @returns
         */
    nowItemStyle(item, index) {
      return {
        width: (this.cellWidth * (item.sizex) - this.baseMarginLeft) + 'px',
        height: (this.cellHeight * (item.sizey) - this.baseMarginTop) + 'px',
        left: (this.cellWidth * (item.x - 1) + this.baseMarginLeft) + 'px',
        top: (this.cellHeight * (item.y - 1) + this.baseMarginTop) + 'px'
      }
    },
    getList() {
      console.log('getList:')

      const returnList = _.sortBy(_.cloneDeep(this.yourList), 'y')
      const finalList = []
      _.forEach(returnList, function(item, index) {
        if (_.isEmpty(item)) return
        delete item['_dragId']
        delete item['show']
        finalList.push(item)
      })
      return finalList
    },
    /**
         * 获取x最大值
         *
         * @returns
         */
    getMaxCell() {
      console.log('getMaxCell:')

      return this.maxCell
    },
    /**
         * 获取渲染状态
         *
         * @returns
         */
    getRenderState() {
      console.log('getRenderState:')

      return this.moveAnimate
    },
    addItem: addItem,
    init: init,
    afterInitOk(func) {
      const timeid = setInterval(() => {
        if (this.moveAnimate) {
          clearInterval(timeid)
          func()
        }
      }, 100)
    },
    addItemBox(item) {
      this.yourList.push(item)

      this.$nextTick(function() {
        addItem.call(this, item, this.yourList.length - 1)
      })
    }
  },
  created() {

  },
  updated() {

  },
  mounted() {
    // init.call(this);
  }
}
