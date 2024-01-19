import { getScaleValue, mobileSpecialProps } from '@/utils/canvasStyle'

export const customAttrTrans = {
  size: [
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
    'radarSize', // 雷达占比
    'quotaSuffixFontSize'
  ],
  label: ['fontSize'],
  tooltip: {
    textStyle: ['fontSize']
  },
  slider: ['fontSize'],
  graphic: ['fontSize'],
  indicator: ['fontSize', 'suffixFontSize'],
  indicatorName: ['fontSize']
}
export const customStyleTrans = {
  text: ['fontSize'],
  legend: {
    textStyle: ['fontSize']
  },
  xAxis: {
    nameTextStyle: ['fontSize'],
    axisLabel: ['fontSize'],
    splitLine: {
      lineStyle: ['width']
    }
  },
  yAxis: {
    nameTextStyle: ['fontSize'],
    axisLabel: ['fontSize'],
    splitLine: {
      lineStyle: ['width']
    }
  },
  yAxisExt: {
    nameTextStyle: ['fontSize'],
    axisLabel: ['fontSize'],
    splitLine: {
      lineStyle: ['width']
    }
  },
  split: {
    name: ['fontSize'],
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
