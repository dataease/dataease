import { COLOR_PANEL } from '@/views/chart/components/editor/util/chart'
import type { UploadFile } from 'element-plus-secondary'

export type BackgroundType = 'outerImage' | 'innerImage'

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

export interface CommonBackground {
  innerPadding?: EdgeValues
  borderRadius?: number
  backdropFilterEnable?: boolean
  backdropFilter?: number
  backgroundColorSelect?: boolean
  backgroundColor?: string
  backgroundImageEnable?: boolean
  backgroundType?: BackgroundType
  innerImageColor?: string
  innerImage?: string
  outerImage?: string
}

export interface State {
  commonBackground: CommonBackground
  BackgroundShowMap: Record<string, any>
  checked: boolean
  backgroundOrigin: Record<string, any>
  fileList: UploadFile[]
  dialogImageUrl: string
  dialogVisible: boolean
  uploadDisabled: boolean
  panel?: any
  predefineColors: typeof COLOR_PANEL
}
