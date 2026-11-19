const DEDUPE_WINDOW = 3000
const MAX_ENTRIES = 50
const recentMessages = new Map<string, number>()

export const markErrorToastShown = (message: string) => {
  if (!message) return
  const now = Date.now()
  recentMessages.set(message, now)
  if (recentMessages.size > MAX_ENTRIES) {
    recentMessages.forEach((shownAt, key) => {
      if (now - shownAt >= DEDUPE_WINDOW) recentMessages.delete(key)
    })
  }
}

export const isErrorToastRecentlyShown = (message: string) => {
  if (!message) return false
  const shownAt = recentMessages.get(message)
  if (shownAt === undefined) return false
  if (Date.now() - shownAt >= DEDUPE_WINDOW) {
    recentMessages.delete(message)
    return false
  }
  return true
}
