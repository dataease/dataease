<script lang="ts" setup>
import { toRefs, PropType, ref, onBeforeMount, watch, nextTick, computed } from 'vue'
import { Calendar } from '@element-plus/icons-vue'
import { type DatePickType } from 'element-plus-secondary'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { type TimeRange } from './time-format'
import { getThisStart, getLastStart, getAround } from './time-format-dayjs'
import VanPopup from 'vant/es/popup'
import VanDatePicker from 'vant/es/date-picker'
import VanTimePicker from 'vant/es/time-picker'
import VanPickerGroup from 'vant/es/picker-group'
import 'vant/es/popup/style'
import 'vant/es/date-picker/style'
import 'vant/es/picker-group/style'
import 'vant/es/time-picker/style'

interface SelectConfig {
  selectValue: any
  defaultValue: any
  defaultValueCheck: boolean
  id: string
  displayType: string
  timeGranularity: DatePickType
  timeGranularityMultiple: DatePickType
  timeRange: TimeRange
  setTimeRange: boolean
}

const props = defineProps({
  config: {
    type: Object as PropType<SelectConfig>,
    default: () => {
      return {
        selectValue: '',
        defaultValue: '',
        defaultValueCheck: false,
        displayType: '1',
        timeGranularity: 'date',
        setTimeRange: false,
        timeGranularityMultiple: 'daterange',
        timeRange: {
          intervalType: 'none',
          dynamicWindow: false,
          maximumSingleQuery: 0,
          regularOrTrends: 'fixed',
          regularOrTrendsValue: '',
          relativeToCurrent: 'custom',
          timeNum: 0,
          relativeToCurrentType: 'year',
          around: 'f',
          timeNumRange: 0,
          relativeToCurrentTypeRange: 'year',
          aroundRange: 'f'
        }
      }
    }
  },
  isConfig: {
    type: Boolean,
    default: false
  }
})
const selectValue = ref()
const multiple = ref(false)
const dvMainStore = dvMainStoreWithOut()

const { config } = toRefs(props)
const minDate = new Date('1970/1/1')
const maxDate = new Date('2100/1/1')
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
  selectValue.value = config.value.displayType === '7' ? [] : undefined
  multiple.value = config.value.displayType === '7'
  config.value.defaultValue = multiple.value ? [] : undefined
  selectValue.value = multiple.value ? [] : undefined
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
    config.value.selectValue = plus ? [] : undefined
    selectValue.value = plus ? [] : undefined
  }
  multiple.value = config.value.displayType === '7'
}

const selectStyle = computed(() => {
  return props.isConfig ? {} : { width: multiple.value ? '470px' : '227px' }
})

const columnsType = computed(() => {
  if (!dvMainStore.mobileInPc) return []
  return ['year', 'month', 'day'].slice(0, getIndex() + 1)
})

const showTimePick = computed(() => {
  if (!dvMainStore.mobileInPc) return false
  const type = multiple.value ? config.value.timeGranularityMultiple : config.value.timeGranularity
  return type.includes('datetime')
})
const currentTime = ref([])
const currentDate = ref(['2021', '01', '01'])
const showDate = ref(false)

const isRange = computed(() => {
  if (!dvMainStore.mobileInPc) return false
  return +config.value.displayType === 7
})

const showPopupRight = () => {
  const [_, end] = selectValue.value || []
  if (!!end) {
    const time = new Date(end)
    currentDate.value = [
      `${time.getFullYear()}`,
      `${time.getMonth() + 1}`,
      `${time.getDate()}`
    ].slice(0, getIndex() + 1)
    showTimePick.value &&
      (currentTime.value = [`${time.getHours()}`, `${time.getMinutes()}`, `${time.getSeconds()}`])
  }
  selectSecond.value = true
  showDate.value = true
}

const getIndex = () => {
  const type = multiple.value ? config.value.timeGranularityMultiple : config.value.timeGranularity
  const index = ['year', 'month', 'date'].findIndex(ele => type.includes(ele))
  return index
}

const disabledDate = val => {
  const timeStamp = +new Date(val)
  if (!config.value.setTimeRange) {
    return false
  }
  const {
    intervalType,
    regularOrTrends,
    regularOrTrendsValue,
    relativeToCurrent,
    timeNum,
    relativeToCurrentType,
    around,
    timeNumRange,
    relativeToCurrentTypeRange,
    aroundRange
  } = config.value.timeRange || {}
  if (intervalType === 'none') {
    return false
  }
  let startTime
  if (relativeToCurrent === 'custom') {
    startTime = getAround(relativeToCurrentType, around === 'f' ? 'subtract' : 'add', timeNum)
  } else {
    switch (relativeToCurrent) {
      case 'thisYear':
        startTime = getThisStart('year')
        break
      case 'lastYear':
        startTime = getLastStart('year')
        break
      case 'thisMonth':
        startTime = getThisStart('month')
        break
      case 'lastMonth':
        startTime = getLastStart('month')
        break
      case 'today':
        startTime = getThisStart('day')
        break
      case 'yesterday':
        startTime = getLastStart('day')
        break
      case 'monthBeginning':
        startTime = getThisStart('month')
        break
      case 'yearBeginning':
        startTime = getThisStart('year')
        break

      default:
        break
    }
  }
  const startValue = regularOrTrends === 'fixed' ? regularOrTrendsValue : startTime

  if (intervalType === 'start') {
    return timeStamp < +new Date(startValue)
  }

  if (intervalType === 'end') {
    return timeStamp > +new Date(startValue)
  }

  if (intervalType === 'timeInterval') {
    const startTime =
      regularOrTrends === 'fixed'
        ? regularOrTrendsValue[0]
        : getAround(relativeToCurrentType, around === 'f' ? 'subtract' : 'add', timeNum)
    const endTime =
      regularOrTrends === 'fixed'
        ? regularOrTrendsValue[1]
        : getAround(
            relativeToCurrentTypeRange,
            aroundRange === 'f' ? 'subtract' : 'add',
            timeNumRange
          )
    return timeStamp < +new Date(startTime) - 1000 || timeStamp > +new Date(endTime)
  }
}

