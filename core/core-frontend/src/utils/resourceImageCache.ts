import { nextTick, shallowReactive } from 'vue'
import { isAxiosError } from 'axios'

interface ImageEntry {
  url: string
  failed: boolean
  ready: Promise<void>
  controller: AbortController
}
const images = shallowReactive(new Map<string, ImageEntry>())
let currentScope = ''
let sequence = 0

const placeholder = (failed = false) => {
  const content = failed ? '<path d="M4 4L20 20M20 4L4 20" stroke="#999"/>' : ''
  return (
    'data:image/svg+xml;base64,' +
    btoa(
      `<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24">${content}<!--${++sequence}--></svg>`
    )
  )
}

export const clearResourceImages = () => {
  for (const entry of images.values()) entry.controller.abort()
  images.clear()
  currentScope = ''
}

const asDataUrl = (blob: Blob): Promise<string> =>
  new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(String(reader.result))
    reader.onerror = () => reject(reader.error)
    reader.readAsDataURL(blob)
  })

// Data URLs are transient display values. ElUpload revokes blob URLs on removal, so a shared
// object URL would let an upload preview invalidate another component's displayed image.
export const resourceImage = (
  scope: string,
  path: string,
  load: (signal: AbortSignal) => Promise<Blob>
): string => {
  if (scope !== currentScope) {
    clearResourceImages()
    currentScope = scope
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
    const { signal } = pending.controller
    pending.ready = Promise.resolve().then(async () => {
      try {
        let blob: Blob
        try {
          blob = await load(signal)
        } catch (error) {
          // Retry a transient transport/server failure once; never retry authentication failures.
          if (
            signal.aborted ||
            !isAxiosError(error) ||
            error.code === 'ERR_CANCELED' ||
            (error.response && error.response.status < 500)
          )
            throw error
          blob = await load(signal)
        }
        if (signal.aborted) return
        if (!blob.type.startsWith('image/')) throw new Error('Expected an image resource')
        const url = await asDataUrl(blob)
        if (images.get(path) === pending) pending.url = url
      } catch {
        if (images.get(path) === pending && !signal.aborted) {
          pending.failed = true
          pending.url = placeholder(true)
        }
      }
    })
  }
  return entry.url
}

// Only wait for images used by this export. A failed preview elsewhere must not block it.
export const waitForResourceImages = async (root: HTMLElement) => {
  await nextTick()
  const entries = new Map(Array.from(images.values(), entry => [entry.url, entry]))
  if (entries.size === 0) return

  const used = new Set<ImageEntry>()
  const addUsed = (url: string) => {
    const entry = entries.get(url)
    if (entry) used.add(entry)
  }
  // Authenticated images are bound to img src or inline backgrounds. Read those bindings
  // without resolving computed styles for every chart label and canvas descendant.
  const candidates = root.querySelectorAll<HTMLElement | SVGElement>('img, [style]')
  for (const element of [root, ...Array.from(candidates)]) {
    if (element instanceof HTMLImageElement) addUsed(element.src)
    for (const match of element.style.backgroundImage.matchAll(
      /url\(\s*(?:"([^"]*)"|'([^']*)'|([^)]*?))\s*\)/g
    )) {
      addUsed(match[1] ?? match[2] ?? match[3].trim())
    }
  }
  await Promise.all(Array.from(used, entry => entry.ready))
  await nextTick()
  if (Array.from(used).some(entry => entry.failed || entry.controller.signal.aborted)) {
    throw new Error('An authenticated image could not be loaded; export cancelled')
  }
  const loadedUrls = new Set(Array.from(used, entry => entry.url))
  const imageElements = Array.from(root.querySelectorAll('img'))
  if (root instanceof HTMLImageElement) imageElements.push(root)
  await Promise.all(imageElements.filter(img => loadedUrls.has(img.src)).map(img => img.decode()))
}
