import { defineStore } from 'pinia'
import { store } from '@/store'
import { queryTreeApi, queryBusiTreeApi } from '@/api/visualization/dataVisualization'
import { getDatasetTree } from '@/api/dataset'
import { listDatasources } from '@/api/datasource'
import type { BusiTreeRequest, BusiTreeNode } from '@/models/tree/TreeNode'
import { pathValid } from '@/store/modules/permission'
import { useCache } from '@/hooks/web/useCache'
import { useAppStoreWithOut } from '@/store/modules/app'
const appStore = useAppStoreWithOut()
const { wsCache } = useCache()
export interface InnerInteractive {
  rootManage: boolean
  anyManage: boolean
  treeNodes: BusiTreeNode[]
  leafNodeCount: number
  menuAuth: boolean
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
    async setInteractive(param: BusiTreeRequest, resParam?: object) {
      const flag = busiFlagMap.findIndex(item => item === param.busiFlag)
      if (!hasMenuAuth(flag) && !window.DataEaseBi && !appStore.getIsIframe) {
        const tempData: InnerInteractive = {
          rootManage: false,
          anyManage: false,
          treeNodes: [],
          leafNodeCount: 0,
          menuAuth: false
        }
        this.data[flag] = tempData
        if (flag === 0) {
          wsCache.set('panel-weight', {})
        }
        if (flag === 1) {
          wsCache.set('screen-weight', {})
        }
        return []
      }
      let res = resParam
      if (!resParam) {
        const method = apiMap[flag]
        res = await method(param)
      }
      this.data[flag] = convertInteractive(res)
      if (flag === 0) {
        wsCache.set('panel-weight', convertLocalStorage(this.data[flag]))
      }
      if (flag === 1) {
        wsCache.set('screen-weight', convertLocalStorage(this.data[flag]))
      }
      return res
    },
    async initInteractive(refresh?: boolean) {
      if (refresh) {
        await this.loadBusiInteractive()
        return
      }
      let index = 4
      while (index--) {
        if (!this.data[index] || refresh) {
          const param: BusiTreeRequest = {
            busiFlag: busiFlagMap[index]
          }
          await this.setInteractive(param)
        }
      }
    },
    async loadBusiInteractive() {
      const param = {}
      for (let i = 0; i < busiFlagMap.length; i++) {
        const key = busiFlagMap[i]
        if (window.DataEaseBi || appStore.getIsIframe || hasMenuAuth(i)) {
          param[key] = { busiFlag: key }
        }
      }
      const data = await queryBusiTreeApi(param)
      for (const busiKey in data) {
        const res = data[busiKey]
        this.setInteractive(param[busiKey], res)
      }
    },
    clear() {
      this.data = {}
      wsCache.set('panel-weight', {})
      wsCache.set('screen-weight', {})
    }
  }
})

export const interactiveStoreWithOut = () => interactiveStore(store)

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

const hasMenuAuth = (flag: number): boolean => {
  let path = '/panel/index'
  if (flag === 1) {
    path = '/screen/index'
  } else if (flag === 2) {
    path = '/data/dataset'
  } else if (flag === 3) {
    path = '/data/datasource'
  }
  const valid = pathValid(path)
  return valid
}

const convertLocalStorage = (data?: InnerInteractive) => {
  if (!data?.leafNodeCount) {
    return {}
  }
  const result = {}
  const treeNodes = data.treeNodes
  const stack = [...treeNodes]
  while (stack.length) {
    const node = stack.pop()
    if (node.leaf) {
      const { id, weight } = node
      result[id] = weight
    }
    if (node.children?.length) {
      node.children.forEach(kid => stack.push(kid))
    }
  }
  return result
}
