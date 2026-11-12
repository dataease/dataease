import type { IMaintenancePlan, ITableField } from '@/api/sync/syncTask'

/**
 * 仅比较持久化字段属性，不把页面行状态当作变更
 */
export const fieldSignature = (field: ITableField) =>
  JSON.stringify([
    field.fieldSource,
    field.fieldName,
    field.fieldType,
    Number(field.fieldSize || 0),
    Number(field.fieldPrecision || 0),
    field.remarks || '',
    !!field.fieldPk,
    !!field.fieldIndex
  ])

/**
 * 使用改名步骤关联前后字段，当前维护与历史日志按同一规则展示
 */
export const getFieldChanges = (
  before: ITableField[],
  after: ITableField[],
  steps: IMaintenancePlan['steps']
) => {
  const renames = new Map(
    steps
      .filter(step => step.action === 'RENAME')
      .map(step => [step.before.fieldName, step.after.fieldName] as const)
  )
  const matched = new Set<string>()
  const result: { before?: ITableField; after?: ITableField }[] = []
  before.forEach(field => {
    const next = after.find(
      item => item.fieldName === (renames.get(field.fieldName) || field.fieldName)
    )
    if (next) matched.add(next.fieldName)
    if (!next || fieldSignature(field) !== fieldSignature(next))
      result.push({ before: field, after: next })
  })
  after
    .filter(field => !matched.has(field.fieldName))
    .forEach(field => result.push({ after: field }))
  return result
}
