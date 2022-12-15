
export const analyzingIconForIconfont = function(json) {
  let font_family = ''
  let css_prefix_text = ''
  let list = []
  if (json) {
    if (json.font_family) {
      font_family = json.font_family
    }
    if (json.css_prefix_text) {
      css_prefix_text = json.css_prefix_text
    }
    if (json.glyphs) {
      list = json.glyphs.map(function(value, index, array) {
        return font_family + ' ' + css_prefix_text + value.font_class
      })
    }
  }
  return {
    font_family,
    css_prefix_text,
    list
  }
}

export const eIconSymbol = function(json) {
  let font_family = ''
  let css_prefix_text = ''
  let list = []
  if (json) {
    if (json.font_family) {
      font_family = json.font_family
    }
    if (json.css_prefix_text) {
      css_prefix_text = json.css_prefix_text
    }
    if (json.glyphs) {
      list = json.glyphs.map(function(value, index, array) {
        return '#' + css_prefix_text + value.font_class
      })
    }
  }
  return {
    font_family,
    css_prefix_text,
    list
  }
}

export function isExternal(path) {
  return /^(https?:|data:|\/\/?)/.test(path)
}
export const isServer = typeof window === 'undefined'
