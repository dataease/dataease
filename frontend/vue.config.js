'use strict'
const path = require('path')
const defaultSettings = require('./src/settings.js')

function resolve(dir) {
  return path.join(__dirname, dir)
}

const name = defaultSettings.title || 'vue Admin Template' // page title

const port = process.env.port || process.env.npm_config_port || 9528 // dev port
module.exports = {
  productionSourceMap: true,
  // 使用mock-server
  devServer: {
    port: port,
    open: true,
    overlay: {
      warnings: false,
      errors: true
    },
    before: require('./mock/mock-server.js')
  },
  configureWebpack: {
    name: name,
    devtool: 'source-map',
    resolve: {
      alias: {
        '@': resolve('src')
      }
    }
  },
  chainWebpack: config => {
    config.module.rules.delete('svg') // 删除默认配置中处理svg,
    // const svgRule = config.module.rule('svg')
    // svgRule.uses.clear()
    config.module
      .rule('svg-sprite-loader')
      .test(/\.svg$/)
      .include
      .add(resolve('src/icons')) // 处理svg目录
      .end()
      .use('svg-sprite-loader')
      .loader('svg-sprite-loader')
      .options({
        symbolId: 'icon-[name]'
      })
  }

}
// module.exports = {

//   publicPath: '/',
//   outputDir: 'dist',
//   assetsDir: 'static',
//   lintOnSave: process.env.NODE_ENV === 'development',
//   productionSourceMap: false,
//   css: {
//     sourceMap: true // 开启 CSS source maps
//   },
//   devServer: {
//     port: port,
//     open: true,
//     overlay: {
//       warnings: false,
//       errors: true
//     },
//     before: require('./mock/mock-server.js')
//   },
//   configureWebpack: {

//     name: name,
//     resolve: {
//       alias: {
//         '@': resolve('src')
//       }
//     }
//   },
//   chainWebpack(config) {
//     config.plugins.delete('preload') // TODO: need test
//     config.plugins.delete('prefetch') // TODO: need test

//     config.module
//       .rule('svg')
//       .exclude.add(resolve('src/icons'))
//       .end()
//     config.module
//       .rule('icons')
//       .test(/\.svg$/)
//       .include.add(resolve('src/icons'))
//       .end()
//       .use('svg-sprite-loader')
//       .loader('svg-sprite-loader')
//       .options({
//         symbolId: 'icon-[name]'
//       })
//       .end()

//     config.module
//       .rule('vue')
//       .use('vue-loader')
//       .loader('vue-loader')
//       .tap(options => {
//         options.compilerOptions.preserveWhitespace = true
//         return options
//       })
//       .end()

//     config
//       .when(process.env.NODE_ENV === 'development',
//         config => config.devtool('source-map')
//       )

//     config
//       .when(process.env.NODE_ENV !== 'development',
//         config => {
//           config
//             .plugin('ScriptExtHtmlWebpackPlugin')
//             .after('html')
//             .use('script-ext-html-webpack-plugin', [{
//               inline: /runtime\..*\.js$/
//             }])
//             .end()
//           config
//             .optimization.splitChunks({
//               chunks: 'all',
//               cacheGroups: {
//                 libs: {
//                   name: 'chunk-libs',
//                   test: /[\\/]node_modules[\\/]/,
//                   priority: 10,
//                   chunks: 'initial' // only package third parties that are initially dependent
//                 },
//                 elementUI: {
//                   name: 'chunk-elementUI', // split elementUI into a single package
//                   priority: 20, // the weight needs to be larger than libs and app or it will be packaged into libs or app
//                   test: /[\\/]node_modules[\\/]_?element-ui(.*)/ // in order to adapt to cnpm
//                 },
//                 commons: {
//                   name: 'chunk-commons',
//                   test: resolve('src/components'), // can customize your rules
//                   minChunks: 3, //  minimum common number
//                   priority: 5,
//                   reuseExistingChunk: true
//                 }
//               }
//             })
//           config.optimization.runtimeChunk('single')
//         }
//       )
//   }
// }
