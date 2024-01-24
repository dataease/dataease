export const positionData = [
  { key: 'left', label: 'X', min: -1000, max: 20000, step: 10 },
  { key: 'width', label: 'W', min: 10, max: 20000, step: 10 },
  { key: 'top', label: 'Y', min: -1000, max: 20000, step: 10 },
  { key: 'height', label: 'H', min: 10, max: 20000, step: 10 }
]

export const styleData = [
  { key: 'lineHeight', label: '行高', min: 0, max: 50, step: 1 },
  { key: 'opacity', label: '不透明度', min: 0, max: 1, step: 0.1 },
  { key: 'borderWidth', label: '边框宽度', min: 0, max: 20, step: 1 },
  { key: 'borderStyle', label: '边框风格' },
  { key: 'borderColor', label: '边框颜色' },
  { key: 'borderRadius', label: '边框半径', min: 0, max: 50, step: 1 },
  { key: 'letterSpacing', label: '字间距', min: 0, max: 50, step: 1 },
  { key: 'fontSize', label: '字体大小', min: 0, max: 128, step: 1 },
  { key: 'activeFontSize', label: '激活字体大小', min: 0, max: 128, step: 1 },
  { key: 'headFontColor', label: '标题字体颜色' },
  { key: 'headFontActiveColor', label: '标题字体激活颜色' },
  { key: 'headBorderColor', label: '标题边框颜色' },
  { key: 'headBorderActiveColor', label: '标题激活边框颜色' },
  { key: 'headHorizontalPosition', label: '标题位置' },
  { key: 'fontWeight', label: '字体粗细', min: 100, max: 900, step: 100 },
  { key: 'textAlign', label: '左右对齐' },
  { key: 'verticalAlign', label: '上下对齐' },
  { key: 'color', label: '颜色' },
  { key: 'backgroundColor', label: '背景色' }
]

export const styleMap = {
  left: 'x 坐标',
  top: 'y 坐标',
  rotate: '旋转角度',
  width: '宽',
  height: '高',
  color: '颜色',
  backgroundColor: '背景色',
  borderWidth: '边框宽度',
  borderStyle: '边框风格',
  borderColor: '边框颜色',
  borderRadius: '边框半径',
  fontSize: '字体大小',
  fontWeight: '字体粗细',
  lineHeight: '行高',
  letterSpacing: '字间距',
  textAlign: '左右对齐',
  verticalAlign: '上下对齐',
  opacity: '不透明度'
}

export const textAlignOptions = [
  {
    label: '左对齐',
    value: 'left'
  },
  {
    label: '居中',
    value: 'center'
  },
  {
    label: '右对齐',
    value: 'right'
  }
]

export const borderStyleOptions = [
  {
    label: '实线',
    value: 'solid'
  },
  {
    label: '虚线',
    value: 'dashed'
  }
]

export const verticalAlignOptions = [
  {
    label: '上对齐',
    value: 'top'
  },
  {
    label: '居中对齐',
    value: 'middle'
  },
  {
    label: '下对齐',
    value: 'bottom'
  }
]

export const selectKey = ['textAlign', 'borderStyle', 'verticalAlign']

export const horizontalPosition = ['headHorizontalPosition']

export const fieldType = ['text', 'time', 'value', 'value', 'value', 'location']

export const optionMap = {
  textAlign: textAlignOptions,
  borderStyle: borderStyleOptions,
  verticalAlign: verticalAlignOptions
}
