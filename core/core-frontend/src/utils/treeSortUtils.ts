import { BusiTreeNode } from '@/models/tree/TreeNode'
import _ from 'lodash'

export function treeParentWeight(tree: BusiTreeNode[], pWeight) {
  const pWeightResult = {}
  weightCheckCircle(tree, pWeightResult, pWeight)
  return pWeightResult
}

export function weightCheckCircle(tree: BusiTreeNode[], pWeightResult, pWeight) {
  _.forEach(tree, node => {
    pWeightResult[node.id] = pWeight
    if (node.children && node.children.length > 0) {
      weightCheckCircle(node.children, pWeightResult, node.weight)
    }
  })
}

export default function treeSort(tree: BusiTreeNode[], sortType: string) {
  const result = _.cloneDeep(tree)
  sortCircle(result, sortType)
  return result
}

export function sortCircle(tree: BusiTreeNode[], sortType: string) {
  sortPer(tree, sortType)
  _.forEach(tree, node => {
    if (node.children && node.children.length > 0) {
      sortCircle(node.children, sortType)
    }
  })
}

export const sortPer = (subTree: BusiTreeNode[], sortType: string) => {
  if (sortType === 'name_desc') {
    subTree.sort((a, b) => b.name.localeCompare(a.name, 'zh-Hans-CN', { sensitivity: 'accent' }))
  } else if (sortType === 'name_asc') {
    subTree.sort((a, b) => a.name.localeCompare(b.name, 'zh-Hans-CN', { sensitivity: 'accent' }))
  } else if (sortType === 'time_asc') {
    subTree.reverse()
  }
}
