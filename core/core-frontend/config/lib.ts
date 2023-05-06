import { pathResolve } from './common'
export default {
  build: {
    //压缩
    sourcemap: false,
    minify: false,
    rollupOptions: {
      //忽略打包vue文件
      external: ['vue'],
      output: {
        globals: {
          vue: 'Vue'
        },
        dir: 'lib'
      }
    },
    lib: {
      entry: pathResolve('src/pages/lib/main.ts'),
      name: 'index',
      fileName: 'index',
      formats: ['es']
    }
  }
}
