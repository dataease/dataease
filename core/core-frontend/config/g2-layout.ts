import { createHash } from 'node:crypto'
import { readFileSync } from 'node:fs'
import path from 'path'
import type { Plugin as EsbuildPlugin } from 'esbuild'
import type { Plugin as VitePlugin } from 'vite'

const G2_RUNTIME_PLOT = '/@antv/g2/esm/runtime/plot.js'
const G2_LAYOUT_IMPORT = './layout'
const G2_LAYOUT_ADAPTER = 'src/views/chart/components/js/panel/types/impl/g2-layout.ts'
const G2_PACKAGE_NAME = '@antv/g2'

/**
 * 最近一次完整通过 DataEase 图表回归验证的 G2 版本
 *
 * package.json 保留小版本自动升级能力，生产构建只接受这里登记的已验证版本
 * 验证新版本通过后更新此值，后续自动升级到其他版本时会再次触发构建保护
 */
export const VERIFIED_G2_VERSION = '5.4.8'

type G2VersionStatus = {
  declaredVersion: string
  installedVersion: string
}

const G2_VERSION_VALIDATION_ITEMS = [
  '普通柱状图、横向柱状图、折线图、面积图和散点图的轴标签、刻度线、网格线',
  '长轴标签的首尾裁剪、旋转、自动抽稀，以及小尺寸画布和容器缩放',
  '顶部、底部、左侧、右侧图例的长文字省略、悬浮完整提示和分页',
  '组合图的左右轴、左右侧图例、图例分页、显示隐藏和图表缩放',
  '对称条形图横向和纵向布局、中轴左侧/中间/上方、长文字和数值轴越界',
  '图表联动、下钻、跳转、导出图片、移动端缩放和仪表板隐藏组件'
]

const readJsonFile = (file: string) => JSON.parse(readFileSync(file, 'utf8'))

/**
 * 同时读取依赖声明和 node_modules 中真正参与本次构建的 G2 版本
 * package-lock.json 可能被 Maven clean 删除，不能把它作为唯一判断来源
 */
const getG2VersionStatus = (root: string): G2VersionStatus => {
  const projectPackage = readJsonFile(path.resolve(root, 'package.json'))
  const installedPackage = readJsonFile(
    path.resolve(root, 'node_modules', '@antv', 'g2', 'package.json')
  )
  return {
    declaredVersion: projectPackage.dependencies?.[G2_PACKAGE_NAME] ?? '未声明',
    installedVersion: installedPackage.version ?? '未知'
  }
}

const getG2VersionMismatchMessage = (status: G2VersionStatus, productionBuild: boolean) => {
  const updateVersionStep = G2_VERSION_VALIDATION_ITEMS.length + 1
  const rebuildStep = updateVersionStep + 1
  return `
[DataEase G2 版本验证]
${productionBuild ? '生产构建已停止' : '当前为开发模式，允许启动以完成新版本验证'}

package.json 声明版本：${status.declaredVersion}
本次实际安装版本：${status.installedVersion}
DataEase 已验证版本：${VERIFIED_G2_VERSION}

g2-layout.ts 使用了 G2 运行时布局能力，未验证的小版本也可能改变轴、图例或多 View 布局

如果本次不准备验证新版本：
1. 执行 npm install --save-exact ${G2_PACKAGE_NAME}@${VERIFIED_G2_VERSION}
2. 重新安装依赖并执行构建

如果准备验证 ${status.installedVersion}：
${G2_VERSION_VALIDATION_ITEMS.map((item, index) => `${index + 1}. ${item}`).join('\n')}
${updateVersionStep}. 确认上述内容通过后，将 config/g2-layout.ts 中 VERIFIED_G2_VERSION 更新为 ${
    status.installedVersion
  }
${rebuildStep}. 重新执行生产构建，并提交版本记录与对应的 package-lock.json
`
}

/**
 * 开发环境只提醒，便于直接启动新版本完成回归验证
 * 生产构建必须使用已验证版本，避免 npm install 自动升级后静默生成风险产物
 */
const verifyG2Version = (
  root: string,
  command: 'build' | 'serve',
  logger: { info: (message: string) => void; warn: (message: string) => void }
) => {
  let status: G2VersionStatus
  try {
    status = getG2VersionStatus(root)
  } catch (error) {
    throw new Error(
      `[DataEase G2 版本验证] 无法读取 @antv/g2 的实际安装版本，请先执行 npm install\n${error}`
    )
  }
  const productionBuild = command === 'build'
  if (status.installedVersion !== VERIFIED_G2_VERSION) {
    const message = getG2VersionMismatchMessage(status, productionBuild)
    if (productionBuild) {
      throw new Error(message)
    }
    logger.warn(message)
    return
  }
  if (productionBuild) {
    logger.info(
      `[DataEase G2 版本验证] 通过：实际安装 ${status.installedVersion}，已验证 ${VERIFIED_G2_VERSION}，package.json 声明 ${status.declaredVersion}`
    )
  }
}

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
      verifyG2Version(root, config.command, config.logger)
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
