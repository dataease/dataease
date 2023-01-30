import store from '@/store'
import eventBus from '@/components/canvas/utils/eventBus'
import { $warning } from '@/utils/message'
import i18n from '@/lang'

const ctrlKey = 17
const commandKey = 91 // mac command
const vKey = 86 // 粘贴
const cKey = 67 // 复制
const xKey = 88 // 剪切

const yKey = 89 // 重做
const zKey = 90 // 撤销

const gKey = 71 // 组合
const bKey = 66 // 拆分

const lKey = 76 // 锁定

const dKey = 68 // 复制并粘贴

const deleteKey = 46 // 删除

const sKey = 83 // 保存

const enlargeKey = 190 // command + .

export const keycodes = [66, 67, 68, 69, 71, 76, 80, 83, 85, 86, 88, 89, 90]

const ignoreComponent = ['de-button', 'de-reset-button']

// 与组件状态无关的操作
const basemap = {
  [vKey]: paste,
  [yKey]: redo,
  [zKey]: undo
}

// 组件未锁定状态下可以执行的操作
const unlockMap = {
  ...basemap,
  [cKey]: copy,
  [xKey]: cut,
  [gKey]: compose,
  [bKey]: decompose,
  [dKey]: copyAndPast,
  [deleteKey]: deleteComponent,
  [lKey]: lock,
  [sKey]: save,
  [enlargeKey]: viewEnlarge
}

let isCtrlOrCommandDown = false

// Monitor key operations globally and execute corresponding commands
export function listenGlobalKeyDown() {
  window.onkeydown = (e) => {
    if (!store.state.isInEditor) return
    const { keyCode } = e
    if (keyCode === ctrlKey || keyCode === commandKey) {
      isCtrlOrCommandDown = true
    } else if (isCtrlOrCommandDown) {
      if (keyCode === zKey || keyCode === yKey || keyCode === dKey || keyCode === sKey || keyCode === enlargeKey) {
        e.preventDefault()
        e.stopPropagation()
        unlockMap[keyCode]()
      }
    }
  }

  window.onkeyup = (e) => {
    if (e.keyCode === ctrlKey || e.keyCode === commandKey) {
      isCtrlOrCommandDown = false
    }
  }
}

export function removeKeyListen() {
  // window.onkeydown = null
  // window.onkeyup = null
}

export function listenGlobalKeyDownPreview() {
  window.onkeydown = (e) => {
    const { keyCode } = e
    if (keyCode === ctrlKey || keyCode === commandKey) {
      isCtrlOrCommandDown = true
    } else if (isCtrlOrCommandDown) {
      if (keyCode === enlargeKey) {
        e.preventDefault()
        e.stopPropagation()
        unlockMap[keyCode]()
      }
    }
  }

  window.onkeyup = (e) => {
    if (e.keyCode === ctrlKey || e.keyCode === commandKey) {
      isCtrlOrCommandDown = false
    }
  }
}

function copy() {
  store.commit('copyToClipboard')
}

function paste() {

}

function cut() {
  store.commit('cut')
}

function redo() {
  store.commit('redo')
}

function undo() {
  store.commit('undo')
}

function compose() {
  if (store.state.areaData.components.length) {
    store.commit('compose')
    store.commit('recordSnapshot')
  }
}

function decompose() {
  const curComponent = store.state.curComponent
  if (curComponent && !curComponent.isLock && curComponent.component === 'Group') {
    store.commit('decompose')
    store.commit('recordSnapshot')
  }
}

function copyAndPast() {
  if (store.state.curComponent) {
    if (ignoreComponent.includes(store.state.curComponent.component)) {
      $warning(i18n.t('panel.forbidden_copy'))
    } else {
      store.commit('copy')
      store.commit('paste', false)
      store.commit('recordSnapshot', 'copyAndPast')
    }
  }
}

function deleteComponent() {
  if (store.state.curComponent) {
    store.commit('deleteComponent')
    store.commit('recordSnapshot')
  }
}

function lock() {
  store.commit('lock')
}

function save() {
  eventBus.$emit('checkAndSave')
}

function viewEnlarge() {
  eventBus.$emit('viewEnlarge')
}
