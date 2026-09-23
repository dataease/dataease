import { Chart } from '@antv/g2'
import { CircularLabelOverflow } from '../../src/views/chart/components/js/panel/charts/g2/relation/pie/text-overflow'

let charts: Chart[] = []
export async function render({
  count = 40,
  width = 390,
  height = 260,
  fontSize = 12,
  fullDisplay = true,
  rose = false,
  position = 'right'
} = {}) {
  charts.forEach(chart => chart.destroy())
  const data = Array.from({ length: count }, (_, i) => ({
    field: `上海人身险平台数据治理部门${i + 1}`,
    value: i + 5
  }))
  document.getElementById('names').textContent = data.map(d => d.field).join(' / ')
  charts = ['before', 'after'].map((id, index) => {
    const chart = new Chart({ container: id, width, height })
    chart.options({
      type: 'interval',
      data,
      animate: false,
      transform: rose ? [] : [{ type: 'stackY' }],
      encode: { color: 'field', y: 'value', ...(rose ? { x: 'field' } : {}) },
      coordinate: { type: rose ? 'polar' : 'theta', innerRadius: 0.55, outerRadius: 0.9 },
      legend: { color: { position } },
      labels: [
        {
          text: 'field',
          position: fullDisplay || rose ? 'outside' : 'spider',
          fontSize,
          transform: index
            ? [{ type: CircularLabelOverflow, fullDisplay }]
            : fullDisplay
            ? []
            : [{ type: 'exceedAdjust' }]
        }
      ]
    } as any)
    return chart
  })
  await Promise.all(charts.map(chart => chart.render()))
  Object.assign(window, { charts })
}
Object.assign(window, { renderCase: render })
document.querySelectorAll('input').forEach(input =>
  input.addEventListener('change', () =>
    render({
      count: Number((document.getElementById('count') as HTMLInputElement).value),
      fontSize: Number((document.getElementById('font') as HTMLInputElement).value),
      fullDisplay: (document.getElementById('full') as HTMLInputElement).checked
    })
  )
)
render()
