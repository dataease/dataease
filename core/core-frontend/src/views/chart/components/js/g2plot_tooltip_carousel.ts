import { DualAxes, Plot } from '@antv/g2plot'

/**
 * 使用 Map 来存储实例，键为 chart.container 对象
 */
export const carouselManagerInstances = new Map<unknown, ChartCarouselTooltip>()
// 支持的图表类型集合
const SUPPORT_CHART_TYPES = new Set([
  'line',
  'area',
  'area-stack',
  'bar',
  'bar-stack',
  'bar-group',
  'bar-group-stack',
  'chart-mix',
  'chart-mix-group',
  'chart-mix-stack',
  'chart-mix-dual-line',
  'pie'
])

// 轮播配置默认值
const DEFAULT_CAROUSEL_CONFIG: Required<CarouselConfig> = {
  xField: '',
  duration: 2000,
  interval: 2000,
  loop: true
}

type CarouselConfig = {
  xField: string
  duration?: number
  interval?: number
  loop?: boolean
}

/**
 * 图表轮播提示管理类
 * */
class ChartCarouselTooltip {
  private plot: Plot | DualAxes
  private config: Required<CarouselConfig>
  private currentIndex = 0
  private values: string[] = []
  // 合并定时器管理
  private timers = { interval: null, carousel: null }
  private states = { paused: false, destroyed: false }
  private observers: Map<Element, IntersectionObserver> = new Map()
  // 滚动事件计时器
  private scrollTimeout: number | null = null
  // 图表是否在可视范围内
  private chartIsVisible: boolean
  private tooltipContainer: HTMLElement

  private constructor(plot: Plot | DualAxes, private chart: Chart, config: CarouselConfig) {
    this.plot = plot
    this.config = { ...DEFAULT_CAROUSEL_CONFIG, ...config }
    this.init()
  }

  /**
   * 创建或更新实例
   * */
  static manage(plot: Plot | DualAxes, chart: Chart, config: CarouselConfig) {
    const container = chart.container
    const exists = carouselManagerInstances.get(container)

    if (exists) {
      // 切换到不支持的图表时
      if (!SUPPORT_CHART_TYPES.has(chart.type)) {
        this.destroyByContainer(container)
        return null
      }
      exists.update(plot, chart, config)
      return exists
    }

    if (SUPPORT_CHART_TYPES.has(chart.type)) {
      const instance = new this(plot, chart, config)
      carouselManagerInstances.set(container, instance)
      return instance
    }
    return null
  }

  /**
   * 通过容器DOM销毁对应实例（外部调用接口）
   * */
  static destroyByContainer(container: string) {
    const instance = carouselManagerInstances.get(container)
    if (instance) {
      instance.destroy()
      // 弱引用会自动清除，这里显式删除确保及时清理
      carouselManagerInstances.delete(container)
    }
  }

  /**
   * 通过容器DOM获取对应实例（外部调用接口）
   * */
  static getInstanceByContainer(container: string) {
    const instance = carouselManagerInstances.get(container)
    if (instance) {
      return instance
    }
    return null
  }

  /**
   * 初始化核心逻辑
   * */
  private init() {
    this.values = [].concat(this.getUniqueValues())
    if (!this.values.length) return
    this.chartIsVisible = true
    this.states.paused = false
    this.states.destroyed = false
    this.bindEventListeners()
    this.startCarousel()
  }

  /**
   * 获取图表唯一值集合
   * */
  private getUniqueValues() {
    const data =
      this.plot instanceof DualAxes
        ? [...this.plot.options.data[0], ...this.plot.options.data[1]]
        : this.plot.options.data

    return [...new Set(data.map(item => item[this.config.xField]))]
  }

