<template>
  <div
    v-if="editShow"
    id="editor"
    class="editor"
    :class="[
      {
        ['edit']: isEdit ,
        ['parent_transform']:!dialogVisible
      }
    ]"
    :style="editStyle"
    @mousedown="handleMouseDown"
    @scroll="canvasScroll"
  >
    <!-- 网格线 -->
    <Grid v-if="showGrid" :matrix-style="matrixStyle" />
    <PGrid v-if="psDebug" :position-box="positionBoxInfoArray" :matrix-style="matrixStyle" />
    <!--页面组件列表展示-->
    <de-drag
      v-for="(item, index) in componentData"
      ref="deDragRef"
      :key="item.id"
      :class="{item:true,moveAnimation:moveAnimate,movingItem:item.isPlayer}"
      :index="index"
      :x="getShapeStyleIntDeDrag(item.style,'left')"
      :y="getShapeStyleIntDeDrag(item.style,'top')"
      :w="getShapeStyleIntDeDrag(item.style,'width')"
      :h="getShapeStyleIntDeDrag(item.style,'height')"
      :r="item.style.rotate"
      :parent="true"
      :rotatable="rotatable"
      :default-style="getShapeStyleInt(item.style)"
      :active="item === curComponent"
      :element="item"
      class-name-active="de-drag-active"
      :snap="true"
      :snap-tolerance="2"
      :change-style="customStyle"
      :draggable="deDraggable"
      :resizable="deResizable"
      :linkage-active="linkageActiveCheck(item)"
      :batch-opt-active="batchOptActiveCheck(item)"
      @refLineParams="getRefLineParams"
      @showViewDetails="showViewDetails($event,index)"
      @resizeView="resizeView(index,item)"
      @onResizeStart="startResize"
      @onDragStart="onStartMove"
      @onHandleUp="onMouseUp"
      @onDragging="onDragging"
      @onResizing="onResizing"
      @elementMouseDown="containerMouseDown"
      @amRemoveItem="removeItem(item._dragId)"
      @amAddItem="addItemBox(item)"
      @linkJumpSet="linkJumpSet(item)"
      @boardSet="boardSet(item)"
      @canvasDragging="canvasDragging"
      @editComponent="editComponent(index,item)"
    >
      <de-out-widget
        v-if="renderOk && item.type==='custom'"
        :id="'component' + item.id"
        ref="wrapperChild"
        class="component"
        :style="getComponentStyleDefault(item.style)"
        :prop-value="item.propValue"
        :element="item"
        :is-relation="searchButtonInfo && searchButtonInfo.buttonExist && searchButtonInfo.relationFilterIds.includes(item.id)"
        :out-style="getShapeStyleInt(item.style)"
        :active="item === curComponent"
        :h="getShapeStyleIntDeDrag(item.style,'height')"
      />
      <component
        :is="item.component"
        v-else-if="renderOk && item.type==='other'"
        :id="'component' + item.id"
        ref="wrapperChild"
        class="component"
        :style="getComponentStyle(item.style)"
        :prop-value="item.propValue"
        :element="item"
        :out-style="getShapeStyleInt(item.style)"
        :active="item === curComponent"
      />
      <component
        :is="item.component"
        v-else-if="renderOk"
        :id="'component' + item.id"
        ref="wrapperChild"
        class="component"
        :filters="filterMap[item.propValue && item.propValue.viewId]"
        :style="getComponentStyleDefault(item.style)"
        :prop-value="item.propValue"
        :element="item"
        :out-style="getShapeStyleInt(item.style)"
        :active="item === curComponent"
        :edit-mode="'edit'"
        :h="getShapeStyleIntDeDrag(item.style,'height')"
        :canvas-style-data="canvasStyleData"
        @input="handleInput"
        @trigger-plugin-edit="pluginEditHandler"
      />
    </de-drag>
    <!--拖拽阴影部分-->
    <!--    <drag-shadow v-if="(curComponent&&this.curComponent.optStatus.dragging)||dragComponentInfo" />-->
    <drag-shadow
      v-if="(curComponent&&curComponent.auxiliaryMatrix&&(curComponent.optStatus.dragging||curComponent.optStatus.resizing))||(dragComponentInfo)"
    />
    <!-- 右击菜单 -->
    <ContextMenu />
    <!-- 标线 (临时去掉标线 吸附等功能)-->
    <!--    <MarkLine />-->
    <!-- 选中区域 -->
    <!--    <Area v-show="isShowArea" :start="start" :width="width" :height="height" />-->

    <span
      v-for="(item, index) in vLine"
      v-show="item.display"
      :key="'v_'+index"
      class="ref-line v-line"
      :style="{
        left: item.position,
        top: item.origin,
        height: item.lineLength,
      }"
    />
    <span
      v-for="(item, index) in hLine"
      :key="'h_'+index"
      class="ref-line h-line"
      :style="{ top: item.position, left: item.origin, width: item.lineLength }"
    />

    <!--视图详情-->
    <el-dialog
      :visible.sync="chartDetailsVisible"
      width="80%"
      class="dialog-css"
      :destroy-on-close="true"
      :show-close="true"
      top="5vh"
    >
      <span v-if="chartDetailsVisible" style="position: absolute;right: 70px;top:15px">
        <el-button v-if="showChartInfoType==='enlarge' && showChartInfo && showChartInfo.type !== 'symbol-map'" class="el-icon-picture-outline" size="mini" @click="exportViewImg">
          {{ $t('chart.export_img') }}
        </el-button>
        <el-button v-if="showChartInfoType==='details'" size="mini" @click="exportExcel">
          <svg-icon icon-class="ds-excel" class="ds-icon-excel" />{{ $t('chart.export') }}Excel
        </el-button>
      </span>
      <UserViewDialog
        v-if="chartDetailsVisible"
        ref="userViewDialog"
        :chart="showChartInfo"
        :chart-table="showChartTableInfo"
        :canvas-style-data="canvasStyleData"
        :open-type="showChartInfoType"
      />
    </el-dialog>

    <el-dialog
      :visible.sync="linkJumpSetVisible"
      width="900px"
      class="dialog-css"
      :show-close="true"
      :destroy-on-close="true"
      :append-to-body="true"
    >
      <LinkJumpSet v-if="linkJumpSetVisible" :view-id="linkJumpSetViewId" @closeJumpSetDialog="closeJumpSetDialog" />
    </el-dialog>

    <el-dialog
      :visible.sync="boardSetVisible"
      width="750px"
      class="dialog-css"
      :close-on-click-modal="false"
      :show-close="false"
      :destroy-on-close="true"
      :append-to-body="true"
    >
      <background v-if="boardSetVisible" @backgroundSetClose="backgroundSetClose" />
    </el-dialog>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import Shape from './Shape'
