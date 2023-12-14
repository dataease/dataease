// 将组合中的各个子组件拆分出来，并计算它们新的 style
export default function decomposeComponent(component, editorRect, parentStyle) {
  // 计算出元素新的 top left 坐标
  component.style.left = component.style.left + parentStyle.left
  component.style.top = component.style.top + parentStyle.top
  component.groupStyle = {}
  component.canvasId = 'canvas-main'
}