  /**
   * 启动轮播
   * */
  private startCarousel() {
    if (!this.shouldStart()) {
      // 如果未启用轮播功能，则停止当前轮播，并解绑相关鼠标事件
      this.stop()
      this.plot.chart.off('plot:mouseenter')
      this.plot.chart.off('plot:mouseleave')
      return
    }
    // 定义启动嵌套定时器的函数
    const startNestedTimers = () => {
      // 重置当前索引
      this.currentIndex = 0
      // 定义递归处理数据数组的函数
      const processArray = () => {
        // 设置定时器，等待 duration 毫秒后执行
        this.timers.carousel = window.setTimeout(
          async () => {
            if (this.states.paused || this.states.destroyed) return
            // 获取当前需要显示的值
            const currentValue = this.values[this.currentIndex]
            // 高亮当前数据点
            this.highlightElement(currentValue)
            // 计算 Tooltip 显示的位置
            const point = this.calculatePosition(currentValue)
            if (point) {
              // 显示 Tooltip，并设置其位置为顶部
              this.plot.chart.showTooltip(point)
              this.plot.chart.getController('tooltip').update()
            }
            // 更新索引，指向下一个数据点
            this.currentIndex++
            // 如果已经遍历完所有数据点
            if (this.currentIndex >= this.values.length) {
              // 等待当前提示显示完成
              await new Promise(resolve => setTimeout(resolve, this.config.duration))
              // 取消所有数据点的高亮状态
              this.unHighlightPoint()
              // 隐藏 Tooltip
              this.hideTooltip()
              // 清除当前定时器
              clearTimeout(this.timers.carousel)
              // 等待配置的轮播间隔
              await new Promise(resolve => setTimeout(resolve, this.config.interval))
              // 重置索引，重新开始轮播
              this.currentIndex = 0
              processArray()
            } else {
              // 如果未遍历完，继续处理下一个数据点
              processArray()
            }
          },
          this.currentIndex === 0 ? 0 : this.config.duration
        )
      }
      // 开始处理数据数组
      processArray()
    }
    // 启动嵌套定时器
    this.debounce(() => {
      this.stop()
      startNestedTimers()
    }, 300)()
  }

  /**
   *  判断是否满足启动条件' */
  private shouldStart() {
    return (
      this.chart.customAttr?.tooltip?.show &&
      this.chart.customAttr?.tooltip?.carousel?.enable &&
      this.values.length > 0 &&
      this.chartIsVisible
    )
  }

  /**
   *  计算元素位置（核心定位逻辑）
   *  */
  private calculatePosition(value: string) {
    const view = this.plot.chart.views?.[0] || this.plot.chart
    // 饼图特殊处理
    if (['pie', 'pie-donut'].includes(this.chart.type)) {
      return this.getPieTooltipPosition(view, value)
    }
    if (this.plot instanceof DualAxes) {
      return this.getDualAxesTooltipPosition(view, value)
    }
    const types = view
      .scale()
      .getGeometries()
      .map(item => item.type)
    let point = { x: 0, y: 0 }
    if (!types.length) return point
    types.forEach(type => {
      if (type === 'interval' || type === 'point') {
        point = view
          .scale()
          .getGeometries()
          .find(item => item.type === type)
          .elements.find(item => item.data.field === value && (item.model.x || item.model.y))?.model
      }
    })
    return { x: [].concat(point?.x)?.[0], y: 60 }
  }

  /**
   *  计算饼图元素位置
   *  */
  private getPieTooltipPosition(view, value: string) {
    const piePoint = view
      .scale()
      .getGeometries()[0]
      .elements.find(item => item.data.field === value)
      .getModel()
    const coordinates = [
      { x: piePoint.x[0], y: piePoint.y[0] },
      { x: piePoint.x[0], y: piePoint.y[1] },
      { x: piePoint.x[1], y: piePoint.y[0] },
      { x: piePoint.x[1], y: piePoint.y[1] }
    ]
    const index = coordinates.findIndex(coord => {
      const items = this.plot.chart.getTooltipItems(coord)
      return items.some(item => item.data.field === value)
    })
    if (index !== -1) {
      return coordinates[index]
    } else {
      return {
        x: piePoint.x[0],
        y: piePoint.y[0]
      }
    }
  }

  /**
   * 获取双轴图表的 Tooltip 位置
   * @param view
   * @param value
   * @private
   */
  private getDualAxesTooltipPosition(view, value: string) {
    const xScale = view.getXScale()
    const values = xScale.values
    const [rangeStart, rangeEnd] = xScale.range
    const totalMonths = values.length
    const bandWidth = (rangeEnd - rangeStart) / totalMonths
    const index = values.indexOf(value)
    const xPos = rangeStart + bandWidth * (index + 0.5)
    return view.getCoordinate().convert({ x: xPos, y: 0 })
  }

  /**
   * 高亮指定元素
   * */
  private highlightElement(value: string) {
    this.unHighlightPoint(value)
    const activeType = this.chart.type === 'pie' ? 'selected' : 'inactive'
    this.plot.setState(activeType, (data: any) => data[this.config.xField] === value, true)
  }

  /**
   * 取消高亮
   * **/
  private unHighlightPoint(value?: string) {
    const activeType = this.chart.type === 'pie' ? 'selected' : 'inactive'
    this.plot.setState(activeType, (data: any) => data[this.config.xField] !== value, false)
  }

