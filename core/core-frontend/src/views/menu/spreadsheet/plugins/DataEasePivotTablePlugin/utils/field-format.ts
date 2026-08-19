import type {
  FieldDatePattern,
  FieldDateStyle,
  FieldFormatterConfig,
  FieldItemData
} from '../../../types/plugin'
import { getLocale } from '@/utils/utils'

const EXCEL_EPOCH_UTC = Date.UTC(1899, 11, 30)
const DAY_IN_MS = 24 * 60 * 60 * 1000

export const DEFAULT_DATE_STYLE: FieldDateStyle = 'y_M_d'
export const DEFAULT_DATE_PATTERN: FieldDatePattern = 'date_sub'

export const createDefaultFormatterConfig = (): FieldFormatterConfig => ({
  type: 'auto',
  unitLanguage: ['zh', 'zh-cn', 'zh-CN', 'tw'].includes(getLocale()) ? 'ch' : 'en',
  unit: 1,
  suffix: '',
  decimalCount: 2,
  thousandSeparator: true
})

export const normalizeFormatterConfig = (
  formatter?: Partial<FieldFormatterConfig>
): FieldFormatterConfig => ({
  ...createDefaultFormatterConfig(),
  ...formatter
})

const quoteFormatText = (value: string) => `"${value.replace(/"/g, '""')}"`

const getDecimalPattern = (
  type: FieldFormatterConfig['type'],
  decimalCount: number,
  value?: unknown,
  unit = 1
) => {
  if (type === 'auto') {
    const displayedValue = typeof value === 'number' ? value / unit : value
    if (
      typeof displayedValue === 'number' &&
      Number.isFinite(displayedValue) &&
      Number.isInteger(displayedValue)
    ) {
      return ''
    }
    return '.##########'
  }
  return decimalCount > 0 ? `.${'0'.repeat(decimalCount)}` : ''
}

const getUnitScalePattern = (unit: number) => {
  const scaleMap: Record<number, string> = {
    1: '',
    1000: ',',
    1000000: ',,',
    1000000000: ',,,'
  }
  return scaleMap[unit] ?? ''
}

export const getFieldDisplayScale = (field?: FieldItemData): number => {
  if (field?.groupType !== 'q') {
    return 1
  }

  const config = normalizeFormatterConfig(field.formatterCfg)
  if (config.type === 'percent' || ![10000, 100000000].includes(config.unit)) {
    return 1
  }

  // Excel/Univer 的数字格式只能按 1000 的幂缩放，万、亿需要在显示链路单独换算。
  return config.unit
}

const UNIT_LABEL_MAP: Record<FieldFormatterConfig['unitLanguage'], Record<number, string>> = {
  ch: {
    1000: '千',
    10000: '万',
    1000000: '百万',
    100000000: '亿'
  },
  en: {
    1000: 'K',
    1000000: 'M',
    1000000000: 'B'
  }
}

export const getUnitLabel = (
  unitLanguage: FieldFormatterConfig['unitLanguage'],
  unit: number
): string => UNIT_LABEL_MAP[unitLanguage]?.[unit] ?? ''

export const getNumberFormatPattern = (
  formatter?: Partial<FieldFormatterConfig>,
  value?: unknown
): string => {
  const config = normalizeFormatterConfig(formatter)
  const integerPattern = config.thousandSeparator ? '#,##0' : '0'
  const decimalPattern = getDecimalPattern(
    config.type,
    config.decimalCount,
    value,
    config.unit
  )
  const unitLabel = getUnitLabel(config.unitLanguage, config.unit)
  const unitSuffix = unitLabel ? quoteFormatText(unitLabel) : ''
  const suffix = config.suffix ? quoteFormatText(config.suffix) : ''

  if (config.type === 'percent') {
    return `${integerPattern}${decimalPattern}%${suffix}`
  }

  return `${integerPattern}${decimalPattern}${getUnitScalePattern(config.unit)}${unitSuffix}${suffix}`
}

export const getDateFormatPattern = (
  dateStyle: FieldDateStyle = DEFAULT_DATE_STYLE,
  datePattern: FieldDatePattern = DEFAULT_DATE_PATTERN
): string | undefined => {
  const separator = datePattern === 'date_split' ? '/' : '-'
  const patternMap: Partial<Record<FieldDateStyle, string>> = {
    y: 'yyyy',
    y_M: `yyyy${separator}MM`,
    y_M_d: `yyyy${separator}MM${separator}dd`,
    H_m_s: 'hh:mm:ss',
    y_M_d_H: `yyyy${separator}MM${separator}dd hh`,
    y_M_d_H_m: `yyyy${separator}MM${separator}dd hh:mm`,
    y_M_d_H_m_s: `yyyy${separator}MM${separator}dd hh:mm:ss`
  }
  return patternMap[dateStyle]
}

