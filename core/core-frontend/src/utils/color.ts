function rgbToHex(r, g, b) {
  // 确保数值在0-255范围内
  r = Math.max(0, Math.min(255, r))
  g = Math.max(0, Math.min(255, g))
  b = Math.max(0, Math.min(255, b))

  // 转换为16进制并补零
  const hexR = r.toString(16).padStart(2, '0')
  const hexG = g.toString(16).padStart(2, '0')
  const hexB = b.toString(16).padStart(2, '0')

  return `#${hexR}${hexG}${hexB}`.toUpperCase()
}
function rgbaToHex(r, g, b, a) {
  // 处理RGB部分
  const hexR = Math.max(0, Math.min(255, r)).toString(16).padStart(2, '0')
  const hexG = Math.max(0, Math.min(255, g)).toString(16).padStart(2, '0')
  const hexB = Math.max(0, Math.min(255, b)).toString(16).padStart(2, '0')

  // 处理透明度（可选）
  const hexA =
    a !== undefined
      ? Math.round(Math.max(0, Math.min(1, a)) * 255)
          .toString(16)
          .padStart(2, '0')
      : ''

  return `#${hexR}${hexG}${hexB}${hexA}`.toUpperCase()
}

export function colorStringToHex(colorStr) {
  // 提取颜色值
  const rgbRegex =
    /^(rgb|rgba)\(\s*(\d{1,3})\s*,\s*(\d{1,3})\s*,\s*(\d{1,3})\s*(?:,\s*(\d+(?:\.\d+)?)\s*)?\)$/
  const match = colorStr.match(rgbRegex)
  if (!match) return null

  const r = parseInt(match[2])
  const g = parseInt(match[3])
  const b = parseInt(match[4])
  const a = match[5] ? parseFloat(match[5]) : undefined

  return a !== undefined ? rgbaToHex(r, g, b, a) : rgbToHex(r, g, b)
}

export function getCSSVariable(element = document.body, property = '--ed-color-primary') {
  const style = window.getComputedStyle(element)
  return style.getPropertyValue(property) || '#3370FF'
}

export const mixColor = (color1: string, color2: string, weight: number) => {
  weight = Math.max(Math.min(Number(weight), 100), 0) / 100
  color1 = color1.replace('#', '')
  color2 = color2.replace('#', '')
  const r = Math.round(
    parseInt(color1.substring(0, 2), 16) * weight +
      parseInt(color2.substring(0, 2), 16) * (1 - weight)
  )
  const g = Math.round(
    parseInt(color1.substring(2, 4), 16) * weight +
      parseInt(color2.substring(2, 4), 16) * (1 - weight)
  )
  const b = Math.round(
    parseInt(color1.substring(4, 6), 16) * weight +
      parseInt(color2.substring(4, 6), 16) * (1 - weight)
  )
  return `#${r.toString(16).padStart(2, '0')}${g.toString(16).padStart(2, '0')}${b
    .toString(16)
    .padStart(2, '0')}`
}
