/**
 * 简写模式枚举，用于定义不同的边值设置模式
 */
export enum ShorthandMode {
  /**
   * 统一模式，所有边使用相同的值
   */
  Uniform = 'uniform',
  /**
   * 轴模式，可能按水平和垂直轴设置值
   */
  Axis = 'axis',
  /**
   * 逐边模式，可单独设置每条边的值
   */
  PerEdge = 'per_edge'
}

export interface EdgeValues {
  mode?: ShorthandMode
  top?: number
  right?: number
  bottom?: number
  left?: number
}
