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
import { queryMapKeyApi } from '@/api/setting/sysParameter'
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
    'tooltip-selector',
    'jump-set',
    'linkage'
  ]
  propertyInner: EditorPropertyInner = {
    ...MAP_EDITOR_PROPERTY_INNER,
    'basic-style-selector': ['colors', 'alpha', 'mapBaseStyle', 'symbolicMapStyle', 'zoom'],
    'label-selector': ['color', 'fontSize', 'showFields', 'customContent'],
    'tooltip-selector': ['color', 'fontSize', 'showFields', 'customContent', 'show']
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
      limit: 1
    }
  }
  constructor() {
    super('symbolic-map', [])
  }

  async drawChart(drawOption: L7DrawConfig<L7Config>) {
    const { chart, container, action } = drawOption
    const xAxis = deepCopy(chart.xAxis)
    const xAxisExt = deepCopy(chart.xAxisExt)
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
    if (xAxis?.length < 2 || xAxisExt?.length < 1) {
      return new L7Wrapper(scene, undefined)
    }
    const configList: L7Config[] = []
    const symbolicLayer = this.buildSymbolicLayer(chart, basicStyle)
    configList.push(symbolicLayer)
    const tooltipLayer = this.buildTooltip(chart, symbolicLayer)
    if (tooltipLayer) {
      scene.addPopup(tooltipLayer)
    }
    this.buildLabel(chart, configList)
    this.configZoomButton(chart, scene)
    symbolicLayer.on('click', ev => {
      const data = ev.feature
      action({
        x: ev.x,
        y: ev.y,
        data: {
          data: {
            ...data,
            dimensionList: chart.data.data.filter(item => item.field === ev.feature.field)?.[0]
              ?.dimensionList,
            quotaList: chart.data.data.filter(item => item.field === ev.feature.field)?.[0]
              ?.quotaList
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
    const c = []
    colors.forEach(ele => {
      c.push(hexColorToRGBA(ele, alpha))
    })
    const sizeKey = extBubble.length > 0 ? extBubble[0].dataeaseName : ''
    const data = chart.data?.tableRow
      ? chart.data?.tableRow.map((item, index) => ({
          ...item,
          color: c[index % c.length],
          size: item[sizeKey] ? item[sizeKey] : mapSymbolSize,
          field:
            item[xAxis[0].dataeaseName] +
            '000\n' +
            item[xAxis[1].dataeaseName] +
            '000\n' +
            item[xAxisExt[0].dataeaseName],
          name: item[xAxisExt[0].dataeaseName]
        }))
      : []
    const color = xAxisExt && xAxisExt.length > 0 ? 'color' : c[0]
    const pointLayer = new PointLayer()
      .source(data, {
        parser: {
          type: 'json',
          x: xAxis[0].dataeaseName,
          y: xAxis[1].dataeaseName
        }
      })
      .shape(mapSymbol)
      .color(color)
      .style({
        stroke: {
          field: 'color'
        },
        strokeWidth: mapSymbolStrokeWidth,
        opacity: {
          field: (mapSymbolOpacity / 100) * 10
        }
      })
      .active(true)
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
  buildTooltip = (chart, pointLayer) => {
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

  getMapKey = async () => {
    const key = 'online-map-key'
    if (!localStorage.getItem(key)) {
      await queryMapKeyApi().then(res => localStorage.setItem(key, res.data))
    }
    return localStorage.getItem(key)
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    chart.customAttr.label = {
      ...chart.customAttr.label,
      show: false
    }
    chart.customAttr.basicStyle = {
      ...chart.customAttr.basicStyle,
      mapSymbolOpacity: 5
    }
    return chart
  }

  protected setupOptions(chart: Chart, config: L7Config): L7Config {
    return flow(this.configEmptyDataStrategy, this.configTooltip, this.configLabel)(chart, config)
  }
}