import DeDrag from '@/components/DeDrag'

// eslint-disable-next-line no-unused-vars
import { getStyle, getComponentRotatedStyle } from '@/components/canvas/utils/style'
import { _$, imgUrlTrans } from '@/components/canvas/utils/utils'
import ContextMenu from './ContextMenu'
import MarkLine from './MarkLine'
import Area from './Area'
import eventBus from '@/components/canvas/utils/eventBus'
import Grid from './Grid'
import PGrid from './PGrid'
import { changeStyleWithScale } from '@/components/canvas/utils/translate'
import { deepCopy } from '@/components/canvas/utils/utils'
import UserViewDialog from '@/components/canvas/custom-component/UserViewDialog'
import DeOutWidget from '@/components/dataease/DeOutWidget'
import DragShadow from '@/components/DeDrag/shadow'
import bus from '@/utils/bus'
import LinkJumpSet from '@/views/panel/LinkJumpSet'
import { buildFilterMap, buildViewKeyMap, formatCondition, valueValid, viewIdMatch } from '@/utils/conditionUtil'
// 挤占式画布
import _ from 'lodash'
import $ from 'jquery'
import Background from '@/views/background/index'

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
  const rows = this.matrixCount.y // 初始100行，后面根据需求会自动增加
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
  try {
    const pb = positionBox
    if (item.x <= 0 || item.y <= 0) return

    for (let i = item.x - 1; i < item.x - 1 + item.sizex; i++) {
      for (let j = item.y - 1; j < item.y - 1 + item.sizey; j++) {
        if (pb[j][i]) {
          pb[j][i].el = item
        }
      }
    }
  } catch (e) {
    // igonre
  }
}

function fillPositionBox(maxY) {
  const pb = positionBox
  maxY += 1
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
}

function removeItemFromPositionBox(item) {
  const pb = positionBox
  if (!item || item.x <= 0 || item.y <= 0) return
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
  this.maxCell = this.matrixCount.x
}

function init() {
  this.cellWidth = this.baseWidth + this.baseMarginLeft
  this.cellHeight = this.baseHeight + this.baseMarginTop
  this.yourList = this.getList()

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
  initPosition(this)
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

  // 还原到像素
  item.style.width = item.sizex * this.matrixStyle.originWidth
  item.style.height = item.sizey * this.matrixStyle.originHeight

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
  try {
    const vm = this
    const item = this.yourList[index]
    removeItemFromPositionBox(item)

    const belowItems = findBelowItems.call(this, item)
    _.forEach(belowItems, function(upItem) {
      const canGoUpRows = canItemGoUp(upItem)
      if (canGoUpRows > 0) {
        moveItemUp.call(vm, upItem, canGoUpRows)
      }
    })
    this.yourList.splice(index, 1, {})
  } catch (e) {
  }
}

// 矩阵设计初始化的时候 预占位，防止编辑仪表板页面，初始化和视图编辑返回时出现组件位置变化问题
function initPosition(_this) {
  _this.yourList.forEach(item => {
    fillPositionBox.call(_this, item.y + item.sizey)
    addItemToPositionBox.call(_this, item)
  })
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

// eslint-disable-next-line no-unused-vars
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
// eslint-disable-next-line no-unused-vars
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
// eslint-disable-next-line no-unused-vars
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
  position = position || {}
  const targetX = position.x || item.x
  const targetY = position.y || item.y

  item.x = targetX
  item.y = targetY

  // 还原到像素
  item.style.left = ((item.x - 1) * this.matrixStyle.width) / this.scalePointWidth
  item.style.top = ((item.y - 1) * this.matrixStyle.height) / this.scalePointHeight
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
  const vm = this

  removeItemFromPositionBox(item)

  const belowItems = findBelowItems.call(this, item)
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
      try {
        const target = positionBox[row][cell]
        if (target && target.el) {
          belowItems[target.el._dragId] = target.el
          break
        }
      } catch (e) {
        console.error('positionBox igonre', e)
      }
    }
  }

  return _.sortBy(_.values(belowItems), 'y')
}

