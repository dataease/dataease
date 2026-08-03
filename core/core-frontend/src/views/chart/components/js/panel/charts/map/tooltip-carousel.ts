import { Popup } from '@antv/l7'
import { Plot } from '@antv/l7plot/dist/lib/core/plot'
import isEmpty from 'lodash-es/isEmpty'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { parseJson } from '@/views/chart/components/js/util'
import { Scene } from '@antv/l7-scene'
import { deepCopy, sanitizeTooltipHtml } from '@/utils/utils'

const MAP_TOOLTIP_BACKGROUND_COLOR_VAR = '--de-map-tooltip-background-color'
const MAP_TOOLTIP_FONT_SIZE_VAR = '--de-map-tooltip-font-size'
const DEFAULT_TOOLTIP_BACKGROUND_COLOR = '#FFFFFF'
const DEFAULT_TOOLTIP_COLOR = '#000000'
const DEFAULT_TOOLTIP_FONT_SIZE = 10
const INVALID_CSS_VALUE_PATTERN = /[;{}<>]/

const normalizeTooltipColor = (value: unknown, fallback: string): string => {
  if (typeof value !== 'string') {
    return fallback
  }
  const color = value.trim()
  if (!color || INVALID_CSS_VALUE_PATTERN.test(color)) {
    return fallback
  }
  const style = document.createElement('span').style
  style.color = color
  return style.color ? color : fallback
}

const normalizeTooltipFontSize = (value: unknown): number => {
  const fontSize = typeof value === 'number' ? value : Number.parseFloat(`${value ?? ''}`)
  if (!Number.isFinite(fontSize)) {
    return DEFAULT_TOOLTIP_FONT_SIZE
  }
  return Math.min(200, Math.max(8, fontSize))
}

export const escapeTooltipHtml = (value: unknown): string => {
  return `${value ?? ''}`.replace(
    /[&<>'"]/g,
    char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[char])
  )
}

export const setupMapTooltipStyle = (
  container: string,
  backgroundColor: unknown,
  fontSize?: unknown
): string => {
  const containerElement = document.getElementById(container)
  if (containerElement) {
    // 动态样式值仅通过 CSSOM 写入，避免进入样式文本
    containerElement.style.setProperty(
      MAP_TOOLTIP_BACKGROUND_COLOR_VAR,
      normalizeTooltipColor(backgroundColor, DEFAULT_TOOLTIP_BACKGROUND_COLOR)
    )
    if (fontSize !== undefined) {
      containerElement.style.setProperty(
        MAP_TOOLTIP_FONT_SIZE_VAR,
        `${normalizeTooltipFontSize(fontSize)}px`
      )
    }
  }
  return `#${CSS.escape(container)}`
}

export const createSymbolicTooltipElement = (
  content: string,
  tooltip: Record<string, unknown>,
  fontFamily?: unknown
): HTMLElement => {
  const element = document.createElement('div')
  element.style.fontSize = `${normalizeTooltipFontSize(tooltip.fontSize)}px`
  element.style.color = normalizeTooltipColor(tooltip.color, DEFAULT_TOOLTIP_COLOR)
  if (typeof fontFamily === 'string') {
    element.style.fontFamily = fontFamily
  }
  element.innerHTML = sanitizeTooltipHtml(content)
  return element
}

export const configCarouselTooltip = (chart, view, data, scene, customSubArea?, drawOption?) => {
  if (['bubble-map', 'map'].includes(chart.type)) {
    const sourceData = view.source?.data?.dataArray
    data = sourceData
      ?.filter(i => i?.dimensionList?.length > 0)
      ?.reduce((acc, current) => {
        const existingItem = acc.find(obj => {
          if (drawOption?.areaId?.startsWith('custom_')) {
            return obj.areaName === current.areaName
          } else {
            return obj.name === current.name || (obj.adcode && obj.adcode === current.adcode)
          }
        })
        if (!existingItem) {
          acc.push(current)
        }
        return acc
      }, [])
  }
  if (carouselManagerInstances[chart.container]) {
    const instances = carouselManagerInstances[chart.container]
    instances.update(scene, chart, view, data, customSubArea, drawOption)
  } else {
    new CarouselManager(scene, chart, view, data, customSubArea, drawOption)
  }
}
export const carouselManagerInstances: { [key: string]: CarouselManager } = {}

