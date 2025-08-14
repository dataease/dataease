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
