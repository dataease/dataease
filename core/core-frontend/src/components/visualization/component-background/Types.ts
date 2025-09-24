import { COLOR_PANEL } from '@/views/chart/components/editor/util/chart'
import type { UploadFile } from 'element-plus-secondary'

export type BackgroundType = 'outerImage' | 'innerImage'
// export type PaddingMode = 'Uniform' | '垂直-水平' | '四边独立'

export enum PaddingMode {
  Uniform = 'uniform',
  V_H = 'v_h',
  PerSide = 'per_side'
}

export interface CommonBackground {
  innerPadding?: number
  innerPaddingRight?: number
  innerPaddingBottom?: number
  innerPaddingLeft?: number
  borderRadius?: number
  backdropFilterEnable?: boolean
  backdropFilter?: number
  backgroundColorSelect?: boolean
  backgroundColor?: string
  backgroundImageEnable?: boolean
  backgroundType?: BackgroundType
  innerImageColor?: string
  innerImage?: string
  outerImage?: string | null
  paddingMode?: PaddingMode
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
