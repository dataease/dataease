import { interactiveStoreWithOut } from '@/store/modules/interactive'
const interactiveStore = interactiveStoreWithOut()
const flagArray = ['panel', 'screen', 'dataset', 'datasource']

export const checkPermission = (el, binding) => {
  const { value } = binding
  if (!Array.isArray(value) || value.length === 0) {
    el.parentNode?.removeChild(el)
    return
  }
  const data = interactiveStore.getData
  const permissionData = {}
  flagArray.forEach((item, index) => {
    permissionData[item] = data[index]
  })
  const needPermissions = value
  // 满足指令中的每个权限才可放行 而不是 满足任意一个即可
  const hasPermission = Array.from(needPermissions).every(needP => {
    if (!flagArray.includes(needP)) return false
    const result =
      permissionData &&
      permissionData[needP] &&
      permissionData[needP]['menuAuth'] &&
      permissionData[needP]['anyManage']
    return result
  })
  if (!hasPermission) {
    el.parentNode && el.parentNode.removeChild(el)
  }
}
