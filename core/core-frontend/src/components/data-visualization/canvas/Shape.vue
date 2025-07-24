<template>
  <div
    class="shape"
    :class="{
      'shape-group-area': isGroupArea,
      'freeze-component': freezeFlag,
      'freeze-component-fullscreen': freezeFlag && fullscreenFlag
    }"
    ref="shapeInnerRef"
    :id="domId"
    v-loading="downLoading"
    element-loading-text="导出中..."
    element-loading-background="rgba(255, 255, 255, 1)"
    @dblclick="handleDbClick"
  >
    <div
      :title="t('visualization.sync_pc_design')"
      v-if="showCheck"
      class="refresh-from-pc"
      @click="updateFromMobile($event, 'syncPcDesign')"
    >
      <el-icon>
        <Icon name="icon_replace_outlined"><replaceOutlined class="svg-icon" /></Icon>
      </el-icon>
    </div>
    <div
      v-if="showCheck"
      class="del-from-mobile"
      @click="updateFromMobile($event, 'delFromMobile')"
    >
      <el-icon>
        <Icon name="mobile-checkbox"><mobileCheckbox class="svg-icon" /></Icon>
      </el-icon>
    </div>
    <div v-if="showHiddenIcon" class="del-from-mobile" @mousedown.stop="hiddenComponent">
      <el-tooltip :content="$t('visualization.hidden')" placement="bottom">
        <el-icon @click.stop>
          <Icon @click.stop name="dvHidden"><dvHidden class="svg-icon" /></Icon>
        </el-icon>
      </el-tooltip>
    </div>
    <div
      class="shape-outer"
      v-show="contentDisplay"
      :class="{
        active,
        'shape-lock': shapeLock,
        'shape-edit': isEditMode && !boardMoveActive,
        'linkage-setting': linkageActive,
        'drag-on-tab-collision': dragCollision,
        'shape-selected': curBatchOptComponents?.includes(element.id)
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
        @componentImageDownload="htmlToImage"
        @userViewEnlargeOpen="userViewEnlargeOpen"
        @datasetParamsInit="datasetParamsInit"
        @linkJumpSetOpen="linkJumpSetOpen"
        @linkageSetOpen="linkageSetOpen"
      ></component-edit-bar>
      <div
        class="shape-inner"
        ref="componentInnerRef"
        :id="viewDemoInnerId"
        :style="componentBackgroundStyle"
        @click="selectCurComponent"
        @mousedown="handleInnerMouseDownOnShape"
      >
        <Icon v-if="shapeLock" name="dv-lock"><dvLock class="svg-icon iconfont icon-suo" /></Icon>
        <div class="component-slot" :style="slotStyle">
          <slot></slot>
        </div>
        <!--边框背景-->
        <Board
          v-if="svgInnerEnable"
          :style="{ color: element.commonBackground.innerImageColor, pointerEvents: 'none' }"
          :name="commonBackgroundSvgInner"
        ></Board>
      </div>
      <div
        v-for="item in isActive() ? getPointList() : []"
        :key="item"
        class="shape-point"
        :style="getPointStyle(item)"
        @mousedown="handleMouseDownOnPoint(item, $event)"
      ></div>
      <div
        class="shape-shadow"
        v-show="batchOptFlag && element.component !== 'DeTabs'"
        @mousedown="batchSelected"
      ></div>
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
import mobileCheckbox from '@/assets/svg/mobile-checkbox.svg'
import replaceOutlined from '@/assets/svg/icon_replace_outlined.svg'
import dvLock from '@/assets/svg/dv-lock.svg'
import eventBus from '@/utils/eventBus'
import calculateComponentPositionAndSize, {
  calculateRadioComponentPositionAndSize
} from '@/utils/calculateComponentPositionAndSize'
import { mod360 } from '@/utils/translate'
import { deepCopy } from '@/utils/utils'
import { computed, nextTick, onMounted, ref, toRefs, reactive } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { contextmenuStoreWithOut } from '@/store/modules/data-visualization/contextmenu'
import { composeStoreWithOut } from '@/store/modules/data-visualization/compose'
import { storeToRefs } from 'pinia'
import { downloadCanvas2, imgUrlTrans } from '@/utils/imgUtils'
import Icon from '@/components/icon-custom/src/Icon.vue'
import ComponentEditBar from '@/components/visualization/ComponentEditBar.vue'
import { useEmitt } from '@/hooks/web/useEmitt'
import ComposeShow from '@/components/data-visualization/canvas/ComposeShow.vue'
import dvHidden from '@/assets/svg/dv-hidden.svg'
import { groupSizeStyleAdaptor, groupStyleRevert, tabInnerStyleRevert } from '@/utils/style'
import {
  checkJoinTab,
  isDashboard,
  isGroupCanvas,
  isMainCanvas,
  isTabCanvas,
  itemCanvasPathCheck
} from '@/utils/canvasUtils'
import Board from '@/components/de-board/Board.vue'
import { activeWatermarkCheckUser, removeActiveWatermark } from '@/components/watermark/watermark'
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()
const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const contextmenuStore = contextmenuStoreWithOut()
const composeStore = composeStoreWithOut()
const parentNode = ref(null)
const shapeInnerRef = ref(null)
const componentInnerRef = ref(null)
const componentEditBarRef = ref(null)
const downLoading = ref(false)
const viewDemoInnerId = computed(() => 'enlarge-inner-shape-' + element.value.id)

