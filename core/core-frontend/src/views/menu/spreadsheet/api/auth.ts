import { tree } from './index'
export interface BusiTreeNode {
  id: string | number
  pid: string | number
  name: string
  leaf?: boolean
  weight: number
  extraFlag: number
  extraFlag1: number
  children?: BusiTreeNode[]
}

export interface BusiTreeRequest {
  busiFlag?: string
  leaf?: boolean
  weight?: number
  sortType?: string
}

export interface InnerInteractive {
  rootManage: boolean
  anyManage: boolean
  treeNodes: BusiTreeNode[]
  leafNodeCount: number
  menuAuth: boolean
}
export const treeWithAuth = async (param: BusiTreeRequest) => {
  const res = await tree(param)
  return convertInteractive(res)
}
const convertInteractive = (list): InnerInteractive => {
  const result: InnerInteractive = {
    rootManage: list[0]['weight'] >= 7,
    anyManage: false,
    treeNodes: (list as unknown as BusiTreeNode[]) || [],
    leafNodeCount: 0,
    menuAuth: true
  }
  const stack = [...list]
  let leafNodeCount = 0
  while (stack.length) {
    const node = stack.pop()
    if (!node['leaf'] && node['weight'] >= 7) {
      result.anyManage = true
      // break
    }
    if (node['leaf'] && node['weight']) {
      ++leafNodeCount
    }
    if (node?.children?.length) {
      node.children.forEach(kid => stack.push(kid))
    }
  }
  result.leafNodeCount = leafNodeCount
  return result
}
