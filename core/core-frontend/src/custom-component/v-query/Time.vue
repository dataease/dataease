<script lang="ts" setup>
import { toRefs, provide, PropType, ref, onBeforeMount, watch, nextTick, computed } from 'vue'
import { Calendar } from '@element-plus/icons-vue'

interface SelectConfig {
  selectValue: any
  defaultValue: any
  defaultValueCheck: boolean
  id: string
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
  () => config.value.defaultValue,
  val => {
    const isMultiple = config.value.displayType === '7'
    if (isMultiple) {
      multiple.value = isMultiple
    }
    selectValue.value = Array.isArray(val) ? [...val] : val
    nextTick(() => {
      multiple.value = isMultiple
    })
  }
)

watch(
  () => config.value.id,
  () => {
    init()
  }
)

const displayTypeChange = () => {
  if (!props.isConfig) return
  selectValue.value = config.value.displayType === '7' ? [] : ''
  multiple.value = config.value.displayType === '7'
  config.value.defaultValue = multiple.value ? [] : ''
  selectValue.value = multiple.value ? [] : ''
}
watch(
  () => config.value.selectValue,
  val => {
    if (props.isConfig) return
    if (config.value.displayType === '7') {
      selectValue.value = Array.isArray(val) ? [...val] : val
    }
    nextTick(() => {
      multiple.value = config.value.displayType === '7'
      if (!multiple.value) {
        selectValue.value = Array.isArray(config.value.selectValue)
          ? [...config.value.selectValue]
          : config.value.selectValue
      }
    })
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

const init = () => {
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
}

const selectStyle = computed(() => {
  return props.isConfig ? {} : { width: multiple.value ? '470px' : '227px' }
})

onBeforeMount(() => {
  init()
})

defineExpose({
  displayTypeChange
})
</script>

<template>
  <el-date-picker
    v-model="selectValue"
    type="datetimerange"
    :style="selectStyle"
    v-if="multiple"
    :shortcuts="shortcuts"
    @change="handleValueChange"
    :prefix-icon="Calendar"
    :range-separator="$t('cron.to')"
    :start-placeholder="$t('datasource.start_time')"
    :end-placeholder="$t('datasource.end_time')"
  />
  <el-date-picker
    v-else
    v-model="selectValue"
    @change="handleValueChange"
    type="datetime"
    :style="selectStyle"
    :prefix-icon="Calendar"
    :placeholder="$t('commons.date.select_date_time')"
    format="YYYY/MM/DD HH:mm:ss"
  />
</template>
