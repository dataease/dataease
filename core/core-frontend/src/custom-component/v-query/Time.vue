<script lang="ts" setup>
import { toRefs, provide, PropType, ref, onBeforeMount, watch, nextTick } from 'vue'
import { Calendar } from '@element-plus/icons-vue'

interface SelectConfig {
  selectValue: any
  defaultValue: any
  defaultValueCheck: boolean
  displayType: string
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
        defaultValue: '',
        defaultValueCheck: false,
        displayType: '1'
      }
    }
  },
  isConfig: {
    type: Boolean,
    default: false
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
const selectValue = ref()
const multiple = ref(false)

const { config, customStyle } = toRefs(props)

provide('$custom-style-filter', customStyle)

watch(
  () => config.value.selectValue,
  val => {
    if (props.isConfig) return
    selectValue.value = Array.isArray(val) ? [...val] : val
    multiple.value = config.value.displayType === '7'
  }
)

watch(
  () => config.value.defaultValue,
  val => {
    const isMultiple = config.value.displayType === '7'
    if (isMultiple) {
      selectValue.value = Array.isArray(val) ? [...val] : val
    }
    nextTick(() => {
      multiple.value = isMultiple
    })
  }
)

watch(
  () => config.value.displayType,
  (val, oldValue) => {
    if (!props.isConfig) return
    selectValue.value = val === '7' ? [] : ''
    multiple.value = val === '7'
    if (!['1', '7', undefined].includes(oldValue)) {
      config.value.defaultValue = multiple.value ? [] : ''
    }
  },
  {
    immediate: true
  }
)

const handleValueChange = () => {
  const value = Array.isArray(selectValue.value) ? [...selectValue.value] : selectValue.value
  if (!props.isConfig) {
    config.value.selectValue = Array.isArray(selectValue.value)
      ? [...selectValue.value]
      : selectValue.value
    return
  }
  config.value.defaultValue = value
}

onBeforeMount(() => {
  const { defaultValueCheck, displayType, defaultValue } = config.value
  const plus = displayType === '7'
  if (defaultValueCheck) {
    config.value.selectValue = Array.isArray(defaultValue) ? [...defaultValue] : defaultValue
    selectValue.value = Array.isArray(defaultValue) ? [...defaultValue] : defaultValue
  } else {
    config.value.selectValue = plus ? [] : ''
    selectValue.value = plus ? [] : ''
  }
  multiple.value = config.value.displayType === '7'
})
</script>

<template>
  <el-date-picker
    v-model="selectValue"
    type="datetimerange"
    v-if="multiple"
    :shortcuts="shortcuts"
    @change="handleValueChange"
    :prefix-icon="Calendar"
    range-separator="To"
    start-placeholder="Start date"
    end-placeholder="End date"
  />
  <el-date-picker
    v-else
    v-model="selectValue"
    @change="handleValueChange"
    type="datetime"
    :prefix-icon="Calendar"
    placeholder="Pick a Date"
    format="YYYY/MM/DD HH:mm:ss"
  />
</template>
