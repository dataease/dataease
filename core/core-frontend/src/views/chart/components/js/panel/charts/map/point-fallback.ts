import type { ChoroplethOptions } from '@antv/l7plot/dist/esm/plots/choropleth'
import type { FeatureCollection } from '@antv/l7plot/dist/esm/plots/choropleth/types'
import type { DotOptions } from '@antv/l7plot'
import { Dot } from '@antv/l7plot'
import { TextLayer } from '@antv/l7plot/dist/esm'
import { isEmpty } from 'lodash-es'
import { hexColorToRGBA, parseJson } from '@/views/chart/components/js/util'
import { mapRendered, mapRendering } from '@/views/chart/components/js/panel/common/common_antv'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { configCarouselTooltip } from '@/views/chart/components/js/panel/charts/map/tooltip-carousel'
import type { L7PlotDrawOptions } from '@/views/chart/components/js/panel/types/impl/l7plot'
import type { Choropleth } from '@antv/l7plot/dist/esm/plots/choropleth'
import type { MapMouseEvent } from '@/views/chart/components/js/panel/charts/map/common'

/**
 * 差异配置
 */
export interface PointFallbackConfig {
  dotSize: DotOptions['size']
  dotColor: DotOptions['color']
  dotName: string
  /** 气泡动画配置，普通地图不传 */
  animate?: DotOptions['animate']
  dotShape?: DotOptions['shape']
  /** 是否禁用所有地图交互 */
  disableInteraction: boolean
  /** 自定义 Choropleth options 回调，用于注入图例等 */
  customizeChoroplethOptions?: (
    options: ChoroplethOptions,
    chart: Chart,
    dotData: any[]
  ) => ChoroplethOptions
}

export function isPointOnlyGeoJson(geoJson: FeatureCollection): boolean {
  return (
    geoJson?.features?.length > 0 &&
    geoJson.features.every(f => f.geometry?.type === 'Point' || f.geometry?.type === 'MultiPoint')
  )
}

/**
 * 村级 GeoJSON 的散点降级渲染逻辑
 * 当 GeoJSON 只包含 Point/MultiPoint 时，Choropleth 无法做区域填充，
 * 改用 Dot 散点来渲染。
 *
 * 通过 PointFallbackConfig 传入各自的样式差异和交互配置
 */