const parseDateParts = (value: unknown): number[] | undefined => {
  if (value instanceof Date && !Number.isNaN(value.getTime())) {
    return [
      value.getFullYear(),
      value.getMonth() + 1,
      value.getDate(),
      value.getHours(),
      value.getMinutes(),
      value.getSeconds(),
      value.getMilliseconds()
    ]
  }

  const text = String(value).trim()
  const timeMatch = text.match(
    /^(\d{1,2}):(\d{1,2})(?::(\d{1,2})(?:\.(\d{1,3}))?)?$/
  )
  if (timeMatch) {
    const hour = Number(timeMatch[1])
    const minute = Number(timeMatch[2])
    const second = Number(timeMatch[3] ?? 0)
    if (hour > 23 || minute > 59 || second > 59) {
      return undefined
    }
    return [
      1899,
      12,
      30,
      hour,
      minute,
      second,
      Number((timeMatch[4] ?? '0').padEnd(3, '0'))
    ]
  }

  const match = text.match(
    /^(\d{4})(?:[-/](\d{1,2})(?:[-/](\d{1,2}))?)?(?:[ T](\d{1,2})(?::(\d{1,2})(?::(\d{1,2})(?:\.(\d{1,3}))?)?)?)?$/
  )
  if (!match) {
    return undefined
  }

  const parts = [
    Number(match[1]),
    Number(match[2] ?? 1),
    Number(match[3] ?? 1),
    Number(match[4] ?? 0),
    Number(match[5] ?? 0),
    Number(match[6] ?? 0),
    Number((match[7] ?? '0').padEnd(3, '0'))
  ]
  const [year, month, day, hour, minute, second, millisecond] = parts
  const date = new Date(year, month - 1, day, hour, minute, second, millisecond)
  if (
    date.getFullYear() !== year ||
    date.getMonth() !== month - 1 ||
    date.getDate() !== day ||
    date.getHours() !== hour ||
    date.getMinutes() !== minute ||
    date.getSeconds() !== second
  ) {
    return undefined
  }
  return parts
}

const toDateSerial = (value: unknown): number | undefined => {
  const parts = parseDateParts(value)
  if (!parts) {
    return undefined
  }
  const [year, month, day, hour, minute, second, millisecond] = parts
  return (
    (Date.UTC(year, month - 1, day, hour, minute, second, millisecond) - EXCEL_EPOCH_UTC) /
    DAY_IN_MS
  )
}

const toNumber = (value: unknown): number | undefined => {
  if (typeof value === 'number') {
    return Number.isFinite(value) ? value : undefined
  }
  const text = String(value).trim().replace(/,/g, '')
  if (!text) {
    return undefined
  }
  const result = Number(text)
  return Number.isFinite(result) ? result : undefined
}

export const toNativeCellValue = (value: unknown, field?: FieldItemData): unknown => {
  if (value === null || value === undefined) {
    return ''
  }

  if (field?.groupType === 'q') {
    return toNumber(value) ?? value
  }

  if (field?.deType === 1 && getDateFormatPattern(field.dateStyle, field.datePattern)) {
    return toDateSerial(value) ?? value
  }

  return value
}

export const getFieldNumberFormat = (
  field?: FieldItemData,
  value?: unknown
): string | undefined => {
  if (!field) {
    return undefined
  }
  if (field.groupType === 'q') {
    return getNumberFormatPattern(field.formatterCfg, value)
  }
  if (field.deType === 1) {
    return getDateFormatPattern(
      field.dateStyle ?? DEFAULT_DATE_STYLE,
      field.datePattern ?? DEFAULT_DATE_PATTERN
    )
  }
  return undefined
}

export const findConfiguredField = (
  fields: FieldItemData[],
  resultField: {
    id: string | number
    name: string
    dataeaseName: string
  }
): FieldItemData | undefined =>
  fields.find(
    field =>
      String(field.id) === String(resultField.id) ||
      field.dataeaseName === resultField.dataeaseName ||
      field.name === resultField.name
  )
