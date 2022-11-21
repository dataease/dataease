'use strict'
const path = require('path')
const defaultSettings = require('./src/settings.js')

const CopyWebpackPlugin = require('copy-webpack-plugin')
// const CompressionPlugin = require('compression-webpack-plugin')
// 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

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
    proxy: {
      '^(?!/login)': {
        // target: 'http://ofmfa31n5roz.ngrok2.xiaomiqiu.cn/',

        // target: 'http://localhost:9528/',
        // target: 'http://ew5nffth3rvv.ngrok2.xiaomiqiu.cn/', 192.168.3.223
        // target: 'http://172.16.226.133/',
        // target: 'http://122.9.188.217:8082/',
        // target: 'http://183.194.64.166:10080/',
        // target: 'http://192.168.3.223/',
        // target: 'http://183.194.65.250:9091/',
        // http://183.194.64.166:9091/
         target: 'http://192.168.3.67/',
        // target: 'http://183.194.64.166:9091/',  // 服务器
        // target: 'http://192.168.3.223/',
        // target: 'http://192.168.3.67/',
        // target: 'http://ofmfa31n5roz.ngrok.xiaomiqiu123.top', // 张庆
        // target: 'http://183.194.64.166:9527',
        ws: false
      }
    },
    open: true,
    overlay: {
      warnings: false,
      errors: true
    },
    before: require('./mock/mock-server.js')
  },

  pages: {
    index: {
      entry: 'src/main.js',
      template: 'public/index.html',
      filename: 'index.html'
    }
  },
  configureWebpack: {
    name: name,
    devtool: 'source-map',
    resolve: {
      alias: {
        '@': resolve('src')
      }
    },
    plugins: [
      new CopyWebpackPlugin([
        {
          from: path.join(__dirname, 'static'),
          to: path.join(__dirname, 'dist/static')
        }
      ])
    ]
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
    if (process.env.NODE_ENV === 'production') {
      /* config.plugin('compressionPlugin').use(new CompressionPlugin({
        test: /\.(js|css|less)$/, // 匹配文件名
        threshold: 10240, // 对超过10k的数据压缩
        minRatio: 0.8,
        deleteOriginalAssets: true // 删除源文件
      })) */
    }
  },
  css: {
    loaderOptions: {
      sass: {
        prependData: `@import "@/style/index.scss"`
      }
    }
  }

}
