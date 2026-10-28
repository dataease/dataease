import request from '@/config/axios'
import { useCache } from '@/hooks/web/useCache'
import { isNull } from '@/utils/utils'

export const load = (key: string) => request.get({ url: `/xpackComponent/content/${key}` })

export const loadPluginApi = (key: string) =>
  request.get({ url: `/xpackComponent/contentPlugin/${key}` })

export const loadDistributed = () => request.get({ url: '/DEXPack.umd.js' })

export const xpackModelApi = () => request.get({ url: '/xpackModel' })

export const getXpackModel = async () => {
  const { wsCache } = useCache()
  const key = 'xpack-model-distributed'
  if (import.meta.env.DEV && import.meta.env.MODE === 'dev') {
    wsCache.set(key, false)
    return false
  }
  const cached = wsCache.get(key)
  if (cached !== null) {
    return cached
  }
  const res = await xpackModelApi()
  wsCache.set(key, isNull(res.data) ? 'null' : res.data)
  return res.data
}
