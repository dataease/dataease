import { sin, cos } from '@/utils/translate'
import { imgUrlTrans } from '@/utils/imgUtils'
import { hexColorToRGBA } from '@/views/chart/components/js/util'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
const dvMainStore = dvMainStoreWithOut()
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

export function getShapeItemStyle(item, { dvModel, cellWidth, cellHeight, curGap }) {
  let result = {}
  if (dvModel === 'dashboard' && !item['isPlayer']) {
    result = {
      padding: curGap + 'px!important',
      width: cellWidth * item.sizeX + 'px',
      height: cellHeight * item.sizeY + 'px',
      left: cellWidth * (item.x - 1) + 'px',
      top: cellHeight * (item.y - 1) + 'px'
    }
  } else {
    result = {
      padding: curGap + 'px!important',
      width: item.style.width + 'px',
      height: item.style.height + 'px',
      left: item.style.left + 'px',
      top: item.style.top + 'px'
    }
  }

  return result
}

export function syncShapeItemStyle(item, cellWidth, cellHeight) {
  item.style.left = cellWidth * (item.x - 1)
  item.style.top = cellHeight * (item.y - 1)
  item.style.width = cellWidth * item.sizeX
  item.style.height = cellHeight * item.sizeY
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

export function getCanvasStyle(canvasStyleData) {
  const {
    backgroundColorSelect,
    background,
    backgroundColor,
    backgroundImageEnable,
    fontSize,
    mobileSetting
  } = canvasStyleData
  const style = { fontSize: fontSize + 'px', color: canvasStyleData.color }
  // 仪表板默认色#f5f6f7 大屏默认配色 #1a1a1a
  let colorRGBA = dvMainStore.dvInfo.type === 'dashboard' ? '#f5f6f7' : '#1a1a1a'
  if (backgroundColorSelect && backgroundColor) {
    colorRGBA = backgroundColor
  }
  if (backgroundImageEnable) {
    style['background'] = `url(${imgUrlTrans(background)}) no-repeat ${colorRGBA}`
  } else {
    style['background-color'] = colorRGBA
  }

  if (dvMainStore.mobileInPc && mobileSetting?.customSetting) {
    const { backgroundColorSelect, color, backgroundImageEnable, background } = mobileSetting
    if (backgroundColorSelect && backgroundImageEnable && typeof background === 'string') {
      style['background'] = `url(${imgUrlTrans(background)}) no-repeat ${color}`
    } else if (backgroundColorSelect) {
      style['background-color'] = color
    } else if (backgroundImageEnable) {
      style['background'] = `url(${imgUrlTrans(background)}) no-repeat`
    }
  }
  return style
}

export function createGroupStyle(groupComponent) {
  const parentStyle = groupComponent.style
  groupComponent.propValue.forEach(component => {
    // 分组计算逻辑
    // 1.groupStyle记录left top width height 在出现分组缩放的时候进行等比例变更（缩放来源有两种a.整个大屏的缩放 b.分组尺寸的调整）
    // 2.component 内部进行位移或者尺寸的变更 要同步到这个比例中
    const style = { ...component.style }
    component.groupStyle.left = (style.left - parentStyle.left) / parentStyle.width
    component.groupStyle.top = (style.top - parentStyle.top) / parentStyle.height
    component.groupStyle.width = style.width / parentStyle.width
    component.groupStyle.height = style.height / parentStyle.height

    component.style.left = component.style.left - parentStyle.left
    component.style.top = component.style.top - parentStyle.top
  })
}

export function groupSizeStyleAdaptor(groupComponent) {
  const parentStyle = groupComponent.style
  groupComponent.propValue.forEach(component => {
    // 分组还原逻辑
    // 当发上分组缩放是，要将内部组件按照比例转换
    const styleScale = component.groupStyle
    component.style.left = parentStyle.width * styleScale.left
    component.style.top = parentStyle.height * styleScale.top
    component.style.width = parentStyle.width * styleScale.width
    component.style.height = parentStyle.height * styleScale.height
  })
}

export function groupStyleRevert(innerComponent, parentStyle) {
  const innerStyle = { ...innerComponent.style }
  innerComponent.groupStyle.left = innerStyle.left / parentStyle.width
  innerComponent.groupStyle.top = innerStyle.top / parentStyle.height
  innerComponent.groupStyle.width = innerStyle.width / parentStyle.width
  innerComponent.groupStyle.height = innerStyle.height / parentStyle.height
}
