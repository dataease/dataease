import { DualAxes, Plot } from '@antv/g2plot'

/**
 * 使用 Map 来存储实例，键为 chart.container 对象
 */
export const CAROUSEL_MANAGER_INSTANCES = new Map<string, ChartCarouselTooltip>()
/**
 * 支持的图表类型
 */
const CHART_CATEGORY = {
  COLUMN: ['bar', 'bar-stack', 'bar-group', 'bar-group-stack', 'percentage-bar-stack'],
  LINE: ['line', 'area', 'area-stack'],
  MIX: ['chart-mix', 'chart-mix-group', 'chart-mix-stack', 'chart-mix-dual-line'],
  PIE: ['pie', 'pie-donut']
}

/**
 * 判断是否为柱状图
 * @param chartType
 */
export function isColumn(chartType: string) {
  return CHART_CATEGORY.COLUMN.includes(chartType)
}

/**
 * 判断是否为折线图
 * @param chartType
 */
export function isLine(chartType: string) {
  return CHART_CATEGORY.LINE.includes(chartType)
}

/**
 * 判断是否为饼图
 * @param chartType
 */
export function isPie(chartType: string) {
  return CHART_CATEGORY.PIE.includes(chartType)
}

/**
 * 判断是否为组合图
 * @param chartType
 */
export function isMix(chartType: string) {
  return CHART_CATEGORY.MIX.includes(chartType)
}

