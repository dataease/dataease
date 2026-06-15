<script lang="ts" setup>
import { ref, PropType, watch, onMounted } from 'vue'
import { propTypes } from '@/utils/propTypes'
import { VAceEditor } from 'vue3-ace-editor'
import { formatJson, formatXml } from './format-utils'
import './ace-config'

const props = defineProps({
  height: [String, Number],
  data: propTypes.string.def(''),
  theme: propTypes.string.def('chrome'),
  init: Function,
  enableFormat: propTypes.bool.def(true),
  readOnly: propTypes.bool.def(true),
  modes: {
    type: Array as PropType<string[]>,
    default: () => ['text', 'json', 'xml']
  },
  mode: propTypes.string.def('text')
})

const formatData = ref('')
watch(formatData, () => {
  emits('update:modelValue', formatData.value)
})

watch([props.theme], () => {
  format()
})
onMounted(() => {
  format()
})
const editorInit = editor => {
  if (props.readOnly) {
    editor.setReadOnly(true)
  }
  if (props.init) {
    props.init(editor)
  }
}
const format = () => {
  if (props.enableFormat) {
    if (props.data) {
      switch (props.mode) {
        case 'json':
          formatData.value = formatJson(props.data)
          break
        case 'xml':
          formatData.value = formatXml(props.data)
          break
        default:
          formatData.value = props.data
      }
    }
  } else {
    formatData.value = props.data
  }
}
const emits = defineEmits(['update:modelValue'])
</script>

<template>
  <v-ace-editor
    v-model:value="formatData"
    :lang="mode"
    :theme="theme"
    :style="{ height }"
    @init="editorInit"
  />
</template>
