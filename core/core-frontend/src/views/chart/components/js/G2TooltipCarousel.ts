import { parseJson } from '@/views/chart/components/js/util'
import {
  getTooltipDisplayMode,
  getTooltipWrapper,
  getThemeContrastColor,
  listenerTooltipShow,
  switchTooltipWrapperHost,
  TOOLTIP_HOVER_LEAVE_EVENT
} from '@/views/chart/components/js/panel/charts/g2/bar/barUtil'

class G2TooltipCarousel {
  /**
   * 存储 G2TooltipCarousel 实例的缓存，key为chart的container
   * @private
   */
  private static instanceCache = new Map<string, G2TooltipCarousel>()
  private static COLUMN_CAROUSEL_ORIGIN_STYLE = '__deTooltipCarouselOriginStyle__'
  private static readonly CAROUSEL_START_GAP = 40
  private static readonly RESIZE_EXECUTION_GAP = 32
  private static readonly SCROLL_IDLE_DELAY = 160
  private static startQueue: G2TooltipCarousel[] = []
  private static startQueueTimer: number | null = null
  private static resizeQueue = new Map<string, () => Promise<void>>()
  private static resizeQueueRunning = false
  private static resizeQueueTimer: number | null = null
  private static scrollEndTimer: number | null = null
  private static pageScrolling = false
  private static pageScrollListenerReady = false
  // 支持轮播的图表类型
  private static SUPPORT_CHART_TYPES = [
    'bar',
    'bar-stack',
    'bar-group',
    'bar-group-stack',
    'percentage-bar-stack',
    'line',
    'area',
    'area-stack',
    'chart-mix',
    'chart-mix-group',
    'chart-mix-stack',
    'chart-mix-dual-line',
    'pie',
    'pie-donut',
    'pie-rose',
    'pie-donut-rose'
  ]
  private newChart: any
  private chart: any
  private data: any[]
  private normalInterval: number
  private finalExtraWait: number
  private index: number
  private isPaused: boolean
  /**
   * 图表与视口可见性观察者
   * @private
   */
  private intersectionObserver: IntersectionObserver
  private chartElement: HTMLElement
  private timers = {
    interval: null,
    carousel: null,
    nextItem: null,
    hoverLeave: null
  }
  private isExecuting: boolean
  private isViewEnlarged: boolean
  private instanceId: number
  private isDestroyed: boolean
  private chartIsVisible: boolean
  // 事件处理函数引用
  private handleMouseEnter: EventListener
  private handleMouseLeave: EventListener
  private handleTooltipMouseLeave: EventListener
  // 图表所在页面可见性变化处理函数引用
  private handleVisibility: EventListener
  private renderWaitCount: number
  private columnSelectionFrameIds: number[]
  private isFirstColumnFrameReady: boolean

  /**
   * 构造函数，初始化轮播实例
   */
  constructor(newChart: any, chart: any, data: any[]) {
    if (typeof newChart?.on === 'function') {
      listenerTooltipShow(newChart, chart)
    }
    // 重新创建实例前销毁已有实例
    G2TooltipCarousel.destroyByContainer(chart.container)
    this.newChart = newChart
    this.chart = chart
    this.data = data || []
    const { isLine, lineEncodedX, isMix, mixEncodedX } = this.specialChartTypes()
    if (isLine && lineEncodedX) {
      this.data = this.groupByField(this.data, { x: lineEncodedX })
    } else if (isMix && mixEncodedX) {
      this.data = this.groupByField(this.data, { x: mixEncodedX })
    } else {
      const encode = this.newChart?.children?.[0]?.value?.encode
      if (encode) {
        this.data = this.groupByField(this.data, encode)
      }
    }
    this.index = 0
    this.isPaused = false
    this.isViewEnlarged = false
    this.isDestroyed = false
    this.instanceId = Date.now()
    this.renderWaitCount = 0
    this.columnSelectionFrameIds = []
    this.isFirstColumnFrameReady = false
    // 事件处理函数绑定
    this.handleMouseEnter = this.mouseEnter.bind(this)
    this.handleMouseLeave = this.mouseLeave.bind(this)
    this.handleTooltipMouseLeave = this.tooltipMouseLeave.bind(this)
    this.handleVisibility = this.handleVisibilityChange.bind(this)
    const { tooltip } = parseJson(this.chart.customAttr)
    // 如果不显示tooltip，销毁实例
    if (!tooltip.show) {
      G2TooltipCarousel.destroyByContainer(this.chart.container)
      return null
    }
    const carousel = chart.customAttr?.tooltip?.carousel
    // 不支持轮播或类型不支持时销毁实例
    if (!carousel?.enable || !G2TooltipCarousel.SUPPORT_CHART_TYPES.includes(chart.type)) {
      G2TooltipCarousel.destroyByContainer(chart.container)
      return null
    }
    const chartElement = this.getChartElement()
    if (!chartElement) {
      return null
    }
    this.chartElement = chartElement
    this.normalInterval = carousel?.stayTime * 1000
    this.finalExtraWait = carousel?.intervalTime * 1000
    this.isViewEnlarged = this.chart.container.indexOf('viewDialog') > -1
    this.checkStopOnViewChange()
    this.init()
    G2TooltipCarousel.instanceCache.set(chart.container, this)
    G2TooltipCarousel.ensurePageScrollListener()
  }

  /**
   * 页面滚动期间暂停轮播，滚动停止后通过全局队列错峰恢复
   */
  private static readonly handlePageScroll = (event: Event) => {
    const target = event.target
    if (target instanceof Element && target.closest('.g2-tooltip')) {
      return
    }
    if (!G2TooltipCarousel.pageScrolling) {
      G2TooltipCarousel.pageScrolling = true
      G2TooltipCarousel.instanceCache.forEach(instance => instance.suspendForPageScroll())
    }
    if (G2TooltipCarousel.scrollEndTimer) {
      clearTimeout(G2TooltipCarousel.scrollEndTimer)
    }
    G2TooltipCarousel.scrollEndTimer = window.setTimeout(() => {
      G2TooltipCarousel.scrollEndTimer = null
      G2TooltipCarousel.pageScrolling = false
      G2TooltipCarousel.instanceCache.forEach(instance => instance.resumeAfterPageScroll())
    }, G2TooltipCarousel.SCROLL_IDLE_DELAY)
  }

