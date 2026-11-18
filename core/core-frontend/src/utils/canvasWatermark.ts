interface CanvasWatermarkInfo {
  settingContent?: unknown
  [key: string]: unknown
}

// Watermark metadata is optional; malformed content must not block the canvas.
// Normalize a copy so the original response and persisted configuration stay intact.
export function normalizeCanvasWatermark(info?: CanvasWatermarkInfo | null) {
  if (!info) return null
  try {
    const content = info.settingContent
    const settings =
      typeof content === 'string' ? (content ? JSON.parse(content) : {}) : content ?? {}
    if (typeof settings !== 'object' || settings === null || Array.isArray(settings)) {
      throw new TypeError('Invalid watermark configuration')
    }
    return { ...info, settingContent: settings }
  } catch {
    console.warn('Invalid watermark configuration; watermark skipped for this canvas load.')
    return null
  }
}
