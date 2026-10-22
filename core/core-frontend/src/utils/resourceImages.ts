import { computed, effectScope, reactive, watch } from 'vue'
import { getActivePinia } from 'pinia'
import { useCache } from '@/hooks/web/useCache'
import { useEmbedded } from '@/store/modules/embedded'
import { useLinkStoreWithOut } from '@/store/modules/link'
import { useUserStoreWithOut } from '@/store/modules/user'
import { clearResourceImages, resourceImage } from './resourceImageCache'

const importPreviews = new Map<string, string>()
export const registerMaterialPreviews = (bundle?: {
  files: { reference: string; format: string; content: string }[]
}) => {
  initialize()
  importPreviews.clear()
  bundle?.files.forEach(file => {
    const mime =
      file.format === 'svg'
        ? 'image/svg+xml'
        : file.format === 'jpg'
        ? 'image/jpeg'
        : 'image/' + file.format
    importPreviews.set(file.reference, 'data:' + mime + ';base64,' + file.content)
  })
}
export const clearMaterialPreviews = () => importPreviews.clear()
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
const imageScope = () => {
  const canvas = getActivePinia()?.state.value.dataVisualization
  return JSON.stringify([
    identityScope(),
    window.location.hash,
    canvas?.dvInfo?.id,
    canvas?.dvInfo?.materialSourceTemplateId,
    canvas?.editMode
  ])
}

const initialize = () => {
  if (initialized) return
  initialized = true
  // Shared by multiple pages/components; must not stop when the first image component unmounts.
  effectScope(true).run(() =>
    watch(
      identityScope,
      () => {
        clearResourceImages()
        clearMaterialPreviews()
      },
      { flush: 'sync' }
    )
  )
  window.addEventListener('pagehide', clearMaterialPreviews)
  window.addEventListener('hashchange', clearMaterialPreviews)
  window.addEventListener('popstate', clearMaterialPreviews)
  window.addEventListener('pagehide', clearResourceImages)
  window.addEventListener('hashchange', clearResourceImages)
  window.addEventListener('popstate', clearResourceImages)
  window.addEventListener('storage', event => {
    if (!event.key || /user\.(token|uid|oid|proxyInfo)$/.test(event.key)) clearResourceImages()
  })
}

export const authenticatedImageUrl = (url: string): string => {
  if (importPreviews.has(url)) return importPreviews.get(url)!
  if (url?.startsWith('material://')) {
    const id = url.slice('material://'.length)
    if (!/^[1-9][0-9]{0,18}$/.test(id)) return ''
    initialize()
    return resourceImage(imageScope(), url, async signal => {
      const { fetchMaterial, materialContext } = await import('@/api/material')
      return fetchMaterial(id, materialContext(), signal)
    })
  }
  if (!url?.startsWith('/static-resource/')) return url
  initialize()
  const file = url.slice('/static-resource/'.length)
  if (!file || /[/\\?#%]/.test(file) || file.includes('..')) return ''
  const path = '/static-resource/' + encodeURIComponent(file)
  return resourceImage(imageScope(), path, async signal => {
    // Avoid the existing axios -> stores -> router -> image utilities initialization cycle.
    const { fetchResourceImage } = await import('@/api/staticResource')
    return fetchResourceImage(path, signal)
  })
}

// Keep the persisted path available for removal; never save the display URL into component data.
export const imageUploadItem = (sourceUrl: string, name?: string) =>
  reactive({ sourceUrl, name, url: computed(() => authenticatedImageUrl(sourceUrl)) })
