import store from '@/store'

function checkPermission(el, binding) {
  const { value } = binding
  // 我们是基于资源授鉴权 不用角色 因为后期可能有对部门授权 对 人员授权
  const permissions = store.getters && store.getters.permissions
  if (value && value instanceof Array) {
    const needPermissions = value
    // 满足指令中的每个权限才可放行 而不是 满足任意一个即可
    const hasPermission = needPermissions.every(needP => {
      const result = permissions.includes(needP)
      return result
    })
    if (!hasPermission) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  } else {
    throw new Error(`使用方式： v-permission="['user:read']"`)
  }
}

export default {
  inserted(el, binding) {
    checkPermission(el, binding)
  },
  update(el, binding) {
    checkPermission(el, binding)
  }
}