const {
  curComponent,
  dvInfo,
  editMode,
  batchOptStatus,
  curBatchOptComponents,
  linkageSettingStatus,
  curLinkageView,
  tabCollisionActiveId,
  tabMoveInActiveId,
  tabMoveOutComponentId,
  mobileInPc,
  mainScrollTop,
  hiddenListStatus,
  fullscreenFlag
} = storeToRefs(dvMainStore)
const { editorMap, areaData, isCtrlOrCmdDown } = storeToRefs(composeStore)
const emit = defineEmits([
  'userViewEnlargeOpen',
  'datasetParamsInit',
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
  ignoreTabMoveComponent: ['de-button', 'de-reset-button', 'DeTabs', 'GroupArea'],
  // 当画布在tab中是 宽度左右拓展的余量
  parentWidthTabOffset: 40,
  canvasChangeTips: 'none',
  tabMoveInYOffset: 70,
  tabMoveInXOffset: 40,
  collisionGap: 10 // 碰撞深度有效区域,
})
const hiddenComponent = event => {
  event.preventDefault()
  event.stopPropagation()
  if (element.value) {
    element.value.dashboardHidden = true
    eventBus.emit('removeMatrixItemPosition-' + canvasId.value, element.value)
    snapshotStore.recordSnapshotCache('hide')
    dvMainStore.setLastHiddenComponent(element.value.id)
  }
}
const showHiddenIcon = computed(() => hiddenListStatus.value && isMainCanvas(canvasId.value))
const contentDisplay = ref(true)
const shapeLock = computed(() => {
  return element.value['isLock'] && isEditMode.value
})

