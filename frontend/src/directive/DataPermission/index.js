function checkDataPermission(el, binding, vnode) {
  // eslint-disable-next-line no-unused-vars
  const dataPermission = vnode.privileges
  // eslint-disable-next-line no-unused-vars
  const { value } = binding
  // // 数据授权采用并集的方式 部门 角色 用户 有一个有权限即可
  // if (value && value instanceof Array) {
  //   const needPermissions = value
  //   // 满足任意一个即可
  //   const hasPermission = needPermissions.some(needP => {
  //     const result = dataPermission.indexOf(needP) > -1
  //     return result
  //   })
  //   if (!hasPermission) {
  //     el.parentNode && el.parentNode.removeChild(el)
  //   }
  // } else {
  //   throw new Error(`使用方式： v-data-permission="['1:1']"`)
  // }
  el.parentNode && el.parentNode.removeChild(el)
}

export default {
  inserted(el, binding, vnode) {
    checkDataPermission(el, binding, vnode)
  },
  update(el, binding, vnode) {
    checkDataPermission(el, binding, vnode)
  }
}
