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
        dir: 'lib',
        entryFileNames: '[name].mjs',
        // preserveModules: true,
        exports: 'named'
      }
    },
    lib: {
      entry: pathResolve('src/pages/lib/main.ts'),
      formats: ['es']
    }
  }
}
