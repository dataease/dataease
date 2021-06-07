import store from '@/store'
export function checkPermission(pers) {
  const permissions = store.getters.permissions
  const hasPermission = pers.every(needP => {
    const result = permissions.includes(needP)
    return result
  })
  return hasPermission
}

export function hasDataPermission(pTarget, pSource) {
  debugger
  if (pSource && pTarget) {
    return pSource.indexOf(pTarget) > -1
  }
  return false
}