  private static ensurePageScrollListener() {
    if (G2TooltipCarousel.pageScrollListenerReady) {
      return
    }
    // scroll 不冒泡，使用捕获阶段统一覆盖预览页和内部滚动容器
    document.addEventListener('scroll', G2TooltipCarousel.handlePageScroll, {
      capture: true,
      passive: true
    })
    G2TooltipCarousel.pageScrollListenerReady = true
  }

  private static releasePageScrollListener() {
    if (G2TooltipCarousel.instanceCache.size || !G2TooltipCarousel.pageScrollListenerReady) {
      return
    }
    document.removeEventListener('scroll', G2TooltipCarousel.handlePageScroll, true)
    G2TooltipCarousel.pageScrollListenerReady = false
    G2TooltipCarousel.pageScrolling = false
    if (G2TooltipCarousel.scrollEndTimer) {
      clearTimeout(G2TooltipCarousel.scrollEndTimer)
      G2TooltipCarousel.scrollEndTimer = null
    }
    if (G2TooltipCarousel.startQueueTimer) {
      clearTimeout(G2TooltipCarousel.startQueueTimer)
      G2TooltipCarousel.startQueueTimer = null
    }
    G2TooltipCarousel.startQueue = []
  }

  private static enqueueStart(instance: G2TooltipCarousel) {
    if (
      instance.isDestroyed ||
      instance.isPaused ||
      G2TooltipCarousel.pageScrolling ||
      G2TooltipCarousel.startQueue.includes(instance)
    ) {
      return
    }
    G2TooltipCarousel.startQueue.push(instance)
    G2TooltipCarousel.drainStartQueue()
  }

  private static dequeueStart(instance: G2TooltipCarousel) {
    G2TooltipCarousel.startQueue = G2TooltipCarousel.startQueue.filter(item => item !== instance)
  }

  private static drainStartQueue() {
    if (
      G2TooltipCarousel.startQueueTimer ||
      G2TooltipCarousel.pageScrolling ||
      G2TooltipCarousel.resizeQueueRunning ||
      G2TooltipCarousel.resizeQueueTimer ||
      G2TooltipCarousel.resizeQueue.size
    ) {
      return
    }
    let instance = G2TooltipCarousel.startQueue.shift()
    while (instance && (instance.isDestroyed || instance.isPaused)) {
      instance = G2TooltipCarousel.startQueue.shift()
    }
    if (instance) {
      instance.startFromQueue()
      // 保留启动冷却窗口，避免同一批新实例绕过错峰队列
      G2TooltipCarousel.startQueueTimer = window.setTimeout(() => {
        G2TooltipCarousel.startQueueTimer = null
        G2TooltipCarousel.drainStartQueue()
      }, G2TooltipCarousel.CAROUSEL_START_GAP)
    }
  }

  static enqueueResize(containerId: string, resizeTask: () => Promise<void>) {
    G2TooltipCarousel.instanceCache.get(containerId)?.suspendForPageScroll()
    G2TooltipCarousel.resizeQueue.set(containerId, resizeTask)
    G2TooltipCarousel.drainResizeQueue()
  }

  static dequeueResize(containerId: string) {
    G2TooltipCarousel.resizeQueue.delete(containerId)
  }

  private static drainResizeQueue() {
    if (G2TooltipCarousel.resizeQueueRunning || G2TooltipCarousel.resizeQueueTimer) {
      return
    }
    const [nextResize] = G2TooltipCarousel.resizeQueue.entries()
    if (!nextResize) {
      G2TooltipCarousel.instanceCache.forEach(instance => instance.resumeAfterPageScroll())
      return
    }
    const [containerId, resizeTask] = nextResize
    G2TooltipCarousel.resizeQueue.delete(containerId)
    G2TooltipCarousel.resizeQueueRunning = true
    resizeTask()
      .catch(error => console.warn(error))
      .finally(() => {
        G2TooltipCarousel.resizeQueueRunning = false
        // 全屏尺寸变化时逐个执行图表适配，避免所有 forceFit 同帧抢占主线程
        G2TooltipCarousel.resizeQueueTimer = window.setTimeout(() => {
          G2TooltipCarousel.resizeQueueTimer = null
          G2TooltipCarousel.drainResizeQueue()
        }, G2TooltipCarousel.RESIZE_EXECUTION_GAP)
      })
  }

  /**
   * 检查是否需要停止轮播（如仪表盘隐藏或有放大视图时）
   * @private
   */
  private checkStopOnViewChange() {
    const containerPrefix = this.chart.container.split(this.chart.id)?.[0] || ''
    G2TooltipCarousel.instanceCache.forEach(instance => {
      // 如果有仪表盘隐藏或者当前有放大视图、图表容器ID前缀与其他都不一样，停止轮播
      if (
        instance.chart.dashboardHidden ||
        this.isViewEnlarged ||
        !instance.chart.container.startsWith(containerPrefix)
      ) {
        G2TooltipCarousel.getInstanceByContainerId(instance.chart.container)?.stop()
      }
    })
  }

  /**
   * 判断图表是否为折线图或面积图
   * @private
   */
  private isLineChart(): boolean {
    return ['line', 'area', 'area-stack'].includes(this.chart.type)
  }

  private isBasicLineChart(): boolean {
    return this.chart.type === 'line'
  }

  private isPieChart(): boolean {
    return ['pie', 'pie-donut', 'pie-rose', 'pie-donut-rose'].includes(this.chart.type)
  }

