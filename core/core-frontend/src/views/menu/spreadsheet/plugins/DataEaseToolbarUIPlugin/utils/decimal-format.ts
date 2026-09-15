import { numfmt } from '@univerjs/core'

const digitTypes = new Set(['zero', 'hash', 'qmark'])
const numericTypes = new Set([
  ...digitTypes,
  'point',
  'group',
  'scale',
  'percent',
  'exp',
  'condition',
  'break',
  'minus',
  'plus',
  'paren',
  'space',
  'color',
  'general'
])

// 按格式 token 修改小数部分，避免误改引号内的单位、转义字符和科学计数法指数。
export const adjustDecimalFormat = (pattern: string, value: number, delta: 1 | -1): string => {
  const info = numfmt.getFormatInfo(pattern)
  if (info.isDate || info.isText || info.type === 'fraction') return pattern
  const tokens = numfmt.tokenize(pattern)
  const numericPattern = tokens
    .filter(token => numericTypes.has(token.type))
    .map(token => token.raw)
    .join('')
  const displayed = numfmt.format(numericPattern || 'General', value)
  const fractionLength = /\.(\d+)/.exec(displayed)?.[1].length ?? 0
  const exponentValue =
    info.type === 'general' ? Number(/[Ee]([+-]?\d+)/.exec(displayed)?.[1] ?? 0) : 0
  const displayedDecimals = Math.max(0, fractionLength - exponentValue)
  const sections: (typeof tokens)[] = [[]]
  tokens.forEach(token => {
    if (token.type === 'break') sections.push([])
    else sections[sections.length - 1].push(token)
  })
  return sections
    .map(section => {
      if (section.some(token => token.type === 'general')) {
        const count = Math.min(30, Math.max(0, displayedDecimals + delta))
        return section
          .map(token =>
            token.type === 'general' ? `0${count ? `.${'0'.repeat(count)}` : ''}` : token.raw
          )
          .join('')
      }
      const exponent = section.findIndex(token => token.type === 'exp')
      const end = exponent < 0 ? section.length : exponent
      const point = section.findIndex((token, index) => index < end && token.type === 'point')
      let lastDigit = -1
      for (let index = 0; index < (point < 0 ? end : point); index++) {
        if (digitTypes.has(section[index].type)) lastDigit = index
      }
      if (lastDigit < 0) return section.map(token => token.raw).join('')
      let decimalEnd = point + 1
      if (point >= 0) {
        while (decimalEnd < end && digitTypes.has(section[decimalEnd].type)) decimalEnd++
      }
      const decimals = point < 0 ? [] : section.slice(point + 1, decimalEnd)
      const current = decimals.some(token => token.type !== 'zero')
        ? displayedDecimals
        : decimals.length
      const count = Math.min(30, Math.max(0, current + delta))
      const replacement = count ? `.${'0'.repeat(count)}` : ''
      const start = point < 0 ? lastDigit + 1 : point
      const stop = point < 0 ? start : decimalEnd
      return (
        section
          .slice(0, start)
          .map(token => token.raw)
          .join('') +
        replacement +
        section
          .slice(stop)
          .map(token => token.raw)
          .join('')
      )
    })
    .join(';')
}
