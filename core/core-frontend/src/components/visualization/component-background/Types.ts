import { COLOR_PANEL } from '@/views/chart/components/editor/util/chart'
import type { UploadFile } from 'element-plus-secondary'

export type BackgroundType = 'outerImage' | 'innerImage'
// export type PaddingMode = 'Uniform' | '垂直-水平' | '四边独立'

export enum PaddingMode {
  Uniform = 'uniform',
  V_H = 'v_h',
  PerSide = 'per_side'
}

export interface InnerPadding {
  mode?: PaddingMode
  top?: number
  right?: number
  bottom?: number
  left?: number
}

export interface CommonBackground {
  innerPadding?: number
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
  innerPadding2?: InnerPadding
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
