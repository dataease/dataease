import { computed, effectScope, reactive, watch } from 'vue'
import { useCache } from '@/hooks/web/useCache'
import { useEmbedded } from '@/store/modules/embedded'
import { useLinkStoreWithOut } from '@/store/modules/link'
import { useUserStoreWithOut } from '@/store/modules/user'
import { clearResourceImages, resourceImage } from './resourceImageCache'

let initialized = false
const { wsCache } = useCache()
const identityScope = () => {
  const user = useUserStoreWithOut()
  const embedded = useEmbedded()
  return JSON.stringify([
    user.getToken,
    wsCache.get('user.token'),
    user.getUid,
    user.getOid,
    user.getProxyInfo,
    wsCache.get('user.proxyInfo'),
    useLinkStoreWithOut().getLinkToken,
    embedded.token,
    embedded.baseUrl,
    wsCache.get('app.desktop')
  ])
}

const initialize = () => {
  if (initialized) return
  initialized = true
  // Shared by multiple pages/components; must not stop when the first image component unmounts.
  effectScope(true).run(() => watch(identityScope, clearResourceImages, { flush: 'sync' }))
  window.addEventListener('pagehide', clearResourceImages)
  window.addEventListener('hashchange', clearResourceImages)
  window.addEventListener('popstate', clearResourceImages)
  window.addEventListener('storage', event => {
    if (!event.key || /user\.(token|uid|oid|proxyInfo)$/.test(event.key)) clearResourceImages()
  })
}

export const authenticatedImageUrl = (url: string): string => {
  if (!url?.startsWith('/static-resource/')) return url
  initialize()
  const file = url.slice('/static-resource/'.length)
  if (!file || /[/\\?#%]/.test(file) || file.includes('..')) return ''
  const path = '/static-resource/' + encodeURIComponent(file)
  return resourceImage(identityScope(), path, async signal => {
    // Avoid the existing axios -> stores -> router -> image utilities initialization cycle.
    const { fetchResourceImage } = await import('@/api/staticResource')
    return fetchResourceImage(path, signal)
  })
}

// Keep the persisted path available for removal; never save the display URL into component data.
export const imageUploadItem = (sourceUrl: string, name?: string) =>
  reactive({ sourceUrl, name, url: computed(() => authenticatedImageUrl(sourceUrl)) })
