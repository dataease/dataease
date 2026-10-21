export const MAX_MAP_RANGES = 9

export const validMapBoundaries = (values: unknown[]): values is number[] =>
  values.length >= 2 &&
  values.length <= MAX_MAP_RANGES + 1 &&
  values.every(
    (value, index) =>
      typeof value === 'number' &&
      Number.isFinite(value) &&
      (index === 0 || value > (values[index - 1] as number))
  )

export const mapBoundaryMidpoint = (min: number, max: number): number | undefined => {
  if (!Number.isFinite(min) || !Number.isFinite(max) || min >= max) return
  const midpoint = min / 2 + max / 2
  const rounded = Number(midpoint.toPrecision(15))
  if (rounded > min && rounded < max) return rounded
  if (midpoint > min && midpoint < max) return midpoint
}

export const insertMapBoundary = (values: number[], index: number): number[] | undefined => {
  if (
    !validMapBoundaries(values) ||
    values.length > MAX_MAP_RANGES ||
    index < 0 ||
    index >= values.length - 1
  )
    return
  const midpoint = mapBoundaryMidpoint(values[index], values[index + 1])
  if (midpoint === undefined) return
  return [...values.slice(0, index + 1), midpoint, ...values.slice(index + 1)]
}

export const removeMapBoundary = (values: number[], index: number): number[] | undefined => {
  if (index <= 0 || index >= values.length - 1) return
  const next = values.filter((_, i) => i !== index)
  return validMapBoundaries(next) ? next : undefined
}
