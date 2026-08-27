import type { IStyleData } from '@univerjs/core'
import type { SlashCellType } from '../types'
import { parseSlashCellParts } from '../utils/parse-slash-cell-parts.ts'

export interface SlashCellRenderRect {
  x: number
  y: number
  width: number
  height: number
}

export interface SlashCellRenderPayload {
  type: SlashCellType
  parts: string[]
  rect: SlashCellRenderRect
  style?: IStyleData
}

export interface SlashCellTextLayout {
  lowerLeft: string
  upperRight: string
  center: string
}

interface SlashCellCustomRenderInfo {
  style?: IStyleData
  primaryWithCoord?: {
    startX?: number
    endX?: number
    startY?: number
    endY?: number
    mergeInfo?: {
      startX?: number
      endX?: number
      startY?: number
      endY?: number
    }
  }
}

type SlashCellTextVerticalAlign = 'top' | 'center' | 'bottom'

type CanvasLikeContext = CanvasRenderingContext2D & {
  save?: () => void
  restore?: () => void
}

export class SlashCellRenderService {
  readonly customRenderKey = 'dataease-slash-cell-custom-render'

  parseParts(value: unknown): string[] {
    return parseSlashCellParts(value)
  }

  getTextLayout(type: SlashCellType, value: unknown): SlashCellTextLayout {
    const parts = this.groupParts(value, type === 'three' ? 3 : 2)
    return {
      lowerLeft: parts[0] || '',
      upperRight: parts[1] || '',
      center: type === 'three' ? parts[2] || '' : ''
    }
  }

  createCustomRender(type: SlashCellType, value: unknown) {
    const layout = this.getTextLayout(type, value)

    return this.createCustomRenderByParts(type, [
      layout.lowerLeft,
      layout.upperRight,
      layout.center
    ])
  }

  createCustomRenderByParts(type: SlashCellType, parts: string[]) {
    const normalizedParts = [
      parts[0] || '',
      parts[1] || '',
      type === 'three' ? parts[2] || '' : ''
    ]

    return {
      uKey: this.customRenderKey,
      zIndex: 999,
      drawWith: (ctx: CanvasLikeContext, info: SlashCellCustomRenderInfo) => {
        this.renderSlashCell(ctx, {
          type,
          parts: normalizedParts,
          rect: this.getRect(info),
          style: info.style
        })
      },
      isHit: () => false
    }
  }

  renderSlashCell(ctx: CanvasLikeContext, payload: SlashCellRenderPayload): void {
    const { rect, type, style, parts } = payload
    if (!rect.width || !rect.height) {
      return
    }

    const padding = 4
    const fontSize = this.getNumberStyleValue(style, 'fs', 12)
    const fontFamily = this.getStringStyleValue(style, 'ff', 'Arial')
    const fontWeight = this.getNumberStyleValue(style, 'bl', 0) ? 'bold ' : ''
    const fontStyle = this.getNumberStyleValue(style, 'it', 0) ? 'italic ' : ''
    const color = this.getColor(style) || '#1f2329'
    const backgroundColor = this.getBackgroundColor(style) || '#ffffff'
    const lineHeight = fontSize * 1.2

    ctx.save?.()
    ctx.beginPath()
    ctx.rect(rect.x, rect.y, rect.width, rect.height)
    ctx.clip()

    ctx.fillStyle = backgroundColor
    ctx.fillRect(rect.x + 1, rect.y + 1, Math.max(rect.width - 2, 0), Math.max(rect.height - 2, 0))

    ctx.strokeStyle = color
    ctx.lineWidth = 1
    ctx.beginPath()
    ctx.moveTo(rect.x, rect.y)
    if (type === 'three') {
      ctx.lineTo(rect.x + rect.width, rect.y + rect.height * 2 / 3)
      ctx.moveTo(rect.x, rect.y)
      ctx.lineTo(rect.x + rect.width * 2 / 3, rect.y + rect.height)
    } else {
      ctx.lineTo(rect.x + rect.width, rect.y + rect.height)
    }
    ctx.stroke()

    ctx.fillStyle = color
    ctx.font = `${fontStyle}${fontWeight}${fontSize}px ${fontFamily}`
    ctx.textBaseline = 'middle'

    this.drawText(
      ctx,
      parts[0] || '',
      rect.x + padding,
      rect.y + rect.height - fontSize / 2 - padding,
      'left',
      'bottom',
      lineHeight
    )
    this.drawText(
      ctx,
      parts[1] || '',
      rect.x + rect.width - padding,
      rect.y + fontSize / 2 + padding,
      'right',
      'top',
      lineHeight
    )
    if (type === 'three') {
      this.drawText(
        ctx,
        parts[2] || '',
        rect.x + rect.width / 2,
        rect.y + rect.height / 2,
        'center',
        'center',
        lineHeight
      )
    }

    ctx.restore?.()
  }

  private getRect(info: SlashCellCustomRenderInfo): SlashCellRenderRect {
    const primaryWithCoord = info.primaryWithCoord || {}
    const coord = primaryWithCoord.mergeInfo || primaryWithCoord
    const x = coord.startX || 0
    const y = coord.startY || 0
    const width = Math.max((coord.endX || x) - x, 0)
    const height = Math.max((coord.endY || y) - y, 0)
    return { x, y, width, height }
  }

  private drawText(
    ctx: CanvasLikeContext,
    text: string,
    x: number,
    y: number,
    align: CanvasTextAlign,
    verticalAlign: SlashCellTextVerticalAlign,
    lineHeight: number
  ): void {
    if (!text) {
      return
    }

    const lines = text.split(/\r\n|\r|\n/)
    const textHeight = (lines.length - 1) * lineHeight
    let startY = y
    if (verticalAlign === 'center') {
      startY -= textHeight / 2
    } else if (verticalAlign === 'bottom') {
      startY -= textHeight
    }

    ctx.textAlign = align
    // Canvas 不会自动处理换行符，需要保持区域锚点后逐行绘制。
    lines.forEach((line, index) => {
      if (line) {
        ctx.fillText(line, x, startY + index * lineHeight)
      }
    })
  }

  private getNumberStyleValue(style: IStyleData | undefined, key: string, fallback: number): number {
    const value = (style as Record<string, unknown> | undefined)?.[key]
    return typeof value === 'number' ? value : fallback
  }

  private getStringStyleValue(style: IStyleData | undefined, key: string, fallback: string): string {
    const value = (style as Record<string, unknown> | undefined)?.[key]
    return typeof value === 'string' ? value : fallback
  }

  private getColor(style?: IStyleData): string | undefined {
    const color = (style as any)?.cl
    if (typeof color === 'string') {
      return color
    }
    if (typeof color?.rgb === 'string') {
      return color.rgb
    }
    return undefined
  }

  private groupParts(value: unknown, limit: number): string[] {
    const parts = this.parseParts(value)
    if (parts.length <= limit) {
      return parts
    }

    return [
      ...parts.slice(0, limit - 1),
      parts.slice(limit - 1).join(',')
    ]
  }

  private getBackgroundColor(style?: IStyleData): string | undefined {
    const color = (style as any)?.bg
    if (typeof color === 'string') {
      return color
    }
    if (typeof color?.rgb === 'string') {
      return color.rgb
    }
    return undefined
  }
}
