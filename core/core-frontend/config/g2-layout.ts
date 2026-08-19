import { createHash } from 'node:crypto'
import { readFileSync } from 'node:fs'
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
 * 将布局适配器的内容摘要放进 Vite 与 esbuild 插件名
 * Vite optimizeDeps 只把插件名写入缓存键，适配器源码变化本身不会触发重新预构建
 * 适配器每次变化都会在下次启动时重建 G2 依赖，增加一次开发环境启动耗时
 */
const getG2LayoutPluginName = (adapter: string) => {
  const version = createHash('sha256').update(readFileSync(adapter)).digest('hex').slice(0, 8)
  return `dataease-g2-layout-${version}`
}

/**
 * 仅将 G2 runtime/plot 对同目录 layout 的引用替换为 DataEase 布局适配器
 * 轴标签边界必须在 G2 最终绘制前参与布局，业务层渲染后修正会产生二次渲染闪动
 * 升级 G2 后若内部文件路径或导出发生变化，需要同步检查该精确匹配
 */
export const createG2LayoutPlugin = (root: string): VitePlugin => {
  const adapter = resolveG2LayoutAdapter(root)
  const name = getG2LayoutPluginName(adapter)
  let productionBuild = false
  let g2Included = false
  let redirectVerified = false
  return {
    name,
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
 * 让开发环境依赖预构建使用与生产构建相同的 G2 layout 重定向
 * 开发环境中的 G2 会先被 esbuild 打包，普通 Vite resolveId 无法覆盖其内部导入
 * esbuild 或 G2 调整内部解析入口时，此处规则可能失效并需要同步适配
 */
export const createG2LayoutOptimizerPlugin = (root: string): EsbuildPlugin => {
  const adapter = resolveG2LayoutAdapter(root)
  return {
    name: getG2LayoutPluginName(adapter),
    setup(build) {
      build.onResolve({ filter: /^\.\/layout$/ }, args => {
        if (isG2LayoutImport(args.path, args.importer)) {
          return { path: adapter }
        }
      })
    }
  }
}
