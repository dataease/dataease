import { AbstractChartView, ChartLibraryType, ChartRenderType } from '../../types'
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()
/**
 * 图片组图表
 */
export class PictureGroupView extends AbstractChartView {
  properties: EditorProperty[] = ['background-overall-component', 'border-style', 'threshold']
  propertyInner: EditorPropertyInner = {
    'background-overall-component': ['all'],
    'border-style': ['all'],
    threshold: ['tableThreshold']
  }
  axis: AxisType[] = ['xAxis', 'yAxis', 'filter']
  axisConfig: AxisConfig = {
    xAxis: {
      name: `${t('chart.dimension')}`,
      type: 'd'
    },
    yAxis: {
      name: `${t('chart.quota')}`,
      type: 'q'
    }
  }
  constructor() {
    super(ChartRenderType.CUSTOM, ChartLibraryType.PICTURE_GROUP, 'picture-group')
  }
}