export function isSupport(chartType: string) {
  return Object.values(CHART_CATEGORY).some(category => category.includes(chartType))
}

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
  // 图表可视性变化
  private observers: Map<string, IntersectionObserver> = new Map()
  // 图表元素大小变化
  private resizeObservers: Map<string, ResizeObserver> = new Map()
  // 图表是否在可视范围内
  private chartIsVisible: boolean

  private constructor(plot: Plot | DualAxes, private chart: Chart, config: CarouselConfig) {
    this.plot = plot
    this.config = { ...DEFAULT_CAROUSEL_CONFIG, ...config }
    this.init()
  }

  /**
   * 创建或更新实例
   * */
  static manage(plot: Plot | DualAxes, chart: Chart, config: CarouselConfig) {
    if (!isSupport(chart.type)) return null
    const container = chart.container
    let instance = CAROUSEL_MANAGER_INSTANCES.get(container)

    CAROUSEL_MANAGER_INSTANCES.forEach(instance => {
      if (container.includes('viewDialog')) {
        instance.paused()
      }
    })

    if (instance) {
      instance.update(plot, chart, config)
      return instance
    }
    if (isSupport(chart.type)) {
      instance = new this(plot, chart, config)
      CAROUSEL_MANAGER_INSTANCES.set(container, instance)
    }

    return instance
  }

  /**
   * 销毁实例
   * @param container
   */
  static destroyByContainer(container: string) {
    const instance = CAROUSEL_MANAGER_INSTANCES.get(container)
    if (instance) {
      instance.destroy()
    }
  }

  /**
   * 通过容器DOM获取对应实例
   * */
  static getInstanceByContainer(container: string) {
    const instance = CAROUSEL_MANAGER_INSTANCES.get(container)
    if (instance) {
      return instance
    }
    return null
  }

  /**
   * 通过chart.id销毁对应实例
   * 关闭放大图表弹窗，销毁对应实例
   * 重启图表自身轮播
   * */
  static closeEnlargeDialogDestroy(id?: string) {
    // 首先，暂停并删除包含 'viewDialog' 的实例
    CAROUSEL_MANAGER_INSTANCES?.forEach((instance, key) => {
      if (instance.chart.id === id && instance.chart.container.includes('viewDialog')) {
        const dialogInstance = CAROUSEL_MANAGER_INSTANCES.get(key)
        if (dialogInstance) {
          dialogInstance.destroy()
        }
      }
    })
    setTimeout(() => {
      // 然后，恢复
      CAROUSEL_MANAGER_INSTANCES?.forEach(instance => {
        if (instance.chartIsVisible) {
          instance.resume()
        }
      })
    }, 400)
  }

  /**
   * 暂停轮播
   * @param id
   */
  static paused(id?: string) {
    CAROUSEL_MANAGER_INSTANCES?.forEach(instance => {
      if (id && instance.chart.id === id) {
        setTimeout(() => instance.paused(), 200)
      }
      if (!id) {
        setTimeout(() => instance.paused(), 200)
      }
    })
  }

  /**
   * @param id
   */
  static resume(id?: string) {
    CAROUSEL_MANAGER_INSTANCES?.forEach(instance => {
      if (instance.chart.id === id) {
        instance.paused()
        setTimeout(() => instance.resume(), 500)
      }
      if (!id) {
        setTimeout(() => instance.resume(), 200)
      }
    })
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
      this.stop()
      return
    }
    // 定义启动嵌套定时器的函数
    const startNestedTimers = () => {
      // 重置当前索引
      this.currentIndex = 0
      // 定义递归处理数据数组的函数
      const processArray = () => {
        if (this.states.paused || this.states.destroyed || !this.isElementFullyVisible()) return
        // 获取当前需要显示的值
        const currentValue = this.values[this.currentIndex]
        // 计算 Tooltip 显示的位置
        const point = this.calculatePosition(currentValue)
        // 高亮当前数据点
        this.highlightElement(currentValue)
        if (point) {
          // 显示 Tooltip，并设置其位置为顶部
          this.plot.chart.showTooltip(point)
          this.plot.chart.getController('tooltip').update()
        }
        // 更新索引，指向下一个数据点
        this.currentIndex++
        if (this.currentIndex > this.values.length) {
          this.currentIndex = 0
          this.hideTooltip()
          this.plot.chart.showTooltip({ x: 0, y: 0 })
          this.plot.chart.getController('tooltip').update()
          this.unHighlightPoint(currentValue)
          this.timers.interval = setTimeout(() => processArray(), this.config.interval)
        } else {
          // 如果未遍历完，继续处理下一个数据点
          this.timers.carousel = setTimeout(() => processArray(), this.config.duration)
        }
      }
      processArray()
    }
    this.stop()
    startNestedTimers()
  }

  /**
   *  判断是否满足启动条件' */
  private shouldStart() {
    return (
      this.chart.customAttr?.tooltip?.show &&
      this.chart.customAttr?.tooltip?.carousel?.enable &&
      this.values.length > 0 &&
      this.chartIsVisible &&
      !this.hasParentWithSwitchHidden(this.plot.chart.ele)
    )
  }

  /**
   *  判断图表是否在可视范围内
   *  */
  private isElementFullyVisible(): boolean {
    // 全屏
    const isFullscreen = document.fullscreenElement !== null
    // 新页面或公共连接
    const isNewPagePublicLink = document
      .getElementById('enlarge-inner-content-' + this.chart.id)
      ?.getBoundingClientRect()
    const isMobileEdit = document.getElementsByClassName('panel-mobile')?.length > 0
    const isMobileList = document.getElementsByClassName('mobile-com-list')?.length > 0
    if (isMobileList) {
      return false
    }
    const rect = this.plot.chart.ele.getBoundingClientRect()
    return (
      rect.top >= (isFullscreen || isNewPagePublicLink || isMobileEdit ? 0 : 64) &&
      rect.left >= 0 &&
      rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) &&
      rect.right <= (window.innerWidth || document.documentElement.clientWidth)
    )
  }
  /**
   *  计算元素位置（核心定位逻辑）
   *  */
  private calculatePosition(value: string) {
    const view = this.plot.chart.views?.[0] || this.plot.chart
    // 饼图特殊处理
    if (CHART_CATEGORY.PIE.includes(this.chart.type)) {
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
    // 处理柱状图和折线图,柱状图固定y轴位置
    const y = CHART_CATEGORY.COLUMN.includes(this.chart.type) ? 0 : [].concat(point?.y)?.[0]
    return { x: [].concat(point?.x)?.[0], y: y }
  }

  /**
   *  计算饼图元素位置
   *  */
  private getPieTooltipPosition(view, value: string) {
    const piePoint = view
      .scale()
      .getGeometries()[0]
      ?.elements.find(item => item.data.field === value)
      ?.getModel()
    if (!piePoint) {
      return { x: 0, y: 0 }
    }
    const coordinates = [
      { x: [].concat(piePoint.x)[0], y: piePoint.y[0] },
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
    if (!xScale) return { x: 0, y: 0 }
    const values = xScale.values
    if (values.length < 2) {
      const point = view
        .getGeometries()?.[0]
        .elements[view.getGeometries()?.[0].elements?.length - 1].getModel()
      return point || { x: 0, y: 0 }
    }
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
    if (CHART_CATEGORY.LINE.includes(this.chart.type)) return
    this.unHighlightPoint(value)
    this.plot.setState(
      this.getHighlightType(),
      (data: any) => data[this.config.xField] === value,
      true
    )
  }

  /**
   * 取消高亮
   * **/
  private unHighlightPoint(value?: string) {
    if (CHART_CATEGORY.LINE.includes(this.chart.type)) return
    this.plot.setState(
      this.getHighlightType(),
      (data: any) => data[this.config.xField] !== value,
      false
    )
  }
  private getHighlightType() {
    return 'active'
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
   * 判断元素是否有父元素包含 'switch-hidden' 类
   * @param element
   */
  public hasParentWithSwitchHidden(element: HTMLElement) {
    let parent = element.parentElement
    while (parent) {
      if (parent.classList.contains('switch-hidden')) {
        return true
      }
      parent = parent.parentElement
    }
    return false
  }

  /**
   *  绑定事件监听
   *  */
  private bindEventListeners() {
    // 定义图表元素ID前缀数组
    // 图表在不同的显示页面可能有不同的ID前缀
    const chartElementIds = ['enlarge-inner-content-', 'enlarge-inner-shape-']
    let chartElement = null

    // 查找图表元素
    for (const idPrefix of chartElementIds) {
      chartElement = document.getElementById(idPrefix + this.chart.id)
      if (chartElement) break
    }

    // 绑定鼠标进入和离开事件
    chartElement?.addEventListener('mouseenter', () => this.paused())
    chartElement?.addEventListener('mouseleave', ev => {
      setTimeout(() => {
        // 获取鼠标位置
        const mouseX = ev.clientX
        const mouseY = ev.clientY
        // 获取div的边界信息
        const rect = chartElement.getBoundingClientRect()
        // 判断鼠标位置是否在div内
        const isInside =
          mouseX >= rect.left + 10 &&
          mouseX <= rect.right - 10 &&
          mouseY >= rect.top + 10 &&
          mouseY <= rect.bottom - 10
        console.log(isInside)
        if (!isInside) {
          this.paused()
          this.resume()
        }
      }, 300)
    })

    // 定义鼠标滚轮事件处理函数
    const handleMouseWheel = this.debounce(() => {
      CAROUSEL_MANAGER_INSTANCES?.forEach(instance => {
        // 如果元素有父元素不包含 'switch-hidden' 类，则继续轮播
        if (!this.hasParentWithSwitchHidden(instance.plot.chart.ele)) {
          instance.paused()
          instance.resume()
        }
      })
    }, 50)
    // 定义 touchmove 事件处理函数（移动端）
    const handleTouchMove = (event: TouchEvent) => {
      handleMouseWheel(event)
    }
    // 获取目标元素，优先全屏预览
    const targetDiv =
      document.getElementById('de-preview-content') ||
      document.getElementById('preview-canvas-main') ||
      document.getElementById('dv-main-center') ||
      document.getElementById('edit-canvas-main') ||
      document.getElementById('canvas-mark-line') ||
      document.getElementById('de-canvas-canvas-main')
    // 绑定目标元素的事件
    if (targetDiv) {
      targetDiv.removeEventListener('wheel', handleMouseWheel)
      targetDiv.addEventListener('wheel', handleMouseWheel)
      //移除和添加 touchmove 事件监听器（移动端）
      targetDiv.removeEventListener('touchmove', handleTouchMove)
      targetDiv.addEventListener('touchmove', handleTouchMove)
    }
    // 页面可见性控制
    document.addEventListener('visibilitychange', () => {
      if (document.visibilityState === 'hidden') {
        CAROUSEL_MANAGER_INSTANCES?.forEach(instance => {
          instance.paused()
        })
      } else if (this.chartIsVisible) {
        CAROUSEL_MANAGER_INSTANCES?.forEach(instance => {
          instance.resume()
        })
      }
    })
    // 元素可视性观察（交叉观察器）
    this.setupIntersectionObserver()
    // 元素大小观察（大小观察器）
    this.setupResizeObserver()
  }

  /**
   * 设置暂停状态
   * */
  private setPaused(state: boolean) {
    this.states.paused = state
    state ? this.stop() : this.startCarousel()
  }
  /**
   * 设置交叉观察器
   * */
  private setupIntersectionObserver() {
    setTimeout(() => {
      // 监听元素可见性变化,全部可见时开始轮播
      if (!this.observers.get(this.plot.chart.ele.id)) {
        this.observers.set(
          this.plot.chart.ele.id,
          new IntersectionObserver(
            entries => {
              entries.forEach(entry => {
                if (entry.intersectionRatio < 0.7) {
                  this.paused()
                  this.chartIsVisible = false
                } else {
                  this.paused()
                  this.chartIsVisible = true
                  this.resume()
                }
              })
            },
            { threshold: [0.7] }
          )
        )
        this.observers.get(this.plot.chart.ele.id).observe(this.plot.chart.ele)
      }
    }, 100)
  }

  /**
   * 设置元素大小观察器
   * 当元素全部可见时
   * 图表的最外层元素
   * @private
   */
  private setupResizeObserver() {
    // 放大图表弹窗不需要监听
    if (this.plot.chart.ele.id.includes('viewDialog')) return
    // 创建防抖回调函数
    const debouncedCallback = (entries: ResizeObserverEntry[]) => {
      for (const entry of entries) {
        if (entry.target) {
          this.debounce(() => {
            this.paused()
            this.resume()
          }, 200)
        }
      }
    }
    // 监听元素大小, 发生变化时重新轮播
    if (!this.resizeObservers.get(this.plot.chart.ele.id)) {
      this.resizeObservers.set(this.plot.chart.ele.id, new ResizeObserver(debouncedCallback))
      this.resizeObservers.get(this.plot.chart.ele.id).observe(this.plot.chart.ele)
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
    this.stop()
    this.clearObserver()
    this.states.destroyed = true
    CAROUSEL_MANAGER_INSTANCES.delete(this.chart.container)
  }
  /**
   * 清除观察器
   * */
  clearObserver() {
    const observer = this.observers.get(this.plot.chart.ele.id)
    if (observer) {
      observer.disconnect()
      this.observers.delete(this.plot.chart.ele.id)
    }
    const resizeObservers = this.resizeObservers.get(this.plot.chart.ele.id)
    if (resizeObservers) {
      resizeObservers.disconnect()
      this.resizeObservers.delete(this.plot.chart.ele.id)
    }
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
