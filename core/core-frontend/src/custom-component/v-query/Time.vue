<script lang="ts" setup>
import { toRefs, PropType, ref, Ref, onBeforeMount, watch, nextTick, computed, inject } from 'vue'
import { type DatePickType } from 'element-plus-secondary'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import type { ManipulateType } from 'dayjs'
import { type TimeRange } from './time-format'
import dayjs, { type Dayjs } from 'dayjs'
import { FixedTimeError, readFixedTimeValue } from './fixed-time-value'
import { useI18n } from '@/hooks/web/useI18n'
import { useShortcuts } from './shortcuts'
import {
  getThisStart,
  getThisEnd,
  getLastStart,
  getAround,
  getAroundStart,
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
const toPickerValue = (value: unknown) => {
  try {
    const resolved = readFixedTimeValue(value)
    return Array.isArray(resolved) ? [...resolved] : resolved
  } catch (error) {
    if (!(error instanceof FixedTimeError)) throw error
    return config.value.displayType === '7' ? [] : undefined
  }
}
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
    selectValue.value = toPickerValue(val)
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
      selectValue.value = toPickerValue(val)
    }
    nextTick(() => {
      multiple.value = config.value.displayType === '7'
      if (!multiple.value) {
        selectValue.value = toPickerValue(config.value.selectValue)
      }
    })
  }
)

const handleValueChange = () => {
  if (selectValue.value === null) {
    selectValue.value = multiple.value ? [] : undefined
  }

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
  if (!value) {
    config.value.defaultValue = undefined
    return
  }
  config.value.defaultValue = value
}

