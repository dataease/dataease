import type { LabelTransformComponent } from '@antv/g2/esm/runtime'
import type { Text } from '@antv/g'

export interface TreemapLabelDatum {
  value: number
  data: {
    field: string
    dimensionList?: { value: unknown }[]
  }
}

/**
 * 按完整内容项缩减标签，避免因某个字段内部换行而截断字段
 */
export const TreemapLabelFit: LabelTransformComponent<{
  getContentItems: (datum: TreemapLabelDatum) => string[]
}> =
  ({ getContentItems }) =>
  labels => {
    labels.forEach(label => {
      const items = getContentItems(label.attr('datum'))
      const bounds = label.attr('bounds') as [number[], number[]]
      const text = label.querySelector<Text>('text')
      if (!text || !bounds) return

      // 每次布局从完整内容开始，缩放变大时可以恢复被缩减的内容
      for (let count = items.length; count > 0; count--) {
        label.style.text = items.slice(0, count).join('\n')
        const { min, max } = label.getLocalBounds()
        const fits =
          min[0] >= bounds[0][0] - 0.01 &&
          min[1] >= bounds[0][1] - 0.01 &&
          max[0] <= bounds[1][0] + 0.01 &&
          max[1] <= bounds[1][1] + 0.01
        // 单个字符比矩形更宽时，文本引擎会直接裁剪，包围盒不能反映内容缺失
        if (fits && !text.isOverflowing()) return
      }
      label.style.text = ''
    })
    return labels
  }
