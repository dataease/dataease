import { sin, cos, toPercent } from '@/utils/translate'
import { imgUrlTrans } from '@/utils/imgUtils'
import { hexColorToRGBA } from '@/views/chart/components/js/util'

export function getShapeStyle(style) {
  const result = {}
  ;['width', 'height', 'top', 'left', 'rotate'].forEach(attr => {
    if (attr != 'rotate') {
      result[attr] = style[attr] + 'px'
    } else {
      result['transform'] = 'rotate(' + style[attr] + 'deg)'
    }
  })

  return result
}

const needUnit = [
  'fontSize',
  'width',
  'height',
  'top',
  'left',
  'borderWidth',
  'letterSpacing',
  'borderRadius'
]

export function getSVGStyle(style, filter = []) {
  const result = {}

  ;[
    'opacity',
    'width',
    'height',
    'top',
    'left',
    'rotate',
    'fontSize',
    'fontWeight',
    'lineHeight',
    'letterSpacing',
    'textAlign',
    'color'
  ].forEach(key => {
    if (!filter.includes(key)) {
      if (key != 'rotate') {
        if (style[key] !== '') {
          result[key] = style[key]

          if (needUnit.includes(key)) {
            result[key] += 'px'
          }
        }
      } else {
        result['transform'] = key + '(' + style[key] + 'deg)'
      }
    }
  })

  return result
}

export function getItemAllStyle(item, filter = []) {
  const style = item.style
  const commonBackground = item.commonBackground
  const result = {}
  Object.keys(style).forEach(key => {
    if (!filter.includes(key)) {
      if (key != 'rotate') {
        if (style[key] !== '') {
          result[key] = style[key]

          if (needUnit.includes(key)) {
            result[key] += 'px'
          }
        }
      } else {
        result['transform'] = key + '(' + style[key] + 'deg)'
      }
    }

    if (commonBackground) {
      //附加背景样式
      let colorRGBA = ''
      if (commonBackground.backgroundColorSelect) {
        colorRGBA = hexColorToRGBA(commonBackground.backgroundColor, commonBackground.alpha)
      }
      if (commonBackground.backgroundImageEnable) {
        if (
          commonBackground.backgroundType === 'outerImage' &&
          typeof commonBackground.outerImage === 'string'
        ) {
          result['background'] = `url(${imgUrlTrans(
            commonBackground.outerImage
          )}) no-repeat ${colorRGBA}`
        } else {
          result['background-color'] = colorRGBA
        }
      } else {
        result['background-color'] = colorRGBA
      }
    }
  })

  return result
}

export function getStyle(style, filter = []) {
  const result = {}
  Object.keys(style).forEach(key => {
    if (!filter.includes(key)) {
      if (key != 'rotate') {
        if (style[key] !== '') {
          result[key] = style[key]

          if (needUnit.includes(key)) {
            result[key] += 'px'
          }
        }
      } else {
        result['transform'] = key + '(' + style[key] + 'deg)'
      }
    }
    //附加背景样式
    let colorRGBA = ''
    if (style.backgroundColorSelect) {
      colorRGBA = hexColorToRGBA(
        this.element.commonBackground.color,
        this.element.commonBackground.alpha
      )
    }
    style['padding'] = (this.element.commonBackground.innerPadding || 0) + 'px'
    if (this.element.commonBackground.enable) {
      if (
        this.element.commonBackground.backgroundType === 'outerImage' &&
        typeof this.element.commonBackground.outerImage === 'string'
      ) {
        style['background'] = `url(${imgUrlTrans(
          this.element.commonBackground.outerImage
        )}) no-repeat ${colorRGBA}`
      } else {
        style['background-color'] = colorRGBA
      }
    } else {
      style['background-color'] = colorRGBA
    }
  })

  return result
}

// 获取一个组件旋转 rotate 后的样式
export function getComponentRotatedStyle(style) {
  style = { ...style }
  if (style.rotate != 0) {
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

const filterKeys = ['width', 'height', 'scale']
export function getCanvasStyle(canvasStyleData) {
  const result = {}
  const backgroundType = canvasStyleData.backgroundType
  Object.keys(canvasStyleData)
    .filter(key => !filterKeys.includes(key))
    .forEach(key => {
      if (key === 'background' || key === 'backgroundColor') {
        result[backgroundType] = canvasStyleData[backgroundType]
        if (backgroundType === 'background') {
          result[backgroundType] = `url(${imgUrlTrans(canvasStyleData[backgroundType])}) no-repeat`
        } else {
          result[backgroundType] = canvasStyleData[backgroundType]
        }
      } else {
        result[key] = canvasStyleData[key]
      }
      if (key === 'fontSize') {
        result[key] += 'px'
      }
    })

  return result
}

export function createGroupStyle(groupComponent) {
  const parentStyle = groupComponent.style
  groupComponent.propValue.forEach(component => {
    // component.groupStyle 的 top left 是相对于 group 组件的位置
    // 如果已存在 component.groupStyle，说明已经计算过一次了。不需要再次计算
    if (!Object.keys(component.groupStyle).length) {
      const style = { ...component.style }
      if (component.component.startsWith('SVG')) {
        component.groupStyle = getSVGStyle(style)
      } else {
        component.groupStyle = getStyle(style)
      }

      component.groupStyle.left = toPercent((style.left - parentStyle.left) / parentStyle.width)
      component.groupStyle.top = toPercent((style.top - parentStyle.top) / parentStyle.height)
      component.groupStyle.width = toPercent(style.width / parentStyle.width)
      component.groupStyle.height = toPercent(style.height / parentStyle.height)
    }
  })
}
