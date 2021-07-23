/**
 * fieldId 字段ID
 * value 字段值
 * operator 操作[eq, ne, gt, ge, lt, le, in, not in, like, not like, between]
 * viewIds 过滤视图范围
 */
export class Condition {
  constructor(componentId, fieldId, operator, value, viewIds) {
    this.componentId = componentId
    this.fieldId = fieldId
    this.operator = operator || 'eq'
    this.value = value
    this.viewIds = viewIds
  }
}
