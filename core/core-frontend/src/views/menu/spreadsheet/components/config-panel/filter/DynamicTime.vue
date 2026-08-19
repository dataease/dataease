<script lang="ts" setup>
import { computed, type PropType } from 'vue'
import { type DatePickType } from 'element-plus-secondary'

interface SelectConfig {
  relativeToCurrent: string
  timeNum: number
  relativeToCurrentType: string
  around: string
  arbitraryTime: Date | string
  timeGranularity: DatePickType
}

const props = defineProps({
  config: {
    type: Object as PropType<SelectConfig>,
    default: () => ({
      relativeToCurrent: 'custom',
      timeNum: 0,
      relativeToCurrentType: 'year',
      around: 'f',
      arbitraryTime: new Date(),
      timeGranularity: 'year'
    })
  }
})

const startOf = (date: Date, type: string) => {
  const result = new Date(date)
  if (['year'].includes(type)) {
    result.setMonth(0)
  }
  if (['year', 'month'].includes(type)) {
    result.setDate(1)
  }
  if (['year', 'month', 'date'].includes(type)) {
    result.setHours(0, 0, 0, 0)
  }
  return result
}

const addTime = (date: Date, amount: number, type: string) => {
  const result = new Date(date)
  if (type === 'year') {
    result.setFullYear(result.getFullYear() + amount)
  } else if (type === 'month') {
    result.setMonth(result.getMonth() + amount)
  } else {
    result.setDate(result.getDate() + amount)
  }
  return result
}

const withArbitraryTime = (date: Date, arbitraryTime: Date | string) => {
  const time = arbitraryTime instanceof Date ? arbitraryTime : new Date(arbitraryTime)
  if (Number.isNaN(time.getTime())) {
    return date
  }
  const result = new Date(date)
  result.setHours(time.getHours(), time.getMinutes(), time.getSeconds(), 0)
  return result
}

const getCustomTime = (config: SelectConfig) => {
  const amount = config.around === 'f' ? -config.timeNum : config.timeNum
  const type = config.relativeToCurrentType === 'date' ? 'day' : config.relativeToCurrentType
  const result = addTime(new Date(), amount, type)
  if (config.timeGranularity === 'datetime') {
    return withArbitraryTime(result, config.arbitraryTime)
  }
  return startOf(result, config.timeGranularity)
}

const previewValue = computed(() => {
  const config = props.config
  const now = new Date()
  switch (config.relativeToCurrent) {
    case 'thisYear':
      return startOf(now, 'year')
    case 'lastYear':
      return startOf(addTime(now, -1, 'year'), 'year')
    case 'thisMonth':
      return startOf(now, 'month')
    case 'lastMonth':
      return startOf(addTime(now, -1, 'month'), 'month')
    case 'today':
      return startOf(now, 'date')
    case 'yesterday':
      return startOf(addTime(now, -1, 'day'), 'date')
    case 'monthBeginning':
      return startOf(now, 'month')
    case 'yearBeginning':
      return startOf(now, 'year')
    case 'custom':
    default:
      return getCustomTime(config)
  }
})
</script>

<template>
  <el-date-picker
    :model-value="previewValue"
    disabled
    class="date-editor-granularity"
    :key="config.timeGranularity"
    :type="config.timeGranularity"
    :placeholder="$t('commons.date.select_date_time')"
  />
</template>

<style lang="less" scoped>
.date-editor-granularity {
  width: 100%;
}
</style>
