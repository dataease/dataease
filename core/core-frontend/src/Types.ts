/**
 * 简写模式枚举，用于定义不同的边值设置模式
 */
export enum ShorthandMode {
  /**
   * 统一模式，所有边使用相同的值
   */
  Uniform = 'uniform',
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

export interface CornerValues {
  mode?: ShorthandMode
  topLeft?: number
  topRight?: number
  bottomLeft?: number
  bottomRight?: number
}
