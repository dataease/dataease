// 将组合中的各个子组件拆分出来，并计算它们新的 style
export default function decomposeComponent(
  component,
  editorRect,
  parentStyle,
  canvasId = 'canvas-main',
  parentGroupStyle?
) {
  // 计算出元素新的 top left 坐标
  component.style.left = component.style.left + parentStyle.left
  component.style.top = component.style.top + parentStyle.top
  if (parentGroupStyle && component.groupStyle) {
    const originLeftScale = component.groupStyle['left'] / component.groupStyle['width']
    const originTopScale = component.groupStyle['top'] / component.groupStyle['height']
    component.groupStyle['width'] = component.groupStyle['width'] * parentGroupStyle['width']
    component.groupStyle['height'] = component.groupStyle['height'] * parentGroupStyle['height']
    component.groupStyle['left'] =
      parentStyle.left + component.groupStyle['width'] * originLeftScale
    component.groupStyle['top'] = parentStyle.top + component.groupStyle['height'] * originTopScale
  }
  component.canvasId = canvasId
}
