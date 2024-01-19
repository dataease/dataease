import eventBus from '@/utils/eventBus'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { copyStoreWithOut } from '@/store/modules/data-visualization/copy'
import { composeStoreWithOut } from '@/store/modules/data-visualization/compose'
import { lockStoreWithOut } from '@/store/modules/data-visualization/lock'
import { storeToRefs } from 'pinia'

const dvMainStore = dvMainStoreWithOut()
const composeStore = composeStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const copyStore = copyStoreWithOut()
const lockStore = lockStoreWithOut()
const { curComponent, isInEditor } = storeToRefs(dvMainStore)
const { areaData } = storeToRefs(composeStore)

const ctrlKey = 17,
  shiftKey = 16, // shift
  commandKey = 91, // mac command
  leftKey = 37, // 向左
  upKey = 38, // 向上
  rightKey = 39, // 向右
  downKey = 40, // 向下
  vKey = 86, // 粘贴
  cKey = 67, // 复制
  xKey = 88, // 剪切
  yKey = 89, // 重做
  zKey = 90, // 撤销
  gKey = 71, // 组合
  bKey = 66, // 拆分
  lKey = 76, // 锁定
  uKey = 85, // 解锁
  sKey = 83, // 保存
  pKey = 80, // 预览
  dKey = 68, // 删除
  deleteKey = 46, // 删除
  macDeleteKey = 8, // 删除
  eKey = 69 // 清空画布

export const keycodes = [8, 37, 38, 39, 40, 66, 67, 68, 69, 71, 76, 80, 83, 85, 86, 88, 89, 90]

// 与组件状态无关的操作
const basemap = {
  [vKey]: paste,
  [yKey]: redo,
  [zKey]: undo,
  [sKey]: save,
  [pKey]: preview,
  [eKey]: clearCanvas
}

// 当处于大屏状态时 按上下左右键 可以移动位置
const positionMoveKey = {
  [leftKey]: move,
  [upKey]: move,
  [rightKey]: move,
  [downKey]: move
}

// 组件锁定状态下可以执行的操作
const lockMap = {
  ...basemap,
  [uKey]: unlock
}

// 组件未锁定状态下可以执行的操作
const unlockMap = {
  ...basemap,
  [cKey]: copy,
  [xKey]: cut,
  [gKey]: compose,
  [bKey]: decompose,
  [dKey]: deleteComponent,
  [deleteKey]: deleteComponent,
  [lKey]: lock
}

// 检查当前页面是否有弹框
const checkDialog = () => {
  let haveDialog = false
  document.querySelectorAll('.ed-overlay').forEach(element => {
    if (window.getComputedStyle(element).getPropertyValue('display') != 'none') {
      haveDialog = true
    }
  })
  document.querySelectorAll('.ed-popover').forEach(element => {
    if (window.getComputedStyle(element).getPropertyValue('display') != 'none') {
      haveDialog = true
    }
  })
  // 富文本单框
  if (document.querySelector('.tox-dialog-wrap')) {
    haveDialog = true
  }

  return haveDialog
}

let isCtrlOrCommandDown = false
let isShiftDown = false
// 全局监听按键操作并执行相应命令
export function listenGlobalKeyDown() {
  window.onkeydown = e => {
    if (!isInEditor || checkDialog()) return
    const { keyCode } = e
    if (positionMoveKey[keyCode] && curComponent.value) {
      positionMoveKey[keyCode](keyCode)
      e.preventDefault()
    } else if (keyCode === shiftKey) {
      isShiftDown = true
      composeStore.setIsShiftDownStatus(true)
      releaseKeyCheck('shift')
    } else if (keyCode === ctrlKey || keyCode === commandKey) {
      isCtrlOrCommandDown = true
      composeStore.setIsCtrlOrCmdDownStatus(true)
      releaseKeyCheck('ctrl')
    } else if ((keyCode == deleteKey || keyCode == macDeleteKey) && curComponent.value) {
      deleteComponent()
    } else if (isCtrlOrCommandDown) {
      if (unlockMap[keyCode] && (!curComponent.value || !curComponent.value.isLock)) {
        e.preventDefault()
        unlockMap[keyCode]()
      } else if (lockMap[keyCode] && curComponent.value && curComponent.value.isLock) {
        e.preventDefault()
        lockMap[keyCode]()
      }
    }
  }

  window.onkeyup = e => {
    if (e.keyCode === ctrlKey || e.keyCode === commandKey) {
      isCtrlOrCommandDown = false
      composeStore.setIsCtrlOrCmdDownStatus(false)
    } else if (e.keyCode === shiftKey) {
      isShiftDown = true
      composeStore.setIsShiftDownStatus(false)
    }
  }

  window.onmousedown = () => {
    dvMainStore.setInEditorStatus(false)
  }
}

export function releaseAttachKey() {
  isCtrlOrCommandDown = false
  composeStore.setIsCtrlOrCmdDownStatus(false)
  isShiftDown = false
  composeStore.setIsShiftDownStatus(false)
}

//当前不支持同时ctrl + shift操作
function releaseKeyCheck(keyType) {
  if (keyType === 'shift' && isCtrlOrCommandDown) {
    isCtrlOrCommandDown = false
    composeStore.setIsCtrlOrCmdDownStatus(false)
  } else if (keyType === 'ctrl' && isShiftDown) {
    isShiftDown = false
    composeStore.setIsShiftDownStatus(false)
  }
}

function copy() {
  copyStore.copy()
}

function paste() {
  copyStore.paste(false)
  snapshotStore.recordSnapshotCache('key-paste')
}

function move(keyCode) {
  if (curComponent.value) {
    if (keyCode === leftKey) {
      curComponent.value.style.left = --curComponent.value.style.left
    } else if (keyCode === rightKey) {
      curComponent.value.style.left = ++curComponent.value.style.left
    } else if (keyCode === upKey) {
      curComponent.value.style.top = --curComponent.value.style.top
    } else if (keyCode === downKey) {
      curComponent.value.style.top = ++curComponent.value.style.top
    }
    snapshotStore.recordSnapshotCache('key-move')
  }
}

function cut() {
  copyStore.cut()
}

function redo() {
  snapshotStore.redo()
}

function undo() {
  snapshotStore.undo()
}

function compose() {
  if (areaData.value.components.length) {
    composeStore.compose()
    snapshotStore.recordSnapshotCache('key-compose')
  }
}

function decompose() {
  const curComponentLink = curComponent.value
  if (curComponentLink && !curComponentLink.isLock && curComponentLink.component == 'Group') {
    composeStore.decompose()
    snapshotStore.recordSnapshotCache('key-decompose')
  }
}

function save() {
  eventBus.emit('save')
}

function preview() {
  eventBus.emit('preview')
}

function deleteComponent() {
  if (curComponent.value) {
    dvMainStore.deleteComponentById(curComponent.value.id)
  } else if (areaData.value.components.length) {
    areaData.value.components.forEach(component => {
      dvMainStore.deleteComponentById(component.id)
    })
    eventBus.emit('hideArea-canvas-main')
  }
  snapshotStore.recordSnapshotCache('listenGlobalKeyDown')
}

function clearCanvas() {
  eventBus.emit('clearCanvas')
}

function lock() {
  lockStore.lock()
}

function unlock() {
  lockStore.unlock()
}
