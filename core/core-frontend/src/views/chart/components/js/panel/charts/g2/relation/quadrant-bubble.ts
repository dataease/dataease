export const DEFAULT_QUADRANT_BUBBLE_RANGE = [5, 30] as const

export const isValidBubbleRange = (min: unknown, max: unknown): boolean =>
  typeof min === 'number' &&
  typeof max === 'number' &&
  Number.isInteger(min) &&
  Number.isInteger(max) &&
  min >= 1 &&
  min <= 50 &&
  max >= 5 &&
  max <= 100 &&
  min <= max

export const bubbleValue = (value: unknown): number | undefined => {
  if (typeof value !== 'number' && typeof value !== 'string') return undefined
  if (typeof value === 'string' && !value.trim()) return undefined
  const number = Number(value)
  return Number.isFinite(number) && number >= 0 ? number : undefined
}

/**
 * 在完整查询结果上确定值域，图例筛选与提示轮播仅消费已计算的半径
 */
export const createBubbleRadius = (
  values: unknown[],
  config: ChartBasicStyle['quadrantBubble'],
  scale = 1
): ((value: unknown) => number) => {
  const [minRadius, maxRadius] =
    config?.mode === 'custom' && isValidBubbleRange(config.min, config.max)
      ? [config.min, config.max]
      : DEFAULT_QUADRANT_BUBBLE_RANGE
  let minValue = Infinity
  let maxValue = -Infinity
  for (const value of values) {
    const number = bubbleValue(value)
    if (number !== undefined) {
      minValue = Math.min(minValue, number)
      maxValue = Math.max(maxValue, number)
    }
  }
  const factor = Number.isFinite(scale) && scale > 0 ? scale : 1
  return value => {
    const number = bubbleValue(value)
    if (number === undefined || minValue === Infinity) return minRadius * factor
    const ratio =
      minValue === maxValue
        ? 0.5
        : Math.max(0, Math.min(1, (number - minValue) / (maxValue - minValue)))
    return Math.sqrt(minRadius ** 2 + ratio * (maxRadius ** 2 - minRadius ** 2)) * factor
  }
}
