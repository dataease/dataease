export function format(uiLists) {
  const result = {}
  uiLists.forEach(element => {
    result[element['paramKey']] = element
  })
  return result
}