  /**
   * 判断图表是否为混合图表
   * @private
   */
  private isMixChart(): boolean {
    return ['chart-mix', 'chart-mix-dual-line', 'chart-mix-group', 'chart-mix-stack'].includes(
      this.chart.type
    )
  }

  private isColumnMixChart(): boolean {
    return ['chart-mix', 'chart-mix-group', 'chart-mix-stack'].includes(this.chart.type)
  }

  private isDualLineMixChart(): boolean {
    return this.chart.type === 'chart-mix-dual-line'
  }

  private isCustomLegendMixChart(): boolean {
    return ['chart-mix-group', 'chart-mix-stack', 'chart-mix-dual-line'].includes(this.chart.type)
  }

  private isColumnChart(): boolean {
    return ['bar', 'bar-stack', 'bar-group', 'bar-group-stack', 'percentage-bar-stack'].includes(
      this.chart.type
    )
  }

  private isGroupedOrStackedColumnChart(): boolean {
    return ['bar-stack', 'bar-group', 'bar-group-stack', 'percentage-bar-stack'].includes(
      this.chart.type
    )
  }

  /**
   * 通过 encode 的 x 字段对数据进行分组，返回每组的第一个数据项
   * @param data
   * @param encode
   * @private
   */
  private groupByField<T>(data: T[], encode: T) {
    const groups: Record<string, T[]> = {}
    data.forEach(item => {
      const key = String(item[encode?.x || encode?.color])
      if (!groups[key]) groups[key] = []
      groups[key].push(item)
    })
    return Object.keys(groups).map(key => ({
      ...groups[key][0]
    }))
  }

  /**
   * 通过容器 ID 获取 G2TooltipCarousel 实例
   */
  static getInstanceByContainerId(containerId: string): G2TooltipCarousel | undefined {
    return G2TooltipCarousel.instanceCache.get(containerId)
  }

  /**
   * 销毁指定容器的 G2TooltipCarousel 实例
   */
  static destroyByContainer(containerId?: string, restoreHoverTooltip = false) {
    if (containerId) {
      const instance = G2TooltipCarousel.instanceCache.get(containerId)
      if (instance) {
        instance.destroy()
        if (restoreHoverTooltip) {
          switchTooltipWrapperHost(instance.chart, 'hover')
        }
      }
    }
  }

  /**
   * 初始化事件监听和可见性检测
   */
  private init() {
    this.addEventListeners()
  }

  private getChartElement(): HTMLElement | null {
    // 兼容公共入口传入的不同图表实例形态，统一回落到 chart.container
    return (
      this.newChart?.getContainer?.() ||
      this.newChart?.chart?.getContainer?.() ||
      this.newChart?.ele ||
      this.newChart?.chart?.ele ||
      document.getElementById(this.chart.container)
    )
  }

  private getTooltipElement(): HTMLElement | null {
    return (
      getTooltipWrapper(this.chart.container)?.querySelector<HTMLElement>('.g2-tooltip') || null
    )
  }

  /**
   * 页面可见性事件监听
   * 绑定时用同一函数引用，防止内存泄漏
   */
  private addEventListeners() {
    document.addEventListener('visibilitychange', this.handleVisibility)
    this.chartElement.addEventListener('mouseenter', this.handleMouseEnter)
    this.chartElement.addEventListener('mouseleave', this.handleMouseLeave)
    getTooltipWrapper(this.chart.container)?.addEventListener(
      TOOLTIP_HOVER_LEAVE_EVENT,
      this.handleTooltipMouseLeave
    )
    if (!this.intersectionObserver) {
      this.intersectionObserver = new IntersectionObserver(this.handleIntersection.bind(this), {
        root: null,
        threshold: [0, 0.1, 0.3, 0.5, 0.7, 0.9, 1]
      })
    }
    this.intersectionObserver?.observe(this.chartElement)
  }

  /**
   * 移除事件监听
   * 解绑时用同一函数引用，防止内存泄漏
   */
  private removeEventListeners() {
    this.chartElement.removeEventListener('mouseenter', this.handleMouseEnter)
    this.chartElement.removeEventListener('mouseleave', this.handleMouseLeave)
    getTooltipWrapper(this.chart.container)?.removeEventListener(
      TOOLTIP_HOVER_LEAVE_EVENT,
      this.handleTooltipMouseLeave
    )
    document.removeEventListener('visibilitychange', this.handleVisibility)
  }

  /**
   * IntersectionObserver回调，处理元素进入/离开视口
   * 只有可见区域大于70%时才恢复轮播，否则暂停
   */
  private handleIntersection() {
    if (!this.isActuallyVisible(this.chartElement) || this.chart.dashboardHidden) {
      this.chartIsVisible = false
      if (G2TooltipCarousel.pageScrolling) {
        // 滚动期间只停调度，避免跨越视口时批量触发 SVG 状态重置
        this.isPaused = true
        this.suspendForPageScroll()
        return
      }
      this.hideTooltipAtData()
      this.pause(true)
    } else {
      this.chartIsVisible = true
      if (G2TooltipCarousel.pageScrolling) {
        this.isPaused = false
        this.suspendForPageScroll()
        return
      }
      this.resume(true)
    }
  }

  private isActuallyVisible(el: HTMLElement): boolean {
    if (!el) return false
    // 检查可见比例
    const rect = el.getBoundingClientRect()
    let visibleHeight = Math.min(rect.bottom, window.innerHeight) - Math.max(rect.top, 60)
    let visibleWidth = Math.min(rect.right, window.innerWidth) - Math.max(rect.left, 0)
    // 编辑画布和预览画布可能同时存在，只使用当前图表所属的画布计算可见区域
    const dvMainCenter = el.closest<HTMLElement>(
      '#dv-main-center, #preview-canvas-main, #edit-canvas-main'
    )
    if (dvMainCenter) {
      const dvRect = dvMainCenter.getBoundingClientRect()
      visibleHeight = Math.min(rect.bottom, dvRect.bottom) - Math.max(rect.top, dvRect.top)
      visibleWidth = Math.min(rect.right, dvRect.right) - Math.max(rect.left, dvRect.left)
    }
    const percentHeight = visibleHeight / rect.height
    const percentWidth = visibleWidth / rect.width
    return percentHeight >= 0.9 && percentWidth >= 0.7
  }

