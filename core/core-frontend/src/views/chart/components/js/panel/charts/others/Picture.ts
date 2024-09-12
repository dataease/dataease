import { AbstractChartView, ChartLibraryType, ChartRenderType } from '../../types'
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()
/**
 * 富文本图表
 */
export class PictureChartView extends AbstractChartView {
  properties: EditorProperty[] = ['background-overall-component', 'border-style']
  propertyInner: EditorPropertyInner = {
    'background-overall-component': ['all'],
    'border-style': ['all']
  }
  axis: AxisType[] = ['filter']
  axisConfig: AxisConfig = {}
  constructor() {
    super(ChartRenderType.CUSTOM, ChartLibraryType.PICTURE, 'Picture')
  }
}
