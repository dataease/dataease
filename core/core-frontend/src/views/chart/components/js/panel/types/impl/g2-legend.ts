/**
 * G2 图例布局共用的尺寸规则
 *
 * 这个文件本身不创建图例，也不直接修改 G2 的绘制结果，只负责统一回答两个问题
 * 1. 左右侧图例最多可以占多宽
 * 2. 当前高度一页最多能放几条图例
 *
 * 为什么单独放在这里
 * - 普通 G2 图表由 g2-layout.ts 使用这些规则，限制左右图例宽度并给分页器留位置
 * - 组合图由 charts/g2/mix/common.ts 使用相同规则，提前分配图例层和绘图区的宽度
 * - 两处共用同一组数值，避免出现普通图表和组合图的侧边图例宽度、分页判断不一致
 *
 * 直接影响
 * - 图例放在左侧或右侧时，图例占用的画布宽度
 * - 图例文字过长时，保留多少文字宽度以及何时显示省略号
 * - 图例项目过多时，是否需要为上一页、下一页和页码预留空间
 * - 组合图中侧边图例和实际绘图区之间如何分配宽度
 *
 * 不影响
 * - 图表数据、坐标轴、柱线饼等图形本身的计算
 * - 图例的颜色、名称和点击筛选逻辑
 * - 后端查询、L7 地图和 S2 表格
 */

/**
 * 侧边图例真正分页后，为上一页、下一页和页码统一预留的宽度
 *
 * 只有确认图例放不下并出现分页时才使用 55px，未分页时不会白白占用这块空间
 */
export const SIDE_LEGEND_NAVIGATOR_WIDTH = 55

/**
 * 侧边图例未分页时使用的普通列间距
 *
 * 可以直白理解为图例内容右边默认留出的 16px 空隙
 */
export const SIDE_LEGEND_DEFAULT_COL_PADDING = 16

/**
 * 侧边图例默认最多占整个画布宽度的 30%
 *
 * 这样长图例不会把主要绘图区挤得过窄，组合图的外层已经限制过宽度时可以传入其他比例
 */
export const SIDE_LEGEND_MAX_WIDTH_RATIO = 0.3

/**
 * 计算侧边图例最大宽度时使用的最低参考值
 *
 * 正常画布下至少按 80px 规划，避免图例稍窄就几乎看不到文字
 * 如果整个容器本身不足 80px，最终仍会服从容器宽度，不会强行占用 80px
 */
export const SIDE_LEGEND_MIN_MAX_WIDTH = 80

/**
 * 扣除颜色标记、间距和分页器后，图例文字至少保留的宽度
 *
 * 24px 主要用于保证极窄场景仍有一小段文字和省略号可显示
 */
export const SIDE_LEGEND_MIN_LABEL_WIDTH = 24

/**
 * 左右侧水平图例最多使用两列
 *
 * 侧边栏本身只占画布的一小部分，继续增加列数会让文字过度省略
 */
export const SIDE_HORIZONTAL_LEGEND_MAX_COLS = 2

export interface SideHorizontalLegendGridOptions {
  containerWidth: number
  containerHeight: number
  itemCount: number
  itemHeight: number
  rowPadding: number
  itemMarkerSize: number
  itemSpacing: number
  crossPadding: number
  maxWidthRatio?: number
  minColumns?: number
}

export interface SideHorizontalLegendGrid {
  columns: number
  rowsPerPage: number
  visibleRows: number
  paged: boolean
  colPadding: number
  labelWidth: number
  length: number
}

/**
 * 计算左右侧图例允许占用的最大宽度
 *
 * 计算方式可以直接理解为
 * - 一般取容器宽度的 30%，也可以由调用方传入其他比例
 * - 正常情况下不让结果小于 80px，给图例文字保留基本可读空间
 * - 同时不允许图例占满容器，至少给绘图区留下 1px
 *
 * 例如容器宽 1000px 时结果是 300px，容器宽 200px 时结果是 80px
 * 容器宽只有 60px 时结果是 59px，因为图例不能超过容器本身
 *
 * 第一次布局拿不到有效容器宽度时先返回 80px，等 G2 获得真实尺寸后会重新计算
 */