/**
 * 轮播管理类
 */
export class CarouselManager {
  /**
   * 停留时长定时器
   * @private
   */
  private popupTimeoutId: number | null = null
  /**
   * 轮播间隔定时器
   * @private
   */
  private popupIntervalId: number | null = null
  /**
   * 是否暂停轮播
   * @private
   */
  private isPaused = false
  /**
   * 当前显示的数据索引
   * @private
   */
  private currentIndex = 0
  /**
   * 地图实例，气泡地图用
   * @private
   */
  private scene: Scene
  private chart: Chart
  /**
   * 轮播弹窗的位置数据
   * @private
   */
  private view: Plot
  private data: any[]
  /**
   * 停留时长
   * @private
   */
  private stayTime: number
  /**
   * 轮播间隔
   * @private
   */
  private intervalTime: number
  /**
   * 轮播弹窗
   * @private
   */
  private popup: Popup

  /**
   * 自定义区域列表
   * @private
   */
  private customSubArea: CustomGeoSubArea[]

  /**
   * 渲染参数
   * @private
   */
  private drawOption: L7PlotDrawOptions

  // 保存事件监听函数的引用
  private onMouseEnterHandler: () => void
  private onMouseLeaveHandler: () => void
  private onVisibilityChangeHandler: () => void

  constructor(scene, chart, view, data: any[], customSubArea, drawOption?) {
    // 绑定事件处理函数
    this.onMouseEnterHandler = this.pauseCarouselPopups.bind(this)
    this.onMouseLeaveHandler = this.resumeCarouselPopups.bind(this)
    this.onVisibilityChangeHandler = this.handleVisibilityChange.bind(this)
    this.clearExistingTimers = this.clearExistingTimers.bind(this)
    this.init(scene, chart, view, data, customSubArea, drawOption)
  }

  /**
   * 更新轮播弹窗对象内容
   * @param scene
   * @param chart
   * @param view
   * @param data
   * @param customSubArea
   */
  public update(scene, chart, view, data: any[], customSubArea, drawOption?) {
    this.init(scene, chart, view, data, customSubArea, drawOption)
  }

  /**
   * 初始化轮播弹窗
   * @param scene
   * @param chart
   * @param view
   * @param data
   * @private
   */
  private init(scene, chart, view, data: any[], customSubArea, drawOption?) {
    this.view = view
    this.chart = chart
    this.scene = scene
    this.data = Array.isArray(data) ? data.filter(Boolean) : []
    this.popup = null
    this.currentIndex = 0
    this.customSubArea = customSubArea
    this.drawOption = drawOption
    this.clearPreviousInstance(this.chart.container)
    if (
      this.chart.customAttr?.tooltip?.show &&
      this.chart.customAttr?.tooltip?.carousel?.enable &&
      this.data.length > 0
    ) {
      this.popup = new Popup({ closeButton: false, maxWidth: 600 })
      const carousel = this.chart.customAttr?.tooltip?.carousel
      this.stayTime = carousel.stayTime * 1000
      this.intervalTime = carousel.intervalTime * 1000
      this.startCarouselPopups()
      const divElement = document.getElementById(this.chart.container)
      divElement.addEventListener('mouseenter', this.pauseCarouselPopups)
      divElement.addEventListener('mouseleave', this.resumeCarouselPopups)
      // 移动端符号地图不支持mouseenter和mouseleave事件，这里特殊处理一下
      if (this.chart.type === 'symbolic-map') {
        // 监听符号触摸事件, 暂停轮播
        scene?.getLayers()?.[0]?.addListener('touchend', () => {
          this.pauseCarouselPopups()
        })
        // 地图空白区域触摸事件, 启动轮播
        scene?.getMapCanvasContainer()?.addEventListener('touchend', () => {
          this.resumeCarouselPopups()
        })
      }
      // 监听页面可见性变化
      document.addEventListener('visibilitychange', this.handleVisibilityChange)
      carouselManagerInstances[this.chart.container] = this
    }
  }

  private handleVisibilityChange = (): void => {
    if (document.hidden) {
      this.clearPreviousInstance(this.chart.container)
    } else {
      this.startCarouselPopups()
    }
  }

