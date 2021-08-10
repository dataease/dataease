
import { Condition } from '@/components/widget/bean/Condition'
/**
 * 判断两个conditions数组是否相同
 * @param {*} conditions1
 * @param {*} conditions2
 * @returns
 */
export const isChange = (conditions1, conditions2) => {
  // 两个都null
  if (!conditions1 && !conditions2) return false
  if (!conditions1 || !conditions2) return true
  // 数组长度不一样 肯定发生了改变
  if (conditions1.length !== conditions2.length) return true
  let arr1 = JSON.parse(JSON.stringify(conditions1))
  let arr2 = JSON.parse(JSON.stringify(conditions2))
  arr1 = arr1.sort((s1, s2) => s1.componentId > s2.componentId)
  arr2 = arr2.sort((s1, s2) => s1.componentId > s2.componentId)
  return JSON.stringify(arr1) !== JSON.stringify(arr2)
}

export const valueValid = condition => {
  return condition && condition.value && condition.value.length > 0 && condition.value[0]
}

export const formatCondition = obj => {
  const { component, value, operator } = obj
  const fieldId = component.options.attrs.fieldId
  const viewIds = component.options.attrs.viewIds
  const condition = new Condition(component.id, fieldId, operator, value, viewIds)
  return condition
}

export const formatLinkageCondition = obj => {
  const { viewIds, fieldId, value, operator } = obj
  const condition = new Condition(null, fieldId, operator, value, viewIds)
  return condition
}
