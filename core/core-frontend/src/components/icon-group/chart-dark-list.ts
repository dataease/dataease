import richTextDark from '@/assets/svg/rich-text-dark.svg'
import chartMixDualLineDark from '@/assets/svg/empty-dark/icon_line_light.svg'
import { defineAsyncComponent } from 'vue'
import { iconChartMapEmpty } from './chart-dark-list-empty'
const svgs = import.meta.glob('@/assets/svg/chart-dark/*.svg')

const iconChartDarkMap = {
  'chart-mix-dual-line-dark': chartMixDualLineDark,
  'rich-text-dark': richTextDark
}
Object.keys(svgs).forEach(path => {
  const name = path.match(/\/assets\/svg\/chart-dark\/([^/]+)\.svg$/)[1]
  iconChartDarkMap[iconChartMapEmpty[name]] = defineAsyncComponent(svgs[path])
})

export { iconChartDarkMap }