  /**
   * 页面可见性变化处理
   */
  private handleVisibilityChange() {
    this.checkStopOnViewChange()
    if (document.hidden) {
      this.pause(true)
      return
    }
    // 元素仍在视口中时恢复轮播
    const chartElement = this.getChartElement()
    if (chartElement && chartElement.offsetParent !== null) {
      const hasViewDialog = Array.from(G2TooltipCarousel.instanceCache.keys()).some(key =>
        key?.includes('viewDialog')
      )
      if (hasViewDialog && !this.chart.dashboardHidden ? this.isViewEnlarged : true) {
        this.resume(true)
      } else {
        this.pause(true)
      }
    }
  }

  /**
   * 判断是否需要暂停轮播
   */
  private shouldPauseCarousel(): boolean {
    return (
      this.isPaused ||
      G2TooltipCarousel.pageScrolling ||
      !this.data ||
      this.data.length === 0 ||
      this.hasParentWithSwitchHidden() ||
      !this.checkChartElementAndCache() ||
      !this.chartIsVisible
    )
  }

  /**
   * 轮播下一个tooltip
   */
  private next() {
    // 防止并发执行
    if (this.isExecuting || this.isDestroyed) {
      return
    }
    this.isExecuting = true
    try {
      // 使用 shouldPauseCarousel 方法判断是否需要暂停轮播
      if (this.shouldPauseCarousel()) {
        this.clearTimer()
        this.isExecuting = false
        return
      }
      this.clearTimer()
      if (this.waitForRenderedElements()) {
        this.isExecuting = false
        return
      }
      if (this.index >= this.data.length) {
        this.index = 0
      }
      const currentItem = this.getCurrentCarouselItem()
      if (!currentItem) {
        this.clearTimer()
        this.isExecuting = false
        return
      }
      const tooltipData = this.buildTooltipData(currentItem)
      // 显示当前tooltip
      this.showTooltipAtData(tooltipData, currentItem)
      // 定时切换到下一个tooltip
      this.timers.carousel = setTimeout(() => {
        if (!this.data || this.data.length === 0) {
          this.clearTimer()
          this.isExecuting = false
          return
        }
        const isLastItem = this.index === this.data.length - 1
        if (isLastItem) {
          this.hideTooltipAtData()
          // 最后一个后等待额外时间再轮播
          this.timers.interval = setTimeout(() => {
            this.index = 0
            this.isExecuting = false
            this.next()
          }, this.finalExtraWait)
        } else {
          this.isExecuting = false
          this.timers.nextItem = setTimeout(() => {
            this.index += 1
            this.next()
          }, 0)
        }
      }, this.normalInterval)
    } finally {
      // 没有定时器活动时释放执行锁
      if (!this.timers.carousel && !this.timers.interval && !this.timers.nextItem) {
        this.isExecuting = false
      }
    }
  }

  /**
   * 构建要显示tooltip的数据项
   */
  buildTooltipData(currentItem?: any) {
    const itemData = currentItem || this.data?.[0] || {}
    const tooltipData = {
      data: {
        data: itemData
      }
    }
    const { isLine, lineEncodedX, isMix, mixEncodedX } = this.specialChartTypes()
    // 折线图和混合图表需要单独处理 encode.x 字段
    if ((isLine && lineEncodedX) || (isMix && mixEncodedX)) {
      tooltipData.data.data = { x: itemData[isLine ? lineEncodedX : mixEncodedX] }
    }
    return tooltipData
  }

  private getCurrentCarouselItem() {
    let attempts = 0
    while (attempts < this.data.length) {
      const item = this.data[this.index]
      const renderedItem = this.getRenderableCarouselItem(item)
      if (renderedItem) {
        return renderedItem
      }
      this.index = (this.index + 1) % this.data.length
      attempts += 1
    }
  }

  private getRenderableCarouselItem(item: any) {
    if (!this.isColumnChart()) {
      return item
    }
    const elements = this.getRenderedElements('interval')
    if (!elements.length) {
      return undefined
    }
    return this.getRenderedCarouselData(item, elements)
  }

  private waitForRenderedElements(): boolean {
    if (!this.isColumnChart() && !this.isColumnMixChart()) {
      return false
    }
    if (this.getRenderedElements('interval').length) {
      if (!this.isFirstColumnFrameReady) {
        this.isFirstColumnFrameReady = true
        // afterrender 后首帧 G2 状态仍可能覆盖轮播选中态，延后一拍再显示第一个柱图 tooltip
        this.timers.nextItem = setTimeout(() => {
          this.timers.nextItem = null
          this.isExecuting = false
          this.next()
        }, 160)
        return true
      }
      this.renderWaitCount = 0
      return false
    }
    if (this.renderWaitCount >= 50) {
      return true
    }
    this.renderWaitCount += 1
    this.timers.nextItem = setTimeout(() => {
      this.timers.nextItem = null
      this.isExecuting = false
      this.next()
    }, 100)
    return true
  }

  /**
   * 特殊图表encode.x获取方式不同
   * 折线图和混合图表的 encode.x 配置
   */
  specialChartTypes() {
    const isLine = this.isLineChart()
    const lineEncodedX = isLine ? this.newChart?.encode?.()?.x : undefined
    const isMix = this.isMixChart()
    const mixEncodedX = isMix ? this.findEncodeX(this.newChart) : undefined
    return { isLine, lineEncodedX, isMix, mixEncodedX }
  }

