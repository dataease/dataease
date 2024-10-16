<script lang="ts" setup>
import { toRefs, onBeforeMount, type PropType, type Ref, inject, computed, nextTick } from 'vue'
interface SelectConfig {
  id: string
  defaultNumValueEnd: number
  numValueEnd: number
  numValueStart: number
  defaultNumValueStart: number
  placeholder: string
}
const placeholder: Ref = inject('placeholder')

const placeholderText = computed(() => {
  if (placeholder.value.placeholderShow) {
    return props.config.placeholder
  }
  return ' '
})

const props = defineProps({
  config: {
    type: Object as PropType<SelectConfig>,
    default: () => {
      return {
        id: '',
        defaultNumValueEnd: '',
        defaultNumValueStart: '',
        numValueEnd: '',
        numValueStart: ''
      }
    }
  },
  isConfig: {
    type: Boolean,
    default: false
  }
})

const { config } = toRefs(props)
const setParams = () => {
  const { defaultNumValueEnd, defaultNumValueStart } = config.value
  config.value.numValueEnd = defaultNumValueEnd
  config.value.numValueStart = defaultNumValueStart
}
onBeforeMount(() => {
  setParams()
})
const queryConditionWidth = inject('com-width', Function, true)
const customStyle = inject<{ background: string }>('$custom-style-filter')
const isConfirmSearch = inject('is-confirm-search', Function, true)
const selectStyle = computed(() => {
  return { width: queryConditionWidth() + 'px' }
})
const handleValueChange = () => {
  if (!props.isConfig) {
    nextTick(() => {
      isConfirmSearch(config.value.id)
    })
    return
  }
}
</script>

<template>
  <div class="num-search-select" :style="{ background: customStyle.background }">
    <el-input-number
      :placeholder="placeholderText"
      @change="handleValueChange"
      :style="selectStyle"
      controls-position="right"
      v-model="config.numValueStart"
    />
    <div class="num-value_line"></div>
    <el-input-number
      :placeholder="placeholderText"
      :style="selectStyle"
      @change="handleValueChange"
      controls-position="right"
      v-model="config.numValueEnd"
    />
  </div>
</template>

<style lang="less" scoped>
.num-search-select {
  display: flex;
  align-items: center;
  .num-value_line {
    background: #1f2329;
    width: 12px;
    height: 1px;
    margin: 0 8px;
  }
}
</style>
