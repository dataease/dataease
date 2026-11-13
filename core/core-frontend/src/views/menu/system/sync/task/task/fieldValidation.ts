import type { ITableField } from '@/api/sync/syncTask'

/**
 * 来源主键计入同步字段，系统生成字段不作为来源映射
 */
export const hasSourceField = (fields: ITableField[]) =>
  fields.some(field => !!field.fieldSource?.trim())
