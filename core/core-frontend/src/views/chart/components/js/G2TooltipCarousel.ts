import { parseJson } from '@/views/chart/components/js/util'
import { listenerTooltipShow } from '@/views/chart/components/js/panel/charts/g2/bar/barUtil'

class G2TooltipCarousel {
  /**
   * 存储 G2TooltipCarousel 实例的缓存，key为chart的container
   * @private
   */
  private static instanceCache = new Map<string, G2TooltipCarousel>()
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
    'pie-donut'
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
  private timers = { interval: null, carousel: null, nextItem: null, rectTimer: null }
  private isExecuting: boolean
  private isViewEnlarged: boolean
  private instanceId: number
  private isDestroyed: boolean
  private chartIsVisible: boolean
  // 事件处理函数引用
  private handleMouseEnter: EventListener
  private handleMouseLeave: EventListener
  // 图表所在页面可见性变化处理函数引用
  private handleVisibility: EventListener

  /**
   * 构造函数，初始化轮播实例
   */
  constructor(newChart: any, chart: any, data: any[]) {
    listenerTooltipShow(newChart, chart)
    // 重新创建实例前销毁已有实例
    G2TooltipCarousel.destroyByContainer(chart.container)
    this.newChart = newChart
    this.chart = chart
    const { isLine, lineEncodedX, isMix, mixEncodedX } = this.specialChartTypes()
    if (isLine && lineEncodedX) {
      this.data = this.groupByField(data, { x: lineEncodedX })
    } else if (isMix && mixEncodedX) {
      this.data = this.groupByField(data, { x: mixEncodedX })
    } else {
      const encode = this.newChart?.children?.[0]?.value?.encode
      if (encode) {
        this.data = this.groupByField(data, encode)
      }
    }
    this.index = 0
    this.isPaused = false
    this.isViewEnlarged = false
    this.isDestroyed = false
    this.instanceId = Date.now()
    // 事件处理函数绑定
    this.handleMouseEnter = this.pause.bind(this)
    this.handleMouseLeave = this.mouseLeave.bind(this)
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
    this.chartElement = this.newChart.getContainer()
    if (!this.chartElement) {
      return null
    }
    this.normalInterval = carousel?.stayTime * 1000
    this.finalExtraWait = carousel?.intervalTime * 1000
    this.isViewEnlarged = this.chart.container.indexOf('viewDialog') > -1
    this.checkStopOnViewChange()
    this.init()
    G2TooltipCarousel.instanceCache.set(chart.container, this)
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

  /**
   * 判断图表是否为混合图表
   * @private
   */
  private isMixChart(): boolean {
    return ['chart-mix', 'chart-mix-dual-line', 'chart-mix-group', 'chart-mix-stack'].includes(
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
  static destroyByContainer(containerId?: string) {
    if (containerId) {
      const instance = G2TooltipCarousel.instanceCache.get(containerId)
      if (instance) {
        instance.destroy()
      }
    }
  }

  /**
   * 初始化事件监听和可见性检测
   */
  private init() {
    this.addEventListeners()
  }

  /**
   * 页面可见性事件监听
   * 绑定时用同一函数引用，防止内存泄漏
   */
  private addEventListeners() {
    document.addEventListener('visibilitychange', this.handleVisibility)
    this.chartElement.addEventListener('mouseenter', this.handleMouseEnter)
    this.chartElement.addEventListener('mouseleave', this.handleMouseLeave)
    if (!this.intersectionObserver) {
      this.intersectionObserver = new IntersectionObserver(this.handleIntersection.bind(this), {
        root: null,
        threshold: [0, 0.3, 0.5, 0.7, 1]
      })
    }
    this.intersectionObserver?.observe(this.newChart.getContainer())
    let lastRect = this.newChart.getContainer().getBoundingClientRect()
    this.timers.rectTimer = setInterval(() => {
      const newRect = this.newChart.getContainer().getBoundingClientRect()
      if (newRect.top !== lastRect.top || newRect.left !== lastRect.left) {
        this.restart()
        lastRect = newRect
      }
    }, 16)
  }

  private restart = this.debounce(() => {
    G2TooltipCarousel.instanceCache?.forEach(instance => {
      instance.stop()
      instance.start()
    })
  }, 100)

  /**
   * 移除事件监听
   * 解绑时用同一函数引用，防止内存泄漏
   */
  private removeEventListeners() {
    this.chartElement.removeEventListener('mouseenter', this.handleMouseEnter)
    this.chartElement.removeEventListener('mouseleave', this.handleMouseLeave)
    document.removeEventListener('visibilitychange', this.handleVisibility)
  }

  /**
   * 防抖
   */
  private debounce(func: (...args: any[]) => void, delay: number): (...args: any[]) => void {
    let timeout: number | null = null
    return (...args: any[]) => {
      if (timeout) clearTimeout(timeout)
      timeout = window.setTimeout(() => {
        func(...args)
      }, delay)
    }
  }

  /**
   * IntersectionObserver回调，处理元素进入/离开视口
   * 只有可见区域大于70%时才恢复轮播，否则暂停
   */
  private handleIntersection(_entries) {
    if (!this.isActuallyVisible(this.newChart.getContainer()) || this.chart.dashboardHidden) {
      this.hideTooltipAtData()
      this.chartIsVisible = false
      this.pause(true)
    } else {
      this.chartIsVisible = true
      this.resume(true)
    }
  }

  private isActuallyVisible(el: HTMLElement): boolean {
    if (!el) return false
    // 检查可见比例
    const rect = el.getBoundingClientRect()
    const visibleHeight = Math.min(rect.bottom, window.innerHeight) - Math.max(rect.top, 60)
    const visibleWidth = Math.min(rect.right, window.innerWidth) - Math.max(rect.left, 0)
    const percentHeight = visibleHeight / rect.height
    const percentWidth = visibleWidth / rect.width
    return percentHeight > 0.7 && percentWidth > 0.7
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
    if (this.newChart.getContainer().offsetParent !== null) {
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
      if (this.index >= this.data.length) {
        this.index = 0
      }
      const currentItem = this.data[this.index]
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
    const isMix = this.isMixChart()
    const isLineOrMix = this.isLineChart() || isMix
    this.newChart.emit('element:select', {
      data: { data: [isMix && originalData ? originalData : tooltipData.data.data] }
    })
    const { offsetX, offsetY } = this.getTooltipOffsetX(tooltipData)
    this.newChart.emit('tooltip:show', {
      ...tooltipData,
      ...(offsetX && isLineOrMix && { offsetX }),
      ...(offsetY && { offsetY })
    })
  }

  /**
   * 计算 tooltip 的 X、Y 轴偏移位置
   * @param tooltipData
   */
  getTooltipOffsetX(tooltipData) {
    try {
      const ctx = this.newChart.getContext()
      const root = ctx.canvas.document.getElementsByClassName('plot')[0]
      const { height } = root.getRenderBounds()
      const scaleX = (this.newChart.getScale() || ctx.views[0].scale).x
      const x =
        tooltipData.data.data.x ||
        tooltipData.data.data[this.newChart?.children?.[0]?.value?.encode?.x]
      const [x2] = (this.newChart.getView()?.coordinate || ctx.views[0].coordinate).map([
        scaleX.map(x),
        0.5
      ])
      const { insetLeft, marginLeft, paddingLeft } = root.__data__
      return {
        offsetX: insetLeft + marginLeft + paddingLeft + x2 * this.chart.tScale,
        offsetY: (height / 2) * this.chart.tScale
      }
    } catch (e) {
      console.error('Get Tooltip offsetX fail:', e)
    }
  }

  /**
   * 隐藏 tooltip
   */
  hideTooltipAtData() {
    this.newChart.emit('tooltip:hide')
    this.newChart.emit('element:unselect', {})
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
    // 防止多次启动
    if (!this.isPaused && this.isExecuting) return
    this.isPaused = false
    this.index = 0
    this.next()
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
      this.next()
    }
  }

  /**
   * 鼠标移出事件处理
   * @param ev
   */
  mouseLeave(ev) {
    const el = this.chartElement
    setTimeout(() => {
      // 获取鼠标位置
      const mouseX = ev.clientX
      const mouseY = ev.clientY
      // 获取div的边界信息
      const rect = el.getBoundingClientRect()
      // 判断鼠标位置是否在div内
      const isInside =
        mouseX >= rect.left + 10 &&
        mouseX <= rect.right - 10 &&
        mouseY >= rect.top + 10 &&
        mouseY <= rect.bottom - 10
      if (!isInside) {
        this.pause(true)
        this.resume(true)
      }
    }, 300)
  }
  /**
   * 销毁实例
   */
  destroy() {
    this.isDestroyed = true
    this.stop()
    this.removeEventListeners()
    this.intersectionObserver?.disconnect()
    if (this.timers.rectTimer) {
      clearTimeout(this.timers.rectTimer)
      this.timers.rectTimer = null
    }
    G2TooltipCarousel.instanceCache.delete(this.chart.container)
  }

  /**
   * 清除所有定时器
   */
  private clearTimer() {
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
    let parent = this.newChart.getContainer()
    while (parent) {
      if (parent.classList.contains('switch-hidden')) {
        return true
      }
      parent = parent.parentElement
    }
    return false
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
