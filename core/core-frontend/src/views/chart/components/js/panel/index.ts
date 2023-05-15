import { AbstractChartView } from '@/views/chart/components/js/panel/types'

class ChartViewManager {
  private static CHART_VIEW_MAP = new Map<string, Map<string, AbstractChartView>>()

  public registerChartView(render: string, name: string, view: AbstractChartView) {
    if (ChartViewManager.CHART_VIEW_MAP.has(render)) {
      ChartViewManager.CHART_VIEW_MAP.get(render).set(name, view)
    } else {
      ChartViewManager.CHART_VIEW_MAP.set(render, new Map([[name, view]]))
    }
  }

  public getChartView(render: string, name: string): AbstractChartView {
    return ChartViewManager.CHART_VIEW_MAP.get(render).get(name)
  }
}
const isParent = (type: any, parentType: any) => {
  let _type = type
  while (_type) {
    if (_type === parentType) {
      return true
    }
    _type = _type.__proto__
  }
  return false
}

const chartViewManager = new ChartViewManager()
// 批量自动注册视图，只要是 AbstractChartView 的子类都初始化然后存起来
const charts = import.meta.glob('./charts/**/*.ts', { eager: true })
for (const chart in charts) {
  const chartModule = charts[chart]
  Object.getOwnPropertyNames(chartModule).forEach(prop => {
    if (isParent(chartModule[prop], AbstractChartView)) {
      const chartView = new chartModule[prop]() as AbstractChartView
      chartViewManager.registerChartView(chartView.render, chartView.name, chartView)
    }
  })
}
export default chartViewManager
