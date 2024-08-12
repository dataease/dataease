import { Popup } from '@antv/l7'
import { Plot } from '@antv/l7plot/dist/lib/core/plot'
import isEmpty from 'lodash-es/isEmpty'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { parseJson } from '@/views/chart/components/js/util'
import { Scene } from '@antv/l7-scene'
import { deepCopy } from '@/utils/utils'

export const configCarouselTooltip = (chart, view, data, scene) => {
  if (['bubble-map', 'map'].includes(chart.type)) {
    data = view.source.data.dataArray
      ?.filter(i => i.value)
      .reduce((acc, current) => {
        if (!acc.some(obj => obj.adcode === current.adcode)) {
          acc.push(current)
        }
        return acc
      }, [])
  }
  if (carouselManagerInstances[chart.container]) {
    const instances = carouselManagerInstances[chart.container]
    instances.update(scene, chart, view, data)
  } else {
    new CarouselManager(scene, chart, view, data)
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

  // 保存事件监听函数的引用
  private onMouseEnterHandler: () => void
  private onMouseLeaveHandler: () => void
  private onVisibilityChangeHandler: () => void

  constructor(scene, chart, view, data: any[]) {
    // 绑定事件处理函数
    this.onMouseEnterHandler = this.pauseCarouselPopups.bind(this)
    this.onMouseLeaveHandler = this.resumeCarouselPopups.bind(this)
    this.onVisibilityChangeHandler = this.handleVisibilityChange.bind(this)
    this.clearExistingTimers = this.clearExistingTimers.bind(this)
    this.init(scene, chart, view, data)
  }

  /**
   * 更新轮播弹窗对象内容
   * @param scene
   * @param chart
   * @param view
   * @param data
   */
  public update(scene, chart, view, data: any[]) {
    this.init(scene, chart, view, data)
  }

  /**
   * 初始化轮播弹窗
   * @param scene
   * @param chart
   * @param view
   * @param data
   * @private
   */
  private init(scene, chart, view, data: any[]) {
    this.view = view
    this.chart = chart
    this.scene = scene
    this.data = data
    this.popup = null
    this.currentIndex = 0
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
          this.createSymbolicMapPopup(index)
        } else {
          this.createPopup(index)
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
  private createPopup(index: number): void {
    const tooltipStyle = this.view.tooltip.options.domStyles
    const tooltipBackgroundColor = tooltipStyle['l7plot-tooltip']['background-color']
    const tooltipFontSize = tooltipStyle['l7plot-tooltip']['font-size']
    const style = document.createElement('style')
    style.id = 'style-' + this.chart.container
    style.innerHTML = `
            #${this.chart.container} .l7-popup-content {
                background-color: ${tooltipBackgroundColor} !important;
                font-size: ${tooltipFontSize};
                padding: 10px 10px 6px;
                line-height: 1.6;
            }
            #${this.chart.container} .l7-popup-tip {
                border-top-color: ${tooltipBackgroundColor} !important;
            }
        `
    document.head.appendChild(style)

    const popupData = this.getPopupData(index)
    if (popupData.data) {
      let tooltipItem = ''
      this.getTooltipItems(popupData.data).forEach(fieldData => {
        tooltipItem += `
                    <li style="list-style-type: none; margin-bottom: 4px; white-space: nowrap; display: flex; justify-content: space-between;">
                        <span style="${this.objectToSemicolonSeparated(
                          tooltipStyle['l7plot-tooltip__name']
                        )}">${fieldData.name}</span>
                        <span style="${this.objectToSemicolonSeparated(
                          tooltipStyle['l7plot-tooltip__value']
                        )}">${fieldData.value}</span>
                    </li>`
      })

      const html = `
                <div>
                <div style="${this.objectToSemicolonSeparated(
                  tooltipStyle['l7plot-tooltip__title']
                )}">${popupData.data.name}</div>
                    <ul style="${this.objectToSemicolonSeparated(
                      tooltipStyle['l7plot-tooltip__list']
                    )}">
                        ${tooltipItem}
                    </ul>
                </div>
            `

      this.popup.setLngLat({ lng: popupData.centroid[0], lat: popupData.centroid[1] })
      this.popup.setHTML(html)
      this.popup.closeButton = false
      this.view.addLayer(this.popup)
      // 地图层高亮
      this.view.scene
        .getLayers()
        ?.find(i => i.name === 'highlightLayer')
        ?.setData(this.getActiveData(index))
      if (this.chart.type === 'bubble-map') {
        // 气泡地图高亮
        const { _id } = this.view.scene
          .getLayers()
          ?.find(i => i.name === 'bubbleLayer')
          ?.layerSource.data.dataArray.find(i => i.name === this.data[index].name)
        this.view.scene
          .getLayers()
          ?.find(i => i.name === 'bubbleLayer' && i.coordCenter)
          ?.setActive(_id, { color: 'rgba(30,90,255,1)' })
      }
    }
  }

  private getActiveData(index): any {
    return {
      type: 'FeatureCollection',
      features: [
        this.view.currentDistrictData.features.find(
          i => i.properties.name === this.data[index].name
        )
      ]
    }
  }

  /**
   * 获取弹窗信息，包括原始数据及位置信息
   * @param index
   * @private
   */
  private getPopupData(index: number): any {
    return {
      data: this.data[index],
      centroid: this.view.currentDistrictData.features.find(
        i => i.properties.name === this.data[index].name
      )?.properties.centroid
    }
  }

  /**
   * 将对象转换为 CSS 属性
   * @param obj
   * @private
   */
  private objectToSemicolonSeparated(obj: any): string {
    let result = ''
    for (const key in obj) {
      if (obj.hasOwnProperty(key)) {
        result += `${this.convertToSnakeCase(key)}:${obj[key]};`
      }
    }
    return result
  }

  private cancelHighlightLayer(index?: number): void {
    this.view.scene
      ?.getLayers()
      ?.find(i => i.name === 'highlightLayer')
      ?.setData({ type: 'FeatureCollection', features: [] })
    if (this.chart.type === 'bubble-map') {
      const { _id } = this.view.scene
        ?.getLayers()
        ?.find(i => i.name === 'bubbleLayer')
        ?.layerSource.data.dataArray.find(i => i.name === this.data[index].name)
      this.view.scene
        .getLayers()
        ?.find(i => i.name === 'bubbleLayer' && i.coordCenter)
        ?.setActive(_id, {
          color: this.view.scene
            .getLayers()
            .find(i => i.name === 'bubbleLayer')
            .styleAttributeService.getLayerStyleAttribute('color').scale.field
        })
    }
    if (this.chart.type === 'symbolic-map') {
      const lngField = this.chart.xAxis[0].dataeaseName
      const latField = this.chart.xAxis[1].dataeaseName
      const { _id } = this.scene
        ?.getLayers()
        ?.find(i => i.type === 'PointLayer')
        ?.layerSource.data.dataArray.find(i => {
          const targetLng = this.data[index][lngField]
          const targetLat = this.data[index][latField]
          return i[lngField] === targetLng && i[latField] === targetLat
        })
      this.scene
        .getLayers()
        ?.find(i => i.type === 'PointLayer' && i.coordCenter)
        ?.setActive(_id, {
          color: this.scene
            .getLayers()
            .find(i => i.type === 'PointLayer')
            .styleAttributeService.getLayerStyleAttribute('color').scale.field
        })
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
        style.innerHTML = `
          #${this.chart.container} .l7-popup-content {
            background-color: ${tooltip.backgroundColor} !important;
            padding: 6px 10px 6px;
            line-height: 1.6;
          }
          #${this.chart.container} .l7-popup-tip {
           border-top-color: ${tooltip.backgroundColor} !important;
          }
        `
        document.head.appendChild(style)
        const lngField = this.chart.xAxis[0].dataeaseName
        const latField = this.chart.xAxis[1].dataeaseName
        const htmlPrefix = `<div style='font-size:${tooltip.fontSize}px;color:${tooltip.color};'>`
        const htmlSuffix = '</div>'
        const data = this.view.sourceOption.data[index]
        if (data && data.details?.length) {
          const fieldData = {
            ...data,
            ...Object.fromEntries(mergeDetailsToMap(data.details))
          }
          const content = buildTooltipContent(tooltip, fieldData, showFields)
          const html = `${htmlPrefix}${content}${htmlSuffix}`
          this.popup.setLngLat({
            lng: data[lngField],
            lat: data[latField]
          })
          this.popup.setHTML(html)
          this.popup.closeButton = false
          this.scene.addPopup(this.popup)
          this.popup.addTo(this.scene)
          const { _id } = this.scene
            .getLayers()
            ?.find(i => i.type === 'PointLayer')
            ?.layerSource.data.dataArray.find(i => {
              const targetLng = this.data[index][lngField]
              const targetLat = this.data[index][latField]
              return i[lngField] === targetLng && i[latField] === targetLat
            })
          this.scene
            .getLayers()
            ?.find(i => i.type === 'PointLayer' && i.coordCenter)
            ?.setActive(_id, { color: 'rgba(30,90,255,1)' })
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
          content = content.replace(`\${${field.split('@')[1]}}`, fieldData[field.split('@')[0]])
        })
      } else {
        showFields.forEach(field => {
          content += `<span style="margin-bottom: 4px">${field.split('@')[1]}: ${
            fieldData[field.split('@')[0]]
          }</span><br>`
        })
      }
      return content
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