// eslint-disable-next-line no-unused-vars
function getoPsitionBox() {
  return positionBox
}

export default {
  components: {
    Background,
    Shape,
    ContextMenu,
    MarkLine,
    Area,
    Grid,
    PGrid,
    DeDrag,
    UserViewDialog,
    DeOutWidget,
    DragShadow,
    LinkJumpSet
  },
  props: {
    isEdit: {
      type: Boolean,
      require: false,
      default: true
    },
    outStyle: {
      type: Object,
      require: false,
      default: null
    },
    // 挤占式画布设计
    dragStart: {
      required: false,
      type: Function,
      default: function() {
      }
    },
    dragging: {
      required: false,
      type: Function,
      default: function() {
      }
    },
    dragEnd: {
      required: false,
      type: Function,
      default: function() {
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
      default: function() {
      }
    },
    resizing: {
      required: false,
      type: Function,
      default: function() {
      }
    },
    resizeEnd: {
      required: false,
      type: Function,
      default: function() {
      }
    },
    matrixCount: {
      required: true,
      type: Object
    },
    scrollTop: {
      type: Number,
      default: 0
    }
  },
  data() {
    return {
      boardSetVisible: false,
      psDebug: false, // 定位调试模式
      editorX: 0,
      editorY: 0,
      start: { // 选中区域的起点
        x: 0,
        y: 0
      },
      width: 0,
      height: 0,
      isShowArea: false,
      conditions: [],

      // 初始化 设置放大比例为3倍 防止在边框限制时 出现较小的父级支持造成组件位移
      scaleWidth: 100,
      scaleHeight: 100,
      // 放大比例 小数
      scalePointWidth: 1,
      scalePointHeight: 1,

      timer: null,
      needToChangeHeight: [
        'top',
        'height',
        'fontSize',
        'borderWidth'
      ],
      needToChangeWidth: [
        'left',
        'width'
      ],
      // private 是否可旋转
      rotatable: false,
      // 矩阵大小
      matrixStyle: {
        width: 80, // 当前尺寸
        height: 20,
        originWidth: 80, // 原始尺寸
        originHeight: 20
      },
      customStyleHistory: null,
      showDrag: true,
      vLine: [],
      hLine: [],
      changeIndex: 0,
      timeMachine: null,
      outStyleOld: null,
      chartDetailsVisible: false,
      showChartInfo: {},
      showChartTableInfo: {},
      showChartInfoType: 'details',
      // 挤占式画布设计
      baseWidth: 100,
      baseHeight: 100,
      baseMarginLeft: 0,
      baseMarginTop: 0,
      draggable: true,
      renderOk: false,
      moveAnimate: false,
      list: [],
      cellWidth: 100,
      cellHeight: 100,
      maxCell: 0,
      lastComponentDataLength: 0,
      positionBoxInfoArray: [],
      yourList: [],
      linkJumpSetVisible: false,
      linkJumpSetViewId: null,
      editShow: false,
      buttonFilterMap: null,
      autoTrigger: true
    }
  },
  computed: {
    deDraggable() {
      return !this.linkageSettingStatus && !this.batchOptStatus
    },
    deResizable() {
      return !this.linkageSettingStatus && !this.batchOptStatus
    },
    showExportImgButton() {
      // if the chart type belong to table,'export image' button should be hidden
      return this.showChartInfo.type && !this.showChartInfo.type.includes('table')
    },
    showGrid() {
      if (this.canvasStyleData && this.canvasStyleData.aidedDesign) {
        return this.canvasStyleData.aidedDesign.showGrid
      } else {
        return false
      }
    },
    editStyle() {
      return {
        height: this.outStyle.height + this.scrollTop + 'px !important'
      }
    },
    dialogVisible() {
      return this.chartDetailsVisible || this.linkJumpSetVisible
    },
    // 挤占式画布设计
    coordinates() {
      return coordinates
    },
    customStyle() {
      let style = {
        width: '100%',
        height: '100%'
      }

      if (this.canvasStyleData.openCommonStyle) {
        if (this.canvasStyleData.panel.backgroundType === 'image' && this.canvasStyleData.panel.imageUrl) {
          style = {
            background: `url(${imgUrlTrans(this.canvasStyleData.panel.imageUrl)}) no-repeat`,
            ...style
          }
        } else if (this.canvasStyleData.panel.backgroundType === 'color') {
          style = {
            background: this.canvasStyleData.panel.color,
            ...style
          }
        }
      }
      return style
    },
    panelInfo() {
      return this.$store.state.panel.panelInfo
    },
    dragComponentInfo() {
      return this.$store.state.dragComponentInfo
    },

    ...mapState([
      'componentData',
      'curComponent',
      'canvasStyleData',
      'editor',
      'linkageSettingStatus',
      'curLinkageView',
      'doSnapshotIndex',
      'componentGap',
      'mobileLayoutStatus',
      'curCanvasScale',
      'batchOptStatus'
    ]),

    searchButtonInfo() {
      const result = this.buildButtonFilterMap(this.componentData)
      return result
    },
    filterMap() {
      const result = buildFilterMap(this.componentData)
      if (this.searchButtonInfo && this.searchButtonInfo.buttonExist && !this.searchButtonInfo.autoTrigger && this.searchButtonInfo.relationFilterIds) {
        for (const key in result) {
          if (Object.hasOwnProperty.call(result, key)) {
            let filters = result[key]
            filters = filters.filter(item => !this.searchButtonInfo.relationFilterIds.includes(item.componentId))
            result[key] = filters
          }
        }
      }
      return result
    }

  },
  watch: {
    customStyle: {
      handler(newVal) {
        // 获取当前宽高（宽高改变后重新渲染画布）
      },
      deep: true
    },
    outStyle: {
      handler(newVal, oldVla) {
        this.resizeParentBoundsRef()
        this.changeScale()
        this.outStyleOld = deepCopy(newVal)
      },
      deep: true
    },
    componentData: {
      handler(newVal, oldVla) {
        // 初始化时componentData 加载可能出现慢的情况 此时重新初始化一下matrix
        if (newVal.length !== this.lastComponentDataLength) {
          this.lastComponentDataLength = newVal.length
          this.initMatrix()
        }
      },
      deep: true
    },
    positionBox: {
      handler(newVal, oldVla) {
      },
      deep: true
    },
    // 镜像索引有变化 刷新一下矩阵（撤销重做等）
    doSnapshotIndex: {
      handler(newVal, oldVla) {
        this.initMatrix()
      },
      deep: true
    },
    autoTrigger: {
      handler(val, old) {
        if (val === old) return
        const result = buildFilterMap(this.componentData)
        for (const key in result) {
          if (Object.hasOwnProperty.call(result, key)) {
            let filters = result[key]
            if (this.searchButtonInfo && this.searchButtonInfo.buttonExist && !this.searchButtonInfo.autoTrigger && this.searchButtonInfo.relationFilterIds) {
              filters = filters.filter(item => !this.searchButtonInfo.relationFilterIds.includes(item.componentId))
            }

            this.filterMap[key] = filters

            this.componentData.forEach(item => {
              if (item.type === 'view' && item.propValue.viewId === key) {
                item.filters = filters
              }
            })
          }
        }
      },
      deep: true
    }
  },

  mounted() {
    this.canvasInit()
    // 获取编辑器元素
    this.$store.commit('getEditor')
    const _this = this
    eventBus.$on('hideArea', this.hideArea)
    eventBus.$on('startMoveIn', this.startMoveIn)
    eventBus.$on('openChartDetailsDialog', this.openChartDetailsDialog)
    bus.$on('onRemoveLastItem', this.removeLastItem)
    bus.$on('trigger-search-button', this.triggerSearchButton)
    bus.$on('trigger-reset-button', this.triggerResetButton)
    bus.$on('refresh-button-info', this.refreshButtonInfo)

    // 矩阵定位调试模式
    if (this.psDebug) {
      setInterval(() => {
        _this.positionBoxInfoArray = positionBox
      }, 500)
    }
  },
  beforeDestroy() {
    eventBus.$off('hideArea', this.hideArea)
    eventBus.$off('startMoveIn', this.startMoveIn)
    eventBus.$off('openChartDetailsDialog', this.openChartDetailsDialog)
    bus.$off('onRemoveLastItem', this.removeLastItem)
    bus.$off('trigger-search-button', this.triggerSearchButton)
    bus.$off('refresh-button-info', this.refreshButtonInfo)
    bus.$off('trigger-reset-button', this.triggerResetButton)
  },
  created() {
  },
  methods: {
    triggerResetButton() {
      this.triggerSearchButton(true)
    },
    refreshButtonInfo(isClear = false) {
      const result = this.buildButtonFilterMap(this.componentData, isClear)
      this.searchButtonInfo.buttonExist = result.buttonExist
      this.searchButtonInfo.relationFilterIds = result.relationFilterIds
      this.searchButtonInfo.filterMap = result.filterMap
      this.searchButtonInfo.autoTrigger = result.autoTrigger
      this.buttonFilterMap = this.searchButtonInfo.filterMap
    },
    triggerSearchButton(isClear = false) {
      this.refreshButtonInfo(isClear)
      this.buttonFilterMap = this.searchButtonInfo.filterMap

      this.componentData.forEach(component => {
        if (component.type === 'view' && this.buttonFilterMap[component.propValue.viewId]) {
          component.filters = this.buttonFilterMap[component.propValue.viewId]
        }
        if (component.type === 'de-tabs') {
          for (let idx = 0; idx < component.options.tabList.length; idx++) {
            const ele = component.options.tabList[idx].content
            if (!ele.type || ele.type !== 'view') continue
            ele.filters = this.buttonFilterMap[ele.propValue.viewId]
          }
        }
      })
    },
    buildButtonFilterMap(panelItems, isClear = false) {
      const result = {
        buttonExist: false,
        relationFilterIds: [],
        autoTrigger: true,
        filterMap: {}
      }
      if (!panelItems || !panelItems.length) return result
      let sureButtonItem = null
      result.buttonExist = panelItems.some(item => {
        if (item.type === 'custom-button' && item.serviceName === 'buttonSureWidget') {
          sureButtonItem = item
          return true
        }
      })

      if (!result.buttonExist) return result

      const customRange = sureButtonItem.options.attrs.customRange
      result.autoTrigger = sureButtonItem.options.attrs.autoTrigger
      this.autoTrigger = result.autoTrigger

      const allFilters = panelItems.filter(item => item.type === 'custom')

      const matchFilters = customRange && allFilters.filter(item => sureButtonItem.options.attrs.filterIds.includes(item.id)) || allFilters

      result.relationFilterIds = matchFilters.map(item => item.id)

      let viewKeyMap = buildViewKeyMap(panelItems)
      viewKeyMap = this.buildViewKeyFilters(matchFilters, viewKeyMap, isClear)
      result.filterMap = viewKeyMap
      return result
    },
    buildViewKeyFilters(panelItems, result, isClear = false) {
      const refs = this.$refs
      if (!this.$refs['wrapperChild'] || !this.$refs['wrapperChild'].length) return result
      const len = this.$refs['wrapperChild'].length
      panelItems.forEach((element) => {
        if (element.type !== 'custom') {
          return true
        }

        let param = null
        const index = this.getComponentIndex(element.id)
        if (index < 0 || index >= len) {
          return true
        }
        const wrapperChild = refs['wrapperChild'][index]
        if (!wrapperChild || !wrapperChild.getCondition) return true
        if (isClear) {
          wrapperChild.clearHandler && wrapperChild.clearHandler()
        }
        param = wrapperChild.getCondition && wrapperChild.getCondition()
        const condition = formatCondition(param)
        const vValid = valueValid(condition)
        const filterComponentId = condition.componentId
        Object.keys(result).forEach(viewId => {
          const vidMatch = viewIdMatch(condition.viewIds, viewId)
          const viewFilters = result[viewId]
          let j = viewFilters.length
          while (j--) {
            const filter = viewFilters[j]
            if (filter.componentId === filterComponentId) {
              viewFilters.splice(j, 1)
            }
          }
          vidMatch && vValid && viewFilters.push(condition)
        })
      })
      return result
    },
    getComponentIndex(id) {
      for (let index = 0; index < this.componentData.length; index++) {
        const item = this.componentData[index]
        if (item.id === id) return index
      }
      return -1
    },
    pluginEditHandler({ e, id }) {
      let index = -1
      for (let i = 0; i < this.componentData.length; i++) {
        const item = this.componentData[i]
        const itemId = item.id
        if (id === itemId) {
          index = i
          break
        }
      }
      if (index >= 0) {
        const _this = this
        _this.$refs.deDragRef && _this.$refs.deDragRef[index] && _this.$refs.deDragRef[index].triggerPluginEdit && _this.$refs.deDragRef[index].triggerPluginEdit(e)
      }
    },
    linkageActiveCheck(item) {
      return this.linkageSettingStatus && item === this.curLinkageView
    },
    batchOptActiveCheck(item) {
      return this.batchOptStatus && item.type === 'view'
    },
    canvasInit() {
      this.editShow = false
      setTimeout(() => {
        this.changeScale()
        this.editShow = true
      }, 500)
    },
    backgroundSetClose() {
      this.boardSetVisible = false
    },
    boardSet(item) {
      this.$emit('boardSet', item)
      this.boardSetVisible = true
    },
    changeStyleWithScale,
    handleMouseDown(e) {
      // 如果没有选中组件 在画布上点击时需要调用 e.preventDefault() 防止触发 drop 事件
      if (!this.curComponent || (this.curComponent.component !== 'v-text' && this.curComponent.component !== 'rect-shape')) {
        e.preventDefault()
      }
      this.hideArea()
      // 挤占式画布设计
      this.containerMouseDown(e)
    },

    hideArea() {
      this.isShowArea = 0
      this.width = 0
      this.height = 0
    },

    createGroup() {
      // 获取选中区域的组件数据
      const areaData = this.getSelectArea()
      if (areaData.length <= 1) {
        this.hideArea()
        return
      }

      // 根据选中区域和区域中每个组件的位移信息来创建 Group 组件
      // 要遍历选择区域的每个组件，获取它们的 left top right bottom 信息来进行比较
      let top = Infinity
      let left = Infinity
      let right = -Infinity
      let bottom = -Infinity
      areaData.forEach(component => {
        let style = {}
        if (component.component === 'Group') {
          component.propValue.forEach(item => {
            const rectInfo = _$(`#component${item.id}`).getBoundingClientRect()
            style.left = rectInfo.left - this.editorX
            style.top = rectInfo.top - this.editorY
            style.right = rectInfo.right - this.editorX
            style.bottom = rectInfo.bottom - this.editorY

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

      this.start.x = left
      this.start.y = top
      this.width = right - left
      this.height = bottom - top

      // 设置选中区域位移大小信息和区域内的组件数据
      this.$store.commit('setAreaData', {
        style: {
          left,
          top,
          width: this.width,
          height: this.height
        },
        components: areaData
      })
    },

    getSelectArea() {
      const result = []
      // 区域起点坐标
      const { x, y } = this.start
      // 计算所有的组件数据，判断是否在选中区域内
      this.componentData.forEach(component => {
        if (component.isLock) return

        const { left, top, width, height } = component.style
        if (x <= left && y <= top && (left + width <= x + this.width) && (top + height <= y + this.height)) {
          result.push(component)
        }
      })

      // 返回在选中区域内的所有组件
      return result
    },

    getShapeStyle(style) {
      const result = {};
      ['width', 'left'].forEach(attr => {
        result[attr] = this.format(style[attr], this.scaleWidth) + 'px'
      });
      ['height', 'top'].forEach(attr => {
        result[attr] = this.format(style[attr], this.scaleHeight) + 'px'
      })
      result.transform = 'rotate(' + style['rotate'] + 'deg)'

      return result
    },

    getShapeStyleInt(style) {
      const result = {};
      ['width', 'left'].forEach(attr => {
        result[attr] = this.format(style[attr], this.scaleWidth)
      });
      ['height', 'top'].forEach(attr => {
        result[attr] = this.format(style[attr], this.scaleHeight)
      })
      result['rotate'] = style['rotate']
      result['borderWidth'] = style['borderWidth']
      result['opacity'] = style['opacity']

      return result
    },

    getComponentStyleDefault(style) {
      return getStyle(style, ['top', 'left', 'width', 'height', 'rotate'])
      // return style
    },

    getComponentStyle(style) {
      //   return getStyle(style, ['top', 'left', 'width', 'height', 'rotate'])
      return style
    },

    handleInput(element, value) {
      // 根据文本组件高度调整 shape 高度
      // remain -自适应画布会导致一些问题 暂时不修改
      // this.$store.commit('setShapeStyle', { height: this.getTextareaHeight(element, value) })
    },

    getTextareaHeight(element, text) {
      // eslint-disable-next-line prefer-const
      let { lineHeight, fontSize, height } = element.style
      if (lineHeight === '') {
        lineHeight = 1.5
      }

      const newHeight = (text.split('<br>').length - 1) * lineHeight * fontSize
      return height > newHeight ? height : newHeight
    },

    format(value, scale) {
      // 自适应画布区域 返回原值
      return value * scale / 100
    },
    // 修改矩阵点
    changeComponentSizePoint(pointScale) {
      if (pointScale) {
        this.componentData.forEach((item, index) => {
          item.x = (item.x - 1) * pointScale + 1
          item.y = (item.y - 1) * pointScale + 1
          item.sizex = item.sizex * pointScale
          item.sizey = item.sizey * pointScale
        })
        this.changeScale()
      }
    },

    changeScale() {
      // 1.3 版本重新设计仪表板定位方式，基准画布宽高为 1600*900 宽度自适应当前画布获取缩放比例scaleWidth
      // 高度缩放比例scaleHeight = scaleWidth 基础矩阵为128*72 矩阵原始宽度12.5*12.5 矩阵高度可以调整

      if (this.outStyle.width && this.outStyle.height) {
        // 矩阵计算
        this.matrixStyle.originWidth = this.canvasStyleData.width / this.matrixCount.x
        this.matrixStyle.originHeight = this.canvasStyleData.height / this.matrixCount.y
        if (!this.canvasStyleData.selfAdaption) {
          this.matrixStyle.width = this.canvasStyleData.width / this.matrixCount.x
          this.matrixStyle.height = this.canvasStyleData.height / this.matrixCount.y
        } else {
          this.matrixStyle.width = this.outStyle.width / this.matrixCount.x
          this.matrixStyle.height = this.outStyle.height / this.matrixCount.y
        }
        this.baseWidth = this.matrixStyle.width
        this.baseHeight = this.matrixStyle.height
        this.cellWidth = this.matrixStyle.width
        this.cellHeight = this.matrixStyle.height
        this.initMatrix()

        this.scaleWidth = this.outStyle.width * 100 / this.canvasStyleData.width
        this.scaleHeight = this.outStyle.height * 100 / this.canvasStyleData.height
        this.scalePointWidth = this.scaleWidth / 100
        this.scalePointHeight = this.scaleHeight / 100
        this.$store.commit('setCurCanvasScale',
          {
            scaleWidth: this.scaleWidth,
            scaleHeight: this.scaleHeight,
            scalePointWidth: this.scalePointWidth,
            scalePointHeight: this.scalePointHeight,
            matrixStyleWidth: this.matrixStyle.width,
            matrixStyleHeight: this.matrixStyle.height,
            matrixStyleOriginWidth: this.matrixStyle.originWidth,
            matrixStyleOriginHeight: this.matrixStyle.originHeight
          })
        this.$store.commit('setPreviewCanvasScale', {
          scaleWidth: this.scalePointWidth,
          scaleHeight: this.scalePointHeight
        })
      }
    },
    getShapeStyleIntDeDrag(style, prop) {
      if (prop === 'rotate') {
        return style['rotate']
      }
      if (prop === 'width') {
        return this.format(style['width'], this.scaleWidth)
      }
      if (prop === 'left') {
        return this.format(style['left'], this.scaleWidth)
      }
      if (prop === 'height') {
        return this.format(style['height'], this.scaleHeight)
      }
      if (prop === 'top') {
        const top = this.format(style['top'], this.scaleHeight)
        return top
      }
    },
    getRefLineParams(params) {
      const { vLine, hLine } = params
      this.vLine = vLine
      this.hLine = hLine
    },
    resizeParentBounds() {
      this.destroyTimeMachine()
      this.changeIndex++
      this.parentBoundsChange(this.changeIndex)
    },
    parentBoundsChange(index) {
      this.timeMachine = setTimeout(() => {
        if (index === this.changeIndex) {
          this.changeScale()
        }
        this.destroyTimeMachine()
      }, 1500)
    },
    destroyTimeMachine() {
      this.timeMachine && clearTimeout(this.timeMachine)
      this.timeMachine = null
    },
    openChartDetailsDialog(chartInfo) {
      this.showChartInfo = chartInfo.chart
      this.showChartTableInfo = chartInfo.tableChart
      this.showChartInfoType = chartInfo.openType
      this.chartDetailsVisible = true
    },
    exportExcel() {
      this.$refs['userViewDialog'].exportExcel()
    },
    exportViewImg() {
      this.$refs['userViewDialog'].exportViewImg()
    },
    showViewDetails(params, index) {
      this.$refs.wrapperChild[index].openChartDetailsDialog(params)
    },

    resizeView(index, item) {
      if (item.type === 'view' || item.type === 'de-show-date') {
        try {
          this.$refs.wrapperChild[index].chartResize()
        } catch (e) {
          // ignore error
        }
      }
    },
    editComponent(index, item) {
      if (item.type === 'view') {
        this.$refs.wrapperChild[index].editChart()
      }
    },
    handleDragOver(e) {
      this.dragComponentInfo.shadowStyle.x = e.pageX - 220
      this.dragComponentInfo.shadowStyle.y = e.pageY - 90 + this.scrollTop
      this.dragComponentInfo.style.left = this.dragComponentInfo.shadowStyle.x / this.scalePointWidth
      this.dragComponentInfo.style.top = this.dragComponentInfo.shadowStyle.y / this.scalePointHeight
      if (this.dragComponentInfo.auxiliaryMatrix) {
        this.onDragging(e, this.dragComponentInfo)
      }
    },
    getPositionX(x) {
      if (this.canvasStyleData.selfAdaption) {
        return x * 100 / this.curCanvasScale.scaleWidth
      } else {
        return x
      }
    },
    getPositionY(y) {
      if (this.canvasStyleData.selfAdaption) {
        return y * 100 / this.curCanvasScale.scaleHeight
      } else {
        return y
      }
    },
    canvasScroll(event) {
      this.$emit('canvasScroll', event)
    },

    // 挤占式画布设计
    startResize(e, item, index) {
      if (!this.resizable) return
      this.resizeStart.call(null, e, item, index)

      // e.preventDefault();
      // eslint-disable-next-line no-unused-vars
      const target = $(e.target)

      if (!this.infoBox) {
        this.infoBox = {}
      }
      this.infoBox.resizeItem = item
      this.infoBox.resizeItemIndex = index
      // this.onStartMove(e, item, index)

      return true
    },
    containerMouseDown(e) {
      // e.preventDefault();
      if (!this.infoBox) {
        this.infoBox = {}
      }
      this.infoBox.startX = e.pageX
      this.infoBox.startY = e.pageY
    },
    onStartMove(e, item, index) {
      if (!this.infoBox) {
        this.infoBox = {}
      }
      const infoBox = this.infoBox
      this.dragStart.call(null, e, item, index)
      infoBox.moveItem = item
      infoBox.moveItemIndex = index

      infoBox.orignX = 0 // 克隆对象原始X位置
      infoBox.orignY = 0
      infoBox.startX = 0
      infoBox.startY = 0

      infoBox.oldX = item.x // 实际对象原始X位置
      infoBox.oldY = item.y
      infoBox.oldSizeX = item.sizex
      infoBox.oldSizeY = item.sizey
    },
    onMouseUp(e) {
      const vm = this
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
    },
    onResizing(e, item) {
      // this.onDragging(e, item)
      const infoBox = this.infoBox
      const resizeItem = _.get(infoBox, 'resizeItem')
      const vm = this
      vm.$set(resizeItem, 'isPlayer', true)
      const startX = infoBox.startX
      const startY = infoBox.startY
      const moveXSize = e.pageX - startX // X方向移动的距离
      const moveYSize = e.pageY - startY // Y方向移动的距离

      const addSizex = (moveXSize) % vm.cellWidth > (vm.cellWidth / 4 * 1) ? parseInt(((moveXSize) / vm.cellWidth + 1)) : parseInt(((moveXSize) / vm.cellWidth))
      const addSizey = (moveYSize) % vm.cellHeight > (vm.cellHeight / 4 * 1) ? parseInt(((moveYSize) / vm.cellHeight + 1)) : parseInt(((moveYSize) / vm.cellHeight))
      let nowX = Math.round((item.style.width * this.scalePointWidth) / this.matrixStyle.width)
      let nowY = Math.round((item.style.height * this.scalePointHeight) / this.matrixStyle.height)
      nowX = nowX > 0 ? nowX : 1
      nowY = nowY > 0 ? nowY : 1

      const oldX = infoBox.oldX
      const oldY = infoBox.oldY
      let newX = Math.round((item.style.left * this.scalePointWidth) / this.matrixStyle.width) + 1
      let newY = Math.round((item.style.top * this.scalePointHeight) / this.matrixStyle.height) + 1
      newX = newX > 0 ? newX : 1
      newY = newY > 0 ? newY : 1
      debounce((function(newX, oldX, newY, oldY, addSizex, addSizey) {
        return function() {
          if (newX !== oldX || oldY !== newY) {
            movePlayer.call(vm, resizeItem, {
              x: newX,
              y: newY
            })

            infoBox.oldX = newX
            infoBox.oldY = newY
          }
          resizePlayer.call(vm, resizeItem, {
            sizex: nowX,
            sizey: nowY
          })
        }
      })(newX, oldX, newY, oldY, addSizex, addSizey), 10)
    },
    onDragging(e, item) {
      const infoBox = this.infoBox
      const moveItem = _.get(infoBox, 'moveItem')
      const vm = this
      scrollScreen(e)
      if (!vm.draggable) return
      vm.dragging.call(null, e, moveItem, moveItem._dragId)

      vm.$set(moveItem, 'isPlayer', true)
      const oldX = infoBox.oldX
      const oldY = infoBox.oldY
      let newX = Math.round((item.style.left * this.scalePointWidth) / this.matrixStyle.width) + 1
      let newY = Math.round((item.style.top * this.scalePointHeight) / this.matrixStyle.height) + 1
      newX = newX > 0 ? newX : 1
      newY = newY > 0 ? newY : 1
      debounce((function(newX, oldX, newY, oldY) {
        return function() {
          if (newX !== oldX || oldY !== newY) {
            movePlayer.call(vm, moveItem, {
              x: newX,
              y: newY
            })

            infoBox.oldX = newX
            infoBox.oldY = newY
          }
        }
      })(newX, oldX, newY, oldY), 10)
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
      // 不使用copy 保持原有对象
      const finalList = []
      _.forEach(this.componentData, function(item, index) {
        if (_.isEmpty(item)) return
        delete item['_dragId']
        delete item['show']
        if (item['auxiliaryMatrix']) {
          finalList.push(item)
        }
      })
      return finalList
    },
    /**
       * 获取x最大值
       *
       * @returns
       */
    getMaxCell() {
      return this.maxCell
    },
    /**
       * 获取渲染状态
       *
       * @returns
       */
    getRenderState() {
      return this.moveAnimate
    },
    addItem: addItem,
    removeItem: removeItem,
    initMatrix: init,
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
    },
    removeLastItem() {
      if (this.canvasStyleData.auxiliaryMatrix) {
        this.removeItem(this.yourList.length - 1)
      }
    },
    startMoveIn() {
      if (this.$store.state.dragComponentInfo.auxiliaryMatrix) {
        const moveInItemInfo = this.$store.state.dragComponentInfo
        this.addItemBox(moveInItemInfo)
        // e.preventDefault();
        if (!this.infoBox) {
          this.infoBox = {}
        }
        const infoBox = this.infoBox

        infoBox.moveItem = moveInItemInfo
        infoBox.moveItemIndex = this.yourList.length - 1

        infoBox.oldX = 1 // 实际对象原始X位置
        infoBox.oldY = 1
        infoBox.oldSizeX = moveInItemInfo.sizex
        infoBox.oldSizeY = moveInItemInfo.sizey
      }
    },
    linkJumpSet(item) {
      this.linkJumpSetViewId = item.propValue.viewId
      this.linkJumpSetVisible = true
    },
    closeJumpSetDialog() {
      this.linkJumpSetVisible = false
    },
    // 调整父级组件边界
    resizeParentBoundsRef() {
      const _this = this
      _this.componentData.forEach(function(data, index) {
        _this.$refs.deDragRef && _this.$refs.deDragRef[index] && _this.$refs.deDragRef[index].checkParentSize()
      })
    },
    canvasDragging(mY, offsetY) {
      this.$emit('canvasDragging', mY, offsetY)
    }
  }
}
</script>

<style lang="scss" scoped>
  .editor {
    width: 100%;
    position: relative;
    /*background: #fff;*/
    margin: auto;
    /*会影响设置组件不能出现在最高层*/
    /*overflow-x: hidden;*/
    background-size: 100% 100% !important;
    /*transform-style:preserve-3d;*/
    .lock {
      opacity: .5;
    }
  }

  .parent_transform {
    //transform transform 会使z-index 失效；为了使编辑仪表板时 按钮一直在上面 采用transform-style 的方式
    // transform-style 会导致 dialog 遮罩有问题 此处暂时用这个样式做控制
    transform-style: preserve-3d;
  }

  .edit {
    /*outline: 1px solid gainsboro;*/
    .component {
      outline: none;
      width: 100%;
      height: 100%;
      position: relative;
    }
  }

  .gap_class {
    padding: 3px;
  }

  .ref-line {
    position: absolute;
    background-color: #70c0ff;;
    z-index: 9999;
  }

  .v-line {
    width: 1px;
  }

  .h-line {
    height: 1px;
  }

  .dialog-css ::v-deep .el-dialog__title {
    font-size: 14px;
  }

  .dialog-css ::v-deep .el-dialog__header {
    padding: 40px 20px 0;
  }

  .dialog-css ::v-deep .el-dialog__body {
    padding: 10px 20px 20px;
  }

</style>
