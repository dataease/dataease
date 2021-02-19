// 根据实际需要修改
const getters = {
  sidebar: state => state.app.sidebar,
  name: state => state.user.name,
  language: state => state.user.language,
  roles: state => state.user.roles,
  permission_routes: state => state.permission.routes,
  license: state => state.license,
  token: state => state.user.token,
}
export default getters