export async function drawPointFallbackChart(
  drawOption: L7PlotDrawOptions<Choropleth>,
  chart: Chart,
  geoJson: FeatureCollection,
  sourceData: any[],
  action: any,
  config: PointFallbackConfig
): Promise<Choropleth> {
  const { level, container } = drawOption
  const { basicStyle, label, tooltip } = parseJson(chart.customAttr)

  const dotData: any[] = []
  const labelData: any[] = []
  const areaMap = sourceData.reduce((obj, value) => {
    obj[value['field']] = { value: value.value, data: value }
    return obj
  }, {})

  const rawPoints: { x: number; y: number; name: string; matched: any; props: any }[] = []
  geoJson.features.forEach(item => {
    const props = item.properties
    const name = props['name'] || props['cun'] || props['fullname'] || ''
    const coords = item.geometry.coordinates || props['center'] || props['centroid']
    if (!name || !coords || coords.length < 2) {
      return
    }
    rawPoints.push({
      x: coords[0],
      y: coords[1],
      name,
      matched: areaMap[name],
      props
    })
  })

  // 对坐标完全相同的点做圆形散开偏移，避免视觉重叠
  const coordGroups: Record<string, number[]> = {}
  rawPoints.forEach((pt, idx) => {
    const key = `${pt.x},${pt.y}`
    if (!coordGroups[key]) coordGroups[key] = []
    coordGroups[key].push(idx)
  })
  const jitterRadius = 0.003
  for (const key in coordGroups) {
    const indices = coordGroups[key]
    if (indices.length <= 1) continue
    const n = indices.length
    indices.forEach((idx, i) => {
      const angle = (2 * Math.PI * i) / n
      rawPoints[idx].x += jitterRadius * Math.cos(angle)
      rawPoints[idx].y += jitterRadius * Math.sin(angle)
    })
  }

  rawPoints.forEach(pt => {
    dotData.push({
      x: pt.x,
      y: pt.y,
      name: pt.name,
      size: pt.matched?.value ?? 1,
      hasData: !!pt.matched,
      properties: {
        ...pt.props,
        name: pt.name,
        ...(pt.matched?.data || {})
      }
    })
    const content: string[] = []
    if (label.show) {
      if (label.showDimension) {
        content.push(pt.name)
      }
      if (label.showQuota && (pt.matched?.value || pt.matched?.value === 0)) {
        content.push(valueFormatter(pt.matched.value, label.quotaLabelFormatter))
      }
    }
    labelData.push({
      x: pt.x,
      y: pt.y,
      name: content.join('\n\n') || pt.name
    })
  })

  // 从 Point 坐标构造包围盒 Polygon，让 Choropleth 有真实的 geometry 来初始化 Scene
  const lngs = dotData.map(d => d.x)
  const lats = dotData.map(d => d.y)
  let minLng = Math.min(...lngs)
  let maxLng = Math.max(...lngs)
  let minLat = Math.min(...lats)
  let maxLat = Math.max(...lats)
  const pad = 0.01
  if (maxLng - minLng < pad) {
    minLng -= pad
    maxLng += pad
  }
  if (maxLat - minLat < pad) {
    minLat -= pad
    maxLat += pad
  }
  const bboxGeoJson: FeatureCollection = {
    type: 'FeatureCollection',
    features: [
      {
        type: 'Feature',
        properties: { name: '__bbox__', adcode: '__bbox__' },
        geometry: {
          type: 'Polygon',
          coordinates: [
            [
              [minLng, minLat],
              [maxLng, minLat],
              [maxLng, maxLat],
              [minLng, maxLat],
              [minLng, minLat]
            ]
          ]
        }
      }
    ]
  }

  const legendSourceData = dotData
    .filter(d => d.hasData)
    .map(d => ({ name: d.name, value: d.size }))

  let options: ChoroplethOptions = {
    preserveDrawingBuffer: true,
    minZoom: -2,
    map: {
      type: 'mapbox',
      style: 'blank',
      minZoom: -2
    },
    geoArea: {
      type: 'geojson'
    },
    source: {
      data: legendSourceData,
      joinBy: {
        sourceField: 'name',
        geoField: 'name',
        geoData: bboxGeoJson
      }
    },
    viewLevel: {
      level,
      adcode: 'all'
    },
    autoFit: true,
    chinaBorder: false,
    color: {
      field: 'value',
      value: basicStyle.colors.map(c => hexColorToRGBA(c, basicStyle.alpha)),
      scale: {
        type: 'quantize',
        unknown: basicStyle.areaBaseColor
      }
    },
    style: {
      opacity: 0,
      lineWidth: 0,
      lineOpacity: 0
    },
    label: false,
    tooltip: false,
    legend: legendSourceData.length ? undefined : false,
    customFetchGeoData: () => null
  }

  // 让调用方注入图例等自定义配置
  if (config.customizeChoroplethOptions) {
    options = config.customizeChoroplethOptions(options, chart, dotData)
  }

  const { Choropleth: ChoroplethCtor } = await import('@antv/l7plot/dist/esm/plots/choropleth')
  const view = new ChoroplethCtor(container, options)

  const dotOptions: DotOptions = {
    source: {
      data: dotData,
      parser: { type: 'json', x: 'x', y: 'y' }
    },
    shape: config.dotShape ?? 'square',
    size: config.dotSize,
    color: config.dotColor,
    name: config.dotName,
    style: { opacity: 1 },
    state: {
      active: { color: 'rgba(30,90,255,1)' }
    },
    tooltip: {
      showComponent: tooltip.show
    }
  }

  if (config.animate) {
    dotOptions.animate = config.animate
  }

  // tooltip 格式化
  if (tooltip.show && dotOptions.tooltip) {
    const formatterMap = tooltip.seriesTooltipFormatter
      ?.filter(i => i.show)
      .reduce((pre, next) => {
        pre[next.id] = next
        return pre
      }, {}) as Record<string, SeriesFormatter>
    dotOptions.tooltip.customTitle = ({ name }) => name
    dotOptions.tooltip.customItems = originalItem => {
      const result: any[] = []
      if (isEmpty(formatterMap)) {
        return result
      }
      const head = originalItem.properties
      if (!head) {
        return result
      }
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
    dotOptions.tooltip.domStyles = {
      'l7plot-tooltip': {
        'background-color': tooltip.backgroundColor,
        'font-size': `${tooltip.fontSize}px`,
        'line-height': 1.6
      },
      'l7plot-tooltip__name': { color: tooltip.color },
      'l7plot-tooltip__value': { color: tooltip.color },
      'l7plot-tooltip__title': { color: tooltip.color }
    }
  }

  const dotLayer = new Dot(dotOptions)

  let textLayer: InstanceType<typeof TextLayer> | null = null
  if (label.show && labelData.length) {
    textLayer = new TextLayer({
      name: 'pointLabelLayer',
      source: {
        data: labelData,
        parser: { type: 'json', x: 'x', y: 'y' }
      },
      field: 'name',
      style: {
        fill: label.color,
        fontSize: label.fontSize,
        opacity: 1,
        fontWeight: 'bold',
        textAnchor: 'center',
        textOffset: [0, -20],
        textAllowOverlap: label.fullDisplay,
        padding: label.fullDisplay ? undefined : [2, 2]
      }
    })
  }

  mapRendering(container)
  view.once('loaded', () => {
    mapRendered(container)
    dotLayer.addToScene(view.scene)
    if (textLayer) {
      textLayer.addTo(view.scene)
    }
    const map = view.scene.map
    map['fitBounds']?.(
      [
        [minLng, minLat],
        [maxLng, maxLat]
      ],
      { padding: 40, animate: false }
    )
    if (config.disableInteraction) {
      map['dragPan']?.disable()
      map['scrollZoom']?.disable()
      map['doubleClickZoom']?.disable()
      map['dragRotate']?.disable()
      map['touchPitch']?.disable()
      map['touchZoomRotate']?.disable()
      map['zoomEnable']?.disable()
      map['dragEnable']?.disable()
    }
    map['keyboard']?.disable()
    dotLayer.on(`${config.dotName}:click`, (ev: MapMouseEvent) => {
      const evData = ev.feature.properties
      const adcode = evData.adcode
      action({
        x: ev.x,
        y: ev.y,
        data: {
          data: evData,
          extra: { adcode: adcode ? adcode + '' : '' }
        }
      })
    })
    chart.container = container
    // 轮播提示
    view.currentDistrictData = {
      type: 'FeatureCollection',
      features: geoJson.features.map(f => {
        const coords = f.geometry.coordinates || f.properties?.center || f.properties?.centroid
        return {
          ...f,
          properties: {
            ...f.properties,
            centroid: coords
          }
        }
      })
    }
    if (!view.source?.data) {
      view.source = { data: { dataArray: sourceData } }
    } else {
      view.source.data.dataArray = sourceData
    }
    if (!view.tooltip) {
      view.tooltip = {
        options: {
          domStyles: {
            'l7plot-tooltip': {
              'background-color': tooltip.backgroundColor,
              'font-size': `${tooltip.fontSize}px`,
              'line-height': 1.6
            },
            'l7plot-tooltip__name': { color: tooltip.color },
            'l7plot-tooltip__value': { color: tooltip.color },
            'l7plot-tooltip__title': { color: tooltip.color },
            'l7plot-tooltip__list': {}
          }
        }
      }
    }
    configCarouselTooltip(chart, view, sourceData, null, undefined, drawOption)
  })
  return view
}
