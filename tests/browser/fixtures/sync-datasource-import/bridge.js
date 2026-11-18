export const useI18n = () => ({ t: (key) => key })
export const useCache = () => ({
  wsCache: {
    set(key, value) {
      ;(window.importCacheEvents ||= []).push({ key, value })
    }
  }
})
const post = async (path, data) => {
  const response = await fetch(path, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  })
  if (!response.ok) throw new Error('Synthetic API failure')
  return response.json()
}
export const getDsTree = (data) => post('/mock/tree', data)
export const checkRepeat = (data) => post('/mock/checkRepeat', data)
export const save = (data) => post('/mock/save', data)
