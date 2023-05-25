<script lang="ts" setup>
import { keycodes } from '@/utils/shortcutKey.js'
import eventBus from '@/utils/eventBus'
import { nextTick, onBeforeUnmount, ref } from 'vue'
import { toRefs } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'

const canEdit = ref(false)
const ctrlKey = ref(17)
const isCtrlDown = ref(false)
const cancelRequest = ref(null)

const emit = defineEmits(['input'])
const text = ref(null)

const props = defineProps({
  propValue: {
    type: String,
    required: true,
    default: ''
  },
  element: {
    type: Object,
    default() {
      return {
        id: null,
        propValue: ''
      }
    }
  }
})

const { propValue, element } = toRefs(props)
const dvMainStore = dvMainStoreWithOut()
const { editMode, curComponent } = storeToRefs(dvMainStore)

const onComponentClick = () => {
  if (curComponent.value.id !== element.value.id) {
    canEdit.value = false
  }
}

const handleInput = e => {
  emit('input', element.value, e.target.innerHTML)
}

const handleKeydown = e => {
  // 阻止冒泡，防止触发复制、粘贴组件操作
  canEdit.value && e.stopPropagation()
  if (e.keyCode == this.ctrlKey) {
    isCtrlDown.value = true
  } else if (isCtrlDown.value && canEdit.value && keycodes.includes(e.keyCode)) {
    e.stopPropagation()
  } else if (e.keyCode == 46) {
    // deleteKey
    e.stopPropagation()
  }
}

const handleKeyup = e => {
  // 阻止冒泡，防止触发复制、粘贴组件操作
  canEdit.value && e.stopPropagation()
  if (e.keyCode == ctrlKey.value) {
    isCtrlDown.value = false
  }
}

const handleMousedown = e => {
  if (canEdit.value) {
    e.stopPropagation()
  }
}

const clearStyle = e => {
  e.preventDefault()
  const clp = e.clipboardData
  const text = clp.getData('text/plain') || ''
  if (text !== '') {
    document.execCommand('insertText', false, text)
  }

  emit('input', this.element, e.target.innerHTML)
}

const handleBlur = e => {
  element.value.propValue = e.target.innerHTML || '&nbsp;'
  const html = e.target.innerHTML
  if (html !== '') {
    element.value.propValue = e.target.innerHTML
  } else {
    element.value.propValue = ''
    nextTick(function () {
      element.value.propValue = '&nbsp;'
    })
  }
  canEdit.value = false
}

const setEdit = () => {
  if (element.value['isLock']) return
  canEdit.value = true
  // 全选
  selectText(text.value)
}
const selectText = element => {
  const selection = window.getSelection()
  const range = document.createRange()
  range.selectNodeContents(element)
  selection.removeAllRanges()
  selection.addRange(range)
}
onBeforeUnmount(() => {
  eventBus.off('componentClick', onComponentClick)
})
</script>

<template>
  <div v-if="editMode == 'edit'" class="v-text" @keydown="handleKeydown" @keyup="handleKeyup">
    <div
      ref="text"
      :contenteditable="canEdit"
      :class="{ 'can-edit': canEdit }"
      tabindex="0"
      :style="{ verticalAlign: element['style'].verticalAlign }"
      @dblclick="setEdit"
      @paste="clearStyle"
      @mousedown="handleMousedown"
      @blur="handleBlur"
      @input="handleInput"
      v-html="element['propValue']"
    ></div>
  </div>
  <div v-else class="v-text preview">
    <div
      :style="{ verticalAlign: element['style'].verticalAlign }"
      v-html="element['propValue']"
    ></div>
  </div>
</template>

<style lang="less" scoped>
.v-text {
  width: 100%;
  height: 100%;
  display: table;

  div {
    display: table-cell;
    width: 100%;
    height: 100%;
    outline: none;
    word-break: break-all;
    padding: 4px;
  }

  .can-edit {
    cursor: text;
    height: 100%;
  }
}

.preview {
  user-select: none;
}
</style>
