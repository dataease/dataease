import eventBus from '@/utils/eventBus'
import { dvMainStoreWithOut } from '@/store/modules/dataVisualization/dvMain'
import { snapshotStoreWithOut } from '@/store/modules/dataVisualization/snapshot'
import { copyStoreWithOut } from '@/store/modules/dataVisualization/copy'
import { composeStoreWithOut } from '@/store/modules/dataVisualization/compose'
import { lockStoreWithOut } from '@/store/modules/dataVisualization/lock'
import { storeToRefs } from 'pinia'

const dvMainStore = dvMainStoreWithOut()
const composeStore = composeStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const copyStore = copyStoreWithOut()
const lockStore = lockStoreWithOut()
const { curComponent, isInEdiotr } = storeToRefs(dvMainStore)
const { areaData } = storeToRefs(composeStore)

const ctrlKey = 17,
  commandKey = 91, // mac command
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
  eKey = 69 // 清空画布

export const keycodes = [66, 67, 68, 69, 71, 76, 80, 83, 85, 86, 88, 89, 90]

// 与组件状态无关的操作
const basemap = {
  [vKey]: paste,
  [yKey]: redo,
  [zKey]: undo,
  [sKey]: save,
  [pKey]: preview,
  [eKey]: clearCanvas
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

let isCtrlOrCommandDown = false
// 全局监听按键操作并执行相应命令
export function listenGlobalKeyDown() {
  window.onkeydown = e => {
    if (!isInEdiotr) return

    const { keyCode } = e
    if (keyCode === ctrlKey || keyCode === commandKey) {
      isCtrlOrCommandDown = true
    } else if (keyCode == deleteKey && curComponent) {
      dvMainStore.deleteComponent(undefined)
      snapshotStore.recordSnapshot()
    } else if (isCtrlOrCommandDown) {
      if (unlockMap[keyCode] && (!curComponent || !curComponent.value.isLock)) {
        e.preventDefault()
        unlockMap[keyCode]()
      } else if (lockMap[keyCode] && curComponent && curComponent.value.isLock) {
        e.preventDefault()
        lockMap[keyCode]()
      }
    }
  }

  window.onkeyup = e => {
    if (e.keyCode === ctrlKey || e.keyCode === commandKey) {
      isCtrlOrCommandDown = false
    }
  }

  window.onmousedown = () => {
    dvMainStore.setInEditorStatus(false)
  }
}

function copy() {
  copyStore.copy()
}

function paste() {
  copyStore.paste(false)
  snapshotStore.recordSnapshot()
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
    snapshotStore.recordSnapshot()
  }
}

function decompose() {
  const curComponentLink = dvMainStore.curComponent
  if (
    curComponentLink &&
    !curComponentLink.value.isLock &&
    curComponentLink.value.component == 'Group'
  ) {
    composeStore.decompose()
    snapshotStore.recordSnapshot()
  }
}

function save() {
  eventBus.emit('save')
}

function preview() {
  eventBus.emit('preview')
}

function deleteComponent() {
  if (curComponent) {
    dvMainStore.deleteComponent(undefined)
    snapshotStore.recordSnapshot()
  }
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
