const defaultLayout = {
  panel: '260px',
  dataset: '260px',
  datasource: '260px',
  system: '260px'
}
const STORAGE_KEY = 'global_layout'

export const getLayout = (type) => {
  const key = STORAGE_KEY + '_' + type
  return localStorage.getItem(key) || defaultLayout[type]
}

export const setLayout = (type, val) => {
  const key = STORAGE_KEY + '_' + type
  localStorage.setItem(key, val || defaultLayout[type])
}