  /**
   * 清除之前的实例数据
   * @param containerId
   * @private
   */
  private clearPreviousInstance(containerId: string): void {
    if (carouselManagerInstances[containerId]) {
      const instance = carouselManagerInstances[containerId]
      this.clearExistingTimers()
      instance.popup?.remove()
      instance.removeStyle()
    }
  }

  /**
   * 开始轮播
   * @private
   */
  private startCarouselPopups(): void {
    this.clearExistingTimers()
    this.carouselPopups()
  }

  /**
   * 鼠标移入暂停轮播
   */
  private pauseCarouselPopups = (): void => {
    if (this.popup) {
      this.popup?.remove()
    }
    this.removeStyle()
    this.isPaused = true
    this.clearExistingTimers()
  }

  /**
   * 鼠标移出开始轮播
   */
  private resumeCarouselPopups = (): void => {
    if (this.isPaused) {
      this.isPaused = false
      this.startCarouselPopups()
    }
  }

  /**
   * 管理轮播弹窗的显示
   *
   * 此方法用于处理轮播弹窗的显示逻辑它会根据当前的索引显示对应的弹窗，
   * 并在一定时间后自动移除当前弹窗并显示下一个弹窗
   *
   * @private
   */
  private carouselPopups(): void {
    const showPopup = (index: number): void => {
      this.removeStyle()
      const containerElement = document.getElementById(this.chart.container)
      if (containerElement) {
        if (this.chart.type === 'symbolic-map') {
          // 轮播进行时，隐藏隐藏鼠标悬浮的tooltip
          const mouseTooltip = containerElement.getElementsByClassName(
            'l7-popup-' + this.chart.container
          )
          for (const tooltip of Array.from(mouseTooltip)) {
            const tooltipElement = tooltip as HTMLElement
            tooltipElement.classList.add('l7-popup-hide')
          }
          this.createSymbolicMapPopup(index)
        } else {
          if (this.chart.type === 'map') {
            // 轮播进行时，隐藏隐藏鼠标悬浮的tooltip
            const mouseTooltip = containerElement.getElementsByClassName('l7plot-tooltip-container')
            for (const tooltip of Array.from(mouseTooltip)) {
              const tooltipElement = tooltip as HTMLElement
              tooltipElement.style.display = 'none'
            }
          }
          const popupCreated = this.createPopup(index)
          if (!popupCreated) {
            this.currentIndex++
            if (this.currentIndex >= this.data.length) {
              this.currentIndex = 0
            }
            if (this.currentIndex !== index) {
              this.popupIntervalId = window.setTimeout(() => {
                showPopup(this.currentIndex)
              }, this.intervalTime)
            }
            return
          }
        }
        this.clearExistingTimers()
        this.popupTimeoutId = window.setTimeout(() => {
          this.currentIndex++
          this.popup?.remove()
          this.cancelHighlightLayer(index)
          if (this.currentIndex >= this.data.length) {
            this.currentIndex = 0
          }
          this.popupIntervalId = window.setTimeout(() => {
            showPopup(this.currentIndex)
          }, this.intervalTime)
        }, this.stayTime)
      } else {
        this.clearExistingTimers()
      }
    }

    showPopup(this.currentIndex)
  }

  /**
   * 清除定时器
   * @private
   */
  private readonly clearExistingTimers = (): void => {
    if (this.popupTimeoutId !== null) {
      clearTimeout(this.popupTimeoutId)
      this.popupTimeoutId = 0
    }
    if (this.popupIntervalId !== null) {
      clearInterval(this.popupIntervalId)
      this.popupIntervalId = 0
    }
  }

  /**
   * 移除样式
   * 每次创建弹窗前移除之前的样式
   * @private
   */
  private removeStyle(): void {
    const styleToRemove = document.getElementById('style-' + this.chart.container)
    if (styleToRemove) {
      styleToRemove.remove()
      styleToRemove.parentNode?.removeChild(styleToRemove)
    }
  }

