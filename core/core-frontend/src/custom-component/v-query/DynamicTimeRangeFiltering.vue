<script lang="ts" setup>
import { toRefs, PropType, ref, onBeforeMount, watch, computed } from 'vue'
import { Calendar } from '@element-plus/icons-vue'
import { type DatePickType } from 'element-plus-secondary'
import { getCustomTime } from './time-format'
interface SelectConfig {
  regularOrTrends: string
  regularOrTrendsValue: [Date, Date]
  intervalType: string
  timeNum: number
  relativeToCurrentType: string
  around: string
  timeGranularity: DatePickType
  timeNumRange: number
  relativeToCurrentTypeRange: string
  aroundRange: string
}

const props = defineProps({
  config: {
    type: Object as PropType<SelectConfig>,
    default: () => {
      return {
        timeGranularityMultiple: 'datetimerange',
        regularOrTrendsValue: [],
        regularOrTrends: 'fixed',
        timeNum: 0,
        intervalType: 'none',
        relativeToCurrentType: 'year',
        around: 'f',
        timeGranularity: 'date',
        timeNumRange: 0,
        relativeToCurrentTypeRange: 'year',
        aroundRange: 'f'
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
    timeNum,
    relativeToCurrentType,
    around,
    intervalType,
    timeGranularity,
    timeNumRange,
    relativeToCurrentTypeRange,
    aroundRange
  } = config.value
  return {
    timeNum,
    relativeToCurrentType,
    around,
    intervalType,
    timeGranularity,
    timeNumRange,
    relativeToCurrentTypeRange,
    aroundRange
  }
})
const timeInterval = computed<DatePickType>(() => {
  return config.value.intervalType !== 'timeInterval'
    ? (props.timeGranularityMultiple.split('range')[0] as DatePickType)
    : props.timeGranularityMultiple
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

const init = () => {
  const {
    timeNum,
    relativeToCurrentType,
    around,
    timeGranularity,
    timeNumRange,
    relativeToCurrentTypeRange,
    aroundRange
  } = timeConfig.value

  const startTime = getCustomTime(
    timeNum,
    relativeToCurrentType,
    timeGranularity,
    around,
    null,
    timeInterval.value,
    'start-config'
  )
  const endTime = getCustomTime(
    timeNumRange,
    relativeToCurrentTypeRange,
    timeGranularity,
    aroundRange,
    null,
    timeInterval.value,
    'end-config'
  )

  config.value.regularOrTrendsValue = [startTime, endTime]
}

onBeforeMount(() => {
  init()
})

const formatDate = computed(() => {
  return (props.timeGranularityMultiple as string) === 'yearrange' ? 'YYYY' : undefined
})
</script>

<template>
  <el-date-picker
    :disabled="config.regularOrTrends !== 'fixed'"
    v-model="config.regularOrTrendsValue"
    :type="timeInterval"
    :prefix-icon="Calendar"
    :format="formatDate"
    :range-separator="$t('cron.to')"
    :start-placeholder="$t('datasource.start_time')"
    :end-placeholder="$t('datasource.end_time')"
  />
</template>
