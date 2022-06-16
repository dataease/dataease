function checkDataPermission(el, binding, vnode) {
  // eslint-disable-next-line no-unused-vars
  const dataPermission = vnode.privileges
  // eslint-disable-next-line no-unused-vars
  const { value } = binding
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
