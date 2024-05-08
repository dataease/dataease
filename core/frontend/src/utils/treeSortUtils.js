import { deepCopy } from '@/components/canvas/utils/utils'

export default function treeSort(tree, hisSortType, sortType) {
  const result = deepCopy(tree)
  sortCircle(result, hisSortType, sortType)
  return result
}

export function sortCircle(tree, hisSortType, sortType) {
  sortPer(tree, hisSortType, sortType)
  tree.forEach(node => {
    if (node.children && node.children.length > 0) {
      sortCircle(node.children, hisSortType, sortType)
    }
  })
  return tree
}

export function sortPer(subTree, hisSortType, sortType) {
  if (sortType === 'name_desc') {
    subTree.sort((a, b) => b.name.localeCompare(a.name, 'zh-Hans-CN', { sensitivity: 'accent' }))
  } else if (sortType === 'name_asc') {
    subTree.sort((a, b) => a.name.localeCompare(b.name, 'zh-Hans-CN', { sensitivity: 'accent' }))
  } else if (sortType === 'time_asc') {
    subTree.reverse()
  }
}
