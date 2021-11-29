import { sin, cos } from '@/components/canvas/utils/translate'

export function getStyle(style, filter = []) {
  const needUnit = [
    'fontSize',
    'width',
    'height',
    'top',
    'left',
    'borderWidth',
    'letterSpacing',
    'borderRadius',
    'margin',
    'padding'
  ]

  const result = {}
  Object.keys(style).forEach(key => {
    if (!filter.includes(key)) {
      if (key !== 'rotate') {
        result[key] = style[key]
        if (key) {
          if (key === 'backgroundColor') {
            result[key] = colorRgb(style[key], style.opacity)
          }
          if (needUnit.includes(key)) {
            result[key] += 'px'
          }
        }
      } else {
        result.transform = key + '(' + style[key] + 'deg)'
      }
    }
  })
  if (result.backgroundColor && (result.opacity || result.opacity === 0)) {
    delete result.opacity
  }
  return result
}

// 获取一个组件旋转 rotate 后的样式
export function getComponentRotatedStyle(style) {
  style = { ...style }
  if (style.rotate !== 0) {
    const newWidth = style.width * cos(style.rotate) + style.height * sin(style.rotate)
    const diffX = (style.width - newWidth) / 2 // 旋转后范围变小是正值，变大是负值
    style.left += diffX
    style.right = style.left + newWidth

    const newHeight = style.height * cos(style.rotate) + style.width * sin(style.rotate)
    const diffY = (newHeight - style.height) / 2 // 始终是正
    style.top -= diffY
    style.bottom = style.top + newHeight

    style.width = newWidth
    style.height = newHeight
  } else {
    style.bottom = style.top + style.height
    style.right = style.left + style.width
  }

  return style
}

export function colorRgb(color, opacity) {
  var reg = /^#([0-9a-fA-f]{3}|[0-9a-fA-f]{6})$/
  var sColor = color
  if (sColor && reg.test(sColor)) {
    sColor = sColor.toLowerCase()
    if (sColor.length === 4) {
      var sColorNew = '#'
      for (var i = 1; i < 4; i += 1) {
        sColorNew += sColor.slice(i, i + 1).concat(sColor.slice(i, i + 1))
      }
      sColor = sColorNew
    }
    // 处理六位的颜色值
    var sColorChange = []
    for (let i = 1; i < 7; i += 2) {
      sColorChange.push(parseInt('0x' + sColor.slice(i, i + 2)))
    }
    if (opacity || opacity === 0) {
      return 'rgba(' + sColorChange.join(',') + ',' + opacity + ')'
    } else {
      return 'rgba(' + sColorChange.join(',') + ')'
    }
  } else {
    return sColor
  }
}
