type MapPictureExporter = (signal: AbortSignal) => Promise<string | undefined>

interface MapExportEntry {
  exportPicture: MapPictureExporter
  controller?: AbortController
}

const mapExports = new Map<HTMLElement, MapExportEntry>()

/**
 * 按实际容器注册，避免同一图表在多个预览实例中互相影响
 */
export function registerMapExport(container: HTMLElement, exportPicture: MapPictureExporter) {
  const entry: MapExportEntry = { exportPicture }
  mapExports.set(container, entry)
  return () => {
    entry.controller?.abort()
    if (mapExports.get(container) === entry) mapExports.delete(container)
  }
}

/**
 * 等待地图取图和解码后截图，并在失败、超时或卸载时恢复现场
 */
export async function withMapPictures<T>(root: HTMLElement, capture: () => Promise<T>): Promise<T> {
  const entries = Array.from(mapExports).filter(
    ([container]) => root.contains(container) && container.getClientRects().length > 0
  )
  if (entries.some(([, entry]) => entry.controller)) {
    throw new Error('Map export is already in progress')
  }
  const controller = new AbortController()
  const { signal } = controller
  const restores: Array<() => void> = []
  let rejectAborted: (() => void) | undefined
  const aborted = new Promise<never>((_, reject) => {
    rejectAborted = () => reject(new Error('Map export cancelled or timed out'))
    signal.addEventListener('abort', rejectAborted, { once: true })
  })
  const timer = setTimeout(() => controller.abort(), 30000)
  entries.forEach(([, entry]) => (entry.controller = controller))
  try {
    await Promise.race([
      aborted,
      Promise.all(
        entries.map(async ([container, entry]) => {
          const dataUrl = await entry.exportPicture(signal)
          if (!dataUrl || signal.aborted) return
          const image = document.createElement('img')
          image.src = dataUrl
          await image.decode()
          if (signal.aborted || !root.contains(container)) return
          const layers = Array.from(
            container.querySelectorAll<HTMLElement>('.l7-scene, .amap-maps')
          )
          const displays = layers.map(layer => layer.style.display)
          restores.push(() => {
            image.remove()
            layers.forEach((layer, index) => (layer.style.display = displays[index]))
          })
          image.classList.add('prepare-picture-img')
          Object.assign(image.style, {
            width: '100%',
            height: '100%',
            position: 'absolute',
            objectFit: 'cover',
            zIndex: '2'
          })
          container.appendChild(image)
          layers.forEach(layer => (layer.style.display = 'none'))
        })
      )
    ])
    clearTimeout(timer)
    return await Promise.race([aborted, capture()])
  } finally {
    // 取消尚未结束的取图，阻止迟到结果再次改动 DOM
    controller.abort()
    clearTimeout(timer)
    if (rejectAborted) signal.removeEventListener('abort', rejectAborted)
    restores.reverse().forEach(restore => restore())
    entries.forEach(([, entry]) => {
      if (entry.controller === controller) entry.controller = undefined
    })
  }
}
