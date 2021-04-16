/**
 * fieldId 字段ID
 * value 字段值
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
