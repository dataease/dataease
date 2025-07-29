<script lang="ts" setup>
import { toRefs, PropType, ref, Ref, onBeforeMount, watch, nextTick, computed, inject } from 'vue'
import { type DatePickType } from 'element-plus-secondary'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import type { ManipulateType } from 'dayjs'
import { type TimeRange } from './time-format'
import dayjs from 'dayjs'
import { useI18n } from '@/hooks/web/useI18n'
import { useShortcuts } from './shortcuts'
import {
  getThisStart,
  getThisEnd,
  getLastStart,
  getAround,
  getCustomRange
} from './time-format-dayjs'
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
  queryConditionWidth: number
  displayType: string
  timeGranularity: DatePickType
  timeGranularityMultiple: DatePickType
  timeRange: TimeRange
  placeholder: string
  setTimeRange: boolean
}
const { t } = useI18n()

const props = defineProps({
  config: {
    type: Object as PropType<SelectConfig>,
    default: () => {
      return {
        selectValue: '',
        defaultValue: '',
        queryConditionWidth: 0,
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
const placeholder: Ref = inject('placeholder')
const placeholderText = computed(() => {
  if (placeholder?.value?.placeholderShow) {
    return props.config.placeholder
  }
  return ' '
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
    if (props.isConfig) return
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
const callback = param => {
  startWindowTime.value = param[0]
  const disabled = param.some(ele => {
    return disabledDate(ele)
  })
  startWindowTime.value = 0
  return disabled
}

const { shortcuts } = useShortcuts(callback)

watch(
  () => config.value.id,
  () => {
    init()
  }
)

const displayTypeChange = () => {
  if (!props.isConfig) return
  if (multiple.value && config.value.displayType === '7') return
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
  selectValue.value = Array.isArray(selectValue.value)
    ? selectValue.value.map(ele => ele && dayjs(ele).format('YYYY/MM/DD HH:mm:ss'))
    : selectValue.value && dayjs(selectValue.value).format('YYYY/MM/DD HH:mm:ss')
  const value = Array.isArray(selectValue.value) ? [...selectValue.value] : selectValue.value
  if (!props.isConfig) {
    config.value.selectValue = Array.isArray(selectValue.value)
      ? [...selectValue.value]
      : selectValue.value
    nextTick(() => {
      isConfirmSearch(config.value.id)
    })
    return
  }
  config.value.defaultValue = Array.isArray(value)
    ? value.map(ele => new Date(ele).toLocaleString())
    : new Date(value).toLocaleString()
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
  currentDate.value = currentDate.value.slice(0, getIndex() + 1)
}

const queryConditionWidth = inject('com-width', Function, true)
const getCustomWidth = () => {
  if (placeholder?.value?.placeholderShow) {
    if (props.config.queryConditionWidth === undefined) {
      return queryConditionWidth()
    }
    return props.config.queryConditionWidth
  }
  return 227
}
const isConfirmSearch = inject('is-confirm-search', Function, true)
const selectStyle = computed(() => {
  return props.isConfig
    ? {}
    : {
        width: (multiple.value ? getCustomWidth() * 2 : getCustomWidth()) + 'px !important'
      }
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
  const end = selectValue.value?.length > 1 ? selectValue.value[1] : null
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
const startWindowTime = ref(0)
const calendarChange = val => {
  startWindowTime.value = +new Date(val[0])
}

const datePicker = ref()

const visibleChange = (visible: boolean) => {
  startWindowTime.value = 0
  if (!visible) {
    datePicker.value?.blur()
  }
}

const queryTimeType = computed(() => {
  const noTime = config.value.timeGranularityMultiple.split('time').join('').split('range')[0]
  return noTime === 'date' ? 'day' : (noTime as ManipulateType)
})

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
    relativeToCurrentRange,
    timeNum,
    relativeToCurrentType,
    around,
    dynamicWindow,
    maximumSingleQuery,
    timeNumRange,
    relativeToCurrentTypeRange,
    aroundRange
  } = config.value.timeRange || {}
  let isDynamicWindowTime = false

  if (startWindowTime.value && dynamicWindow) {
    isDynamicWindowTime =
      dayjs(startWindowTime.value)
        .add(maximumSingleQuery, queryTimeType.value)
        .startOf(queryTimeType.value)
        .valueOf() -
        1000 <
        timeStamp ||
      dayjs(startWindowTime.value)
        .subtract(maximumSingleQuery, queryTimeType.value)
        .startOf(queryTimeType.value)
        .valueOf() +
        1000 >
        timeStamp
  }
  if (intervalType === 'none') {
    if (dynamicWindow) return isDynamicWindowTime
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
      case 'thisQuarter':
        startTime = getThisStart('quarter')
        break
      case 'thisWeek':
        startTime = new Date(
          dayjs().startOf('week').add(1, 'day').startOf('day').format('YYYY/MM/DD HH:mm:ss')
        )
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
      case 'monthEnd':
        startTime = getThisEnd('month')
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
    return timeStamp < +new Date(startValue) || isDynamicWindowTime
  }

  if (intervalType === 'end') {
    return timeStamp > +new Date(startValue) || isDynamicWindowTime
  }

  if (intervalType === 'timeInterval') {
    let endTime
    if (relativeToCurrentRange === 'custom') {
      startTime =
        regularOrTrends === 'fixed'
          ? new Date(
              dayjs(new Date(regularOrTrendsValue[0]))
                .startOf(queryTimeType.value)
                .format('YYYY/MM/DD HH:mm:ss')
            )
          : getAround(relativeToCurrentType, around === 'f' ? 'subtract' : 'add', timeNum)
      endTime =
        regularOrTrends === 'fixed'
          ? new Date(
              dayjs(new Date(regularOrTrendsValue[1]))
                .endOf(queryTimeType.value)
                .format('YYYY/MM/DD HH:mm:ss')
            )
          : getAround(
              relativeToCurrentTypeRange,
              aroundRange === 'f' ? 'subtract' : 'add',
              timeNumRange
            )
    } else {
      ;[startTime, endTime] = getCustomRange(relativeToCurrentRange)
    }
    return (
      timeStamp < +new Date(startTime) - 1000 ||
      timeStamp > +new Date(endTime) ||
      isDynamicWindowTime
    )
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
  const timeFormat = [1, 2].includes(currentDate.value.length)
    ? currentDate.value.concat(Array([0, 2, 1][currentDate.value.length]).fill('01'))
    : currentDate.value
  if (isRange.value) {
    const [start, end] = selectValue.value || []
    if (selectSecond.value) {
      selectValue.value = [
        start ? start : new Date(`${timeFormat.join('/')} ${currentTime.value.join(':')}`),
        new Date(`${timeFormat.join('/')} ${currentTime.value.join(':')}`)
      ]
    } else {
      selectValue.value = [
        new Date(`${timeFormat.join('/')} ${currentTime.value.join(':')}`),
        end ? end : new Date(`${timeFormat.join('/')} ${currentTime.value.join(':')}`)
      ]
    }
  } else {
    selectValue.value = new Date(`${timeFormat.join('/')} ${currentTime.value.join(':')}`)
  }
}

const onClear = () => {
  showDate.value = false
  const { displayType } = config.value
  const plus = displayType === '7'
  config.value.selectValue = plus ? [] : undefined
  selectValue.value = plus ? [] : undefined
  handleValueChange()
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
    v-if="multiple"
    :key="config.timeGranularityMultiple"
    :type="config.timeGranularityMultiple"
    :style="selectStyle"
    ref="datePicker"
    @visible-change="visibleChange"
    :disabled-date="disabledDate"
    @calendar-change="calendarChange"
    :format="formatDate"
    :shortcuts="
      ['datetimerange', 'daterange'].includes(config.timeGranularityMultiple) ? shortcuts : []
    "
    @change="handleValueChange"
    :editable="false"
    :range-separator="$t('cron.to')"
    :start-placeholder="placeholderText"
    :end-placeholder="placeholderText"
  />
  <el-date-picker
    v-else
    :key="config.timeGranularity + 1"
    v-model="selectValue"
    @visible-change="visibleChange"
    :disabled-date="disabledDate"
    :type="config.timeGranularity"
    @change="handleValueChange"
    :style="selectStyle"
    :placeholder="placeholderText"
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
      :title="t('v_query.time_selection')"
      :tabs="[t('dataset.select_date'), t('dataset.select_time')]"
      :next-step-text="t('sync_datasource.next')"
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
      :title="t('dataset.select_date')"
      :columns-type="columnsType"
      @confirm="onConfirm"
      @cancel="onCancel"
      :min-date="minDate"
      :max-date="maxDate"
      v-if="!showTimePick"
      v-model="currentDate"
    />
  </van-popup>
  <Teleport v-if="showDate" to=".van-picker__toolbar">
    <button
      style="position: absolute; top: 0; right: 60px"
      @click="onClear"
      class="van-picker__confirm van-haptics-feedback oooo"
    >
      {{ t('commons.clear') }}
    </button></Teleport
  >
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
