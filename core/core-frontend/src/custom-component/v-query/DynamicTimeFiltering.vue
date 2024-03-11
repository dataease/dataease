<script lang="ts" setup>
import { toRefs, PropType, onBeforeMount, watch, computed } from 'vue'
import { Calendar } from '@element-plus/icons-vue'
import { type DatePickType } from 'element-plus-secondary'
import {
  getThisYear,
  getLastYear,
  getThisMonth,
  getLastMonth,
  getToday,
  getYesterday,
  getMonthBeginning,
  getYearBeginning,
  getCustomTime
} from './time-format'
interface SelectConfig {
  timeType: string
  intervalType: string
  regularOrTrendsValue: Date
  regularOrTrends: string
  relativeToCurrent: string
  timeNum: number
  relativeToCurrentType: string
  around: string
  timeGranularity: DatePickType
}

const props = defineProps({
  config: {
    type: Object as PropType<SelectConfig>,
    default: () => {
      return {
        regularOrTrendsValue: '',
        intervalType: 'none',
        regularOrTrends: 'fixed',
        relativeToCurrent: 'custom',
        timeNum: 0,
        relativeToCurrentType: 'year',
        around: 'f'
      }
    }
  },
  timeGranularityMultiple: {
    type: Object as PropType<DatePickType>,
    default: () => {
      return 'yearrange'
    }
  }
})

const { config } = toRefs(props)

const timeConfig = computed(() => {
  const {
    relativeToCurrent,
    intervalType,
    timeNum,
    relativeToCurrentType,
    around,
    timeGranularity
  } = config.value
  return {
    relativeToCurrent,
    timeNum,
    intervalType,
    relativeToCurrentType,
    around,
    timeGranularity
  }
})

watch(
  () => timeConfig.value,
  () => {
    init()
  },
  {
    deep: true
  }
)

const timeInterval = computed<DatePickType>(() => {
  return config.value.intervalType !== 'timeInterval'
    ? (props.timeGranularityMultiple.split('range')[0] as DatePickType)
    : props.timeGranularityMultiple
})

const init = () => {
  const { relativeToCurrent, timeNum, relativeToCurrentType, around, timeGranularity } =
    timeConfig.value
  if (relativeToCurrent === 'custom') {
    config.value.regularOrTrendsValue = getCustomTime(
      timeNum,
      relativeToCurrentType,
      timeGranularity,
      around,
      null
    )
  } else {
    switch (relativeToCurrent) {
      case 'thisYear':
        config.value.regularOrTrendsValue = getThisYear()
        break
      case 'lastYear':
        config.value.regularOrTrendsValue = getLastYear()
        break
      case 'thisMonth':
        config.value.regularOrTrendsValue = getThisMonth()
        break
      case 'lastMonth':
        config.value.regularOrTrendsValue = getLastMonth()
        break
      case 'today':
        config.value.regularOrTrendsValue = getToday()
        break
      case 'yesterday':
        config.value.regularOrTrendsValue = getYesterday()
        break
      case 'monthBeginning':
        config.value.regularOrTrendsValue = getMonthBeginning()
        break
      case 'yearBeginning':
        config.value.regularOrTrendsValue = getYearBeginning()
        break

      default:
        break
    }
  }
}

onBeforeMount(() => {
  init()
})
</script>

<template>
  <el-date-picker
    :disabled="config.regularOrTrends !== 'fixed'"
    v-model="config.regularOrTrendsValue"
    :type="timeInterval"
    :prefix-icon="Calendar"
    :placeholder="$t('commons.date.select_date_time')"
  />
</template>
