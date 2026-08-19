const STYLE_ID = 'dataease-slash-cell-native-diagonal-hider'

const CSS = `
.univer-box-border.univer-grid.univer-grid-cols-5 > a:nth-child(n+11) {
  display: none !important;
}
`

export class SlashCellStyleHiderService {
  private styleElement?: HTMLStyleElement

  mount(): void {
    if (typeof document === 'undefined') {
      return
    }
    const existing = document.getElementById(STYLE_ID) as HTMLStyleElement | null
    if (existing) {
      this.styleElement = existing
      return
    }
    const style = document.createElement('style')
    style.id = STYLE_ID
    style.textContent = CSS
    document.head.appendChild(style)
    this.styleElement = style
  }

  dispose(): void {
    this.styleElement?.remove()
    this.styleElement = undefined
  }
}