const showPopup = () => {
  if (isRange.value) {
    const [start] = selectValue.value || []
    if (!!start) {
      const time = new Date(start)
      currentDate.value = [
        `${time.getFullYear()}`,
        `${time.getMonth() + 1}`,
        `${time.getDate()}`
      ].slice(0, getIndex() + 1)
      showTimePick.value &&
        (currentTime.value = [`${time.getHours()}`, `${time.getMinutes()}`, `${time.getSeconds()}`])
    }
  } else {
    const time = selectValue.value ? new Date(selectValue.value) : new Date()
    currentDate.value = [
      `${time.getFullYear()}`,
      `${time.getMonth() + 1}`,
      `${time.getDate()}`
    ].slice(0, getIndex() + 1)
    showTimePick.value &&
      (currentTime.value = [`${time.getHours()}`, `${time.getMinutes()}`, `${time.getSeconds()}`])
  }
  selectSecond.value = false
  showDate.value = true
}

const onCancel = () => {
  showDate.value = false
}

const selectSecond = ref(false)

const setArrValue = () => {
  currentDate.value = currentDate.value.slice(0, getIndex() + 1)
  if (isRange.value) {
    const [start, end] = selectValue.value || []
    if (selectSecond.value) {
      selectValue.value = [
        start,
        new Date(`${currentDate.value.join('/')} ${currentTime.value.join(':')}`)
      ]
    } else {
      selectValue.value = [
        new Date(`${currentDate.value.join('/')} ${currentTime.value.join(':')}`),
        end
      ]
    }
  } else {
    selectValue.value = new Date(`${currentDate.value.join('/')} ${currentTime.value.join(':')}`)
  }
}

const onConfirm = () => {
  setArrValue()
  handleValueChange()
  showDate.value = false
}

onBeforeMount(() => {
  init()
})

defineExpose({
  displayTypeChange
})

const formatDate = computed(() => {
  return (config.value.timeGranularityMultiple as string) === 'yearrange' ? 'YYYY' : undefined
})
</script>

<template>
  <el-date-picker
    v-model="selectValue"
    :key="config.timeGranularityMultiple"
    :type="config.timeGranularityMultiple"
    :style="selectStyle"
    :disabled-date="disabledDate"
    :format="formatDate"
    v-if="multiple"
    @change="handleValueChange"
    :prefix-icon="Calendar"
    :range-separator="$t('cron.to')"
    :start-placeholder="$t('datasource.start_time')"
    :end-placeholder="$t('datasource.end_time')"
  />
  <el-date-picker
    v-else
    v-model="selectValue"
    :type="config.timeGranularity"
    :disabled-date="disabledDate"
    :style="selectStyle"
    :prefix-icon="Calendar"
    :placeholder="$t('commons.date.select_date_time')"
  />
  <div
    v-if="dvMainStore.mobileInPc"
    class="vant-mobile"
    :class="isRange && 'wl50'"
    @click="showPopup"
  />
  <div v-if="dvMainStore.mobileInPc && isRange" class="vant-mobile wr50" @click="showPopupRight" />
  <van-popup
    v-if="dvMainStore.mobileInPc"
    teleport="body"
    position="bottom"
    v-model:show="showDate"
  >
    <van-picker-group
      @confirm="onConfirm"
      @cancel="onCancel"
      v-if="showTimePick"
      title="时间选择"
      :tabs="['选择日期', '选择时间']"
      next-step-text="下一步"
    >
      <van-date-picker
        :min-date="minDate"
        :max-date="maxDate"
        :columns-type="columnsType"
        v-model="currentDate"
      />
      <van-time-picker :columns-type="['hour', 'minute', 'second']" v-model="currentTime" />
    </van-picker-group>
    <van-date-picker
      title="选择日期"
      :columns-type="columnsType"
      @confirm="onConfirm"
      @cancel="onCancel"
      :min-date="minDate"
      :max-date="maxDate"
      v-if="!showTimePick"
      v-model="currentDate"
    />
  </van-popup>
</template>

<style lang="less">
.vant-mobile {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;

  &.wl50 {
    width: 50%;
  }

  &.wr50 {
    left: auto;
    right: 0;
    width: 50%;
  }
}
</style>
