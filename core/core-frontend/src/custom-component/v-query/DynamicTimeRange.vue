<script lang="ts" setup>
import { toRefs, PropType, ref, onBeforeMount, watch, computed } from 'vue'
import { Calendar } from '@element-plus/icons-vue'
import { type DatePickType } from 'element-plus-secondary'
import { getCustomTime } from './time-format'
interface SelectConfig {
  timeType: string
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

const { config } = toRefs(props)

const timeConfig = computed(() => {
  const {
    timeNum,
    relativeToCurrentType,
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
    init()
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
    init()
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
    arbitraryTime
  )
  const endTime = getCustomTime(
    timeNumRange,
    relativeToCurrentTypeRange,
    timeGranularity,
    aroundRange,
    arbitraryTimeRange
  )
  selectValue.value = [startTime, endTime]
}

onBeforeMount(() => {
  init()
})
</script>

<template>
  <el-date-picker
    disabled
    v-model="selectValue"
    type="datetimerange"
    :prefix-icon="Calendar"
    :range-separator="$t('cron.to')"
    :start-placeholder="$t('datasource.start_time')"
    :end-placeholder="$t('datasource.end_time')"
  />
</template>