  /**
   * 创建弹窗信息
   * @param index
   * @private
   */
  private createPopup(index: number): boolean {
    if (!this.popup) {
      return false
    }
    const tooltipStyle = this.view.tooltip.options.domStyles
    const tooltipBackgroundColor = tooltipStyle['l7plot-tooltip']['background-color']
    const tooltipFontSize = tooltipStyle['l7plot-tooltip']['font-size']
    const style = document.createElement('style')
    style.id = 'style-' + this.chart.container
    const tooltipSelector = setupMapTooltipStyle(
      this.chart.container,
      tooltipBackgroundColor,
      tooltipFontSize
    )
    style.textContent = `
            ${tooltipSelector} .l7-popup-content {
                background-color: var(${MAP_TOOLTIP_BACKGROUND_COLOR_VAR}, ${DEFAULT_TOOLTIP_BACKGROUND_COLOR}) !important;
                font-size: var(${MAP_TOOLTIP_FONT_SIZE_VAR}, ${DEFAULT_TOOLTIP_FONT_SIZE}px);
                padding: 10px 10px 6px;
                line-height: 1.6;
            }
            ${tooltipSelector} .l7-popup-tip {
                border-top-color: var(${MAP_TOOLTIP_BACKGROUND_COLOR_VAR}, ${DEFAULT_TOOLTIP_BACKGROUND_COLOR}) !important;
            }
        `
    document.head.appendChild(style)

    const popupData = this.getPopupData(index)
    if (popupData?.data && popupData.centroid?.length >= 2) {
      this.popup.setLngLat({ lng: popupData.centroid[0], lat: popupData.centroid[1] })
      this.popup.setHTML(this.createPopupContent(popupData.data, tooltipStyle))
      this.popup.closeButton = false
      this.view.addLayer(this.popup)
      // 地图层高亮
      this.view.scene
        .getLayers()
        ?.find(i => i.name === 'highlightLayer')
        ?.setData(this.getActiveData(index))
      if (this.chart.type === 'bubble-map') {
        // 气泡地图高亮
        const bubbleItem = this.view.scene
          .getLayers()
          ?.find(i => i.name === 'bubbleLayer')
          ?.layerSource?.data?.dataArray?.find(i => i.name === this.data[index].name)
        if (bubbleItem?._id) {
          this.view.scene
            .getLayers()
            ?.find(i => i.name === 'bubbleLayer' && i.coordCenter)
            ?.setActive(bubbleItem._id, { color: 'rgba(30,90,255,1)' })
        }
      }
      return true
    }
    return false
  }

  private getActiveData(index): any {
    if (this.drawOption?.areaId?.startsWith('custom_')) {
      const result = {
        type: 'FeatureCollection',
        features: []
      }
      const area = this.customSubArea?.find(a => a.name === this.data[index].areaName)
      const areaMap = (this.view.currentDistrictData?.features || []).reduce((p, n) => {
        p['156' + n.properties.adcode] = n
        return p
      }, {})
      area?.scopeArr?.forEach(s => {
        if (areaMap[s]) {
          result.features.push(areaMap[s])
        }
      })
      return result
    }
    return {
      type: 'FeatureCollection',
      features: [
        (this.view.currentDistrictData?.features || []).find(
          i => i.properties.name === this.data[index].name
        )
      ].filter(Boolean)
    }
  }

  /**
   * 获取弹窗信息，包括原始数据及位置信息
   * @param index
   * @private
   */
  private getPopupData(index: number): any {
    if (this.drawOption?.areaId?.startsWith('custom_')) {
      const data = this.data[index]
      const area = this.customSubArea?.find(a => a.name === data.areaName)
      data.name = data.areaName
      return {
        data,
        centroid: area?.centroid || data.centroid || data.properties?.centroid
      }
    } else {
      const data = this.data[index]
      const feature = (this.view.currentDistrictData?.features || []).find(
        i => i.properties.name === data.name
      )
      return {
        data,
        centroid:
          feature?.properties?.centroid ||
          data?.centroid ||
          data?.properties?.centroid ||
          (data?.x !== undefined && data?.y !== undefined ? [data.x, data.y] : undefined)
      }
    }
  }

