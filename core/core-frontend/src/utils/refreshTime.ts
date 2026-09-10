// Keep refresh intervals within the editor's supported range. Both seconds and
// minutes also fit the backend Integer field and the browser timer delay.
export const MIN_REFRESH_TIME = 1
export const MAX_REFRESH_TIME = 3600

export const normalizeRefreshTime = (value: unknown): number => {
  const time = Number(value)
  if (Number.isNaN(time)) return MIN_REFRESH_TIME
  return Math.min(MAX_REFRESH_TIME, Math.max(MIN_REFRESH_TIME, Math.trunc(time)))
}
