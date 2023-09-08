export const buildParam = (conditions, keyword) => {
  const result = {}
  conditions.forEach(condition => {
    const key = condition.field
    const val = condition.value
    const keyArray = key.split('.')
    const len = keyArray.length
    const fieldKey = keyArray[len - 1]
    result[formatField(fieldKey)] = val
  })
  if (keyword === 0 || !!keyword) {
    result['keyword'] = keyword
  }
  return result
}

const formatField = fieldText => {
  const array = fieldText.split('_')
  let result = ''
  for (let index = 0; index < array.length; index++) {
    let temp = array[index]
    if (index) {
      temp = temp.substr(0, 1).toLocaleUpperCase() + temp.substr(1)
    }
    result += temp
  }
  return result
}
