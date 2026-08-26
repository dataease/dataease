/**
 * G2 左右侧图例分页与 Plot 动态布局之间的运行时适配器
 * - 定位显式开启 dataeaseSideLegendAutoLayout 的左右侧单列图例
 * - 包装 Navigator.goTo，在原生翻页动画结束后保存当前页并触发一次安全重排
 * - 重排后回放用户已有的图例筛选和 DataEase 联动状态
 * - 图表重绘、切换渲染器或销毁时恢复原始 goTo，避免事件和实例残留
 * - 依赖 AntV G2 5.4.x Navigator 的 getContainer、goTo、currPage 和 totalPages
 * - AntV 升级后需要回归翻页动画完成时机与 legend-category 节点结构
 */
import { ChartEvent, type Chart as G2Chart } from '@antv/g2'

const SIDE_LEGEND_NAVIGATOR_STATE = Symbol('dataease-side-legend-navigator-state')

type NavigatorState = {
  originalGoTo: (pageNum: number) => any
  patchedGoTo: (pageNum: number) => any
}

type SideLegendNavigator = {
  attributes?: Record<string, any>
  currPage?: number
  totalPages?: number
  children?: any[]
  parentNode?: any
  getContainer?: () => any
  goTo?: (pageNum: number) => any
  [SIDE_LEGEND_NAVIGATOR_STATE]?: NavigatorState
  [key: string | symbol]: any
}

type SideLegendPaginationAdapterOptions = {
  afterPageLayout?: () => void | Promise<void>
}

type LegendSelection = {
  channel: string
  values: unknown[]
}

const isObject = (value: unknown): value is Record<string, any> =>
  typeof value === 'object' && value !== null

const isSideLegendGuide = (guide: unknown): guide is Record<string, any> =>
  isObject(guide) &&
  guide.dataeaseSideLegendAutoLayout === true &&
  ['left', 'right'].includes(guide.position)

const getSideLegendGuides = (options: unknown) => {
  const guides: Record<string, any>[] = []
  const visited = new Set<unknown>()
  const appendGuide = (guide: unknown) => {
    if (isSideLegendGuide(guide) && !guides.includes(guide)) {
      guides.push(guide)
    }
  }
  const visit = (node: unknown) => {
    if (!isObject(node) || visited.has(node)) {
      return
    }
    visited.add(node)
    appendGuide(node)
    const legend = node.legend
    if (isObject(legend)) {
      appendGuide(legend)
      Object.values(legend).forEach(appendGuide)
    }
    ;[node.children, node.marks].forEach(children => {
      if (Array.isArray(children)) {
        children.forEach(visit)
      }
    })
  }
  visit(options)
  return guides
}

// 组合图在外层 SpaceFlex 单独分配图例层宽度，翻页时需要同步它保存的当前页
const getSideLegendLayouts = (options: unknown) => {
  const layouts: Record<string, any>[] = []
  const visited = new Set<unknown>()
  const visit = (node: unknown) => {
    if (!isObject(node) || visited.has(node)) {
      return
    }
    visited.add(node)
    if (isObject(node.dataeaseSideLegendLayout)) {
      layouts.push(node.dataeaseSideLegendLayout)
    }
    ;[node.children, node.marks].forEach(children => {
      if (Array.isArray(children)) {
        children.forEach(visit)
      }
    })
  }
  visit(options)
  return layouts
}

// 当前页同时写入布局标记和 Navigator.defaultPage，重排后仍停留在用户选择的页码
const updateCurrentLegendPage = (
  chart: G2Chart,
  legendIndex: number,
  pageNum: number,
  totalPages: number
) => {
  const options = chart.options() as Record<string, any>
  const guides = getSideLegendGuides(options)
  const guide = guides[legendIndex] ?? (guides.length === 1 ? guides[0] : undefined)
  if (!guide || guide.dataeaseLegendOrientLayout === 'horizontal') {
    return false
  }
  const safePageNum = Math.max(0, Math.min(pageNum, Math.max(0, totalPages - 1)))
  const layouts = getSideLegendLayouts(options)
  const sideLegendLayout = layouts[legendIndex] ?? (layouts.length === 1 ? layouts[0] : undefined)
  const pageChanged =
    Number(guide.dataeaseSideLegendCurrentPage) !== safePageNum ||
    Number(guide.navDefaultPage) !== safePageNum ||
    (sideLegendLayout && Number(sideLegendLayout.currentPage) !== safePageNum)
  if (!pageChanged) {
    return false
  }
  guide.dataeaseSideLegendCurrentPage = safePageNum
  guide.navDefaultPage = safePageNum
  if (sideLegendLayout) {
    sideLegendLayout.currentPage = safePageNum
  }
  chart.options(options)
  return true
}

// Navigator 没有公开查询 API，只在当前 legend-category 子树内做受控递归定位
const findLegendNavigators = (root: any) => {
  const navigators = new Set<SideLegendNavigator>()
  const visited = new Set<any>()
  const visit = (node: any) => {
    if (!node || visited.has(node)) {
      return
    }
    visited.add(node)
    if (
      typeof node.getContainer === 'function' &&
      typeof node.goTo === 'function' &&
      typeof node.totalPages === 'number'
    ) {
      navigators.add(node)
      return
    }
    Array.from(node.children || []).forEach(visit)
  }
  visit(root)
  return navigators
}

/**
 * 左右图例翻页后按当前页最长文本重新计算图例与 Plot 的布局
 * 页集合、页码和分页器显隐继续完全交给 AntV 管理
 */
