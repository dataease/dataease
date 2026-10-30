export const PATH_URL = '/api'
export const clearResourceImages = () => {
  // Resource images are outside this isolated language fixture.
}
export const clearBindings = () => {
  // Platform bindings are outside this isolated language fixture.
}
export const Icon = { render: () => null }
export const usePermissionStoreWithOut = () => ({
  $reset() {
    sessionStorage.setItem(
      'permissionResets',
      String(Number(sessionStorage.getItem('permissionResets') || 0) + 1)
    )
  }
})
export default {
  async get({ url }) {
    if (url === '/user/info')
      return {
        data: {
          id: '1',
          language: JSON.parse(sessionStorage.getItem('scenario')).user
        }
      }
    if (url !== '/sysParameter/i18nOptions')
      throw new Error(`Unexpected API: ${url}`)
    const scenario = JSON.parse(sessionStorage.getItem('scenario'))
    if (scenario.failOptions) throw new Error('Language options unavailable')
    return { data: scenario.custom || {} }
  },
  async post({ url, data }) {
    if (url !== '/user/switchLanguage')
      throw new Error(`Unexpected API: ${url}`)
    const posts = JSON.parse(sessionStorage.getItem('posts') || '[]')
    posts.push(data)
    sessionStorage.setItem('posts', JSON.stringify(posts))
    const scenario = JSON.parse(sessionStorage.getItem('scenario'))
    if (scenario.switchNetworkFailure)
      throw new Error('Synthetic network failure')
    if (scenario.switchFailure)
      return { code: 1, msg: 'Synthetic server rejection' }
    if (scenario.switchEmptyResponse) return undefined
    scenario.user = data.lang
    sessionStorage.setItem('scenario', JSON.stringify(scenario))
    return { code: 0 }
  }
}
