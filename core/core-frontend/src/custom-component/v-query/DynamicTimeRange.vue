<script lang="ts" setup>
import type { ManipulateType } from 'dayjs'
import { toRefs, PropType, ref, onBeforeMount, watch, computed } from 'vue'
import { Calendar } from '@element-plus/icons-vue'
import { type DatePickType } from 'element-plus-secondary'
import { getCustomTime } from './time-format'
import { getCustomRange } from './time-format-dayjs'
interface SelectConfig {
  timeType: string
  timeGranularityMultiple: DatePickType
  defaultValue: [Date, Date]
  selectValue: [Date, Date]
  defaultValueCheck: boolean
  id: string
  relativeToCurrentRange: string
  timeNum: number
  relativeToCurrentType: ManipulateType
  around: string
  arbitraryTime: Date
  timeGranularity: DatePickType
  timeNumRange: number
  relativeToCurrentTypeRange: ManipulateType
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
        relativeToCurrentRange: 'custom',
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
    timeGranularityMultiple,
    around,
    relativeToCurrentRange,
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
    relativeToCurrentRange,
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
    timeGranularityMultiple,
    timeNumRange,
    relativeToCurrentTypeRange,
    aroundRange,
    relativeToCurrentRange,
    arbitraryTimeRange
  } = timeConfig.value
  if (!defaultValueCheck) {
    selectValue.value = [new Date(), new Date()]
    return
  }

  if (relativeToCurrentRange !== 'custom') {
    selectValue.value = getCustomRange(relativeToCurrentRange)
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
    v-model="selectValue"
    :key="config.timeGranularityMultiple"
    :type="config.timeGranularityMultiple"
    :prefix-icon="Calendar"
    :format="formatDate"
    :popper-class="'custom-dynamic-time-range-popper_class'"
    :range-separator="$t('cron.to')"
    :start-placeholder="$t('datasource.start_time')"
    :end-placeholder="$t('datasource.end_time')"
  />
</template>
<style lang="less">
.custom-dynamic-time-range-popper_class {
  font-family: var(--de-canvas_custom_font);
}
</style>
