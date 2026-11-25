import { effectScope, nextTick, shallowReactive, watch } from 'vue'
import { useCache } from '@/hooks/web/useCache'
import { useEmbedded } from '@/store/modules/embedded'
import { useLinkStoreWithOut } from '@/store/modules/link'
import { useUserStoreWithOut } from '@/store/modules/user'

interface ImageEntry {
  url: string
  failed: boolean
  ready: Promise<void>
  controller: AbortController
}
const images = shallowReactive(new Map<string, ImageEntry>())
const { wsCache } = useCache()
let scope = ''
let initialized = false
let sequence = 0
const identity = () => {
  const user = useUserStoreWithOut()
  const embedded = useEmbedded()
  return JSON.stringify([
    user.getToken,
    user.getUid,
    user.getOid,
    wsCache.get('user.token'),
    wsCache.get('user.oid'),
    wsCache.get('user.proxyInfo'),
    useLinkStoreWithOut().getLinkToken,
    embedded.token,
    embedded.baseUrl,
    wsCache.get('app.desktop'),
    window.location.hash
  ])
}
export const clearResourceImages = () => {
  for (const entry of images.values()) entry.controller.abort()
  images.clear()
  scope = ''
}
const placeholder = (failed = false) =>
  'data:image/svg+xml;base64,' +
  btoa(
    `<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24">${
      failed ? '<path d="M4 4L20 20M20 4L4 20" stroke="#999"/>' : ''
    }<!--${++sequence}--></svg>`
  )
const asDataUrl = (blob: Blob): Promise<string> =>
  new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(String(reader.result))
    reader.onerror = () => reject(reader.error)
    reader.readAsDataURL(blob)
  })
export const authenticatedImageUrl = (path: string): string => {
  if (!path?.startsWith('/static-resource/')) return path
  const file = path.slice('/static-resource/'.length)
  if (!file || /[/\\?#%]/.test(file) || file.includes('..')) return ''
  if (!initialized) {
    initialized = true
    effectScope(true).run(() => watch(identity, clearResourceImages, { flush: 'sync' }))
    window.addEventListener('pagehide', clearResourceImages)
    window.addEventListener('hashchange', clearResourceImages)
    window.addEventListener('popstate', clearResourceImages)
    window.addEventListener('storage', () => {
      // Other tabs may write unrelated metadata without changing image authorization.
      if (scope && identity() !== scope) clearResourceImages()
    })
  }
  const current = identity()
  if (scope !== current) {
    clearResourceImages()
    scope = current
  }
  let entry = images.get(path)
  if (!entry) {
    entry = shallowReactive({
      url: placeholder(),
      failed: false,
      ready: Promise.resolve(),
      controller: new AbortController()
    })
    images.set(path, entry)
    const pending = entry
    pending.ready = Promise.resolve().then(async () => {
      try {
        // Dynamic import avoids axios -> stores -> router -> image utilities cycles.
        const { fetchResourceImage } = await import('@/api/staticResource')
        const blob = await fetchResourceImage(
          '/static-resource/' + encodeURIComponent(file),
          pending.controller.signal
        )
        if (!blob.type.startsWith('image/')) throw new Error('Expected an image resource')
        const url = await asDataUrl(blob)
        if (images.get(path) === pending) pending.url = url
      } catch {
        if (images.get(path) === pending && !pending.controller.signal.aborted) {
          pending.failed = true
          pending.url = placeholder(true)
        }
      }
    })
  }
  // Data URLs are display values only; keep persisted background paths unchanged.
  return entry.url
}
export const waitForResourceImages = async (root: HTMLElement) => {
  await nextTick()
  const entries = new Map(Array.from(images.values(), entry => [entry.url, entry]))
  const used = new Set<ImageEntry>()
  const add = (url: string) => {
    const entry = entries.get(url)
    if (entry) used.add(entry)
  }
  for (const element of [root, ...Array.from(root.querySelectorAll<HTMLElement>('img, [style]'))]) {
    if (element instanceof HTMLImageElement) add(element.src)
    for (const match of element.style.backgroundImage.matchAll(
      /url\(\s*(?:"([^"]*)"|'([^']*)'|([^)]*?))\s*\)/g
    ))
      add(match[1] ?? match[2] ?? match[3].trim())
  }
  await Promise.all(Array.from(used, entry => entry.ready))
  await nextTick()
  if (Array.from(used).some(entry => entry.failed || entry.controller.signal.aborted))
    throw new Error('An authenticated image could not be loaded; export cancelled')
  const urls = new Set(Array.from(used, entry => entry.url))
  const elements = Array.from(root.querySelectorAll('img'))
  if (root instanceof HTMLImageElement) elements.push(root)
  await Promise.all(elements.filter(img => urls.has(img.src)).map(img => img.decode()))
}