  /**
   * 递归查找混合图表中的 encode.x 配置
   * @param chart
   */
  findEncodeX(chart: any): any {
    if (chart?.encode?.x) {
      return chart.encode.x
    }
    if (Array.isArray(chart?.children) && chart.children.length > 0) {
      for (const child of chart.children) {
        if (child.value?.encode?.x) {
          return child.value.encode.x
        }
        const result = this.findEncodeX(child)
        if (result !== undefined) {
          return result
        }
      }
    }
    return undefined
  }

  /**
   * 显示指定数据项的 tooltip
   * @param tooltipData
   * @param originalData
   */
  showTooltipAtData(tooltipData, originalData?: any) {
    if (typeof this.newChart?.emit !== 'function') {
      return
    }
    const isMix = this.isMixChart()
    const isLineOrMix = this.isLineChart() || isMix
    const isColumn = this.isColumnChart()
    const isColumnMix = this.isColumnMixChart()
    const isDualLineMix = this.isDualLineMixChart()
    const isPie = this.isPieChart()
    const renderedData = this.getRenderedCarouselData(originalData || tooltipData.data.data)
    if (isColumn && !renderedData) {
      this.clearElementState()
      return
    }
    const finalTooltipData = renderedData
      ? { ...tooltipData, data: { ...tooltipData.data, data: renderedData } }
      : tooltipData
    this.clearElementState()
    const highlightSource = isLineOrMix && originalData ? originalData : finalTooltipData.data.data
    const highlightData = this.getElementMatchData(highlightSource)
    if (isColumn && !highlightData) {
      return
    }
    if (isColumn) {
      const selectionData = this.getColumnSelectionMatchData(highlightData)
      // 分组和堆叠柱按当前维度选中全部子柱，其它维度不进入淡化态
      this.newChart.emit('element:select', {
        data: { data: [selectionData] }
      })
      // 所有柱形图复用已有背景高亮能力，轮播时同时展示当前分类背景
      this.newChart.emit('element:highlight', {
        data: { data: finalTooltipData.data.data }
      })
      this.applyColumnSelectionState(selectionData)
      this.scheduleColumnSelectionState(selectionData)
    } else if (isColumnMix && highlightData) {
      // 含柱组合图轮播只选中当前柱形元素
      this.newChart.emit('element:select', {
        data: { data: [highlightData] }
      })
      this.applyColumnSelectionState(highlightData)
      this.scheduleColumnSelectionState(highlightData)
    } else if (isPie && highlightData) {
      // 饼图轮播使用 selected，避免覆盖普通悬浮 active 样式
      this.newChart.emit('element:select', {
        data: { data: [highlightData] }
      })
    } else if (!isDualLineMix && highlightData) {
      // 双线组合图轮播只展示 tooltip，避免 G2 region 高亮使用历史坐标造成错位选中
      // G2 elementHighlight 从 e.data.data 读取 datum，不能复用 elementSelect 的数组结构
      this.newChart.emit('element:highlight', {
        data: { data: highlightData }
      })
    }
    // 轮播显示前切回图表容器，避免复用悬浮时的 body 坐标
    switchTooltipWrapperHost(this.chart, 'carousel')
    const { offsetX, offsetY } = this.getTooltipOffsetX(finalTooltipData)
    // 组合图交由 G2 按当前 x 定位，避免不同图形命中不同维度
    if (this.isColumnMixChart()) {
      this.newChart.emit('tooltip:show', finalTooltipData)
    } else {
      this.newChart.emit('tooltip:show', {
        ...finalTooltipData,
        ...(!isMix && offsetX && isLineOrMix && { offsetX }),
        ...(offsetY && { offsetY })
      })
    }
  }

  private getRenderedCarouselData(originalData?: any, renderedElements?: any[]) {
    if (!this.isColumnChart()) {
      return undefined
    }
    const elements = renderedElements || this.getRenderedElements('interval')
    if (!elements.length) {
      return undefined
    }
    const exactElement = elements.find(element =>
      this.isSameRenderedDatum(this.getElementDatum(element), originalData, true)
    )
    if (exactElement) {
      return this.getElementDatum(exactElement)
    }
    const sameDimensionElement = elements.find(element =>
      this.isSameRenderedDatum(this.getElementDatum(element), originalData, false)
    )
    return sameDimensionElement ? this.getElementDatum(sameDimensionElement) : undefined
  }

  private getRenderedElements(markType?: string): any[] {
    const ctx = this.newChart?.getContext?.()
    const elements = Array.from(ctx?.canvas?.document?.getElementsByClassName('element') || [])
    return markType ? elements.filter((element: any) => element.markType === markType) : elements
  }

  private getElementDatum(element: any) {
    return element?.__data__?.data?.data ?? element?.__data__?.data
  }

  private isSameRenderedDatum(renderedDatum: any, originalDatum: any, exact: boolean): boolean {
    if (!renderedDatum || !originalDatum) {
      return false
    }
    const renderedField = this.getDatumField(renderedDatum)
    const originalField = this.getDatumField(originalDatum)
    if (`${renderedField}` !== `${originalField}`) {
      return false
    }
    if (!exact) {
      return true
    }
    return ['category', 'group'].every(field => {
      if (originalDatum[field] === undefined || originalDatum[field] === null) {
        return true
      }
      return `${renderedDatum[field]}` === `${originalDatum[field]}`
    })
  }

  private getDatumField(datum: any) {
    return datum?.field ?? datum?.x ?? datum?.title
  }

