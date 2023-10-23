export function isExternal(path) {
  return (
    /^(https?:|mailto:|tel:)/.test(path) ||
    /^(http?:|mailto:|tel:)/.test(path) ||
    path.startsWith('/api/pluginCommon/staticInfo')
  )
}

export function validUsername(str) {
  const valid_map = ['admin', 'cyw']
  return valid_map.indexOf(str.trim()) >= 0
}

export const PHONE_REGEX = '^1[3|4|5|7|8][0-9]{9}$'