export const installG2SideLegendPaginationAdapter = (
  chart: G2Chart,
  options: SideLegendPaginationAdapterOptions = {}
) => {
  const adaptedNavigators = new Set<SideLegendNavigator>()
  const legendSelections = new Map<string, LegendSelection>()
  let chartRendering = false
  let pageLayoutRendering = false
  let disposed = false

  const rememberLegendFilter = event => {
    const channel = event?.data?.channel
    const values = event?.data?.values
    if (typeof channel === 'string' && Array.isArray(values)) {
      legendSelections.set(channel, { channel, values: [...values] })
    }
  }
  const rememberLegendFocus = event => {
    const channel = event?.data?.channel
    if (typeof channel === 'string' && event?.data?.value !== undefined) {
      legendSelections.set(channel, { channel, values: [event.data.value] })
    }
  }
  const rememberLegendReset = () => {
    legendSelections.clear()
  }
  const replayLegendSelections = () => {
    legendSelections.forEach(selection => {
      chart.emit('legend:filter', {
        nativeEvent: false,
        data: { channel: selection.channel, values: [...selection.values] }
      })
    })
  }

  const renderCurrentPageLayout = async (
    navigator: SideLegendNavigator,
    legendIndex: number,
    pageNum: number
  ) => {
    if (
      disposed ||
      chartRendering ||
      pageLayoutRendering ||
      Number(navigator.currPage) !== pageNum ||
      !updateCurrentLegendPage(chart, legendIndex, pageNum, Number(navigator.totalPages) || 0)
    ) {
      return
    }
    pageLayoutRendering = true
    try {
      await chart.render()
      if (!disposed) {
        // G2 render 会重建 legendFilter 交互，先恢复系列筛选再回放 DataEase 联动
        replayLegendSelections()
        await options.afterPageLayout?.()
      }
    } catch (error) {
      console.error('render G2 side legend page layout error', error)
    } finally {
      pageLayoutRendering = false
    }
  }

  const adaptNavigator = (navigator: SideLegendNavigator, legendIndex: number) => {
    if (
      navigator.attributes?.orientation !== 'vertical' ||
      Number(navigator.totalPages) < 2 ||
      typeof navigator.goTo !== 'function' ||
      navigator[SIDE_LEGEND_NAVIGATOR_STATE]
    ) {
      return
    }
    const originalGoTo = navigator.goTo
    const patchedGoTo = function (this: SideLegendNavigator, pageNum: number) {
      const shouldRelayout = !chartRendering && !pageLayoutRendering
      const animation = originalGoTo.call(this, pageNum)
      const finished = animation?.finished
      if (shouldRelayout && finished && typeof finished.then === 'function') {
        // 等 AntV 完成页码和显隐切换后再重排 Plot，避免读取到上一页状态
        void finished.then(
          () => queueMicrotask(() => void renderCurrentPageLayout(this, legendIndex, pageNum)),
          () => undefined
        )
      }
      return animation
    }
    navigator[SIDE_LEGEND_NAVIGATOR_STATE] = { originalGoTo, patchedGoTo }
    navigator.goTo = patchedGoTo
    adaptedNavigators.add(navigator)
  }

  const adapt = () => {
    // 某些 G2 更新会替换组件实例，及时释放已脱离画布的旧 Navigator 引用
    adaptedNavigators.forEach(navigator => {
      if (navigator.parentNode) {
        return
      }
      const state = navigator[SIDE_LEGEND_NAVIGATOR_STATE]
      if (state && navigator.goTo === state.patchedGoTo) {
        navigator.goTo = state.originalGoTo
      }
      delete navigator[SIDE_LEGEND_NAVIGATOR_STATE]
      adaptedNavigators.delete(navigator)
    })
    const document = chart.getContext()?.canvas?.document
    const legendRoots = Array.from(document?.getElementsByClassName?.('legend-category') || [])
    let sideLegendIndex = 0
    legendRoots.forEach(root => {
      findLegendNavigators(root).forEach(navigator => {
        if (navigator.attributes?.orientation !== 'vertical') {
          return
        }
        adaptNavigator(navigator, sideLegendIndex)
        sideLegendIndex++
      })
    })
  }

  const beforeRender = () => {
    chartRendering = true
  }
  const afterRender = () => {
    chartRendering = false
    adapt()
  }
  chart.on('legend:filter', rememberLegendFilter)
  chart.on('legend:focus', rememberLegendFocus)
  chart.on('legend:reset', rememberLegendReset)
  chart.on(ChartEvent.BEFORE_RENDER, beforeRender)
  chart.on(ChartEvent.AFTER_RENDER, afterRender)
  adapt()

  return () => {
    disposed = true
    chart.off('legend:filter', rememberLegendFilter)
    chart.off('legend:focus', rememberLegendFocus)
    chart.off('legend:reset', rememberLegendReset)
    chart.off(ChartEvent.BEFORE_RENDER, beforeRender)
    chart.off(ChartEvent.AFTER_RENDER, afterRender)
    adaptedNavigators.forEach(navigator => {
      const state = navigator[SIDE_LEGEND_NAVIGATOR_STATE]
      if (state && navigator.goTo === state.patchedGoTo) {
        navigator.goTo = state.originalGoTo
      }
      delete navigator[SIDE_LEGEND_NAVIGATOR_STATE]
    })
    adaptedNavigators.clear()
  }
}
