import { HorizontalAlign, TextDecoration, VerticalAlign, WrapStrategy } from '@univerjs/core'
import type { IStyleData } from '@univerjs/core'
import type { Color, Style } from 'exceljs'

const themeSlots = [
  'lt1',
  'dk1',
  'lt2',
  'dk2',
  'accent1',
  'accent2',
  'accent3',
  'accent4',
  'accent5',
  'accent6',
  'hlink',
  'folHlink'
]
const officeColors =
  'FFFFFF 000000 EEECE1 1F497D 4F81BD C0504D 9BBB59 8064A2 4BACC6 F79646 0000FF 800080'.split(' ')
const indexedColors = (
  '000000 FFFFFF FF0000 00FF00 0000FF FFFF00 FF00FF 00FFFF ' +
  '000000 FFFFFF FF0000 00FF00 0000FF FFFF00 FF00FF 00FFFF ' +
  '800000 008000 000080 808000 800080 008080 C0C0C0 808080 ' +
  '9999FF 993366 FFFFCC CCFFFF 660066 FF8080 0066CC CCCCFF ' +
  '000080 FF00FF FFFF00 00FFFF 800080 800000 008080 0000FF ' +
  '00CCFF CCFFFF CCFFCC FFFF99 99CCFF FF99CC CC99FF FFCC99 ' +
  '3366FF 33CCCC 99CC00 FFCC00 FF9900 FF6600 666699 969696 ' +
  '003366 339966 003300 333300 993300 993366 333399 333333'
).split(' ')

function themePalette(theme?: string) {
  const scheme = theme?.match(
    /<(?:[\w.-]+:)?clrScheme\b[^>]*>([\s\S]*?)<\/(?:[\w.-]+:)?clrScheme>/
  )?.[1]
  if (!scheme) return officeColors
  return themeSlots.map((slot, index) => {
    const startTag = new RegExp('<(?:[\\w.-]+:)?' + slot + '\\s*>\\s*<[^>]+>')
    const source = scheme.match(startTag)?.[0]
    return source?.match(/\b(?:lastClr|val)=["']([a-f\d]{6})["']/i)?.[1] || officeColors[index]
  })
}

type ExcelColor = Partial<Color> & { indexed?: number; tint?: number }

function colorFromExcel(color: ExcelColor, palette: string[]) {
  if (!color) return
  let hex = color.argb?.slice(-6)
  if (!hex && color.theme !== undefined) hex = palette[color.theme]
  if (!hex && color.indexed !== undefined) hex = indexedColors[color.indexed]
  if (!hex || !/^[a-f\d]{6}$/i.test(hex)) return
  if (!color.tint) return { rgb: '#' + hex }
  // OOXML 的 tint 改变 HSL 亮度，保留色相和饱和度。
  const channels = hex.match(/../g).map(part => parseInt(part, 16) / 255)
  const light = (Math.max(...channels) + Math.min(...channels)) / 2
  const tint = Math.max(-1, Math.min(1, color.tint))
  const next = light + tint * (tint < 0 ? light : 1 - light)
  const scale = Math.min(next, 1 - next) / (Math.min(light, 1 - light) || 1)
  const rgb = channels.map(channel => Math.round((next + (channel - light) * scale) * 255))
  return { rgb: '#' + rgb.map(channel => channel.toString(16).padStart(2, '0')).join('') }
}

const horizontal = {
  left: HorizontalAlign.LEFT,
  center: HorizontalAlign.CENTER,
  centerContinuous: HorizontalAlign.CENTER,
  right: HorizontalAlign.RIGHT,
  justify: HorizontalAlign.JUSTIFIED,
  distributed: HorizontalAlign.DISTRIBUTED
}
const vertical = {
  top: VerticalAlign.TOP,
  middle: VerticalAlign.MIDDLE,
  bottom: VerticalAlign.BOTTOM
}

export function createStyleConverter(theme?: string) {
  const palette = themePalette(theme)
  const cache = new WeakMap<Partial<Style>, IStyleData | undefined>()
  return (source: Partial<Style>): IStyleData | undefined => {
    if (!source || cache.has(source)) return source && cache.get(source)
    const style: IStyleData = {}
    const { font, fill, alignment, numFmt } = source
    if (font) {
      if (font.name) style.ff = font.name
      if (font.size) style.fs = font.size
      if (font.bold) style.bl = 1
      if (font.italic) style.it = 1
      if (font.underline && font.underline !== 'none') {
        const double = font.underline === 'double' || font.underline === 'doubleAccounting'
        style.ul = { s: 1, t: double ? TextDecoration.DOUBLE : TextDecoration.SINGLE }
      }
      if (font.strike) style.st = { s: 1 }
      const color = colorFromExcel(font.color, palette)
      if (color) style.cl = color
    }
    if (fill?.type === 'pattern' && fill.pattern === 'solid') {
      const background = colorFromExcel(fill.fgColor, palette)
      if (background) style.bg = background
    }
    if (numFmt && numFmt.toLowerCase() !== 'general') style.n = { pattern: numFmt }
    if (alignment?.horizontal && horizontal[alignment.horizontal])
      style.ht = horizontal[alignment.horizontal]
    if (alignment?.vertical && vertical[alignment.vertical]) style.vt = vertical[alignment.vertical]
    if (alignment?.wrapText) style.tb = WrapStrategy.WRAP
    const converted = Object.keys(style).length ? style : undefined
    cache.set(source, converted)
    return converted
  }
}