export const getSideLegendMaxWidth = (containerWidth: number, ratio?: number) => {
  if (!Number.isFinite(containerWidth) || containerWidth <= 1) {
    return SIDE_LEGEND_MIN_MAX_WIDTH
  }
  const safeRatio = Number.isFinite(ratio) && ratio > 0 ? ratio : SIDE_LEGEND_MAX_WIDTH_RATIO
  return Math.min(
    containerWidth - 1,
    Math.max(SIDE_LEGEND_MIN_MAX_WIDTH, Math.floor(containerWidth * safeRatio))
  )
}

/**
 * 计算侧边图例在当前高度下一页最多能显示多少条
 *
 * 每条图例要占 itemHeight，高低两条之间还要留 rowPadding
 * 因为最后一条下面不需要再留间距，所以公式是
 * (容器高度 + 行间距) / (单条高度 + 行间距)，再向下取整
 *
 * 组合图用这个结果比较图例总数，超过一页容量时才给分页器增加宽度
 * 容器高度无效或过小时按 1 条处理，保证后续布局仍然有可用结果
 */
export const getSideLegendRowsPerPage = (
  containerHeight: number,
  itemHeight: number,
  rowPadding: number
) => {
  if (!Number.isFinite(containerHeight) || containerHeight <= 0) {
    return 1
  }
  return Math.max(1, Math.floor((containerHeight + rowPadding) / (itemHeight + rowPadding)))
}

/**
 * 为左右侧水平分类图例计算两列网格
 *
 * 宽度允许时图例按“先左右、再向下”排列，极窄场景会自动降级为单列
 * 分页器只占一份 55px 区域，平均分摊到各列的布局间距中
 */
export const getSideHorizontalLegendGrid = (
  options: SideHorizontalLegendGridOptions
): SideHorizontalLegendGrid => {
  const {
    containerWidth,
    containerHeight,
    itemCount,
    itemHeight,
    rowPadding,
    itemMarkerSize,
    itemSpacing,
    crossPadding,
    maxWidthRatio,
    minColumns
  } = options
  const safeItemCount = Math.max(1, itemCount)
  const safeMinColumns = Math.min(
    Math.min(SIDE_HORIZONTAL_LEGEND_MAX_COLS, safeItemCount),
    Math.max(1, Number(minColumns) || 1)
  )
  const rowsPerPage = getSideLegendRowsPerPage(containerHeight, itemHeight, rowPadding)
  const maxLegendWidth = getSideLegendMaxWidth(containerWidth, maxWidthRatio)
  let columns = Math.min(SIDE_HORIZONTAL_LEGEND_MAX_COLS, safeItemCount)
  while (columns > safeMinColumns) {
    const paged = safeItemCount > rowsPerPage * columns
    const requiredWidth =
      crossPadding +
      columns *
        (itemMarkerSize +
          itemSpacing +
          SIDE_LEGEND_MIN_LABEL_WIDTH +
          SIDE_LEGEND_DEFAULT_COL_PADDING) +
      (paged ? SIDE_LEGEND_NAVIGATOR_WIDTH : 0)
    if (requiredWidth <= maxLegendWidth) {
      break
    }
    columns--
  }
  const paged = safeItemCount > rowsPerPage * columns
  const colPadding =
    SIDE_LEGEND_DEFAULT_COL_PADDING + (paged ? SIDE_LEGEND_NAVIGATOR_WIDTH / columns : 0)
  const labelWidth = Math.max(
    SIDE_LEGEND_MIN_LABEL_WIDTH,
    Math.floor(
      (maxLegendWidth - crossPadding) / columns - itemMarkerSize - itemSpacing - colPadding
    )
  )
  const visibleRows = Math.max(1, Math.min(rowsPerPage, Math.ceil(safeItemCount / columns)))
  const gridLength = visibleRows * (itemHeight + rowPadding)
  const length =
    Number.isFinite(containerHeight) && containerHeight > 0
      ? Math.min(containerHeight, gridLength)
      : gridLength
  return {
    columns,
    rowsPerPage,
    visibleRows,
    paged,
    colPadding,
    labelWidth,
    // G2 测量单项高度时已把 rowPadding 计入每一行
    length: Math.max(1, length)
  }
}
