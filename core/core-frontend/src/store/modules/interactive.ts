import { defineStore } from 'pinia'
import { store } from '../index'
import { queryTreeApi } from '@/api/visualization/dataVisualization'
import { getDatasetTree } from '@/api/dataset'
import { listDatasources } from '@/api/datasource'
import type { BusiTreeRequest, BusiTreeNode } from '@/models/tree/TreeNode'
import router from '@/router'

interface InnerInteractive {
  rootManage: boolean
  anyManage: boolean
  treeNodes: BusiTreeNode[]
}

interface InteractiveState {
  data: Record<number, InnerInteractive>
}

const apiMap = [queryTreeApi, queryTreeApi, getDatasetTree, listDatasources]

const busiFlagMap = ['dashboard', 'dataV', 'dataset', 'datasource']

export const interactiveStore = defineStore('interactive', {
  state: (): InteractiveState => ({
    data: {}
  }),
  getters: {
    getPanel(): InnerInteractive {
      return this.data[0]
    },
    getScreen(): InnerInteractive {
      return this.data[1]
    },
    getDataset(): InnerInteractive {
      return this.data[2]
    },
    getDatasource(): InnerInteractive {
      return this.data[3]
    },
    getData(): InteractiveState {
      return this.data
    }
  },
  actions: {
    async setInteractive(param: BusiTreeRequest) {
      const flag = busiFlagMap.findIndex(item => item === param.busiFlag)
      if (!hasMenuAuth(flag)) {
        const tempData: InnerInteractive = {
          rootManage: false,
          anyManage: false,
          treeNodes: []
        }
        this.data[flag] = tempData
        return []
      }
      const method = apiMap[flag]
      const res = await method(param)
      this.data[flag] = convertInteractive(res)
      return res
    },
    async initInteractive(refresh?: boolean) {
      let index = 4
      while (index--) {
        if (!this.data[index] || refresh) {
          const param: BusiTreeRequest = {
            busiFlag: busiFlagMap[index]
          }
          this.setInteractive(param)
        }
      }
    }
  }
})

export const interactiveStoreWithOut = () => interactiveStore(store)

const convertInteractive = (list): InnerInteractive => {
  const result: InnerInteractive = {
    rootManage: list[0]['weight'] >= 7,
    anyManage: false,
    treeNodes: (list as unknown as BusiTreeNode[]) || []
  }
  const stack = [...list]
  while (stack.length) {
    const node = stack.pop()
    if (!node['leaf'] && node['weight'] >= 7) {
      result.anyManage = true
      break
    }
    if (node?.children?.length) {
      node.children.forEach(kid => stack.push(kid))
    }
  }
  return result
}

const hasMenuAuth = (flag: number): boolean => {
  let name = 'panel'
  if (flag === 1) {
    name = 'screen'
  } else if (flag === 2) {
    name = 'dataset'
  } else if (flag === 3) {
    name = 'datasource'
  }
  const match = router.hasRoute(name)
  return match
}
