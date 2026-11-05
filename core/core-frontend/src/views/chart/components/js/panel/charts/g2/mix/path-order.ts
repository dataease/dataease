import type { MarkComponent, SingleMark } from '@antv/g2/esm/runtime/types/mark'

/**
 * 按最终横轴位置连接各系列，保留比例尺推断结果及系列绘制层次
 */
export const createOrderedMixPathMark = (baseMark: MarkComponent): MarkComponent => {
  const orderedMark: MarkComponent = options => {
    const geometry = baseMark(options) as SingleMark
    return (indices, scale, value, coordinate) => {
      const series = new Map<unknown, number[]>()
      indices.forEach(index => {
        const key = value.series?.[index]
        if (!series.has(key)) series.set(key, [])
        series.get(key).push(index)
      })
      // 此时 X 通道已完成比例尺映射，只调整连线顺序，不改写数据或横轴分类顺序
      const ordered = Array.from(series.values()).flatMap(group =>
        group.sort((a, b) => Number(value.x[a]) - Number(value.x[b]))
      )
      return geometry(ordered, scale, value, coordinate)
    }
  }
  orderedMark.props = baseMark.props
  return orderedMark
}
