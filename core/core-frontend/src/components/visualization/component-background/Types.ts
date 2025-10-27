import { EdgeValues, CornerValues } from '@/Types'
import { COLOR_PANEL } from '@/views/chart/components/editor/util/chart'
import type { UploadFile } from 'element-plus-secondary'

export type BackgroundType = 'outerImage' | 'innerImage'

export interface CommonBackground {
  innerPadding?: EdgeValues
  borderRadius?: CornerValues
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
