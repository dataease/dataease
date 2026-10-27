import { existsSync, readdirSync } from 'node:fs'
import { dirname, resolve } from 'node:path'
import { createRequire } from 'node:module'

const require = createRequire(import.meta.url)

// Vite 6 的 include 通配符与该 UI 包的 exports 组合不能可靠展开样式子入口，显式枚举已安装入口。
export const getElementPlusStyleDependencies = () => {
  const componentsDir = resolve(
    dirname(require.resolve('element-plus-secondary/package.json')),
    'es/components'
  )
  return readdirSync(componentsDir, { withFileTypes: true })
    .filter(
      entry =>
        entry.isDirectory() && existsSync(resolve(componentsDir, entry.name, 'style/css.mjs'))
    )
    .map(entry => `element-plus-secondary/es/components/${entry.name}/style/css`)
    .sort()
}

// 懒路由首次加载时才出现的依赖：提前优化，避免导航未提交时触发整页刷新。
export const lazyRouteDevDependencies = [
  'html-to-image',
  'vuedraggable',
  '@videojs-player/vue',
  'flv-h265',
  'dayjs/locale/zh-cn',
  'dayjs/plugin/quarterOfYear',
  'dayjs/plugin/updateLocale',
  'jquery',
  'vant/es/popup',
  'vant/es/date-picker',
  'vant/es/time-picker',
  'vant/es/picker-group',
  'vant/es/popup/style',
  'vant/es/date-picker/style',
  'vant/es/picker-group/style',
  'vant/es/time-picker/style',
  'tinymce/tinymce',
  '@tinymce/tinymce-vue',
  'tinymce/models/dom',
  'tinymce/themes/silver',
  'tinymce/icons/default',
  'tinymce-i18n/langs6/zh-Hans.js',
  'tinymce/plugins/code',
  'tinymce/themes/silver/theme',
  'tinymce/plugins/advlist',
  'tinymce/plugins/autolink',
  'tinymce/plugins/link',
  'tinymce/plugins/image',
  'tinymce/plugins/lists',
  'tinymce/plugins/charmap',
  'tinymce/plugins/media',
  'tinymce/plugins/wordcount',
  'tinymce/plugins/table',
  'tinymce/plugins/directionality',
  'tinymce/plugins/nonbreaking',
  'tinymce/plugins/pagebreak',
  // 数据集切换到数据源时加载的 SQL 编辑器和加密等依赖。
  'vue-uuid',
  'jsencrypt/bin/jsencrypt.min',
  'mathjs',
  'vue3-ace-editor',
  'ace-builds',
  'ace-builds/src-noconflict/ext-language_tools'
]