const showPosition = computed(() => {
  let position
  // 数据大屏批量操作合并为分组
  if (batchOptFlag.value) {
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

const pTabGroupFlag = itemCanvasPathCheck(element.value, 'pTabGroup')
const pJoinTab = checkJoinTab(element.value)
const domId = ref('shape-id-' + element.value.id)
const pointList = ['lt', 't', 'rt', 'r', 'rb', 'b', 'lb', 'l']
const pointCorner = ['lt', 'rt', 'rb', 'lb']
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

const freezeFlag = computed(() => {
  return (
    isMainCanvas(canvasId.value) &&
    element.value.freeze &&
    !mobileInPc.value &&
    mainScrollTop.value - defaultStyle.value.top > 0
  )
})

const showCheck = computed(() => {
  return mobileInPc.value && element.value.canvasId === 'canvas-main'
})

const updateFromMobile = (e, type) => {
  if (type === 'syncPcDesign') {
    e.preventDefault()
    e.stopPropagation()
  }
  useEmitt().emitter.emit('onMobileStatusChange', {
    type: type,
    value: element.value.id
  })
}

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

const isGroupArea = computed(() => {
  return curComponent.value?.component === 'GroupArea'
})

const active = computed(() => {
  return curComponent.value?.id === element.value.id
})

const boardMoveActive = computed(() => {
  const CHARTS = [
    'flow-map',
    'map',
    'bubble-map',
    'table-info',
    'table-normal',
    'table-pivot',
    'symbolic-map',
    'heat-map',
    't-heatmap',
    'circle-packing'
  ]
  return element.value.isPlugin || CHARTS.includes(element.value.innerType)
})

const dashboardActive = computed(() => {
  return dvInfo.value.type === 'dashboard'
})

const getPointList = () => {
  return element.value.component === 'line-shape' ? pointList2 : pointList
}

const isActive = () => {
  return active.value && !element.value['isLock'] && isEditMode.value && !freezeFlag.value
}

const userViewEnlargeOpen = opt => {
  emit('userViewEnlargeOpen', opt)
}

const datasetParamsInit = opt => {
  emit('datasetParamsInit', opt)
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
  if (
    component &&
    !component.isLock &&
    component.isShow &&
    component.canvasId === 'canvas-main' &&
    !['GroupArea'].includes(component.component)
  ) {
    areaData.value.components.push(component)
  }
}
const handleDbClick = () => {
  if (element.value.canvasId !== 'canvas-main') {
    dvMainStore.setCurComponent({ component: element.value, index: index.value })
  }
}

const handleInnerMouseDownOnShape = e => {
  if (!canvasActive.value) {
    return
  }
  batchSelected(e)
  // ctrl or command 按下时 鼠标点击为选择需要组合的组件(取消需要组合的组件在ComposeShow组件中)
  if (isCtrlOrCmdDown.value && !areaData.value.components.includes(element)) {
    areaDataPush(element.value)
    if (curComponent.value && curComponent.value.id !== element.value.id) {
      areaDataPush(curComponent.value)
    }
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
  // 锁定 非编辑状态 冻结状态 不进行移动
  if (element.value['isLock'] || !isEditMode.value || freezeFlag.value) return

  cursors.value = getCursor() // 根据旋转角度获取光标位置

  const pos = { ...defaultStyle.value }
  const startY = e.clientY
  const startX = e.clientX

  const offsetY = e.offsetY
  const offsetX = e.offsetX

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
  let outerTabDom = isTabCanvas(canvasId.value)
    ? document.getElementById('shape-id-' + canvasId.value.split('--')[0])
    : null
  const curDom = document.getElementById(domId.value)
  const move = moveEvent => {
    hasMove = true
    const curX = moveEvent.clientX
    const curY = moveEvent.clientY
    const top = curY - startY + startTop
    const left = curX - startX + startLeft
    pos['top'] = top
    pos['left'] = left
    // 非主画布非分组画布的情况 需要检测是否从Tab中移除组件(向左移除30px 或者向右移除30px 向左移除30px)
    // 因为仪表板中组件向下移动可能只是为了挤占空间 不一定是为了移出 这里无法判断明确意图 暂时支不支持向下移出
    // 大屏和仪表板暂时做位置算法区分 仪表板暂时使用curX 因为缩放的影响 大屏使用 tab位置 + 组件位置（相对内部画布）+初始触发点
    // 如果组件在tab中且tab在Group中 不允许移入移出 pTabGroupFlag = true
    if (
      !pTabGroupFlag &&
      pJoinTab &&
      !isMainCanvas(canvasId.value) &&
      !isGroupCanvas(canvasId.value) &&
      !isGroupArea.value &&
      (top < -30 || left < -30 || left + componentWidth - canvasWidth > 30)
    ) {
      contentDisplay.value = false
      dvMainStore.setMousePointShadowMap({
        mouseX:
          !isDashboard() && outerTabDom
            ? outerTabDom.offsetLeft + curDom.offsetLeft + offsetX
            : curX,
        mouseY:
          !isDashboard() && outerTabDom
            ? outerTabDom.offsetTop + curDom.offsetTop + offsetY + 100
            : curY + mainScrollTop.value,
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
    tabMoveInCheck()
    // 仪表板模式 会造成移动现象 当检测组件正在碰撞有效区内或者移入有效区内 则周边组件不进行移动
    if (
      dashboardActive.value &&
      (isFirst || (!tabMoveInActiveId.value && !tabCollisionActiveId.value))
    ) {
      element.value['dragging'] = true
      emit('onDragging', e)
    }

    //如果当前组件是Group分组 则要进行内部组件深度计算
    if (['DeTabs', 'Group'].includes(element.value.component)) {
      groupSizeStyleAdaptor(element.value)
    }
    //如果当前画布是Group内部画布 则对应组件定位在resize时要还原到groupStyle中
    if (isGroupCanvas(canvasId.value) || isTabCanvas(canvasId.value)) {
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
    dvMainStore.setShapeStyle(pos, areaData.value.components, 'move')
    // 等更新完当前组件的样式并绘制到屏幕后再判断是否需要吸附
    // GroupArea是分组视括组件 不需要进行吸附
    // 如果不使用 nextTick，吸附后将无法移动
    if (!isGroupArea.value) {
      nextTick(() => {
        // 触发元素移动事件，用于显示标线、吸附功能
        // 后面两个参数代表鼠标移动方向
        // curY - startY > 0 true 表示向下移动 false 表示向上移动
        // curX - startX > 0 true 表示向右移动 false 表示向左移动
        eventBus.emit('move', { isDownward: curY - startY > 0, isRightward: curX - startX > 0 })
      })
    }
  }

  const up = () => {
    dashboardActive.value && emit('onMouseUp')
    element.value['dragging'] = false
    hasMove &&
      snapshotStore.recordSnapshotCacheWithPositionChange('shape-handleMouseDownOnShape-up')
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
  if (batchOptFlag.value) {
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
  const originRadio = curComponent.value.aspectRatio
  const baseGroupComponentsRadio = {}
  // 计算初始状态 分组内组件宽高占比
  if (areaData.value.components.length > 0) {
    areaData.value.components.forEach(groupComponent => {
      baseGroupComponentsRadio[groupComponent.id] = {
        topRadio: (groupComponent.style.top - style.top) / style.height,
        leftRadio: (groupComponent.style.left - style.left) / style.width,
        widthRadio: groupComponent.style.width / style.width,
        heightRadio: groupComponent.style.height / style.height
      }
    })
  }

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
    // 保持宽搞比例调整
    if (curComponent.value.maintainRadio) {
      // 高度偏移量
      const heightOffset = style.height - defaultStyle.value.height
      // 宽度偏移量
      const widthOffset = style.width - defaultStyle.value.width
      // 保持宽高比例是相对宽度偏移量
      const adaptorWidthOffset = heightOffset * originRadio
      if (pointCorner.includes(point)) {
        style.height = defaultStyle.value.width / originRadio
      } else if (Math.abs(widthOffset) > Math.abs(adaptorWidthOffset)) {
        // 调整高度
        style.height = defaultStyle.value.width / originRadio
      } else {
        // 调整宽度
        style.width = defaultStyle.value.height * originRadio
      }
    }
    calculateRadioComponentPositionAndSize(point, style, symmetricPoint)

    dvMainStore.setShapeStyle(style, areaData.value.components, 'resize', baseGroupComponentsRadio)
    // 矩阵逻辑 如果当前是仪表板（矩阵模式）则要进行矩阵重排
    dashboardActive.value && emit('onResizing', moveEvent)
    element.value['resizing'] = true
    //如果当前组件是Group分组或者Tab 则要进行内部组件深度计算
    if (
      ['Group'].includes(element.value.component) ||
      (['DeTabs'].includes(element.value.component) && !element.value.resizeInnerKeep)
    ) {
      groupSizeStyleAdaptor(element.value)
    }

    //如果当前画布是Group内部画布 则对应组件定位在resize时要还原到groupStyle中
    if (isGroupCanvas(canvasId.value) || isTabCanvas(canvasId.value)) {
      groupStyleRevert(element.value, {
        width: parentNode.value.offsetWidth,
        height: parentNode.value.offsetHeight
      })
    }
  }

  const up = () => {
    // 如果内部组件保持尺寸时，这里在鼠标抬起时，重新计算一下内部组件占比
    if (['DeTabs'].includes(element.value.component) && element.value.resizeInnerKeep) {
      tabInnerStyleRevert(element.value)
    }

    dashboardActive.value && emit('onMouseUp')
    element.value['resizing'] = false
    document.removeEventListener('mousemove', move)
    document.removeEventListener('mouseup', up)
    needSave &&
      snapshotStore.recordSnapshotCacheWithPositionChange('shape-handleMouseDownOnPoint-up')
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
  if (element.value.commonBackground && element.value.component !== 'GroupArea') {
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
    } = element.value.commonBackground
    const innerPaddingTarget = ['Group'].includes(element.value.component) ? 0 : innerPadding
    let style = {
      padding: innerPaddingTarget * scale.value + 'px',
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
    if (element.value.component !== 'UserView') {
      style['overflow'] = 'hidden'
    }
    if (backdropFilterEnable) {
      style['backdrop-filter'] = 'blur(' + backdropFilter + 'px)'
    }
    return style
  }
  return {}
})

const editBarShowFlag = computed(() => {
  return (
    ((active.value || batchOptFlag.value) &&
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
  // 如当前是分组且分组中含有Tab 不允许移入 pJoinTab = false
  if (
    pJoinTab &&
    isTabMoveCheck.value &&
    !state.ignoreTabMoveComponent.includes(element.value.component)
  ) {
    const nodes = Array.from(parentNode.value.childNodes) // 获取当前父节点下所有子节点
    for (const item of nodes) {
      if (
        item.className !== undefined &&
        typeof item.className === 'string' &&
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
const slotStyle = computed(() => {
  // 3d效果支持
  if (element.value['multiDimensional'] && element.value['multiDimensional']?.enable) {
    const width = defaultStyle.value.width // 原始元素宽度
    const height = defaultStyle.value.height // 原始元素高度
    const rotateX = element.value['multiDimensional'].x // 旋转X角度
    const rotateY = element.value['multiDimensional'].y // 旋转Y角度

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
      transform: `rotateX(${element.value['multiDimensional'].x}deg) rotateY(${element.value['multiDimensional'].y}deg) rotateZ(${element.value['multiDimensional'].z}deg)`
    }
  } else {
    return {}
  }
})

const batchOptFlag = computed(() => {
  return batchOptStatus.value && dashboardActive.value
})

const dragCollision = computed(() => {
  return active.value && Boolean(tabCollisionActiveId.value)
})

const htmlToImage = () => {
  downLoading.value = true
  useEmitt().emitter.emit('l7-prepare-picture', element.value.id)
  setTimeout(() => {
    activeWatermarkCheckUser(viewDemoInnerId.value, 'canvas-main', scale.value)
    const dom = document.getElementById(viewDemoInnerId.value)
    downloadCanvas2('img', dom, '图表', () => {
      // do callback
      removeActiveWatermark(viewDemoInnerId.value)
      downLoading.value = false
      useEmitt().emitter.emit('l7-unprepare-picture', element.value.id)
    })
  }, 200)
}

const handleGroupComponent = () => {
  if (element.value.canvasId.includes('Group')) {
    composeStore.updateGroupBorder(element.value.canvasId)
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
  const methodName = 'componentImageDownload-' + element.value.id
  if (!useEmitt().emitter.all.get(methodName)?.length) {
    useEmitt().emitter.on(methodName, () => {
      htmlToImage()
    })
  }
})
</script>

<style lang="less" scoped>
.shape {
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
  .del-from-mobile {
    position: absolute;
    right: 12px;
    top: 12px;
    z-index: 2;
    font-size: 16px;
    cursor: pointer;
    color: var(--ed-color-primary);
  }
}

.shape-group-area {
  z-index: 15;
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

.shape-selected {
  outline: 1px solid #3370ff;
}

.shape-edit {
  &:hover {
    cursor: move;
    outline: 1px solid var(--ed-color-primary-99, rgba(51, 112, 255, 0.6));
  }
}

.shape-lock {
  &:hover {
    cursor: not-allowed !important;
  }
}

.active {
  outline: 1px solid var(--ed-color-primary) !important;
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
  top: 70px;
  width: 12px;
  height: calc(100% - 110px);
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
  transform-style: preserve-3d;
}

.freeze-component {
  position: fixed;
  z-index: 1;
  top: 66px !important;
}

.freeze-component-fullscreen {
  top: 5px !important;
}
</style>
