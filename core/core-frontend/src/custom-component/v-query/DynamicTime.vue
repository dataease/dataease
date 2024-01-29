<script lang="ts" setup>
import { toRefs, PropType, ref, onBeforeMount, watch, computed } from 'vue'
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
  defaultValue: string
  selectValue: string
  relativeToCurrent: string
  defaultValueCheck: boolean
  id: string
  timeNum: number
  relativeToCurrentType: string
  around: string
  arbitraryTime: Date
  timeGranularity: DatePickType
}

const props = defineProps({
  config: {
    type: Object as PropType<SelectConfig>,
    default: () => {
      return {
        defaultValue: '',
        selectValue: '',
        timeType: 'fixed',
        relativeToCurrent: 'custom',
        timeNum: 0,
        relativeToCurrentType: 'year',
        around: 'f',
        arbitraryTime: new Date(),
        defaultValueCheck: false,
        timeGranularity: 'date'
      }
    }
  }
})
const selectValue = ref()

const { config } = toRefs(props)

const timeConfig = computed(() => {
  const {
    relativeToCurrent,
    timeNum,
    relativeToCurrentType,
    around,
    defaultValueCheck,
    arbitraryTime,
    timeGranularity
  } = config.value
  return {
    relativeToCurrent,
    timeNum,
    relativeToCurrentType,
    around,
    defaultValueCheck,
    arbitraryTime,
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
    relativeToCurrent,
    timeNum,
    relativeToCurrentType,
    around,
    defaultValueCheck,
    arbitraryTime,
    timeGranularity
  } = timeConfig.value
  if (!defaultValueCheck) {
    selectValue.value = null
    return
  }
  if (relativeToCurrent === 'custom') {
    selectValue.value = getCustomTime(
      timeNum,
      relativeToCurrentType,
      timeGranularity,
      around,
      timeGranularity === 'datetime' ? arbitraryTime : null
    )
  } else {
    switch (relativeToCurrent) {
      case 'thisYear':
        selectValue.value = getThisYear()
        break
      case 'lastYear':
        selectValue.value = getLastYear()
        break
      case 'thisMonth':
        selectValue.value = getThisMonth()
        break
      case 'lastMonth':
        selectValue.value = getLastMonth()
        break
      case 'today':
        selectValue.value = getToday()
        break
      case 'yesterday':
        selectValue.value = getYesterday()
        break
      case 'monthBeginning':
        selectValue.value = getMonthBeginning()
        break
      case 'yearBeginning':
        selectValue.value = getYearBeginning()
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
    disabled
    v-model="selectValue"
    :type="config.timeGranularity"
    :prefix-icon="Calendar"
    :placeholder="$t('commons.date.select_date_time')"
  />
</template>
