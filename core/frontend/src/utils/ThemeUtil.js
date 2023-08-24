const styleId = 'dataease-wggznb'

export function initTheme() {
  const json = localStorage.getItem('theme-css-text')
  if (!json) return
  const { cssText, themeId } = JSON.parse(json)
  if (!themeId) return

  let style = document.getElementById(styleId)
  if (themeId === 1) {
    document.body.className = ''
    style && document.getElementById(styleId).parentNode.removeChild(document.getElementById(styleId))
    return
  }

  document.body.className = 'blackTheme'

  if (style) {
    style.innerText = cssText
  } else {
    style = document.createElement('style')
    style.id = styleId
    style.innerText = cssText
    document.head.appendChild(style)
  }
}

