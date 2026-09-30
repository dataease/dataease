// Only passive raster data URLs are accepted; SVG is reserved for our static placeholder.
export const tableImageUrl = (value: unknown): string => {
  if (typeof value !== 'string') return ''
  const url = value.trim()
  if (/^data:image\/(png|jpeg|gif|webp|bmp|avif);base64,[a-z0-9+/=\s]+$/i.test(url)) return url
  try {
    const parsed = new URL(url, window.location.origin)
    if (!/^https?:$/.test(parsed.protocol) || parsed.username || parsed.password) return ''
    if (!/^https?:\/\//i.test(url) && !/^\/(?!\/)/.test(url)) return ''
    return parsed.href
  } catch {
    return ''
  }
}

export const TABLE_IMAGE_PLACEHOLDER =
  'data:image/svg+xml;charset=utf-8,' +
  encodeURIComponent(
    '<svg xmlns="http://www.w3.org/2000/svg" width="48" height="36" viewBox="0 0 48 36"><rect x="1" y="1" width="46" height="34" rx="3" fill="#f5f6f7" stroke="#8f959e"/><circle cx="14" cy="11" r="3" fill="#8f959e"/><path d="M6 29l12-12 8 8 6-6 10 10" fill="none" stroke="#8f959e" stroke-width="2"/></svg>'
  )
