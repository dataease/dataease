<script lang="ts" setup>
import { toRefs, provide, PropType } from 'vue'

interface SelectConfig {
  selectValue: any
  multiple: boolean
  options?: Array<{
    label: string
    value: string
  }>
}

const props = defineProps({
  config: {
    type: Object as PropType<SelectConfig>,
    default: () => {
      return {
        selectValue: '',
        multiple: false
      }
    }
  },
  customStyle: {
    type: Object as PropType<{
      border: string
      background: string
      text: string
    }>,
    default: () => ({
      border: '',
      background: '',
      text: ''
    })
  }
})
const shortcuts = [
  {
    text: 'Last week',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    }
  },
  {
    text: 'Last month',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    }
  },
  {
    text: 'Last 3 months',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
      return [start, end]
    }
  }
]

const { config, customStyle } = toRefs(props)

provide('$custom-style-filter', customStyle)
</script>

<template>
  <el-date-picker
    v-model="config.selectValue"
    type="datetimerange"
    v-if="config.multiple"
    :shortcuts="shortcuts"
    range-separator="To"
    start-placeholder="Start date"
    end-placeholder="End date"
  />
  <el-date-picker
    v-else
    v-model="config.selectValue"
    type="datetime"
    placeholder="Pick a Date"
    format="YYYY/MM/DD HH:mm:ss"
  />
</template>
