export function isFunction(func) {
  return (typeof func === 'function' || Object.prototype.toString.call(func) === '[object Function]')
}

// 对齐栅格
export function snapToGrid(grid, pendingX, pendingY, scale = 1) {
  const x = Math.round((pendingX / scale) / grid[0]) * grid[0]
  const y = Math.round((pendingY / scale) / grid[1]) * grid[1]
  return [x, y]
}

// 获取rect模型
export function getSize(el) {
  const rect = el.getBoundingClientRect()
  return [
    parseInt(rect.width),
    parseInt(rect.height)
  ]
}

export function computeWidth(parentWidth, left, right) {
  return parentWidth - left - right
}

export function computeHeight(parentHeight, top, bottom) {
  return parentHeight - top - bottom
}

export function restrictToBounds(value, min, max) {
  if (min !== null && value < min) {
    return min
  }

  if (max !== null && max < value) {
    return max
  }

  return value
}

// 返回相对于参考点旋转后的坐标
export function rotatedPoint(originX, originY, offsetX, offsetY, rotate) {
  const rad = (Math.PI / 180) * rotate
  const cos = Math.cos(rad)
  const sin = Math.sin(rad)
  const x = offsetX - originX
  const y = offsetY - originY
  return {
    x: x * cos - y * sin + originX,
    y: x * sin + y * cos + originY
  }
}

// 根据相对坐标返回角度，正方形为顺时针
export function getAngle(x, y) {
  let theta = Math.atan2(y, x) // 正切转弧度
  theta = Math.round((180 / Math.PI) * theta) // 弧度转角度
  if (theta < 0) theta = 360 + theta // 控制角度在0~360度
  return theta // 返回角度
}
