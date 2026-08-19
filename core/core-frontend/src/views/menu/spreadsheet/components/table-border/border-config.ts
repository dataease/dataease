import { BorderStyleTypes } from '@univerjs/core'

export const TABLE_BORDER_POSITIONS = [
  'left',
  'vertical',
  'right',
  'top',
  'horizontal',
  'bottom'
] as const

export type TableBorderPosition = (typeof TABLE_BORDER_POSITIONS)[number]
export type TableBorderPreset = 'all' | 'inside' | 'outside'
export type TableBorderIconName =
  | 'all'
  | 'inside'
  | 'outside'
  | 'left'
  | 'vertical'
  | 'right'
  | 'top'
  | 'horizontal'
  | 'bottom'

export interface TableBorderConfig extends Record<TableBorderPosition, boolean> {
  color: string
  style: BorderStyleTypes
}

export const TABLE_BORDER_STYLE_OPTIONS = [
  BorderStyleTypes.THIN,
  BorderStyleTypes.HAIR,
  BorderStyleTypes.DOTTED,
  BorderStyleTypes.DASHED,
  BorderStyleTypes.DASH_DOT,
  BorderStyleTypes.DASH_DOT_DOT,
  BorderStyleTypes.MEDIUM,
  BorderStyleTypes.MEDIUM_DASHED,
  BorderStyleTypes.MEDIUM_DASH_DOT,
  BorderStyleTypes.MEDIUM_DASH_DOT_DOT,
  BorderStyleTypes.THICK,
  BorderStyleTypes.DOUBLE
] as const

const TABLE_BORDER_PRESETS: Record<
  TableBorderPreset,
  Record<TableBorderPosition, boolean>
> = {
  all: {
    left: true,
    vertical: true,
    right: true,
    top: true,
    horizontal: true,
    bottom: true
  },
  inside: {
    left: false,
    vertical: true,
    right: false,
    top: false,
    horizontal: true,
    bottom: false
  },
  outside: {
    left: true,
    vertical: false,
    right: true,
    top: true,
    horizontal: false,
    bottom: true
  }
}

export const createDefaultTableBorderConfig = (): TableBorderConfig => ({
  left: false,
  vertical: false,
  right: false,
  top: false,
  horizontal: false,
  bottom: false,
  color: '#000000',
  style: BorderStyleTypes.THIN
})

const isSupportedBorderStyle = (style: unknown): style is BorderStyleTypes =>
  TABLE_BORDER_STYLE_OPTIONS.some(option => option === style)

export const normalizeTableBorderConfig = (
  value?: Partial<TableBorderConfig> | null
): TableBorderConfig => {
  const normalized = createDefaultTableBorderConfig()

  TABLE_BORDER_POSITIONS.forEach(position => {
    if (typeof value?.[position] === 'boolean') {
      normalized[position] = value[position]
    }
  })

  if (typeof value?.color === 'string' && value.color.trim()) {
    normalized.color = value.color
  }

  if (isSupportedBorderStyle(value?.style)) {
    normalized.style = value.style
  }

  return normalized
}

export const applyTableBorderPreset = (
  value: Partial<TableBorderConfig> | null | undefined,
  preset: TableBorderPreset
): TableBorderConfig => ({
  ...normalizeTableBorderConfig(value),
  ...TABLE_BORDER_PRESETS[preset]
})

export const clearTableBorders = (
  value?: Partial<TableBorderConfig> | null
): TableBorderConfig => ({
  ...normalizeTableBorderConfig(value),
  left: false,
  vertical: false,
  right: false,
  top: false,
  horizontal: false,
  bottom: false
})

export const toggleTableBorderPosition = (
  value: Partial<TableBorderConfig> | null | undefined,
  position: TableBorderPosition
): TableBorderConfig => {
  const normalized = normalizeTableBorderConfig(value)
  return {
    ...normalized,
    [position]: !normalized[position]
  }
}

export const updateTableBorderColor = (
  value: Partial<TableBorderConfig> | null | undefined,
  color: string
): TableBorderConfig => {
  const normalized = normalizeTableBorderConfig(value)
  return typeof color === 'string' && color.trim()
    ? { ...normalized, color }
    : normalized
}

export const updateTableBorderStyle = (
  value: Partial<TableBorderConfig> | null | undefined,
  style: BorderStyleTypes
): TableBorderConfig => {
  const normalized = normalizeTableBorderConfig(value)
  return isSupportedBorderStyle(style)
    ? { ...normalized, style }
    : normalized
}
