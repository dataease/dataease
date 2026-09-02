import pkg from '../package.json'
import viteCompression from 'vite-plugin-compression'

export default {
  plugins: [
    viteCompression({
      // gzip静态资源压缩配置
      disable: false, // 是否禁用压缩
      threshold: 10240, // 启用压缩的文件大小限制
      algorithm: 'gzip', // 采用的压缩算法
      ext: '.gz' // 生成的压缩包后缀
    })
  ],
  build: {
    sourcemap: false
  }
}
