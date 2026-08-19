import { useI18n } from "@/hooks/web/useI18n";
const { t } = useI18n();
function formatEnum(ele) {
  return {
    value: ele,
    label: `chart.filter_${ele.replace(' ', '_')}`
  }
}
const textEnum = ['eq', 'not_eq', 'in', 'not in', 'like', 'not like', 'null', 'not_null', 'empty', 'not_empty']
const textOptions = textEnum.map(formatEnum)

const dateEnum = ['eq', 'not_eq', 'lt', 'gt', 'le', 'ge']
const dateOptions = dateEnum.map(formatEnum)

const valueEnum = [...dateEnum]
const valueOptions = valueEnum.map(formatEnum)
const fieldEnums = ['text', 'time', 'value', 'value', 'value', 'location']
const timeOptions = [
  { value: 'YYYY', label: t('dynamic_time.year') },
  { value: 'YYYY-MM', label: `${t('dynamic_time.year')}-${t('dynamic_time.month')}` },
  { value: 'YYYY-MM-DD', label: `${t('dynamic_time.year')}-${t('dynamic_time.month')}-${t('dynamic_time.date')}` },
  { value: 'HH:mm:ss', label: t('sync_task.hour_minute_second') },
  { value: 'YYYY-MM-DD HH:mm', label: `${t('dynamic_time.year')}-${t('dynamic_time.month')}-${t('dynamic_time.date')} ${t('cron.hour')}:${t('cron.minute')}` },
  { value: 'YYYY-MM-DD HH:mm:ss', label: `${t('dynamic_time.year')}-${t('dynamic_time.month')}-${t('dynamic_time.date')} ${t('sync_task.hour_minute_second')}` },
]

const timeFlagYearOptions = [
  { value: 1, label: t('dynamic_year.current') },
  { value: 2, label: t('dynamic_year.last') },
  { value: 3, label: `${t('threshold.next_time')}${t('dynamic_time.year')}` },
  { value: 9, label: t('commons.custom') }
]

const timeFlagMonthOptions = [
  { value: 1, label: t('dynamic_month.current') },
  { value: 2, label: t('dynamic_month.last') },
  { value: 3, label: `${t('threshold.next_time')}${t('dynamic_time.month')}` },
  { value: 4, label: t('dynamic_time.firstOfYear') },
  { value: 5, label: t('threshold.end_of_year') },
  { value: 9, label: t('commons.custom') }
]

const timeFlagDayOptions = [
  { value: 1, label: t('dynamic_time.today') },
  { value: 2, label: t('dynamic_time.yesterday') },
  { value: 3, label: `${t('threshold.next_time')}${t('dynamic_time.date')}` },
  { value: 4, label: t('dynamic_time.firstOfMonth') },
  { value: 5, label: t('dynamic_time.endOfMonth') },
  { value: 9, label: t('commons.custom') }
]

const timeFlagTimeOptions = [
  { value: 1, label: '当前' },
  { value: 2, label: '1小时前' },
  { value: 3, label: '1小时后' },
  { value: 9, label: t('commons.custom') }
]
const timeShortOptionMap = {
  'YYYY': timeFlagYearOptions,
  'YYYY-MM': timeFlagMonthOptions,
  'YYYY-MM-DD': timeFlagDayOptions,
  'YYYY-MM-DD HH:mm': timeFlagDayOptions,
  'YYYY-MM-DD HH:mm:ss': timeFlagDayOptions,
  'HH:mm:ss': timeFlagTimeOptions
}

const timeUnitOptionMap = {
  'YYYY': [{value: 1, label: t('dynamic_time.year')}],
  'YYYY-MM': [{value: 1, label: t('dynamic_time.year')}, {value: 2, label: t('dynamic_time.month')}],
  'YYYY-MM-DD': [{value: 1, label: t('dynamic_time.year')}, {value: 2, label: t('dynamic_time.month')}, {value: 3, label: t('dynamic_time.date')}],
  'YYYY-MM-DD HH:mm': [{value: 1, label: t('dynamic_time.year')}, {value: 2, label: t('dynamic_time.month')}, {value: 3, label: t('dynamic_time.date')}],
  'YYYY-MM-DD HH:mm:ss': [{value: 1, label: t('dynamic_time.year')}, {value: 2, label: t('dynamic_time.month')}, {value: 3, label: t('dynamic_time.date')}]
}

