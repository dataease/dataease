import { useI18n } from '@/hooks/web/useI18n'
import {
  L7ChartView,
  L7Config,
  L7DrawConfig,
  L7Wrapper
} from '@/views/chart/components/js/panel/types/impl/l7'
import { MAP_EDITOR_PROPERTY_INNER } from '@/views/chart/components/js/panel/charts/map/common'
import { flow, hexColorToRGBA, parseJson } from '@/views/chart/components/js/util'
import { deepCopy } from '@/utils/utils'
import { GaodeMap } from '@antv/l7-maps'
import { Scene } from '@antv/l7-scene'
import { PointLayer } from '@antv/l7-layers'
import { LayerPopup } from '@antv/l7'
import { mapRendered, mapRendering } from '@/views/chart/components/js/panel/common/common_antv'
const { t } = useI18n()

/**
 * 符号地图
 */
export class SymbolicMap extends L7ChartView<Scene, L7Config> {
  properties: EditorProperty[] = [
    'background-overall-component',
    'basic-style-selector',
    'title-selector',
    'label-selector',
    'tooltip-selector'
  ]
  propertyInner: EditorPropertyInner = {
    ...MAP_EDITOR_PROPERTY_INNER,
    'basic-style-selector': ['colors', 'alpha', 'mapBaseStyle', 'symbolicMapStyle', 'zoom'],
    'label-selector': ['color', 'fontSize', 'showFields', 'customContent'],
    'tooltip-selector': [
      'color',
      'fontSize',
      'showFields',
      'customContent',
      'show',
      'backgroundColor'
    ]
  }
  axis: AxisType[] = ['xAxis', 'xAxisExt', 'extBubble', 'filter', 'extLabel', 'extTooltip']
  axisConfig: AxisConfig = {
    xAxis: {
      name: `经纬度 / ${t('chart.dimension')}`,
      type: 'd',
      limit: 2
    },
    xAxisExt: {
      name: `颜色 / ${t('chart.dimension')}`,
      type: 'd',
      limit: 1
    },
    extBubble: {
      name: `${t('chart.bubble_size')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1,
      tooltip: '该指标生效时，样式基础样式中的大小属性将失效'
    }
  }
  constructor() {
    super('symbolic-map', [])
  }

  async drawChart(drawOption: L7DrawConfig<L7Config>) {
    const { chart, container, action } = drawOption
    const xAxis = deepCopy(chart.xAxis)
    let basicStyle
    let miscStyle
    if (chart.customAttr) {
      basicStyle = parseJson(chart.customAttr).basicStyle
      miscStyle = parseJson(chart.customAttr).misc
    }

    const mapStyle = `amap://styles/${basicStyle.mapStyle ? basicStyle.mapStyle : 'normal'}`
    const key = await this.getMapKey()
    // 底层
    const scene = new Scene({
      id: container,
      logoVisible: false,
      map: new GaodeMap({
        token: key ?? undefined,
        style: mapStyle,
        pitch: miscStyle.mapPitch,
        center: [104.434765, 38.256735],
        zoom: 1.8
      })
    })
    mapRendering(container)
    scene.once('loaded', () => {
      mapRendered(container)
    })
    if (xAxis?.length < 2) {
      return new L7Wrapper(scene, undefined)
    }
    const configList: L7Config[] = []
    const symbolicLayer = this.buildSymbolicLayer(chart, basicStyle)
    configList.push(symbolicLayer)
    const tooltipLayer = this.buildTooltip(chart, container, symbolicLayer)
    if (tooltipLayer) {
      scene.addPopup(tooltipLayer)
    }
    this.buildLabel(chart, configList)
    this.configZoomButton(chart, scene)
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
  buildSymbolicLayer = (chart, basicStyle) => {
    const xAxis = deepCopy(chart.xAxis)
    const xAxisExt = deepCopy(chart.xAxisExt)
    const extBubble = deepCopy(chart.extBubble)
    const { mapSymbolOpacity, mapSymbolSize, mapSymbol, mapSymbolStrokeWidth, colors, alpha } =
      deepCopy(basicStyle)
    const colorsWithAlpha = colors.map(color => hexColorToRGBA(color, alpha))
    let colorIndex = 0
    // 存储已分配的颜色
    const colorAssignments = new Map()
    const sizeKey = extBubble.length > 0 ? extBubble[0].dataeaseName : ''
    const data = chart.data?.tableRow
      ? chart.data.tableRow.map(item => {
          // 颜色标识
          const identifier = item[xAxisExt[0]?.dataeaseName]
          // 检查该标识是否已有颜色分配，如果没有则分配
          let color = colorAssignments.get(identifier)
          if (!color) {
            color = colorsWithAlpha[colorIndex++ % colorsWithAlpha.length]
            // 记录分配的颜色
            colorAssignments.set(identifier, color)
          }
          return {
            ...item,
            color,
            size: item[sizeKey] ?? mapSymbolSize,
            name: identifier
          }
        })
      : []
    const pointLayer = new PointLayer({ autoFit: true })
      .source(data, {
        parser: {
          type: 'json',
          x: xAxis[0].dataeaseName,
          y: xAxis[1].dataeaseName
        }
      })
      .shape(mapSymbol)
      .active(true)
    if (xAxisExt[0]?.dataeaseName) {
      pointLayer.color(xAxisExt[0]?.dataeaseName, colorsWithAlpha)
      pointLayer.style({
        stroke: {
          field: 'color'
        },
        strokeWidth: mapSymbolStrokeWidth,
        opacity: mapSymbolOpacity / 10
      })
    } else {
      pointLayer.color(colorsWithAlpha[0])
      pointLayer.style({
        stroke: colorsWithAlpha[0],
        strokeWidth: mapSymbolStrokeWidth,
        opacity: mapSymbolOpacity / 10
      })
    }
    if (sizeKey) {
      pointLayer.size('size', [4, 30])
    } else {
      pointLayer.size(mapSymbolSize)
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
   * 构建 tooltip
   * @param chart
   * @param pointLayer
   */
  buildTooltip = (chart, container, pointLayer) => {
    const customAttr = chart.customAttr ? parseJson(chart.customAttr) : null
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
      const style = document.createElement('style')
      style.innerHTML = `
          #${container} .l7-popup-content {
            background-color: ${tooltip.backgroundColor} !important;
          }
          #${container} .l7-popup-tip {
           border-top-color: ${tooltip.backgroundColor} !important;
          }
        `
      document.head.appendChild(style)
      const htmlPrefix = `<div style='font-size:${tooltip.fontSize}px;color:${tooltip.color}'>`
      const htmlSuffix = '</div>'
      return new LayerPopup({
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
    let content = ''
    if (tooltip.customContent) {
      content = tooltip.customContent
      showFields.forEach(field => {
        content = content.replace(`\${${field.split('@')[1]}}`, fieldData[field.split('@')[0]])
      })
    } else {
      showFields.forEach(field => {
        //const value = ${fieldData[field.split('@')[0]] as string
        content += `${field.split('@')[1]}: ${fieldData[field.split('@')[0]]}<br>`
      })
    }
    return content
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
            textAnchor: 'center',
            textOffset: [0, 0]
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

  protected setupOptions(chart: Chart, config: L7Config): L7Config {
    return flow(this.configEmptyDataStrategy, this.configLabel)(chart, config)
  }
}
