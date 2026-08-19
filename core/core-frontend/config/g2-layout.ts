import path from 'path'
import type { Plugin as EsbuildPlugin } from 'esbuild'
import type { Plugin as VitePlugin } from 'vite'

const G2_RUNTIME_PLOT = '/@antv/g2/esm/runtime/plot.js'
const G2_LAYOUT_IMPORT = './layout'
const G2_LAYOUT_ADAPTER = 'src/views/chart/components/js/panel/types/impl/g2-layout.ts'

const normalizeModuleId = (id = '') => id.replaceAll('\\', '/').split('?')[0]
const isG2RuntimePlot = (id = '') => normalizeModuleId(id).endsWith(G2_RUNTIME_PLOT)
const isG2PackageModule = (id = '') => normalizeModuleId(id).includes('/node_modules/@antv/g2/')
const isG2LayoutImport = (source: string, importer = '') =>
  source === G2_LAYOUT_IMPORT && isG2RuntimePlot(importer)
const resolveG2LayoutAdapter = (root: string) => path.resolve(root, G2_LAYOUT_ADAPTER)

/**
 * 仅替换 G2 runtime/plot 对同目录 layout 的引用，避免影响项目内其他同名模块
 * 风险：升级 G2 后若内部文件路径或导出发生变化，需要同步检查该精确匹配
 */
export const createG2LayoutPlugin = (root: string): VitePlugin => {
  const adapter = resolveG2LayoutAdapter(root)
  let productionBuild = false
  let g2Included = false
  let redirectVerified = false
  return {
    name: 'dataease-g2-layout',
    enforce: 'pre',
    configResolved(config) {
      productionBuild = config.command === 'build'
    },
    resolveId(source, importer) {
      if (isG2LayoutImport(source, importer)) {
        return adapter
      }
    },
    transform(_, id) {
      if (productionBuild && isG2PackageModule(id)) {
        g2Included = true
      }
    },
    moduleParsed({ id, importedIds }) {
      if (!productionBuild || !isG2RuntimePlot(id)) {
        return
      }
      const adapterId = normalizeModuleId(adapter)
      const redirected = importedIds.some(importedId => normalizeModuleId(importedId) === adapterId)
      if (!redirected) {
        // 防止升级 G2 后内部入口变化却生成未启用边界校正的生产包
        this.error('G2 layout 重定向未生效，请检查 @antv/g2 runtime/plot 的 layout 导入')
      }
      redirectVerified = true
    },
    buildEnd(error) {
      if (!error && productionBuild && g2Included && !redirectVerified) {
        // G2 整体调整 runtime 路径时同样阻止静默失效
        this.error('未找到可验证的 G2 layout 重定向，请检查 @antv/g2 的内部目录结构')
      }
    }
  }
}

/**
 * Vite 开发环境会预构建 G2，需与生产构建使用相同的精确重定向规则
 */
export const createG2LayoutOptimizerPlugin = (root: string): EsbuildPlugin => {
  const adapter = resolveG2LayoutAdapter(root)
  return {
    name: 'dataease-g2-layout',
    setup(build) {
      build.onResolve({ filter: /^\.\/layout$/ }, args => {
        if (isG2LayoutImport(args.path, args.importer)) {
          return { path: adapter }
        }
      })
    }
  }
}