  private createPopupContent(data, tooltipStyle): HTMLElement {
    const content = document.createElement('div')
    const title = document.createElement('div')
    this.applyElementStyles(title, tooltipStyle['l7plot-tooltip__title'])
    title.textContent = `${data.name ?? ''}`
    content.appendChild(title)

    const list = document.createElement('ul')
    this.applyElementStyles(list, tooltipStyle['l7plot-tooltip__list'])
    this.getTooltipItems(data).forEach(fieldData => {
      const item = document.createElement('li')
      item.style.listStyleType = 'none'
      item.style.marginBottom = '4px'
      item.style.whiteSpace = 'nowrap'
      item.style.display = 'flex'
      item.style.justifyContent = 'space-between'

      const name = document.createElement('span')
      this.applyElementStyles(name, tooltipStyle['l7plot-tooltip__name'])
      name.textContent = `${fieldData.name ?? ''}`
      const value = document.createElement('span')
      this.applyElementStyles(value, tooltipStyle['l7plot-tooltip__value'])
      value.textContent = `${fieldData.value ?? ''}`
      item.append(name, value)
      list.appendChild(item)
    })
    content.appendChild(list)
    return content
  }

  private applyElementStyles(element: HTMLElement, styles: Record<string, unknown>): void {
    Object.entries(styles || {}).forEach(([key, value]) => {
      if (value !== null && value !== undefined) {
        element.style.setProperty(this.convertToSnakeCase(key), `${value}`)
      }
    })
  }

  private cancelHighlightLayer(index?: number): void {
    this.view.scene
      ?.getLayers()
      ?.find(i => i.name === 'highlightLayer')
      ?.setData({ type: 'FeatureCollection', features: [] })
    if (this.chart.type === 'bubble-map') {
      const bubbleItem = this.view.scene
        ?.getLayers()
        ?.find(i => i.name === 'bubbleLayer')
        ?.layerSource?.data?.dataArray?.find(i => i.name === this.data[index]?.name)
      if (bubbleItem?._id) {
        this.view.scene
          .getLayers()
          ?.find(i => i.name === 'bubbleLayer' && i.coordCenter)
          ?.setActive(bubbleItem._id, {
            color: this.view.scene
              .getLayers()
              .find(i => i.name === 'bubbleLayer')
              .styleAttributeService.getLayerStyleAttribute('color').scale.field
          })
      }
    }
    if (this.chart.type === 'symbolic-map') {
      const lngField = this.chart.xAxis[0].dataeaseName
      const latField = this.chart.xAxis[1].dataeaseName
      const pointItem = this.scene
        ?.getLayers()
        ?.find(i => i.type === 'PointLayer')
        ?.layerSource?.data?.dataArray?.find(i => {
          const targetLng = this.data[index][lngField]
          const targetLat = this.data[index][latField]
          return i[lngField] === targetLng && i[latField] === targetLat
        })
      if (pointItem?._id) {
        this.scene
          .getLayers()
          ?.find(i => i.type === 'PointLayer' && i.coordCenter)
          ?.setActive(pointItem._id, {
            color: this.scene
              .getLayers()
              .find(i => i.type === 'PointLayer')
              .styleAttributeService.getLayerStyleAttribute('color').scale.field
          })
      }
    }
  }

  /**
   * 将驼峰式命名转换为蛇形命名
   * @param str
   * @private
   */
  private convertToSnakeCase(str: string): string {
    return str.replace(/([A-Z])/g, match => '-' + match.toLowerCase())
  }

  /**
   * 获取弹窗字段信息
   * 与tooltip要显示的内容一致
   * @param data
   * @private
   */
  private getTooltipItems(data) {
    const result = []
    const customAttr = parseJson(this.chart.customAttr)
    const tooltip = customAttr.tooltip
    const formatterMap = tooltip.seriesTooltipFormatter
      ?.filter(i => i.show)
      .reduce((pre, next) => {
        pre[next.id] = next
        return pre
      }, {}) as Record<string, SeriesFormatter>
    if (isEmpty(formatterMap)) {
      return result
    }
    const head = data
    const formatter = formatterMap[head.quotaList?.[0]?.id]
    if (!isEmpty(formatter)) {
      const originValue = parseFloat(head.value as string)
      const value = valueFormatter(originValue, formatter.formatterCfg)
      const name = isEmpty(formatter.chartShowName) ? formatter.name : formatter.chartShowName
      result.push({ ...head, name, value: `${value ?? ''}` })
    }
    head.dynamicTooltipValue?.forEach(item => {
      const formatter = formatterMap[item.fieldId]
      if (formatter) {
        const value = valueFormatter(parseFloat(item.value), formatter.formatterCfg)
        const name = isEmpty(formatter.chartShowName) ? formatter.name : formatter.chartShowName
        result.push({ color: 'grey', name, value: `${value ?? ''}` })
      }
    })
    return result
  }

