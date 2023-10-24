<template>
  <code-mirror
    :quotaMap="props.linkJumpInfoArray.map(ele => ele.sourceFieldName)"
    ref="myCm"
    dom-id="jumpSetField"
    style="height: 100%"
  ></code-mirror>
</template>

<script setup lang="ts">
import { onBeforeUnmount, reactive, ref, toRefs } from 'vue'
import CodeMirror from '@/views/visualized/data/dataset/form/CodeMirror.vue'
const myCm = ref(null)
const mirror = ref(null)
const props = defineProps({
  linkJumpInfoArray: Array,
  linkJumpInfo: Object
})

const { linkJumpInfo } = toRefs(props)
const state = reactive({
  name2Auto: [],
  content: ''
})
const timer = ref(null)

const insertFieldToCodeMirror = (value: string) => {
  mirror.value.dispatch({
    changes: { from: mirror.value.viewState.state.selection.ranges[0].from, insert: value },
    selection: { anchor: mirror.value.viewState.state.selection.ranges[0].from }
  })
}

const setNameIdTrans = (from, to, originName, name2Auto?: string[]) => {
  if (!originName) {
    return originName
  }
  let name2Id = originName
  const nameIdMap = props.linkJumpInfoArray.reduce((pre, next) => {
    pre[next[from]] = next[to]
    return pre
  }, {})
  const on = originName.match(/\[(.+?)\]/g)
  if (on) {
    on.forEach(itm => {
      const ele = itm.slice(1, -1)
      if (name2Auto) {
        name2Auto.push(nameIdMap[ele])
      }
      name2Id = name2Id.replace(`[${ele}]`, `[${nameIdMap[ele]}]`)
    })
  }
  return name2Id
}

const editorInit = content => {
  state.name2Auto = []
  if (!mirror.value) {
    mirror.value = myCm.value.codeComInit()
  }
  state.content = setNameIdTrans('sourceFieldId', 'sourceFieldName', content, state.name2Auto)
  mirror.value.dispatch({
    changes: {
      from: 0,
      to: mirror.value.viewState.state.doc.length,
      insert: state.content
    }
  })
  clearTimeout(timer.value)
  timer.value = setInterval(() => {
    const content = mirror.value ? mirror.value.state.doc.toString() : ''
    const contentTrans = setNameIdTrans(
      'sourceFieldName',
      'sourceFieldId',
      content,
      state.name2Auto
    )
    linkJumpInfo.value.content = contentTrans
  }, 1500)
}
defineExpose({
  editorInit,
  insertFieldToCodeMirror
})

onBeforeUnmount(() => {
  clearTimeout(timer.value)
  mirror.value && mirror.value.destroy?.()
})
</script>

<style lang="less" scoped></style>