const dateTypeMap = {
  'YYYY': 'year',
  'YYYY-MM': 'month',
  'YYYY-MM-DD': 'date'
}

const timeSuffixOptions = [
  { value: 1, label: t('threshold.ago') },
  { value: 2, label: t('threshold.later') }
]

const simplePreviewTime = (format, key) => {
  if (!format) {
    format === 'YYYY-MM-DD'
  }
  format = format.replace('YYYY', 'yyyy').replace('-DD', '-dd').replace('HH', 'hh')
  const date = new Date()
  if (format === 'yyyy') {
    switch(key) {
      case 2: 
        date.setFullYear(date.getFullYear() - 1)
        break
      case 3: 
        date.setFullYear(date.getFullYear() + 1)
        break
      default:
        break
    }
  }
  if (format === 'yyyy-MM') {
    switch(key) {
      case 2: 
        date.setMonth(date.getMonth() - 1);
        break
      case 3: 
        date.setMonth(date.getMonth() + 1);
        break
      case 4: 
        date.setMonth(0);
        break
      case 5: 
        date.setMonth(11);
        break
      default:
        break
    }
  }
  if (format.includes('yyyy-MM-dd')) {
    switch(key) {
      case 2: 
        date.setDate(date.getDate() - 1);
        break
      case 3: 
        date.setDate(date.getDate() + 1);
        break
      case 4: 
        date.setDate(1);
        break
      case 5:
        date.setMonth(date.getMonth() + 1)
        date.setDate(0) 
        break
      default:
        break
    }
  }
  if (format === 'hh:mm:ss') {
    switch(key) {
      case 2: 
        date.setHours(date.getHours() - 1);
        break
      case 3: 
        date.setHours(date.getHours() + 1);
        break
      default:
        break
    }
  } else {
    date.setHours(0)
    date.setMinutes(0)
    date.setSeconds(0)
  }
  const result = date.format(format)
  return result
}

const customPreviewTime = (format, form) => {
  const { count, unit, suffix, time } = form
  let timeArray = []
  if (time) {
    timeArray = time.split(':')
  }
  const date = new Date()
  
  if (count) {
    const realCount = suffix === 1 ? (-1 * count) : count
    if (unit === 1) {
      date.setFullYear(date.getFullYear() + realCount)
    } else if (unit === 2) {
      date.setMonth(date.getMonth() + realCount)
    } else if (unit === 3) {
      date.setDate(date.getDate() + realCount)
    }
  }
  const len = timeArray?.length
  if (len) {
    date.setHours(timeArray[0])
    if (len > 1) {
      date.setMinutes(timeArray[1])
      if (len > 2) {
        date.setSeconds(timeArray[2])
      }
    }
  }
  if (!format) {
    format === 'YYYY-MM-DD'
  }
  format = format.replace('YYYY', 'yyyy').replace('-DD', '-dd').replace('HH', 'hh')
  return date.format(format)
}

const getSureTime = (form) => {
  const { format, timeFlag, count, unit, suffix, time } = form
  if (timeFlag === 9) {
    const unitOptions = timeUnitOptionMap[format]
    const unitLabel = unitOptions[unit - 1]['label']
    const suffixLabel = timeSuffixOptions[suffix - 1]['label']
    let timeText = ''
    if (format.includes('HH')) {
      timeText = ' ' + time
    }
    return `${count} ${unitLabel}${suffixLabel}${timeText}`
  } else {
    const shortOptions = timeShortOptionMap[format]
    const shortLabel = shortOptions[timeFlag -1]['label']
    return shortLabel
  }
}

export {
  textOptions,
  dateOptions,
  valueOptions,
  fieldEnums,
  timeOptions,
  timeUnitOptionMap,
  timeShortOptionMap,
  timeSuffixOptions,
  dateTypeMap,
  customPreviewTime,
  simplePreviewTime,
  getSureTime
}
