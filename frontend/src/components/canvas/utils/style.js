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
    const sColorChange = []
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

export const customAttrTrans = {
  'size': [
    'barWidth',
    'lineWidth',
    'lineSymbolSize',
    'funnelWidth', // 漏斗图 最大宽度
    'tableTitleFontSize',
    'tableItemFontSize',
    'tableTitleHeight',
    'tableItemHeight',
    'dimensionFontSize',
    'quotaFontSize',
    'spaceSplit', // 间隔
    'scatterSymbolSize', // 气泡大小，散点图
    'treemapWidth', // 矩形树图
    'treemapHeight',
    'radarSize'// 雷达占比
  ],
  'label': [
    'fontSize'
  ],
  'tooltip': {
    'textStyle': ['fontSize']
  }
}
export const customStyleTrans = {
  'text': ['fontSize'],
  'legend': {
    'textStyle': ['fontSize']
  },
  'xAxis': {
    'nameTextStyle': ['fontSize'],
    'axisLabel': ['fontSize'],
    'splitLine': {
      'lineStyle': ['width']
    }
  },
  'yAxis': {
    'nameTextStyle': ['fontSize'],
    'axisLabel': ['fontSize'],
    'splitLine': {
      'lineStyle': ['width']
    }
  },
  'split': {
    'name': ['fontSize'],
    'axisLine': {
      'lineStyle': ['width']
    },
    'axisTick': {
      'lineStyle': ['width']
    },
    'axisLabel': ['margin', 'fontSize'],
    'splitLine': {
      'lineStyle': ['width']
    }
  }
}

// 移动端特殊属性
export const mobileSpecialProps = {
  'lineWidth': 3, // 线宽固定值
  'lineSymbolSize': 5// 折点固定值
}

export function getScaleValue(propValue, scale) {
  const propValueTemp = Math.round(propValue * scale)
  return propValueTemp > 1 ? propValueTemp : 1
}

export function recursionTransObj(template, infoObj, scale, terminal) {
  console.log()
  for (const templateKey in template) {
    // 如果是数组 进行赋值计算
    if (template[templateKey] instanceof Array) {
      template[templateKey].forEach(templateProp => {
        if (infoObj[templateKey] && infoObj[templateKey][templateProp]) {
          // 移动端特殊属性值设置
          if (terminal === 'mobile' && mobileSpecialProps[templateProp] !== undefined) {
            // console.log('mobile:' + templateProp + mobileSpecialProps[templateProp])
            infoObj[templateKey][templateProp] = mobileSpecialProps[templateProp]
          } else {
            infoObj[templateKey][templateProp] = getScaleValue(infoObj[templateKey][templateProp], scale)
          }
        }
      })
    } else {
      // 如果是对象 继续进行递归
      if (infoObj[templateKey]) {
        recursionTransObj(template[templateKey], infoObj[templateKey], scale, terminal)
      }
    }
  }
}

export function componentScalePublic(chartInfo, heightScale, widthScale) {
  const scale = Math.min(heightScale, widthScale)
  // attr 缩放转换
  recursionTransObj(this.customAttrTrans, chartInfo.customAttr, scale)
  // style 缩放转换
  recursionTransObj(this.customStyleTrans, chartInfo.customStyle, scale)
  return chartInfo
}

