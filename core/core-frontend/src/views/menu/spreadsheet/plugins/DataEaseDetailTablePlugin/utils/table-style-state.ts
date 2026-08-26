 import type { DetailTableConfig } from '../types'

/** 表头样式总开关关闭时，序号配置仅保留状态，不参与实际渲染。 */
export const isDetailTableIndexVisible = (config: DetailTableConfig): boolean => {
  const headerStyle = config.style?.header
  return headerStyle?.enable === true && headerStyle.showIndex === true
}
