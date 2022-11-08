function formatEnum(ele) {
  return {
    value: ele,
    label: `chart.filter_${ele.replace(' ', '_')}`
  }
}

const textEnum = ['eq', 'not_eq', 'like', 'not like', 'null', 'not_null', 'empty', 'not_empty']
const textOptions = textEnum.map(formatEnum)

const dateEnum = ['eq', 'not_eq', 'lt', 'gt', 'le', 'ge']
const dateOptions = dateEnum.map(formatEnum)

const valueEnum = [...dateEnum]
const valueOptions = valueEnum.map(formatEnum)

const fieldEnum = ['text', 'time', 'value', 'value', '', 'location']

export {
  textOptions,
  dateOptions,
  valueOptions,
  fieldEnum
}
