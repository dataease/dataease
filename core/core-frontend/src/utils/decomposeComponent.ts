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
  // parentGroupStyle / groupStyle 都可能是 component-list.ts 里的默认空对象（例如分组位于选项卡内、
  // 或子组件加入分组后尚未重排）。此时拿不到有效的宽高分母，等比例换算会得到 NaN 并在序列化后变成 null。
  const parentGroupWidth = parentGroupStyle?.['width']
  const parentGroupHeight = parentGroupStyle?.['height']
  const originWidth = component.groupStyle?.['width']
  const originHeight = component.groupStyle?.['height']
  const canScale =
    parentGroupWidth > 0 &&
    parentGroupHeight > 0 &&
    originWidth > 0 &&
    originHeight > 0
  if (canScale) {
    const originLeftScale = component.groupStyle['left'] / originWidth
    const originTopScale = component.groupStyle['top'] / originHeight
    component.groupStyle['width'] = originWidth * parentGroupWidth
    component.groupStyle['height'] = originHeight * parentGroupHeight
    component.groupStyle['left'] =
      parentStyle.left + component.groupStyle['width'] * originLeftScale
    component.groupStyle['top'] =
      parentStyle.top + component.groupStyle['height'] * originTopScale
  }
  component.canvasId = canvasId
}
