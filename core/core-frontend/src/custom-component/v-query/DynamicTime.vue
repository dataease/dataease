<script lang="ts" setup>
import { toRefs, PropType, ref, onBeforeMount, watch, computed } from 'vue'
import { Calendar } from '@element-plus/icons-vue'
import { type DatePickType } from 'element-plus-secondary'
import {
  getThisYear,
  getlastYear,
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
  relativeToCurrent: string
  defaultValueCheck: boolean
  id: string
  timeNum: number
  relativeToCurrentType: string
  around: string
  arbitraryTime: string
  timeGranularity: DatePickType
}

const props = defineProps({
  config: {
    type: Object as PropType<SelectConfig>,
    default: () => {
      return {
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
    arbitraryTime,
    timeGranularity
  } = config.value
  return {
    relativeToCurrent,
    timeNum,
    relativeToCurrentType,
    around,
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
    arbitraryTime,
    timeGranularity
  } = timeConfig.value
  if (relativeToCurrent === 'custom') {
    selectValue.value = getCustomTime(
      timeNum,
      relativeToCurrentType,
      around,
      timeGranularity === 'datetime' ? arbitraryTime : null
    )
  } else {
    switch (relativeToCurrent) {
      case 'thisYear':
        selectValue.value = getThisYear()
        break
      case 'lastYear':
        selectValue.value = getlastYear()
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
