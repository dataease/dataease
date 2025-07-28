import { useI18n } from '@/hooks/web/useI18n'
import {
  L7ChartView,
  L7Config,
  L7DrawConfig,
  L7Wrapper
} from '@/views/chart/components/js/panel/types/impl/l7'
import { MAP_EDITOR_PROPERTY_INNER } from '@/views/chart/components/js/panel/charts/map/common'
import {
  getColorFormAlphaColor,
  hexColorToRGBA,
  parseJson,
  svgStrToUrl
} from '@/views/chart/components/js/util'
import { deepCopy } from '@/utils/utils'
import { Scene } from '@antv/l7-scene'
import { PointLayer } from '@antv/l7-layers'
import { LayerPopup, Popup } from '@antv/l7'
import {
  getMapCenter,
  getMapScene,
  getMapStyle,
  mapRendered,
  qqMapRendered
} from '@/views/chart/components/js/panel/common/common_antv'
import { configCarouselTooltip } from '@/views/chart/components/js/panel/charts/map/tooltip-carousel'
import { filter } from 'lodash-es'
const { t } = useI18n()

/**
 * 符号地图
 */
export class SymbolicMap extends L7ChartView<Scene, L7Config> {
  properties: EditorProperty[] = [
    'background-overall-component',
    'border-style',
    'basic-style-selector',
    'symbolic-style-selector',
    'title-selector',
    'label-selector',
    'tooltip-selector',
    'threshold',
    'bubble-animate'
  ]
  propertyInner: EditorPropertyInner = {
    ...MAP_EDITOR_PROPERTY_INNER,
    'basic-style-selector': [
      'colors',
      'alpha',
      'mapBaseStyle',
      'zoom',
      'showLabel',
      'autoFit',
      'mapCenter',
      'zoomLevel'
    ],
    'symbolic-style-selector': ['symbolicMapStyle'],
    'label-selector': ['color', 'fontSize', 'showFields', 'customContent'],
    'tooltip-selector': [
      'color',
      'fontSize',
      'showFields',
      'customContent',
      'show',
      'backgroundColor',
      'carousel'
    ],
    threshold: ['lineThreshold']
  }
  axis: AxisType[] = ['xAxis', 'xAxisExt', 'extBubble', 'filter', 'extLabel', 'extTooltip']
  axisConfig: AxisConfig = {
    xAxis: {
      name: `${t('chart.symbolic_map_coordinates')} / ${t('chart.dimension')}`,
      type: 'd',
      limit: 2
    },
    xAxisExt: {
      name: `${t('chart.color')} / ${t('chart.dimension')}`,
      type: 'd',
      limit: 1,
      allowEmpty: true
    },
    extBubble: {
      name: `${t('chart.bubble_size')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1,
      tooltip: t('chart.symbolic_map_bubble_size_tip'),
      allowEmpty: true
    }
  }
  constructor() {
    super('symbolic-map', [])
  }

  async drawChart(drawOption: L7DrawConfig<L7Config>) {
    const { chart, container, action } = drawOption
    const containerDom = document.getElementById(container)
    const rect = containerDom?.getBoundingClientRect()
    if (rect?.height <= 0) {
      return new L7Wrapper(drawOption.chartObj?.getScene(), [])
    }
    const xAxis = deepCopy(chart.xAxis)
    let basicStyle
    let miscStyle
    if (chart.customAttr) {
      basicStyle = parseJson(chart.customAttr).basicStyle
      miscStyle = parseJson(chart.customAttr).misc
    }

    const mapKey = await this.getMapKey()
    const mapStyle = getMapStyle(mapKey, basicStyle)

    let center = getMapCenter(basicStyle)
    // 联动时，聚焦到数据点，多个取第一个
    if (
      chart.chartExtRequest?.linkageFilters?.length &&
      xAxis?.length === 2 &&
      chart.data?.tableRow.length
    ) {
      // 经度
      const lng = chart.data?.tableRow?.[0][chart.xAxis[0].dataeaseName]
      // 纬度
      const lat = chart.data?.tableRow?.[0][chart.xAxis[1].dataeaseName]
      center = [lng, lat]
    }
    const chartObj = drawOption.chartObj as unknown as L7Wrapper<L7Config, Scene>
    let scene = chartObj?.getScene()
    scene = await getMapScene(
      chart,
      scene,
      container,
      mapKey,
      basicStyle,
      miscStyle,
      mapStyle,
      center
    )

    this.configZoomButton(chart, scene, mapKey)
    if (xAxis?.length < 2) {
      return new L7Wrapper(scene, undefined)
    }
    const configList: L7Config[] = []
    const symbolicLayer = await this.buildSymbolicLayer(chart, scene)
    configList.push(symbolicLayer)
    const tooltipLayer = this.buildTooltip(chart, container, symbolicLayer, scene)
    if (tooltipLayer) {
      scene.addPopup(tooltipLayer)
    }
    this.buildLabel(chart, configList)
    symbolicLayer.once('inited', () => {
      mapRendered(container)
    })
    symbolicLayer.on('inited', () => {
      chart.container = container
      configCarouselTooltip(chart, symbolicLayer, symbolicLayer.sourceOption.data, scene)
      qqMapRendered(scene)
    })
    symbolicLayer.on('click', ev => {
      const data = ev.feature
      const dimensionList = []
      const quotaList = []
      chart.data.fields.forEach((item, index) => {
        Object.keys(data).forEach(key => {
          if (key.startsWith('f_') && item.dataeaseName === key) {
            if (index === 0) {
              dimensionList.push({
                id: item.id,
                dataeaseName: item.dataeaseName,
                value: data[key]
              })
            } else {
              quotaList.push({
                id: item.id,
                dataeaseName: item.dataeaseName,
                value: data[key]
              })
            }
          }
        })
      })
      action({
        x: ev.x,
        y: ev.y,
        data: {
          data: {
            ...data,
            value: quotaList[0].value,
            name: dimensionList[0].id,
            dimensionList: dimensionList,
            quotaList: quotaList
          }
        }
      })
    })

    return new L7Wrapper(scene, configList)
  }

  /**
   * 构建符号图层
   * @param chart
   */
  buildSymbolicLayer = async (chart, scene: Scene) => {
    const { basicStyle } = parseJson(chart.customAttr) as ChartAttr
    const xAxis = deepCopy(chart.xAxis)
    const xAxisExt = deepCopy(chart.xAxisExt)
    const extBubble = deepCopy(chart.extBubble)
    const {
      mapSymbolOpacity,
      mapSymbolSize,
      mapSymbol,
      mapSymbolStrokeWidth,
      colors,
      alpha,
      mapSymbolSizeMin,
      mapSymbolSizeMax
    } = deepCopy(basicStyle)
    const colorsWithAlpha = colors.map(color => hexColorToRGBA(color, alpha))
    let colorIndex = 0
    // 存储已分配的颜色
    const colorAssignments = new Map()
    const sizeKey = extBubble.length > 0 ? extBubble[0].dataeaseName : ''

    //条件颜色
    const { threshold, bubbleCfg } = parseJson(chart.senior)
    let conditions = []
    if (threshold.enable) {
      conditions = threshold.lineThreshold ?? []
    }
    const extBubbleIds = chart.extBubble.map(i => i.id)
    conditions = filter(conditions, c => extBubbleIds.includes(c.fieldId))

    const baseColor = colorsWithAlpha[0]
    const baseColorList = []

    const data = chart.data?.tableRow
      ? chart.data.tableRow.map((item, index) => {
          item['_index'] = '_index' + index
          // 颜色标识
          const identifier = item[xAxisExt[0]?.dataeaseName]
          // 检查该标识是否已有颜色分配，如果没有则分配
          let color = colorAssignments.get(identifier)
          if (!color) {
            color = colorsWithAlpha[colorIndex++ % colorsWithAlpha.length]
            // 记录分配的颜色
            colorAssignments.set(identifier, color)
          }

          baseColorList[index] = color

          if (conditions.length > 0) {
            for (let i = 0; i < conditions.length; i++) {
              const c = conditions[i]
              const value = item[c.field.dataeaseName]
              for (const t of c.conditions) {
                const v = t.value

                //保存一下颜色到map
                const _color = getColorFormAlphaColor(t.color)

                if (t.term === 'between') {
                  const start = parseFloat(t.min)
                  const end = parseFloat(t.max)
                  if (start <= value && value <= end) {
                    color = hexColorToRGBA(_color, alpha)
                    baseColorList[index] = color
                  }
                } else if ('lt' === t.term) {
                  if (value < v) {
                    color = hexColorToRGBA(_color, alpha)
                    baseColorList[index] = color
                  }
                } else if ('le' === t.term) {
                  if (value <= v) {
                    color = hexColorToRGBA(_color, alpha)
                    baseColorList[index] = color
                  }
                } else if ('gt' === t.term) {
                  if (value > v) {
                    color = hexColorToRGBA(_color, alpha)
                    baseColorList[index] = color
                  }
                } else if ('ge' === t.term) {
                  if (value >= v) {
                    color = hexColorToRGBA(_color, alpha)
                    baseColorList[index] = color
                  }
                } else if ('eq' === t.term) {
                  if (value === v) {
                    color = hexColorToRGBA(_color, alpha)
                    baseColorList[index] = color
                  }
                } else if ('not_eq' === t.term) {
                  if (value !== v) {
                    color = hexColorToRGBA(_color, alpha)
                    baseColorList[index] = color
                  }
                }
              }
            }
          }

          return {
            ...item,
            color,
            size: parseInt(item[sizeKey]) ?? mapSymbolSize,
            name: identifier
          }
        })
      : []
    const pointLayer = new PointLayer({ autoFit: !(basicStyle.autoFit === false) })
      .source(data, {
        parser: {
          type: 'json',
          x: xAxis[0].dataeaseName,
          y: xAxis[1].dataeaseName
        }
      })
      .active(true)
    if (xAxisExt[0]?.dataeaseName) {
      if (basicStyle.mapSymbol === 'custom' && basicStyle.customIcon) {
        // 图片无法改色
        if (basicStyle.customIcon.startsWith('data')) {
          scene.removeImage('customIcon')
          await scene.addImage('customIcon', basicStyle.customIcon)
          pointLayer.shape('customIcon')
        } else {
          const parser = new DOMParser()
          for (let index = 0; index < Math.min(baseColorList.length, colorIndex + 1); index++) {
            const color = baseColorList[index]
            const fillRegex = /(fill="[^"]*")/g
            const svgStr = basicStyle.customIcon.replace(fillRegex, '')
            const doc = parser.parseFromString(svgStr, 'image/svg+xml')
            const svgEle = doc.documentElement
            svgEle.setAttribute('fill', color)
            scene.removeImage(`icon-${color}`)
            await scene.addImage(`icon-${color}`, svgStrToUrl(svgEle.outerHTML))
          }
          pointLayer.shape('color', c => {
            return `icon-${c}`
          })
        }
      } else {
        pointLayer.shape(mapSymbol).color('_index', baseColorList)
        pointLayer.style({
          stroke: {
            field: 'color'
          },
          strokeWidth: mapSymbolStrokeWidth,
          opacity: mapSymbolOpacity / 10
        })
      }
    } else {
      if (basicStyle.mapSymbol === 'custom' && basicStyle.customIcon) {
        scene.removeImage('customIcon')
        if (basicStyle.customIcon.startsWith('data')) {
          await scene.addImage('customIcon', basicStyle.customIcon)
          pointLayer.shape('customIcon')
        } else {
          const parser = new DOMParser()
          const color = baseColor
          const fillRegex = /(fill="[^"]*")/g
          const svgStr = basicStyle.customIcon.replace(fillRegex, '')
          const doc = parser.parseFromString(svgStr, 'image/svg+xml')
          const svgEle = doc.documentElement
          svgEle.setAttribute('fill', color)
          await scene.addImage(`customIcon`, svgStrToUrl(svgEle.outerHTML))
          pointLayer.shape('customIcon')
        }
      } else {
        pointLayer
          .shape(mapSymbol)
          .color('_index', baseColorList)
          .style({
            stroke: {
              field: 'color'
            },
            strokeWidth: mapSymbolStrokeWidth,
            opacity: mapSymbolOpacity / 10
          })
      }
    }
    if (sizeKey) {
      pointLayer.size('size', [mapSymbolSizeMin, mapSymbolSizeMax])
    } else {
      pointLayer.size(mapSymbolSize)
    }
    if (bubbleCfg && bubbleCfg.enable) {
      pointLayer.animate({ enable: true, speed: bubbleCfg.speed, rings: bubbleCfg.rings })
      pointLayer.style({
        ...pointLayer.style,
        opacity: mapSymbolOpacity / 2
      })
    } else {
      pointLayer.animate(false)
    }
    return pointLayer
  }

  /**
   * 合并详情到 map
   * @param details
   * @returns {Map<string, any>}
   */
  mergeDetailsToMap = details => {
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

  /**
   * 清除 popup
   * @param container
   */
  clearPopup = container => {
    const containerElement = document.getElementById(container)
    containerElement?.querySelectorAll('.l7-popup').forEach((element: Element) => element.remove())
  }

  /**
   * 构建 tooltip
   * @param chart
   * @param pointLayer
   */
  buildTooltip = (chart, container, pointLayer, scene) => {
    const customAttr = chart.customAttr ? parseJson(chart.customAttr) : null
    this.clearPopup(container)
    if (customAttr?.tooltip?.show) {
      const { tooltip } = deepCopy(customAttr)
      let showFields = tooltip.showFields || []
      if (!tooltip.showFields || tooltip.showFields.length === 0) {
        showFields = [
          ...chart.xAxisExt.map(i => `${i.dataeaseName}@${i.name}`),
          ...chart.xAxis.map(i => `${i.dataeaseName}@${i.name}`)
        ]
      }
      // 修改背景色
      const styleId = 'tooltip-' + container
      const styleElement = document.getElementById(styleId)
      if (styleElement) {
        styleElement.remove()
        styleElement.parentNode?.removeChild(styleElement)
      }
      const style = document.createElement('style')
      style.id = styleId
      style.innerHTML = `
          #${container} .l7-popup-content {
            background-color: ${tooltip.backgroundColor} !important;
            padding: 6px 10px 6px;
            line-height: 1.6;
            border-top-left-radius: 3px;
          }
          #${container} .l7-popup-tip {
           border-top-color: ${tooltip.backgroundColor} !important;
          }
        `
      document.head.appendChild(style)
      const htmlPrefix = `<div style='font-size:${tooltip.fontSize}px;color:${tooltip.color};font-family: ${chart.fontFamily}'>`
      const htmlSuffix = '</div>'
      const containerElement = document.getElementById(container)
      if (containerElement) {
        containerElement.addEventListener('mousemove', event => {
          const rect = containerElement.getBoundingClientRect()
          const mouseX = event.clientX - rect.left
          const mouseY = event.clientY - rect.top
          const tooltipElement = containerElement.getElementsByClassName('l7-popup')
          for (let i = 0; i < tooltipElement?.length; i++) {
            const element = tooltipElement[i] as HTMLElement
            element.firstElementChild.style.display = 'none'
            element.style.transform = 'translate(15px, 12px)'
            const isNearRightEdge =
              containerElement.clientWidth - mouseX <= element.clientWidth + 10
            const isNearBottomEdge = containerElement.clientHeight - mouseY <= element.clientHeight
            let transform = ''
            if (isNearRightEdge) {
              transform += 'translateX(-120%) translateY(15%) '
            }
            if (isNearBottomEdge) {
              transform += 'translateX(15%) translateY(-80%) '
            }
            if (transform) {
              element.style.transform = transform.trim()
            }
          }
        })
      }
      pointLayer.on('touchend', e => {
        if (e.lngLat) {
          const fieldData = {
            ...e.feature,
            ...Object.fromEntries(this.mergeDetailsToMap(e.feature.details ?? []))
          }
          const content = this.buildTooltipContent(tooltip, fieldData, showFields)
          const popup = new Popup({
            lngLat: e.lngLat,
            title: '',
            closeButton: false,
            closeOnClick: true,
            html: `${htmlPrefix}${content}${htmlSuffix}`
          })
          scene.addPopup(popup)
        }
      })
      return new LayerPopup({
        anchor: 'top-left',
        className: 'l7-popup-' + container,
        items: [
          {
            layer: pointLayer,
            customContent: item => {
              const fieldData = {
                ...item,
                ...Object.fromEntries(this.mergeDetailsToMap(item.details))
              }
              const content = this.buildTooltipContent(tooltip, fieldData, showFields)
              return `${htmlPrefix}${content}${htmlSuffix}`
            }
          }
        ],
        trigger: 'hover'
      })
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
  buildTooltipContent = (tooltip, fieldData, showFields) => {
    let content = ``
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
    return content.replace(/\n/g, '<br>')
  }

  /**
   * 构建 label
   * @param chart
   * @param configList
   */
  buildLabel = (chart, configList) => {
    const xAxis = deepCopy(chart.xAxis)

    const customAttr = chart.customAttr ? parseJson(chart.customAttr) : null
    if (customAttr?.label?.show) {
      const { label } = customAttr
      const data = chart.data?.tableRow || []
      let showFields = label.showFields || []
      if (!label.showFields || label.showFields.length === 0) {
        showFields = [
          ...chart.xAxisExt.map(i => `${i.dataeaseName}@${i.name}`),
          ...chart.xAxis.map(i => `${i.dataeaseName}@${i.name}`)
        ]
      }
      data.forEach(item => {
        const fieldData = {
          ...item,
          ...Object.fromEntries(this.mergeDetailsToMap(item.details))
        }
        let content = label.customContent || ''

        if (content) {
          showFields.forEach(field => {
            const [fieldKey, fieldName] = field.split('@')
            content = content.replace(`\${${fieldName}}`, fieldData[fieldKey])
          })
        } else {
          content = showFields.map(field => fieldData[field.split('@')[0]]).join(',')
        }

        content = content.replace(/\n/g, '')
        item.textLayerContent = content
      })

      configList.push(
        new PointLayer()
          .source(data, {
            parser: {
              type: 'json',
              x: xAxis[0].dataeaseName,
              y: xAxis[1].dataeaseName
            }
          })
          .shape('textLayerContent', 'text')
          .color(label.color)
          .size(label.fontSize)
          .style({
            textAllowOverlap: label.fullDisplay,
            textAnchor: 'center',
            textOffset: [0, 0],
            fontFamily: chart.fontFamily ? chart.fontFamily : undefined
          })
      )
    }
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    chart.customAttr.label = {
      ...chart.customAttr.label,
      show: false
    }
    chart.customAttr.basicStyle = {
      ...chart.customAttr.basicStyle,
      mapSymbolOpacity: 5,
      mapStyle: 'normal'
    }
    return chart
  }
}
