import type { Chart as G2Chart } from '@antv/g2'
import { parseJson } from '../../../util'

/** 指标通过 ID 绑定，图例通过实际数据的 category 映射，兼容别名和子维度。 */
export const createAssistLineVisibility = (chart: Chart) => {
  const lines: AssistLine[] = parseJson(chart.senior)?.assistLineCfg?.assistLine ?? []
  const series = new Map<string, Set<string>>()
  const collect = (data: Record<string, any>[], fields: Axis[], axis: string) => {
    data?.forEach(row => {
      if (Array.isArray(row.data)) collect(row.data, fields, axis)
      if (row.category === undefined || row.category === null) return
      const quotas = row.quotaList?.length ? row.quotaList : fields?.length === 1 ? fields : []
      quotas?.forEach(field => {
        const id = `${axis}:${field.id}`
        if (!series.has(id)) series.set(id, new Set())
        series.get(id).add(String(row.category))
      })
    })
  }
  collect(chart.data?.data, chart.yAxis, 'left')
  collect(chart.data?.left?.data, chart.yAxis, 'left')
  collect(chart.data?.right?.data, chart.yAxisExt, 'right')
  const fields = new Set([
    ...(chart.yAxis ?? []).map(f => `left:${f.id}`),
    ...(chart.yAxisExt ?? []).map(f => `right:${f.id}`)
  ])
  let selected: Set<string> | undefined
  return {
    select(values?: unknown[]) {
      selected = values === undefined ? undefined : new Set(values.map(String))
    },
    visible(datum: AssistLine) {
      // 动态计算响应不必携带新配置；显隐始终从持久化的辅助线配置读取。
      const line =
        datum.field === '0'
          ? datum
          : lines.find(
              item =>
                item.field === datum.field &&
                item.name === datum.name &&
                item.color === datum.color &&
                item.lineType === datum.lineType &&
                item.fieldId === datum.fieldId &&
                (item.yAxisType ?? 'left') === (datum.yAxisType ?? 'left') &&
                (item.field === '0' || item.summary === datum.summary)
            )
      if (!line?.followField) return true
      const id = line.field === '1' ? line.fieldId : line.boundFieldId
      if (!id) return true
      // 已绑定的指标被移除后不将其阈值误用于其它指标；用户可清空绑定恢复常显。
      if (!fields.has(`${line.yAxisType ?? 'left'}:${id}`)) return false
      if (!selected) return true
      const categories = series.get(`${line.yAxisType ?? 'left'}:${id}`)
      // 无法可靠映射时不使用名称猜测，避免隐藏无关辅助线。
      return !categories?.size || [...categories].some(category => selected.has(category))
    }
  }
}

/** 在首次 render 前接入，与原有轴域裁剪共同决定辅助线及其标签的可见性。 */
export const installG2AssistLineVisibility = (instance: G2Chart, chart: Chart) => {
  const lines: AssistLine[] = parseJson(chart.senior)?.assistLineCfg?.assistLine ?? []
  if (!lines.some(line => line.followField)) return () => undefined
  const state = createAssistLineVisibility(chart)
  const visit = spec => {
    if (spec.type === 'lineY' && Array.isArray(spec.data) && spec.data[0]?.name !== undefined) {
      const previous = spec.style?.visibility
      spec.style = {
        ...spec.style,
        visibility: (datum, ...args) =>
          state.visible(datum)
            ? typeof previous === 'function'
              ? previous(datum, ...args)
              : previous ?? 'visible'
            : 'hidden'
      }
      spec.animate = { ...spec.animate, update: { type: null } }
    }
    if (Array.isArray(spec.children)) spec.children.forEach(visit)
  }
  const options = instance.options()
  visit(options)
  instance.options(options)
  const filter = event => {
    if (event?.data?.channel !== 'color' || !Array.isArray(event.data.values)) return
    state.select(event.data.values)
    // G2 原生图例在完成筛选后才发布事件；重放一次筛选使辅助线使用最新状态。
    // 平铺图例的程序化事件发生在筛选前，无需重放。
    if (event.nativeEvent) instance.emit('legend:filter', { data: event.data })
  }
  const focus = event => {
    if (event?.data?.channel !== 'color') return
    state.select([event.data.value])
    if (event.nativeEvent) instance.emit('legend:focus', { data: event.data })
  }
  const reset = event => {
    state.select()
    if (event?.nativeEvent) instance.emit('legend:reset', {})
  }
  instance.on('legend:filter', filter)
  instance.on('legend:focus', focus)
  instance.on('legend:reset', reset)
  return () => {
    instance.off('legend:filter', filter)
    instance.off('legend:focus', focus)
    instance.off('legend:reset', reset)
  }
}
