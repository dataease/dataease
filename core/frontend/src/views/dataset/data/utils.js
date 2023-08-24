export function getFieldName(fields, name) {
  let n = name
  n = n + '_copy'
  for (let i = 0; i < fields.length; i++) {
    const field = fields[i]
    if (field.name === n) {
      n = getFieldName(fields, n)
    }
  }
  return n
}
