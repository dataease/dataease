import { Base64 } from 'js-base64'
import { useI18n } from '@/hooks/web/useI18n'
import type { SpreadsheetJumpField, SpreadsheetJumpParams } from '../../../types/link-jump'

export const invalidJumpParams = (): Error =>
  new Error(useI18n().t('spreadsheet.invalid_jump_params'))

const isId = (value: unknown): value is string | number =>
  (typeof value === 'string' && /^-?\d+$/.test(value)) ||
  (typeof value === 'number' && Number.isSafeInteger(value))

const isValue = (value: unknown): boolean =>
  typeof value === 'string' ||
  typeof value === 'boolean' ||
  (typeof value === 'number' && Number.isFinite(value))

const isField = (value: unknown): value is SpreadsheetJumpField => {
  if (!value || typeof value !== 'object') return false
  const field = value as SpreadsheetJumpField
  if (!isId(field.id)) return false
  if (field.value != null && !isValue(field.value)) return false
  if (Array.isArray(field.timeValue)) {
    if (
      field.timeValue.length !== 2 ||
      !field.timeValue.every(item => typeof item !== 'boolean' && isValue(item))
    )
      return false
  } else if (field.timeValue != null && !isValue(field.timeValue)) {
    return false
  }
  if (field.linkageDateStyle && !['H_m_s', 'M_d'].includes(field.linkageDateStyle)) return false
  if (field.linkageDatePattern && !['date_sub', 'date_split'].includes(field.linkageDatePattern))
    return false
  return true
}

export const parseSpreadsheetJumpParams = (encoded: string): SpreadsheetJumpParams => {
  let params: SpreadsheetJumpParams
  try {
    // URLSearchParams 已解码一次；嵌入 store 沿用仪表板的 URI 编码格式。
    params = JSON.parse(Base64.decode(decodeURIComponent(encoded)))
  } catch {
    throw invalidJumpParams()
  }
  if (
    !params ||
    !isId(params.sourceDvId) ||
    !isId(params.sourceViewId) ||
    !isId(params.sourceFieldId) ||
    (params.sourceResourceTable !== undefined &&
      !['core', 'snapshot'].includes(params.sourceResourceTable)) ||
    !Array.isArray(params.dimensionList) ||
    !Array.isArray(params.quotaList) ||
    !params.dimensionList.every(isField) ||
    !params.quotaList.every(isField)
  )
    throw invalidJumpParams()
  // shortcut: 分组自定义筛选需按实例字段副本重写整棵条件树，支持前明确拒绝，不能降级成等值取数。
  if (params.customFilter && Object.keys(params.customFilter).length) {
    throw new Error(useI18n().t('spreadsheet.unsupported_jump_filter'))
  }
  return params
}
