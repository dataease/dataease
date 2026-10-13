import { extend, Runtime, stdlib, type Vector2 } from '@antv/g2'

const library = stdlib()

/**
 * 在本轮比例尺计算完成后定位象限背景，避免读取轴范围后再次渲染整图
 */
export const QuadrantRange: (typeof library)['mark.range'] =
  () => (index, scale, value, coordinate) => {
    const [xMin, xMax] = scale.x.getOptions().domain.map(value => scale.x.map(value))
    const [yMin, yMax] = scale.y.getOptions().domain.map(value => scale.y.map(value))
    const points = index.map(i => {
      const x = Number(value.x[i])
      const y = Number(value.y[i])
      // 四条数据按右上、左上、左下、右下排列，外边界使用比例尺映射后的轴端点
      const x1 = i === 1 || i === 2 ? xMin : x
      const x2 = i === 1 || i === 2 ? x : xMax
      const y1 = i < 2 ? y : yMin
      const y2 = i < 2 ? yMax : y
      return [
        [x1, y1],
        [x2, y1],
        [x2, y2],
        [x1, y2]
      ].map(point => coordinate.map(point) as Vector2)
    })
    return [index, points]
  }

QuadrantRange.props = library['mark.range'].props

// 仅替换象限图实例的背景定位，保留 range 注解语义及其他图表的默认实现
export const QuadrantChart = extend(Runtime, { ...library, 'mark.range': QuadrantRange })
