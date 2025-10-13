export const clearCache = () => {
  const keys = [
    'DataEaseKey',
    'TreeSort-backend',
    'app.desktop',
    'de-global-refresh',
    'open-backend',
    'panel-weight',
    'screen-weight',
    'user.exp',
    'user.language',
    'user.name',
    'user.oid',
    'user.time',
    'user.token',
    'user.uid'
  ]
  keys.forEach(key => {
    localStorage.removeItem(key)
  })
}
