import { cos, sin } from '@/utils/translate'
import {
  DEFAULT_COLOR_CASE,
  DEFAULT_COLOR_CASE_DARK
} from '@/views/chart/components/editor/util/chart'

import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { useEmitt } from '@/hooks/web/useEmitt'
import { merge } from 'lodash-es'
const dvMainStore = dvMainStoreWithOut()

export const LIGHT_THEME_COLOR_MAIN = '#000000'
export const LIGHT_THEME_COLOR_SLAVE1 = '#CCCCCC'
export const LIGHT_THEME_DASHBOARD_BACKGROUND = '#f5f6f7'
export const LIGHT_THEME_COMPONENT_BACKGROUND = '#FFFFFF'

export const DARK_THEME_COLOR_MAIN = '#FFFFFF'
export const DARK_THEME_COLOR_SLAVE1 = '#858383'
export const DARK_THEME_DASHBOARD_BACKGROUND = '#030B2E'
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
        result['transform'] = key + '(' + style[key] + 'deg)'
      }
    }
  })
  if (result['backgroundColor'] && (result['opacity'] || result['opacity'] === 0)) {
    delete result['opacity']
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
  const reg = /^#([0-9a-fA-f]{3}|[0-9a-fA-f]{6})$/
  let sColor = color
  if (sColor && reg.test(sColor)) {
    sColor = sColor.toLowerCase()
    if (sColor.length === 4) {
      let sColorNew = '#'
      for (let i = 1; i < 4; i += 1) {
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
  basicStyle: ['barWidth', 'lineWidth', 'lineSymbolSize'],
  tableHeader: ['tableTitleFontSize', 'tableTitleHeight'],
  tableCell: ['tableItemFontSize', 'tableItemHeight'],
  misc: [
    'nameFontSize',
    'valueFontSize',
    'spaceSplit', // 间隔
    'scatterSymbolSize', // 气泡大小，散点图
    'radarSize' // 雷达占比
  ],
  label: ['fontSize'],
  tooltip: ['fontSize'],
  indicator: ['fontSize', 'suffixFontSize'],
  indicatorName: ['fontSize']
}
export const customStyleTrans = {
  text: ['fontSize'],
  legend: ['fontSize'],
  xAxis: {
    fontSize: 'fontSize',
    axisLabel: ['fontSize'],
    splitLine: {
      lineStyle: ['width']
    }
  },
  yAxis: {
    fontSize: 'fontSize',
    axisLabel: ['fontSize'],
    splitLine: {
      lineStyle: ['width']
    }
  },
  yAxisExt: {
    fontSize: 'fontSize',
    axisLabel: ['fontSize'],
    splitLine: {
      lineStyle: ['width']
    }
  },
  misc: {
    fontSize: 'fontSize',
    axisLine: {
      lineStyle: ['width']
    },
    axisTick: {
      lineStyle: ['width']
    },
    axisLabel: ['margin', 'fontSize'],
    splitLine: {
      lineStyle: ['width']
    }
  }
}

export const THEME_STYLE_TRANS_MAIN_BACK = {
  legend: {
    textStyle: ['color']
  },
  xAxis: {
    nameTextStyle: ['color'],
    axisLabel: ['color'],
    splitLine: {
      lineStyle: ['color']
    }
  },
  yAxis: {
    nameTextStyle: ['color'],
    axisLabel: ['color'],
    splitLine: {
      lineStyle: ['color']
    }
  },
  yAxisExt: {
    nameTextStyle: ['color'],
    axisLabel: ['color'],
    splitLine: {
      lineStyle: ['color']
    }
  },
  split: {
    name: ['color'],
    axisLine: {
      lineStyle: ['color']
    },
    axisTick: {
      lineStyle: ['color']
    },
    axisLabel: ['color'],
    splitLine: {
      lineStyle: ['color']
    }
  }
}

export const THEME_STYLE_TRANS_MAIN = {
  legend: ['color'],
  xAxis: {
    // 一级属性直接字符串
    color: 'color',
    axisLabel: ['color']
  },
  yAxis: {
    color: '',
    axisLabel: ['color']
  },
  yAxisExt: {
    color: '',
    axisLabel: ['color']
  },
  misc: {
    color: 'color',
    axisTick: {
      lineStyle: ['color']
    },
    axisLabel: ['color']
  }
}

export const THEME_STYLE_TRANS_SLAVE1 = {
  xAxis: {
    splitLine: {
      lineStyle: ['color']
    }
  },
  yAxis: {
    splitLine: {
      lineStyle: ['color']
    }
  },
  yAxisExt: {
    splitLine: {
      lineStyle: ['color']
    }
  },
  misc: {
    splitLine: {
      lineStyle: ['color']
    },
    axisLine: {
      lineStyle: ['color']
    }
  }
}

export const THEME_ATTR_TRANS_MAIN = {
  label: ['color'],
  tooltip: ['color'],
  indicatorName: ['color']
}

export const THEME_ATTR_TRANS_MAIN_SYMBOL = {
  label: ['color']
}

export const THEME_ATTR_TRANS_SLAVE1_BACKGROUND = {
  tooltip: ['backgroundColor']
}

// 移动端特殊属性
export const mobileSpecialProps = {
  lineWidth: 2, // 线宽固定值
  lineSymbolSize: 8 // 折点固定值
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
            infoObj[templateKey][templateProp] = getScaleValue(
              infoObj[templateKey][templateProp],
              scale
            )
          }
        }
      })
    } else if (typeof template[templateKey] === 'string') {
      // 一级字段为字符串直接赋值
      infoObj[templateKey] = getScaleValue(infoObj[templateKey], scale)
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
          infoObj[templateKey][templateProp] = color
        }
      })
    } else if (typeof template[templateKey] === 'string') {
      // 一级字段为字符串直接赋值
      infoObj[templateKey] = color
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
  recursionTransObj(this.customAttrTrans, chartInfo.customAttr, scale, null)
  // style 缩放转换
  recursionTransObj(this.customStyleTrans, chartInfo.customStyle, scale, null)
  return chartInfo
}

