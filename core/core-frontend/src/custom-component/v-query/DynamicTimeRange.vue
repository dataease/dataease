<script lang="ts" setup>
import { toRefs, nextTick, PropType, ref, onBeforeMount, watch, computed } from 'vue'
import { Calendar } from '@element-plus/icons-vue'
import { type DatePickType } from 'element-plus-secondary'
import { getCustomTime } from './time-format'
interface SelectConfig {
  timeType: string
  timeGranularityMultiple: DatePickType
  defaultValue: [Date, Date]
  selectValue: [Date, Date]
  defaultValueCheck: boolean
  id: string
  timeNum: number
  relativeToCurrentType: string
  around: string
  arbitraryTime: Date
  timeGranularity: DatePickType
  timeNumRange: number
  relativeToCurrentTypeRange: string
  aroundRange: string
  arbitraryTimeRange: Date
}

const props = defineProps({
  config: {
    type: Object as PropType<SelectConfig>,
    default: () => {
      return {
        timeGranularityMultiple: 'datetimerange',
        defaultValue: [],
        selectValue: [],
        timeType: 'fixed',
        timeNum: 0,
        relativeToCurrentType: 'year',
        around: 'f',
        arbitraryTime: new Date(),
        defaultValueCheck: false,
        timeGranularity: 'date',
        timeNumRange: 0,
        relativeToCurrentTypeRange: 'year',
        aroundRange: 'f',
        arbitraryTimeRange: new Date()
      }
    }
  }
})
const selectValue = ref<[Date, Date]>([new Date(), new Date()])
const rendering = ref(true)

const { config } = toRefs(props)

const timeConfig = computed(() => {
  const {
    timeNum,
    relativeToCurrentType,
    timeGranularityMultiple,
    around,
    defaultValueCheck,
    arbitraryTime,
    timeGranularity,
    timeNumRange,
    relativeToCurrentTypeRange,
    aroundRange,
    arbitraryTimeRange
  } = config.value
  return {
    timeNum,
    relativeToCurrentType,
    around,
    timeGranularityMultiple,
    defaultValueCheck,
    arbitraryTime,
    timeGranularity,
    timeNumRange,
    relativeToCurrentTypeRange,
    aroundRange,
    arbitraryTimeRange
  }
})

watch(
  () => timeConfig.value,
  () => {
    rendering.value = false
    nextTick(() => {
      init()
      rendering.value = true
    })
  },
  {
    deep: true
  }
)

watch(
  () => selectValue.value,
  val => {
    config.value.defaultValue = val
    config.value.selectValue = val
  }
)

watch(
  () => config.value.id,
  () => {
    rendering.value = false
    nextTick(() => {
      init()
      rendering.value = true
    })
  }
)

const init = () => {
  const {
    timeNum,
    relativeToCurrentType,
    around,
    defaultValueCheck,
    arbitraryTime,
    timeGranularity,
    timeGranularityMultiple,
    timeNumRange,
    relativeToCurrentTypeRange,
    aroundRange,
    arbitraryTimeRange
  } = timeConfig.value
  if (!defaultValueCheck) {
    selectValue.value = [new Date(), new Date()]
    return
  }

  const startTime = getCustomTime(
    timeNum,
    relativeToCurrentType,
    timeGranularity,
    around,
    arbitraryTime,
    timeGranularityMultiple,
    'start-config'
  )
  const endTime = getCustomTime(
    timeNumRange,
    relativeToCurrentTypeRange,
    timeGranularity,
    aroundRange,
    arbitraryTimeRange,
    timeGranularityMultiple,
    'end-config'
  )

  console.log('startTime, endTime', startTime, endTime)
  selectValue.value = [startTime, endTime]
}

onBeforeMount(() => {
  init()
})

const formatDate = computed(() => {
  return (config.value.timeGranularityMultiple as string) === 'yearrange' ? 'YYYY' : undefined
})
</script>

<template>
  <el-date-picker
    disabled
    v-if="rendering && config.timeGranularityMultiple !== 'yearrange'"
    v-model="selectValue"
    :type="config.timeGranularityMultiple"
    :prefix-icon="Calendar"
    :format="formatDate"
    :range-separator="$t('cron.to')"
    :start-placeholder="$t('datasource.start_time')"
    :end-placeholder="$t('datasource.end_time')"
  />
  <el-date-picker
    v-else
    disabled
    v-model="selectValue"
    type="yearrange"
    :prefix-icon="Calendar"
    format="YYYY"
    :range-separator="$t('cron.to')"
    :start-placeholder="$t('datasource.start_time')"
    :end-placeholder="$t('datasource.end_time')"
  />
</template>
