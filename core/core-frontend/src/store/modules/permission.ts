import { defineStore } from 'pinia'
import { routes } from '@/router'

import { generateRoutesFn2 } from '@/router/establish'
import { store } from '../index'
import { cloneDeep } from 'lodash-es'

export interface PermissionState {
  routers: AppRouteRecordRaw[]
  addRouters: AppRouteRecordRaw[]
  isAddRouters: boolean
}

export const usePermissionStore = defineStore('permission', {
  state: (): PermissionState => ({
    routers: [],
    addRouters: [],
    isAddRouters: false
  }),
  getters: {
    getRouters(): AppRouteRecordRaw[] {
      return this.routers
    },
    getAddRouters(): AppRouteRecordRaw[] {
      return cloneDeep(this.addRouters)
    },
    getIsAddRouters(): boolean {
      return this.isAddRouters
    }
  },
  actions: {
    generateRoutes(routers?: AppCustomRouteRecordRaw[] | string[]): Promise<unknown> {
      return new Promise<void>(resolve => {
        let routerMap: AppRouteRecordRaw[] = []
        routerMap = generateRoutesFn2(routers as AppCustomRouteRecordRaw[]) || []

        // 动态路由，404一定要放到最后面
        this.addRouters = routerMap.concat([
          {
            path: '/:path(.*)*',
            redirect: '/404',
            name: '404Page',
            meta: {
              hidden: true,
              breadcrumb: false
            }
          }
        ])
        // 渲染菜单的所有路由
        this.routers = cloneDeep(routes).concat(routerMap)
        resolve()
      })
    },
    setIsAddRouters(state: boolean): void {
      this.isAddRouters = state
    }
  }
})

export const usePermissionStoreWithOut = () => {
  return usePermissionStore(store)
}