export function adaptCurTheme(customStyle, customAttr) {
  const canvasStyle = dvMainStore.canvasStyleData
  const themeColor = canvasStyle.dashboard.themeColor
  if (themeColor === 'light') {
    recursionThemTransObj(THEME_STYLE_TRANS_MAIN, customStyle, LIGHT_THEME_COLOR_MAIN)
    recursionThemTransObj(THEME_STYLE_TRANS_SLAVE1, customStyle, LIGHT_THEME_COLOR_SLAVE1)
    recursionThemTransObj(THEME_ATTR_TRANS_MAIN, customAttr, LIGHT_THEME_COLOR_MAIN)
    recursionThemTransObj(
      THEME_ATTR_TRANS_SLAVE1_BACKGROUND,
      customAttr,
      LIGHT_THEME_COMPONENT_BACKGROUND
    )
    merge(customAttr, DEFAULT_COLOR_CASE, canvasStyle.component.chartColor)
  } else {
    recursionThemTransObj(THEME_STYLE_TRANS_MAIN, customStyle, DARK_THEME_COLOR_MAIN)
    recursionThemTransObj(THEME_STYLE_TRANS_SLAVE1, customStyle, DARK_THEME_COLOR_SLAVE1)
    recursionThemTransObj(THEME_ATTR_TRANS_MAIN, customAttr, DARK_THEME_COLOR_MAIN)
    recursionThemTransObj(
      THEME_ATTR_TRANS_SLAVE1_BACKGROUND,
      customAttr,
      DARK_THEME_COMPONENT_BACKGROUND_BACK
    )
    merge(customAttr, DEFAULT_COLOR_CASE_DARK, canvasStyle.component.chartColor)
  }
  customStyle['text'] = {
    ...canvasStyle.component.chartTitle,
    title: customStyle['text']['title'],
    show: customStyle['text']['show'],
    remarkShow: customStyle['text']['remarkShow'],
    remark: customStyle['text']['remark']
  }
}