const init = () => {
  const { defaultValueCheck, displayType, defaultValue } = config.value
  const plus = displayType === '7'
  if (defaultValueCheck) {
    config.value.selectValue = Array.isArray(defaultValue) ? [...defaultValue] : defaultValue
    selectValue.value = toPickerValue(defaultValue)
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
    startTime = getAroundStart(relativeToCurrentType, around === 'f' ? 'subtract' : 'add', timeNum)
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
        startTime = getThisStart('week')
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
    return (
      timeStamp < +new Date(dayjs(startValue).startOf('day').format('YYYY/MM/DD HH:mm:ss')) ||
      isDynamicWindowTime
    )
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
          : getAroundStart(relativeToCurrentType, around === 'f' ? 'subtract' : 'add', timeNum)
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

const PASTE_DATE_PATTERN =
  /^(\d{4})(?:[-/.](\d{1,2}))?(?:[-/.](\d{1,2}))?(?:[ T](\d{1,2}):(\d{1,2})(?::(\d{1,2}))?)?$/

const parsePastedDate = (text: string) => {
  const matched = PASTE_DATE_PATTERN.exec(text)
  if (!matched) return null
  const [, yearText, monthText, dayText, hourText, minuteText, secondText] = matched
  const year = Number(yearText)
  const month = Number(monthText ?? 1)
  const day = Number(dayText ?? 1)
  const date = dayjs(
    new Date(
      year,
      month - 1,
      day,
      Number(hourText ?? 0),
      Number(minuteText ?? 0),
      Number(secondText ?? 0)
    )
  )
  const valid =
    date.isValid() &&
    date.year() === year &&
    date.month() === month - 1 &&
    date.date() === day &&
    date.toDate() >= minDate &&
    date.toDate() < maxDate
  return valid ? { date, hasTime: hourText !== undefined } : null
}

const normalizePastedDate = (date: Dayjs, hasTime: boolean, isEnd: boolean) => {
  const type = config.value.timeGranularityMultiple
  if (type === 'datetimerange') {
    if (hasTime) return date
    return isEnd ? date.endOf('day') : date.startOf('day')
  }
  const unit: 'year' | 'month' | 'day' =
    type === 'yearrange' ? 'year' : type === 'monthrange' ? 'month' : 'day'
  return isEnd ? date.endOf(unit) : date.startOf(unit)
}

const exceedsQueryWindow = (other: Dayjs, value: Dayjs) => {
  const { dynamicWindow, maximumSingleQuery } = config.value.timeRange || {}
  if (!dynamicWindow || !maximumSingleQuery) return false
  const unit = queryTimeType.value
  const upper = other.clone().add(maximumSingleQuery, unit).startOf(unit).valueOf()
  const lower = other.clone().subtract(maximumSingleQuery, unit).startOf(unit).valueOf()
  const timestamp = value.valueOf()
  return timestamp > upper || timestamp < lower
}

const handleRangePaste = (event: ClipboardEvent) => {
  const target = event.target
  if (!(target instanceof HTMLInputElement)) return
  const container = target.closest('.ed-date-editor')
  const inputs = Array.from(
    container?.querySelectorAll<HTMLInputElement>('input.ed-range-input') || []
  )
  const index = inputs.indexOf(target)
  if (index < 0) return
  const text = event.clipboardData?.getData('text')?.trim()
  if (!text) return
  const parsed = parsePastedDate(text)
  if (!parsed) return
  const isEnd = index === 1
  const value = normalizePastedDate(parsed.date, parsed.hasTime, isEnd)
  if (disabledDate(value.toDate())) return
  const current = Array.isArray(selectValue.value) ? [...selectValue.value] : [null, null]
  const otherRaw = current[isEnd ? 0 : 1]
  const other = otherRaw ? dayjs(otherRaw) : null
  if (other?.isValid() && (isEnd ? value.isBefore(other) : value.isAfter(other))) return
  if (other?.isValid() && exceedsQueryWindow(other, value)) return
  current[isEnd ? 1 : 0] = value.format('YYYY/MM/DD HH:mm:ss')
  selectValue.value = current
  if (current[0] && current[1]) {
    handleValueChange()
  }
}

const handleRangeKeydown = (event: KeyboardEvent) => {
  if (event.metaKey || event.ctrlKey || event.altKey) return
  if (event.key.length === 1 || event.key === 'Backspace' || event.key === 'Delete') {
    event.preventDefault()
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
const showDateQuick = ref(false)
const showQuick = () => {
  showDateQuick.value = true
}

const emitMobile = (_, val) => {
  const [start, end] = val
  selectValue.value = [start?.format('YYYY/MM/DD HH:mm:ss'), end?.format('YYYY/MM/DD HH:mm:ss')]
  handleValueChange()
  showDateQuick.value = false
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
  <div
    v-if="multiple"
    class="range-paste-editor"
    @paste.prevent="handleRangePaste"
    @keydown="handleRangeKeydown"
  >
    <!-- 保持可编辑以接收 paste 事件，键盘输入由 handleRangeKeydown 拦截 -->
    <el-date-picker
      v-model="selectValue"
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
      :editable="true"
      :range-separator="$t('cron.to')"
      :start-placeholder="placeholderText"
      :end-placeholder="placeholderText"
    />
  </div>
  <el-date-picker
    v-else
    :key="config.timeGranularity + 1"
    v-model="selectValue"
    class="icon-fixed_16"
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
  <div v-if="dvMainStore.mobileInPc && isRange" class="vant-mobile wr50" @click="showPopupRight">
    <div class="quick-selection" @click.stop="showQuick"></div>
    <van-popup teleport="body" position="bottom" v-model:show="showDateQuick">
      <div
        @click="ele.onClick({ emit: emitMobile })"
        class="shortcuts-mobile"
        v-for="ele in shortcuts"
        :key="ele.text"
      >
        {{ ele.text }}
      </div></van-popup
    >
  </div>
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
.range-paste-editor {
  display: contents;
}
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

    .quick-selection {
      position: absolute;
      top: 0px;
      right: 10px;
      width: 24px;
      height: 32px;
      z-index: 10;
    }
  }
}
.shortcuts-mobile {
  padding: 10px;
  text-align: center;
  border-bottom: 1px solid #eee;
}
.icon-fixed_16 {
  .ed-input__icon {
    font-size: 16px !important;
  }
}
</style>
