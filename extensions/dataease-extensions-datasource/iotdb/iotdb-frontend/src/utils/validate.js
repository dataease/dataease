/**
 * Created by PanJiaChen on 16/11/18.
 */

/**
 * @param {string} path
 * @returns {Boolean}
 */
export function isExternal(path) {
  return /^(https?:|mailto:|tel:)/.test(path) || /^(http?:|mailto:|tel:)/.test(path) || path.startsWith('/api/pluginCommon/staticInfo')
}