  private getElementMatchData(data: any) {
    if (!data || typeof data !== 'object' || Array.isArray(data)) {
      return undefined
    }
    const matchData = {} as Record<string, any>
    for (const key of ['field', 'x', 'title', 'name', 'path']) {
      const value = data[key]
      if (['string', 'number', 'boolean'].includes(typeof value) && !Number.isNaN(value)) {
        matchData[key] = value
        break
      }
    }
    ;['category', 'group'].forEach(key => {
      const value = data[key]
      if (['string', 'number', 'boolean'].includes(typeof value) && !Number.isNaN(value)) {
        matchData[key] = value
      }
    })
    return Object.keys(matchData).length ? matchData : undefined
  }

  private getColumnSelectionMatchData(matchData: Record<string, any>) {
    if (!this.isGroupedOrStackedColumnChart()) {
      return matchData
    }
    const dimensionMatchData = Object.fromEntries(
      Object.entries(matchData).filter(([key]) => !['category', 'group'].includes(key))
    )
    return Object.keys(dimensionMatchData).length ? dimensionMatchData : matchData
  }

  private applyColumnSelectionState(matchData: Record<string, any>) {
    if (typeof this.newChart?.setState !== 'function') {
      this.applyColumnSelectionAttributes(matchData)
      return
    }
    const isSelected = param => !Array.isArray(param) && this.isMatchData(param, matchData)
    this.newChart.setState('selected', isSelected)
    this.newChart.setState('unselected', () => true, false)
    this.applyColumnSelectionAttributes(matchData)
  }

  private scheduleColumnSelectionState(matchData: Record<string, any>) {
    this.cancelColumnSelectionFrames()
    // G2 select/background 会在下一帧补样式，这里补刷保证非选中柱子保持原色
    const reapply = (count: number) => {
      const frameId = requestAnimationFrame(() => {
        this.columnSelectionFrameIds = this.columnSelectionFrameIds.filter(id => id !== frameId)
        if (this.isDestroyed || this.isPaused) {
          return
        }
        this.applyColumnSelectionAttributes(matchData)
        if (count > 1) {
          reapply(count - 1)
        }
      })
      this.columnSelectionFrameIds.push(frameId)
    }
    reapply(2)
  }

  private applyColumnSelectionAttributes(matchData: Record<string, any>) {
    const intervalElements = this.getRenderedElements('interval')
    let selectedElement
    intervalElements.forEach(element => {
      if (this.isMatchData(this.getElementDatum(element), matchData)) {
        selectedElement = element
        this.setDisplayObjectAttributes(element, {
          opacity: 1,
          fillOpacity: 1,
          stroke: getThemeContrastColor(this.chart),
          lineWidth: 1
        })
        return
      }
      this.setDisplayObjectAttributes(element, {
        opacity: 1,
        fillOpacity: 1
      })
    })
    if (selectedElement) {
      this.alignColumnSelectionBackground(selectedElement, intervalElements)
    }
    this.flushCanvasRender()
  }

  private alignColumnSelectionBackground(selectedElement: any, intervalElements: any[]) {
    const selectedDimension = this.getDatumField(this.getElementDatum(selectedElement))
    const dimensionElements =
      selectedDimension === undefined || selectedDimension === null
        ? [selectedElement]
        : intervalElements.filter(element => {
            const dimension = this.getDatumField(this.getElementDatum(element))
            return `${dimension}` === `${selectedDimension}`
          })
    const dimensionBounds = dimensionElements
      .map(element => element.getRenderBounds?.())
      .filter(bounds => Number.isFinite(bounds?.min?.[0]) && Number.isFinite(bounds?.max?.[0]))
    if (!dimensionBounds.length) {
      return
    }
    const dimensionCenterX =
      (Math.min(...dimensionBounds.map(bounds => bounds.min[0])) +
        Math.max(...dimensionBounds.map(bounds => bounds.max[0]))) /
      2
    const backgrounds = Array.from(
      this.newChart
        ?.getContext?.()
        ?.canvas?.document?.getElementsByClassName?.('element-background') || []
    ) as any[]
    backgrounds.forEach(background => {
      const backgroundBounds = background.getRenderBounds?.()
      const backgroundCenterX = (backgroundBounds?.min?.[0] + backgroundBounds?.max?.[0]) / 2
      const backgroundX = Number(this.getDisplayObjectAttribute(background, 'x'))
      if (!Number.isFinite(backgroundCenterX) || !Number.isFinite(backgroundX)) {
        return
      }
      // 基础柱按单柱中心，分组和堆叠柱按同一维度整体中心校准背景
      this.setDisplayObjectAttribute(
        background,
        'x',
        backgroundX + dimensionCenterX - backgroundCenterX
      )
    })
  }

  private isMatchData(data: any, matchData: Record<string, any>) {
    const source = data?.data ?? data
    if (!source || typeof source !== 'object' || Array.isArray(source)) {
      return false
    }
    return Object.entries(matchData).every(
      ([key, value]) => source[key] === value || `${source[key]}` === `${value}`
    )
  }

  private setDisplayObjectAttributes(element: any, attrs: Record<string, any>) {
    this.traverseDisplayObject(element, node => {
      if (
        typeof node?.setAttribute !== 'function' &&
        typeof node?.attr !== 'function' &&
        !node?.style
      ) {
        return
      }
      const originKey = G2TooltipCarousel.COLUMN_CAROUSEL_ORIGIN_STYLE
      node[originKey] = node[originKey] || {}
      Object.keys(attrs).forEach(key => {
        if (!(key in node[originKey])) {
          node[originKey][key] = this.getDisplayObjectAttribute(node, key)
        }
        this.setDisplayObjectAttribute(node, key, attrs[key])
      })
    })
  }

  private restoreColumnSelectionAttributes() {
    this.getRenderedElements('interval').forEach(element => {
      this.traverseDisplayObject(element, node => {
        const originKey = G2TooltipCarousel.COLUMN_CAROUSEL_ORIGIN_STYLE
        const originStyle = node?.[originKey]
        if (!originStyle) {
          return
        }
        Object.entries(originStyle).forEach(([key, value]) => {
          this.setDisplayObjectAttribute(node, key, value)
        })
        delete node[originKey]
      })
    })
    this.flushCanvasRender()
  }