  /**
   * 符号地图特殊处理，tooltip的配置可自定义显示内容
   * @param index
   * @private
   */
  private createSymbolicMapPopup(index): void {
    const buildTooltip = () => {
      const customAttr = this.chart.customAttr ? parseJson(this.chart.customAttr) : null
      if (customAttr?.tooltip?.show) {
        if (!this.popup) {
          return undefined
        }
        const { tooltip } = deepCopy(customAttr)
        let showFields = tooltip.showFields || []
        if (!tooltip.showFields || tooltip.showFields.length === 0) {
          showFields = [
            ...this.chart.xAxisExt.map(i => `${i.dataeaseName}@${i.name}`),
            ...this.chart.xAxis.map(i => `${i.dataeaseName}@${i.name}`)
          ]
        }
        const style = document.createElement('style')
        style.id = 'style-' + this.chart.container
        const tooltipSelector = setupMapTooltipStyle(this.chart.container, tooltip.backgroundColor)
        style.textContent = `
          ${tooltipSelector} .l7-popup-content {
            background-color: var(${MAP_TOOLTIP_BACKGROUND_COLOR_VAR}, ${DEFAULT_TOOLTIP_BACKGROUND_COLOR}) !important;
            padding: 6px 10px 6px;
            line-height: 1.6;
          }
          ${tooltipSelector} .l7-popup-tip {
           border-top-color: var(${MAP_TOOLTIP_BACKGROUND_COLOR_VAR}, ${DEFAULT_TOOLTIP_BACKGROUND_COLOR}) !important;
          }
        `
        document.head.appendChild(style)
        const lngField = this.chart.xAxis[0].dataeaseName
        const latField = this.chart.xAxis[1].dataeaseName
        const data = this.view.sourceOption.data[index]
        if (data && data.details?.length) {
          const fieldData = {
            ...data,
            ...Object.fromEntries(mergeDetailsToMap(data.details))
          }
          const content = buildTooltipContent(tooltip, fieldData, showFields)
          this.popup.setLngLat({
            lng: data[lngField],
            lat: data[latField]
          })
          this.popup.setHTML(createSymbolicTooltipElement(content, tooltip))
          this.popup.closeButton = false
          this.scene.addPopup(this.popup)
          this.popup.addTo(this.scene)
          const pointItem = this.scene
            .getLayers()
            ?.find(i => i.type === 'PointLayer')
            ?.layerSource?.data?.dataArray?.find(i => {
              const targetLng = this.data[index][lngField]
              const targetLat = this.data[index][latField]
              return i[lngField] === targetLng && i[latField] === targetLat
            })
          if (pointItem?._id) {
            this.scene
              .getLayers()
              ?.find(i => i.type === 'PointLayer' && i.coordCenter)
              ?.setActive(pointItem._id, { color: 'rgba(30,90,255,1)' })
          }
        }
      }
      return undefined
    }

    /**
     * 构建 tooltip 内容
     * @param tooltip
     * @param fieldData
     * @param showFields
     * @returns {string}
     */
    const buildTooltipContent = (tooltip, fieldData, showFields) => {
      let content = ''
      if (tooltip.customContent) {
        content = tooltip.customContent
        showFields.forEach(field => {
          content = content.replace(
            `\${${field.split('@')[1]}}`,
            escapeTooltipHtml(fieldData[field.split('@')[0]])
          )
        })
      } else {
        showFields.forEach(field => {
          content += `<span>${escapeTooltipHtml(field.split('@')[1])}: ${escapeTooltipHtml(
            fieldData[field.split('@')[0]]
          )}</span><br>`
        })
      }
      return content.replace(/\n/g, '<br>')
    }
    /**
     * 合并详情到 map
     * @param details
     * @returns {Map<string, any>}
     */
    const mergeDetailsToMap = details => {
      const resultMap = new Map()
      details.forEach(item => {
        Object.entries(item).forEach(([key, value]) => {
          if (resultMap.has(key)) {
            const existingValue = resultMap.get(key)
            if (existingValue !== value) {
              resultMap.set(key, `${existingValue}, ${value}`)
            }
          } else {
            resultMap.set(key, value)
          }
        })
      })
      return resultMap
    }
    buildTooltip()
  }
}
