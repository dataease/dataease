export const formatterItem = {
  type: 'auto', // auto,value,percent
  unit: 1, // 换算单位
  suffix: '', // 单位后缀
  decimalCount: 2, // 小数位数
  thousandSeparator: true// 千分符
}

// 单位list
export const unitList = [
  { name: 'unit_none', value: 1 },
  { name: 'unit_thousand', value: 1000 },
  { name: 'unit_ten_thousand', value: 10000 },
  { name: 'unit_million', value: 1000000 },
  { name: 'unit_hundred_million', value: 100000000 }
]

// 格式化方式
export const formatterType = [
  { name: 'value_formatter_auto', value: 'auto' },
  { name: 'value_formatter_value', value: 'value' },
  { name: 'value_formatter_percent', value: 'percent' }
]

export function valueFormatter(value, formatter) {
  // 1.unit 2.decimal 3.thousand separator and suffix
  let result
  if (formatter.type === 'auto') {
    result = transSeparatorAndSuffix(transUnit(value, formatter), formatter)
  } else if (formatter.type === 'value') {
    result = transSeparatorAndSuffix(transDecimal(transUnit(value, formatter), formatter), formatter)
  } else if (formatter.type === 'percent') {
    value = value * 100
    result = transSeparatorAndSuffix(transDecimal(value, formatter), formatter)
  } else {
    result = value
  }
  return result
}

function transUnit(value, formatter) {
  return value / formatter.unit
}

function transDecimal(value, formatter) {
  return value.toFixed(formatter.decimalCount)
}

function transSeparatorAndSuffix(value, formatter) {
  let str = value + ''
  if (formatter.thousandSeparator) {
    const thousandsReg = /(\d)(?=(\d{3})+$)/g
    const numArr = str.split('.')
    numArr[0] = numArr[0].replace(thousandsReg, '$1,')
    str = numArr.join('.')
  }
  if (formatter.type === 'percent') {
    str += '%'
  } else {
    if (formatter.unit === 1000) {
      str += '千'
    } else if (formatter.unit === 10000) {
      str += '万'
    } else if (formatter.unit === 1000000) {
      str += '百万'
    } else if (formatter.unit === 100000000) {
      str += '亿'
    }
  }
  return str + formatter.suffix.replace(/(^\s*)|(\s*$)/g, '')
}