  private traverseDisplayObject(element: any, visitor: (node: any) => void) {
    if (!element) {
      return
    }
    visitor(element)
    if (element.tagName !== 'g') {
      return
    }
    ;(element.childNodes || []).forEach(child => this.traverseDisplayObject(child, visitor))
  }

  private getDisplayObjectAttribute(node: any, key: string) {
    const value = node?.getAttribute?.(key) ?? node?.attr?.(key) ?? node?.style?.[key]
    if (value !== undefined && value !== null) {
      return value
    }
    if (['opacity', 'fillOpacity', 'strokeOpacity'].includes(key)) {
      return 1
    }
    if (key === 'lineWidth') {
      return 0
    }
    return ''
  }

  private setDisplayObjectAttribute(node: any, key: string, value: any) {
    if (node?.style) {
      node.style[key] = value
    }
    if (typeof node?.setAttribute === 'function') {
      node.setAttribute(key, value)
      return
    }
    if (typeof node?.attr === 'function') {
      try {
        node.attr(key, value)
      } catch {
        node.attr({ [key]: value })
      }
    }
  }

  private flushCanvasRender() {
    const canvas = this.newChart?.getContext?.()?.canvas
    canvas?.render?.()
  }

  private cancelColumnSelectionFrames() {
    this.columnSelectionFrameIds.forEach(id => cancelAnimationFrame(id))
    this.columnSelectionFrameIds = []
  }

  private clearColumnSelectionState() {
    this.cancelColumnSelectionFrames()
    this.restoreColumnSelectionAttributes()
    if (typeof this.newChart?.setState !== 'function') {
      return
    }
    this.newChart.setState('selected', () => true, false)
    this.newChart.setState('unselected', () => true, false)
  }

  private clearElementState() {
    const shouldClearColumnSelect = this.isColumnChart() || this.isColumnMixChart()
    const shouldClearSelect = shouldClearColumnSelect || this.isPieChart()
    // 基础折线和双线组合图不展示轮播背景，但需要清掉历史交互残留
    const shouldClearBackground =
      shouldClearColumnSelect || this.isDualLineMixChart() || this.isBasicLineChart()
    if (shouldClearSelect) {
      this.newChart.emit('element:unselect', {})
    }
    if (shouldClearColumnSelect) {
      this.clearColumnSelectionState()
    }
    this.newChart.emit('element:unhighlight', {})
    if (shouldClearBackground) {
      this.clearInteractionBackgrounds()
    }
  }

  private clearInteractionBackgrounds() {
    try {
      const ctx = this.newChart?.getContext?.()
      const backgrounds = Array.from(
        ctx?.canvas?.document?.getElementsByClassName?.('element-background') || []
      )
      backgrounds.forEach((background: any) => background.remove?.())
      this.getRenderedElements().forEach((element: any) => {
        if (element?.background) {
          element.background = null
        }
      })
    } catch (e) {
      console.warn('Clear tooltip carousel background fail:', e)
    }
  }

  /**
   * 计算 tooltip 的 X、Y 轴偏移位置
   * @param tooltipData
   */
  getTooltipOffsetX(tooltipData) {
    try {
      const x =
        tooltipData?.data?.data?.x ??
        tooltipData?.data?.data?.[this.newChart?.children?.[0]?.value?.encode?.x]
      const ctx = this.newChart?.getContext()
      if (!ctx) return {}
      const elements = ctx.canvas?.document?.getElementsByClassName('element') || []
      const plots = ctx.canvas?.document?.getElementsByClassName('plot') || []
      // 独立图例也是 plot，组合图必须使用 key=chart 对应的真实绘图区
      const chartView = this.isCustomLegendMixChart()
        ? ctx.views?.find(view => view.key === 'chart')
        : undefined
      const root = chartView
        ? Array.from(plots).find((plot: any) => plot.__data__ === chartView.layout)
        : plots[0]
      if (!root || elements.length === 0) return {}
      const { insetLeft = 0, marginLeft = 0, paddingLeft = 0 } = root.__data__ || {}
      const plotPaddingLeft = insetLeft + marginLeft + paddingLeft
      const offsetY = (root.getRenderBounds().height / 2) * (this.chart.tScale || 1)
      if (this.isLineChart()) {
        const xField = ctx.views[0].options.marks[0].encode.x
        const firstXElement = elements.filter(
          ele => ele.markType === 'point' && ele.__data__.data[xField] === x
        )?.[0]
        const offsetX =
          plotPaddingLeft +
          (firstXElement?.__data__?.points?.[0]?.[0] + firstXElement?.__data__?.points?.[1]?.[0]) /
            2
        return { offsetX, offsetY }
      } else {
        const xElement = Array.from(elements).find(ele => ele.__data__?.title === x)
        if (!xElement || !xElement.__data__?.points) return {}
        const points = xElement.__data__.points
        const offsetX = plotPaddingLeft + (points[0][0] + points[1][0]) / 2
        return { offsetX, offsetY }
      }
    } catch (e) {
      console.error('Get Tooltip offsetX fail:', e)
      return {}
    }
  }

  /**
   * 隐藏 tooltip
   */
  hideTooltipAtData() {
    if (typeof this.newChart?.emit !== 'function') {
      return
    }
    this.newChart.emit('tooltip:hide')
    this.clearElementState()
  }

  /**
   * 停止轮播
   */
  stop() {
    this.isPaused = true
    this.isExecuting = false
    this.clearTimer()
    this.hideTooltipAtData()
  }

  /**
   * 启动轮播
   * 增加防重入判断，避免多次启动导致多个定时器
   */
  start() {
    // 未完成初始化或已被替换的实例不进入全局启动队列
    if (this.isDestroyed || G2TooltipCarousel.instanceCache.get(this.chart?.container) !== this) {
      return
    }
    // 防止多次启动
    if (!this.isPaused && this.isExecuting) return
    this.isPaused = false
    this.isExecuting = false
    this.index = 0
    G2TooltipCarousel.enqueueStart(this)
  }