  /**
   *  隐藏工具提示
   *  */
  private hideTooltip() {
    const container = this.getTooltipContainer()
    if (container) {
      container.style.display = 'none'
    }
  }

  /**
   *  获取工具提示容器
   *  */
  private getTooltipContainer() {
    const tooltipCtl = this.plot.chart.getController('tooltip')
    if (!tooltipCtl) {
      return
    }
    return tooltipCtl.tooltip?.cfg?.container
  }

  /**
   *  绑定事件监听
   *  */
  private bindEventListeners() {
    // 用于监听在不同的浏览页面的滚动事件
    const elementIds = ['de-canvas-canvas-main', 'preview-canvas-main', 'canvas-mark-line']
    let deCanvasElement = null
    for (const id of elementIds) {
      deCanvasElement = document.getElementById(id)
      if (deCanvasElement) break
    }
    if (!deCanvasElement) {
      this.unHighlightPoint()
      this.hideTooltip()
      this.setPaused(true)
    }
    deCanvasElement?.addEventListener('scroll', this.handleScroll.bind(this))
    const chartElement = document.getElementById(this.chart.container)
    chartElement.addEventListener('mouseenter', () => {
      this.unHighlightPoint()
      this.hideTooltip()
      this.setPaused(true)
    })

    // 当鼠标离开 chart 时，检查状态
    chartElement.addEventListener('mouseleave', () => {
      if (deCanvasElement) {
        this.setPaused(false)
      }
    })

    if (deCanvasElement) {
      // 页面可见性控制
      document.addEventListener('visibilitychange', () => {
        if (document.visibilityState === 'hidden') {
          this.unHighlightPoint()
          this.hideTooltip()
          this.setPaused(true)
        } else if (this.chartIsVisible) {
          this.setPaused(false)
        }
      })
      // 元素可视性观察（交叉观察器）
      this.setupIntersectionObserver()
    }
  }

  private handleScroll() {
    this.hideTooltip()
    this.unHighlightPoint()
    this.stop()
    this.debounce(() => {
      clearTimeout(this.scrollTimeout)
      // 设置新的定时器，如果在 200 毫秒内没有新的滚动事件，就认为滚动停止
      this.scrollTimeout = setTimeout(() => {
        this.hideTooltip()
        this.unHighlightPoint()
        // 可视范围内才恢复轮播
        if (this.chartIsVisible) {
          this.setPaused(false)
        }
      }, 200)
    }, 300)()
  }

  /**
   * 设置暂停状态
   * */
  private setPaused(state: boolean) {
    this.debounce(() => {
      this.states.paused = state
      state ? this.stop() : this.startCarousel()
      this.plot.chart.render(true)
    }, 300)()
  }

  /**
   * 设置交叉观察器
   * */
  private setupIntersectionObserver() {
    // 监听元素可见性变化,全部可见时开始轮播
    if (!this.observers.get(this.plot.chart.ele.id)) {
      this.observers.set(
        this.plot.chart.ele.id,
        new IntersectionObserver(
          entries => {
            entries.forEach(entry => {
              if (entry.intersectionRatio < 1) {
                this.hideTooltip()
                this.unHighlightPoint()
                this.chartIsVisible = false
                this.setPaused(true)
              } else {
                this.chartIsVisible = true
                this.setPaused(false)
              }
            })
          },
          { threshold: 1 }
        )
      )
      this.observers.get(this.plot.chart.ele.id).observe(this.plot.chart.ele)
    }
  }

  /**
   * 更新配置
   * */
  private update(plot: Plot | DualAxes, chart: Chart, config: CarouselConfig) {
    this.stop()
    this.plot = plot
    this.chart = chart
    this.config = { ...this.config, ...config }
    this.currentIndex = 0
    this.init()
  }

  /**
   * 停止定时器
   * @private
   */
  private stop() {
    clearTimeout(this.timers.interval)
    clearTimeout(this.timers.carousel)
    this.timers = { interval: null, carousel: null }
  }

  /**
   * 销毁实例
   * */
  destroy() {
    this.states.destroyed = true
    this.stop()
    this.plot.chart.off('plot:mouseenter')
    this.plot.chart.off('plot:mouseleave')
  }

  /** 暂停 */
  paused() {
    this.hideTooltip()
    this.unHighlightPoint()
    this.setPaused(true)
  }

  /** 恢复 */
  resume() {
    this.setPaused(false)
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
}

export default ChartCarouselTooltip
