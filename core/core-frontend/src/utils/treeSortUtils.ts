import { BusiTreeNode } from '@/models/tree/TreeNode'
import _ from 'lodash'

export default function treeSort(tree: BusiTreeNode[], sortType: string, historySortType: string) {
  const result = _.cloneDeep(tree)
  sortPer(result, sortType, historySortType)
  _.forEach(result, node => {
    if (node.children && node.children.length > 0) {
      sortPer(node.children, sortType, historySortType)
    }
  })
  return result
}

export const sortPer = (subTree: BusiTreeNode[], sortType: string, historySortType: string) => {
  if (sortType === 'name_desc') {
    subTree.sort((a, b) => b.name.localeCompare(a.name, 'zh-Hans-CN', { sensitivity: 'accent' }))
  } else if (sortType === 'name_asc') {
    subTree.sort((a, b) => a.name.localeCompare(b.name, 'zh-Hans-CN', { sensitivity: 'accent' }))
  } else if (sortType !== historySortType) {
    return subTree.reverse()
  }
}
