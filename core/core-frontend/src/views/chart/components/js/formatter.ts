import { find } from 'lodash-es'
import { useI18n } from '@/hooks/web/useI18n'
import { getLocale } from '@/utils/utils'
const { t } = useI18n()

export const isEnLocal = !['zh', 'zh-cn', 'zh-CN', 'tw'].includes(getLocale())

export const formatterItem = {
  type: 'auto', // auto,value,percent
  unitLanguage: isEnLocal ? 'en' : 'ch',
  unit: 1, // 换算单位
  suffix: '', // 单位后缀
  decimalCount: 2, // 小数位数
  thousandSeparator: true // 千分符
}

// 单位list
export const unitType = [
  { name: t('chart.unit_none'), value: 1 },
  { name: t('chart.unit_thousand'), value: 1000 },
  { name: t('chart.unit_ten_thousand'), value: 10000 },
  { name: t('chart.unit_million'), value: 1000000 },
  { name: t('chart.unit_hundred_million'), value: 100000000 }
]
export const unitEnType = [
  { name: 'None', value: 1 },
  { name: 'Thousand (K)', value: 1000 },
  { name: 'Million (M)', value: 1000000 },
  { name: 'Billion (B)', value: 1000000000 }
]

export function getUnitTypeList(lang) {
  if (isEnLocal) {
    return unitEnType
  }
  if (lang === 'ch') {
    return unitType
  }
  return unitEnType
}

export function getUnitTypeValue(lang, value) {
  const list = getUnitTypeList(lang)
  const item = find(list, l => l.value === value)
  if (item) {
    return value
  }
  return 1
}

export function initFormatCfgUnit(cfg) {
  if (cfg && cfg.unitLanguage === undefined) {
    cfg.unitLanguage = 'ch'
  }
  if (cfg && isEnLocal) {
    cfg.unitLanguage = 'en'
  }
  onChangeFormatCfgUnitLanguage(cfg, cfg.unitLanguage)
}

export function onChangeFormatCfgUnitLanguage(cfg, lang) {
  cfg.unit = getUnitTypeValue(lang, cfg.unit)
}

// 格式化方式
export const formatterType = [
  { name: 'value_formatter_auto', value: 'auto' },
  { name: 'value_formatter_value', value: 'value' },
  { name: 'value_formatter_percent', value: 'percent' }
]

export function valueFormatter(value, formatter) {
  if (value === null || value === undefined) {
    return null
  }
  // 1.unit 2.decimal 3.thousand separator and suffix
  let result
  if (formatter.type === 'auto') {
    result = transSeparatorAndSuffix(transUnit(value, formatter), formatter)
  } else if (formatter.type === 'value') {
    result = transSeparatorAndSuffix(
      transDecimal(transUnit(value, formatter), formatter),
      formatter
    )
  } else if (formatter.type === 'percent') {
    value = value * 100
    result = transSeparatorAndSuffix(transDecimal(value, formatter), formatter)
  } else {
    result = value
  }
  return result
}

function transUnit(value, formatter) {
  initFormatCfgUnit(formatter)
  return value / formatter.unit
}

function transDecimal(value, formatter) {
  const resultV = retain(value, formatter.decimalCount) as string
  if (Object.is(parseFloat(resultV), -0)) {
    return resultV.slice(1)
  }
  return resultV
}

function retain(value, n) {
  if (!n) return Math.round(value)
  const tran = Math.round(value * Math.pow(10, n)) / Math.pow(10, n)
  let tranV = tran.toString()
  const newVal = tranV.indexOf('.')
  if (newVal < 0) {
    tranV += '.'
  }
  for (let i = tranV.length - tranV.indexOf('.'); i <= n; i++) {
    tranV += '0'
  }
  return tranV
}

function transSeparatorAndSuffix(value, formatter) {
  let str = value + ''
  if (str.match(/^(\d)(\.\d)?e-(\d)/)) {
    str = value.toFixed(18).replace(/\.?0+$/, '')
  }
  if (formatter.thousandSeparator) {
    const thousandsReg = /(\d)(?=(\d{3})+$)/g
    const numArr = str.split('.')
    numArr[0] = numArr[0].replace(thousandsReg, '$1,')
    str = numArr.join('.')
  }
  if (formatter.type === 'percent') {
    str += '%'
    //百分比没有后缀，直接返回
    return str
  } else {
    const unit = formatter.unit

    if (formatter.unitLanguage === 'ch') {
      if (unit === 1000) {
        str += t('chart.unit_thousand')
      } else if (unit === 10000) {
        str += t('chart.unit_ten_thousand')
      } else if (unit === 1000000) {
        str += t('chart.unit_million')
      } else if (unit === 100000000) {
        str += t('chart.unit_hundred_million')
      }
    } else {
      if (unit === 1000) {
        str += 'K'
      } else if (unit === 1000000) {
        str += 'M'
      } else if (unit === 1000000000) {
        str += 'B'
      }
    }
  }
  return str + formatter.suffix.replace(/(^\s*)|(\s*$)/g, '')
}
