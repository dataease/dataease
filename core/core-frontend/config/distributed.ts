import pkg from '../package.json'
import viteCompression from 'vite-plugin-compression'

export default {
  plugins: [
    viteCompression({
      // gzip静态资源压缩配置
      verbose: true, // 是否在控制台输出压缩结果
      disable: false, // 是否禁用压缩
      threshold: 10240, // 启用压缩的文件大小限制
      algorithm: 'gzip', // 采用的压缩算法
      ext: '.gz' // 生成的压缩包后缀
    })
  ],
  build: {
    cssCodeSplit: false,
    rollupOptions: {
      external: id => /de-xpack/.test(id) || /extensions/.test(id),
      output: {
        // 用于命名代码拆分时创建的共享块的输出命名
        chunkFileNames: `assets/chunk/[name]-${pkg.version}-${pkg.name}.js`,
        assetFileNames: `assets/[ext]/[name]-${pkg.version}-${pkg.name}.[ext]`,
        entryFileNames: `js/[name]-${pkg.version}-${pkg.name}.js`,
        manualChunks: {
          echarts: ['echarts'],
          vue: ['vue', 'vue-router', 'pinia', 'vue-i18n', 'mitt'],
          lodash: ['lodash-es', 'lodash'],
          library: ['jspdf', '@tinymce/tinymce-vue', 'screenfull'],
          antv: ['@antv/g2', '@antv/g2plot', '@antv/l7', '@antv/l7plot', '@antv/s2'],
          tinymce: ['tinymce'],
          axios: ['axios'],
          'vuedraggable-es': ['vuedraggable']
        }
      }
    },
    modulePreload: {
      resolveDependencies() {
        return []
      }
    },
    sourcemap: false
  }
}