export function adaptCurThemeCommonStyle(component) {
  if (['DeTabs'].includes(component.component)) {
    component.commonBackground['innerPadding'] = 0
  }
  // 背景融合-Begin 如果是大屏['CanvasBoard', 'CanvasIcon', 'Picture']组件不需要设置背景
  if (
    dvMainStore.dvInfo.type === 'dataV' &&
    ['CanvasBoard', 'CanvasIcon', 'Picture', 'Group'].includes(component.component)
  ) {
    component.commonBackground['backgroundColorSelect'] = false
    component.commonBackground['innerPadding'] = 0
  } else {
    const commonStyle = dvMainStore.canvasStyleData.component.chartCommonStyle
    for (const key in commonStyle) {
      component.commonBackground[key] = commonStyle[key]
    }
  }
  // 背景融合-End
  // 通用样式-Begin
  if (component.style.color) {
    if (dvMainStore.canvasStyleData.dashboard.themeColor === 'light') {
      component.style.color = LIGHT_THEME_COLOR_MAIN
    } else {
      component.style.color = DARK_THEME_COLOR_MAIN
    }
  }
  if (component.component === 'UserView') {
    // 图表-Begin
    const curViewInfo = dvMainStore.canvasViewInfo[component.id]
    adaptCurTheme(curViewInfo.customStyle, curViewInfo.customAttr)
    useEmitt().emitter.emit('renderChart-' + component.id, curViewInfo)
    // 图表-Begin
  } else if (component.component === 'Group') {
    component.propValue.forEach(groupItem => {
      adaptCurThemeCommonStyle(groupItem)
    })
  } else if (component.component === 'DeTabs') {
    if (dvMainStore.canvasStyleData.dashboard.themeColor === 'light') {
      component.style.headFontColor = LIGHT_THEME_COLOR_MAIN
      component.style.headFontActiveColor = LIGHT_THEME_COLOR_MAIN
    } else {
      component.style.headFontColor = DARK_THEME_COLOR_MAIN
      component.style.headFontActiveColor = DARK_THEME_COLOR_MAIN
    }
    component.propValue.forEach(tabItem => {
      tabItem.componentData.forEach(tabComponent => {
        adaptCurThemeCommonStyle(tabComponent)
      })
    })
  } else if (component.component === 'VQuery') {
    const viewInfo = dvMainStore.canvasViewInfo[component.id]
    if (viewInfo) {
      adaptCurThemeFilterStyleAllKeyComponent(viewInfo)
    }
  }

  return component
}

export function adaptCurThemeCommonStyleAll() {
  const componentData = dvMainStore.componentData
  componentData.forEach(item => {
    adaptCurThemeCommonStyle(item)
  })
}

interface CanvasViewInfo {
  type: string
  customStyle: {
    component: object
  }
}

const colors = ['labelColor', 'borderColor', 'text', 'bgColor']
const colorsSwitch = ['labelColorShow', 'borderShow', 'textColorShow', 'bgColorShow']

export function adaptCurThemeFilterStyleAllKeyComponent(component) {
  if (isFilterComponent(component.type)) {
    const filterStyle = dvMainStore.canvasStyleData.component.filterStyle
    colors.forEach(styleKey => {
      component.customStyle.component[styleKey] = filterStyle[styleKey]
      const index = colors.indexOf(styleKey)
      if (index !== -1) {
        component.customStyle.component[colorsSwitch[index]] = true
      }
    })
  }
}

export function adaptCurThemeFilterStyleAll(styleKey) {
  const componentViewData = Object.values(dvMainStore.canvasViewInfo) as CanvasViewInfo[]
  const filterStyle = dvMainStore.canvasStyleData.component.filterStyle
  componentViewData.forEach(item => {
    if (isFilterComponent(item.type)) {
      item.customStyle.component[styleKey] = filterStyle[styleKey]
      const index = colors.indexOf(styleKey)
      if (index !== -1) {
        item.customStyle.component[colorsSwitch[index]] = true
      }
    }
  })
}

export function isFilterComponent(component) {
  return ['VQuery'].includes(component)
}

export function isTabComponent(component) {
  return ['DeTabs'].includes(component)
}
