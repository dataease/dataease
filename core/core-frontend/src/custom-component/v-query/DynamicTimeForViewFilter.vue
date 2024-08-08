<script lang="ts" setup>
import { toRefs, PropType, ref, onBeforeMount, watch, computed } from 'vue'
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
  relativeToCurrent: string
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
        relativeToCurrent: 'custom',
        timeNum: 0,
        relativeToCurrentType: 'year',
        around: 'f',
        arbitraryTime: new Date(),
        timeGranularity: 'year'
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
    class="date-editor_granularity"
    :key="config.timeGranularity"
    v-model="selectValue"
    :type="config.timeGranularity"
    :placeholder="$t('commons.date.select_date_time')"
  />
</template>

<style lang="less">
.date-editor_granularity .ed-input__wrapper {
  width: 325px;
  margin-top: 0;
}
</style>
