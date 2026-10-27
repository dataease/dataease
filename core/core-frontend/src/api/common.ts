import { createConfigRequestCache } from '@/utils/configRequestCache'
import request from '@/config/axios'

// 获取权限路由
export const getRoleRouters = async (): Promise<Array<AppCustomRouteRecordRaw>> => {
  return request.get({ url: '/menu/query' }).then(res => {
    return res?.data
  })
}

// 获取默认排序
export const getDefaultSettings = async (): Promise<IResponse> => {
  return request.get({ url: '/sysParameter/defaultSettings' }).then(res => {
    return res?.data
  })
}

const defaultSettingsCache = createConfigRequestCache(() => getDefaultSettings())
export const getCachedDefaultSettings = (force = false) => defaultSettingsCache.get(force)
export const invalidateDefaultSettings = () => defaultSettingsCache.clear()
