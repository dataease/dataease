import { sin, cos } from '@/components/canvas/utils/translate'
import store from '@/store'
import Vue from 'vue'

export const LIGHT_THEME_COLOR_MAIN = '#000000'
export const LIGHT_THEME_COLOR_SLAVE1 = '#CCCCCC'
export const LIGHT_THEME_PANEL_BACKGROUND = '#F1F3F5'
export const LIGHT_THEME_COMPONENT_BACKGROUND = '#FFFFFF'

export const DARK_THEME_COLOR_MAIN = '#FFFFFF'
export const DARK_THEME_COLOR_SLAVE1 = '#CCCCCC'
export const DARK_THEME_PANEL_BACKGROUND = '#030B2E'
export const DARK_THEME_COMPONENT_BACKGROUND = '#131E42'
export const DARK_THEME_COMPONENT_BACKGROUND_BACK = '#5a5c62'

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
          if (key === 'fontSize' && result[key] < 12) {
            result[key] = 12
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
  'yAxisExt': {
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

export const THEME_STYLE_TRANS_MAIN_BACK = {
  'legend': {
    'textStyle': ['color']
  },
  'xAxis': {
    'nameTextStyle': ['color'],
    'axisLabel': ['color'],
    'splitLine': {
      'lineStyle': ['color']
    }
  },
  'yAxis': {
    'nameTextStyle': ['color'],
    'axisLabel': ['color'],
    'splitLine': {
      'lineStyle': ['color']
    }
  },
  'yAxisExt': {
    'nameTextStyle': ['color'],
    'axisLabel': ['color'],
    'splitLine': {
      'lineStyle': ['color']
    }
  },
  'split': {
    'name': ['color'],
    'axisLine': {
      'lineStyle': ['color']
    },
    'axisTick': {
      'lineStyle': ['color']
    },
    'axisLabel': ['color'],
    'splitLine': {
      'lineStyle': ['color']
    }
  }
}

export const THEME_STYLE_TRANS_MAIN = {
  'legend': {
    'textStyle': ['color']
  },
  'xAxis': {
    'nameTextStyle': ['color'],
    'axisLabel': ['color']
  },
  'yAxis': {
    'nameTextStyle': ['color'],
    'axisLabel': ['color']
  },
  'yAxisExt': {
    'nameTextStyle': ['color'],
    'axisLabel': ['color']
  },
  'split': {
    'name': ['color'],
    'axisTick': {
      'lineStyle': ['color']
    },
    'axisLabel': ['color']
  }
}

export const THEME_STYLE_TRANS_SLAVE1 = {
  'xAxis': {
    'splitLine': {
      'lineStyle': ['color']
    }
  },
  'yAxis': {
    'splitLine': {
      'lineStyle': ['color']
    }
  },
  'yAxisExt': {
    'splitLine': {
      'lineStyle': ['color']
    }
  },
  'split': {
    'splitLine': {
      'lineStyle': ['color']
    },
    'axisLine': {
      'lineStyle': ['color']
    }
  }
}

export const THEME_ATTR_TRANS_MAIN = {
  'label': ['color'],
  'tooltip': {
    'textStyle': ['color']
  }
}

export const THEME_ATTR_TRANS_MAIN_SYMBOL = {
  'label': ['color']
}

export const THEME_ATTR_TRANS_SLAVE1_BACKGROUND = {
  'tooltip': ['backgroundColor']
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
  for (const templateKey in template) {
    // 如果是数组 进行赋值计算
    if (template[templateKey] instanceof Array) {
      template[templateKey].forEach(templateProp => {
        if (infoObj[templateKey] && infoObj[templateKey][templateProp]) {
          // 移动端特殊属性值设置
          if (terminal === 'mobile' && mobileSpecialProps[templateProp] !== undefined) {
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

export function recursionThemTransObj(template, infoObj, color) {
  for (const templateKey in template) {
    // 如果是数组 进行赋值计算
    if (template[templateKey] instanceof Array) {
      template[templateKey].forEach(templateProp => {
        if (infoObj[templateKey]) {
          Vue.set(infoObj[templateKey], templateProp, color)
        }
      })
    } else {
      // 如果是对象 继续进行递归
      if (infoObj[templateKey]) {
        recursionThemTransObj(template[templateKey], infoObj[templateKey], color)
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

export function adaptCurTheme(customStyle, customAttr, chartType) {
  const canvasStyle = store.state.canvasStyleData
  const themeColor = canvasStyle.panel.themeColor
  if (themeColor === 'light') {
    recursionThemTransObj(THEME_STYLE_TRANS_MAIN, customStyle, LIGHT_THEME_COLOR_MAIN)
    recursionThemTransObj(THEME_STYLE_TRANS_SLAVE1, customStyle, LIGHT_THEME_COLOR_SLAVE1)
    recursionThemTransObj(THEME_ATTR_TRANS_MAIN, customAttr, LIGHT_THEME_COLOR_MAIN)
    recursionThemTransObj(THEME_ATTR_TRANS_SLAVE1_BACKGROUND, customAttr, LIGHT_THEME_COMPONENT_BACKGROUND)
    if (chartType === 'symbol-map') {
      // 符号地图特殊处理
      Vue.set(customStyle['baseMapStyle'], 'baseMapTheme', 'light')
    }
  } else {
    recursionThemTransObj(THEME_STYLE_TRANS_MAIN, customStyle, DARK_THEME_COLOR_MAIN)
    recursionThemTransObj(THEME_STYLE_TRANS_SLAVE1, customStyle, DARK_THEME_COLOR_SLAVE1)
    if (chartType === 'symbol-map') {
      // 符号地图特殊处理
      Vue.set(customStyle['baseMapStyle'], 'baseMapTheme', 'dark')
      recursionThemTransObj(THEME_ATTR_TRANS_MAIN_SYMBOL, customAttr, '#000000')
    } else {
      recursionThemTransObj(THEME_ATTR_TRANS_MAIN, customAttr, DARK_THEME_COLOR_MAIN)
      recursionThemTransObj(THEME_ATTR_TRANS_SLAVE1_BACKGROUND, customAttr, DARK_THEME_COMPONENT_BACKGROUND_BACK)
    }
  }
  customAttr['color'] = { ...canvasStyle.chartInfo.chartColor }
  customStyle['text'] = { ...canvasStyle.chartInfo.chartTitle, title: customStyle['text']['title'], show: customStyle['text']['show'] }
  if (customStyle.background) {
    delete customStyle.background
  }
}

export function adaptCurThemeCommonStyle(component) {
  const commonStyle = store.state.canvasStyleData.chartInfo.chartCommonStyle
  for (const key in commonStyle) {
    component.commonBackground[key] = commonStyle[key]
  }
  if (isFilterComponent(component.component)) {
    const filterStyle = store.state.canvasStyleData.chartInfo.filterStyle
    for (const styleKey in filterStyle) {
      Vue.set(component.style, styleKey, filterStyle[styleKey])
    }
  } else if (isTabComponent(component.component)) {
    const tabStyle = store.state.canvasStyleData.chartInfo.tabStyle
    for (const styleKey in tabStyle) {
      if(typeof tabStyle[styleKey] === 'string'){
        Vue.set(component.style, styleKey, tabStyle[styleKey])
      }else{
        Vue.set(component.style, styleKey, null)
      }
    }
  } else {
    if (component.style.color) {
      if (store.state.canvasStyleData.panel.themeColor === 'light') {
        component.style.color = LIGHT_THEME_COLOR_MAIN
      } else {
        component.style.color = DARK_THEME_COLOR_MAIN
      }
    }
  }
  return component
}

export function adaptCurThemeCommonStyleAll() {
  const componentData = store.state.componentData
  componentData.forEach((item) => {
    adaptCurThemeCommonStyle(item)
    if (item.style.backgroundColor) {
      delete item.style.backgroundColor
    }
  })
}

export function adaptCurThemeFilterStyleAll(styleKey) {
  const componentData = store.state.componentData
  const filterStyle = store.state.canvasStyleData.chartInfo.filterStyle
  componentData.forEach((item) => {
    if (isFilterComponent(item.component)) {
      Vue.set(item.style, styleKey, filterStyle[styleKey])
    }
  })
}

export function isFilterComponent(component) {
  return ['de-select', 'de-select-grid', 'de-date', 'de-input-search', 'de-number-range', 'de-select-tree'].includes(component)
}

export function isTabComponent(component) {
  return ['de-tabs'].includes(component)
}

