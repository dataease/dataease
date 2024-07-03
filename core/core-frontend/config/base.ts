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
    rollupOptions: {
      output: {
        // 用于命名代码拆分时创建的共享块的输出命名
        chunkFileNames: `assets/chunk/[name]-${pkg.version}-${pkg.name}.js`,
        assetFileNames: `assets/[ext]/[name]-${pkg.version}-${pkg.name}.[ext]`,
        entryFileNames: `js/[name]-${pkg.version}-${pkg.name}.js`,
        manualChunks(id: string) {
          if (id.includes('node_modules')) {
            return id.toString().split('node_modules/')[1].split('/')[0].toString()
          }
        }
      }
    },
    sourcemap: false
  }
}
