import { AbstractChartView, ChartLibraryType, ChartRenderType } from '../../types'
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()
/**
 * 富文本图表
 */
export class RichTextChartView extends AbstractChartView {
  properties: EditorProperty[] = [
    'background-overall-component',
    'border-style',
    'threshold',
    'function-cfg'
  ]
  propertyInner: EditorPropertyInner = {
    'background-overall-component': ['all'],
    'border-style': ['all'],
    threshold: ['tableThreshold'],
    'function-cfg': ['emptyDataStrategy']
  }
  axis: AxisType[] = ['xAxis', 'yAxis', 'filter']
  axisConfig: AxisConfig = {
    xAxis: {
      name: `${t('chart.dimension')}`,
      type: 'd',
      allowEmpty: true
    },
    yAxis: {
      name: `${t('chart.quota')}`,
      type: 'q',
      allowEmpty: true
    }
  }
  constructor() {
    super(ChartRenderType.CUSTOM, ChartLibraryType.RICH_TEXT, 'rich-text')
  }
}