  private startFromQueue() {
    if (this.isDestroyed || this.isPaused || G2TooltipCarousel.pageScrolling) {
      return
    }
    this.isExecuting = false
    this.next()
  }

  private suspendForPageScroll() {
    this.isExecuting = false
    this.clearTimer()
    this.cancelColumnSelectionFrames()
    const tooltip = this.getTooltipElement()
    if (tooltip) {
      tooltip.style.visibility = 'hidden'
    }
  }

  private resumeAfterPageScroll() {
    if (
      this.isDestroyed ||
      this.isPaused ||
      !this.chartIsVisible ||
      document.hidden ||
      this.hasParentWithSwitchHidden() ||
      this.chartElement?.matches(':hover') ||
      this.getTooltipElement()?.matches(':hover')
    ) {
      return
    }
    this.isExecuting = false
    G2TooltipCarousel.enqueueStart(this)
  }

  /**
   * 暂停轮播
   */
  pause(force = false) {
    if (force) {
      this.isPaused = true
      this.isExecuting = true
    }
    this.clearTimer()
  }

  /**
   * 鼠标进入图表时只暂停轮播，不清理联动或选中状态
   */
  private mouseEnter() {
    this.pause(true)
    switchTooltipWrapperHost(this.chart, 'hover')
  }

  /**
   * 恢复轮播
   */
  resume(force = false) {
    if (force) {
      this.isPaused = false
    }
    // 没有活跃定时器时才启动轮播
    if (!this.isPaused) {
      this.clearTimer()
      this.isExecuting = false
      G2TooltipCarousel.enqueueStart(this)
    }
  }

  private mouseLeave() {
    this.scheduleResumeAfterHover()
  }

  private tooltipMouseLeave() {
    if (getTooltipDisplayMode(this.chart.container) !== 'hover') return
    this.scheduleResumeAfterHover()
  }

  private scheduleResumeAfterHover() {
    if (this.timers.hoverLeave) {
      clearTimeout(this.timers.hoverLeave)
    }
    this.timers.hoverLeave = window.setTimeout(() => {
      this.timers.hoverLeave = null
      this.resumeAfterHover()
    }, 300)
  }

  private resumeAfterHover() {
    if (this.isDestroyed) return
    const chartHovered = this.chartElement?.matches(':hover')
    const tooltipHovered = this.getTooltipElement()?.matches(':hover')
    if (chartHovered || tooltipHovered) return

    this.pause(true)
    switchTooltipWrapperHost(this.chart, 'carousel')
    this.resume(true)
  }
  /**
   * 销毁实例
   */
  destroy() {
    this.isDestroyed = true
    this.stop()
    this.removeEventListeners()
    this.intersectionObserver?.disconnect()
    G2TooltipCarousel.dequeueStart(this)
    G2TooltipCarousel.instanceCache.delete(this.chart.container)
    G2TooltipCarousel.releasePageScrollListener()
  }

  /**
   * 清除所有定时器
   */
  private clearTimer() {
    G2TooltipCarousel.dequeueStart(this)
    if (this.timers.carousel) {
      clearTimeout(this.timers.carousel)
      this.timers.carousel = null
    }
    if (this.timers.interval) {
      clearTimeout(this.timers.interval)
      this.timers.interval = null
    }
    if (this.timers.nextItem) {
      clearTimeout(this.timers.nextItem)
      this.timers.nextItem = null
    }
    if (this.timers.hoverLeave) {
      clearTimeout(this.timers.hoverLeave)
      this.timers.hoverLeave = null
    }
  }

  /**
   * 检查图表元素以及缓存是否存在
   * @private
   */
  private checkChartElementAndCache() {
    const element = document.getElementById(this.chart.container)
    return element && G2TooltipCarousel.instanceCache.has(this.chart.container)
  }

  /**
   * 判断元素是否有父元素包含 'switch-hidden' 类
   */
  private hasParentWithSwitchHidden() {
    let parent = this.getChartElement()
    while (parent) {
      if (parent.classList.contains('switch-hidden')) {
        return true
      }
      parent = parent.parentElement
    }
    return false
  }

  static paused(id?: string) {
    G2TooltipCarousel.instanceCache?.forEach(instance => {
      if (!id || instance.chart.id === id) {
        setTimeout(() => instance.stop(), 200)
      }
    })
  }

  static suspendExistingForFullscreen() {
    // 全屏副本挂载前仅暂停现有实例，避免底层画布与全屏画布同时轮播
    G2TooltipCarousel.instanceCache?.forEach(instance => {
      instance.isPaused = true
      instance.suspendForPageScroll()
    })
  }

  static resume(id?: string) {
    G2TooltipCarousel.instanceCache?.forEach(instance => {
      if (!id || instance.chart.id === id) {
        setTimeout(
          () => {
            instance.isExecuting = false
            instance.resume(true)
          },
          id ? 500 : 200
        )
      }
    })
  }

  /**
   * 关闭放大图表弹窗，销毁对应实例并恢复其他实例轮播
   */
  static closeEnlargeDialogDestroy(id?: string) {
    if (id) {
      G2TooltipCarousel.instanceCache?.forEach(instance => {
        if (
          instance &&
          instance.chart.id === id &&
          instance.chart.container.indexOf('viewDialog') > -1
        ) {
          instance.destroy()
        }
      })
    }
    setTimeout(() => {
      // 恢复其他实例的轮播
      const instances = Array.from(G2TooltipCarousel.instanceCache.values())
      instances?.forEach(instance => {
        instance.isExecuting = false
        instance?.start()
      })
    }, 400)
  }
}
export default G2TooltipCarousel
