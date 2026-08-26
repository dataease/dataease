import type {
  TableHeaderStyle
} from '../../../types/plugin'
import { BooleanNumber, HorizontalAlign, VerticalAlign } from '@univerjs/core'
import type { IStyleData } from '@univerjs/core'
import type {
  DetailTableCellStyle,
  DetailTableHeaderStyle,
  DetailTableStyle,
  DetailTableTotalStyle
} from '../types'

const horizontalAlignMap: Record<TableHeaderStyle['textAlign'], HorizontalAlign> = {
  left: HorizontalAlign.LEFT,
  center: HorizontalAlign.CENTER,
  right: HorizontalAlign.RIGHT
}

const verticalAlignMap: Record<NonNullable<TableHeaderStyle['verticalAlign']>, VerticalAlign> = {
  top: VerticalAlign.TOP,
  middle: VerticalAlign.MIDDLE,
  bottom: VerticalAlign.BOTTOM
}

/**
 * 表格样式服务
 * 将配置转换为 Univer 单元格样式
 */
export class TableStyleService {
  /**
   * 转换表头样式为 Univer 格式
   */
  convertHeaderStyle(style: DetailTableHeaderStyle): Partial<IStyleData> {
    return {
      // 区域样式开启后，空背景色也要显式覆盖用户自定义背景色。
      bg: style.backgroundColor ? { rgb: style.backgroundColor } : null,
      cl: { rgb: style.textColor },
      fs: style.fontSize,
      bl: style.bold ? BooleanNumber.TRUE : BooleanNumber.FALSE,
      it: style.italic ? BooleanNumber.TRUE : BooleanNumber.FALSE,
      ul: { s: style.underline ? BooleanNumber.TRUE : BooleanNumber.FALSE },
      st: { s: style.strikethrough ? BooleanNumber.TRUE : BooleanNumber.FALSE },
      ht: horizontalAlignMap[style.textAlign],
      vt: verticalAlignMap[style.verticalAlign || 'middle']
    }
  }

  /**
   * 转换单元格样式为 Univer 格式
   */
  convertCellStyle(style: DetailTableCellStyle): Partial<IStyleData> {
    return {
      bg: style.backgroundColor ? { rgb: style.backgroundColor } : null,
      cl: { rgb: style.textColor },
      fs: style.fontSize,
      bl: style.bold ? BooleanNumber.TRUE : BooleanNumber.FALSE,
      it: style.italic ? BooleanNumber.TRUE : BooleanNumber.FALSE,
      ul: { s: style.underline ? BooleanNumber.TRUE : BooleanNumber.FALSE },
      st: { s: style.strikethrough ? BooleanNumber.TRUE : BooleanNumber.FALSE },
      ht: horizontalAlignMap[style.textAlign],
      vt: verticalAlignMap[style.verticalAlign]
    }
  }

  /**
   * 转换合计行样式为 Univer 格式
   */
  convertTotalRowStyle(style: DetailTableTotalStyle): Partial<IStyleData> {
    return {
      bg: style.backgroundColor ? { rgb: style.backgroundColor } : null,
      cl: { rgb: style.textColor },
      fs: style.fontSize,
      bl: style.bold ? BooleanNumber.TRUE : BooleanNumber.FALSE,
      it: style.italic ? BooleanNumber.TRUE : BooleanNumber.FALSE,
      ul: { s: style.underline ? BooleanNumber.TRUE : BooleanNumber.FALSE },
      st: { s: style.strikethrough ? BooleanNumber.TRUE : BooleanNumber.FALSE },
      ht: horizontalAlignMap[style.textAlign],
      vt: verticalAlignMap[style.verticalAlign]
    }
  }

  /**
   * 应用斑马纹样式
   */
  applyAlternatingRowStyle(
    baseStyle: Partial<IStyleData>,
    rowIndex: number,
    zebraColor = '#f5f7fa'
  ): Partial<IStyleData> {
    if (rowIndex % 2 === 1) {
      return {
        ...baseStyle,
        bg: { rgb: zebraColor }
      }
    }
    return baseStyle
  }

  /**
   * 获取默认表头样式
   */
  getDefaultHeaderStyle(): Partial<IStyleData> {
    return {
      bg: { rgb: '#fafafa' },
      cl: { rgb: '#333333' },
      fs: 12,
      bl: BooleanNumber.TRUE,
      ht: HorizontalAlign.LEFT,
      vt: VerticalAlign.MIDDLE
    }
  }

  /**
   * 获取默认单元格样式
   */
  getDefaultCellStyle(): Partial<IStyleData> {
    return {}
  }

  /**
   * 根据列索引获取列宽
   */
  getColumnWidth(styleConfig: DetailTableStyle, colIndex: number): number {
    if (!styleConfig.columnWidth) {
      return 120
    }
    if (styleConfig.columnWidth.autoWidth) {
      return styleConfig.columnWidth.fixedWidth
    }
    return styleConfig.columnWidth.fixedWidth
  }
}
