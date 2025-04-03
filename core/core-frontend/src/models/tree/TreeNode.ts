export interface BusiTreeNode {
  id: string | number
  pid: string | number
  name: string
  leaf?: boolean
  weight: number
  ext?: number
  extraFlag: number
  extraFlag1: number
  children?: BusiTreeNode[]
}

export interface BusiTreeRequest {
  busiFlag?: string
  leaf?: boolean
  weight?: number
  sortType?: string
  resourceTable?: string
}
